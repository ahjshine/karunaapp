package com.medsentinel.config;

import com.medsentinel.auth.User;
import com.medsentinel.patient.*;
import com.medsentinel.rbac.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manages the connection, collections, sequence numbers, and bootstrap data for MongoDB.
 */
public class MongoDBManager {

    private static MongoDBManager instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private static final String DEFAULT_PW_HASH = "$2a$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe";

    public MongoDBManager(String uri, String dbName) {
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(dbName);
        createIndexes();
        bootstrapIfNeeded();
    }

    public static synchronized MongoDBManager getInstance() {
        if (instance == null) {
            String uri = System.getenv("MONGODB_URI");
            if (uri == null || uri.isBlank()) {
                uri = "mongodb://localhost:27017";
            }
            String dbName = System.getenv("MONGODB_DATABASE");
            if (dbName == null || dbName.isBlank()) {
                dbName = "medsentinel";
            }
            instance = new MongoDBManager(uri, dbName);
        }
        return instance;
    }

    public static synchronized void init(String uri, String dbName) {
        if (instance != null) {
            try { instance.close(); } catch (Exception ignored) {}
        }
        instance = new MongoDBManager(uri, dbName);
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String name) {
        return database.getCollection(name);
    }

    public long getNextSequence(String seqName) {
        MongoCollection<Document> seqCol = database.getCollection("database_sequences");
        Document result = seqCol.findOneAndUpdate(
            Filters.eq("_id", seqName),
            Updates.inc("seq", 1L),
            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );
        return result != null ? result.getLong("seq") : 1L;
    }

