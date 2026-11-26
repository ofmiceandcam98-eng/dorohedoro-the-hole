#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
javac -d "$WORK" \
 tools/fixtures/pass63/net/minecraft/world/level/biome/Climate.java \
 tools/fixtures/pass63/net/minecraft/world/level/levelgen/DensityFunction.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/DeepDarkBuffer63.java \
 tools/fixtures/pass63/DeepDarkBufferFixture63.java
java -cp "$WORK" DeepDarkBufferFixture63
