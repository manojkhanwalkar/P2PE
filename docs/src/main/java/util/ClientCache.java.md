# `src/main/java/util/ClientCache.java`

**Purpose:** Thread-safe, bounded, expiring in-memory store of client sessions on the server, keyed by JWK key ID.

**Depends on:** [`ClientHandler.java`](ClientHandler.java.md); Nimbus `JWK`; Guava `Cache`.
**Used by:** [`SampleResource.java`](../service/SampleResource.java.md) (owns it), [`KeyExchangeUtil.java`](KeyExchangeUtil.java.md) (writes), [`ClientManager.java`](ClientManager.java.md) (reads).

## Types
### `ClientCache`
- `idHandlerMap` — Guava `Cache<String, ClientHandler>`, key ID → session.
- `DEFAULT_MAX_SESSIONS` (10,000), `DEFAULT_IDLE_TIMEOUT` (30 min), `DEFAULT_MAX_LIFETIME` (24 h).

## Functions
### `ClientCache()` / `ClientCache(long maxSessions, Duration idleTimeout, Duration maxLifetime, Ticker ticker)`
The public constructor uses the defaults. The package-private one lets tests set the limits and a fake clock.

### `Optional<ClientHandler> get(String keyId)`
Looks up a session; empty if the key ID is `null`, unknown or expired. A lookup counts as activity for the idle timeout.

### `void put(JWK key, ClientHandler clientHandler)`
Stores the session under `key.getKeyID()`, replacing any existing entry with that ID.

### `String toString()`
Debug representation with the current session count.

## Notes
- The limits exist because `/keyexchange` is unauthenticated: an unbounded map let any caller exhaust the heap. The maximum lifetime also forces clients to re-run key exchange, which rotates the server's session keys.
- When a session is evicted, the client's next `/verify` gets a 400 "Unknown key id" and must run key exchange again.
