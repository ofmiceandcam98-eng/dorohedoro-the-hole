# LATEST SOURCE CHECKPOINT — PASS 32 (2026-11-01)

**Read `docs/PASS32_PURPOSE_FIRST_AUDIT.md` before modifying this source.** This is the latest source checkpoint, **NOT a compiled or Minecraft-tested release**. The default new world now uses world-detail profile 19 to prevent unnecessary permanent canal fish and unsupported supply barrels; versions 0–18 preserve their previous choices. The cosmetic preserved-conduit edit from Pass 31 was rolled back. Phase 3 Hell and Healing tests now verify gameplay/schema intent instead of obsolete text.

**Project rule:** do not add items, rooms, decor or mechanics without a meaningful survival/exploration/worldbuilding purpose. World quality and build reliability come first. The original housing, sewer dungeons, creatures, rivers, lighting (Cavern Lamp, lanterns, torches), and magic stay intact. Faction/villager and Ash-Snow systems remain deferred.

---

# CURRENT HANDOFF — PASS 18 / DISTRICT VISUAL IDENTITY (2026-10-31)

**Source checkpoint, NOT a verified playable release.** The user has approved continuing visual/world-generation work, but has **not approved** overall city quality. Fresh worlds use `world_detail_version: 7` with optional winter districts retained from Pass 17. Existing world versions 0–6 must remain backwards compatible; NEVER switch an existing save to version 7.

## User's required development order

1. FIRST: visibly differentiate every Hole district via architecture, landmark forms, street layouts, materials and atmosphere; preserve playable rooms, terrain, survival resources, mine/sewer access and prior features. User will test after this quality phase is prepared.
2. AFTER user is satisfied with districts: Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning and Cross-Eye camps (all deferred; **not implemented**).
3. LATER: faction, villager/citizen/profession/economy overhaul. Do not implement during this pass. Preserve emerald usefulness.

## Pass 18 source changes

- `DistrictIdentity18.java` introduces seven seed-selected district signature landmarks plus district-specific ground/street/edge palettes. Landmark types: `COMMUNAL_COURT`, `BAZAAR`, `MACHINE_YARD`, `CANAL_DEPOT`, `RUIN_COURT`, `BURIAL_GROUND`, `FRINGE_YARD`.
- `CityPlan.landmarkSite` keeps the existing hospital/department-store/manor/refuse rotation for most regions and selects a district-specific anchor in some regions. This does not change saved v0–v6 worlds.
- `LotPainter`, `OpenLots`, `WorldDetail`, `LotType`, `Canvas`, `CityPopulator`, `CitySettings`, and default normal preset integrated for version 7; Bazaar uses existing villager behavior only (no overhaul).
- See `docs/DISTRICT_IDENTITY_PASS18.md` and `docs/TEST_CHECKLIST_ACCUMULATED.md`.

## Tests actually executed in this source checkpoint

Offline static scripts passed: world detail 25/25, irregular city 54/54, interiors 23/23, derelict buildings 24/24. ZIP CRC/integrity check passed. These are SOURCE assertions, not runtime tests. **Full NeoForge `runData`, `build`, `runClient`, `runServer`, worldgen, FPS/TPS or multiplayer tests have NOT been run successfully here**. A previous checkpoint reported Gradle download unavailable. Claude must compile/build and fix blockers before promising an installable JAR.

## Claude's immediate instructions

1. Unzip full source; read this file and `docs/DISTRICT_IDENTITY_PASS18.md`, `docs/BIOMES_COLD_AND_DISTRICTS_PASS17.md`, `docs/INTERIOR_ARCHITECTURE_PASS15.md`.
2. Install/use Java 21 and project's matching NeoForge dependencies; run `gradlew.bat runData` and `gradlew.bat build` on Windows; diagnose and repair compilation/registry/worldgen defects rather than hiding them.
3. Run client and dedicated-server tests on multiple **NEW** worlds; compare districts at street level and rooftop level; verify all seven new landmarks, atmosphere, building entrances, stairs, shops, interiors, sewage, rivers/canals, rail crossing, ores, saplings, mob eggs, recipes, and legacy v0–v6 saves.
4. Find and fix performance, water leaks, floating decor, inaccessible fences/structures, save compatibility, generation seams, chunk-order mismatch, and visual repetition. Do not alter live save generation version.
5. Provide real game screenshots, a verified build output if successful, logs/results, and a change report. Keep deferred mechanics untouched until user approval.

