from PIL import Image,ImageDraw
from pathlib import Path
import random, math
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/dorohedoro/textures/block'
ROOT.mkdir(parents=True,exist_ok=True)

def C(h): return tuple(bytes.fromhex(h.lstrip('#'))) + (255,)
def img(c):return Image.new('RGBA',(32,32),C(c))
def put(a,x,y,c):
 if 0<=x<32 and 0<=y<32:a.putpixel((x,y),C(c))
def R(a,box,c):ImageDraw.Draw(a).rectangle(box, fill=C(c))
def L(a,pts,c,w=1):ImageDraw.Draw(a).line(pts,fill=C(c),width=w)
def darken(h,scale):return '#'+''.join(f'{max(0,min(255,round(int(h[i:i+2],16)*scale))):02x}' for i in (1,3,5))

def modular_bricks(seed,mortar,base,w=16,h=8,stagger=8,tint=None,damage=6,bevel=True):
 rng=random.Random(seed);a=img(mortar)
 palette=[base,darken(base,.89),darken(base,1.12),darken(base,.98)]
 for row in range(-1,math.ceil(32/h)+1):
  y=row*h;offset=(stagger if row%2 else 0)
  for col in range(-4,5):
   x=col*w+offset; colc=palette[rng.randrange(len(palette))]
   for yy in range(max(0,y+1),min(32,y+h)):
    for xx in range(max(0,x+1),min(32,x+w)):
     put(a,xx,yy,colc)
   if bevel:
    L(a,[(max(0,x+2),y+1),(min(31,x+w-2),y+1)],darken(colc,1.16)) if 0<=y+1<32 else None
    L(a,[(max(0,x+2),y+h-1),(min(31,x+w-2),y+h-1)],darken(colc,.72)) if 0<=y+h-1<32 else None
   for _ in range(damage):
    px=x+rng.randrange(2,max(3,w-2));py=y+rng.randrange(2,max(3,h-1))
    if 0<=px<32 and 0<=py<32:put(a,px,py,darken(colc,rng.choice([.72,.83,1.14])))
 # occasional micro pitting across mortar and bricks; intentional sparse clusters
 return a

def stone(seed,base,rough=1,veins=False):
 a=img(base);rng=random.Random(seed)
 shades=[darken(base,f) for f in (.73,.83,.93,1.09,1.17)]
 for i in range(int(rough*128)):
  x=rng.randrange(32);y=rng.randrange(32);col=shades[rng.randrange(5)]
  put(a,x,y,col)
  if i%4==0:put(a,x+1,y,col)
 if veins:
  # Short low-contrast irregular mineral fractures rather than a repeating full-block squiggle.
  # Long strokes create visible 3x3 wallpaper patterns at ordinary Minecraft viewing distance.
  for j in range(5):
   x=rng.randrange(3,27);y=rng.randrange(3,27)
   dx=rng.choice([-1,1]);dy=rng.choice([-1,1])
   pts=[(x,y),(x+2*dx,y+dy),(x+3*dx,y+3*dy)]
   L(a,pts,darken(base,.79),1)
   if j%2==0:put(a,x,y,darken(base,1.12))
 return a

def mineral(name,seed,bas,border,mid,shiny):
 a=stone(seed,bas,rough=.74,veins=True)
 r=random.Random(seed+10)
 coords=[(5,6),(21,7),(10,23),(26,24),(2,29)]
 for j,(x,y) in enumerate(coords):
  x+=r.choice([-1,0,1]);y+=r.choice([-1,0,1]);
  L(a,[(x-1,y+1),(x+1,y-1),(x+4,y+1)],border)
  R(a,(x,y,x+3,y+2),mid)
  put(a,x+1,y,shiny);put(a,x+2,y+1,darken(mid,.78))
  if j%2==0:put(a,x+4,y+3,mid)
 return a

