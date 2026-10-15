# `src/main/java/service/SampleApplication.java`

**Purpose:** Entry point and Dropwizard bootstrap for the sample service, which accepts key exchanges and encrypted+signed requests.

**Depends on:** Dropwizard `Application`, [`SampleConfiguration.java`](SampleConfiguration.java.md), [`SampleResource.java`](SampleResource.java.md).
**Used by:** nothing (entry point; run as `service.SampleApplication server src/main/resources/sample.yml`).

## Types
### `SampleApplication extends Application<SampleConfiguration>`
Standard Dropwizard application that registers the sample REST resource.

## Functions
### `static void main(String[] args) throws Exception`
Starts Dropwizard with the given CLI arguments.

### `String getName()`
Returns `"Sample Service"`.

### `void initialize(Bootstrap<SampleConfiguration> bootstrap)`
Enables `${ENV_VAR}` substitution in the YAML (see [`AppConfiguration.java`](../common/AppConfiguration.java.md)), so the keystore settings come from the environment.

### `void run(SampleConfiguration configuration, Environment environment)`
Creates a [`SampleResource.java`](SampleResource.java.md) (with the unused `template`/`defaultName` values) and registers it with Jersey, exposing `POST /keyexchange` and `POST /verify`.

## Notes
- Listens on HTTPS 8180 (application) and 8181 (admin), per [`sample.yml`](../../resources/sample.yml.md).
- At runtime the service itself calls the KMS over HTTPS, so its JVM also needs a trust store that contains the KMS certificate.
