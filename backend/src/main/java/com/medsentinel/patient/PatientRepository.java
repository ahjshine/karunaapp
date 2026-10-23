package com.medsentinel.patient;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientRepository {

    private final MongoDBManager mongoManager;

    public PatientRepository() {
        this(MongoDBManager.getInstance());
    }

    public PatientRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("patients");
    }

    public Patient save(Patient patient) {
        if (patient.getPatientId() == null) {
            patient.setPatientId(mongoManager.getNextSequence("patients"));
        }
        Document doc = toDocument(patient);
        col().replaceOne(Filters.eq("patientId", patient.getPatientId()), doc, new ReplaceOptions().upsert(true));
        return patient;
    }

    public Optional<Patient> findById(Long patientId) {
        Document doc = col().find(Filters.eq("patientId", patientId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<Patient> findByWardId(Long wardId) {
        List<Patient> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("wardId", wardId))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<Patient> findByNameContaining(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Bson filter = Filters.regex("name", Pattern.quote(query.trim()), "i");
        List<Patient> list = new ArrayList<>();
        for (Document doc : col().find(filter)) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
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

    public static Document toDocument(Patient p) {
        if (p == null) return null;
        Document doc = new Document("patientId", p.getPatientId())
            .append("name", p.getName())
            .append("wardId", p.getWardId());
        if (p.getDob() != null) doc.append("dob", p.getDob().toString());
        return doc;
    }

    public static Patient fromDocument(Document doc) {
        if (doc == null) return null;
        Patient p = new Patient();
        if (doc.containsKey("patientId")) p.setPatientId(doc.getLong("patientId"));
        p.setName(doc.getString("name"));
        if (doc.containsKey("wardId")) p.setWardId(doc.getLong("wardId"));
        if (doc.getString("dob") != null) p.setDob(LocalDate.parse(doc.getString("dob")));
        return p;
    }

}
