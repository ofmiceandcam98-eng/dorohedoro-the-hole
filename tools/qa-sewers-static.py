#!/usr/bin/env python3
"""Offline sewer worldgen/source checks. DOES NOT compile or prove Minecraft runtime."""
from pathlib import Path
import json,struct,sys
P=Path(__file__).resolve().parents[1]
S=P/'src/main/java/com/dorohedoro/hole'
A=P/'src/main/resources'
G=P/'src/generated/resources'
checks=[]
def check(desc,ok):
 checks.append((desc,bool(ok)));print(('PASS ' if ok else 'FAIL ')+desc)
for p in [A/'data/dorohedoro/worldgen/noise_settings/hole.json',A/'data/dorohedoro/worldgen/noise_settings/hole_sewers.json',A/'data/minecraft/worldgen/world_preset/normal.json']:
 check(f'{p.name} loads',isinstance(json.loads(p.read_text()),dict))
old=json.loads((A/'data/dorohedoro/worldgen/noise_settings/hole.json').read_text());new=json.loads((A/'data/dorohedoro/worldgen/noise_settings/hole_sewers.json').read_text());wp=json.loads((A/'data/minecraft/worldgen/world_preset/normal.json').read_text())
check('old caves retained for existing saved worlds',old['aquifers_enabled'] and old['noise_router']['final_density']['type'] != 'minecraft:y_clamped_gradient')
check('deep natural caves preserved below Y=24',new['noise_router']['final_density']['type']=='minecraft:range_choice'
      and new['noise_router']['final_density']['when_in_range']==old['noise_router']['final_density']
      and new['noise_router']['final_density']['max_exclusive']==24)
check('shallow strata solid around sewer layers',new['noise_router']['final_density']['when_out_of_range']['type']=='minecraft:y_clamped_gradient')
check('deep aquifers available for vanilla mining',new['aquifers_enabled'])
check('underground structure feature source preserved',old['surface_rule']==new['surface_rule'] and old['ore_veins_enabled']==new['ore_veins_enabled'])
check('new mode preserves ore veins',new['ore_veins_enabled'] and 'vein_toggle' in new['noise_router'])
check('new mode keeps bedrock surface rule', 'bedrock' in json.dumps(new['surface_rule']))
check('new preset references new worldgen',wp['dimensions']['minecraft:overworld']['generator']['settings']=='dorohedoro:hole_sewers' and wp['dimensions']['minecraft:overworld']['generator']['city']['sewer_underworld'])
config=(S/'worldgen/city/CitySettings.java').read_text();city=(S/'worldgen/city/CityPainter.java').read_text();qa=(S/'qa/HoleQa.java').read_text()
check('existing saves decode to no sewer change','optionalFieldOf("sewer_underworld", false)' in config and 'LEGACY_CAVE_FREQUENCY' in config)
check('new worlds select 30 percent cave approaches', wp['dimensions']['minecraft:overworld']['generator']['city'].get('sewer_cave_frequency')==30)
check('new worlds opt into improved cavern geometry',wp['dimensions']['minecraft:overworld']['generator']['city'].get('sewer_cavern_version')==1)
check('older saves keep original cavern geometry', 'optionalFieldOf("sewer_cavern_version", 0)' in config)
check('old worlds retain legacy sewer branch profile', 'optionalFieldOf("sewer_cave_frequency", SewerBranchPolicy.LEGACY_CAVE_FREQUENCY)' in config)
policy=(S/'worldgen/city/SewerBranchPolicy.java').read_text()
check('legacy branch function remains unchanged', 'CityHash.range(CityHash.of(seed, SALT, xIndex, zIndex), 11)' in policy)
check('adjustable cave percent selector is deterministic', 'roll < caveFrequency' in policy and 'INSPECTION_FREQUENCY = 18' in policy)

for x in ['paintDeepExcavations','paintMaintenanceTunnels','paintCisterns','paintGalleries','paintSewers','paintShafts']:
 check('city generation has '+x,x in city)
check('vertical shafts open into maintenance level','boolean maintenance = y >= 30 && y <= 34' in city)
check('new QA validates deep chamber and drainage','sewer_deep_chamber_carved' in qa and 'sewer_midlevel_water' in qa)
check('new cisterns avoid utility shafts','if (plan.hasShaft(xLine.index(), zLine.index())) continue' in city)
check('starter chambers always generated','boolean starter = (i == -1 || i == 0) && (j == -1 || j == 0);' in city)
for path in [S/'registry/ModBlocks.java',S/'datagen/ModRecipes.java',S/'datagen/ModModels.java',S/'datagen/ModLoot.java',S/'datagen/ModDataGen.java']:
 check('sewer brick linked in '+path.name,'SEWER_BRICKWORK' in path.read_text())
image=A/'assets/dorohedoro/textures/block/sewer_brickwork.png'
raw=image.read_bytes() if image.exists() else b''
check('original 16px texture',raw[:8]==b'\x89PNG\r\n\x1a\n' and len(raw)>=24 and struct.unpack('>II',raw[16:24])==(16,16))
lang=json.loads((A/'assets/dorohedoro/lang/en_us.json').read_text());check('material translated','block.dorohedoro.sewer_brickwork' in lang)
for rel in ['assets/dorohedoro/blockstates/sewer_brickwork.json','assets/dorohedoro/models/block/sewer_brickwork.json','assets/dorohedoro/models/item/sewer_brickwork.json','data/dorohedoro/loot_table/blocks/sewer_brickwork.json','data/dorohedoro/recipe/sewer_brickwork.json']:
 f=G/rel;check('resource '+rel,f.exists() and isinstance(json.loads(f.read_text()),dict))
branch=(S/'worldgen/city/SewerCaveBranches.java').read_text()
check('extended cavern geometry uses saved version', 'sewerCavernVersion() == 1' in branch)
check('walkable stair landing is painted', 'SewerCavernProfile.entranceFloor(seed, bx, cz, direction,' in branch)
check('long cave connections implemented','paintCaveApproach' in branch and 'step <= 49' in branch)
check('closed inspection branches implemented','paintClosedInspectionBay' in branch)
check('most branches do not reach caves','branchKind' in branch and 'type == 0' in branch and 'type == 1 || type == 2' in branch)
check('staged passage is first masonry then broken then stone',all(t in branch for t in ['step < 19','step < 31','Blocks.DEEPSLATE']))
check('cave connection deliberately deep','int mouthZ = cz + direction * 59' in branch)
check('lower galleries have a protective shell','d <= 3' in city and 'SewerCaveBranches.paint' in city)
check('runtime QA covers cave approach and destination','sewer_branch_begins_lined' in qa and 'sewer_selective_cave_destination' in qa)
check('unique salvage chest registered','table(out, "sewer_cache"' in (S/'datagen/ModLoot.java').read_text() and '"sewer_cache"' in branch)
loot=G/'data/dorohedoro/loot_table/chests/sewer_cache.json'
check('sewer cache loot generated',loot.exists() and json.loads(loot.read_text()).get('type')=='minecraft:chest' if loot.exists() else False)
fail=sum(not yes for _,yes in checks)
print(f'\nSEWER STRUCTURAL CHECK: {len(checks)-fail}/{len(checks)} passed (NOT a runtime build)')
sys.exit(1 if fail else 0)
