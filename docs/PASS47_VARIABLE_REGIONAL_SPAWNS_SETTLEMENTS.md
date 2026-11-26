# Pass 47 — Seed-variable starting regions and mixed-density frontier settlements

**Source checkpoint only — Minecraft 1.21.1 / NeoForge. NOT compiled, installed or visually verified.** Built from Pass 46; preserved prior city, river, transit, mining, magic, custom mobs, and the staged/delayed mechanics plan.

## User requirement

The player must not always start in the dense downtown plaza. Snowfields, dead taiga, dust basin, iron badlands and mountain-rim biomes should be legitimate starting places. Equally, an entire biome must not be all wilderness or all city: varied occupied neighborhoods, ruined outskirts and open land belong in each outer province. Every start must remain inside The Hole overworld, not a separate overworld or the Sorcerers' Realm.

## Actual source changes

- New saved `world_detail_version: 31` for **new** Hole world presets; old 0–30 `level.dat` profiles continue with their previous city, location and version-dependent features. Version 0 without a stored value still defaults to 0.
- `StartingRegions47.java`: deterministic seed-selected preferred first biome, uniformly among CITY / SNOWFIELDS / DEAD_TAIGA / DUST_BASIN / IRON_BADLANDS / MOUNTAIN_RIM. For noncity starts, samples up to 1,024 candidate superblocks and accepts only those where the climate-backed **actual `HoleRegions42` selection** agrees at the landing center and four corners, the usable lot fits, and no unique reserved landmark is replaced. If there is no viable matching candidate, falls back safely to the original downtown starting plaza. There is no promise a specific rare biome exists close enough for every seed.
- `CityPlan.java`: changes new-world spawn to the center of the selected reserved landing. Its Y coordinate uses the same frontage terrain grade as the lot painter, rather than blindly placing a player at Y=65 in a mountain. Old worlds still use exactly `(0,65,0)` in `spawnPoint()`.
- `Superblock.java`: new first-neighborhood landing is a 27×27 open plaza with a separate 19×27 furnished existing `STARTER_SHELTER` building and a **four-block service aisle**; other city parcels are still subdivided using existing generation. This replaces the selected outer superblock's normal parcels, not the whole province. Historical downtown starter plaza and Hungry Bug remain generated at the origin even when the player starts elsewhere.
- `Landmarks.regionalLanding`: biome-appropriate harvested block surface; cooking station, crafting, rainwater cauldron, composting, repair wood and one starting cache against the perimeter. Central landing remains clear; only two local guide lanterns, not a floodlit wilderness.
- `DiagonalBoulevards14`: avoids the reserved starter superblock so the after-lot road pass cannot slice its beds, supplies or spawn footprint.
- `SettlementBands47.java`: continuous seeded 704-block neighborhood-level land-use field to differentiate WORKING, DERELICT, and OPEN_COUNTRY inside the **same** biome. Outdoor wilderness lots mostly remain unbuilt, while working settlements have real houses, shops, warehouses, farms/workstations, or factories. The old city lot distribution is untouched.
- `RegionalSurface42.java`: open-country side-street grid uses utilitarian loose ground appropriate to the region, not uninterrupted urban paving; tram lines, canals and major arteries remain preserved. `CityPainter.java`: scattered wilderness avoids the old periodic arterial lamp grid; active settlements retain usable light sources.
- Four existing historical source checks are modernized to accept upper codec version 31 without weakening legacy version-0 compatibility checks. No deferred Ash-Snow, Sludge-Ice poisoning, toxic snowballs, Cross-Eye or villager overhaul were implemented.

## Exact verification done

1. `bash tools/qa-pass47-all.sh` passed, including earlier Pass 41–46 offline source and Java fixtures.
2. New static source checks: 15/15 passed. Existing preset retains `sewer_cave_frequency=30`, enabled underworld and full frontier biome source.
3. Actual Java selector and continuous land-use field compiled under Java 21 with **synthetic climate stubs**. 30,000 seed selections (25,005 noncity selections in the stub climate), and 211,250 neighborhood field samples passed. Each of the six preferred starting profiles appeared about 1/6 of the time. This proves deterministic selection, not suitability of real biome climates.
4. 45,000 synthetic starting-lot geometries tested for nonoverlap, contained shelter footprint and four-wide open service aisle.
5. Archived source ZIPs checked by CRC and full entry comparisons.

## Not verified (important)

- Actual `runData`, `gradlew build`, `runClient`, dedicated `runServer`, TestGame or server FPS/TPS. The source still has a missing `gradle/wrapper/gradle-wrapper.jar` and this environment cannot fetch it; `tools/restore-official-gradle-wrapper.ps1` is included for a verified download on the user's connected Windows machine.
- Real Minecraft chunk painting, spawn terrain height/air clearance, path from starter landing to nearby streets in high mountains, first-night monster spawning, village population behavior, portal compatibility, or client texture quality.
- Whether the actual Minecraft climate sampler provides all five candidate regions on all seeds. The code falls back safely to downtown when it cannot find the requested region.
- Precise visual authenticity of the new-region settlements. Prior November 1 user screenshots remain the in-game baseline but were of an older source build.

## In-game acceptance criteria — do this before calling Pass 47 playable

1. Restore wrapper on a networked machine. Run `./gradlew runData` then `./gradlew build`, fix real Java, registry, data-generation and resource problems, run both client and dedicated server.
2. Start **at least 24 completely NEW worlds** using differing seeds and the Dorohedoro world preset. Record `/seed`, F3 biome, `x y z` spawn, immediately visible infrastructure and collision. The tested worlds must include multiple different starting environments, not all at origin.
3. For a frontier start, F3 must report its matching custom biome. The player must stand on solid ground, have two blocks of air, and reach the actual starter shelter bed and crafting/furnace areas. Inspect at all four chunk borders around the landing.
4. Inspect the adjoining hills/mountains, buildings, river banks and sewer hatches. No floating paths, unreachable doors, exposed underground voids, chunk seams, water leaks or large deleted landmarks. Confirm the four-block shelter aisle is traversable and paths connect to streets across grades.
5. Fly 400–1500 blocks through a given **single** biome. Confirm real city pockets / derelict blocks / open terrain in that biome, with consistent local materials and realistic access roads. Forests must remain dead/stunted and give useful wood; do not add healthy vanilla trees to The Hole.
6. Verify starter resources, no exposed chest spam, dark nights outside settlements with hostile spawning, retaining vanilla mining, sewers and the 30% sewer-to-cave passages. No forced day or loss of Minecraft survival mechanics.
7. Test a version-30 saved world with backups: spawn and generated chunks should retain previous rules, rather than changing terrain retroactively. New version 31 must not convert existing saves to new setting.
8. Compare real screenshots with the user's November 1 canal/market/street/skyline screenshots. **World quality approval is still pending.**

## Suggested next pass

Fix the actual Gradle build, produce a genuine Minecraft playtest of six starting-region types, and then prioritize city-grade paths and region-to-region transition visuals. Do not add cosmetic filler, mobs, deferred village overhaul or new survival hazards merely to inflate patch numbers.
