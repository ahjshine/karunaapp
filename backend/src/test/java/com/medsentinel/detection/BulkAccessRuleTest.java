package com.medsentinel.detection;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.rbac.AccessGuard;
import com.medsentinel.rbac.Role;
import com.medsentinel.rbac.RoleBaseline;
import com.medsentinel.rbac.RoleBaselineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BulkAccessRuleTest {

    private AccessLogRepository accessLogRepo;
    private UserRepository userRepo;
    private RoleBaselineRepository baselineRepo;
    private AccessGuard accessGuard;
    private BulkAccessRule rule;

    private User nurse;
    private User clerk;
    private Role nurseRole;
    private Role clerkRole;

    @BeforeEach
    void setUp() {
        accessLogRepo = mock(AccessLogRepository.class);
        userRepo = mock(UserRepository.class);
        baselineRepo = mock(RoleBaselineRepository.class);
        accessGuard = mock(AccessGuard.class);
        rule = new BulkAccessRule(accessLogRepo, userRepo, baselineRepo, accessGuard, 10);

        nurseRole = new Role("NURSE");
        nurseRole.setRoleId(2L);

        nurse = new User();
        nurse.setUserId(2L);
        nurse.setUsername("nurse.priya");
        nurse.setRole(nurseRole);
        nurse.setWardId(1L);

        clerkRole = new Role("BILLING");
        clerkRole.setRoleId(3L);

        clerk = new User();
        clerk.setUserId(3L);
        clerk.setUsername("clerk.ravi");
        clerk.setRole(clerkRole);

        when(userRepo.findById(2L)).thenReturn(Optional.of(nurse));
        when(userRepo.findById(3L)).thenReturn(Optional.of(clerk));
    }

    @Test
    void busyClinicianBurst_40OwnWardCharts_ProducesNoAlert() {
        // Nurse baseline: ON_WARD = 50 * 1.5 = 75, ANY = 55 * 1.5 = 82
        RoleBaseline onWardBaseline = new RoleBaseline();
        onWardBaseline.setMaxRecordsPer10Min(50);
        onWardBaseline.setMultiplier(BigDecimal.valueOf(1.5));

        RoleBaseline anyBaseline = new RoleBaseline();
        anyBaseline.setMaxRecordsPer10Min(55);
        anyBaseline.setMultiplier(BigDecimal.valueOf(1.5));

        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.ON_WARD)).thenReturn(onWardBaseline);
        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.OFF_WARD)).thenReturn(null);
        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.ANY)).thenReturn(anyBaseline);

        List<AccessLog> logs = new ArrayList<>();
        for (long i = 1; i <= 40; i++) {
            AccessLog log = new AccessLog().userId(2L).usernameAttempted("nurse.priya")
                .action("VIEW").outcome(AccessLog.Outcome.ALLOWED).patientId(i);
            log.setLogId(i);
            log.setTs(LocalDateTime.now().minusMinutes(2));
            logs.add(log);
        }

        when(accessLogRepo.findAllowedAccessByUserSince(eq(2L), any())).thenReturn(logs);
        // All patients are on ward
        when(accessGuard.isOnWardOrAssigned(eq(2L), anyLong(), eq(1L))).thenReturn(true);

        List<RuleHit> hits = rule.evaluate(logs.get(39));

        assertTrue(hits.isEmpty(), "Nurse opening 40 own-ward charts must not be flagged (busy-clinician guard)");
    }

    @Test
    void nurseAccessing40OffWardCharts_FiresR2() {
        // Off-ward baseline = 5 * 1.5 = 7
        RoleBaseline offWardBaseline = new RoleBaseline();
        offWardBaseline.setMaxRecordsPer10Min(5);
        offWardBaseline.setMultiplier(BigDecimal.valueOf(1.5));

        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.ON_WARD)).thenReturn(null);
        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.OFF_WARD)).thenReturn(offWardBaseline);
        when(baselineRepo.findByRoleIdAndScope(2L, RoleBaseline.BaselineScope.ANY)).thenReturn(null);

        List<AccessLog> logs = new ArrayList<>();
        for (long i = 1; i <= 40; i++) {
            AccessLog log = new AccessLog().userId(2L).usernameAttempted("nurse.priya")
                .action("VIEW").outcome(AccessLog.Outcome.ALLOWED).patientId(100L + i);
            log.setLogId(i);
            log.setTs(LocalDateTime.now().minusMinutes(2));
            logs.add(log);
        }

        when(accessLogRepo.findAllowedAccessByUserSince(eq(2L), any())).thenReturn(logs);
        // None on ward
        when(accessGuard.isOnWardOrAssigned(eq(2L), anyLong(), eq(1L))).thenReturn(false);

        List<RuleHit> hits = rule.evaluate(logs.get(39));

        assertFalse(hits.isEmpty());
        assertEquals("R2", hits.get(0).getRuleCode());
        assertEquals(35, hits.get(0).getPoints());
        assertTrue(hits.get(0).getExplanation().contains("off-ward"));
    }

    @Test
    void billingClerkPulling38Records_ExceedsBaseline_FiresR2() {
        // Clerk baseline: ANY = 15 * 1.5 = 22
        RoleBaseline anyBaseline = new RoleBaseline();
        anyBaseline.setMaxRecordsPer10Min(15);
        anyBaseline.setMultiplier(BigDecimal.valueOf(1.5));

        when(baselineRepo.findByRoleIdAndScope(3L, RoleBaseline.BaselineScope.ON_WARD)).thenReturn(null);
        when(baselineRepo.findByRoleIdAndScope(3L, RoleBaseline.BaselineScope.OFF_WARD)).thenReturn(null);
        when(baselineRepo.findByRoleIdAndScope(3L, RoleBaseline.BaselineScope.ANY)).thenReturn(anyBaseline);

        List<AccessLog> logs = new ArrayList<>();
        for (long i = 1; i <= 38; i++) {
            AccessLog log = new AccessLog().userId(3L).usernameAttempted("clerk.ravi")
                .action("VIEW").outcome(AccessLog.Outcome.ALLOWED).patientId(i);
            log.setLogId(i);
            log.setTs(LocalDateTime.now().minusMinutes(1));
            logs.add(log);
        }

        when(accessLogRepo.findAllowedAccessByUserSince(eq(3L), any())).thenReturn(logs);
        when(accessGuard.isOnWardOrAssigned(eq(3L), anyLong(), any())).thenReturn(false);

        List<RuleHit> hits = rule.evaluate(logs.get(37));

        assertFalse(hits.isEmpty());
        assertEquals("R2", hits.get(0).getRuleCode());
        assertEquals(35, hits.get(0).getPoints());
        assertTrue(hits.get(0).getExplanation().contains("38 total records"));
    }
}
