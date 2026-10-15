# `src/test/java/util/JWUtilTest.java`

**Purpose:** Unit tests for the project's crypto envelope in [`JWUtil.java`](../../../main/java/util/JWUtil.java.md).

**Depends on:** [`JWUtil.java`](../../../main/java/util/JWUtil.java.md), JUnit Jupiter, Nimbus.
**Used by:** `mvn test`.

## Overview
Each test gets a fresh sender signing key and receiver ECDH key (JUnit creates a new instance per test).

## Functions
- `signThenEncryptRoundTrips()` — `signAndEncrypt` followed by `decryptAndVerify` returns the original payload, the nonce and a current timestamp.
- `newNonceIsUnique()` — two calls to `newNonce` differ.
- `verifyRejectsWrongSigner()` — `verify` returns empty for a JWS signed by a different key.
- `decryptAndVerifyRejectsWrongSigner()` — a message signed by an unexpected key throws `SecurityException`.
- `decryptAndVerifyRejectsWrongRecipient()` — a message encrypted to a different key throws `SecurityException`.
- `encryptUsesRecipientKeyIdInHeader()` — the JWE header `kid` equals the recipient key's ID (the server's session lookup relies on this).
