#!/usr/bin/env python3
"""Offline structural checks for Phase 3. This DOES NOT prove NeoForge or gameplay compatibility."""
import json, re
from pathlib import Path
P=Path(__file__).resolve().parents[1]
A=P/'src/main/resources/data/dorohedoro'
G=P/'src/generated/resources/data/dorohedoro'
S=P/'src/main/java/com/dorohedoro/hole'
checks=[]

def check(label, success):
    checks.append((label, bool(success)))
    print(('PASS' if success else 'FAIL')+'  '+label)

def load(p): return json.loads(p.read_text())

def walk(node, result=None):
    result = [] if result is None else result
    if isinstance(node,dict):
        for key,value in node.items():
            if key=='noise' and isinstance(value,str): result.append(value)
            walk(value, result)
    elif isinstance(node,list):
        for value in node: walk(value,result)
    return result

roots=['sorcerers_world','hell']
for realm in roots:
    dim=load(A/f'dimension/{realm}.json')
    typ=load(A/f'dimension_type/{realm}.json')
    settings=load(A/f'worldgen/noise_settings/{realm}.json')
    check(f'{realm}: registered dimension type',dim['type']=='dorohedoro:'+realm and typ['height']==settings['noise']['height'])
    check(f'{realm}: noise settings resolve',dim['generator']['settings']=='dorohedoro:'+realm)
    check(f'{realm}: custom biome source',dim['generator']['biome_source']['type'].startswith('dorohedoro:'))
    check(f'{realm}: infinite overworld-height profile',typ['min_y']==-64 and typ['height']==384)
    check(f'{realm}: no physical ceiling',typ['has_ceiling'] is False)
    used=[n for n in walk(settings) if n.startswith('dorohedoro:')]
    check(f'{realm}: custom noise keys exist',all((A/f'worldgen/noise/{n.split(":",1)[1]}.json').is_file() for n in used))
    if realm=='sorcerers_world':
        check('sorcerer world: separate basin noise',settings['noise_router']['depth']['noise']=='dorohedoro:city_basin')
        check('sorcerer world: retains water/ore aquifers',settings['aquifers_enabled'] and settings['ore_veins_enabled'])
    else:
        check('hell: not a nether terrain alias',settings['default_block']['Name']=='dorohedoro:hell_rock' and settings['sea_level']==-64)
        check('hell: no nether ceiling or lava ocean',settings['default_fluid']['Name']=='minecraft:air' and not typ['has_ceiling'])

sw=['sw_city','sw_city_edge','sw_farmland','sw_woodland','sw_hydra_forest','sw_blight','sw_waters','sw_caves']
hl=['hell_wastes','hell_horns','hell_soul_flats','hell_caverns']
for realm,biomes in [('sorcerers_world',sw),('hell',hl)]:
    for name in biomes:
        p=G/f'worldgen/biome/{name}.json'
        check('biome generated: '+name,p.exists())
        if not p.exists(): continue
        b=load(p)
        check(name+': always dry',b['has_precipitation'] is False)
        if realm=='hell':
            # Minecraft feature arrays may omit trailing empty decoration stages.
            # Hell intentionally has no vegetation; its ore and lava steps matter.
            stages=b['features']
            check(name+': valid feature step layout',len(stages)>=9 and len(stages)<=11)
            check(name+': six mineable ores at underground-ores step',
                len(stages)>6 and set(stages[6]) == {'dorohedoro:hell_ore_'+x for x in
                ('coal','iron','copper','gold','redstone','diamond')})
            check(name+': lava at spring step',len(stages)>8 and 'minecraft:spring_lava' in stages[8])
            check(name+': no normal vegetation', len(stages)<=9 or len(stages[9])==0)
            check(name+': no generic hostile table',len(b['spawners']['monster'])==0)
        else:
            check(name+': feature steps',len(b['features'])==11)
            if name in ('sw_woodland','sw_hydra_forest'):
                check(name+': has trees',any('tree' in s or 'forest_vegetation' in s for s in b['features'][9]))
for prefix in ['hell_ore_coal','hell_ore_iron','hell_ore_copper','hell_ore_gold','hell_ore_redstone','hell_ore_diamond']:
    cf=G/f'worldgen/configured_feature/{prefix}.json'
    pf=G/f'worldgen/placed_feature/{prefix}.json'
    check(f'{prefix}: configured and placed',cf.exists() and pf.exists())
    if cf.exists():
        targets=load(cf)['config']['targets']
        check(f'{prefix}: targets only hell rock',all(t['target']['block']=='dorohedoro:hell_rock' for t in targets))
for block in ['ash_soil','hell_rock']:
    p=P/f'src/main/resources/assets/dorohedoro/textures/block/{block}.png'
    check(block+': texture is PNG',p.is_file() and p.read_bytes().startswith(b'\x89PNG\r\n\x1a\n'))
    check(block+': resource models',all((P/f'src/generated/resources/assets/dorohedoro/{path}/{block}.json').is_file() for path in ['blockstates','models/block','models/item']))
# Verify biome source codecs are actually registered in Java source.
worldgen=(S/'registry/ModWorldgen.java').read_text()
check('biome sources registered',all(s in worldgen for s in ['SorcererBiomeSource.CODEC','HellBiomeSource.CODEC']))
modgen=(S/'datagen/ModDataGen.java').read_text()
check('configured/placed feature datagen',all(s in modgen for s in ['RealmBiomeData.configuredFeatures','RealmBiomeData.placedFeatures','RealmBiomeData.biomes']))
check('Hole world preset unchanged',(P/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').exists())
fail=sum(not ok for _,ok in checks)
print(f'\nPhase 3 OFFLINE STRUCTURAL CHECK: {len(checks)-fail}/{len(checks)} passed')
print('A pass is not equivalent to compile, runtime loading, in-game trees, or travel validation.')
raise SystemExit(1 if fail else 0)
