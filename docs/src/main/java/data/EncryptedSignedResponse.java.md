# `src/main/java/data/EncryptedSignedResponse.java`

**Purpose:** Response body type returned by the service's `/verify` endpoint. Same shape as the request.

**Depends on:** [`EncryptedSignedMessage.java`](EncryptedSignedMessage.java.md).
**Used by:** [`ClientManager.java`](../util/ClientManager.java.md) (builds it), [`SampleResource.java`](../service/SampleResource.java.md) (returns it), [`ServiceConnector.java`](../util/ServiceConnector.java.md) (parses it).

## Types
### `EncryptedSignedResponse extends EncryptedSignedMessage`
No extra fields or methods. JSON: `{"encryptedSignedPayload": "<JWE>"}`.
