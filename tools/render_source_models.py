from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import json, math, random
root=Path(__file__).resolve().parents[1]
source=root/'docs/model_geometry'
import sys
COLORS={
 'roach_shell':'#573522','roach_high':'#925a35','roach_dark':'#33211d','roach_edge':'#ba8550','roach_legs':'#402a25','roach_eye':'#dd5540','roach_mouth':'#b59a70',
 'rat_fur':'#8c7973','rat_dark':'#554c4b','rat_light':'#b8a49a','rat_pink':'#aa777f','rat_inner':'#d89e9d','rat_eye':'#191716','rat_whisker':'#dccbb6',
 'leech_body':'#622936','leech_bright':'#904154','leech_dark':'#321c2a','leech_mouth':'#b76d75','leech_tooth':'#d6b6a5','leech_slime':'#76636f',
 'moth_fur':'#a3ab89','moth_light':'#d1d1aa','moth_green':'#71886c','moth_wing':'#8eac8a','moth_pattern':'#eef4ba','moth_eye':'#fff6bd','moth_legs':'#505b4b',
 'sludge_body':'#38372f','sludge_ooze':'#625f3f','sludge_sheen':'#8c8860','sludge_eye':'#c5c978','sludge_tentacle':'#48483b',
 'beetle_shell':'#6e4933','beetle_rust':'#b9753d','beetle_dark':'#332a26','beetle_plate':'#9d8056','beetle_eye':'#e1c27c',
 'burrower_fur':'#74645e','burrower_dark':'#453b39','burrower_pale':'#b19f8e','burrower_claw':'#ded6b6','burrower_nose':'#ad777c','burrower_eye':'#282421',
 'spore_skin':'#554c48','spore_pod':'#7c715d','spore_cap':'#9d8b63','spore_gill':'#c0ba91','spore_glow':'#b6cf75','spore_claw':'#4c4037',
}

def rgb(v):return tuple(int(v[i:i+2],16) for i in (1,3,5))
def shade(c,fac):return tuple(max(0,min(255,round(v*fac))) for v in c)
def proj(p):
 x,y,z=p
 return (x+z)*1.22, y*1.33+(x-z)*0.5

def mesh(name):
 data=json.loads((source/f'{name}.json').read_text())
 faces=[]
 for cubes in data['groups'].values():
  for c in cubes:
   x,y,z=c['xyz'];w,h,d=c['dim']; r=rgb(COLORS[c['material']]);
   verts={
    'top':[(x,y,z),(x+w,y,z),(x+w,y,z+d),(x,y,z+d)],
    'right':[(x+w,y,z),(x+w,y,z+d),(x+w,y+h,z+d),(x+w,y+h,z)],
    'front':[(x,y,z),(x+w,y,z),(x+w,y+h,z),(x,y+h,z)],
   }
   for side,pts in verts.items():
    avg=[sum(v[i] for v in pts)/4 for i in range(3)]
    depth=avg[0]-avg[2]-0.7*avg[1]
    faces.append((depth, side,pts,r,c['name']))
 return sorted(faces, key=lambda f:f[0]),data

