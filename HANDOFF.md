# Dorohedoro: The Hole — handoff

Handoff written 2026-10-31 by Claude Code for the next developer or AI assistant. Read this section fully, then use the rest of the file as reference. Everything after the brief is the complete source of the project at the time of writing.

**Do not trust a claim about this project unless it appears under "Verified" below or in `docs/TEST_RESULTS.md`.**

## What this project is

A Minecraft Java total-conversion mod. The Overworld becomes the Hole, the endless polluted city from Dorohedoro by Q Hayashida, with the Sorcerers' World and Hell as further infinite dimensions. It is a **survival sandbox, not a story mod**. There are no mandatory quests. All of vanilla survival must keep working.

- Minecraft 1.21.1, NeoForge 21.1.265, ModDevGradle 2.0.148, Gradle 9.2.1, Java 21, Parchment 2024.11.17. Mod ID `dorohedoro`, base package `com.dorohedoro.hole`.
- Git branch `gate-0-1-hole-world`, branched from `main`. `main` holds only the unmodified template import.
- No external mod dependencies yet. GeckoLib 4.9.3 is the planned choice when the first animated entity needs it.
- The owner's account is on GitHub, but **nothing has been pushed**. The repo is local only.

## Spec documents, in priority order

1. `docs/spec/SANDBOX_DIRECTIVE.md`: current direction. Wins any conflict.
2. `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md`: queued, not started.
3. `docs/spec/FTB_QUESTS_DIRECTIVE.md`: **deferred. The owner said not to start it until asked.**
4. `docs/spec/CLAUDE_MASTER_PROMPT.md`: the original spec. Still applies where not contradicted. Its quest campaign is dropped.
5. `docs/spec/*.csv`: seed data, explicitly not proof that anything exists.

The first three are digests of longer prompts from the owner. They keep every concrete requirement, number and recipe, but ask the owner for the original text if wording matters.

## Status

### Verified, with evidence

Evidence is in `docs/TEST_RESULTS.md` and `docs/evidence/`. All of this ran on a real headless dedicated server.

| Claim | Result |
| --- | --- |
| Project builds | `gradlew build`, `compileJava` and `runData` pass |
| 10-seed world validation | **8 of 10 seeds passed all 26 checks. 2 failed one check, the district variety check** |
| New world's `minecraft:overworld` uses `HoleChunkGenerator` | Passed on every seed |
| Spawn on the plaza at 0, 65, 0, standable, open sky | Passed on every seed |
| No vanilla surface biome near spawn | Passed on all 10 seeds |
| Streets all reachable on foot from spawn | Passed on all 10 seeds |
| Enclosed buildings enterable on foot | Passed on all 10 seeds |
| All vanilla ores plus water, lava, clay, sand, gravel, dirt and sugar cane in a generated region | Passed |
| No flowing water from y=46 to y=66 | Passed |
| No ore or replaceable rock at street level or above | Passed |
| City layer identical when chunks generate in opposite order | **Seeds 1 and 42: 625 of 625 chunks. Seed 8675309: 624 of 625, one open bug** |
| Generation speed | About 33 to 57 ms per chunk on this machine, by wall clock on a blocking load. Not a tick-time measurement |

### Implemented but NOT verified

Do not describe these as working yet.

- **`runClient` has never been launched.** No one has seen this world rendered. The Create World screen, the "The Hole" label, textures, lighting, fog and any visual result are unconfirmed.
- The "no experimental settings warning" claim comes from reading the source, not from running the client.
- No GameTests exist, and `runGameTestServer` has not been run.
- Nobody has played a survival loop by hand. Sleeping, respawning, crafting, smelting, villager trading, breeding and redstone are vanilla and should work, but no test has shown it.
- Stairs are confirmed only by the column flood fill. It proves a player can get inside a building at ground level, not that every stair flight is climbable.
- Loot tables load in datagen, but no chest has been opened in game.
- Villager, golem, animal, bee and cat population runs without log errors, but no one has checked that they behave.
- Strongholds, mineshafts and trial chambers are wired through biome tags. No QA check locates them, and End access is untested.
- Landmarks other than the Research Manor have not been inspected on a map. Hospital, Department Store and Lake of Refuse exist in code.

### Known defects and gaps

- **Open bug:** seeds -987654321 and 555 fail `multiple_districts`, with only Market and Outskirts near spawn. Tune the 0.42 threshold in `District.ofCell`.
- **Open bug:** on seed 8675309, chunk 1,2 differs at y≥64 between generation orders. Cause unknown. Reproduction steps are in `docs/TEST_RESULTS.md`. This is a possible flaw in the order-independence guarantee, so fix it before building more on that guarantee.
- The street layout is still fairly regular despite 32-block jitter. The owner explicitly said to avoid repetitive grids. Next idea: merge some superblocks or terminate some lines at T-junctions.
- Buildings have no basements, and there are no elevated walkways, power lines, or pipe bridges. Reference art for the Hole is far denser.
- `Tower.pipes` is a single chain. Facades are simple.
- Hole rain has no effect yet, because sorcerers do not exist yet.
- Wood sources from the spec (pallets, crates, scrap lumber) are not built. Wood currently comes from building materials, loot, junkyards, trees and saplings.
- Vanilla decoration below y=64 is **not** order independent. Ore and cave decoration overlap across chunk borders exactly as in vanilla, so only 31 of 289 whole-chunk hashes matched in one early test. This comes from vanilla features, not the city painter. If the owner wants strict determinism underground, that needs separate work.
- The first commit's `.gitignore` covers `run/` and `runs/`. Evidence worth keeping must be copied to `docs/evidence/`.

## How to run things

From the repo root, in Git Bash on Windows:

```bash
./gradlew --no-daemon build
./gradlew --no-daemon runData
./gradlew --no-daemon runClient
bash tools/qa-world.sh 12345 16 forward
bash tools/qa-world.sh 12345 16 reverse
node tools/qa-compare.mjs runs/qa/12345/forward/qa/report-forward.json runs/qa/12345/reverse/qa/report-reverse.json
```

`tools/qa-world.sh <seed> [radius] [forward|reverse]` starts a real dedicated server through the `qaServer` run, generates chunks, writes `qa/report-<order>.json` and `qa/map-<order>.png` under `runs/qa/<seed>/<order>/`, then stops. The owner approved `eula=true` for local test folders. `QA_DUMP="x,z;x,z"` also dumps blocks of those chunks for diffing.

`tools/make-noise-settings.ps1` regenerates the Hole noise settings from the local client jar. `tools/make-textures.mjs` regenerates original textures. `tools/make-handoff.mjs` rebuilds this file.

## Environment traps

- **Windows commit charge.** LM Studio's `llama-server` once used enough commit to fail the JVM with error 1455 at 58.8 of 63.9 GB. Check free commit if builds die. `disable_recompilation=true` in `gradle.properties` skips the decompile. It is `false` now.
- **Background shell exit codes lie** when a command ends in `| tail`. Read `BUILD SUCCESSFUL` in the log.
- **Interrupted Gradle** can leave a corrupt `~/.gradle/caches/9.2.1/javaCompile/javaCompile.lock`. Delete just that file.
- **Long paths.** The folder is deep inside the Claude app's scratch space. `core.longpaths=true` is set. Moving the repo to a short path is recommended.
- **Large bash heredocs** with mixed quotes failed once. Put patch scripts in files.
- `python.exe` is only the Windows Store stub. Use Node or PowerShell.
- **Fandom blocks automated fetching with HTTP 402.** The lore wiki pages were not read. Do not assume their contents. See `docs/LORE_REFERENCE.md`.

## Architecture

The core idea: **the city is a pure function of seed and coordinates.**

- `CityPlan` computes arterial `Line`s per axis from a hash of seed and index, so roads and canals cannot disagree across a chunk border. Lines cut the plane into `Superblock`s.
- `Superblock` subdivides itself with a binary space partition. Every cut leaves a street, so every lot touches a street on all sides. Superblock (-1,-1) is always the starting quarter.
- `District` comes from the seeded climate sampler, so the biome source and the planner agree without sharing state. `Climate.Sampler` instances differ per chunk because they wrap `NoiseChunk` caches, but their values do not.
- `CityPainter.paint` runs at the surface step, inside `HoleChunkGenerator.buildSurface`: galleries, sewers, ground and roads, canals, lots, then shafts.
- `Canvas` clips every write to the current chunk, so a structure drawn in absolute coordinates is correct however it splits across chunks. **Variation must come from `Canvas.hash` or the lot hash, never from a stateful random.**
- `Tower` is the shared building skeleton: walls, floors, stair route, windows, door, roof. A `Furnisher` callback fills each storey.
- `CityPopulator` adds residents once per lot, from the chunk holding the lot's center.
- `HoleChunkGenerator extends NoiseBasedChunkGenerator`, which is non-final in NeoForge 21.1.265. Vanilla noise gives caves, aquifers, veins, bedrock and deepslate. `applyCarvers` is a no-op because carvers run after painting and would cut the city.
- `data/minecraft/worldgen/world_preset/normal.json` is overridden, which covers both the Create World default and dedicated servers. The `normal` preset tag is replaced so only the Hole shows in the cycle. Other presets moved to `extended` (Alt-click).
- `WorldGuard` sets the spawn through `LevelEvent.CreateSpawnPosition` and warns on worlds that are not the Hole. It never converts them.
- `HoleQa` is the test harness and does nothing without `-Ddorohedoro.qa=true`.

### Rules that must not be broken

1. **At street level and above, never place stone, tuff, deepslate, andesite, granite or diorite.** Vanilla ore features replace them up to the build limit. This put coal ore in roads before it was fixed. Use polished or brick variants. QA enforces it.
2. Vanilla feature order must be kept in biomes, or the feature sorter crashes. Follow `OverworldBiomes`.
3. Do not add water springs to Hole biomes. They can flood basements.
4. Water above an open room needs a solid block under it. See the beam in `Tower.garden`.
5. Worldgen tuning belongs in the generator codec (`CitySettings`, saved in level.dat), not in a config file, or editing a config puts seams into existing worlds.
6. GameTests run on a hard-coded flat world, so generator tests must use the plan directly or go through `qaServer`.
7. Weather is shared across dimensions, so the Sorcerers' World stays dry only through `has_precipitation: false` on every biome.
8. Any extra dimension is expected to make the world `Lifecycle.experimental` (`WorldDimensions.java:121,180`). Test the warning in a real client and decide whether to suppress it.

## What to do next, in order

1. Fix the two open bugs above and rerun the 10-seed batch.
2. **Launch the client.** Add an automatic path that opens `CreateWorldScreen`, creates a default world, takes screenshots with `Screenshot.grab`, and quits, or use `--quickPlaySingleplayer`. Confirm the world really renders as the Hole. This is the largest unverified risk.
3. Add GameTests. Purity of the plan, recipes, loot tables, door and bed placement.
4. **Phase 3.** Apply the draft in `docs/handoff/phase3-draft/`. See below.
5. **Phase 4, portals.** See below.
6. Then Phases 5 to 8 in `docs/ROADMAP.md`.

### Phase 3 draft, written but never compiled

`docs/handoff/phase3-draft/` mirrors the repo layout. It holds `RealmBiomes`, `SorcererBiomeSource`, `HellBiomeSource`, `RealmBiomeData` and the dimension JSON. `tools/make-realm-settings.ps1` builds both noise settings and has not been run. To apply:

1. Copy the draft's `src` tree into `src`.
2. Add to `ModBlocks`: `HELL_ROCK` (stone-like, `requiresCorrectToolForDrops`, strength around 2/6) and `ASH_SOIL` (shovel, strength around 0.6). Both `dropSelf` in `ModLoot`, both in `HoleBlockTags` under `MINEABLE_WITH_PICKAXE` and `MINEABLE_WITH_SHOVEL`, both in `ModModels.Blocks`, both in `en_us.json`, and add textures to `tools/make-textures.mjs`.
3. Register `SorcererBiomeSource.CODEC` as `sorcerer_lands` and `HellBiomeSource.CODEC` as `hell_realms` in `ModWorldgen`.
4. In `ModDataGen`, extend the `RegistrySetBuilder` with `.add(Registries.CONFIGURED_FEATURE, RealmBiomeData::configuredFeatures)` and `.add(Registries.PLACED_FEATURE, RealmBiomeData::placedFeatures)`, and make the biome bootstrap call both `HoleBiomeData.bootstrap` and `RealmBiomeData.biomes`. Add `sw_*` biomes to `HAS_MINESHAFT`.
5. Run the PowerShell script, `runData`, then extend `HoleQa` to generate and map those two dimensions.
6. Expected problem areas: a feature order cycle in `RealmBiomeData`, the horn tuning constants, and the experimental warning. `ModBlocks.HELL_ROCK` is referenced by the features.

Phase 3 limits to keep in mind: Hydra Forest uses vanilla dark forest trees for now, so custom tree species are still needed. City land in the Sorcerers' World is flat basin with no buildings yet. Cities belong to Phase 6. Hell has an empty mob table on purpose, because the spec bans reskinned zombies and Devils arrive in Phase 7.

### Phase 4 design, not started

- `PortalRecord`: UUID, type (`SMOKE_DOOR`, `ARTIFICIAL_DOOR`, `EN_FAMILY_DOOR`, `DEVIL_GATE`), owner or faction, source and destination `GlobalPos`, state, cost, expiry, access rules, linked return UUID, cooldown.
- `PortalRegistry extends SavedData` on the overworld data store, codec-backed.
- `DimensionTravelService`: server only. Force-load the destination chunk with a ticket, find a safe position one-to-one with the source, then call `entity.changeDimension(new DimensionTransition(level, pos, Vec3.ZERO, yaw, pitch, post))`. Per-player cooldown stops loops.
- Blocks: `reinforced_door_frame`, `artificial_door_core` with a block entity holding the portal UUID, a non-solid door-surface block that triggers travel from `entityInside`, `etched_gate_stone`, `infernal_door_core`.
- Items: `smoke_residue`, `smoke_condenser`, `smoke_conduit`, `preserved_smoke_conduit`, `door_activation_tool`, `door_linking_key`, `devil_sigil`, ritual tool.
- Multiblock validation on both horizontal axes. Artificial Door frame 4 by 5 with a 2 by 3 opening. Devil Gate frame 5 by 5 with a 3 by 3 opening.
- Create the paired exit in the destination on first use, and never duplicate it.
- Recipes and sizes are in `docs/spec/SANDBOX_DIRECTIVE.md`.
- The Artificial Door and Devil Gate need no magic, so build them first. The Smoke Door needs smoke data, so ship it with sorcerer biology on the same backend.

## Honesty rules the owner requires

Do not claim something works without evidence. Separate verified, implemented and planned. Do not invent API calls: check the 1.21.1 sources in `build/moddev/artifacts/neoforge-21.1.265-sources.jar`. Keep canon and gameplay adaptation labelled. Use original art only. After each work session report what was completed, files changed, build result, test results, blockers and the next smallest milestone.

## Files still required by the specs

`README.md` (real one, the template's is still present), `WORLD_GENERATION.md`, `PORTAL_SYSTEM.md`, `SURVIVAL_BALANCE.md`, `CRAFTING_RECIPES.md`, `VANILLA_COMPATIBILITY_MATRIX.csv` built from the real registry, and the magic documents once that work begins. `docs/LORE_REFERENCE.md`, `docs/ROADMAP.md`, `docs/IMPLEMENTATION_STATUS.md` and `docs/TEST_RESULTS.md` exist.

---

## Contents of the rest of this file

121 files follow. Generated JSON is last and can be rebuilt with `runData`. Binary files are not included: three PNG textures rebuilt by `tools/make-textures.mjs`, and the Gradle wrapper jar, which comes from the NeoForge MDK template.

- `docs/IMPLEMENTATION_STATUS.md`
- `docs/ROADMAP.md`
- `docs/TEST_RESULTS.md`
- `docs/spec/FTB_QUESTS_DIRECTIVE.md`
- `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md`
- `docs/spec/SANDBOX_DIRECTIVE.md`
- `build.gradle`
- `gradle.properties`
- `settings.gradle`
- `src/main/templates/META-INF/neoforge.mods.toml`
- `src/main/java/com/dorohedoro/hole/datagen/HoleBiomeData.java`
- `src/main/java/com/dorohedoro/hole/datagen/ModDataGen.java`
- `src/main/java/com/dorohedoro/hole/datagen/ModLoot.java`
- `src/main/java/com/dorohedoro/hole/datagen/ModModels.java`
- `src/main/java/com/dorohedoro/hole/datagen/ModRecipes.java`
- `src/main/java/com/dorohedoro/hole/Dorohedoro.java`
- `src/main/java/com/dorohedoro/hole/qa/HoleQa.java`
- `src/main/java/com/dorohedoro/hole/registry/ModBlocks.java`
- `src/main/java/com/dorohedoro/hole/registry/ModItems.java`
- `src/main/java/com/dorohedoro/hole/registry/ModWorldgen.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Canvas.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/CityPainter.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/CityPlan.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/CityPopulator.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/CitySettings.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/District.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Landmarks.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Line.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Lot.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/LotChooser.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/LotPainter.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/LotType.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Mix.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/OpenLots.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Rect.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Style.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Superblock.java`
- `src/main/java/com/dorohedoro/hole/worldgen/city/Tower.java`
- `src/main/java/com/dorohedoro/hole/worldgen/HoleBiomes.java`
- `src/main/java/com/dorohedoro/hole/worldgen/HoleBiomeSource.java`
- `src/main/java/com/dorohedoro/hole/worldgen/HoleChunkGenerator.java`
- `src/main/java/com/dorohedoro/hole/worldgen/WorldGuard.java`
- `docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/datagen/RealmBiomeData.java`
- `docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/HellBiomeSource.java`
- `docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/RealmBiomes.java`
- `docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/SorcererBiomeSource.java`
- `docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension_type/hell.json`
- `docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension_type/sorcerers_world.json`
- `docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension/hell.json`
- `docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension/sorcerers_world.json`
- `tools/make-handoff.mjs`
- `tools/make-noise-settings.ps1`
- `tools/make-realm-settings.ps1`
- `tools/make-textures.mjs`
- `tools/qa-compare.mjs`
- `tools/qa-world.sh`
- `src/main/resources/assets/dorohedoro/lang/en_us.json`
- `src/main/resources/assets/minecraft/lang/en_us.json`
- `src/main/resources/data/minecraft/tags/worldgen/world_preset/extended.json`
- `src/main/resources/data/minecraft/tags/worldgen/world_preset/normal.json`
- `src/main/resources/data/minecraft/worldgen/world_preset/normal.json`
- `docs/evidence/qa-summary.txt`
- `docs/LORE_REFERENCE.md`
- `docs/PROJECT_AUDIT.md`
- `docs/spec/ASSET_REFERENCE_AND_MODEL_MANIFEST.csv`
- `docs/spec/CLAUDE_MASTER_PROMPT.md`
- `docs/spec/CRAFTING_AND_ITEMS_SEED.csv`
- `docs/spec/MAGIC_SPELL_IMPLEMENTATION_MATRIX.csv`
- `docs/spec/README_FIRST.md`
- `docs/spec/VANILLA_MOB_COMPATIBILITY_SEED.csv`
- `.gitattributes`
- `.gitignore`
- `gradlew`
- `gradlew.bat`
- `README.md`
- `TEMPLATE_LICENSE.txt`
- `src/generated/resources/assets/dorohedoro/blockstates/industrial_debris.json`
- `src/generated/resources/assets/dorohedoro/blockstates/rusted_plating.json`
- `src/generated/resources/assets/dorohedoro/models/block/industrial_debris.json`
- `src/generated/resources/assets/dorohedoro/models/block/rusted_plating.json`
- `src/generated/resources/assets/dorohedoro/models/item/industrial_debris.json`
- `src/generated/resources/assets/dorohedoro/models/item/rusted_plating.json`
- `src/generated/resources/assets/dorohedoro/models/item/scrap_metal.json`
- `src/generated/resources/data/dorohedoro/advancement/recipes/building_blocks/rusted_plating.json`
- `src/generated/resources/data/dorohedoro/advancement/recipes/misc/iron_ingot_from_scrap.json`
- `src/generated/resources/data/dorohedoro/advancement/recipes/misc/iron_nugget_from_smelting_scrap.json`
- `src/generated/resources/data/dorohedoro/loot_table/blocks/industrial_debris.json`
- `src/generated/resources/data/dorohedoro/loot_table/blocks/rusted_plating.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/apartment.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/clinic.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/crypt.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/department.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/factory.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/guard.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/hospital.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/hungry_bug.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/lab.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/market.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/nursery.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/scrap.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/shop.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/starter_cache.json`
- `src/generated/resources/data/dorohedoro/loot_table/chests/warehouse.json`
- `src/generated/resources/data/dorohedoro/recipe/iron_ingot_from_scrap.json`
- `src/generated/resources/data/dorohedoro/recipe/iron_nugget_from_smelting_scrap.json`
- `src/generated/resources/data/dorohedoro/recipe/rusted_plating.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_derelict.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_graveyard.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_industrial.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_market.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_outskirts.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_residential.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_undercity.json`
- `src/generated/resources/data/dorohedoro/worldgen/biome/hole_waterfront.json`
- `src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- `src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/mineshaft.json`
- `src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/trial_chambers.json`
- `src/generated/resources/data/minecraft/tags/worldgen/biome/is_overworld.json`
- `src/generated/resources/data/minecraft/tags/worldgen/biome/stronghold_biased_to.json`
- `src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json`

## FILE: docs/IMPLEMENTATION_STATUS.md

```markdown
# Implementation status

Status values: `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified`, `server_verified`. `server_verified` means a headless QA run measured it on a real dedicated server. Nothing here is `client_verified`, because the client has not been launched.

| Feature | Status | Notes |
| --- | --- | --- |
| Mod scaffold, `dorohedoro` ID, datagen | `server_verified` | Loads on a dedicated server |
| Overworld replaced through `minecraft:normal` preset | `server_verified` | Create World screen unconfirmed |
| Spawn on the plaza | `server_verified` | |
| Street plan, avenues, tram streets, canals | `server_verified` | Walkability and leak checks. Layout still fairly regular |
| Districts and 8 Hole biomes | `server_verified` | |
| Towers, tenements, apartments, shops, clinics, guard posts | `server_verified` for entry only | Stair flights not individually proven |
| Warehouses, factories | `server_verified` for entry only | |
| Market stalls, greenhouses, pens, nurseries, parks, vacant lots, junkyards, rubble, cemeteries | `implemented` | Appear in lot counts. Not inspected individually |
| Starting quarter with Hungry Bug, shelter, plaza | `implemented` | Layout unseen in a client |
| Hospital, Department Store, Research Manor, Lake of Refuse | `implemented` | Placement exists. Not inspected |
| Sewers, mine galleries, ladder shafts | `implemented` | Not walked |
| Vanilla ores, caves, aquifers underground | `server_verified` | Census only |
| Scrap metal, debris, rusted plating, recipes | `build_verified` | Not crafted in game |
| Chest loot tables | `build_verified` | Not opened in game |
| Villagers, golems, animals, bees, cats in lots | `implemented` | No errors logged. Behaviour unchecked |
| Strongholds, mineshafts, trial chambers | `implemented` | Biome tags only. Not located |
| Headless QA harness | `server_verified` | |
| GameTests | `planned` | None exist |
| Sorcerers' World, Hell | `planned` | Draft in `docs/handoff/phase3-draft/`, never compiled |
| Portals | `planned` | Design in `HANDOFF.md` |
| Sorcerers, smoke, magic, masks | `planned` | `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md` |
| NPCs, factions, trading beyond vanilla villagers | `planned` | |
| FTB Quests | `planned`, on hold | Owner asked not to start |
| Immersion and Compatibility modes, gameplay config | `planned` | Nether and End are untouched vanilla |
```

## FILE: docs/ROADMAP.md

```markdown
# Roadmap

The phase order comes from `docs/spec/SANDBOX_DIRECTIVE.md`. Status values are `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified` and `server_verified`. Evidence for anything past `implemented` is in `docs/TEST_RESULTS.md`.

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Project audit | Done, see `docs/PROJECT_AUDIT.md` |
| 2 | Hole sandbox foundation | In progress |
| 3 | Sorcerers' World and Hell generation | Planned |
| 4 | Portals: Smoke Door, Artificial Door, En Family Door, Devil Gate | Planned |
| 5 | Resources, crafting, wood sources, gyoza | Planned |
| 6 | Structures, NPCs, trading, environment | Planned |
| 7 | Combat, magic, masks, hostile sorcerers | Planned |
| 8 | Optimization and full survival testing | Planned |
| Deferred | FTB Quests integration, see below | **Not started, on hold until the owner asks** |
| Queued | Complete magic system, see below. Expands Phase 7 | **Not started** |

## Queued: complete magic system

Added at the owner's request on 2026-10-31. The full requirements are in `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md`. It expands Phase 7 of this roadmap with its own 12 internal phases: lore audit, sorcerer biology, magic engine, six first types (Mushroom, Healing, Dismemberment, Reptile Transformation, Destruction, Dispelling), world interaction, smoke economy and equipment, mastery, doors, NPCs and environment, rare powers, FTB Quests, verification.

Ordering conflict to settle: that directive puts magical doors at its Phase 8, while the sandbox directive puts portals at Phase 4. The Artificial Door and Devil Gate need no magic, so they stay at Phase 4. The Smoke Door needs smoke, so it should ship with sorcerer biology, on the shared portal backend.

## Deferred: FTB Quests integration

Added at the owner's request on 2026-10-31 with the instruction **not to start yet**. The full requirements are in `docs/spec/FTB_QUESTS_DIRECTIVE.md`.

It is an optional guide and achievement layer on the real FTB Quests mod. It must never gate recipes, dimensions, portals, trading or abilities, so it does not conflict with the no-quests sandbox rule.

Dependencies: it references real registry IDs, so it needs Phases 3 to 5 in place first (dimensions, biomes, portal blocks and items, gyoza, scrap wood). Its custom portal triggers need the Phase 4 events to exist.

First batch when started: audit supported FTB versions for NeoForge 1.21.1, then 20 to 30 tested quests covering survival, farming, industry, smoke materials, Artificial Door construction, dimension travel, forests and Hell. Later batches grow to about 150 to 200 quests across 15 chapters.
```

## FILE: docs/TEST_RESULTS.md

````markdown
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
````

## FILE: docs/spec/FTB_QUESTS_DIRECTIVE.md

```markdown
# FTB Quests integration directive

Digest of the owner's FTB Quests master prompt, received 2026-10-31. **Status: deferred. Do not start until the owner asks.** It adds to the sandbox directive and replaces nothing.

## Rules

- **Optional.** Quests are a guide, achievement tracker and resource reward system. Players can ignore them completely. They must never gate crafting, mining, building, farming, redstone, recipes, doors, dimensions, Devil Gates, trading or innate abilities.
- **Credit organic play.** A player who activates an Artificial Door before opening the book should still get credit where technically feasible. Where FTB does not backfill, add a reliable state check or document the limit.
- **Use the real mod.** This project is Java on NeoForge 1.21.1, so the Java FTB Quests applies, not the Bedrock Marketplace add-on. Do not mix SNBT with Bedrock script events. Do not invent API calls. Read the docs and code for the exact target version first.
- **Dependencies:** FTB Quests, FTB Library, FTB Teams, plus whatever the chosen releases need. Evaluate FTB XMod Compat, KubeJS, JEI or REI, FTB Filter System and FTB Chunks, and do not install anything unnecessary. Do not bundle FTB binaries or classes against their licence.
- **Packaging.** Quest data lives under `config/ftbquests/quests/`. Do not assume SNBT inside the mod jar loads automatically. Provide a modpack layout, a dedicated-server install method, and a dependency manifest.
- **Stable quest IDs** so updates do not invalidate progress.
- **Real IDs only.** Check every item, block, biome, entity, structure, advancement and dimension against the actual registries. If something does not exist, implement it properly or defer the quest. No uncompletable objectives.
- **Use the stock FTB UI** with supported theming: rusted industrial textures, dirty concrete and metal, smoke motifs, black, gray, dark red and industrial orange, skull, mask, mushroom, heart and door iconography. No separate GUI. Original or licensed art only.

## Chapters

1. Welcome to the Hole: scrap lumber, table, tools, stone, furnace, cooking, coal, bed, shelter, storage.
2. Surviving the Streets: abandoned building, scrap, urban biome, medical facility, protection, sewers, weapon.
3. Industrial Survival: iron, copper, redstone, hopper, storage, machine, automated furnace, redstone door.
4. Farming and Cooking: seeds, farmland, harvest, cooking station, gyoza, fishing, food farm, sapling, renewable food. No rare magic ingredients for basic food.
5. The Underground: depth, caves, minerals, diamonds, rare resources, underground structure, industrial tunnel. Every task must match real generation.
6. Building a Life: materials, furniture, storage, lighting, farm, reinforced blocks, redstone, automation. No arbitrary house shape.
7. Magic and Smoke: magic material, smoke processing, Smoke Condenser, mask, component, device. Sorcerer-only quests get alternatives or are marked optional. Rewards never grant biological abilities to humans.
8. Doors Between Worlds: door materials, condenser, core, frame, activation, temporary Smoke Door, Sorcerers' World, return, linked pair, second portal. Track real construction and activation. Holding a core does not count.
9. Sorcerers' World: enter, settlement, local materials, trade, biomes, landmark, base, resources.
10. Hydra Forest: discover, timber, saplings, grow a tree, woodland structures, Haru's former cabin if present. No attacking nonhostile characters.
11. Masks and Equipment: mask, protective gear, repair, weapon upgrade, advanced material, crafting station.
12. Combat and Survival: hostile mobs, dangerous encounter, armor, repair, hostile sorcerer, Living Dead Day if enabled, advanced challenges. No named nonhostile kills.
13. Commerce and Industry: trade, sell, bulk production, automated farm, machinery, repeatable production.
14. Hell: Devil Gate materials, Infernal Door Core, assemble, activate, enter, biome, resources, shelter, return safely.
15. Mastery and Sandbox Challenges: bases in all three dimensions, several portals, large automation, tree farm, advanced gear, food farm, rare structures, combat milestones, rare collection, long-term co-op. Mostly independent, with no single ending.

## Tasks, dependencies and rewards

- Prefer native task types: item, submission, kill, advancement, dimension, biome, structure, location, stats, XP, observation. Custom tasks only when needed, through stable FTB API or KubeJS with FTB XMod Compat.
- Custom triggers wanted: door constructed, door activated, Smoke Door used, safe return from Hell, custom tree grown, innate ability used, industrial process completed. Server-authoritative, success only, no duplicates, event-based rather than per-tick scans.
- Dependencies organize the book and never lock the game. Example branches: scrap wood to table to furnace to iron to machinery; smoke materials to condenser to core to activation to Sorcerers' World; Devil materials to gate to Hell to Hell resources.
- Rewards: food, medical items, torches, scrap, iron, copper, blocks, saplings, seeds, ingredients, XP, modest equipment, decoration. Rare items are sparse, no routine diamonds, and no rewards that make mining or trading pointless.
- Repeatables for scrap, crops, food, trading, ores, mobs and manufactured goods, with cooldowns. No loops that return more of an item than they consume.

## Multiplayer

FTB Teams: shared progress, individual and team rewards as appropriate, membership changes handled, no duplicate claims from simultaneous clicks, safe across restarts, and no team hopping or PvP exploit for infinite rewards. Document the difference between team progress and reward ownership.

## Testing

Verify in singleplayer and on a dedicated server with the real installed version: FTB loads, book opens, chapters show, all quest IDs valid, no references to missing items or biomes, dimension, structure and item tracking work, crafting objectives work, portal events count only real successes, Smoke Door, Artificial Door and Hell entry are recognized, forestry objectives work, rewards claim, repeatables reset, reward loops cannot duplicate valuables, team sharing and reward separation work, progress survives restarts and book updates, a player can ignore every quest, no system is gated, and packaging is correct.

## Deliverables

First a working foundation of 20 to 30 quests, expanded only after verification, reaching about 150 to 200 quests. Files: quest configuration, chapters, localization, custom task code or scripts, reward definitions, icons and themes, dependency manifest, install instructions, `FTB_QUESTS_INTEGRATION.md`, `FTB_QUESTS_TEST_RESULTS.md`, updated `HANDOFF.md` and `IMPLEMENTATION_STATUS.md`. A chapter is not complete because its title and icon exist.

## Sources named by the owner

docs.feed-the-beast.com/mod-docs/mods/suite/Quests, github.com/FTBTeam/FTB-Quests, github.com/FTBTeam/FTB-XMod-Compat, the CurseForge FTB Quests pages, kubejs.com/wiki/addons/ftb-xmod-compat, and the FTB Bedrock add-on docs for reference only.
```

## FILE: docs/spec/MAGIC_SYSTEM_DIRECTIVE.md

```markdown
# Complete magic system directive

Digest of the owner's magic system master prompt, received 2026-10-31. **Status: on the roadmap, not started.** It expands the earlier specs and replaces nothing. The sandbox rule still holds: no mandatory quests.

## Canon rules to preserve

Sorcerers are biologically different from humans, with smoke-producing organs and a Devil Tumor. Each ordinary sorcerer has one innate type and cannot learn unrelated magic. Smoke carries the effect, and quantity is distinct from quality. Smoke can be stored and used by others. High-quality masks can enhance magic. Black Powder temporarily enhances magic and has exceptional interactions with people who cannot otherwise produce it. Sorcerers make doors when they have enough smoke. Devils are exceptions. Magic can transform creatures and objects, sometimes persistently. Hole rain can severely affect sorcerers. Time, resurrection and curse magic have exceptional rules. The manga outranks secondary summaries. Always separate canon, reasonable implementation, and original extension, in `MAGIC_LORE_REFERENCE.md`.

## Origins

- **Human:** no natural smoke, viable through equipment, machinery, medicine, trade and smoke products. Temporary effects never rewrite origin.
- **Sorcerer:** one innate type, Devil Tumor, smoke organs, and independently generated stats. Type and capacity are separate.
- **Weak Sorcerer:** a sorcerer preset with low output, not a species.
- **Devil:** optional advanced system with permissions. Never required.
- Creation offers Human, Sorcerer, Weak preset, random or manual magic, weighted rarity, and server-restricted rares. Data survives death, logout, restart and travel. No reroll exploit. Safe migration for existing players.

## Smoke

Stats: Maximum Smoke, Current Smoke, Production, Quality, Control, Efficiency, Mastery, Physical Condition, Emission. Regeneration depends on rest, food, injury, masks, environment, exhaustion and enhancement, never on just standing still. At zero smoke, casting and doors fail but normal Minecraft actions continue. Visible smoke is client particles with bounded server volumes. Most smoke is black, with canon exceptions. This is not renamed XP or mana.

## Innate types

One type with several techniques. Modular registry. First playable batch, in order: **Mushroom (En), Healing (Noi), Dismemberment (Shin), Reptile Transformation (Ebisu), Destruction (Fujita), Dispelling (Chota).** Then the exceptional handlers: Time (Nikaido), Curse (Risu), Resurrection (Kikurage), Puppet/Cooking (Turkey), Gravity, Restoration, then flight, barriers, rot, enlargement, reduction, transformation, strength and others in `MAGIC_TYPES.md`, each listing owner, effect, source, rarity, techniques, systems, exploits, status and verification.

Key constraints:

- Healing uses the real health and damage system and does not repair stone or metal.
- Dismemberment is not instant death and must not pretend Slowness is a detached limb. Use an abstract, reversible injury system.
- Reptile transformation preserves inventory and identity.
- Destruction is bounded and respects claims and griefing rules. Fujita's weakness is low output.
- Dispelling never destroys structures, inventories or origins.
- Time magic is never a world rollback. It must never duplicate items, reverse trades, restore consumed resources, rewind bases or undo saves.
- Curse is not a repeatable cast or a default starter.
- Resurrection is distinct from healing and never duplicates inventories.
- Puppets are not an army, and cannot duplicate items, bypass claims or load infinite chunks.
- Restoration cannot create free ores or diamonds.

## Mastery

Novice, Practiced, Experienced, Master, Exceptional, as gameplay ranks and not canon. Improves control, efficiency, precision, range, complexity, area and stability. Capacity and production improve separately. XP only from meaningful successful uses, with diminishing returns against AFK farming. Never adds unrelated types.

## Controls and HUD

Smoke bar, innate icon, selected technique, cooldown, regen feedback, mastery, mask bonuses. Rebindable keys: open menu, next, previous, cast, alternate, create door. Radial menu shows only the player's own techniques with cost, cooldown, targets, availability and mastery needs. Accessibility options. No silent failures.

## Casting pipeline, server-authoritative

Validate player and origin, type, technique, mastery, smoke, cooldown and status, range and sight, world and dimension, protection and PvP, then consume smoke, apply the effect, record persistence, sync, and notify progression and quests. Costs, cooldowns and ranges are data-driven and datapack or config tunable, with registered handlers for complex behaviour and no arbitrary scripting.

## Stored smoke and Black Powder

Empty and filled bottle, jar, spray can, tank, extraction and transfer equipment. Containers keep type, quantity, quality, source, special properties and usage. Collection consumes the producer's smoke. Using a bottle never grants the innate ability or organs. Works with trading, shops, hoppers, storage, crafting and multiplayer. Black Powder is separate from gunpowder, gives temporary amplification with configurable drawbacks, and comes from rare loot, trading, faction encounters or a documented adapted process, never a fake lore recipe.

## Masks

Improvised, cheap commercial, quality crafted, specialized sorcerer, rare Devil-made. Physical models, multiplayer sync, repair, tooltips. Cheap masks are cosmetic. Devil-made masks are not casually craftable.

## Doors

Smoke Door is a general ability themed by magic and mask, not a new affinity, with thresholds, valid destination and placement. Artificial Door stays usable by humans with no quest. En Family Door is a real structure. Devil Gate stays separate and gives no general Hell teleport to sorcerers. All share the persistent server backend.

## Environment

Hole rain reduces production and control, adds casting penalties and illness on prolonged exposure, with shelter and configurable severity, and does not hit humans with sorcerer sickness. Sorcerers' World is normal for magic and stays dry. Hell does not blanket-disable magic.

## Blocks and entities

Block transformation service records original and new state, source, dimension, position, time and reversion rules. Never transform bedrock, protected blocks, or portal cores without support, never copy container contents, never allow ore duplication, and bound snapshots per cast, chunk, player and server. Permanent changes are supported when explicitly permitted. Entity transformation keeps identity, health rules and equipment, persists across saves, prevents duplication and supports reversal. Player transformation keeps UUID, inventory, permissions, origin and magic data, with disconnect recovery.

## Systems beyond combat

Each type gets a canon-fitting non-combat use: fungal agriculture, medical services, controlled demolition, repair, movement. No generic shared techniques. Smoke tanks with hoppers, smoke machines, dispensers with bottled smoke, redstone doors, with no infinite magic automation.

## NPCs, PvP, performance

NPC sorcerers use the same engine with their own type, smoke, regen, targeting, doors, rain reaction, masks and bottled smoke. Named characters keep canon powers. Bounded casting and effects. The server decides everything. Admin config for player transformation, dismemberment, destruction, friendly fire, protected areas, permanent changes, rarity and production. Bounds on volumes, projectiles, structures, controlled mobs, changes per cast, transformations, snapshots and NPC cast rate. No per-tick scans of loaded blocks.

## FTB Quests

Optional chapters: Discovering Magic, Your Innate Ability, Smoke Production, Masks, Magic and Survival, Magical Doors, Advanced Magic. Never gates ability use. The magic system must work when FTB is absent. See `FTB_QUESTS_DIRECTIVE.md`.

## Its phase order

1 repository and lore audit, 2 sorcerer biology, 3 magic engine, 4 first six types, 5 world interaction, 6 smoke economy and equipment, 7 mastery, 8 magical doors, 9 NPCs and environment, 10 rare powers, 11 FTB Quests, 12 verification.

## Required tests (summary of the 38)

Human has no natural regen, one innate type, persistence, regen per config, exact cost, failure at low smoke, cooldowns, mastery on valid use, no unrelated types, each first-batch type working, rare abilities not exploitable, bottles keep type, storing consumes equal smoke, humans use smoke products, Black Powder effects, mask bonuses, cheap masks weak, rain effects, normal controls intact, Smoke Doors, Artificial Doors for humans, persistence after restart, no inventory duplication, identity preserved, dedicated servers, simultaneous casters, PvP and claims, NPC abilities, FTB records success, no quest gating, assets load, no severe tick degradation, dependencies work.

## Required documents

`MAGIC_SYSTEM.md`, `MAGIC_TYPES.md`, `MAGIC_LORE_REFERENCE.md`, `SMOKE_SYSTEM.md`, `MAGIC_DOORS.md`, `MASKS_AND_EQUIPMENT.md`, `MAGIC_SURVIVAL_BALANCE.md`, `MAGIC_FTB_INTEGRATION.md`, plus `README.md`, `HANDOFF.md`, `IMPLEMENTATION_STATUS.md`, `TEST_RESULTS.md`. Per type, list which techniques work, are partial, or are planned.

## Lore references given

viz.com/dorohedoro and the fandom wiki pages Magic, Magic_Users, Types_of_Magic, Devil_Tumor, Masks, Black_Powder, Magic_User_Door. FTB docs at docs.feed-the-beast.com/docs/mods/suite/Quests, github.com/FTBTeam/FTB-Quests and FTB-XMod-Compat.
```

## FILE: docs/spec/SANDBOX_DIRECTIVE.md

````markdown
# Sandbox directive (supersedes the earlier master prompt where they conflict)

Digest of the owner's "Complete Minecraft Sandbox Conversion" prompt, received 2026-10-31. `CLAUDE_MASTER_PROMPT.md` still applies wherever this does not contradict it, including its canon rules, anti-exploit rules and honesty rules.

## What changed

- **Not a story mod.** No mandatory campaign, quests, chapters or scripted progression. The earlier quest framework and campaign are dropped as required content. Any story content must be optional and separate.
- **Three genuine, infinite, persistent dimensions:** the Hole (the Overworld), the Sorcerers' World, and Hell. Hell is no longer a late optional instance.
- **Portals are top priority**, ahead of NPCs and magic.
- **Sorcerers' World weather is dry.** No random rain or snow. Humans can live there without gaining magic.
- **Hell must not look like the Nether**, and is independent of it in terrain, mobs, architecture and portals.

## Phase order

1. Project audit. 2. Hole sandbox foundation. 3. Sorcerers' World and Hell generation. 4. Portals: Smoke Door, Artificial Door, En Family Door, Devil Gate. 5. Resources and crafting. 6. Structures, NPCs, environment. 7. Combat and magic. 8. Optimization and testing.

## Hard rules

- Every Minecraft system keeps working: blocks, crafting, survival, exploration, farming, redstone, multiplayer. Adapt appearance, never remove capability.
- All players start in the Hole. Any essential resource must be renewable or reliably repeatable. No permanent stranding.
- All portals share one server-authoritative backend. Teleports validate that the destination is loaded and safe, and preserve inventory, health and passengers where supported.
- No progression material may be obtainable only by killing innocent NPCs or only by killing a named Devil. Nothing needed for a first portal may be found only in the destination.
- Portals respect protection, cannot be used to bypass bases or claims, and redstone cannot bypass activation costs or access rules.
- Chidaruma must not be a conventional farmable boss. Devils must not be reskinned zombies.
- Magic never auto-grants on dimension entry. Sorcerers have one innate type.
- No block transformations that can corrupt chunks.

## Hole additions

Rare vegetation only: dead and struggling trees, bushes, weeds, weighted to abandoned lots, courtyards and outskirts, never forests. Add outer urban districts and sparse rural-industrial terrain with natural transitions. Avoid repetitive grids. Wood must not depend on rare trees: furniture, pallets, crates, scrap lumber and traders, plus saplings for tree farms. Underground: stone layers, ore, caves, waterways, sewers, tunnels, hazards, without huge openings under every foundation. Landmarks: Hungry Bug, the Hospital, Kasukabe's laboratory, Lake of Refuse. Rain weakens sorcerers but not ordinary humans to the point of impossibility.

## Sorcerers' World content

Gothic-industrial cities, Zagan City, residential districts, En's Mansion, sorcerer schools, poor districts, markets, Devil Churches, manufacturing, Hydra Forest (with Haru's cabin and Cross-Eyes structures), rural woodland, roads and countryside, gardens, and Mastema's mushroom-devastated region. Not wall-to-wall giant mushrooms. Full forests with saplings and renewable wood. Caves, stone, minerals, ruins, hazards. Magic-reactive minerals are allowed as labelled adaptations.

## Hell content

Hostile and barren: horn-shaped mountains, wastelands, immense rock formations, caverns, unusual stone, giant Devil structures, soul-associated areas. No natural forests. Building, mining and storage must still work, and any dimension-specific rule must be documented and configurable.

## Portal specification

**Shared record per portal:** unique ID, type, owner or faction, source and destination dimension, source and destination coordinates, state, activation cost, lifetime, access restrictions, linked return portal, persistence and cooldown data. Anchors are one-to-one, not Nether 8:1. Prevent duplicate destinations and loops. Provide a safe default destination.

**Type A, Smoke Door.** Sorcerer ability. Target a valid surface, spend smoke, spawn an animated door. Validate space, stable floor, destination dimension, smoke, protection and portal conflicts. Suggested defaults, all configurable: high capacity requirement, significant activation cost, about 90 seconds active, short cooldown. Weak sorcerers cannot make independent doors. Connects the Hole and Sorcerers' World with a paired exit while active. Visual styles by owner: Shin heart, hammer and stitches; Noi bandages; En mushrooms; Ebisu skull; Nikaido cassette. Custom styles for player sorcerers.

**Type B, Artificial Door.** For humans. Required content: Reinforced Door Frame, Smoke Condenser, Smoke Conduit, Artificial Door Core, Door Activation Tool, Door Linking Key, animated door block or entity, recipes and loot. Proposed recipes, marked as gameplay adaptations:

```text
Smoke Condenser      I R I / C S C / I R I   I iron ingot, R redstone dust, C copper ingot, S Smoke Residue
Artificial Door Core B C B / I M I / B R B   B bone block, C Smoke Condenser, I iron ingot, M Preserved Smoke Conduit, R redstone block
```

Smoke Residue comes from sorcerer-related drops, abandoned laboratories or traders. Frame is 4 wide by 5 tall with a 2 by 3 opening and the core in the bottom edge:

```text
F F F F
F . . F
F . . F
F . . F
F C F F
```

Sequence: craft parts, build frame, install core, activate with tool, smoke emits and a physical door forms, the exit is created or located, walk through. Persistent across restarts, saved IDs and links, several per world, optional ownership and access control. Must not look like a Nether portal.

**Type C, En Family Door.** Naturally generated interactive structure in the Sorcerers' World, repeatable travel to the Hole, guarded by reputation but never the only route.

**Type D, Devil Gate.** Reaches Hell. Parts: Devil Sigil, Etched Gate Stone, Infernal Door Core, ritual tool, reinforced frame blocks. Example: 5 by 5 outer frame, 3 by 3 interior. Sigil comes from rare loot, bartering, exploration finds, or renewable high-cost trade, never only from killing a named Devil. Not consumed by routine use unless a harder mode is configured. Safe landing area, connected return gate, interaction or cooldown against loops. All materials for the first gate must be obtainable before entering Hell.

## Economy and systems

Vanilla ore progression worthwhile in all three dimensions. All wood types functional, scrap wood craftable into basic wooden materials. Gyoza is a repeatable useful food. Currency abstraction, repeatable trades, no dialogue chains. Trees are real features with saplings, growth, bone meal, leaf decay and farming. Vanilla fantasy mobs do not auto-spawn in inappropriate regions; provide replacements or configurable vanilla spawning with resource alternatives. NPCs are inhabitants: routines, trading, restocking, workplaces, threat reactions, factions. Structures are not indestructible.

## Modes and options

- **Dorohedoro Immersion Mode** (default): the three realms are primary, and essential materials stay available by alternate routes.
- **Minecraft Compatibility Mode:** Nether and End stay available with normal vanilla progression.

Configurable: world size and biome weights, urban and tree density, dimension difficulty, hostile sorcerer rates, vanilla mob spawning, smoke availability, portal costs and lifetimes, Hell entry difficulty, building protection, PvP, explosions, NPC griefing, respawn rules, keep inventory, mob drops, resource generation, weather, Nether and End compatibility. Existing gamerules are honoured, not duplicated.

## Required documents

`README.md`, `HANDOFF.md`, `LORE_REFERENCE.md`, `WORLD_GENERATION.md`, `PORTAL_SYSTEM.md`, `SURVIVAL_BALANCE.md`, `CRAFTING_RECIPES.md`, `IMPLEMENTATION_STATUS.md`, `TEST_RESULTS.md`. Include exact recipe, block, item and dimension IDs, and mark canon versus adaptation. Update the handoff after every major feature.

## Lore references given

viz.com/dorohedoro, and the Dorohedoro fandom wiki pages Magic_User_Door, En_Family_Door, Hydra_Forest, Hell, Magic and Category:The_Magic_User_World.
````

## FILE: build.gradle

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.148'
    id 'idea'
}

tasks.named('wrapper', Wrapper).configure {
    // Define wrapper values here so as to not have to always do so when updating gradlew.properties.
    // Switching this to Wrapper.DistributionType.ALL will download the full gradle sources that comes with
    // documentation attached on cursor hover of gradle classes and methods. However, this comes with increased
    // file size for Gradle. If you do switch this to ALL, run the Gradle wrapper task twice afterwards.
    // (Verify by checking gradle/wrapper/gradle-wrapper.properties to see if distributionUrl now points to `-all`)
    distributionType = Wrapper.DistributionType.BIN
}

version = mod_version
group = mod_group_id

sourceSets.main.resources {
    // Include resources generated by data generators.
    srcDir('src/generated/resources')

    // Exclude common development only resources from finalized outputs
    exclude("**/*.bbmodel") // BlockBench project files
    exclude("src/generated/**/.cache") // datagen cache files
}

repositories {
    // Add here additional repositories if required by some of the dependencies below.
}

base {
    archivesName = mod_id
}

// Mojang ships Java 21 to end users in 1.21.1, so mods should target Java 21.
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    enable {
        // Specify the version of NeoForge to use.
        version = project.neo_version
        // Full decompilation needs roughly 6 GB of free commit, which the dev machine does not reliably have.
        // Set disable_recompilation=false in gradle.properties to get browsable patched sources.
        disableRecompilation = project.disable_recompilation.toBoolean()
    }

    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    // This line is optional. Access Transformers are automatically detected
    // accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        client {
            client()
            gameDirectory = project.file('runs/client')
            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        server {
            server()
            gameDirectory = project.file('runs/server')
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        // Headless world validation on a real dedicated server. Driven by tools/qa-world.sh.
        qaServer {
            server()
            gameDirectory = project.file(project.findProperty('qa_dir') ?: 'runs/qa/manual')
            programArgument '--nogui'
            systemProperty 'dorohedoro.qa', 'true'
            systemProperty 'dorohedoro.qa.radius', project.findProperty('qa_radius') ?: '16'
            systemProperty 'dorohedoro.qa.order', project.findProperty('qa_order') ?: 'forward'
            systemProperty 'dorohedoro.qa.dump', project.findProperty('qa_dump') ?: ''
            systemProperty 'java.awt.headless', 'true'
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        gameTestServer {
            type = "gameTestServer"
            gameDirectory = project.file('runs/gametest')
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        data {
            data()
            gameDirectory = project.file('runs/data')

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll '--mod', project.mod_id, '--all', '--output', file('src/generated/resources/').getAbsolutePath(), '--existing', file('src/main/resources/').getAbsolutePath()
        }

        // applies to all the run configs above
        configureEach {
            logLevel = org.slf4j.event.Level.INFO
            jvmArguments.addAll '-Xmx3G', '-XX:+UseG1GC'
        }
    }

    mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // multi mod projects should define one per mod
        "${mod_id}" {
            sourceSet(sourceSets.main)
        }
    }
}

// Sets up a dependency configuration called 'localRuntime'.
// This configuration should be used instead of 'runtimeOnly' to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
configurations {
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    // Example optional mod dependency with JEI
    // The JEI API is declared for compile time use, while the full JEI artifact is used at runtime
    // compileOnly "mezz.jei:jei-${mc_version}-common-api:${jei_version}"
    // compileOnly "mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}"
    // We add the full version to localRuntime, not runtimeOnly, so that we do not publish a dependency on it
    // localRuntime "mezz.jei:jei-${mc_version}-neoforge:${jei_version}"

    // Example mod dependency using a mod jar from ./libs with a flat dir repository
    // This maps to ./libs/coolmod-${mc_version}-${coolmod_version}.jar
    // The group id is ignored when searching -- in this case, it is "blank"
    // implementation "blank:coolmod-${mc_version}:${coolmod_version}"

    // Example mod dependency using a file as dependency
    // implementation files("libs/coolmod-${mc_version}-${coolmod_version}.jar")

    // Example project dependency using a sister or child project:
    // implementation project(":myproject")

    // For more info:
    // http://www.gradle.org/docs/current/userguide/artifact_dependencies_tutorial.html
    // http://www.gradle.org/docs/current/userguide/dependency_management.html
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
            minecraft_version      : minecraft_version,
            minecraft_version_range: minecraft_version_range,
            neo_version            : neo_version,
            loader_version_range   : loader_version_range,
            mod_id                 : mod_id,
            mod_name               : mod_name,
            mod_license            : mod_license,
            mod_version            : mod_version,
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
// Include the output of "generateModMetadata" as an input directory for the build
// this works with both building through Gradle and the IDE.
sourceSets.main.resources.srcDir generateModMetadata
// To avoid having to run "generateModMetadata" manually, make it run on every project reload
neoForge.ideSyncTask generateModMetadata

// Example configuration to allow publishing using the maven-publish plugin
publishing {
    publications {
        register('mavenJava', MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url "file://${project.projectDir}/repo"
        }
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8' // Use the UTF-8 charset for Java compilation
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        downloadSources = true
        downloadJavadoc = true
    }
}
```

## FILE: gradle.properties

```properties
# Sets default memory used for gradle commands. Can be overridden by user or command line properties.
org.gradle.jvmargs=-Xmx2G -XX:+UseSerialGC
org.gradle.daemon=true
org.gradle.parallel=false
org.gradle.caching=true
org.gradle.configuration-cache=true

#read more on this at https://github.com/neoforged/ModDevGradle?tab=readme-ov-file#better-minecraft-parameter-names--javadoc-parchment
# you can also find the latest versions at: https://parchmentmc.org/docs/getting-started
parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.17
# Environment Properties
# You can find the latest versions here: https://projects.neoforged.net/neoforged/neoforge
# The Minecraft version must agree with the Neo version to get a valid artifact
minecraft_version=1.21.1
# The Minecraft version range can use any release version of Minecraft as bounds.
# Snapshots, pre-releases, and release candidates are not guaranteed to sort properly
# as they do not follow standard versioning conventions.
minecraft_version_range=[1.21.1]
# The Neo version must agree with the Minecraft version to get a valid artifact
neo_version=21.1.265
# The loader version range can only use the major version of FML as bounds
loader_version_range=[1,)

## Mod Properties

# The unique mod identifier for the mod. Must be lowercase in English locale. Must fit the regex [a-z][a-z0-9_]{1,63}
# Must match the String constant located in the main mod class annotated with @Mod.
mod_id=dorohedoro
# The human-readable display name for the mod.
mod_name=Dorohedoro: The Hole
# The license of the mod. Review your options at https://choosealicense.com/. All Rights Reserved is the default.
mod_license=All Rights Reserved
# The mod version. See https://semver.org/
mod_version=0.1.0
# The group ID for the mod. It is only important when publishing as an artifact to a Maven repository.
# This should match the base package used for the mod sources.
# See https://maven.apache.org/guides/mini/guide-naming-conventions.html
mod_group_id=com.dorohedoro.hole

# Skip decompile/recompile and apply NeoForge's binary patches instead. See build.gradle.
disable_recompilation=false
```

## FILE: settings.gradle

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
```

## FILE: src/main/templates/META-INF/neoforge.mods.toml

```toml
# This is an example neoforge.mods.toml file. It contains the data relating to the loading mods.
# There are several mandatory fields (#mandatory), and many more that are optional (#optional).
# The overall format is standard TOML format, v0.5.0.
# Note that there are a couple of TOML lists in this file.
# Find more information on toml format here:  https://github.com/toml-lang/toml

# The name of the mod loader type to load - for regular FML @Mod mods it should be javafml
modLoader="javafml" #mandatory

# A version range to match for said mod loader - for regular FML @Mod it will be the FML version. This is currently 2.
loaderVersion="${loader_version_range}" #mandatory

# The license for you mod. This is mandatory metadata and allows for easier comprehension of your redistributive properties.
# Review your options at https://choosealicense.com/. All rights reserved is the default copyright stance, and is thus the default here.
license="${mod_license}"

# A URL to refer people to when problems occur with this mod
#issueTrackerURL="https://change.me.to.your.issue.tracker.example.invalid/" #optional

# A list of mods - how many allowed here is determined by the individual mod loader
[[mods]] #mandatory

# The modid of the mod
modId="${mod_id}" #mandatory

# The version number of the mod
version="${mod_version}" #mandatory

# A display name for the mod
displayName="${mod_name}" #mandatory

# A URL to query for updates for this mod. See the JSON update specification https://docs.neoforged.net/docs/misc/updatechecker/
#updateJSONURL="https://change.me.example.invalid/updates.json" #optional

# A URL for the "homepage" for this mod, displayed in the mod UI
#displayURL="https://change.me.to.your.mods.homepage.example.invalid/" #optional

# A file name (in the root of the mod JAR) containing a logo for display
#logoFile="examplemod.png" #optional

# A text field displayed in the mod UI
#credits="" #optional

# The authors of the mod, displayed in the mod UI (optional)
authors="Cam"

# The description text for the mod (multi line!) (#mandatory)
description='''
Total-conversion fan mod: the Overworld becomes the Hole, an endless polluted city, with sorcerer smoke magic layered on full vanilla survival.
'''

# The [[mixins]] block allows you to declare your mixin config to FML so that it gets loaded.
#[[mixins]]
#config="${mod_id}.mixins.json"

# The [[accessTransformers]] block allows you to declare where your AT file is.
# If this block is omitted, a fallback attempt will be made to load an AT from META-INF/accesstransformer.cfg
#[[accessTransformers]]
#file="META-INF/accesstransformer.cfg"

# A dependency - use the . to indicate dependency for a specific modid. Dependencies are optional.
[[dependencies.${mod_id}]] #optional
    # the modid of the dependency
    modId="neoforge" #mandatory
    # The type of the dependency. Can be one of "required", "optional", "incompatible" or "discouraged" (case insensitive).
    # 'required' requires the mod to exist, 'optional' does not
    # 'incompatible' will prevent the game from loading when the mod exists, and 'discouraged' will show a warning
    type="required" #mandatory
    # Optional field describing why the dependency is required or why it is incompatible
    # reason="..."
    # The version range of the dependency
    versionRange="[${neo_version},)" #mandatory
    # An ordering relationship for the dependency.
    # BEFORE - This mod is loaded BEFORE the dependency
    # AFTER - This mod is loaded AFTER the dependency
    ordering="NONE"
    # Side this dependency is applied on - BOTH, CLIENT, or SERVER
    side="BOTH"

# Here's another dependency
[[dependencies.${mod_id}]]
    modId="minecraft"
    type="required"
    # This version range declares a minimum of the current minecraft version up to but not including the next major version
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"

# Features are specific properties of the game environment, that you may want to declare you require. This example declares
# that your mod requires GL version 3.2 or higher. Other features will be added. They are side aware so declaring this won't
# stop your mod loading on the server for example.
#[features.${mod_id}]
#openGLVersion="[3.2,)"
```

## FILE: src/main/java/com/dorohedoro/hole/datagen/HoleBiomeData.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.worldgen.HoleBiomes;
import java.util.function.Consumer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** The Hole's biomes. They differ in air, water and what hunts there, and all carry the full vanilla ore table. */
final class HoleBiomeData {
    private HoleBiomeData() {}

    private record Air(int fog, int sky, int water, int waterFog, int grass, int foliage, float ash) {}

    private static final Air CITY = new Air(0x59665A, 0x47544C, 0x46604A, 0x202C20, 0x6B7443, 0x5A6438, 0.0025F);
    private static final Air SOOT = new Air(0x4C4F45, 0x3A3D36, 0x4A4F3C, 0x1C1E16, 0x5F5F3A, 0x505030, 0.0070F);
    private static final Air BAZAAR = new Air(0x6A6650, 0x55564A, 0x46604A, 0x202C20, 0x72763F, 0x62683A, 0.0015F);
    private static final Air BRACKISH = new Air(0x55685F, 0x46574F, 0x3D6650, 0x162A20, 0x5E7346, 0x4C6039, 0.0020F);
    private static final Air BLIGHT = new Air(0x5B5A49, 0x484A3D, 0x535A3D, 0x22261A, 0x777044, 0x6A6238, 0.0040F);
    private static final Air GRAVE = new Air(0x4F5752, 0x3C4440, 0x3E5247, 0x1A2420, 0x4F6048, 0x44523D, 0.0030F);
    private static final Air BELOW = new Air(0x2E3430, 0x262B28, 0x35503F, 0x121C14, 0x4F6048, 0x44523D, 0.0F);

    static void bootstrap(BootstrapContext<Biome> context) {
        context.register(HoleBiomes.RESIDENTIAL, biome(context, CITY, spawns -> hunters(spawns, 100, 100, 100, 25)));
        context.register(HoleBiomes.MARKET, biome(context, BAZAAR, spawns -> hunters(spawns, 70, 60, 50, 10)));
        context.register(HoleBiomes.INDUSTRIAL, biome(context, SOOT, spawns -> {
            hunters(spawns, 80, 110, 140, 30);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SILVERFISH, 12, 1, 3));
        }));
        context.register(HoleBiomes.WATERFRONT, biome(context, BRACKISH, spawns -> {
            hunters(spawns, 90, 70, 70, 15);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 40, 1, 2));
            spawns.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 3, 1, 2));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 12, 2, 4));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 8, 1, 3));
        }));
        context.register(HoleBiomes.DERELICT, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 110, 100, 90, 35);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 6, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 6, 2, 3));
        }));
        context.register(HoleBiomes.GRAVEYARD, biome(context, GRAVE, spawns -> {
            hunters(spawns, 150, 150, 40, 10);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 30, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 1, 2));
        }));
        context.register(HoleBiomes.OUTSKIRTS, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 100, 90, 110, 35);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 3, 1, 2));
        }));
        context.register(HoleBiomes.UNDERCITY, biome(context, BELOW, spawns -> {
            hunters(spawns, 95, 100, 100, 60);
            spawns.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 2, 4));
            spawns.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 4, 2, 3));
        }));
    }

    /**
     * The shared monster table. Creepers are rare and witches, slimes and endermen stay present so their drops stay
     * obtainable: redstone-brewing ingredients, slime balls and ender pearls all come from here.
     */
    private static void hunters(MobSpawnSettings.Builder spawns, int zombies, int skeletons, int spiders, int creepers) {
        BiomeDefaultFeatures.caveSpawns(spawns);
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, zombies, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, skeletons, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, spiders, 2, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, creepers, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 60, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    private static Biome biome(BootstrapContext<Biome> context, Air air, Consumer<MobSpawnSettings.Builder> spawnList) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawnList.accept(spawns);

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(air.fog)
                .skyColor(air.sky)
                .waterColor(air.water)
                .waterFogColor(air.waterFog)
                .grassColorOverride(air.grass)
                .foliageColorOverride(air.foliage)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        if (air.ash > 0.0F) {
            effects.ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, air.ash));
        }

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.7F)
                .downfall(0.6F)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation(context).build())
                .build();
    }

    /**
     * Follows {@code OverworldBiomes.globalOverworldGeneration} and vanilla's mountain biomes in the same order, because
     * the feature sorter crashes on any ordering conflict with the vanilla cave biomes this world also uses. Carvers and
     * surface lakes are omitted because they would cut the city, and water springs because they could flood basements.
     */
    private static BiomeGenerationSettings.Builder generation(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        generation.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addExtraEmeralds(generation);
        BiomeDefaultFeatures.addInfestedStone(generation);
        return generation;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/datagen/ModDataGen.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.worldgen.HoleBiomes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Wires up {@code runData}. Output lands in {@code src/generated/resources}. */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class ModDataGen {
    private ModDataGen() {}

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        RegistrySetBuilder registries = new RegistrySetBuilder().add(Registries.BIOME, HoleBiomeData::bootstrap);
        DatapackBuiltinEntriesProvider entries =
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), registries, Set.of(Dorohedoro.MOD_ID));
        generator.addProvider(event.includeServer(), entries);
        generator.addProvider(event.includeServer(), ModLoot.provider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ModRecipes(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new HoleBlockTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModModels.Blocks(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModModels.Items(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(),
                new HoleBiomeTags(output, entries.getRegistryProvider(), event.getExistingFileHelper()));
    }

    private static final class HoleBlockTags extends BlockTagsProvider {
        HoleBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.INDUSTRIAL_DEBRIS.get(), ModBlocks.RUSTED_PLATING.get());
        }
    }

    /** Hole biomes join the vanilla tags that place strongholds, mineshafts and trial chambers. */
    private static final class HoleBiomeTags extends BiomeTagsProvider {
        HoleBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BiomeTags.IS_OVERWORLD).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.UNDERCITY);
            tag(BiomeTags.STRONGHOLD_BIASED_TO).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.UNDERCITY);
            tag(BiomeTags.HAS_MINESHAFT).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.UNDERCITY);
            tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(HoleBiomes.UNDERCITY);
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/datagen/ModLoot.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Block drops and chest tables. Chests are how items with no other Hole source stay obtainable. */
final class ModLoot {
    private ModLoot() {}

    static LootTableProvider provider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockDrops::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(provider -> new Chests(), LootContextParamSets.CHEST)
        ), lookup);
    }

    private static LootPoolSingletonContainer.Builder<?> item(ItemLike item, int min, int max) {
        return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight, int min, int max) {
        return item(item, min, max).setWeight(weight);
    }

    private static final class BlockDrops extends BlockLootSubProvider {
        BlockDrops(HolderLookup.Provider lookup) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookup);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.RUSTED_PLATING.get());
            add(ModBlocks.INDUSTRIAL_DEBRIS.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(item(ModItems.SCRAP_METAL.get(), 1, 3)))
                    .withPool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.25F)).add(item(Items.IRON_NUGGET, 1, 2))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).toList();
        }
    }

    private static final class Chests implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out) {
            table(out, "starter_cache", 4, 6,
                    entry(Items.BREAD, 10, 2, 4), entry(Items.COOKED_COD, 6, 1, 3), entry(Items.APPLE, 6, 1, 3),
                    entry(Items.OAK_LOG, 10, 3, 6), entry(Items.OAK_SAPLING, 6, 1, 2), entry(Items.WHEAT_SEEDS, 6, 2, 4),
                    entry(Items.CARROT, 4, 1, 3), entry(Items.POTATO, 4, 1, 3), entry(Items.COAL, 8, 2, 5),
                    entry(Items.TORCH, 8, 4, 8), entry(Items.STONE_PICKAXE, 6, 1, 1), entry(Items.STONE_AXE, 4, 1, 1),
                    entry(Items.LEATHER, 4, 1, 3), entry(Items.WATER_BUCKET, 2, 1, 1));
            table(out, "apartment", 1, 4,
                    entry(Items.BREAD, 10, 1, 3), entry(Items.PAPER, 8, 1, 4), entry(Items.BOOK, 4, 1, 2), entry(Items.STRING, 8, 1, 3),
                    entry(Items.STICK, 10, 2, 5), entry(Items.COAL, 6, 1, 3), entry(Items.BONE_MEAL, 4, 1, 3), entry(Items.LEATHER_BOOTS, 2, 1, 1),
                    entry(Items.CANDLE, 4, 1, 2), entry(Items.GLASS_BOTTLE, 4, 1, 2), entry(Items.BOWL, 4, 1, 2), entry(Items.IRON_NUGGET, 4, 1, 4));
            table(out, "shop", 2, 5,
                    entry(Items.EMERALD, 4, 1, 2), entry(Items.PAPER, 8, 2, 5), entry(Items.INK_SAC, 4, 1, 2), entry(Items.WHITE_WOOL, 6, 1, 3),
                    entry(Items.LEATHER, 6, 1, 3), entry(Items.IRON_INGOT, 3, 1, 2), entry(Items.COPPER_INGOT, 5, 1, 3), entry(Items.GLASS_BOTTLE, 6, 1, 3),
                    entry(Items.NAME_TAG, 1, 1, 1), entry(Items.LEAD, 2, 1, 1), entry(Items.BUCKET, 2, 1, 1), entry(Items.SHEARS, 2, 1, 1));
            table(out, "market", 3, 6,
                    entry(Items.WHEAT, 8, 2, 5), entry(Items.CARROT, 8, 2, 4), entry(Items.POTATO, 8, 2, 4), entry(Items.BEETROOT, 6, 2, 4),
                    entry(Items.BEEF, 5, 1, 3), entry(Items.PORKCHOP, 5, 1, 3), entry(Items.CHICKEN, 5, 1, 3), entry(Items.COD, 5, 1, 3),
                    entry(Items.EGG, 5, 1, 3), entry(Items.MILK_BUCKET, 2, 1, 1), entry(Items.SUGAR_CANE, 5, 1, 3), entry(Items.HONEY_BOTTLE, 2, 1, 1),
                    entry(Items.PUMPKIN_SEEDS, 3, 1, 2), entry(Items.MELON_SEEDS, 3, 1, 2), entry(Items.BROWN_MUSHROOM, 4, 1, 2), entry(Items.RED_MUSHROOM, 4, 1, 2),
                    entry(Items.SWEET_BERRIES, 3, 1, 3), entry(Items.COCOA_BEANS, 2, 1, 2), entry(Items.BAMBOO, 2, 1, 2), entry(Items.CACTUS, 2, 1, 1));
            table(out, "clinic", 2, 4,
                    entry(Items.GLASS_BOTTLE, 8, 1, 3), entry(Items.PAPER, 6, 1, 3), entry(Items.SUGAR, 5, 1, 3), entry(Items.GLISTERING_MELON_SLICE, 2, 1, 1),
                    entry(Items.GOLDEN_APPLE, 1, 1, 1), entry(Items.SPIDER_EYE, 4, 1, 2), entry(Items.WHITE_WOOL, 5, 1, 2), entry(Items.HONEY_BOTTLE, 3, 1, 1));
            table(out, "guard", 2, 4,
                    entry(Items.ARROW, 8, 3, 8), entry(Items.IRON_SWORD, 2, 1, 1), entry(Items.SHIELD, 3, 1, 1), entry(Items.BOW, 3, 1, 1),
                    entry(Items.IRON_INGOT, 6, 1, 3), entry(Items.COOKED_BEEF, 8, 1, 3), entry(Items.GUNPOWDER, 3, 1, 2), entry(Items.SADDLE, 1, 1, 1));
            table(out, "warehouse", 3, 6,
                    entry(Items.OAK_PLANKS, 10, 4, 8), entry(Items.OAK_LOG, 8, 2, 6), entry(Items.SPRUCE_LOG, 6, 2, 6), entry(Items.SAND, 6, 3, 8),
                    entry(Items.GRAVEL, 6, 3, 8), entry(Items.CLAY_BALL, 5, 3, 8), entry(Items.GLASS, 5, 2, 6), entry(Items.RAIL, 5, 3, 8),
                    entry(Items.MINECART, 2, 1, 1), entry(Items.HOPPER, 2, 1, 1), entry(Items.REDSTONE, 4, 2, 6), entry(Items.PAPER, 4, 2, 6));
            table(out, "factory", 3, 5,
                    entry(ModItems.SCRAP_METAL.get(), 12, 2, 6), entry(Items.COAL, 10, 3, 8), entry(Items.IRON_NUGGET, 8, 2, 6), entry(Items.IRON_INGOT, 4, 1, 3),
                    entry(Items.COPPER_INGOT, 6, 1, 4), entry(Items.REDSTONE, 5, 2, 5), entry(Items.PISTON, 2, 1, 1), entry(Items.BUCKET, 3, 1, 1));
            table(out, "scrap", 2, 4,
                    entry(ModItems.SCRAP_METAL.get(), 12, 2, 5), entry(Items.IRON_NUGGET, 8, 1, 4), entry(Items.STRING, 6, 1, 3), entry(Items.ROTTEN_FLESH, 5, 1, 3),
                    entry(Items.BONE, 5, 1, 3), entry(Items.FLINT, 6, 1, 3), entry(Items.LEATHER, 3, 1, 2), entry(Items.NAME_TAG, 1, 1, 1));
            table(out, "nursery", 3, 5,
                    entry(Items.OAK_SAPLING, 6, 1, 3), entry(Items.BIRCH_SAPLING, 5, 1, 3), entry(Items.SPRUCE_SAPLING, 5, 1, 3), entry(Items.JUNGLE_SAPLING, 3, 1, 2),
                    entry(Items.ACACIA_SAPLING, 3, 1, 2), entry(Items.DARK_OAK_SAPLING, 3, 1, 4), entry(Items.WHEAT_SEEDS, 8, 2, 5), entry(Items.BEETROOT_SEEDS, 6, 2, 4),
                    entry(Items.BONE_MEAL, 6, 2, 5), entry(Items.DIRT, 6, 3, 8), entry(Items.APPLE, 4, 1, 2), entry(Items.HONEYCOMB, 2, 1, 2));
            table(out, "hungry_bug", 4, 7,
                    entry(Items.BREAD, 10, 2, 4), entry(Items.COOKED_PORKCHOP, 8, 1, 3), entry(Items.COOKED_CHICKEN, 8, 1, 3), entry(Items.COOKED_BEEF, 6, 1, 3),
                    entry(Items.BAKED_POTATO, 8, 2, 4), entry(Items.WHEAT, 8, 2, 5), entry(Items.CARROT, 6, 2, 4), entry(Items.EGG, 4, 1, 3));
            table(out, "crypt", 2, 4,
                    entry(Items.BONE, 10, 2, 5), entry(Items.ROTTEN_FLESH, 8, 2, 4), entry(Items.GOLD_NUGGET, 6, 1, 5), entry(Items.CANDLE, 5, 1, 2),
                    entry(Items.GOLD_INGOT, 2, 1, 1), entry(Items.MUSIC_DISC_13, 1, 1, 1), entry(Items.EMERALD, 2, 1, 1));
            table(out, "hospital", 3, 6,
                    entry(Items.GLASS_BOTTLE, 8, 2, 4), entry(Items.GOLDEN_APPLE, 3, 1, 1), entry(Items.GLISTERING_MELON_SLICE, 4, 1, 2), entry(Items.GOLDEN_CARROT, 4, 1, 2),
                    entry(Items.PAPER, 6, 2, 5), entry(Items.BREWING_STAND, 2, 1, 1), entry(Items.TOTEM_OF_UNDYING, 1, 1, 1));
            table(out, "department", 3, 6,
                    entry(Items.LEATHER_CHESTPLATE, 5, 1, 1), entry(Items.IRON_HELMET, 2, 1, 1), entry(Items.CLOCK, 3, 1, 1), entry(Items.COMPASS, 3, 1, 1),
                    entry(Items.MAP, 3, 1, 1), entry(Items.SPYGLASS, 2, 1, 1), entry(Items.DIAMOND, 1, 1, 1), entry(Items.EMERALD, 4, 1, 3),
                    entry(Items.BOOK, 5, 1, 3), entry(Items.WHITE_BANNER, 3, 1, 1), entry(Items.TOTEM_OF_UNDYING, 1, 1, 1), entry(Items.MUSIC_DISC_CAT, 1, 1, 1));
            table(out, "lab", 3, 5,
                    entry(Items.GLASS_BOTTLE, 8, 1, 3), entry(Items.BLAZE_POWDER, 2, 1, 1), entry(Items.NETHER_WART, 3, 1, 2), entry(Items.SPIDER_EYE, 5, 1, 2),
                    entry(Items.FERMENTED_SPIDER_EYE, 3, 1, 1), entry(Items.ENDER_PEARL, 1, 1, 1), entry(Items.GHAST_TEAR, 1, 1, 1), entry(Items.BOOK, 5, 1, 3));
        }

        @SafeVarargs
        private static void table(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out, String name, int minRolls, int maxRolls,
                                  LootPoolSingletonContainer.Builder<?>... entries) {
            LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(minRolls, maxRolls));
            for (LootPoolSingletonContainer.Builder<?> entry : entries) {
                pool.add(entry);
            }
            out.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/" + name)),
                    LootTable.lootTable().withPool(pool));
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/datagen/ModModels.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModModels {
    private ModModels() {}

    static final class Blocks extends BlockStateProvider {
        Blocks(PackOutput output, ExistingFileHelper files) {
            super(output, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void registerStatesAndModels() {
            simpleBlockWithItem(ModBlocks.INDUSTRIAL_DEBRIS.get(), cubeAll(ModBlocks.INDUSTRIAL_DEBRIS.get()));
            simpleBlockWithItem(ModBlocks.RUSTED_PLATING.get(), cubeAll(ModBlocks.RUSTED_PLATING.get()));
        }
    }

    static final class Items extends ItemModelProvider {
        Items(PackOutput output, ExistingFileHelper files) {
            super(output, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void registerModels() {
            basicItem(ModItems.SCRAP_METAL.get());
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/datagen/ModRecipes.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Salvage recipes. They give a scavenger a route to iron without ever finding an ore vein. */
final class ModRecipes extends RecipeProvider {
    ModRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.IRON_INGOT)
                .requires(ModItems.SCRAP_METAL.get(), 9)
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output, id("iron_ingot_from_scrap"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUSTED_PLATING.get(), 2)
                .pattern("SS").pattern("SS")
                .define('S', ModItems.SCRAP_METAL.get())
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SCRAP_METAL.get()), RecipeCategory.MISC, Items.IRON_NUGGET, 0.1F, 200)
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output, id("iron_nugget_from_smelting_scrap"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/Dorohedoro.java

```java
package com.dorohedoro.hole;

import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import com.dorohedoro.hole.registry.ModWorldgen;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** Mod entry point. Registers content on the mod bus; game logic lives in feature packages. */
@Mod(Dorohedoro.MOD_ID)
public final class Dorohedoro {
    public static final String MOD_ID = "dorohedoro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Dorohedoro(IEventBus modBus, ModContainer container) {
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModWorldgen.register(modBus);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/qa/HoleQa.java

```java
package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.District;
import com.dorohedoro.hole.worldgen.city.Lot;
import com.dorohedoro.hole.worldgen.city.LotType;
import com.dorohedoro.hole.worldgen.city.Rect;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Headless world validation. When started with {@code -Ddorohedoro.qa=true} on a dedicated server it generates a region
 * around spawn, measures it, writes {@code qa/report.json} and {@code qa/map.png} under the server directory, then stops
 * the server. Nothing runs without that property, so this has no effect on normal play.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class HoleQa {
    private static final String PROPERTY = "dorohedoro.qa";
    private static final int CITY_LAYER_BOTTOM = CityPlan.STREET_Y;

    private HoleQa() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }
        MinecraftServer server = event.getServer();
        try {
            new Run(server, Integer.getInteger(PROPERTY + ".radius", 16), System.getProperty(PROPERTY + ".order", "forward")).execute();
        } catch (Exception e) {
            Dorohedoro.LOGGER.error("QA run failed", e);
        } finally {
            server.halt(false);
        }
    }

    private static final class Run {
        private final MinecraftServer server;
        private final ServerLevel level;
        private final int radius;
        private final String order;
        private final JsonObject report = new JsonObject();
        private final JsonArray checks = new JsonArray();
        private int failures;

        Run(MinecraftServer server, int radius, String order) {
            this.server = server;
            this.level = server.overworld();
            this.radius = radius;
            this.order = order;
        }

        void execute() throws IOException {
            report.addProperty("seed", level.getSeed());
            report.addProperty("radius_chunks", radius);
            report.addProperty("order", order);
            boolean isHole = level.getChunkSource().getGenerator() instanceof HoleChunkGenerator;
            check("generator_is_hole", isHole, level.getChunkSource().getGenerator().getClass().getSimpleName());
            if (!isHole) {
                write(null);
                return;
            }
            HoleChunkGenerator generator = (HoleChunkGenerator) level.getChunkSource().getGenerator();
            CityPlan plan = generator.plan(level);

            List<ChunkPos> positions = positions();
            long start = System.nanoTime();
            JsonObject hashes = new JsonObject();
            JsonObject fullHashes = new JsonObject();
            Map<ChunkPos, LevelChunk> chunks = new HashMap<>();
            for (ChunkPos pos : positions) {
                chunks.put(pos, level.getChunk(pos.x, pos.z));
            }
            double seconds = (System.nanoTime() - start) / 1.0e9;
            report.addProperty("generation_seconds", seconds);
            report.addProperty("chunks", positions.size());
            report.addProperty("ms_per_chunk", seconds * 1000.0 / positions.size());

            List<ChunkPos> sorted = new ArrayList<>(positions);
            sorted.sort((a, b) -> a.x != b.x ? Integer.compare(a.x, b.x) : Integer.compare(a.z, b.z));
            for (ChunkPos pos : sorted) {
                hashes.addProperty(pos.x + "," + pos.z, Long.toHexString(hash(chunks.get(pos), CITY_LAYER_BOTTOM)));
                fullHashes.addProperty(pos.x + "," + pos.z, Long.toHexString(hash(chunks.get(pos), Integer.MIN_VALUE)));
            }
            report.add("chunk_hashes", hashes);
            report.add("chunk_hashes_full", fullHashes);

            dumpChunks(chunks);
            checkSpawn(plan);
            checkBiomes();
            Rect area = new Rect(-radius * 16, -radius * 16, radius * 16 + 15, radius * 16 + 15);
            checkConnectivity(plan, area);
            census(chunks, area, plan);
            checkFluids(chunks);
            checkCityMaterials(chunks);
            write(area);
        }

        /** Generation order is the point of the determinism test: the same chunks are requested in opposite orders. */
        private List<ChunkPos> positions() {
            List<ChunkPos> list = new ArrayList<>();
            for (int ring = 0; ring <= radius; ring++) {
                for (int x = -ring; x <= ring; x++) {
                    for (int z = -ring; z <= ring; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) == ring) {
                            list.add(new ChunkPos(x, z));
                        }
                    }
                }
            }
            if ("reverse".equals(order)) {
                Collections.reverse(list);
            }
            return list;
        }

        /**
         * Hashes block states from {@code bottom} upward. The city layer is hashed apart from the deep underground
         * because that is the part this mod's generator owns: vanilla feature placement can vary slightly with order.
         */
        private static long hash(LevelChunk chunk, int bottom) {
            long hash = 1125899906842597L;
            LevelChunkSection[] sections = chunk.getSections();
            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                if (section.hasOnlyAir()) {
                    hash = hash * 31 + 7;
                    continue;
                }
                for (int y = 0; y < 16; y++) {
                    if (baseY + y < bottom) {
                        continue;
                    }
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            hash = hash * 31 + Block.getId(section.getBlockState(x, y, z));
                        }
                    }
                }
            }
            return hash;
        }

        /** Writes every block of the chunks named by {@code dorohedoro.qa.dump} ("x,z;x,z"), for diffing two runs. */
        private void dumpChunks(Map<ChunkPos, LevelChunk> chunks) throws IOException {
            String wanted = System.getProperty(PROPERTY + ".dump", "");
            if (wanted.isEmpty()) {
                return;
            }
            Path directory = server.getServerDirectory().resolve("qa");
            Files.createDirectories(directory);
            for (String entry : wanted.split(";")) {
                String[] parts = entry.split(",");
                ChunkPos pos = new ChunkPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                LevelChunk chunk = chunks.get(pos);
                StringBuilder out = new StringBuilder();
                for (int y = level.getMinBuildHeight(); y < 140; y++) {
                    for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                        for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                            out.append(x).append(' ').append(y).append(' ').append(z).append(' ')
                                    .append(chunk.getBlockState(new BlockPos(x, y, z))).append(System.lineSeparator());
                        }
                    }
                }
                Files.writeString(directory.resolve("dump-" + order + "-" + pos.x + "_" + pos.z + ".txt"), out.toString());
            }
        }

        private void checkSpawn(CityPlan plan) {
            BlockPos spawn = level.getSharedSpawnPos();
            report.addProperty("spawn", spawn.toShortString());
            check("spawn_at_plaza", Math.abs(spawn.getX()) <= 13 && Math.abs(spawn.getZ()) <= 13 && spawn.getY() == CityPlan.GROUND_Y, spawn.toShortString());
            boolean feet = level.getBlockState(spawn).isAir();
            boolean head = level.getBlockState(spawn.above()).isAir();
            boolean floor = level.getBlockState(spawn.below()).isFaceSturdy(level, spawn.below(), net.minecraft.core.Direction.UP);
            check("spawn_standable", feet && head && floor, "feet=" + feet + " head=" + head + " floor=" + floor);
            boolean roofless = level.canSeeSky(spawn);
            check("spawn_open_sky", roofless, String.valueOf(roofless));
        }

        private void checkBiomes() {
            Set<ResourceLocation> surface = new HashSet<>();
            int span = radius * 16;
            for (int x = -span; x <= span; x += 8) {
                for (int z = -span; z <= span; z += 8) {
                    level.getBiome(new BlockPos(x, CityPlan.GROUND_Y, z)).unwrapKey().ifPresent(key -> surface.add(key.location()));
                }
            }
            JsonArray seen = new JsonArray();
            boolean allHole = true;
            for (ResourceLocation id : new java.util.TreeSet<>(surface)) {
                seen.add(id.toString());
                allHole &= id.getNamespace().equals(Dorohedoro.MOD_ID);
            }
            report.add("surface_biomes", seen);
            check("no_vanilla_surface_biome", allHole, seen.toString());
        }

        private boolean walkable(int x, int y, int z) {
            BlockState feet = level.getBlockState(new BlockPos(x, y, z));
            BlockState head = level.getBlockState(new BlockPos(x, y + 1, z));
            BlockPos below = new BlockPos(x, y - 1, z);
            BlockState ground = level.getBlockState(below);
            return passable(feet) && passable(head) && !ground.getCollisionShape(level, below).isEmpty() && ground.getFluidState().isEmpty();
        }

        private static boolean passable(BlockState state) {
            return (state.isAir() || state.is(BlockTags.RAILS) || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS)
                    || state.is(BlockTags.WOOL_CARPETS) || state.is(Blocks.LADDER) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS)
                    || state.is(BlockTags.CROPS) || state.is(Blocks.SHORT_GRASS)) && state.getFluidState().isEmpty();
        }

        /**
         * Flood-fills on foot from spawn across street level, stepping up or down one block at a time. A street column
         * the fill never reaches is a sealed road, a broken crossing or a seam.
         */
        private void checkConnectivity(CityPlan plan, Rect area) {
            int width = area.width();
            BitSet visited = new BitSet(width * area.depth());
            ArrayDeque<long[]> queue = new ArrayDeque<>();
            BlockPos spawn = level.getSharedSpawnPos();
            queue.add(new long[]{spawn.getX(), spawn.getY(), spawn.getZ()});
            visited.set(columnIndex(area, spawn.getX(), spawn.getZ()));
            int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            while (!queue.isEmpty()) {
                long[] here = queue.poll();
                for (int[] step : steps) {
                    int x = (int) here[0] + step[0];
                    int z = (int) here[2] + step[1];
                    if (!area.contains(x, z)) {
                        continue;
                    }
                    int index = columnIndex(area, x, z);
                    if (visited.get(index)) {
                        continue;
                    }
                    int y = (int) here[1];
                    for (int candidate : new int[]{y, y + 1, y - 1}) {
                        if (candidate >= CityPlan.GROUND_Y - 1 && candidate <= CityPlan.GROUND_Y + 3 && walkable(x, candidate, z)) {
                            visited.set(index);
                            queue.add(new long[]{x, candidate, z});
                            break;
                        }
                    }
                }
            }

            Set<Long> lotColumns = new HashSet<>();
            int enclosedTotal = 0;
            int enclosedReached = 0;
            Map<LotType, Integer> lotTypes = new EnumMap<>(LotType.class);
            Map<District, Integer> districtBlocks = new EnumMap<>(District.class);
            Set<Long> seenLots = new HashSet<>();
            for (int cx = area.x0() >> 4; cx <= area.x1() >> 4; cx++) {
                for (int cz = area.z0() >> 4; cz <= area.z1() >> 4; cz++) {
                    for (Lot lot : plan.lotsIn(new ChunkPos(cx, cz))) {
                        if (!seenLots.add(lot.hash())) {
                            continue;
                        }
                        lotTypes.merge(lot.type(), 1, Integer::sum);
                        districtBlocks.merge(lot.district(), lot.rect().area(), Integer::sum);
                        Rect r = lot.rect();
                        for (int x = r.x0(); x <= r.x1(); x++) {
                            for (int z = r.z0(); z <= r.z1(); z++) {
                                lotColumns.add(((long) x << 32) | (z & 0xFFFFFFFFL));
                            }
                        }
                        boolean inside = r.x0() >= area.x0() && r.x1() <= area.x1() && r.z0() >= area.z0() && r.z1() <= area.z1();
                        if (lot.type().enclosed && inside) {
                            enclosedTotal++;
                            Rect interior = r.inset(2);
                            boolean reached = false;
                            for (int x = interior.x0(); x <= interior.x1() && !reached; x++) {
                                for (int z = interior.z0(); z <= interior.z1() && !reached; z++) {
                                    reached = visited.get(columnIndex(area, x, z));
                                }
                            }
                            if (reached) {
                                enclosedReached++;
                            }
                        }
                    }
                }
            }

            int street = 0;
            int reachedStreet = 0;
            JsonArray missed = new JsonArray();
            for (int x = area.x0(); x <= area.x1(); x++) {
                for (int z = area.z0(); z <= area.z1(); z++) {
                    if (lotColumns.contains(((long) x << 32) | (z & 0xFFFFFFFFL))) {
                        continue;
                    }
                    BlockState top = level.getBlockState(new BlockPos(x, CityPlan.STREET_Y, z));
                    if (!top.isSolidRender(level, new BlockPos(x, CityPlan.STREET_Y, z))) {
                        continue;
                    }
                    if (!walkable(x, CityPlan.GROUND_Y, z)) {
                        continue;
                    }
                    street++;
                    if (visited.get(columnIndex(area, x, z))) {
                        reachedStreet++;
                    } else if (missed.size() < 12) {
                        missed.add(x + "," + z);
                    }
                }
            }
            double fraction = street == 0 ? 0.0 : (double) reachedStreet / street;
            report.addProperty("street_columns", street);
            report.addProperty("street_reached_fraction", fraction);
            report.add("street_missed_sample", missed);
            check("streets_reachable_from_spawn", fraction >= 0.995, String.format("%.4f of %d", fraction, street));

            report.addProperty("enclosed_lots", enclosedTotal);
            report.addProperty("enclosed_lots_reached", enclosedReached);
            double enclosedFraction = enclosedTotal == 0 ? 0.0 : (double) enclosedReached / enclosedTotal;
            check("enclosed_lots_enterable", enclosedTotal > 0 && enclosedFraction >= 0.95, enclosedReached + "/" + enclosedTotal);

            JsonObject types = new JsonObject();
            lotTypes.forEach((type, count) -> types.addProperty(type.name(), count));
            report.add("lot_types", types);
            JsonObject districts = new JsonObject();
            districtBlocks.forEach((district, blocks) -> districts.addProperty(district.name(), blocks));
            report.add("district_blocks", districts);
            check("multiple_districts", districtBlocks.size() >= 3, districtBlocks.keySet().toString());
        }

        private static int columnIndex(Rect area, int x, int z) {
            return (x - area.x0()) * area.depth() + (z - area.z0());
        }

        /** Counts what survival needs, so a missing resource fails the run rather than surfacing hours into a playthrough. */
        private void census(Map<ChunkPos, LevelChunk> chunks, Rect area, CityPlan plan) {
            Map<Block, Integer> counts = new HashMap<>();
            for (LevelChunk chunk : chunks.values()) {
                for (LevelChunkSection section : chunk.getSections()) {
                    if (section.hasOnlyAir()) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                Block block = section.getBlockState(x, y, z).getBlock();
                                if (block != Blocks.AIR && block != Blocks.STONE && block != Blocks.DEEPSLATE) {
                                    counts.merge(block, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
            JsonObject census = new JsonObject();
            new TreeMap<>(Map.copyOf(names(counts))).forEach(census::addProperty);
            report.add("block_census", census);

            need(counts, "coal", 1, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
            need(counts, "iron", 1, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
            need(counts, "copper", 1, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
            need(counts, "gold", 1, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
            need(counts, "redstone", 1, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
            need(counts, "lapis", 1, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
            need(counts, "diamond", 1, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
            need(counts, "emerald", 1, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
            need(counts, "dirt", 1, Blocks.DIRT, Blocks.COARSE_DIRT);
            need(counts, "gravel", 1, Blocks.GRAVEL);
            need(counts, "sand", 1, Blocks.SAND);
            need(counts, "clay", 1, Blocks.CLAY);
            need(counts, "sugar_cane", 1, Blocks.SUGAR_CANE);
            need(counts, "water", 1, Blocks.WATER);
            need(counts, "lava", 1, Blocks.LAVA);
        }

        private void need(Map<Block, Integer> counts, String name, int minimum, Block... blocks) {
            int total = 0;
            for (Block block : blocks) {
                total += counts.getOrDefault(block, 0);
            }
            check("resource_" + name, total >= minimum, String.valueOf(total));
        }

        private static Map<String, Integer> names(Map<Block, Integer> counts) {
            Map<String, Integer> names = new HashMap<>();
            counts.forEach((block, count) -> names.put(BuiltInRegistries.BLOCK.getKey(block).toString(), count));
            return names;
        }

        /** Water that is not a source block above the sewers means a canal or pipe is leaking. */
        private void checkFluids(Map<ChunkPos, LevelChunk> chunks) {
            int flowing = 0;
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        for (int y = 46; y <= 66; y++) {
                            var fluid = chunk.getFluidState(x, y, z);
                            if (!fluid.isEmpty() && fluid.getType() != Fluids.WATER && fluid.getType() != Fluids.LAVA) {
                                flowing++;
                            }
                        }
                    }
                }
            }
            check("no_flowing_water_near_surface", flowing == 0, String.valueOf(flowing));
        }

        /** Ore or raw rock at street level or above means a vanilla feature wrote through the city. */
        private void checkCityMaterials(Map<ChunkPos, LevelChunk> chunks) {
            int ore = 0;
            int rock = 0;
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
                        for (int y = CityPlan.STREET_Y; y <= top; y++) {
                            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                            if (state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
                                ore++;
                            } else if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
                                rock++;
                            }
                        }
                    }
                }
            }
            check("no_ore_in_city", ore == 0, String.valueOf(ore));
            check("no_replaceable_rock_in_city", rock == 0, String.valueOf(rock));
        }

        private void check(String name, boolean passed, String detail) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", name);
            entry.addProperty("passed", passed);
            entry.addProperty("detail", detail);
            checks.add(entry);
            if (!passed) {
                failures++;
            }
            Dorohedoro.LOGGER.info("QA {} {}: {}", passed ? "PASS" : "FAIL", name, detail);
        }

        private void write(Rect area) throws IOException {
            Path directory = server.getServerDirectory().resolve("qa");
            Files.createDirectories(directory);
            report.addProperty("failures", failures);
            report.add("checks", checks);
            Files.writeString(directory.resolve("report-" + order + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
            if (area != null) {
                map(area, directory.resolve("map-" + order + ".png"));
            }
            Dorohedoro.LOGGER.info("QA complete: {} failure(s), report in {}", failures, directory.toAbsolutePath());
        }

        private void map(Rect area, Path file) throws IOException {
            BufferedImage image = new BufferedImage(area.width(), area.depth(), BufferedImage.TYPE_INT_RGB);
            for (int x = area.x0(); x <= area.x1(); x++) {
                for (int z = area.z0(); z <= area.z1(); z++) {
                    int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                    BlockPos pos = new BlockPos(x, top, z);
                    BlockState state = level.getBlockState(pos);
                    MapColor color = state.getMapColor(level, pos);
                    int shade = Math.min(255, 150 + (top - CityPlan.STREET_Y) * 3);
                    int rgb = color.col;
                    int r = ((rgb >> 16) & 255) * shade / 255;
                    int g = ((rgb >> 8) & 255) * shade / 255;
                    int b = (rgb & 255) * shade / 255;
                    image.setRGB(x - area.x0(), z - area.z0(), (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255));
                }
            }
            ImageIO.write(image, "png", file.toFile());
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/registry/ModBlocks.java

```java
package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Hole's own blocks. Each drops something useful, so salvage is a real early-game resource. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dorohedoro.MOD_ID);

    /** Heaps of machine scrap. The early-game source of metal, drops scrap and sometimes iron nuggets. */
    public static final DeferredBlock<Block> INDUSTRIAL_DEBRIS = register("industrial_debris", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.5F, 3.0F).sound(SoundType.CHAIN).requiresCorrectToolForDrops()));

    /** Corroded wall sheet used across the industrial districts. Weathered copper does not scavenge into anything useful. */
    public static final DeferredBlock<Block> RUSTED_PLATING = register("rusted_plating", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.5F, 5.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));

    private ModBlocks() {}

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> holder = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
        return holder;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/registry/ModItems.java

```java
package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dorohedoro.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dorohedoro.MOD_ID);

    /** Salvaged metal. Nine craft into an iron ingot, so scavenging alone can reach iron tools. */
    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.registerSimpleItem("scrap_metal");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("hole", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dorohedoro.hole"))
            .icon(() -> new ItemStack(SCRAP_METAL.get()))
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/registry/ModWorldgen.java

```java
package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleBiomeSource;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the generator and biome source codecs that world presets refer to. */
public final class ModWorldgen {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Dorohedoro.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Dorohedoro.MOD_ID);

    public static final Supplier<MapCodec<HoleChunkGenerator>> HOLE_GENERATOR =
            CHUNK_GENERATORS.register("hole", () -> HoleChunkGenerator.CODEC);
    public static final Supplier<MapCodec<HoleBiomeSource>> HOLE_BIOME_SOURCE =
            BIOME_SOURCES.register("hole_districts", () -> HoleBiomeSource.CODEC);

    private ModWorldgen() {}

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Canvas.java

```java
package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Draws into one chunk, silently dropping anything outside it. A structure drawn through a canvas produces the same
 * result however it is split across chunks, so generators can place blocks in absolute coordinates and never think
 * about borders. Variation must come from {@link #hash}, never from a stateful random, for that to stay true.
 */
public final class Canvas {
    private final ChunkAccess chunk;
    private final long seed;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    public final int minX;
    public final int minZ;
    public final int maxX;
    public final int maxZ;

    public Canvas(ChunkAccess chunk, long seed) {
        this.chunk = chunk;
        this.seed = seed;
        ChunkPos pos = chunk.getPos();
        this.minX = pos.getMinBlockX();
        this.minZ = pos.getMinBlockZ();
        this.maxX = pos.getMaxBlockX();
        this.maxZ = pos.getMaxBlockZ();
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(Rect rect) {
        return rect.x0() <= maxX && rect.x1() >= minX && rect.z0() <= maxZ && rect.z1() >= minZ;
    }

    public long hash(int x, int y, int z) {
        return CityHash.of(seed, x, y, z);
    }

    public void set(int x, int y, int z, BlockState state) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), state, false);
        if (state.hasBlockEntity()) {
            CompoundTag tag = positioned(x, y, z);
            tag.putString("id", "DUMMY");
            chunk.setBlockEntityNbt(tag);
        }
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public void set(int x, int y, int z, Mix mix) {
        if (contains(x, z)) {
            set(x, y, z, mix.pick(hash(x, y, z)));
        }
    }

    public BlockState get(int x, int y, int z) {
        return chunk.getBlockState(cursor.set(x, y, z));
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Mix mix) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, mix.pick(hash(x, y, z)));
                }
            }
        }
    }

    public void fill(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    public void fill(Rect rect, int y0, int y1, Block block) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), block);
    }

    public void clear(Rect rect, int y0, int y1) {
        fill(rect, y0, y1, Blocks.AIR);
    }

    /** The four outer walls of a rectangle. */
    public void walls(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z0(), mix);
        fill(rect.x0(), y0, rect.z1(), rect.x1(), y1, rect.z1(), mix);
        fill(rect.x0(), y0, rect.z0(), rect.x0(), y1, rect.z1(), mix);
        fill(rect.x1(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    /** A pane or bar run along one axis, with each piece's connections set directly so no neighbour update is needed. */
    public void paneRun(int x0, int y0, int z0, int x1, int y1, int z1, Block pane) {
        boolean alongX = x1 != x0;
        boolean alongZ = z1 != z0;
        BlockState state = pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, alongZ)
                .setValue(CrossCollisionBlock.SOUTH, alongZ);
        fill(x0, y0, z0, x1, y1, z1, state);
    }

    /** A pane in a wall running along {@code axis}. */
    public void pane(int x, int y, int z, Direction.Axis axis, Block pane) {
        boolean alongX = axis == Direction.Axis.X;
        set(x, y, z, pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, !alongX)
                .setValue(CrossCollisionBlock.SOUTH, !alongX));
    }

    public void fenceRun(int x0, int y, int z0, int x1, int z1, Block fence) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                set(x, y, z, fence.defaultBlockState()
                        .setValue(CrossCollisionBlock.EAST, x < x1)
                        .setValue(CrossCollisionBlock.WEST, x > x0)
                        .setValue(CrossCollisionBlock.SOUTH, z < z1)
                        .setValue(CrossCollisionBlock.NORTH, z > z0));
            }
        }
    }

    /** A rectangular fence with a one-block gap at {@code (gapX, gapZ)}. */
    public void fenceRing(Rect rect, int y, Block fence, int gapX, int gapZ) {
        fenceRun(rect.x0(), y, rect.z0(), rect.x1(), rect.z0(), fence);
        fenceRun(rect.x0(), y, rect.z1(), rect.x1(), rect.z1(), fence);
        fenceRun(rect.x0(), y, rect.z0(), rect.x0(), rect.z1(), fence);
        fenceRun(rect.x1(), y, rect.z0(), rect.x1(), rect.z1(), fence);
        set(gapX, y, gapZ, Blocks.AIR);
    }

    public void door(int x, int y, int z, Direction facing, Block door) {
        BlockState lower = door.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        set(x, y, z, lower);
        set(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** A bed whose foot is at {@code (x, y, z)}, with the head one block toward {@code facing}. */
    public void bed(int x, int y, int z, Direction facing, Block bed) {
        BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
        set(x, y, z, foot);
        set(x + facing.getStepX(), y, z + facing.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    public void ladder(int x, int y0, int y1, int z, Direction facing) {
        BlockState state = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        for (int y = y0; y <= y1; y++) {
            set(x, y, z, state);
        }
    }

    public void stair(int x, int y, int z, Direction facing, Block stair) {
        set(x, y, z, stair.defaultBlockState().setValue(StairBlock.FACING, facing));
    }

    public void facing(int x, int y, int z, Direction facing, Block block) {
        set(x, y, z, block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
    }

    /** A chest that rolls {@code table} when a player first opens it. */
    public void chest(int x, int y, int z, Direction facing, String table) {
        container(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), "minecraft:chest", table);
    }

    public void barrel(int x, int y, int z, String table) {
        container(x, y, z, Blocks.BARREL.defaultBlockState(), "minecraft:barrel", table);
    }

    private void container(int x, int y, int z, BlockState state, String id, String table) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), state, false);
        CompoundTag tag = positioned(x, y, z);
        tag.putString("id", id);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/" + table));
        tag.putString("LootTable", key.location().toString());
        tag.putLong("LootTableSeed", hash(x, y, z));
        chunk.setBlockEntityNbt(tag);
    }

    /** A wall sign on the face of the block behind it, reading up to four lines. */
    public void sign(int x, int y, int z, Direction facing, Block wallSign, String... lines) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), wallSign.defaultBlockState().setValue(WallSignBlock.FACING, facing), false);
        ListTag messages = new ListTag();
        for (int i = 0; i < 4; i++) {
            String line = i < lines.length ? lines[i] : "";
            messages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line), net.minecraft.core.RegistryAccess.EMPTY)));
        }
        CompoundTag side = new CompoundTag();
        side.put("messages", messages);
        side.putString("color", "black");
        side.putBoolean("has_glowing_text", false);
        CompoundTag tag = positioned(x, y, z);
        tag.putString("id", "minecraft:sign");
        tag.put("front_text", side);
        tag.putBoolean("is_waxed", true);
        chunk.setBlockEntityNbt(tag);
    }

    private static CompoundTag positioned(int x, int y, int z) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java

```java
package com.dorohedoro.hole.worldgen.city;

/**
 * Pure coordinate hashing. Everything the city generator decides comes from these functions, which is what keeps a
 * chunk's contents independent of the order chunks are generated in.
 */
public final class CityHash {
    private CityHash() {}

    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static long of(long seed, long a) {
        return mix(seed + 0x9E3779B97F4A7C15L + a * 0xC2B2AE3D27D4EB4FL);
    }

    public static long of(long seed, long a, long b) {
        return mix(of(seed, a) + b * 0x165667B19E3779F9L);
    }

    public static long of(long seed, long a, long b, long c) {
        return mix(of(seed, a, b) + c * 0x27D4EB2F165667C5L);
    }

    /** Uniform integer in {@code [0, bound)}. */
    public static int range(long hash, int bound) {
        return (int) Long.remainderUnsigned(hash, bound);
    }

    /** Uniform float in {@code [0, 1)}. */
    public static float unit(long hash) {
        return (hash >>> 40) * (1.0F / (1L << 24));
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/CityPainter.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Turns the plan into blocks for a single chunk: underground levels first, then the ground and streets, then each lot.
 * It writes only inside the chunk and reads nothing but the plan.
 */
public final class CityPainter {
    private static final int FILL_BOTTOM = 56;

    private static final int SEWER_FLOOR = 49;
    private static final int SEWER_CEILING = 54;
    private static final int SEWER_SHELL_RADIUS = 4;
    private static final int SEWER_AIR_RADIUS = 3;

    private static final int CANAL_WATER_RADIUS = 6;
    private static final int CANAL_WATER_TOP = 61;
    private static final int CANAL_BED = 57;

    public static final int[] GALLERY_LEVELS = {12, -36};
    private static final int SHAFT_BOTTOM = GALLERY_LEVELS[GALLERY_LEVELS.length - 1];
    private static final int KIOSK_RADIUS = 3;

    // Nothing at street level or above may use stone, tuff, deepslate, andesite, granite or diorite. Vanilla ore and
    // rock features replace those blocks up to the build limit, and they would punch ore into the roads and buildings.
    private static final Mix ROAD = Mix.of(Blocks.GRAY_CONCRETE, 9, Blocks.CYAN_TERRACOTTA, 2, Blocks.POLISHED_TUFF, 2, Blocks.POLISHED_ANDESITE, 1, Blocks.COBBLED_DEEPSLATE, 1);
    private static final Mix LOCAL_ROAD = Mix.of(Blocks.COBBLED_DEEPSLATE, 4, Blocks.POLISHED_TUFF, 3, Blocks.GRAY_CONCRETE, 3, Blocks.PACKED_MUD, 2);
    private static final Mix SIDEWALK = Mix.of(Blocks.POLISHED_ANDESITE, 8, Blocks.STONE_BRICKS, 2, Blocks.CRACKED_STONE_BRICKS, 1);
    private static final Mix QUAY = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MOSSY_STONE_BRICKS, 2);
    private static final Mix ROAD_BED = Mix.of(Blocks.GRAVEL, 4, Blocks.DIRT, 4, Blocks.COARSE_DIRT, 2, Blocks.COBBLESTONE, 1, Blocks.SAND, 1);
    private static final Mix DEEP_FILL = Mix.of(Blocks.DIRT, 5, Blocks.GRAVEL, 4, Blocks.STONE, 5, Blocks.CLAY, 1, Blocks.SAND, 1);
    private static final Mix CANAL_MUD = Mix.of(Blocks.CLAY, 4, Blocks.DIRT, 2, Blocks.GRAVEL, 3, Blocks.SAND, 2);
    private static final Mix SEWER_WALL = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.MOSSY_STONE_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MUD_BRICKS, 1);
    private static final Mix SHAFT_WALL = Mix.of(Blocks.DEEPSLATE_BRICKS, 3, Blocks.STONE_BRICKS, 3, Blocks.CRACKED_DEEPSLATE_BRICKS, 1);
    private static final Mix KIOSK_WALL = Mix.of(Blocks.WAXED_WEATHERED_CUT_COPPER, 3, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.DEEPSLATE_BRICKS, 2);

    private CityPainter() {}

    public static void paint(CityPlan plan, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        Canvas canvas = new Canvas(chunk, plan.seed);
        List<Line> xLines = plan.xLinesNear(canvas.minX - 8, canvas.maxX + 8);
        List<Line> zLines = plan.zLinesNear(canvas.minZ - 8, canvas.maxZ + 8);

        paintGalleries(canvas, plan, xLines, zLines);
        paintSewers(canvas, xLines, zLines);
        paintGround(canvas, plan, xLines, zLines);
        paintCanals(canvas, xLines, zLines);
        for (Lot lot : plan.lotsIn(pos)) {
            LotPainter.paint(canvas, plan, lot);
        }
        paintShafts(canvas, plan, xLines, zLines);
    }

    /** Which arterial band, if any, holds a coordinate. */
    private static Line bandAt(List<Line> lines, int coordinate) {
        for (Line line : lines) {
            if (line.contains(coordinate)) {
                return line;
            }
        }
        return null;
    }

    private static void paintGround(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line xBand = bandAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line zBand = bandAt(zLines, z);
                c.fill(x, FILL_BOTTOM, z, x, STREET_Y - 5, z, DEEP_FILL);
                c.fill(x, STREET_Y - 4, z, x, STREET_Y - 1, z, ROAD_BED);

                if (xBand != null || zBand != null) {
                    paintArterialColumn(c, x, z, xBand, zBand);
                } else {
                    c.set(x, STREET_Y, z, LOCAL_ROAD);
                }
            }
        }
    }

    private static void paintArterialColumn(Canvas c, int x, int z, Line xBand, Line zBand) {
        int dx = xBand == null ? Integer.MAX_VALUE : Math.abs(x - xBand.center());
        int dz = zBand == null ? Integer.MAX_VALUE : Math.abs(z - zBand.center());
        boolean xCarriageway = xBand != null && dx <= curbOf(xBand);
        boolean zCarriageway = zBand != null && dz <= curbOf(zBand);

        if (xBand != null && zBand != null) {
            c.set(x, STREET_Y, z, ROAD);
            return;
        }
        if (xCarriageway || zCarriageway) {
            c.set(x, STREET_Y, z, ROAD);
            Line band = xCarriageway ? xBand : zBand;
            int offset = xCarriageway ? x - xBand.center() : z - zBand.center();
            if (band.kind() == Line.Kind.TRAM && (offset == -2 || offset == 2)) {
                BlockState rail = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE,
                        xCarriageway ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                c.set(x, GROUND_Y, z, rail);
            }
        } else {
            Line band = xBand != null ? xBand : zBand;
            c.set(x, STREET_Y, z, band.kind() == Line.Kind.CANAL ? QUAY : SIDEWALK);
            int edge = xBand != null ? dx : dz;
            int along = xBand != null ? z : x;
            if (edge == band.halfWidth() && Math.floorMod(along + band.center(), 15) == 0) {
                c.fill(x, GROUND_Y, z, x, GROUND_Y + 3, z, Blocks.SPRUCE_FENCE);
                c.set(x, GROUND_Y + 4, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
        }
    }

    private static int curbOf(Line band) {
        return band.kind() == Line.Kind.CANAL ? CANAL_WATER_RADIUS - 1 : band.halfWidth() - 2;
    }

    private static void paintCanals(Canvas c, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line xBand = bandAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line zBand = bandAt(zLines, z);
                boolean xCanal = xBand != null && xBand.kind() == Line.Kind.CANAL && Math.abs(x - xBand.center()) < CANAL_WATER_RADIUS;
                boolean zCanal = zBand != null && zBand.kind() == Line.Kind.CANAL && Math.abs(z - zBand.center()) < CANAL_WATER_RADIUS;
                if (!xCanal && !zCanal) {
                    continue;
                }
                // Where another arterial crosses, it carries on over a deck and the canal runs underneath.
                boolean bridged = (xCanal && zBand != null && !zCanal) || (zCanal && xBand != null && !xCanal);
                paintCanalColumn(c, x, z, bridged, xCanal ? z : x, xCanal ? Math.abs(x - xBand.center()) : Math.abs(z - zBand.center()));
            }
        }
    }

    private static void paintCanalColumn(Canvas c, int x, int z, boolean bridged, int along, int offset) {
        c.set(x, CANAL_BED, z, CANAL_MUD);
        c.fill(x, CANAL_BED + 1, z, x, CANAL_WATER_TOP, z, Blocks.WATER);
        c.fill(x, CANAL_WATER_TOP + 1, z, x, bridged ? STREET_Y - 2 : STREET_Y, z, Blocks.AIR);
        if (bridged) {
            c.set(x, STREET_Y - 1, z, Blocks.DEEPSLATE_TILES);
            return;
        }
        // Reed beds: a raised bank against the quay wall, which is where sugar cane can grow beside the water.
        int segment = Math.floorMod(along, 24);
        if (offset == CANAL_WATER_RADIUS - 1 && segment < 6) {
            c.set(x, CANAL_WATER_TOP, z, segment % 2 == 0 ? Blocks.SAND : Blocks.DIRT);
            c.fill(x, CANAL_WATER_TOP + 1, z, x, CANAL_WATER_TOP + 3, z, Blocks.SUGAR_CANE);
        }
    }

    private static void paintSewers(Canvas c, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            int dx = nearestOffset(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                int dz = nearestOffset(zLines, z);
                int d = Math.min(dx, dz);
                if (d > SEWER_SHELL_RADIUS) {
                    continue;
                }
                c.fill(x, SEWER_FLOOR - 2, z, x, SEWER_CEILING + 1, z, SEWER_WALL);
                if (d <= SEWER_AIR_RADIUS) {
                    c.fill(x, SEWER_FLOOR + 1, z, x, SEWER_CEILING - 1, z, Blocks.AIR);
                    if (d <= 1) {
                        c.set(x, SEWER_FLOOR, z, Blocks.AIR);
                        c.set(x, SEWER_FLOOR - 1, z, Blocks.WATER);
                    }
                }
            }
        }
    }

    private static int nearestOffset(List<Line> lines, int coordinate) {
        int best = Integer.MAX_VALUE;
        for (Line line : lines) {
            best = Math.min(best, Math.abs(coordinate - line.center()));
        }
        return best;
    }

    private static void paintGalleries(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int level : GALLERY_LEVELS) {
            for (int x = c.minX; x <= c.maxX; x++) {
                int dx = nearestOffset(xLines, x);
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    int dz = nearestOffset(zLines, z);
                    int d = Math.min(dx, dz);
                    if (d > 1) {
                        continue;
                    }
                    c.fill(x, level + 1, z, x, level + 3, z, Blocks.AIR);
                    c.set(x, level, z, level < 0 ? Blocks.COBBLED_DEEPSLATE : Blocks.COBBLESTONE);
                    int along = dx <= 1 ? z : x;
                    boolean crossing = dx <= 1 && dz <= 1;
                    if (!crossing && Math.floorMod(along, 8) == 0) {
                        if (d == 1) {
                            c.fill(x, level + 1, z, x, level + 2, z, Blocks.OAK_FENCE);
                        }
                        c.set(x, level + 3, z, Blocks.OAK_PLANKS);
                        if (d == 0 && Math.floorMod(along, 16) == 0) {
                            c.set(x, level + 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                        }
                    }
                    if (d == 0 && !crossing && Math.floorMod(along, 8) != 0) {
                        c.set(x, level + 1, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE,
                                dx == 0 ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST));
                    }
                }
            }
        }
    }

    /** Ladder shafts at selected crossings, joining the street, the sewers and both mine levels. */
    private static void paintShafts(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                if (!plan.hasShaft(xLine.index(), zLine.index())) {
                    continue;
                }
                if (xLine.kind() == Line.Kind.CANAL || zLine.kind() == Line.Kind.CANAL) {
                    continue;
                }
                paintShaft(c, xLine.center(), zLine.center());
            }
        }
    }

    private static void paintShaft(Canvas c, int cx, int cz) {
        Rect ring = new Rect(cx - 2, cz - 2, cx + 2, cz + 2);
        Rect inner = ring.inset(1);
        if (!c.overlaps(new Rect(cx - KIOSK_RADIUS, cz - KIOSK_RADIUS, cx + KIOSK_RADIUS, cz + KIOSK_RADIUS))) {
            return;
        }
        for (int y = SHAFT_BOTTOM + 1; y <= STREET_Y; y++) {
            boolean sewer = y > SEWER_FLOOR - 1 && y < SEWER_CEILING;
            boolean gallery = false;
            for (int level : GALLERY_LEVELS) {
                gallery |= y > level && y <= level + 3;
            }
            if (!sewer && !gallery) {
                c.walls(ring, y, y, SHAFT_WALL);
            }
            c.fill(inner, y, y, Blocks.AIR);
        }
        // The ladder keeps its own backing column so it survives where the shaft passes through open levels.
        c.fill(cx, SHAFT_BOTTOM, cz + 2, cx, STREET_Y - 1, cz + 2, SHAFT_WALL);
        c.ladder(cx, SHAFT_BOTTOM + 1, STREET_Y - 1, cz + 1, Direction.NORTH);
        c.fill(inner, SHAFT_BOTTOM, SHAFT_BOTTOM, Blocks.COBBLED_DEEPSLATE);

        // Street-level hatch house, an island the traffic lanes pass around.
        Rect kiosk = new Rect(cx - KIOSK_RADIUS, cz - KIOSK_RADIUS, cx + KIOSK_RADIUS, cz + KIOSK_RADIUS);
        c.clear(kiosk, GROUND_Y, GROUND_Y + 4);
        c.walls(kiosk, GROUND_Y, GROUND_Y + 3, KIOSK_WALL);
        c.fill(kiosk, GROUND_Y + 4, GROUND_Y + 4, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        c.fill(kiosk, STREET_Y, STREET_Y, Blocks.DEEPSLATE_TILES);
        // The floor is solid apart from one trapdoor over the ladder, so nobody walks into a hundred-block drop.
        c.set(cx, STREET_Y, cz + 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(TrapDoorBlock.HALF, Half.TOP));
        c.door(cx, GROUND_Y, cz - KIOSK_RADIUS, Direction.NORTH, Blocks.SPRUCE_DOOR);
        c.door(cx, GROUND_Y, cz + KIOSK_RADIUS, Direction.SOUTH, Blocks.SPRUCE_DOOR);
        c.door(cx - KIOSK_RADIUS, GROUND_Y, cz, Direction.WEST, Blocks.SPRUCE_DOOR);
        c.door(cx + KIOSK_RADIUS, GROUND_Y, cz, Direction.EAST, Blocks.SPRUCE_DOOR);
        c.set(cx, GROUND_Y + 3, cz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/CityPlan.java

```java
package com.dorohedoro.hole.worldgen.city;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;

/**
 * The deterministic city layout for one world.
 *
 * <p>Two families of arterials, one per axis, cut the plane into superblocks. Each superblock is subdivided by a binary
 * space partition: every cut leaves a street, so any lot touches a street on all four sides and every street joins an
 * arterial. Nothing here reads the world. Results depend only on the seed and coordinates, and the cache only saves
 * recomputation, so a chunk's content cannot depend on generation order.
 */
public final class CityPlan {
    /** Nominal distance between neighbouring arterials. */
    public static final int LINE_PITCH = 120;
    private static final int LINE_JITTER = 32;
    private static final int START_LINE_JITTER = 12;

    /** Top block of the road surface. Players walk at {@link #GROUND_Y}. */
    public static final int STREET_Y = 64;
    public static final int GROUND_Y = STREET_Y + 1;

    /** Landmark regions are this many superblocks across, which spaces landmarks apart by construction. */
    public static final int LANDMARK_REGION = 8;

    private static final long SALT_X = 0x51ED27L;
    private static final long SALT_Z = 0x2F0B1DL;
    private static final long SALT_BLOCK = 0x7A11E5L;
    private static final long SALT_SHAFT = 0x5A4F7DL;
    private static final long SALT_LANDMARK = 0x1A9D3AL;

    public static final Line.Kind[] KIND_ROLLS = {
            Line.Kind.AVENUE, Line.Kind.AVENUE, Line.Kind.AVENUE, Line.Kind.TRAM, Line.Kind.TRAM, Line.Kind.CANAL
    };

    public final long seed;
    public final CitySettings settings;
    private final Climate.Sampler climate;
    private final LoadingCache<Long, Superblock> superblocks;

    public CityPlan(long seed, Climate.Sampler climate, CitySettings settings) {
        this.seed = seed;
        this.climate = climate;
        this.settings = settings;
        this.superblocks = CacheBuilder.newBuilder().maximumSize(2048).build(new CacheLoader<>() {
            @Override
            public Superblock load(Long key) {
                return Superblock.build(CityPlan.this, (int) (key >> 32), key.intValue());
            }
        });
    }

    public Line xLine(int index) {
        return line(SALT_X, index);
    }

    public Line zLine(int index) {
        return line(SALT_Z, index);
    }

    private Line line(long salt, int index) {
        long hash = CityHash.of(seed, salt, index);
        // Lines -1 and 0 frame the starting quarter, which must keep its size and stay walkable on every seed.
        boolean startLine = index == -1 || index == 0;
        int reach = startLine ? START_LINE_JITTER : LINE_JITTER;
        int jitter = CityHash.range(hash, reach * 2 + 1) - reach;
        int center = index * LINE_PITCH + LINE_PITCH / 2 + jitter;
        Line.Kind kind = KIND_ROLLS[CityHash.range(hash >>> 16, KIND_ROLLS.length)];
        if (startLine && kind == Line.Kind.CANAL) {
            kind = Line.Kind.TRAM;
        }
        int halfWidth = switch (kind) {
            case AVENUE -> 6;
            case TRAM -> 7;
            case CANAL -> 9;
        };
        return new Line(index, center, halfWidth, kind);
    }

    /** Index of the superblock column containing {@code x}: the last line whose center is at or before it. */
    public int xIndex(int x) {
        return index(SALT_X, x);
    }

    public int zIndex(int z) {
        return index(SALT_Z, z);
    }

    private int index(long salt, int coordinate) {
        int guess = Math.floorDiv(coordinate - LINE_PITCH / 2, LINE_PITCH);
        for (int i = guess + 1; i >= guess - 1; i--) {
            if (line(salt, i).center() <= coordinate) {
                return i;
            }
        }
        return guess - 2;
    }

    /** Lines of one axis whose band could touch {@code [min, max]}. */
    public List<Line> xLinesNear(int min, int max) {
        return linesNear(SALT_X, min, max);
    }

    public List<Line> zLinesNear(int min, int max) {
        return linesNear(SALT_Z, min, max);
    }

    private List<Line> linesNear(long salt, int min, int max) {
        List<Line> lines = new ArrayList<>(3);
        int first = Math.floorDiv(min - LINE_PITCH / 2, LINE_PITCH) - 1;
        int last = Math.floorDiv(max - LINE_PITCH / 2, LINE_PITCH) + 2;
        for (int i = first; i <= last; i++) {
            Line line = line(salt, i);
            if (line.center() + line.halfWidth() >= min && line.center() - line.halfWidth() <= max) {
                lines.add(line);
            }
        }
        return lines;
    }

    public District districtAt(int x, int z) {
        return District.at(climate, x, z);
    }

    public Superblock superblock(int i, int j) {
        return superblocks.getUnchecked(((long) i << 32) | (j & 0xFFFFFFFFL));
    }

    public long blockHash(int i, int j) {
        return CityHash.of(seed, SALT_BLOCK, i, j);
    }

    /** All lots overlapping the chunk. */
    public List<Lot> lotsIn(ChunkPos chunk) {
        Rect area = new Rect(chunk.getMinBlockX(), chunk.getMinBlockZ(), chunk.getMaxBlockX(), chunk.getMaxBlockZ());
        List<Lot> lots = new ArrayList<>();
        int i0 = xIndex(area.x0());
        int i1 = xIndex(area.x1());
        int j0 = zIndex(area.z0());
        int j1 = zIndex(area.z1());
        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                for (Lot lot : superblock(i, j).lots()) {
                    if (lot.rect().intersects(area)) {
                        lots.add(lot);
                    }
                }
            }
        }
        return lots;
    }

    /** Whether the crossing of lines {@code i} and {@code j} carries a mine shaft to the underground levels. */
    public boolean hasShaft(int i, int j) {
        boolean atStart = (i == -1 || i == 0) && (j == -1 || j == 0);
        return atStart || CityHash.range(CityHash.of(seed, SALT_SHAFT, i, j), 3) == 0;
    }

    /** The superblock holding this landmark region's landmark. */
    public LandmarkSite landmarkSite(int regionX, int regionZ) {
        long hash = CityHash.of(seed, SALT_LANDMARK, regionX, regionZ);
        int blockI = regionX * LANDMARK_REGION + 1 + CityHash.range(hash, LANDMARK_REGION - 2);
        int blockJ = regionZ * LANDMARK_REGION + 1 + CityHash.range(hash >>> 12, LANDMARK_REGION - 2);
        LotType[] rotation = {LotType.HOSPITAL, LotType.DEPARTMENT_STORE, LotType.RESEARCH_MANOR, LotType.HOSPITAL,
                LotType.DEPARTMENT_STORE, LotType.LAKE_OF_REFUSE};
        return new LandmarkSite(blockI, blockJ, rotation[CityHash.range(hash >>> 24, rotation.length)]);
    }

    public record LandmarkSite(int blockI, int blockJ, LotType type) {}

    /** Where players first appear. The plaza at the origin exists on every seed. */
    public BlockPos spawnPoint() {
        return new BlockPos(0, GROUND_Y, 0);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/CityPopulator.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Adds a chunk's fixed residents. A lot populates only from the chunk holding its center, so it happens exactly once,
 * and counts are small and bounded so a dense district cannot overload entity ticking.
 */
public final class CityPopulator {
    private static final EntityType<?>[] LIVESTOCK = {EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN};

    private CityPopulator() {}

    public static void populate(WorldGenRegion region, CityPlan plan) {
        ChunkPos chunk = region.getCenter();
        for (Lot lot : plan.lotsIn(chunk)) {
            Rect r = lot.rect();
            if (r.centerX() >> 4 != chunk.x || r.centerZ() >> 4 != chunk.z) {
                continue;
            }
            switch (lot.type()) {
                case LIVESTOCK_PEN -> {
                    EntityType<?> first = LIVESTOCK[CityHash.range(lot.hash(), LIVESTOCK.length)];
                    EntityType<?> second = LIVESTOCK[CityHash.range(lot.hash() >>> 16, LIVESTOCK.length)];
                    spawn(region, first, r.centerX() - 1, r.centerZ());
                    spawn(region, first, r.centerX() + 1, r.centerZ());
                    spawn(region, second, r.centerX(), r.centerZ() - 1);
                    spawn(region, second, r.centerX(), r.centerZ() + 1);
                }
                case SHOP, CLINIC, MARKET_STALLS, STARTER_SHELTER, HUNGRY_BUG -> spawn(region, EntityType.VILLAGER, r.centerX(), r.centerZ());
                case GUARD_POST -> spawn(region, EntityType.IRON_GOLEM, r.centerX(), r.centerZ());
                case GREENHOUSE -> spawn(region, EntityType.BEE, r.centerX(), r.centerZ() - 1);
                case TENEMENT -> {
                    if (CityHash.range(lot.hash() >>> 40, 6) == 0) {
                        spawn(region, EntityType.CAT, r.centerX(), r.centerZ());
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void spawn(WorldGenRegion region, EntityType<?> type, int x, int z) {
        BlockPos pos = new BlockPos(x, GROUND_Y, z);
        if (!region.getBlockState(pos).isAir() || !region.getBlockState(pos.above()).isAir()) {
            return;
        }
        if (!(type.create(region.getLevel()) instanceof Mob mob)) {
            return;
        }
        mob.moveTo(x + 0.5, GROUND_Y, z + 0.5, 0.0F, 0.0F);
        mob.setPersistenceRequired();
        EventHooks.finalizeMobSpawn(mob, region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        region.addFreshEntityWithPassengers(mob);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/CitySettings.java

```java
package com.dorohedoro.hole.worldgen.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Per-world city tuning, stored with the chunk generator in level.dat. Keeping it there instead of in a config file
 * means editing a config can never put a seam between old and new chunks of an existing world.
 *
 * @param density   chance in {@code [0, 1]} that a lot holds a building rather than open ground
 * @param maxFloors tallest residential tower, in storeys
 */
public record CitySettings(float density, int maxFloors) {
    public static final CitySettings DEFAULT = new CitySettings(0.92F, 11);

    public static final Codec<CitySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("density", DEFAULT.density).forGetter(CitySettings::density),
            Codec.intRange(2, 16).optionalFieldOf("max_floors", DEFAULT.maxFloors).forGetter(CitySettings::maxFloors)
    ).apply(instance, CitySettings::new));
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/District.java

```java
package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * City districts. A district is decided per {@value #CELL_SIZE}-block cell from the world's seeded climate noise, so the
 * biome source and the street planner reach the same answer without sharing any state.
 */
public enum District {
    RESIDENTIAL,
    MARKET,
    INDUSTRIAL,
    WATERFRONT,
    DERELICT,
    GRAVEYARD,
    OUTSKIRTS;

    public static final int CELL_SIZE = 128;

    /**
     * The climate noise is tuned for continents. Sampling it at a higher frequency brings districts down to
     * neighbourhood size, so walking a few blocks changes the character of the city.
     */
    private static final int MIX_SCALE = 7;

    public static int cellOf(int block) {
        return Math.floorDiv(block, CELL_SIZE);
    }

    /** The block column at {@code (x, z)}'s district. */
    public static District at(Climate.Sampler sampler, int x, int z) {
        return ofCell(sampler, cellOf(x), cellOf(z));
    }

    public static District ofCell(Climate.Sampler sampler, int cellX, int cellZ) {
        // The four cells around the origin form the starting quarter on every seed.
        if (cellX >= -1 && cellX <= 0 && cellZ >= -1 && cellZ <= 0) {
            return MARKET;
        }
        int centerX = cellX * CELL_SIZE + CELL_SIZE / 2;
        int centerZ = cellZ * CELL_SIZE + CELL_SIZE / 2;
        // The unscaled noise picks out broad zones where the city thins into scrubland and yards.
        if (Math.abs(sampler.temperature().compute(new DensityFunction.SinglePointContext(centerX, 0, centerZ))) > 0.42) {
            return OUTSKIRTS;
        }
        DensityFunction.SinglePointContext mixed = new DensityFunction.SinglePointContext(centerX * MIX_SCALE, 0, centerZ * MIX_SCALE);
        return classify(sampler.temperature().compute(mixed), sampler.humidity().compute(mixed));
    }

    static District classify(double temperature, double humidity) {
        if (humidity > 0.34 && temperature > 0.06) {
            return GRAVEYARD;
        }
        if (humidity < -0.26) {
            return temperature < 0.0 ? WATERFRONT : DERELICT;
        }
        if (temperature > 0.16) {
            return INDUSTRIAL;
        }
        if (temperature < -0.20) {
            return MARKET;
        }
        return RESIDENTIAL;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Landmarks.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The starting quarter and the named places. Their layouts are original designs inspired by the source's locations. */
final class Landmarks {
    private static final BlockState POST_LANTERN = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);

    private Landmarks() {}

    /** The spawn square. Everything stays against the rim, because players land anywhere within ten blocks of the center. */
    static void plaza(Canvas c, Lot lot) {
        Rect r = lot.rect();
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.POLISHED_ANDESITE, 4, Blocks.STONE_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 1));
        c.fill(r.centerX() - 3, STREET_Y, r.centerZ() - 3, r.centerX() + 3, STREET_Y, r.centerZ() + 3, Blocks.CHISELED_STONE_BRICKS);
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int x = r.centerX() + i * 12;
                int z = r.centerZ() + j * 12;
                c.fill(x, GROUND_Y, z, x, GROUND_Y + 3, z, Blocks.SPRUCE_FENCE);
                c.set(x, GROUND_Y + 4, z, POST_LANTERN);
            }
        }
        int rim = r.z0();
        c.set(r.centerX() - 3, GROUND_Y, rim, Blocks.CRAFTING_TABLE);
        c.facing(r.centerX() - 2, GROUND_Y, rim, Direction.SOUTH, Blocks.FURNACE);
        c.chest(r.centerX() - 1, GROUND_Y, rim, Direction.SOUTH, "starter_cache");
        c.chest(r.centerX(), GROUND_Y, rim, Direction.SOUTH, "starter_cache");
        c.barrel(r.centerX() + 1, GROUND_Y, rim, "market");
        c.set(r.centerX() + 2, GROUND_Y, rim, Blocks.CRAFTING_TABLE);
        c.fill(r.centerX() + 3, GROUND_Y, rim, r.centerX() + 3, GROUND_Y + 1, rim, Blocks.SPRUCE_LOG);
        c.set(r.centerX() + 3, GROUND_Y + 2, rim, Blocks.SPRUCE_PLANKS);
        c.sign(r.centerX() + 3, GROUND_Y + 1, rim + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "THE HOLE", "Food: Hungry Bug", "north of here", "Mines: road hatches");
    }

    static void hungryBug(Canvas c, Lot lot) {
        Rect box = lot.rect();
        Style style = Style.HUNGRY_BUG;
        Tower.build(c, box, 2, style, lot.hash(), Tower.Roof.TANK, (cv, k, in, y) -> {
            if (k == 1) {
                LotPainter.dwelling(cv, style, k, in, y);
                return;
            }
            // Dining room to the south, kitchen behind a counter to the north.
            int counterZ = in.z0() + 5;
            cv.fill(in.x0(), y + 1, counterZ, in.x1() - 3, y + 1, counterZ, Blocks.SMOOTH_STONE);
            for (int x = in.x0() + 1; x <= in.x1() - 3; x += 3) {
                cv.facing(x, y + 1, in.z0() + 3, Direction.SOUTH, Blocks.SMOKER);
                cv.facing(x + 1, y + 1, in.z0() + 3, Direction.SOUTH, Blocks.FURNACE);
            }
            cv.barrel(in.x1(), y + 1, in.z0() + 2, "hungry_bug");
            cv.barrel(in.x1(), y + 1, in.z0() + 3, "hungry_bug");
            cv.chest(in.x1(), y + 1, in.z0() + 4, Direction.WEST, "hungry_bug");
            cv.set(in.x0(), y + 1, in.z0() + 3, Blocks.CAULDRON);
            cv.set(in.x1() - 1, y + 2, counterZ - 1, Blocks.CAKE);
            for (int x = in.x0() + 2; x <= in.x1() - 2; x += 6) {
                if (Math.abs(x - in.centerX()) <= 1) {
                    continue;
                }
                table(cv, x, y + 1, in.z1() - 3);
                Tower.lamp(cv, x, y, in.z1() - 3);
            }
            Tower.lamp(cv, in.centerX(), y, in.z0() + 3);
        });
        // The open-all-hours storefront: glass across the plaza side, a yellow sign band, and a green awning.
        int front = box.z1();
        for (int x = box.x0() + 2; x <= box.x1() - 2; x++) {
            if (Math.abs(x - box.centerX()) > 1) {
                c.pane(x, GROUND_Y + 1, front, Direction.Axis.X, Blocks.GLASS_PANE);
                c.pane(x, GROUND_Y + 2, front, Direction.Axis.X, Blocks.GLASS_PANE);
            }
        }
        c.fill(box.x0() + 1, GROUND_Y + 4, front, box.x1() - 1, GROUND_Y + 5, front, Blocks.YELLOW_TERRACOTTA);
        c.sign(box.centerX() - 1, GROUND_Y + 5, front + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "HUNGRY", "BUG");
        c.sign(box.centerX() + 1, GROUND_Y + 5, front + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "GYOZA", "24 OPEN");
        c.fill(box.x0() + 1, GROUND_Y + 3, front + 1, box.x1() - 1, GROUND_Y + 3, front + 1, Blocks.GREEN_WOOL);
    }

    private static void table(Canvas c, int x, int y, int z) {
        c.set(x, y, z, Blocks.SPRUCE_FENCE);
        c.set(x, y + 1, z, Blocks.SPRUCE_PRESSURE_PLATE);
        c.stair(x - 1, y, z, Direction.WEST, Blocks.SPRUCE_STAIRS);
        c.stair(x + 1, y, z, Direction.EAST, Blocks.SPRUCE_STAIRS);
    }

    /** Free beds and basics beside the plaza, so a first night is survivable on every seed. */
    static void starterShelter(Canvas c, Lot lot) {
        Style style = Style.BRICK;
        Tower.build(c, lot.rect(), 2, style, lot.hash(), Tower.Roof.GARDEN, (cv, k, in, y) -> {
            for (int z = in.z0() + 3; z <= in.z1() - 3; z += 2) {
                cv.bed(in.x1() - 1, y + 1, z, Direction.EAST, Blocks.BROWN_BED);
            }
            cv.chest(in.x0(), y + 1, in.z0() + 3, Direction.EAST, "starter_cache");
            cv.set(in.x0(), y + 1, in.z0() + 4, Blocks.CRAFTING_TABLE);
            cv.facing(in.x0(), y + 1, in.z0() + 5, Direction.EAST, Blocks.FURNACE);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
            Tower.lamp(cv, in.centerX(), y, in.z0() + 3);
        });
    }

    static void hospital(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(8);
        c.fill(lot.rect(), STREET_Y, STREET_Y, Mix.of(Blocks.SMOOTH_STONE, 3, Blocks.LIGHT_GRAY_CONCRETE, 2));
        int floors = 5 + CityHash.range(lot.hash(), 3);
        Tower.build(c, box, floors, Style.CLEAN, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> {
            LotPainter.ward(cv, k, in, y);
            cv.chest(in.x1(), y + 1, in.z0() + 4, Direction.WEST, "hospital");
            for (int x = in.x0() + 6; x <= in.x1() - 6; x += 6) {
                Tower.lamp(cv, x, y, in.z0() + 5);
            }
        });
        int roof = STREET_Y + floors * Tower.STOREY + 1;
        LotPainter.redCross(c, box.centerX(), roof + 3, box.z1());
        c.fill(box.centerX(), roof, box.z1(), box.centerX(), roof + 1, box.z1(), Blocks.RED_CONCRETE);
    }

    static void departmentStore(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(10);
        c.fill(lot.rect(), STREET_Y, STREET_Y, Mix.of(Blocks.POLISHED_ANDESITE, 3, Blocks.SMOOTH_STONE, 2));
        int floors = 6;
        Tower.build(c, box, floors, Style.CONCRETE, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            // Sales floors: aisles of stock, kept off the stair corridor and the entrance.
            for (int x = in.x0() + 3; x <= in.x1() - 2; x += 4) {
                if (Math.abs(x - in.centerX()) <= 2) {
                    continue;
                }
                for (int z = in.z0() + 4; z <= in.z1() - 3; z += 2) {
                    cv.barrel(x, y + 1, z, "department");
                }
            }
            for (int x = in.x0() + 4; x <= in.x1() - 3; x += 8) {
                for (int z = in.z0() + 4; z <= in.z1() - 3; z += 8) {
                    Tower.lamp(cv, x, y, z);
                }
            }
        });
        c.fill(box.x0() + 1, STREET_Y + floors * Tower.STOREY - 1, box.z1(), box.x1() - 1, STREET_Y + floors * Tower.STOREY - 1, box.z1(), Blocks.RED_TERRACOTTA);
    }

    static void researchManor(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect box = r.inset(Math.max(4, Math.min(r.width(), r.depth()) / 2 - 14));
        Style style = Style.MANOR;
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.PODZOL, 3, Blocks.COARSE_DIRT, 2, Blocks.MOSS_BLOCK, 1));
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y + 1, fence.z1(), Blocks.AIR);
        Tower.build(c, box, 3, style, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            if (k == 0) {
                for (int x = in.x0() + 6; x <= in.x1() - 2; x += 4) {
                    cv.set(x, y + 1, in.z0() + 3, Blocks.BREWING_STAND);
                    cv.chest(x, y + 1, in.z0() + 4, Direction.SOUTH, "lab");
                }
                Tower.lamp(cv, in.centerX(), y, in.centerZ());
            } else {
                LotPainter.dwelling(cv, style, k, in, y);
            }
        });
    }

    /** A bowl of dark water ringed by refuse. Kept shallow enough to clear the sewers, and sealed against leaks. */
    static void lakeOfRefuse(Canvas c, Lot lot) {
        Rect r = lot.rect();
        int cx = r.centerX();
        int cz = r.centerZ();
        double rx = r.width() / 2.0 - 8.0;
        double rz = r.depth() / 2.0 - 8.0;
        Mix mud = Mix.of(Blocks.MUD, 3, Blocks.SOUL_SOIL, 2, Blocks.CLAY, 1);
        Mix rim = Mix.of(ModBlocks.INDUSTRIAL_DEBRIS.get(), 4, Blocks.BONE_BLOCK, 1, Blocks.SOUL_SOIL, 2, Blocks.GRAVEL, 2);
        for (int x = Math.max(r.x0(), c.minX); x <= Math.min(r.x1(), c.maxX); x++) {
            for (int z = Math.max(r.z0(), c.minZ); z <= Math.min(r.z1(), c.maxZ); z++) {
                double dx = (x - cx) / rx;
                double dz = (z - cz) / rz;
                double d = dx * dx + dz * dz;
                if (d < 1.0) {
                    int depth = 2 + (int) Math.round((1.0 - d) * 4.0);
                    int bed = STREET_Y - depth;
                    c.fill(x, bed - 1, z, x, bed, z, mud);
                    c.fill(x, bed + 1, z, x, STREET_Y - 1, z, Blocks.WATER);
                    c.set(x, STREET_Y, z, Blocks.AIR);
                } else if (d < 1.35) {
                    c.fill(x, STREET_Y - 1, z, x, STREET_Y, z, rim);
                    int mound = CityHash.range(c.hash(x, 0, z), 3);
                    if (mound > 0) {
                        c.fill(x, GROUND_Y, z, x, GROUND_Y + mound - 1, z, rim);
                    }
                }
            }
        }
        c.sign(r.x0() + 1, GROUND_Y, r.z1() - 1, Direction.SOUTH, Blocks.DARK_OAK_WALL_SIGN, "KEEP OUT", "LAKE OF", "REFUSE");
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Line.java

```java
package com.dorohedoro.hole.worldgen.city;

/**
 * An arterial that runs forever along one axis. Because a line is fully described by its index, roads and canals
 * cannot disagree across a chunk border.
 *
 * @param center    fixed coordinate on the cross axis
 * @param halfWidth blocks from the center to the band's outer edge
 */
public record Line(int index, int center, int halfWidth, Kind kind) {
    public enum Kind {
        AVENUE,
        TRAM,
        CANAL
    }

    public boolean contains(int coordinate) {
        return Math.abs(coordinate - center) <= halfWidth;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Lot.java

```java
package com.dorohedoro.hole.worldgen.city;

/**
 * One building plot. A lot's content is a pure function of this record, so each chunk it overlaps can draw its own
 * slice without any knowledge of its neighbours.
 */
public record Lot(Rect rect, LotType type, District district, long hash) {}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/LotChooser.java

```java
package com.dorohedoro.hole.worldgen.city;

import net.minecraft.util.RandomSource;

/** Picks what occupies a lot, by district. */
final class LotChooser {
    private LotChooser() {}

    static LotType choose(CityPlan plan, District district, Rect rect, RandomSource random) {
        float roll = random.nextFloat();
        if (random.nextFloat() > plan.settings.density()) {
            return roll < 0.4F ? LotType.RUBBLE : roll < 0.75F ? LotType.VACANT : LotType.JUNKYARD;
        }
        boolean large = rect.width() >= 20 && rect.depth() >= 20;
        return switch (district) {
            case RESIDENTIAL -> {
                if (roll < 0.02F) yield LotType.NURSERY;
                if (roll < 0.05F) yield LotType.VACANT;
                if (roll < 0.08F) yield LotType.GREENHOUSE;
                if (roll < 0.11F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.15F) yield LotType.CLINIC;
                if (roll < 0.19F) yield LotType.GUARD_POST;
                if (roll < 0.24F) yield LotType.SHOP;
                yield large || roll > 0.72F ? LotType.APARTMENT : LotType.TENEMENT;
            }
            case MARKET -> {
                if (roll < 0.34F) yield LotType.SHOP;
                if (roll < 0.56F) yield LotType.MARKET_STALLS;
                if (roll < 0.64F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.72F) yield LotType.GREENHOUSE;
                if (roll < 0.78F) yield LotType.WAREHOUSE;
                yield LotType.TENEMENT;
            }
            case INDUSTRIAL -> {
                if (roll < 0.44F) yield LotType.FACTORY;
                if (roll < 0.76F) yield LotType.WAREHOUSE;
                if (roll < 0.90F) yield LotType.JUNKYARD;
                yield LotType.TENEMENT;
            }
            case WATERFRONT -> {
                if (roll < 0.30F) yield LotType.WAREHOUSE;
                if (roll < 0.55F) yield LotType.TENEMENT;
                if (roll < 0.70F) yield LotType.SHOP;
                if (roll < 0.84F) yield LotType.GREENHOUSE;
                yield LotType.JUNKYARD;
            }
            case DERELICT -> {
                if (roll < 0.26F) yield LotType.RUBBLE;
                if (roll < 0.44F) yield LotType.JUNKYARD;
                if (roll < 0.62F) yield LotType.VACANT;
                if (roll < 0.72F) yield LotType.PARK;
                if (roll < 0.80F) yield LotType.NURSERY;
                if (roll < 0.88F) yield LotType.LIVESTOCK_PEN;
                yield LotType.TENEMENT;
            }
            case GRAVEYARD -> {
                if (roll < 0.70F) yield LotType.CEMETERY;
                if (roll < 0.85F) yield LotType.PARK;
                yield LotType.TENEMENT;
            }
            case OUTSKIRTS -> {
                if (roll < 0.34F) yield LotType.VACANT;
                if (roll < 0.52F) yield LotType.RUBBLE;
                if (roll < 0.64F) yield LotType.JUNKYARD;
                if (roll < 0.74F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.82F) yield LotType.GREENHOUSE;
                if (roll < 0.88F) yield LotType.PARK;
                if (roll < 0.93F) yield LotType.WAREHOUSE;
                yield LotType.TENEMENT;
            }
        };
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/LotPainter.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Draws enclosed lots. Open-air lots live in {@link OpenLots}, landmarks in {@link Landmarks}. */
final class LotPainter {
    private static final Mix YARD = Mix.of(Blocks.PACKED_MUD, 4, Blocks.COARSE_DIRT, 3, Blocks.COBBLED_DEEPSLATE, 2, Blocks.GRAVEL, 1);
    private static final Block[] JOB_SITES = {
            Blocks.SMOKER, Blocks.BLAST_FURNACE, Blocks.CARTOGRAPHY_TABLE, Blocks.LECTERN, Blocks.SMITHING_TABLE, Blocks.FLETCHING_TABLE,
            Blocks.LOOM, Blocks.STONECUTTER, Blocks.CAULDRON, Blocks.BREWING_STAND, Blocks.COMPOSTER, Blocks.BARREL
    };

    private LotPainter() {}

    static void paint(Canvas c, CityPlan plan, Lot lot) {
        if (!c.overlaps(lot.rect())) {
            return;
        }
        c.fill(lot.rect(), STREET_Y, STREET_Y, YARD);
        switch (lot.type()) {
            case PLAZA -> Landmarks.plaza(c, lot);
            case HUNGRY_BUG -> Landmarks.hungryBug(c, lot);
            case STARTER_SHELTER -> Landmarks.starterShelter(c, lot);
            case HOSPITAL -> Landmarks.hospital(c, lot);
            case DEPARTMENT_STORE -> Landmarks.departmentStore(c, lot);
            case RESEARCH_MANOR -> Landmarks.researchManor(c, lot);
            case LAKE_OF_REFUSE -> Landmarks.lakeOfRefuse(c, lot);
            case TENEMENT -> tenement(c, plan, lot);
            case APARTMENT -> apartment(c, plan, lot);
            case SHOP -> shop(c, lot);
            case CLINIC -> clinic(c, lot);
            case GUARD_POST -> guardPost(c, lot);
            case WAREHOUSE -> warehouse(c, lot);
            case FACTORY -> factory(c, lot);
            case MARKET_STALLS -> OpenLots.marketStalls(c, lot);
            case GREENHOUSE -> OpenLots.greenhouse(c, lot);
            case LIVESTOCK_PEN -> OpenLots.livestockPen(c, lot);
            case NURSERY -> OpenLots.nursery(c, lot);
            case PARK -> OpenLots.park(c, lot);
            case JUNKYARD -> OpenLots.junkyard(c, lot);
            case RUBBLE -> OpenLots.rubble(c, lot);
            case VACANT -> OpenLots.vacant(c, lot);
            case CEMETERY -> OpenLots.cemetery(c, lot);
        }
    }

    static Tower.Roof roofFor(long hash) {
        return switch (CityHash.range(hash >>> 32, 5)) {
            case 0, 1 -> Tower.Roof.GARDEN;
            case 2 -> Tower.Roof.TANK;
            case 3 -> Tower.Roof.ANTENNA;
            default -> Tower.Roof.PLAIN;
        };
    }

    private static void tenement(Canvas c, CityPlan plan, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int floors = Math.min(plan.settings.maxFloors(), 2 + CityHash.range(lot.hash(), 3));
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Tower.build(c, box, floors, style, lot.hash(), roofFor(lot.hash()), (cv, k, in, y) -> dwelling(cv, style, k, in, y));
    }

    private static void apartment(Canvas c, CityPlan plan, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int span = Math.max(1, plan.settings.maxFloors() - 4);
        int floors = Math.min(plan.settings.maxFloors(), 5 + CityHash.range(lot.hash(), span));
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Tower.build(c, box, floors, style, lot.hash(), roofFor(lot.hash()), (cv, k, in, y) -> dwelling(cv, style, k, in, y));
    }

    /** Living quarters: a shared entry on the ground floor, then rooms off a corridor beside the stairs. */
    static void dwelling(Canvas c, Style style, int storey, Rect in, int y) {
        if (storey == 0) {
            c.barrel(in.x1(), y + 1, in.z0() + 2, "apartment");
            c.set(in.x1(), y + 1, in.z0() + 3, Blocks.CRAFTING_TABLE);
            Tower.lamp(c, in.centerX(), y, in.centerZ());
            return;
        }
        int wallZ = in.z0() + 2;
        if (in.depth() < 6) {
            Tower.lamp(c, in.centerX(), y, in.centerZ());
            return;
        }
        c.fill(in.x0(), y + 1, wallZ, in.x1(), y + Tower.STOREY - 1, wallZ, style.wall());
        int roomWidth = 6;
        for (int rx = in.x0(); rx <= in.x1() - 3; rx += roomWidth + 1) {
            int rx1 = Math.min(rx + roomWidth - 1, in.x1());
            room(c, style, new Rect(rx, wallZ + 1, rx1, in.z1()), wallZ, y);
            if (rx1 < in.x1()) {
                c.fill(rx1 + 1, y + 1, wallZ + 1, rx1 + 1, y + Tower.STOREY - 1, in.z1(), style.wall());
            }
        }
    }

    private static void room(Canvas c, Style style, Rect r, int wallZ, int y) {
        c.door(r.centerX(), y + 1, wallZ, Direction.SOUTH, style.door());
        if (r.depth() >= 3 && r.width() >= 3) {
            c.bed(r.x0(), y + 1, r.z1() - 1, Direction.SOUTH, style.bed());
            switch (CityHash.range(c.hash(r.x0(), y, r.z0()), 4)) {
                case 0 -> c.chest(r.x1(), y + 1, r.z1(), Direction.NORTH, "apartment");
                case 1 -> c.barrel(r.x1(), y + 1, r.z1(), "apartment");
                case 2 -> c.set(r.x1(), y + 1, r.z1(), Blocks.CRAFTING_TABLE);
                default -> c.facing(r.x1(), y + 1, r.z1(), Direction.NORTH, Blocks.FURNACE);
            }
        }
        Tower.lamp(c, r.centerX(), y, r.centerZ());
    }

    private static void shop(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.marketStalls(c, lot);
            return;
        }
        int floors = 1 + CityHash.range(lot.hash(), 3);
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Block site = JOB_SITES[CityHash.range(lot.hash() >>> 12, JOB_SITES.length)];
        Tower.build(c, box, floors, style, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> {
            if (k > 0) {
                dwelling(cv, style, k, in, y);
                return;
            }
            for (int z = in.z0() + 3; z <= in.z1() - 2; z++) {
                cv.barrel(in.x1() - 1, y + 1, z, "shop");
            }
            cv.set(in.x1(), y + 1, in.centerZ(), site);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
        });
        awning(c, box, lot.hash());
    }

    private static void awning(Canvas c, Rect box, long hash) {
        Block[] cloth = {Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.GREEN_WOOL, Blocks.CYAN_WOOL, Blocks.YELLOW_WOOL};
        Block a = cloth[CityHash.range(hash >>> 20, cloth.length)];
        for (int x = box.x0(); x <= box.x1(); x++) {
            c.set(x, GROUND_Y + 3, box.z1() + 1, Math.floorMod(x, 2) == 0 ? a : Blocks.WHITE_WOOL);
        }
    }

    private static void clinic(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int floors = 2 + CityHash.range(lot.hash(), 2);
        Tower.build(c, box, floors, Style.CLEAN, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> ward(cv, k, in, y));
        redCross(c, box.centerX(), GROUND_Y + floors * Tower.STOREY - 3, box.z1());
    }

    /** Beds along the south wall, with supplies at the east end. */
    static void ward(Canvas c, int storey, Rect in, int y) {
        for (int x = in.x0(); x <= in.x1() - 1; x += 2) {
            if (Math.abs(x - in.centerX()) > 1) {
                c.bed(x, y + 1, in.z1() - 1, Direction.SOUTH, Blocks.WHITE_BED);
            }
        }
        c.set(in.x1(), y + 1, in.z0() + 2, Blocks.BREWING_STAND);
        c.chest(in.x1(), y + 1, in.z0() + 3, Direction.WEST, "clinic");
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        Tower.lamp(c, in.x0() + 2, y, in.centerZ());
        Tower.lamp(c, in.x1() - 2, y, in.centerZ());
    }

    static void redCross(Canvas c, int x, int y, int z) {
        c.fill(x - 1, y, z, x + 1, y, z, Blocks.RED_CONCRETE);
        c.fill(x, y - 1, z, x, y + 1, z, Blocks.RED_CONCRETE);
    }

    private static void guardPost(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        Tower.build(c, box, 2, Style.SOOT, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            cv.chest(in.x1(), y + 1, in.z0() + 2, Direction.WEST, "guard");
            cv.set(in.x1() - 1, y + 1, in.z1() - 1, Blocks.SMITHING_TABLE);
            cv.bed(in.x0(), y + 1, in.z1() - 1, Direction.SOUTH, Blocks.BLACK_BED);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
        });
    }

    /** A tall single-room shed with aisles of crates. */
    private static void hall(Canvas c, Rect box, int height, Style style) {
        int roofY = GROUND_Y + height;
        c.fill(box, STREET_Y, STREET_Y, style.floor());
        c.walls(box, GROUND_Y, roofY, style.wall());
        c.fill(box, roofY, roofY, style.trim());
        // High slit windows, then a cart-sized opening in the south wall.
        for (int x = box.x0() + 2; x < box.x1() - 1; x += 4) {
            c.pane(x, roofY - 2, box.z0(), Direction.Axis.X, Blocks.IRON_BARS);
            c.pane(x, roofY - 2, box.z1(), Direction.Axis.X, Blocks.IRON_BARS);
        }
        for (int z = box.z0() + 2; z < box.z1() - 1; z += 4) {
            c.pane(box.x0(), roofY - 2, z, Direction.Axis.Z, Blocks.IRON_BARS);
            c.pane(box.x1(), roofY - 2, z, Direction.Axis.Z, Blocks.IRON_BARS);
        }
        int doorX = box.centerX();
        c.fill(doorX - 1, GROUND_Y, box.z1(), doorX + 1, GROUND_Y + 3, box.z1(), Blocks.AIR);
        Rect in = box.inset(1);
        for (int x = in.x0() + 2; x < in.x1(); x += 6) {
            for (int z = in.z0() + 2; z < in.z1(); z += 6) {
                c.set(x, roofY - 1, z, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
        }
    }

    private static void warehouse(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 7 || box.depth() < 7) {
            OpenLots.junkyard(c, lot);
            return;
        }
        int height = 6 + CityHash.range(lot.hash(), 5);
        hall(c, box, height, Style.forDistrict(lot.district(), lot.hash()));
        Rect in = box.inset(1);
        for (int x = in.x0() + 1; x <= in.x1() - 1; x += 3) {
            if (Math.abs(x - box.centerX()) <= 1) {
                continue;
            }
            for (int z = in.z0() + 1; z <= in.z1() - 3; z++) {
                long h = c.hash(x, STREET_Y, z);
                int stack = 1 + CityHash.range(h, 3);
                for (int s = 0; s < stack; s++) {
                    c.set(x, GROUND_Y + s, z, Blocks.SPRUCE_PLANKS);
                }
                if (CityHash.range(h >>> 16, 9) == 0) {
                    c.barrel(x, GROUND_Y + stack, z, "warehouse");
                }
            }
        }
        c.chest(in.x0(), GROUND_Y, in.z1(), Direction.EAST, "warehouse");
    }

    private static void factory(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 9 || box.depth() < 9) {
            OpenLots.junkyard(c, lot);
            return;
        }
        int height = 9 + CityHash.range(lot.hash(), 6);
        Style style = lot.hash() < 0 ? Style.RUST : Style.SOOT;
        hall(c, box, height, style);
        Rect in = box.inset(1);
        int roofY = GROUND_Y + height;

        // Furnace lines along the north wall, with scrap heaped along the west wall.
        for (int x = in.x0() + 1; x <= in.x1() - 1; x += 2) {
            c.facing(x, GROUND_Y, in.z0(), Direction.SOUTH, CityHash.range(c.hash(x, 1, in.z0()), 3) == 0 ? Blocks.BLAST_FURNACE : Blocks.FURNACE);
        }
        for (int z = in.z0() + 2; z <= in.z1() - 3; z++) {
            c.set(in.x0(), GROUND_Y, z, com.dorohedoro.hole.registry.ModBlocks.INDUSTRIAL_DEBRIS.get());
        }
        c.set(in.x1(), GROUND_Y, in.z1() - 3, Blocks.ANVIL);
        c.chest(in.x1(), GROUND_Y, in.z1() - 2, Direction.WEST, "factory");

        // Smokestacks are the district's skyline.
        int stacks = 1 + CityHash.range(lot.hash() >>> 8, 3);
        for (int i = 0; i < stacks; i++) {
            int x = box.x0() + 3 + i * Math.max(5, (box.width() - 6) / Math.max(1, stacks));
            if (x + 1 > box.x1() - 2) {
                break;
            }
            int z = box.z0() + 3;
            int stackTop = roofY + 16 + CityHash.range(lot.hash() >>> (16 + i), 26);
            c.fill(x - 1, roofY + 1, z - 1, x + 1, stackTop, z + 1, Mix.of(Blocks.BRICKS, 3, Blocks.DEEPSLATE_BRICKS, 2, Blocks.CRACKED_DEEPSLATE_BRICKS, 1));
            c.fill(x, roofY + 1, z, x, stackTop, z, Blocks.AIR);
            c.fill(x - 1, stackTop - 2, z - 1, x + 1, stackTop - 2, z + 1, Blocks.WAXED_OXIDIZED_CUT_COPPER);
            c.set(x, stackTop - 2, z, Blocks.AIR);
        }
        // Roof pipework running the length of the hall.
        int pipeZ = box.z1() - 3;
        c.fill(box.x0() + 1, roofY + 1, pipeZ, box.x1() - 1, roofY + 2, pipeZ, Blocks.WAXED_OXIDIZED_COPPER);
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/LotType.java

```java
package com.dorohedoro.hole.worldgen.city;

/** What occupies a lot. */
public enum LotType {
    PLAZA(false),
    HUNGRY_BUG(true),
    STARTER_SHELTER(true),
    TENEMENT(true),
    APARTMENT(true),
    SHOP(true),
    CLINIC(true),
    GUARD_POST(true),
    WAREHOUSE(true),
    FACTORY(true),
    MARKET_STALLS(false),
    GREENHOUSE(true),
    LIVESTOCK_PEN(false),
    NURSERY(false),
    PARK(false),
    JUNKYARD(false),
    RUBBLE(false),
    VACANT(false),
    CEMETERY(false),
    HOSPITAL(true),
    DEPARTMENT_STORE(true),
    RESEARCH_MANOR(true),
    LAKE_OF_REFUSE(false);

    /** Whether the lot holds an enclosed structure with an interior. */
    public final boolean enclosed;

    LotType(boolean enclosed) {
        this.enclosed = enclosed;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Mix.java

```java
package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A weighted block palette, sampled by position hash. */
public final class Mix {
    private final BlockState[] states;

    private Mix(BlockState[] states) {
        this.states = states;
    }

    /** Alternating blocks and integer weights, for example {@code Mix.of(Blocks.STONE, 3, Blocks.ANDESITE, 1)}. */
    public static Mix of(Object... blocksAndWeights) {
        List<BlockState> expanded = new ArrayList<>();
        for (int i = 0; i < blocksAndWeights.length; i += 2) {
            Object entry = blocksAndWeights[i];
            BlockState state = entry instanceof Block block ? block.defaultBlockState() : (BlockState) entry;
            int weight = (Integer) blocksAndWeights[i + 1];
            for (int n = 0; n < weight; n++) {
                expanded.add(state);
            }
        }
        return new Mix(expanded.toArray(BlockState[]::new));
    }

    public BlockState pick(long hash) {
        return states[CityHash.range(hash, states.length)];
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/OpenLots.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lots with no building, or only glass over soil. These are where wood, saplings, crops, animals and loose earth come
 * from now that the Overworld has no countryside, so what each one offers matters as much as how it looks.
 */
final class OpenLots {
    private OpenLots() {}

    static Mix debris() {
        return Mix.of(ModBlocks.INDUSTRIAL_DEBRIS.get(), 5, Blocks.GRAVEL, 3, ModBlocks.RUSTED_PLATING.get(), 2, Blocks.SAND, 1, Blocks.COBBLESTONE, 2);
    }

    private static void soil(Canvas c, Rect r) {
        c.fill(r, STREET_Y - 2, STREET_Y - 1, Blocks.DIRT);
        c.fill(r, STREET_Y, STREET_Y, Blocks.GRASS_BLOCK);
    }

    /** A small hardwood. Leaves are persistent so a lone tree never decays away before it can yield saplings. */
    static void tree(Canvas c, int x, int z, int height, Block log, Block leaves) {
        BlockState foliage = leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int top = GROUND_Y + height - 1;
        for (int y = top - 2; y <= top + 1; y++) {
            int radius = y >= top ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    boolean corner = Math.abs(dx) == radius && Math.abs(dz) == radius;
                    if (!corner || CityHash.range(c.hash(x + dx, y, z + dz), 2) == 0) {
                        c.set(x + dx, y, z + dz, foliage);
                    }
                }
            }
        }
        c.fill(x, GROUND_Y, z, x, top, z, log);
    }

    static void marketStalls(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Block[][] goods = {
                {Blocks.HAY_BLOCK, Blocks.MELON},
                {Blocks.PUMPKIN, Blocks.DRIED_KELP_BLOCK},
                {Blocks.BARREL, Blocks.COMPOSTER},
                {Blocks.HONEYCOMB_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK}
        };
        Block[] cloth = {Blocks.RED_WOOL, Blocks.GREEN_WOOL, Blocks.ORANGE_WOOL, Blocks.CYAN_WOOL};
        int index = 0;
        for (int x = r.x0() + 1; x + 4 <= r.x1() - 1; x += 7) {
            for (int z = r.z0() + 1; z + 3 <= r.z1() - 1; z += 6) {
                long h = CityHash.mix(lot.hash() + index++);
                Block roof = cloth[CityHash.range(h, cloth.length)];
                Block[] stock = goods[CityHash.range(h >>> 16, goods.length)];
                for (int y = GROUND_Y; y <= GROUND_Y + 2; y++) {
                    c.set(x, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x + 4, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x, y, z + 3, Blocks.SPRUCE_FENCE);
                    c.set(x + 4, y, z + 3, Blocks.SPRUCE_FENCE);
                }
                for (int dx = 0; dx <= 4; dx++) {
                    c.fill(x + dx, GROUND_Y + 3, z, x + dx, GROUND_Y + 3, z + 3, dx % 2 == 0 ? roof : Blocks.WHITE_WOOL);
                }
                c.set(x + 1, GROUND_Y, z + 1, stock[0]);
                c.set(x + 2, GROUND_Y, z + 1, stock[1]);
                c.chest(x + 3, GROUND_Y, z + 1, Direction.SOUTH, "market");
            }
        }
    }

    /** Soil under glass: crops, sugar cane, pumpkins, melons, mushrooms and bees, with a water channel feeding it all. */
    static void greenhouse(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 9 || box.depth() < 7) {
            nursery(c, lot);
            return;
        }
        int roofY = GROUND_Y + 5;
        c.fill(box, STREET_Y - 1, STREET_Y - 1, Blocks.DIRT);
        c.fill(box, STREET_Y, STREET_Y, Blocks.DIRT);
        c.walls(box, GROUND_Y, roofY, Mix.of(Blocks.GLASS, 1));
        c.fill(box, roofY, roofY, Blocks.GLASS);
        for (int x = box.x0(); x <= box.x1(); x += 4) {
            c.fill(x, GROUND_Y, box.z0(), x, roofY, box.z0(), Blocks.DARK_OAK_LOG);
            c.fill(x, GROUND_Y, box.z1(), x, roofY, box.z1(), Blocks.DARK_OAK_LOG);
            c.fill(x, roofY, box.z0(), x, roofY, box.z1(), Blocks.DARK_OAK_LOG);
        }
        Rect in = box.inset(1);
        int aisleZ = in.centerZ();
        BlockState wet = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
        for (int x = in.x0(); x <= in.x1(); x++) {
            int column = x - in.x0();
            boolean channel = column % 5 == 2;
            Block crop = crops[Math.floorMod(column / 5 + CityHash.range(lot.hash(), 4), crops.length)];
            for (int z = in.z0(); z <= in.z1(); z++) {
                if (z == aisleZ && !channel) {
                    c.set(x, STREET_Y, z, Blocks.MOSS_BLOCK);
                } else if (channel) {
                    c.set(x, STREET_Y, z, Blocks.WATER);
                } else if (column % 5 == 3 && z < aisleZ) {
                    // Cane rooted beside the channel.
                    c.set(x, STREET_Y, z, Blocks.SAND);
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + 2, z, Blocks.SUGAR_CANE);
                } else if (crop instanceof CropBlock cropBlock) {
                    c.set(x, STREET_Y, z, wet);
                    c.set(x, GROUND_Y, z, cropBlock.getStateForAge(CityHash.range(c.hash(x, STREET_Y, z), cropBlock.getMaxAge() + 1)));
                }
            }
        }
        // The aisle bridges every channel so the whole house stays walkable.
        for (int x = in.x0(); x <= in.x1(); x++) {
            c.set(x, STREET_Y, aisleZ, Blocks.MOSS_BLOCK);
        }
        // Stems with their fruit beside them at the two aisle ends.
        BlockState pumpkinStem = Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE);
        BlockState melonStem = Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE);
        c.set(in.x0(), STREET_Y, in.z1(), wet);
        c.set(in.x0(), GROUND_Y, in.z1(), pumpkinStem);
        c.set(in.x0() + 1, STREET_Y, in.z1(), Blocks.DIRT);
        c.set(in.x0() + 1, GROUND_Y, in.z1(), Blocks.PUMPKIN);
        c.set(in.x1(), STREET_Y, in.z1(), wet);
        c.set(in.x1(), GROUND_Y, in.z1(), melonStem);
        c.set(in.x1() - 1, STREET_Y, in.z1(), Blocks.DIRT);
        c.set(in.x1() - 1, GROUND_Y, in.z1(), Blocks.MELON);
        c.facing(box.x0() + 1, GROUND_Y + 3, box.z0() + 1, Direction.SOUTH, Blocks.BEE_NEST);
        c.set(in.x1() - 1, GROUND_Y, aisleZ + 1, Blocks.COMPOSTER);
        c.chest(in.x0(), GROUND_Y, aisleZ, Direction.EAST, "nursery");

        // The door sits at the east end of the central aisle.
        c.door(box.x1(), GROUND_Y, aisleZ, Direction.WEST, Blocks.DARK_OAK_DOOR);
    }

    /** Fenced grass the city's animals live in. The animals themselves are added by {@link CityPopulator}. */
    static void livestockPen(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.SPRUCE_FENCE, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y, fence.z1(), Blocks.SPRUCE_FENCE_GATE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.SOUTH));
        c.set(fence.x0() + 1, GROUND_Y, fence.z0() + 1, Blocks.HAY_BLOCK);
        c.set(fence.x0() + 2, GROUND_Y, fence.z0() + 1, Blocks.HAY_BLOCK);
        c.set(fence.x1() - 1, GROUND_Y, fence.z0() + 1, Blocks.COMPOSTER);
        c.fill(fence.x0() + 2, STREET_Y, fence.z1() - 2, fence.x0() + 3, STREET_Y, fence.z1() - 2, Blocks.WATER);
    }

    static void nursery(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.SPRUCE_FENCE, fence.centerX(), fence.z1());
        Rect in = fence.inset(1);
        Block[] saplings = {Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.CHERRY_SAPLING};
        for (int x = in.x0(); x <= in.x1(); x += 2) {
            c.set(x, GROUND_Y, in.z0(), saplings[Math.floorMod(x, saplings.length)]);
        }
        tree(c, in.x0() + 2, in.centerZ(), 5, Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        if (in.width() >= 11) {
            tree(c, in.x1() - 2, in.centerZ(), 6, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
        }
        c.chest(in.x1(), GROUND_Y, in.z1(), Direction.WEST, "nursery");
        c.set(in.x0(), GROUND_Y, in.z1(), Blocks.COMPOSTER);
    }

    static void park(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Block[][] trees = {{Blocks.OAK_LOG, Blocks.OAK_LEAVES}, {Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES}, {Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES}};
        int count = 2 + r.area() / 180;
        for (int i = 0; i < count; i++) {
            long h = CityHash.mix(lot.hash() + i);
            int x = r.x0() + 3 + CityHash.range(h, Math.max(1, r.width() - 6));
            int z = r.z0() + 3 + CityHash.range(h >>> 16, Math.max(1, r.depth() - 6));
            Block[] kind = trees[CityHash.range(h >>> 32, trees.length)];
            tree(c, x, z, 5 + CityHash.range(h >>> 40, 3), kind[0], kind[1]);
        }
        // A pond rimmed with sand and clay, which keeps both obtainable without digging canals.
        if (r.width() >= 14 && r.depth() >= 14) {
            int px = r.centerX();
            int pz = r.centerZ();
            c.fill(px - 4, STREET_Y - 1, pz - 3, px + 4, STREET_Y, pz + 3, Blocks.SAND);
            c.fill(px - 3, STREET_Y - 2, pz - 2, px + 3, STREET_Y - 2, pz + 2, Blocks.CLAY);
            c.fill(px - 3, STREET_Y - 1, pz - 2, px + 3, STREET_Y, pz + 2, Blocks.WATER);
        }
        for (int x = r.x0() + 1; x <= r.x1() - 1; x += 2) {
            for (int z = r.z0() + 1; z <= r.z1() - 1; z += 2) {
                long h = c.hash(x, GROUND_Y, z);
                if (c.contains(x, z) && c.get(x, GROUND_Y, z).isAir() && c.get(x, STREET_Y, z).is(Blocks.GRASS_BLOCK) && CityHash.range(h, 5) == 0) {
                    c.set(x, GROUND_Y, z, CityHash.range(h >>> 16, 4) == 0 ? Blocks.DANDELION : Blocks.SHORT_GRASS);
                }
            }
        }
    }

    /**
     * Overgrown empty ground: patchy soil, weeds and the odd dead or struggling tree. This is where the Hole's rare
     * vegetation belongs. Most lots stay treeless, and even a living tree here is small and thin.
     */
    static void vacant(Canvas c, Lot lot) {
        Rect r = lot.rect();
        c.fill(r, STREET_Y - 1, STREET_Y - 1, Blocks.DIRT);
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.COARSE_DIRT, 5, Blocks.DIRT, 3, Blocks.GRASS_BLOCK, 3, Blocks.GRAVEL, 1, Blocks.PODZOL, 1));
        for (int x = Math.max(r.x0(), c.minX); x <= Math.min(r.x1(), c.maxX); x++) {
            for (int z = Math.max(r.z0(), c.minZ); z <= Math.min(r.z1(), c.maxZ); z++) {
                long h = c.hash(x, GROUND_Y, z);
                if (!c.get(x, STREET_Y, z).is(Blocks.GRASS_BLOCK) && !c.get(x, STREET_Y, z).is(Blocks.PODZOL)) {
                    continue;
                }
                int roll = CityHash.range(h, 12);
                if (roll < 3) {
                    c.set(x, GROUND_Y, z, Blocks.SHORT_GRASS);
                } else if (roll == 3) {
                    c.set(x, GROUND_Y, z, Blocks.DEAD_BUSH);
                } else if (roll == 4 && CityHash.range(h >>> 16, 4) == 0) {
                    c.set(x, GROUND_Y, z, Blocks.SWEET_BERRY_BUSH);
                }
            }
        }
        int roll = CityHash.range(lot.hash() >>> 8, 10);
        if (roll < 4) {
            deadTree(c, r.centerX(), r.centerZ(), lot.hash());
        } else if (roll == 4) {
            tree(c, r.centerX(), r.centerZ(), 4, Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        }
    }

    /** A bare trunk with a few stubs of branch. It yields a little wood but no saplings. */
    static void deadTree(Canvas c, int x, int z, long hash) {
        int height = 4 + CityHash.range(hash, 4);
        c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, Blocks.STRIPPED_OAK_LOG);
        Direction[] sides = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < 3; i++) {
            Direction side = sides[CityHash.range(hash >>> (i * 8 + 4), sides.length)];
            int y = GROUND_Y + height - 1 - i * 2;
            if (y > GROUND_Y) {
                c.set(x + side.getStepX(), y, z + side.getStepZ(), Blocks.STRIPPED_OAK_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, side.getAxis()));
            }
        }
    }

    private static void heap(Canvas c, int cx, int cz, int radius, Mix mix) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                int height = radius - Math.max(Math.abs(x - cx), Math.abs(z - cz)) - CityHash.range(c.hash(x, 0, z), 2);
                if (height > 0) {
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, mix);
                }
            }
        }
    }

    static void junkyard(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect fence = r.inset(1);
        Mix debris = debris();
        int heaps = 1 + r.area() / 120;
        for (int i = 0; i < heaps; i++) {
            long h = CityHash.mix(lot.hash() ^ (i * 31L));
            int radius = 2 + CityHash.range(h, 3);
            int x = r.x0() + 2 + radius + CityHash.range(h >>> 16, Math.max(1, r.width() - 4 - radius * 2));
            int z = r.z0() + 2 + radius + CityHash.range(h >>> 32, Math.max(1, r.depth() - 4 - radius * 2));
            heap(c, x, z, radius, debris);
        }
        // Stacked salvage timber: the only logs in the district.
        for (int x = fence.x0() + 1; x <= Math.min(fence.x1() - 1, fence.x0() + 4); x++) {
            c.fill(x, GROUND_Y, fence.z0() + 1, x, GROUND_Y + 1, fence.z0() + 1, Blocks.OAK_LOG);
        }
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y + 1, fence.z1(), Blocks.AIR);
        c.chest(fence.x1() - 1, GROUND_Y, fence.z1() - 1, Direction.WEST, "scrap");
    }

    static void rubble(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect shell = r.inset(1);
        Mix broken = Mix.of(Blocks.CRACKED_STONE_BRICKS, 3, Blocks.COBBLESTONE, 3, Blocks.MOSSY_COBBLESTONE, 1, Blocks.BRICKS, 2);
        Mix ground = Mix.of(Blocks.COARSE_DIRT, 4, Blocks.GRAVEL, 3, Blocks.DIRT, 3, Blocks.SAND, 1, Blocks.COBBLESTONE, 1);
        c.fill(r, STREET_Y, STREET_Y, ground);
        for (int x = shell.x0(); x <= shell.x1(); x++) {
            for (int z = shell.z0(); z <= shell.z1(); z++) {
                boolean edge = x == shell.x0() || x == shell.x1() || z == shell.z0() || z == shell.z1();
                if (!edge || !c.contains(x, z)) {
                    continue;
                }
                int height = CityHash.range(c.hash(x, 0, z), 4);
                if (height > 0 && Math.abs(x - shell.centerX()) > 1) {
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, broken);
                }
            }
        }
        heap(c, shell.centerX() - 1, shell.centerZ(), Math.min(3, shell.width() / 4), broken);
        c.set(shell.x0() + 1, GROUND_Y, shell.z0() + 1, Blocks.RED_MUSHROOM);
        c.set(shell.x0() + 2, GROUND_Y, shell.z0() + 1, Blocks.BROWN_MUSHROOM);
    }

    static void cemetery(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Mix plot = Mix.of(Blocks.PODZOL, 3, Blocks.COARSE_DIRT, 2, Blocks.GRASS_BLOCK, 3);
        c.fill(r, STREET_Y, STREET_Y, plot);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y, fence.z1(), Blocks.AIR);
        Rect in = fence.inset(1);
        for (int x = in.x0(); x <= in.x1(); x += 3) {
            if (Math.abs(x - in.centerX()) <= 1) {
                continue;
            }
            for (int z = in.z0(); z <= in.z1() - 1; z += 3) {
                long h = c.hash(x, GROUND_Y, z);
                Block marker = CityHash.range(h, 3) == 0 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.COBBLESTONE_WALL;
                c.set(x, GROUND_Y, z, marker);
                c.set(x, STREET_Y, z + 1, Blocks.PODZOL);
            }
        }
        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        c.fill(in.centerX(), GROUND_Y, in.centerZ(), in.centerX(), GROUND_Y + 2, in.centerZ(), Blocks.DARK_OAK_FENCE);
        c.set(in.centerX(), GROUND_Y + 3, in.centerZ(), lantern);
        if (in.depth() >= 9) {
            c.barrel(in.centerX(), GROUND_Y, in.z0(), "crypt");
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Rect.java

```java
package com.dorohedoro.hole.worldgen.city;

/** Axis-aligned XZ rectangle. All four bounds are inclusive. */
public record Rect(int x0, int z0, int x1, int z1) {
    public int width() {
        return x1 - x0 + 1;
    }

    public int depth() {
        return z1 - z0 + 1;
    }

    public int area() {
        return width() * depth();
    }

    public int centerX() {
        return (x0 + x1) >> 1;
    }

    public int centerZ() {
        return (z0 + z1) >> 1;
    }

    public boolean contains(int x, int z) {
        return x >= x0 && x <= x1 && z >= z0 && z <= z1;
    }

    public boolean intersects(Rect other) {
        return x0 <= other.x1 && x1 >= other.x0 && z0 <= other.z1 && z1 >= other.z0;
    }

    public Rect inset(int by) {
        return new Rect(x0 + by, z0 + by, x1 - by, z1 - by);
    }

    public boolean isValid() {
        return x1 >= x0 && z1 >= z0;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Style.java

```java
package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A building's materials. Blocks here end up above street level, so none may be raw stone, tuff, deepslate, andesite,
 * granite or diorite: see the note on {@link CityPainter}.
 */
record Style(Mix wall, Mix trim, Mix floor, Block stairs, Block door, Block glass, Block bed, Block sign, Block fence) {
    static final Style BRICK = new Style(
            Mix.of(Blocks.MUD_BRICKS, 5, Blocks.BRICKS, 2, Blocks.PACKED_MUD, 2, Blocks.BROWN_TERRACOTTA, 2),
            Mix.of(Blocks.STRIPPED_DARK_OAK_LOG, 2, Blocks.DARK_OAK_PLANKS, 1),
            Mix.of(Blocks.SPRUCE_PLANKS, 3, Blocks.DARK_OAK_PLANKS, 1),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style CONCRETE = new Style(
            Mix.of(Blocks.GRAY_CONCRETE, 4, Blocks.LIGHT_GRAY_CONCRETE, 2, Blocks.CYAN_TERRACOTTA, 2, Blocks.GRAY_TERRACOTTA, 2),
            Mix.of(Blocks.POLISHED_TUFF, 2, Blocks.POLISHED_ANDESITE, 1),
            Mix.of(Blocks.SMOOTH_STONE, 2, Blocks.SPRUCE_PLANKS, 3),
            Blocks.STONE_BRICK_STAIRS, Blocks.DARK_OAK_DOOR, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_BED, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    static final Style SOOT = new Style(
            Mix.of(Blocks.DEEPSLATE_BRICKS, 5, Blocks.CRACKED_DEEPSLATE_BRICKS, 2, Blocks.POLISHED_BLACKSTONE_BRICKS, 2, Blocks.BLACK_TERRACOTTA, 1),
            Mix.of(Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.WAXED_WEATHERED_CUT_COPPER, 2),
            Mix.of(Blocks.DARK_OAK_PLANKS, 3, Blocks.POLISHED_BLACKSTONE, 1),
            Blocks.DARK_OAK_STAIRS, Blocks.IRON_DOOR, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.BLACK_BED, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    static final Style RUST = new Style(
            Mix.of(ModBlocks.RUSTED_PLATING.get(), 5, Blocks.WAXED_WEATHERED_CUT_COPPER, 3, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.BROWN_TERRACOTTA, 2),
            Mix.of(Blocks.WAXED_OXIDIZED_COPPER, 2, Blocks.POLISHED_BLACKSTONE, 1),
            Mix.of(Blocks.POLISHED_BLACKSTONE, 2, Blocks.SPRUCE_PLANKS, 3),
            Blocks.DARK_OAK_STAIRS, Blocks.SPRUCE_DOOR, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.IRON_BARS);

    static final Style CLEAN = new Style(
            Mix.of(Blocks.WHITE_CONCRETE, 5, Blocks.LIGHT_GRAY_CONCRETE, 2, Blocks.WHITE_TERRACOTTA, 2),
            Mix.of(Blocks.CYAN_CONCRETE, 2, Blocks.LIGHT_BLUE_TERRACOTTA, 1),
            Mix.of(Blocks.SMOOTH_QUARTZ, 3, Blocks.LIGHT_GRAY_CONCRETE, 2),
            Blocks.QUARTZ_STAIRS, Blocks.BIRCH_DOOR, Blocks.GLASS_PANE, Blocks.WHITE_BED, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_FENCE);

    static final Style HUNGRY_BUG = new Style(
            Mix.of(Blocks.ORANGE_TERRACOTTA, 5, Blocks.RED_TERRACOTTA, 2, Blocks.TERRACOTTA, 2),
            Mix.of(Blocks.GREEN_TERRACOTTA, 3, Blocks.LIME_TERRACOTTA, 1),
            Mix.of(Blocks.CHERRY_PLANKS, 1, Blocks.SPRUCE_PLANKS, 2, Blocks.MANGROVE_PLANKS, 1),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR, Blocks.GLASS_PANE, Blocks.RED_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style MANOR = new Style(
            Mix.of(Blocks.POLISHED_BLACKSTONE_BRICKS, 4, Blocks.DEEPSLATE_TILES, 3, Blocks.NETHER_BRICKS, 2),
            Mix.of(Blocks.CRIMSON_PLANKS, 2, Blocks.DARK_PRISMARINE, 1),
            Mix.of(Blocks.DARK_OAK_PLANKS, 2, Blocks.CRIMSON_PLANKS, 1),
            Blocks.DARK_OAK_STAIRS, Blocks.CRIMSON_DOOR, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_BED, Blocks.CRIMSON_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    static Style forDistrict(District district, long hash) {
        return switch (district) {
            case RESIDENTIAL -> CityHash.range(hash, 3) == 0 ? CONCRETE : BRICK;
            case MARKET -> CityHash.range(hash, 3) == 0 ? BRICK : CONCRETE;
            case INDUSTRIAL -> CityHash.range(hash, 2) == 0 ? RUST : SOOT;
            case WATERFRONT -> CityHash.range(hash, 3) == 0 ? RUST : CONCRETE;
            case DERELICT -> CityHash.range(hash, 2) == 0 ? SOOT : BRICK;
            case GRAVEYARD -> SOOT;
            case OUTSKIRTS -> CityHash.range(hash, 2) == 0 ? RUST : BRICK;
        };
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Superblock.java

```java
package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

/** The lots between four arterials. Everything inside {@link #area} that is not a lot is street. */
public record Superblock(int i, int j, Rect area, List<Lot> lots) {
    private static final int STREET_GAP = 5;
    private static final int ALLEY_GAP = 3;
    private static final int PLAZA_RADIUS = 13;

    static Superblock build(CityPlan plan, int i, int j) {
        Line west = plan.xLine(i);
        Line east = plan.xLine(i + 1);
        Line north = plan.zLine(j);
        Line south = plan.zLine(j + 1);
        Rect area = new Rect(
                west.center() + west.halfWidth() + 1,
                north.center() + north.halfWidth() + 1,
                east.center() - east.halfWidth() - 1,
                south.center() - south.halfWidth() - 1);

        long hash = plan.blockHash(i, j);
        RandomSource random = new XoroshiroRandomSource(hash);
        District district = plan.districtAt(area.centerX(), area.centerZ());
        List<Lot> lots = new ArrayList<>();

        if (i == -1 && j == -1) {
            buildStartingQuarter(plan, area, district, random, lots);
        } else {
            CityPlan.LandmarkSite site = plan.landmarkSite(
                    Math.floorDiv(i, CityPlan.LANDMARK_REGION), Math.floorDiv(j, CityPlan.LANDMARK_REGION));
            if (site.blockI() == i && site.blockJ() == j) {
                lots.add(new Lot(area, site.type(), district, random.nextLong()));
            } else {
                split(plan, area, district, 0, random, lots);
            }
        }
        return new Superblock(i, j, area, List.copyOf(lots));
    }

    /**
     * The fixed quarter around the origin: an open plaza ringed by the things a new player needs, on every seed.
     * Road gaps of {@value #ALLEY_GAP} around the plaza keep it connected to the rest of the street grid.
     */
    private static void buildStartingQuarter(CityPlan plan, Rect area, District district, RandomSource random, List<Lot> lots) {
        int inner = PLAZA_RADIUS + ALLEY_GAP + 1;
        lots.add(new Lot(new Rect(-PLAZA_RADIUS, -PLAZA_RADIUS, PLAZA_RADIUS, PLAZA_RADIUS), LotType.PLAZA, district, random.nextLong()));

        Rect northStrip = new Rect(area.x0(), area.z0(), area.x1(), -inner);
        Rect southStrip = new Rect(area.x0(), inner, area.x1(), area.z1());
        Rect westStrip = new Rect(area.x0(), -PLAZA_RADIUS, -inner, PLAZA_RADIUS);
        Rect eastStrip = new Rect(inner, -PLAZA_RADIUS, area.x1(), PLAZA_RADIUS);

        place(plan, northStrip, new Rect(-15, -inner - 15, 15, -inner), LotType.HUNGRY_BUG, district, random, lots);
        place(plan, southStrip, new Rect(-15, inner, 15, inner + 15), LotType.NURSERY, district, random, lots);
        place(plan, eastStrip, new Rect(inner, -PLAZA_RADIUS, inner + 14, PLAZA_RADIUS), LotType.STARTER_SHELTER, district, random, lots);
        place(plan, westStrip, new Rect(-inner - 14, -PLAZA_RADIUS, -inner, PLAZA_RADIUS), LotType.MARKET_STALLS, district, random, lots);
    }

    private static void place(CityPlan plan, Rect strip, Rect special, LotType type, District district, RandomSource random, List<Lot> lots) {
        lots.add(new Lot(special, type, district, random.nextLong()));
        Rect[] rest = {
                new Rect(strip.x0(), strip.z0(), special.x0() - ALLEY_GAP - 1, strip.z1()),
                new Rect(special.x1() + ALLEY_GAP + 1, strip.z0(), strip.x1(), strip.z1()),
                new Rect(special.x0(), strip.z0(), special.x1(), special.z0() - ALLEY_GAP - 1),
                new Rect(special.x0(), special.z1() + ALLEY_GAP + 1, special.x1(), strip.z1())
        };
        for (Rect piece : rest) {
            if (piece.isValid() && piece.width() >= 8 && piece.depth() >= 8) {
                split(plan, piece, district, 1, random, lots);
            }
        }
    }

    private static void split(CityPlan plan, Rect rect, District district, int depth, RandomSource random, List<Lot> lots) {
        int minLot = district == District.INDUSTRIAL ? 16 : 11;
        int maxLot = switch (district) {
            case INDUSTRIAL, OUTSKIRTS -> 44;
            case DERELICT, GRAVEYARD -> 34;
            default -> 26;
        };
        int gap = depth == 0 ? STREET_GAP : ALLEY_GAP;
        boolean canCutX = rect.width() >= minLot * 2 + gap;
        boolean canCutZ = rect.depth() >= minLot * 2 + gap;
        boolean smallEnough = rect.width() <= maxLot && rect.depth() <= maxLot;

        if ((!canCutX && !canCutZ) || (smallEnough && random.nextFloat() < 0.35F)) {
            lots.add(new Lot(rect, LotChooser.choose(plan, district, rect, random), district, random.nextLong()));
            return;
        }

        boolean cutX = canCutX && (!canCutZ || rect.width() > rect.depth() || (rect.width() == rect.depth() && random.nextBoolean()));
        if (cutX) {
            int cut = rect.x0() + minLot + random.nextInt(rect.width() - minLot * 2 - gap + 1);
            split(plan, new Rect(rect.x0(), rect.z0(), cut - 1, rect.z1()), district, depth + 1, random, lots);
            split(plan, new Rect(cut + gap, rect.z0(), rect.x1(), rect.z1()), district, depth + 1, random, lots);
        } else {
            int cut = rect.z0() + minLot + random.nextInt(rect.depth() - minLot * 2 - gap + 1);
            split(plan, new Rect(rect.x0(), rect.z0(), rect.x1(), cut - 1), district, depth + 1, random, lots);
            split(plan, new Rect(rect.x0(), cut + gap, rect.x1(), rect.z1()), district, depth + 1, random, lots);
        }
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/city/Tower.java

```java
package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A multi-storey building: walls, floors, a stair route from the street to the roof, windows and a door. What goes in
 * each storey is left to a {@link Furnisher}, so every building type shares the same guaranteed walkable skeleton.
 *
 * <p>Flights are stacked directly above each other along the north wall, with a corridor row beside them, so a player
 * always reaches the next flight by walking back along the corridor.
 */
final class Tower {
    static final int STOREY = 4;

    @FunctionalInterface
    interface Furnisher {
        /** @param floorY the block players stand on for storey {@code index} */
        void furnish(Canvas c, int index, Rect interior, int floorY);
    }

    enum Roof { PLAIN, TANK, GARDEN, ANTENNA }

    private Tower() {}

    /** Smallest footprint that fits a stair flight and its corridor. */
    static boolean fits(Rect box) {
        return box.width() >= STOREY + 5 && box.depth() >= 7;
    }

    static void build(Canvas c, Rect box, int floors, Style style, long hash, Roof roof, Furnisher furnisher) {
        int top = STREET_Y + floors * STOREY;
        Rect interior = box.inset(1);

        c.fill(box, STREET_Y, STREET_Y, style.floor());
        c.walls(box, GROUND_Y, top + 1, style.wall());
        for (int k = 1; k <= floors; k++) {
            c.fill(interior, STREET_Y + k * STOREY, STREET_Y + k * STOREY, style.floor());
        }
        corners(c, box, top, style);

        for (int k = 0; k < floors; k++) {
            int floorY = STREET_Y + k * STOREY;
            flight(c, interior, floorY, style.stairs());
            windows(c, box, floorY, k == 0, style, hash);
            furnisher.furnish(c, k, interior, floorY);
        }
        // The south door is cut last so nothing above can wall it back in.
        int doorX = box.centerX();
        c.door(doorX, GROUND_Y, box.z1(), Direction.NORTH, style.door());
        c.clear(new Rect(doorX, box.z1() - 1, doorX, box.z1() - 1), GROUND_Y, GROUND_Y + 1);

        roofDetail(c, box, top, hash, roof);
        pipes(c, box, top, hash);
    }

    private static void corners(Canvas c, Rect box, int top, Style style) {
        int[] xs = {box.x0(), box.x1()};
        int[] zs = {box.z0(), box.z1()};
        for (int x : xs) {
            for (int z : zs) {
                c.fill(x, GROUND_Y, z, x, top + 1, z, style.trim());
            }
        }
    }

    private static void flight(Canvas c, Rect interior, int floorY, Block stairs) {
        int x = interior.x0() + 1;
        int z = interior.z0();
        for (int i = 0; i < STOREY; i++) {
            c.stair(x + i, floorY + 1 + i, z, Direction.EAST, stairs);
        }
        // Headroom through the slab above: the last step sits in the slab, the two before it need it gone.
        c.fill(x + STOREY - 3, floorY + STOREY, z, x + STOREY - 2, floorY + STOREY, z, Blocks.AIR);
    }

    private static void windows(Canvas c, Rect box, int floorY, boolean ground, Style style, long hash) {
        int spacing = 3 + CityHash.range(hash, 2);
        int low = floorY + 2;
        int high = floorY + 3;
        for (int x = box.x0() + 2; x <= box.x1() - 2; x += spacing) {
            windowPair(c, x, low, high, box.z0(), Direction.Axis.X, style);
            if (!ground || Math.abs(x - box.centerX()) > 1) {
                windowPair(c, x, low, high, box.z1(), Direction.Axis.X, style);
            }
        }
        for (int z = box.z0() + 2; z <= box.z1() - 2; z += spacing) {
            windowPair(c, box.x0(), low, high, z, Direction.Axis.Z, style);
            windowPair(c, box.x1(), low, high, z, Direction.Axis.Z, style);
        }
    }

    private static void windowPair(Canvas c, int x, int low, int high, int z, Direction.Axis axis, Style style) {
        if (!c.contains(x, z)) {
            return;
        }
        int roll = CityHash.range(c.hash(x, low, z), 11);
        Block pane = switch (roll) {
            case 0 -> Blocks.AIR;
            case 1, 2 -> Blocks.IRON_BARS;
            case 3, 4 -> Blocks.YELLOW_STAINED_GLASS_PANE;
            default -> style.glass();
        };
        for (int y = low; y <= high; y++) {
            if (pane == Blocks.AIR) {
                c.set(x, y, z, Blocks.AIR);
            } else {
                c.pane(x, y, z, axis, pane);
            }
        }
    }

    private static void pipes(Canvas c, Rect box, int top, long hash) {
        if (box.x1() - box.x0() < 8) {
            return;
        }
        int x = box.x0() + 2 + CityHash.range(hash >>> 8, box.width() - 4);
        BlockState chain = Blocks.CHAIN.defaultBlockState();
        for (int y = GROUND_Y + 1; y <= top; y += 1) {
            c.set(x, y, box.z0() - 1, chain);
        }
    }

    private static void roofDetail(Canvas c, Rect box, int top, long hash, Roof roof) {
        int y = top + 1;
        Rect deck = box.inset(1);
        switch (roof) {
            case PLAIN -> {
                int vents = 2 + CityHash.range(hash >>> 16, 3);
                for (int i = 0; i < vents; i++) {
                    long h = CityHash.mix(hash + i);
                    int x = deck.x0() + 6 + CityHash.range(h, Math.max(1, deck.width() - 7));
                    int z = deck.z0() + 2 + CityHash.range(h >>> 20, Math.max(1, deck.depth() - 3));
                    c.set(x, y, z, Blocks.IRON_TRAPDOOR);
                }
            }
            case TANK -> {
                int x = deck.x1() - 3;
                int z = deck.z1() - 3;
                if (deck.width() > 11 && deck.depth() > 7) {
                    c.set(x, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x + 2, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x, y, z + 2, Blocks.SPRUCE_FENCE);
                    c.set(x + 2, y, z + 2, Blocks.SPRUCE_FENCE);
                    c.fill(x, y + 1, z, x + 2, y + 2, z + 2, Blocks.SPRUCE_PLANKS);
                    c.fill(x + 1, y + 2, z + 1, x + 1, y + 2, z + 1, Blocks.WATER);
                }
            }
            case GARDEN -> garden(c, deck, top);
            case ANTENNA -> {
                int x = deck.x1() - 2;
                int z = deck.z1() - 2;
                c.fill(x, y, z, x, y + 9, z, Blocks.IRON_BARS);
                c.set(x, y + 10, z, Blocks.LIGHTNING_ROD);
            }
        }
    }

    /** Rooftop plots: the "urban farms" that keep wheat, carrots, potatoes and beetroot obtainable. */
    private static void garden(Canvas c, Rect deck, int top) {
        int y = top;
        int x0 = deck.x0() + 7;
        int z0 = deck.z0() + 2;
        int x1 = Math.min(deck.x1(), x0 + 8);
        int z1 = Math.min(deck.z1(), z0 + 6);
        if (x1 - x0 < 4 || z1 - z0 < 2) {
            return;
        }
        BlockState soil = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
        int waterX = (x0 + x1) >> 1;
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
        // A beam under the channel, or the water would drain through the roof slab into the top floor.
        c.fill(waterX, y - 1, z0, waterX, y - 1, z1, Blocks.SMOOTH_STONE);
        for (int x = x0; x <= x1; x++) {
            Block crop = crops[Math.floorMod(x - x0, crops.length)];
            for (int z = z0; z <= z1; z++) {
                if (x == waterX) {
                    c.set(x, y, z, Blocks.WATER);
                    continue;
                }
                c.set(x, y, z, soil);
                if (crop instanceof CropBlock cropBlock) {
                    int age = cropBlock.getMaxAge();
                    c.set(x, y + 1, z, cropBlock.getStateForAge(CityHash.range(c.hash(x, y, z), age + 1)));
                }
            }
        }
    }

    /** Ceiling light for a room. */
    static void lamp(Canvas c, int x, int floorY, int z) {
        c.set(x, floorY + STOREY - 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/HoleBiomes.java

```java
package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.city.District;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** Biome keys. Each district reads as its own biome, and everything below {@link #UNDERCITY} is mining ground. */
public final class HoleBiomes {
    public static final ResourceKey<Biome> RESIDENTIAL = key("hole_residential");
    public static final ResourceKey<Biome> MARKET = key("hole_market");
    public static final ResourceKey<Biome> INDUSTRIAL = key("hole_industrial");
    public static final ResourceKey<Biome> WATERFRONT = key("hole_waterfront");
    public static final ResourceKey<Biome> DERELICT = key("hole_derelict");
    public static final ResourceKey<Biome> GRAVEYARD = key("hole_graveyard");
    public static final ResourceKey<Biome> OUTSKIRTS = key("hole_outskirts");
    public static final ResourceKey<Biome> UNDERCITY = key("hole_undercity");

    private HoleBiomes() {}

    public static ResourceKey<Biome> forDistrict(District district) {
        return switch (district) {
            case RESIDENTIAL -> RESIDENTIAL;
            case MARKET -> MARKET;
            case INDUSTRIAL -> INDUSTRIAL;
            case WATERFRONT -> WATERFRONT;
            case DERELICT -> DERELICT;
            case GRAVEYARD -> GRAVEYARD;
            case OUTSKIRTS -> OUTSKIRTS;
        };
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/HoleBiomeSource.java

```java
package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.District;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Maps the city's districts to biomes at street level and picks cave biomes underground. Vanilla's lush, dripstone and
 * deep dark caves stay reachable below the city because their resources have no other source.
 */
public final class HoleBiomeSource extends BiomeSource {
    /** Below this height a column stops being city and uses underground biomes. */
    public static final int UNDERGROUND_TOP = 40;
    private static final int DEEP_DARK_TOP = -24;

    public static final MapCodec<HoleBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(HoleBiomes.RESIDENTIAL),
            RegistryOps.retrieveElement(HoleBiomes.MARKET),
            RegistryOps.retrieveElement(HoleBiomes.INDUSTRIAL),
            RegistryOps.retrieveElement(HoleBiomes.WATERFRONT),
            RegistryOps.retrieveElement(HoleBiomes.DERELICT),
            RegistryOps.retrieveElement(HoleBiomes.GRAVEYARD),
            RegistryOps.retrieveElement(HoleBiomes.OUTSKIRTS),
            RegistryOps.retrieveElement(HoleBiomes.UNDERCITY),
            RegistryOps.retrieveElement(Biomes.LUSH_CAVES),
            RegistryOps.retrieveElement(Biomes.DRIPSTONE_CAVES),
            RegistryOps.retrieveElement(Biomes.DEEP_DARK)
    ).apply(instance, instance.stable(HoleBiomeSource::new)));

    private final Holder<Biome> residential;
    private final Holder<Biome> market;
    private final Holder<Biome> industrial;
    private final Holder<Biome> waterfront;
    private final Holder<Biome> derelict;
    private final Holder<Biome> graveyard;
    private final Holder<Biome> outskirts;
    private final Holder<Biome> undercity;
    private final Holder<Biome> lushCaves;
    private final Holder<Biome> dripstoneCaves;
    private final Holder<Biome> deepDark;

    private HoleBiomeSource(Holder<Biome> residential, Holder<Biome> market, Holder<Biome> industrial, Holder<Biome> waterfront,
                            Holder<Biome> derelict, Holder<Biome> graveyard, Holder<Biome> outskirts, Holder<Biome> undercity, Holder<Biome> lushCaves,
                            Holder<Biome> dripstoneCaves, Holder<Biome> deepDark) {
        this.residential = residential;
        this.market = market;
        this.industrial = industrial;
        this.waterfront = waterfront;
        this.derelict = derelict;
        this.graveyard = graveyard;
        this.outskirts = outskirts;
        this.undercity = undercity;
        this.lushCaves = lushCaves;
        this.dripstoneCaves = dripstoneCaves;
        this.deepDark = deepDark;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(residential, market, industrial, waterfront, derelict, graveyard, outskirts, undercity, lushCaves, dripstoneCaves, deepDark);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockY = QuartPos.toBlock(y);
        int blockZ = QuartPos.toBlock(z);
        if (blockY < UNDERGROUND_TOP) {
            return underground(blockX, blockY, blockZ, sampler);
        }
        return switch (District.at(sampler, blockX, blockZ)) {
            case RESIDENTIAL -> residential;
            case MARKET -> market;
            case INDUSTRIAL -> industrial;
            case WATERFRONT -> waterfront;
            case DERELICT -> derelict;
            case GRAVEYARD -> graveyard;
            case OUTSKIRTS -> outskirts;
        };
    }

    private Holder<Biome> underground(int x, int y, int z, Climate.Sampler sampler) {
        // Rotated and offset so cave biomes do not line up with the districts above them.
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(z + 4099, 0, -x + 7919);
        double wet = sampler.humidity().compute(point);
        double warm = sampler.temperature().compute(point);
        if (y < DEEP_DARK_TOP && wet < -0.22 && warm < 0.0) {
            return deepDark;
        }
        if (wet > 0.30) {
            return lushCaves;
        }
        if (wet < -0.30) {
            return dripstoneCaves;
        }
        return undercity;
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/HoleChunkGenerator.java

```java
package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.CityPainter;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.CityPopulator;
import com.dorohedoro.hole.worldgen.city.CitySettings;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * The Overworld generator. Vanilla noise supplies the underground (caves, aquifers, ore veins, bedrock, deepslate), and
 * the city is painted over it at the surface step. Biome decoration and structures still run on top, which is how ores,
 * dungeons, mineshafts and strongholds survive the replacement.
 */
public final class HoleChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<HoleChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings),
            CitySettings.CODEC.optionalFieldOf("city", CitySettings.DEFAULT).forGetter(HoleChunkGenerator::citySettings)
    ).apply(instance, instance.stable(HoleChunkGenerator::new)));

    private final CitySettings citySettings;
    private volatile CityPlan plan;

    public HoleChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, CitySettings citySettings) {
        super(biomeSource, settings);
        this.citySettings = citySettings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public CitySettings citySettings() {
        return citySettings;
    }

    /** The world's plan. Only the seed and the seeded climate go in, so this is safe to build lazily on any thread. */
    public CityPlan plan(long seed, RandomState randomState) {
        CityPlan current = plan;
        if (current == null || current.seed != seed) {
            current = new CityPlan(seed, randomState.sampler(), citySettings);
            plan = current;
        }
        return current;
    }

    public CityPlan plan(ServerLevel level) {
        return plan(level.getSeed(), level.getChunkSource().randomState());
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        super.buildSurface(region, structureManager, random, chunk);
        CityPainter.paint(plan(region.getSeed(), random), chunk);
    }

    /** Carvers run after the surface step and would gouge the city, so all caves come from the noise router. */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        super.spawnOriginalMobs(region);
        CityPopulator.populate(region, plan(region.getSeed(), region.getLevel().getChunkSource().randomState()));
    }
}
```

## FILE: src/main/java/com/dorohedoro/hole/worldgen/WorldGuard.java

```java
package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Places the world spawn and detects worlds that are not the Hole. A world created without this mod keeps its original
 * generator in level.dat, so nothing can convert it silently. It is reported instead.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class WorldGuard {
    private WorldGuard() {}

    /** Replaces vanilla's biome-based spawn search, which has nothing to find in a city. */
    @SubscribeEvent
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.getChunkSource().getGenerator() instanceof HoleChunkGenerator generator) {
            BlockPos spawn = generator.plan(level).spawnPoint();
            event.getSettings().setSpawn(spawn, 0.0F);
            event.setCanceled(true);
            Dorohedoro.LOGGER.info("The Hole: world spawn set to {} on seed {}", spawn.toShortString(), level.getSeed());
        }
    }

    public static boolean isHoleWorld(ServerLevel overworld) {
        return overworld.getChunkSource().getGenerator() instanceof HoleChunkGenerator;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (isHoleWorld(overworld)) {
            Dorohedoro.LOGGER.info("The Hole: Overworld generator verified as {}", overworld.getChunkSource().getGenerator().getClass().getSimpleName());
        } else {
            Dorohedoro.LOGGER.warn("The Hole: this world does not use the Hole generator. It was left unchanged. Create a new world for the city.");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !isHoleWorld(player.getServer().overworld())) {
            player.sendSystemMessage(Component.translatable("dorohedoro.world.not_hole").withStyle(ChatFormatting.GOLD));
        }
    }
}
```

## FILE: docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/datagen/RealmBiomeData.java

```java
package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

/**
 * Biomes and ores of the Sorcerers' World and Hell. No Sorcerers' World biome has precipitation: weather is shared
 * between dimensions in vanilla, so the biome flag is the only thing that keeps that world dry during a Hole storm.
 */
final class RealmBiomeData {
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_COAL_C = configured("hell_ore_coal");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_IRON_C = configured("hell_ore_iron");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_COPPER_C = configured("hell_ore_copper");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_GOLD_C = configured("hell_ore_gold");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_REDSTONE_C = configured("hell_ore_redstone");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_DIAMOND_C = configured("hell_ore_diamond");

    private static final ResourceKey<PlacedFeature> HELL_COAL = placed("hell_ore_coal");
    private static final ResourceKey<PlacedFeature> HELL_IRON = placed("hell_ore_iron");
    private static final ResourceKey<PlacedFeature> HELL_COPPER = placed("hell_ore_copper");
    private static final ResourceKey<PlacedFeature> HELL_GOLD = placed("hell_ore_gold");
    private static final ResourceKey<PlacedFeature> HELL_REDSTONE = placed("hell_ore_redstone");
    private static final ResourceKey<PlacedFeature> HELL_DIAMOND = placed("hell_ore_diamond");

    private RealmBiomeData() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }

    /** Vanilla ores do not replace hell rock, so Hell declares its own veins. Mining stays worthwhile in every realm. */
    static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        ore(context, HELL_COAL_C, Blocks.DEEPSLATE_COAL_ORE, 14);
        ore(context, HELL_IRON_C, Blocks.DEEPSLATE_IRON_ORE, 9);
        ore(context, HELL_COPPER_C, Blocks.DEEPSLATE_COPPER_ORE, 10);
        ore(context, HELL_GOLD_C, Blocks.DEEPSLATE_GOLD_ORE, 9);
        ore(context, HELL_REDSTONE_C, Blocks.DEEPSLATE_REDSTONE_ORE, 8);
        ore(context, HELL_DIAMOND_C, Blocks.DEEPSLATE_DIAMOND_ORE, 7);
    }

    private static void ore(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, Block ore, int size) {
        FeatureUtils.register(context, key, Feature.ORE, new OreConfiguration(
                List.of(OreConfiguration.target(new BlockMatchTest(ModBlocks.HELL_ROCK.get()), ore.defaultBlockState())), size));
    }

    static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        vein(context, features, HELL_COAL, HELL_COAL_C, 24, -40, 200);
        vein(context, features, HELL_IRON, HELL_IRON_C, 16, -50, 120);
        vein(context, features, HELL_COPPER, HELL_COPPER_C, 12, -40, 120);
        vein(context, features, HELL_GOLD, HELL_GOLD_C, 8, -60, 70);
        vein(context, features, HELL_REDSTONE, HELL_REDSTONE_C, 8, -60, 40);
        vein(context, features, HELL_DIAMOND, HELL_DIAMOND_C, 5, -60, 24);
    }

    private static void vein(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> features,
                             ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> configured, int count, int minY, int maxY) {
        PlacementUtils.register(context, key, features.getOrThrow(configured), List.of(
                CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)), BiomeFilter.biome()));
    }

    static void biomes(BootstrapContext<Biome> context) {
        sorcerers(context);
        hell(context);
    }

    private record Air(int fog, int sky, int water, int waterFog, int grass, int foliage) {}

    private static final Air OPEN_SKY = new Air(0xB9C6C4, 0x8FB4C4, 0x3F76E4, 0x050533, 0x7DA24E, 0x5F8F3E);
    private static final Air CITY_AIR = new Air(0xA9A7A0, 0x8D9CA6, 0x3F6D9E, 0x050533, 0x76924E, 0x5C7E3C);
    private static final Air DEEP_WOOD = new Air(0x7D9184, 0x6E8E86, 0x3D6A6E, 0x06242A, 0x3E6B38, 0x2F5A2D);
    private static final Air BLIGHT_AIR = new Air(0x9C8E8E, 0x8A7F86, 0x5A5A6E, 0x1B1420, 0x7A6E56, 0x6C5F4C);

    private static void sorcerers(BootstrapContext<Biome> context) {
        context.register(RealmBiomes.SW_CITY, sorcererBiome(CITY_AIR, sorcererSpawns(false), city(context)));
        context.register(RealmBiomes.SW_CITY_EDGE, sorcererBiome(CITY_AIR, sorcererSpawns(true), countryside(context)));
        context.register(RealmBiomes.SW_FARMLAND, sorcererBiome(OPEN_SKY, sorcererSpawns(true), countryside(context)));
        context.register(RealmBiomes.SW_WOODLAND, sorcererBiome(OPEN_SKY, sorcererSpawns(true), woodland(context)));
        context.register(RealmBiomes.SW_HYDRA_FOREST, sorcererBiome(DEEP_WOOD, sorcererSpawns(true), hydraForest(context)));
        context.register(RealmBiomes.SW_BLIGHT, sorcererBiome(BLIGHT_AIR, sorcererSpawns(false), blight(context)));

        MobSpawnSettings.Builder waterLife = new MobSpawnSettings.Builder();
        waterLife.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 4, 1, 2));
        waterLife.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 12, 3, 5));
        waterLife.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 8, 2, 4));
        context.register(RealmBiomes.SW_WATERS, sorcererBiome(OPEN_SKY, waterLife, waters(context)));

        // Only the deep underground keeps vanilla's hostile cave life. Its drops stay reachable for anyone living here.
        MobSpawnSettings.Builder caveLife = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.caveSpawns(caveLife);
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 60, 2, 3));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 60, 1, 3));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 20, 1, 2));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 40, 2, 3));
        context.register(RealmBiomes.SW_CAVES, sorcererBiome(OPEN_SKY, caveLife, countryside(context)));
    }

    private static MobSpawnSettings.Builder sorcererSpawns(boolean farmAnimals) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        if (farmAnimals) {
            BiomeDefaultFeatures.farmAnimals(spawns);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 4, 2, 4));
        }
        return spawns;
    }

    private static Biome sorcererBiome(Air air, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(air.fog).skyColor(air.sky).waterColor(air.water).waterFogColor(air.waterFog)
                        .grassColorOverride(air.grass).foliageColorOverride(air.foliage)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    /** The vanilla feature order every Sorcerers' World biome shares, which is what keeps the feature sorter happy. */
    private static BiomeGenerationSettings.Builder base(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        return generation;
    }

    private static void minerals(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
    }

    private static BiomeGenerationSettings.Builder city(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder countryside(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder woodland(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        BiomeDefaultFeatures.addOtherBirchTrees(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder hydraForest(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
        minerals(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder blight(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder waters(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        return generation;
    }

    private static final Air ASH = new Air(0x3A3532, 0x1C1816, 0x2E2A2A, 0x0E0C0C, 0x4A4438, 0x3E382E);
    private static final Air HORN_AIR = new Air(0x4A2E2A, 0x241210, 0x2E2A2A, 0x0E0C0C, 0x4A4438, 0x3E382E);
    private static final Air SOUL_AIR = new Air(0x2E3A40, 0x141C20, 0x2E3A40, 0x0E1416, 0x3A4A48, 0x2E3E3C);
    private static final Air BLACK_AIR = new Air(0x161413, 0x0C0A0A, 0x1E1C1C, 0x080707, 0x2E2A26, 0x26221E);

    private static void hell(BootstrapContext<Biome> context) {
        context.register(RealmBiomes.HELL_WASTES, hellBiome(ASH, 0.004F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_HORNS, hellBiome(HORN_AIR, 0.002F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_SOUL_FLATS, hellBiome(SOUL_AIR, 0.006F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_CAVERNS, hellBiome(BLACK_AIR, 0.0F, hellGeneration(context)));
    }

    private static Biome hellBiome(Air air, float ash, BiomeGenerationSettings.Builder generation) {
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(air.fog).skyColor(air.sky).waterColor(air.water).waterFogColor(air.waterFog)
                .grassColorOverride(air.grass).foliageColorOverride(air.foliage)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        if (ash > 0.0F) {
            effects.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, ash));
        }
        // Devils replace this empty table in Phase 7. Reskinned vanilla undead are ruled out by the spec.
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(1.2F)
                .downfall(0.0F)
                .specialEffects(effects.build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(generation.build())
                .build();
    }

    private static BiomeGenerationSettings.Builder hellGeneration(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        for (ResourceKey<PlacedFeature> vein : List.of(HELL_COAL, HELL_IRON, HELL_COPPER, HELL_GOLD, HELL_REDSTONE, HELL_DIAMOND)) {
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, vein);
        }
        generation.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
        return generation;
    }
}
```

## FILE: docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/HellBiomeSource.java

```java
package com.dorohedoro.hole.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Biomes of Hell. The horn regions match the terrain's region noise, routed through the climate depth slot. */
public final class HellBiomeSource extends BiomeSource {
    private static final int UNDERGROUND_TOP = 54;

    public static final MapCodec<HellBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(RealmBiomes.HELL_WASTES),
            RegistryOps.retrieveElement(RealmBiomes.HELL_HORNS),
            RegistryOps.retrieveElement(RealmBiomes.HELL_SOUL_FLATS),
            RegistryOps.retrieveElement(RealmBiomes.HELL_CAVERNS)
    ).apply(instance, instance.stable(HellBiomeSource::new)));

    private final Holder<Biome> wastes;
    private final Holder<Biome> horns;
    private final Holder<Biome> soulFlats;
    private final Holder<Biome> caverns;

    private HellBiomeSource(Holder<Biome> wastes, Holder<Biome> horns, Holder<Biome> soulFlats, Holder<Biome> caverns) {
        this.wastes = wastes;
        this.horns = horns;
        this.soulFlats = soulFlats;
        this.caverns = caverns;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(wastes, horns, soulFlats, caverns);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (QuartPos.toBlock(y) < UNDERGROUND_TOP) {
            return caverns;
        }
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(QuartPos.toBlock(x), 0, QuartPos.toBlock(z));
        double regions = sampler.depth().compute(point);
        if (regions > 0.0) {
            return horns;
        }
        return sampler.humidity().compute(point) > 0.12 ? soulFlats : wastes;
    }
}
```

## FILE: docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/RealmBiomes.java

```java
package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Dimension and biome keys for the two realms beyond the Hole. */
public final class RealmBiomes {
    public static final ResourceKey<Level> SORCERERS_WORLD = ResourceKey.create(Registries.DIMENSION, id("sorcerers_world"));
    public static final ResourceKey<Level> HELL = ResourceKey.create(Registries.DIMENSION, id("hell"));

    public static final ResourceKey<Biome> SW_CITY = biome("sw_city");
    public static final ResourceKey<Biome> SW_CITY_EDGE = biome("sw_city_edge");
    public static final ResourceKey<Biome> SW_FARMLAND = biome("sw_farmland");
    public static final ResourceKey<Biome> SW_WOODLAND = biome("sw_woodland");
    public static final ResourceKey<Biome> SW_HYDRA_FOREST = biome("sw_hydra_forest");
    public static final ResourceKey<Biome> SW_BLIGHT = biome("sw_blight");
    public static final ResourceKey<Biome> SW_WATERS = biome("sw_waters");
    public static final ResourceKey<Biome> SW_CAVES = biome("sw_caves");

    public static final ResourceKey<Biome> HELL_WASTES = biome("hell_wastes");
    public static final ResourceKey<Biome> HELL_HORNS = biome("hell_horns");
    public static final ResourceKey<Biome> HELL_SOUL_FLATS = biome("hell_soul_flats");
    public static final ResourceKey<Biome> HELL_CAVERNS = biome("hell_caverns");

    private RealmBiomes() {}

    private static ResourceKey<Biome> biome(String name) {
        return ResourceKey.create(Registries.BIOME, id(name));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name);
    }
}
```

## FILE: docs/handoff/phase3-draft/src/main/java/com/dorohedoro/hole/worldgen/SorcererBiomeSource.java

```java
package com.dorohedoro.hole.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Biomes of the Sorcerers' World. City land is read back from the terrain's basin noise, routed through the climate
 * depth slot, so city biomes always sit on the flattened ground the noise settings carve out for them.
 */
public final class SorcererBiomeSource extends BiomeSource {
    private static final int UNDERGROUND_TOP = 40;
    private static final double CITY = 0.42;
    private static final double CITY_EDGE = 0.32;

    public static final MapCodec<SorcererBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(RealmBiomes.SW_CITY),
            RegistryOps.retrieveElement(RealmBiomes.SW_CITY_EDGE),
            RegistryOps.retrieveElement(RealmBiomes.SW_FARMLAND),
            RegistryOps.retrieveElement(RealmBiomes.SW_WOODLAND),
            RegistryOps.retrieveElement(RealmBiomes.SW_HYDRA_FOREST),
            RegistryOps.retrieveElement(RealmBiomes.SW_BLIGHT),
            RegistryOps.retrieveElement(RealmBiomes.SW_WATERS),
            RegistryOps.retrieveElement(RealmBiomes.SW_CAVES)
    ).apply(instance, instance.stable(SorcererBiomeSource::new)));

    private final Holder<Biome> city;
    private final Holder<Biome> cityEdge;
    private final Holder<Biome> farmland;
    private final Holder<Biome> woodland;
    private final Holder<Biome> hydraForest;
    private final Holder<Biome> blight;
    private final Holder<Biome> waters;
    private final Holder<Biome> caves;

    private SorcererBiomeSource(Holder<Biome> city, Holder<Biome> cityEdge, Holder<Biome> farmland, Holder<Biome> woodland,
                                Holder<Biome> hydraForest, Holder<Biome> blight, Holder<Biome> waters, Holder<Biome> caves) {
        this.city = city;
        this.cityEdge = cityEdge;
        this.farmland = farmland;
        this.woodland = woodland;
        this.hydraForest = hydraForest;
        this.blight = blight;
        this.waters = waters;
        this.caves = caves;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(city, cityEdge, farmland, woodland, hydraForest, blight, waters, caves);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);
        if (QuartPos.toBlock(y) < UNDERGROUND_TOP) {
            return caves;
        }
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(blockX, 0, blockZ);
        double basin = sampler.depth().compute(point);
        if (basin > CITY) {
            return city;
        }
        if (basin > CITY_EDGE) {
            return cityEdge;
        }
        double continents = sampler.continentalness().compute(point);
        double weirdness = sampler.weirdness().compute(point);
        if (continents < -0.19 || (continents < 0.25 && Math.abs(weirdness) < 0.035)) {
            return waters;
        }
        double warmth = sampler.temperature().compute(point);
        double moisture = sampler.humidity().compute(point);
        if (warmth > 0.34 && moisture < -0.12) {
            return blight;
        }
        if (moisture > 0.16) {
            return hydraForest;
        }
        return moisture > -0.14 ? woodland : farmland;
    }
}
```

## FILE: docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension_type/hell.json

```json
{
  "ambient_light": 0.06, "bed_works": true, "coordinate_scale": 1.0, "effects": "minecraft:overworld",
  "fixed_time": 18000, "has_ceiling": false, "has_raids": false, "has_skylight": false, "height": 384,
  "infiniburn": "#minecraft:infiniburn_overworld", "logical_height": 384, "min_y": -64,
  "monster_spawn_block_light_limit": 15,
  "monster_spawn_light_level": { "type": "minecraft:uniform", "max_inclusive": 11, "min_inclusive": 0 },
  "natural": false, "piglin_safe": false, "respawn_anchor_works": false, "ultrawarm": false
}
```

## FILE: docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension_type/sorcerers_world.json

```json
{
  "ambient_light": 0.0, "bed_works": true, "coordinate_scale": 1.0, "effects": "minecraft:overworld",
  "has_ceiling": false, "has_raids": false, "has_skylight": true, "height": 384,
  "infiniburn": "#minecraft:infiniburn_overworld", "logical_height": 384, "min_y": -64,
  "monster_spawn_block_light_limit": 0,
  "monster_spawn_light_level": { "type": "minecraft:uniform", "max_inclusive": 7, "min_inclusive": 0 },
  "natural": true, "piglin_safe": false, "respawn_anchor_works": false, "ultrawarm": false
}
```

## FILE: docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension/hell.json

```json
{
  "type": "dorohedoro:hell",
  "generator": {
    "type": "minecraft:noise",
    "biome_source": { "type": "dorohedoro:hell_realms" },
    "settings": "dorohedoro:hell"
  }
}
```

## FILE: docs/handoff/phase3-draft/src/main/resources/data/dorohedoro/dimension/sorcerers_world.json

```json
{
  "type": "dorohedoro:sorcerers_world",
  "generator": {
    "type": "minecraft:noise",
    "biome_source": { "type": "dorohedoro:sorcerer_lands" },
    "settings": "dorohedoro:sorcerers_world"
  }
}
```

## FILE: tools/make-handoff.mjs

```js
// Rebuilds HANDOFF.md: the brief, then every text file in the repo, so one file carries the whole project.
// Files appear most useful first. Generated data comes last. Re-run with: node tools/make-handoff.mjs
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';
import { extname } from 'node:path';

const BRIEF = 'docs/HANDOFF_BRIEF.md';
const OUTPUT = 'HANDOFF.md';
const binary = new Set(['.png', '.jar', '.zip', '.ogg', '.bundle']);
const fenceFor = { '.java': 'java', '.json': 'json', '.gradle': 'groovy', '.ps1': 'powershell', '.sh': 'bash', '.mjs': 'js', '.toml': 'toml', '.md': 'markdown', '.csv': 'csv', '.properties': 'properties' };

const listed = execFileSync('git', ['ls-files', '--cached', '--others', '--exclude-standard'], { encoding: 'utf8' })
  .split('\n').map((line) => line.trim()).filter(Boolean)
  .filter((file) => file !== OUTPUT && file !== BRIEF && !binary.has(extname(file)) && !file.startsWith('gradle/wrapper/') && !file.includes('.cache/'));

const rank = (file) => {
  if (file.startsWith('docs/TEST_RESULTS') || file.startsWith('docs/IMPLEMENTATION_STATUS') || file.startsWith('docs/ROADMAP')) return 0;
  if (file.startsWith('docs/spec/') && file.endsWith('DIRECTIVE.md')) return 1;
  if (['build.gradle', 'gradle.properties', 'settings.gradle'].includes(file) || file.endsWith('neoforge.mods.toml')) return 2;
  if (file.startsWith('src/main/java/')) return 3;
  if (file.startsWith('docs/handoff/')) return 4;
  if (file.startsWith('tools/')) return 5;
  if (file.startsWith('src/main/resources/') && !file.endsWith('noise_settings/hole.json')) return 6;
  if (file.startsWith('docs/')) return 7;
  if (file.startsWith('src/generated/') || file.endsWith('noise_settings/hole.json')) return 9;
  return 8;
};
const files = listed.sort((a, b) => rank(a) - rank(b) || a.localeCompare(b));

const fenceAround = (text) => {
  const longest = Math.max(2, ...[...text.matchAll(/`{3,}/g)].map((m) => m[0].length));
  return '`'.repeat(longest + 1);
};

const parts = [readFileSync(BRIEF, 'utf8').trimEnd(), '', '## Contents of the rest of this file', '', `${files.length} files follow. Generated JSON is last and can be rebuilt with \`runData\`. Binary files are not included: three PNG textures rebuilt by \`tools/make-textures.mjs\`, and the Gradle wrapper jar, which comes from the NeoForge MDK template.`, ''];
for (const file of files) parts.push(`- \`${file}\``);
parts.push('');
for (const file of files) {
  const text = readFileSync(file, 'utf8').replace(/\r\n/g, '\n');
  const fence = fenceAround(text);
  parts.push(`## FILE: ${file}`, '', fence + (fenceFor[extname(file)] ?? ''), text.trimEnd(), fence, '');
}
writeFileSync(OUTPUT, parts.join('\n'));
console.log(`${OUTPUT}: ${files.length} files, ${Buffer.byteLength(parts.join('\n')) >> 10} KB`);
```

## FILE: tools/make-noise-settings.ps1

```powershell
# Rebuilds src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json.
#
# The Hole keeps the vanilla 1.21.1 underground (caves, aquifers, ore veins, bedrock, deepslate) and swaps the
# terrain shape for a flat ground plane, with caves capped below the sewers. The fluid and vein functions are read
# from the local client jar so they always match the pinned Minecraft version.
param(
    [string]$ClientJar = "$env:USERPROFILE\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar",
    [string]$Out = "src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json"
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [IO.Compression.ZipFile]::OpenRead($ClientJar)
try {
    $entry = $zip.GetEntry('data/minecraft/worldgen/noise_settings/overworld.json')
    $reader = New-Object IO.StreamReader($entry.Open())
    $vanilla = $reader.ReadToEnd() | ConvertFrom-Json
    $reader.Close()
} finally {
    $zip.Dispose()
}

function Gradient([int]$fromY, [int]$toY, [double]$fromValue, [double]$toValue) {
    [ordered]@{ type = 'minecraft:y_clamped_gradient'; from_y = $fromY; to_y = $toY; from_value = $fromValue; to_value = $toValue }
}
function Op([string]$type, $a, $b) { [ordered]@{ type = "minecraft:$type"; argument1 = $a; argument2 = $b } }
function Noise([string]$name, [double]$xz, [double]$y) { [ordered]@{ type = 'minecraft:noise'; noise = "minecraft:$name"; xz_scale = $xz; y_scale = $y } }

# Mirrors NoiseRouterData.underground, without the surface-relative sloped cheese term.
$caveLayer = Op 'mul' 4.0 ([ordered]@{ type = 'minecraft:square'; argument = (Noise 'cave_layer' 1.0 8.0) })
$cheese = [ordered]@{ type = 'minecraft:clamp'; input = (Op 'add' 0.27 (Noise 'cave_cheese' 1.0 0.6666666666666666)); min = -1.0; max = 1.0 }
$caves = Op 'min' (Op 'min' (Op 'add' $caveLayer $cheese) 'minecraft:overworld/caves/entrances') (Op 'add' 'minecraft:overworld/caves/spaghetti_2d' 'minecraft:overworld/caves/spaghetti_roughness_function')
$pillars = [ordered]@{
    type = 'minecraft:range_choice'; input = 'minecraft:overworld/caves/pillars'
    min_inclusive = -1000000.0; max_exclusive = 0.03
    when_in_range = -1000000.0; when_out_of_range = 'minecraft:overworld/caves/pillars'
}
$underground = Op 'max' $caves $pillars

# Above y=26 caves fade out so nothing natural breaches the sewers. Near bedrock they fade out the same way.
$capTop = Gradient 26 44 -1.5 1.5
$capBottom = Gradient -64 -56 1.5 -1.5
$cappedCaves = Op 'max' (Op 'max' $underground $capTop) $capBottom
$ground = Gradient 58 66 2.0 -2.0
$solid = Op 'min' $ground $cappedCaves

$finalDensity = Op 'min' ([ordered]@{
    type = 'minecraft:squeeze'
    argument = (Op 'mul' 0.64 ([ordered]@{ type = 'minecraft:interpolated'; argument = [ordered]@{ type = 'minecraft:blend_density'; argument = $solid } }))
}) (Op 'max' (Op 'max' 'minecraft:overworld/caves/noodle' $capTop) $capBottom)

$bedrock = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:bedrock_floor'; true_at_and_below = @{ above_bottom = 0 }; false_at_and_above = @{ above_bottom = 5 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'minecraft:bedrock' } }
}
$deepslate = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:deepslate'; true_at_and_below = @{ absolute = 0 }; false_at_and_above = @{ absolute = 8 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = [ordered]@{ Name = 'minecraft:deepslate'; Properties = @{ axis = 'y' } } }
}

$router = [ordered]@{
    barrier = $vanilla.noise_router.barrier
    continents = 0.0
    depth = 0.0
    erosion = 0.0
    final_density = $finalDensity
    fluid_level_floodedness = $vanilla.noise_router.fluid_level_floodedness
    fluid_level_spread = $vanilla.noise_router.fluid_level_spread
    initial_density_without_jaggedness = (Gradient 58 66 1.0 -1.0)
    lava = $vanilla.noise_router.lava
    ridges = 0.0
    temperature = $vanilla.noise_router.temperature
    vegetation = $vanilla.noise_router.vegetation
    vein_gap = $vanilla.noise_router.vein_gap
    vein_ridged = $vanilla.noise_router.vein_ridged
    vein_toggle = $vanilla.noise_router.vein_toggle
}

$settings = [ordered]@{
    aquifers_enabled = $true
    default_block = $vanilla.default_block
    default_fluid = $vanilla.default_fluid
    disable_mob_generation = $false
    legacy_random_source = $false
    noise = $vanilla.noise
    noise_router = $router
    ore_veins_enabled = $true
    sea_level = 63
    spawn_target = $vanilla.spawn_target
    surface_rule = [ordered]@{ type = 'minecraft:sequence'; sequence = @($bedrock, $deepslate) }
}

$dir = Split-Path $Out -Parent
New-Item -ItemType Directory -Force $dir | Out-Null
$json = $settings | ConvertTo-Json -Depth 60
[IO.File]::WriteAllText((Join-Path (Get-Location) $Out), $json + "`n", (New-Object Text.UTF8Encoding($false)))
"wrote $Out ($($json.Length) chars)"
```

## FILE: tools/make-realm-settings.ps1

```powershell
# Rebuilds the noise settings for the Sorcerers' World and Hell from the pinned client jar.
#
# Sorcerers' World keeps vanilla's natural terrain, caves and aquifers, and blends it into flat basins where cities sit.
# The basin noise is also routed through the climate "depth" slot, which terrain does not read, so the biome source can
# find city land without any shared state.
#
# Hell is built from scratch and deliberately avoids the Nether's shape: open wastes under an open sky, tablelands,
# caverns, and huge curved horns. There is no lava sea and no ceiling.
param(
    [string]$ClientJar = "$env:USERPROFILE\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar",
    [string]$OutDir = "src/main/resources/data/dorohedoro/worldgen/noise_settings"
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [IO.Compression.ZipFile]::OpenRead($ClientJar)
try {
    $entry = $zip.GetEntry('data/minecraft/worldgen/noise_settings/overworld.json')
    $reader = New-Object IO.StreamReader($entry.Open())
    $vanilla = $reader.ReadToEnd() | ConvertFrom-Json
    $reader.Close()
} finally {
    $zip.Dispose()
}

function Gradient([int]$fromY, [int]$toY, [double]$fromValue, [double]$toValue) {
    [ordered]@{ type = 'minecraft:y_clamped_gradient'; from_y = $fromY; to_y = $toY; from_value = $fromValue; to_value = $toValue }
}
function Op([string]$type, $a, $b) { [ordered]@{ type = "minecraft:$type"; argument1 = $a; argument2 = $b } }
function Noise([string]$name, [double]$xz, [double]$y) { [ordered]@{ type = 'minecraft:noise'; noise = $name; xz_scale = $xz; y_scale = $y } }
function Clamp($input, [double]$min, [double]$max) { [ordered]@{ type = 'minecraft:clamp'; input = $input; min = $min; max = $max } }
function Square($a) { [ordered]@{ type = 'minecraft:square'; argument = $a } }

$utf8 = New-Object Text.UTF8Encoding($false)
function Save($object, [string]$path) {
    $full = Join-Path (Get-Location) $path
    New-Item -ItemType Directory -Force (Split-Path $full -Parent) | Out-Null
    $json = $object | ConvertTo-Json -Depth 80
    [IO.File]::WriteAllText($full, $json + "`n", $utf8)
    "wrote $path ($($json.Length) chars)"
}

# Noise parameters. firstOctave sets feature size: -9 is roughly 512 blocks, -6 roughly 64.
Save ([ordered]@{ firstOctave = -9; amplitudes = @(1.0, 1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/city_basin.json'
Save ([ordered]@{ firstOctave = -6; amplitudes = @(1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_horns.json'
Save ([ordered]@{ firstOctave = -9; amplitudes = @(1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_regions.json'
Save ([ordered]@{ firstOctave = -7; amplitudes = @(1.0, 0.75, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_mesas.json'
Save ([ordered]@{ firstOctave = -5; amplitudes = @(1.0, 0.5, 0.25) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_ground.json'

# Caves shared by city basins: vanilla's underground, capped so nothing natural opens under foundations.
$caveLayer = Op 'mul' 4.0 (Square (Noise 'minecraft:cave_layer' 1.0 8.0))
$cheese = Clamp (Op 'add' 0.27 (Noise 'minecraft:cave_cheese' 1.0 0.6666666666666666)) -1.0 1.0
$caves = Op 'min' (Op 'min' (Op 'add' $caveLayer $cheese) 'minecraft:overworld/caves/entrances') (Op 'add' 'minecraft:overworld/caves/spaghetti_2d' 'minecraft:overworld/caves/spaghetti_roughness_function')

# --- Sorcerers' World ---------------------------------------------------------------------------------------------
$basin = Noise 'dorohedoro:city_basin' 1.0 0.0
$mask = Clamp (Op 'mul' (Op 'add' $basin -0.30) 7.0) 0.0 1.0
$inverse = Op 'add' 1.0 (Op 'mul' -1.0 $mask)
$cityGround = Op 'min' (Gradient 68 76 2.0 -2.0) (Op 'max' (Op 'max' $caves (Gradient 30 48 -1.5 1.5)) (Gradient -64 -56 1.5 -1.5))

$natural = $vanilla.noise_router.final_density.argument1.argument.argument2.argument.argument
$blended = Op 'add' (Op 'mul' $mask $cityGround) (Op 'mul' $inverse $natural)

$finalDensity = $vanilla.noise_router.final_density | ConvertTo-Json -Depth 80 | ConvertFrom-Json
$finalDensity.argument1.argument.argument2.argument.argument = $blended

$router = [ordered]@{}
foreach ($name in $vanilla.noise_router.PSObject.Properties.Name) { $router[$name] = $vanilla.noise_router.$name }
$router['final_density'] = $finalDensity
$router['depth'] = $basin

Save ([ordered]@{
    aquifers_enabled = $true
    default_block = $vanilla.default_block
    default_fluid = $vanilla.default_fluid
    disable_mob_generation = $false
    legacy_random_source = $false
    noise = $vanilla.noise
    noise_router = $router
    ore_veins_enabled = $true
    sea_level = 63
    spawn_target = $vanilla.spawn_target
    surface_rule = $vanilla.surface_rule
}) "$OutDir/sorcerers_world.json"

# --- Hell ---------------------------------------------------------------------------------------------------------
# Column height comes from three layers: gently broken ground around y=70, flat-topped mesas, and horns.
$slope = Op 'add' (Gradient 30 270 1.0 -1.0) -0.667
$ground = Op 'mul' 0.05 (Noise 'dorohedoro:hell_ground' 1.0 0.0)
$mesa = Op 'mul' 0.30 (Clamp (Op 'mul' (Op 'add' (Noise 'dorohedoro:hell_mesas' 1.0 0.0) -0.32) 9.0) 0.0 1.0)

# Regions decide where horns stand. Everywhere else stays open waste.
$regions = Noise 'dorohedoro:hell_regions' 1.0 0.0
$hornField = Clamp (Op 'mul' (Op 'add' $regions 0.05) 6.0) 0.0 1.0

# Shifting the sample point with height bends a straight spike into a curved horn.
$hornNoise = [ordered]@{
    type = 'minecraft:shifted_noise'; noise = 'dorohedoro:hell_horns'
    shift_x = (Gradient 70 280 0.0 34.0); shift_y = 0.0; shift_z = (Gradient 90 280 0.0 -18.0)
    xz_scale = 1.0; y_scale = 0.0
}
$hornCore = Clamp (Op 'mul' (Op 'add' $hornNoise -0.40) 5.0) 0.0 1.0
$horn = Op 'mul' 1.50 (Op 'mul' $hornField (Square $hornCore))

$surface = Op 'add' $slope (Op 'add' $ground (Op 'add' $mesa $horn))

# Vast caverns below the wastes, closed off from bedrock and the surface.
$cavern = Op 'add' 0.22 (Noise 'minecraft:cave_cheese' 0.5 0.3)
$cavernCap = Op 'max' $cavern (Gradient 24 54 -1.5 1.5)
$cavernFloor = Op 'max' $cavernCap (Gradient -64 -50 1.5 -1.5)
$solid = Op 'min' $surface $cavernFloor

$hellRouter = [ordered]@{
    barrier = 0.0
    continents = 0.0
    depth = $regions
    erosion = 0.0
    final_density = [ordered]@{ type = 'minecraft:squeeze'; argument = (Op 'mul' 0.64 ([ordered]@{ type = 'minecraft:interpolated'; argument = $solid })) }
    fluid_level_floodedness = 0.0
    fluid_level_spread = 0.0
    initial_density_without_jaggedness = $slope
    lava = 0.0
    ridges = 0.0
    temperature = $vanilla.noise_router.temperature
    vegetation = $vanilla.noise_router.vegetation
    vein_gap = 0.0
    vein_ridged = 0.0
    vein_toggle = 0.0
}

$bedrock = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:bedrock_floor'; true_at_and_below = @{ above_bottom = 0 }; false_at_and_above = @{ above_bottom = 5 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'minecraft:bedrock' } }
}
$ash = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:stone_depth'; offset = 0; add_surface_depth = $true; secondary_depth_range = 0; surface_type = 'floor' }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'dorohedoro:ash_soil' } }
}

Save ([ordered]@{
    aquifers_enabled = $false
    default_block = @{ Name = 'dorohedoro:hell_rock' }
    default_fluid = @{ Name = 'minecraft:air' }
    disable_mob_generation = $true
    legacy_random_source = $false
    noise = $vanilla.noise
    noise_router = $hellRouter
    ore_veins_enabled = $false
    sea_level = -64
    spawn_target = @()
    surface_rule = [ordered]@{ type = 'minecraft:sequence'; sequence = @($bedrock, $ash) }
}) "$OutDir/hell.json"
```

## FILE: tools/make-textures.mjs

```js
// Generates the mod's block and item textures as 16x PNGs, using only Node built-ins.
// All art here is procedural and original. Re-run with: node tools/make-textures.mjs
import { deflateSync } from 'node:zlib';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = crcTable[(c ^ b) & 255] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type), data]);
  const sum = Buffer.alloc(4);
  sum.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, sum]);
}
function png(size, pixel) {
  const rows = [];
  for (let y = 0; y < size; y++) {
    const row = Buffer.alloc(1 + size * 4);
    for (let x = 0; x < size; x++) row.set(pixel(x, y), 1 + x * 4);
    rows.push(row);
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(size, 0);
  header.writeUInt32BE(size, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(Buffer.concat(rows))),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
const noise = (x, y, s) => {
  let h = Math.imul((x * 73856093) ^ (y * 19349663) ^ (s * 83492791), 0x5bd1e995);
  h ^= h >>> 13;
  h = Math.imul(h, 0x5bd1e995);
  return ((h ^ (h >>> 15)) >>> 0) / 4294967295;
};
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const save = (path, data) => {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, data);
};
const root = 'src/main/resources/assets/dorohedoro/textures';

// Riveted sheet steel, streaked with rust running down from the seams.
save(`${root}/block/rusted_plating.png`, png(16, (x, y) => {
  const steel = [92, 74, 62];
  const rust = [146, 76, 38];
  const dark = [48, 38, 34];
  const streak = noise(x, 0, 5) * 0.7 + noise(x, y >> 2, 6) * 0.3;
  let c = mix(steel, rust, Math.min(1, streak * 1.1 + y / 40));
  c = mix(c, [c[0] + 14, c[1] + 10, c[2] + 8], noise(x, y, 7) * 0.5);
  if (y === 0 || y === 15 || x === 0 || x === 15) c = dark;
  if ((x === 2 || x === 13) && (y === 2 || y === 13)) c = [176, 150, 120];
  return [...c, 255];
}));

// A compacted heap of pipe ends, gears and sheet offcuts.
save(`${root}/block/industrial_debris.png`, png(16, (x, y) => {
  const cell = noise(x >> 2, y >> 2, 11);
  const palette = [[70, 62, 56], [112, 72, 44], [58, 66, 62], [134, 96, 58], [40, 36, 34]];
  let c = palette[Math.floor(cell * palette.length) % palette.length];
  c = mix(c, [24, 20, 18], noise(x, y, 12) * 0.45);
  if ((x & 3) === 0 && noise(x, y, 13) > 0.55) c = [22, 18, 16];
  return [...c, 255];
}));

// A twisted shard of salvaged metal.
save(`${root}/item/scrap_metal.png`, png(16, (x, y) => {
  const inShard = Math.abs(x - y) < 5 && x + y > 7 && x + y < 24 && noise(x, y, 21) > 0.12;
  if (!inShard) return [0, 0, 0, 0];
  const edge = Math.abs(x - y) === 4;
  const base = mix([120, 112, 104], [150, 84, 44], noise(x, y, 22));
  return [...(edge ? mix(base, [40, 34, 30], 0.6) : base), 255];
}));
console.log('textures written');
```

## FILE: tools/qa-compare.mjs

```js
// Compares two QA reports for the same seed and reports chunks whose contents differ.
//   node tools/qa-compare.mjs <report-a.json> <report-b.json>
import { readFileSync } from 'node:fs';
const [a, b] = process.argv.slice(2).map((p) => JSON.parse(readFileSync(p, 'utf8')));
if (a.seed !== b.seed) { console.error('seeds differ'); process.exit(2); }
function diff(key) {
  const x = a[key], y = b[key];
  const keys = Object.keys(x).filter((k) => k in y);
  const bad = keys.filter((k) => x[k] !== y[k]);
  console.log(key + ': ' + (keys.length - bad.length) + '/' + keys.length + ' identical' + (bad.length ? ', differing e.g. ' + bad.slice(0, 6).join(' ') : ''));
  return bad.length;
}
const city = diff('chunk_hashes');
const full = diff('chunk_hashes_full');
process.exit(city ? 1 : 0);
```

## FILE: tools/qa-world.sh

```bash
#!/usr/bin/env bash
# Runs the headless world check on a real dedicated server for one seed.
#   tools/qa-world.sh <seed> [radius_chunks] [forward|reverse]
# Output lands in runs/qa/<seed>/<order>/qa/. The order is a separate world folder so the two runs can be compared.
set -u
seed="${1:?seed required}"
radius="${2:-16}"
order="${3:-forward}"
dir="runs/qa/$seed/$order"
mkdir -p "$dir"
rm -rf "$dir/world" "$dir/qa"
echo "eula=true" > "$dir/eula.txt"
cat > "$dir/server.properties" <<EOF
level-seed=$seed
online-mode=false
spawn-protection=0
view-distance=6
simulation-distance=4
EOF
./gradlew --no-daemon runQaServer -Pqa_dir="$dir" -Pqa_radius="$radius" -Pqa_order="$order" -Pqa_dump="${QA_DUMP:-}" > "$dir/gradle.log" 2>&1
rc=$?
report="$dir/qa/report-$order.json"
if [ ! -f "$report" ]; then
  echo "seed=$seed order=$order: NO REPORT (gradle exit $rc). See $dir/gradle.log"
  exit 1
fi
node -e "
const r=JSON.parse(require('fs').readFileSync('$report','utf8'));
console.log('seed='+r.seed+' order='+r.order+' failures='+r.failures+' chunks='+r.chunks+' ms/chunk='+(r.ms_per_chunk||0).toFixed(1));
for (const c of r.checks) if(!c.passed) console.log('  FAIL '+c.name+': '+c.detail);
"
```

## FILE: src/main/resources/assets/dorohedoro/lang/en_us.json

```json
{
  "mod.dorohedoro.name": "Dorohedoro: The Hole",
  "biome.dorohedoro.hole_residential": "Hole Tenements",
  "biome.dorohedoro.hole_market": "Hole Market Quarter",
  "biome.dorohedoro.hole_industrial": "Hole Foundries",
  "biome.dorohedoro.hole_waterfront": "Hole Canalside",
  "biome.dorohedoro.hole_derelict": "Hole Wastes",
  "biome.dorohedoro.hole_graveyard": "Hole Burial Ground",
  "biome.dorohedoro.hole_outskirts": "Hole Outskirts",
  "biome.dorohedoro.hole_undercity": "Undercity",
  "block.dorohedoro.industrial_debris": "Industrial Debris",
  "block.dorohedoro.rusted_plating": "Rusted Plating",
  "item.dorohedoro.scrap_metal": "Scrap Metal",
  "itemGroup.dorohedoro.hole": "The Hole",
  "dorohedoro.world.not_hole": "This world was not created as the Hole, so it has been left unchanged. Create a new world to enter the city."
}
```

## FILE: src/main/resources/assets/minecraft/lang/en_us.json

```json
{
  "generator.minecraft.normal": "The Hole"
}
```

## FILE: src/main/resources/data/minecraft/tags/worldgen/world_preset/extended.json

```json
{
  "values": [
    "minecraft:flat",
    "minecraft:large_biomes",
    "minecraft:amplified",
    "minecraft:single_biome_surface"
  ]
}
```

## FILE: src/main/resources/data/minecraft/tags/worldgen/world_preset/normal.json

```json
{
  "replace": true,
  "values": [
    "minecraft:normal"
  ]
}
```

## FILE: src/main/resources/data/minecraft/worldgen/world_preset/normal.json

```json
{
  "dimensions": {
    "minecraft:overworld": {
      "type": "minecraft:overworld",
      "generator": {
        "type": "dorohedoro:hole",
        "biome_source": {
          "type": "dorohedoro:hole_districts"
        },
        "settings": "dorohedoro:hole"
      }
    },
    "minecraft:the_end": {
      "type": "minecraft:the_end",
      "generator": {
        "type": "minecraft:noise",
        "biome_source": {
          "type": "minecraft:the_end"
        },
        "settings": "minecraft:end"
      }
    },
    "minecraft:the_nether": {
      "type": "minecraft:the_nether",
      "generator": {
        "type": "minecraft:noise",
        "biome_source": {
          "type": "minecraft:multi_noise",
          "preset": "minecraft:nether"
        },
        "settings": "minecraft:nether"
      }
    }
  }
}
```

## FILE: docs/evidence/qa-summary.txt

```
seed=1 order=forward failures=0 chunks=625 ms/chunk=33.3
seed=2 order=forward failures=0 chunks=625 ms/chunk=42.2
seed=3 order=forward failures=0 chunks=625 ms/chunk=41.6
seed=42 order=forward failures=0 chunks=625 ms/chunk=47.7
seed=777 order=forward failures=0 chunks=625 ms/chunk=32.9
seed=1234567 order=forward failures=0 chunks=625 ms/chunk=57.2
seed=-987654321 order=forward failures=1 chunks=625 ms/chunk=42.6
  FAIL multiple_districts: [MARKET, OUTSKIRTS]
seed=20261030 order=forward failures=0 chunks=625 ms/chunk=32.2
seed=555 order=forward failures=1 chunks=625 ms/chunk=44.3
  FAIL multiple_districts: [MARKET, OUTSKIRTS]
seed=8675309 order=forward failures=0 chunks=625 ms/chunk=38.5
seed=1 order=reverse failures=0 chunks=625 ms/chunk=24.9
determinism seed 1:
chunk_hashes: 625/625 identical
chunk_hashes_full: 81/625 identical, differing e.g. -12,-12 -12,-10 -12,-9 -12,-8 -12,-7 -12,-6
seed=42 order=reverse failures=0 chunks=625 ms/chunk=24.3
determinism seed 42:
chunk_hashes: 625/625 identical
chunk_hashes_full: 75/625 identical, differing e.g. -12,-12 -12,-11 -12,-10 -12,-9 -12,-8 -12,-7
seed=8675309 order=reverse failures=0 chunks=625 ms/chunk=25.5
determinism seed 8675309:
chunk_hashes: 624/625 identical, differing e.g. 1,2
chunk_hashes_full: 19/625 identical, differing e.g. -12,-12 -12,-11 -12,-10 -12,-9 -12,-8 -12,-7
BATCH_DONE
```

## FILE: docs/LORE_REFERENCE.md

```markdown
# Lore reference

Every entry says where it came from. Nothing here may be presented in game as canon unless its status is `sourced`.

## Status values

| Status | Meaning |
| --- | --- |
| `sourced` | Stated by a secondary source reachable on 2026-10-31. Still not checked against the manga or anime. |
| `owner_spec` | Stated in the owner's specification. Treated as a design requirement. Not independently verified. |
| `adaptation` | Original gameplay invented for this mod. Never canon. |
| `unverified` | Needs a source before it can be labelled. |

## Source access

The owner's listed Dorohedoro fan-wiki pages (Hell, Hydra Forest, Magic User Door, En Family Door, Magic, the Magic User World category) returned HTTP 402 to automated fetching on 2026-10-31, so **none of them were read**. No workaround was attempted. Those pages remain the first thing to check, by the owner pasting the text or in a browser session.

Reached instead: Wikipedia's [Dorohedoro](https://en.wikipedia.org/wiki/Dorohedoro) article, the [SF Encyclopedia entry](https://sf-encyclopedia.com/entry/dorohedoro), and the [Moegirl entry](https://zh.moegirl.org.cn/en/%E5%BC%82%E5%85%BD%E9%AD%94%E9%83%BD), through search summaries.

## Setting

| Fact | Status | Source |
| --- | --- | --- |
| Two coexisting worlds: the Hole, where ordinary people live, and the sorcerers' world | `sourced` | Wikipedia |
| The sorcerers' world is a Gothic class society controlled by crime boss En | `sourced` | Wikipedia |
| Hell is the afterlife of sorcerers, ruled by devils under Chidaruma | `sourced` | Wikipedia |
| Sorcerers were made by Chidaruma, humans arose naturally | `sourced` | Wikipedia. Spoiler, keep out of default UI |
| Devils rank above sorcerers in a caste-like order | `sourced` | Wikipedia |
| En once survived in Hell for years by growing and eating a massive mushroom forest | `sourced` | Wikipedia. Spoiler |
| Horn-shaped mountains, immense rock formations, caverns in Hell | `owner_spec` | Could not be confirmed by search |
| Hydra Forest as a dense forest with Haru's cabin and Cross-Eyes structures | `owner_spec` | Could not be confirmed by search |
| Mastema's mushroom-devastated region | `owner_spec` | Unverified |
| Zagan City | `owner_spec` | Unverified |

## Doors and smoke

| Fact | Status | Source |
| --- | --- | --- |
| Sorcerers travel by magically created doors or passages | `sourced` | Wikipedia summary |
| Output can be raised by surgery or by illicit Black Powder | `sourced` | Wikipedia snapshot |
| Smoke can be bought and sold, keeping its effect | `sourced` | Wikipedia snapshot |
| Kasukabe made a portal from sorcerers' skins, which Caiman and Nikaido use | `sourced` | Moegirl and Wikipedia summaries |
| Haru is Kasukabe's estranged sorcerer wife, later a devil | `sourced` | Wikipedia. Spoiler |
| Cross-Eyes members carry red cross tattoos on their eyelids | `sourced` | Wikipedia |
| Each door's look reflects its owner | `owner_spec` | |
| The En Family Door as a shared door for those who cannot make their own | `owner_spec` | |
| Character-specific door motifs (Shin, Noi, En, Ebisu, Nikaido) | `owner_spec` | Listed as examples to research |

## Adaptations

Everything below is original to this mod.

| Item | Status |
| --- | --- |
| All spell and technique names in `docs/spec/MAGIC_SPELL_IMPLEMENTATION_MATRIX.csv` | `adaptation` |
| Artificial Door recipes, frame size and Smoke Residue | `adaptation` |
| Devil Gate and Devil Sigil. Mortals building Hell gates is not canon | `adaptation` |
| Portal timing and smoke costs | `adaptation` |
| Hole district names and the street layout | `adaptation` |
| Scrap metal, industrial debris and rusted plating | `adaptation` |
| Sewers, mine galleries and street hatches | `adaptation` |
| Layout of Hungry Bug, the Hospital, the Department Store and the Research Manor | `adaptation`, inspired by named places |
| Lake of Refuse layout | `adaptation`. The landmark name comes from the spec |
```

## FILE: docs/PROJECT_AUDIT.md

```markdown
# Project audit

Audit date: 2026-10-30. Everything below was observed on the dev machine, not assumed.

## Starting state

| Item | Finding |
| --- | --- |
| Existing mod source | None. No local repo, and none of the 5 GitHub repos on the account match. This repo was started from the official template. |
| `smoke-over-the-hole-wfs3sdqr.jar` (Downloads) | **Rejected as a base.** Fabric, Minecraft 1.21.5, compiled classes only, licence "ARR - CreativeMode.net/mod-license", bundles `creativemode-telemetry.jar`. The spec explicitly excludes a CreativeMode import. Nothing was taken from it. |
| Spec bundle | `Dorohedoro_Handoff_Fresh.zip` copied to `docs/spec/`. The master prompt in the zip is byte-identical to the standalone copy. Row counts match `README_FIRST.md`: 58 spells, 80 mobs, 25 items, 56 assets. |
| Reference sheet | `Dorohedoro_12_Labeled_References.html` links copyrighted images. Used for visual study only, per the spec. Not copied into the repo. |

## Pinned toolchain

| Component | Version | Source |
| --- | --- | --- |
| Minecraft | 1.21.1 | MDK template, spec recommendation |
| NeoForge | 21.1.265 | Newest 21.1.x on the NeoForged maven at audit time |
| ModDevGradle | 2.0.148 | Newest on the maven at audit time |
| Gradle | 9.2.1 (wrapper) | MDK template. Gradle 9.8.2 is current; not upgraded, the template pin is the tested combination |
| JDK | Microsoft OpenJDK 21.0.12.1 | Installed, on `PATH`, `JAVA_HOME` set |
| Parchment | 2024.11.17 for 1.21.1 | MDK template |
| GeckoLib | Not added yet. 4.9.3 is the current 1.21.1 NeoForge release | Add when the first animated entity needs it, per "dependencies must be justified" |

Git 2.54 and `gh` (authenticated) are available. Node 26.8.1 is available for tooling. `python.exe` is only the Windows Store stub, so tools in this repo must not assume Python.

The launcher's `.minecraft/versions/1.21.1` holds only the JSON, not the jar. The exact-version reference is the jar Gradle downloads to `~/.gradle/caches/neoformruntime/artifacts/`.

## What runs

See `docs/TEST_MATRIX.md` for the current evidence. A claim is not made here unless that file has a recorded run.

## Environment constraints that affected the work

- **Memory.** The first builds failed with Windows error 1455, "paging file is too small": 58.8 GB of a 63.9 GB commit limit was in use while `llama-server` (LM Studio) was loaded. Once it was gone, free commit rose to 45 GB and the full decompile succeeded. The pagefile is fixed at 32 GB. `disable_recompilation=true` in `gradle.properties` is the fallback for low-memory sessions. It skips decompilation, so there are no browsable sources in that mode.
- **Path length.** The app's scratch folder is long and is really under `AppData\Local\Packages\Claude_pzs8sxrjxfjjc\LocalCache\...`. `core.longpaths=true` is set on this repo. A `git clone` into the even longer scratchpad failed with "Filename too long".
- **Interrupted Gradle.** An interrupted run left a corrupt `~/.gradle/caches/9.2.1/javaCompile/javaCompile.lock`. Deleting that one file fixed it.
- **Misleading background exit codes.** Background jobs ending in `| tail` report the exit code of `tail`. Always read `BUILD SUCCESSFUL` or a recorded `gradle_exit=` value instead.

## API facts verified against the 1.21.1 sources

| Question | Answer | Where |
| --- | --- | --- |
| Can the terrain generator be extended? | Yes, `NoiseBasedChunkGenerator` is a non-final public class | `NoiseBasedChunkGenerator.java:50` |
| Does overriding `minecraft:normal` affect the dedicated server? | Yes. `level-type` resolves through the `WORLD_PRESET` registry, and the default is `WorldPresets.NORMAL` | `DedicatedServerProperties.java:212` |
| Does the Create World screen list come from tags? | Yes, `WorldPresetTags.NORMAL` and `EXTENDED` | `WorldCreationUiState.java:244` |
| Will a custom overworld trigger the experimental warning? | No, provided the biome source is not a non-standard `MultiNoiseBiomeSource`. To be confirmed in a real client run | `WorldDimensions.java:135` |
| Will adding the Sorcerers' World trigger it? | Expected yes: any extra `LevelStem` gets `Lifecycle.experimental()`. NeoForge shows the warning once per world. Gate 3 must handle this | `WorldDimensions.java:121,180` |
| Can the spawn be set by a mod? | Yes, `EventHooks.onCreateWorldSpawn` runs before vanilla spawn search and can cancel it | `MinecraftServer.java:437` |
| Do GameTests run on the Hole? | No. `GameTestServer` hard-codes `WorldPresets.FLAT`, so generator tests must call the plan directly, and real-world validation needs dedicated server runs | `GameTestServer.java:100` |
| How do strongholds find biomes? | `#minecraft:has_structure/stronghold` is `#minecraft:is_overworld`, so Hole biomes must join that tag | vanilla data |

## Issues found in the spec bundle

| Issue | Handling |
| --- | --- |
| `shin_hammer` in the crafting seed, `heart_hammer` in the asset manifest | Same item. Use `heart_hammer`, matching the master prompt's "heart-hammer" |
| Master prompt says `explosion_magic` and `teleport_magic`; the magic CSV says `explosive` and `teleportation` | Master prompt wins as the authoritative spec |
| Baku and Yaku are `[CANON]` in the master prompt but `source-check` in the magic CSV | Treat as unverified until `docs/LORE_MATRIX.md` has a citation |
| `minecraft:allay`, `minecraft:snow_golem` and others are grouped under "Nether & End progression" in the mob seed | Seed only. `VANILLA_COMPATIBILITY_MATRIX.csv` must be built from the real registry |

## Next smallest Gate 0 task

Add client, server, data and game-test run configurations, then prove each one launches.
```

## FILE: docs/spec/ASSET_REFERENCE_AND_MODEL_MANIFEST.csv

```csv
asset_id,group,expected_formats,reference_or_license_note,status
hole_street_variants,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hole_apartment_variants,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hole_factory,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hole_sewer_network,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hole_hospital,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hungry_bug,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
hole_cemetery,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
lake_of_refuse,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
sorcerer_market,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
en_mansion,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
sorcerer_door_variants,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
cross_eyes_hideout,World structures,structure NBT + palettes + loot,Reference needed; create from scratch; confirm licensed,UNCREATED
caiman,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
nikaido,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
en,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
shin,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
noi,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
ebisu,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
fujita,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
risu,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
kikurage,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
chota,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
turkey,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
shou,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
asu,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
kasukabe,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
johnson,Characters,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
lesser_roach,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
mutated_human,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
mushroom_thrall,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
rogue_sorcerer,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
living_dead_event_zombie,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
cross_eyes_fighter,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
en_family_enforcer,Enemies,bbmodel + animated geo JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
combat_knife,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
twin_knives,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
heart_hammer,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
smoke_flask,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
smoke_refinery,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
magic_door_anchor,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
mask_bench,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
industrial_debris,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
gyoza,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
rain_cloak,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
black_powder_packet,Items/blocks,item/block model JSON + texture PNG,Reference needed; create from scratch; confirm licensed,UNCREATED
smoke_black,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
smoke_white_kikurage,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
en_mushroom_impact,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
healing_effect,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
sever_effect,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
portal_transition,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
origin_select,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
smoke_meter,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
spell_radial_menu,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
reputation_screen,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
quest_tracker,Effects/UI,UI texture PNG / animation + UI component,Reference needed; create from scratch; confirm licensed,UNCREATED
```

## FILE: docs/spec/CLAUDE_MASTER_PROMPT.md

```markdown
# CLAUDE CODE MASTER HANDOFF — DOROHEDORO: THE HOLE

> **Paste this entire document into Claude Code inside the mod repository.**  Treat this as the project specification and acceptance contract. This is for **Minecraft Java Edition**, not Bedrock. The aim is a real mod, not a datapack-only mock-up, a CreativeMode import, or a texture pack.
>
> **Nonnegotiable player requirement:** **The Hole is the entire playable Overworld**, and **every new character begins inside the Hole**. No normal biome landscape, vanilla village or forest should appear on the Overworld surface. All Minecraft survival functions and necessary resources must still work. All sorcerer magic and spells must be mechanically functional and consistent with the source material. No feature is “complete” without evidence.

## A. YOUR ROLE AND NONNEGOTIABLE WORK RULES

Act as a principal Minecraft Java mod developer, gameplay designer, world-generation engineer, network engineer, asset technical artist, QA automation engineer, and lore researcher. Build and maintain the actual source repository. Prioritize correctness over count of features. If the repository already exists, audit and **preserve existing work**; never overwrite it blindly. Use atomic commits, changelog and tests. Do not claim to have installed software, downloaded files, created visual models, run Minecraft, or passed tests unless you have evidence. If the environment cannot launch Minecraft, state precisely which checks were run and which still require a client or server.

1. FIRST inspect repository, branches, README, installed JDK, Gradle wrapper, Minecraft version, loader, mappings, dependencies, current assets, licenses, tests, and build logs. Preserve valid existing choices; propose a migration if essential.
2. **Suggested version pin:** Minecraft Java **1.21.1**, **NeoForge matching 1.21.1**, **JDK 21**. Pin exact dependencies in Gradle; consult the *version-specific* documentation and installed sources, not memory or an unrelated Minecraft version. GeckoLib is an optional pinned animation dependency; verify matching loader/version. Other dependencies must be justified.
3. Use mod ID `dorohedoro` unless already fixed by repository. Organize source by registries, worldgen, entities, spell engine, mechanics, gameplay, client, network, config, datagen, gametests, assets, and integration tests. Use resource location IDs everywhere.
4. Create/update **`docs/HANDOFF.md`**, **`docs/ROADMAP.md`**, **`docs/LORE_MATRIX.md`**, **`docs/CONTENT_MATRIX.md`**, **`docs/TEST_MATRIX.md`**, **`docs/ASSET_MANIFEST.md`**, and **`CHANGELOG.md`**. Keep these aligned with code every milestone. Maintain a feature status field: `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified`, `server_verified`.
5. Separate **anime-confirmed**, **manga-only/possible spoilers**, **lore interpretation**, and **original gameplay extension**. Never label fan-designed spell names as canonical dialogue or canonical spells. Use the official anime references for visuals and a marked lore database for deeper facts.
6. Generate original in-game art, 3D models and sound where needed or use licensed content; do not rip anime audio, soundtrack, proprietary art, or another mod's assets. Keep sources/license notes. Reference pictures may guide fidelity but should not simply be redistributed as in-game textures.
7. Do not add placeholder registered items with no recipes/functions, broken models, empty dimension JSON, fake spell buttons or inert NPCs and mark them finished. If a requested feature is technically blocked, record the blocker, preserve stability and deliver a working alternative clearly labeled as an adaptation.

## B. CANON / CREATIVE TARGET

This is **Dorohedoro** by Q Hayashida. Essential aesthetic and systems: the grimy human city **the Hole**, toxic smoke-tainted rain, clinical experiments on humans, the separate **Sorcerers' World**, individual smoke-generated magical doors, masked sorcerers, Devil involvement, En Family, Cross-Eyes, strange creatures, horror-comedy and food (especially Hungry Bug and gyoza). Humans are not simply sorcerers with empty mana bars. Sorcerers have **one intrinsic magic type** produced by biological smoke organs; output volume varies. Magic is not a generic fantasy spellbook. Black smoke is normal; **Kikurage's smoke is white**. Smoke can be bottled/traded. The system should distinguish innate spells from equipment-activated stolen/bottled smoke. **Risu's Curse** has exceptionally unusual activation conditions; do not give all starting sorcerers free resurrection or ordinary repeatable Curse blasts. **Chota's magic is dispelling through difficult ritual**, not a generic short-cooldown projectile. **Turkey's magic creates living tracking dolls from cooking ingredients**, not ordinary commanded summons. **Dokuga's venom should not be misclassified as an established ordinary smoke spell**. A player-derived skill tree consists of *mastery techniques* for their one magic type, not multiple innate types. Devils are a late-game exception.

Include an optional `spoiler_content` config. At launch prioritize television anime S1+S2-confirmed material; expand with manga-only material only with clear data tags and references. Never spoil late mystery identities in default quests/UI unless story progression uncovers them.

## C. WORLDGEN — THE HOLE MUST REPLACE THE OVERWORLD

### Absolute world behavior

- Start/create a new world, load `minecraft:overworld` and visually spawn **inside the Hole**, not on normal plains, in an empty flat test world, in an unrelated dimension, or above an enormous city pasted in one location. Prefer a **world preset bound to an Overworld generator** and verify whether it can be selected or safely enforced as the default. **Do not claim automatic world replacement if a player must manually choose a preset**; expose a clear required world-selection/setup flow if the loader cannot safely override world creation. Never silently corrupt existing worlds. Add preflight incompatibility detection and warning for existing vanilla saves/worldgen mods.
- City goes in every direction across new chunks, with connected roads crossing chunk borders. Underlying biome/climate variation may drive *districts* but not normal visible forests/plains/deserts. Spawn safe, lit, navigable, with food, building materials, workbenches and meaningful starter gameplay within reach; beds and respawns work.
- Procedural generation deterministic by seed and coordinates, independently of chunk-generation order; no road seams, cut-off structures, leaking fluids or severe lighting/performance errors. Plan districts ahead of chunks, then populate with structures and loot. Provide adjustable city density, chunk budgets and optional dedicated-server pregeneration. Never load unlimited neighboring chunks during generation.

### Urban district types and landmarks

- Residential slums, alleyways, rooftops, bridges, tram/rail streets, markets, rusty factories, ruined industrial zones, warehouses, hospitals/clinics, magic-victim wards, police/guard posts, canals and polluted waterways, cemeteries, subterranean sewers and tunnels, lab basements, abandoned shops, junkyards, rare parks, underground settlements, caves and ore strata.
- **Hungry Bug** as a recognizable discoverable restaurant; hospital and magic-victim facilities; Nikaido/Caiman-associated locales; a central department store, cemetery for Living Dead Day, Kasukabe research/manor, **Lake of Refuse** as a rare dangerous landmark. En Family raids or magic-user doors can temporarily appear in the Hole. Named landmarks should obey rarity/minimum-distance and persistent placement so they are findable, not millions of blocks apart.
- Buildings require roofs, windows, doors, interior floors, stair routes, collisions, connected terrain, appropriate lighting, spawn control, loot and usable paths. Add several modular building palettes plus prefab variety; do not clone one giant structure endlessly. No structure may seal the only exit from player spawn.
- Below the city: functioning mineshaft/cave-like resource access, stone/deepslate, ores, gravel, clay, water, lava, diamonds, and renewable sources where sensible. Include stairs/hatches to the underground. Surface derelict parks, rooftop gardens, nurseries and market supply crates may provide saplings, seeds, dirt, grass, sand, logs and animals. Show resource acquisition in the guidebook.
- Atmospheric pollution: rain in the Hole may weaken sorcerers and reduce smoke output in *exposed* areas; a sheltered player is protected; provide medicine/protection and configurable severity. Human water contamination should be survivable and repairable, not a forced death spiral. Industrial sounds/particles must be bounded to visible chunks.
- Night/weather/lighting/daytime cycle; nighttime danger; optional Living Dead Day event frequency configurable and not forced permanently.

### Sorcerers' World and other dimensions

- Sorcerers' World is a **genuine separate Minecraft dimension** with its own structures/sky/biomes and no ordinary Hole city replacement, accessed via an **individualized, functional magical door**. Validate return teleport, portal persistence, safe arrival, client/server dimension sync and no cross-dimension duplication. Important sites: En's mansion, sorcerer cities/shops, mask makers, magic smoke industry, Cross-Eyes hideouts, Blue Night festival, magic school and realm-specific fauna.
- Custom magic doors are visually keyed to casters, with configurable textures/geometry, ownership and destination metadata. Allow rare constructed artificial door variants appropriate to lore, but don't make magic travel free for all humans at spawn. Teleport locations must be safe and returnable after save/load.
- Hell and Devil trials are *late-stage optional* worlds/instances with gameplay, not an empty registered dimension.
- Keep Nether and End accessible or supply carefully tested alternative progression paths. Define an explicit compatibility mode so vanilla advancement/stronghold/End paths and modded dimension requirements do not silently break.

## D. SURVIVAL AND VANILLA MINECRAFT COMPATIBILITY — AUDIT EVERYTHING

Preserve: block breaking/placing, wood/stone/iron/diamond/netherite tool progression, mining speed/harvest levels, inventories, hotbar, hunger, armor, hunger saturation, food cooking, furnaces/smokers/blast furnaces, crafting table, stonecutter, smithing table, anvils, grindstone, enchanting, brewing, villagers/trading or lore-themed equivalents, fishing, farming/animals, beds, sleep/respawn, XP, death/drop rules, maps/compasses, rail/minecart, redstone and hoppers, trading UI, tags, enchantments, shield combat, arrows/tridents, status effects, boss bars, commands, advancements and loot tables. Survival gameplay must not deadlock because normal biomes are gone.

**Vanilla resource acquisition audit:**
- Logs, planks, sticks, saplings, leaves, apples -> rooftop gardens, salvage, fenced nurseries/markets, reusable saplings.
- Cobblestone, stone, deepslate, coal, copper, iron, gold, redstone, lapis, diamonds, emeralds -> underground strata, tunnels, merchants and ore veins.
- Dirt, grass, clay, sand, gravel, flint, glass -> derelict lots, canals, rubble, excavations, sifting, kiln/smelting; renewable where needed.
- Wheat seeds, potatoes, carrots, beetroot, sugarcane, mushrooms, pumpkins, eggs, milk, leather, wool, feathers, honey, fish -> urban farms, livestock pens, protected greenhouses, wetlands, traders, mob drops.
- Blaze rods/powder, ender pearls/eyes, obsidian, nether wart, ghast tears, ancient debris, elytra, shulker materials, quartz -> preserve Nether/End pathways or clearly implemented equivalent sources without easy exploits.
- Totems, music discs, name tags, saddle, slimes, spider eye, bones/string, gunpowder, rotten flesh, paper/books -> test access via enemies, treasure, trades, crafting or events.
- Ensure recipes and loot tables use correct **1.21.1 data paths** (`data/<id>/recipe/`, `data/<id>/loot_table/`); no legacy path assumptions. Use vanilla tags when appropriate and datagen for recipes, loot, models, blockstates, item components, lang entries, advancements and tags.

### Existing Minecraft mob audit and adaptation

Do not blindly remove every vanilla mob because the world is urban. Define spawn policy by district and dimension:
- Zombies / husks / drowned / zombie villagers -> ordinary Hole undead in sewers, graveyards, toxic canals; special Living Dead Day behavior should be separately implemented, not every zombie globally reskinned.
- Skeletons / strays / bogged -> cemetery/basement or rusted industrial scavengers, with original skins only when properly licensed.
- Spiders / cave spiders / silverfish -> infestations in pipes, sewers and laboratories.
- Slimes / magma cubes -> sewer sludge / industrial leaks and optionally underworld settings; retain required drops.
- Creepers -> rare unstable mutant hazards; protect terrain according to gamerules/claims; no unlimited city destruction.
- Endermen -> rare interdimensional disturbances or in appropriate other dimensions; preserve pearl access.
- Witches -> rogue sorcerer-like encounters **without assuming vanilla potion throwers are canonical sorcerers**.
- Pillagers / vindicators / evokers / ravagers -> raider or faction combat substitutes only with safe vanilla mechanics, inventories, AI, raid compatibility and totem access.
- Villagers / wandering traders / iron golems -> urban citizens, vendors, guards; retain appropriate workstations/trades and pathfinding without overloading dense city AI.
- Bats, cats, rats (custom), roaches, chickens, cows, pigs, sheep, horses, wolves, fish, bees, frogs -> spawn in appropriate interiors/green spaces/supply farms, or supply their resources another reproducible way.
- Nether and End mobs -> leave present in their respective dimensions unless a tested replacement system exists; endgame game progression needs to work.

Create `VANILLA_COMPATIBILITY_MATRIX.csv` listing **every registry ID used in Minecraft 1.21.1 for mobs, crops, workstation blocks, loot-dependent entities and bosses** with `spawn allowed?`, district/dimension, drop access, special behavior, conflicts, and test status. Inspect actual vanilla registries / jar and data packs: do not invent the full ID set by hand. The suggestions above are seed design requirements, not an exhaustive list.

## E. COMPLETE SMOKE MAGIC / SPELL ENGINE

### Canon-first rules

Each player sorcerer has only **one native magic identity**. Magic is typically smoke released through fingers or other biological channels, with variable quantity/strength. Most smoke black, Kikurage white. The client sends **cast intent** only; the server validates origin, innate type, learned mastery, smoke amount, cooldown, range, line of sight, valid world, status, target, gamerules and claim protections. Server deducts smoke **once** atomically and then applies effects. Clients display cosmetics/hud/animation and never directly grant damage or loot. Stored bottled smoke is *consumable equipment magic*, never a second learned innate power.

Common fields for every spell: `id`, `display_name`, `canon_source`, `canon_confidence`, `magic_type`, `tier`, `unlock`, `cast_mode`, `range`, `area`, `targets`, `smoke_cost`, `cast_time`, `cooldown_ticks`, `duration`, `damage/effects`, `LOS`, `PVP_allowed`, `grief_policy`, `animation`, `particles`, `sounds`, `AI_weight`, `loot_and_death_behavior`, `network_payload`, and automated tests. Keep spells data-driven with a codec-backed definition plus a small number of effect executors; don't write a hundred duplicate monolithic event handlers. Targeting: self, raycast entity, raycast block, cone, projectile, AoE, anchored persistent area, ritual altar. Effective range and affected targets must exactly match what UI describes. Provide casting interrupts, dodge/counter interactions, particle culling, and server logging for rejections.

**Design guideline:** Names such as “Mushroom Burst” and “Cutting Wave” are **original Minecraft ability labels**, not canon incantations. Sorcerers' magic isn't a universal learned-spell system. Treat each branch below as a unique inherent *type* with 2–5 gameplay expressions. Every listed branch must be audited against anime/manga; low-confidence entries are optional or add-ons, not lies presented as confirmed anime canon.

### Required character magic types and executable gameplay translations

**01 | En — MUSHROOM CONVERSION** [CANON] `mushroom_magic`: Aimed conversion cloud affects organic mobs or certain materials, fungal spread, temporary mushroom hindrance, mushroom cover/cage, summon distinct fungal minion, rare high-tier mass conversion. NPC En can convert terrain/objects inside explicitly authorized boss arena, but player versions default non-destructive. Transformations must preserve original target UUID, inventory, ownership, health and persistence; reversible timer and dispelling route. Never turn item inventories into mushrooms or create duplication.

**02 | Noi — HEALING** [CANON] `healing_magic`: Contact heal, directed heal, regeneration field, cleanse magic injuries/status, emergency restoration of living teammates. No arbitrary resurrection; healing cannot give free duplicated hearts/inventory. XP/mastery based on actual wounds healed, not spam at full health.

**03 | Shin — DISMEMBERMENT / SEPARATION** [CANON] `dismemberment_magic`: Close smoke severance, cutting cone, targeted immobilization/limb-disabling, advanced dismantle with nonlethal separation/reassembly effect on bosses where appropriate. Combat should be strong without gory limb meshes being compulsory. No deleting entity references, dropping extra body-part loot or instantly killing bosses without phase checks.

**04 | Ebisu — REPTILE / LIZARD TRANSFORMATION** [CANON] `reptile_magic`: Apply temporary reptile form to a targeted entity, self reptile rage/mobility, claw and bite techniques, instability/mutation effects. Transform entity model safely or use reversible status/visual overlay. Clearly tag role as adaptation when anime doesn't show exact move.

**05 | Fujita — DESTRUCTION / PROJECTILE** [CANON] `destruction_magic`: Short smoke shot, concentrated projectile, ranged blast/burst and strong charge shot. Early Fujita-themed NPC has weak smoke output, not En-level area damage. Use predictable projectiles and hit validation.

**06 | Nikaido — TIME** [CANON, EXTREMELY RARE] `time_magic`: Reserved/unique special progression. Brief localized slow of entities, temporal step, optional safe snapshot-and-revert *of caster only* over bounded ticks, short targeted stasis. True story-specific time abilities may be scripted/one-use with progression costs. NEVER use global tick reversal, change shared world history, undo player inventories, resurrect killed entities wholesale, or revert placed blocks. Time snapshotted state must exclude items/containers/XP to prevent duplication. High-priority anti-exploit tests.

**07 | Risu — CURSE** [CANON, EXCEPTIONAL] `curse_magic`: Do **not** implement as ordinary renewable curse missile. Create rare death-triggered vengeance/Curse manifestation encounter as warranted by story, with safeguards for player death and inventory. Player access only as lore-appropriate rare quest/cursed artifact or endgame mode, clearly distinguished from a born ordinary sorcerer. Needs safe revive/respawn and multiplayer targeting rules.

**08 | Kikurage — REVIVAL** [CANON] `revival_magic`: **White** magic smoke. Rare NPC-assisted resurrection of eligible non-player entities / companions or tightly controlled player downed-state rescue with costs and cooldowns; forbid ordinary arbitrary player resurrection across death with duped equipment. Define exact cases that can be revived, inventory behavior, time window, and ritual.

**09 | Chota — DISPELLING** [CANON] `dispelling_magic`: Complex ritual to undo persistent sorcerer magic such as reptile transformations, mushroom corruption, curse effects where lore and gameplay permit. Requires ritual time, specific reagents, target and interruption rules; not a rapid no-cost projectile.

**10 | Kawajiri / Asu — TELEPORTATION** [CANON] `teleport_magic`: Selected-point blink, marked safe-location teleport, access/assistance with interdimensional travel, portal emergency rescue. Permission/claim/dimension-boundary checks; no teleport inside blocks, walls, unloaded chunks or banned regions.

**11 | Shou — INVISIBILITY** [CANON] `invisibility_magic`: Visual stealth, temporary concealment with detection counters and PvP fairness (particles/sounds close up, server still can target legitimately). Movement/attack may break concealment; do not remove entity from authoritative world.

**12 | Turkey — LIVING DOLL / COOKING** [CANON] `living_doll_magic`: Special ritual recipe at cooking workstation constructs a **non-speaking living tracker doll** mirroring a selected subject. It autonomously searches or points toward valid target as in source; player does not freely command it to fight. Requires target memory/token, ingredients, leash and chunk-awareness. Handle offline targets, privacy, PvP opt-in, loaded chunk budget; no always-on cross-server player tracking.

**13 | Natsuki — PROTECTION** [CANON] `protection_magic`: Strong temporary protective field/bubble, barrier health, limited ally enclosure. Performance-safe shield collision and barrier damage; vulnerability/counter mechanism; protection can't trap a player indefinitely.

**14 | Fukuyama — MEAT PIE TRANSFORMATION** [CANON] `meat_pie_magic`: Transform target into a temporary pie-like incapacitated state, strict anti-dupe/inventory preservation and duration. Humorous magic type; not generic fireball.

**15 | Baku — EXPLOSIVE** [CANON] `explosion_magic`: Targeted blast, remote delayed explosion, mine-like smoke traps; obey mobGriefing, land claims and configured entity/terrain damage.

**16 | Yaku — TRANQUILIZER** [CANON] `tranquilizer_magic`: Smoke dart, drowsiness meter, slowing/sleep-like stun on NPCs, time-limited balanced PvP CC; bosses partially resistant.

**17 | Haru — REVITALIZATION** [SOURCE-CHECK] `revitalization_magic`: Temporary restoration/buff where verified, differentiated from Noi's healing. Confirm exact powers in sources before shipping canonical designation.

**18 | Dr. Omori — SURGERY / TIME-RELATED** [SOURCE-CHECK] `surgery_time_magic`: Medical ritual reducing surgical recovery/cast time and repairing blocked smoke pathways if verified. Not a generic global time rewind. Quest/service implementation preferred.

**19 | Acupuncturist — FACE ALTERATION** [SOURCE-CHECK] `face_transformation_magic`: Temporary appearance/face change on consenting players or NPC disguise, visual model component with identity preserved. Social stealth not permanent identity theft.

**20 | Aitake / Maitake impersonators — METAMORPHOSIS** [SOURCE-CHECK] `metamorphosis_magic`: Controlled disguise/shape change where canon confirmed, duration, disguise detection, no inventory replication.

**21 | Additional observed lesser magics** [AUDIT BEFORE MARKING CANON]: power control, dissolution, muscle enhancement, enlarged size, flight, carpet/transport, giant-growth, elemental/rot, clone-like effect, and uncommon protective/transformation effects. Add all unique *actually depicted* abilities to `LORE_MATRIX.md` with episode/chapter and source verification, then implement appropriate types. **Do not assert every minor entry in a fan list appeared onscreen.**

### Optional manga-expanded repertoire (spoiler-gated, source-verify before implementation)

The wider fan-indexed magic roster includes augmentation, reduction, gravitational control, liquid manipulation, shapeshifting, telepathy, precognition, exorcism, advanced speed, barrier, magic absorption, life absorption, evaporation, clairvoyance, sabotage, restoration, invisibility-like elimination, enhanced strength, healing variants and more. Add content in audited tiers with working mechanics and IDs, or mark unverified/late-manga-only. **No fake canon**. Avoid giving non-sorcerers magic just because they are powerful. **Dokuga has venom/poison characteristics; verify whether a separate magical smoke type is ever explicitly established before using one.**

### Magic counters and interactions

- Magic suppression: blocked/clogged smoke veins, low output, toxic Hole rain, smoke suppression equipment and dispelling rituals, all with cure paths.
- Attack/defense matrix: healing removes debuffs but not all transformation states; dispelling can undo specified magical transformations, shields absorb finite damage, stealth has detection tools; magic immunity/resistance is an individual trait (e.g., Caiman) and must not be generalized.
- Stored smoke: smoke bottle/spray can holds exact type/output, owner provenance, charges/volume, cost and potency. Using bottles consumes stored smoke; never changes innate type. Secure serialization and crafting remainders.
- Partnerships/contracts: optional Blue Night partnership mechanic with mutual consent, pact benefits, social consequences, clean termination and no power cloning. Devil deals/Devil training rare and lore-sensitive.
- Controls: customizable keybind, radial menu, hotbar targeting, spell icon, tooltip, resource HUD, color-blind-safe cues; spell particles normal black plus type-specific impact visualization without implying every smoke type is neon green.
- Events: high-cost magic, interrupted rituals, PvP duels, boss immunity windows, magic interference, shield stack caps, and rain casting are covered by tests.

## F. WEAPONS / COMBAT / ARMOR / MASKS

Weapons must have working collision, attack damage/speed/durability, stamina interactions if enabled, enchant compatibility, swing/projectile animations, tooltips and recipes or loot sources. At minimum:

1. Caiman-inspired paired combat knives (dual wield visually; offhand handling safely; slash combo), kitchen cleaver, scavenged knife, reinforced knife, meat hook, rusted pipe, rebar club, pipe wrench, industrial hatchet, scrap spear.
2. Shin-inspired heart-hammer (heavy melee/short stun; requires proper model), heavy sledgehammer, En-family ceremonial weapons, Cross-Eyes knives and improvised arms.
3. Bullet/firearm themes must be balanced and optional (e.g. compact gun/handgun if justified and workable under gameplay rules), with finite ammo, reload and server-validated projectile physics; don't promise a full gun system without testing.
4. Masks: Caiman's face is anatomy, **not a removable sorcerer mask**. Individual sorcerer masks for En, Shin, Noi, Fujita, Ebisu, Nikaido, Chota and other documented characters. Distinct geometry, slots, visibility, custom armor layers and durability; masks alter modest resistances/status/efficiency, **never grant their owners' unique innate magic**. Mask-making workshop with ingredients and models.
5. Protective equipment: rubber boots, rain cloak (Hole rain defense), respirator, gas mask, industrial armor, sorcerer suit/robes, special high-tier Devil training gear. Preserve vanilla armor values and enchantments.
6. Unique items: black-smoke flask, rare white-smoke flask, bottled smoke labels, magic-user door shard/anchor, smoke-vein testing device, magical identification equipment, mask molds, Devil tumor specimen (rare quest material), smoke suppression medicine, Black Powder, mushrooms, gyoza components, quest documents/contracts.

### Crafting requirement (fully implement real recipe JSONs)

For each item produce unique registry ID, recipe(s), station, vanilla ingredients, optional Dorohedoro ingredient, unlock advancement, loot/vendor source, durability, repair item, tags, creative tab and balance/test row. Guarantee no recipes have impossible circular prerequisites.

Initial reproducible example designs (**proposed gameplay recipes, not canonical formulas**):
- `scrap_metal`: yield from salvaging industrial debris with proper block drop or smelting iron scraps; iron nuggets remain obtainable.
- `combat_knife`: 1 iron ingot + 1 stick + leather or string; repaired with iron.
- `reinforced_knife`: combat knife + iron ingot + leather at smithing/repair station; carry existing durability/data safely.
- `smoke_flask`: glass bottles + iron ingot + copper ingot + leather; store one type of smoke, no free magic generation.
- `mask_blank`: leather + paper + dye + iron nugget; later forge in mask bench with mold and design-specific ingredients.
- `sorcerer_door_anchor`: obsidian + iron + smoke-flask charge + rare sorcerer-world drop; never available without some tested first portal/unlock path.
- `black_powder_packet`: acquired as illicit vendor/event/quest loot; if craftable, dangerous reagents and tested balance. Debuffs/instability, no infinite potion stacking.
- `rain_cloak`: leather + wool + resin/honeycomb; rain defense.
- `gyoza`: wheat flour-equivalent + cooked meat/vegetable ingredients + water; proper food and saturation with cooking station/furnace recipe.
- `healing_bandages`: wool/string + mushroom/medicinal herb; distinct from Noi magic.

Use the actual **1.21.1 recipe schema and item data components**. Write GameTests for missing ingredients, output, container return, item component copy, stack limits, dupes, reload/datapack validation and recipe book visibility.

## G. NPC, CREATURE AND BOSS CONTENT MATRIX

**Friendly/neutral:** Caiman (distinct lizard head, paired knives, high physical ability and relevant magical resistance), Nikaido (Hungry Bug chef, martial fighter and rare lore magic with secrets gated), Kasukabe (human scientist/quest giver), Dr. Vaux/hospital staff, ordinary Hole citizens, vendors, shopkeepers, healers, sorcerer mask craftsmen, guards, laborers, students, and food-service NPCs. Make NPC routine schedules and dialogue practical for survival and quest delivery, not just stationary cosmetic statues.

**Sorcerers/factions:** En Family: En (boss/leader), Shin and Noi (coordinated combat duo), Fujita, Ebisu, Chota, Kikurage, Turkey, Shou and other documented associates. Cross-Eyes: appropriate members/underpowered sorcerers, Black Powder network, leader quests. Unaligned magic users, masked traveling sorcerers, gang members, bounty hunters, shapechanged victims. Distinguish NPC allegiance from species.

**Creatures:** Johnson (large roach, signature behavior), lesser roaches/rats (original ambient mobs), mutated humans, mushroom creatures and fungal thralls, magical test subjects, lab mutants, raiders, sewer hazards and Living Dead Day undead. Create original Minecraft-compatible models and animation. Avoid generic reskinned zombies as the *only* Dorohedoro creatures. Have a spawning rule and meaningful drop for every new mob, a path to remove persistent mobs and no uncontrolled breeding/summon loops.

**Boss fights / milestones:** En: mushroom cover, conversion hazards, minions and phase changes; Shin+Noi: mobility melee plus active healing/rescue of teammate; rogue transformation sorcerer, Cross-Eyes raid captain, rare Devil challenge, Lake of Refuse supernatural event as late content. Every boss has activation conditions, cooldown/telegraph, fair line-of-sight, target selection, non-cheese-but-achievable mechanics, drop tables, progression reward, despawn/reset and multiplayer health scaling. Avoid designing named lore characters as kill-on-sight mobs everywhere.

**AI architecture:** server-authoritative goals, navigation through stairs/doors/alleys and interiors, target selection/faction hostility, day schedules, attack windup, ranged projectile, casting, retreat, allies rescue, trade/interact, client animation state sync; avoid pathfinding every entity every tick across a city. Test server lag during NPC rushes and after unloading/reloading city chunks.

## H. FACTIONS, CIVILIAN LIFE, EVENTS, RPG PROGRESSION

- Origins human/sorcerer chosen once per character, story-safe respec only via admin/config or rare lore-justified mechanism. Sorcerer innate type chosen/randomized with weighted rarities and disclosed odds; no mandatory legendary roll; sane human gameplay parity via equipment, agility and resistance.
- Progression tracks: exploration, scavenging, combat, crafting, magic mastery, relationships, faction reputation and quest milestones. No infinite XP by self-heal, alt account farming or harmless block spam. Data persists UUID-based and server controlled.
- Reputation: Hole Residents, En Family, Cross-Eyes, optional neutral sorcerers, hospital/merchants; changes from theft, violence, trades, contracts, protection. NPCs react meaningfully, guards respond. Avoid permanent irreversible hostility without recovery path.
- Blue Night festival and sorcerer partnerships, Living Dead Day cemetery defense, hazardous rainstorms, sorcerer raids into the Hole, clandestine Black Powder trade, gyoza cooking festivals, bounty hunts, lab rescue events, sorcerer mask contests and hidden Devil trials. All event schedules configurable, deterministic and recover gracefully across reboot.
- Quest framework: data-driven objectives, NPC/dialogue tags, server-tracked progress, reward claims once, branching/optional decisions. Base campaign starts in Hole and introduces survival, Nikaido/Caiman, magical experiments and door travel. Don't force the player to literally be Caiman or overwrite named character arcs. Spoiler boundaries respected.
- Economy: scavenged scrap, trade tokens/currency, special reagents, authentic-vibe food, repair/material barter. No duplicated rewards or broken ingredient economics. Integrate inventories and trading APIs.

## I. MODELS, TEXTURES, ANIMATIONS, AUDIO, USER INTERFACE

- Art direction: readable voxel Minecraft silhouette with Dorohedoro's filthy industrial palette, weathering, concrete/rust textures, masks, black smoke and grotesque humor. Distinguish Hole vs Sorcerers' World architecturally. Do not make every attack neon green.
- Pipeline: Blockbench `*.bbmodel` source -> exported GeckoLib/other version-compatible entity animation/model JSON, 16x/32x texture PNGs, compiled resource assets and renderer registrations. Attach source/editable files and naming conventions. Check UV overlaps, texture alpha, pivot transforms, visible parts, handedness, walking cycles, cast cues, hitbox/eye height, LOD/culling and missing texture purple-black debug.
- Original sound categories: city wind/rain, pipes, electrical buzz, shop ambience, distant siren, footsteps, mask toggle, gyoza cooking, different spell casts/impacts, portal transitions, boss telegraphs, roach calls. Only license-safe audio. Use proper `.ogg` and `sounds.json`, attenuation and categories.
- UI: origin chooser, one-innate-magic selector/rarity explanation, smoke meter, spell hotbar/radial menu, spell/curse/cooldown indicator, reputation tab, quest tracker/guidebook, teleport return info, clear errors and settings. Must run with gamepad/keyboard remapping, GUI scaling and multiplayer.
- Translation keys for every UI element/item/mob/spell; descriptive accessibility labels; 16:9 and smaller screen test; particle-density config; subtitles for audio cues.

## J. ARCHITECTURE, NETWORKING, SECURITY AND PERFORMANCE

- Use `DeferredRegister` or official registries for blocks/items/entities/menus/sounds/particles, data components for ItemStack metadata, attachments for player/entity data (configure persistence/copy-on-death), `SavedData` for world/faction/event/landmark data, JSON/datapack codecs where appropriate, proper packet payloads for client visual events and server-authoritative spell use. Validate only IDs/coordinates/settings sent from client; never accept client-sent damage/healing/smoke quantity/target effects/teleport destination without server validation.
- Keep worldgen deterministic and bounded. Server tick time target: measure against baseline hardware and report p50/p95 tick latency at 1/4/8 players, mob load and chunk traversal. Do not invent performance metrics. Lazy init, unload cleanup, finite entity caps; avoid pathfinding to unloaded chunks, per-tick full-world scans, looping self-spawn, and unbounded particles.
- Claim/anti-grief compatibility: non-destructive by default for mushroom magic, explosions, temporary structures, giant growth and time magic. Configurable claims interaction where available, honor `mobGriefing` and team PvP. No spells editing containers, bedrock, portals, spawn-protected zones, command blocks or another player's protected build without explicit permission.
- Handle death/logout/dimension travel/portal reentry while casting; prevent casting twice due to network retransmit; idempotent event and reward handling; mitigate infinite potion, storage-smoke and respawn clone exploits. Null-safe handling for offline player targets and delayed tasks.
- Compatible dedicated server: do not load client-only classes server-side; use separate visual and logical classes; handshake for required mod version and payload protocol. Default multiplayer works with matching client+server mod versions. Test LAN, localhost dedicated, and optional higher-latency link. Test 2+ simultaneous casters, conflicts, disconnect, duels and bosses.
- Config: world preset enforcement/setup, existing world guard, vanilla progression mode, spoilers, dimension access, magic rarity, smoke regeneration, PvP, terrain effects, pollution severity, performance caps, event cadence, NPC density, server admin overrides, debug flags.
- API mistakes are critical: pin Minecraft/NeoForge mappings, consult 1.21.1 docs and Gradle sources, don't paste API snippets from 1.21.5+/26.x without porting; no placeholders or deprecation hacks merely to compile.

## K. AUTOMATED + ACTUAL GAME ACCEPTANCE MATRIX

Build incremental tests and a reproducible QA script. For each test record command, environment, pass/fail, screenshot/log, timestamp or commit, caveats.

**Build/data:** `./gradlew build`, data generation, client run, dedicated server run, no unresolved models/lang/sounds/recipes/loot, jar loads without crash.

**Worldgen:** create 10+ seeded worlds (configurable) and inspect spawn; every spawn in Hole; no vanilla surface biome regions; roads connected crossing chunks in X/Z; buildings navigable/interiors usable; city keeps generating far from origin; no orphan building/road seams; resources/mine entrances/reachable food; deterministic same seed results; save/reload stable; safe spawn light; weather shelters work. If world preset selection required, test user setup and make that requirement unmissable in menu/docs.

**Survival:** full path from first spawn to wooden/stone/iron gear, farm, food, shelter, enchant/brew, locate diamonds, portal/dimension progression, trading, ender pearls/blaze, death/respawn, bed sleeping, inventory recovery, common vanilla mob drops. Verify resource balance across several seeds.

**Magic:** one innate type per sorcerer; human cannot cast innate; all spell IDs registered; smoke spent exactly once; insufficient smoke rejected; regen/negative clamp; casting cooldown; ray/LOS correct; friendly fire and claims; combat damage matches armor/effects; potion interaction; non-spoiler unlocks; models/animations in sync. For each canonical type make at least one **mechanically unique** working acceptance test, not merely a test that item registry exists. Additional targeted tests: En mushroom conversion temporary and reversible; Noi restores actual missing HP; Shin disables/mutilates safely without duplication; Ebisu transformation restores appearance and health; Fujita projectile hits at range; Nikaido cannot time-rewind items; Kikurage white smoke and restricted revival; Chota ritual reverses the exact tagged effects; Turkey dolls track without cross-server surveillance; Shou concealment ends correctly; Asu teleport has safe return; Risu special trigger avoids player death duplication; Chota does not become universal instant dispel; smoke flask consumes exactly one dose.

**NPCs/events:** mobs spawn/despawn by district, pathfind in interiors, bosses switch phases, loot/rewards only once, faction hostility and trade react, named NPCs aren't generic aggressive spawns, Living Dead Day starts/stops and survives restart, quest progress persists.

**Portal/dimensions:** spawn in Overworld Hole, sorcerer door travel and two-way return, old coordinates safe, dimension-specific mob spawns, no lost players after restart, no duplicated worlds/entities/items.

**Multiplayer:** 2/4/8 clients near boss; consistent HUD magic reserves, animation and particles; network reordering/rate-limits; PvP on/off; logout during transformation/rewind; duplicate cast requests; dimension crossings; boss kills and individual loot; server save and crash/restart recovery.

**Performance:** compare vanilla-mod-free baseline and loaded mod on same machine/hardware; measure tick duration, chunk generation, heap usage, entity count, particles, multiplayer load. Set and meet explicit measurable budgets appropriate to test environment; fix severe spikes instead of declaring quality from a compile.

**Save integrity:** back up; load/reload, force restart, incompatible world presets warning, migration tests; never modify existing vanilla worlds silently; config updates don't delete saved origin/reputation; server world duplication safe.

## L. DEVELOPMENT GATES / DELIVERABLES

**Gate 0 — Audit & boot:** project compiles, runClient/runServer, mod ID, registries, configs, datagen, game tests, documentation and Git history.

**Gate 1 — THE WORLD FIRST:** Hole is actual Overworld via properly selected/enforced preset; safe player spawn; endless roads/districts; functional interiors, resources, mobs, mining, starter loot and survival loop; 10-seed validation. DO NOT proceed to giant character asset collections if Gate 1 is failing.

**Gate 2 — Spell vertical slice:** Human/Sorcerer origin, persistent innate type, smoke meter, one En conversion attack, one Noi heal, one mask, one smoke flask and one custom enemy; server authority, cross-client sync and tests.

**Gate 3 — Sorcerers' World:** working safe two-way door, generated distinct realm and initial structures; lore-specific rain difference.

**Gate 4 — Full core repertoire:** all anime-confirmed active magic types in section E with differentiated actual skills, effects, limitations, assets, UI and tests; priority to observed S1/S2 content. Source-check and document every optional/manga-only type.

**Gate 5 — World inhabitants:** NPCs, masks, equipment, mobs, bosses, Hungry Bug, faction AI, vendors, gyoza and quests.

**Gate 6 — World events, advanced magic and Devil content:** Living Dead Day, Blue Night, partnerships, Black Powder economy, advanced bosses, rare time/curse/revival, optional Hell.

**Gate 7 — Beta qualification:** complete QA matrices, visual model audit, performance profiling, dedicated multiplayer tests, reproduction fixes, build ZIP/JAR, dependency/install guide and known issues. No completion claim unless tests demonstrated.

**After each work session:** report `Completed / Files changed / Build command+result / Tests run+result / Bugs/blockers / Next smallest verified milestone`. Update `docs/HANDOFF.md` so work survives context resets. Make progress on actual code now, not just brainstorming. Each milestone must leave the repository buildable.

## M. AUTHORITATIVE REFERENCE LINKS / ASSET RESEARCH QUEUE

Sources below are **research and APIs**, not permission to copy copyrighted content. Keep per-asset provenance in `docs/ASSET_MANIFEST.md`; official artwork should guide new models.

**Dorohedoro anime (S1/S2 official) and character visual designs**
- Official anime site, cast, S1/S2 visuals/episodes: https://dorohedoro.net/
- Manga/anime synopsis and season pages: https://dorohedoro.net/sp/
- Dorohedoro magic and biological smoke (fan-edited; verify details with anime/manga): https://dorohedoro.fandom.com/wiki/Magic
- Named magic type roster (fan-edited, contains spoilers and uncertain classifications): https://dorohedoro.fandom.com/wiki/Types_of_Magic
- Magic doors/design references: https://dorohedoro.fandom.com/wiki/Magic_User_Door
- Locations reference index: https://dorohedoro.fandom.com/wiki/Category:Locations
- Objects, masks, flasks and equipment index: https://dorohedoro.fandom.com/wiki/Category:Objects
- Official full-body anime reference filenames (if accessible; credit and use for study, **not imported textures**): https://dorohedoro.net/images/character/character1_full.png (Caiman); .../character2_full.png (Nikaido); .../character3_full.png (En); .../character4_full.png (Shin); .../character5_full.png (Noi); .../character6_full.png (Fujita); .../character7_full.png (Ebisu).

**Minecraft 1.21.1 and NeoForge (USE VERSIONED DOCUMENTS)**
- Getting started / Java 21: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- Registries, data-driven registration and custom spell registry guidance: https://docs.neoforged.net/docs/1.21.1/concepts/registries/
- Attachments for player/entity data and death-copy rules: https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- SavedData for world/faction/event info: https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
- Server/client payloads: https://docs.neoforged.net/docs/1.21.1/networking/payload/
- Recipes and data generation: https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/
- Loot tables and paths: https://docs.neoforged.net/docs/1.21.1/resources/server/loottables/
- GameTests: https://docs.neoforged.net/docs/1.21.1/misc/gametest/
- Minecraft vanilla recipes/data reference: https://minecraft.wiki/w/Recipe_(Java_Edition)
- Minecraft vanilla Java game jar in *locally owned installation* is the exact-version reference for all vanilla registry IDs, block/item behavior, recipes, mob attributes, loot and asset conventions. Inspect it via Gradle-generated sources and game's installed data. Do not redistribute Mojang files.

**Models / animations / auxiliary tooling**
- GeckoLib project, version/loader matching: https://modrinth.com/mod/Geckolib
- Compatible player animation library if needed: https://modrinth.com/mod/player-animation-library
- Blockbench: https://www.blockbench.net/
- When using other mods as examples, inspect license/attribution. Do not copy closed code/assets from unrelated mods.

**MANDATORY FIRST ACTION FOR CLAUDE:** Audit the repository and environment. Write `docs/PROJECT_AUDIT.md` listing exact Minecraft/loader/JDK versions, what already runs, what is missing, and next smallest Gate 0 task. Then implement and verify that task, update handoff, and continue working through the gates. No invented evidence and no changing the world requirement into “a city somewhere in the Overworld.”
```

## FILE: docs/spec/CRAFTING_AND_ITEMS_SEED.csv

```csv
item_id,sample_inputs_NOT_CANON,station,functional_requirement
combat_knife,"iron ingot,stick,leather",crafting table,melee weapon; iron repair
twin_knives,"combat knife x2,leather",smithing/combine station,two-handed or dual wield with safe offhand
shin_hammer,"iron blocks,stick,rare loot",industrial forge,heavy melee; boss and quest loot alternative
pipe_wrench,"iron ingots,sticks",crafting table,melee + machinery interactions
rusted_pipe,"scrap metal,stick",crafting table,low tier weapon
industrial_hatchet,"iron ingots,sticks",crafting table,melee / wood harvesting
rain_cloak,"leather,wool,honeycomb",crafting table,Hole rain protection
respirator,"leather,glass,charcoal",crafting table,pollution defense
mask_blank,"leather,paper,dye,iron nugget",mask bench,base for unique masks
mushroom_mask,"mask blank,mushrooms,rare dye",mask bench,En aesthetic; no innate magic granted
heart_mask,"mask blank,red dye,metal",mask bench,Shin aesthetic; no innate magic granted
smoke_flask,"glass bottles,iron,copper,leather",crafting table,single smoke type; exact finite charge
smoke_canister,"smoke flask,iron,rare reagent",smoke refinery,advanced store/trade magic
smoke_detector,"copper,redstone,glass",crafting table,detect smoke residues
sorcerer_door_anchor,"obsidian,smoke flask,rare drop",door bench,requires first transport acquisition path
black_powder_packet,faction vendor/raid loot,non-craftable early,boost with severe drawbacks
reagent_jar,"glass bottle,clay,cork substitute",crafting table,recipe component
scrap_metal,salvage rusty industrial blocks,salvage/loot/smelting,basic urban resource
frying_pan,"iron ingots,stick",crafting table,gyoza cooking station interaction
gyoza,wheat/vegetables/meat/water,cooking station,"food, hunger/saturation"
mushroom_stew_special,"mushrooms,bowl,spices",cooking station,special food and minor buff
medical_bandage,"wool,string,herbs",crafting table,heal modestly not as Noi
smoke_suppressant,rare medicinal ingredients,medical station,magic debuff remedy
quest_contract,"paper,ink",quest station,server signature and rewards
devil_training_armor,late game ritual materials,rare progression station,postgame gating
```

## FILE: docs/spec/MAGIC_SPELL_IMPLEMENTATION_MATRIX.csv

```csv
spell_id,innate_type,associated_character,lore_status,move_label_status,proposed_ui_name,actual_required_mechanical_effect,initial_smoke_cost_PROPOSAL,initial_cooldown_ticks_PROPOSAL,validation,notes
mushroom_01,mushroom,En,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Mushroom Puff,directional smoke; temporary slowness and fungal marking,12,80,Needs in-game server test,Design baseline; balance later
mushroom_02,mushroom,En,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Fungal Conversion,reversible entity transformation with stable UUID/inventory,28,240,Needs in-game server test,Design baseline; balance later
mushroom_03,mushroom,En,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Mushroom Barricade,temporary limited blocks respecting claims,22,220,Needs in-game server test,Design baseline; balance later
mushroom_04,mushroom,En,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Spore Thrall,spawn capped allied mushroom minion,40,500,Needs in-game server test,Design baseline; balance later
mushroom_05,mushroom,En,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Fungal Catastrophe,telegraphed arena-only area conversion,80,1200,Needs in-game server test,Design baseline; balance later
healing_01,healing,Noi,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Healing Touch,single allied health restore,12,50,Needs in-game server test,Design baseline; balance later
healing_02,healing,Noi,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Restorative Smoke,heal over time,20,160,Needs in-game server test,Design baseline; balance later
healing_03,healing,Noi,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Trauma Cleanse,remove selected magic debuffs,25,280,Needs in-game server test,Design baseline; balance later
healing_04,healing,Noi,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Group Restoration,heal damaged allies in cone,38,360,Needs in-game server test,Design baseline; balance later
healing_05,healing,Noi,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Emergency Restoration,high-cost critical heal with restrictions,60,900,Needs in-game server test,Design baseline; balance later
dismemberment_01,dismemberment,Shin,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Severing Strike,melee attack and transient disable status,14,80,Needs in-game server test,Design baseline; balance later
dismemberment_02,dismemberment,Shin,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Cutting Wave,short cone cut attack,22,180,Needs in-game server test,Design baseline; balance later
dismemberment_03,dismemberment,Shin,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Precision Disruption,short-target disable; boss partial resistance,30,300,Needs in-game server test,Design baseline; balance later
dismemberment_04,dismemberment,Shin,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Reassembly,remove own reversible limb effect,12,160,Needs in-game server test,Design baseline; balance later
reptile_01,reptile,Ebisu,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Reptile Mark,temporary transformation effects and debuffs,18,140,Needs in-game server test,Design baseline; balance later
reptile_02,reptile,Ebisu,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Claw Frenzy,enhanced attack while transformed,22,200,Needs in-game server test,Design baseline; balance later
reptile_03,reptile,Ebisu,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Reptilian Mobility,short leap and movement boost,15,120,Needs in-game server test,Design baseline; balance later
reptile_04,reptile,Ebisu,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Unstable Rampage,powerful form with drawback,55,900,Needs in-game server test,Design baseline; balance later
destruction_01,destruction,Fujita,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Smoke Bullet,ranged low-damage projectile,10,32,Needs in-game server test,Design baseline; balance later
destruction_02,destruction,Fujita,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Burst Shot,short projectile burst,22,120,Needs in-game server test,Design baseline; balance later
destruction_03,destruction,Fujita,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Piercing Smoke,charged targeted damage,35,250,Needs in-game server test,Design baseline; balance later
time_01,time,Nikaido,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Temporal Slip,short safe blink,25,250,Needs in-game server test,Design baseline; balance later
time_02,time,Nikaido,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Local Slow,bounded entity slow; never world tick manipulation,35,400,Needs in-game server test,Design baseline; balance later
time_03,time,Nikaido,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Personal Rewind,position and health snapshot only; no inventory rewind,60,1600,Needs in-game server test,Design baseline; balance later
time_04,time,Nikaido,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Temporal Lock,brief CC and boss resistance,50,700,Needs in-game server test,Design baseline; balance later
curse_01,curse,Risu,canon-exception,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Vengeance Mark,story/ritual mark only; not standard spam cast,0,0,Needs in-game server test,Design baseline; balance later
curse_02,curse,Risu,canon-exception,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Curse Manifestation,exceptional death-linked quest/encounter process,0,0,Needs in-game server test,Design baseline; balance later
curse_03,curse,Risu,canon-exception,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Curse Resolution,complete vengeance encounter safely,0,0,Needs in-game server test,Design baseline; balance later
revival_01,revival,Kikurage,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,White Smoke Revival,NPC or permitted companion revival with consumed reagent,70,2400,Needs in-game server test,Design baseline; balance later
revival_02,revival,Kikurage,canon-rare,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Downed Rescue,restore valid incapacitated ally without inventory cloning,65,1600,Needs in-game server test,Design baseline; balance later
dispelling_01,dispelling,Chota,canon-ritual,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Ritual Unbinding,lengthy multi-ingredient ritual clears tagged transformation,45,1200,Needs in-game server test,Design baseline; balance later
dispelling_02,dispelling,Chota,canon-ritual,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Counterspell Preparation,prepare ritual ingredients and target,10,160,Needs in-game server test,Design baseline; balance later
teleportation_01,teleportation,Asu/Kawajiri,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Short Blink,safe ray target teleport,25,250,Needs in-game server test,Design baseline; balance later
teleportation_02,teleportation,Asu/Kawajiri,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Recall Marker,return to bound safe location,50,1000,Needs in-game server test,Design baseline; balance later
teleportation_03,teleportation,Asu/Kawajiri,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Door Rescue,limited multi-entity portal service with permission,75,1800,Needs in-game server test,Design baseline; balance later
invisibility_01,invisibility,Shou,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Smoke Veil,stealth against AI with counters,24,300,Needs in-game server test,Design baseline; balance later
invisibility_02,invisibility,Shou,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Silent Step,reduced aggro for bounded time,20,400,Needs in-game server test,Design baseline; balance later
invisibility_03,invisibility,Shou,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Reveal Break,remove own veil on attack,0,0,Needs in-game server test,Design baseline; balance later
living_doll_01,living_doll,Turkey,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Cook Tracking Doll,ritual item creates target-looking tracker NPC,40,1200,Needs in-game server test,Design baseline; balance later
living_doll_02,living_doll,Turkey,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Target Clue,doll indicates coarse target direction if privacy permits,12,300,Needs in-game server test,Design baseline; balance later
living_doll_03,living_doll,Turkey,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Doll Dismissal,remove doll and cleanup AI,0,0,Needs in-game server test,Design baseline; balance later
protection_01,protection,Natsuki,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Protective Bubble,finite HP shield,30,400,Needs in-game server test,Design baseline; balance later
protection_02,protection,Natsuki,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Protective Hold,maintained allied field with upkeep cost,22,200,Needs in-game server test,Design baseline; balance later
protection_03,protection,Natsuki,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Barrier Burst,bubble breaks with defensive knockback,20,400,Needs in-game server test,Design baseline; balance later
meat_pie_01,meat_pie,Fukuyama,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Pie Transformation,temporary target debuff/visual morph; no item cloning,40,540,Needs in-game server test,Design baseline; balance later
meat_pie_02,meat_pie,Fukuyama,canon,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Pie Reversion,undo reversible effect,0,0,Needs in-game server test,Design baseline; balance later
explosive_01,explosive,Baku,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Smoke Detonation,bounded blast respecting claims,22,180,Needs in-game server test,Design baseline; balance later
explosive_02,explosive,Baku,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Delayed Charge,telegraphed temporary explosion marker,32,300,Needs in-game server test,Design baseline; balance later
tranquilizer_01,tranquilizer,Yaku,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Soporific Smoke,short range slowness and tiredness,20,170,Needs in-game server test,Design baseline; balance later
tranquilizer_02,tranquilizer,Yaku,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Sleep Haze,limited AoE CC with PvP caps,35,450,Needs in-game server test,Design baseline; balance later
revitalization_01,revitalization,Haru,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Revitalizing Aura,temporary restoration buff pending canon verification,25,300,Needs in-game server test,Design baseline; balance later
revitalization_02,revitalization,Haru,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Recovery Ritual,limited recovery service after verification,35,540,Needs in-game server test,Design baseline; balance later
surgery_time_01,surgery_time,Doctor Omori,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Surgical Acceleration,hospital treatment process only,40,800,Needs in-game server test,Design baseline; balance later
surgery_time_02,surgery_time,Doctor Omori,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Smoke Vein Treatment,quest-based condition repair after verification,45,900,Needs in-game server test,Design baseline; balance later
face_transformation_01,face_transformation,Acupuncturist,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Face Reshaping,temporary consent-gated appearance disguise,20,360,Needs in-game server test,Design baseline; balance later
face_transformation_02,face_transformation,Acupuncturist,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Remove Disguise,safe reveal and persistence reset,0,0,Needs in-game server test,Design baseline; balance later
metamorphosis_01,metamorphosis,Impersonator sorcerers,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Temporary Mimicry,disguise with no inventories or abilities copied,28,420,Needs in-game server test,Design baseline; balance later
metamorphosis_02,metamorphosis,Impersonator sorcerers,source-check,ORIGINAL GAMEPLAY MOVE; POWER FROM LORE,Disguise Detection,detection checks for NPCs and devices,20,200,Needs in-game server test,Design baseline; balance later
```

## FILE: docs/spec/README_FIRST.md

```markdown
# Dorohedoro: The Hole — Claude Code handoff bundle

This bundle is a detailed specification, **not a working Minecraft mod**. Open a Java modding project in Claude Code and provide the full `CLAUDE_MASTER_PROMPT.md` as the task. The CSV files are concrete implementation queues and audit seeds, not proof that assets or spells already exist.

## Bundle files

- `CLAUDE_MASTER_PROMPT.md`: end-to-end mandate, worldgen, magic, vanilla survival, weapons, crafting, creatures, AI, networking, QA, official docs.
- `MAGIC_SPELL_IMPLEMENTATION_MATRIX.csv`: 58 proposed working game techniques across 20 lore-oriented innate-power families. The move names are **game design**, not canonical spell dialogue; lore status varies.
- `VANILLA_MOB_COMPATIBILITY_SEED.csv`: 80 tentative vanilla entity registry IDs and policies. Claude MUST check them against the exact 1.21.1 registry and fill any missing mobs.
- `CRAFTING_AND_ITEMS_SEED.csv`: 25 initial craftable/tool/food/content entries and their functional goals; recipes are proposed gameplay designs, not canonical recipes.
- `ASSET_REFERENCE_AND_MODEL_MANIFEST.csv`: 56 required structure/entity/item/visual references to source or build.

## Primary goal

New world => actual `minecraft:overworld` is an endlessly generated **Hole**, player spawns in it, no visible normal vanilla landscape, and survival has a viable resource/tech tree. Magic, the Sorcerers' World, NPCs and factions then build on that base.

## Recommended implementation baseline

Minecraft 1.21.1 / NeoForge for 1.21.1 / Java 21 **subject to audit and existing repo constraints**. Reference version-specific docs in master prompt. All systems are expected to be independently validated on a running client and dedicated server. No AI prompt can guarantee bug-free code.

## Intellectual property

Dorohedoro is someone else's copyrighted franchise. Keep a private reference folder for screenshots/art; produce original models/textures/audio, record provenance and check distribution permissions before any public release. Do not include art ripped from anime in a redistributed mod.
```

## FILE: docs/spec/VANILLA_MOB_COMPATIBILITY_SEED.csv

```csv
candidate_registry_id,group,proposed_spawn_and_drop_policy,source_status,test_status
minecraft:villager,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:wandering_trader,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:iron_golem,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:cat,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:wolf,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:horse,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:donkey,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:mule,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:llama,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:trader_llama,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:camel,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:chicken,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:cow,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:pig,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:sheep,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:rabbit,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:bee,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:frog,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:tadpole,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:parrot,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:fox,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:goat,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:armadillo,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:sniffer,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:mooshroom,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:panda,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:ocelot,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:polar_bear,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:turtle,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:axolotl,Urban citizens & animals,Hole: urban trader/farm/rooftop/garden or equivalent controlled supply,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:zombie,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:husk,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:drowned,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:zombie_villager,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:skeleton,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:stray,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:bogged,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:spider,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:cave_spider,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:silverfish,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:slime,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:creeper,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:witch,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:pillager,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:vindicator,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:evoker,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:ravager,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:vex,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:phantom,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:endermite,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:enderman,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:warden,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:breeze,Selected urban hostiles,Hole: conditional district/night/event spawn; verify drops and no destructive loopholes,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:bat,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:squid,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:glow_squid,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:cod,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:salmon,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:pufferfish,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:tropical_fish,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:dolphin,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:guardian,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:elder_guardian,Water and caves,Hole waterways/basements or relevant vanilla dimension; verify drops,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:blaze,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:ghast,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:hoglin,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:zoglin,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:piglin,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:piglin_brute,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:zombified_piglin,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:strider,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:magma_cube,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:wither_skeleton,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:shulker,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:ender_dragon,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:wither,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:snow_golem,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:skeleton_horse,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:zombie_horse,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
minecraft:allay,Nether & End progression,Keep in vanilla dimension or source equivalent; verify boss and resource progression,VERIFY AGAINST 1.21.1 REGISTRY,TODO
```

## FILE: .gitattributes

```
# Disable autocrlf on generated files, they always generate with LF
# Add any extra files or paths here to make git stop saying they
# are changed when only line endings change.
src/generated/**/.cache/* text eol=lf
src/generated/**/*.json text eol=lf
```

## FILE: .gitignore

```
### Gradle ###
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/**/build/

### IntelliJ IDEA ###
.idea/
*.iws
*.iml
*.ipr
out/
!**/src/**/out/

.run/

### Eclipse ###
.apt_generated
.classpath
.eclipse/
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/**/bin/

### VS Code ###
.vscode/

### Mac OS ###
.DS_Store

### Minecraft Modding ###
run/
!**/src/**/run/
**/src/generated/**/.cache/
repo/
!**/src/**/repo/

### Project ###
runs/
*.log
hs_err_pid*.log
replay_pid*.log
/dorohedoro-the-hole-source.zip
```

## FILE: gradlew

```
#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#   Gradle start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh Gradle
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/HEAD/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\n' "$PWD" ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH="\\\"\\\""


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
```

## FILE: gradlew.bat

```
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
```

## FILE: README.md

````markdown
# Dorohedoro: The Hole

A Minecraft Java total-conversion fan mod for Minecraft 1.21.1 on NeoForge. The Overworld becomes the Hole, an endless polluted city, and full vanilla survival keeps working. It is a sandbox, not a story mod.

**Status: early development.** The Hole generates on dedicated servers and passes automated world checks, but the game client has not been launched yet. See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) and [docs/TEST_RESULTS.md](docs/TEST_RESULTS.md).

**Continuing this project? Read [HANDOFF.md](HANDOFF.md).** It holds the full context and all source in one file.

## Build

Requires JDK 21.

```bash
./gradlew build
./gradlew runData
./gradlew runClient
```

World validation on a real dedicated server:

```bash
bash tools/qa-world.sh 12345 16 forward
```

## Credits and rights

Dorohedoro is created by Q Hayashida. This is an unofficial fan project with no affiliation. All textures here are original and generated by `tools/make-textures.mjs`. Do not add art or audio taken from the manga or anime.

The project was started from the NeoForge MDK template, whose licence is in `TEMPLATE_LICENSE.txt`.
````

## FILE: TEMPLATE_LICENSE.txt

```
MIT License

Copyright (c) 2023 NeoForged project

This license applies to the template files as supplied by github.com/NeoForged/MDK


Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## FILE: src/generated/resources/assets/dorohedoro/blockstates/industrial_debris.json

```json
{
  "variants": {
    "": {
      "model": "dorohedoro:block/industrial_debris"
    }
  }
}
```

## FILE: src/generated/resources/assets/dorohedoro/blockstates/rusted_plating.json

```json
{
  "variants": {
    "": {
      "model": "dorohedoro:block/rusted_plating"
    }
  }
}
```

## FILE: src/generated/resources/assets/dorohedoro/models/block/industrial_debris.json

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "dorohedoro:block/industrial_debris"
  }
}
```

## FILE: src/generated/resources/assets/dorohedoro/models/block/rusted_plating.json

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "dorohedoro:block/rusted_plating"
  }
}
```

## FILE: src/generated/resources/assets/dorohedoro/models/item/industrial_debris.json

```json
{
  "parent": "dorohedoro:block/industrial_debris"
}
```

## FILE: src/generated/resources/assets/dorohedoro/models/item/rusted_plating.json

```json
{
  "parent": "dorohedoro:block/rusted_plating"
}
```

## FILE: src/generated/resources/assets/dorohedoro/models/item/scrap_metal.json

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "dorohedoro:item/scrap_metal"
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/advancement/recipes/building_blocks/rusted_plating.json

```json
{
  "parent": "minecraft:recipes/root",
  "criteria": {
    "has_scrap": {
      "conditions": {
        "items": [
          {
            "items": "dorohedoro:scrap_metal"
          }
        ]
      },
      "trigger": "minecraft:inventory_changed"
    },
    "has_the_recipe": {
      "conditions": {
        "recipe": "dorohedoro:rusted_plating"
      },
      "trigger": "minecraft:recipe_unlocked"
    }
  },
  "requirements": [
    [
      "has_the_recipe",
      "has_scrap"
    ]
  ],
  "rewards": {
    "recipes": [
      "dorohedoro:rusted_plating"
    ]
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/advancement/recipes/misc/iron_ingot_from_scrap.json

```json
{
  "parent": "minecraft:recipes/root",
  "criteria": {
    "has_scrap": {
      "conditions": {
        "items": [
          {
            "items": "dorohedoro:scrap_metal"
          }
        ]
      },
      "trigger": "minecraft:inventory_changed"
    },
    "has_the_recipe": {
      "conditions": {
        "recipe": "dorohedoro:iron_ingot_from_scrap"
      },
      "trigger": "minecraft:recipe_unlocked"
    }
  },
  "requirements": [
    [
      "has_the_recipe",
      "has_scrap"
    ]
  ],
  "rewards": {
    "recipes": [
      "dorohedoro:iron_ingot_from_scrap"
    ]
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/advancement/recipes/misc/iron_nugget_from_smelting_scrap.json

```json
{
  "parent": "minecraft:recipes/root",
  "criteria": {
    "has_scrap": {
      "conditions": {
        "items": [
          {
            "items": "dorohedoro:scrap_metal"
          }
        ]
      },
      "trigger": "minecraft:inventory_changed"
    },
    "has_the_recipe": {
      "conditions": {
        "recipe": "dorohedoro:iron_nugget_from_smelting_scrap"
      },
      "trigger": "minecraft:recipe_unlocked"
    }
  },
  "requirements": [
    [
      "has_the_recipe",
      "has_scrap"
    ]
  ],
  "rewards": {
    "recipes": [
      "dorohedoro:iron_nugget_from_smelting_scrap"
    ]
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/blocks/industrial_debris.json

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "dorohedoro:scrap_metal"
        }
      ],
      "rolls": 1.0
    },
    {
      "bonus_rolls": 0.0,
      "conditions": [
        {
          "chance": 0.25,
          "condition": "minecraft:random_chance"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_nugget"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "dorohedoro:blocks/industrial_debris"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/blocks/rusted_plating.json

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "dorohedoro:rusted_plating"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "dorohedoro:blocks/rusted_plating"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/apartment.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bread",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:paper",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:book",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:string",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:stick",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:coal",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bone_meal",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:leather_boots",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:candle",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass_bottle",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bowl",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_nugget",
          "weight": 4
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 4.0,
        "min": 1.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/apartment"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/clinic.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass_bottle",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:paper",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:sugar",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glistering_melon_slice",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:golden_apple"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:spider_eye",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:white_wool",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:honey_bottle",
          "weight": 3
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 4.0,
        "min": 2.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/clinic"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/crypt.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bone",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:rotten_flesh",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:gold_nugget",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:candle",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:gold_ingot",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:music_disc_13"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:emerald",
          "weight": 2
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 4.0,
        "min": 2.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/crypt"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/department.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:leather_chestplate",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_helmet",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:clock",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:compass",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:map",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:spyglass",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:diamond"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:emerald",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:book",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:white_banner",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:totem_of_undying"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:music_disc_cat"
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 6.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/department"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/factory.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "dorohedoro:scrap_metal",
          "weight": 12
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:coal",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_nugget",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_ingot",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:copper_ingot",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:redstone",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:piston",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bucket",
          "weight": 3
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 5.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/factory"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/guard.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:arrow",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_sword",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:shield",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bow",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_ingot",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cooked_beef",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:gunpowder",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:saddle"
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 4.0,
        "min": 2.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/guard"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/hospital.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass_bottle",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:golden_apple",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glistering_melon_slice",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:golden_carrot",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:paper",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:brewing_stand",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:totem_of_undying"
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 6.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/hospital"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/hungry_bug.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bread",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cooked_porkchop",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cooked_chicken",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cooked_beef",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:baked_potato",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:wheat",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:carrot",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:egg",
          "weight": 4
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 7.0,
        "min": 4.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/hungry_bug"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/lab.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass_bottle",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:blaze_powder",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:nether_wart",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:spider_eye",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:fermented_spider_eye",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:ender_pearl"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:ghast_tear"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:book",
          "weight": 5
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 5.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/lab"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/market.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:wheat",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:carrot",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:potato",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:beetroot",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:beef",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:porkchop",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:chicken",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cod",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:egg",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:milk_bucket",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:sugar_cane",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:honey_bottle",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:pumpkin_seeds",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:melon_seeds",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:brown_mushroom",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:red_mushroom",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:sweet_berries",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cocoa_beans",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bamboo",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cactus",
          "weight": 2
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 6.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/market"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/nursery.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:oak_sapling",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:birch_sapling",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:spruce_sapling",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:jungle_sapling",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:acacia_sapling",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:dark_oak_sapling",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:wheat_seeds",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:beetroot_seeds",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bone_meal",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:dirt",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:apple",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:honeycomb",
          "weight": 2
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 5.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/nursery"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/scrap.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "dorohedoro:scrap_metal",
          "weight": 12
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_nugget",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:string",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:rotten_flesh",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bone",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:flint",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:leather",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:name_tag"
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 4.0,
        "min": 2.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/scrap"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/shop.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:emerald",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:paper",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:ink_sac",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:white_wool",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:leather",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:iron_ingot",
          "weight": 3
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:copper_ingot",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass_bottle",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:name_tag"
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:lead",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bucket",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:shears",
          "weight": 2
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 5.0,
        "min": 2.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/shop"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/starter_cache.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:bread",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:cooked_cod",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:apple",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:oak_log",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:oak_sapling",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 4.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:wheat_seeds",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:carrot",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:potato",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 5.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:coal",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 4.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:torch",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:stone_pickaxe",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:stone_axe",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 3.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:leather",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:water_bucket",
          "weight": 2
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 6.0,
        "min": 4.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/starter_cache"
}
```

## FILE: src/generated/resources/data/dorohedoro/loot_table/chests/warehouse.json

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 4.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:oak_planks",
          "weight": 10
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:oak_log",
          "weight": 8
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:spruce_log",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:sand",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:gravel",
          "weight": 6
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:clay_ball",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:glass",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 8.0,
                "min": 3.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:rail",
          "weight": 5
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:minecart",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 1.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:hopper",
          "weight": 2
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:redstone",
          "weight": 4
        },
        {
          "type": "minecraft:item",
          "functions": [
            {
              "add": false,
              "count": {
                "type": "minecraft:uniform",
                "max": 6.0,
                "min": 2.0
              },
              "function": "minecraft:set_count"
            }
          ],
          "name": "minecraft:paper",
          "weight": 4
        }
      ],
      "rolls": {
        "type": "minecraft:uniform",
        "max": 6.0,
        "min": 3.0
      }
    }
  ],
  "random_sequence": "dorohedoro:chests/warehouse"
}
```

## FILE: src/generated/resources/data/dorohedoro/recipe/iron_ingot_from_scrap.json

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    },
    {
      "item": "dorohedoro:scrap_metal"
    }
  ],
  "result": {
    "count": 1,
    "id": "minecraft:iron_ingot"
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/recipe/iron_nugget_from_smelting_scrap.json

```json
{
  "type": "minecraft:smelting",
  "category": "misc",
  "cookingtime": 200,
  "experience": 0.1,
  "ingredient": {
    "item": "dorohedoro:scrap_metal"
  },
  "result": {
    "count": 1,
    "id": "minecraft:iron_nugget"
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/recipe/rusted_plating.json

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "key": {
    "S": {
      "item": "dorohedoro:scrap_metal"
    }
  },
  "pattern": [
    "SS",
    "SS"
  ],
  "result": {
    "count": 2,
    "id": "dorohedoro:rusted_plating"
  }
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_derelict.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5986889,
    "foliage_color": 6971960,
    "grass_color": 7827524,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.004
    },
    "sky_color": 4737597,
    "water_color": 5462589,
    "water_fog_color": 2237978
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [
      {
        "type": "minecraft:rabbit",
        "maxCount": 3,
        "minCount": 2,
        "weight": 6
      },
      {
        "type": "minecraft:chicken",
        "maxCount": 4,
        "minCount": 2,
        "weight": 10
      },
      {
        "type": "minecraft:sheep",
        "maxCount": 3,
        "minCount": 2,
        "weight": 8
      },
      {
        "type": "minecraft:pig",
        "maxCount": 3,
        "minCount": 2,
        "weight": 8
      },
      {
        "type": "minecraft:cow",
        "maxCount": 3,
        "minCount": 2,
        "weight": 6
      }
    ],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 110
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 90
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 35
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_graveyard.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5199698,
    "foliage_color": 4477501,
    "grass_color": 5201992,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.003
    },
    "sky_color": 3949632,
    "water_color": 4084295,
    "water_fog_color": 1713184
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 150
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 150
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 40
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:husk",
        "maxCount": 3,
        "minCount": 1,
        "weight": 30
      },
      {
        "type": "minecraft:stray",
        "maxCount": 2,
        "minCount": 1,
        "weight": 20
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_industrial.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5001029,
    "foliage_color": 5263408,
    "grass_color": 6250298,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.007
    },
    "sky_color": 3816758,
    "water_color": 4869948,
    "water_fog_color": 1842710
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 80
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 110
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 140
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 30
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:silverfish",
        "maxCount": 3,
        "minCount": 1,
        "weight": 12
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_market.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 6973008,
    "foliage_color": 6449210,
    "grass_color": 7501375,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.0015
    },
    "sky_color": 5592650,
    "water_color": 4612170,
    "water_fog_color": 2108448
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 70
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 50
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_outskirts.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5986889,
    "foliage_color": 6971960,
    "grass_color": 7827524,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.004
    },
    "sky_color": 4737597,
    "water_color": 5462589,
    "water_fog_color": 2237978
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [
      {
        "type": "minecraft:rabbit",
        "maxCount": 3,
        "minCount": 2,
        "weight": 8
      },
      {
        "type": "minecraft:chicken",
        "maxCount": 4,
        "minCount": 2,
        "weight": 10
      },
      {
        "type": "minecraft:sheep",
        "maxCount": 3,
        "minCount": 2,
        "weight": 10
      },
      {
        "type": "minecraft:pig",
        "maxCount": 3,
        "minCount": 2,
        "weight": 10
      },
      {
        "type": "minecraft:cow",
        "maxCount": 3,
        "minCount": 2,
        "weight": 8
      },
      {
        "type": "minecraft:horse",
        "maxCount": 2,
        "minCount": 1,
        "weight": 3
      }
    ],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 90
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 110
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 35
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_residential.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5858906,
    "foliage_color": 5923896,
    "grass_color": 7042115,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.0025
    },
    "sky_color": 4674636,
    "water_color": 4612170,
    "water_fog_color": 2108448
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 25
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_undercity.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 3028016,
    "foliage_color": 4477501,
    "grass_color": 5201992,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "sky_color": 2501416,
    "water_color": 3493951,
    "water_fog_color": 1186836
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [
      {
        "type": "minecraft:axolotl",
        "maxCount": 3,
        "minCount": 2,
        "weight": 4
      }
    ],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 95
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 100
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 60
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      },
      {
        "type": "minecraft:glow_squid",
        "maxCount": 4,
        "minCount": 2,
        "weight": 10
      }
    ],
    "water_ambient": [],
    "water_creature": []
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/dorohedoro/worldgen/biome/hole_waterfront.json

```json
{
  "carvers": {},
  "downfall": 0.6,
  "effects": {
    "fog_color": 5597279,
    "foliage_color": 5005369,
    "grass_color": 6189894,
    "mood_sound": {
      "block_search_extent": 8,
      "offset": 2.0,
      "sound": "minecraft:ambient.cave",
      "tick_delay": 6000
    },
    "particle": {
      "options": {
        "type": "minecraft:ash"
      },
      "probability": 0.002
    },
    "sky_color": 4609871,
    "water_color": 4023888,
    "water_fog_color": 1452576
  },
  "features": [
    [],
    [],
    [
      "minecraft:amethyst_geode"
    ],
    [
      "minecraft:monster_room",
      "minecraft:monster_room_deep"
    ],
    [],
    [],
    [
      "minecraft:ore_dirt",
      "minecraft:ore_gravel",
      "minecraft:ore_granite_upper",
      "minecraft:ore_granite_lower",
      "minecraft:ore_diorite_upper",
      "minecraft:ore_diorite_lower",
      "minecraft:ore_andesite_upper",
      "minecraft:ore_andesite_lower",
      "minecraft:ore_tuff",
      "minecraft:ore_coal_upper",
      "minecraft:ore_coal_lower",
      "minecraft:ore_iron_upper",
      "minecraft:ore_iron_middle",
      "minecraft:ore_iron_small",
      "minecraft:ore_gold",
      "minecraft:ore_gold_lower",
      "minecraft:ore_redstone",
      "minecraft:ore_redstone_lower",
      "minecraft:ore_diamond",
      "minecraft:ore_diamond_medium",
      "minecraft:ore_diamond_large",
      "minecraft:ore_diamond_buried",
      "minecraft:ore_lapis",
      "minecraft:ore_lapis_buried",
      "minecraft:ore_copper",
      "minecraft:underwater_magma",
      "minecraft:ore_emerald"
    ],
    [
      "minecraft:ore_infested"
    ],
    [
      "minecraft:spring_lava"
    ],
    [
      "minecraft:glow_lichen"
    ],
    [
      "minecraft:freeze_top_layer"
    ]
  ],
  "has_precipitation": true,
  "spawn_costs": {},
  "spawners": {
    "ambient": [
      {
        "type": "minecraft:bat",
        "maxCount": 8,
        "minCount": 8,
        "weight": 10
      }
    ],
    "axolotls": [],
    "creature": [],
    "misc": [],
    "monster": [
      {
        "type": "minecraft:zombie",
        "maxCount": 4,
        "minCount": 2,
        "weight": 90
      },
      {
        "type": "minecraft:zombie_villager",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:skeleton",
        "maxCount": 4,
        "minCount": 2,
        "weight": 70
      },
      {
        "type": "minecraft:spider",
        "maxCount": 3,
        "minCount": 2,
        "weight": 70
      },
      {
        "type": "minecraft:creeper",
        "maxCount": 2,
        "minCount": 1,
        "weight": 15
      },
      {
        "type": "minecraft:slime",
        "maxCount": 4,
        "minCount": 2,
        "weight": 60
      },
      {
        "type": "minecraft:enderman",
        "maxCount": 2,
        "minCount": 1,
        "weight": 10
      },
      {
        "type": "minecraft:witch",
        "maxCount": 1,
        "minCount": 1,
        "weight": 5
      },
      {
        "type": "minecraft:drowned",
        "maxCount": 2,
        "minCount": 1,
        "weight": 40
      }
    ],
    "underground_water_creature": [
      {
        "type": "minecraft:glow_squid",
        "maxCount": 6,
        "minCount": 4,
        "weight": 10
      }
    ],
    "water_ambient": [
      {
        "type": "minecraft:cod",
        "maxCount": 4,
        "minCount": 2,
        "weight": 12
      },
      {
        "type": "minecraft:salmon",
        "maxCount": 3,
        "minCount": 1,
        "weight": 8
      }
    ],
    "water_creature": [
      {
        "type": "minecraft:squid",
        "maxCount": 2,
        "minCount": 1,
        "weight": 3
      }
    ]
  },
  "temperature": 0.7
}
```

## FILE: src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json

```json
{
  "values": [
    "dorohedoro:industrial_debris",
    "dorohedoro:rusted_plating"
  ]
}
```

## FILE: src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/mineshaft.json

```json
{
  "values": [
    "dorohedoro:hole_residential",
    "dorohedoro:hole_market",
    "dorohedoro:hole_industrial",
    "dorohedoro:hole_waterfront",
    "dorohedoro:hole_derelict",
    "dorohedoro:hole_graveyard",
    "dorohedoro:hole_outskirts",
    "dorohedoro:hole_undercity"
  ]
}
```

## FILE: src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/trial_chambers.json

```json
{
  "values": [
    "dorohedoro:hole_undercity"
  ]
}
```

## FILE: src/generated/resources/data/minecraft/tags/worldgen/biome/is_overworld.json

```json
{
  "values": [
    "dorohedoro:hole_residential",
    "dorohedoro:hole_market",
    "dorohedoro:hole_industrial",
    "dorohedoro:hole_waterfront",
    "dorohedoro:hole_derelict",
    "dorohedoro:hole_graveyard",
    "dorohedoro:hole_outskirts",
    "dorohedoro:hole_undercity"
  ]
}
```

## FILE: src/generated/resources/data/minecraft/tags/worldgen/biome/stronghold_biased_to.json

```json
{
  "values": [
    "dorohedoro:hole_residential",
    "dorohedoro:hole_market",
    "dorohedoro:hole_industrial",
    "dorohedoro:hole_waterfront",
    "dorohedoro:hole_derelict",
    "dorohedoro:hole_graveyard",
    "dorohedoro:hole_outskirts",
    "dorohedoro:hole_undercity"
  ]
}
```

## FILE: src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json

```json
{
    "aquifers_enabled":  true,
    "default_block":  {
                          "Name":  "minecraft:stone"
                      },
    "default_fluid":  {
                          "Name":  "minecraft:water",
                          "Properties":  {
                                             "level":  "0"
                                         }
                      },
    "disable_mob_generation":  false,
    "legacy_random_source":  false,
    "noise":  {
                  "height":  384,
                  "min_y":  -64,
                  "size_horizontal":  1,
                  "size_vertical":  2
              },
    "noise_router":  {
                         "barrier":  {
                                         "type":  "minecraft:noise",
                                         "noise":  "minecraft:aquifer_barrier",
                                         "xz_scale":  1.0,
                                         "y_scale":  0.5
                                     },
                         "continents":  0,
                         "depth":  0,
                         "erosion":  0,
                         "final_density":  {
                                               "type":  "minecraft:min",
                                               "argument1":  {
                                                                 "type":  "minecraft:squeeze",
                                                                 "argument":  {
                                                                                  "type":  "minecraft:mul",
                                                                                  "argument1":  0.64,
                                                                                  "argument2":  {
                                                                                                    "type":  "minecraft:interpolated",
                                                                                                    "argument":  {
                                                                                                                     "type":  "minecraft:blend_density",
                                                                                                                     "argument":  {
                                                                                                                                      "type":  "minecraft:min",
                                                                                                                                      "argument1":  {
                                                                                                                                                        "type":  "minecraft:y_clamped_gradient",
                                                                                                                                                        "from_y":  58,
                                                                                                                                                        "to_y":  66,
                                                                                                                                                        "from_value":  2,
                                                                                                                                                        "to_value":  -2
                                                                                                                                                    },
                                                                                                                                      "argument2":  {
                                                                                                                                                        "type":  "minecraft:max",
                                                                                                                                                        "argument1":  {
                                                                                                                                                                          "type":  "minecraft:max",
                                                                                                                                                                          "argument1":  {
                                                                                                                                                                                            "type":  "minecraft:max",
                                                                                                                                                                                            "argument1":  {
                                                                                                                                                                                                              "type":  "minecraft:min",
                                                                                                                                                                                                              "argument1":  {
                                                                                                                                                                                                                                "type":  "minecraft:min",
                                                                                                                                                                                                                                "argument1":  {
                                                                                                                                                                                                                                                  "type":  "minecraft:add",
                                                                                                                                                                                                                                                  "argument1":  {
                                                                                                                                                                                                                                                                    "type":  "minecraft:mul",
                                                                                                                                                                                                                                                                    "argument1":  4,
                                                                                                                                                                                                                                                                    "argument2":  {
                                                                                                                                                                                                                                                                                      "type":  "minecraft:square",
                                                                                                                                                                                                                                                                                      "argument":  {
                                                                                                                                                                                                                                                                                                       "type":  "minecraft:noise",
                                                                                                                                                                                                                                                                                                       "noise":  "minecraft:cave_layer",
                                                                                                                                                                                                                                                                                                       "xz_scale":  1,
                                                                                                                                                                                                                                                                                                       "y_scale":  8
                                                                                                                                                                                                                                                                                                   }
                                                                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                                                },
                                                                                                                                                                                                                                                  "argument2":  {
                                                                                                                                                                                                                                                                    "type":  "minecraft:clamp",
                                                                                                                                                                                                                                                                    "input":  {
                                                                                                                                                                                                                                                                                  "type":  "minecraft:add",
                                                                                                                                                                                                                                                                                  "argument1":  0.27,
                                                                                                                                                                                                                                                                                  "argument2":  {
                                                                                                                                                                                                                                                                                                    "type":  "minecraft:noise",
                                                                                                                                                                                                                                                                                                    "noise":  "minecraft:cave_cheese",
                                                                                                                                                                                                                                                                                                    "xz_scale":  1,
                                                                                                                                                                                                                                                                                                    "y_scale":  0.66666666666666663
                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                              },
                                                                                                                                                                                                                                                                    "min":  -1,
                                                                                                                                                                                                                                                                    "max":  1
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                              },
                                                                                                                                                                                                                                "argument2":  "minecraft:overworld/caves/entrances"
                                                                                                                                                                                                                            },
                                                                                                                                                                                                              "argument2":  {
                                                                                                                                                                                                                                "type":  "minecraft:add",
                                                                                                                                                                                                                                "argument1":  "minecraft:overworld/caves/spaghetti_2d",
                                                                                                                                                                                                                                "argument2":  "minecraft:overworld/caves/spaghetti_roughness_function"
                                                                                                                                                                                                                            }
                                                                                                                                                                                                          },
                                                                                                                                                                                            "argument2":  {
                                                                                                                                                                                                              "type":  "minecraft:range_choice",
                                                                                                                                                                                                              "input":  "minecraft:overworld/caves/pillars",
                                                                                                                                                                                                              "min_inclusive":  -1000000,
                                                                                                                                                                                                              "max_exclusive":  0.03,
                                                                                                                                                                                                              "when_in_range":  -1000000,
                                                                                                                                                                                                              "when_out_of_range":  "minecraft:overworld/caves/pillars"
                                                                                                                                                                                                          }
                                                                                                                                                                                        },
                                                                                                                                                                          "argument2":  {
                                                                                                                                                                                            "type":  "minecraft:y_clamped_gradient",
                                                                                                                                                                                            "from_y":  26,
                                                                                                                                                                                            "to_y":  44,
                                                                                                                                                                                            "from_value":  -1.5,
                                                                                                                                                                                            "to_value":  1.5
                                                                                                                                                                                        }
                                                                                                                                                                      },
                                                                                                                                                        "argument2":  {
                                                                                                                                                                          "type":  "minecraft:y_clamped_gradient",
                                                                                                                                                                          "from_y":  -64,
                                                                                                                                                                          "to_y":  -56,
                                                                                                                                                                          "from_value":  1.5,
                                                                                                                                                                          "to_value":  -1.5
                                                                                                                                                                      }
                                                                                                                                                    }
                                                                                                                                  }
                                                                                                                 }
                                                                                                }
                                                                              }
                                                             },
                                               "argument2":  {
                                                                 "type":  "minecraft:max",
                                                                 "argument1":  {
                                                                                   "type":  "minecraft:max",
                                                                                   "argument1":  "minecraft:overworld/caves/noodle",
                                                                                   "argument2":  {
                                                                                                     "type":  "minecraft:y_clamped_gradient",
                                                                                                     "from_y":  26,
                                                                                                     "to_y":  44,
                                                                                                     "from_value":  -1.5,
                                                                                                     "to_value":  1.5
                                                                                                 }
                                                                               },
                                                                 "argument2":  {
                                                                                   "type":  "minecraft:y_clamped_gradient",
                                                                                   "from_y":  -64,
                                                                                   "to_y":  -56,
                                                                                   "from_value":  1.5,
                                                                                   "to_value":  -1.5
                                                                               }
                                                             }
                                           },
                         "fluid_level_floodedness":  {
                                                         "type":  "minecraft:noise",
                                                         "noise":  "minecraft:aquifer_fluid_level_floodedness",
                                                         "xz_scale":  1.0,
                                                         "y_scale":  0.67
                                                     },
                         "fluid_level_spread":  {
                                                    "type":  "minecraft:noise",
                                                    "noise":  "minecraft:aquifer_fluid_level_spread",
                                                    "xz_scale":  1.0,
                                                    "y_scale":  0.7142857142857143
                                                },
                         "initial_density_without_jaggedness":  {
                                                                    "type":  "minecraft:y_clamped_gradient",
                                                                    "from_y":  58,
                                                                    "to_y":  66,
                                                                    "from_value":  1,
                                                                    "to_value":  -1
                                                                },
                         "lava":  {
                                      "type":  "minecraft:noise",
                                      "noise":  "minecraft:aquifer_lava",
                                      "xz_scale":  1.0,
                                      "y_scale":  1.0
                                  },
                         "ridges":  0,
                         "temperature":  {
                                             "type":  "minecraft:shifted_noise",
                                             "noise":  "minecraft:temperature",
                                             "shift_x":  "minecraft:shift_x",
                                             "shift_y":  0.0,
                                             "shift_z":  "minecraft:shift_z",
                                             "xz_scale":  0.25,
                                             "y_scale":  0.0
                                         },
                         "vegetation":  {
                                            "type":  "minecraft:shifted_noise",
                                            "noise":  "minecraft:vegetation",
                                            "shift_x":  "minecraft:shift_x",
                                            "shift_y":  0.0,
                                            "shift_z":  "minecraft:shift_z",
                                            "xz_scale":  0.25,
                                            "y_scale":  0.0
                                        },
                         "vein_gap":  {
                                          "type":  "minecraft:noise",
                                          "noise":  "minecraft:ore_gap",
                                          "xz_scale":  1.0,
                                          "y_scale":  1.0
                                      },
                         "vein_ridged":  {
                                             "type":  "minecraft:add",
                                             "argument1":  -0.07999999821186066,
                                             "argument2":  {
                                                               "type":  "minecraft:max",
                                                               "argument1":  {
                                                                                 "type":  "minecraft:abs",
                                                                                 "argument":  {
                                                                                                  "type":  "minecraft:interpolated",
                                                                                                  "argument":  {
                                                                                                                   "type":  "minecraft:range_choice",
                                                                                                                   "input":  "minecraft:y",
                                                                                                                   "max_exclusive":  51.0,
                                                                                                                   "min_inclusive":  -60.0,
                                                                                                                   "when_in_range":  {
                                                                                                                                         "type":  "minecraft:noise",
                                                                                                                                         "noise":  "minecraft:ore_vein_a",
                                                                                                                                         "xz_scale":  4.0,
                                                                                                                                         "y_scale":  4.0
                                                                                                                                     },
                                                                                                                   "when_out_of_range":  0.0
                                                                                                               }
                                                                                              }
                                                                             },
                                                               "argument2":  {
                                                                                 "type":  "minecraft:abs",
                                                                                 "argument":  {
                                                                                                  "type":  "minecraft:interpolated",
                                                                                                  "argument":  {
                                                                                                                   "type":  "minecraft:range_choice",
                                                                                                                   "input":  "minecraft:y",
                                                                                                                   "max_exclusive":  51.0,
                                                                                                                   "min_inclusive":  -60.0,
                                                                                                                   "when_in_range":  {
                                                                                                                                         "type":  "minecraft:noise",
                                                                                                                                         "noise":  "minecraft:ore_vein_b",
                                                                                                                                         "xz_scale":  4.0,
                                                                                                                                         "y_scale":  4.0
                                                                                                                                     },
                                                                                                                   "when_out_of_range":  0.0
                                                                                                               }
                                                                                              }
                                                                             }
                                                           }
                                         },
                         "vein_toggle":  {
                                             "type":  "minecraft:interpolated",
                                             "argument":  {
                                                              "type":  "minecraft:range_choice",
                                                              "input":  "minecraft:y",
                                                              "max_exclusive":  51.0,
                                                              "min_inclusive":  -60.0,
                                                              "when_in_range":  {
                                                                                    "type":  "minecraft:noise",
                                                                                    "noise":  "minecraft:ore_veininess",
                                                                                    "xz_scale":  1.5,
                                                                                    "y_scale":  1.5
                                                                                },
                                                              "when_out_of_range":  0.0
                                                          }
                                         }
                     },
    "ore_veins_enabled":  true,
    "sea_level":  63,
    "spawn_target":  [
                         {
                             "continentalness":  [
                                                     -0.11,
                                                     1.0
                                                 ],
                             "depth":  0.0,
                             "erosion":  [
                                             -1.0,
                                             1.0
                                         ],
                             "humidity":  [
                                              -1.0,
                                              1.0
                                          ],
                             "offset":  0.0,
                             "temperature":  [
                                                 -1.0,
                                                 1.0
                                             ],
                             "weirdness":  [
                                               -1.0,
                                               -0.16
                                           ]
                         },
                         {
                             "continentalness":  [
                                                     -0.11,
                                                     1.0
                                                 ],
                             "depth":  0.0,
                             "erosion":  [
                                             -1.0,
                                             1.0
                                         ],
                             "humidity":  [
                                              -1.0,
                                              1.0
                                          ],
                             "offset":  0.0,
                             "temperature":  [
                                                 -1.0,
                                                 1.0
                                             ],
                             "weirdness":  [
                                               0.16,
                                               1.0
                                           ]
                         }
                     ],
    "surface_rule":  {
                         "type":  "minecraft:sequence",
                         "sequence":  [
                                          {
                                              "type":  "minecraft:condition",
                                              "if_true":  {
                                                              "type":  "minecraft:vertical_gradient",
                                                              "random_name":  "minecraft:bedrock_floor",
                                                              "true_at_and_below":  {
                                                                                        "above_bottom":  0
                                                                                    },
                                                              "false_at_and_above":  {
                                                                                         "above_bottom":  5
                                                                                     }
                                                          },
                                              "then_run":  {
                                                               "type":  "minecraft:block",
                                                               "result_state":  {
                                                                                    "Name":  "minecraft:bedrock"
                                                                                }
                                                           }
                                          },
                                          {
                                              "type":  "minecraft:condition",
                                              "if_true":  {
                                                              "type":  "minecraft:vertical_gradient",
                                                              "random_name":  "minecraft:deepslate",
                                                              "true_at_and_below":  {
                                                                                        "absolute":  0
                                                                                    },
                                                              "false_at_and_above":  {
                                                                                         "absolute":  8
                                                                                     }
                                                          },
                                              "then_run":  {
                                                               "type":  "minecraft:block",
                                                               "result_state":  {
                                                                                    "Name":  "minecraft:deepslate",
                                                                                    "Properties":  {
                                                                                                       "axis":  "y"
                                                                                                   }
                                                                                }
                                                           }
                                          }
                                      ]
                     }
}
```
