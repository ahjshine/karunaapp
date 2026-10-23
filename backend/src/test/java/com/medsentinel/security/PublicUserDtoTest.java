package com.medsentinel.security;

import com.google.gson.Gson;
import com.medsentinel.auth.User;
import com.medsentinel.rbac.Permission;
import com.medsentinel.rbac.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicUserDtoTest {

    @Test
    void userResponseDoesNotExposePasswordHash() {
        Role role = new Role(1L, "DOCTOR");
        role.getPermissions().add(new Permission("VIEW_CLINICAL"));

        User user = new User();
        user.setUserId(1L);
        user.setUsername("dr.asha");
        user.setPwHash("$2a$10$private-password-hash");
        user.setRole(role);

        PublicUserDto dto = PublicUserDto.from(user);
        String json = new Gson().toJson(dto);

        assertTrue(json.contains("dr.asha"));
        assertTrue(json.contains("VIEW_CLINICAL"));
        assertFalse(json.contains("pwHash"));
        assertFalse(json.contains("private-password-hash"));
    }
}
