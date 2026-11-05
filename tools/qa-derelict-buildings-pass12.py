#!/usr/bin/env python3
"""Static invariants for ruined buildings: complements, not replaces, Minecraft worldgen tests."""
from pathlib import Path
import sys, json
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
r=(city/'DerelictBuildings.java').read_text()
d=(city/'WorldDecayPolicy.java').read_text()
p=(city/'LotPainter.java').read_text()
c=(city/'LotChooser.java').read_text()
sp=(city/'CityPopulator.java').read_text()
w=(city/'WorldDetail.java').read_text()
s=(city/'CitySettings.java').read_text()
normal=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
'new-world detail version saved in generator':normal['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version'] in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
'old-save detail defaults to disabled':'optionalFieldOf("world_detail_version", 0)' in s,
'paint ruins only after fully furnished building':'DerelictBuildings.decorate(c, plan, lot)' in p and p.index('DerelictBuildings.decorate(c, plan, lot)') > p.index('WorldDetail.lotYard(c, lot)'),
'open-air remains distinct from enclosed': 'LotType.RUBBLE && lot.district() == District.DERELICT' in r,
'ruin selection independent of random stream':'CityHash.mix(hash ^ 0xDECAB1E2026L)' in d,
'ruin selection includes all neighbourhoods':all('case '+i+' ->' in d for i in ['DERELICT','OUTSKIRTS','INDUSTRIAL','RESIDENTIAL','WATERFRONT','MARKET','GRAVEYARD']),
'different abandonment levels defined':'r < 35 ? 1 : r < 72 ? 2 : 3' in d,
'only eligible non-landmark buildings ruined':'private static boolean isEligible(LotType type)' in r and 'LotType.FACTORY' in r,
'shop identity reflects abandoned state':'"CLOSED", "ABANDONED"' in r,
'blocked up windows in derelict buildings':'Blocks.DARK_OAK_PLANKS' in r and 'Blocks.SPRUCE_PLANKS' in r,
'masonry has cracked and stained finishes':'Blocks.CRACKED_DEEPSLATE_BRICKS' in r and 'Blocks.BROWN_TERRACOTTA' in r,
'big buildings get partial roof holes':'int fromX = b.x1() - 5' in r and 'c.set(x, roofY, z, Blocks.AIR)' in r,
'roof crops and tanks protected':'lot.type() == LotType.SHOP || lot.type() == LotType.WAREHOUSE' in r and 'LotPainter.roofFor(hash) == Tower.Roof.PLAIN' in r,
'roof holes restricted to severe damage':'if (state == 3 && b.width() >= 16' in r,
'roof/corner hole offset from north-wall stairwell':'int fromX = b.x1() - 5, fromZ = b.z1() - 6;' in r,
'door center clearance guarded':'Math.abs(x - b.centerX()) <= 3' in r,
'fully ruined rubble lots have remaining walls':'shellRemnant(c, lot)' in r and 'North/west corner of a skeletal wall' in r,
'varied building types in derelict districts':all(f'LotType.{i}' in c for i in ['TENEMENT','WAREHOUSE','SHOP','FACTORY','RUBBLE','VACANT','JUNKYARD']),
'landmarks and farms cannot be ruined': 'LotType.CLINIC' not in r.split('private static boolean isEligible')[1] and 'LotType.HUNGRY_BUG' not in r.split('private static boolean isEligible')[1],
'boarded shops do not have resident villagers': 'WorldDecayPolicy.severity(lot.hash(), lot.district()) < 2' in sp,
'old shop generation still available':'plan.settings.worldDetailVersion() == 0' in sp,
'changes confined to lot-local Canvas writes':'c.set(' in r and 'c.fill(' not in r and 'new Random' not in r,
'sewer/ore generation not touched':'paintSewers(' not in r and 'Carver' not in r and 'Features' not in r,
'world detail covers other districts too':'static void streetFixtures' in w and 'static void tower(' in w,
}
failed=[name for name,ok in checks.items() if not ok]
for name,ok in checks.items():print(('PASS ' if ok else 'FAIL ')+name)
print(f'DERELICT BUILDING CHECK: {len(checks)-len(failed)}/{len(checks)} passed (offline static assertions only)')
if failed: sys.exit(1)
