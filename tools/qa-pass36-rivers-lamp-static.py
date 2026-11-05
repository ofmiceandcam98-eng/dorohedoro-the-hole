#!/usr/bin/env python3
"""Offline regression for Pass 36 design/schema. NOT a NeoForge build or runtime test."""
from pathlib import Path
import json, re
root = Path(__file__).resolve().parents[1]
def read(rel): return (root / rel).read_text()
source = 'src/main/java/com/dorohedoro/hole/worldgen/city/'
check = lambda flag,msg: (print(('PASS' if flag else 'FAIL')+' '+msg),flag)[1]
checks=[]
settings=read(source+'CitySettings.java')
plan=read(source+'CityPlan.java')
river=read(source+'RiverChannels24.java')
lantern=read('src/main/java/com/dorohedoro/hole/world/light/CavernLampBlock.java')
facades=read(source+'FacadeDetail36.java')
reg=read('src/main/java/com/dorohedoro/hole/registry/ModBlocks.java')
normal=json.loads(read('src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
checks.append(check('worldDetailVersion > 22' in settings or 'worldDetailVersion > 21' in settings,'world detail upper limit extended for new profile'))
checks.append(check('worldDetailVersion() >= 21 ? 17 : 9' in plan,'river reservation enlarged ONLY for new profile'))
checks.append(check('line.halfWidth() < 15' in river and '3.0 * station' in river,'legacy river offset unchanged'))
checks.append(check('line.halfWidth() - 2 - Math.abs(centerOffset' in river,'new river banks reserve two-block verge'))
checks.append(check('Math.abs(coord - line.center()) <= line.halfWidth()' in river,'river query respects actual reservation width'))
checks.append(check('"wall"' in lantern and 'getStateForPlacement' in lantern and 'canSurvive' in lantern,'lamp wall placement and support rules implemented'))
checks.append(check('BlockStateProperties.WATERLOGGED' in lantern and 'getFluidState' in lantern,'lamp waterlogging preserved'))
checks.append(check('CavernLampBlock(' in reg and 'lightLevel(state -> 12)' in reg,'lamp registered with reduced light level'))
checks.append(check('hasFacadeDetail36()' in read(source+'LotPainter.java') and 'hasFacadeDetail36()' in read(source+'Canvas.java'),'facade enhancements gated to new profile'))
checks.append(check('Blocks.CHEST' not in facades and 'LANTERN' not in facades,'facades add no exposed chests or bright lanterns'))
checks.append(check(re.search(r'"world_detail_version"\s*:\s*(?:21|22)', read('src/main/resources/data/minecraft/worldgen/world_preset/normal.json')) is not None,'fresh world uses new profile'))
blockstates=json.loads(read('src/main/resources/assets/dorohedoro/blockstates/cavern_lamp.json'))
checks.append(check(len(blockstates['variants'])==32,'lamp states cover wall/ceiling/floor/facing/waterlogged variations'))
for kind in ('standing','hanging','wall'):
 model=json.loads(read('src/main/resources/assets/dorohedoro/models/block/cavern_lamp_'+kind+'.json'))
 coords=[(c,e[key]) for e in model['elements'] for key in ('from','to') for c in e[key]]
 checks.append(check(all(0<=c<=16 for c,v in coords),f'{kind} model lies in 16-unit Minecraft voxel space'))
 checks.append(check(len(model['elements']) >= 7,f'{kind} model has multiple distinct voxel parts'))
passed=sum(checks)
print(f'{passed}/{len(checks)} source/schema checks passed')
raise SystemExit(0 if all(checks) else 1)
