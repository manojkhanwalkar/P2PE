# `src/main/java/common/Processor.java`

**Purpose:** Tiny functional interface for "business logic" that runs on a decrypted, verified payload on the server side.

**Depends on:** nothing.
**Used by:** [`ClientManager.java`](../util/ClientManager.java.md) (calls it), [`SampleRequestProcessor.java`](../service/SampleRequestProcessor.java.md) (implements it), [`SampleResource.java`](../service/SampleResource.java.md) (holds an instance).

## Types
### `Processor<T, R>`
Generic transformer from an input `T` to a result `R`. In this project it is always `Processor<String, String>`: plaintext request in, plaintext response out.

## Functions
### `R process(T t)`
- **What it does:** application-specific handling of one request.
- **Returns:** the response that will be signed and encrypted back to the caller.
- **Errors:** implementation-defined; anything thrown propagates to the REST layer (HTTP 500).
