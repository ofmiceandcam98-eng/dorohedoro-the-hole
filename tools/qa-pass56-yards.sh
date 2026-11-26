#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
STUBS=tools/fixtures/pass56/src
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
 "$STUBS/net/minecraft/core/Direction.java" \
 "$STUBS/net/minecraft/world/level/block/Block.java" \
 "$STUBS/net/minecraft/world/level/block/Blocks.java" \
 "$STUBS/com/dorohedoro/hole/worldgen/city/LinkedYardStubs56.java" \
 "$STUBS/com/dorohedoro/hole/worldgen/city/LinkedServiceYardsFixture56.java" \
 "$CITY/Rect.java" "$CITY/LotType.java" "$CITY/CityHash.java" "$CITY/LinkedServiceYards56.java"
java -Xmx512m -cp "$OUT" com.dorohedoro.hole.worldgen.city.LinkedServiceYardsFixture56
