package com.medsentinel.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SeverityTest {

    @Test
    void mapsScoresAtEveryThreshold() {
        assertNull(Severity.fromScore(29));
        assertEquals(Severity.LOW, Severity.fromScore(30));
        assertEquals(Severity.LOW, Severity.fromScore(49));
        assertEquals(Severity.MEDIUM, Severity.fromScore(50));
        assertEquals(Severity.MEDIUM, Severity.fromScore(69));
        assertEquals(Severity.HIGH, Severity.fromScore(70));
    }
}
