#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
SRC=tools/fixtures/pass58/src
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
  "$SRC/net/minecraft/core/Direction.java" \
  "$SRC/net/minecraft/world/level/block/Block.java" \
  "$SRC/net/minecraft/world/level/block/Blocks.java" \
  "$SRC/com/dorohedoro/hole/worldgen/city/IndustrialAnnexStubs58.java" \
  "$SRC/com/dorohedoro/hole/worldgen/city/IndustrialAnnexFixture58.java" \
  "$CITY/Rect.java" "$CITY/LotType.java" "$CITY/CityHash.java" "$CITY/IndustrialAnnex58.java"
java -Xmx512m -cp "$OUT" com.dorohedoro.hole.worldgen.city.IndustrialAnnexFixture58
