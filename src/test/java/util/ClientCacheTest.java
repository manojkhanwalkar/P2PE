package util;

import com.google.common.base.Ticker;
import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class ClientCacheTest {

    private final AtomicLong nanos = new AtomicLong();
    private final Ticker ticker = new Ticker() {
        @Override
        public long read() {
            return nanos.get();
        }
    };

    private void advance(Duration d) {
        nanos.addAndGet(d.toNanos());
    }

    @Test
    public void idleSessionExpires() {
        ClientCache cache = new ClientCache(100, Duration.ofMinutes(30), Duration.ofHours(24), ticker);
        JWK key = JWUtil.createKey();
        cache.put(key, new ClientHandler());

        advance(Duration.ofMinutes(29));
        assertTrue(cache.get(key.getKeyID()).isPresent());

        advance(Duration.ofMinutes(31));
        assertTrue(cache.get(key.getKeyID()).isEmpty());
    }

    @Test
    public void activeSessionStillEndsAtMaxLifetime() {
        ClientCache cache = new ClientCache(100, Duration.ofMinutes(30), Duration.ofHours(1), ticker);
        JWK key = JWUtil.createKey();
        cache.put(key, new ClientHandler());

        for (int i = 0; i < 3; i++) {
            advance(Duration.ofMinutes(19));
            assertTrue(cache.get(key.getKeyID()).isPresent());
        }

        advance(Duration.ofMinutes(4));
        assertTrue(cache.get(key.getKeyID()).isEmpty());
    }

    @Test
    public void sessionCountIsBounded() {
        ClientCache cache = new ClientCache(10, Duration.ofMinutes(30), Duration.ofHours(24), ticker);
        for (int i = 0; i < 1000; i++)
            cache.put(JWUtil.createKey(), new ClientHandler());

        assertTrue(cache.toString().matches("ClientCache\\{sessions=(10|[0-9])\\}"), cache.toString());
    }

    @Test
    public void nullKeyIdIsAbsent() {
        assertTrue(new ClientCache().get(null).isEmpty());
    }
}
