# Pass 53 — The Hole's city density controls real block geometry

Minecraft Java 1.21.1 / NeoForge. **Source-only; UNCOMPILED against NeoForge.**

## Design direction from the user

The Hole should resemble the source manga/anime's oppressive, irregular, grimy lived-in urbanism, not a flat generic modern city. Crucially Minecraft's core biomes, cave exploration, ores, farming, hostile mobs, and wilderness must remain. A dense stretch of The Hole can occupy snow, desert, badlands, taiga, or rocky mountains, and the same biome can also have quieter settlements, ruin fields, and open land. Dense cities should have walkable alleys and furnished working shops/homes, not fake empty facades. Outer regions need not all be small outskirts; building density is seed-dependent and separate from climate.

## Concrete bug fixed

In Pass 52, `UrbanPattern52` determined lot *type* but **not parcel shape**. `Superblock.split` still selected dense/sparse geometry from `DenseCity25.isDenseDistrict(district)`, where the `OUTSKIRTS` district is always marked non-dense. This meant seeded DENSE neighborhoods in snow, badlands and deserts could inherit wide empty outskirts parcels despite the urban fabric selector calling them DENSE. Conversely derelict and OPEN_COUNTRY districts within a formerly urban district could retain extremely tight city alley grids.

## Versioned implementation (fresh world detail version 36)

- `UrbanParcels53.profile(fabric,district,depth)` now controls minimum/maximum parcel sizes, gap widths, and stop probability based on actual DENSE, INHABITED, DERELICT or OPEN_COUNTRY land use — **regardless of climate**. District labels still determine appropriate industrial building sizes and materials.
- DENSE fabric produces compact, walkable neighborhoods even if the district happens to be named OUTSKIRTS. Narrow pedestrian alley openings remain **at least three blocks wide**.
- INHABITED fabric allows larger functional plots; DERELICT retains large former-building sites; OPEN_COUNTRY reduces repeated street segmentation so terrain, agriculture, and gathering grounds can be spacious.
- `IrregularBlocks14.parcelsV36` retains stepped, connected streets but avoids ten-wide sliver parcels below the new 11-block minimum. The original irregular parcel routine is untouched for v0-v35 saves.
- `LotChooser` gives dense OUTSKIRTS fabric a functional mix: compact houses, shops with upper rooms, warehouses, properly sized factories, clinics, greenhouses, limited scrap yards and scarce empty plots. No new chest placement or lamps. A small plot receives a repair shop rather than an undersized factory.
- `CitySettings` codec + `normal.json` activate version **36** for **new** worlds. Historical worlds retaining version 0..35 keep their original morphology and the older region/shelter logic.
- Starters, municipal canals, tram corridors, sewer caves (30% selector), and underground system generation are not modified. No deferred Ash-Snow, environmental poisons, Cross-Eye camps or villager overhaul added.

## Actual offline checks performed

- 20/20 Pass53 source and compatibility assertions passed.
- Java 21 compiled **the actual source** of `Superblock`, `IrregularBlocks14`, `UrbanParcels53`, `UrbanPattern52`, `SettlementBands47`, `Rect`, `LotType`, `DenseCity25`, and `CityHash`, using purpose-built minimal stubs for unavailable Minecraft APIs and fixtures for CityPlan / LotChooser.
- A seeded fixture generated **43,200 superblocks / 863,899 parcels**, checked each parcel inside its reserved area, no parcel overlaps, minimum geometry, repeatability by seed, and distinct geometry profiles.
- Mean lot area by block-level fabric, fixture only: DENSE 247.4 blocks; INHABITED 334.0; DERELICT 482.5; OPEN_COUNTRY 1,011.3. Results are not a rendering or Minecraft world; generated areas use a transparent simplified street-line fixture and Java `Random` instead of Minecraft's true Xoroshiro source.
- Verified both small-patch repair-shop substitutions and appropriately large factory parcels, for all policy categories and 10,000 deterministic roll samples.
- Pass52 and previous Pass41–51 **offline** regression suites passed; historical QA scripts updated only to recognize a newer version maximum and newer fresh-world preset. Old algorithms were preserved behind explicit `<36` compatibility gates.
- Full Gradle, NeoForge compile, runData, world generation, player pathfinding, mob spawn testing, Minecraft client or server **not executed**. Build preflight still reports missing `gradle/wrapper/gradle-wrapper.jar`; use the project's official checksum-verified one-click Windows recovery in `BUILD_AND_TEST_WINDOWS.cmd` on a network-enabled Java 21 PC.

## Required in-game acceptance gate

1. Run `BUILD_AND_TEST_WINDOWS.cmd`, `gradlew.bat build`, runData, runClient and runServer / GameTests on Java 21; repair actual compile errors before releasing installable artifacts.
2. Create fresh v36 worlds with multiple seeds where each of snow, desert, mountain, badlands, dead taiga and CITY climate intersects DENSE neighborhoods, INHABITED blocks, DERELICT and OPEN_COUNTRY. Check F3 biome and roofline/street screenshots.
3. Examine whether opening roads still appear as an excessive grid in open countryside; the per-block ground road painter remains a limitation. Our pass reduces subdivision and restores larger plots but does not fully replace the main arterial topology.
4. Verify exterior doors lead into functional multiroom interiors, correct roof access, 3+-wide walking gaps, no floating floors on mountains, and no central street clogged by the extra closely-spaced buildings.
5. Validate cave mining, dead log sources, crop farms, animals, sewer exits, accessibility, sound, hostile nighttime spawning and sparse city lighting. Check v35 save migration on a **backup**; don't assume source version gates alone guarantee an existing-world save works.
6. Compare real in-game output to the user's five November 1 screenshots (street/canal/market/lighting/skyline). The linked screenshots belong to older versions, **not** this pass.

## Remaining visual priorities

The Hole must still become much more convincingly lived-in. Follow with actual screenshots of the user running a compiled mod and carefully improve architectural massing, uneven attached buildings, utility corridors, settlement transition, purposeful clutter, and road discontinuities. Avoid meaningless cosmetic passes and unfinished fake interiors.
