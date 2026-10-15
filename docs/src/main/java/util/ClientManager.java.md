# `src/main/java/util/ClientManager.java`

**Purpose:** Server-side pipeline for one encrypted request: find the session, decrypt and verify, run the business logic, then sign and encrypt the reply.

**Depends on:** [`ClientCache.java`](ClientCache.java.md), [`ClientHandler.java`](ClientHandler.java.md), [`Processor.java`](../common/Processor.java.md), the `data` message classes; Nimbus `JWEObject`; Jakarta `BadRequestException`.
**Used by:** [`SampleResource.java`](../service/SampleResource.java.md).

## Functions
### `static EncryptedSignedResponse process(EncryptedSignedRequest encryptedSignedRequest, ClientCache clientCache, Processor<String,String> processor)`
- **What it does:**
  1. Parses the JWE header (without decrypting) to read its `kid`, which identifies the server key the client encrypted to and therefore the session.
  2. Looks the session up in `clientCache`.
  3. `clientHandler.unwrap` decrypts, verifies the signature and rejects replayed or stale nonces.
  4. Passes the plaintext to `processor.process`.
  5. `clientHandler.wrap` signs and encrypts the result, echoing the request nonce, and returns it in an `EncryptedSignedResponse`.
- **Errors:** `BadRequestException` (HTTP 400) when the JWE is malformed, the `kid` is unknown, or decryption, verification or the nonce check fails. Exceptions from `processor` are not caught (HTTP 500).
- **Logging:** unknown-session and decrypt, verify or replay rejections are logged at WARN (Dropwizard doesn't log 4xx itself). An unknown `kid` is not logged, because it is attacker-controlled and could forge log lines.
- **Notes:** a `null` payload field would cause a `NullPointerException` while parsing (HTTP 500), not a 400.
