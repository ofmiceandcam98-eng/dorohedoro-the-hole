#!/usr/bin/env python3
"""Offline only: verify assets and sources, not Minecraft runtime or biome reachability."""
from pathlib import Path
import struct
import json,re
root=Path(__file__).resolve().parents[1]
src=root/'src/main/java/com/dorohedoro/hole'
main=root/'src/main/resources'
gen=root/'src/generated/resources'
ids={'sewer_roach':'SewerRoach','scrap_rat':'ScrapRat','sewer_leech':'SewerLeech','glow_moth':'GlowMoth','sludge_crawler':'SludgeCrawler','rust_eater_beetle':'RustEaterBeetle','cavern_burrower':'CavernBurrower','spore_crawler':'SporeCrawler'}
material_ids={'sludge_gland','coagulated_tar','corrosive_gland','metallic_carapace','burrower_claw','dense_hide','spore_sac','fungal_membrane','antidote_poultice'}
items=(src/'registry/ModItems.java').read_text()
entity_registry=(src/'registry/ModEntities.java').read_text()
render_registry=(src/'client/entity/HoleEntityRenderers.java').read_text()
biome=json.loads((gen/'data/dorohedoro/worldgen/biome/hole_undercity.json').read_text())
allrecipes={p.stem:json.loads(p.read_text()) for path in [main/'data/dorohedoro/recipe',gen/'data/dorohedoro/recipe'] if path.exists() for p in path.glob('*.json')}
outputs={r.get('result',{}).get('id') for r in allrecipes.values() if 'result' in r}
inputs={e['item'] for r in allrecipes.values() for e in r.get('ingredients',[]) if isinstance(e,dict) and 'item' in e}
count=0
def check(condition,msg):
 global count
 count+=1
 if not condition:raise AssertionError(msg)
for name,cls in ids.items():
 geometry=json.loads((root/'docs/model_geometry'/f'{name}.json').read_text())
 cubes=[c for cs in geometry['groups'].values() for c in cs]
 java=(src/'client/entity'/f'{cls}Model.java').read_text()
 texture=main/'assets/dorohedoro/textures/entity'/f'{name}.png'
 content=texture.read_bytes()
 check(content[:8]==b'\x89PNG\r\n\x1a\n',f'{name} is not a PNG')
 check(tuple(struct.unpack('>II',content[16:24]))==tuple(geometry['texture_size']),f'{name} texture/model size mismatch')
 check(len(content)>1000,f'{name} texture unusually small')
 check(len(cubes)>=20,f'{name} model too simple')
 check(java.count('.addBox(')==len(cubes),f'{name} Java geometry differs from design data')
 check('LayerDefinition.create(mesh, '+', '.join(map(str,geometry['texture_size']))+')' in java,f'{name} atlas not matched')
 check((src/'entity'/f'{cls}Entity.java').exists(),f'{name} missing entity')
 check((src/'client/entity'/f'{cls}Renderer.java').exists(),f'{name} missing renderer')
 check(f'ModEntities.{re.sub("([a-z])([A-Z])",r"\1_\2",cls).upper()}.get()' in render_registry,f'{name} missing renderer registration')
 check(f'"{name}"' in entity_registry,f'{name} missing registry')
 check((main/'data/dorohedoro/loot_table/entities'/f'{name}.json').exists(),f'{name} missing loot table')
 check(any(s['type']=='dorohedoro:'+name for pool in biome['spawners'].values() for s in pool),f'{name} missing biome spawn')
 for c in cubes:
  u,v=c['uv'];w,h,d=c['dim']
  check(u+int(2*(w+d))+1<=geometry['texture_size'][0] and v+int(h+d)+1<=geometry['texture_size'][1],f'{name} UV out of bounds for {c["name"]}')
for item in material_ids:
 check('"'+item+'"' in items,f'{item} not registered')
 check((main/'assets/dorohedoro/textures/item'/f'{item}.png').exists(),f'{item} missing art')
 check((main/'assets/dorohedoro/models/item'/f'{item}.json').exists(),f'{item} missing model')
 check('dorohedoro:'+item in (outputs if item=='antidote_poultice' else inputs),f'{item} lacks a survival recipe path')
check('dorohedoro:antidote_poultice' in outputs,'antidote has no crafting path')
check('MobEffects.POISON' in (src/'item/AntidotePoulticeItem.java').read_text(),'antidote does not cure poison')
print(f'PASS: {count} integrated entity/geometry/UV/texture/loot/spawn/crafting checks')
print('NOTE: No NeoForge build, Minecraft renderer, spawn distribution, save migration, or player testing performed.')
