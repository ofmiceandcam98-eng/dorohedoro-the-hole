# Pass 42 — The Hole's recognizable city, frontier biomes and highlands

**2026-11-02 · SOURCE-ONLY CHECKPOINT · UNCOMPILED · NOT READY TO INSTALL OR PUBLISH**

This file is the authoritative Pass 42 source handoff. It supersedes old “latest Pass 32” / Pass 41 status summaries in the historical root HANDOFF. Source is based on `Dorohedoro_Pass41_CONSOLIDATED_Source_UNCOMPILED.zip`; no working Minecraft jar was produced. Static checks are not proof of gameplay or visual quality.

## Artistic direction and acceptance standard

The objective is not a vanilla Minecraft city wearing a Dorohedoro texture palette. Every district must feel like a **lived-in, polluted, improvised industrial city**: compressed irregular tenements, repairs over repairs, signage, roads and connected tram lines, hanging pipes/overhead infrastructure where justified, canals and sewer access, clinics and shops with usable workstations, cramped homes with purpose-specific rooms, worn but traversable public space, and fragments of wilderness reclaimed from dangerous industrial margins. Buildings must be walkable, rooms actually usable and furnished, street loot storage not scattered openly, and deliberately dark side streets must support hostile night spawns. The old mixed vanilla lamps / Cavern Lantern / torches remain available rather than uniformly deployed. Surface sky remains obscured only in the Hole, preserving underlying light/weather behavior. No healthy taiga trees; surviving wood must be scarce, dead, stunted, or polluted, yet mineable for survival.

**User-requested biome identity**, interpreted as polluted geographical zones within one connected Hole rather than pristine vanilla wilderness:

| Regional biome ID | Visual and structural intent | Survival purpose / distinguishing activity |
|---|---|---|
| `dorohedoro:hole_snowfields` | grimy snow, grey-white slag, thin abandoned housing and warehouses | harvest snow; navigate storm-battered exposed streets, no Ash-Snow mechanic yet |
| `dorohedoro:hole_dead_taiga` | sickly podzol, leafless timber, damaged nurseries, marginal greenhouses, sparse inhabited blocks | recover actual spruce logs and food cultivation sites |
| `dorohedoro:hole_dust_basin` | sand-choked condemned housing, scrap heaps and scattered depots | collect vanilla sand, salvage equipment and explore salvage streets |
| `dorohedoro:hole_iron_badlands` | red dust, terracotta/rust colors, yards and heavy factories | collect red sand/terracotta and find industrial workstations |
| `dorohedoro:hole_mountain_rim` | elevated stone escarpments, mining-facing warehouses, defended service posts | explore highlands for quarried cobble, access roads and underground mining |

The inhabited central core (a Chebyshev radius of 1,536 blocks) is explicitly protected from region substitution; all five frontiers appear further out in broad roughly 512-block climate provinces. Each biome has its own F3 biome key, air palette and baseline spawn list in Java datagen. Border warps and smooth relief masks avoid regular square-cut cliff walls, but **real in-game chunk-edge and route QA is still required**.

## Exact changes in this source

1. `worldgen/city/HoleRegions42.java`: deterministic per-coordinate, climate-aware province selection and transition weights, including mountain outcrops; no generation-order random state.
2. `worldgen/HoleBiomes.java`, `worldgen/HoleBiomeSource.java`, `datagen/HoleBiomeData.java`: five registered biome keys, bundled registry Holder codec, F3-recognizable biomes, atmospheric colors, weather temperatures and entity spawns.
3. `worldgen/city/CityTerrain40.java`: v26 extra highland relief, badlands and snow uplifts; existing v0-v25 grading intact. Height caps fit existing byte-encoded height columns.
4. `worldgen/city/RegionalSurface42.java`: regional side street paving, open-lot material accents and rare mineable dead timber. It does **not** add any lamps or open chests.
5. `worldgen/city/LotChooser.java`: region-specific proportions of occupied tenements, factories, salvage, nurseries, warehouses and vacant parcels; unlike changing just a color, this changes settlement structure. Major existing landmarks and central neighborhoods are preserved.
6. `worldgen/city/CityPainter.java`: new-profile raised-ground fill before excavating sewer galleries, and calls regional surface and tram stop layers; pre-v26 generation order remains unchanged.
7. `worldgen/city/HoleTransit42.java`: sparse sheltered benches and signage next to existing tram corridors, keeping tracks and crossings clear by intent; stop placement and clearances not yet runtime verified.
8. `worldgen/city/CitySettings.java`, `worldgen/city/CityPlan.java`, `src/main/resources/data/minecraft/worldgen/world_preset/normal.json`: fresh-world profile `world_detail_version = 26`, `frontier_biomes = true`, while older serialized worlds default to their old flags and versions.
9. `datagen/ModDataGen.java`, `registry/ModEntities.java`, generated biome JSON and vanilla biome tags: resource routing and biome-specific spawn habitats. **Generated biome JSON is OFFLINE BOOTSTRAP, not authoritative output from runData.**
10. `tools/qa-pass42-source.py` and `tools/qa-pass42-region.sh`: offline JSON/source checks, Java-compiled deterministic province algorithm fixture. Pass 41 / Phase 3 checks retained.

