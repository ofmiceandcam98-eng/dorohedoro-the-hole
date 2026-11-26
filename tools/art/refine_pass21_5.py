"""Hand-directed pixel-art refinements of existing Dorohedoro item/block PNGs.
Runs on Pass 21.4 only. Original item IDs, models, source, art dimensions are kept.
No outside artwork is copied. Does not touch Java or gameplay files.
"""
from PIL import Image, ImageDraw
from pathlib import Path
import hashlib, json
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/dorohedoro/textures'
IP=ROOT/'item'; BP=ROOT/'block'
changed=[]

def C(c):return tuple(bytes.fromhex(c.lstrip('#')))+(255,)
def art():return Image.new('RGBA',(32,32),(0,0,0,0))
def rect(a,xy,c):ImageDraw.Draw(a).rectangle(xy, fill=C(c))
def poly(a,pts,c):ImageDraw.Draw(a).polygon(pts,fill=C(c))
def line(a,pts,c,w=1):ImageDraw.Draw(a).line(pts,fill=C(c),width=w,joint='curve')
def dot(a,x,y,c):ImageDraw.Draw(a).point((x,y),fill=C(c))
def ellipse(a,xy,c):ImageDraw.Draw(a).ellipse(xy,fill=C(c))
def save_item(name,a):
 p=IP/(name+'.png'); assert p.exists(); assert a.size==(32,32)
 before=hashlib.sha256(p.read_bytes()).hexdigest(); a.save(p, optimize=True)
 after=hashlib.sha256(p.read_bytes()).hexdigest(); assert before != after,name
 changed.append(('item',name))
def save_block(name,a):
 p=BP/(name+'.png'); assert p.exists(); assert a.size==(32,32)
 before=hashlib.sha256(p.read_bytes()).hexdigest(); a.save(p, optimize=True)
 after=hashlib.sha256(p.read_bytes()).hexdigest(); assert before != after,name
 changed.append(('block',name))

# Everything reads cleanly at native 16x inventory presentation, not only at enlarged pixel scale.
a=art()
# Bitumen - black tar with an irregular ochre stone crust and clear rim separation.
poly(a,[(4,22),(5,17),(9,14),(13,15),(16,10),(22,12),(26,15),(29,19),(27,25),(21,28),(10,27)],'#202B2B')
poly(a,[(6,19),(11,17),(14,15),(19,13),(23,16),(27,20),(24,25),(11,25),(7,23)],'#424746')
poly(a,[(8,18),(13,16),(17,17),(19,15),(23,17),(21,19),(12,20)],'#66706B')
rect(a,(11,21,22,23),'#303A3B');line(a,[(17,13),(21,14),(23,16)],'#9A9380')
dot(a,10,22,'#8C8575');line(a,[(12,26),(21,27)],'#1A2325')
save_item('bitumen',a)

a=art() # solidified droplets: different from black bitumen, separated shards and ochre inclusions
poly(a,[(4,24),(8,17),(12,17),(14,11),(21,10),(27,16),(29,23),(23,27),(9,28)],'#263031')
poly(a,[(7,22),(11,16),(16,17),(18,12),(23,15),(26,21),(24,24),(10,25)],'#514942')
poly(a,[(11,17),(17,14),(21,16),(17,19)],'#9A795A')
line(a,[(9,22),(14,23),(20,21)],'#B29270');line(a,[(14,27),(25,24)],'#313637')
for p in [(6,26),(27,17),(22,11)]:dot(a,*p,'#83735E')
save_item('coagulated_tar',a)

# Salt must not be confused with raw zinc: large stepped white crystalline cubes with hard edges.
a=art()
for ox,oy,k in [(3,16,10),(15,8,12),(19,20,9)]:
 poly(a,[(ox,oy+3),(ox+3,oy),(ox+k-2,oy),(ox+k,oy+3),(ox+k-2,oy+k-1),(ox+1,oy+k-1)],'#738E93')
 poly(a,[(ox+2,oy+2),(ox+4,oy+1),(ox+k-3,oy+1),(ox+k-2,oy+3),(ox+k-3,oy+k-4),(ox+2,oy+k-4)],'#D9EAE6')
 line(a,[(ox+2,oy+2),(ox+k-3,oy+2)],'#FCFFF0')
 line(a,[(ox+2,oy+k-2),(ox+k-1,oy+k-2)],'#6A8B8D')
