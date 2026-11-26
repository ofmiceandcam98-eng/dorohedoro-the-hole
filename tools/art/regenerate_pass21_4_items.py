from PIL import Image,ImageDraw
from pathlib import Path
import random, math, json
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/dorohedoro/textures/item'
MOD=ROOT.parent.parent/'models/item'
ROOT.mkdir(parents=True,exist_ok=True)
def C(s):return tuple(bytes.fromhex(s.lstrip('#'))) + (255,)
def canvas(s=32):return Image.new('RGBA',(s,s),(0,0,0,0))
def D(a):return ImageDraw.Draw(a)
def poly(a,pts,c):D(a).polygon(pts,fill=C(c))
def rect(a,box,c):D(a).rectangle(box,fill=C(c))
def line(a,pts,c,w=1):D(a).line(pts,fill=C(c),width=w)
def dot(a,x,y,c):D(a).point((x,y),fill=C(c))
def ell(a,box,c):D(a).ellipse(box,fill=C(c))
def drawbright(a,pts,base,shadow,glint):poly(a,[(x+1,y+1) for x,y in pts],shadow);poly(a,pts,base);line(a,pts[:2],glint)
ITEM={}
# Consistent directed light from top left; blackening of glass is confined to inner smoke, not the entire container.
FLASKS={
'empty':('#859EA0','#AEC9C5','#D5E6DC','empty'),
'mushroom':('#7C845B','#D3CB8D','#F6EDB7','fungus'),
'reptile':('#456E56','#82CA8C','#C9E0A3','scale'),
'dismemberment':('#714C5C','#E8A8AF','#FCE3C8','slash'),
'healing':('#B5AB8B','#E6D6A4','#FCF4C7','cross'),
'curse':('#644A74','#A989C9','#DAB9EE','curse'),
'protection':('#466B6E','#91C7CD','#E1F5D6','shield'),
'destruction':('#8D543D','#E9A563','#FFF0B3','blast'),
'time':('#71654C','#E2C17D','#FAE8B5','dial')}
def flask(typ):
 bottom,mid,bright,symbol=FLASKS[typ];a=canvas()
 # bottle silhouette: shoulders, thick rim, recognisable stoppered neck
 poly(a,[(11,5),(21,5),(21,9),(25,12),(26,17),(26,26),(23,29),(9,29),(6,26),(6,17),(7,12),(11,9)],'#202D30')
 poly(a,[(11,11),(21,11),(24,15),(24,26),(22,27),(10,27),(8,25),(8,16)],'#719095')
 poly(a,[(10,18),(23,18),(23,25),(21,27),(10,27),(9,25)],'#253237')
 # colored smoke with layered glow, distinctive to each type
 if typ!='empty':
  poly(a,[(11,22),(14,19),(17,22),(21,20),(22,25),(19,26),(12,26)],bottom)
  line(a,[(11,24),(14,22),(16,23),(19,21)],mid,2)
  dot(a,12,20,mid);dot(a,21,24,mid)
 else:
  line(a,[(13,25),(18,24),(20,25)],'#B7CDCA')
 # engraved type glyph sitting in the actual suspended smoke rather than colored flat bottle label
 if symbol=='fungus':
  ell(a,(12,19,20,22),bright);rect(a,(15,22,16,25),'#E9E4C2');dot(a,14,20,'#80734F')
 elif symbol=='scale':
  for x,y in [(12,20),(17,20),(15,23)]:poly(a,[(x,y+1),(x+2,y-1),(x+4,y+1),(x+2,y+3)],bright)
 elif symbol=='slash':
  line(a,[(12,25),(20,18)],bright,2);dot(a,14,20,'#FFF1CF')
 elif symbol=='cross':
  rect(a,(15,19,17,26),bright);rect(a,(12,21,20,23),bright)
 elif symbol=='curse':
  line(a,[(12,20),(17,19),(16,23),(20,24),(18,26)],bright,2)
 elif symbol=='shield':
  poly(a,[(16,18),(21,20),(20,24),(16,27),(12,24),(11,20)],bright)
  line(a,[(16,20),(16,25)],'#4D7078')
 elif symbol=='blast':
  poly(a,[(16,18),(18,22),(22,21),(19,24),(20,27),(16,25),(13,27),(14,23),(11,22)],bright)
 elif symbol=='dial':
  ell(a,(12,18,20,26),bright);ell(a,(14,20,18,24),'#5A5047');line(a,[(16,20),(16,22),(19,23)],'#FCEDCC')
 # layered top lip + cork; readable shape at 16px
 rect(a,(12,6,20,10),'#2B3A3A');rect(a,(13,3,19,7),'#98795B');line(a,[(13,3),(19,3)],'#E1C99A',2)
 rect(a,(11,8,21,9),'#A1AFA9');line(a,[(10,12),(9,16),(9,21)],'#E8F6E7',2)
 line(a,[(22,14),(23,18)],'#BDD9CD');line(a,[(11,27),(21,27)],'#3F5658')
 dot(a,12,14,'#D1E8DA');dot(a,10,24,'#BBD2CA')
 return a
