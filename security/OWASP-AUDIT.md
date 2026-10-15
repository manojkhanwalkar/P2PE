# OWASP Top 10 (2025) Audit — P2PE

_Date: 2026-10-15 · Scope: whole repository · Commit: ad8bf43_

## System summary
P2PE is a demonstration framework for application-level encryption between services: messages travel as JWE<JWS<payload>> (ECDH-ES + A256GCM, ES256) on top of HTTPS. It has three parts: a KMS (Dropwizard, `:8280`, admin `:8281`) that hands out freshly generated EC key pairs, a sample service (Dropwizard, `:8180`, admin `:8181`) exposing `POST /keyexchange` and `POST /verify`, and a demo `Client`. The untrusted inputs are the bodies of all three POST endpoints and the TLS channel. None of the endpoints authenticate the caller (peer authentication is a documented exclusion in the README). The assets are the per-session private keys (signing and key agreement), the TLS private key, and the plaintext payloads, which in a P2PE deployment would be card or other sensitive data.

## Summary
| Category | Status | Findings |
|---|---|---|
| A01 Broken Access Control | ⚠ Issues | C:0 H:0 M:1 L:0 |
| A02 Security Misconfiguration | ⚠ Issues | C:0 H:1 M:1 L:0 |
| A03 Software Supply Chain Failures | ⚠ Issues | C:0 H:0 M:0 L:1 |
| A04 Cryptographic Failures | ⚠ Issues | C:0 H:0 M:1 L:0 |
| A05 Injection | N/A | C:0 H:0 M:0 L:0 |
| A06 Insecure Design | ⚠ Issues | C:0 H:1 M:0 L:0 |
| A07 Authentication Failures | ⚠ Issues | C:0 H:0 M:1 L:0 |
| A08 Software or Data Integrity Failures | ⚠ Issues | C:0 H:0 M:0 L:1 |
| A09 Security Logging and Alerting Failures | ⚠ Issues | C:0 H:0 M:1 L:1 |
| A10 Mishandling of Exceptional Conditions | ⚠ Issues | C:0 H:0 M:0 L:1 |

**Totals:** Critical 0 · High 2 · Medium 5 · Low 4

The message-level cryptography is sound: it uses modern algorithms, a CSPRNG, separate signing and key-agreement keys, kid lookup against server-issued key IDs only, fail-closed decrypt/verify, and per-session replay protection. The weaknesses are in what surrounds it. The TLS private key is committed to the repository and shipped in the jar, the KMS hands out private keys to anyone, unauthenticated key exchanges create sessions that are never evicted, and nobody authenticates the peer. Taken together, an active network attacker who has the repository can sit in the middle of TLS and of the key exchange, and the application-layer encryption then gives no protection.

**Fix first:**
1. Stop shipping and trusting the committed TLS keystore (A02).
2. Bound and expire key-exchange sessions, and rate-limit `/keyexchange` (A06).
3. Stop logging decrypted payloads (A09).

## Remediation (`--fix`, 2026-10-15)

| Finding | Severity | Status |
|---|---|---|
| A02 Committed TLS keystore and password | High | **Fixed** |
| A06 Unbounded sessions from unauthenticated `/keyexchange` | High | **Fixed** (rate limiting deferred to infrastructure) |
| A02 Admin connectors on all interfaces | Medium | **Fixed** |
| A09 Decrypted payload printed to stdout | Medium | **Fixed** |
| A01 KMS `/issuekey` unauthenticated | Medium | Deferred (protocol/identity redesign) |
| A04 Private keys issued over the network | Medium | Deferred (protocol redesign) |
| A07 No peer authentication | Medium | Deferred (documented exclusion; protocol redesign) |
| A03 Unused dependencies | Low | **Fixed** |
| A09 Security failures not logged | Low | **Fixed** (partially: no metric yet) |
| A10 Peer key types not validated | Low | **Fixed** |
| A08 No recipient binding in signed payload | Low | Not fixed (wire-format change) |

**Verification:**
- **Baseline:** 19 tests, 0 failures, 1 skipped.
- **After the fixes:** `mvn clean package` gives 25 tests, 0 failures, 1 skipped (`NimbusFeaturesTest.test1`, which needs external PEM files, as before). The 6 new tests are 4 in `ClientCacheTest` and 2 in `ClientHandlerTest`.
- **End to end:** the KMS, the service and the client ran with a generated dev keystore, and the client completed the full flow.
- **Probes against the running service:** the admin ports are bound to 127.0.0.1; an RSA key at key exchange gets a 400; an unknown `kid` is logged without echoing it; startup without `P2PE_KEYSTORE_PASSWORD` fails with `UndefinedEnvironmentVariableException`.

