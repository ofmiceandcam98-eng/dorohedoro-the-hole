#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parent.parent
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda name:(city/name).read_text()
s=read('LotPainter.java');a=read('IndustrialAnnex58.java');settings=read('CitySettings.java');canvas=read('Canvas.java')
preset=(root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text()
checks={
 'new detail version gated': 'worldDetailVersion >= 41' in canvas,
 'older save default remains 0': 'optionalFieldOf("world_detail_version", 0)' in settings,
 'settings maximum is 41': 'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52).optionalFieldOf("world_detail_version"' in settings,
 'sewer cave frequency range unchanged': 'Codec.intRange(0, 40).optionalFieldOf("sewer_cave_frequency"' in settings,
 'new preset v41':'"world_detail_version": 52' in preset,
 'industrial hall footprint split':s.count('IndustrialAnnex58.mainFootprint(c, lot, industrialBase)')==2,
 'annex built for warehouse and factory':s.count('IndustrialAnnex58.build(c, lot, industrialBase, box')==2,
 'main hall retains west-side workshop':s.count('StructuralVolumes14.serviceOutbuilding(c, lot, box)')==2,
 'main road-facing loading entrance maintained': 'int doorX = box.centerX()' in s,
 'two functional rooms with partition door':'c.door(partitionX' in a,
 'internal entrance into hall':'c.door(main.x1()' in a,
 'interior path cleared to hall aisle':'main.centerX()' in a,
 'no annex reward chests':'.chest(' not in a,
 'no annex lighting':'LANTERN' not in a and 'TORCH' not in a,
 'roof has usable ladder':'c.ladder(' in a,
 'no parcel-external writes guarded':'lot.rect().contains(bay.x1(), bay.z1())' in a,
 'all climates kept; no climate dispatch': 'HoleRegions' not in a,
 'source geometric test exists':(root/'tools/qa-pass58-annex.sh').exists(),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+' '+name)
if not all(checks.values()):raise SystemExit(1)
print(f'PASS Pass58: {len(checks)}/{len(checks)} source checks')
