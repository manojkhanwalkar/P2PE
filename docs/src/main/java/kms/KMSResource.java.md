# `src/main/java/kms/KMSResource.java`

**Purpose:** The KMS REST endpoint. Each call generates and returns two brand-new EC key pairs (one for encryption, one for signing).

**Depends on:** [`JWUtil.java`](../util/JWUtil.java.md), [`KeyRequest.java`](../data/KeyRequest.java.md), [`KeyResponse.java`](../data/KeyResponse.java.md); Jakarta REST annotations; Dropwizard Metrics `@Timed`.
**Used by:** [`KMSApplication.java`](KMSApplication.java.md) (registers it). Called over HTTP by [`KMSConnector.java`](../util/KMSConnector.java.md).

## Overview
Mounted at `/` and produces JSON. There is no storage: keys are generated per request and forgotten immediately.

## Types
### `KMSResource`
- `template`, `defaultName` — copied from configuration; unused.

## Functions
### `KMSResource(String template, String defaultName)`
Stores the two configuration strings.

### `KeyResponse issueKey(KeyRequest request)` — `POST /issuekey`
- **What it does:** calls `JWUtil.createKey()` twice and returns both keys as JWK JSON strings.
- **Parameters:** `request` is ignored.
- **Returns:** a [`KeyResponse.java`](../data/KeyResponse.java.md) with private+public JWKs in `kaKey` and `signingKey`. Timed via Dropwizard Metrics.
- **Errors:** if key generation fails `createKey()` returns `null`, and this method would then throw a `NullPointerException` (HTTP 500).

## Notes
- No authentication or authorisation: anyone who can reach port 8280 can obtain key pairs.
- Every call returns different keys; nothing ties keys to a client.
