#!/usr/bin/env python3
"""Offline-only stopgap generated resources for a source archive without a Gradle cache.

These assets MUST be overwritten with canonical NeoForge DataGen output by running
`./gradlew runData` on the owner's Windows machine, then tested in Minecraft.
No runtime correctness is claimed for this offline bootstrap.
"""
import copy
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
G=ROOT/'src/generated/resources'

def dump(p, v):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(v,indent=2)+'\n')

MODEL=G/'assets/dorohedoro'
for name in ('hell_rock','ash_soil'):
    dump(MODEL/f'blockstates/{name}.json',{'variants':{'':{'model':f'dorohedoro:block/{name}'}}})
    dump(MODEL/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'dorohedoro:block/{name}'}})
    dump(MODEL/f'models/item/{name}.json',{'parent':f'dorohedoro:block/{name}'})
    dump(G/f'data/dorohedoro/loot_table/blocks/{name}.json',{
        'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'dorohedoro:{name}'}],
        'conditions':[{'condition':'minecraft:survives_explosion'}]}]})

# Vanilla datagen overwrites these generated tags; the bootstrap keeps versioned sources self-contained.
for typ, src, dst in [('pickaxe','src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json','dorohedoro:hell_rock'),
                      ('shovel','src/generated/resources/data/minecraft/tags/block/mineable/shovel.json','dorohedoro:ash_soil')]:
    path=ROOT/src
    body=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
    if dst not in body['values']:
        body['values'].append(dst)
    dump(path,body)

# Create generated configured + placed features that exactly mirror the new Java bootstrap keys.
veins=[('coal','deepslate_coal_ore',14,24,-40,200),('iron','deepslate_iron_ore',9,16,-50,120),
       ('copper','deepslate_copper_ore',10,12,-40,120),('gold','deepslate_gold_ore',9,8,-60,70),
       ('redstone','deepslate_redstone_ore',8,8,-60,40),('diamond','deepslate_diamond_ore',7,5,-60,24)]
for name,ore,size,count,low,high in veins:
    key='hell_ore_'+name
    dump(G/f'data/dorohedoro/worldgen/configured_feature/{key}.json',{
        'type':'minecraft:ore','config':{'size':size,'discard_chance_on_air_exposure':0.0,
        'targets':[{'target':{'predicate_type':'minecraft:block_match','block':'dorohedoro:hell_rock'},
                    'state':{'Name':'minecraft:'+ore}}]}})
    dump(G/f'data/dorohedoro/worldgen/placed_feature/{key}.json',{
        'feature':'dorohedoro:'+key,'placement':[{'type':'minecraft:count','count':count},
        {'type':'minecraft:in_square'},
        {'type':'minecraft:height_range','height':{'type':'minecraft:uniform',
        'min_inclusive':{'absolute':low},'max_inclusive':{'absolute':high}}},
        {'type':'minecraft:biome'}]})

# Prefill biome resource files for first compile/runtime trial; runData remains authoritative.
base=json.loads((G/'data/dorohedoro/worldgen/biome/hole_outskirts.json').read_text())
spawner_keys=list(base['spawners'])
def sf(name,fog,sky,water,waterfog,grass,foliage,precip=False):
    b=copy.deepcopy(base)
    b.update(temperature=0.8,downfall=0.4,has_precipitation=precip)
    b['effects'].update(fog_color=fog,sky_color=sky,water_color=water,
                        water_fog_color=waterfog,grass_color=grass,foliage_color=foliage)
    b['effects'].pop('particle',None)
    b['spawners']={k:[] for k in spawner_keys}
    b['features']=[list(layer) for layer in base['features']]
    b['features'][8]=['minecraft:spring_lava','minecraft:spring_water']
    b['features'][9]=['minecraft:glow_lichen']
    b['features'][10]=[]
    return b

