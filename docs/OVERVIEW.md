# P2PE — Code Overview

## What this project is

P2PE is a reference implementation of **application-level encryption between services**, built on top of TLS with the JOSE standards (JWK, JWS, JWE) via the Nimbus library. Every message is signed by the sender (ES256) and then encrypted to the recipient (ECDH-ES + A256GCM), giving a **JWE&lt;JWS&lt;payload&gt;&gt;** envelope. The repo contains three runnable pieces: a toy key-management service (KMS), a sample service, and a demo client. All are Java 26, and the two servers run on Dropwizard 5.

## Architecture

```mermaid
flowchart LR
    C["Client<br/>client.Client"] -- "POST /issuekey" --> K["KMS<br/>kms.KMSApplication :8280"]
    C -- "POST /keyexchange<br/>POST /verify" --> S["Sample service<br/>service.SampleApplication :8180"]
    S -- "POST /issuekey<br/>(once per key exchange)" --> K
```

- **KMS** ([`KMSResource.java`](src/main/java/kms/KMSResource.java.md)) generates two fresh EC P-256 key pairs per request, one for key agreement and one for signing, and returns them **including the private keys**. Nothing is stored.
- **Sample service** ([`SampleResource.java`](src/main/java/service/SampleResource.java.md)) does the handshake and then processes encrypted requests. It keeps one in-memory session per client ([`ClientCache.java`](src/main/java/util/ClientCache.java.md) → [`ClientHandler.java`](src/main/java/util/ClientHandler.java.md)).
- **Client** ([`Client.java`](src/main/java/client/Client.java.md)) gets its keys from the KMS, then talks to the service through [`ServiceConnector.java`](src/main/java/util/ServiceConnector.java.md).
- **Shared utilities:** all crypto is in [`JWUtil.java`](src/main/java/util/JWUtil.java.md). HTTPS calls go through [`Connection.java`](src/main/java/util/Connection.java.md) and JSON through [`JSONUtil.java`](src/main/java/util/JSONUtil.java.md).

## Key flows

```mermaid
sequenceDiagram
    participant C as Client (ServiceConnector)
    participant K as KMS
    participant S as Service (SampleResource)
    C->>K: POST /issuekey
    K-->>C: client ECDH + signing key pairs
    C->>S: POST /keyexchange {client public keys}
    S->>K: POST /issuekey
    K-->>S: server ECDH + signing key pairs
    S-->>C: {server public keys}  (session cached under server ECDH kid)
    C->>S: POST /verify JWE(kid=server ECDH)<JWS(client sig)<payload>>
    S-->>C: JWE(kid=client ECDH)<JWS(server sig)<response>>
```

1. **Key issuance:** the client calls `KMSConnector.getKAandSignPubKeys`, which reaches `KMSResource.issueKey` → `JWUtil.createKey` ×2.
2. **Key exchange:**
   - `ServiceConnector.init` posts the client's public keys to `/keyexchange`.
   - On the server, `KeyExchangeUtil.keyExchange` creates a `ClientHandler`. `ClientHandler.init` fetches server keys from the KMS and parses the client's keys.
   - The handler is cached under the server ECDH key's `kid`, and the server's public keys are returned.
3. **Encrypted request:**
   - `ServiceConnector.send` → `JWUtil.signAndEncrypt` → `POST /verify`.
   - Server: `ClientManager.process` reads the JWE `kid`, finds the session, then calls `ClientHandler.unwrap` (`JWUtil.decryptAndVerify`), `SampleRequestProcessor.process` (echo) and `ClientHandler.wrap`.
   - Client: `ServiceConnector.UnwrapResponse` decrypts and verifies the reply.
4. **Errors:** a malformed JWE, an unknown session, or a failed decryption or signature check returns HTTP 400. A KMS outage during the key exchange returns HTTP 500.

## Directory / module map

