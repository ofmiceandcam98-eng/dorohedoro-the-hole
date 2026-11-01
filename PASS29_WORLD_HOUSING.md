# Pass 29 — Procedural housing identity and lived-in architecture

**Status:** Source changes and offline verification only. **NOT** compiled against NeoForge, launched in Minecraft, or visually approved in-game.

## Goal
Continue the seed-driven development of The Hole as a dense, gritty survival city. Small homes should read as actual small homes, not miniature versions of an apartment tower. Retain regular lanterns, torches, and the existing Cavern Lamp; no replacement of functional lighting. No new static city map, imported prefabs, NPC profession logic, village/faction overhaul, or deferred environmental features.

## Integrated in `world_detail_version: 17`

- New pure `HousingPlan29`: 5 facade identities, 5 compact roofline profiles, 7 household-use variants and 3/4-block window spacing driven by world/lot hash and coordinates.
- New `HousingDetail29`: caps tiny homes with plain roofs instead of a tall antenna, adds front-window shutters only where the generated wall contains a pane, varied low roof trim/compact chimney, restrained side pilasters, and a low entry mat.
- Adds wall-hugging, non-overwriting domestic furnishings (pantries, shelves, handwork corner, wash areas and reading areas) while preserving the center aisle and north staircase.
- Integrated for tenements and apartments, and additional furnishings in upstairs shop homes. Shop ground-floor workspaces are unchanged.
- Every change is guarded by `Canvas.hasHousing29()` so saved older version profiles continue on their earlier path.
- Updated the `normal` world preset to use version 17; earlier saved-world generator settings are not upgraded.
- Retains Pass 28 Cavern Lamp + regular lantern mix and occasional torches, and retains existing rivers, banks, fish, sewers, sewer dungeon, waterfront entrances, creatures and district worldgen.

## Verified offline

| Check | Result |
|---|---|
| JDK 21 parsing, full `src/main/java` | 143 Java files, 0 parse errors |
| Resource JSON parse, `src/main/resources` + `src/generated/resources` | 397 valid JSON files |
| `tools/qa-pass29-housing.py` | 800 seeds: stable facade/roof/household distributions; same world writes whole-area versus 16x16 chunks; doors and room center protected |
| `tools/qa-pass28-mixed-lighting.py` | Java stub compile and functional smoke test: passed |
| `tools/qa-pass25-density.py` | Density and lot-boundary regression: passed |
| `tools/qa-urban-fabric-pass25.py` | Street and yard deterministic splitting: passed |
| `tools/qa-pass24-canal-ecology.py` | 114/114 static checks passed |
| `tools/qa-pass22-dungeon-ecology.py` | 117/117 static checks passed |
| `tools/qa-pass23-waterway-entrances.py` | 26/26 static checks passed |

`tools/qa-sewers-static.py` still reports 48/51 due to three old assumptions that do not reflect later source versions: improved cavern geometry expectation, original 16x16 texture expectation, and an older extended cavern-version expectation. These are **not** claimed fixed or tested here.

## Known limitations

- Geometry and furnishing correctness in Minecraft remains **unverified**. Standalone Java stubs do not validate all NeoForge mappings, JSON schemas, block placement/support or game runtime.
- The roofline treatment is secondary decoration above the existing walkable Tower roof; it does not yet introduce separate structural gable roof volumes with attic rooms.
- Some shutter pairs are deliberately omitted near chunk borders to avoid reading an ungenerated neighboring chunk. This prevents nondeterministic seams at the expense of an occasional missing shutter.
- Existing save profiles remain fixed by their `world_detail_version`; **new features need a newly created world** using the updated preset.
- Full quality acceptance requires `/gradlew runData`, `/gradlew build`, dedicated-server worldgen and client screenshots of compact houses, apartments, upper-storey shops, lamps, roofs and chunk seams. Keep old world backups.
