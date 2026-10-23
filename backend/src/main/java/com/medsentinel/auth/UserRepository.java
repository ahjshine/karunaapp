package com.medsentinel.auth;

import com.medsentinel.config.MongoDBManager;
import com.medsentinel.rbac.Permission;
import com.medsentinel.rbac.Role;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.time.LocalTime;
import java.util.*;

public class UserRepository {

    private final MongoDBManager mongoManager;

    public UserRepository() {
        this(MongoDBManager.getInstance());
    }

    public UserRepository(MongoDBManager mongoManager) {
        this.mongoManager = mongoManager;
    }

    private MongoCollection<Document> col() {
        return mongoManager.getCollection("users");
    }

    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(mongoManager.getNextSequence("users"));
        }
        Document doc = toDocument(user);
        col().replaceOne(Filters.eq("userId", user.getUserId()), doc, new ReplaceOptions().upsert(true));
        return user;
    }

    public Optional<User> findById(Long userId) {
        Document doc = col().find(Filters.eq("userId", userId)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        Document doc = col().find(Filters.eq("username", username)).first();
        return Optional.ofNullable(fromDocument(doc));
    }

    public List<User> findByStatus(User.UserStatus status) {
        List<User> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("status", status.name()))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<User> findByRoleRoleId(Long roleId) {
        List<User> list = new ArrayList<>();
        for (Document doc : col().find(Filters.eq("role.roleId", roleId))) {
            list.add(fromDocument(doc));
        }
        return list;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
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

    public static Document toDocument(User user) {
        if (user == null) return null;
        Document doc = new Document("userId", user.getUserId())
            .append("username", user.getUsername())
            .append("pwHash", user.getPwHash())
            .append("wardId", user.getWardId())
            .append("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");

        if (user.getShiftStart() != null) doc.append("shiftStart", user.getShiftStart().toString());
        if (user.getShiftEnd() != null) doc.append("shiftEnd", user.getShiftEnd().toString());

        if (user.getRole() != null) {
            Document roleDoc = new Document("name", user.getRole().getName());
            if (user.getRole().getRoleId() != null) roleDoc.append("roleId", user.getRole().getRoleId());
            List<String> permCodes = user.getRole().getPermissions() == null
                ? List.of()
                : user.getRole().getPermissions().stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getCode)
                    .filter(Objects::nonNull)
                    .toList();
            roleDoc.append("permissions", permCodes);
            doc.append("role", roleDoc);
        }
        return doc;
    }

    public static User fromDocument(Document doc) {
        if (doc == null) return null;
        User user = new User();
        if (doc.containsKey("userId")) user.setUserId(doc.getLong("userId"));
        user.setUsername(doc.getString("username"));
        user.setPwHash(doc.getString("pwHash"));
        if (doc.containsKey("wardId")) user.setWardId(doc.getLong("wardId"));
        if (doc.getString("shiftStart") != null) user.setShiftStart(LocalTime.parse(doc.getString("shiftStart")));
        if (doc.getString("shiftEnd") != null) user.setShiftEnd(LocalTime.parse(doc.getString("shiftEnd")));
        if (doc.getString("status") != null) user.setStatus(User.UserStatus.valueOf(doc.getString("status")));

        if (doc.containsKey("role") && doc.get("role") instanceof Document roleDoc) {
            Role role = new Role();
            Object rawRoleId = roleDoc.get("roleId");
            if (rawRoleId instanceof Number number) role.setRoleId(number.longValue());
            role.setName(roleDoc.getString("name"));
            if (roleDoc.containsKey("permissions")) {
                List<?> permsRaw = roleDoc.getList("permissions", Object.class);
                Set<Permission> perms = new HashSet<>();
                for (Object p : permsRaw) {
                    if (p instanceof String s) perms.add(new Permission(s));
                    else if (p instanceof Document pd) perms.add(new Permission(pd.getString("code")));
                }
                role.setPermissions(perms);
            }
            user.setRole(role);
        }
        return user;
    }
}
