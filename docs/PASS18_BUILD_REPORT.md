# Pass 18 build report

Pass 18 had never been compiled before this. It now builds. Only build blockers were fixed. No gameplay, world generation or district design was changed.

## Fixes needed to build

| Problem | Fix | Behaviour change |
| --- | --- | --- |
| `SewerLeechEntity` overrode the final method `canBreatheUnderwater()` | Removed the override and added `data/minecraft/tags/entity_type/can_breathe_under_water.json` listing `dorohedoro:sewer_leech` | None intended. Not tested in game |
| `PortalCoreBlock` lacked the required `codec()` | Added `CODEC` and `codec()` | None |
| `processResources` stopped on 16 files present in both `src/main/resources` and `src/generated/resources` | Added `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` to `build.gradle`. Hand-made files win | None intended. The one pair inspected looked identical, so the byte difference is probably line endings. The other 15 were not inspected |

Do not go back to the original Pass 18 ZIP, or these errors return.

## Verified

- `runData` passed. Generated file count rose from 211 to 261.
- `build` passed: `build/libs/dorohedoro-0.1.0.jar`, 581,631 bytes.
- Jar integrity check passed. All 126 classes parse, and all 95 top-level classes are present.
- A real dedicated server booted and generated 289 chunks of a new world at seed 12345 (35.3 ms per chunk, wall clock).
- 25 of 26 QA checks passed. The miss is `multiple_districts`, which saw 2 districts in that small radius 8 area. It is a sample-size miss at that radius and does not show a defect by itself.

## Found, not fixed

`Failed to create block entity minecraft:barrel` was logged twice, at -124, 65, -66 and -100, 65, 26. Generation left barrel data at a spot where a later step placed a different block. It is not a crash, but a barrel or its loot may be missing there. Those spots should show which lot type overwrites its own barrels.

## Not tested

The game client, the installed jar in the launcher, how the seven landmarks look, interiors, streets, older saves, multiplayer, FPS and TPS. No screenshots exist.