sw={
'sw_city': ('city',0xA9A7A0,0x8D9CA6,0x3F6D9E,0x050533,0x76924E,0x5C7E3C),
'sw_city_edge':('countryside',0xA9A7A0,0x8D9CA6,0x3F6D9E,0x050533,0x76924E,0x5C7E3C),
'sw_farmland':('countryside',0xB9C6C4,0x8FB4C4,0x3F76E4,0x050533,0x7DA24E,0x5F8F3E),
'sw_woodland':('woodland',0xB9C6C4,0x8FB4C4,0x3F76E4,0x050533,0x7DA24E,0x5F8F3E),
'sw_hydra_forest':('hydra',0x7D9184,0x6E8E86,0x3D6A6E,0x06242A,0x3E6B38,0x2F5A2D),
'sw_blight':('blight',0x9C8E8E,0x8A7F86,0x5A5A6E,0x1B1420,0x7A6E56,0x6C5F4C),
'sw_waters':('waters',0xB9C6C4,0x8FB4C4,0x3F76E4,0x050533,0x7DA24E,0x5F8F3E),
'sw_caves':('caves',0xB9C6C4,0x8FB4C4,0x3F76E4,0x050533,0x7DA24E,0x5F8F3E),
}
flowers=['minecraft:flower_default','minecraft:patch_grass_forest','minecraft:brown_mushroom_normal','minecraft:red_mushroom_normal']
for name,(kind,*palette) in sw.items():
    b=sf(name,*palette)
    if kind in ('countryside','caves'): b['features'][8]=['minecraft:spring_lava','minecraft:spring_water']
    if kind in ('countryside','woodland','hydra','blight'):
        vegetation = {'countryside':['minecraft:trees_plains'],
                     'woodland':['minecraft:trees_birch_and_oak'],
                     'hydra':['minecraft:dark_forest_vegetation'],
                     'blight':['minecraft:mushroom_island_vegetation']}[kind]
        b['features'][8]=list(dict.fromkeys(b['features'][8]))
        b['features'][9]=['minecraft:glow_lichen']+vegetation+flowers
    if kind in ('countryside','woodland','hydra'):
        b['spawners']['creature']=[{'type':'minecraft:cow','weight':8,'minCount':2,'maxCount':4},
                                   {'type':'minecraft:sheep','weight':8,'minCount':2,'maxCount':4},
                                   {'type':'minecraft:chicken','weight':10,'minCount':2,'maxCount':4}]
    if kind=='caves':
        b['spawners']['monster']=[{'type':'minecraft:zombie','weight':95,'minCount':1,'maxCount':3},
                                  {'type':'minecraft:skeleton','weight':60,'minCount':2,'maxCount':3},
                                  {'type':'minecraft:spider','weight':60,'minCount':1,'maxCount':3},
                                  {'type':'minecraft:creeper','weight':20,'minCount':1,'maxCount':2},
                                  {'type':'minecraft:slime','weight':40,'minCount':2,'maxCount':3}]
    if kind=='waters':
        b['spawners']['water_creature']=[{'type':'minecraft:squid','weight':4,'minCount':1,'maxCount':2}]
        b['spawners']['water_ambient']=[{'type':'minecraft:cod','weight':12,'minCount':3,'maxCount':5},
                                        {'type':'minecraft:salmon','weight':8,'minCount':2,'maxCount':4}]
    dump(G/f'data/dorohedoro/worldgen/biome/{name}.json',b)

hell={
'hell_wastes':(0x3A3532,0x1C1816,0x2E2A2A,0x0E0C0C,0x4A4438,0x3E382E,0.004),
'hell_horns':(0x4A2E2A,0x241210,0x2E2A2A,0x0E0C0C,0x4A4438,0x3E382E,0.002),
'hell_soul_flats':(0x2E3A40,0x141C20,0x2E3A40,0x0E1416,0x3A4A48,0x2E3E3C,0.006),
'hell_caverns':(0x161413,0x0C0A0A,0x1E1C1C,0x080707,0x2E2A26,0x26221E,0),
}
for name,(*palette,ash) in hell.items():
    b=sf(name,*palette)
    b.update(temperature=1.2, downfall=0.0)
    b['features']=[[],[],[],[],[],[],['dorohedoro:hell_ore_'+v[0] for v in veins],[],['minecraft:spring_lava'],[],[]]
    if ash:
        b['effects']['particle']={'options':{'type':'minecraft:white_ash'},'probability':ash}
    dump(G/f'data/dorohedoro/worldgen/biome/{name}.json',b)

# Keep the generated mineshaft biome tag consistent with ModDataGen on fresh source checkouts.
mineshaft=G.parent/'minecraft/tags/worldgen/biome/has_structure/mineshaft.json'
existing=json.loads(mineshaft.read_text()) if mineshaft.is_file() else {'replace':False,'values':[]}
for name in sw:
    key='dorohedoro:'+name
    if key not in existing['values']: existing['values'].append(key)
dump(mineshaft,existing)

print('Created provisional generated resources. RUN ./gradlew runData BEFORE installing to a live world.')
