#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
javac -d "$OUT" "$CITY/CityHash.java" "$CITY/SewerBranchPolicy.java" "$CITY/SewerHazards61.java" tools/fixtures/pass61/SewerHazardsFixture61.java
java -Xmx384m -cp "$OUT" com.dorohedoro.hole.worldgen.city.SewerHazardsFixture61