save_item('rock_salt',a)

# Metallic raw ore: rounded jagged nugget faces, metallic rather than glassy.
a=art()
for ox,oy,k in [(2,17,11),(13,8,14),(19,20,10)]:
 poly(a,[(ox,oy+3),(ox+4,oy),(ox+k-4,oy-1),(ox+k,oy+5),(ox+k-3,oy+k-1),(ox+2,oy+k)],'#445B62')
 poly(a,[(ox+2,oy+3),(ox+5,oy+1),(ox+k-4,oy+1),(ox+k-1,oy+4),(ox+k-3,oy+6),(ox+2,oy+6)],'#9EBAB9')
 line(a,[(ox+4,oy+2),(ox+k-3,oy+2)],'#DAE9DB')
 line(a,[(ox+3,oy+8),(ox+k-4,oy+9)],'#607D80')
save_item('raw_zinc',a)

# Crumbly sulfur stays opaque and craggy, no crystal facets.
a=art()
for ox,oy,k in [(4,16,10),(15,8,12),(19,20,8)]:
 poly(a,[(ox,oy+3),(ox+3,oy),(ox+7,oy+1),(ox+k,oy+5),(ox+k-3,oy+k),(ox+1,oy+k-2)],'#6D5A30')
 poly(a,[(ox+2,oy+3),(ox+4,oy+1),(ox+7,oy+3),(ox+k-2,oy+5),(ox+k-4,oy+7),(ox+1,oy+7)],'#D4AC43')
 line(a,[(ox+3,oy+2),(ox+6,oy+2)],'#F7DA73');dot(a,ox+4,oy+5,'#FFE99A')
save_item('raw_sulfur',a)

# Fungal membrane - curled translucent sheet with distinct thin rim, pores and curled end.
a=art()
poly(a,[(3,17),(9,13),(12,8),(21,10),(25,15),(30,15),(26,22),(22,24),(17,28),(10,25),(5,21)],'#394943')
poly(a,[(5,17),(11,13),(13,10),(19,12),(24,17),(27,18),(22,23),(16,25),(10,23),(7,20)],'#879E80')
poly(a,[(7,17),(13,12),(17,13),(21,17),(17,19),(14,23),(10,21)],'#CBD4A6')
line(a,[(11,23),(16,19),(23,18)],'#526A59')
for x,y in [(10,18),(17,16),(18,22),(22,20)]:dot(a,x,y,'#5B765C')
line(a,[(24,15),(29,16),(26,21)],'#DDE3B5')
save_item('fungal_membrane',a)

# Reptile-like carapace plates no longer use the same silhouette as scrap metal/hide.
a=art()
poly(a,[(2,17),(7,10),(15,7),(23,10),(29,17),(24,26),(14,29),(6,25)],'#2D3E38')
poly(a,[(5,16),(10,10),(18,10),(25,14),(26,19),(21,25),(13,26),(7,22)],'#688577')
poly(a,[(9,12),(16,9),(21,12),(17,17),(10,18),(6,16)],'#BED0AF')
poly(a,[(18,18),(24,15),(25,19),(20,24),(14,25)],'#405C52')
line(a,[(16,11),(15,18),(18,23)],'#314C48',2)
line(a,[(8,21),(14,22),(18,26)],'#C0C6A8')
for x,y in [(8,17),(21,15),(12,25)]:dot(a,x,y,'#DFD7AC')
save_item('metallic_carapace',a)

a=art() # thick layered chitin with rivet-like nodules and serrated side edge
poly(a,[(4,19),(5,11),(12,6),(20,8),(25,12),(30,17),(26,20),(27,25),(18,29),(8,26)],'#2B3835')
poly(a,[(7,17),(9,11),(14,9),(22,11),(26,15),(22,22),(17,26),(9,23)],'#607865')
poly(a,[(10,12),(15,9),(22,11),(23,14),(19,17),(12,18)],'#B1C1A0')
poly(a,[(10,19),(18,18),(23,21),(18,25),(11,23)],'#42574A')
line(a,[(13,11),(11,21)],'#E0D8B2');line(a,[(18,14),(18,24)],'#293F39',2)
for x,y in [(24,14),(7,20),(22,23)]:dot(a,x,y,'#DBD5AE')
save_item('reinforced_chitin',a)

