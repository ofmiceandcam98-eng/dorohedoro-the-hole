#!/usr/bin/env python3
"""Pass09 spawn/AI speed regression. Existing Pass09 GIFs are historical, and current gait frequencies use Pass10 models. Not a live test."""
from pathlib import Path
from PIL import Image, ImageChops
import json

R=Path(__file__).resolve().parents[1]
G=R/'docs/model_geometry'
ENT=R/'src/main/java/com/dorohedoro/hole/entity'
CL=R/'src/main/java/com/dorohedoro/hole/client/entity'
PREV=R/'docs/previews/animation_pass09'
B=R/'src/generated/resources/data/dorohedoro/worldgen/biome'
check_count=0
motions={
  'sludge_crawler': ('SludgeCrawlerEntity.java','0.24D','1.72F'),
  'rust_eater_beetle': ('RustEaterBeetleEntity.java','0.27D','2.76F'),
  'cavern_burrower': ('CavernBurrowerEntity.java','0.23D','2.08F'),
  'spore_crawler': ('SporeCrawlerEntity.java','0.30D','2.35F'),
}
for mob,(entity,move_speed,gait_freq) in motions.items():
  data=json.loads((G/f'{mob}.json').read_text())
  java=(CL/f"{data['class']}Model.java").read_text()
  es=(ENT/entity).read_text()
  assert f'MOVEMENT_SPEED, {move_speed}' in es,mob
  assert any(gait_freq in expr for expr in data['animations'].values()),mob
  for key,expr in data['animations'].items():
    assert f'{key} = {expr};' in java,(mob,key)
    check_count+=1
  gif=Image.open(PREV/f'{mob}_movement_5s.gif')
  assert gif.n_frames == 50,(mob,gif.n_frames)
  duration=sum((gif.seek(i) or gif.info.get('duration',0)) for i in range(gif.n_frames))
  assert duration==5000,(mob,duration)
  gif.seek(0);a=gif.convert('RGB').copy()
  gif.seek(10);b=gif.convert('RGB').copy()
  assert ImageChops.difference(a,b).getbbox() is not None,(mob,'static render')
  check_count+=5

for biome_name,weight,max_count in [('hole_industrial',9,2),('hole_derelict',6,2),('hole_outskirts',3,1),('hole_undercity',18,2)]:
  bi=json.loads((B/(biome_name+'.json')).read_text())
  beetles=[mob for mob in bi['spawners']['creature'] if mob['type']=='dorohedoro:rust_eater_beetle']
  assert len(beetles)==1,(biome_name,beetles)
  assert beetles[0]['weight']==weight and beetles[0]['minCount']==1 and beetles[0]['maxCount']==max_count
  check_count+=1
for biome_name in ['hole_residential','hole_market','hole_graveyard','hole_waterfront']:
  bi=json.loads((B/(biome_name+'.json')).read_text())
  assert not any(mob['type']=='dorohedoro:rust_eater_beetle' for mob in bi['spawners'].get('creature',[])),biome_name
  check_count+=1
mod=(R/'src/main/java/com/dorohedoro/hole/registry/ModEntities.java').read_text()
assert 'rustBeetleHabitat(level, pos)' in mod
assert 'level.getBiome(pos).is(HoleBiomes.UNDERCITY)' in mod
for required in ['HoleBiomes.INDUSTRIAL','HoleBiomes.DERELICT','HoleBiomes.OUTSKIRTS']:
  assert f'level.getBiome(pos).is({required})' in mod,required
assert 'pos.getY() > 82' in mod
assert 'level.getFluidState(pos).isEmpty()' in mod
check_count+=7
for kind in ['sewer','cavern']:
  gif=Image.open(PREV/f'{kind}_movement_5s.gif')
  assert gif.n_frames==50
  assert sum((gif.seek(i) or gif.info.get('duration',0)) for i in range(gif.n_frames))==5000
  check_count+=2
print(f'PASS {check_count} cavern motion source and archived GIF validity, surface spawn weights, habitat bounds and 5s animation checks')
print('NOTE: No NeoForge build, in-game spawning, actual AI pathing, lighting or client renderer tests performed.')
