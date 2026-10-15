# `src/main/java/util/NonceCache.java`

**Purpose:** Replay guard for one server-side session. It accepts each request nonce once, and only while the request's timestamp is fresh.

**Depends on:** [`VerifiedMessage.java`](VerifiedMessage.java.md); `java.time`.
**Used by:** [`ClientHandler.java`](ClientHandler.java.md) (one instance per session), [`NonceCacheTest.java`](../../../test/java/util/NonceCacheTest.java.md).

## Types
### `NonceCache`
- `DEFAULT_WINDOW` — 5 minutes.
- `seen` — a concurrent map from nonce to its signed timestamp.

## Functions
### `NonceCache()` / `NonceCache(Duration window, Clock clock)`
The no-arg constructor uses `DEFAULT_WINDOW` and the UTC system clock. The second constructor lets tests fix the clock.

### `void checkAndRecord(VerifiedMessage message)`
- **What it does:** checks that the message has a nonce and timestamp, and that the timestamp is no more than `window` in the past or the future (the future side allows for clock skew). It drops remembered nonces older than the window, because the timestamp check alone now rejects them. Then it records the nonce atomically (`putIfAbsent`).
- **Errors:** `SecurityException` if the nonce or timestamp is missing, the timestamp is outside the window, or the nonce was already seen.

## Notes
- Memory is bounded by the number of requests per window. Each check scans the map for expired entries, which is fine at demo scale.
- State is in memory only and lost on restart. After a restart the old sessions are gone too, so old messages fail the `kid` lookup.
