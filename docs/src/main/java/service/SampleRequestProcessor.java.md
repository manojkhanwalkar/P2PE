# `src/main/java/service/SampleRequestProcessor.java`

**Purpose:** The sample service's business logic: an echo.

**Depends on:** [`Processor.java`](../common/Processor.java.md).
**Used by:** [`SampleResource.java`](SampleResource.java.md).

## Types
### `SampleRequestProcessor implements Processor<String, String>`

## Functions
### `String process(String payloadUnwrapped)`
- **What it does:** returns the decrypted, verified payload unchanged. It deliberately doesn't log it: in a P2PE deployment the payload is the sensitive data.
- **Returns:** the same string, which is then signed and encrypted back to the client.

## Notes
- Replace this class to put real logic behind the encrypted channel.
