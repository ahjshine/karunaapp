package com.medsentinel.rbac;

import com.medsentinel.config.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.*;

public class RoleRepository {

    private final MongoDBManager mongoManager;

    public RoleRepository() {
        this(MongoDBManager.getInstance());
    }

    public RoleRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("roles");
    }

    public Role save(Role role) {
        if (role.getRoleId() == null) {
            role.setRoleId(mongoManager.getNextSequence("roles"));
        }
        Document doc = toDocument(role);
        col().replaceOne(Filters.eq("roleId", role.getRoleId()), doc, new ReplaceOptions().upsert(true));
        return role;
    }

    public Optional<Role> findById(Long roleId) {
        Document doc = col().find(Filters.eq("roleId", roleId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public Optional<Role> findByName(String name) {
        Document doc = col().find(Filters.eq("name", name)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<Role> findAll() {
        List<Role> list = new ArrayList<>();
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

    public static Document toDocument(Role role) {
        if (role == null) return null;
        Document doc = new Document("roleId", role.getRoleId()).append("name", role.getName());
        List<String> perms = role.getPermissions().stream().map(Permission::getCode).toList();
        doc.append("permissions", perms);
        return doc;
    }

    public static Role fromDocument(Document doc) {
        if (doc == null) return null;
        Role r = new Role();
        if (doc.containsKey("roleId")) r.setRoleId(doc.getLong("roleId"));
        r.setName(doc.getString("name"));
        if (doc.containsKey("permissions")) {
            List<?> raw = doc.getList("permissions", Object.class);
            Set<Permission> perms = new HashSet<>();
            for (Object o : raw) {
                if (o instanceof String s) perms.add(new Permission(s));
            }
            r.setPermissions(perms);
        }
        return r;
    }
}