def panel(seed,base,iron,rim,rust=None,mode='quad'):
 a=img(base);d=ImageDraw.Draw(a);rng=random.Random(seed)
 if mode=='quad':
  for x in (0,16):R(a,(x,0,x,31),iron);R(a,(x+1,0,x+1,31),rim)
  for y in (0,16):R(a,(0,y,31,y),iron);R(a,(0,y+1,31,y+1),rim)
  for px in (5,27):
   for py in (5,21):
    R(a,(px-1,py-1,px+1,py+1),iron);put(a,px-1,py-1,rim);put(a,px+1,py+1,darken(iron,.67))
 elif mode=='bands':
  for x in (0,8,16,24):R(a,(x,0,x,31),iron);R(a,(x+1,1,x+1,30),rim)
  for y in (0,16):R(a,(0,y,31,y),iron)
  for x in (5,13,21,29):put(a,x,5,rim);put(a,x,24,iron)
 elif mode=='cross':
  R(a,(0,15,31,17),iron);R(a,(15,0,17,31),iron)
  L(a,[(1,14),(30,14)],rim);L(a,[(14,1),(14,30)],rim)
  for x in (3,27):
   for y in (3,27):put(a,x,y,rim);put(a,x+1,y+1,iron)
 if rust:
  for k in range(20):
   x,y=rng.randrange(32),rng.randrange(32)
   if (x%16<=3 or y%16<=2) or k<7:
    col=rng.choice(rust);put(a,x,y,col)
    if k%4==0:put(a,x+1,y,col)
 for i in range(12):
  x,y=rng.randrange(32),rng.randrange(32)
  put(a,x,y,darken(base,rng.choice([.78,1.12])))
 return a

def frame(seed,base,trim,shadow,detail):
 rng=random.Random(seed);a=stone(seed,base,rough=.4)
 for i in range(3):
  p=i;col=[shadow,trim,base][i];L(a,[(p,p),(31-p,p),(31-p,31-p),(p,31-p),(p,p)],col)
 for y in (4,27):L(a,[(3,y),(28,y)],trim)
 for x in (4,27):L(a,[(x,3),(x,28)],trim)
 for x in (5,26):
  for y in (5,26):put(a,x,y,detail)
 return a

B={}
# Unique field-pattern silhouettes aligned with Minecraft 1.21 tuff/bricks family semantics
B['soot_blackened_brick']=modular_bricks(211,'#282827','#504B49',16,8,8,damage=3)
B['sewer_brickwork']=modular_bricks(212,'#393E39','#6E766E',16,8,8,damage=3)
B['seepage_brick']=modular_bricks(213,'#263C3B','#537873',16,8,8,damage=4)
for x,y in [(2,2),(21,4),(7,19),(23,26)]:
 for dx,dy in [(0,0),(0,1),(1,1),(0,2)]:put(B['seepage_brick'],x+dx,y+dy,'#294C48')