a=art() # folded hide, stitched/beveled, not armor
poly(a,[(3,18),(9,11),(18,8),(25,10),(30,17),(25,24),(19,28),(9,26)],'#403C34')
poly(a,[(6,18),(12,12),(20,11),(26,14),(27,18),(22,24),(16,25),(8,23)],'#9B8063')
poly(a,[(9,16),(14,12),(21,12),(24,15),(19,18)],'#D2BB91')
line(a,[(9,21),(13,22),(16,24),(22,21)],'#61574B',2)
for x,y in [(11,23),(14,24),(17,24),(20,23)]:dot(a,x,y,'#D9CAAB')
save_item('dense_hide',a)

a=art() # jagged bent steel plate with rivets and different edges
poly(a,[(2,17),(8,9),(14,11),(20,7),(28,14),(29,22),(20,27),(12,25),(5,27)],'#2E4248')
poly(a,[(6,17),(11,11),(15,14),(20,10),(25,15),(26,20),(20,24),(12,21)],'#809D9E')
poly(a,[(9,15),(13,12),(18,15),(22,12),(25,16),(16,18)],'#C6D4C9')
line(a,[(7,20),(13,22),(16,18),(22,23)],'#455A5C',2)
for x,y in [(9,17),(22,18)]:rect(a,(x,y,x+2,y+2),'#415154');dot(a,x,y,'#E6D7A9')
save_item('scrap_metal',a)

# The gland family: irregular anatomy and stalks, readable even without saturated blobs.
a=art()
poly(a,[(12,6),(18,6),(19,10),(24,12),(28,18),(25,25),(18,29),(9,27),(5,21),(8,12)],'#30452F')
poly(a,[(11,12),(17,10),(23,13),(25,19),(22,24),(16,26),(9,23),(8,18)],'#6EAD51')
poly(a,[(12,14),(18,12),(21,16),(17,19),(11,18)],'#B7DC71')
line(a,[(16,10),(16,15),(21,20),(18,25)],'#476E38',2)
for x,y in [(10,18),(22,19),(14,23)]:dot(a,x,y,'#E0E78B')
line(a,[(13,8),(15,5),(18,6)],'#A5C377',2)
save_item('corrosive_gland',a)

a=art()
poly(a,[(12,6),(19,8),(22,11),(26,15),(28,22),(24,28),(16,30),(8,26),(4,19),(7,12)],'#283B37')
poly(a,[(10,13),(17,10),(22,13),(25,19),(22,25),(16,27),(9,23)],'#56785F')
poly(a,[(11,14),(15,11),(20,14),(20,18),(16,20),(10,18)],'#94A780')
line(a,[(16,12),(16,19),(11,22)],'#344E45',2)
line(a,[(22,21),(24,25),(23,29)],'#849979',2)
for x,y in [(13,15),(20,23)]:dot(a,x,y,'#B1B996')
save_item('sludge_gland',a)

a=art() # clustered spore pods wrapped in fungal webbing
for cx,cy,r in [(9,14,7),(22,12,7),(16,23,8)]:
 ellipse(a,(cx-r,cy-r,cx+r,cy+r),'#495439')
 ellipse(a,(cx-r+2,cy-r+1,cx+r-2,cy+r-3),'#A9B15C')
 line(a,[(cx-3,cy-2),(cx,cy-4)],'#E8D99A',2)
poly(a,[(10,17),(16,19),(19,15),(23,22),(16,28)],'#65744B')
line(a,[(8,20),(16,25),(24,19)],'#D9D5A2')
for x,y in [(7,15),(21,11),(15,21)]:dot(a,x,y,'#E5E2AB')
save_item('spore_sac',a)

