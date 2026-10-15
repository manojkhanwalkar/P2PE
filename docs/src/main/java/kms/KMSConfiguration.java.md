# `src/main/java/kms/KMSConfiguration.java`

**Purpose:** Configuration type of the KMS application; exists so the KMS has its own named config class.

**Depends on:** [`AppConfiguration.java`](../common/AppConfiguration.java.md).
**Used by:** [`KMSApplication.java`](KMSApplication.java.md).

## Types
### `KMSConfiguration extends AppConfiguration`
Adds nothing; all keys come from [`AppConfiguration.java`](../common/AppConfiguration.java.md) and Dropwizard's `Configuration`.
