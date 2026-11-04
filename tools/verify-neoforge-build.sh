#!/usr/bin/env bash
# Purpose: verify the real Minecraft 1.21.1/NeoForge build using an official Gradle wrapper.
# Requires Java 21, curl, sha256sum and internet access to Gradle/Maven repositories.
set -euo pipefail
cd "$(dirname "$0")/.."

EXPECTED_WRAPPER_SHA256="423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9"
WRAPPER_PATH="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-9.2.1-wrapper.jar"
LOG_DIR="build/verification"
mkdir -p "$LOG_DIR" "gradle/wrapper"

if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | head -n 1 | grep -Eq 'version "21([."]|$)'; then
  echo 'ERROR: Java 21 is required. Install/select JDK 21 and set JAVA_HOME.' >&2
  exit 2
fi
if [[ ! -e "$WRAPPER_PATH" ]]; then
  echo 'Downloading official Gradle 9.2.1 wrapper JAR...'
  curl --fail --location --show-error --silent --retry 2     --output "$WRAPPER_PATH.pending" "$WRAPPER_URL"
  printf '%s  %s\n' "$EXPECTED_WRAPPER_SHA256" "$WRAPPER_PATH.pending" | sha256sum -c -
  mv "$WRAPPER_PATH.pending" "$WRAPPER_PATH"
else
  printf '%s  %s\n' "$EXPECTED_WRAPPER_SHA256" "$WRAPPER_PATH" | sha256sum -c -
fi
# gradle-wrapper.properties also pins the Gradle 9.2.1 binary ZIP checksum.
grep -Fq 'distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f' gradle/wrapper/gradle-wrapper.properties
chmod +x ./gradlew

echo 'STEP 1/3 Gradle startup...'
./gradlew --no-daemon --stacktrace help 2>&1 | tee "$LOG_DIR/01-gradle-help.log"
echo 'STEP 2/3 NeoForge datagen...'
./gradlew --no-daemon --stacktrace runData 2>&1 | tee "$LOG_DIR/02-runData.log"
echo 'STEP 3/3 NeoForge JAR build...'
./gradlew --no-daemon --stacktrace build 2>&1 | tee "$LOG_DIR/03-build.log"

echo 'REAL GRADLE BUILD PASSED. Minecraft client/server worldgen is still NOT verified.'
find build/libs -type f -name '*.jar' -maxdepth 1 -print
