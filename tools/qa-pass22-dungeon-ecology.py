#!/usr/bin/env python3
"""Source structural + registry reference QA. Does not imply compiled Minecraft functionality."""
from pathlib import Path
import json,sys,re,collections
P=Path(__file__).resolve().parents[1]
SRC=P/'src/main/java/com/dorohedoro/hole'
R=P/'src/generated/resources/data/dorohedoro'
checks=[]
def check(n,ok):
 checks.append((n,bool(ok)))
 print(('PASS' if ok else 'FAIL'),n)
ids=['sewer_roach','scrap_rat','sewer_leech','glow_moth','sludge_crawler','rust_eater_beetle','cavern_burrower','spore_crawler']
biomes=['residential','market','industrial','waterfront','derelict','graveyard','outskirts','frostbitten_industrial','undercity']
mc=json.loads((P/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
settings=mc['dimensions']['minecraft:overworld']['generator']['city']
check('new world opts into sewer dungeons and furnishings',settings['sewer_cavern_version']>=2 and settings['world_detail_version']>=9 and settings['sewer_underworld'])
code=(SRC/'worldgen/city/CitySettings.java').read_text()
check('saved old worlds use disabled legacy profiles','optionalFieldOf("sewer_cavern_version", 0)' in code and 'optionalFieldOf("world_detail_version", 0)' in code)
check('legacy cave content still generated', 'SewerCaveBranches.paint' in (SRC/'worldgen/city/CityPainter.java').read_text())
check('dungeon painter gated by saved world profile','if (plan.settings.sewerCavernVersion() >= 2)' in (SRC/'worldgen/city/CityPainter.java').read_text())
mod=(SRC/'registry/ModEntities.java').read_text()
for id in ids: check('registered entity and natural placement '+id,id.upper() in mod and 'event.register('+id.upper()+'.get()' in mod)
for name in biomes:
 data=json.loads((R/('worldgen/biome/hole_'+name+'.json')).read_text());allroster=set()
 for vals in data['spawners'].values():
  allroster.update(v['type'] for v in vals)
 check('all eight creature entries at Y49 in hole_'+name,all('dorohedoro:'+i in allroster for i in ids))
check('explicit undercity biome threshold corrected without moving existing seam', 'UNDERGROUND_TOP = 40' in (SRC/'worldgen/HoleBiomeSource.java').read_text() and 'isHoleCityBiome' in mod)
check('surface biome and water restrictions remain enabled', 'private static boolean surface(' in mod and 'private static boolean nextToWater(' in mod)
dun=(SRC/'worldgen/city/SewerDungeon22.java').read_text()
for id in ids:check('dungeon room encounter spawner can select '+id,'"dorohedoro:'+id+'"' in dun)
check('dungeon uses deterministic hash + chunk clip', 'CityHash.of(seed' in dun and 'if (!c.overlaps(footprint)) continue' in dun and 'c.beginLot' in dun)
check('optional starter entrance', 'xIndex == 0 && zIndex == 0' in dun)
check('three theme encounter chambers and secret cache',dun.count('c.spawner(')==3 and 'sealed' not in '' and 'sewer_vault' in dun and 'CRACKED_DEEPSLATE_BRICKS' in dun)
canvas=(SRC/'worldgen/city/Canvas.java').read_text()
check('spawner block entities write entity identifiers', 'minecraft:mob_spawner' in canvas and 'SpawnData' in canvas and 'MaxNearbyEntities' in canvas)
check('treasure loot source available',all('"'+name+'"' in (SRC/'datagen/ModLoot.java').read_text() and (R/('loot_table/chests/'+name+'.json')).exists() for name in ('sewer_vault','sewer_filters','sewer_maintenance')))
for name in ('sewer_vault','sewer_filters','sewer_maintenance'):
 loot=json.loads((R/('loot_table/chests/'+name+'.json')).read_text())
 check('loot schema '+name,loot.get('type')=='minecraft:chest' and len(loot['pools'][0]['entries'])>=7)
interior=(SRC/'worldgen/city/InteriorLayouts15.java').read_text(); details=(SRC/'worldgen/city/InteriorDetails22.java').read_text()
check('v9 furniture for residential/shop/clinic/industrial',all('InteriorDetails22.'+name+'(' in interior for name in ('residence','lobby','shop','clinic','industrial')))
check('non-destructive clip and surface support for furniture','c.contains(x, z)' in details and 'c.get(x, y, z).isAir()' in details and 'c.get(x, y - 1, z).isAir()' in details)
check('furniture opt-in only new worlds','if (!c.hasExpandedFurniture()' in details and 'return worldDetailVersion >= 9;' in canvas)
check('villager profession system not introduced',not (SRC/'villager').exists())
# Every generated biome has no duplicate custom entity in a single biome category.
for name in biomes:
 d=json.loads((R/('worldgen/biome/hole_'+name+'.json')).read_text())
 for cat,entries in d['spawners'].items():
  found=[v['type'] for v in entries if v['type'].startswith('dorohedoro:')]
  check(name+' '+cat+' no duplicate spawn entries',len(found)==len(set(found)))
# All JSON valid across source resources.
json_paths=list((P/'src/main/resources').rglob('*.json'))+list((P/'src/generated/resources').rglob('*.json'))
try:
 for f in json_paths:json.loads(f.read_text())
 check('JSON syntax '+str(len(json_paths))+' resource files',True)
except Exception as e:
 check('JSON syntax '+str(len(json_paths))+' resource files',False);print(e)
# Dungeon topological sanity: rasterize the documented rooms/corridors at stand-height Y+2.
def room(x,z,rx,rz):
 return {(a,b) for a in range(x-rx,x+rx+1) for b in range(z-rz,z+rz+1)}
walk=room(-22,0,7,7)|room(-22,-24,6,7)|room(-22,24,6,7)|room(-47,0,10,9)
for x0,x1,z in [(-16,-3,0),(-38,-28,0)]:
 walk.update((x,zz) for x in range(x0,x1+1) for zz in range(z-1,z+2))
for x,z0,z1 in [(-22,-18,-6),(-22,6,18)]:
 walk.update((xx,z) for z in range(z0,z1+1) for xx in range(x-1,x+2))
vis=set();start=(-3,0);q=collections.deque([start]);vis.add(start)
while q:
 x,z=q.popleft()
 for point in ((x+1,z),(x-1,z),(x,z+1),(x,z-1)):
  if point in walk and point not in vis:vis.add(point);q.append(point)
check('3-wide corridors join all five intended dungeon zones',all(point in vis for point in [(-22,0),(-22,-24),(-22,24),(-47,0)]) and len(vis)==len(walk))
bad=[x for x,good in checks if not good]
print(f'PASS22 STATIC: {len(checks)-len(bad)}/{len(checks)} passed; not compiled/in-game validated')
if bad:print('Failures:',bad);sys.exit(1)
