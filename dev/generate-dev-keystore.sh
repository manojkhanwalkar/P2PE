#!/usr/bin/env bash
# Creates a local, self-signed TLS keystore for localhost (servers) and a certificate-only truststore (clients).
# Generated per developer and gitignored, so the private key is never committed or shipped in the jar.
set -euo pipefail

: "${P2PE_KEYSTORE_PASSWORD:?set P2PE_KEYSTORE_PASSWORD first}"

dir="$(cd "$(dirname "$0")" && pwd)"
keystore="$dir/dev-keystore.p12"
truststore="$dir/dev-truststore.p12"
cert="$(mktemp)"
trap 'rm -f "$cert"' EXIT

if [[ -e "$keystore" ]]; then
    echo "$keystore already exists; delete it to regenerate" >&2
    exit 0
fi

keytool -genkeypair -alias localhost -keyalg EC -groupname secp256r1 -sigalg SHA256withECDSA \
        -validity 365 -dname CN=localhost -ext SAN=dns:localhost,ip:127.0.0.1 \
        -storetype PKCS12 -keystore "$keystore" -storepass "$P2PE_KEYSTORE_PASSWORD"

keytool -exportcert -alias localhost -rfc -file "$cert" \
        -keystore "$keystore" -storepass "$P2PE_KEYSTORE_PASSWORD"

rm -f "$truststore"
keytool -importcert -noprompt -alias localhost -file "$cert" \
        -storetype PKCS12 -keystore "$truststore" -storepass "$P2PE_KEYSTORE_PASSWORD"

echo "Created $keystore and $truststore"
