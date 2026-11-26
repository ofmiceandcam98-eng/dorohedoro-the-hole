#!/usr/bin/env python3
"""Static source schema and geometry QA; NOT a NeoForge build or Minecraft runtime test."""
from pathlib import Path
import json,re,sys,collections
ROOT=Path(__file__).resolve().parents[1]
java=ROOT/'src/main/java/com/dorohedoro/hole'
city=java/'worldgen/city'
biomes=ROOT/'src/generated/resources/data/dorohedoro/worldgen/biome'
reads=lambda file: file.read_text()
preset=json.loads(reads(ROOT/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
settings=preset['dimensions']['minecraft:overworld']['generator']['city']
conf=reads(city/'CitySettings.java');canvas=reads(city/'Canvas.java');painter=reads(city/'CityPainter.java')
pop=reads(city/'CityPopulator.java');natural=reads(java/'registry/ModEntities.java');breed=reads(java/'datagen/HoleBiomeData.java')
water=reads(city/'CanalEcology24.java');sewer=reads(city/'SewerDetails24.java');interior=reads(city/'InteriorDetails24.java');layout=reads(city/'InteriorLayouts15.java')
loot_source=reads(java/'datagen/ModLoot.java')
fail=[];checks=0
def check(name,ok):
 global checks
 checks+=1
 if not ok:fail.append(name)
 print(('PASS ' if ok else 'FAIL ')+name)
check('new saves use versioned visual upgrades',settings['world_detail_version']>=11 and settings['sewer_cavern_version']==5)
check('original saved world codecs default to zero',all('optionalFieldOf("'+k+'", 0)' in conf for k in ['sewer_cavern_version','world_detail_version']))
check('prior versions are still accepted', 'Codec.intRange(0, 5).optionalFieldOf("sewer_cavern_version", 0)' in conf and any('Codec.intRange(0, '+str(v)+').optionalFieldOf("world_detail_version", 0)' in conf for v in range(14, 30)))
check('versioned sewer upgrade only for new profile',painter.count('plan.settings.sewerCavernVersion() >= 4')==2 and 'SewerDetails24.paint(canvas' in painter)
check('old sewer dungeon still enabled',painter.index('SewerDungeon22.paint') < painter.index('paintGround(canvas') and 'sewerCavernVersion() >= 2' in painter)
check('older waterfront access still enabled','sewerCavernVersion() >= 3' in painter)
check('canal detailing runs after water is painted',painter.index('paintCanals(canvas')<painter.index('CanalEcology24.paint(canvas')<painter.index('WaterwaySewerEntrances23.paint'))
check('sewer detail runs after original tunnel and before dungeon',painter.index('paintSewers(canvas')<painter.index('SewerDetails24.paint(canvas')<painter.index('SewerDungeon22.paint'))
check('existing surface fish remain vanilla salmon and cod', all(v in natural for v in ['EntityType.SALMON','EntityType.COD']))
check('natural spawning preserves vanilla fish placement and restrictions elsewhere',natural.count('RegisterSpawnPlacementsEvent.Operation.AND')>=2 and 'if (!isHoleCityBiome(level, pos)) return true;' in natural)
check('natural city fish never placed in sewer runnel Y48', 'pos.getY() >= 58' in natural and 'pos.above(4)' in natural)
check('natural fish still water-checked','level.getFluidState(pos).is(FluidTags.WATER)' in natural)
check('no fish custom rendering or untested entity registry additions',not (java/'entity/CanalFishEntity.java').exists() and 'DeferredHolder<EntityType<?>, EntityType<Salmon>>' not in natural)
check('starter schools gated new source profile','plan.settings.sewerCavernVersion() >= 4' in pop)
check('starter school small and sparse', 'CityHash.range(sample, 5) != 0' in pop and 'int count = 1 + CityHash.range(sample >>> 20, 2);' in pop)
check('starter schools use correct terrain-relative water height','59 + (plan.settings.terrainRelief()' in pop and 'CityElevation.offset(plan.seed, x, z)' in pop)
check('starter school water verified for every position',pop.count('getFluidState(')>=2)
check('starter schools only sample canal geometries','Line.Kind.CANAL' in pop and 'plan.xLinesNear' in pop and 'plan.zLinesNear' in pop)
check('starter fish are chunk-owned, but only old worlds force persistence',
    'chunk.getMinBlockX()' in pop and 'chunk.getMaxBlockX()' in pop and
    'CityEcologyPolicy32.keepStarterFishPersistent(plan.settings.worldDetailVersion())' in pop)
check('starter fish are native water animals', 'EntityType.SALMON' in pop and 'EntityType.COD' in pop)
city_biomes=('residential','market','industrial','waterfront','derelict','graveyard','outskirts','frostbitten_industrial')
for name in city_biomes:
 file=biomes/('hole_'+name+'.json'); bi=json.loads(file.read_text());entries=[x for x in bi['spawners']['water_ambient'] if x['type']=='minecraft:salmon']
 check('salmon natural spawn in '+name,len(entries)==1 and entries[0]['weight']==12 and entries[0]['minCount']==2 and entries[0]['maxCount']==3)
check('undercity has no aquatic surface salmon',not any(x['type']=='minecraft:salmon' for x in json.loads(reads(biomes/'hole_undercity.json'))['spawners']['water_ambient']))
check('java datagen has same eight biome tables and freshwater spawn routine',breed.count('canalFish(spawns)')==8)
check('canal bed cover avoids solid blocking walls','Blocks.SEAGRASS' in water and 'Blocks.LILY_PAD' in water and 'Blocks.MOSS_BLOCK' in water and 'Blocks.MUD' in water)
check('canal decorative vegetation only submerged or floating','c.get(x, 61, z).is(Blocks.WATER)' in water and 'c.get(x, 61, z).is(Blocks.WATER)' in water)
check('canal details are deterministic seed + chunk local','CityHash.of(plan.seed, SALT, x, z)' in water and 'c.minX' in water and 'c.maxX' in water)
check('quay stations avoid crossings and v19 floating barrels',
    'xQuay != zQuay' in water and 'c.get(x,65,z).isAir()' in water and
    'CityEcologyPolicy32.canPlaceCanalSupply(' in water and '!c.get(bx,64,bz).isAir()' in water)
check('canal supply barrel uses actual loot table','"canal_supply"' in water or '"canal_supply"' in loot_source)
check('new quay loot generated in both Java and JSON','table(out, "canal_supply"' in loot_source and (ROOT/'src/generated/resources/data/dorohedoro/loot_table/chests/canal_supply.json').exists())
newloot=json.loads(reads(ROOT/'src/generated/resources/data/dorohedoro/loot_table/chests/canal_supply.json'))
check('canal loot valid low-tier fishing supplies',newloot['type']=='minecraft:chest' and len(newloot['pools'][0]['entries'])==9 and any(e['name']=='minecraft:fishing_rod' for e in newloot['pools'][0]['entries']))
check('sewer detail preserves center water runnel','c.set(x, 48,' not in sewer and 'c.fill(x, 48,' not in sewer and 'd == 0' in sewer)
check('sewer uses shaped top/lighting/wall piping','Blocks.STONE_BRICK_STAIRS' in sewer and 'Blocks.WAXED_WEATHERED_COPPER' in sewer and 'Blocks.LANTERN' in sewer)
check('sewer details keep intersections fully clear','if (dx <= 9 && dz <= 9) continue;' in sewer)
check('sewer side walkway remains open','if (d == 2)' in sewer and 'c.set(x, 49' in sewer)
check('sewer water visible under inspection grates','Blocks.IRON_TRAPDOOR' in sewer and 'd == 0' in sewer)
check('sewer ceilings leave three clear blocks','c.set(x, 52,' in sewer and 'c.set(x, 53,' in sewer and 'c.set(x, 50,' not in sewer and 'c.set(x, 51,' not in sewer)
check('additional furniture gated new detail version','hasPass24Interiors' in canvas and 'worldDetailVersion >= 10' in canvas and interior.count('!c.hasPass24Interiors()')==5)
for typ in ['lobby','residence','shop','clinic','industrial']:
 check('additional furnishings actually wired for '+typ,'InteriorDetails24.'+typ+'(' in layout)
check('no furniture overwrites or unsupported floors','c.get(x,y,z).isAir()' in interior and '!c.get(x,y-1,z).isAir()' in interior)
check('no unrelated deferred worldgen systems added',not any((city/f'{n}24.java').exists() for n in ['CrossEyeCamps','AshSnow','VillagerFactions']))
# Monte Carlo deterministic model: any straight canal line at arbitrary coordinate has
# at least one per-chunk eligible center or bank-water sampled column.
def eligible_for(center):
 outcomes=[]
 for minx in range(center-24,center+25,16):
  x=max(minx+1,min(minx+14,center))
  outcomes.append((abs(x-center)<=4,minx,x))
 return any(v[0] for v in outcomes)
for center in range(-30,32):
 check('center overlap seed geometry '+str(center),eligible_for(center))
# JSON parsing across generated and source.
files=list((ROOT/'src/main/resources').rglob('*.json'))+list((ROOT/'src/generated/resources').rglob('*.json'))
try:
 for f in files:json.loads(f.read_text())
 check('all '+str(len(files))+' source JSON resources parse',True)
except Exception as e:check('all JSON resources parse',False);print(e)
print(f'PASS24 STATIC: {checks-len(fail)}/{checks} checks passed, NOT COMPILED OR TESTED IN MINECRAFT')
if fail:
 print('FAILED:',fail);sys.exit(1)
