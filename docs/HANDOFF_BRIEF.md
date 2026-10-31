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
| New world's `minecraft:overworld` uses `HoleChunkGenerator` | Passed on every seed |
| Spawn on the plaza at 0, 65, 0, standable, open sky | Passed on every seed |
| No vanilla surface biome near spawn | Passed on every seed |
| Streets all reachable on foot from spawn | 100% in every run so far |
| Enclosed buildings enterable on foot | 100% in every run so far |
| All vanilla ores plus water, lava, clay, sand, gravel, dirt and sugar cane in a generated region | Passed |
| No flowing water from y=46 to y=66 | Passed |
| No ore or replaceable rock at street level or above | Passed |
| City layer identical when chunks generate in opposite order | **See TEST_RESULTS.** The first pass was on the early shell-only city. The rerun on the full city is in the batch |
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

1. Check `runs/qa/summary.txt` or `docs/TEST_RESULTS.md` for the final batch.
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
