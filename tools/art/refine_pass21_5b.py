"""Second hand-directed item texture refinement: Pass 21.5 companion script.
Run ONCE after refine_pass21_5.py on Pass 21.4 source.
"""
from pathlib import Path
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[2]/'src/main/resources/assets/dorohedoro/textures/item'
def C(s):return tuple(bytes.fromhex(s.lstrip('#')))+(255,)
def n():return Image.new('RGBA',(32,32),(0,0,0,0))
def poly(a,pts,c):ImageDraw.Draw(a).polygon(pts,fill=C(c))
def line(a,pts,c,w=1):ImageDraw.Draw(a).line(pts,fill=C(c),width=w)
def dot(a,x,y,c):ImageDraw.Draw(a).point((x,y),fill=C(c))
def rect(a,xy,c):ImageDraw.Draw(a).rectangle(xy,fill=C(c))
def ell(a,xy,c):ImageDraw.Draw(a).ellipse(xy,fill=C(c))
def save(name,a):
 p=root/(name+'.png');assert p.is_file();a.save(p,optimize=True)

# Loosely wrapped fur pelt. Distinct from claw, silk and hide.
a=n()
poly(a,[(6,10),(11,7),(14,4),(17,9),(22,6),(23,11),(28,13),(24,18),(27,22),(22,24),(21,28),(15,26),(12,29),(9,25),(4,23),(6,18),(3,14)],'#383D38')
poly(a,[(8,12),(13,9),(17,12),(22,10),(24,15),(22,21),(16,25),(9,22),(6,17)],'#9C9385')
poly(a,[(9,12),(14,10),(17,13),(20,12),(19,17),(13,20),(8,18)],'#D0C2A5')
line(a,[(9,22),(14,23),(18,25)],'#6A685F',2)
for x,y in [(7,18),(22,18),(14,13),(19,21)]:dot(a,x,y,'#E2CFAB')
save('rat_fur',a)

# Two subtly different articulated cockroach antennae with thin side hairs.
a=n()
for off in (0,13):
 line(a,[(7+off,29),(5+off,24),(5+off,19),(7+off,15),(10+off,10),(13+off,5)],'#443B35',3)
 line(a,[(8+off,28),(6+off,23),(6+off,19),(8+off,15),(11+off,10),(14+off,5)],'#BA986E')
 for x,y in [(7,17),(8,13),(10,10)]:line(a,[(x+off,y),(x+off+2,y-2)],'#8D6D4F')
 dot(a,14+off,5,'#E2C19A')
save('roach_antenna',a)

# Ringed leech sucker with irregular overlapping suction pads.
a=n()
ell(a,(4,8,28,29),'#293E3A');ell(a,(6,9,26,27),'#698E7C');ell(a,(9,12,24,25),'#B1C6A3')
ell(a,(12,15,22,23),'#344C45');ell(a,(14,16,20,21),'#1F3632')
for x,y in [(10,13),(22,13),(9,22),(23,24),(16,11)]:dot(a,x,y,'#DADEB8')
line(a,[(7,17),(9,23)],'#3D6356',2)
save('leech_sucker',a)

# Glow tonic receives visible illuminated vial fluid but keeps canonical bottle proportions.
a=n()
poly(a,[(12,5),(21,5),(21,11),(25,14),(26,26),(22,29),(9,29),(6,26),(7,14),(12,11)],'#24393D')
poly(a,[(10,15),(23,15),(24,25),(21,27),(10,27),(8,24)],'#69B09B')
poly(a,[(11,17),(22,17),(23,23),(20,26),(11,26),(9,22)],'#ABF0B9')
rect(a,(13,3,19,7),'#A68C65');rect(a,(11,9,22,11),'#6C9F95')
line(a,[(10,15),(10,23)],'#F1FFE2',2)
for x,y in [(15,20),(20,18),(18,24)]:dot(a,x,y,'#F1FFD9')
line(a,[(14,26),(19,26)],'#D9FFE2')
save('glow_tonic',a)

# Mineral dust: shavings with pale chalky highlight, not shapeless grey triangular field.
a=n()
poly(a,[(3,25),(7,22),(11,20),(14,21),(17,16),(22,18),(28,23),(29,27),(4,28)],'#676E67')
poly(a,[(6,24),(10,21),(15,23),(18,20),(23,22),(26,25),(6,26)],'#C5C8AE')
line(a,[(9,23),(14,23),(17,20),(20,21)],'#F4F2D9')
for x,y in [(6,18),(13,15),(23,16),(28,21)]:dot(a,x,y,'#C9C6AA')
save('lime_powder',a)

# Activation key is a crank-and-disc device with a mechanical notch, rather than a ring-ended wand.
a=n()
line(a,[(6,27),(18,15)],'#2D3B3D',7);line(a,[(6,27),(18,15)],'#B39B70',4)
ell(a,(14,3,29,18),'#293C40');ell(a,(16,5,27,16),'#AE976D');ell(a,(19,8,24,13),'#3B5558')
rect(a,(23,7,29,9),'#D6BF87');rect(a,(25,12,28,14),'#6C7E76')
line(a,[(7,25),(17,15)],'#E3D1A6');dot(a,5,27,'#DFD3AE')
save('door_activation_tool',a)

# Skeleton key with split metal teeth, distinct from the door activation crank.
a=n()
ell(a,(5,5,18,18),'#283939');ell(a,(7,7,16,16),'#B8A67A');ell(a,(10,10,13,13),'#3C4946')
line(a,[(16,16),(27,27)],'#303D3C',6);line(a,[(16,16),(26,26)],'#C8B184',3)
rect(a,(24,22,28,25),'#C8B184');rect(a,(25,26,29,28),'#C8B184')
line(a,[(8,8),(13,8)],'#F0DFB2');dot(a,26,26,'#6C6554')
save('door_linking_key',a)

# Ritual knife/stylus: industrial magical device with red seal, unlike both keys.
a=n()
poly(a,[(5,26),(10,21),(16,13),(22,7),(28,4),(27,11),(22,17),(14,25),(8,29)],'#293639')
poly(a,[(8,25),(12,20),(20,11),(25,7),(23,13),(14,24),(10,27)],'#C0AF8A')
line(a,[(8,25),(17,16)],'#EFE1BB',2)
poly(a,[(20,12),(24,15),(21,20),(17,17)],'#713E40')
poly(a,[(20,14),(22,15),(20,17),(18,16)],'#D17D68')
dot(a,8,27,'#E8D4AB')
save('ritual_tool',a)

# Inset striped shale with oil inclusions, unlike raw zinc crystalline chunks.
a=n()
for ox,oy,k in [(3,18,11),(15,9,12),(18,21,9)]:
 poly(a,[(ox,oy+3),(ox+3,oy),(ox+k-3,oy+1),(ox+k,oy+5),(ox+k-2,oy+k-2),(ox+1,oy+k)],'#303E42')
 poly(a,[(ox+2,oy+3),(ox+4,oy+2),(ox+k-2,oy+4),(ox+k-4,oy+6),(ox+1,oy+6)],'#66787B')
 line(a,[(ox+2,oy+6),(ox+k-3,oy+7)],'#85908C')
 dot(a,ox+4,oy+4,'#B3AEA0')
save('oil_shale_chunk',a)

print('Second refinement applied: nine additional existing item PNGs.')
