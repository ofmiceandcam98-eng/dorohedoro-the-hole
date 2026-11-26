# Sorcerers' World: test report (2026-11-26)

All results below are from real runs on this machine (Windows 11, Java 21.0.12, NeoForge 21.1.265, Minecraft 1.21.1).
The terrain is the vanilla-terrain Sorcerers' World (new worlds).

## Build

| Command | Result |
|---|---|
| `./gradlew compileJava` | **Pass**, no errors |
| `./gradlew build` | **Pass**: `build/libs/Dorohedoro The Hole-BETA0.4.jar` (0.4.0-beta), SHA-256 `89f8a9c6592e282ce38dd44779c540346d833aeae7f41210daa3d2cc16a39a41` |

## Offline planner fixture: `bash tools/qa-sorcerer-planner.sh 40`

Synthetic terrains: flat, rolling, hillside, river, rough. Three kinds x 40 seeds each, every plan run twice.

- **643,738 checks, 3 failures.**
  - 2 plans (out of 600) have 3 to 9 road cells where neighbouring columns differ by 2 blocks, at an odd junction.
  - 1 rough-terrain city has only 2 of its 5 basic services.
- **Deterministic:** every plan identical on its second run.
- **Clean placement:** no overlaps, no building on a road, every walk reaches a road, every walk is within the stair
  limit, every road keeps its grade.
- **Average size:** about 27 to 30 buildings per city on gentle land and 22 on rough land; about 15 to 18 per town and
  6 to 7 per hamlet.
- **Poor sites:** rough or wet sites step down to a smaller kind, or stay countryside.
- **Plan time:** about 6 ms on synthetic terrain. In game the noise sampling dominates; see ms per chunk below.

## Dedicated-server QA: `bash tools/qa-sorcerers.sh <seed> 4`

Each run is a new world, generated twice (forward and reverse chunk order). The survey covers 36 placement cells
(2,880 x 2,880 blocks).

| Measure | Seed 12345 | Seed 8675309 |
|---|---|---|
| Settlements in survey | 11 (3 cities, 3 towns, 5 hamlets) | 6 (2 cities, 2 towns, 2 hamlets) |
| Land inside settlements | 1.6% | 1.0% |
| Inspected: city | 35 buildings, 63 rooms, 36 villagers (13 with a trade), 57 loot containers, 6 shop signs | 22 buildings, 54 rooms, 29 villagers (11 with a trade), 55 loot containers, 6 signs |
| Vanilla villages or outposts in the realm | 0 | 0 |
| Inspected: town | 11 buildings, 15 rooms, 12 villagers, 15 loot containers | 16 buildings, 38 rooms, 20 villagers, 35 loot containers |
| Inspected: hamlet | 9 buildings, 13 rooms, 7 villagers, 12 loot containers | 6 buildings, 9 rooms, 7 villagers, 11 loot containers |
| Overlaps / buildings on roads / floating corners | 0 / 0 / 0 | 0 / 0 / 0 |
| Doors reaching the road | all | all |
| Rooms reachable from the door, lit, roofed | all | all |
| Settlement only in the Sorcerers' World | pass (0 placements elsewhere) | pass |
| No Hole blocks, no Hole or Hell biomes in Sorcerers' World chunks | pass | pass |
| Determinism (settlement-written blocks per chunk, forward vs reverse) | 370/371 | 395/395 |
| Generation time, settlement chunks | about 60 to 95 ms/chunk | about 60 to 85 ms/chunk |

**Not settlement checks:** `multiple_districts` and `resource_sugar_cane` fail in the Hole part of the same run. These
are Hole checks at the small radius used here (4 chunks). They were not investigated in this pass.

**Road walkability:** every sample passes in the final run. Earlier "failures" were the plaza well or fountain pillar,
which stand at the centre on purpose; QA now skips the centrepiece.

**Remaining determinism mismatch:** one chunk on seed 12345. Earlier mismatches of the same kind were traced to vanilla
features (ore blobs, flower patches, trees, a lava lake) from a neighbouring chunk that was decorated later. The fixes:

- Settlements now generate at `top_layer_modification`, after ores and vegetation.
- Lakes and boulders are kept out of settlement chunks (`SettlementFeatureGuardMixin`).
- Tree remnants are removed once a chunk is final (`SettlementTidy`).

The last chunk was not dumped, so its cause is not confirmed.

## How determinism is measured

The hash covers what the settlement writes:
- every position a building painter sets, replayed with a recording sink;
- the paving band (y-1 to y+3) of every road and verge column.

At those positions:
- open cells (air, plants, flowers, trees, snow) are left out;
- natural rock and ore count as one class, and so does natural soil.

Whole-chunk hashes would measure vanilla's own order dependence. Ore blobs that straddle chunk borders differ with
generation order in vanilla too.

## Defects found and fixed during these runs

1. **The countryside was labelled `sw_waters`** (84% of the realm, from the old biome source). This led to the owner's
   decision to use vanilla terrain.
2. **QA false negatives:** the door search was spending its budget indoors, stairs were treated as jumps, and the room
   search wandered into the streets. All fixed in QA, with walk and stairwell dumps added for diagnosis.
3. **Identity hash codes** made every chunk hash differ between launches. The hash now uses block state ids.
4. **Vanilla trees grew on coarse-dirt walks and over roads.** Fixed: placement after vegetation, no coarse dirt in
   paving, and the tidy pass.
5. **Ore blobs and gravel disks overwrote stone paving.** Fixed by placing after ores.
6. **A surface lava lake from a neighbouring chunk carved into a townhouse.** Fixed by the lake and boulder guard.
7. **Two-block lips** where roads leave the plaza or a sloping parent road. Fixed: plaza points held level, and a road's
   first step rises at most one block.

## Not verified

- **Visual quality** as judged by the owner. A muted QA client run (seed 12345, 0 errors) took overviews and street
  views of a city, a town and a hamlet, plus 7 interiors: `docs/evidence/sorcerers-world/`. It was run before the village
  filter, so the town overview still shows a vanilla village that no longer generates.
- **Multiplayer**, and long play sessions: villager survival at night with vanilla monsters, golem spawning.
- **Old saves:** upgrading a pre-update world. The assumption is that saved worlds keep their stored Sorcerers' World
  generator; test on a backup.
- **Performance** on the target mid-range PC. Only ms per chunk on this machine was measured.
