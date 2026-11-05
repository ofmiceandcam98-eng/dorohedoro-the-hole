#!/usr/bin/env python3
"""Offline source+asset coherence, NOT a Minecraft runtime validator."""
from pathlib import Path
from PIL import Image
import re,json
P=Path(__file__).resolve().parents[1]
J=P/'src/main/java/com/dorohedoro/hole'
R=P/'src/main/resources'
G=P/'src/generated/resources'
registry=(J/'registry/ModEntities.java').read_text()
render=(J/'client/entity/HoleEntityRenderers.java').read_text()
items=(J/'registry/ModItems.java').read_text()
blocks=(J/'registry/ModBlocks.java').read_text()
gen=(J/'datagen/HoleMineralFeatures.java').read_text()
biomes=(J/'datagen/HoleBiomeData.java').read_text()
recipes=(J/'datagen/ModRecipes.java').read_text()
modeldata=(J/'datagen/ModModels.java').read_text()
check_count=0
def check(ok,description):
 global check_count
 check_count+=1
 if not ok:raise AssertionError('FAIL: '+description)

for key,cls in [('sewer_leech','SewerLeech'),('glow_moth','GlowMoth')]:
 camel=cls.upper();
 check(key in registry,'entity registered '+key)
 check(f'{camel}' not in registry or cls+'Entity::new' in registry,'entity factory '+key)
 check(f'{cls}Entity' in registry and f'event.put({"SEWER_LEECH" if cls=="SewerLeech" else "GLOW_MOTH"}.get()' in registry,'attributes '+key)
 check((J/f'entity/{cls}Entity.java').exists(),'entity code '+key)
 check((J/f'client/entity/{cls}Model.java').exists(),'model '+key)
 check((J/f'client/entity/{cls}Renderer.java').exists(),'renderer '+key)
 check(cls+'Renderer' in render and cls+'Model' in render,'renderer and model layer '+key)
 check(('SEWER_LEECH' if cls=='SewerLeech' else 'GLOW_MOTH') in biomes,'biome spawn '+key)
 loot=R/f'data/dorohedoro/loot_table/entities/{key}.json'
 check(loot.exists() and len(json.loads(loot.read_text())['pools'])>=2,'loot '+key)
 tex=R/f'assets/dorohedoro/textures/entity/{key}.png'
 # Entity atlas dimensions are data-driven because fidelity passes use individual textured UV islands.
 model_json=P/f'docs/model_geometry/{key}.json'
 with Image.open(tex) as image:
  expected=tuple(json.loads(model_json.read_text())['texture_size']) if model_json.exists() else (64,64)
  check(image.size==expected,'atlas size '+key)
for key in ('leech_mucus','leech_sucker','medical_salve','luminous_scale','moth_silk','glow_tonic','raw_zinc','zinc_ingot','brass_ingot','oil_shale_chunk','bitumen','sewer_sealant'):
 check('"'+key+'"' in items,'registered item '+key)
 check(key.upper() in recipes or '"'+key+'"' in recipes,'recipe reference '+key)
 tex=R/f'assets/dorohedoro/textures/item/{key}.png'
 with Image.open(tex) as image:check(image.size==(16,16),'item sprite '+key)
 check((G/f'assets/dorohedoro/models/item/{key}.json').exists(),'item model '+key)
for key in ('zinc_ore','oil_shale_ore','galvanized_plating','cavern_lamp'):
 check('"'+key+'"' in blocks,'block registered '+key)
 check(key.upper() in modeldata,'block datagen '+key)
 tex=R/f'assets/dorohedoro/textures/block/{key}.png'
 with Image.open(tex) as image:check(image.size==(16,16),'block sprite '+key)
 check((G/f'assets/dorohedoro/blockstates/{key}.json').exists(),'blockstate '+key)
 check((R/f'data/dorohedoro/loot_table/blocks/{key}.json').exists(),'block loot '+key)
for key in ('zinc_veins','oil_shale_veins'):
 check('\"'+key+'\"' in gen,'placed registration '+key)
 for kind in ('configured_feature','placed_feature'):
  check((G/f'data/dorohedoro/worldgen/{kind}/{key}.json').exists(),'worldgen '+kind+' '+key)
check('ModEntities.SEWER_LEECH' in biomes and 'ModEntities.GLOW_MOTH' in biomes,'both new mobs added to Undercity')
check('HoleMineralFeatures.ZINC_PLACED' in biomes and 'HoleMineralFeatures.OIL_SHALE_PLACED' in biomes,'both ores in biomes')
check('scaleCooldown = SHEAR_COOLDOWN' in (J/'entity/GlowMothEntity.java').read_text(),'renewable cooldown persisted')
check('tag.putInt("MothScaleCooldown"' in (J/'entity/GlowMothEntity.java').read_text(),'moth cooldown persisted to save')
check('MobEffects.REGENERATION' in items and 'MobEffects.NIGHT_VISION' in items,'usable consumable effects')
for k in ('medical_salve','glow_tonic','cavern_lamp','sewer_sealant_from_slime','galvanized_plating','zinc_ingot','bitumen_from_shale'):
 check((G/f'data/dorohedoro/recipe/{k}.json').exists(),'cached survival recipe '+k)
all_json=list(P.glob('src/**/*.json'))
for file in all_json:json.loads(file.read_text())
print(f'BATCH 02 OFFLINE QA: {check_count}/{check_count} checks passed; {len(all_json)} JSON parsed; runtime UNVERIFIED')
