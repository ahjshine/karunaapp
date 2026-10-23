package com.medsentinel.patient;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class CareAssignmentRepository {

    private final MongoDBManager mongoManager;

    public CareAssignmentRepository() {
        this(MongoDBManager.getInstance());
    }

    public CareAssignmentRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("care_assignments");
    }

    public CareAssignment save(CareAssignment assignment) {
        Bson filter = Filters.and(
            Filters.eq("userId", assignment.getUserId()),
            Filters.eq("patientId", assignment.getPatientId())
        );
        Document doc = new Document("userId", assignment.getUserId())
            .append("patientId", assignment.getPatientId());
        col().replaceOne(filter, doc, new ReplaceOptions().upsert(true));
        return assignment;
    }

    public boolean existsByUserIdAndPatientId(Long userId, Long patientId) {
        if (userId == null || patientId == null) return false;
        Bson filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("patientId", patientId)
        );
        return col().countDocuments(filter) > 0;
    }

    public List<CareAssignment> findByUserId(Long userId) {
        List<CareAssignment> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("userId", userId))) {
            list.add(new CareAssignment(doc.getLong("userId"), doc.getLong("patientId")));
        }
        return list;
    }

    public long count() {
        return col().countDocuments();
    }

    public void deleteAll() {
        col().deleteMany(new Document());
    }
}
