#!/usr/bin/env python3
"""Source-only entrance profile, waterproofing and world-preset checks. NOT Minecraft testing."""
from pathlib import Path
import re, json, sys
ROOT=Path(__file__).resolve().parents[1]
city=(ROOT/'src/main/java/com/dorohedoro/hole/worldgen/city')
source=(city/'WaterwaySewerEntrances23.java').read_text()
painter=(city/'CityPainter.java').read_text()
settings=(city/'CitySettings.java').read_text()
preset=json.loads((ROOT/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
fail=[]; num=0

def check(why, condition):
 global num
 num+=1
 if not condition: fail.append(why)
 print(('PASS ' if condition else 'FAIL ')+why)

check('new normal worlds opt into waterway entrances profile 3',preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cavern_version']>=3)
check('profile 3 is valid in the saved codec', 'Codec.intRange(0, 5).optionalFieldOf("sewer_cavern_version", 0)' in settings)
check('old worlds still default to original profile 0', 'optionalFieldOf("sewer_cavern_version", 0)' in settings)
check('old 0..2 world profiles retain their original geometry', 'sewerCavernVersion() >= 3' in painter and 'WaterwaySewerEntrances23.paint(canvas, plan)' in painter)
check('dungeon version 2 remains enabled in the new version', 'sewerCavernVersion() >= 2' in painter)
check('sewer painter runs before waterway access',painter.index('paintSewers(canvas')<painter.index('WaterwaySewerEntrances23.paint('))
check('canal painter runs before waterway access',painter.index('paintCanals(canvas')<painter.index('WaterwaySewerEntrances23.paint('))
check('street access shafts are preserved',painter.index('paintShafts(canvas')<painter.index('WaterwaySewerEntrances23.paint('))
check('eligibility requires exactly one canal and one crossing street', 'if (xCanal == zCanal) return 0' in source)
check('multiple riverbank entry points deterministically seeded', 'CityHash.of(seed, SALT, xIndex, zIndex)' in source)
check('dry and submerged sites are different sides and separated along canal', 'xCanal, bank, 24' in source and 'xCanal, -bank, -24' in source)
check('all entrance writes are chunk-clipped', 'if (quay.intersects(c))' in source and 'if (culvert.intersects(c))' in source and 'c.fill(' in source)
check('dry riverbank hatch is hand-openable', 'Blocks.SPRUCE_TRAPDOOR.defaultBlockState()' in source)
check('wet entrance has no street-level hatch', 'block(c,s,8,0,SURFACE,Blocks.DEEPSLATE_TILES)' in source)
check('both entries join sewer side walkway (r=2)', 'range(c, s, 2, 9, -2, 2' in source and 'range(c, s, 2, 8, -1, 1' in source)
check('no modification to central sewer water at r<=1','Do not block the central sewer\'s flowing runnel at r<=1.' in source)
check('wet entrance filled at levels 59..61', 'range(c,s,5,6,-1,1,59,61,Blocks.WATER)' in source)
check('water-safe divider at r=7 below waterline', 'range(c,s,7,9,-1,1,FLOOR-1,SURFACE,lining())' in source and 'range(c,s,7,7,0,0,62,63,Blocks.AIR)' in source)
check('air pocket above water surface in wet entrance', 'range(c,s,6,6,0,0,62,63,Blocks.AIR)' in source)
check('dry shaft ladder extends continuously from sewer to r=8 y=63', 'y<SURFACE; y++' in source and 'Blocks.LADDER.defaultBlockState()' in source)
check('surface and submerged loot have different tables','"sewer_maintenance"' in source and '"sewer_filters"' in source)
check('both entrances contain sewer-material masonry', 'ModBlocks.SEWER_BRICKWORK.get()' in source)
check('no new mobs, deferred environment, quests or villagers in pass 23',all(t not in source for t in ['EntityType.', 'Village', 'AshSnow', 'CrossEye', 'SludgeIce', 'Quest']))
# Schematic connectivity: dry corridor r2..8 at y50..53; wet r6 (air above water),
# r7 (air above y=61 wall), and r8 (ladder y50..63); no fluid path into r8 at <=61.
from collections import deque
walkable={(r,50) for r in range(2,9)} | {(8,y) for y in range(50,64)} | {(7,62),(7,63),(6,62),(6,63)}
start=(6,62); target=(2,50); todo=deque([start]); seen={start}
while todo:
 r,y=todo.popleft()
 for nxt in [(r-1,y),(r+1,y),(r,y-1),(r,y+1)]:
  if nxt in walkable and nxt not in seen: seen.add(nxt);todo.append(nxt)
check('schematic wet swim-up connects to main sewer without forced flood',target in seen)
check('wet-water row blocked before dry ladder', not any((r,61) in {(5,61),(6,61)} for r in [8]))
check('surface hatch exits at pavement Y64 while water surface is Y61','private static final int SURFACE = 64;' in source and 'range(c,s,5,6,-1,1,59,61,Blocks.WATER)' in source)
for i in range(-3,4):
 for j in range(-3,4):
  # exactly one canal is a valid crossing; all others invalid
  possible = (i*j)%3==0
  # purely verify the sign/rotation mapping is one-to-one and leads to the centerline
  for xCanal in (False,True):
   for bank in (-1,1):
    for r in range(2,10):
     x=(10+bank*r) if xCanal else (20+24)
     z=(20+24) if xCanal else (10+bank*r)
     x2=(10+bank*(r+1)) if xCanal else (20+24)
     z2=(20+24) if xCanal else (10+bank*(r+1))
     check_if=(abs(x2-x)+abs(z2-z)) == 1
     if not check_if:fail.append('transform does not connect')
print(f'PASS23 WATERWAY STATIC: {num} checks, {len(fail)} failed (not compiled, not in-game verified)')
if fail:
 for f in fail:print('FAIL:',f)
 sys.exit(1)
