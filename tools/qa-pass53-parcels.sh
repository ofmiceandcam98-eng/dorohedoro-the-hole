#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
FIX=tools/fixtures/pass53/src
CITY=src/main/java/com/dorohedoro/hole/worldgen/city
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
  "$FIX/net/minecraft/util/RandomSource.java" \
  "$FIX/net/minecraft/world/level/levelgen/XoroshiroRandomSource.java" \
  "$FIX/com/dorohedoro/hole/worldgen/city/"*.java \
  "$CITY/CityHash.java" "$CITY/Rect.java" "$CITY/LotType.java" \
  "$CITY/DenseCity25.java" "$CITY/SettlementBands47.java" "$CITY/UrbanPattern52.java" \
  "$CITY/IrregularBlocks14.java" "$CITY/UrbanParcels53.java" "$CITY/CrookedLanes54.java" "$CITY/Superblock.java"
java -Xmx512m -cp "$OUT" com.dorohedoro.hole.worldgen.city.ParcelFixture53
