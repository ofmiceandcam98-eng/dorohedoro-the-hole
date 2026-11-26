# Sewer-to-Cavern Traversal & Size Variation — 2026-10-31

**Status: source-integrated and offline-regression tested; NOT NeoForge-build or in-game verified.**

## User requirements preserved

The Hole remains a sewer-first environment. Thirty percent of junctions are *selected* to lead through a **long constructed-to-ruined-to-mined tunnel** to a cavern. Approximately 18% lead to closed inspection facilities; approximately 52% remain ordinary sewers. Vanilla deep mining, normal caves, ore distribution, aquifers, tools and survival gameplay are retained. No quest gates any underground access.

## This iteration

- Introduced pure deterministic `SewerCavernProfile` defining a cavern's footprint at each seeded entrance.
- Approximately 1/6 of cavern destinations become broader rooms (radius X 20–24 blocks, radius Z 21–25). Others remain radius X 14–17, radius Z 14–18. These are **carved destinations**, not a guarantee that the chamber physically connects to a pre-existing naturally generated Minecraft cave tunnel.
- Fixed the original exit landing: the last 17 blocks of the artificial approach now gradually descend to the excavated cavern floor. Rather than using a static staircase that can turn into a two-block drop across city relief steps, the stair profile interpolates physical Y coordinates. Neighboring landings descend by **no more than one block** in the standalone plan test.
- Added `city.sewer_cavern_version` to the *saved* chunk-generator codec. **New worlds use `1`. Existing saved worlds that omit this field default to `0` and retain the exact old geometry** (including worlds created with the earlier 30% frequency). Do not manually change an existing world's stored generator settings. Use a new test world to see these changes.
- Added dedicated-server QA assertion `sewer_cavern_walkable_descent` that inspects block support, clearance and adjacent grade for the guaranteed sample branch when `sewer_cavern_version=1`.
- Synchronized the outdated sewer static check to the existing 30% frequency, and added regression checks for the new version field.

## Offline verification performed

- Existing `VerifySewerBranchFrequency` across 146,410 seed/intersection decisions: 29.89% cave-leading, 18.06% inspection, 52.06% ordinary. Legacy branch selector unchanged.
- `VerifySewerCavernProfile`: 146,410 cavern plans, 16.46% broad chambers, profile dimensions within stated bounds, deterministic.
- `VerifySewerCavernProfile`: **1,768,680** individual entrance-floor checks (both directions, with and without elevated terrain): no more than one block difference between neighboring entry landings and exact final center-floor height.
- `python tools/qa-sewers-static.py`: 51/51 checks passed.
- Java 21 compiler parser: 48 active sources, 0 Java **syntax** errors (does not compile against Minecraft/NeoForge).

## Required runtime verification on the Windows development PC

1. Back up old saves and make a fresh world with `sewer_cavern_version: 1` in the world preset.
2. Run one Gradle command at a time (never two in parallel): `./gradlew --no-daemon runData`, then `./gradlew --no-daemon build` and `./gradlew --no-daemon runClient`.
3. From spawn, descend through a city access shaft into the lower gallery. Follow the starter cave branch near the `(0,0)` arterial crossing (branch X = arterial center X + 12). Confirm the **long** stages: sewer masonry, damaged masonry, mining stone, cavern mouth.
4. Walk *both ways* through the new descending corridor without jumping or falling more than one block at a time. Check head clearance and surfaces in the actual game.
5. Visit several ordinary sewer-only sites and inspection rooms: they should NOT suddenly be open caves.
6. Discover several broad caverns and confirm geometry, drainage, lighting, no unbounded water/lava floods, no instant chasms, and walkable connections.
7. Mine deep underground off the sewer routes as in vanilla. Verify normal ore types, mineshafts, caves, aquifers and relevant progression.
8. Re-run dedicated-server `qa-world.sh` with reverse chunk orders across multiple seeds; inspect `sewer_cavern_walkable_descent` in the report and benchmark generation.
9. Run save/restart, player death/respawn, and multiplayer tests; do not represent these as passed without evidence.

**Important:** The room at the end of the branch is a deliberately carved cavern. The mod still enables vanilla deep cave noise, but no runtime trace has yet demonstrated that every such cavern joins another *naturally generated* cave passage. If that strict guarantee becomes necessary, add a targeted connectivity search/test rather than claiming it now.
