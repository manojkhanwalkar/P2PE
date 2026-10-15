# `src/main/java/util/JWUtil.java`

**Purpose:** All the cryptography in the project: EC key generation, JWE encryption (ECDH-ES + A256GCM), JWS signing (ES256), and the combined sign-then-encrypt envelope.

**Depends on:** Nimbus JOSE+JWT (`JWEObject`, `JWSObject`, `ECDH*`/`ECDSA*` crypters, `ECKey`); JDK `KeyPairGenerator`.
**Used by:** [`KMSResource.java`](../kms/KMSResource.java.md) (`createKey`), [`ClientHandler.java`](ClientHandler.java.md) and [`ServiceConnector.java`](ServiceConnector.java.md) (`signAndEncrypt`/`decryptAndVerify`), [`JWUtilTest.java`](../../../test/java/util/JWUtilTest.java.md).

## Overview
The wire format is **JWE&lt;JWS&lt;payload&gt;&gt;**: the payload is signed with the sender's private signing key (ES256), and that compact JWS is then encrypted to the recipient's public EC key with ECDH-ES. Each message gets a fresh ephemeral key from Nimbus. The JWE header's `kid` is the recipient key's ID, which is how the server finds the session. Several low-level methods print to stdout and return `null` or empty on failure; `decryptAndVerify` turns those failures into exceptions.

## Functions
### `static JWK createKey()`
- **What it does:** generates a 256-bit EC key pair (P-256 with the default JDK provider) and wraps it as a JWK with a random UUID `kid`, **including the private key**.
- **Returns:** the JWK, or **`null`** on failure (the stack trace is printed).

### `static String encrypt(JWK encrypterKey, String payload)`
- **What it does:** builds a JWE with `alg=ECDH-ES`, `enc=A256GCM`, `kid=encrypterKey.kid`, encrypts it to the public EC key, and prints the header to stdout.
- **Returns:** the compact JWE, or **`null`** on `JOSEException`.

### `static String decrypt(JWK decrypterKey, String payload)`
- **What it does:** parses a compact JWE and decrypts it with the EC private key.
- **Returns:** the plaintext, or **`null`** on parse or decryption failure (the stack trace is printed).
- **Notes:** a non-EC `decrypterKey` throws an uncaught `ClassCastException`.

### `static String sign(JWK jwk, String payload)`
Signs with the JWK's EC private key and sets the JWS `kid` to the JWK's ID. Returns **`null`** on `JOSEException`.

### `static String sign(PrivateKey privateKey, String msg, String keyId) throws JOSEException`
Builds a JWS with `alg=ES256`, `cty=text/plain`, `kid=keyId`, signs it with the EC private key, and returns the compact serialisation.

### `static Optional<String> verify(JWK publicKey, String compactSerialization)`
- **What it does:** parses a compact JWS and verifies it with the EC public key; on success it prints "Receiver successfully verified signature".
- **Returns:** the payload if the signature is valid, otherwise empty (parse and JOSE errors are printed and swallowed).

### `static String signAndEncrypt(JWK signingKey, JWK encryptionKey, String payload)`
Returns `encrypt(encryptionKey, sign(signingKey, payload))`, the JWE&lt;JWS&lt;payload&gt;&gt; envelope.

### `static String decryptAndVerify(JWK decryptionKey, JWK verificationKey, String message)`
- **What it does:** the inverse of `signAndEncrypt`.
- **Errors:** throws `SecurityException("Unable to decrypt message")` or `SecurityException("Signature verification failed")`.

### `static JWK createECJWK(KeyPair keyPair, String keyId, boolean includePrivate) throws Exception`
Wraps a JDK EC key pair as a Nimbus `ECKey` labelled curve P-256 with the given `kid`, optionally including the private key. It assumes the key pair really is on P-256.

## Notes
- `signAndEncrypt` does not check for a `null` result from `sign`; a signing failure surfaces as an exception inside `encrypt` (Nimbus rejects a `null` payload).
