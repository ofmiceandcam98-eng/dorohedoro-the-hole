#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mapfile -t stubs < <(find tools/fixtures/pass64/net -type f -name '*.java')
javac -d "$work" "${stubs[@]}" \
 src/main/java/com/dorohedoro/hole/worldgen/city/VanillaStructureBuffer64.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/VanillaStructureLookup64.java \
 tools/fixtures/pass64/VanillaStructureFixture64.java
java -cp "$work" VanillaStructureFixture64
