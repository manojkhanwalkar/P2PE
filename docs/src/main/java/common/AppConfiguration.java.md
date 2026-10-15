# `src/main/java/common/AppConfiguration.java`

**Purpose:** Dropwizard configuration class shared by both servers; it maps the top-level keys of [`kms.yml`](../../resources/kms.yml.md) and [`sample.yml`](../../resources/sample.yml.md).

**Depends on:** Dropwizard `io.dropwizard.core.Configuration`, Jackson `@JsonProperty`, Jakarta Validation `@NotEmpty`.
**Used by:** [`KMSConfiguration.java`](../kms/KMSConfiguration.java.md), [`SampleConfiguration.java`](../service/SampleConfiguration.java.md) (subclasses).

## Overview
Inherits everything Dropwizard needs from `Configuration` (notably the `server:` block with connectors and ports) and adds two string properties left over from the Dropwizard "Hello World" template. Both are validated as non-empty at startup.

## Types
### `AppConfiguration`
- `template` (`@NotEmpty`, no default) — YAML key `template`. Must be present or startup fails validation.
- `defaultName` (`@NotEmpty`, default `"Stranger"`) — YAML key `defaultName`.

## Functions
- `getTemplate()` / `setTemplate(String)` and `getDefaultName()` / `setDefaultName(String)` — plain Jackson-bound accessors.
- `static void enableEnvironmentSubstitution(Bootstrap<?> bootstrap)` — makes Dropwizard resolve `${ENV_VAR}` references in the YAML (strict: startup fails on an unset variable). Both applications call it from `initialize`.

## Notes
- The values are passed into [`KMSResource.java`](../kms/KMSResource.java.md) and [`SampleResource.java`](../service/SampleResource.java.md), which store them but never use them.
