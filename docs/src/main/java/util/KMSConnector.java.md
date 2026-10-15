# `src/main/java/util/KMSConnector.java`

**Purpose:** Client for the KMS `issuekey` endpoint.

**Depends on:** [`Connection.java`](Connection.java.md), [`KeyRequest.java`](../data/KeyRequest.java.md), [`KeyResponse.java`](../data/KeyResponse.java.md).
**Used by:** [`Client.java`](../client/Client.java.md), [`ClientHandler.java`](ClientHandler.java.md).

## Types
### `KMSConnector`
- `DEFAULT_URL` — `"https://localhost:8280/"`, the hard-coded KMS location used by both the client and the service.
- `connection` — a [`Connection.java`](Connection.java.md) to the KMS base URL.

## Functions
### `KMSConnector(String url)`
Creates the connection to `url` (must end in `/`).

### `KeyResponse getKAandSignPubKeys(KeyRequest request)`
- **What it does:** `POST issuekey` and parses the reply.
- **Returns:** a [`KeyResponse.java`](../data/KeyResponse.java.md). Despite the method name, it contains **private** keys.
- **Errors:** `RuntimeException` if the call fails or returns a non-200 status.
