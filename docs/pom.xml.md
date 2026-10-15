# `pom.xml`

**Purpose:** Maven build definition: dependencies, Java version and packaging into a single runnable "uber" jar.

**Used by:** `mvn` (and IDE import).

## Key settings
- **Java:** `maven.compiler.release` = 26.
- **Dependency management:** imports the Jackson BOM (pinned to 2.22.3, ahead of Dropwizard's version), the JUnit BOM (6.1.3), and the Dropwizard 5.0.2 BOM.
- **Dependencies:**
  - `dropwizard-core` — REST servers.
  - `nimbus-jose-jwt` — JOSE crypto.
  - Jackson `jsr310`.
  - Guava (the session cache in `ClientCache`).
  - Test scope: `junit-jupiter`; Tink (Nimbus' Ed25519 support); BouncyCastle `bcprov`/`bcpkix` (`jdk18on`, PEM parsing in `NimbusFeaturesTest`).
  - Unused commons-io, commons-codec and Jackson CBOR were removed, and Tink and BouncyCastle moved to test scope, so they're no longer shipped in the uber jar (OWASP A03).
- **Plugins:**
  - compiler 3.16.0, surefire and failsafe 3.6.0.
  - shade 3.6.2, bound to `package`. It produces `target/P2PE-1.0-SNAPSHOT.jar` with every dependency inside.
  - The shade configuration strips signature files (`META-INF/*.SF`, `*.DSA`, `*.RSA`, `*.EC`; signed BouncyCastle jars would otherwise break the uber jar) and merges `META-INF/services` (Dropwizard finds its connector and logging factories through `ServiceLoader`).
- No main class is set in the manifest; run classes with `java -cp`.
