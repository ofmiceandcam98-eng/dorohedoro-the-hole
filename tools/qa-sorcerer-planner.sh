#!/usr/bin/env bash
# Offline test of the Sorcerers' World settlement planner (no Minecraft, no Gradle).
#   bash tools/qa-sorcerer-planner.sh [seeds_per_case]
# Compiles the planner's pure-Java classes with the fixture and runs it against synthetic terrain.
set -eu
out="build/qa-sorcerer-planner"
rm -rf "$out"
mkdir -p "$out"
src=src/main/java/com/dorohedoro/hole/worldgen/sorcerer
javac -d "$out" "$src/TownRandom.java" "$src/TownFrame.java" "$src/TownCatalog.java" "$src/RoadNet.java" \
  "$src/TownPlanner.java" tools/fixtures/sorcerer/TownPlannerFixture.java
java -cp "$out" TownPlannerFixture "${1:-40}"
