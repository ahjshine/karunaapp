package com.medsentinel.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    private AlertRepository alertRepo;
    private AlertEvidenceRepository evidenceRepo;
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertRepo = mock(AlertRepository.class);
        evidenceRepo = mock(AlertEvidenceRepository.class);
        alertService = new AlertService(alertRepo, evidenceRepo);
    }

    @Test
    void createNewAlert_WhenNoExistingAlert() {
        when(alertRepo.findOpenForUserSince(eq(3L), any())).thenReturn(List.of());

        Alert savedAlert = new Alert();
        savedAlert.setAlertId(1L);
        savedAlert.setUserId(3L);
        savedAlert.setRuleCodes("R3");
        savedAlert.setScore(40);
        savedAlert.setSeverity(Alert.Severity.MEDIUM);
        savedAlert.setExplanation("Out-of-scope access");

        when(alertRepo.save(any(Alert.class))).thenReturn(savedAlert);

        Alert alert = alertService.createOrMergeAlert(3L, "R3", 40, "MEDIUM", "Out-of-scope access", List.of(101L));

        assertNotNull(alert);
        assertEquals(1L, alert.getAlertId());
        verify(evidenceRepo).save(any(AlertEvidence.class));
    }

    @Test
    void mergeAlertsInWindow_CombinesR3AndR2IntoHighSeverity() {
        Alert existingAlert = new Alert();
        existingAlert.setAlertId(10L);
        existingAlert.setUserId(3L);
        existingAlert.setRuleCodes("R3");
        existingAlert.setScore(40);
        existingAlert.setSeverity(Alert.Severity.MEDIUM);
        existingAlert.setExplanation("Clinical access denied");
        existingAlert.setEventCount(1);
        existingAlert.setLastSeen(LocalDateTime.now().minusMinutes(2));

        when(alertRepo.findOpenForUserSince(eq(3L), any())).thenReturn(List.of(existingAlert));
        when(alertRepo.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert merged = alertService.createOrMergeAlert(3L, "R2", 35, "LOW", "Bulk retrieval of 38 records", List.of(201L, 202L));

        assertEquals(10L, merged.getAlertId());
        assertEquals("R2,R3", merged.getRuleCodes());
        assertEquals(75, merged.getScore(), "40 (R3) + 35 (R2) = 75");
        assertEquals(Alert.Severity.HIGH, merged.getSeverity(), "Score >= 70 maps to HIGH");
        assertEquals(2, merged.getEventCount());
        assertTrue(merged.getExplanation().contains("Clinical access denied"));
        assertTrue(merged.getExplanation().contains("Bulk retrieval"));
    }

    @Test
    void updateStatus_AllowsReviewerAction() {
        Alert alert = new Alert();
        alert.setAlertId(5L);
        alert.setStatus(Alert.AlertStatus.OPEN);

        when(alertRepo.findById(5L)).thenReturn(Optional.of(alert));
        when(alertRepo.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert escalated = alertService.updateStatus(5L, Alert.AlertStatus.ESCALATED, "Escalated to CISO");

        assertEquals(Alert.AlertStatus.ESCALATED, escalated.getStatus());
        assertEquals("Escalated to CISO", escalated.getReviewerNote());
    }
}
