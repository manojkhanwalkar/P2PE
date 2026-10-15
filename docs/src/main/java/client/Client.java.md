# `src/main/java/client/Client.java`

**Purpose:** Command-line demo client that exercises the whole P2PE flow once: get keys from the KMS, exchange keys with the sample service, send one encrypted+signed message and print the decrypted reply.

**Depends on:** [`KMSConnector.java`](../util/KMSConnector.java.md), [`ServiceConnector.java`](../util/ServiceConnector.java.md), [`JSONUtil.java`](../util/JSONUtil.java.md), [`KeyRequest.java`](../data/KeyRequest.java.md); Nimbus `JWK`.
**Used by:** nothing (entry point, run with `java ... client.Client`).

## Overview
A single `main` method. All endpoints are hard-coded: KMS at `KMSConnector.DEFAULT_URL` (`https://localhost:8280/`) and the service at `https://localhost:8180/`. Because both use HTTPS with a self-signed certificate, the JVM must be started with a trust store that contains it (see [`OVERVIEW.md`](../../../../OVERVIEW.md)).

## Functions
### `static void main(String[] args) throws Exception`
- **What it does:**
  1. Calls the KMS `issuekey` endpoint to obtain this client's key-agreement (ECDH) and signing private keys.
  2. Builds a `ServiceConnector` with those keys and calls `init()`, which performs the `/keyexchange` handshake with the service.
  3. JSON-encodes the string `"Hello World Request"`, sends it to the service's `/verify` endpoint as JWE&lt;JWS&lt;payload&gt;&gt;, and prints the verified, decrypted response to stdout.
- **Parameters:** `args` is ignored.
- **Errors:** any failure (KMS/service unreachable, non-200 response, bad signature) propagates as an exception and ends the program.

## Notes
- Hard-coded URLs; no CLI arguments or configuration.
