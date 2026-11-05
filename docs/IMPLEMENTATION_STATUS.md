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

## Pass 17 — cold districts / dry frostbound highlands (source integrated, gameplay unverified)

- Optional rare frostbitten industrial neighborhoods in the Hole and optional dry frostbound highlands in the Sorcerers' World. New flags preserve previous-source defaults, with world-detail version 6 for the city snow patch pass.
- Repaired duplicate methods in CityPlan and CityPainter from earlier source. Biome JSON/feature snapshots included; `runData`, full compile and in-game QA still required.
- **Lore:** Hole snowfall is canonical; permanent frozen districts and Sorcerer highland snowy geography are original gameplay adaptations, not canon. Sorcerer climate remains precipitation-free. See `docs/BIOMES_COLD_AND_DISTRICTS_PASS17.md`.
- No toxin armor mechanics, special Ash-Snow/Sludge-Ice materials, Cross-Eyes camps, or villager overhaul implemented here.

**Pass 16 — Scarce Hole vegetation (new-world v5):** The Hole is a polluted *surface* city, not underground. Outdoor canopy has been cut back to rare, stunted or dead trees; parks are mostly dirty vacant soil, not lush grass. Nursery saplings are sheltered behind glass, scavenged timber remains obtainable, and older saves retain vegetation rules. Source-integrated, needs full NeoForge build and client verification. See `docs/HOLE_SPARSE_TREES_PASS16.md`.

**Pass 14 — Irregular City / Structural Building Forms:** source-integrated, NOT NeoForge compiled or visually verified. New-world `world_detail_version=3` changes 120×120 square arterial pitch to 144×104, preserves canals/trams and starter infrastructure, adds gently bending avenues, connected offset local streets and true 45° cross-city boulevards with safe open-lot reservations. Building shells now include L-shaped recesses, inner courtyards, upper-floor terraces, independent factory service sheds and elevated offices. Old-world v0/v1/v2 behavior remains encoded; use a NEW world. Geometry/static tests pass; full build blocked by Gradle. See `docs/IRREGULAR_CITY_AND_BUILDING_FORMS_PASS14.md` and accumulated test checklist.

**Pass 13 — Multiscale world architecture:** active source integration; six neighborhoods / facades, five roof shapes, eight setbacks and detailed industrial/storefront types. Fresh worlds opt into `world_detail_version=2`, old saves retain v0/v1. Offline geometry/profile checks pass, full build/in-game visual check pending. See `docs/WORLD_ARCHITECTURE_PASS13.md`.

**Derelict buildings (Pass 12):** Source integrated, not run in Minecraft. Distinct abandonment states, closed shopfront signs, broken upper shells, some roof gaps, brick ruin remnants, no shopkeeper in abandoned stores. New-world detail feature only. Theoretical/probability check 714,070 seeded sites passed.

**World detail pass 12: source integrated, build unverified.** New-world-only `world_detail_version=1` enables diverse façades, roofline and yard detail, shop signs, streets, district block sizes, and minor additional interior fixtures. Legacy worlds remain on 0. See docs/WORLD_DETAIL_PASS12.md.


Cavern Motion Pass 10 refines all four cavern mob animation rigs with creature-specific locomotion, footfalls, independently jointed Beetle antennae/mandibles, Sludge drips and Spore sacs. Five-second, 50-frame source-formula previews and offline QA; no NeoForge/client runtime verification. See `docs/CAVERN_ANIMATION_PASS10.md`.

# Latest update — Cavern Motion Pass 09 (2026-10-31)

Cavern mob movement rates and their source model animation cycles were increased in tandem after the user reported they looked slow. Sludge Crawler speed 0.19→0.24, Rust-Eater Beetle 0.22→0.27, Cavern Burrower 0.17→0.23, Spore Crawler 0.25→0.30. A smoother 50-frame, exactly five-second preview is stored in `docs/previews/animation_pass09/`. Rust-Eater Beetles now spawn on the Hole **surface** (industrial weight 9, derelict 6, outskirts 3) as well as underground (weight 18), with biome- and elevation-validated server spawn placement. Added to the Java biome bootstrap and saved generated biome data. Source-only: live NeoForge build and in-game spawning/pathfinding are **not yet verified**. See `docs/CAVERN_MOTION_PASS09.md` and `tools/qa-cavern-motion09.py`. The approved moth animation and its moving light feature were not changed.

## MOVEMENT PASS 08 — 5-second continuous motion previews + better gait