**Breaking changes for operators** (no API or wire-format changes):
- The servers now require the `P2PE_KEYSTORE_PATH` and `P2PE_KEYSTORE_PASSWORD` environment variables.
- `mytestkey.jks` is gone; run `dev/generate-dev-keystore.sh` instead.
- The admin endpoints are reachable only from localhost.
- Sessions expire after 30 minutes idle or 24 hours in total, so long-running clients must re-run key exchange when they get a 400 "Unknown key id". The bundled `ServiceConnector` doesn't do this automatically.

## Findings

### A01 — Broken Access Control

#### [MEDIUM] KMS `/issuekey` is open to any caller (Confirmed)
**Status: Deferred.** Needs caller identities (mTLS or service tokens), which is a deployment and protocol decision. See the A04 and A07 recommendations.

- **Location:** `src/main/java/kms/KMSResource.java:32-43`
- **Evidence:**
  ```java
  @POST @Path("/issuekey")
  public KeyResponse issueKey(KeyRequest request) {
      response.setKaKey(JWUtil.createKey().toJSONString());
      response.setSigningKey(JWUtil.createKey().toJSONString());
  ```
- **Scenario:** Anyone who can reach `:8280` can mint unlimited key pairs. The keys aren't bound to an identity, so a key from the KMS proves nothing about who holds it. The KMS adds no trust beyond what the caller could achieve by calling `KeyPairGenerator` locally, and it is a free CPU target.
- **Fix:** Require client authentication on the KMS (mTLS with per-service client certificates, or a signed service token) and record which identity a key was issued to. Better still, see the A04 finding: have the KMS certify public keys instead of issuing private ones.

Also checked: `/verify` looks up sessions only by the server-issued JWE `kid` (`KeyExchangeUtil.java:25`), so a caller cannot reach another session's handler without that session's client signing key, because signature verification fails closed. No file paths, outbound URLs from input (SSRF) or CORS configuration are present.

### A02 — Security Misconfiguration

#### [HIGH] TLS private key and password committed, shipped in the jar, and used as the client trust anchor (Plausible)
**Status: Fixed.** `mytestkey.jks` was deleted from the repository. `kms.yml` and `sample.yml` now read `${P2PE_KEYSTORE_PATH}` and `${P2PE_KEYSTORE_PASSWORD}` through strict Dropwizard environment substitution (`AppConfiguration.enableEnvironmentSubstitution`), so startup fails if they're unset. `dev/generate-dev-keystore.sh` creates a per-developer EC keystore and a certificate-only truststore, both gitignored, so clients no longer trust a file that contains the private key. A clean `mvn package` produces a jar with no keystore in it. The old key remains in git history: treat it as compromised and never use it.

- **Location:** `src/main/resources/mytestkey.jks`, `src/main/resources/kms.yml:10-11,20-21`, `src/main/resources/sample.yml:10-11,20-21`, `README.md` (run instructions)
- **Evidence:**
  ```yaml
  keyStorePath: src/main/resources/mytestkey.jks
  keyStorePassword: password
  ```
  `unzip -l target/P2PE-1.0-SNAPSHOT.jar` lists `mytestkey.jks`, `kms.yml` and `sample.yml`.
- **Scenario:** Anyone with the repository or the jar has the TLS private key. Clients started as the README shows (`-Djavax.net.ssl.trustStore=mytestkey.jks`) trust that key, so an attacker on the network path can impersonate the KMS and the service. Impersonating the KMS gives them the client's **private** keys (see A04). Impersonating the service during `/keyexchange` lets them substitute their own public keys (see A07). Either way, all application-layer encryption is defeated.
- **To confirm:** check whether any non-local deployment uses this keystore or truststore. The README warns against it, but the shaded jar bundles it by default.
- **Fix:** Move the keystore out of `src/main/resources` (or exclude `*.jks` from the shade plugin) and stop committing it. Load the keystore path and password from the environment (Dropwizard supports `${ENV_VAR}` substitution with `SubstitutingSourceProvider`). Generate dev certificates locally with a script, and use a real CA or an internal PKI elsewhere.

#### [MEDIUM] Dropwizard admin connectors exposed on all interfaces without authentication (Confirmed)
**Status: Fixed.** Both admin connectors now set `bindHost: 127.0.0.1`. Verified with `ss -ltn`: 8181 and 8281 listen on 127.0.0.1 only.

