package com.medsentinel.logging;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogChainServiceTest {

    @Test
    void reportsBrokenPrevHashAtFirstAffectedRow() {
        AccessLogRepository repo = mock(AccessLogRepository.class);
        AccessLog first = log(1L, "0".repeat(64), null);
        AccessLog second = log(2L, "wrong-previous-hash", null);
        when(repo.findAllOrderedById()).thenReturn(List.of(first, second));
        LogChainService service = new LogChainService(repo);

        LogChainService.ChainVerificationResult result = service.verifyChain();

        assertFalse(result.intact());
        assertEquals(2L, result.brokenAtLogId());
        assertEquals(2, result.rowsChecked());
    }

    private AccessLog log(Long id, String previous, String hash) {
        AccessLog log = new AccessLog().usernameAttempted("user").action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED);
        log.setLogId(id);
        log.setTs(LocalDateTime.of(2026, 10, 18, 12, 0));
        log.setPrevHash(previous);
        log.setRowHash(hash);
        return log;
    }
}
