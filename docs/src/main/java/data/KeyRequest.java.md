# `src/main/java/data/KeyRequest.java`

**Purpose:** Request body for the KMS `issuekey` endpoint.

**Depends on:** nothing.
**Used by:** [`Client.java`](../client/Client.java.md), [`ClientHandler.java`](../util/ClientHandler.java.md), [`KMSConnector.java`](../util/KMSConnector.java.md), [`KMSResource.java`](../kms/KMSResource.java.md).

## Types
### `KeyRequest`
- `clientName` — intended to identify the caller.

## Functions
- `getClientName()` / `setClientName(String)` — plain accessors.

## Notes
- `clientName` is never set by any caller and never read by the KMS, so every request is the empty object `{"clientName":null}`.
