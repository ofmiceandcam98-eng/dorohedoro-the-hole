# Pass 52 — A Hole city that can exist in any generated climate

**Minecraft Java 1.21.1 / NeoForge — source-only and UNCOMPILED.**

## User-approved art and gameplay intent

The Hole is not restricted to the preset's fixed urban-core biome, nor are snow, taiga, desert, badlands, or mountain areas automatically empty outskirts. Minecraft's survival fundamentals matter: seed variety, biome-driven terrain, exploration, mining, caves, ores, crafting, real furnishings, manageable hostile spawning, and natural progression.

The visual standard remains Q Hayashida's and MAPPA's grimy, irregular, lived-in Hole. No copied builds, generic city with color-swapped facades, fake decorative rooms, excessive nighttime lights, or exposed chest spam. Biome and settlement density must vary independently: a desert can have dense blocks, ruins, and open wild country; a snowy part of The Hole can have a dense neighborhood. The same world can transition between many climates and density levels.

## Root issue found and changed

Previous fresh worlds forced every block within 1,536 blocks of the world origin to the CITY region in `HoleRegions42.at(...)`. `LotChooser` then automatically picked sparse outskirts policies for non-CITY climates. This prevented fully urban snowy/desert/badlands starts regardless of the user's world seed.

**New version 35 only:**

- `HoleRegions42.atAnywhere(climate,x,z)` samples seed-dependent Minecraft climate anywhere, including at 0,0. `regionInCellAnywhere` adds temperature/humidity mountain options so the center is not locked out of the MOUNTAIN_RIM climate on every seed. The old `at` is unchanged.
- `HoleBiomeSource` has a new serializable optional `climate_across_city` field, default **false**. The new world preset explicitly sets **true**. The v35 `CityPlan.regionAt()` uses the same `atAnywhere` as the biome source; old saves retain the older mapping.
- `UrbanPattern52.at(seed,x,z)` independently chooses DENSE, INHABITED, DERELICT, or OPEN_COUNTRY. The deliberately irregular dense hub has a variable ~400–600-block radius. Larger interpolated 704-block seed fields create outlying dense pockets, surviving neighborhoods, ruins, and real open terrain. These are **land-use choices**, not new biomes or new fake buildings.
- `LotChooser` uses the independent fabric before applying climate-specific **functional** occupied/abandoned lot mixes. DENSE neighborhoods use existing district-aware real interiors, shops, apartments, and industry regardless of climate.
- `Canvas.isHoleCity` and `Backstreets43` follow urban fabric, so coherent occupied buildings and street repairs work inside a snowy/industrial/desert city too.
- `RegionalSurface42` and `CityPainter` preserve usable climate-appropriate ground in open areas and suppress unnecessary lights at night regardless of climate. Existing municipal rail, quays, rivers, sewer shafts and furnished starter areas are retained.
- `HoleRegions42.terrainBlendAnywhere` grades only genuine climate borders without false ridges at every 512-block cell. Terrain gets modest relief inside the inhabited hub and more outside. `CityTerrain40` still protects the immediate spawn and floodplain.

The city hub remains physically near **world origin** in this pass. The world **biome of the city** is no longer always CITY, and the prior regional spawn selector may still start players in different regions. This pass does **not** implement a completely location-shifted city footprint or introduce vanilla overworld biome IDs: it uses the mod's original Hole-themed custom biome palette and the world's climate noise.

## Validation actually performed

- 23/23 Pass52 source-integration checks passed, including codec/preset alignment, shared climate mapping, legacy save gates, urbanization wiring, dark outskirts, starters and 30% sewer-to-cave links.
- Java 21 compiled the REAL `HoleRegions42`, `SettlementBands47`, `UrbanPattern52`, `CityHash`, and `LotType` against small, transparent Minecraft climate data-type stubs.
- 3,267,000 seed/coordinate samples were checked; all **six** core climate categories appeared (under simulated noise) and all **four** urban fabric bands appeared. At least 366,557 non-CITY climate samples were densely urban; over 901,171 open-country samples appeared in other climates.
- Original `HoleRegions42.at` still returns CITY within the old 1,536-block core in test fixtures. Region/lot selection is deterministic and does not depend on chunk exploration order.
- Pass 41–51 offline regression chains passed with old static QA scripts updated solely to recognize the new codec/preset version. The Pass 43 mini-Java fixture was taught a stub reference for the new class when compiling the historical test under version 27.
- Build preflight passed its static checks but the official Gradle wrapper JAR remains absent. No official Gradle/NeoForge compilation, real Minecraft noise sampler, Minecraft client, real mobs, FPS, saved-world migration, or server tests occurred.

## Required real-world acceptance test (not completed)

1. Restore the official Gradle wrapper using `BUILD_AND_TEST_WINDOWS.cmd` on a network-enabled Java 21 machine, then run `build`, `runData`, `runClient`, `runServer` and GameTests as available.
2. Create fresh v35 worlds using multiple distinct seeds. Check the actual F3 biomes at (0,0), elsewhere downtown, every regional start, and the transition between climates. Confirm six climates eventually appear across tested seeds; this is not guaranteed in a fixed small batch.
3. Verify **city within desert, snowy, taiga, badlands and mountain climate**: each needs real street layouts, appropriately furnished mixed-use buildings, passable doors, neighborhood street repairs, and usable survival activities. A biome alone must not dictate building density.
4. Verify DENSE / INHABITED / DERELICT / OPEN_COUNTRY **within a single climate**, check density transitions, chest exposure and no endless paving/rails in wilderness. In particular evaluate the abruptness near the central hub and the natural feel of the outer roads.
5. Check regional spawn shelters, south doors, paths and roads with actual colliders, nighttime hostile spawning, sparse lantern use, dead-tree logs, exposed ores, mines, cave links, farmland, crop light, portals, underwater crossings and deep world generation.
6. Compare actual screenshots to the original Nov 1 skyline/street/canal/market/lighting images; do **not** present synthetic concept art as implemented Minecraft output.
7. Test a **backup** of a v34 world before calling save compatibility confirmed. New `climate_across_city` defaults to false for old stored biome-source data; old worldDetailVersion keeps old plan code.

## Remaining priority

Real build first, then source-guided structural quality: dense yet non-grid neighborhoods, crooked alleys and natural infrastructure, layerable roofs, furnished homes and workshops, and visually convincing transitions between city neighborhoods and wild Minecraft terrain. Deferred environmental mechanics and villager/faction overhaul remain postponed until the user approves the world visual quality.
