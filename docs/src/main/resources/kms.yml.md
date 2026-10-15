# `src/main/resources/kms.yml`

**Purpose:** Dropwizard configuration for the KMS ([`KMSApplication.java`](../java/kms/KMSApplication.java.md)).

**Read by:** Dropwizard into [`KMSConfiguration.java`](../java/kms/KMSConfiguration.java.md) when started with `server src/main/resources/kms.yml`.

## Keys
| Key | Value | Meaning |
|---|---|---|
| `template` | `Hello, %s!` | Required by `AppConfiguration`; unused. |
| `defaultName` | `Stranger` | Required by `AppConfiguration`; unused. |
| `server.applicationConnectors[0]` | `type: https`, `port: 8280` | REST API (`/issuekey`). |
| `server.adminConnectors[0]` | `type: https`, `bindHost: 127.0.0.1`, `port: 8281` | Dropwizard admin (metrics, health checks, tasks). Local-only, because it has no authentication. |
| `keyStorePath` | `${P2PE_KEYSTORE_PATH}` | TLS keystore, taken from the environment. A relative path resolves against the working directory. |
| `keyStorePassword` | `${P2PE_KEYSTORE_PASSWORD}` | Keystore password, taken from the environment. |
| `keyStoreType` | `PKCS12` | Keystore format. |

`${...}` values are resolved by `AppConfiguration.enableEnvironmentSubstitution`; startup fails if a variable is unset.

The commented-out `h2` connector block is an inactive HTTP/2 example.
