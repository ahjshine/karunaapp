package com.medsentinel.logging;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccessLogRepository {

    private final MongoDBManager mongoManager;

    public AccessLogRepository() {
        this(MongoDBManager.getInstance());
    }

    public AccessLogRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("access_logs");
    }

    public long nextId() {
        return mongoManager.getNextSequence("access_logs");
    }

    /**
     * Append a new audit row. Existing log IDs are never overwritten through
     * the normal write path.
     */
    public AccessLog append(AccessLog entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Access log entry is required");
        }
        if (entry.getLogId() == null) {
            entry.setLogId(nextId());
        }
        if (col().find(Filters.eq("logId", entry.getLogId())).first() != null) {
            throw new IllegalStateException("Audit log rows are append-only");
        }
        col().insertOne(toDocument(entry));
        return entry;
    }

    /**
     * Compatibility alias for older callers. It retains append-only semantics.
     */
    public AccessLog save(AccessLog entry) {
        return append(entry);
    }

    /**
     * Explicitly controlled update path used only by the admin tamper demo.
     * Ordinary application writes must call append/save instead.
     */
    public AccessLog updateForTamper(AccessLog entry) {
        if (entry == null || entry.getLogId() == null) {
            throw new IllegalArgumentException("Existing audit log entry is required");
        }
        if (col().find(Filters.eq("logId", entry.getLogId())).first() == null) {
            throw new IllegalArgumentException("Audit log entry was not found");
        }
        col().replaceOne(Filters.eq("logId", entry.getLogId()), toDocument(entry));
        return entry;
    }

    public Optional<AccessLog> findById(Long logId) {
        Document doc = col().find(Filters.eq("logId", logId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public AccessLog findTopByOrderByLogIdDesc() {
        Document doc = col().find().sort(Sorts.descending("logId")).first();
        return fromDocument(doc);
    }

    public List<AccessLog> findAllOrderedById() {
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find().sort(Sorts.ascending("logId"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findAll() {
        return findAllOrderedById();
    }

    public List<AccessLog> findFailedLoginsSince(LocalDateTime since) {
        Bson filter = Filters.and(
            Filters.eq("action", "LOGIN"),
            Filters.eq("outcome", "FAILED"),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.ascending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findFailedLoginsForAttemptSince(String username, String ip, LocalDateTime since) {
        List<Bson> identities = new ArrayList<>();
        if (username != null && !username.isBlank()) {
            identities.add(Filters.eq("usernameAttempted", username));
        }
        if (ip != null && !ip.isBlank()) {
            identities.add(Filters.eq("ip", ip));
        }
        if (identities.isEmpty()) {
            return List.of();
        }

        Bson filter = Filters.and(
            Filters.or(identities),
            Filters.eq("action", "LOGIN"),
            Filters.eq("outcome", "FAILED"),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.ascending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findAllowedAccessByUserSince(Long userId, LocalDateTime since) {
        Bson filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.in("action", "VIEW", "SEARCH", "EXPORT"),
            Filters.eq("outcome", "ALLOWED"),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.ascending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findDeniedAccessSince(LocalDateTime since) {
        Bson filter = Filters.and(
            Filters.eq("outcome", "DENIED"),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.ascending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findDeniedAccessForUserSince(Long userId, String username, LocalDateTime since) {
        Bson identity = userId != null
            ? Filters.eq("userId", userId)
            : Filters.eq("usernameAttempted", username);
        Bson filter = Filters.and(
            identity,
            Filters.eq("outcome", "DENIED"),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.ascending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findSuccessfulLoginsSince(String username, LocalDateTime since) {
        Bson filter = Filters.and(
            Filters.eq("action", "LOGIN"),
            Filters.eq("outcome", "ALLOWED"),
            Filters.eq("usernameAttempted", username),
            Filters.gte("ts", since.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.descending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findByUserIdAndTsBetweenOrderByTsDesc(Long userId, LocalDateTime start, LocalDateTime end) {
        Bson filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.gte("ts", start.toString()),
            Filters.lte("ts", end.toString())
        );
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.descending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findTop100ByOrderByTsDesc() {
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find().sort(Sorts.descending("ts")).limit(100)) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<AccessLog> findTop20ByOrderByTsDesc() {
        List<AccessLog> list = new ArrayList<>();
        for (Document doc : col().find().sort(Sorts.descending("ts")).limit(20)) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public long countFailedLoginsSince(LocalDateTime since) {
        Bson filter = Filters.and(
            Filters.eq("action", "LOGIN"),
            Filters.eq("outcome", "FAILED"),
            Filters.gte("ts", since.toString())
        );
        return col().countDocuments(filter);
    }

    public long count() {
        return col().countDocuments();
    }

    public void deleteAll() {
        col().deleteMany(new Document());
    }

    public static Document toDocument(AccessLog log) {
        if (log == null) return null;
        Document doc = new Document("logId", log.getLogId());
        if (log.getTs() != null) doc.append("ts", log.getTs().toString());
        if (log.getUserId() != null) doc.append("userId", log.getUserId());
        if (log.getUsernameAttempted() != null) doc.append("usernameAttempted", log.getUsernameAttempted());
        if (log.getIp() != null) doc.append("ip", log.getIp());
        if (log.getAction() != null) doc.append("action", log.getAction());
        if (log.getResourceType() != null) doc.append("resourceType", log.getResourceType());
        if (log.getResourceId() != null) doc.append("resourceId", log.getResourceId());
        if (log.getPatientId() != null) doc.append("patientId", log.getPatientId());
        if (log.getOutcome() != null) doc.append("outcome", log.getOutcome().name());
        if (log.getReason() != null) doc.append("reason", log.getReason());
        if (log.getPrevHash() != null) doc.append("prevHash", log.getPrevHash());
        if (log.getRowHash() != null) doc.append("rowHash", log.getRowHash());
        return doc;
    }

    public static AccessLog fromDocument(Document doc) {
        if (doc == null) return null;
        AccessLog log = new AccessLog();
        if (doc.containsKey("logId")) log.setLogId(doc.getLong("logId"));
        if (doc.getString("ts") != null) log.setTs(LocalDateTime.parse(doc.getString("ts")));
        if (doc.containsKey("userId")) log.setUserId(doc.getLong("userId"));
        log.setUsernameAttempted(doc.getString("usernameAttempted"));
        log.setIp(doc.getString("ip"));
        log.setAction(doc.getString("action"));
        log.setResourceType(doc.getString("resourceType"));
        if (doc.containsKey("resourceId")) log.setResourceId(doc.getLong("resourceId"));
        if (doc.containsKey("patientId")) log.setPatientId(doc.getLong("patientId"));
        if (doc.getString("outcome") != null) log.setOutcome(AccessLog.Outcome.valueOf(doc.getString("outcome")));
        log.setReason(doc.getString("reason"));
        log.setPrevHash(doc.getString("prevHash"));
        log.setRowHash(doc.getString("rowHash"));
        return log;
    }
}