for t in FLASKS:ITEM['empty_smoke_flask' if t=='empty' else f'{t}_smoke_flask']=flask(t)

def case():
 a=canvas();poly(a,[(2,14),(6,11),(26,11),(30,14),(30,25),(27,28),(5,28),(2,25)],'#2F3637')
 poly(a,[(4,15),(28,15),(28,24),(25,26),(5,26)],'#9D907B')
 poly(a,[(5,14),(26,14),(25,17),(6,17)],'#E6D2A8')
 rect(a,(11,9,21,12),'#3F4A47');rect(a,(13,10,19,11),'#D1BDA0')
 rect(a,(7,18,24,24),'#D6C8AB');line(a,[(8,19),(23,19)],'#F4E5C7')
 rect(a,(14,17,17,25),'#9B4840');rect(a,(11,20,21,22),'#9B4840')
 for x in (6,25):rect(a,(x,25,x+2,28),'#584F47');dot(a,x,25,'#DCCDAF')
 rect(a,(3,18,5,21),'#5A5C52');rect(a,(27,18,29,21),'#5A5C52')
 return a
ITEM['surgical_reattachment_kit']=case()

def medical(st):
 a=canvas();r=st
 if st in ('antidote_poultice','medical_salve','sewer_sealant'):
  # wrapped treatment jar with distinguishable content and white bandage strip
  fill={'antidote_poultice':'#8CA879','medical_salve':'#D1C19C','sewer_sealant':'#758F89'}[st]
  poly(a,[(8,13),(24,13),(27,16),(27,26),(23,29),(9,29),(5,26),(5,17)],'#344044')
  rect(a,(8,14,24,25),fill);line(a,[(8,14),(23,14)],'#E2D5AF',2)
  rect(a,(9,7,23,13),'#6F6254');rect(a,(11,5,21,9),'#C3AD88')
  rect(a,(11,19,22,23),'#E0D4B8');rect(a,(16,17,17,25),'#8C665A')
  if st=='antidote_poultice':poly(a,[(13,19),(15,17),(18,20),(21,18),(19,24),(15,24)],'#72986D')
  if st=='sewer_sealant':rect(a,(12,22,21,24),'#455E60')
 else:
  fill={'glow_tonic':'#A1D9B0','leech_mucus':'#829E74'}[st]
  poly(a,[(12,5),(20,5),(20,11),(24,15),(24,26),(21,28),(9,28),(7,25),(8,15),(12,11)],'#29383B')
  poly(a,[(10,16),(22,16),(22,25),(20,26),(11,26),(9,24)],fill)
  rect(a,(12,4,20,7),'#9D8060');rect(a,(10,12,22,14),'#8EA9A0')
  line(a,[(11,18),(11,22)],'#E5EEDF',2)
  for x,y in [(15,23),(18,19)]:dot(a,x,y,'#D7ECD0')
 return a
for name in ('antidote_poultice','medical_salve','sewer_sealant','glow_tonic','leech_mucus'):ITEM[name]=medical(name)

