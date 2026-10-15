# `src/main/java/data/EncryptedSignedMessage.java`

**Purpose:** Base JSON body for messages carrying a JWE&lt;JWS&lt;payload&gt;&gt; compact string.

**Depends on:** nothing.
**Used by:** [`EncryptedSignedRequest.java`](EncryptedSignedRequest.java.md), [`EncryptedSignedResponse.java`](EncryptedSignedResponse.java.md) (subclasses).

## Types
### `EncryptedSignedMessage`
- `encryptedSignedPayload` — the compact-serialised JWE whose plaintext is a compact JWS. JSON field name `encryptedSignedPayload`.

## Functions
- `getEncryptedSignedPayload()` / `setEncryptedSignedPayload(String)` — plain accessors used by Jackson.
