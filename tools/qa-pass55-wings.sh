#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
STUBS=tools/fixtures/pass55/src
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
 "$STUBS/net/minecraft/core/Direction.java" \
 "$STUBS/net/minecraft/world/level/block/Block.java" \
 "$STUBS/net/minecraft/world/level/block/Blocks.java" \
 "$STUBS/com/dorohedoro/hole/worldgen/city/AttachedWingStubs55.java" \
 "$STUBS/com/dorohedoro/hole/worldgen/city/AttachedWingsFixture55.java" \
 "$CITY/Rect.java" "$CITY/LotType.java" "$CITY/CityHash.java" "$CITY/AttachedWings55.java"
java -Xmx512m -cp "$OUT" com.dorohedoro.hole.worldgen.city.AttachedWingsFixture55
