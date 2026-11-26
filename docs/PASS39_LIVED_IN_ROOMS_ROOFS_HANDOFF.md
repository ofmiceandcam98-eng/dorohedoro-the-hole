# Pass 39 — Inhabited Interiors and Purposeful Rooftops (source only)

## User-approved scope
- Continue locally; do not modify Codespaces, installed Minecraft or CurseForge.
- Every interior must have a reason to exist, be decorated, and use vanilla functionality where appropriate.
- Less exposed chest storage and fewer exterior lights; dark nights must still permit vanilla mob spawning.
- Do not start the deferred villager/faction overhaul.

## Actual source changes
- Added `LivedInRooms39.java`: non-destructive context-specific furniture for residential, shop, clinic and industrial interiors. Uses existing partitions, doors, beds, workstations and footprint; never creates loot containers or lights.
- Added `OccupiedRoofs39.java`: maintenance area, clothesline or rainwater corner on selected large *plain* rooftops, located away from north stair landing. Leaves garden/tank/antenna roofs alone.
- Integrated both into `LotPainter` / `Tower`. Every change is gated to `world_detail_version >= 23` to preserve old-world generation profiles.
- Updated `CitySettings` validation and Codec, `Canvas` version flag, and new-world `normal.json` preset.
- Added `tools/qa-pass39-interiors.py`, a portable JDK-only fixture test that compiles the two helpers with miniature mocked Minecraft classes and checks protected floorplan zones over 1,792 iterations.

## Tests actually completed
- `python tools/qa-pass39-interiors.py`: PASS, 1,792 fixture runs across four seeds and multiple lot dimensions/storeys. Checks preservation of simulated entrance doors, beds, reserved north stair corridor and south main-entry approach; tests roof placements remain inside the footprint.
- Source wiring check: PASS (3 residence call sites, one shop, one clinic, two industrial, one roof; v23 gating validated).
- Did **not** run full Gradle/NeoForge build or actual client simulation; mocked-block tests do not assert actual pathfinding, support rules, room quality or Minecraft physics.

## Acceptance criteria before any release
1. Compile full NeoForge 1.21.1 mod and runData; address all compiler and runtime errors.
2. Create a fresh v23 world and visit at least 8 interiors across residential/shop/clinic/factory, with rooms closed, doors operable, workstations usable, adequate passage and meaningful furnishings.
3. Verify each roof type; especially no floating clotheslines, protected top stair, rooftop crop/tank survival and roof collision.
4. Nighttime hostile mobs spawn on eligible outdoor blocks (with settings enabled); verify street brightness without creating excessive interior darkness.
5. Test river shape, bank continuity, terrain hills, building façades and remaining vanilla-style custom PNG quality against actual client screenshots.
6. Diagnose the previously reported Curse Mark technique crash from `crash-reports/*.txt` or `latest.log`, then retest saving and casting.
7. No villager AI/profession overhaul and no deferred weather/faction changes until the city itself is visually approved.

## Risk / remaining blockers
- Purpose-specific decorative props are extra layering, **not** proof that every generated floorplan is navigable or visually polished. Existing room planning code remains the authority.
- New-world profile v23 is intentional. Existing world-detail values are unchanged.
- Rooftop fence/chain placement needs in-client block-support and visual checks.
- Full city-wide elevation grading and natural watersheds remain separate pending hydrology, structures and pathing validation.
- Technique crash remains unresolved without a client crash report.
