package com.medsentinel.security;

import com.medsentinel.auth.AppUserDetails;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSecurityTest {

    private final ApiSecurity security = new ApiSecurity(
        new SessionStore(Duration.ofMinutes(30)),
        Set.of("http://localhost:5173"),
        false
    );

    @Test
    void unauthenticatedRequestsAreRejectedWithSafeApiError() {
        ApiException exception = assertThrows(ApiException.class, () ->
            security.requireAuthenticated(null));

        assertEquals(401, exception.getStatus());
        assertEquals("UNAUTHENTICATED", exception.getCode());
    }

    @Test
    void permissionsMustBeExplicitEvenWhenAUserHasARole() {
        AppUserDetails doctor = new AppUserDetails(
            1L, "dr.asha", "hash", "DOCTOR", 1L,
            List.of("ROLE_DOCTOR", "VIEW_CLINICAL")
        );

        assertDoesNotThrow(() -> security.requirePermission(doctor, "VIEW_CLINICAL"));
        ApiException exception = assertThrows(ApiException.class, () ->
            security.requirePermission(doctor, "VIEW_BILLING"));

        assertEquals(403, exception.getStatus());
        assertEquals("FORBIDDEN", exception.getCode());
    }

    @Test
    void sessionCookieIsOpaqueHttpOnlyAndSameSite() {
        String cookie = security.sessionCookie("opaque-token");

        assertTrue(cookie.startsWith("MEDSENTINEL_SESSION=opaque-token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertFalse(cookie.contains(" Secure"));
    }
}
