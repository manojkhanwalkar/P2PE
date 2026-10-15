# `src/main/java/util/JSONUtil.java`

**Purpose:** Static Jackson helpers for turning objects into JSON and back on the client side of HTTP calls.

**Depends on:** Jackson `ObjectMapper` and `JavaTimeModule`.
**Used by:** [`Connection.java`](Connection.java.md), [`Client.java`](../client/Client.java.md).

## Overview
One shared, statically configured `ObjectMapper` with the Java Time module registered. The server side does not use this class; Dropwizard uses its own mapper.

## Functions
### `static String toJSON(Object object)`
Serialises to compact JSON. **Returns `null`** on failure (the stack trace is printed).

### `static <T> T fromJSON(String json, Class<T> clazz)`
Deserialises JSON into `clazz`. **Returns `null`** on failure (the stack trace is printed). Unknown JSON properties cause a failure, because this is Jackson's default.

### `static String toPrettyJSON(Object object)`
Like `toJSON` but indented. Currently unused.
