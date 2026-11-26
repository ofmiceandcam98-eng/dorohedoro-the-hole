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

## PASS 17 CURRENT

Cold biomes and dry highlands are new optional additions in `normal.json`/Sorcerer dimension JSON; complete integration instructions and test failures are in `docs/BIOMES_COLD_AND_DISTRICTS_PASS17.md`. Full compile still unavailable. Duplicated city method definitions removed. Do not mark new regions gameplay-proven until `runData`, `build`, new-world and old-world tests. Human/sorcerer faction and villager systems remain deferred.

**Pass 16 — Scarce Hole vegetation (new-world v5):** The Hole is a polluted *surface* city, not underground. Outdoor canopy has been cut back to rare, stunted or dead trees; parks are mostly dirty vacant soil, not lush grass. Nursery saplings are sheltered behind glass, scavenged timber remains obtainable, and older saves retain vegetation rules. Source-integrated, needs full NeoForge build and client verification. See `docs/HOLE_SPARSE_TREES_PASS16.md`.

# Current design approval gate — Interior/Architecture Pass 15

User explicitly instructed: **Do not start the new exhaustive Dorohedoro lore, magic, named entity or boss phase until our existing world and building work meets their standards.** The focus is irregular streets, non-repetitive neighborhoods, distinctive buildings and ruins, usable room layouts and strong atmosphere. Fresh-world preset is now `world_detail_version: 4`. Pass 15 adds interior layouts and revises old QA scripts to accept version 4 while retaining legacy support. See `docs/INTERIOR_ARCHITECTURE_PASS15.md` and `docs/previews/interiors15/floorplan_review.png`. Source passes deterministic Java room sampling; full NeoForge compile and in-game visual approval are still pending.

**Pass 14 — Irregular City / Structural Building Forms:** source-integrated, NOT NeoForge compiled or visually verified. New-world `world_detail_version=3` changes 120×120 square arterial pitch to 144×104, preserves canals/trams and starter infrastructure, adds gently bending avenues, connected offset local streets and true 45° cross-city boulevards with safe open-lot reservations. Building shells now include L-shaped recesses, inner courtyards, upper-floor terraces, independent factory service sheds and elevated offices. Old-world v0/v1/v2 behavior remains encoded; use a NEW world. Geometry/static tests pass; full build blocked by Gradle. See `docs/IRREGULAR_CITY_AND_BUILDING_FORMS_PASS14.md` and accumulated test checklist.

**NEW ruined building variety:** Pass 12 now adds boarded stores, cracked/overgrown façades, exposed upper corners, small roof gaps, brownfields with standing wall remnants and district-dependent building decay. Derelict ~65% of eligible buildings abandoned/partly collapsed (seeded test). Entrances, stairs, farms and landmarks protected; closed shops skip regular village shopkeepers. **Not gameplay-verified.**

**NEW: WHOLE WORLD DETAIL PASS 12.** City architecture, street surfaces, district lot silhouettes, hall exteriors, shops, yards, and interior fixtures now have a versioned procedural visual pass. `world_detail_version: 1` is in fresh-world normal preset; missing field = old world generation (0). Full NeoForge build not possible here; see `docs/WORLD_DETAIL_PASS12.md` and `tools/qa-world-detail-pass12.py`. This is source-integrated only.


# Most recent checkpoint — Pass 11: Creature Spawn Eggs

Registered eight test spawn eggs in `ModItems.java` using `DeferredSpawnEggItem` from NeoForge 1.21.1. Separate `The Hole: Creatures` creative tab, existing Hole tab, and vanilla Spawn Eggs tab; colored template models and lang translations included. See `docs/SPAWN_EGGS_PASS11.md` and the extended `docs/TEST_CHECKLIST_ACCUMULATED.md`. **Full client/build not verified.** No changes to spawn mechanics, animations, mob drops, sewers, or lore.

# Latest update — Cavern Motion Pass 09 (2026-10-31)