---

**Pass 14 — Irregular City / Structural Building Forms:** source-integrated, NOT NeoForge compiled or visually verified. New-world `world_detail_version=3` changes 120×120 square arterial pitch to 144×104, preserves canals/trams and starter infrastructure, adds gently bending avenues, connected offset local streets and true 45° cross-city boulevards with safe open-lot reservations. Building shells now include L-shaped recesses, inner courtyards, upper-floor terraces, independent factory service sheds and elevated offices. Old-world v0/v1/v2 behavior remains encoded; use a NEW world. Geometry/static tests pass; full build blocked by Gradle. See `docs/IRREGULAR_CITY_AND_BUILDING_FORMS_PASS14.md` and accumulated test checklist.

**NEW SOURCE CHECKPOINT — Pass 13 multiscale architecture.** Six seeded neighborhood identities, six facade treatments, five rooftop silhouettes, eight footprint setbacks, three industrial roof types and neighborhood-driven lot mix are integrated into the active city generator for *new worlds only* via `world_detail_version=2`. Existing v0/v1 saves remain on their prior generator profile. 123,210 seeded selection tests and 35/35 structural checks passed; no full Gradle compile or in-game visual validation. See `docs/WORLD_ARCHITECTURE_PASS13.md`.

## Abandoned buildings in Pass 12

Implemented `DerelictBuildings.java` and `WorldDecayPolicy.java`. Lots in derelict districts now include boarded shopfronts, ruins, warehouses, partially collapsed upper walls and open skeletal remnants. `CityPopulator` skips ordinary villagers at boarded/abandoned shops. Seeded district-dependent decay distribution validated over 714,070 sites; derelict ~65% abandoned or damaged, residential ~2%. Existing worlds keep the legacy setting and unchanged lot types. Source and data checks passed; live worldgen/build not yet verified. See `docs/WORLD_DETAIL_PASS12.md`, `tools/VerifyWorldDecay.java` and `tools/qa-derelict-buildings-pass12.py`.

# Pass 12 whole-world architectural detail handoff

Completed code changes: city settings versioning, seven footprint/setback profiles, three new palettes, wider block-size selection, altered industrial mix, architectural overlays for towers/halls/shops, refined street surfacing and spaced roadside fixtures, detailed yards, subtle interior fixtures. The world generator has preserved legacy behavior when the saved codec field is missing. Full in-game visual validation and rendering/performance tests remain required. See docs/WORLD_DETAIL_PASS12.md.


**Latest Pass 10:** User wants cavern animations to feel distinct, not just faster. Modified canonical model geometry JSON and regenerated four actual Java mob model classes; added separately pivoted appendages for the Beetle, Sludge and Spore. Renderer reads authored Java pose expressions to generate true five-second offline previews. Beetle surface habitat, 30% cave links, all loot/recipes and Glow Moth lighting retained. Full compile and in-game behavior UNVERIFIED. See `docs/CAVERN_ANIMATION_PASS10.md`, `tools/qa-cavern-motion10.py`.

# Latest update — Cavern Motion Pass 09 (2026-10-31)

Cavern mob movement rates and their source model animation cycles were increased in tandem after the user reported they looked slow. Sludge Crawler speed 0.19→0.24, Rust-Eater Beetle 0.22→0.27, Cavern Burrower 0.17→0.23, Spore Crawler 0.25→0.30. A smoother 50-frame, exactly five-second preview is stored in `docs/previews/animation_pass09/`. Rust-Eater Beetles now spawn on the Hole **surface** (industrial weight 9, derelict 6, outskirts 3) as well as underground (weight 18), with biome- and elevation-validated server spawn placement. Added to the Java biome bootstrap and saved generated biome data. Source-only: live NeoForge build and in-game spawning/pathfinding are **not yet verified**. See `docs/CAVERN_MOTION_PASS09.md` and `tools/qa-cavern-motion09.py`. The approved moth animation and its moving light feature were not changed.

# Latest update — Fauna Batch 03 (2026-10-31)