- **Location:** `src/main/resources/kms.yml:17-21`, `src/main/resources/sample.yml:17-21`
- **Evidence:**
  ```yaml
  adminConnectors:
    - type: https
      port: 8281        # 8181 in sample.yml
  ```
- **Scenario:** No `bindHost` is set, so the admin servlet listens on every interface. Without authentication, anyone who can reach it can read `/metrics`, `/threads` (thread dumps) and `/healthcheck`, and can POST to `/tasks/log-level` and `/tasks/gc`. Raising log levels to DEBUG can push more sensitive data into the logs.
- **Fix:** Set `bindHost: 127.0.0.1` on the admin connectors (or firewall them), and add authentication if they must be reachable remotely.

### A03 — Software Supply Chain Failures

#### [LOW] Unused dependencies widen the attack surface (Confirmed)
**Status: Fixed.** Removed commons-io, commons-codec and `jackson-dataformat-cbor`. Moved Tink and BouncyCastle to test scope, because `NimbusFeaturesTest` needs them for Ed25519 and PEM parsing. Guava stays, now used by `ClientCache`. Verified that the shaded jar no longer contains them. A dependency-vulnerability scanner in the build is still recommended.

- **Location:** `pom.xml:65-110`
- **Evidence:** Nothing in `src/` imports Tink, Guava, commons-io, commons-codec, `bcprov`/`bcpkix` or `jackson-dataformat-cbor`. All of them are declared and shaded into the runnable jar.
- **Scenario:** Every bundled library is code that future CVEs can affect and that has to be patched, even though the application never uses it.
- **Fix:** Remove the unused dependencies. Add a dependency-vulnerability check to the build, such as `org.owasp:dependency-check-maven` or the GitHub/OSV scanner, so CVEs in Dropwizard, Jackson and Nimbus are flagged automatically.

**Not run:** no dependency scanner was run, because `mvn` isn't installed in this environment. The declared versions (Dropwizard 5.0.2, Jackson 2.22.3, Nimbus 10.10, BouncyCastle 1.86) were upgraded recently (commit 68c01d5), and I know of no outstanding critical CVEs for them, but please confirm with a scanner. There is no CI configuration in the repository.

### A04 — Cryptographic Failures

#### [MEDIUM] Private keys are generated centrally and sent over the network (Confirmed)
**Status: Deferred.** Changes the KMS protocol and the wire format (the KMS would certify public keys instead of issuing private ones). Left as a recommendation for a deliberate redesign.

- **Location:** `src/main/java/kms/KMSResource.java:38-39`, `src/main/java/util/ClientHandler.java:48-53`, `src/main/java/client/Client.java:19-21`
- **Evidence:**
  ```java
  response.setKaKey(JWUtil.createKey().toJSONString());   // JWK includes "d" (private part)
  ```
- **Scenario:** The KMS and its TLS channel see every private key in the system, both client and server. Anyone who compromises the KMS, its logs or memory, or the TLS channel (trivial with the committed keystore, see A02) can decrypt and forge all P2PE traffic. This defeats the point of end-to-end encryption.
- **Fix:** Generate key pairs locally in each party (`JWUtil.createKey()` already does this). Send only public keys to the KMS and have the KMS sign or certify them, for example as a signed JWK, a JWT binding `kid` to an identity, or an X.509 certificate. Peers then verify that certification during `/keyexchange` (this also fixes A07).

Also checked: the algorithms (ES256, ECDH-ES, A256GCM, P-256) are strong. Nonces come from `SecureRandom` with 128 bits. The JWE content encryption uses fresh ephemeral keys and IVs (Nimbus). Signing and key-agreement keys are separate. No hard-coded keys exist in the source.

### A05 — Injection

N/A. There is no SQL/NoSQL, shell execution, HTML rendering, template engine or `eval`. The only parsing is Jackson binding to plain DTOs without default typing, plus Nimbus JOSE parsing. Outbound URLs are built from code constants (`KMSConnector.DEFAULT_URL`, the action names). Log injection through printed payloads is covered under A09.

### A06 — Insecure Design

#### [HIGH] Unauthenticated `/keyexchange` creates sessions that are never evicted, plus a KMS call each time (Confirmed)
**Status: Fixed (rate limiting deferred).** `ClientCache` is now a Guava cache with at most 10,000 sessions, a 30-minute idle timeout and a 24-hour maximum lifetime (which also forces key rotation). A `null` `kid` now returns 400 instead of throwing an NPE. Client keys are validated before the KMS call (see A10), so malformed requests no longer reach the KMS. Regression tests: `ClientCacheTest` (idle expiry, maximum lifetime, size bound, null kid). Rate limiting `/keyexchange` per source is still recommended, at the gateway or load balancer, and is not implemented here.

