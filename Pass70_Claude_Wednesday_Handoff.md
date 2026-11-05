# Claude handoff — Pass 70, November 4, 2026

**Start with `Dorohedoro_Pass70_CONSOLIDATED_Source_UNCOMPILED.zip`.** This replaces Pass69 as latest source. Do **not** apply the changes-only ZIP on top of Pass70 full ZIP; the changes-only ZIP is for upgrading an independent Pass69 source tree.

## Immediately required
- Fix official Gradle wrapper JAR; Java 21; Gradle 9.2.1; NeoForge 21.1.265.
- Run compilation/resource processing, then actually launch a new Minecraft 1.21.1 test world. All existing offline fixture successes are **NOT** NeoForge build proof.
- Read `docs/PASS70_CONNECTED_STORM_DRAINS_CAVE_TRANSITIONS.md` for precise details, version gating, test evidence, and acceptance criteria.
- Run `bash tools/qa-pass70-sewer-architecture.sh`, `python3 tools/qa-pass70-source.py`. Historical suite via `bash tools/qa-pass70-all.sh` may exceed one execution limit; run earlier subchains independently if necessary.

## Implemented Pass70
- Two-entrance parallel stormwater bypasses with continuous dry walking routes and a recessed channel, built only when no existing maintenance/pump sites or junctions conflict.
- Gradual rugged transition through selected deep sewer cave entrances; original three-wide staircase untouched. **Connection to pre-existing natural Minecraft noise caves remains unproven.**
- Three architecturally varied wall profiles on upper sewer shoulders; zero extra lamps, loot, spawners, toxic status, or magic locks.
- Fresh world-detail version 52; older saves remain at their saved versions.

## High-priority Minecraft acceptance tests
1. Confirm the main sewer trunk is accessible and the two bypass entrances are open from both ends; no random fluids block dry walks.
2. Walk the newly roughened cave descent without techniques, verify terrain and ore access, and find both carved destination and independent natural Minecraft caves.
3. Check valves and operator booths of Pass69 for actual piston redstone+water updates after chunk reload.
4. Check actual structures: ancient cities/sculk/Warden, strongholds with active End portal frames, mineshafts, trial chambers, vanilla temples, ocean monuments/ruins/shipwrecks, Nether, End and Ender Dragon without techniques.
5. Capture *real screenshots*, including construction density, roof occlusion, purposefully furnished interiors and sewer darkness.

## Known limitations
- Missing `gradle/wrapper/gradle-wrapper.jar`; source archive deliberately labeled **uncompiled**.
- The full regression runner timed out after passing through Pass54, but remaining Pass53-to-Pass41 suite passed separately.
- In-game neighbor-structure collisions, actual natural-cave intersections, ocean structure placement, performance and visuals still require testing.
