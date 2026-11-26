#!/usr/bin/env python3
"""Pass51 source contracts. Static/offline only, not a NeoForge build."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
source=lambda name:(city/name).read_text()
plan=source('CityPlan.java')
route=source('SpawnRoute51.java')
settings=source('CitySettings.java')
orig=source('StartingRegions47.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
 'new worlds opt into version 34':preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=34,
 'codec accepts version 34':'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
 'missing version still defaults to legacy zero':'optionalFieldOf("world_detail_version", 0)' in settings,
 'v31-v32 retain legacy choice':': StartingRegions47.choose(seed, probe)' in plan,
 'v33 retains Pass50 path check':'settings.worldDetailVersion() < 33' in plan and 'SpawnApproach50.isWalkable' in plan,
 'v34 adds separate southern route check':'settings.worldDetailVersion() < 34' in plan and 'SpawnRoute51.accessible' in plan,
 'v34 checks all 27 eastward steps':'dx = 1; dx <= 27' in route,
 'v34 samples shelter front at z+14':'final int frontageZ = z + 14' in route,
 'v34 guards both public lane exits':'x - 14' in route and 'x + 37' in route,
 'v34 inspects two south turns':'new int[]{0, 27}' in route and 'dz <= 17' in route,
 'legacy start-point choice cached under lock':'synchronized (this)' in plan and 'result = selectedStart;' in plan,
 'legacy zero spawn retained':'new BlockPos(0, GROUND_Y, 0)' in plan,
 'regional shelters remain furnished':'LotType.STARTER_SHELTER' in source('Superblock.java'),
 'versioned climate biomes kept':'HoleRegions42.Region.valueOf(preferred.name())' in plan,
 '30-percent cave links retained':preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cave_frequency']==30,
 'nightlight reduction preserved':'plan.settings.worldDetailVersion() >= 31' in source('CityPainter.java'),
 'all project JSON parses':all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL')+' '+name)
print(f'{sum(checks.values())}/{len(checks)} Pass51 source checks')
assert all(checks.values())
print('WARNING: no real NeoForge compile, pathfinding, spawned terrain or Minecraft client verification.')