- **Location:** `src/main/java/service/SampleResource.java:40-49`, `src/main/java/util/KeyExchangeUtil.java:12-25`, `src/main/java/util/ClientHandler.java:48`, `src/main/java/util/ClientCache.java:11,19-22`
- **Evidence:**
  ```java
  var keys = new KMSConnector(KMSConnector.DEFAULT_URL).getKAandSignPubKeys(new KeyRequest()); // ClientHandler.init
  clientCache.put(clientHandler.kaPubKReceiver, clientHandler);                                 // never removed
  ```
- **Scenario:** An attacker loops `POST /keyexchange` with any two valid EC public JWKs. Each call costs the service an outbound HTTPS round trip to the KMS, makes the KMS generate two EC key pairs, and leaves a `ClientHandler` (six JWKs plus a `NonceCache`) in a `ConcurrentHashMap` that is never cleared. The heap grows until the service runs out of memory, and the KMS is driven along with it. No rate limiting exists anywhere. Because sessions never expire, keys are also never rotated.
- **Fix:** Use a bounded, expiring cache, for example Guava (already a dependency) or Caffeine with `maximumSize` and `expireAfterAccess`, and give sessions a maximum lifetime so keys rotate. Rate-limit `/keyexchange` per source. Validate the client keys before calling the KMS (see A10). Long term, authenticate callers (A07) so that only known peers can create sessions.

### A07 — Authentication Failures

#### [MEDIUM] No peer authentication; key exchange accepts any public keys (Confirmed, documented exclusion)
**Status: Deferred.** A documented exclusion in the README. Fixing it changes the key-exchange protocol.

- **Location:** `src/main/java/util/KeyExchangeUtil.java:10-27`, `src/main/java/util/ServiceConnector.java:32-44`, `README.md` "Exclusions" 1 and 4
- **Evidence:**
  ```java
  signSenderKey = JWK.parse(signPublicKey);   // whatever the caller sent is trusted for the session
  kaSenderKey   = JWK.parse(kaPublicKey);
  ```
- **Scenario:** Neither side can tell whose keys it received. Anyone can open a session with the service and have arbitrary payloads processed. An active attacker who can intercept TLS (made realistic by A02) can run separate key exchanges with each side and relay traffic, reading and modifying every message. JWE/JWS then only protects against passive observers and TLS-terminating intermediaries.
- **Fix:** The README excludes this deliberately, but it should be a prerequisite before handling real card data. Bind keys to identities, for example with KMS-signed public keys (see A04) or mTLS client certificates checked at `/keyexchange`, and have the client verify the service's keys the same way.

### A08 — Software or Data Integrity Failures

#### [LOW] Signed payload doesn't name its intended recipient (sign-then-encrypt forwarding) (Plausible)
**Status: Not fixed (recommendation).** Adding a required `aud` header changes the wire format for every client.

- **Location:** `src/main/java/util/JWUtil.java:134-141`, `src/main/java/util/JWUtil.java:211-214`
- **Evidence:**
  ```java
  new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.getKeyID())
          .customParam(NONCE_HEADER, nonce).customParam(ISSUED_AT_HEADER, ...)   // no audience / recipient kid
  ```
- **Scenario:** If a client reuses its signing key with two services, which `ServiceConnector` allows, a malicious service B can decrypt a request, re-encrypt the inner JWS to service A, and replay it within 5 minutes. A has never seen that nonce, so it accepts it as a genuine request from the client.
- **To confirm:** check whether any deployment shares client keys across services. The demo `Client` talks to only one service.
- **Fix:** Add the recipient's key-agreement `kid` (or a service identifier) as a signed header such as `aud`, and have the receiver reject a message whose `aud` isn't its own `kaPubKReceiver.getKeyID()`.

Also checked: there is no Java native deserialization, no Jackson default typing and no YAML/pickle loading of untrusted data. The signature is verified before the payload is used, and the client checks that the response echoes its nonce. The shade plugin strips dependency signatures (`pom.xml:155-165`), which is expected for an uber-jar.

### A09 — Security Logging and Alerting Failures

#### [MEDIUM] Decrypted payloads are printed to stdout (Confirmed)
**Status: Fixed.** `SampleRequestProcessor` no longer prints the payload. Verified that an end-to-end run leaves no plaintext in the service log. `Client.java:37` still prints the response, because that's the demo client showing its own result.

- **Location:** `src/main/java/service/SampleRequestProcessor.java:11`, `src/main/java/client/Client.java:37`
- **Evidence:**
  ```java
  System.out.println(payloadUnwrapped);
  ```
