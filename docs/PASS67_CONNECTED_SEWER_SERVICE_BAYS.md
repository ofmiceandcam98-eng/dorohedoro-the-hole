# Pass 67 — Connected municipal sewer repair and filtration bays

**Status:** actual NeoForge production source changes and compiled offline Java fixtures, **not compiled against Minecraft 1.21.1 / NeoForge 21.1.265; not visually or mechanically tested in-game**. Source-only checkpoint; the wrapper/build handoff remains scheduled for Wednesday, Nov 4, 2026.

## Problem addressed

The upper municipal sewers had repetitive seven-wide trunk passages, rare deep dungeons and infested inspections, but relatively little useful mid-scale infrastructure. Add side rooms that make exploring a sewer useful without needing techniques, keeping the main paths continuous and dark enough to remain dangerous.

## Implementation

- Added `SewerServiceBays67.java`, drawn immediately after `SewerDetails24.paint`, before existing maintenance galleries, cave branches, cisterns and four-room dungeons.
- Two deterministic 9-block-wide, roofed masonry service bays: **repair** (crafting table, furnace, grindstone) and **filtration** (cauldron, composter, stonecutter). Each has a three-wide, three-high walk-in passage through the sewer's side wall and a four-high workroom. Existing sewer center drains and walking lanes are untouched.
- Sites chosen by a seed hash of **axis, arterial index, 64-block absolute section**, including negative positions. Each section yields no room or one of the two types. The lateral room side is seeded as well; placements vary along a trunk and avoid a purely periodic look.
- No new street or underground lights, loot chests, barrels, spawners, potion hazards, technique-locked mechanics or permanently sealed doors. Rooms are useful but not shelter guarantees. The existing Pass61 optional infested inspection rooms still supply localized enemy pressure.
- Canal trunks excluded. Sites within 39 blocks of crossing arterials are excluded to preserve vertical shafts, larger cisterns, Pass22 dungeon rooms, and selected cave exit branches. **IMPORTANT:** exclusion lookup sees the full exclusion distance from *each chunk*, not just nearby trunk lines; without this, crossings could be excluded in one chunk but painted in its neighbor. A dedicated regression verifies this case.
- No bays under new ocean water or reserved dry coastal shelves. All room writes use `Canvas.set`/`Canvas.fill`, inheriting Pass63 Deep Dark and Pass64 vanilla bounding-box protections.
- Fresh world preset `world_detail_version: 49`; old worlds with version 0–48 retain their original service-wall generation. Missing version defaults to zero. Validated version max and codec both raised from 48 to 49.

## Offline verification

- `bash tools/qa-pass67-service-bays.sh`: compile **production** `SewerServiceBays67`, `CityHash`, `Line`, and `Rect` against small vanilla-class stubs. Check 221,155 assertions including both bay types, negative sections, correct floor and three-high doorway, unobstructed trunk lanes, no new lamps or loot/spawners, old-world and sewer-disabled paths, mocked ocean, deterministic 16x16 chunk stitching, and **44 sampled sites skipped consistently around nearby intersections**.
- `python3 tools/qa-pass67-source.py`: 31/31 source/JSON integration checks covering painter order, version gates, coast and deep-vanilla protections, sewer cave ratio and preserved Nether/End dimensions.
- `bash tools/qa-pass67-all.sh`: runs both above and the historical Pass66→Pass41 offline test chain. Historical test strings for the shared maximum version were bumped only to accommodate the new valid version. Historical behavior is still gated.

## In-game acceptance required before calling it finished

1. Build with Java 21, Gradle 9.2.1 wrapper, NeoForge 21.1.265; correct all genuine Java/API errors. Then create **fresh world version 49** and test at least 10 seeds, including negative coords and cold/dry regions.
2. Walk from a street manhole into a repair bay and a filter bay in survival. Verify physical doorway, use each workstation, check no blocked sewer route, broken geometry, bright light spam or exposed loot containers.
3. Check both sides of any room crossing a 16x16 chunk edge; compare repeated generation of the same seed and coordinates.
4. Compare a Pass66 version-48 saved world: previously generated/unexplored chunks should keep the old wall plan. Make backups before opening old saves.
5. Confirm ordinary zombies/mobs can spawn in dark trunk sections while a well-prepared non-magic player can enter, fight or evade, and escape. Evaluate balance with iron armor, iron tools, torches, food and no techniques.
6. `/locate structure minecraft:ancient_city`, `minecraft:stronghold`, `minecraft:trial_chambers`, and `minecraft:mineshaft`, inspect their real generation and functional contents. Do not claim that a successful `/summon` Warden proves Ancient Cities are intact.
7. Inspect cave branches for real natural cave connections—30% cave-leading branch selection is not proof the endpoint reaches a vanilla cave—and confirm world can still be beaten with no techniques.

## Known limitations

- Rooms are small practical service alcoves, **not** yet the larger interactive pumping stations and floodgates planned for later passes.
- Pass64 bounding-box protections can clip a room at a protected vanilla structure boundary; this avoids overwriting vanilla but may produce an incomplete bay. Requires runtime survey, possibly a future earlier structure collision rejection.
- No claim yet of actual natural ocean monuments, shipwrecks, complete vanilla progression, profitable station usage, acceptable client performance, or authentic in-game visuals.
- Java fixtures exercise production planner and paint logic with test stubs, not runtime Minecraft block state/terrain/lighting/entity code.
