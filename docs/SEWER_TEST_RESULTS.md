# Latest offline test results — cavern traversal / room variation

- Pure Java size selection: 146,410 profiles across 10 seeds; 16.46% have broader chamber dimensions.
- Pure Java floor safety: 1,768,680 landings across 10 seeds, both directions and both terrain-relief modes; all adjacent drops <=1 and ends match the cavern floor.
- Sewers structural/source checks: 51/51 passed. Java parser: 48 source files, 0 syntax errors.
- New server-side test `sewer_cavern_walkable_descent` added to `HoleQa` but **not executed**.
- Actual NeoForge build, save/restart, Minecraft client and in-game mining/cave connections: **NOT TESTED** here.

# Sewer & Cave Frequency Update — October 31, 2026

**Status: source integrated, offline-tested, not Minecraft runtime verified.**

- `sewer_cave_frequency` is now persisted per world in the `CitySettings` generator codec.
- New world preset explicitly uses `30`, producing about **30% cave-leading** junctions, **18% sealed inspection branches**, and **52% ordinary sewers**.
- Legacy worlds missing the field decode to `9`, preserving the **exact old 1-in-11 branch choices** and preventing altered tunnel junctions in existing saves.
- Compiled and ran the pure Java selector regression across ten seeds / **146,410 intersections**: **29.89% cave**, **17.99% inspection**, **52.06% ordinary** (new) versus **9.07% cave**, **18.16% inspection**, **72.77% ordinary** (old). Every legacy choice matched the original calculation; deterministic selector, guaranteed demonstration cave at (0,0), and directions tested.
- Java 21 syntax parser: **47 active source files, 0 parsing errors**.
- Sewer static structure checks: **47/47 passed**.
- Cavern transition geometry is unchanged: intact sewer for 19 blocks, ruined masonry for 12, stone passage thereafter, cavern mouth approximately 59 blocks away. The deep noise router, aquifers, ore veins, and normal mining are unchanged.
- **Still NOT tested:** full NeoForge compilation, data generation, survival exploration, connected passage collision, chunk borders, and save/restart in Minecraft.

## Previous hybrid test results

# Sewer–Cave Hybrid Validation (2026-10-31)

**Snapshot:** source-code work, not a playable/verified NeoForge build.

- Java 21 parser: 47 Java source files, 0 syntax errors (Java 21 compiler parser).
- JSON/worldgen structures: 43/43 offline structural checks passed; 153 JSON files parsed with no errors.
- Natural cave density: restored original vanilla-style noise at depth less than Y=24; shallow area solid to protect sewers.
- Ore vein toggle: preserved.
- Aquifers: preserved in deep caves.
- Branch policy: increased cave approach selection to a 30% target, with most intersections still sewer-only; natural Minecraft caves preserved. Deterministic by seed and crossing.
- New runtime QA asserts: lined cave-branch approach and open cavern destination in starter region.
- Full Gradle, actual in-game craft/mine/explore, server restart & multiplayer: NOT VERIFIED in this environment.

## Executed offline regression

- `javac` + `java` compiled/ran the actual pure Java `CityHash` / `SewerBranchPolicy` code over 10 seeds and 146,410 intersections.
- Observed: **9.07% cave-leading**, **18.16% closed sewer inspection rooms**, **72.77% unmodified sewer intersections** in the legacy profile. The starter `(0,0)` is intentionally a cave-leading example.
- Original `hole.json` verified byte-for-byte identical to the previous Phase 4 source ZIP.
- `./gradlew --offline --no-daemon compileJava` attempted, but the wrapper failed to fetch Gradle 9.2.1 (`UnknownHostException: services.gradle.org`). Therefore **no actual NeoForge compilation has passed** here.
- No actual Minecraft terrain-generation, survival mining, caves, water interactions or chunk-boundary tests were executed in game.

## Re-run the updated branch-policy regression on your PC

From the repository root (Java 21), run sequentially:

```bash
mkdir -p build/sewer-policy-check
javac -d build/sewer-policy-check \
  src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/SewerBranchPolicy.java \
  tools/VerifySewerBranchFrequency.java
java -cp build/sewer-policy-check VerifySewerBranchFrequency
python tools/qa-sewers-static.py
./gradlew --no-daemon runData
./gradlew --no-daemon build
```

Then start a **new world**, physically inspect normal sewers, inspection bays, and the intact-sewer → ruined-masonry → mining-tunnel → deep-cavern routes. Benchmark generation time and re-run reverse-order chunk QA. The standalone test confirms only the selection algorithm, not in-game cave connectivity or performance.
