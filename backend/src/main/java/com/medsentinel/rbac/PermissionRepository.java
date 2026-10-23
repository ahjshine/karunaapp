package com.medsentinel.rbac;

import java.util.Optional;

public class PermissionRepository {

    public Optional<Permission> findByCode(String code) {
        return Optional.of(new Permission(code));
    }

    public Permission save(Permission p) {
        return p;
    }

    public void deleteAll() {}
}
