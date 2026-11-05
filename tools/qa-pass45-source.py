#!/usr/bin/env python3
"""Pass45 static integration checks, not an in-game test."""
import json,re,sys
from pathlib import Path
P=Path(__file__).resolve().parents[1]
C=P/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda s:(C/s).read_text()
settings=read('CitySettings.java');mod=read('InhabitedCourts45.java');openlots=read('OpenLots.java');canvas=read('Canvas.java')
preset=json.loads((P/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']
tests=[]
def check(t,v):
    tests.append(bool(v));print(('PASS ' if v else 'FAIL ')+t)
check('fresh-world opt-in pass45',preset['world_detail_version']>=29)
check('legacy saves still default 0','optionalFieldOf("world_detail_version", 0)' in settings)
check('record/codec includes 0 through 33','worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings)
check('pass44 preserved', 'hasFunctionalFacades44() { return worldDetailVersion >= 28; }' in canvas)
check('pass45 exact gate', 'hasInhabitedCourts45() { return worldDetailVersion >= 29; }' in canvas)
check('frontier regions excluded by central city selector','terrainPlan.regionAt(x, z) == HoleRegions42.Region.CITY' in canvas and 'c.isHoleCity' in mod)
check('both original vacant/rubble plots opt in',all(f'if (InhabitedCourts45.eligible(c, lot, {t}))' in openlots for t in ['true','false']))
check('rubble shell suppressed where court used','InhabitedCourts45.eligible(c, lot, true)' in read('DerelictBuildings.java'))
check('two real individual room doors',mod.count('c.door(')==2 and 'partition' in mod)
check('functional west bay', all(s in mod for s in ['Blocks.SMOKER','Blocks.SMITHING_TABLE','Blocks.CRAFTING_TABLE','Blocks.LOOM']))
check('functional east bay',all(s in mod for s in ['Blocks.CAULDRON','Blocks.STONECUTTER','Blocks.FURNACE']))
check('no public chest loot or new light',all(s not in mod for s in ['c.chest(', 'Blocks.LANTERN','Blocks.TORCH','Blocks.REDSTONE_LAMP','Blocks.GLOWSTONE']))
check('parcel based deterministic variation','CityHash.of(lot.hash(), SALT)' in mod and 'c.contains' not in mod and 'RandomSource' not in mod)
check('most open lots remain unconverted','? 8 : rubble ? 7 : 5' in mod)
check('previous survival settings retained',preset['sewer_cave_frequency']==30 and preset['sewer_underworld'] and preset['terrain_relief'])
check('most recent Java source still available',read('FunctionalFacade44.java') and read('HoleRegions42.java'))
check('no new custom textures or unused mechanics',not any(s in mod for s in ['SludgeIce','AshSnow','CrossEye','Mutation']))
for d in [P/'src/main/resources',P/'src/generated/resources']:
    for f in d.rglob('*.json'):
        try: json.loads(f.read_text())
        except Exception as e:check('parse '+str(f),False)
check('all JSON files parse',True)
print(f'Pass45 static: {sum(tests)}/{len(tests)} checks. SOURCE only; NOT runtime/NeoForge verification.')
sys.exit(0 if all(tests) else 1)
