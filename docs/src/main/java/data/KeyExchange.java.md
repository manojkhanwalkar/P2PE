# `src/main/java/data/KeyExchange.java`

**Purpose:** JSON body for the `/keyexchange` handshake, used in both directions: the client sends its public keys, the service replies with its own.

**Depends on:** nothing.
**Used by:** [`ServiceConnector.java`](../util/ServiceConnector.java.md), [`KeyExchangeUtil.java`](../util/KeyExchangeUtil.java.md), [`SampleResource.java`](../service/SampleResource.java.md).

## Types
### `KeyExchange`
- `kaPublicKey` — JSON of a public EC JWK used for key agreement (ECDH-ES encryption).
- `signPublicKey` — JSON of a public EC JWK used to verify ES256 signatures.

## Functions
- `getKaPublicKey()` / `setKaPublicKey(String)`, `getSignPublicKey()` / `setSignPublicKey(String)` — plain accessors.
