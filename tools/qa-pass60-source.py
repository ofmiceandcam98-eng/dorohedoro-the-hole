#!/usr/bin/env python3
"""Pass60 source integration assertions; not a NeoForge build or gameplay test."""
import json
from pathlib import Path
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
def read(name):return (city/name).read_text()
s=read('FreightApproaches60.java'); lot=read('LotPainter.java')
settings=read('CitySettings.java'); canvas=read('Canvas.java'); saved=read('WorkingServiceCourts59.java')
config=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'fresh world preset 43': config['world_detail_version']>=43,
 'world detail validation upper 43': 'worldDetailVersion > 52' in settings,
 'world detail codec upper 43': 'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
 'existing saved worlds default version zero': 'optionalFieldOf("world_detail_version", 0)' in settings,
 'Pass59 feature remains gated to version 42': 'worldDetailVersion >= 42' in canvas,
 'new freight option version gated to 43': 'hasFreightApproaches60() { return worldDetailVersion >= 43; }' in canvas,
 'new freight invoked after existing yard completion': lot.index('WorkingServiceCourts59.build(c, lot)') < lot.index('FreightApproaches60.build(c, lot)'),
 'workstation clear after legacy yard furnishings': lot.index('UrbanFabric25.yard(c, lot)') < lot.index('FreightApproaches60.build(c, lot)'),
 'only industrial factory warehouse types': 'lot.type() != LotType.FACTORY && lot.type() != LotType.WAREHOUSE' in s,
 'loading opening exactly three wide': 'x - 1, hall.z1() + 1, x + 1, yard.z1()' in s,
 'loading route reaches public frontage': 'yard.z1()' in s and 'route.z1()' in s,
 'loading begins outside hall door': 'hall.z1() + 1' in s,
 'interior vestibule clears four high': 'hall.z1() - 1' in s and 'GROUND_Y + 3' in s,
 'layout uses original industrial footprint rules': 'StructuralVolumes14.industrialFootprint(c, lot, WorldDetail.footprint(c, lot))' in s,
 'layout honors east industrial annex': 'IndustrialAnnex58.mainFootprint' in s,
 'large-plot side route tied to prior pass': 'WorkingServiceCourts59.approach' in s and 'LinkedServiceYards56.shack' in s,
 'transverse path requires three deep open setback': 'hall.z1() > yard.z1() - 3' in s,
 'cross path never intersects hall': 'link.intersects(hall)' in s,
 'only parcel-contained writes': 'yard.contains(route.x0(), route.z0())' in s and 'yard.contains(link.x1(), link.z1())' in s,
 'pass60 preserves open sky': 'Blocks.LANTERN' not in s and 'Blocks.TORCH' not in s,
 'no exposed loot created': 'c.chest(' not in s and 'c.barrel(' not in s,
 'existing workshop fixtures unchanged': 'Blocks.CAULDRON' in saved and 'Blocks.SMITHING_TABLE' in saved,
 'existing city street and canal file unchanged': 'CityPainter.java' not in s,
 'existing regional climate and texture file unchanged': 'HoleRegions42' not in s,
 '30 percent sewer to cave still set': config['sewer_cave_frequency']==30,
 'fixture covers actual production class': '"$CITY/FreightApproaches60.java"' in (r/'tools/qa-pass60-freight.sh').read_text(),
 'all JSON resources parse': all(json.loads(p.read_text()) is not None for p in (r/'src').rglob('*.json')),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+' '+name)
if not all(checks.values()):raise SystemExit(1)
print(f'PASS Pass60: {len(checks)} new integration checks')
