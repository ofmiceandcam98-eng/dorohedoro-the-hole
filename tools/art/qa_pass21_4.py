"""Static/structural QA; requires Pillow. This is not compilation/gameplay verification."""
from pathlib import Path
from PIL import Image
import json, hashlib
P=Path(__file__).resolve().parents[2]
A=P/'src/main/resources/assets/dorohedoro'
assert A.exists(),A
IT=A/'textures/item';BL=A/'textures/block';MI=A/'models/item'
items=sorted(IT.glob('*.png'));blocks=sorted(BL.glob('*.png'))
assert len(items)==59,len(items)
assert len(blocks)==31,len(blocks)
check=0
for p in items+blocks:
 with Image.open(p) as im:
  im.load(); assert im.mode=='RGBA',(p,im.mode)
  name=p.stem;is_limb=('severed_' in name or 'cooked_left_' in name or 'cooked_right_' in name) and p.parent==IT
  assert im.size==((64,64) if is_limb else (32,32)),(p,im.size)
  assert im.getbbox() is not None,p
  if p.parent==BL and name not in ('portal_surface','condensed_smoke_barrier'):
   assert im.getpixel((0,0))[3]==255,p
  check+=1
# All JSON parse (includes models/recipes/particles)
js=sorted((P/'src/main/resources').rglob('*.json'))
for p in js:
 try:json.loads(p.read_text())
 except Exception as e:raise ValueError(str(p)) from e
for prefix in ('severed','cooked'):
 for side in ('left','right'):
  for part in ('arm','leg'):
   n=f'{prefix}_{side}_{part}';m=json.loads((MI/(n+'.json')).read_text())
   assert m['textures']['limb']=='dorohedoro:item/'+n
   assert len(m['elements'])==5,(n,len(m['elements']))
   for el in m['elements']:
    assert all(0<=v<=16 for v in el['from']+el['to']),(n,el)
    for face in el['faces'].values():
     assert all(0<=v<=16 for v in face['uv'])
    if 'rotation' in el: assert el['rotation']['angle'] in (-45,-22.5,0,22.5,45)
# All namespace-qualified texture references in local model JSON resolve.
for model_path in (A/'models').rglob('*.json'):
 m=json.loads(model_path.read_text())
 for value in m.get('textures',{}).values():
  if isinstance(value,str) and value.startswith('dorohedoro:'):
   tex=A/'textures'/(value.split(':',1)[1]+'.png')
   assert tex.exists(),(model_path,tex)
# Critical portal fix, magic definitions and three slot/nine presets
portal=(P/'src/main/java/com/dorohedoro/hole/world/PortalService.java')
if not portal.exists():
 candidates=list(P.rglob('PortalService.java'));assert candidates;candidates=sorted(candidates);portal=candidates[0]
s=portal.read_text();assert 'axis == Direction.Axis.X ? core.offset(0, 0, sign) : core.offset(sign, 0, 0)' in s
print('STATIC QA PASS: 59 items; 31 blocks; 8 limb models; %d JSON valid; portal correction intact.'%len(js))
print('Not compiled or tested in Minecraft.')
