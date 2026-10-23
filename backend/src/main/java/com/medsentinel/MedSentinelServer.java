package com.medsentinel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.medsentinel.alert.*;
import com.medsentinel.auth.*;
import com.medsentinel.breakglass.BreakGlassRepository;
import com.medsentinel.breakglass.BreakGlassService;
import com.medsentinel.config.MongoDBManager;
import com.medsentinel.detection.*;
import com.medsentinel.logging.*;
import com.medsentinel.patient.*;
import com.medsentinel.rbac.*;
import com.medsentinel.scoring.ScoringService;
import com.medsentinel.security.ApiException;
import com.medsentinel.security.ApiSecurity;
import com.medsentinel.security.AuthenticationRequiredException;
import com.medsentinel.security.PublicUserDto;
import com.medsentinel.simulator.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Lightweight, pure Java HTTP server for MedSentinel.
 * Zero Spring Boot dependencies — persists directly to MongoDB.
 */
public class MedSentinelServer {

    private final int port;
    private HttpServer server;
    private final Gson gson;
    private static final int MAX_REQUEST_BODY_BYTES = readMaxRequestBodyBytes();

    // Services & Repositories
    private final MongoDBManager mongoManager;
    private final AccessLogRepository logRepo;
    private final LogChainService chainService;
    private final AccessLogService logService;
    private final AlertRepository alertRepo;
    private final AlertEvidenceRepository evidenceRepo;
    private final AlertService alertService;
    private final UserRepository userRepo;
    private final AuthService authService;
    private final PatientRepository patientRepo;
    private final RecordRepository recordRepo;
    private final CareAssignmentRepository careRepo;
    private final AccessGuard accessGuard;
    private final PatientService patientService;
    private final RoleRepository roleRepo;
    private final RoleBaselineRepository baselineRepo;
    private final RoleService roleService;
    private final BreakGlassRepository breakGlassRepo;
    private final BreakGlassService breakGlassService;
    private final DetectionEngine detectionEngine;
    private final ScenarioRunner scenarioRunner;
    private final DataSeeder dataSeeder;
    private final ApiSecurity apiSecurity;

