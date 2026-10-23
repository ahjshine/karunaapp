package com.medsentinel.alert;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AlertEvidenceRepository {

    private final MongoDBManager mongoManager;

    public AlertEvidenceRepository() {
        this(MongoDBManager.getInstance());
    }

    public AlertEvidenceRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("alert_evidence");
    }

    public void save(AlertEvidence evidence) {
        Document doc = new Document("alertId", evidence.getAlertId()).append("logId", evidence.getLogId());
        col().replaceOne(
            Filters.and(Filters.eq("alertId", evidence.getAlertId()), Filters.eq("logId", evidence.getLogId())),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public boolean existsById(AlertEvidenceId id) {
        return col().countDocuments(
            Filters.and(Filters.eq("alertId", id.getAlertId()), Filters.eq("logId", id.getLogId()))
        ) > 0;
    }

    public List<AlertEvidence> findByAlertId(Long alertId) {
        List<AlertEvidence> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("alertId", alertId))) {
            list.add(new AlertEvidence(doc.getLong("alertId"), doc.getLong("logId")));
        }
        return list;
    }

    public void deleteAll() {
        col().deleteMany(new Document());
    }
}
