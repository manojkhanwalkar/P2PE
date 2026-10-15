package util;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jose.jwk.JWK;

import java.time.Duration;
import java.util.Optional;

/**
 * Sessions keyed by the server-issued key-agreement key id. Bounded and expiring, because /keyexchange is
 * unauthenticated: an unbounded map lets any caller exhaust the heap, and sessions that never end never rotate keys.
 */
public class ClientCache {

    public static final long DEFAULT_MAX_SESSIONS = 10_000;
    public static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMinutes(30);
    public static final Duration DEFAULT_MAX_LIFETIME = Duration.ofHours(24);

    private final Cache<String, ClientHandler> idHandlerMap;

    public ClientCache()
    {
        this(DEFAULT_MAX_SESSIONS, DEFAULT_IDLE_TIMEOUT, DEFAULT_MAX_LIFETIME, Ticker.systemTicker());
    }

    ClientCache(long maxSessions, Duration idleTimeout, Duration maxLifetime, Ticker ticker)
    {
        idHandlerMap = CacheBuilder.newBuilder()
                .maximumSize(maxSessions)
                .expireAfterAccess(idleTimeout)
                .expireAfterWrite(maxLifetime)
                .ticker(ticker)
                .build();
    }

    public Optional<ClientHandler> get(String keyId)
    {
        if (keyId == null)
            return Optional.empty();

        return Optional.ofNullable(idHandlerMap.getIfPresent(keyId));
    }

    public void put(JWK key, ClientHandler clientHandler) {

        idHandlerMap.put(key.getKeyID(),clientHandler);
    }

    @Override
    public String toString() {
        return "ClientCache{" +
                "sessions=" + idHandlerMap.size() +
                '}';
    }
}
