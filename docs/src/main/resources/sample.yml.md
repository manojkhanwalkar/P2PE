# `src/main/resources/sample.yml`

**Purpose:** Dropwizard configuration for the sample service ([`SampleApplication.java`](../java/service/SampleApplication.java.md)).

**Read by:** Dropwizard into [`SampleConfiguration.java`](../java/service/SampleConfiguration.java.md) when started with `server src/main/resources/sample.yml`.

## Keys
Identical in structure to [`kms.yml`](kms.yml.md), except for the ports:

| Key | Value | Meaning |
|---|---|---|
| `template`, `defaultName` | `Hello, %s!`, `Stranger` | Required; unused. |
| application connector | `https`, port `8180` | REST API (`/keyexchange`, `/verify`). |
| admin connector | `https`, port `8181` | Dropwizard admin. |
| `keyStorePath` / `keyStorePassword` | `src/main/resources/mytestkey.jks` / `password` | Same dev keystore; path relative to the working directory. |
