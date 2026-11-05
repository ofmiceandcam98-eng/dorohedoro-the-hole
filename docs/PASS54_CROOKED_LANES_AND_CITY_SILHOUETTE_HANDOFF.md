# Pass 54 — Crooked local lanes and nonuniform street frontage

**Status: Minecraft Java 1.21.1 / NeoForge SOURCE ONLY. Not yet compiled or visually verified in Minecraft.**

## Why this was necessary

The user's November 1 in-game screenshots show long uninterrupted axial streets, repetitive block alignment, a very wide smooth public surface and building silhouettes still too regular to feel like the industrial, grimy Hole of Dorohedoro. Earlier passes introduced density-based lots and old *linearly* offset alleys, but neighboring buildings still lined up around long straight internal passages. The actual manga/anime environment is the reference point, not generic dystopian decoration. The user also wants normal Minecraft mining, biomes, wilderness, and survival intact.

## Implementation: fresh world `world_detail_version` 37

- `CrookedLanes54` is a pure, seed-coordinate-based parcel generator for selected **55-by-55-plus** superblocks. It chooses an orientation and **three lateral anchors** for each major local passage, then builds an intentionally non-linear stepped dogleg (sometimes an S-bend) through varying-length frontage strips. The old module had only a linear start/end interpolation.
- Connecting perpendicular alleys vary between **3 and 5 blocks** in dense city, and **4 and 6 blocks** in other urban fabric. The longitudinal corridor remains at least **5 blocks** wide (usually 7 in less dense areas). The 11-block minimum rectangular building footprint is preserved. Cross alleys open to both sides; corridors connect opposite arterial margins without sealing entrances.
- Crooked-lane selection follows `UrbanPattern52` independent of biome: 15/16 of eligible DENSE superblocks, 12/16 INHABITED, 10/16 DERELICT and 4/16 OPEN_COUNTRY, using a reproducible hash. Remaining eligible superblocks receive the existing simple parcel splitter, preventing universal noise and preserving a real mixture of street ages.
- The new paragraph geometry is used only for version 37+: `Superblock` routes version 0..35 through the unchanged original `IrregularBlocks14.parcels`, version 36 through its existing `parcelsV36`, and version 37 through the new selector/shape. The old historical `avenueOffset` and primary street painter are not modified.
- `CitySettings` and `normal.json` enable version 37 on **new worlds**. Existing `level.dat` generator codecs retain the exact saved world-detail version. **Migration to an existing world is not offered**, as changing generator versions in old worlds can cause visible seams.
- The fixed downtown starting quarter, selected regional start shelters, protected landmarks, major tram corridors, canal-river reservations, diagonal boulevard clearance, sewer caves, town workstations, and interiors still use their existing generators. No new block/item, chest, torch, lamp, lore power, mob, villager or environmental damage system was added.

## Verified offline (NOT Minecraft)

- **21/21** Pass54 static source and saved-version contract assertions passed.
- Java 21 compiled the actual `CrookedLanes54`, `Superblock`, `UrbanParcels53`, `IrregularBlocks14`, and supporting source against clearly marked minimal Minecraft type fixtures. Sampled **10,320** local passage layouts, with **5,869** showing a measurable non-linear dogleg (4+ blocks deviating from an end-to-end chord).
- **2,611** layout samples passed graph-search traversal with a **3x3 free-space pedestrian footprint** from one superblock arterial edge to the opposite edge; no 1-block slit is counted as a usable road. The fixture samples both horizontal and vertical orientations.
- **1,024** integrated `Superblock.build` seeds checked for deterministic results, in-bounds non-overlapping lots, and reproducibility of the historical version-36 algorithm. **973** of those selected cases yielded changed version-37 layouts.
- All **Pass41–53** offline regression suites invoked through `tools/qa-pass53-all.sh` passed, after raising their allowed world-detail codec version from 36 to 37 and updating the previous fresh-world assertion to `>=36`. Pass53's Java geometry fixture now compiles the new source dependency, but its old scenario still runs using version 36.
- The historical scripts' version-check changes do NOT mean they revalidated anything in a NeoForge runtime. Fixture classes are not real Minecraft source dependencies.
- ZIPs were CRC-tested. Actual Gradle wrapper JAR is absent at `gradle/wrapper/gradle-wrapper.jar`, so `gradlew build`, `runData`, `runClient`, `runServer` and in-game performance/visual checks remain unverified.

## Limitations and next priorities

The local passage layout is more crooked, but **primary railway and vehicle avenues remain relatively straight**, and the city is not yet visually approved. The source-only fixture does not know about vanilla entity navigation, real mountain terrain height, chunk-light updates, mob spawn placement or final roof geometry. The actual block painting already follows parcel exclusion and is still part of the user's screenshots review plan.

### Game acceptance checklist (must run after official wrapper restoration)

1. Use Java 21 on a network-enabled machine to run `BUILD_AND_TEST_WINDOWS.cmd` and the repository's checksum-verified `restore-official-gradle-wrapper.ps1` if required; then build and runData. Do not distribute a working JAR until compilation succeeds.
2. New v37 worlds in six climates, screenshot at least four different blocks per climate. Compare density, building shapes, frontage alignment, walls, alleys and main avenues against `/docs/evidence/` and user's November 1 actual images; do NOT pass off old screenshots as new output.
3. Enter every reachable shop, home and repair workshop. Doors, stairs, doors behind corners, furniture, block entities and accessible roof routes must work. Confirm local laneways actually connect to adjacent arterial walking paths.
4. Verify highland/night/spawn accessibility, 30% sewer cave passages, caves and ores, useful dead-tree logs, vanilla survival crops and food, monster spawning at night and limited streetlamp density.
5. Test legacy v36 world open on a copy and compare chunk hashes across previous/unseen chunk generation (no generator version override). Check v37 chunks in different generation orders for identical built structures.
6. Next pass: actual interactive rooftop and industrial annex improvements **only if** they keep existing room layouts and maintenance access; continue work on broad arterial spacing after full in-game inspection rather than narrowing tram/canal transport at random.

## Run offline tests

```
bash tools/qa-pass54-all.sh
```

**No deferred Ash-Snow, Sludge-Ice, poison, Cross-Eye camps or villager faction overhaul work is included.**
