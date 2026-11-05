# Latest: Pass 70 — expanded connected storm drains and graded natural-rock cave approaches

**Source-only, uncompiled against NeoForge.** New world-detail v52 adds two-mouth parallel stormwater bypass galleries, 3 construction variants on sewer walls, and gradually widening rock at cave branch approaches. The three-wide cave staircase and main municipal dry lanes stay open, the 30% cave-branch selection target is unchanged, and vanilla structure safeguards remain active. Complete details: `docs/PASS70_CONNECTED_STORM_DRAINS_CAVE_TRANSITIONS.md`. Wednesday handoff: `Pass70_Claude_Wednesday_Handoff.md`. Targeted Java fixture: **171,461 checks passed**; source QA: **30/30 passed**. Historical passes 69–54 and 53–41 passed in two runs; full single chain timed out after Pass54. **Actual NeoForge/MC build and gameplay unverified.**

---

# Pass 69 CURRENT CHECKPOINT — Vanilla piston sluice controls (2026-11-03)

Fresh-world `world_detail_version: 51` extends the Pass68 14×19 stormwater pumping halls with a **local, lever-operated sticky-piston sluice** feeding a two-cell runoff. Vanilla iron block movement is intended to close/reopen a narrow water channel; the dry bridge, main sewer and repair benches remain accessible without techniques. Versions 0–50 retain previous gallery geometry, 30% cave-branch target remains, and vanilla structure protections remain. **OFFLINE SOURCE ONLY — NOT NEOFORGE-COMPILED OR IN-GAME VERIFIED.** Actual lever power and fluid updates must be tested in Minecraft on Wednesday, Nov 4. Tests: `bash tools/qa-pass69-all.sh`; details: `docs/PASS69_REDSTONE_SLUICE_CONTROLS.md` and `Pass69_Claude_Wednesday_Handoff.md`. This supersedes older latest-checkpoint headings below.

---

# Pass 68 CURRENT CHECKPOINT — Pump halls and no-techniques sewer survival (2026-11-03)

Fresh-world `world_detail_version: 50` adds rare 14×19 masonry stormwater halls connected by an open 3-wide entrance to the existing seven-wide municipal sewer, contained wet sumps and dry footbridges, practical vanilla workstations, and an optional operator room with a manual lever-operated iron door. Pass67 small bay generation reserves the same footprints only in new v50 worlds. No extra lanterns/spawners/chests or artificial need for techniques. **100,679 Pass68 production-class Java fixture assertions and 34/34 source checks passed; earlier Pass41–67 offline regressions completed across split runs. UNCOMPILED against real NeoForge; no Minecraft playtest.** See `docs/PASS68_STORMWATER_PUMP_HALLS_AND_MANUAL_CONTROLS.md` and `Pass68_Claude_Wednesday_Handoff.md`; run `bash tools/qa-pass68-all.sh` (long). As agreed, Claude restores Gradle and builds on Wednesday Nov 4. This supersedes the prior latest-checkpoint headings below.

---

# Pass 67 CURRENT CHECKPOINT — New worlds v49 (2026-11-03)

Added sparse, walk-in municipal sewer repair and filtration bays on the upper trunk walls, with real vanilla workstations, no new lamps/chests/spawners and no obstruction of the municipal water channel or dry walkways. Both side and along-axis positions are deterministic; room placement avoids shafts/intersections, ocean/coast, and inherits real vanilla structure protection. Pass67 Java geometry tests and integration tests are included. **Uncompiled against real NeoForge; in-game verification remains mandatory.** Details: `docs/PASS67_CONNECTED_SEWER_SERVICE_BAYS.md`; run `bash tools/qa-pass67-all.sh`. The latest complete checkpoint supersedes Pass66 for Wednesday's Claude build.

## CURRENT SOURCE CHECKPOINT — PASS 66 (November 2, 2026)

