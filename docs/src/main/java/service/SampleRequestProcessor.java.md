# `src/main/java/service/SampleRequestProcessor.java`

**Purpose:** The sample service's business logic: an echo.

**Depends on:** [`Processor.java`](../common/Processor.java.md).
**Used by:** [`SampleResource.java`](SampleResource.java.md).

## Types
### `SampleRequestProcessor implements Processor<String, String>`

## Functions
### `String process(String payloadUnwrapped)`
- **What it does:** prints the decrypted, verified payload to stdout and returns it unchanged.
- **Returns:** the same string, which is then signed and encrypted back to the client.

## Notes
- Replace this class to put real logic behind the encrypted channel.
