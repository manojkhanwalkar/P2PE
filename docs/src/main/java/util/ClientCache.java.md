# `src/main/java/util/ClientCache.java`

**Purpose:** Thread-safe in-memory store of client sessions on the server, keyed by JWK key ID.

**Depends on:** [`ClientHandler.java`](ClientHandler.java.md); Nimbus `JWK`; `ConcurrentHashMap`.
**Used by:** [`SampleResource.java`](../service/SampleResource.java.md) (owns it), [`KeyExchangeUtil.java`](KeyExchangeUtil.java.md) (writes), [`ClientManager.java`](ClientManager.java.md) (reads).

## Types
### `ClientCache`
- `idHandlerMap` — `ConcurrentMap<String, ClientHandler>`, key ID → session.

## Functions
### `Optional<ClientHandler> get(String keyId)`
Looks up a session; empty if the key ID is unknown.

### `void put(JWK key, ClientHandler clientHandler)`
Stores the session under `key.getKeyID()`, replacing any existing entry with that ID.

### `String toString()`
Debug representation including the map (handlers print their default object identity, not key material).

## Notes
- Entries are never removed, so memory grows with every key exchange until the service restarts.
