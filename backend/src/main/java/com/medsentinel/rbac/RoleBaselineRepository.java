package com.medsentinel.rbac;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleBaselineRepository {

    private final MongoDBManager mongoManager;

    public RoleBaselineRepository() {
        this(MongoDBManager.getInstance());
    }

    public RoleBaselineRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("role_baselines");
    }

    public RoleBaseline save(RoleBaseline baseline) {
        if (baseline.getBaselineId() == null) {
            baseline.setBaselineId(mongoManager.getNextSequence("baselines"));
        }
        Document doc = toDocument(baseline);
        col().replaceOne(Filters.eq("baselineId", baseline.getBaselineId()), doc, new ReplaceOptions().upsert(true));
        return baseline;
    }

    public Optional<RoleBaseline> findById(Long baselineId) {
        Document doc = col().find(Filters.eq("baselineId", baselineId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<RoleBaseline> findByRoleId(Long roleId) {
        List<RoleBaseline> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("roleId", roleId))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public RoleBaseline findByRoleIdAndScope(Long roleId, RoleBaseline.BaselineScope scope) {
        Bson filter = Filters.and(Filters.eq("roleId", roleId), Filters.eq("scope", scope.name()));
        Document doc = col().find(filter).first();
        return fromDocument(doc);
    }

    public List<RoleBaseline> findAll() {
        List<RoleBaseline> list = new ArrayList<>();
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

    public static Document toDocument(RoleBaseline b) {
        if (b == null) return null;
        return new Document("baselineId", b.getBaselineId())
            .append("roleId", b.getRoleId())
            .append("scope", b.getScope() != null ? b.getScope().name() : null)
            .append("maxRecordsPer10Min", b.getMaxRecordsPer10Min())
            .append("multiplier", b.getMultiplier() != null ? b.getMultiplier().doubleValue() : 1.5);
    }

    public static RoleBaseline fromDocument(Document doc) {
        if (doc == null) return null;
        RoleBaseline b = new RoleBaseline();
        if (doc.containsKey("baselineId")) b.setBaselineId(doc.getLong("baselineId"));
        if (doc.containsKey("roleId")) b.setRoleId(doc.getLong("roleId"));
        if (doc.getString("scope") != null) b.setScope(RoleBaseline.BaselineScope.valueOf(doc.getString("scope")));
        if (doc.containsKey("maxRecordsPer10Min")) b.setMaxRecordsPer10Min(doc.getInteger("maxRecordsPer10Min"));
        if (doc.containsKey("multiplier")) b.setMultiplier(BigDecimal.valueOf(doc.getDouble("multiplier")));
        return b;
    }
}
