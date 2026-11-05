#!/usr/bin/env python3
"""Fresh-world vegetation policy static checks. Not proof of Minecraft world rendering."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
r=lambda n:(city/n).read_text()
settings=r('CitySettings.java'); trees=r('HoleTrees.java'); policy=r('HoleTreePolicy.java')
openlots=r('OpenLots.java'); canvas=r('Canvas.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
 'fresh world retains vegetation profile >=5':preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=5,
 'older world settings default to version 0':'optionalFieldOf("world_detail_version", 0)' in settings,
 'older generation uses original vegetation': 'if (c.hasSparseHoleTrees())' in openlots and 'else {' in openlots,
 'nursery remains survival sapling source': 'Blocks.OAK_SAPLING' in openlots and 'Blocks.BIRCH_SAPLING' in openlots and '"nursery"' in openlots,
 'protected nursery canopy': 'int roofY = GROUND_Y + 4' in openlots and 'Blocks.GLASS' in openlots,
 'outdoor nursery not planted with full living trees': 'if (c.hasSparseHoleTrees())' in openlots and 'tree(c, in.x0() + 2' in openlots,
 'new parks have damaged ground':'Mix.of(Blocks.COARSE_DIRT, 6' in openlots,
 'no outdoor flowers for new parks':'if (c.hasSparseHoleTrees())' in openlots and 'Blocks.DEAD_BUSH' in openlots,
 'only one park tree attempted':'HoleTrees.park(c, lot)' in openlots and 'static void park(Canvas c, Lot lot)' in trees,
 'outdoor crowns ragged':'if (CityHash.range(part, 5) >= 2) continue;' in trees,
 'dead tree yields harvestable wood':'Blocks.STRIPPED_OAK_LOG' in trees,
 'living tree drops vanilla oak/spruce wood and saplings':'Blocks.OAK_LEAVES' in trees and 'Blocks.SPRUCE_LEAVES' in trees,
 'living foliage does not overwrite trunk':'!(dx == 0 && dz == 0 && dy == 0)' in trees,
 'tree uses deterministic lot seed':'CityHash.mix(seed ^ CityHash.mix(lotHash + SALT))' in policy,
 'placements protected inside lot':'if (!lot.contains(x - 2, z - 2)' in trees,
 'low frequency in crowded industrial vacant lots':'case INDUSTRIAL -> 3;' in policy,
 'underground remains unchanged':'HoleTrees.' not in r('CityPainter.java') and 'paintSewers(canvas' in r('CityPainter.java'),
}
for k,v in checks.items():print(('PASS' if v else 'FAIL'),k)
print(f'HOLE TREES: {sum(checks.values())}/{len(checks)} static checks passed')
if not all(checks.values()):raise SystemExit(1)
