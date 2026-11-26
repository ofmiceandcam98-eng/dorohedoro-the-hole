# HOTFIX — PORTAL LANDING (current test feedback)

User has begun hands-on testing and observed **"No safe unoccupied location found for the return door"** after activating an Artificial Door. This confirms source frame validation succeeded; destination-site selection failed. The verified code-level mismatch is fixed in `PortalService.Frame.stand()`: heightmap first-air Y was being raised by another block, causing the landing support test to reject flat ground. See `docs/PORTAL_DESTINATION_LANDING_PASS18_FIX.md` and `tools/qa-portal-landing-pass18.py`. **Must rebuild and test in-game before claiming fixed.** No recipe, terrain generator, or world-save changes made. Also user requests Cavern Lamp become a recognizable actual lantern/lamp rather than cubic block; *visual redesign not included in this hotfix*. User has requested all existing recipes be audited (see their separate audit report).

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

For specific source details see docs/DISTRICT_IDENTITY_PASS18.md. This is the latest source handoff; older headings in existing docs are historical and may reference earlier "latest" checkpoints.
