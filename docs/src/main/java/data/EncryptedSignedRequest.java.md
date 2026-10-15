# `src/main/java/data/EncryptedSignedRequest.java`

**Purpose:** Request body type for the service's `/verify` endpoint. Exists only to give requests a distinct type; it adds nothing to [`EncryptedSignedMessage.java`](EncryptedSignedMessage.java.md).

**Depends on:** [`EncryptedSignedMessage.java`](EncryptedSignedMessage.java.md).
**Used by:** [`ServiceConnector.java`](../util/ServiceConnector.java.md) (builds it), [`SampleResource.java`](../service/SampleResource.java.md) and [`ClientManager.java`](../util/ClientManager.java.md) (receive it).

## Types
### `EncryptedSignedRequest extends EncryptedSignedMessage`
No extra fields or methods. JSON: `{"encryptedSignedPayload": "<JWE>"}`.
