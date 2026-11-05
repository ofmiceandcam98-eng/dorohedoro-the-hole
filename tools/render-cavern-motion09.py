"""Render offline cuboid animation previews from the exact authored Java source geometry and textures.
Rotation curves mirror the Java model expressions in docs/model_geometry; previews are NOT game captures.
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path
import math,json
ROOT=Path(__file__).resolve().parents[1]
GEO=ROOT/'docs/model_geometry'; TEX=ROOT/'src/main/resources/assets/dorohedoro/textures/entity'
OUT=ROOT/'docs/previews/animation_pass09'; OUT.mkdir(parents=True,exist_ok=True)
FONT='/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'; BOLD='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
NAMES={'sewer_roach':'SEWER COCKROACH','scrap_rat':'SCRAP RAT','sewer_leech':'SEWER LEECH','glow_moth':'GLOW MOTH','sludge_crawler':'SLUDGE CRAWLER','rust_eater_beetle':'RUST-EATER BEETLE','cavern_burrower':'CAVERN BURROWER','spore_crawler':'SPORE CRAWLER'}

# Model render-space position is pre-origin; inherited groups have independent pivots.
def rotate(p,pv,ang):
 x,y,z=[p[i]-pv[i] for i in range(3)];rx,ry,rz=ang
 c=math.cos(rx);s=math.sin(rx);y,z=y*c-z*s,y*s+z*c
 c=math.cos(ry);s=math.sin(ry);x,z=x*c+z*s,-x*s+z*c
 c=math.cos(rz);s=math.sin(rz);x,y=x*c-y*s,x*s+y*c
 return (x+pv[0],y+pv[1],z+pv[2])

def pose(mob, group, t, stage):
 motion=0.87 if stage=='MOVE' else (0.15 if stage=='ATTACK' else 0)
 attack=0.8 if stage=='ATTACK' else 0
 limb=t*1.6;age=t*4.0
 x=y=z=0
 def legprefix():
  nonlocal x,y,z
  # tripod gait + paw pairs
  if group.startswith(('leg_','paw_')):
   side='l' if '_l_' in group else 'r'; i=int(group[-1]); phase=math.pi*(i%2 if mob in ['scrap_rat','cavern_burrower'] else i%2) + (math.pi if side=='r' else 0)
   if mob in ['sewer_roach','rust_eater_beetle','spore_crawler']:
    speed={'sewer_roach':2.15,'rust_eater_beetle':2.32,'spore_crawler':1.98}[mob]
    amp={'sewer_roach':.48,'rust_eater_beetle':.39,'spore_crawler':.43}[mob]
    z=(1 if side=='r' else -1)*math.sin(limb*speed+phase)*motion*amp
    x=math.cos(limb*speed+phase)*motion*.20
   else:
    speed=1.88 if mob=='scrap_rat' else 1.65
    x=math.sin(limb*speed+phase)*motion*.55
    if mob=='cavern_burrower' and stage=='ATTACK':x+=(-.83 if i==0 else .20)*attack
    if mob=='cavern_burrower':z+=math.sin(age*.75+phase)*attack*.27
 legprefix()
 if group.startswith('tail_'):
  i=int(group[-1]);y=math.sin(age*.17-i*.37)*(.08+i*.035)+math.sin(limb*.91-i*.45)*motion*.11
  x=math.sin(age*.11+i*.4)*.06
 if mob=='sewer_leech':
  if group.startswith('seg_'):
   i=int(group[-1]);y=math.sin(age*(.32 if stage=='MOVE' else .16)-i*.55)*(.038+i*.014+motion*.14)
   x=math.sin(age*.22-i*.42)*(.025+motion*.08)
  elif group=='mouth':x=math.sin(age*.21)*.12-attack*.55;y=math.sin(age*.13)*.04
 if mob=='glow_moth':
  if 'wing' in group:
   left=group.startswith('left');hind='hindwing' in group
   phase=age*1.25-(.34 if hind else 0)
   if stage=='IDLE':z=(.88 if hind else 1.04);y=.03;x=0
   else:
    z=(.38 if hind else .44)+math.sin(phase)*(.56 if hind else .69)
    y=(.03 if hind else .05)+math.sin(phase)*(.04 if hind else .06)
    x=math.sin(phase)*.08
   if not left:z=-z;y=-y
  if group=='body':x=math.sin(age*.24)*.025;y=math.sin(age*.16)*.055
  if group=='head':y=math.sin(age*.08)*.05
 if mob=='sewer_roach':
  if group.startswith('antenna_'):
   left=group.endswith('_l');y=(1 if left else -1)*math.sin(age*(.21 if left else .19)+ (0 if left else .8))*.30+(1 if left else -1)*attack*.34
  elif group=='head':x=-attack*.32;y=math.sin(age*.07)*.085
  elif group=='body':x=math.sin(limb*2.15)*motion*.045
 if mob=='scrap_rat':
  if group=='head':x=-.07+math.sin(age*.35)*(.10-motion*.07)-attack*.19;y=math.sin(age*.07)*.16*(1-motion*.65)
  elif group=='body':x=math.sin(limb*3.75)*motion*.07
 if mob=='sludge_crawler':
  if group=='body':x=math.sin(age*.24)*.07+motion*math.sin(limb*1.46)*.20;z=math.sin(age*.16)*.055
  elif group=='head':x=math.sin(age*.20)*.065-attack*.40;y=math.sin(age*.11)*.07
  elif group in ('tentacle_l','tentacle_r'):
   left=group.endswith('_l');x=math.sin(age*.35+(.3 if left else 2.8))*.17+(1 if left else -1)*math.sin(limb*1.80)*motion*.36-attack*.5
 if mob=='rust_eater_beetle':
  if group=='head':x=math.sin(age*.23)*.075-attack*.24;y=math.sin(age*.1)*.085
  if group=='body':z=math.sin(limb*2.32)*motion*.035
 if mob=='cavern_burrower':
  if group=='head':x=-attack*.3+math.sin(age*.07)*.045
  if group=='body':x=math.sin(limb*3.30)*motion*.045
 if mob=='spore_crawler':
  if group=='head':x=-attack*.33+math.sin(age*.12)*.08
  if group=='body':x=math.sin(age*.16)*.09+attack*.22;z=math.sin(age*.11)*.045
 return (x,y,z)

def pose_offset(mob, group, t, stage):
    # Exact displacement amplitudes/phase choices in docs/model_geometry/{mob}.json.
    motion = 0.87 if stage == 'MOVE' else 0.0
    limb = t * 1.6
    if group.startswith('seg_') and mob == 'sewer_leech':
        idx = int(group[-1]); phase = limb * 1.72 - idx * .60
        return (0, -max(0, math.sin(phase)) * motion * .55,
                math.sin(phase) * motion * .40)
    if group.startswith(('paw_', 'leg_')):
        side = 'r' if '_r_' in group else 'l'; idx = int(group[-1]);
        phase = (idx%2)*math.pi + (math.pi if side=='r' else 0)
        speed = {'sewer_roach':2.15,'scrap_rat':1.88,'rust_eater_beetle':2.32,
                 'cavern_burrower':1.65,'spore_crawler':1.98}.get(mob)
        if speed is None: return (0,0,0)
        lift = {'scrap_rat':.92,'cavern_burrower':.72}.get(mob,.75)
        return (0, -max(0, math.sin(limb * speed + phase)) * motion * lift, 0)
    return (0,0,0)

def project(p):
 x,y,z=p;return (x*.97+z*.63,y*1.17+(x-z)*.37)

# Pre-compute UV, original textured face centers; avoid reloading for each pose.
def load(mob):
 m=json.load(open(GEO/(mob+'.json')));texture=Image.open(TEX/(mob+'.png')).convert('RGB')
 faces=[]
 for gr,cs in m['groups'].items():
  for cub in cs:
   x,y,z=cub['xyz'];w,h,d=cub['dim'];u,v=cub['uv'];dw,dh,dd=[math.ceil(a) for a in (w,h,d)]
   for side,poly,uu,vv,sw,sh in [
    ('top',[(x,y,z),(x+w,y,z),(x+w,y,z+d),(x,y,z+d)],u+dd,v,dw,dd),
    ('front',[(x,y,z),(x+w,y,z),(x+w,y+h,z),(x,y+h,z)],u+dd,v+dd,dw,dh),
    ('right',[(x+w,y,z),(x+w,y,z+d),(x+w,y+h,z+d),(x+w,y+h,z)],u+dd+dw,v+dd,dd,dh)]:
    tx=min(texture.width-1,int(uu+sw*.5));ty=min(texture.height-1,int(vv+sh*.45));color=texture.getpixel((tx,ty));sha={'top':1.13,'front':.95,'right':.80}[side];color=tuple(max(0,min(255,int(ch*sha))) for ch in color)
    faces.append((gr,poly,color))
 return m,faces

def render(mob, m, faces,t,stage,W=450,H=315):
 coords=[]
 for gr,poly,color in faces:
  pv=m.get('part_pivots',{}).get(gr,[0,0,0]);ang=pose(mob,gr,t,stage)
  off=pose_offset(mob,gr,t,stage)
  pp=[tuple(a+b for a,b in zip(rotate(v,pv,ang),off)) for v in poly]
  dep=sum(v[0]-v[2]-.7*v[1] for v in pp)*.25
  coords.append((dep,[project(v) for v in pp],color))
 coords.sort(key=lambda x:x[0]); xmin=min(v[0] for _,poly,_ in coords for v in poly);xmax=max(v[0] for _,poly,_ in coords for v in poly);ymin=min(v[1] for _,poly,_ in coords for v in poly);ymax=max(v[1] for _,poly,_ in coords for v in poly)
 # fixed per-mob viewport, no zoom jitter; allow plenty of room for wing flaps
 base_sc={'glow_moth':7.1,'sewer_leech':7.3,'scrap_rat':7.2,'sewer_roach':7.6,'cavern_burrower':8.3,'sludge_crawler':10,'rust_eater_beetle':10,'spore_crawler':10}.get(mob,8)
 scale=min(base_sc, (W-45)/(xmax-xmin), (H-40)/(ymax-ymin))
 cenx=(xmin+xmax)/2;ceny=(ymin+ymax)/2
 img=Image.new('RGB',(W,H),(16,27,29));dr=ImageDraw.Draw(img)
 for gx in range(0,W,30):dr.line((gx,0,gx,H),fill=(22,35,35))
 for gy in range(0,H,30):dr.line((0,gy,W,gy),fill=(22,35,35))
 for _,poly,color in coords:
  pts=[(round((x-cenx)*scale+W/2),round((y-ceny)*scale+H/2)) for x,y in poly]
  dr.polygon(pts,fill=color);dr.line(pts+[pts[0]],fill=(18,25,25),width=1)
 dr.text((12,7),NAMES[mob],fill=(233,221,196),font=ImageFont.truetype(BOLD,18))
 dr.text((13,H-28),stage + ('  /  LIGHT LVL 12' if mob=='glow_moth' else ''),fill=(150,194,167),font=ImageFont.truetype(FONT,14))
 return img

if __name__=='__main__':
    from PIL import Image
    N_FRAMES=50
    DELAY_MS=100
    # GIF timing is in centiseconds: alternating 120/130ms gives exactly 5.00s.
    gif_delays=[100 for n in range(N_FRAMES)]
    dims=(410,285)
    data={mob:load(mob) for mob in NAMES}
    paths={}
    frames={mob:[] for mob in NAMES}
    for mob,(model,faces) in data.items():
        for n in range(N_FRAMES):
            # 5 seconds of actual continuous sample times, not a 1.3s GIF repeated.
            t=n*DELAY_MS/1000.0
            frames[mob].append(render(mob,model,faces,t,'MOVE',W=dims[0],H=dims[1]))
        outfile=OUT/f'{mob}_movement_5s.gif'
        frames[mob][0].save(outfile,save_all=True,append_images=frames[mob][1:],duration=gif_delays,loop=0,optimize=True)
        paths[mob]=outfile
        print(mob,outfile.stat().st_size,flush=True)
    groups=[('sewer','sewer_roach','scrap_rat','sewer_leech','glow_moth'),
            ('cavern','sludge_crawler','rust_eater_beetle','cavern_burrower','spore_crawler')]
    for kind,*members in groups:
        composite=[]
        for n in range(N_FRAMES):
            pic=Image.new('RGB',(dims[0]*2,dims[1]*2),(13,23,24))
            for j,mob in enumerate(members):
                pic.paste(frames[mob][n],((j%2)*dims[0],(j//2)*dims[1]))
            composite.append(pic)
        dest=OUT/f'{kind}_movement_5s.gif'
        composite[0].save(dest,save_all=True,append_images=composite[1:],duration=gif_delays,loop=0,optimize=True)
        print(kind,'combined',dest.stat().st_size,flush=True)
