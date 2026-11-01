#!/usr/bin/env python3
"""Actual source and UV layout checks; these do not substitute for running Minecraft."""
from pathlib import Path
from PIL import Image
import json,re
root=Path(__file__).resolve().parents[1]
geo=root/'docs/model_geometry'
models=root/'src/main/java/com/dorohedoro/hole/client/entity'
textures=root/'src/main/resources/assets/dorohedoro/textures/entity'
req={'sewer_roach':(130,'eye_front_','mouth_taster_','feeler_'),'scrap_rat':(120,'body_pelt_','whisker_fan_','toe_'),'sewer_leech':(110,'gullet_dark_depth','upper_canine_tip_','side_hook_tip_'),'glow_moth':(270,'sculpted_primary_wing_','curved_hindwing_feather_','feather_antenna_'),'sludge_crawler':(38,'organic_sludge_slope_','sludged_plate_','oozing_tooth_'),'rust_eater_beetle':(50,'rusty_scutum_','antenna_tip_','leg_spur'),'cavern_burrower':(60,'shoulder_slope_','matted_fur_','claw_segment_'),'spore_crawler':(50,'fungal_spore_sacs_','speckled_cap_','back_fungus_')}
num=0
for name,(minn,*parts) in req.items():
 m=json.loads((geo/(name+'.json')).read_text())
 boxes=[box for group in m['groups'].values() for box in group]
 assert len(boxes)>=minn,(name,len(boxes),minn)
 names={c['name'] for c in boxes}
 for p in parts:assert any(x.startswith(p) for x in names),(name,p)
 java=(models/(m['class']+'Model.java')).read_text()
 assert java.count('.addBox(')==len(boxes),(name,'java geometry parity')
 assert java.count('PartPose.offset(')==len(m['groups']),(name,'part positions')
 with Image.open(textures/(name+'.png')) as im:
  assert list(im.size)==m['texture_size'] and im.mode=='RGBA',name
  assert len(im.getcolors(maxcolors=1000000) or [])>14,(name,'flat texture')
 w,h=m['texture_size'];regions=[]
 for c in boxes:
  u,v=c['uv'];cw,ch,cd=[max(1,int(__import__('math').ceil(x))) for x in c['dim']]
  assert min(c['dim'])>0,(name,c['name'])
  bw=2*(cw+cd)+2;bh=ch+cd+2
  assert 0<=u and 0<=v and u+bw<=w and v+bh<=h,(name,c['name'],'UV bounds')
  for x,y,x2,y2 in regions:assert not (u<x2 and u+bw>x and v<y2 and v+bh>y),(name,c['name'],'UV overlap')
  regions.append((u,v,u+bw,v+bh));num+=1
 print(f'PASS {name}: {len(boxes)} source cuboids, UV boxes nonoverlapping, texture matched, model source synced')
assert (root/'docs/reference/approved_creatures.png').exists(),'approved design missing'
print(f'PASS: {len(req)} model classes and {num} authored cuboid UV islands checked (offline only)')

# Design-specific tests for the source model geometry, not Minecraft collision/animation runtime.
leech=json.loads((geo/'sewer_leech.json').read_text())
leech_boxes=[b for grp in leech['groups'].values() for b in grp]
fangs=[b for b in leech_boxes if any(x in b['name'] for x in ('upper_canine_','lower_canine_','side_hook_'))]
assert len(fangs)==12, f'Expected six modeled teeth with 2 pieces each; got {len(fangs)}'
leech_len=max(b['xyz'][2]+b['dim'][2] for b in leech_boxes)-min(b['xyz'][2] for b in leech_boxes)
assert leech_len>=30, f'Large leech should span 30+ model pixels: {leech_len}'
entity=(root/'src/main/java/com/dorohedoro/hole/registry/ModEntities.java').read_text()
assert '.sized(0.88F, 0.62F)' in entity, 'Enlarged leech needs aligned entity collision dimensions'
moth=json.loads((geo/'glow_moth.json').read_text())
for side in ('left_wing','right_wing'):
 wing=moth['groups'][side]
 assert len([b for b in wing if b['name'].startswith('sculpted_primary_wing_')])==30
 assert len([b for b in moth['groups'][side.replace('_wing','_hindwing')] if b['name'].startswith('curved_hindwing_feather_')])==4
 wing_shell=[b for b in wing if b['name'].startswith('sculpted_primary_wing_')]
 assert max(b['xyz'][1]+b['dim'][1] for b in wing_shell)-min(b['xyz'][1] for b in wing_shell)>=4.2,'Wing needs meaningful depth'
 byrow={row:max(abs(b['xyz'][0])+(b['dim'][0] if side=='right_wing' else 0) for b in wing_shell if f'wing_{row}_' in b['name']) for row in range(6)}
 assert byrow[3]>byrow[0]+4.0 and byrow[3]>byrow[5]+1.0, 'Wing silhouette must taper at both ends'
assert len(moth['groups']) == 6, 'Moth must have body, head and four independently pivoted wings'
for group in ('left_wing','right_wing','left_hindwing','right_hindwing'):
 piv=moth['part_pivots'][group]
 assert group in moth['groups'] and abs(piv[0])>=2.5 and piv[1]<0
assert all(group in moth['part_pivots'] for group in moth['groups']), 'Every movable anatomical group needs a local joint pivot'
assert 'left_hindwing.zRot' in moth['animations'] and 'right_hindwing.zRot' in moth['animations']
assert 'entity.isResting()' in moth['animations']['left_wing.zRot']
assert ' - 0.34F' in moth['animations']['left_hindwing.zRot']
java_m=(models/'GlowMothModel.java').read_text()
assert 'root.getChild("left_hindwing")' in java_m
assert 'PartPose.offset(-3.05F, 12.75F, -1.8F)' in java_m
assert 'PartPose.offset(3.05F, 12.75F, -1.8F)' in java_m
assert 'PartPose.offset(-2.65F, 14.85F, 4.7F)' in java_m
print('PASS Pass 06: six distinct leech teeth, large hitbox, independent mirrored shoulder-hinge fore/hind wings and phase lag')
