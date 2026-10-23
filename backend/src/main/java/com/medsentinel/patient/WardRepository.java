package com.medsentinel.patient;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WardRepository {

    private final MongoDBManager mongoManager;

    public WardRepository() {
        this(MongoDBManager.getInstance());
    }

    public WardRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("wards");
    }

    public Ward save(Ward ward) {
        if (ward.getWardId() == null) {
            ward.setWardId(mongoManager.getNextSequence("wards"));
        }
        Document doc = new Document("wardId", ward.getWardId()).append("name", ward.getName());
        col().replaceOne(Filters.eq("wardId", ward.getWardId()), doc, new ReplaceOptions().upsert(true));
        return ward;
    }

    public Optional<Ward> findById(Long wardId) {
        Document doc = col().find(Filters.eq("wardId", wardId)).first();
        if (doc == null) return Optional.empty();
        return Optional.of(new Ward(doc.getLong("wardId"), doc.getString("name")));
    }

    public List<Ward> findAll() {
        List<Ward> list = new ArrayList<>();
        for (Document doc : col().find()) {
            list.add(new Ward(doc.getLong("wardId"), doc.getString("name")));
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
