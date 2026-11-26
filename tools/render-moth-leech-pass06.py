"""Offline stills and animated GIF from authored Minecraft cuboids and UV colors, using actual wing pivots/phase code."""
import math,json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[1]
GEO=ROOT/'docs/model_geometry'
TEX=ROOT/'src/main/resources/assets/dorohedoro/textures/entity'
FONT='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
BOLD='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'

def fnt(size,bold=False):return ImageFont.truetype(BOLD if bold else FONT,size)

def project(p):
 x,y,z=p
 return (x*.98+z*.63, y*1.22+(x-z)*.38)

def transform(p,gr,m,time=0.0,mode='rest'):
 pv=m.get('part_pivots',{}).get(gr)
 if not pv:return p
 left=gr.startswith('left')
 fore='hindwing' not in gr
 if mode=='rest':zr=(1.04 if fore else .88);yr=.03
 else:
  phase=1.25*time - (0.34 if not fore else 0.0)
  zr=(.44 if fore else .38)+math.sin(phase)*(.69 if fore else .56)
  yr=(.05 if fore else .03)+math.sin(phase)*(.06 if fore else .04)
 if not left:zr=-zr;yr=-yr
 x,y,z=(p[i]-pv[i] for i in range(3))
 x1=x*math.cos(zr)-y*math.sin(zr)
 y1=x*math.sin(zr)+y*math.cos(zr)
 z1=z
 x2=x1*math.cos(yr)+z1*math.sin(yr)
 z2=-x1*math.sin(yr)+z1*math.cos(yr)
 return (x2+pv[0],y1+pv[1],z2+pv[2])

def geometry(mob,t=0,mode='rest'):
 m=json.loads((GEO/(mob+'.json')).read_text());tex=Image.open(TEX/(mob+'.png')).convert('RGB')
 out=[]
 for gr,cs in m['groups'].items():
  for c in cs:
   x,y,z=c['xyz'];w,h,d=c['dim'];u,v=c['uv'];dw,dh,dd=math.ceil(w),math.ceil(h),math.ceil(d)
   faces=[('top',[(x,y,z),(x+w,y,z),(x+w,y,z+d),(x,y,z+d)],u+dd,v,dw,dd),
       ('front',[(x,y,z),(x+w,y,z),(x+w,y+h,z),(x,y+h,z)],u+dd,v+dd,dw,dh),
       ('right',[(x+w,y,z),(x+w,y,z+d),(x+w,y+h,z+d),(x+w,y+h,z)],u+dd+dw,v+dd,dd,dh)]
   for side,pts,uu,vv,sw,sh in faces:
    poly=[transform(pt,gr,m,t,mode) for pt in pts]
    depth=sum(pt[0]-pt[2]-.7*pt[1] for pt in poly)/4
    # Tiny face sampling from actual original UV atlas and per-face shading
    cx=min(tex.width-1,int(uu+sw*.51));cy=min(tex.height-1,int(vv+sh*.45))
    rgb=tex.getpixel((cx,cy));sha={'top':1.13,'front':.98,'right':.78}[side]
    rgb=tuple(max(0,min(255,int(v*sha))) for v in rgb)
    out.append((depth,poly,rgb))
 return sorted(out,key=lambda x:x[0])

def render(mob,t=0,mode='rest',size=(590,410)):
 faces=geometry(mob,t,mode)
 coords=[project(v) for _,poly,_ in faces for v in poly]
 xmin=min(p[0] for p in coords);xmax=max(p[0] for p in coords)
 ymin=min(p[1] for p in coords);ymax=max(p[1] for p in coords)
 W,H=size; scale=min((W-70)/max(1,xmax-xmin),(H-60)/max(1,ymax-ymin));sx=(W-scale*(xmax-xmin))/2-xmin*scale;sy=(H-scale*(ymax-ymin))/2-ymin*scale
 img=Image.new('RGBA',size,(0,0,0,0));dr=ImageDraw.Draw(img)
 for _,poly,rgb in faces:
  coords2=[(round(project(p)[0]*scale+sx),round(project(p)[1]*scale+sy)) for p in poly]
  dr.polygon(coords2,fill=rgb+(255,))
  dr.line(coords2+[coords2[0]],fill=(20,26,24,180),width=1)
 return img

