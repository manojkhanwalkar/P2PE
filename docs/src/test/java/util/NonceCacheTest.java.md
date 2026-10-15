# `src/test/java/util/NonceCacheTest.java`

**Purpose:** Unit tests for [`NonceCache.java`](../../../main/java/util/NonceCache.java.md) with a fixed clock and a 5-minute window.

## Functions
- `acceptsFreshNonceOnce()` — the first use passes and a repeat throws `SecurityException`.
- `acceptsDistinctNonces()` — different nonces are each accepted.
- `rejectsStaleOrFutureTimestamps()` — timestamps 6 minutes in the past or the future are rejected.
- `rejectsMissingNonceOrTimestamp()` — a `null` nonce or timestamp is rejected.