| Package / path | Responsibility |
|---|---|
| `src/main/java/client` | Demo client entry point. |
| `src/main/java/kms` | KMS Dropwizard app, config and `/issuekey` resource. |
| `src/main/java/service` | Sample service Dropwizard app, config, resource and the echo processor. |
| `src/main/java/common` | Shared Dropwizard config base and the `Processor` interface. |
| `src/main/java/data` | JSON request/response beans. |
| `src/main/java/util` | Crypto (`JWUtil`), HTTP/JSON helpers, client and server session logic. |
| `src/main/resources` | Dropwizard YAML configs and the dev TLS keystore `mytestkey.jks`. |
| `src/test/java` | Unit tests for `JWUtil` plus exploratory Nimbus examples. |

## File index

| File | Purpose |
|---|---|
| [`pom.xml`](pom.xml.md) | Maven build: Java 26, dependencies, shaded uber jar. |
| [`client/Client.java`](src/main/java/client/Client.java.md) | Demo client running the full flow once. |
| [`common/AppConfiguration.java`](src/main/java/common/AppConfiguration.java.md) | Shared Dropwizard config (`template`, `defaultName`). |
| [`common/Processor.java`](src/main/java/common/Processor.java.md) | Interface for server-side business logic. |
| [`data/EncryptedSignedMessage.java`](src/main/java/data/EncryptedSignedMessage.java.md) | Base bean holding the JWE string. |
| [`data/EncryptedSignedRequest.java`](src/main/java/data/EncryptedSignedRequest.java.md) | `/verify` request body. |
| [`data/EncryptedSignedResponse.java`](src/main/java/data/EncryptedSignedResponse.java.md) | `/verify` response body. |
| [`data/KeyExchange.java`](src/main/java/data/KeyExchange.java.md) | `/keyexchange` body (public keys). |
| [`data/KeyRequest.java`](src/main/java/data/KeyRequest.java.md) | `/issuekey` request body (unused field). |
| [`data/KeyResponse.java`](src/main/java/data/KeyResponse.java.md) | `/issuekey` response (private JWKs). |
| [`kms/KMSApplication.java`](src/main/java/kms/KMSApplication.java.md) | KMS entry point. |
| [`kms/KMSConfiguration.java`](src/main/java/kms/KMSConfiguration.java.md) | KMS config type. |
| [`kms/KMSResource.java`](src/main/java/kms/KMSResource.java.md) | `POST /issuekey`. |
| [`service/SampleApplication.java`](src/main/java/service/SampleApplication.java.md) | Sample service entry point. |
| [`service/SampleConfiguration.java`](src/main/java/service/SampleConfiguration.java.md) | Sample service config type. |
| [`service/SampleRequestProcessor.java`](src/main/java/service/SampleRequestProcessor.java.md) | Echo business logic. |
| [`service/SampleResource.java`](src/main/java/service/SampleResource.java.md) | `POST /keyexchange`, `POST /verify`. |
| [`util/ClientCache.java`](src/main/java/util/ClientCache.java.md) | Server session store (kid → handler). |
| [`util/ClientHandler.java`](src/main/java/util/ClientHandler.java.md) | One server-side session: keys + wrap/unwrap. |
| [`util/ClientManager.java`](src/main/java/util/ClientManager.java.md) | Server pipeline for one encrypted request. |
| [`util/Connection.java`](src/main/java/util/Connection.java.md) | HTTPS JSON POST client. |
| [`util/JSONUtil.java`](src/main/java/util/JSONUtil.java.md) | Jackson helpers. |
| [`util/JWUtil.java`](src/main/java/util/JWUtil.java.md) | Key generation, JWE/JWS, sign+encrypt envelope. |
| [`util/KeyExchangeUtil.java`](src/main/java/util/KeyExchangeUtil.java.md) | Server-side key-exchange handshake. |
| [`util/KMSConnector.java`](src/main/java/util/KMSConnector.java.md) | Client for the KMS. |
| [`util/ServiceConnector.java`](src/main/java/util/ServiceConnector.java.md) | Client side of a session. |
| [`resources/kms.yml`](src/main/resources/kms.yml.md) | KMS config (ports 8280/8281). |
| [`resources/sample.yml`](src/main/resources/sample.yml.md) | Service config (ports 8180/8181). |
| [`test/NimbusFeaturesTest.java`](src/test/java/NimbusFeaturesTest.java.md) | Exploratory Nimbus examples. |
| [`test/util/JWUtilTest.java`](src/test/java/util/JWUtilTest.java.md) | Tests for the crypto envelope. |

