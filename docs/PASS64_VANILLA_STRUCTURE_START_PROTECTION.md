# Pass 64 — Registered vanilla structure preservation (SOURCE ONLY)

Date: November 2, 2026. Baseline: Pass 63, complete source checkpoint. Fresh world detail profile: **46**.

## Problem

The existing Pass 63 climate-based buffer protects much of the Deep Dark underground, but it cannot know where a real stronghold, mineshaft, trial chamber, or Ancient City starts. The Hole paints deep galleries, sewers, cisterns, shafts, underground rooms and custom spawners at the SURFACE stage; registered Minecraft structures are generally placed later. Without a direct protection lookup, excavations could obstruct critical vanilla loot/portal areas.

## Change

- `VanillaStructureLookup64.java`: queries `StructureManager.startsForStructure(currentChunk, predicate)` using actual registry keys and valid registered `StructureStart` bounding boxes. Only five exact vanilla IDs: `minecraft:stronghold`, `minecraft:mineshaft`, `minecraft:mineshaft_mesa`, `minecraft:trial_chambers`, `minecraft:ancient_city`. No speculative seed or fabricated coordinates. No neighboring chunk loads or scans requested explicitly.
- `VanillaStructureBuffer64.java`: immutable, bounded safety envelopes, 4 blocks horizontal and 6 vertically. Conservative *whole structure start* envelope, not individual pieces. Out-of-chunk boxes filtered. Integer comparison widens to `long` to avoid overflow near negative/extreme coordinates. V46+ only.
- `HoleChunkGenerator.buildSurface()`: for v46+, extracts actual structure references and supplies them to `CityPainter`; older saved generation versions use their original painter path.
- `CityPainter` & `Canvas`: protect all four write routes: regular block/set, custom mob spawner, chest/barrel, and signs, including their block-entity NBT writes. The v45 climate buffer stays active as well. Surface streets/buildings directly above deep vanilla structures are not globally disabled (vertical filtering).
- `CitySettings` codec / `world_preset/normal.json`: new-world v46 with older saves' stored `world_detail_version` respected.

## Test evidence — offline only

- `bash tools/qa-pass64-buffer.sh`: actual Pass64 Java policy and lookup classes compiled against small signatures-compatible Minecraft test fixtures. **220,320 checks**, including 55,000 deterministic randomized bounding boxes, registry filtering, invalid starts, negative coordinates, limit boundaries, and v45-versus-v46 preservation.
- `python3 tools/qa-pass64-source.py`: **33/33** source/data wiring checks.
- `bash tools/qa-pass63-all.sh`: previous 63/62/61 suites passed.
- `bash tools/qa-pass60-all.sh`: previous 41–60 chain completed with final PASS in full log; run separately due long runtime. The log is `Pass64_Regression41_60.log` outside the project for audit.

**NOT run:** NeoForge Gradle `build`, `runData`, `runClient`, `runServer`, GameTests, a Minecraft world, or any real structure placement. Mapped API signatures for `StructureManager`, `StructureStart`, and `BoundingBox` were cross-checked with published NeoForge 1.21.1 API references; the real compile is pending. The test fixtures cannot prove an actual stronghold and sewer will coexist.

## Wednesday Nov 4 acceptance — Claude

1. Restore verified Gradle wrapper JAR, compile with NeoForge 1.21.1 and fix any API/mapping errors. Run `runData`, `build`, `runClient`, `runServer` and applicable GameTests.
2. Create new v46 worlds in several climates. Compare v45 older world saves separately without modifying them; back up any saves.
3. With Minecraft QA commands find naturally generated structures: `/locate structure minecraft:stronghold`, `/locate structure minecraft:trial_chambers`, `/locate structure minecraft:mineshaft`, `/locate structure minecraft:ancient_city`. Use spectators for inspection only; these commands are NOT proof that a regular survival character can reach structures.
4. In each naturally generated structure, verify room integrity and original loot mechanics: stronghold portal room / End portal frames; trial spawners/vaults/Breeze; mineshaft paths and chests; sculk sensors/shriekers and natural Warden summoning in Deep Dark ancient cities. Do not use `/summon warden` as the test.
5. Inspect the above/below adjacency of city tunnels; confirm no cut-off entrance or sealed cave passage. Where the bounding-box guard creates abrupt tunnel stops, later add purposeful masonry collapse/repair access only when it does not threaten the vanilla structure.
6. Complete the vanilla survival route without any Dorohedoro techniques: collect resources, craft Nether portal, enter Nether, obtain blaze rods and pearls, find stronghold, activate real End portal, defeat dragon, enter outer End and visit End Cities. Test no-technique sewer survival with iron armor, shield, food and ordinary torches.
7. **Still outstanding:** real ocean biomes plus monuments, ocean ruins, shipwrecks; full vanilla structure placement proof, End-game parity and natural cave connectivity. Do NOT mark as completed simply because data tags exist.

## Non-negotiable design rules

Keep Ancient Cities vanilla. No additional custom Forgotten City (user meant vanilla Ancient City). Existing Pass61 sewer danger and 30% selected cave-branch policy remain, but actual cave connectivity is not guaranteed. World climate/urban density remain independent, and all Minecraft mining, survival, crafting, portals and boss progression must stay playable without techniques. No forced environmental poison, Ash-Snow, Sludge-Ice, toxic snowballs, Cross-Eye camps, or villager overhaul until their approved development stages.
