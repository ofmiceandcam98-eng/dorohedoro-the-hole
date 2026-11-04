#!/usr/bin/env python3
"""Offline integration contract checks; not a NeoForge runtime build."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
source=lambda name:(city/name).read_text()
chooser=source('LotChooser.java')
policy=source('IndustrialBelts57.java')
settings=source('CitySettings.java')
canvas=source('Canvas.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
opts=preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'fresh v41 selected with v40 industrial policy retained': opts['world_detail_version']>=41,
 'codec accepts v40':'Codec.intRange(0, 52)' in settings,
 'constructor accepts v40':'worldDetailVersion > 52' in settings,
 'old save detail default remains zero':'optionalFieldOf("world_detail_version", 0)' in settings,
 'new industrial policy version-gated':'plan.settings.worldDetailVersion() >= 40 && district == District.INDUSTRIAL' in chooser,
 'nonindustrial districts untouched':'district == District.INDUSTRIAL' in chooser,
 'open country untouched':'fabric != UrbanPattern52.Fabric.OPEN_COUNTRY' in chooser,
 'new policy returns concrete lots':'return IndustrialBelts57.choose(plan.seed, rect, fabric, roll);' in chooser,
 'climate choice remains independent':'plan.regionAt' not in policy and 'HoleRegions42' not in policy,
 'only stable seed neighborhood used':'CityArchitecture.neighborhood(seed, x, z)' in policy,
 'small plant becomes workable shop':'site.width() >= 20 && site.depth() >= 20 ? LotType.FACTORY : LotType.SHOP' in policy,
 'small warehouse becomes shop':'site.width() >= 13 && site.depth() >= 13 ? LotType.WAREHOUSE : LotType.SHOP' in policy,
 'four distinct industrial subdistricts':'enum Belt { MANUFACTURING, FREIGHT, RECLAMATION, WORKER_SERVICES }' in policy,
 'existing inhabited region selector preserved':'return UrbanPattern52.choose(plan.regionAt(' in chooser,
 'existing residential annex preserved':'worldDetailVersion >= 38' in canvas,
 'existing 3-wide yards preserved':'worldDetailVersion >= 39' in canvas,
 'existing 30% sewer caves preserved':opts['sewer_cave_frequency']==30,
 'Minecraft JSON resources parse':all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for key,ok in checks.items():print(('PASS' if ok else 'FAIL'),key)
print(f'Pass57 source checks: {sum(checks.values())}/{len(checks)}')
assert all(checks.values())
