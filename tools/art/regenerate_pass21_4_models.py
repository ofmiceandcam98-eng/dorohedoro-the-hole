from pathlib import Path
import json
P=Path(__file__).resolve().parents[2]/'src/main/resources/assets/dorohedoro/models/item'

def cuboid(a,b,layer='upper', rotation=None):
 # Atlas rows map to logical UV rows (2..6, 6..10, 10..14) for shoulder/forearm/hand
 cols={'upper':(2,6),'middle':(6,10),'lower':(10,14),'cap':(0,4)}
 lo,hi=cols[layer]
 faces={
 'north': {'uv':[0,lo,4,hi], 'texture':'#limb'},
 'south': {'uv':[4,lo,8,hi], 'texture':'#limb'},
 'east': {'uv':[8,lo,12,hi], 'texture':'#limb'},
 'west': {'uv':[0,lo,4,hi], 'texture':'#limb'},
 'up': {'uv':[10,0,14,4], 'texture':'#limb'},
 'down': {'uv':[0,0,4,4], 'texture':'#limb'},
 }
 obj={'from':a,'to':b,'faces':faces}
 if rotation:obj['rotation']={'origin':rotation[0],'axis':'z','angle':rotation[1],'rescale':False}
 return obj

for type in ('severed','cooked'):
 for side in ('left','right'):
  for limb in ('arm','leg'):
   name=f'{type}_{side}_{limb}'
   p=P/(name+'.json');model=json.loads(p.read_text())
   arm=limb=='arm';sign=(-1 if side=='left' else 1)
   if arm:
    els=[
      cuboid([5.1,11.6,5.5],[10.7,15.4,10.5],'upper'),
      cuboid([5.9,7.1,5.9],[10.3,12,10.1],'middle',([8.0,10.4,8.0],sign*22.5)),
      cuboid([6.1,3.2,6.1],[9.9,7.8,9.9],'lower',([8.0,7.3,8.0],sign*22.5)),
      cuboid([6.2,2.0,6.1],[10.0,4.2,10.1],'lower',([8.0,4.0,8.0],sign*22.5)),
      cuboid(([10.0,3.0,6.8] if sign==1 else [4.4,3.0,6.8]),([11.8,5.0,9.7] if sign==1 else [6.2,5.0,9.7]),'lower')
    ]
   else:
    els=[
      cuboid([5.2,11.2,5.5],[10.8,15.6,10.5],'upper'),
      cuboid([5.6,7.2,5.8],[10.4,12.4,10.2],'middle'),
      cuboid([6.1,3.5,6.0],[10.3,8.0,10.0],'middle',([8.0,7.0,8.0],sign*22.5)),
      cuboid([5.9,1.6,5.8],[10.6,4.4,11.9],'lower'),
      cuboid([5.7,1.4,9.2],[10.7,3.4,13.0],'lower')
    ]
   model['elements']=els
   model['display']['gui']={'rotation':[24,137 if side=='left' else 224,-12 if arm else 6], 'translation':[0,0.25,0], 'scale':[1.5,1.5,1.5]}
   model['display']['ground']={'rotation':[0,0,90],'translation':[0,1,0],'scale':[.72,.72,.72]}
   model['display']['fixed']={'rotation':[0,120,0],'scale':[1.1,1.1,1.1]}
   model['display']['firstperson_righthand']={'rotation':[0,40,20],'translation':[0,1,0],'scale':[.86,.86,.86]}
   model['display']['thirdperson_righthand']={'rotation':[75,45,0],'translation':[0,2,0],'scale':[.7,.7,.7]}
   p.write_text(json.dumps(model,indent=2)+'\n')
print('updated 8 limb models')
