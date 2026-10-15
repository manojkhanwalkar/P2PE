# `src/main/java/util/VerifiedMessage.java`

**Purpose:** Result of a successful decrypt-and-verify: the payload plus the replay-protection fields from the signed JWS header.

**Used by:** [`JWUtil.java`](JWUtil.java.md) (`verifyMessage`, `decryptAndVerify`), [`NonceCache.java`](NonceCache.java.md), [`ClientHandler.java`](ClientHandler.java.md), [`ClientManager.java`](ClientManager.java.md), [`ServiceConnector.java`](ServiceConnector.java.md).

## Types
### `record VerifiedMessage(String payload, String nonce, Instant issuedAt)`
- `payload` — the verified plaintext.
- `nonce` — the JWS header's `nonce`, or `null` if absent.
- `issuedAt` — the JWS header's `iat` (epoch seconds), or `null` if absent.