- **Scenario:** In a P2PE deployment the payload is the sensitive data, such as a PAN. The plaintext goes to stdout and from there to container or log aggregation, which takes it out of the protected channel and into PCI scope. An authenticated peer can also inject CR/LF into the payload and forge log lines. The sample processor is the template integrators will copy.
- **Fix:** Remove the print. If logging is needed, use a logger and record only metadata such as the session `kid`, nonce, size and outcome, never the payload.

#### [LOW] Security failures are not logged (Confirmed)
**Status: Fixed (partially).** `ClientManager` now logs WARN through SLF4J for unknown or expired sessions and for decrypt, verify or replay rejections. The unknown `kid` is deliberately not logged, because it is attacker-controlled; a probe with a newline in the `kid` was verified not to reach the log. Still open: a metric or alert counter, and replacing the remaining `printStackTrace` and `System.out` calls in `JWUtil`, `JSONUtil` and `Connection`.

- **Location:** `src/main/java/util/ClientManager.java:17-31`
- **Evidence:**
  ```java
  } catch (SecurityException e) {
      throw new BadRequestException(e.getMessage(), e);
  ```
- **Scenario:** Replayed nonces, stale timestamps, decryption failures, failed signatures and unknown key IDs all become 400s. Dropwizard's exception mapper logs only 5xx, so replay or forgery attempts leave no trace and nothing can trigger an alert. Elsewhere the code uses `printStackTrace` and `System.out` rather than a logger (`JWUtil`, `JSONUtil`, `Connection`), so log levels and routing can't be controlled.
- **Fix:** Log each rejected request at WARN through SLF4J (which Dropwizard already provides), with the reason, `kid` and remote address, and add a metric counter (`@Metered` or a `Meter`) that can drive alerts. Replace the `printStackTrace` and `System.out` calls with a logger.

### A10 — Mishandling of Exceptional Conditions

#### [LOW] Peer key types aren't validated; a non-EC key fails only after the request has been processed (Confirmed)
**Status: Fixed.** `ClientHandler.parsePeerKey` accepts only public EC P-256 JWKs and runs before the KMS call. Missing, non-EC, wrong-curve and private keys are rejected with 400. Regression tests: `ClientHandlerTest.initRejectsNonEcKeysBeforeCallingKms` and `initRejectsMissingWrongCurveAndPrivateKeys`. Verified live: an RSA key sent to `/keyexchange` returns HTTP 400.

- **Location:** `src/main/java/util/ClientHandler.java:55-56`, `src/main/java/util/ClientManager.java:33-36`, `src/main/java/util/JWUtil.java:76,188`
- **Evidence:**
  ```java
  kaSenderKey = JWK.parse(kaPublicKey);                         // any key type accepted
  ...
  String processedResponse = processor.process(request.payload());
  ... clientHandler.wrap(...)  →  encrypterKey.toECKey()        // ClassCastException for RSA/oct keys
  ```
- **Scenario:** If a session registers an RSA or symmetric JWK as `kaPublicKey`, `/verify` runs `processor.process(...)` and then throws `ClassCastException` while encrypting the response, so the caller gets a 500 after the side effects have happened. A real processor (for example a payment) would commit work that the caller sees as failed. A non-EC `signPublicKey` causes a 500 rather than a 400. Missing fields cause `JWK.parse(null)` to fail with a 500. In addition, `ClientHandler.init` calls the KMS before parsing the client's keys, so invalid requests still cost a KMS round trip.
- **Fix:** In `KeyExchangeUtil` and `ClientHandler.init`, parse and validate the client keys first: both must be `ECKey`, on `Curve.P_256`, public-only, and non-null. Reject anything else with a 400, and only then call the KMS. Optionally prepare the response encryption before invoking the processor.

Also checked: decryption and verification fail closed, because `decryptAndVerify` turns `null` or empty into `SecurityException` and nothing on that path catches it and continues. Replay checks reject a missing nonce or timestamp. KMS or JSON failures surface as exceptions or 500s rather than proceeding with `null` keys.

## Out of scope / not checked
- **Dependency CVE scanning:** not run, because Maven isn't installed in this environment (see A03).
- **Runtime and deployment:** TLS protocol and cipher configuration as negotiated at runtime (the Dropwizard 5 defaults were assumed), network exposure, container settings, and how secrets are provided in real deployments.
- **Request size limits:** Jetty and Jersey body-size limits were not tested.
- **Nimbus library internals:** curve-point validation and algorithm handling were assumed correct for version 10.10.
- **Test code:** reviewed for shipped secrets only; none found.
