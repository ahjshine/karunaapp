package com.medsentinel.alert;

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

public class AlertRepository {

    private final MongoDBManager mongoManager;

    public AlertRepository() {
        this(MongoDBManager.getInstance());
    }

    public AlertRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("alerts");
    }

    public Alert save(Alert alert) {
        if (alert.getAlertId() == null) {
            alert.setAlertId(mongoManager.getNextSequence("alerts"));
        }
        Document doc = toDocument(alert);
        col().replaceOne(Filters.eq("alertId", alert.getAlertId()), doc, new ReplaceOptions().upsert(true));
        return alert;
    }

    public Optional<Alert> findById(Long alertId) {
        Document doc = col().find(Filters.eq("alertId", alertId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<Alert> findOpenAlerts() {
        Bson filter = Filters.in("status", "OPEN", "ACK", "ESCALATED");
        List<Alert> list = new ArrayList<>();
        // Sort: HIGH first, then MEDIUM, then LOW, then lastSeen desc
        for (Document doc : col().find(filter).sort(Sorts.descending("lastSeen"))) {
            list.add(fromDocument(doc));
        }
        list.sort((a, b) -> {
            int sa = a.getSeverity() == Alert.Severity.HIGH ? 0 : (a.getSeverity() == Alert.Severity.MEDIUM ? 1 : 2);
            int sb = b.getSeverity() == Alert.Severity.HIGH ? 0 : (b.getSeverity() == Alert.Severity.MEDIUM ? 1 : 2);
            if (sa != sb) return Integer.compare(sa, sb);
            return b.getLastSeen().compareTo(a.getLastSeen());
        });
        return list;
    }

    public Optional<Alert> findForDedup(Long userId, String ruleCodes, LocalDateTime windowStart) {
        Bson filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("ruleCodes", ruleCodes),
            Filters.eq("status", "OPEN"),
            Filters.gte("lastSeen", windowStart.toString())
        );
        Document doc = col().find(filter).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<Alert> findByUserIdOrderByLastSeenDesc(Long userId) {
        Bson filter = Filters.eq("userId", userId);
        List<Alert> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.descending("lastSeen"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<Alert> findOpenForUserSince(Long userId, LocalDateTime windowStart) {
        Bson filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("status", "OPEN"),
            Filters.gte("lastSeen", windowStart.toString())
        );
        List<Alert> list = new ArrayList<>();
        for (Document doc : col().find(filter).sort(Sorts.descending("lastSeen"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public long countOpenAlerts() {
        return col().countDocuments(Filters.in("status", "OPEN", "ACK", "ESCALATED"));
    }

    public long countHighSeverityOpen() {
        return col().countDocuments(Filters.and(
            Filters.eq("severity", "HIGH"),
            Filters.in("status", "OPEN", "ACK", "ESCALATED")
        ));
    }

    public void deleteAll() {
        col().deleteMany(new Document());
    }

    public static Document toDocument(Alert alert) {
        if (alert == null) return null;
        Document doc = new Document("alertId", alert.getAlertId());
        if (alert.getUserId() != null) doc.append("userId", alert.getUserId());
        if (alert.getRuleCodes() != null) doc.append("ruleCodes", alert.getRuleCodes());
        if (alert.getScore() != null) doc.append("score", alert.getScore());
        if (alert.getSeverity() != null) doc.append("severity", alert.getSeverity().name());
        if (alert.getExplanation() != null) doc.append("explanation", alert.getExplanation());
        if (alert.getStatus() != null) doc.append("status", alert.getStatus().name());
        if (alert.getReviewerNote() != null) doc.append("reviewerNote", alert.getReviewerNote());
        if (alert.getFirstSeen() != null) doc.append("firstSeen", alert.getFirstSeen().toString());
        if (alert.getLastSeen() != null) doc.append("lastSeen", alert.getLastSeen().toString());
        if (alert.getEventCount() != null) doc.append("eventCount", alert.getEventCount());
        if (alert.getEvidenceLogIds() != null) doc.append("evidenceLogIds", alert.getEvidenceLogIds());
        return doc;
    }

    @SuppressWarnings("unchecked")
    public static Alert fromDocument(Document doc) {
        if (doc == null) return null;
        Alert alert = new Alert();
        if (doc.containsKey("alertId")) alert.setAlertId(doc.getLong("alertId"));
        if (doc.containsKey("userId")) alert.setUserId(doc.getLong("userId"));
        alert.setRuleCodes(doc.getString("ruleCodes"));
        if (doc.containsKey("score")) alert.setScore(doc.getInteger("score"));
        if (doc.getString("severity") != null) alert.setSeverity(Alert.Severity.valueOf(doc.getString("severity")));
        alert.setExplanation(doc.getString("explanation"));
        if (doc.getString("status") != null) alert.setStatus(Alert.AlertStatus.valueOf(doc.getString("status")));
        alert.setReviewerNote(doc.getString("reviewerNote"));
        if (doc.getString("firstSeen") != null) alert.setFirstSeen(LocalDateTime.parse(doc.getString("firstSeen")));
        if (doc.getString("lastSeen") != null) alert.setLastSeen(LocalDateTime.parse(doc.getString("lastSeen")));
        if (doc.containsKey("eventCount")) alert.setEventCount(doc.getInteger("eventCount"));
        if (doc.containsKey("evidenceLogIds")) {
            List<?> rawList = doc.getList("evidenceLogIds", Object.class);
            List<Long> ids = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof Number num) ids.add(num.longValue());
            }
            alert.setEvidenceLogIds(ids);
        }
        return alert;
    }
}
