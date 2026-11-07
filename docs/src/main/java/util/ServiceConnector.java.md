# `src/main/java/util/ServiceConnector.java`

**Purpose:** The client side of a P2PE session: it performs the key exchange with a service and then sends encrypted+signed requests and unwraps the replies.

**Depends on:** [`Connection.java`](Connection.java.md), [`JWUtil.java`](JWUtil.java.md), [`KeyExchange.java`](../data/KeyExchange.java.md), [`EncryptedSignedRequest.java`](../data/EncryptedSignedRequest.java.md), [`EncryptedSignedResponse.java`](../data/EncryptedSignedResponse.java.md); Nimbus `JWK`.
**Used by:** [`Client.java`](../client/Client.java.md).

## Types
### `ServiceConnector`
- `kaPrivKSender` / `kaPubKSender` — the client's ECDH key pair (responses are encrypted to it).
- `signPrivKSender` / `signPubKSender` — the client's signing key pair.
- `kareceiverKey` / `signreceiverKey` — the service's public ECDH and signing keys, set by `init()`.
- `connection` — [`Connection.java`](Connection.java.md) to the service base URL.

## Functions
### `ServiceConnector(String url, JWK kaKey, JWK signKey)`
Stores the base URL and the client's two private JWKs, and derives their public halves.

### `void init() throws Exception`
- **What it does:** `POST keyexchange` with the client's public keys, then validates and stores the service's public keys from the reply (only public EC P-256 JWKs are accepted, via `ClientHandler.parsePeerKey`).
- **Errors:** `RuntimeException` if the call fails; `ParseException` if the returned JWKs are invalid, not EC P-256, or contain private parts.

### `String send(String payload, String action)`
- **What it does:** logs the key IDs in use at DEBUG, generates a fresh nonce (`JWUtil.newNonce`), wraps `payload` as JWE&lt;JWS&gt; (signed with the client key, encrypted to the service key, nonce in the signed header), POSTs it to `action`, and unwraps the reply.
- **Returns:** the verified plaintext response.
- **Errors:** `NullPointerException` if `init()` was not called; `RuntimeException` on HTTP failure; `SecurityException` if the reply fails decryption or verification, or doesn't echo the nonce.

### `String unwrapResponse(String message, String expectedNonce)`
Decrypts a response with the client's ECDH private key and verifies it with the service's signing key. Throws `SecurityException` on failure, or if the response's nonce isn't `expectedNonce`, which rejects a replayed old response.
