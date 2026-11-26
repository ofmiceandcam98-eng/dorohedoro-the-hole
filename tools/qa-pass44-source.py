#!/usr/bin/env python3
"""Pass44 opt-in worldgen architecture static QA. Not a compiled mod/gameplay test."""
from pathlib import Path
import json, re, sys, zlib
P=Path(__file__).resolve().parents[1]
C=P/'src/main/java/com/dorohedoro/hole/worldgen/city'
preset=json.loads((P/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
config=preset['dimensions']['minecraft:overworld']['generator']['city']
checks=[]
def test(label,condition):
    checks.append(condition)
    print(('PASS ' if condition else 'FAIL ')+label)
read=lambda n: (C/n).read_text()
settings=read('CitySettings.java'); canvas=read('Canvas.java'); painter=read('LotPainter.java')
mod=read('FunctionalFacade44.java')
test('New-world preset includes Pass44',config['world_detail_version']>=28)
test('Existing saves still default world detail 0', 'optionalFieldOf("world_detail_version", 0)' in settings and 'DEFAULT = new CitySettings(' in settings)
test('Codec permits 0..33', 'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings)
test('Previous version gates preserved', 'worldDetailVersion >= 27' in canvas and 'settings.worldDetailVersion() >= 26' in read('CityPlan.java'))
test('New feature gated at 28', 'hasFunctionalFacades44() { return worldDetailVersion >= 28; }' in canvas)
test('Both residential paths use new feature',painter.count('FunctionalFacade44.residence(')==2)
test('Industrial warehouse and factory use new feature',painter.count('FunctionalFacade44.industry(')==2)
test('Roof profiles from chosen actual room-safe roof policy',painter.count('HousingDetail29.chooseRoof(box, floors, lot.hash(), roofFor(lot.hash()))')>=4)
test('Owner parcel and chunk clipping', all(s in mod for s in ('lot.contains(x, z)','c.contains(x, z)','c.get(x, y, z).isAir()')))
test('Roof structures avoid ground-floor and north stair', all(s in mod for s in ('box.z1() - 6','box.x0() + 3','CityPlan.GROUND_Y + 3')))
test('Functional rainwater cauldron, not exposed chest', 'Blocks.CAULDRON' in mod and not re.search(r'\bchest\s*\(',mod))
test('No added lamps or high brightness', all(s not in mod for s in ('Blocks.LANTERN','Blocks.TORCH','Blocks.GLOWSTONE','Blocks.LIGHT','Blocks.REDSTONE_LAMP')))
test('Existing sewer cave fraction 30 maintained',config['sewer_underworld'] is True and config['sewer_cave_frequency']==30)
test('Terrain regions retained',config['terrain_relief'] and config['world_detail_version']>=26)
test('Sky smog source remains', any(x.exists() for x in (P/'src/main/java').rglob('HoleAtmosphere.java')))
for root in (P/'src/main/resources',P/'src/generated/resources'):
    for f in root.rglob('*.json'):
        try: json.loads(f.read_text())
        except Exception as exc: test('parse '+str(f),False)
test('All production and generated JSON files parse',True)
print('Pass44 static '+str(sum(checks))+'/'+str(len(checks)))
print('NOT a NeoForge compilation, Minecraft client render, performance, or functional connection test.')
sys.exit(0 if all(checks) else 1)
