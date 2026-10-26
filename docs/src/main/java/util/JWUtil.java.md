# `src/main/java/util/JWUtil.java`

**Purpose:** All the cryptography in the project: EC key generation, JWE encryption (ECDH-ES + A256GCM), JWS signing (ES256), and the combined sign-then-encrypt envelope.

**Depends on:** Nimbus JOSE+JWT (`JWEObject`, `JWSObject`, `ECDH*`/`ECDSA*` crypters, `ECKey`); JDK `KeyPairGenerator`.
**Used by:** [`KMSResource.java`](../kms/KMSResource.java.md) (`createKey`), [`ClientHandler.java`](ClientHandler.java.md) and [`ServiceConnector.java`](ServiceConnector.java.md) (`signAndEncrypt`/`decryptAndVerify`), [`JWUtilTest.java`](../../../test/java/util/JWUtilTest.java.md).

## Overview
The wire format is **JWE&lt;JWS&lt;payload&gt;&gt;**: the payload is signed with the sender's private signing key (ES256), and that compact JWS is then encrypted to the recipient's public EC key with ECDH-ES. Each message gets a fresh ephemeral key from Nimbus. For replay protection the signed JWS header also carries custom `nonce` and `iat` (epoch seconds) parameters; they are inside the encryption and covered by the signature. The JWE header's `kid` is the recipient key's ID, which is how the server finds the session. Several low-level methods log through SLF4J and return `null` or empty on failure; `decryptAndVerify` turns those failures into exceptions.

## Functions
### `static JWK createKey()`
- **What it does:** generates a 256-bit EC key pair (P-256 with the default JDK provider) and wraps it as a JWK with a random UUID `kid`, **including the private key**.
- **Returns:** the JWK, or **`null`** on failure (logged at ERROR).

### `static String encrypt(JWK encrypterKey, String payload)`
- **What it does:** builds a JWE with `alg=ECDH-ES`, `enc=A256GCM`, `kid=encrypterKey.kid`, and encrypts it to the public EC key.
- **Returns:** the compact JWE, or **`null`** on `JOSEException`.

### `static String decrypt(JWK decrypterKey, String payload)`
- **What it does:** parses a compact JWE and decrypts it with the EC private key.
- **Returns:** the plaintext, or **`null`** on parse or decryption failure (logged at DEBUG without a stack trace, since callers can trigger it).
- **Notes:** a non-EC `decrypterKey` throws an uncaught `ClassCastException`.

### `static String newNonce()`
Returns a fresh 128-bit `SecureRandom` value, base64url-encoded without padding.

### `static String sign(JWK jwk, String payload, String nonce)`
Like `sign(JWK, String)`, but also puts `nonce` and the current time (`iat`) into the JWS header. Returns **`null`** on `JOSEException`.

### `static String sign(JWK jwk, String payload)`
Signs with the JWK's EC private key and sets the JWS `kid` to the JWK's ID. Returns **`null`** on `JOSEException`.

### `static String sign(PrivateKey privateKey, String msg, String keyId) throws JOSEException`
Builds a JWS with `alg=ES256`, `cty=text/plain`, `kid=keyId`, signs it with the EC private key, and returns the compact serialisation.

### `static Optional<VerifiedMessage> verifyMessage(JWK publicKey, String compactSerialization)`
- **What it does:** parses a compact JWS and verifies it with the EC public key; no output on success.
- **Returns:** a [`VerifiedMessage`](VerifiedMessage.java.md) with the payload and the header's `nonce` and `iat` (each `null` if absent or of the wrong type) if the signature is valid, otherwise empty (parse and JOSE errors are logged at DEBUG and swallowed).

### `static Optional<String> verify(JWK publicKey, String compactSerialization)`
Returns only the payload from `verifyMessage`.

### `static String signAndEncrypt(JWK signingKey, JWK encryptionKey, String payload, String nonce)`
Returns `encrypt(encryptionKey, sign(signingKey, payload, nonce))`, the JWE&lt;JWS&lt;payload&gt;&gt; envelope, with `nonce` and `iat` in the signed header.

### `static VerifiedMessage decryptAndVerify(JWK decryptionKey, JWK verificationKey, String message)`
- **What it does:** the inverse of `signAndEncrypt`. It returns the nonce and timestamp but does **not** check them: replay checks are the caller's job ([`NonceCache`](NonceCache.java.md) on the server, nonce matching in [`ServiceConnector`](ServiceConnector.java.md) on the client).
- **Errors:** throws `SecurityException("Unable to decrypt message")` or `SecurityException("Signature verification failed")`.

### `static JWK createECJWK(KeyPair keyPair, String keyId, boolean includePrivate) throws Exception`
Wraps a JDK EC key pair as a Nimbus `ECKey` labelled curve P-256 with the given `kid`, optionally including the private key. It assumes the key pair really is on P-256.

## Notes
- `signAndEncrypt` does not check for a `null` result from `sign`; a signing failure surfaces as an exception inside `encrypt` (Nimbus rejects a `null` payload).