def render(name,imgsz=(710,440)):
 faces,data=mesh(name)
 allpts=[proj(v) for _,_,pts,_,_ in faces for v in pts]
 xmin=min(x for x,y in allpts);xmax=max(x for x,y in allpts)
 ymin=min(y for x,y in allpts);ymax=max(y for x,y in allpts)
 width,height=imgsz
 scl=min((width-85)/max(1,xmax-xmin),(height-75)/max(1,ymax-ymin));scy=scl
 ox=(width-(xmax-xmin)*scl)/2-xmin*scl
 oy=(height-(ymax-ymin)*scl)/2-ymin*scl
 im=Image.new('RGBA',(width,height),(0,0,0,0)); draw=ImageDraw.Draw(im)
 rng=random.Random(22)
 for depth,side,pts,base,n in faces:
  poly=[(round(proj(v)[0]*scl+ox),round(proj(v)[1]*scy+oy)) for v in pts]
  facecol=shade(base,{'top':1.17,'front':0.92,'right':0.75}[side])
  draw.polygon(poly, fill=facecol+(255,),outline=shade(base,0.47)+(255,),width=max(1,round(scl*.05)))
  # sparse pixel-scale light/dark speckles that do not extend beyond cuboid faces
  import PIL.ImageDraw
  for _ in range(6):
   a=rng.uniform(0.12,0.89);b=rng.uniform(.1,.92)
   pt=( (1-a)*(1-b)*poly[0][0]+a*(1-b)*poly[1][0]+a*b*poly[2][0]+(1-a)*b*poly[3][0], (1-a)*(1-b)*poly[0][1]+a*(1-b)*poly[1][1]+a*b*poly[2][1]+(1-a)*b*poly[3][1] )
   rr=max(1,round(scl*.04));delta=rng.choice([.76,1.23,1.1])
   c=shade(facecol,delta);draw.rectangle((round(pt[0]),round(pt[1]),round(pt[0])+rr,round(pt[1])+rr),fill=c+(180,))
 return im

def font(n,bold=False):
 for f in ['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf','/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf']:
  if Path(f).exists():return ImageFont.truetype(f,n)
 return ImageFont.load_default()

def sheet(names,title,outfile):
 W,H=1680,1160
 canvas=Image.new('RGB',(W,H),(15,22,24));dr=ImageDraw.Draw(canvas)
 for i in range(0,W,34):dr.line((i,0,i,H),fill=(21,29,30),width=1)
 for i in range(0,H,34):dr.line((0,i,W,i),fill=(21,29,30),width=1)
 dr.text((38,18),'DOROHEDORO / THE HOLE',font=font(48,True),fill=(238,225,204))
 dr.text((42,77),title,font=font(20,True),fill=(136,166,150))
 labels={
 'sewer_roach':('SEWER COCKROACH','54 cuboids  |  animated legs + antennae'),
 'scrap_rat':('SCRAP RAT','35 cuboids  |  animated paws + tail'),
 'sewer_leech':('SEWER LEECH','33 cuboids  |  segmented, moving body'),
 'glow_moth':('GLOW MOTH','46 cuboids  |  layered articulated wings'),
 'sludge_crawler':('SLUDGE CRAWLER','21 cuboids  |  moving sludge limbs'),
 'rust_eater_beetle':('RUST-EATER BEETLE','23 cuboids  |  animated insect legs'),
 'cavern_burrower':('CAVERN BURROWER','32 cuboids  |  oversized mining claws'),
 'spore_crawler':('SPORE CRAWLER','28 cuboids  |  fungal plating + legs'),
 }
 for i,name in enumerate(names):
  cx=28+(i%2)*835;cy=120+(i//2)*505
  dr.rectangle((cx,cy,cx+808,cy+485),fill=(22,31,33),outline=(80,96,91),width=2)
  im=render(name,(796,407));canvas.paste(im,(cx+6,cy+4),im)
  dr.line((cx+12,cy+406,cx+796,cy+406),fill=(89,104,97),width=1)
  dr.text((cx+16,cy+412),f'{i+1:02d}  {labels[name][0]}',font=font(27,True),fill=(242,227,199))
  dr.text((cx+18,cy+452),labels[name][1],font=font(16),fill=(157,180,162))
 dr.text((38,H-24),'RENDERED FROM GENERATED JAVA MODEL CUBOIDS  •  NOT AN IN-GAME SCREENSHOT',font=font(16),fill=(149,157,151))
 canvas.save(outfile)

sheet(['sewer_roach','scrap_rat','sewer_leech','glow_moth'],'HIGH-DETAIL MOBS  •  REBUILT SOURCE GEOMETRY',str(root/'docs/previews/upgraded_4_source_models.png'))
sheet(['sludge_crawler','rust_eater_beetle','cavern_burrower','spore_crawler'],'NEW UNDERGROUND SPECIES  •  INITIAL SOURCE GEOMETRY',str(root/'docs/previews/new_4_source_models.png'))
