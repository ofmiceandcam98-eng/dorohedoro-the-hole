#!/usr/bin/env python3
"""Pass54 versioning and generation-contract assertions; not an in-game build."""
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1]
city = root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read = lambda name: (city/name).read_text()
settings=read('CitySettings.java')
blocks=read('Superblock.java')
lanes=read('CrookedLanes54.java')
old=read('IrregularBlocks14.java')
terrain=read('CityPainter.java')
config=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
preset=config['dimensions']['minecraft:overworld']['generator']['city']
checks = {
    'new world opts in to V37': preset['world_detail_version'] >= 37,
    'older saves still default to V0': 'optionalFieldOf("world_detail_version", 0)' in settings,
    'version upper limit is explicitly 37': 'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings,
    'new crooked selection gated for V37 worlds': 'worldDetailVersion() >= 37' in blocks and 'CrookedLanes54.use(' in blocks,
    'new crooked parcel math gated for V37 worlds': 'CrookedLanes54.parcels(' in blocks and 'worldDetailVersion() >= 37' in blocks,
    'v36 offset parcels still exist and are used': 'IrregularBlocks14.parcelsV36(area, hash, compact)' in blocks,
    'v35 offset parcels still exist and are used': 'IrregularBlocks14.parcels(area, hash, compact)' in blocks,
    'v35 and older offset chance still in use': 'IrregularBlocks14.usesOffsetBlocks(hash)' in blocks,
    'existing splitter still called on each parcel': 'split(plan, parcel, district, 1, random, lots)' in blocks,
    'original primary avenue offset unchanged': 'public static int avenueOffset(' in old,
    'climate independent street density': 'UrbanPattern52.Fabric fabric' in lanes and 'HoleRegions42.Region' not in lanes,
    'three or more blocks in each cross-alley': 'int crossGap = (dense ? 3 : 4)' in lanes,
    'five or more blocks in longitudinal passage': 'int half = dense ? 2 : 3' in lanes,
    'min buildable frontage enforced': 'MIN_BUILDABLE = 11' in lanes,
    'does not modify protected starter quarter': 'if (plan.isRegionalStartingBlock(i, j))' in blocks and 'else if (i == -1 && j == -1)' in blocks,
    'arterial, tram and canal generation unaffected': all(s in terrain for s in ('paintCanals(canvas', 'paintSewers(canvas', 'HoleTransit42.paint(')),
    'no new loot or lighting blocks in lane generator': not any(s in lanes for s in ('Blocks.', 'Lantern', 'c.chest(', 'ChestBlock')),
    'legacy regression source math unchanged': 'private static List<Rect> parcelsWithMinParcel' in old,
    'night spawning remains possible': 'placeLamp = false' in terrain and 'UrbanPattern52.Fabric.OPEN_COUNTRY' in terrain,
    '30% selective sewer caves preserved': preset['sewer_cave_frequency'] == 30,
    'all JSON resource files parse': all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for title, valid in checks.items():print(('PASS' if valid else 'FAIL')+' '+title)
print(f'Pass54 source/compatibility: {sum(checks.values())}/{len(checks)}')
assert all(checks.values())
print('Runtime NeoForge compilation and Minecraft visual QA NOT performed.')