# Silk is bundled on a spool rather than a few comb teeth.
a=art()
poly(a,[(8,8),(22,7),(26,10),(25,23),(21,27),(8,26),(5,22),(6,12)],'#57574C')
poly(a,[(10,9),(21,9),(23,12),(23,23),(21,25),(9,24),(8,21),(8,12)],'#D4C8A5')
for x in (11,14,17,20):line(a,[(x,11),(x-1,22)],'#A9A68E',2)
line(a,[(10,11),(21,11)],'#F1E8C9')
line(a,[(5,12),(26,12)],'#6C6150',2);line(a,[(5,23),(25,23)],'#6C6150',2)
line(a,[(21,23),(26,27),(29,25)],'#F1E8CB',2)
save_item('moth_silk',a)

# Medicine: antidote is a herb poultice, salve is a jar, sealant is an industrial tube.
a=art()
poly(a,[(7,11),(12,6),(23,8),(27,15),(27,25),(21,29),(10,27),(5,21)],'#34443D')
poly(a,[(9,12),(14,8),(22,10),(25,16),(24,25),(20,27),(11,25),(7,20)],'#A5BA89')
poly(a,[(11,16),(15,12),(21,14),(22,18),(18,22),(12,20)],'#E2D9B1')
line(a,[(14,19),(18,14),(22,16)],'#527D5D',2);line(a,[(17,14),(18,21)],'#789A64')
line(a,[(8,21),(23,23)],'#EEE2BF',2)
line(a,[(10,12),(22,10)],'#EFF1D0');dot(a,8,19,'#DFE4B4')
save_item('antidote_poultice',a)

a=art()
poly(a,[(7,10),(25,10),(27,14),(27,26),(24,29),(8,29),(5,26),(5,14)],'#343A36')
rect(a,(8,13,24,25),'#B9A77E');rect(a,(10,16,22,23),'#E5D9B2')
rect(a,(7,8,25,13),'#746A5B');rect(a,(9,6,23,9),'#E0C9A1')
line(a,[(9,13),(22,13)],'#F1E2BD');line(a,[(9,25),(23,25)],'#716A58',2)
rect(a,(14,17,17,23),'#92634F');rect(a,(12,19,20,21),'#92634F')
for x in (9,22):dot(a,x,20,'#FFF0C7')
save_item('medical_salve',a)

a=art()
poly(a,[(11,5),(21,5),(22,10),(26,15),(26,26),(23,29),(8,29),(5,25),(6,14),(10,10)],'#323D3E')
poly(a,[(9,14),(23,14),(24,25),(22,27),(10,27),(8,24)],'#7E9A91')
rect(a,(11,6,21,10),'#D7BB87');rect(a,(12,8,20,10),'#A6916E')
rect(a,(9,17,23,23),'#D7D5BD');rect(a,(11,19,21,21),'#476A65')
rect(a,(16,17,17,24),'#AE7D5C');line(a,[(10,13),(22,13)],'#DCE5D3')
dot(a,11,16,'#ECF1D9');dot(a,21,25,'#3B5C59')
save_item('sewer_sealant',a)

# Green mucous material: not a jar of potion; tear-shaped viscous organic drop.
a=art()
poly(a,[(16,2),(20,9),(24,14),(27,21),(25,27),(18,30),(10,28),(6,22),(9,14),(13,9)],'#2E4540')
poly(a,[(16,7),(20,13),(24,20),(23,25),(18,28),(11,25),(9,21),(12,14)],'#72977B')
poly(a,[(15,11),(18,13),(21,19),(18,20),(13,17)],'#D9E0A7')
line(a,[(12,22),(16,25),(20,24)],'#436C5A',2);dot(a,14,13,'#F0EFC3')
save_item('leech_mucus',a)

# Iconic food silhouettes, with bone, char and tied salting wrap to distinguish three.
a=art()
poly(a,[(2,17),(6,12),(13,9),(20,12),(25,12),(29,17),(28,23),(20,27),(11,25),(5,22)],'#683D47')
poly(a,[(5,16),(9,12),(15,12),(19,15),(23,14),(26,18),(24,23),(16,24),(8,21)],'#C47E82')
line(a,[(9,17),(15,14),(20,17)],'#E9AAAA',2)
poly(a,[(17,20),(22,17),(26,20),(23,24)],'#F2C3AE')
line(a,[(18,22),(24,22)],'#83555B')
save_item('raw_rat_meat',a)

