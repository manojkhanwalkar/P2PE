# `src/main/java/util/Connection.java`

**Purpose:** Minimal HTTPS JSON client used to call the KMS and the service.

**Depends on:** `java.net.http.HttpClient`, [`JSONUtil.java`](JSONUtil.java.md).
**Used by:** [`KMSConnector.java`](KMSConnector.java.md), [`ServiceConnector.java`](ServiceConnector.java.md).

## Overview
All instances share one static `HttpClient` (which is thread-safe). A `Connection` stores a base URL and POSTs JSON to `base + action`. TLS uses the JVM's default trust store, so for the self-signed dev certificate the JVM must be started with `-Djavax.net.ssl.trustStore=...`. Hostname verification is on.

## Types
### `Connection`
- `CLIENT` — the shared `HttpClient`.
- `target` — base URL, e.g. `https://localhost:8180/`. It must end in `/` because actions are appended by plain string concatenation.

## Functions
### `Connection(String target)`
Stores the base URL.

### `Optional<String> sendSimple(String request, String action)`
POSTs a raw JSON string; see `post`.

### `<T> T send(Object request, String action, Class<T> responseType)`
- **What it does:** serialises `request` to JSON, POSTs it, and deserialises the body as `responseType`.
- **Errors:** throws `RuntimeException("Request to <url> failed")` when the call fails, the status is not 200, or the body can't be parsed. The original cause is not attached.

### `private Optional<String> post(String str, String url)`
- **What it does:** sends `POST target+url` with `Content-Type: application/json` and a 1-minute timeout, and prints the `HttpResponse` summary to stdout.
- **Returns:** the body if the status is 200, otherwise empty.
- **Errors:** an `IOException` is printed and swallowed (returns empty). On `InterruptedException` it restores the interrupt flag and returns empty.