## Build, run, test

You need Java 26 and Maven 3.9+. Run everything **from the repository root**, because the YAML configs reference the keystore by a relative path.

```bash
mvn package          # compiles, runs tests, builds target/P2PE-1.0-SNAPSHOT.jar (uber jar)
mvn test             # tests only

JAR=target/P2PE-1.0-SNAPSHOT.jar
TRUST="-Djavax.net.ssl.trustStore=src/main/resources/mytestkey.jks -Djavax.net.ssl.trustStorePassword=password"

java -cp $JAR kms.KMSApplication server src/main/resources/kms.yml               # KMS on https://localhost:8280
java $TRUST -cp $JAR service.SampleApplication server src/main/resources/sample.yml  # service on https://localhost:8180
java $TRUST -cp $JAR client.Client                                                # prints "Hello World Request"
```

- **Ports:**
  - KMS: 8280 (API) and 8281 (admin).
  - Service: 8180 (API) and 8181 (admin).
  - All HTTPS.
- **TLS:** `src/main/resources/mytestkey.jks` is a self-signed development certificate for `localhost` (PKCS12, password `password`). The service and the client need the trust-store flags because both make HTTPS calls; the KMS only serves. Hostname verification is on.
- **Start order:** KMS first, then the service, then the client. The service needs the KMS for every key exchange.
- **Tests:** 12 tests; `NimbusFeaturesTest.test1` is skipped unless its external PEM files exist.

## Known issues & caveats

- **The KMS issues private keys to anyone:** no authentication, and private JWKs are returned over the network ([`KMSResource.java`](src/main/java/kms/KMSResource.java.md), [`KeyResponse.java`](src/main/java/data/KeyResponse.java.md)).
- **Sessions are never evicted:** [`ClientCache.java`](src/main/java/util/ClientCache.java.md) grows with every key exchange, and sessions don't survive a restart.
- **No replay protection:** there are no nonces or timestamps, as the README's exclusions list notes.
- **Hard-coded endpoints:** `https://localhost:8280/` in [`KMSConnector.java`](src/main/java/util/KMSConnector.java.md) and `https://localhost:8180/` in [`Client.java`](src/main/java/client/Client.java.md).
- **Plain-text dev keystore password** (`password`) in [`kms.yml`](src/main/resources/kms.yml.md) and [`sample.yml`](src/main/resources/sample.yml.md).
- **Low-level helpers return `null` or empty on failure:** [`JWUtil.java`](src/main/java/util/JWUtil.java.md) and [`JSONUtil.java`](src/main/java/util/JSONUtil.java.md) print stack traces and return `null` or empty instead of throwing. Only `decryptAndVerify` turns failures into exceptions.
- **Unused leftovers from the Dropwizard template:**
  - The `template` / `defaultName` config values are required but never used ([`AppConfiguration.java`](src/main/java/common/AppConfiguration.java.md)).
  - `KeyRequest.clientName` is never set or read.
  - `JSONUtil.toPrettyJSON` is unused.
- **Noisy stdout logging:** HTTP responses, JWE headers, key IDs and decrypted payloads are printed with `System.out` rather than a logger, and plaintext ends up in the logs ([`SampleRequestProcessor.java`](src/main/java/service/SampleRequestProcessor.java.md)).
- **Dependencies not used directly by the code:** Guava, commons-io, commons-codec, BouncyCastle and CBOR are declared in [`pom.xml`](pom.xml.md) but not used directly.