a=art()
poly(a,[(3,15),(7,11),(15,9),(22,12),(26,16),(25,22),(18,27),(8,26),(4,21)],'#573C31')
poly(a,[(6,15),(11,11),(18,11),(23,15),(24,20),(19,25),(10,24),(6,20)],'#AD6E3E')
poly(a,[(9,14),(14,12),(20,14),(20,18),(15,19)],'#E3AB62')
line(a,[(10,20),(17,23),(21,20)],'#78482F',2)
line(a,[(22,22),(27,26),(29,24)],'#EAD8AF',2);dot(a,29,23,'#F8E7C0')
save_item('cooked_rat_meat',a)

a=art()
poly(a,[(3,16),(9,9),(22,9),(29,16),(27,24),(19,28),(8,26)],'#564B3F')
poly(a,[(5,16),(11,11),(21,11),(27,17),(24,23),(18,25),(9,23)],'#B7A180')
poly(a,[(9,13),(22,12),(24,17),(18,19),(10,18)],'#E7CDA3')
line(a,[(8,18),(21,23)],'#9A7554',2);line(a,[(13,13),(16,20)],'#FAEACB')
for x,y in [(11,21),(18,14),(22,19)]:dot(a,x,y,'#F4EACF')
save_item('salted_rat_ration',a)

# Concentrated smoke residue should be legible even on a charcoal inventory.
a=art()
poly(a,[(3,24),(8,18),(13,20),(18,13),(22,17),(28,22),(29,26),(7,27)],'#252E32')
poly(a,[(7,23),(11,20),(16,21),(20,16),(25,22),(24,25),(8,25)],'#505E62')
line(a,[(11,21),(17,22),(21,20)],'#87908A');dot(a,19,17,'#AFB5A4')
for x,y in [(9,17),(15,15),(26,17)]:dot(a,x,y,'#788581')
save_item('smoke_residue',a)

# Filter cartridge with ribbed removable frame (not a gray rectangle with stripes).
a=art()
poly(a,[(4,12),(9,7),(24,7),(29,12),(29,26),(24,29),(8,29),(3,25)],'#303F40')
poly(a,[(6,13),(10,9),(23,9),(27,13),(26,25),(23,27),(9,27),(6,24)],'#727D74')
rect(a,(9,13,23,24),'#35484A')
for x in (11,14,17,20,23):line(a,[(x,12),(x,25)],'#B4C1AD',2)
line(a,[(7,12),(25,12)],'#D7C09A',2);line(a,[(7,25),(25,25)],'#4B615B',2)
for x,y in [(8,15),(25,22)]:dot(a,x,y,'#D5CDA9')
for x,y in [(13,19),(17,16),(21,23)]:dot(a,x,y,'#5F6A62')
save_item('smoke_soaked_filter',a)

# Respect all 3D limb texture atlases, unchanged. Enhance generic 32x bottles without deleting glyphs.
flask_accents={
'mushroom':'#DAD29A','reptile':'#91C69A','dismemberment':'#D6A2AB',
'healing':'#ECE0AC','curse':'#B996D0','protection':'#98CDD4',
'destruction':'#F4B078','time':'#E6CA8F'}
for name,accent in flask_accents.items():
 p=IP/(name+'_smoke_flask.png'); a=Image.open(p).convert('RGBA')
 # color-coded stopper collar is subtly readable in 16px thumbnail
 rect(a,(12,8,20,9),'#4A6463')
 for x,y in [(12,9),(20,9),(12,16),(22,17)]:dot(a,x,y,accent)
 # a unique fluid side refraction at level of contents, does not obscure symbol
 line(a,[(10,23),(11,25)],accent)
 save_item(name+'_smoke_flask',a)

# Blocks: edit the REAL 32px textures with hand-placed weathering clusters. Do not overlay noise.
def existing_block(name):return Image.open(BP/(name+'.png')).convert('RGBA')