Fresh `world_detail_version: 48` reserves a walkable dry coast around Pass65 ocean basins, introduces real vanilla beach/snowy-beach biome holders on the near-sea strip, suppresses city construction/block entities and artificial residents in reserved shoreline columns, and keeps old v47 and earlier save generation intact. **SOURCE ONLY — not NeoForge-compiled or in-game verified.** New tests: 37/37 source checks, production-class Java coast + seabed fixtures (255 dry coast / 119 sand beach cells), and earlier offline regression family passed across time-split runs. Runtime beach ecology/turtles/monuments, coast appearance/performance and vanilla survival completion remain unverified. Read `docs/PASS66_WALKABLE_VANILLA_BEACHES_AND_COASTAL_ACCESS.md` and `Pass66_Claude_Wednesday_Handoff.md` first. Claude will handle verified Gradle wrapper and NeoForge 1.21.1 build Wednesday Nov 4 as user directed.

---

## CURRENT SOURCE CHECKPOINT — PASS 65 (2026-11-02)

Fresh-world version **47** enables broad real-water vanilla ocean basins (normal/deep/cold/deep-cold ocean biome IDs) outside protected randomized starting regions, preserving land biomes and city/sewer systems. New ocean-aware Canvas stops submerged urban block entities before placement, ocean painter shapes ocean seabed and surface water, while old saved generators retain their prior biome-source flag and version. **SOURCE ONLY, NOT A COMPILED OR INSTALLABLE MOD.** Offline Java ocean and bathymetry fixtures, 29 source checks, and previous pass regression chains pass; actual vanilla ocean structures, biomes, end completion and live performance are NOT verified. See `docs/PASS65_TRUE_OCEAN_BASINS_AND_VANILLA_WATER_STRUCTURES.md` and `Pass65_Claude_Wednesday_Handoff.md`.

---

## CURRENT SOURCE CHECKPOINT — PASS 64 (2026-11-02)

**Fresh-world detail profile 46. SOURCE ONLY, NOT A PLAYABLE MOD.** Registered vanilla structure starts (strongholds, mineshafts, trial chambers, Ancient Cities) now supply 3D protective envelopes to the custom city/sewer painter. All block and block-entity writes respect them; v45 and older saved profiles remain unchanged. Pass63 Deep Dark climate guard retained. New Java fixture: **220,320 assertions**; new QA **33/33**; Pass41–63 offline suites completed. **NeoForge compilation, live worldgen and vanilla End completion still NOT verified**; planned with Claude Wednesday Nov 4. Read `docs/PASS64_VANILLA_STRUCTURE_START_PROTECTION.md`, then `Pass64_Claude_Wednesday_Handoff.md`. This replaces all below status headings; the older entries are historical.

---

> **Latest source: Pass 60, version 43 (November 2, 2026).** Three-wide cargo approaches and where-possible connected industrial courtyards. **Not installable or NeoForge compiled.** Run `bash tools/qa-pass60-source.py` (via python3), `bash tools/qa-pass60-freight.sh`, then historical offline suites. Wednesday Claude build handoff: `docs/PASS60_ACCESSIBLE_INDUSTRIAL_FREIGHT_COURTS.md`.

**LATEST SOURCE PASS: 59 (2026-11-02), world_detail_version 42.** Industrial workshop courts gain a three-wide entrance-to-street walk and a small functional wash/repair area, with prior systems retained. **Not NeoForge compiled**; official wrapper restore and build reserved for Wednesday Nov 4. Read `docs/PASS59_WORKING_SERVICE_COURTS_AND_CLAUDE_HANDOFF.md` and `HANDOFF.md` before further work.

---

## CURRENT SOURCE CHECKPOINT — PASS 58 (2026-11-02)

Latest fresh-world preset: **world_detail_version 41**. Some sufficiently large factories and warehouses gain real shared-wall low service bays with two distinct functional rooms, an interior hall connection, open loading setback, and reachable lower roof. Western Pass56 covered workshops remain in place; biome-independent Pass52/57 development and vanilla survival are preserved. No new outdoor lamps or public loot chests. Older saved-world v0–40 generation stays on original logic. **Verified offline:** 18/18 source checks, 3,600 actual Java geometry fixtures, 2,090 annexes, 71 chunk checks, 515 legacy unchanged cases, and full Pass41–57 offline regression suites. **NOT VERIFIED:** NeoForge `build`, `runData`, client/server or Minecraft rendering; official Gradle wrapper JAR still missing. See `docs/PASS58_SHARED_WALL_INDUSTRIAL_ANNEXES.md`; run `bash tools/qa-pass58-all.sh`. Historical material follows.

