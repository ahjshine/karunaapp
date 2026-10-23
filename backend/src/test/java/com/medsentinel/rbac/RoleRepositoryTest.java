package com.medsentinel.rbac;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleRepositoryTest {

    @Test
    void roleBootstrapDocumentPreservesRoleIdAndPermissions() {
        Role role = new Role(6L, "REVIEWER");
        role.getPermissions().add(new Permission("VIEW_ALERTS"));
        role.getPermissions().add(new Permission("VIEW_LOGS"));

        Document document = RoleRepository.toDocument(role);
        Role restored = RoleRepository.fromDocument(document);

        assertEquals(6L, document.getLong("roleId"));
        assertNotNull(restored);
        assertEquals(6L, restored.getRoleId());
        assertEquals("REVIEWER", restored.getName());
        assertEquals(2, restored.getPermissions().size());
        assertEquals("VIEW_ALERTS", restored.getPermissions().stream()
            .filter(permission -> "VIEW_ALERTS".equals(permission.getCode()))
            .findFirst()
            .orElseThrow()
            .getCode());
    }
}
