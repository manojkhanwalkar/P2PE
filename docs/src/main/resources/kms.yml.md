# `src/main/resources/kms.yml`

**Purpose:** Dropwizard configuration for the KMS ([`KMSApplication.java`](../java/kms/KMSApplication.java.md)).

**Read by:** Dropwizard into [`KMSConfiguration.java`](../java/kms/KMSConfiguration.java.md) when started with `server src/main/resources/kms.yml`.

## Keys
| Key | Value | Meaning |
|---|---|---|
| `template` | `Hello, %s!` | Required by `AppConfiguration`; unused. |
| `defaultName` | `Stranger` | Required by `AppConfiguration`; unused. |
| `server.applicationConnectors[0]` | `type: https`, `port: 8280` | REST API (`/issuekey`). |
| `server.adminConnectors[0]` | `type: https`, `port: 8281` | Dropwizard admin (metrics, health checks). |
| `keyStorePath` | `src/main/resources/mytestkey.jks` | TLS keystore. **Relative to the working directory**, so start the server from the repository root. |
| `keyStorePassword` | `password` | Plain-text keystore password (development only). |

The commented-out `h2` connector block is an inactive HTTP/2 example.