---

## CURRENT SOURCE CHECKPOINT — PASS 57 (2026-11-02)

**Fresh worlds use `world_detail_version: 40`.** Industrial neighborhoods now remain functional working neighborhoods across snow, dust, badlands, mountain and other climates even when the urban fabric is not dense. Four coordinated production/freight/salvage/worker-support subdistricts retain real factories, warehouses, repair shops and housing, while **open country preserves the original survival/biome lot selection**. Older worldgen versions are unchanged. Offline QA: `bash tools/qa-pass57-all.sh`. **Source-only; still missing official Gradle wrapper JAR and actual Minecraft/NeoForge compile and visual playtests.** See `docs/PASS57_COHERENT_INDUSTRIAL_BELTS.md`. Historical pass notes follow.

---

## CURRENT SOURCE CHECKPOINT — PASS 56 (2026-11-02)

**New worlds use `world_detail_version: 39`.** Industrial service yards now include traversable sheltered routes between real workshops and factory/warehouse interiors (rather than 1-block leftover side strips). Fully version-gated; preexisting Minecraft save profiles are unaffected. Run `bash tools/qa-pass56-all.sh` for source/Java fixture tests. Read `docs/PASS56_COVERED_INDUSTRIAL_SERVICE_YARDS.md` for exact scope and unverified Minecraft QA. **Source-only: missing Gradle wrapper JAR, no full NeoForge build or fresh-world visual playtest.**

---

## CURRENT SOURCE HANDOFF — PASS 55 (2026-11-02)

Newest fresh-world profile: **38**. Larger homes, apartments and multi-floor shops can include a *real* connected service wing (laundry/repairs/food prep) and a reachable stepped maintenance roof, changing silhouettes without filling city streets or opening redundant chests. Built-in rooms are purposeful and the original staircase / entrance is protected. Old save profiles remain untouched. **Java offline QA passed; full NeoForge compilation and Minecraft visual testing remain unverified.** Read `docs/PASS55_LIVED_IN_ATTACHED_SERVICE_WINGS.md`. Offline QA: `bash tools/qa-pass55-all.sh`. All historical status reports below are superseded.

---

## CURRENT CHECKPOINT — Pass 54: Crooked local streets (SOURCE ONLY)

Fresh `world_detail_version: 37` worlds gain seeded, doglegged pedestrian passages and nonuniform parcel frontages tied to urban density **independent of climate**. At least 3-block cross alleys and 5-block through-lanes are kept open; historical v0–v36 generator profiles retain their old paths. In-game screenshots, NeoForge compilation and functional navigation tests remain outstanding because the official Gradle wrapper JAR is absent. **Read `docs/PASS54_CROOKED_LANES_AND_CITY_SILHOUETTE_HANDOFF.md` for the authoritative status.** Offline QA: `bash tools/qa-pass54-all.sh`.

---

## Pass 53 — biome-independent parcel geometry (source-only)

The new world-detail v36 profile makes actual parcel sizes and walkable local alleys track the seed-based urban density map, not climate labels. Snow/desert/badlands can support compact occupied blocks; open country gets fewer small city-grid parcels. See [`docs/PASS53_URBAN_PARCEL_MORPHOLOGY.md`](docs/PASS53_URBAN_PARCEL_MORPHOLOGY.md). The entire project remains **uncompiled and not installable** until a NeoForge build is completed.