def ingot(kind):
 a=canvas();base,light,dark=({'brass':('#BA8A49','#F6D79A','#725132'), 'zinc':('#A3BEB9','#F1F6DD','#647C80')}[kind]);
 poly(a,[(5,16),(9,11),(25,11),(29,16),(26,23),(8,23)],dark)
 poly(a,[(7,16),(11,12),(24,12),(27,16),(24,20),(9,20)],base)
 poly(a,[(11,12),(24,12),(27,16),(8,16)],light)
 line(a,[(9,21),(25,21)],dark,2)
 line(a,[(10,14),(22,14)],'#FFF2CB' if kind=='brass' else '#F3F7EC')
 dot(a,23,18,light);dot(a,12,19,light)
 return a
ITEM['brass_ingot']=ingot('brass');ITEM['zinc_ingot']=ingot('zinc')

def fragment(name):
 # Distinct resource silhouettes drawn from their stated material (not identical blob recolors)
 a=canvas();d=D(a)
 if name in ('raw_sulfur','raw_zinc','rock_salt','lime_powder','oil_shale_chunk','smoke_residue'):
  color={
  'raw_sulfur':['#7C6733','#D6AE49','#FFEB88'], 'raw_zinc':['#60797B','#B2D5CA','#F2FFF0'],
  'rock_salt':['#6F9090','#CBE4DE','#FCFCF1'], 'lime_powder':['#7A8174','#D8D8BC','#F2EEDF'],
  'oil_shale_chunk':['#303D40','#59686B','#899394'], 'smoke_residue':['#273037','#435058','#6B7171']}[name]
  if name in ('lime_powder','smoke_residue'):
   pts=[(3,23),(8,18),(13,20),(18,15),(24,17),(29,24),(26,27),(4,28)]
   poly(a,pts,color[0]);poly(a,[(6,24),(12,20),(17,22),(23,21),(26,24),(22,25),(7,26)],color[1]);dot(a,13,21,color[2]);dot(a,21,20,color[2])
  else:
   for x,y,size in [(5,16,11),(17,8,12),(17,21,8)]:
    poly(a,[(x,y+size//2),(x+size//3,y),(x+size-1,y+2),(x+size,y+size-3),(x+size//2,y+size)],color[0])
    poly(a,[(x+size//3,y+1),(x+size-2,y+3),(x+size-3,y+size//2),(x+1,y+size//2)],color[1])
    line(a,[(x+size//3,y+2),(x+size-3,y+3)],color[2])
  return a
 if name in ('bitumen','coagulated_tar'):
  poly(a,[(4,21),(7,15),(13,13),(17,10),(25,14),(29,20),(25,26),(8,27)],'#252D2E')
  poly(a,[(7,20),(12,16),(19,16),(24,18),(24,24),(11,25)],'#393E3D')
  line(a,[(9,17),(14,14),(20,15)],'#6D7771')
  if name=='coagulated_tar':
   ell(a,(16,11,24,17),'#51473E');line(a,[(19,13),(22,14)],'#857362')
  return a
 if name in ('scrap_metal','metallic_carapace','luminous_scale','reinforced_chitin','dense_hide','fungal_membrane'):
  col={'scrap_metal':('#44565B','#94B2AB','#E3C9A5'), 'metallic_carapace':('#45594B','#849E86','#D6CBA1'),
       'luminous_scale':('#3B7477','#78CED1','#CFFFE9'), 'reinforced_chitin':('#3E4F46','#839783','#CFD4AD'),
       'dense_hide':('#625C4D','#96836A','#CDB99A'),'fungal_membrane':('#67765A','#9CA980','#DBD2AC')}[name]
  poly(a,[(2,15),(11,8),(23,10),(30,17),(22,28),(10,26)],'#263234')
  poly(a,[(5,16),(12,10),(22,11),(27,18),(21,26),(11,23)],col[0])
  poly(a,[(7,15),(13,11),(19,12),(24,17),(17,17),(10,21)],col[1])
  line(a,[(8,15),(15,12),(20,14)],col[2])
  if name in ('luminous_scale','metallic_carapace','reinforced_chitin'):
   line(a,[(16,13),(16,23)],'#324B4C',2);line(a,[(16,18),(10,20)],col[2])
  if name=='fungal_membrane':
   for x,y in [(12,16),(22,20),(9,22)]:dot(a,x,y,'#D9D5B2')
  return a
 if name in ('rat_fur','moth_silk','roach_antenna','leech_sucker','leech_mucus','burrower_claw','corrosive_gland','sludge_gland','spore_sac'):
  if name=='rat_fur':
   for points in [[(6,25),(9,11),(11,23),(15,6),(17,25),(21,10),(24,25)]]:poly(a,points,'#8A8279')
   line(a,[(10,15),(8,24)],'#D3BDA0',2);line(a,[(15,9),(15,24)],'#B2A092',2)
  elif name=='moth_silk':
   for off in range(0,5):line(a,[(8+off*3,8),(6+off*3,16),(8+off*3,25)],'#E7DEC4' if off%2 else '#A99F86',2)
   line(a,[(7,18),(24,20)],'#C9C4AA')
  elif name=='roach_antenna':
   for ox in (3,17):line(a,[(ox+5,27),(ox+3,18),(ox+6,10),(ox+10,5)],'#AF8C68',2);dot(a,ox+10,5,'#DBC19D')
  elif name=='burrower_claw':
   poly(a,[(4,26),(7,19),(17,6),(25,2),(20,14),(11,24)],'#5B574A')
   poly(a,[(7,25),(13,17),(20,9),(23,4),(21,12),(11,26)],'#D5C9AB');line(a,[(13,18),(20,7)],'#F1E8CC',2)
  elif name=='leech_sucker':
   ell(a,(4,8,28,28),'#354C47');ell(a,(8,11,26,25),'#829D83');ell(a,(12,14,22,22),'#2E3F3D');dot(a,12,13,'#C0CBA6')
  elif name in ('corrosive_gland','sludge_gland','spore_sac'):
   cols={'corrosive_gland':('#4D713C','#9ECD6B','#DEEA7D'), 'sludge_gland':('#354C40','#708E69','#A6AF7F'), 'spore_sac':('#525C34','#A3AC57','#E6E7AA')}[name]
   if name=='spore_sac':
    for x,y,r in [(10,14,6),(20,12,6),(15,23,7)]:ell(a,(x-r,y-r,x+r,y+r),cols[0]);ell(a,(x-r+2,y-r+1,x+r-2,y+r-3),cols[1]);dot(a,x-1,y-3,cols[2])
   else:
    poly(a,[(7,23),(5,16),(10,8),(20,7),(27,15),(25,24),(18,29),(10,27)],cols[0]);ell(a,(10,10,24,23),cols[1]);dot(a,13,11,cols[2]);dot(a,18,14,cols[2])
  return a
 if name=='devil_sigil':
  ell(a,(3,3,29,29),'#2B3536');ell(a,(5,5,27,27),'#AA8A5E');ell(a,(8,8,24,24),'#383733')
  poly(a,[(16,7),(23,16),(16,25),(9,16)],'#C6AA78');poly(a,[(16,10),(21,16),(16,22),(12,16)],'#735E4B')
  line(a,[(8,8),(12,5)],'#F7DDA0');line(a,[(20,27),(24,24)],'#F7DDA0')
  return a
 return None

for name in ('raw_sulfur','raw_zinc','rock_salt','lime_powder','oil_shale_chunk','smoke_residue','bitumen','coagulated_tar','scrap_metal','metallic_carapace','luminous_scale','reinforced_chitin','dense_hide','fungal_membrane','rat_fur','moth_silk','roach_antenna','leech_sucker','burrower_claw','corrosive_gland','sludge_gland','spore_sac','devil_sigil'):
 ITEM[name]=fragment(name)

def tool(name):
 a=canvas()
 if name in ('door_activation_tool','door_linking_key','ritual_tool','sewer_echo_probe','preserved_smoke_conduit','smoke_conduit'):
  if name in ('smoke_conduit','preserved_smoke_conduit'):
   poly(a,[(4,18),(12,11),(21,15),(28,9),(29,15),(22,20),(12,17),(7,25)],'#334343')
   line(a,[(7,20),(12,14),(21,18),(27,13)],'#91A09A',3)
   line(a,[(10,21),(14,19)],'#D5C3A0')
  else:
   line(a,[(6,27),(23,8)],'#2E3536',6);line(a,[(6,27),(23,8)],'#B49C70',3)
   if name=='sewer_echo_probe':
    ell(a,(18,4,29,15),'#33454A');ell(a,(20,6,27,13),'#93CDD0');dot(a,21,7,'#E9F4DD')
   else:
    ell(a,(16,2,29,16),'#374243');ell(a,(19,4,27,13),'#B4A57C');ell(a,(22,7,25,11),'#445253')
    if name=='ritual_tool':line(a,[(17,13),(27,18)],'#AD644D',2)
    if name=='door_linking_key':rect(a,(6,23,8,28),'#6B5D4D')
   dot(a,5,28,'#E1C8A1')
  return a
 if name=='smoke_condenser':
  poly(a,[(4,16),(7,12),(25,12),(28,17),(28,27),(25,29),(6,29),(4,26)],'#2E3E41')
  rect(a,(7,17,25,26),'#627A76');rect(a,(8,23,24,25),'#AD956E')
  ell(a,(9,5,23,19),'#AF9A71');ell(a,(12,8,20,16),'#2B5255');ell(a,(14,9,18,13),'#B8E1D0')
  line(a,[(10,8),(12,5),(20,5)],'#DED1A4',2)
  for x in (8,22):rect(a,(x,27,x+2,30),'#9D8969')
  return a
 if name=='smoke_soaked_filter':
  poly(a,[(3,12),(11,7),(26,9),(29,13),(28,27),(7,27),(3,23)],'#343C3C')
  rect(a,(7,13,25,24),'#79867A')
  for x in (10,14,18,22):line(a,[(x,12),(x,26)],'#B7BBA5',2)
  line(a,[(7,11),(26,11)],'#C4A984')
  return a
 return None
for name in ('door_activation_tool','door_linking_key','ritual_tool','sewer_echo_probe','preserved_smoke_conduit','smoke_conduit','smoke_condenser','smoke_soaked_filter'):
 ITEM[name]=tool(name)

def food(name):
 a=canvas();color={'raw_rat_meat':('#B27778','#DB9890','#6E494F'), 'cooked_rat_meat':('#965C37','#D59558','#4B3D34'),'salted_rat_ration':('#BFA17B','#E3CB9E','#6B5F52')}[name]
 base,light,dark=color
 poly(a,[(3,19),(7,12),(15,10),(21,13),(26,11),(30,17),(26,25),(17,28),(8,26)],dark)
 poly(a,[(6,19),(10,14),(16,12),(22,16),(27,14),(28,20),(24,24),(16,25),(9,23)],base)
 line(a,[(8,17),(15,14),(20,16)],light,2)
 if name=='salted_rat_ration':
  for x,y in [(12,22),(19,18),(24,21)]:dot(a,x,y,'#F4E9BF')
 if name=='cooked_rat_meat':
  for x,y in [(14,19),(21,21)]:line(a,[(x,y),(x+3,y+1)],'#60412C',2)
 return a
for name in ('raw_rat_meat','cooked_rat_meat','salted_rat_ration'):ITEM[name]=food(name)

# 64x64 UV atlas. Every face used by the 3D model has intentional anatomy/cooking material.
# Vanilla block models use 0..16 UV logical coordinates, so atlas is 4 physical pixels per unit.
def limb_atlas(name):
 cooked=name.startswith('cooked');leg=name.endswith('leg');right='right' in name
 a=canvas(64);d=D(a)
 cloth=('#686E5D' if right else '#666F6B') if not cooked else '#AA6740'
 clothlt=('#A5AE98' if right else '#ABB5A7') if not cooked else '#E5AA69'
 clothdk=('#394A43' if right else '#3E4D4A') if not cooked else '#75442F'
 skin= '#BAA38A' if not cooked else '#B97C4A'
 sklt= '#E8CEAA' if not cooked else '#F2B87C'
 skdk= '#7D7564' if not cooked else '#74432E'
 outline='#2D3C3B' if not cooked else '#613D30'
 rng=random.Random(231 if leg else 228+(1 if right else 0)+(4 if cooked else 0))
 for seg in range(3):
  yy=8+seg*16
  for side in range(3):
   xx=side*16
   material=(cloth if seg<(2 if leg else 1) else skin)
   matlt=(clothlt if seg<(2 if leg else 1) else sklt)
   matdk=(clothdk if seg<(2 if leg else 1) else skdk)
   rect(a,(xx,yy,xx+15,yy+15),outline)
   rect(a,(xx+1,yy+1,xx+14,yy+14),material)
   # left-lit face, right-side depth, lower edge shadow
   line(a,[(xx+2,yy+1),(xx+13,yy+1)],matlt,2)
   rect(a,(xx+13,yy+3,xx+14,yy+14),matdk)
   line(a,[(xx+2,yy+13),(xx+13,yy+13)],matdk)
   if seg==0:  # upper end / sleeve
    rect(a,(xx+3,yy+4,xx+4,yy+11),matdk)
    if not cooked:
     line(a,[(xx+5,yy+6),(xx+10,yy+6)],'#C4BD9E')
     dot(a,xx+10,yy+8,'#D4BFA0')
    else:
     line(a,[(xx+5,yy+5),(xx+11,yy+7)],'#6F442C',2)
   if seg==1:  # forearm or trouser knee panel
    line(a,[(xx+2,yy+4),(xx+12,yy+4)],matdk)
    line(a,[(xx+3,yy+5),(xx+11,yy+5)],matlt)
    if not cooked:
     line(a,[(xx+4,yy+11),(xx+10,yy+11)],'#C0AF8D')
     dot(a,xx+5,yy+11,'#383D3B')
    else:
     for k in (6,10):line(a,[(xx+k,yy+6),(xx+k-2,yy+10)],'#7C422A')
   if seg==2:
    if leg and not cooked: # boot toe
     rect(a,(xx+1,yy+9,xx+14,yy+14),'#3C4743')
     line(a,[(xx+2,yy+10),(xx+12,yy+10)],'#9F9A84')
     rect(a,(xx+2,yy+12,xx+13,yy+13),'#262D30')
    elif not cooked: # hand glove, palm line
     line(a,[(xx+5,yy+6),(xx+11,yy+6)],sklt,2)
     rect(a,(xx+2,yy+11,xx+13,yy+12),skdk)
     for x in (6,9,12):line(a,[(xx+x,yy+9),(xx+x,yy+12)],'#877D68')
    else:
     for k in (4,8,12):line(a,[(xx+k,yy+5),(xx+k-2,yy+8)],'#7D492D')
     dot(a,xx+10,yy+10,sklt)
   # restrained purposeful tiny wear details; no background filler noise
   if (seg+side)%2==0:dot(a,xx+6,yy+3,matlt)
 # end cap for UV 10..14 x 0..4 (physical 40..56 x 0..16)
 rect(a,(40,0,55,15),outline);rect(a,(42,2,53,13),skdk if cooked else '#746E5D')
 line(a,[(43,3),(52,3)],sklt)
 rect(a,(0,0,15,15),outline);rect(a,(2,2,13,13),skdk)
 line(a,[(3,3),(12,3)],sklt)
 # side cap at 0..16 when down
 return a
for cooked in (False,True):
 for side in ('left','right'):
  for limb in ('arm','leg'):
   name=('cooked' if cooked else 'severed')+f'_{side}_{limb}'
   ITEM[name]=limb_atlas(name)

# Preserve the old item IDs. Redraw all 59 item texture files, not just selected icons.
missing=sorted({p.stem for p in ROOT.glob('*.png')}-set(ITEM))
assert not missing,missing
assert len(ITEM)==59,len(ITEM)
for name,a in ITEM.items():
 a.save(ROOT/(name+'.png'),optimize=True)
print('items:',len(ITEM))
