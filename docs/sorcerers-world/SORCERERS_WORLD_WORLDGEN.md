# Sorcerers' World: settlement generation

## Pipeline

1. **Placement**: `data/dorohedoro/worldgen/structure_set/sorcerer_settlements.json`. Random spread, spacing 30
   chunks, separation 18, salt 1170431891. Adjacent attempts are at least 19 chunks (about 300 blocks) apart, so even
   two of the largest settlements cannot touch.
2. **Biomes and dimension guard**:
   - `#dorohedoro:has_structure/sorcerer_settlement` lists vanilla land biomes (plains, sunflower plains, meadow,
     savanna, the forests, taiga, cherry grove, snowy plains) plus the old `sw_*` land biomes for saved worlds.
   - `findGenerationPoint` also refuses any biome source other than `SorcererVanillaBiomeSource` or
     `SorcererBiomeSource`, so a vanilla overworld, the Hole and Hell can never get a settlement.
   - QA checks both: `sw_settlement_not_in_other_dimensions`, `sw_no_hole_blocks` and `sw_no_hole_or_hell_biomes`.
3. **Site check**: `SorcererTownStructure.findGenerationPoint`. The centre must be dry land above sea level, with less
   than 12 blocks of relief within 20 blocks of it.
4. **Kind**: from the biome at the centre:
   - Grassland (plains, meadow, savanna): city 40%, town 40%, hamlet 20%.
   - Woods: town 35%, hamlet 65%.
   - Snowy plains: hamlet only.
   - Old worlds: `sw_city` gives a city, `sw_city_edge` a town.
5. **Terrain sampling**: `SorcererTownStructure.Terrain`. Natural height and water come straight from the noise
   generator (`getBaseColumn`), on a 4-block grid with bilinear interpolation, cached for the whole plan. This is
   independent of chunk load order.
6. **Plan**: `TownPlanner.planViable`. It plans the requested kind, and steps down to a smaller kind when the site is too
   rough or wet for it (minimum plots: city 14, town 7, hamlet 3). When not even a hamlet fits, the site stays
   countryside.
7. **Step**: `top_layer_modification`, after vanilla trees and ore blobs. Roads, walks and sites remove logs and leaves
   left hanging over them, and nothing vanilla can overwrite the paving afterwards.
8. **Pieces**: one `TownRoadsPiece` (the roads, plaza, lamps and centrepiece for the whole settlement), then one
   `TownBuildingPiece` per plot. Terrain adaptation is `none`, because the pieces do their own grading.

## The planner (`TownPlanner`, pure Java)

- Settlement radius: city 80 to 92, town 56 to 68, hamlet 32 to 42 blocks.
- **Roads grow from the plaza in 6-block steps.** At each step the planner scores five headings (-0.4 to +0.4 rad) by
  climb, turn, drift from the intended heading, water and a little jitter, and takes the cheapest. Grades are capped at
  2 blocks per step (1 for a road's first step), so neighbouring columns differ by at most one block. Points within the
  plaza's apron stay at plaza height.
- Cities get four thoroughfares, towns two or three, hamlets two. Streets branch from thoroughfares every 13 to 26
  blocks, sometimes on both sides; alleys branch from streets.
- A road that comes within clearance of another either **joins** it (when the heights agree within the grade limit) or
  **stops as a dead end**. Clearance is tested at both ends of the step and at two points in between.
- **Plots**: two passes along both sides of every road (the second fills gaps with smaller buildings). Types are chosen by
  ring and kind weights, with a penalty on repeating the previous one or two types and a boost for each missing basic
  service. Setbacks are tried from small to large. A plot is accepted only if:
  - its footprint is clear;
  - its one-block ring touches only free ground, verge or another building's ring;
  - the terrain rise is within the type's limit;
  - a walk of at most 8 cells reaches the road;
  - |floor - road| is at most the walk length.
- **Occupancy raster**: road, verge, lamp, building, near (ring), walk. Lamps are placed before plots so no walk is
  blocked by a lamp.

## Painting

- **Roads** (`TownRoadsPiece.column`): `RoadNet.sample` decides each column. The higher road class wins where ribbons
  overlap, so a branch always meets its parent at the parent's height.
  - Below the paving: fill to natural ground. Over water, or more than 6 blocks of drop, the road becomes a bridge with
    pillars every 4 blocks and railings.
  - Above the paving: cut back for headroom, up to 14 blocks.
  - Half-step slabs where a neighbouring road column is one higher.
  - Verge: a retaining wall where the road cuts more than one block into a slope; an earth shoulder where it runs above
    the ground.
  - Ground heights come from the `*_WG` heightmaps, which hold the natural terrain.
- **Buildings** (`TownBuildingPiece.paint`): the walk first, then the building.
  - The walk gets stairs at every rise. Descending walks reach road height at their last cell; ascending walks start
    level with the door.
  - Buildings: `site()` lays foundations to natural ground and clears the volume, then `shell()` and the per-type
    painter run.
  - Every decision comes from a `TownRandom` re-created from the plot seed, so a building split across chunks paints
    identically from every side. The chunk sink drops writes outside the current chunk.
- **Block entities**: chests and barrels get loot tables. Signs get waxed text through NBT load (no level access needed).
  Fences, walls, panes and stairs are marked for post-processing so their shapes connect.
- **Villagers**: spawned with `MobSpawnType.STRUCTURE`, persistent, with a profession that matches the workstation
  painted next to them.

## Determinism

- The plan depends only on the seed, the chunk position and noise terrain.
- Painting depends only on the stored plan and the plot seed, plus natural heights from the WG heightmaps.
- QA hashes every non-vegetation block in each settlement chunk (`chunk_hashes`), so forward and reverse runs can be
  compared (`tools/qa-sorcerers.sh`).

## Guards (mixins)

| Mixin | What it does |
|---|---|
| `SorcererWeatherMixin` | Rain and thunder level 0 in the Sorcerers' World (canon: no ordinary rain) |
| `SorcererStructureFilterMixin` | `#dorohedoro:not_in_sorcerers_world` (vanilla villages, pillager outposts) never starts where the biome source is `SorcererVanillaBiomeSource` |
| `SettlementFeatureGuardMixin` | Lakes (including surface lava lakes) and boulders skip any chunk that references a settlement |

`SettlementTidy` removes tree blocks over streets, walks and buildings once a new chunk is final.

## Save compatibility

- New registry entries only: structure type `dorohedoro:sorcerer_settlement`, piece types `dorohedoro:sw_town_roads` and
  `dorohedoro:sw_town_building`, and the structure, structure set and biome tag.
- Existing Sorcerers' World chunks are not regenerated. Settlements appear only in chunks generated after this update,
  and one that straddles old and new chunks would be cut off at the old chunk border. Test with a **new world**, or with
  a part of an existing world that has never been visited.
- The Hole's `world_detail_version` is untouched.