> **Latest source pass: 52 (version 35 fresh worlds).** The Hole's urban density now varies **independently** of seed-dependent climate, allowing real dense neighborhoods and open survival countryside in snowy, desert/dust, dead-taiga, badlands and mountain-edge biomes. Historical worlds remain version-gated. **Source only; full NeoForge build and in-game testing are outstanding.** See [`docs/PASS52_BIOME_INDEPENDENT_HOLE_CITY.md`](docs/PASS52_BIOME_INDEPENDENT_HOLE_CITY.md) and [`docs/HOLE_WORLD_VISUAL_AND_SURVIVAL_RULES.md`](docs/HOLE_WORLD_VISUAL_AND_SURVIVAL_RULES.md).

> **Current checkpoint: Pass 51 (2026-11-02)** — New `world_detail_version: 34` adds the complete south-frontage pedestrian route check for regional starts and synchronizes first-spawn candidate selection. Pass 51 and legacy offline tests passed. **Full NeoForge build and in-game worldgen remain unverified**; see `docs/PASS51_REGIONAL_START_ROUTE_AND_SPAWN_CACHE.md`.

# CURRENT CHECKPOINT — Pass 49: Region-aware terrain blending (SOURCE ONLY)

New-world `world_detail_version: 32` removes false periodic terrain troughs between same-biome 512-block cells, while preserving true biome-edge smoothing. Old version-31-and-earlier saves keep their terrain and spawn rules. **Not NeoForge-compiled, not a playable JAR.** See `docs/PASS49_REGION_TERRAIN_CONTINUITY_HANDOFF.md`; offline regression: `bash tools/qa-pass49-all.sh`. The full build still needs network access to restore Gradle's verified wrapper.

---

# Pass 47 — Different starting biomes and settlement densities (SOURCE ONLY)

Fresh `world_detail_version: 31` Hole worlds choose a seed-dependent starting environment (city, snowfields, dead taiga, dust basin, iron badlands, mountain rim), with a protected regional landing/shelter. Each outer biome can now contain inhabited town pockets, abandoned areas, and open country; previous saved-world versions retain their old generator rules. **Not compiled and not an installable mod.** Read [`docs/PASS47_VARIABLE_REGIONAL_SPAWNS_SETTLEMENTS.md`](docs/PASS47_VARIABLE_REGIONAL_SPAWNS_SETTLEMENTS.md), then run `bash tools/qa-pass47-all.sh` for offline checks. The Minecraft build and actual spawning still require runtime verification.

---

# Pass 46 — Neighborhood-scale architecture and build verification

The freshest world preset is now world-detail v30, with coherent occupied rooflines in the central city. Existing saves retain their own version. **This is uncompiled source; not an installable Minecraft mod.** For specifics and the exact Windows verification command, read `docs/PASS46_NEIGHBORHOOD_SKYLINE_AND_BUILD_HANDOFF.md`.

# PASS 42 — SOURCE-ONLY FRONTIER / THE HOLE IDENTITY CHECKPOINT (2026-11-02)

**Not compiled; not a game build or installable mod.** The Pass 41 archive is extended by five polluted region biomes, mountain and badland elevation, purpose-led regional lots, paved material distinctions and sparse tram stops, while preserving the crowded Hole core. **Newest authoritative status and build blockers:** [`docs/PASS42_HOLE_REGIONS_CITY_IDENTITY_HANDOFF.md`](docs/PASS42_HOLE_REGIONS_CITY_IDENTITY_HANDOFF.md). Existing paragraphs below are historic checkpoints, not evidence of Pass 42 validation.

---

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

## Pass 43 source checkpoint — November 2, 2026

This archive is **not a compiled NeoForge mod**. A new-world v27 source pass
improves inhabited markets, plaza, alley repairs, sparse quay job sites and
streetlamp spacing against the owner's Nov 1 in-game photos. Run
`bash tools/qa-pass43-all.sh` for offline fixtures, then restore the official
Gradle wrapper and perform genuine `runData`, `build`, `runClient`, server
and gameplay tests before installation or publication. See
`docs/PASS43_WORLD_IDENTITY_SCREENSHOT_REFINEMENT.md`.

## Pass 44 source checkpoint — November 2, 2026

