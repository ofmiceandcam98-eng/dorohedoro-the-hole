# Pass 51 — Regional starter walkability, correct entrance route, and start lookup caching

**Date:** 2026-11-02  
**Baseline:** Pass 50 consolidated source archive.  
**Target:** Minecraft Java 1.21.1 / NeoForge.  
**Build status:** **NOT COMPILED** against NeoForge; no fresh in-game screenshots. Existing source-only tests passed.

## Why

Pass 47 gave every new seed a chance to start outside central The Hole. Pass 50 tried to reject hillside starts with a two-block access jump but checked a four-block **east-west service alley** at `z`, whereas the existing `STARTER_SHELTER` has its ground-floor entrance on the **south frontage** (`z + 13`; grade sampled at `z + 14`). A player heading toward that entrance must traverse the open southern sidewalk to `x + 27`, including columns around `x + 18..26` that Pass 50 did not sample. A local two-block obstruction on those columns could pass the old test. This is a source-derived gap, not a user-observed runtime failure.

## Work

1. New `SpawnRoute51.accessible` pure Java predicate walks the actual south sidewalk from the landing's south frontage to the shelter's front step, checking **every consecutive block** for an elevation step of at most one. It also checks the adjacent west/east public-lane approaches and the short outgoing southern lanes at both doors.
2. New-world `world_detail_version: 34` runs this *in addition* to the original Pass 50 check. Pre-34 saved values retain their previous algorithm and chosen starts. The codec's optional missing-value fallback stays zero. Never manually change an old world's generator version: its existing terrain/spawn can diverge.
3. `CityPlan.startingSite` memoization now uses a synchronized double check. During parallel chunk generation, only one thread performs the potentially expensive, bounded climate/grade candidate scan. The selected site is stored once; the deterministic selection algorithm remains unchanged.
4. The prescribed new-world preset and QA version assertions move to 34. **No changes** to district lot odds, buildings, techniques, maps, biome definitions, villages, loot, sewer-to-cave ratio, no-sky rendering, nighttime mob lighting, or the prior art assets.

## Test evidence

Run `bash tools/qa-pass51-all.sh` at the project root. In this environment:

- Pass 51 source contracts **17/17 passed** (includes valid JSON and saved-world gates).
- Pass 51 Java: **4,941** seeded coordinate route fixtures, including 4,941 reproduced false positives in the Pass 50 route when a previously unsampled entrance segment is blocked. Verified 24,000 seed selections preserve all six possible starting regions.
- Passes 41–50 existing offline regression suites passed.
- ZIP files must pass `unzip -t` or `ZipFile.testzip` before delivery.

**These are offline checks and Java fixtures.** They do not validate real `Climate.Sampler` inputs, Minecraft collisions, generated stair access, walkability under entity navigation, chunk-order performance, or NeoForge compilation. The older QA outputs still describe historical passes and have no bearing on in-game success.

## Main blockers / next actions on a development PC

- This source archive has `gradle-wrapper.properties` but no `gradle-wrapper.jar`; network access to the official Gradle distribution was unavailable in this environment. Windows: use `BUILD_AND_TEST_WINDOWS.cmd` to execute the existing checksum-verified wrapper recovery and run `runData`, `build`, `runServer`, `runClient` with Java 21; investigate any compiler errors, especially our new `SpawnRoute51` integration.
- Create six **fresh** worlds where the selected starts correspond to city, snowfields, taiga, dust basin, badlands, and mountain rim. Walk from the middle of the plaza along the SOUTH frontage to the furnished shelter door. Look for rails, water, raised foundations, two-block curbs, or missing stairs that an elevation-only algorithm cannot detect.
- On the same seeds, compare actual screenshots with the owner's November 1 reference images; validate industrial density, rooms, canals, ugly smog, low lighting outside safe areas, and nearby survival resources. Compare 16x16 chunk borders and walkability on negative coordinates.
- Save/reload; inspect the same spawn after restarting the dedicated server; test a pre-v34 world without manually changing its generator settings.

### Design reminder

The Hole is not a uniform city grid, nor should every biome be a completely empty wilderness. Preserve populated and ruined pockets, industrial settlements, sparse lifeless vegetation with harvestable wood, accessible mining and sewers, and genuinely different physical terrain. Do **not** implement deferred ash-snow/sludge-ice/environmental poisoning, camps or villager overhaul until the current world is visually verified.
