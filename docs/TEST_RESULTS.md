# Test results

Only what actually ran. Environment: Windows 11, Ryzen 7 3700X, 32 GB RAM, Microsoft OpenJDK 21.0.12, NeoForge 21.1.265. Server runs used `-Xmx3G`.

## Build

| Command | Result | Date |
| --- | --- | --- |
| `gradlew build` (full decompile pipeline) | Passed after clearing a corrupt Gradle lock | 2026-10-30 |
| `gradlew compileJava` | Passed | 2026-10-31 |
| `gradlew runData` | Passed, 42 generated files at the time | 2026-10-31 |

Not run: `runClient`, `runGameTestServer`, plain interactive `runServer`. No GameTests exist.

## World validation

Command: `bash tools/qa-world.sh <seed> <radius> <order>`. Each run is a real dedicated server creating a brand-new world. It has 26 checks: generator class, spawn position, standing room, open sky, surface biomes, street reachability, enclosed lots enterable, at least 3 districts, 15 resource presence checks, flowing water near the surface, ore in the city, and replaceable rock in the city.

### 10-seed batch at radius 12 (625 chunks), code at commit `79f3198`

| Seed | Failures | ms per chunk |
| --- | --- | --- |
| 1 | 0 | 33.3 |
| 2 | 0 | 42.2 |
| 3 | 0 | 41.6 |
| 42 | 0 | 47.7 |
| 777 | 0 | 32.9 |
| 1234567 | 0 | 57.2 |
| -987654321 | **1** | 42.6 |
| 20261030 | 0 | 32.2 |
| 555 | **1** | 44.3 |
| 8675309 | 0 | 38.5 |

Result: **8 of 10 seeds passed all 26 checks. 2 of 10 failed one check.** Raw output is in `docs/evidence/qa-summary.txt`.

**Open failure, seeds -987654321 and 555:** `multiple_districts` failed with only `[MARKET, OUTSKIRTS]` in range. Outskirts covers too much of the area around spawn on these seeds. Likely fix: raise the 0.42 threshold in `District.ofCell`, or add a minimum distance from the origin. The fix is not applied.

On every one of the 10 seeds, all other checks passed, including spawn, street reachability, building entry, resources, fluid leaks and ore in the city.

ms per chunk is wall-clock time for blocking loads on the main thread. It is not tick time, and no vanilla baseline has been measured.

### Seed 12345 at radius 16 (1,089 chunks), commit `1f0f865`

26 of 26 checks passed. Streets 100% reachable, 400 of 400 enclosed lots enterable, districts: Residential, Market, Industrial, Waterfront.

### Determinism

Same seed, chunks requested in opposite orders, per-chunk block-state hashes compared with `tools/qa-compare.mjs`.

| Test | City layer (y≥64) | Whole chunk |
| --- | --- | --- |
| Seed 12345, radius 8, shell-only city, before the ore fix | 284 of 289 identical | |
| Same, after the fix | 289 of 289 identical | 31 of 289 identical |
| Full city, seed 1, radius 12 | **625 of 625 identical** | 81 of 625 identical |
| Full city, seed 42, radius 12 | **625 of 625 identical** | 75 of 625 identical |
| Full city, seed 8675309, radius 12 | **624 of 625 identical** | 19 of 625 identical |

**Open bug, seed 8675309:** chunk 1,2 differs at y≥64 between generation orders. The cause is not known and has not been investigated. Reproduce with:

```bash
QA_DUMP="1,2" bash tools/qa-world.sh 8675309 12 forward
QA_DUMP="1,2" bash tools/qa-world.sh 8675309 12 reverse
```

Then diff `dump-forward-1_2.txt` against `dump-reverse-1_2.txt` in the two `qa` folders, looking at y≥64 only. Chunk 1,2 is near spawn, so it may be a difference in what has ticked rather than in generation. That is a guess, not a finding.

Bug found by this test: vanilla ore features replaced `tuff` in road surfaces, putting `deepslate_coal_ore` in the streets. Fixed by removing every feature-replaceable block from city palettes. QA now enforces this.

Below the city, whole-chunk hashes differ. A block-level diff of one chunk showed vanilla decoration overlapping across chunk borders (moss, cave vines, clay in lush caves, and an `ore_gravel` and `ore_dirt` overlap), which depends on generation order. Vanilla worlds have the same property.

## Known not verified

Rendering, Create World screen, experimental warning, survival play, stairs beyond ground entry, chest loot in game, mob behaviour, vanilla structures, End access, multiplayer with real clients, tick performance, restart and save integrity.
