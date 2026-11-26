#!/usr/bin/env python3
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
code=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda name:(code/name).read_text()
settings=read('CitySettings.java')
city=read('CityPlan.java')
chooser=read('StartingRegions47.java')
grade=read('SpawnApproach50.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
assert preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=33
checks={
 'v33 new-world opt-in':'world_detail_version' in (root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text(),
 'codec and constructor accept 0..33':'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings,
 'old level.dat default remains 0':'optionalFieldOf("world_detail_version", 0)' in settings,
 'legacy region chooser not removed':'public static Site choose(long seed, Probe probe)' in chooser,
 'old saved v31-v32 select unchanged':'settings.worldDetailVersion() >= 33' in city and ': StartingRegions47.choose(seed, probe)' in city,
 'preferred biome searched first':'Site initial = choose(seed, probe);' in chooser,
 'alternate frontier search bounded':'MAX_ATTEMPTS / 4' in chooser,
 'downtown intentional option':'if (requested == Origin.CITY)' in chooser,
 'landing actual grade based on frontage':'CityTerrain40.frontageOffset(this, landing)' in city,
 'both lots height checked':'SpawnApproach50.isWalkable(x, z' in city,
 'four-wide central public aisle checked':'dx <= 17' in grade and 'dx = 14' in grade,
 'west and east egress elevation checked':'x - 14' in grade and 'x + 37' in grade,
 'no changes to existing biome registry': 'HoleRegions42.terrainBlend(climate, x, z)' in city,
 'no change to 30pct cave link frequency':preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cave_frequency']==30,
 'all JSON valid':all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL'), name)
print(f'{sum(checks.values())}/{len(checks)} Pass50 structural checks (not game verification)')
assert all(checks.values())
