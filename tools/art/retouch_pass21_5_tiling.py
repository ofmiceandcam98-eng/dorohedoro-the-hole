"""Pass 21.5 normal-distance tiling corrective follow-up.
Run after base Pass21.5 sprite scripts, before packaging.
Restore original Pass21.4 block tiles for 5 materials then apply less prominent hand-placed details.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import io,zipfile
P=Path(__file__).resolve().parents[2]
ZIP=Path('/mnt/data/files/Dorohedoro_Pass21_4_Full_Source_Minecraft_Style_Art_Rework_NOT_COMPILED.zip')
BL=P/'src/main/resources/assets/dorohedoro/textures/block'
def C(s):return tuple(bytes.fromhex(s.lstrip('#')))+(255,)
def img(name):
 with zipfile.ZipFile(ZIP) as z:
  return Image.open(io.BytesIO(z.read('src/main/resources/assets/dorohedoro/textures/block/'+name+'.png'))).convert('RGBA')
def line(a,pts,c,w=1):ImageDraw.Draw(a).line(pts,fill=C(c),width=w)
def dot(a,x,y,c):ImageDraw.Draw(a).point((x,y),fill=C(c))
def save(a,n):a.save(BL/(n+'.png'),optimize=True)
# Fine soot streaks with different interval lengths, retain single material shade.
a=img('water_damaged_plaster')
for (x,y,h,shade) in [(3,1,2,'#A7B8AD'),(19,5,3,'#9BAEA5'),(29,18,2,'#A6B7AC')]:
 line(a,[(x,y),(x,y+h)],shade)
 dot(a,x+1,y+1,'#B0C0B5')
for x,y in [(13,16),(23,27)]:dot(a,x,y,'#E0DACA')
save(a,'water_damaged_plaster')
# Limestone - clustered chalk highlights, no horizontal fake banding on entire walls.
a=img('limestone')
for x,y in [(2,4),(19,5),(11,15),(24,25),(5,28)]:
 dot(a,x,y,'#D1C9AF');dot(a,x+1,y,'#C4BDA9')
for x,y in [(13,10),(26,17)]:dot(a,x,y,'#8F958B')
save(a,'limestone')
# Rust concentrates near seams/fasteners; not the same vivid neon flake at every fastener.
a=img('rusted_plating')
for x,y in [(5,5),(27,5),(5,21),(27,21),(16,2),(16,18)]:
 dot(a,x,y,'#9D6247');dot(a,x+1,y,'#80604C')
for x,y in [(8,3),(24,18)]:line(a,[(x,y),(x+2,y)],'#9C7159')
save(a,'rusted_plating')
# zinc oxide bloom: subdued cool highlights, muted brush strokes.
a=img('galvanized_plating')
for x,y in [(4,3),(22,4),(6,19),(22,22)]:
 dot(a,x,y,'#ADBDB8');dot(a,x+1,y,'#7A8F8D')
save(a,'galvanized_plating')
# less patterned trickles; keep a muted washed blue-green as a rare accent.
a=img('seepage_brick')
for x,y,h in [(4,0,2),(27,7,2),(18,16,3),(7,24,2)]:
 line(a,[(x,y),(x,y+h)],'#416F65')
 dot(a,x+1,y+h,'#739489')
save(a,'seepage_brick')
print('Retouched five block tiles to remove overly regular 3x3 patterns.')
