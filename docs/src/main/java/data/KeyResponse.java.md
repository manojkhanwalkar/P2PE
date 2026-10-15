# `src/main/java/data/KeyResponse.java`

**Purpose:** Response body of the KMS `issuekey` endpoint: a freshly generated key pair for encryption and one for signing.

**Depends on:** nothing.
**Used by:** [`KMSResource.java`](../kms/KMSResource.java.md) (builds it), [`KMSConnector.java`](../util/KMSConnector.java.md) (parses it), [`Client.java`](../client/Client.java.md) and [`ClientHandler.java`](../util/ClientHandler.java.md) (consume it).

## Types
### `KeyResponse`
- `kaKey` — JSON of an EC P-256 JWK **including its private part**, used for key agreement.
- `signingKey` — JSON of an EC P-256 JWK **including its private part**, used for signing.

## Functions
- `getKaKey()` / `setKaKey(String)`, `getSigningKey()` / `setSigningKey(String)` — plain accessors.

## Notes
- Private keys travel over the network in this body (protected only by TLS).