# Soot accumulates in seams and below brick courses; ceramic face highlights still survive.
a=existing_block('soot_blackened_brick')
for x,y in [(2,2),(20,3),(8,11),(26,12),(3,20),(19,27)]:
 line(a,[(x,y),(x+3,y)],'#373634',2);dot(a,x+2,y+2,'#5B5550')
for x,y in [(4,7),(14,15),(24,23)]:line(a,[(x,y),(x+2,y)],'#8B7D6F')
save_block('soot_blackened_brick',a)

# Sewers: restrained mineral streaks and algae concentrated at seams.
a=existing_block('sewer_brickwork')
for x,y in [(2,2),(21,5),(10,17),(27,25)]:
 line(a,[(x,y),(x,y+3)],'#4A6356');dot(a,x+1,y+1,'#8C9A86')
for x,y in [(6,13),(22,21)]:line(a,[(x,y),(x+2,y)],'#ADB0A0')
save_block('sewer_brickwork',a)

# A small number of deliberate teal seepage streaks form one family across district assets.
a=existing_block('seepage_brick')
for x,y,depth in [(4,0,5),(27,7,3),(18,16,6),(7,24,5)]:
 line(a,[(x,y),(x,y+depth)],'#2E615B');dot(a,x+1,y+depth,'#74A79B')
line(a,[(13,15),(15,15)],'#A0B6A4')
save_block('seepage_brick',a)

a=existing_block('patchwork_masonry')
for x,y,c in [(3,3,'#B7AD99'),(23,5,'#5C625C'),(12,12,'#B1A68C'),(6,20,'#4E5955'),(23,27,'#C7BCA8')]:
 rect(a,(x,y,x+3,y+1),c)
for x,y in [(11,8),(25,17),(3,26)]:line(a,[(x,y),(x+2,y+1)],'#514F4A')
save_block('patchwork_masonry',a)

# Rust follows bolts, rivet heads and metal seams, not scattered flat specks.
a=existing_block('rusted_plating')
for x,y in [(5,5),(27,5),(5,21),(27,21),(16,2),(16,18)]:
 for dx,dy,c in [(0,0,'#B7764C'),(1,0,'#9A5C3E'),(0,1,'#84533E'),(2,1,'#C68C5D')]:dot(a,x+dx,y+dy,c)
save_block('rusted_plating',a)

a=existing_block('galvanized_plating')
for x,y in [(4,3),(22,4),(6,19),(22,22)]:
 line(a,[(x,y),(x+3,y)],'#BAC9C4');line(a,[(x+2,y+1),(x+5,y+1)],'#677D7D')
save_block('galvanized_plating',a)

a=existing_block('smoke_stained_concrete')
for x,y in [(7,0),(24,2),(14,15),(29,23)]:
 line(a,[(x,y),(x,y+3)],'#5B6866')
for x,y in [(5,12),(19,27)]:line(a,[(x,y),(x+4,y)],'#92928A')
save_block('smoke_stained_concrete',a)

a=existing_block('water_damaged_plaster')
for x,y in [(2,1),(19,4),(28,18)]:
 poly(a,[(x,y),(x+3,y),(x+4,y+2),(x+2,y+5),(x,y+4)],'#8DADA3')
 line(a,[(x+2,y+4),(x+2,y+7)],'#738C85')
for x,y in [(12,16),(23,28)]:line(a,[(x,y),(x+3,y)],'#E2DAC2')
save_block('water_damaged_plaster',a)

a=existing_block('limestone')
for x,y in [(2,4),(18,5),(10,15),(23,25),(4,28)]:line(a,[(x,y),(x+5,y)],'#C9C5AD')
for x,y in [(13,10),(26,17)]:line(a,[(x,y),(x+3,y)],'#858E82')
save_block('limestone',a)

print(json.dumps({'changed_items':sum(1 for k,_ in changed if k=='item'),
 'changed_blocks':sum(1 for k,_ in changed if k=='block'),
 'item_names':[n for k,n in changed if k=='item'],
 'block_names':[n for k,n in changed if k=='block']},indent=2))
