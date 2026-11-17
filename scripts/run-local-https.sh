#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
keystore_path="${EMP_KEYSTORE_PATH:-$project_root/dev-keystore.p12}"
keystore_password="${EMP_KEYSTORE_PASSWORD:-changeit}"

if [[ ! -f "$keystore_path" ]]; then
    mkdir -p "$(dirname "$keystore_path")"
    keytool -genkeypair -alias erasmus-demo -keyalg RSA -keysize 2048 \
        -storetype PKCS12 -keystore "$keystore_path" -validity 365 \
        -storepass "$keystore_password" -keypass "$keystore_password" \
        -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"
fi

export EMP_KEYSTORE="file:$keystore_path"
export EMP_KEYSTORE_PASSWORD="$keystore_password"
export SPRING_PROFILES_ACTIVE="demo,https"

exec "$project_root/mvnw" -f "$project_root/pom.xml" spring-boot:run
