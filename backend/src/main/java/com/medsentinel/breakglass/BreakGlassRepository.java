package com.medsentinel.breakglass;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BreakGlassRepository {

    private final MongoDBManager mongoManager;

    public BreakGlassRepository() {
        this(MongoDBManager.getInstance());
    }

    public BreakGlassRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("break_glass_events");
    }

    public BreakGlassEvent save(BreakGlassEvent event) {
        if (event.getBgId() == null) {
            event.setBgId(mongoManager.getNextSequence("break_glass_events"));
        }
        Document doc = toDocument(event);
        col().replaceOne(Filters.eq("bgId", event.getBgId()), doc, new ReplaceOptions().upsert(true));
        return event;
    }

    public Optional<BreakGlassEvent> findById(Long bgId) {
        Document doc = col().find(Filters.eq("bgId", bgId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<BreakGlassEvent> findByReviewedFalseOrderByTsDesc() {
        List<BreakGlassEvent> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("reviewed", false)).sort(Sorts.descending("ts"))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<BreakGlassEvent> findByUserIdOrderByTsDesc(Long userId) {
        List<BreakGlassEvent> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("userId", userId)).sort(Sorts.descending("ts"))) {
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

    public static Document toDocument(BreakGlassEvent ev) {
        if (ev == null) return null;
        Document doc = new Document("bgId", ev.getBgId())
            .append("userId", ev.getUserId())
            .append("patientId", ev.getPatientId())
            .append("justification", ev.getJustification())
            .append("reviewed", ev.getReviewed());
        if (ev.getTs() != null) doc.append("ts", ev.getTs().toString());
        return doc;
    }

    public static BreakGlassEvent fromDocument(Document doc) {
        if (doc == null) return null;
        BreakGlassEvent ev = new BreakGlassEvent();
        if (doc.containsKey("bgId")) ev.setBgId(doc.getLong("bgId"));
        if (doc.containsKey("userId")) ev.setUserId(doc.getLong("userId"));
        if (doc.containsKey("patientId")) ev.setPatientId(doc.getLong("patientId"));
        ev.setJustification(doc.getString("justification"));
        if (doc.containsKey("reviewed")) ev.setReviewed(doc.getBoolean("reviewed"));
        if (doc.getString("ts") != null) ev.setTs(LocalDateTime.parse(doc.getString("ts")));
        return ev;
    }
}
