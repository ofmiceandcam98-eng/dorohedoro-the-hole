# LATEST SOURCE CHECKPOINT — PASS 32 (2026-11-01)

**Read `docs/PASS32_PURPOSE_FIRST_AUDIT.md` before modifying this source.** This is the latest source checkpoint, **NOT a compiled or Minecraft-tested release**. The default new world now uses world-detail profile 19 to prevent unnecessary permanent canal fish and unsupported supply barrels; versions 0–18 preserve their previous choices. The cosmetic preserved-conduit edit from Pass 31 was rolled back. Phase 3 Hell and Healing tests now verify gameplay/schema intent instead of obsolete text.

**Project rule:** do not add items, rooms, decor or mechanics without a meaningful survival/exploration/worldbuilding purpose. World quality and build reliability come first. The original housing, sewer dungeons, creatures, rivers, lighting (Cavern Lamp, lanterns, torches), and magic stay intact. Faction/villager and Ash-Snow systems remain deferred.

---

# CURRENT CHECKPOINT — PASS 29 (2026-11-01)

Fresh-world procedural housing facades, rooflines and added domestic furnishings use saved `world_detail_version: 17`. Read `PASS29_WORLD_HOUSING.md` and `CHANGED_FILES_PASS29.md` for exact source changes, tests and limitations. **This version has not been compiled or tested in Minecraft.** Older saved versions remain on their previous generator profiles.

> **LATEST SOURCE PASS — 2026-11-01:** Procedural city density + houses, shops, furnishing and light updates; read `PASS25_PROCEDURAL_HOUSING_CITY_AND_LIGHTING.md`. This is NOT a compiled game build. Save profiles for existing worlds remain versioned.

> **LATEST SOURCE CHECKPOINT — PASS 24 (2026-11-01):** Fish-bearing, curving variable-width urban rivers with tiered shorelines, water-sensitive ecology, sewage detailing and interior furniture. **Source only; NOT COMPILED OR IN-GAME TESTED.** Read `PASS24_RIVERS_ECOLOGY_SEWERS_INTERIORS.md` before continuing. Preserves Pass23 entrances and Pass22 dungeon.

> **LATEST SOURCE CHECKPOINT — PASS 23 (2026-11-01):** Canal/riverbank sewer entrances are source-integrated, source-only QA tested and **not Minecraft compiled**. Start with `PASS23_WATERWAY_ENTRANCES.md`. Older profiles are preserved.

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

**Current latest source milestone: Pass 14 irregular city and structural architecture.** Fresh worlds use `world_detail_version: 3`: varied macro block ratios, offset local alleys, cross-city diagonal boulevards, L-shaped and courtyard tenements, terraces and industrial outbuildings. Source and geometric offline checks passed; full Minecraft/NeoForge build and gameplay have **not** been verified here. See `docs/IRREGULAR_CITY_AND_BUILDING_FORMS_PASS14.md` and `docs/TEST_CHECKLIST_ACCUMULATED.md`.

**New in Pass 12:** Weathered, boarded and partially collapsed derelict buildings, richer streets and façades, varied skyline and vacant lot remnants. Preserves vanilla mining and existing saves; new-world-only settings. See `docs/WORLD_DETAIL_PASS12.md`.

**Latest architectural content update:** Pass 12 brings world-scale building/street detail and more varied neighborhood blocks; opt-in via `world_detail_version: 1` on *new* worlds. Legacy saves preserve old geometry. See docs/WORLD_DETAIL_PASS12.md. Full Minecraft build pending.


> **Latest source checkpoint (Pass 11):** All eight existing Dorohedoro mobs have Creative-mode spawn eggs with different colors. Look in **The Hole: Creatures** or **Spawn Eggs** and use the test commands in [`docs/SPAWN_EGGS_PASS11.md`](docs/SPAWN_EGGS_PASS11.md). Minecraft runtime remains unverified here due to Gradle download unavailability.

# Dorohedoro: The Hole — working source snapshot

The Hole is a procedurally generated Minecraft Java/NeoForge survival overworld. The experimental `dorohedoro:hole_sewers` preset adds sewers, maintenance tunnels, galleries and storm cisterns **while retaining natural caves and Minecraft mining at depth** in new worlds. See `docs/SEWER_UNDERWORLD.md` for source and testing status; **do not assume this source has passed a real Minecraft build**.

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

## Takeover update — terrain / interiors / day-night (2026-10-31)

This source contains experimental elevation relief and richer generated interiors.
The default Dorohedoro world preset enables `city.terrain_relief` for newly created worlds;
older saved generators without this field remain flat to avoid unexpected seams.
Nighttime streetlamp switching and new sky colors require actual client testing.
A client-only smog-canopy patch now hides vanilla celestial sky rendering in Hole biomes only.
It leaves the physical sky/weather/light mechanics intact and **has not been client-tested**.
**Read `docs/HANDOFF_TAKEOVER_2026-10-31.md` before merging or installing.**


### Character progression canon note (Pass 19.1)
The future first-join RPG interface will recognize Human, Sorcerer and Devil classifications, but only Humans and Sorcerers are ordinary starting choices. Devilhood is a late-game Sorcerer ascension; one innate Sorcerer magic type per character. **Design docs only: not implemented.** See `docs/DOROHEDORO_SPECIES_MAGIC_DEVILHOOD_PASS19_1.md`.
