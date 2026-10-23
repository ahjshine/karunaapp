package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutOfScopeRuleTest {

    @Test
    void deniedClinicalAccessCreatesR3Evidence() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        OutOfScopeRule rule = new OutOfScopeRule(repo);
        AccessLog denied = new AccessLog().userId(3L).usernameAttempted("clerk.ravi")
            .action("VIEW").resourceType("CLINICAL").patientId(1L)
            .outcome(AccessLog.Outcome.DENIED).reason("Role BILLING lacks permission VIEW_CLINICAL");
        denied.setLogId(23L);
        when(repo.findDeniedAccessForUserSince(eq(3L), eq("clerk.ravi"), any())).thenReturn(List.of(denied));

        RuleHit hit = rule.evaluate(denied).get(0);

        assertEquals("R3", hit.getRuleCode());
        assertEquals(40, hit.getPoints());
        assertEquals(List.of(23L), hit.getEvidenceLogIds());
        assertTrue(hit.getExplanation().contains("without permission"));
    }
}
