# Pass 66 — Walkable coastal land buffers and vanilla beach habitat

**Latest source only; not NeoForge-compiled, not installable, and not validated in Minecraft.** Fresh-world `world_detail_version: 48` and biome-source option `coastal_shelves: true`. Earlier generator versions 0–47 and old `coastal_shelves`-absent biome sources retain their existing algorithms.

## Gameplay goals

Pass65 introduced genuinely broad water basins, but ocean masking could delete half a factory and leave the remaining facade sitting abruptly above the water. Pass66 preserves a **16-block-wide, dry, uninhabited coast** between true-water columns and the original urban fabric. It is a walkable area to reach the water with a boat, collect vanilla sand/gravel, avoid exposed building interiors and prevent orphaned city chests or spawners. Beach columns 1–7 can use native `minecraft:beach` and `minecraft:snowy_beach` biomes, allowing vanilla beach ecology rules where appropriate. **Turtle nesting, spawns, and eggs are not validated until an actual client/server run.** No new blocks, items, techniques, damage, poison, water effects or outdoor lights.

## Source modifications

- `CoastalShelf66.java`: pure seeded climate policy. Returns -1 for distant land, 0 for real sea, and 1–16 for dry land next to ocean; computes the location independently of neighbor chunk order; fast starter-area exclusion. Height increases from Y=63 at water's edge to Y=66 at the inland reserve boundary. It uses the same `OceanRegions65.depthAt` as biome and bathymetry, not a second imaginary coastline.
- `HoleBiomeSource.java`: extend the existing bundled frontier record with vanilla `BEACH` and `SNOWY_BEACH` holders and an optional `coastal_shelves` flag (missing is `false`). New profile assigns beach biome only on dry land within the first seven blocks of the coast, using existing seeded temperature. The previously added ocean IDs and older saved-world biome availability are retained. Real NeoForge codec deserialization still needs testing.
- `CityPlan.java`: world-version-aware `coastalDistanceAt` method, returning -1 for v0–47.
- `Canvas.java`: cache 16×16 dry-coast reservation along with the existing ocean columns, and suppress all four ordinary city block and associated block-entity writers in the reserved strip above Y=28 before the beach painter runs. Raw absolute-Y seawater/sand painter continues to use vanilla structure-protection checks.
- `OceanBathymetry65.java`: preserve the exact old v47 ocean painting branch; on v48, also paint dry-coast-only chunks: solid shallow stone beneath, usable sand/gravel/coarse-dirt surfaces, and no ghost buildings above ground. The sea itself remains real water at Y=62, and caves below Y=57 are not newly filled.
- `CityPopulator.java`: skip city-spawn residents on reserved beach plots.
- `CitySettings.java` and `normal.json`: version 48 and default new-world opt-in. The old codec default `world_detail_version=0` is retained.

## Offline checks

- `bash tools/qa-pass66-coasts.sh`: compiles the **production** `CoastalShelf66`, `OceanRegions65`, `OceanBathymetry65`, `CityHash` Java sources against a minimal synthetic Minecraft terrain fixture; discovers a real wet coastline and verifies 255 dry coast cells, 119 sand-beach cells, correct Y-level clearance and solid bases, deterministic policy, old-version no-op, water and seabed integrity. These are stub-geometry checks, not native Minecraft simulation.
- `python3 tools/qa-pass66-source.py`: 37/37 codec, versioning, coastal worldgen, biome identity, urban write masking, spawner/chest suppression and vanilla-progression source checks passed.
- Previous ocean, city, sewer, Deep Dark and vanilla-structure offline QA passed from `qa-pass66-all.sh` through Pass47 source tests; long combined suite timed out during Pass47. Running `bash tools/qa-pass47-all.sh` separately passed the complete Pass41–47 tail. No failed assertions. See `/mnt/data/Pass66_Full_Offline_QA.log` and `/mnt/data/Pass66_Historical_47_to41.log` in the working environment for recorded evidence. These logs are not part of the release ZIP (the checked scripts are included).

## Wednesday, Nov 4 — required NeoForge client and survival verification

1. Restore official verified Gradle wrapper JAR; run Java 21 `gradlew.bat build`, `runData`, `runClient`, `runServer` and fix real biome holders, codec serialization/registry errors and crashes.
2. Create several fresh normal v48 worlds, test starting location on land, compare the same seeds and chunk transitions. Verify no structures are cut in half by beach reservation; verify cross-chunk dry shorelines and actual Minecraft Y=63..66 grading. Test cliff/high-relief shore cases, negative world coordinates, and performance near ocean chunks.
3. Check F3 actual vanilla `minecraft:beach` and `minecraft:snowy_beach` on sand and true ocean IDs over water. Confirm that beach vegetation/mob spawning/animal availability are reasonable; **turtle spawning, nesting, eggs and sand survival must be proven in a living game world.** Do not claim them on the strength of tags.
4. `/locate structure minecraft:ocean_monument`, `minecraft:shipwreck`, `minecraft:ocean_ruin_cold` then inspect genuine naturally placed structures, loot and guardians, as well as real ocean fish, drowned, underwater caves and monuments; inspect any encounter with custom sewers.
5. Verify vanilla strongholds/End portals, Nether, actual Ancient Cities with Wardens, mineshafts/trial chambers, and Ender Dragon defeat in survival with no techniques.
6. Inspect urban coastline for unexplained raised walls, too-clean beaches (Dorohedoro atmosphere should read), offshore floating city blocks, lighthouse-like excess lamps, beach-spawn villagers, missing water sources or phantom containers. Improve only from actual screenshots/playtests.

## Known remaining gaps

- These beaches do **not** implement warm oceans/coral reefs, full turtle behavior, marine monument generation proofs or underwater structure spacing.
- The 16-block reservation can meet high inland terrain abruptly; it does not replace genuine graded cliff/coastal infrastructure. Do not invent piers/marina facades until real screenshots justify them.
- The shallow shoreline can still meet naturally generated vanilla structures and get protected by Pass64; the final result must be inspected in the client.
- The coastal nearest-water policy uses finite directional samples; inspect curved coves and corners for missed one-block holes or inconsistent biome edges.
- Chunk generation cost from ocean climate sampling and repeated near-sea distance queries needs in-game profiling. Offline fixture timings are not a production performance benchmark.
- No live NeoForge build or worldgen test has occurred. Full vanilla Minecraft parity remains **unproven**.
