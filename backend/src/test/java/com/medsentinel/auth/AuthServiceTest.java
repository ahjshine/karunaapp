package com.medsentinel.auth;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void loginRejectsMalformedPasswordHashAndRecordsFailure() {
        UserRepository userRepository = mock(UserRepository.class);
        AccessLogService accessLogService = mock(AccessLogService.class);
        User user = new User();
        user.setUserId(7L);
        user.setUsername("dr.asha");
        user.setPwHash("not-a-valid-bcrypt-hash");

        when(userRepository.findByUsername("dr.asha")).thenReturn(Optional.of(user));

        AuthService authService = new AuthService(userRepository, accessLogService);

        assertTrue(authService.login("dr.asha", "password123", "10.0.0.7").isEmpty());
        verify(accessLogService).logAccess(argThat(log ->
            log.getUserId().equals(7L)
                && log.getOutcome() == AccessLog.Outcome.FAILED
                && "Bad credentials".equals(log.getReason())));
    }

    @Test
    void loginRejectsNullPasswordHashWithoutCallingTheHashVerifier() {
        UserRepository userRepository = mock(UserRepository.class);
        AccessLogService accessLogService = mock(AccessLogService.class);
        User user = new User();
        user.setUserId(8L);
        user.setUsername("nurse.priya");
        user.setPwHash(null);

        when(userRepository.findByUsername("nurse.priya")).thenReturn(Optional.of(user));

        AuthService authService = new AuthService(userRepository, accessLogService);

        assertTrue(authService.login("nurse.priya", "password123", null).isEmpty());
        verify(accessLogService).logAccess(any(AccessLog.class));
    }
}