The user APPROVED the more detailed four-creature concept sheet as the quality target. This batch replaces the actual four Java models and textures and adds four original underground mobs (Sludge Crawler, Rust-Eater Beetle, Cavern Burrower, Spore Crawler) with registered entity classes, loot and crafting uses. Native 1.21.1 `HierarchicalModel`, `ModelLayerLocation` and `EntityRenderersEvent` integration retained. Eight model geometry JSON exports are in `docs/model_geometry/`, details and in-game QA at `docs/HIGH_QUALITY_FAUNA_BATCH03.md`. Offline source/resource validator reported **406 checks passed** and Java parser reported **77 source files, no syntax errors**. **No Gradle build or Minecraft runtime verification.** Rendered previews in chat and outside source are *geometry-derived*, not client screenshots; visual parity with approved concept needs a real Minecraft review. This does not advance the full magic system or FTB Quests.

# Latest checkpoint — 2026-10-31 — Fauna & Minerals Batch 02

Source-integrated (NOT client/server build verified): original Sewer Leech and Cavern Glow Moth entities with native NeoForge entity types, renderers, animated Java voxel models, 64×64 texture atlases, Undercity spawns, 1.21.1 entity loot tables, and **functional** medical salve / glow tonic / lamp / silk recipe systems. Glow Moths can be sheared every 6000 ticks, with cooldown persisted across saves. New Zinc and Oil Shale ore blocks and generation, ingots/bitumen/brass, Galvanized Plating and light-emitting Cavern Lamp, original source textures and models, cached recipe outputs, loot and mineable tags. Surviving vanilla ore/cave progression, 30% cave-leading sewer profile, and all prior content deliberately preserved.

Verification: Java parser 64 files 0 syntax errors; Batch02 106/106 source checks; legacy sewer 51/51, Phase3 81/81, Phase4 materials 86/86. `./gradlew compileJava` **could not start** because `services.gradle.org` DNS is unavailable; no in-game tests. Further compiler/runtime checks required. Visual previews of all four mob models are in `docs/evidence/`. See `docs/FAUNA_AND_MINERAL_BATCH_02.md` and `docs/TEST_CHECKLIST_ACCUMULATED.md`. FTB Quests still deferred.

---

## October 31 — Fauna and Mineral Batch 01 source update

Native NeoForge entity registration, models/animation and textures for an original Sewer Cockroach and Scrap Rat; thematic loot and concrete survival uses. Mineable sulfur, rock salt and limestone; chitin masonry; 10 item sprites, resources and generated data. **Offline-only:** 93/93 content cross-checks, 58 Java files parse. Gradle cannot download distribution here; no compiled/runtime proof. Full details and exact QA instructions in `docs/FAUNA_AND_MINERAL_BATCH_01.md`. Proceed with on-PC `runData`, `build`, `runClient`, `runServer` and cross-client verification before calling playable. The current 30% sewer-to-cave layout, deep mining, original Hole atmosphere and portal work are preserved. FTB Quests remain deferred.

# Latest source continuation — cavern traversal and variation (2026-10-31)

At the owner's request to continue after setting ~30% sewer-to-cave junctions, completed an additional **source-integrated, offline-tested** pass: a deterministic cavern-size profile (about 16.46% wider chambers), physically graded descending landings that reach the cavern floor without >1-block drops across district relief, and a per-world `sewer_cavern_version` field that protects old saves. Runtime QA now probes the starter branch for connected support/air. See `docs/SEWER_CAVERN_TRAVERSAL.md`. **Full NeoForge compilation, in-game walkability and true natural-cave connectivity are unverified**. Original deep cave noise, aquifers and vanilla ore mining remain unchanged. FTB Quests stays deferred.

# Latest update: More sewer routes to caves — October 31, 2026

**User requested significantly more cave access without losing sewer identity.** New default worlds now select long cave-leading branches at approximately **30%** of arterial intersections (observed **29.89%** across 146,410 selections), up from **9.07%**. Closed inspection areas remain around 18%, ordinary sewer-only junctions around 52%. The long masonry-to-ruins-to-rock-to-cavern transition (~59 blocks) and vanilla cave/mining system are unchanged.

**Save safety:** A new `sewer_cave_frequency` city-generator codec field is explicitly `30` in `normal.json`. Existing saves without this field use the **exact legacy** branch selection (~9%, not merely a close approximation). Do not rewrite existing world settings: test on a new world. Changes: `CitySettings.java`, `SewerBranchPolicy.java`, `SewerCaveBranches.java`, `normal.json`, `tools/VerifySewerBranchFrequency.java`, `tools/qa-sewers-static.py`, plus docs. Offline checks pass; NeoForge/client tests are pending. See `docs/SEWER_UNDERWORLD.md` and `docs/SEWER_TEST_RESULTS.md`. Continue Phase 4/runtime verification; FTB Quests stays deferred.

