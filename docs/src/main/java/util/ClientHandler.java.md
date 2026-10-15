# `src/main/java/util/ClientHandler.java`

**Purpose:** The server side of one client session: it holds the server's key pairs for that client and the client's public keys, and wraps and unwraps messages.

**Depends on:** [`JWUtil.java`](JWUtil.java.md), [`NonceCache.java`](NonceCache.java.md), [`VerifiedMessage.java`](VerifiedMessage.java.md), [`KMSConnector.java`](KMSConnector.java.md), [`KeyRequest.java`](../data/KeyRequest.java.md); Nimbus `JWK`.
**Used by:** [`KeyExchangeUtil.java`](KeyExchangeUtil.java.md) (creates and initialises it), [`ClientCache.java`](ClientCache.java.md) (stores it), [`ClientManager.java`](ClientManager.java.md) (uses it per request).

## Types
### `ClientHandler`
Package-private fields (read directly by `KeyExchangeUtil`):
- `kaPrivKReceiver` / `kaPubKReceiver` — the server's ECDH key pair for this session (from the KMS). The public key's `kid` is the session ID.
- `signPrivKReceiver` / `signPubKReceiver` — the server's signing key pair for this session.
- `kaSenderKey` — the client's public ECDH key (responses are encrypted to it).
- `signSenderKey` — the client's public signing key (requests are verified against it).
- `nonceCache` (private) — this session's [`NonceCache`](NonceCache.java.md) replay guard.

All fields are `null` until `init` succeeds.

## Functions
### `String getKAPubKey()` / `String getSignPubKey()`
Return the server's public ECDH / signing JWK as JSON, to send back to the client. NPE if called before `init`.

### `void init(String kaPublicKey, String signPublicKey) throws ParseException`
- **What it does:** makes a **blocking HTTPS call to the KMS** (`KMSConnector.DEFAULT_URL`) to get two new server key pairs, derives their public halves, and parses the client's two public JWKs.
- **Parameters:** the client's public ECDH and signing keys as JWK JSON.
- **Errors:** `ParseException` if any JWK JSON is invalid; `RuntimeException` from `Connection.send` if the KMS call fails.

### `VerifiedMessage unwrap(String message)`
Decrypts an incoming JWE with the server's ECDH private key, verifies the inner JWS with the client's signing key (`JWUtil.decryptAndVerify`), then records its nonce in the session's `NonceCache`. Throws `SecurityException` if decryption or verification fails, or if the nonce is missing, repeated or outside the time window.

### `String wrap(String message, String requestNonce)`
Signs `message` with the server's signing key and encrypts it to the client's ECDH key (`JWUtil.signAndEncrypt`). The request's nonce is echoed in the signed header so the client can match the response to its request.

## Notes
- The KMS URL is hard-coded through `KMSConnector.DEFAULT_URL`.