Five-second previews of all eight mobs now reside in `docs/previews/animation_pass08/` with an exact 5000 ms timeline and `tools/render-movement-pass08.py` to regenerate them. Real source motion changes add swing-phase foot lifting for the Roach, Rat, Beetle, Burrower and Spore Crawler, plus a compressing/lifting longitudinal body wave for the Leech. No changes to the approved Glow Moth wing design or six-tooth Leech mouth; no changes to vanilla mining or portal content. All animation changes are mirrored in geometry JSON and regenerated native Minecraft Java model classes. Source/parser/previews offline checked; NO full NeoForge compilation or in-game approval. Start testing against the accumulated test checklist. See `docs/MOB_MOVEMENT_REFINEMENT_PASS08.md`.

## 2026-10-31 — Animation and Glow Moth light Pass 07
Eight original mobs now have individually articulated local-pivot movement cycles. Glow Moth has a server-controlled temporary real block-light source (level 12, 14-tick expiry). Offline GIFs show the authored source geometry and rotation curves.  **Minecraft compilation, illumination and animation in-game still unverified.** See docs/MOB_ANIMATIONS_AND_LIGHT_PASS07.md.

Pass 06: limited Sewer Leech to six visible fangs; remodeled Glow Moth with anatomically appropriate, independently pivoted four-wing animation (source integrated; Minecraft runtime unverified). See docs/MOTH_LEECH_REFINEMENT_PASS06.md.

**Pass 05 update (visual models, not Minecraft-verified):** Glow Moth and Sewer Leech source geometry, textures and Java `LayerDefinition` classes updated to respond to the user’s visual feedback. See `docs/MOTH_LEECH_REFINEMENT_PASS05.md` and `docs/previews/moth_leech_pass05_comparison.png`. No changes to mob drops, existing recipes or worldgen.

# Latest update — Fauna Batch 03 (2026-10-31)

The user APPROVED the more detailed four-creature concept sheet as the quality target. This batch replaces the actual four Java models and textures and adds four original underground mobs (Sludge Crawler, Rust-Eater Beetle, Cavern Burrower, Spore Crawler) with registered entity classes, loot and crafting uses. Native 1.21.1 `HierarchicalModel`, `ModelLayerLocation` and `EntityRenderersEvent` integration retained. Eight model geometry JSON exports are in `docs/model_geometry/`, details and in-game QA at `docs/HIGH_QUALITY_FAUNA_BATCH03.md`. Offline source/resource validator reported **406 checks passed** and Java parser reported **77 source files, no syntax errors**. **No Gradle build or Minecraft runtime verification.** Rendered previews in chat and outside source are *geometry-derived*, not client screenshots; visual parity with approved concept needs a real Minecraft review. This does not advance the full magic system or FTB Quests.

# Latest checkpoint — 2026-10-31 — Fauna & Minerals Batch 02

Source-integrated (NOT client/server build verified): original Sewer Leech and Cavern Glow Moth entities with native NeoForge entity types, renderers, animated Java voxel models, 64×64 texture atlases, Undercity spawns, 1.21.1 entity loot tables, and **functional** medical salve / glow tonic / lamp / silk recipe systems. Glow Moths can be sheared every 6000 ticks, with cooldown persisted across saves. New Zinc and Oil Shale ore blocks and generation, ingots/bitumen/brass, Galvanized Plating and light-emitting Cavern Lamp, original source textures and models, cached recipe outputs, loot and mineable tags. Surviving vanilla ore/cave progression, 30% cave-leading sewer profile, and all prior content deliberately preserved.

Verification: Java parser 64 files 0 syntax errors; Batch02 106/106 source checks; legacy sewer 51/51, Phase3 81/81, Phase4 materials 86/86. `./gradlew compileJava` **could not start** because `services.gradle.org` DNS is unavailable; no in-game tests. Further compiler/runtime checks required. Visual previews of all four mob models are in `docs/evidence/`. See `docs/FAUNA_AND_MINERAL_BATCH_02.md` and `docs/TEST_CHECKLIST_ACCUMULATED.md`. FTB Quests still deferred.

---

## October 31 — Fauna and Mineral Batch 01 source update

Native NeoForge entity registration, models/animation and textures for an original Sewer Cockroach and Scrap Rat; thematic loot and concrete survival uses. Mineable sulfur, rock salt and limestone; chitin masonry; 10 item sprites, resources and generated data. **Offline-only:** 93/93 content cross-checks, 58 Java files parse. Gradle cannot download distribution here; no compiled/runtime proof. Full details and exact QA instructions in `docs/FAUNA_AND_MINERAL_BATCH_01.md`. Proceed with on-PC `runData`, `build`, `runClient`, `runServer` and cross-client verification before calling playable. The current 30% sewer-to-cave layout, deep mining, original Hole atmosphere and portal work are preserved. FTB Quests remain deferred.

