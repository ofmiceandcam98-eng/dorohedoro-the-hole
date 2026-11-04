#!/usr/bin/env python3
"""Integration assertions for the Pass59 source, not a Minecraft runtime test."""
import json
from pathlib import Path
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
def read(n): return (city/n).read_text()
s=read('WorkingServiceCourts59.java')
lot=read('LotPainter.java')
canvas=read('Canvas.java')
struct=read('StructuralVolumes14.java')
settings=read('CitySettings.java')
pre=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
config=pre['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'fresh v42 selected':config['world_detail_version']>=42,
 'v42 settings validation':'worldDetailVersion > 52' in settings,
 'codec accepts v42':'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
 'old detail default zero':'optionalFieldOf("world_detail_version", 0)' in settings,
 'new feature has v42 gate':'public boolean hasWorkingServiceCourts59() { return worldDetailVersion >= 42; }' in canvas,
 'industrial court after yard detailing':lot.index('UrbanFabric25.yard(c, lot)') < lot.index('WorkingServiceCourts59.build(c, lot)'),
 'courts only in industrial lots':'lot.type() != LotType.FACTORY && lot.type() != LotType.WAREHOUSE' in s,
 'no new isolated building outside lot':'StructuralVolumes14.industrialFootprint' in s and 'LinkedServiceYards56.shack' in s,
 'three-wide clear route':'center - 1' in s and 'center + 1' in s and 'route.width()' not in s,
 'route reaches frontage':'center + 1, yard.z1()' in s,
 'hard minimum site size':'lot.rect().width() < 36' in s and 'lot.rect().depth() < 26' in s,
 'wall-sided repairs':'workX = main.x0() - 2' in s,
 'work surface separated from lane':'workX > route.x1() + 1' in s,
 'usable cauldron and smithing or stonecutting':'Blocks.CAULDRON' in s and 'Blocks.SMITHING_TABLE' in s and 'Blocks.STONECUTTER' in s,
 'courts have no lanterns or exposed loot':all(x not in s for x in ('Blocks.LANTERN','Blocks.TORCH','c.chest(','c.barrel(')),
 'main entrance unchanged':'int doorX = box.centerX()' in lot,
 'street tram and canal geometry untouched':'CityPainter.java' not in s,
 'old west-side passage unchanged':'c.hasLinkedServiceYards56()' in struct,
 'sewer cave ratio still 30 percent':config['sewer_cave_frequency']==30,
 'json resources parse':all(json.loads(p.read_text()) is not None for p in (r/'src').rglob('*.json')),
 'java fixture present':(r/'tools/qa-pass59-courts.sh').exists(),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+' '+name)
if not all(checks.values()):raise SystemExit(1)
print(f'PASS Pass59: {len(checks)} source checks')