    private void createIndexes() {
        try {
            getCollection("access_logs").createIndex(Indexes.ascending("logId"), new IndexOptions().unique(true));
            getCollection("access_logs").createIndex(Indexes.descending("ts"));
            getCollection("access_logs").createIndex(Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.descending("ts")));
            getCollection("access_logs").createIndex(Indexes.compoundIndex(
                Indexes.ascending("userId"), Indexes.ascending("outcome"), Indexes.ascending("action"), Indexes.descending("ts")));
            getCollection("access_logs").createIndex(Indexes.compoundIndex(
                Indexes.ascending("usernameAttempted"), Indexes.ascending("outcome"), Indexes.ascending("action"), Indexes.descending("ts")));
            getCollection("access_logs").createIndex(Indexes.compoundIndex(
                Indexes.ascending("ip"), Indexes.ascending("outcome"), Indexes.ascending("action"), Indexes.descending("ts")));
            getCollection("access_logs").createIndex(Indexes.ascending("action"));
            getCollection("access_logs").createIndex(Indexes.ascending("outcome"));

            getCollection("alerts").createIndex(Indexes.ascending("alertId"), new IndexOptions().unique(true));
            getCollection("alerts").createIndex(Indexes.descending("lastSeen"));
            getCollection("alerts").createIndex(Indexes.ascending("status"));
            getCollection("alerts").createIndex(Indexes.compoundIndex(
                Indexes.ascending("userId"), Indexes.ascending("status"), Indexes.descending("lastSeen")));

            getCollection("users").createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
            getCollection("users").createIndex(Indexes.ascending("userId"), new IndexOptions().unique(true));

            getCollection("patients").createIndex(Indexes.ascending("patientId"), new IndexOptions().unique(true));
            getCollection("patients").createIndex(Indexes.ascending("wardId"));

            getCollection("records").createIndex(Indexes.ascending("recordId"), new IndexOptions().unique(true));
            getCollection("records").createIndex(Indexes.compoundIndex(Indexes.ascending("patientId"), Indexes.ascending("type")));

            getCollection("care_assignments").createIndex(
                Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.ascending("patientId")),
                new IndexOptions().unique(true)
            );
        } catch (Exception e) {
            System.err.println("Warning creating indexes: " + e.getMessage());
        }
    }

    public void bootstrapIfNeeded() {
        MongoCollection<Document> usersCol = getCollection("users");
        if (usersCol.countDocuments() > 0) {
            // Existing local databases may predate the explicit permission model.
            // Re-apply the canonical demo roles/users so every local account has
            // a usable, least-privilege session after a restart.
            ensureDemoRolesAndUsers();
            updateMissingEmbeddedRoleIds(usersCol);
            return;
        }

        System.out.println("Initializing MongoDB default database state (roles, permissions, wards, baselines, users, patients)...");

        // 1. Roles, permissions, and local demo users
        ensureDemoRolesAndUsers();

        // 2. Wards
        saveWard(1L, "Cardiology");
        saveWard(2L, "Neurology");
        saveWard(3L, "Orthopedics");
        saveWard(4L, "Pediatrics");
        saveWard(5L, "General Medicine");

        // 3. Baselines
        saveBaseline(1L, 2L, RoleBaseline.BaselineScope.ON_WARD, 50, BigDecimal.valueOf(1.5));
        saveBaseline(2L, 2L, RoleBaseline.BaselineScope.OFF_WARD, 5, BigDecimal.valueOf(1.5));
        saveBaseline(3L, 1L, RoleBaseline.BaselineScope.ANY, 30, BigDecimal.valueOf(1.5));
        saveBaseline(4L, 3L, RoleBaseline.BaselineScope.ANY, 15, BigDecimal.valueOf(1.5));
        saveBaseline(5L, 4L, RoleBaseline.BaselineScope.ANY, 20, BigDecimal.valueOf(1.5));

        // 4. Patients across wards
        String[] firstNames = {"Arjun", "Priya", "Rahul", "Ananya", "Rohan", "Sneha", "Vikram", "Deepa", "Siddharth", "Kavita",
                               "Aditya", "Pooja", "Varun", "Meera", "Karan", "Tanvi", "Nikhil", "Divya", "Gaurav", "Isha",
                               "Abhishek", "Shweta", "Manish", "Sunita", "Rajesh", "Anita", "Suresh", "Geeta", "Amit", "Rekha"};
        String[] lastNames = {"Sharma", "Verma", "Patel", "Mehta", "Rao", "Nair", "Iyer", "Reddy", "Gupta", "Malhotra"};

        Long[] wardIds = {1L, 2L, 3L, 4L, 5L};
        MongoCollection<Document> patientsCol = getCollection("patients");
        MongoCollection<Document> recordsCol = getCollection("records");
        MongoCollection<Document> careCol = getCollection("care_assignments");

        long recordSeq = 1;
        for (int i = 0; i < 50; i++) {
            long patientId = i + 1;
            String name = firstNames[i % firstNames.length] + " " + lastNames[i % lastNames.length];
            LocalDate dob = LocalDate.of(1960 + (i % 45), 1 + (i % 12), 1 + (i % 28));
            Long wardId = wardIds[i % wardIds.length];

            Document pDoc = new Document("patientId", patientId)
                .append("name", name)
                .append("dob", dob.toString())
                .append("wardId", wardId);
            patientsCol.insertOne(pDoc);

            // Clinical Record
            Document clinDoc = new Document("recordId", recordSeq++)
                .append("patientId", patientId)
                .append("type", "CLINICAL")
                .append("content", String.format("Patient %s: BP %d/%d mmHg, HR %d bpm. Assessment: Condition stable.",
                    name, 115 + (i % 25), 75 + (i % 15), 68 + (i % 20)));
            recordsCol.insertOne(clinDoc);

            // Billing Record
            Document billDoc = new Document("recordId", recordSeq++)
                .append("patientId", patientId)
                .append("type", "BILLING")
                .append("content", String.format("Invoice #INV-%05d: Inpatient stay ward charge INR %d. Status: PAID.",
                    i + 1, 1500 * ((i % 5) + 1)));
            recordsCol.insertOne(billDoc);

            // Care assignment for Cardiology (Ward 1)
            if (wardId.equals(1L)) {
                careCol.insertOne(new Document("userId", 1L).append("patientId", patientId));
                careCol.insertOne(new Document("userId", 2L).append("patientId", patientId));
            }
        }

        // Initialize sequences to high enough value
        initSequence("users", 10L);
        initSequence("patients", 60L);
        initSequence("records", recordSeq + 10L);
        initSequence("wards", 10L);
        initSequence("roles", 10L);
        initSequence("baselines", 10L);
        initSequence("access_logs", 1L);
        initSequence("alerts", 1L);
        initSequence("break_glass_events", 1L);

        System.out.println("MongoDB bootstrap completed: 8 demo users, 5 wards, 50 patients initialized in MongoDB.");
    }

    private void ensureDemoRolesAndUsers() {
        List<String> doctorPerms = List.of("VIEW_DEMOGRAPHICS", "VIEW_CLINICAL", "EXPORT");
        List<String> nursePerms = List.of("VIEW_DEMOGRAPHICS", "VIEW_CLINICAL");
        List<String> billingPerms = List.of("VIEW_DEMOGRAPHICS", "VIEW_BILLING");
        List<String> receptionistPerms = List.of("VIEW_DEMOGRAPHICS");
        List<String> adminPerms = List.of(
            "VIEW_DEMOGRAPHICS", "VIEW_CLINICAL", "VIEW_BILLING", "VIEW_ALERTS",
            "VIEW_LOGS", "EXPORT", "ADMIN"
        );
        List<String> reviewerPerms = List.of("VIEW_ALERTS", "VIEW_LOGS");

        saveRole(1L, "DOCTOR", doctorPerms);
        saveRole(2L, "NURSE", nursePerms);
        saveRole(3L, "BILLING", billingPerms);
        saveRole(4L, "RECEPTIONIST", receptionistPerms);
        saveRole(5L, "ADMIN", adminPerms);
        saveRole(6L, "REVIEWER", reviewerPerms);

        String pwHash = DEFAULT_PW_HASH;
        saveUser(1L, "dr.asha", pwHash, 1L, "DOCTOR", 1L, LocalTime.of(8, 0), LocalTime.of(20, 0), doctorPerms);
        saveUser(2L, "nurse.priya", pwHash, 2L, "NURSE", 1L, LocalTime.of(8, 0), LocalTime.of(20, 0), nursePerms);
        saveUser(3L, "clerk.ravi", pwHash, 3L, "BILLING", null, LocalTime.of(9, 0), LocalTime.of(18, 0), billingPerms);
        saveUser(4L, "rec.neha", pwHash, 4L, "RECEPTIONIST", null, LocalTime.of(9, 0), LocalTime.of(18, 0), receptionistPerms);
        saveUser(5L, "admin", pwHash, 5L, "ADMIN", null, LocalTime.of(0, 0), LocalTime.of(23, 59), adminPerms);
        saveUser(6L, "rev.meera", pwHash, 6L, "REVIEWER", null, LocalTime.of(0, 0), LocalTime.of(23, 59), reviewerPerms);

        // These two named accounts are the default frontend placeholders.
        // They use the same local-only demo password as the canonical accounts.
        saveUser(7L, "dr.elena.vance@stjude-health.org", pwHash, 1L, "DOCTOR", 1L,
            LocalTime.of(8, 0), LocalTime.of(20, 0), doctorPerms);
        saveUser(8L, "nurse.sarah.connor@stjude-health.org", pwHash, 2L, "NURSE", 1L,
            LocalTime.of(8, 0), LocalTime.of(20, 0), nursePerms);
    }

    private void initSequence(String name, long val) {
        getCollection("database_sequences").updateOne(
            Filters.eq("_id", name),
            Updates.set("seq", val),
            new UpdateOptions().upsert(true)
        );
    }

    private void saveRole(Long roleId, String name, List<String> perms) {
        Document doc = new Document("roleId", roleId)
            .append("name", name)
            .append("permissions", perms);
        getCollection("roles").replaceOne(Filters.eq("roleId", roleId), doc, new ReplaceOptions().upsert(true));
    }

    private void saveWard(Long wardId, String name) {
        Document doc = new Document("wardId", wardId).append("name", name);
        getCollection("wards").replaceOne(Filters.eq("wardId", wardId), doc, new ReplaceOptions().upsert(true));
    }

    private void saveBaseline(Long baselineId, Long roleId, RoleBaseline.BaselineScope scope, int maxRecords, BigDecimal mult) {
        Document doc = new Document("baselineId", baselineId)
            .append("roleId", roleId)
            .append("scope", scope.name())
            .append("maxRecordsPer10Min", maxRecords)
            .append("multiplier", mult.doubleValue());
        getCollection("role_baselines").replaceOne(Filters.eq("baselineId", baselineId), doc, new ReplaceOptions().upsert(true));
    }

    private void saveUser(Long userId, String username, String pwHash, Long roleId, String roleName, Long wardId,
                          LocalTime start, LocalTime end, List<String> perms) {
        Document roleDoc = new Document("roleId", roleId)
            .append("name", roleName)
            .append("permissions", perms);
        Document userDoc = new Document("userId", userId)
            .append("username", username)
            .append("pwHash", pwHash)
            .append("role", roleDoc)
            .append("wardId", wardId)
            .append("shiftStart", start != null ? start.toString() : null)
            .append("shiftEnd", end != null ? end.toString() : null)
            .append("status", "ACTIVE");
        getCollection("users").replaceOne(Filters.eq("userId", userId), userDoc, new ReplaceOptions().upsert(true));
    }

    /**
     * Adds only the missing embedded role ID to existing demo users. Existing
     * passwords, status, shifts, permissions, and other user data are left
     * untouched.
     */
    private void updateMissingEmbeddedRoleIds(MongoCollection<Document> usersCol) {
        Map<String, Long> roleIds = Map.of(
            "DOCTOR", 1L,
            "NURSE", 2L,
            "BILLING", 3L,
            "RECEPTIONIST", 4L,
            "ADMIN", 5L,
            "REVIEWER", 6L
        );

        for (Document userDoc : usersCol.find()) {
            if (!(userDoc.get("role") instanceof Document roleDoc)) {
                continue;
            }
            String roleName = roleDoc.getString("name");
            Long roleId = roleIds.get(roleName);
            if (roleId == null || roleDoc.get("roleId") != null) {
                continue;
            }
            Object userId = userDoc.get("userId");
            if (userId != null) {
                usersCol.updateOne(
                    Filters.eq("userId", userId),
                    Updates.set("role.roleId", roleId)
                );
            }
        }
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
