# `src/main/java/service/SampleResource.java`

**Purpose:** The sample service's REST endpoints: the key-exchange handshake and the encrypted message endpoint.

**Depends on:** [`KeyExchangeUtil.java`](../util/KeyExchangeUtil.java.md), [`ClientManager.java`](../util/ClientManager.java.md), [`ClientCache.java`](../util/ClientCache.java.md), [`SampleRequestProcessor.java`](SampleRequestProcessor.java.md), the `data` classes; Jakarta REST; Dropwizard Metrics.
**Used by:** [`SampleApplication.java`](SampleApplication.java.md) (registers one instance). Called over HTTP by [`ServiceConnector.java`](../util/ServiceConnector.java.md).

## Overview
Mounted at `/`, produces JSON. One instance is registered, so its `clientCache` acts as the service-wide store of client sessions, shared by all request threads.

## Types
### `SampleResource`
- `template`, `defaultName` — from configuration; unused.
- `clientCache` — [`ClientCache.java`](../util/ClientCache.java.md) mapping server-issued key IDs to client sessions.
- `processor` — the [`SampleRequestProcessor.java`](SampleRequestProcessor.java.md) that handles decrypted requests.

## Functions
### `SampleResource(String template, String defaultName)`
Stores the configuration strings.

### `KeyExchange keyexchange(KeyExchange request)` — `POST /keyexchange`
- **What it does:** delegates to `KeyExchangeUtil.keyExchange`, which fetches new server keys from the KMS, stores a session in `clientCache`, and returns the server's public keys.
- **Errors:** HTTP 400 if the client's keys don't parse; HTTP 500 if the KMS is unreachable.

### `EncryptedSignedResponse verify(EncryptedSignedRequest encryptedSignedRequest)` — `POST /verify`
- **What it does:** delegates to `ClientManager.process`: decrypt, verify the signature, run `processor`, then sign and encrypt the result back.
- **Errors:** HTTP 400 for a malformed JWE, an unknown session, or a failed decryption or signature check.

## Notes
- Despite its name, `/verify` is the general "process an encrypted request" endpoint.