    public MedSentinelServer(int port) {
        this.port = port;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class, (com.google.gson.JsonSerializer<LocalTime>) (src, typeOfSrc, context) ->
                new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class, (com.google.gson.JsonDeserializer<LocalTime>) (json, typeOfT, context) ->
                LocalTime.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

        // 1. Initialize MongoDB
        this.mongoManager = MongoDBManager.getInstance();
        this.apiSecurity = new ApiSecurity();

        // 2. Repositories
        this.logRepo = new AccessLogRepository(mongoManager);
        this.chainService = new LogChainService(logRepo);
        this.logService = new AccessLogService(logRepo, chainService);
        this.alertRepo = new AlertRepository(mongoManager);
        this.evidenceRepo = new AlertEvidenceRepository(mongoManager);
        this.alertService = new AlertService(alertRepo, evidenceRepo);
        this.userRepo = new UserRepository(mongoManager);
        this.authService = new AuthService(userRepo, logService);
        this.patientRepo = new PatientRepository(mongoManager);
        this.recordRepo = new RecordRepository(mongoManager);
        this.careRepo = new CareAssignmentRepository(mongoManager);
        this.accessGuard = new AccessGuard(careRepo, patientRepo);
        this.patientService = new PatientService(patientRepo, recordRepo, accessGuard, logService);
        this.roleRepo = new RoleRepository(mongoManager);
        this.baselineRepo = new RoleBaselineRepository(mongoManager);
        this.roleService = new RoleService(roleRepo, baselineRepo);
        this.breakGlassRepo = new BreakGlassRepository(mongoManager);
        this.breakGlassService = new BreakGlassService(breakGlassRepo, patientRepo, recordRepo, logService, alertService);

        // 3. Detection Engine & Rules
        List<DetectionRule> rules = List.of(
            new FailedLoginRule(logRepo),
            new BulkAccessRule(logRepo, userRepo, baselineRepo, accessGuard),
            new OutOfScopeRule(logRepo),
            new OffHoursRule(userRepo),
            new UnassignedPatientRule(userRepo, patientRepo, careRepo)
        );
        ScoringService scoringService = new ScoringService(alertService);
        this.detectionEngine = new DetectionEngine(rules, scoringService);
        this.logService.setDetectionEngine(detectionEngine);

        // 4. Simulator
        this.scenarioRunner = new ScenarioRunner(logService, alertService, userRepo, patientRepo, logRepo);
        this.dataSeeder = new DataSeeder(logRepo, chainService, userRepo, patientRepo);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", this::handleRoot);

        server.start();
        System.out.println("=================================================================");
        System.out.println("🛡️  MedSentinel Pure Java Server (No Spring Boot)");
        System.out.println("📦 Persistence: MongoDB (configured with MONGODB_URI)");
        System.out.println("🚀 Server listening at: http://localhost:" + port);
        System.out.println("=================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        apiSecurity.applySecurityHeaders(exchange);
        try {
            apiSecurity.applyCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            validateContentLength(exchange);

            PatientService.clearCurrentUser();
            AppUserDetails principal = apiSecurity.resolve(exchange);
            if (principal != null) {
                PatientService.setCurrentUser(principal);
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.startsWith("/api/")) {
                handleApi(exchange, path, method, principal);
            } else {
                handleWebPages(exchange, path, method, principal);
            }
        } catch (ApiException e) {
            if (e.getStatus() == 401) {
                exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
            }
            sendError(exchange, e.getStatus(), e.getCode(), e.getMessage());
        } catch (AuthenticationRequiredException e) {
            sendError(exchange, 401, "UNAUTHENTICATED", "Authentication is required");
        } catch (PatientService.AccessDeniedException e) {
            sendError(exchange, 403, "FORBIDDEN", "You do not have permission to access this resource");
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, "BAD_REQUEST", "The request could not be processed");
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getClass().getSimpleName());
            sendError(exchange, 500, "INTERNAL_ERROR", "An internal server error occurred");
        } finally {
            PatientService.clearCurrentUser();
        }
    }

    private void handleApi(HttpExchange exchange, String path, String method,
                           AppUserDetails principal) throws IOException {
        if (path.equals("/api/auth/login")) {
            requireMethod(method, "POST");
            Map<String, String> body = parseJsonBody(exchange);
            String username = requiredBodyValue(body, "username");
            String password = requiredBodyValue(body, "password");
            String ip = exchange.getRemoteAddress().getAddress().getHostAddress();

            Optional<AppUserDetails> userOpt = authService.login(username, password, ip);
            if (userOpt.isEmpty()) {
                throw new ApiException(401, "AUTHENTICATION_FAILED", "Invalid username or password");
            }

            AppUserDetails user = userOpt.get();
            String token = apiSecurity.getSessionStore().create(user);
            exchange.getResponseHeaders().add("Set-Cookie", apiSecurity.sessionCookie(token));
            sendJson(exchange, 200, authenticationResponse(user));
            return;
        }

        if (path.equals("/api/auth/logout")) {
            requireMethod(method, "POST");
            apiSecurity.invalidatePresentedSessions(exchange);
            exchange.getResponseHeaders().add("Set-Cookie", apiSecurity.expiredSessionCookie());
            sendJson(exchange, 200, Map.of("authenticated", false));
            return;
        }

        if (path.equals("/api/auth/me")) {
            requireMethod(method, "GET");
            apiSecurity.requireAuthenticated(principal);
            sendJson(exchange, 200, authenticationResponse(principal));
            return;
        }

        if (path.equals("/api/stats")) {
            requireMethod(method, "GET");
            apiSecurity.requireStats(principal);
            sendJson(exchange, 200, Map.of(
                "openAlerts", alertService.countOpenAlerts(),
                "highSeverity", alertService.countHighSeverity(),
                "failedLogins", logRepo.countFailedLoginsSince(LocalDateTime.now().minusHours(24))
            ));
            return;
        }

        if (path.equals("/api/alerts")) {
            requireMethod(method, "GET");
            apiSecurity.requireAlertRead(principal);
            sendJson(exchange, 200, alertService.getOpenAlerts());
            return;
        }

        if (path.startsWith("/api/alerts/")) {
            String remainder = path.substring("/api/alerts/".length());
            if (remainder.endsWith("/action")) {
                requireMethod(method, "POST");
                apiSecurity.requireReviewer(principal);
                Long id = parseId(remainder.substring(0, remainder.length() - "/action".length()), "alert");
                if (alertService.getAlert(id) == null) {
                    throw ApiException.notFound();
                }
                Map<String, String> body = parseJsonBody(exchange);
                String action = requiredBodyValue(body, "action").toLowerCase(Locale.ROOT);
                String note = body.getOrDefault("note", "").trim();
                if (note.length() > 2000) {
                    throw ApiException.badRequest("The reviewer note is too long");
                }
                Alert.AlertStatus status = switch (action) {
                    case "acknowledge", "ack" -> Alert.AlertStatus.ACK;
                    case "escalate" -> Alert.AlertStatus.ESCALATED;
                    case "false_positive" -> Alert.AlertStatus.FALSE_POSITIVE;
                    default -> throw ApiException.badRequest("Unsupported alert action");
                };
                sendJson(exchange, 200, alertService.updateStatus(id, status, note));
                return;
            }

            requireMethod(method, "GET");
            apiSecurity.requireAlertRead(principal);
            Long id = parseId(remainder, "alert");
            Alert alert = alertService.getAlert(id);
            if (alert == null) {
                throw ApiException.notFound();
            }
            List<AccessLog> evidenceLogs = new ArrayList<>();
            if (alert.getEvidenceLogIds() != null) {
                for (Long logId : alert.getEvidenceLogIds()) {
                    logRepo.findById(logId).ifPresent(evidenceLogs::add);
                }
            }
            sendJson(exchange, 200, Map.of("alert", alert, "evidenceLogs", evidenceLogs));
            return;
        }

        if (path.equals("/api/logs")) {
            requireMethod(method, "GET");
            apiSecurity.requireLogs(principal);
            sendJson(exchange, 200, logRepo.findTop100ByOrderByTsDesc());
            return;
        }

        if (path.equals("/api/integrity")) {
            requireMethod(method, "GET");
            apiSecurity.requireLogs(principal);
            sendJson(exchange, 200, chainService.verifyChain());
            return;
        }

        if (path.equals("/api/patients")) {
            requireMethod(method, "GET");
            apiSecurity.requirePatientDirectory(principal);
            String query = getQueryParam(exchange, "q");
            sendJson(exchange, 200, query != null && !query.isBlank()
                ? patientService.searchPatients(principal, query)
                : patientService.listPatients(principal));
            return;
        }

        if (path.startsWith("/api/patients/")) {
            String remainder = path.substring("/api/patients/".length());
            if (remainder.endsWith("/break-glass")) {
                requireMethod(method, "POST");
                apiSecurity.requireBreakGlass(principal);
                Long id = parseId(remainder.substring(0, remainder.length() - "/break-glass".length()), "patient");
                if (patientRepo.findById(id).isEmpty()) {
                    throw ApiException.notFound();
                }
                Map<String, String> body = parseJsonBody(exchange);
                String justification = requiredBodyValue(body, "justification").trim();
                if (justification.length() < 10 || justification.length() > 2000) {
                    throw ApiException.badRequest("Justification must be between 10 and 2000 characters");
                }
                BreakGlassService.BreakGlassResult result = breakGlassService.executeBreakGlass(
                    principal, id, justification
                );
                sendJson(exchange, 200, Map.of("success", true, "records", result.records()));
                return;
            }

            requireMethod(method, "GET");
            apiSecurity.requirePatientDirectory(principal);
            Long id = parseId(remainder, "patient");
            Patient patient = patientService.getPatient(principal, id);
            if (patient == null) {
                throw ApiException.notFound();
            }
            PatientService.PatientRecordBundle bundle = patientService.getAccessibleRecords(principal, id);
            sendJson(exchange, 200, Map.of(
                "patient", patient,
                "records", bundle.getRecords(),
                "denied", bundle.getDenied()
            ));
            return;
        }

        if (path.equals("/api/admin/users")) {
            requireMethod(method, "GET");
            apiSecurity.requireAdmin(principal);
            sendJson(exchange, 200, userRepo.findAll().stream().map(PublicUserDto::from).toList());
            return;
        }

        if (path.startsWith("/api/admin/users/") && path.endsWith("/toggle")) {
            requireMethod(method, "POST");
            apiSecurity.requireAdmin(principal);
            String idText = path.substring("/api/admin/users/".length(), path.length() - "/toggle".length());
            Long id = parseId(idText, "user");
            User user = userRepo.findById(id).orElse(null);
            if (user == null) {
                throw ApiException.notFound();
            }
            user.setStatus(user.getStatus() == User.UserStatus.ACTIVE
                ? User.UserStatus.DISABLED : User.UserStatus.ACTIVE);
            userRepo.save(user);
            if (user.getStatus() == User.UserStatus.DISABLED) {
                apiSecurity.getSessionStore().invalidateUser(user.getUserId());
            }
            sendJson(exchange, 200, PublicUserDto.from(user));
            return;
        }

        if (path.equals("/api/admin/baselines")) {
            requireMethod(method, "GET");
            apiSecurity.requireAdmin(principal);
            sendJson(exchange, 200, roleService.getAllBaselines());
            return;
        }

        if (path.startsWith("/api/admin/baselines/")) {
            requireMethod(method, "POST");
            apiSecurity.requireAdmin(principal);
            Long id = parseId(path.substring("/api/admin/baselines/".length()), "baseline");
            Map<String, String> body = parseJsonBody(exchange);
            int maxRecords = parseInt(body.getOrDefault("maxRecords", "15"), "maxRecords");
            BigDecimal multiplier = parseDecimal(body.getOrDefault("multiplier", "1.5"), "multiplier");
            if (maxRecords < 1 || maxRecords > 100000 || multiplier.signum() <= 0) {
                throw ApiException.badRequest("Baseline values are outside the allowed range");
            }
            sendJson(exchange, 200, roleService.updateBaseline(id, maxRecords, multiplier));
            return;
        }

        if (path.startsWith("/api/simulator/")) {
            requireMethod(method, "POST");
            apiSecurity.requireAdmin(principal);
            String sim = path.substring("/api/simulator/".length());
            String result = switch (sim) {
                case "normal-nurse" -> scenarioRunner.runNormalNurseShift();
                case "busy-clinician" -> scenarioRunner.runBusyClinician();
                case "brute-force" -> scenarioRunner.runBruteForceLogin();
                case "billing-bulk" -> scenarioRunner.runBillingClerkBulkPull();
                case "receptionist-clinical" -> scenarioRunner.runReceptionistReadsClinical();
                case "break-glass" -> scenarioRunner.runBreakGlass();
                case "tamper-log" -> scenarioRunner.tamperLogRow();
                case "seed" -> {
                    int count = parseInt(getQueryParam(exchange, "count"), "count", 5000);
                    if (count < 1 || count > 100000) {
                        throw ApiException.badRequest("Seed count is outside the allowed range");
                    }
                    yield String.format("Seeded %d realistic, hash-chained log rows in MongoDB across 7 days.",
                        dataSeeder.generateSeedLogs(count));
                }
                default -> throw ApiException.notFound();
            };
            sendJson(exchange, 200, Map.of("result", result));
            return;
        }

        throw ApiException.notFound();
    }

    private void handleWebPages(HttpExchange exchange, String path, String method,
                                AppUserDetails principal) throws IOException {
        if (path.equals("/login")) {
            renderLoginHtml(exchange);
            return;
        }

        apiSecurity.requireAuthenticated(principal);

        if (path.equals("/") || path.equals("/home") || path.equals("/dashboard")) {
            apiSecurity.requireStats(principal);
            renderDashboardHtml(exchange);
        } else if (path.equals("/dashboard/integrity")) {
            apiSecurity.requireLogs(principal);
            renderIntegrityHtml(exchange);
        } else if (path.equals("/dashboard/logs")) {
            apiSecurity.requireLogs(principal);
            renderLogsHtml(exchange);
        } else if (path.startsWith("/dashboard/alerts/")) {
            apiSecurity.requireAlertRead(principal);
            Long id = Long.parseLong(path.substring("/dashboard/alerts/".length()));
            renderAlertDetailHtml(exchange, id);
        } else if (path.equals("/patients")) {
            apiSecurity.requirePatientDirectory(principal);
            renderPatientsHtml(exchange);
        } else if (path.startsWith("/patients/")) {
            apiSecurity.requirePatientDirectory(principal);
            Long id = Long.parseLong(path.substring("/patients/".length()));
            renderPatientViewHtml(exchange, id);
        } else if (path.equals("/simulator")) {
            apiSecurity.requireAdmin(principal);
            renderSimulatorHtml(exchange);
        } else if (path.equals("/admin/users")) {
            apiSecurity.requireAdmin(principal);
            renderAdminUsersHtml(exchange);
        } else if (path.equals("/admin/baselines")) {
            apiSecurity.requireAdmin(principal);
            renderAdminBaselinesHtml(exchange);
        } else {
            apiSecurity.requireStats(principal);
            renderDashboardHtml(exchange);
        }
    }

    private void renderDashboardHtml(HttpExchange exchange) throws IOException {
        long openAlerts = alertService.countOpenAlerts();
        long highSeverity = alertService.countHighSeverity();
        long failedLogins = logRepo.countFailedLoginsSince(LocalDateTime.now().minusHours(24));
        List<Alert> alerts = alertService.getOpenAlerts();
        List<AccessLog> recentLogs = logRepo.findTop20ByOrderByTsDesc();

        StringBuilder alertRows = new StringBuilder();
        if (alerts.isEmpty()) {
            alertRows.append("<tr><td colspan='5' class='text-center text-muted py-4'>No open alerts. Run a simulator scenario to generate activity.</td></tr>");
        } else {
            for (Alert a : alerts) {
                String badgeClass = a.getSeverity() == Alert.Severity.HIGH ? "text-bg-danger" : (a.getSeverity() == Alert.Severity.MEDIUM ? "text-bg-warning" : "text-bg-info");
                alertRows.append(String.format("<tr><td><span class='badge %s'>%s</span></td><td>%s</td><td>%d</td><td><a href='/dashboard/alerts/%d' class='text-decoration-none'>%s</a></td><td class='small'>%s</td></tr>",
                    badgeClass, a.getSeverity(), a.getRuleCodes(), a.getScore(), a.getAlertId(), escapeHtml(a.getExplanation()), a.getLastSeen().toString()));
            }
        }

        StringBuilder logRows = new StringBuilder();
        if (recentLogs.isEmpty()) {
            logRows.append("<li class='list-group-item text-muted'>No access events yet.</li>");
        } else {
            for (AccessLog l : recentLogs) {
                String badgeClass = l.getOutcome() == AccessLog.Outcome.ALLOWED ? "text-bg-success" : (l.getOutcome() == AccessLog.Outcome.DENIED ? "text-bg-warning" : "text-bg-danger");
                logRows.append(String.format("<li class='list-group-item'><div class='d-flex justify-content-between'><strong>%s</strong><span class='badge %s'>%s</span></div><small class='text-muted'>%s · %s</small></li>",
                    l.getUsernameAttempted(), badgeClass, l.getOutcome(), l.getAction(), escapeHtml(l.getReason())));
            }
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>MedSentinel SOC · MongoDB Backend</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>body{background:#f8fafc;font-family:system-ui,-apple-system,sans-serif}.navbar{background:#0f172a}.navbar-brand{font-weight:700;color:#38bdf8!important}.kpi-card{border-left:4px solid}.kpi-card span{font-size:.85rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em}</style>
        </head><body>
        <nav class="navbar navbar-expand-lg navbar-dark px-4"><a class="navbar-brand" href="/">🛡️ MedSentinel</a>
        <div class="navbar-nav me-auto"><a class="nav-link active" href="/dashboard">SOC Dashboard</a><a class="nav-link" href="/patients">Patients</a><a class="nav-link" href="/simulator">Simulator</a><a class="nav-link" href="/admin/users">Admin</a></div>
        <span class="badge bg-success">MongoDB Connected</span></nav>
        <main class="container py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
             <div><h1 class="h2 mb-1">Security Operations Center</h1><p class="text-muted mb-0">Simple Java 21 backend backed by MongoDB.</p></div>
            <a href="/dashboard/integrity" class="btn btn-outline-primary">Verify Hash Chain Integrity</a>
        </div>
        <div class="row g-3 mb-4">
            <div class="col-md-4"><div class="card kpi-card border-primary p-3"><span class="text-muted">Open alerts</span><p class="display-6 mb-0">%d</p></div></div>
            <div class="col-md-4"><div class="card kpi-card border-danger p-3"><span class="text-muted">High severity</span><p class="display-6 mb-0 text-danger">%d</p></div></div>
            <div class="col-md-4"><div class="card kpi-card border-warning p-3"><span class="text-muted">Failed logins (24h)</span><p class="display-6 mb-0">%d</p></div></div>
        </div>
        <div class="row g-4">
            <section class="col-lg-8"><div class="card shadow-sm"><div class="card-header bg-white d-flex justify-content-between"><strong>Active Alert Feed</strong><span class="badge bg-secondary">%d open</span></div>
            <div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead><tr><th>Severity</th><th>Rules</th><th>Score</th><th>Explanation</th><th>Last seen</th></tr></thead><tbody>%s</tbody></table></div></div></section>
            <section class="col-lg-4"><div class="card shadow-sm"><div class="card-header bg-white"><strong>Recent Audit Stream (MongoDB)</strong></div><ul class="list-group list-group-flush">%s</ul></div></section>
        </div></main></body></html>
        """.formatted(openAlerts, highSeverity, failedLogins, alerts.size(), alertRows.toString(), logRows.toString());

        sendHtml(exchange, 200, html);
    }

    private void renderIntegrityHtml(HttpExchange exchange) throws IOException {
        LogChainService.ChainVerificationResult result = chainService.verifyChain();
        String resultHtml;
        if (result.intact()) {
            resultHtml = String.format("<div class='text-success'><h1 class='display-1'>✓</h1><h2>Hash Chain Intact</h2><p class='lead'>All <strong>%d</strong> audit-log rows in MongoDB passed SHA-256 cryptographic verification.</p></div>", result.totalRows());
        } else {
            resultHtml = String.format("<div class='text-danger'><h1 class='display-1'>⚠</h1><h2>Integrity Violation Detected</h2><p class='lead'>First broken row is log ID <strong>%d</strong>.</p><p class='text-muted'>Database tampering detected by append-only SHA-256 cryptographic chain.</p></div>", result.brokenAtLogId());
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Integrity Check · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        </head><body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a></nav>
        <main class="container py-5"><div class="card shadow-sm mx-auto text-center p-5" style="max-width:700px">%s<a href="/dashboard" class="btn btn-outline-primary mt-4">← Back to Dashboard</a></div></main></body></html>
        """.formatted(resultHtml);
        sendHtml(exchange, 200, html);
    }

    private void renderAlertDetailHtml(HttpExchange exchange, Long alertId) throws IOException {
        Alert alert = alertService.getAlert(alertId);
        if (alert == null) {
            exchange.getResponseHeaders().set("Location", "/dashboard");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        List<AccessLog> evidenceLogs = new ArrayList<>();
        if (alert.getEvidenceLogIds() != null) {
            for (Long id : alert.getEvidenceLogIds()) {
                logRepo.findById(id).ifPresent(evidenceLogs::add);
            }
        }

        StringBuilder rows = new StringBuilder();
        for (AccessLog log : evidenceLogs) {
            rows.append(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                log.getLogId(), log.getTs(), log.getUsernameAttempted(), log.getAction(), log.getResourceType(), log.getOutcome(), log.getReason()));
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Alert A-%d · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a></nav>
        <main class="container py-4"><a href="/dashboard" class="btn btn-sm btn-outline-secondary mb-3">← Dashboard</a>
        <div class="card shadow-sm mb-4"><div class="card-body"><h3>Alert A-%d: %s</h3><p class="lead">%s</p>
        <p><strong>Rules:</strong> %s · <strong>Score:</strong> %d · <strong>Severity:</strong> %s · <strong>Status:</strong> %s</p></div></div>
        <div class="card shadow-sm"><div class="card-header bg-white"><strong>Linked Cryptographic Evidence Rows (%d)</strong></div>
        <div class="table-responsive"><table class="table table-sm mb-0"><thead><tr><th>ID</th><th>Time</th><th>User</th><th>Action</th><th>Resource</th><th>Outcome</th><th>Reason</th></tr></thead><tbody>%s</tbody></table></div></div>
        </main></body></html>
        """.formatted(alert.getAlertId(), alert.getAlertId(), alert.getRuleCodes(), alert.getExplanation(), alert.getRuleCodes(), alert.getScore(), alert.getSeverity(), alert.getStatus(), evidenceLogs.size(), rows.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderPatientsHtml(HttpExchange exchange) throws IOException {
        List<Patient> patients = patientService.listPatients();
        StringBuilder rows = new StringBuilder();
        for (Patient p : patients) {
            rows.append(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%d</td><td><a href='/patients/%d' class='btn btn-sm btn-outline-primary'>View records</a></td></tr>",
                p.getPatientId(), p.getName(), p.getDob(), p.getWardId(), p.getPatientId()));
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Patients · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a><div class="navbar-nav me-auto"><a class="nav-link" href="/dashboard">SOC Dashboard</a><a class="nav-link active" href="/patients">Patients</a><a class="nav-link" href="/simulator">Simulator</a></div></nav>
        <main class="container py-4"><h1 class="h2 mb-4">Patient Directory (MongoDB)</h1>
        <div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover mb-0"><thead><tr><th>ID</th><th>Name</th><th>Date of birth</th><th>Ward ID</th><th></th></tr></thead><tbody>%s</tbody></table></div></div></main></body></html>
        """.formatted(rows.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderPatientViewHtml(HttpExchange exchange, Long id) throws IOException {
        Patient patient = patientService.getPatient(id);
        if (patient == null) {
            exchange.getResponseHeaders().set("Location", "/patients");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        PatientService.PatientRecordBundle bundle = patientService.getAccessibleRecords(id);
        StringBuilder recs = new StringBuilder();
        bundle.getRecords().forEach((type, list) -> {
            recs.append("<div class='card shadow-sm mb-3'><div class='card-header bg-white'><strong>").append(type).append("</strong></div><div class='card-body'>");
            for (PatientRecord r : list) {
                recs.append("<p class='mb-1'>").append(r.getContent()).append("</p>");
            }
            recs.append("</div></div>");
        });

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Patient Record · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a></nav>
        <main class="container py-4"><a href="/patients" class="btn btn-sm btn-outline-secondary mb-3">← Patient directory</a>
        <div class="card shadow-sm mb-4"><div class="card-body"><h2>%s</h2><p class="text-muted">Patient #%d · DOB %s · Ward %d</p></div></div>
        %s</main></body></html>
        """.formatted(patient.getName(), patient.getPatientId(), patient.getDob(), patient.getWardId(), recs.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderSimulatorHtml(HttpExchange exchange) throws IOException {
        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Scenario Simulator · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <script>
        async function runSim(endpoint) {
            const btn = event.target;
            btn.disabled = true;
            btn.innerText = "Running...";
            try {
                const res = await fetch(endpoint, { method: "POST" });
                const data = await res.json();
                document.getElementById("output").innerText = data.result || JSON.stringify(data);
                document.getElementById("alert-box").style.display = "block";
            } catch (e) {
                alert("Error: " + e);
            } finally {
                btn.disabled = false;
                btn.innerText = "Run";
            }
        }
        </script>
        </head><body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a><div class="navbar-nav me-auto"><a class="nav-link" href="/dashboard">SOC Dashboard</a><a class="nav-link" href="/patients">Patients</a><a class="nav-link active" href="/simulator">Simulator</a></div></nav>
        <main class="container py-4"><h1 class="h2 mb-2">Scenario Simulator</h1><p class="text-muted mb-4">Run live security attack and clinician scenarios against the MongoDB backend.</p>
        <div id="alert-box" class="alert alert-info" style="display:none"><strong>Result:</strong> <span id="output"></span></div>
        <div class="list-group shadow-sm">
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>1. Normal nurse shift</strong><p class="text-muted mb-0">Nurse views own-ward records. Expected: 0 alerts.</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/normal-nurse')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>2. Busy clinician burst (Busy-Clinician Guard)</strong><p class="text-muted mb-0">Nurse opens 40 own-ward charts rapidly. Expected: 0 alerts (on-ward baseline).</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/busy-clinician')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>3. Brute-force login attack</strong><p class="text-muted mb-0">6 failed logins followed by success. Expected: R1 + R1b alert.</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/brute-force')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>4. Billing clerk bulk pull</strong><p class="text-muted mb-0">Clerk tries clinical (403 denial) then 38 billing records. Expected: High alert (R2 + R3).</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/billing-bulk')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>5. Receptionist reads clinical</strong><p class="text-muted mb-0">Denied 403 clinical access attempt. Expected: R3 Medium alert.</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/receptionist-clinical')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>6. Doctor emergency break-glass</strong><p class="text-muted mb-0">Dr. Asha accesses unassigned patient with clinical justification. Expected: Medium review alert.</p></div><button class="btn btn-primary btn-sm" onclick="runSim('/api/simulator/break-glass')">Run</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>7. Tamper MongoDB log row</strong><p class="text-muted mb-0">Directly alters an audit log row to test SHA-256 chain tamper detection.</p></div><button class="btn btn-danger btn-sm" onclick="runSim('/api/simulator/tamper-log')">Tamper</button></div>
            <div class="list-group-item d-flex justify-content-between align-items-center"><div><strong>8. Generate 5,000 Seed Logs</strong><p class="text-muted mb-0">Generates 5,000 hash-chained logs in MongoDB spread across 7 days.</p></div><button class="btn btn-success btn-sm" onclick="runSim('/api/simulator/seed')">Seed 5k Logs</button></div>
        </div></main></body></html>
        """;
        sendHtml(exchange, 200, html);
    }

    private void renderLogsHtml(HttpExchange exchange) throws IOException {
        List<AccessLog> logs = logRepo.findTop100ByOrderByTsDesc();
        StringBuilder rows = new StringBuilder();
        for (AccessLog l : logs) {
            String badge = l.getOutcome() == AccessLog.Outcome.ALLOWED ? "text-bg-success" : (l.getOutcome() == AccessLog.Outcome.DENIED ? "text-bg-warning" : "text-bg-danger");
            rows.append(String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td><span class='badge %s'>%s</span></td><td>%s</td></tr>",
                l.getLogId(), l.getTs(), l.getUsernameAttempted(), l.getIp(), l.getAction(), l.getResourceType(), badge, l.getOutcome(), l.getReason()));
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Audit Logs · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a><div class="navbar-nav me-auto"><a class="nav-link" href="/dashboard">SOC Dashboard</a><a class="nav-link active" href="/dashboard/logs">Logs</a></div></nav>
        <main class="container py-4"><h1 class="h2 mb-4">MongoDB Audit Log Stream (100 most recent)</h1>
        <div class="card shadow-sm"><div class="table-responsive"><table class="table table-sm table-hover mb-0"><thead><tr><th>ID</th><th>Timestamp</th><th>User</th><th>IP</th><th>Action</th><th>Resource</th><th>Outcome</th><th>Reason</th></tr></thead><tbody>%s</tbody></table></div></div></main></body></html>
        """.formatted(rows.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderAdminUsersHtml(HttpExchange exchange) throws IOException {
        List<User> users = userRepo.findAll();
        StringBuilder rows = new StringBuilder();
        for (User u : users) {
            String roleName = u.getRole() != null ? u.getRole().getName() : "-";
            rows.append(String.format("<tr><td>%d</td><td><strong>%s</strong></td><td>%s</td><td>%s</td><td><span class='badge bg-success'>%s</span></td></tr>",
                u.getUserId(), u.getUsername(), roleName, u.getWardId() != null ? u.getWardId() : "-", u.getStatus()));
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Admin Users · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a></nav>
        <main class="container py-4"><h1 class="h2 mb-4">User Administration (MongoDB)</h1>
        <div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover mb-0"><thead><tr><th>ID</th><th>Username</th><th>Role</th><th>Ward</th><th>Status</th></tr></thead><tbody>%s</tbody></table></div></div></main></body></html>
        """.formatted(rows.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderAdminBaselinesHtml(HttpExchange exchange) throws IOException {
        List<RoleBaseline> baselines = roleService.getAllBaselines();
        StringBuilder rows = new StringBuilder();
        for (RoleBaseline b : baselines) {
            rows.append(String.format("<tr><td>%d</td><td>%d</td><td>%s</td><td>%d</td><td>%s</td></tr>",
                b.getBaselineId(), b.getRoleId(), b.getScope(), b.getMaxRecordsPer10Min(), b.getMultiplier()));
        }

        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Admin Baselines · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light"><nav class="navbar navbar-dark bg-dark px-4"><a class="navbar-brand text-info" href="/">🛡️ MedSentinel</a></nav>
        <main class="container py-4"><h1 class="h2 mb-4">Role Baselines (MongoDB)</h1>
        <div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover mb-0"><thead><tr><th>ID</th><th>Role ID</th><th>Scope</th><th>Max Records / 10 Min</th><th>Multiplier</th></tr></thead><tbody>%s</tbody></table></div></div></main></body></html>
        """.formatted(rows.toString());
        sendHtml(exchange, 200, html);
    }

    private void renderLoginHtml(HttpExchange exchange) throws IOException {
        String html = """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>Login · MedSentinel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
        <body class="bg-light d-flex align-items-center justify-content-center vh-100">
        <div class="card shadow-sm p-4" style="width:360px"><h2 class="h4 mb-3 text-center">MedSentinel Login</h2>
        <form onsubmit="event.preventDefault(); window.location.href='/dashboard';">
        <div class="mb-3"><label class="form-label">Username</label><input class="form-control" value="dr.asha" required></div>
        <div class="mb-3"><label class="form-label">Password</label><input type="password" class="form-control" value="password123" required></div>
        <button class="btn btn-primary w-100">Sign in</button></form></div></body></html>
        """;
        sendHtml(exchange, 200, html);
    }

    private Map<String, Object> authenticationResponse(AppUserDetails user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("role", user.getRoleName());
        response.put("wardId", user.getWardId());
        response.put("permissions", user.getPermissions());
        response.put("user", PublicUserDto.from(user));
        return response;
    }

    private void requireMethod(String actual, String expected) {
        if (!expected.equalsIgnoreCase(actual)) {
            throw ApiException.methodNotAllowed();
        }
    }

    private String requiredBodyValue(Map<String, String> body, String field) {
        String value = body.get(field);
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest("Field '" + field + "' is required");
        }
        return value;
    }

    private Long parseId(String value, String resourceName) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest("A valid " + resourceName + " ID is required");
        }
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw ApiException.badRequest("A valid " + resourceName + " ID is required");
            }
            return id;
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("A valid " + resourceName + " ID is required");
        }
    }

    private int parseInt(String value, String field) {
        return parseInt(value, field, null);
    }

    private int parseInt(String value, String field, Integer defaultValue) {
        if (value == null || value.isBlank()) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw ApiException.badRequest("Field '" + field + "' must be an integer");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Field '" + field + "' must be an integer");
        }
    }

    private BigDecimal parseDecimal(String value, String field) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            throw ApiException.badRequest("Field '" + field + "' must be a number");
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] bytes = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String code, String message) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        sendJson(exchange, status, Map.of("error", error));
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseJsonBody(HttpExchange exchange) throws IOException {
        String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
        if (contentLength != null && !contentLength.isBlank()) {
            try {
                if (Long.parseLong(contentLength) > MAX_REQUEST_BODY_BYTES) {
                    throw ApiException.payloadTooLarge();
                }
            } catch (NumberFormatException e) {
                throw ApiException.badRequest("Invalid Content-Length header");
            }
        }

        try (InputStream input = exchange.getRequestBody();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_REQUEST_BODY_BYTES) {
                    throw ApiException.payloadTooLarge();
                }
                output.write(buffer, 0, read);
            }

            String body = output.toString(StandardCharsets.UTF_8);
            if (body.isBlank()) {
                return Collections.emptyMap();
            }
            Map<String, String> parsed = gson.fromJson(
                body,
                new TypeToken<Map<String, String>>() {}.getType()
            );
            return parsed != null ? parsed : Collections.emptyMap();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.badRequest("Request body must be a valid JSON object");
        }
    }

    private void validateContentLength(HttpExchange exchange) {
        String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
        if (contentLength == null || contentLength.isBlank()) {
            return;
        }
        try {
            long length = Long.parseLong(contentLength);
            if (length < 0) {
                throw ApiException.badRequest("Invalid Content-Length header");
            }
            if (length > MAX_REQUEST_BODY_BYTES) {
                throw ApiException.payloadTooLarge();
            }
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Invalid Content-Length header");
        }
    }

    private String getQueryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            int separator = param.indexOf('=');
            if (separator > 0 && param.substring(0, separator).equals(key)) {
                return URLDecoder.decode(param.substring(separator + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static int readMaxRequestBodyBytes() {
        String raw = System.getenv("MEDSENTINEL_MAX_BODY_BYTES");
        if (raw == null || raw.isBlank()) {
            return 64 * 1024;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value >= 1024 && value <= 10 * 1024 * 1024 ? value : 64 * 1024;
        } catch (NumberFormatException e) {
            return 64 * 1024;
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }

    public static void main(String[] args) {
        int port = 8080;
        for (String arg : args) {
            if (arg.startsWith("--server.port=")) {
                port = Integer.parseInt(arg.substring("--server.port=".length()));
            } else if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring("--port=".length()));
            }
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            port = Integer.parseInt(envPort);
        }

        try {
            MedSentinelServer server = new MedSentinelServer(port);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