# New cavern traversal and room profile (source-integrated; NOT runtime verified)

Versioned per-world cavern geometry now offers broader caves and graded walkable entrances. Static/regression tests pass; real Minecraft/NeoForge tests are pending. The 30% selected cave routes are not guaranteed to intersect a separate natural noise cave. See `docs/SEWER_CAVERN_TRAVERSAL.md`.

# Latest sewer cave frequency: implemented, NOT NeoForge-verified

In new worlds the preset selects roughly 30% cave-leading long passages, while 52% of junctions remain ordinary sewers. Older worlds without `sewer_cave_frequency` retain their original ~9% layout. Offline policy regression: 146,410 decisions, 29.89% cave-leading. Full build/game tests pending.

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
| Sorcerers' World, Hell | `implemented` (source/data only) | Active codecs/dimensions/biomes/ore features. **Not runData/build/server/client verified**. |
| Portals | `planned` | Design in `HANDOFF.md` |
| Sorcerers, smoke, magic, masks | `planned` | `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md` |
| NPCs, factions, trading beyond vanilla villagers | `planned` | |
| FTB Quests | `planned`, on hold | Owner asked not to start |
| Immersion and Compatibility modes, gameplay config | `planned` | Nether and End are untouched vanilla |

## Post-handoff source patch (2026-10-31)

| Feature | Status | Notes |
| --- | --- | --- |
| Elevated relief and level lot foundations | `implemented` | New worlds only; not run in Minecraft |
| Expanded apartment/shop/clinic/factory furnishing | `implemented` | Java syntax only; not client-verified |
| Day/night sky palette and sensor streetlights | `implemented` | Java syntax/biome JSON only; not client-verified |
| Hole-only sky occlusion and smog | `implemented` | Client hooks and fog present; no NeoForge build or in-game test |

See `docs/HANDOFF_TAKEOVER_2026-10-31.md`. Do not inherit pre-patch server-verified
labels for these changes until the new build and tests have actually passed.

## Phase 3 source integration (2026-10-31)

The previously documented Phase 3 draft was moved into live `src/`, registered, and supplied with offline-generated noise, biome, ore and item assets. Offline 81/81 structural checks and Java syntax parsing passed; **these checks do not establish functionality**. See `docs/PHASE3_REALMS_IMPLEMENTATION.md`. Do not label these realms verified until the pinned-JAR generation, Gradle build, dedicated server and client tests pass.


## October 31 follow-on: Phase 4 portal source

- **Implemented in source but NOT build/client/server verified**: Artificial Door and Devil Gate shared backend, portal cores, source frames, no-drop linked return endpoints, recipes, item sprites and localization.
- **Material acquisition implemented in source**: smoke-soaked filter from Industrial Debris and chest loot, smoke residue from smelting, expensive craftable pre-Hell Devil Sigil.
- **Offline checks only**: 86/86 material assets/recipes/loot checks; 81/81 existing Phase 3 checks; 45 Java files syntax parse; full Minecraft build not available in this environment.
- **Not yet implemented/verified**: Smoke Door, En Family Door, claims hook, magic costs, real client visual check and in-game portal travel/return. See `docs/PHASE4_PORTALS_HANDOFF.md`.

## Sewer–cave hybrid continuation (2026-10-31)

| Feature | Status | Notes |
| --- | --- | --- |
| Shallow sewers, mid-level drains, closed inspection rooms | `implemented` | Java syntax and static checks only; no client/server runtime test |
| Deep natural caves, aquifers and mining | `implemented` (preserved in noise settings) | Original noise restored below Y=24; actual resource distribution not runtime verified |
| Rare sewer-to-cave transition tunnels | `implemented` | About 30% of newly generated sites; graded masonry → ruins → rock → cavern; needs exploration QA |
| Sewer salvage chest loot, brick material, recipes, texture | `implemented` | JSON and asset checks pass; craft/loot not in-game tested |

**Do not use historical server-verification labels as evidence that the new hybrid preset works.**


## Creature Fidelity Pass 04 (2026-10-31)
Eight existing actual model classes / custom UV atlases rebuilt from the approved visual direction, with 4 detailed existing mobs and 4 new species. Offline source tests only; Minecraft runtime build, texture rendering and visual match remain pending. See `docs/MOB_FIDELITY_PASS_04.md`.