Cavern mob movement rates and their source model animation cycles were increased in tandem after the user reported they looked slow. Sludge Crawler speed 0.19→0.24, Rust-Eater Beetle 0.22→0.27, Cavern Burrower 0.17→0.23, Spore Crawler 0.25→0.30. A smoother 50-frame, exactly five-second preview is stored in `docs/previews/animation_pass09/`. Rust-Eater Beetles now spawn on the Hole **surface** (industrial weight 9, derelict 6, outskirts 3) as well as underground (weight 18), with biome- and elevation-validated server spawn placement. Added to the Java biome bootstrap and saved generated biome data. Source-only: live NeoForge build and in-game spawning/pathfinding are **not yet verified**. See `docs/CAVERN_MOTION_PASS09.md` and `tools/qa-cavern-motion09.py`. The approved moth animation and its moving light feature were not changed.

## MOVEMENT PASS 08 — 5-second continuous motion previews + better gait

Five-second previews of all eight mobs now reside in `docs/previews/animation_pass08/` with an exact 5000 ms timeline and `tools/render-movement-pass08.py` to regenerate them. Real source motion changes add swing-phase foot lifting for the Roach, Rat, Beetle, Burrower and Spore Crawler, plus a compressing/lifting longitudinal body wave for the Leech. No changes to the approved Glow Moth wing design or six-tooth Leech mouth; no changes to vanilla mining or portal content. All animation changes are mirrored in geometry JSON and regenerated native Minecraft Java model classes. Source/parser/previews offline checked; NO full NeoForge compilation or in-game approval. Start testing against the accumulated test checklist. See `docs/MOB_MOVEMENT_REFINEMENT_PASS08.md`.

## LATEST ANIMATION PASS 07
Eight mob movement rigs with true anatomical pivots, attack and injury reactions, plus Glow Moth temporary vanilla block light 12. Full procedure, cleanup caveats, performance tests, source preview animations: docs/MOB_ANIMATIONS_AND_LIGHT_PASS07.md and docs/previews/animation_pass07/. Full Gradle build not performed.

# Latest creature visuals: Moth / Leech Pass 06 (review required)

Per direct user feedback, **Sewer Leech now has just six teeth** (2 top, 2 bottom, 2 corner hooks, each with base+tip) and no dense rakers, tooth rows, or papillae. **Glow Moth now flaps from two shoulder-based forewing hinges and two hindwing hinges** with lagging hindwing motion; folds upward when resting. Both changes are in the actual generated Java LayerDefinitions and UV atlases, not just renders. See `docs/MOTH_LEECH_REFINEMENT_PASS06.md` and `docs/previews/glow_moth_pass06_flap.gif`.

Static/animation source rendering and syntax passed, **but the NeoForge compilation and actual gameplay are still unverified**. Do not claim approval or continue adding new mobs until the user has reviewed the new moth/leech models.

---

**Pass 05 update (visual models, not Minecraft-verified):** Glow Moth and Sewer Leech source geometry, textures and Java `LayerDefinition` classes updated to respond to the user’s visual feedback. See `docs/MOTH_LEECH_REFINEMENT_PASS05.md` and `docs/previews/moth_leech_pass05_comparison.png`. No changes to mob drops, existing recipes or worldgen.

# Latest checkpoint: Creature Fidelity Pass 04 — 2026-10-31

Eight source-backed Minecraft mob models were refined toward the user-approved design sheet. See `docs/MOB_FIDELITY_PASS_04.md` and `docs/reference/approved_creatures.png`, plus the latest source-geometry renders. Models have new textured UV atlases, expanded model parts, and preserved animations. Four existing and four newer fauna species stay registered with their prior gameplay systems. Full Minecraft/NeoForge compilation remains **unverified** due to missing Gradle distribution. Next priority: runData/build/in-game geometry and AI, fix any missing parts or data, and do another direct reference-vs-game screenshot fidelity pass. Preserve Minecraft normal mining, 30% sewer cave paths, crafting and all test requirements. FTB Quests remains deferred.