B['patchwork_masonry']=modular_bricks(214,'#363836','#918A7C',16,8,8,damage=3)
for x,y in [(5,3),(20,11),(9,19),(27,28)]:L(B['patchwork_masonry'],[(x,y),(x+2,y)],'#575952')
B['galvanized_plating']=panel(215,'#859194','#4A5A5E','#C3C9C2',mode='quad')
B['rusted_plating']=panel(216,'#776E66','#474642','#A6A398',['#B96F45','#A45637','#73483A'],'quad')
B['rusted_industrial_sheet']=panel(217,'#655952','#3B4141','#9D9080',['#A4593A','#A97049','#6B4B37'],'bands')
B['chitin_plating']=panel(218,'#53665B','#334740','#98AB98',mode='cross')
# three visually related, materially distinct stones
B['limestone']=stone(219,'#B0AD98',.52,True)
B['smoke_stained_concrete']=stone(220,'#777875',.74,True)
for x,y in [(3,11),(4,12),(24,0),(25,1),(7,25),(8,26),(12,30)]:put(B['smoke_stained_concrete'],x,y,'#353C3A')
B['water_damaged_plaster']=stone(221,'#C2BDAB',.43,True)
for x,y in [(6,0),(7,3),(22,0),(23,5),(5,24),(20,24)]:L(B['water_damaged_plaster'],[(x,y),(x,y+6)],'#728881')
B['hell_rock']=stone(222,'#62474E',.84,True)
for pts in [[(1,7),(4,8),(7,6)],[(21,30),(23,23),(29,19)]]:L(B['hell_rock'],pts,'#AC645D',1)
B['ash_soil']=stone(223,'#635F5A',1.05,False)
for x,y in [(6,11),(19,22),(24,4)]:R(B['ash_soil'],(x,y,x+2,y+1),'#33383B')
B['industrial_debris']=stone(224,'#625F5A',.38,False)
for pts,clr in [([(0,3),(6,1),(10,6),(8,10),(1,8)],'#3D5458'), ([(17,15),(22,11),(31,17),(29,20),(19,20)],'#B19B85'), ([(5,25),(11,22),(15,27),(12,31),(6,30)],'#856144')]:ImageDraw.Draw(B['industrial_debris']).polygon(pts,fill=C(clr))
B['oil_shale_ore']=mineral('oil',225,'#535A57','#343F42','#313A3A','#73817E')
B['zinc_ore']=mineral('zinc',226,'#696F6A','#4D5A60','#A8C7C9','#E0EEDE')
B['rock_salt_ore']=mineral('salt',227,'#80827C','#5D6768','#D3DCCF','#FFFFFF')
B['sulfur_ore']=mineral('sulfur',228,'#777567','#66634C','#DDC457','#FFF2AB')
# styled flat functional textures for mechanisms
for name,seed,base,trim,shadow,detail in [
 ('generated_door_frame',229,'#75716B','#B1A38E','#3D4345','#D0BA91'),('reinforced_door_frame',230,'#55595A','#A69B88','#313B3E','#D3C29F'),('generated_gate_stone',231,'#73716A','#9E9889','#424849','#D4C4AA'),('etched_gate_stone',232,'#777168','#ADA58D','#42494A','#D5B98B'),('generated_artificial_core',233,'#77716C','#B2A68A','#3E4B4B','#8ED6CF'),('artificial_door_core',234,'#77716C','#B2A68A','#3E4B4B','#8ED6CF'),('generated_infernal_core',235,'#63575A','#B2A68A','#393C3E','#BF6B67'),('infernal_door_core',236,'#63575A','#B2A68A','#393C3E','#BF6B67')]:
 a=frame(seed,base,trim,shadow,detail)
 if 'core' in name:
  d=ImageDraw.Draw(a);R(a,(8,8,23,23),'#292F30');d.ellipse((9,8,23,24),fill=C(detail),outline=C('#262B2D'),width=2)
  d.polygon([(16,10),(22,17),(16,22),(11,17)],fill=C('#353A3A'))
  d.polygon([(16,11),(21,17),(16,21),(12,17)],fill=C(detail))
  L(a,[(16,11),(13,17),(16,20)],'#E7D9B0')
 else:
  R(a,(9,9,22,22),darken(base,.69));L(a,[(8,12),(13,8),(19,8)],trim)
  for p in range(4):put(a,12+p*2,18-p,detail)
 B[name]=a
# Lamp cage defined by texture models; keep crisp glass and warm emissive spots
B['cavern_lamp_frame']=panel(237,'#494841','#242E32','#B9A57E',mode='bands')
for name,lit in [('cavern_lamp',False),('cavern_lamp_glow',True)]:
 a=img('#3D4441');R(a,(3,2,28,29),'#333C3B');R(a,(6,5,25,26),'#A98D5D' if not lit else '#FFCE74')
 R(a,(9,7,22,24),'#D6B77A' if not lit else '#FFE39E')
 L(a,[(7,5),(7,26)],'#DED1B0',2);L(a,[(24,5),(24,26)],'#695D4F',2)
 L(a,[(2,4),(29,4)],'#B9AA8B',2);L(a,[(2,27),(29,27)],'#4A493D',2)
 L(a,[(15,5),(15,26)],'#695744',2);L(a,[(7,15),(24,15)],'#816849',2)
 if lit:
  R(a,(11,9,13,12),'#FFF4CF');L(a,[(12,11),(20,24)],'#FFF2C7')
 B[name]=a
# translucent smoke surfaces: no opaque fill
for name,base,hi in [('portal_surface',(18,27,31,160),(132,160,163,200)),('condensed_smoke_barrier',(26,35,38,130),(101,132,133,175))]:
 a=Image.new('RGBA',(32,32),base);d=ImageDraw.Draw(a)
 for pts in [[(2,31),(6,25),(5,20),(13,12),(10,3)],[(21,31),(23,25),(19,20),(28,12),(29,0)],[(0,13),(8,14),(14,19),(21,17),(31,20)]]:
  d.line(pts,fill=hi,width=1)
 for x,y in [(4,4),(18,26),(25,7),(30,30)]: d.rectangle((x,y,x+2,y+1),fill=(56,67,70,85))
 B[name]=a
assert len(B)==31,len(B)
for name,a in B.items():a.save(ROOT/(name+'.png'),optimize=True)
print('blocks:',len(B))