---

# Latest source continuation: Selective sewer-to-cave connections — October 31, 2026

**User clarified that normal mining and large Minecraft cave systems must be preserved.** The sewer worldgen now uses a hybrid density router: shallow foundations sealed above Y=24, natural caves restored below, and deliberately rare long sewer branches connecting to caves after masonry/ruin/rock transitional sections. Most sewers remain sewers. See `docs/SEWER_UNDERWORLD.md` and `docs/SEWER_TEST_RESULTS.md`. Do not use the earlier cave-suppression design. This is source-integrated only and still requires real NeoForge build and client test. The rest of the mod (Hole terrain/interiors/smog, Phase 3 dimensions, Phase 4 portals/materials, future magic, deferred FTB Quests) remains intact.

# Previous sewer checkpoint — retained for historical context; superseded by hybrid cave policy above

**New underground requirements are source-integrated but NOT built or tested in Minecraft.** See `docs/SEWER_UNDERWORLD.md` and `docs/SEWER_TEST_RESULTS.md` first. The old `dorohedoro:hole` noise settings are retained for existing worlds, and new worlds select `dorohedoro:hole_sewers` through the normal preset. Run Gradle/datagen/client/server QA on Windows before describing Phase 2.5 as complete. Then continue verification of Phase 3 realms and Phase 4 portals; magic and FTB Quests remain deferred.

# Latest handoff — Phase 4 source checkpoint (2026-10-31)

**Read `docs/PHASE4_PORTALS_HANDOFF.md` first**, then `docs/PHASE4_PORTAL_MATERIALS.md`. Phase 4 is source-integrated and offline tested, NOT Minecraft/NeoForge compile-verified. The previous Phase 3 instructions follow below.

# Current developer handoff — 2026-10-31

**Read first:** `docs/PHASE3_REALMS_IMPLEMENTATION.md` (the latest source changes and limits). Then read `docs/HANDOFF_TAKEOVER_2026-10-31.md` (the Hole elevation, interiors, smog patch), `docs/ROADMAP.md`, `docs/spec/SANDBOX_DIRECTIVE.md`, and `docs/TEST_RESULTS.md`.

The original repo root `HANDOFF.md` is a consolidated historical snapshot; its embedded source cannot supersede active `src/` files.

## Latest objective
Move from the Hole foundation to **Phase 3 (Sorcerers' World and Hell generation)**, then, after verification, **Phase 4 (portals)**. Phase 3 is **source integrated and statically checked**, **not compile-verified**.

## What changed this session
- Active realm biome-source codecs, world/dimension JSON, datagen and registry wiring.
- Hell Rock and Ash Soil item/blocks, texture models, tags and drops.
- 12 candidate biome resources, six Hell ore veins and custom realm terrain settings.
- Extended server QA with additional realm load and surface-biome checks.
- Portable source bootstrap and structural regression scripts.

## Tests
- `python tools/qa-phase3-static.py`: 81/81 offline structural checks passed.
- Java 21 compiler parser: 39 active files, no syntax errors.
- JSON parser: 93 files, no syntax errors.
- **No NeoForge build or Minecraft server/client QA**: Gradle wrapper distribution unavailable due offline DNS.

## Next exact steps
1. Regenerate realm noise settings from pinned vanilla 1.21.1 client jar via `tools/make-realm-settings.ps1`.
2. `./gradlew --no-daemon runData` and `./gradlew --no-daemon build`. Resolve actual compile/datagen errors. **Do not run concurrent Gradle builds.**
3. Run new dedicated-server QA for 3 dimensions. Inspect new `realm_*` checks and original Hole checks.
4. Run Minecraft client and verify Hole smog/elevated terrain/furnished interiors, Sorcerers' World woodland, and Hell rock/ash/horns. Preserve old worlds and test a new save.
5. If those pass, implement Phase 4 portal backend. Use the portal specification in `docs/spec/SANDBOX_DIRECTIVE.md` and the prior handoff; do not duplicate that design.
6. After each stage, update `docs/TEST_RESULTS.md`, `docs/IMPLEMENTATION_STATUS.md`, and this handoff with verified status only.

**FTB Quests:** Planned, currently deferred. Magic remains queued. Minecraft survival sandbox freedom and lore distinctions are mandatory.
