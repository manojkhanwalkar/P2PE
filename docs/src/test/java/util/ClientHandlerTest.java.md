# `src/test/java/util/ClientHandlerTest.java`

**Purpose:** Session-level replay tests for [`ClientHandler.java`](../../../main/java/util/ClientHandler.java.md). The test sets the handler's package-private key fields directly, so it doesn't need the KMS that `init` calls.

## Functions
- `rejectsReplayedRequest()` — the same encrypted request is accepted once, then rejected with `SecurityException`.
- `responseEchoesRequestNonce()` — `wrap` puts the given request nonce into the signed response.
