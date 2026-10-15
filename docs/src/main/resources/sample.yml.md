# `src/main/resources/sample.yml`

**Purpose:** Dropwizard configuration for the sample service ([`SampleApplication.java`](../java/service/SampleApplication.java.md)).

**Read by:** Dropwizard into [`SampleConfiguration.java`](../java/service/SampleConfiguration.java.md) when started with `server src/main/resources/sample.yml`.

## Keys
Identical in structure to [`kms.yml`](kms.yml.md), except for the ports:

| Key | Value | Meaning |
|---|---|---|
| `template`, `defaultName` | `Hello, %s!`, `Stranger` | Required; unused. |
| application connector | `https`, port `8180` | REST API (`/keyexchange`, `/verify`). |
| admin connector | `https`, `bindHost: 127.0.0.1`, port `8181` | Dropwizard admin, local-only. |
| `keyStorePath` / `keyStorePassword` / `keyStoreType` | `${P2PE_KEYSTORE_PATH}` / `${P2PE_KEYSTORE_PASSWORD}` / `PKCS12` | Same keystore settings as the KMS, from the environment. |
