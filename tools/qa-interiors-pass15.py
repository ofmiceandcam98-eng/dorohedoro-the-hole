#!/usr/bin/env python3
"""Static API-independent integration checks; not a proof of in-game correctness."""
from pathlib import Path
import json
root=Path(__file__).resolve().parent.parent
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda n:(city/n).read_text()
plan=read('InteriorPlan15.java'); layout=read('InteriorLayouts15.java'); settings=read('CitySettings.java'); canvas=read('Canvas.java'); lot=read('LotPainter.java'); tower=read('Tower.java'); preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
    'new world v4+ interiors': preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=4,
    'world version saved in generator': 'Codec.intRange(0, 14).optionalFieldOf("world_detail_version", 0)' in settings,
    'v0 old worlds retain settings': '0, 0);' in settings,
    'version 1/2/3 unaffected': 'worldDetailVersion >= 4' in canvas,
    'only single irregular city method': canvas.count('public boolean hasIrregularCity()')==1,
    'residential layouts wired': 'InteriorLayouts15.residence' in lot,
    'shop layouts wired': 'InteriorLayouts15.shop' in lot,
    'clinic layouts wired': 'InteriorLayouts15.clinic' in lot,
    'factory/warehouse layouts wired': lot.count('InteriorLayouts15.industrial')==2,
    'tower stairs retained': 'flight(c, interior, floorY, style.stairs());' in tower,
    'tower street door retained': 'c.door(doorX, GROUND_Y, box.z1()' in tower,
    'north aisle code': 'corridorWallZ' in layout and 'z0() + 3' in plan,
    'apartment doors actual two-block door': layout.count('c.door(')>=4,
    'two-flat partition': 'splitX' in layout and 'furnishFlat' in layout,
    'dormitory beds': 'Blocks.BROWN_BED' in layout and 'Blocks.GRAY_BED' in layout,
    'repair workshop functional blocks': 'Blocks.STONECUTTER' in layout and 'Blocks.SMITHING_TABLE' in layout,
    'shop storage room separate': 'int serviceZ = in.z1() - 5' in layout and '"shop"' in layout,
    'clinic enclosed ward separate': 'int wardZ = in.z1() - 6' in layout,
    'clinic real healing research fixtures': 'Blocks.BREWING_STAND' in layout,
    'industrial central aisle preserved': 'central' in layout,
    'location-hashed variation': 'CityHash.of(seed' in plan,
    'no global random': 'Random' not in plan and 'Random' not in layout,
    'no magic/documented lore mobs misregistered': 'Kikurage' not in layout,
}
for name,check in checks.items():print(('PASS' if check else 'FAIL'),name)
print(f'PASS15: {sum(checks.values())}/{len(checks)} static checks')
if not all(checks.values()):raise SystemExit(1)