## What we have actually checked, and what we have not

- PASS: offline source checks (26/26); climate region algorithm compiled and exercised with Java 21 + temporary Minecraft API test doubles; six region categories seen and deterministic on fixed sampler inputs; existing Pass 41 source/asset checks, Phase 3 static tests; JSON parsed.
- PASS: older profile default flags are unchanged in source. This **does not** guarantee backwards-compatible chunk generation without real save/reload testing.
- FAIL/BLOCKED: `./gradlew --offline help` cannot start because the source archive does not contain `gradle/wrapper/gradle-wrapper.jar` (not a genuine NeoForge compilation result).
- NOT RUN: `runData`, NeoForge `build`, `runClient`, dedicated `runServer`, GameTests, `qa-world.sh` on loaded terrain, visual screenshot comparisons, actual fall-damage / access routes / rails / sewers / mob-spawning / performance checks.
- NOT PROVEN: edge smoothness, actual mountains (visually), snowfall and taiga density in an in-game seed, atmospheric smog appearance, flooded surface canals, shelter headroom, rail gradient, dangerous night-spawn balance, and functional path through every interior room.
- NOT IMPLEMENTED BY PASS 42: ash/snow status mechanics, toxicity / environmental poisoning, sorcerer faction or villager overhaul, new unique biome mobs, all-new geometry for the streets, and full terrain-noise mountains outside the paved city foundation; those are later work after this world looks right and runs.

## Release blockers and test order on the development PC

1. Restore the official Gradle wrapper from the configured Gradle version / correct original project. Inspect the wrapper source and obtain the jar from trusted Gradle tooling. Do not substitute a random jar from an untrusted download.
2. Use JDK 21 from this project root: `./gradlew --no-daemon runData` (authoritative output replacing bootstrap JSON). Then `./gradlew --no-daemon build`. Fix every compiler and registry error; do not claim success from a source/static QA pass.
3. Launch `./gradlew --no-daemon runClient` and create a **new** default Hole world. Validate the F3 biome name across all five regions at multiple seeds and confirm no exposed normal vanilla sky, with day/night still functioning.
4. In game, inspect all regional transitions, steep road grades, rail ascending blocks, cliffs, rivers, slopes, building entrance paths, highland sewers (especially deep excavation under 100-block escarpments), and chunk seams; screenshot and patch all broken access.
5. Play survival: acquire dead logs, snow, sand, red sand, cobble and normal vanilla ores; find usable and furnished homes, shops, workstations, food, beds, storage tucked indoors. Night should be dangerous without turning the entire city into a floodlit safe zone. Test custom mobs and magic as well.
6. Run `./gradlew --no-daemon runServer`, `bash tools/qa-world.sh 12345 12 forward`, repeat reverse, seed 42 / 8675309 / 20261102, and compare available deterministic report hashes; complete GameTests and final post-fix build.
7. Only after those checks may an installation ZIP/jar or CurseForge release be described as working.

## Intentional next improvements after an actual screenshot review

The first region pass improves material ecology, relief and occupied-lot mix; it **does not yet establish an anime-faithful environment**. Compare in-game shots with actual Dorohedoro establishing backgrounds. Prioritize block-scale occluded industrial skyline, asymmetrical tenement exteriors, connected utility pipes/cables, distinct clinics and stores, hidden infrastructure, meaningful building interiors and street vertical layering. Make each major structure and new visual asset justify its existence in the world, not decorative clutter. Keep existing 64 technique icons and magic systems untouched until world generation passes its quality gate.
