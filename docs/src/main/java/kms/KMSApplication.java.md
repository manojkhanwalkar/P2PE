# `src/main/java/kms/KMSApplication.java`

**Purpose:** Entry point and Dropwizard bootstrap for the Key Management Service (KMS), which hands out freshly generated key pairs.

**Depends on:** Dropwizard `Application`, [`KMSConfiguration.java`](KMSConfiguration.java.md), [`KMSResource.java`](KMSResource.java.md).
**Used by:** nothing (entry point; run as `kms.KMSApplication server src/main/resources/kms.yml`).

## Types
### `KMSApplication extends Application<KMSConfiguration>`
A standard Dropwizard application; the only thing it adds is registering the KMS REST resource.

## Functions
### `static void main(String[] args) throws Exception`
Starts Dropwizard with the given CLI arguments (typically `server <config.yml>`).

### `String getName()`
Returns `"KMS"` (shown in logs and the admin page).

### `void initialize(Bootstrap<KMSConfiguration> bootstrap)`
Enables `${ENV_VAR}` substitution in the YAML (see [`AppConfiguration.java`](../common/AppConfiguration.java.md)), so the keystore settings come from the environment.

### `void run(KMSConfiguration configuration, Environment environment)`
- **What it does:** creates a [`KMSResource.java`](KMSResource.java.md) with the `template`/`defaultName` config values and registers it with Jersey, exposing `POST /issuekey`.

## Notes
- Listens on the ports in [`kms.yml`](../../resources/kms.yml.md): HTTPS 8280 (application) and 8281 (admin).
