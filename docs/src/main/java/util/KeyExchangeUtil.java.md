# `src/main/java/util/KeyExchangeUtil.java`

**Purpose:** Server-side implementation of the `/keyexchange` handshake.

**Depends on:** [`ClientHandler.java`](ClientHandler.java.md), [`ClientCache.java`](ClientCache.java.md), [`KeyExchange.java`](../data/KeyExchange.java.md); Jakarta `BadRequestException`.
**Used by:** [`SampleResource.java`](../service/SampleResource.java.md).

## Functions
### `static KeyExchange keyExchange(KeyExchange request, ClientCache clientCache)`
- **What it does:** creates a [`ClientHandler.java`](ClientHandler.java.md), initialises it with the client's public keys (which also fetches fresh server keys from the KMS), caches it under the **server-issued** ECDH key ID, and returns the server's public ECDH and signing keys.
- **Returns:** a `KeyExchange` with the server's public keys.
- **Errors:** `BadRequestException` (HTTP 400) if the client's keys don't parse; `RuntimeException` (HTTP 500) if the KMS call fails.
- **Notes:** client-supplied key IDs are deliberately not used as cache keys, so a caller can't overwrite another client's session.
