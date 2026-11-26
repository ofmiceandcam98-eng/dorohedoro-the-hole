#!/usr/bin/env python3
"""Pass47 source-integration sanity. Pure static contracts, not NeoForge runtime."""
from pathlib import Path
import json, re
root=Path(__file__).resolve().parents[1]
code=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda n: (code/n).read_text()
city=read('CityPlan.java')
settings=read('CitySettings.java')
plans=read('Superblock.java')
land=read('Landmarks.java')
chooser=read('LotChooser.java')
regions=read('StartingRegions47.java')
config=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']
checks={
 'fresh world opts in, all frontier biome IDs enabled':config['city']['world_detail_version']>=31 and config['biome_source']['frontier_biomes'],
 'historical world default stays zero':'optionalFieldOf("world_detail_version", 0)' in settings and 'worldDetailVersion() < 31' in city,
 'full range versioned 0..33':'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings and 'worldDetailVersion > 52' in settings,
 'seed determines site, no runtime random': 'StartingRegions47.choose(seed' in city and 'Math.random' not in regions and 'new Random' not in regions,
 'climate confirms region including four corners':city.count('regionAt(')>=5 and 'regionAt(x + 38, z + 24)' in city,
 'superblock uses shared geometry':plans.count('areaFor(plan, i, j)')==1 and 'Superblock.areaFor(this,' in city,
 'starter lot fully reserved': 'buildRegionalStartingQuarter' in plans and 'LotType.PLAZA' in plans and 'LotType.STARTER_SHELTER' in plans,
 'regional plaza accessible with supplies': 'regionalLanding' in land and 'starter_cache' in land and 'Blocks.CRAFTING_TABLE' in land and 'Blocks.CAULDRON' in land,
 'walkable, height-graded world spawn': 'CityTerrain40.frontageOffset(this, landing)' in city and 'return new BlockPos(x, GROUND_Y + elevation, z)' in city,
 'origin plaza retained for old saves':'new BlockPos(0, GROUND_Y, 0)' in city and 'i == -1 && j == -1' in plans,
 'diagonals do not erase regional starting lot':'plan.isRegionalStartingBlock(i, j)' in read('DiagonalBoulevards14.java'),
 'not uniformly urban in fringe':'SettlementBands47.choose' in chooser and 'Form.OPEN_COUNTRY' in read('RegionalSurface42.java'),
 'remote street lights scarce':'placeLamp = false' in read('CityPainter.java'),
 'existing sewer and night mechanics preserved':config['city']['sewer_cave_frequency']==30 and config['city']['sewer_underworld'] and 'paintSewers(canvas' in read('CityPainter.java'),
 'all JSON parse':all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL'), name)
print(f'{sum(checks.values())}/{len(checks)} Pass47 source checks; not a Minecraft build')
assert all(checks.values())
