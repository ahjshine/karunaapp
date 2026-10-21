package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailedLoginRuleTest {

    @Test
    void createsR1HitForFiveFailuresInWindow() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        FailedLoginRule rule = new FailedLoginRule(repo, 5, 5);
        List<AccessLog> failures = java.util.stream.IntStream.rangeClosed(1, 5)
            .mapToObj(i -> failed(i)).toList();
        when(repo.findFailedLoginsSince(any())).thenReturn(failures);

        List<RuleHit> hits = rule.evaluate(failures.get(4));

        assertEquals(1, hits.size());
        assertEquals("R1", hits.get(0).getRuleCode());
        assertEquals(30, hits.get(0).getPoints());
        assertEquals(5, hits.get(0).getEvidenceLogIds().size());
    }

    @Test
    void createsR1bWhenLoginSucceedsAfterThresholdFailures() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        FailedLoginRule rule = new FailedLoginRule(repo, 5, 5);
        List<AccessLog> failures = java.util.stream.IntStream.rangeClosed(1, 5)
            .mapToObj(i -> failed(i)).toList();
        when(repo.findFailedLoginsSince(any())).thenReturn(failures);
        AccessLog success = new AccessLog().usernameAttempted("dr.asha").action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED);
        success.setLogId(6L);

        List<RuleHit> hits = rule.evaluate(success);

        assertEquals(2, hits.size());
        assertTrue(hits.stream().anyMatch(hit -> hit.getRuleCode().equals("R1b") && hit.getPoints() == 25));
    }

    private AccessLog failed(long id) {
        AccessLog log = new AccessLog().userId(1L).usernameAttempted("dr.asha")
            .ip("10.0.0.1").action("LOGIN").outcome(AccessLog.Outcome.FAILED);
        log.setLogId(id);
        log.setTs(LocalDateTime.now());
        return log;
    }
}
