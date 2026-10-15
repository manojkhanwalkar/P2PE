package util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class NonceCacheTest {

    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final NonceCache cache = new NonceCache(Duration.ofMinutes(5), Clock.fixed(now, ZoneOffset.UTC));

    private static VerifiedMessage message(String nonce, Instant issuedAt) {
        return new VerifiedMessage("payload", nonce, issuedAt);
    }

    @Test
    public void acceptsFreshNonceOnce() {
        cache.checkAndRecord(message("a", now));

        assertThrows(SecurityException.class, () -> cache.checkAndRecord(message("a", now)));
    }

    @Test
    public void acceptsDistinctNonces() {
        cache.checkAndRecord(message("a", now));
        assertDoesNotThrow(() -> cache.checkAndRecord(message("b", now)));
    }

    @Test
    public void rejectsStaleOrFutureTimestamps() {
        assertThrows(SecurityException.class, () -> cache.checkAndRecord(message("old", now.minus(Duration.ofMinutes(6)))));
        assertThrows(SecurityException.class, () -> cache.checkAndRecord(message("future", now.plus(Duration.ofMinutes(6)))));
    }

    @Test
    public void rejectsMissingNonceOrTimestamp() {
        assertThrows(SecurityException.class, () -> cache.checkAndRecord(message(null, now)));
        assertThrows(SecurityException.class, () -> cache.checkAndRecord(message("a", null)));
    }
}
