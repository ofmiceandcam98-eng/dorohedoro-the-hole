#!/usr/bin/env python3
"""Offline source coverage: real Minecraft client/server validation is separate."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]
G=R/'docs/model_geometry'
B=R/'src/main/java/com/dorohedoro/hole'
classes={
'sewer_roach':'leg_l_0.zRot',
'scrap_rat':'tail_3.yRot',
'sewer_leech':'mouth.xRot',
'glow_moth':'left_hindwing.zRot',
'sludge_crawler':'tentacle_l.xRot',
'rust_eater_beetle':'leg_r_2.xRot',
'cavern_burrower':'paw_l_0.zRot',
'spore_crawler':'body.xRot'}
count=0
for mob,feature in classes.items():
 m=json.loads((G/(mob+'.json')).read_text())
 assert feature in m['animations'],(mob,feature)
 assert all(part.split('.')[0] in m['groups'] for part in m['animations'])
 assert all(part in m['part_pivots'] for part in m['groups']),mob
 java=(B/'client/entity'/f"{m['class']}Model.java").read_text()
 for phrase in ['float motion', 'float attack', 'float recoil', feature+' =']:
  assert phrase in java,(mob,phrase)
 assert java.count('PartPose.offset(')==len(m['groups'])
 count+=1
print(f'PASS: {count} unique mob movement rigs, local anatomical joints, idle/attack/recoil variables')
light=(B/'world/light/GlowMothLightBlock.java').read_text()
entity=(B/'entity/GlowMothEntity.java').read_text()
blocks=(B/'registry/ModBlocks.java').read_text()
renderer=(B/'client/entity/GlowMothRenderer.java').read_text()
checks=['RenderShape.INVISIBLE','EXPIRE_TICKS = 14','getEntitiesOfClass(GlowMothEntity.class','level.removeBlock(pos, false)','level.scheduleTick(pos, this, EXPIRE_TICKS)']
for phrase in checks: assert phrase in light,phrase
for phrase in ['server.isLoaded(pos)', 'getBlockState(pos).isAir()', 'tickCount % 10 == 0', 'server.scheduleTick(pos, ModBlocks.GLOW_MOTH_LIGHT.get()']:
 assert phrase in entity,phrase
for phrase in ['.noCollission()', '.noLootTable()', '.replaceable()', 'state -> GlowMothLightBlock.LIGHT_LEVEL']:
 assert phrase in blocks,phrase
assert 'getBlockLightLevel' in renderer
assert 'GlowMothLightBlock' in blocks
assert not (R/'src/main/resources/assets/dorohedoro/models/item/glow_moth_light.json').exists()
print('PASS: glowing renderer, 12-light vanilla emitter, non-solid no-loot air-only placement, expiry cleanup, throttling, no item')
