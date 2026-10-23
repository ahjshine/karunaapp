package com.medsentinel.patient;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordRepository {

    private final MongoDBManager mongoManager;

    public RecordRepository() {
        this(MongoDBManager.getInstance());
    }

    public RecordRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("records");
    }

    public PatientRecord save(PatientRecord record) {
        if (record.getRecordId() == null) {
            record.setRecordId(mongoManager.getNextSequence("records"));
        }
        Document doc = toDocument(record);
        col().replaceOne(Filters.eq("recordId", record.getRecordId()), doc, new ReplaceOptions().upsert(true));
        return record;
    }

    public Optional<PatientRecord> findById(Long recordId) {
        Document doc = col().find(Filters.eq("recordId", recordId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<PatientRecord> findByPatientId(Long patientId) {
        List<PatientRecord> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("patientId", patientId))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<PatientRecord> findByPatientIdAndType(Long patientId, PatientRecord.RecordType type) {
        Bson filter = Filters.and(
            Filters.eq("patientId", patientId),
            Filters.eq("type", type.name())
        );
        List<PatientRecord> list = new ArrayList<>();
        for (Document doc : col().find(filter)) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<PatientRecord> findAll() {
        List<PatientRecord> list = new ArrayList<>();
        for (Document doc : col().find()) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public long count() {
        return col().countDocuments();
    }

    public void deleteAll() {
        col().deleteMany(new Document());
    }

    public static Document toDocument(PatientRecord r) {
        if (r == null) return null;
        return new Document("recordId", r.getRecordId())
            .append("patientId", r.getPatientId())
            .append("type", r.getType() != null ? r.getType().name() : null)
            .append("content", r.getContent());
    }

    public static PatientRecord fromDocument(Document doc) {
        if (doc == null) return null;
        PatientRecord r = new PatientRecord();
        if (doc.containsKey("recordId")) r.setRecordId(doc.getLong("recordId"));
        if (doc.containsKey("patientId")) r.setPatientId(doc.getLong("patientId"));
        if (doc.getString("type") != null) r.setType(PatientRecord.RecordType.valueOf(doc.getString("type")));
        r.setContent(doc.getString("content"));
        return r;
    }
}
