#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
F=tools/fixtures/pass57/com/dorohedoro/hole/worldgen/city
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" "$CITY/Rect.java" "$CITY/LotType.java" "$CITY/CityHash.java" "$CITY/CityArchitecture.java" "$CITY/IndustrialBelts57.java" "$F/UrbanPattern52.java" "$F/IndustrialBeltsFixture57.java"
java -Xmx256m -cp "$OUT" com.dorohedoro.hole.worldgen.city.IndustrialBeltsFixture57
