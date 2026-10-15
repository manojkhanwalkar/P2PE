package util;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Replay guard for one session: accepts each nonce once, and only while its timestamp is inside the freshness window.
 * Nonces older than the window are dropped, because the timestamp check alone rejects them.
 */
public class NonceCache {

    public static final Duration DEFAULT_WINDOW = Duration.ofMinutes(5);

    private final ConcurrentMap<String, Instant> seen = new ConcurrentHashMap<>();
    private final Duration window;
    private final Clock clock;

    public NonceCache()
    {
        this(DEFAULT_WINDOW, Clock.systemUTC());
    }

    public NonceCache(Duration window, Clock clock)
    {
        this.window = window;
        this.clock = clock;
    }

    /** Records the message's nonce; throws {@link SecurityException} if it is missing, stale or already seen. */
    public void checkAndRecord(VerifiedMessage message)
    {
        if (message.nonce() == null || message.issuedAt() == null)
            throw new SecurityException("Message has no nonce or timestamp");

        Instant now = clock.instant();
        Instant oldest = now.minus(window);
        if (message.issuedAt().isBefore(oldest) || message.issuedAt().isAfter(now.plus(window)))
            throw new SecurityException("Message timestamp is outside the allowed window");

        seen.values().removeIf(issuedAt -> issuedAt.isBefore(oldest));

        if (seen.putIfAbsent(message.nonce(), message.issuedAt()) != null)
            throw new SecurityException("Replayed nonce");
    }
}
