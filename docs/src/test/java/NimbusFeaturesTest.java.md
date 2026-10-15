# `src/test/java/NimbusFeaturesTest.java`

**Purpose:** Exploratory tests and examples of Nimbus JOSE features: RSA/EC encryption, nested signed-then-encrypted JWTs, key generation and PEM parsing. They don't test this project's own code.

**Depends on:** Nimbus JOSE+JWT, JUnit Jupiter, JDK crypto; Tink (pulled in by Nimbus for Ed25519).
**Used by:** `mvn test`.

## Functions
### `test8()`
Encrypts a JWE with RSA-OAEP-256 + A128CBC-HS256 using a preset AES content-encryption key, then decrypts it twice: with the RSA private key and directly with the CEK (`DirectDecrypter` in "promiscuous" mode). Prints the payload.

### `test7()`
Builds a signed JWT with an EC P-256 key, nests it in a JWE (ECDH-ES+A256KW, A256GCM, `cty=JWT`), decrypts it and asserts that the signature verifies. This is the same JWE&lt;JWS&gt; idea the project uses.

### `test6()`
Same as `test7` with RSA keys (RS256 signature, RSA-OAEP-256 encryption).

### `test5()`
Generates an Ed25519 `OctetKeyPair` and prints its private and public forms.

### `test4()`
Generates an EC P-256 JWK with `ECKeyGenerator` and via the JDK `KeyPairGenerator` + `ECKey.Builder`, and prints both.

### `test3()`
Builds an RSA JWK from a JDK key pair and prints its JSON.

### `static test2()`
Generates an RSA JWK and prints it. **Not annotated with `@Test`, so it never runs.**

### `test1()`
Parses an X.509 certificate and PEM objects into JWKs. It reads `/home/manoj/data/nimbus/spcert.pem` and `trial.pem`, which are **not in the repository**; the test is skipped (via `assumeTrue`) when they're missing.

### `private byte[] extractPublicKeyFromFile(String fileName) throws IOException`
Reads a whole file into bytes (`Files.readAllBytes`). It's used for PEM files despite the name.

## Notes
- Most tests only print output and assert nothing beyond "no exception".