**UNCOMPILED source, NOT a NeoForge mod JAR.** New-world v28 integrates restrained residential rainwater/vent structures, service-front roofline massing, and sheltered factory/warehouse loading bays without obstructing main doors, roads or existing rooms. It is based on the owner's actual November 1 Minecraft screenshots, not new gameplay screenshots. See `docs/PASS44_HOLE_SKYLINE_FUNCTIONAL_FACADES.md` and run `bash tools/qa-pass44-all.sh` for offline checks. Full Gradle, client and server testing is still required before installation or CurseForge distribution. Legacy saved generator settings remain unchanged.

## Pass 45 source checkpoint — November 2, 2026

**UNCOMPILED; NOT AN INSTALLABLE MOD.** New-world `world_detail_version: 29` selects a minority of central-city large vacant or rubble lots for usable, distinct two-room repair / wash / processing service courts with two independent clear front approaches. No new exterior lighting, public loot chests, villager overhaul, pollution or deferred biome hazards. See `docs/PASS45_WORKING_CITY_SERVICE_COURTS.md`; run `bash tools/qa-pass45-all.sh` before full NeoForge, client, server and GameTests. Earlier saves remain on their stored generator settings.

## Pass 48 — Build-ready Windows entrypoint (source-only)

Run `BUILD_AND_TEST_WINDOWS.cmd` from a fully extracted checkout on Windows with JDK 21 and internet access. The script verifies and downloads the official pinned Gradle wrapper if necessary, then runs `help`, `runData`, and `build` with logs in `build/verification`. The Gradle configuration now defaults to `disable_recompilation=true` for lower memory use, overridable with `-Pdisable_recompilation=false`. This archive still has no Gradle wrapper JAR or verified NeoForge build; see `docs/PASS48_BUILD_PREP_AND_CLAUDE_HANDOFF.md`.

## Latest source checkpoint — Pass 50 (2026-11-02)

Fresh `world_detail_version: 33` checks whether the regional starting plaza can be walked from its furnished shelter at one-block step height, and will search alternate fringe biomes before falling back downtown if a requested biome has no suitable start. Earlier saves keep their old settings. Run `bash tools/qa-pass50-all.sh` for offline tests. **The archive is uncompiled; full NeoForge build and Minecraft client QA are still mandatory.** See `docs/PASS50_REGIONAL_START_ACCESS_AND_FALLBACK.md`.

## Pass 61 — Underground survival without mandatory techniques

Fresh version-44 worlds feature selective dark municipal sewer runs and uncommon, optional breakable roach-spawner nests in existing inspection rooms. Vanilla torches, shields, armor, tools, food, mining and retreat remain valid; no new poisoning or technique gate. Old-world detail versions <=43 retain the old lantern pattern and uninfested inspection rooms. See `docs/PASS61_SEWER_SURVIVAL_WITHOUT_TECHNIQUES.md`, `tools/qa-pass61-all.sh`, and the Wednesday Claude handoff. Source-only until verified with NeoForge in Minecraft.

## Pass 62 — Vanilla Minecraft 1.21.1 completion compatibility

**Vanilla survival must remain beatable without techniques.** This pass adds 19 land-structure biome-tag extensions for Hole climate biomes (temples, villages, igloos, ruins, trial chambers, etc.) without replacing vanilla tags. It retains vanilla Nether, End, strongholds, mineshafts and natural deep dark. It also fixes regional-spawn assumptions in the headless QA tool.

Run `bash tools/qa-pass62-all.sh`. See `docs/PASS62_VANILLA_MINECRAFT_COMPLETION_AND_STRUCTURE_TAGS.md` and `docs/PASS62_STRUCTURE_RUNTIME_CHECKLIST.md` for detailed Wednesday testing. **Uncompiled source only. No real Minecraft completion, Warden, End or structure survival verified.** Real oceans/ocean exploration and the distinct custom Forgotten City are **not implemented** and remain hard blockers for the user's full parity requirement. New structure biome tags can affect newly generated chunks in existing saves; back up old worlds.
