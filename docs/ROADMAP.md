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

**Current source checkpoint Pass 13:** Add coherent neighborhood identities and six facade geometries, five skyline variants, eight lot setbacks, and revised block mix to the Hole. Test in new worlds, do not upgrade an old save worldgen flag; full build and screenshots remain outstanding. See `docs/WORLD_ARCHITECTURE_PASS13.md`.

**Pass 12 ruins integration:** Abandoned and crumbling buildings vary by district and lot: boarded shops, fractured roofs and upper walls, remnant walls, debris, damaged façades. First in-game review must evaluate whether ruin density and variation look organic, verify roof access and loot/shops, and test chunk seams.

**Pass 12 integrated (not verified in-game):** whole-world structural detail pass for city palettes, shapes, roofs, service yards, street wear, drainage, and neighborhood-level variation. Follow with a fresh-world visual review and build before calling release-ready.


## October 31 — Fauna and Mineral Batch 01 source update

Native NeoForge entity registration, models/animation and textures for an original Sewer Cockroach and Scrap Rat; thematic loot and concrete survival uses. Mineable sulfur, rock salt and limestone; chitin masonry; 10 item sprites, resources and generated data. **Offline-only:** 93/93 content cross-checks, 58 Java files parse. Gradle cannot download distribution here; no compiled/runtime proof. Full details and exact QA instructions in `docs/FAUNA_AND_MINERAL_BATCH_01.md`. Proceed with on-PC `runData`, `build`, `runClient`, `runServer` and cross-client verification before calling playable. The current 30% sewer-to-cave layout, deep mining, original Hole atmosphere and portal work are preserved. FTB Quests remain deferred.

# Cavern traversal extension (source-only checkpoint, 2026-10-31)

Added larger deterministic cave destinations and a graded entry walk to the cavern floor; 30% junction frequency unchanged. Saved `sewer_cavern_version` preserves existing saves' old geometry. Vanilla deep mining is unchanged. Requires real NeoForge build, client, and server traversal verification before advancing phase status. See `docs/SEWER_CAVERN_TRAVERSAL.md`.

# Sewer frequency update (2026-10-31)

The source now targets **30%** of arterial sewer intersections for long cave-leading routes (previously ~9%); 18% are inspection branches and ~52% remain ordinary sewers. Saved generator field keeps the old selection for existing worlds. This is **source integrated / offline tested only**, not verified in-game. No changes to vanilla mining or cave noise.

# Roadmap

The phase order comes from `docs/spec/SANDBOX_DIRECTIVE.md`. Status values are `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified` and `server_verified`. Evidence for anything past `implemented` is in `docs/TEST_RESULTS.md`.

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Project audit | Done, see `docs/PROJECT_AUDIT.md` |
| 2 | Hole sandbox foundation | In progress |
| 3 | Sorcerers' World and Hell generation | **Source integrated; awaiting NeoForge build, server and client QA** |
| 4 | Portals: Artificial Door / Devil Gate backend, Smoke Door and En Family Door remaining | Source-integrated (partial); no NeoForge build/client verification |
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

## Phase 3 handoff from this session

See `docs/PHASE3_REALMS_IMPLEMENTATION.md`. Biome/dimension definitions have been added to active sources with offline structural validation. Phase 4 portals remain blocked on real-world verification of the two dimensions.

### Phase 2.5: sewer–cave hybrid

Source-integrated: shallow connected sewers and flood infrastructure, ordinary natural caves below Y=24, deliberately rare long branches into caverns. **Mining and Minecraft vanilla resource progression must remain intact.** Status is `implemented` only; see `docs/SEWER_UNDERWORLD.md`. Verify in actual Minecraft before closing this work item. Do not regress to the previous cave-suppression design.
