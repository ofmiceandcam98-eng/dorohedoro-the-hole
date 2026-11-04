# Pass 44 — Inhabited skyline & building-service architecture

**2026-11-02 · SOURCE-ONLY · UNCOMPILED · NOT AN INSTALLABLE MOD**

Baseline: Pass 43 consolidated source, including the Pass 42 five outer region biomes, the Pass 43 working markets/streets/canals, and all earlier interiors, night light controls, sewers, techniques, and creatures. Existing saves must not be switched to a newer generator version.

## Source changes

* Introduced `FunctionalFacade44.java` for parcel-contained building services. For multi-storey residential buildings it adds restrained exterior rainwater service runs above 3-block pedestrian headroom, localized parapet shielding, and selected 3×3×4 high roof ventilation/filtration risers on the accessible roof's south-west service side. Selected roofs also receive one **usable vanilla cauldron** for outdoor rainwater collection, rather than a fake storage chest.
* Added industrial canopy source to the existing **warehouse and factory** generation paths, with wide clearance for the cart entrance. Only lots with enough southern setback qualify. Corners have elevated supports; no new lights or loot blocks are added. Selected factories get a second, lower-scale extraction hood behind their existing smokestacks. This changes their roof silhouette without replacing or duplicating the existing tall chimneys.
* Each placement is coordinate deterministic, chunk clipped and constrained by the generating `Lot` bounds. The new exterior writer operates only into air. No interior beds, stair flights, windows, building floor geometry, tram rails, canals, sewers, spawn square, magic, ore distribution or saved world's existing profile is intentionally modified.
* New world preset opts into `world_detail_version: 28`. The codec accepts 0 through 28 and still defaults missing version fields to 0, ensuring previously saved generators keep their legacy setting.
* Updated the older Pass 41 frontage *test fixture* to stub the newer Pass 42 climate enum, fixing a source-only Java compilation failure in the test harness. The actual generation code was not changed by this repair.
* Updated Pass 43 static QA's exact-27 expectation to accept later versions while still checking all Pass 43 features remain present.

## Why this pass, based on the player's own Minecraft screenshots

November 1 images show large regular facades and rooflines that do not yet make the industrial city feel fully inhabited. The visuals already have smog, tall factories and walkable interiors. Adding more flat textures would not correct the massing. The new structural accents express real needs (rain capture, vertical utilities, industrial loading, ventilation) and create additional silhouette variation at rooftop height rather than filling plazas with random decorative blocks.

The original screenshot overview is included at `docs/evidence/nov1_actual_minecraft_contact.jpg` as a baseline for new-v28 client comparison. These are **older screenshots from the user's world**, not renders of Pass 44.

## Verification executed

- **16/16 Pass 44 static source checks**: versioning, previous passes, preservation of world systems, JSON readability, lighting and lot-bound placement conditions.
- **Pass 44 Java fixture** compiled the *actual new source* on Java 21 against narrow Minecraft block stubs: **4,320 seeded building scenarios**, 39,772 new block writes; 4,002 cells in tall roof structures and 24,648 canopy frontage cells in synthetic footprints. Full-area painting and 16×16 tiled painting matched exactly. A synthetic invariant checked every added block stays on its lot, above the 3-block access headroom threshold, and away from the main ground-floor door.
- **Pass 43**: 16/16 static checks plus four chunk-order/aisle/street/river fixture tests.
- **Pass 42**: 26/26 static checks and compiled climate selector test; all six selector categories encountered.
- **Pass 41**: 95 parsable JSONs, 198 decodable PNGs, 64/64 technique icons; historical geometry fixture 55,696 lots with 0 approach-grade regressions, and the earlier facade 6,300-building fixture.
- **Pass 35 night-lighting**: 11/11 static checks.
- **All source ZIPs**: CRC-tested after packaging.

**These are source-only offline checks. None demonstrates a full NeoForge compile, real-world walkability, solid rooftop support, 3D appearance, vanilla cauldron weather behavior or an in-game nighttime hostile spawn rate.**

## Real-client quality gates required before calling this visually complete

1. Restore verified official Gradle wrapper JAR (missing from this archive), JDK 21 and pinned NeoForge 1.21.1 toolchain; run `runData`, `build`, `runClient`, a dedicated server, and GameTests. Keep stack traces/logs when compiling.
2. Generate fresh v28 worlds with at least three seeds. Walk the same five screenshots' locations from November 1: tram street, canal intersection, market, plaza, and elevated skyline. Capture equal-FOV before/after screenshots at day/night. Confirm the *new* silhouettes are coherent; remove over-dense or misplaced pipes if not.
3. Verify upstairs bedrooms and stair routes, roof gardens and existing shelters, every shop entrance, all industrial 3-block cart doors, roof turbine/duct supports and weather cauldrons, and sidewalk/lot transitions over slopes. In particular ensure no protruding facades overlap streetlamps or player-sized paths in steep hillside districts.
4. Ensure canopy and service lines look natural under Dorohedoro's gloomy smog, not like excessive decorative scaffolding; check visible glow does not increase. Measure hostile spawning in the same night streets in which zombies were previously seen.
5. Validate all five new climate regions and their mountain approaches; preserve 30% sewer-to-cave profiles, ore/mining, all 64 technique icons, portals, creatures, and survival recipes; retest the previous crash with real `latest.log` / crash report.

## Not part of this pass

No Ash-Snow, Sludge-Ice, toxic snowballs, factions, villager overhaul, Cross-Eye camps, client renderer rewrite, generic lamp additions, fake loot chests, or new item PNGs. The original Curse Mark crash is still unresolved without runtime reproduction. **Do not install or publish this as a working `.jar`.**
