package com.medsentinel.scoring;

import com.medsentinel.alert.AlertService;
import com.medsentinel.detection.RuleHit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoringServiceTest {

    @Test
    void combinesR2AndR3IntoHighSeverityAlert() {
        AlertService alertService = mock(AlertService.class);
        ScoringService scoring = new ScoringService(alertService);
        RuleHit bulk = new RuleHit("R2", 3L, "clerk.ravi", 35, "38 records retrieved", List.of(10L, 11L));
        RuleHit scope = new RuleHit("R3", 3L, "clerk.ravi", 40, "clinical record denied", List.of(12L));

        scoring.processHits(List.of(bulk, scope));

        verify(alertService).createOrMergeAlert(eq(3L), eq("R2,R3"), eq(75), eq("HIGH"),
            contains("38 records"), eq(List.of(10L, 11L, 12L)));
    }
}
