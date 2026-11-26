#!/usr/bin/env python3
"""Pass52 cross-component source contracts. Not a Minecraft/NeoForge build."""
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
base=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda x: (base/x).read_text()
biome=(r/'src/main/java/com/dorohedoro/hole/worldgen/HoleBiomeSource.java').read_text()
config=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']
settings=read('CitySettings.java')
plan=read('CityPlan.java')
regions=read('HoleRegions42.java')
urban=read('UrbanPattern52.java')
lot=read('LotChooser.java')
backstreets=read('Backstreets43.java')
canvas=read('Canvas.java')
streets=read('CityPainter.java')
surface=read('RegionalSurface42.java')
checks={
    'fresh worlds explicitly opt in to world-detail version 35':config['city']['world_detail_version']>=35,
    'new biome flag enabled only in fresh-world preset':config['biome_source']['climate_across_city'] is True,
    'new biome flag defaults to false for historical worlds':'Codec.BOOL.optionalFieldOf("climate_across_city", false)' in biome,
    'biome source uses climate-anywhere when opted in':'climateAcrossCity ? HoleRegions42.atAnywhere' in biome,
    'plan uses same climate mapping as biome source':'settings.worldDetailVersion() >= 35 ? HoleRegions42.atAnywhere' in plan,
    'legacy climate rule hardcodes historical CITY core':'if (Math.max(Math.abs(x), Math.abs(z)) < CORE_RADIUS) return Region.CITY;' in regions,
    'old versions still use old climate rule':': settings.worldDetailVersion() >= 26 ? HoleRegions42.at(climate, x, z)' in plan,
    'new terrain blend accounts for v35 climate borders':'HoleRegions42.terrainBlendAnywhere(climate, x, z)' in plan,
    'old climate grading remains on saved v32-34 worlds':'HoleRegions42.terrainBlend(climate, x, z)' in plan,
    'legacy unversioned defaults are still 0':'optionalFieldOf("world_detail_version", 0)' in settings,
    'versioned codec and validation accept v35':'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
    'urban fabric not determined by climate':'HoleRegions42.Region' not in urban[:urban.index('    /** Used only for new worlds.')],
    'irregular geographic core not a square radius':'Math.hypot((double) x, (double) z)' in urban and 'Math.sin(x / 211.0)' in urban,
    'four distinct seeded urban fabric bands':'DENSE, INHABITED, DERELICT, OPEN_COUNTRY' in urban,
    'non-dense lots selected by seeded urbanization':'UrbanPattern52.choose(plan.regionAt' in lot and 'UrbanPattern52.at(plan.seed' in lot,
    'old v26-v34 lot mapping gated off in v35':'worldDetailVersion() < 35' in lot and lot.count('worldDetailVersion() < 35')>=2,
    'dense skyline no longer requires CITY biome':'UrbanPattern52.Fabric.DENSE' in canvas,
    'dense street surface applied in all climate areas':'UrbanPattern52.Fabric.DENSE' in backstreets,
    'open land streetlights suppressed regardless of biome':'UrbanPattern52.Fabric.OPEN_COUNTRY' in streets and 'placeLamp = false' in streets,
    'open terrain surfaces tied to urbanization':'UrbanPattern52.Fabric.OPEN_COUNTRY' in surface,
    'starter shelter and regional spawn retained':'LotType.STARTER_SHELTER' in read('Superblock.java') and 'StartingRegions47.chooseWithRegionalFallback' in plan,
    'sewer and cave access frequency unchanged':config['city']['sewer_cave_frequency']==30 and config['city']['sewer_underworld'],
    'source resources all valid JSON':all(json.loads(x.read_text()) is not None for x in (r/'src').rglob('*.json')),
}
for label,good in checks.items():print(('PASS' if good else 'FAIL')+' '+label)
print(f'Pass52 source integration: {sum(checks.values())}/{len(checks)} checks')
assert all(checks.values())
print('No NeoForge compilation, real seeded noise validation, client rendering or Minecraft server executed.')
