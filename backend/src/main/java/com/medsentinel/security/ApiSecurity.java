package com.medsentinel.security;

import com.medsentinel.auth.AppUserDetails;
import com.sun.net.httpserver.HttpExchange;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Central HTTP authentication, authorization, cookie, CORS, and header policy.
 */
public class ApiSecurity {

    public static final String SESSION_COOKIE = "MEDSENTINEL_SESSION";

    private final SessionStore sessionStore;
    private final Set<String> allowedOrigins;
    private final boolean secureCookie;

    public ApiSecurity() {
        this(new SessionStore(), readAllowedOrigins(), readSecureCookie());
    }

    public ApiSecurity(SessionStore sessionStore, Set<String> allowedOrigins, boolean secureCookie) {
        this.sessionStore = sessionStore;
        this.allowedOrigins = allowedOrigins == null ? Set.of() : Set.copyOf(allowedOrigins);
        this.secureCookie = secureCookie;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public AppUserDetails resolve(HttpExchange exchange) {
        Optional<String> bearer = bearerToken(exchange);
        if (bearer.isPresent()) {
            return sessionStore.resolve(bearer.get()).orElse(null);
        }
        return cookieToken(exchange)
            .flatMap(sessionStore::resolve)
            .orElse(null);
    }

    public void invalidatePresentedSessions(HttpExchange exchange) {
        bearerToken(exchange).ifPresent(sessionStore::invalidate);
        cookieToken(exchange).ifPresent(sessionStore::invalidate);
    }

    public void requireAuthenticated(AppUserDetails principal) {
        if (principal == null) {
            throw ApiException.unauthorized();
        }
    }

    public void requirePermission(AppUserDetails principal, String permission) {
        requireAuthenticated(principal);
        if (!principal.hasPermission(permission)) {
            throw ApiException.forbidden();
        }
    }

    public void requireAnyPermission(AppUserDetails principal, String... permissions) {
        requireAuthenticated(principal);
        for (String permission : permissions) {
            if (principal.hasPermission(permission)) {
                return;
            }
        }
        throw ApiException.forbidden();
    }

    public void requireStats(AppUserDetails principal) {
        requireAnyPermissionOrRole(principal, Set.of("VIEW_ALERTS", "VIEW_LOGS"), "ADMIN");
    }

    public void requireAlertRead(AppUserDetails principal) {
        requireAnyPermissionOrRole(principal, Set.of("VIEW_ALERTS"), "ADMIN");
    }

    public void requireReviewer(AppUserDetails principal) {
        requireRoleOrAdmin(principal, "REVIEWER");
    }

    public void requireLogs(AppUserDetails principal) {
        requireAnyPermissionOrRole(principal, Set.of("VIEW_LOGS"), "ADMIN");
    }

    public void requirePatientDirectory(AppUserDetails principal) {
        requireAnyPermissionOrRole(principal, Set.of("VIEW_DEMOGRAPHICS"), "ADMIN");
    }

    public void requireBreakGlass(AppUserDetails principal) {
        requireAuthenticated(principal);
        if (!principal.hasRole("DOCTOR") || !principal.hasPermission("VIEW_CLINICAL")) {
            throw ApiException.forbidden();
        }
    }

    public void requireAdmin(AppUserDetails principal) {
        requireAuthenticated(principal);
        if (!principal.hasRole("ADMIN") || !principal.hasPermission("ADMIN")) {
            throw ApiException.forbidden();
        }
    }

    public void applySecurityHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.getResponseHeaders().set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        exchange.getResponseHeaders().set(
            "Content-Security-Policy",
            "default-src 'self'; style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; " +
                "script-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'"
        );
    }

    public void applyCors(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin == null || origin.isBlank()) {
            return;
        }
        if (!allowedOrigins.contains(origin)) {
            throw ApiException.originNotAllowed();
        }

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().set("Vary", "Origin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public String sessionCookie(String token) {
        return cookie(token, sessionStore.ttlSeconds());
    }

    public String expiredSessionCookie() {
        return cookie("", 0);
    }

    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    private void requireAnyPermissionOrRole(AppUserDetails principal, Set<String> permissions, String role) {
        requireAuthenticated(principal);
        if (principal.hasRole(role)) {
            return;
        }
        for (String permission : permissions) {
            if (principal.hasPermission(permission)) {
                return;
            }
        }
        throw ApiException.forbidden();
    }

    private void requireRoleOrAdmin(AppUserDetails principal, String role) {
        requireAuthenticated(principal);
        if (!principal.hasRole(role) && !principal.hasRole("ADMIN")) {
            throw ApiException.forbidden();
        }
    }

    private Optional<String> bearerToken(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        int separator = header.indexOf(' ');
        if (separator <= 0 || !"bearer".equalsIgnoreCase(header.substring(0, separator))) {
            return Optional.empty();
        }
        String token = header.substring(separator + 1).trim();
        return token.isBlank() ? Optional.empty() : Optional.of(token);
    }

    private Optional<String> cookieToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return Optional.empty();
        }
        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2 && SESSION_COOKIE.equals(pair[0].trim())) {
                String token = pair[1].trim();
                return token.isBlank() ? Optional.empty() : Optional.of(token);
            }
        }
        return Optional.empty();
    }

    private String cookie(String token, long maxAgeSeconds) {
        String secure = secureCookie ? "; Secure" : "";
        return SESSION_COOKIE + "=" + token + "; Max-Age=" + maxAgeSeconds +
            "; Path=/; HttpOnly; SameSite=Lax" + secure;
    }

    private static Set<String> readAllowedOrigins() {
        String raw = System.getenv("MEDSENTINEL_ALLOWED_ORIGINS");
        if (raw == null || raw.isBlank()) {
            return Set.of("http://localhost:5173", "http://127.0.0.1:5173");
        }
        LinkedHashSet<String> origins = new LinkedHashSet<>();
        Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .forEach(origins::add);
        return origins;
    }

    private static boolean readSecureCookie() {
        return Boolean.parseBoolean(System.getenv().getOrDefault("MEDSENTINEL_COOKIE_SECURE", "false"));
    }
}