def bg(W,H):
 img=Image.new('RGB',(W,H),(13,23,24));d=ImageDraw.Draw(img)
 for x in range(0,W,37):d.line((x,0,x,H),fill=(23,34,34),width=1)
 for y in range(0,H,37):d.line((0,y,W,y),fill=(23,34,34),width=1)
 return img

def frame_moth(t,mode='fly',size=(610,420)):
 base=Image.new('RGBA',size,(17,28,29,255))
 view=render('glow_moth',t,mode,(size[0]-25,size[1]-25));base.alpha_composite(view,(12,4));return base.convert('RGB')

def sheet():
 W,H=1820,1060;image=bg(W,H);d=ImageDraw.Draw(image)
 d.text((31,20),'DOROHEDORO / THE HOLE',font=fnt(43,True),fill=(240,224,194))
 d.text((37,78),'ACTUAL MODEL SOURCE · LEANER LEECH MOUTH + HINGED MOTH FLIGHT',font=fnt(19),fill=(156,187,170))
 def panel(x,y,w,h,title,desc,mob,t=0,mode='rest'):
  d.rectangle((x,y,x+w,y+h),fill=(19,30,31),outline=(91,113,104),width=2)
  v=render(mob,t,mode,(w-16,h-95));image.paste(v,(x+8,y+7),v)
  d.line((x+12,y+h-88,x+w-12,y+h-88),fill=(87,111,106),width=2)
  d.text((x+19,y+h-81),title,font=fnt(25,True),fill=(239,226,198))
  d.text((x+20,y+h-42),desc,font=fnt(16),fill=(164,194,172))
 panel(24,132,874,420,'SEWER LEECH  ·  SIX TEETH','2 upper + 2 lower + 2 side-hooks; open sucker, no crowded rakers','sewer_leech')
 panel(921,132,875,420,'GLOW MOTH  ·  WINGS RAISED','Four moving parts, shoulder pivots; forewings lead hindwings','glow_moth',t=(math.pi/2)/1.25,mode='fly')
 panel(24,570,874,416,'GLOW MOTH  ·  DOWNSTROKE','Forewings lower without rotating through the body center','glow_moth',t=(3*math.pi/2)/1.25,mode='fly')
 panel(921,570,875,416,'GLOW MOTH  ·  RESTING','Wings fold upward and hold still rather than fluttering','glow_moth',mode='rest')
 d.text((32,H-35),'GEOMETRY + TEXTURE ATLAS DATA · THREE POSES OF ACTUAL JOINT ANIMATION · NOT GAMEPLAY SCREENSHOTS',font=fnt(15),fill=(158,170,163))
 out=str(ROOT/'docs/previews/moth_leech_pass06_source.png');image.save(out);print('Saved',out)

def gif():
 frames=[]
 for k in range(20):
  t=k*2*math.pi/20/1.25
  im=frame_moth(t,size=(580,410))
  d=ImageDraw.Draw(im)
  d.text((16,10),'GLOW MOTH   /   FLIGHT CYCLE',font=fnt(20,True),fill=(230,226,197))
  d.text((17,376),f'Frame {k+1:02d}/20  |  forewing + hindwing separately hinged',font=fnt(13),fill=(188,201,183))
  frames.append(im)
 out=str(ROOT/'docs/previews/glow_moth_pass06_flap.gif')
 frames[0].save(out,save_all=True,append_images=frames[1:],duration=90,loop=0,optimize=True)
 print('Saved',out)

if __name__=='__main__':
 sheet();gif()
