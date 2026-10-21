package com.medsentinel.detection;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OffHoursRuleTest {

    @Test
    void accessOutsideShiftCreatesR4Hit() {
        UserRepository users = mock(UserRepository.class);
        User user = new User();
        user.setUserId(2L);
        user.setUsername("nurse.priya");
        user.setShiftStart(LocalTime.of(8, 0));
        user.setShiftEnd(LocalTime.of(20, 0));
        when(users.findById(2L)).thenReturn(Optional.of(user));
        OffHoursRule rule = new OffHoursRule(users);
        AccessLog access = new AccessLog().userId(2L).usernameAttempted("nurse.priya")
            .action("VIEW").outcome(AccessLog.Outcome.ALLOWED);
        access.setLogId(1L);
        access.setTs(LocalDateTime.of(2026, 10, 18, 23, 0));

        RuleHit hit = rule.evaluate(access).get(0);

        assertEquals("R4", hit.getRuleCode());
        assertEquals(20, hit.getPoints());
    }

    @Test
    void accessWithinShiftDoesNotCreateHit() {
        UserRepository users = mock(UserRepository.class);
        User user = new User();
        user.setUserId(2L);
        user.setShiftStart(LocalTime.of(8, 0));
        user.setShiftEnd(LocalTime.of(20, 0));
        when(users.findById(2L)).thenReturn(Optional.of(user));
        OffHoursRule rule = new OffHoursRule(users);
        AccessLog access = new AccessLog().userId(2L).usernameAttempted("nurse.priya")
            .action("VIEW").outcome(AccessLog.Outcome.ALLOWED);
        access.setTs(LocalDateTime.of(2026, 10, 18, 12, 0));

        assertTrue(rule.evaluate(access).isEmpty());
    }
}
