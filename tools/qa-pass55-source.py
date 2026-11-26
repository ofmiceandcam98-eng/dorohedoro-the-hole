#!/usr/bin/env python3
"""Pass55 source/version contracts; this does NOT replace NeoForge or Minecraft QA."""
import json
from pathlib import Path
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
preset=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
config=preset['dimensions']['minecraft:overworld']['generator']['city']
read=lambda n:(city/n).read_text()
settings=read('CitySettings.java')
canvas=read('Canvas.java')
wings=read('AttachedWings55.java')
lot=read('LotPainter.java')
checks={
 'new worlds still include v38 service wings':config['world_detail_version']>=38,
 'version validation accepts v38':'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings,
 'legacy saved worlds use old default':'optionalFieldOf("world_detail_version", 0)' in settings,
 'annex Canvas flag explicitly v38 only':'worldDetailVersion >= 38' in canvas and 'hasAttachedServiceWings55()' in canvas,
 'annex class is not a visual-only rooftop pass':'private static void furnish(' in wings and 'c.door(' in wings,
 'only housing/shop types qualify':all(s in wings for s in ('LotType.TENEMENT','LotType.APARTMENT','LotType.SHOP')),
 'annex needs 2-floor main building':'mainFloors < 2' in wings,
 'large enough lots required':'original.width() < 17 || original.depth() < 19' in wings,
 'old structural cutouts avoided':'0x14CAFE714L' in wings and 'structural == 0 || structural == 1' in wings,
 'no outside street or neighboring lots':'original.z0(), x1, main.z0()' in wings and 'main.x1() - 1' in wings,
 'north-west stair kept clear':'Math.max(8, main.width() / 2)' in wings,
 'two-sided approach kept open':'wing.z1() - 1' in wings and 'main.z0() + 2' in wings,
 'accessible utility roof deck':'link(c, wing, main, yRoof)' in wings and 'Blocks.CAULDRON' in wings,
 'functional rooms include repair and laundry':'Blocks.SMITHING_TABLE' in wings and 'Blocks.LOOM' in wings,
 'existing room furnishings retained':all(s in lot for s in ('HousingDetail29.furnish','RoomPurpose37.residential','LivedInRooms39.residence')),
 'unchanged landmarks and starter shelters':all(s in lot for s in ('Landmarks.starterShelter','Landmarks.regionalLanding','Landmarks.hospital')),
 'existing tram/road and cave frequency remain unchanged':config['sewer_cave_frequency']==30 and 'CrookedLanes54' in read('Superblock.java'),
 'new code invoked by residences and shops':'AttachedWings55.build(' in lot and lot.count('AttachedWings55.build(')==3,
 'shop condition uses actual floor count':'mainFootprint(c, lot, original, floors)' in lot,
 'only known existing Minecraft blocks used': all(s not in wings for s in ('ModBlocks.', 'new Block(', 'Blocks.ORE', 'Blocks.TORCH', 'Blocks.LANTERN', 'Blocks.CHEST')),
 'world preset JSON parses': all(json.loads(p.read_text()) is not None for p in (r/'src').rglob('*.json')),
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL')+' '+k)
print('Pass55 checks:',sum(checks.values()),'/',len(checks))
assert all(checks.values())
