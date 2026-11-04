#!/usr/bin/env python3
"""Verify version-gated source wiring and guard compatibility, not a Minecraft build."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
code=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
files={n:(code/n).read_text() for n in ['HoleRegions42.java','CityPlan.java','CityTerrain40.java','CitySettings.java']}
settings=files['CitySettings.java']
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
city=preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
    'fresh world retains v32+ terrain fix': city['world_detail_version']>=32,
    'old saved worlds keep codec default 0': 'optionalFieldOf("world_detail_version", 0)' in settings,
    'codec supports historical versions 0 through 33': 'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
    'old region blend unchanged': 'public static double terrainBlend(int x, int z)' in files['HoleRegions42.java'],
    'old saves use old terrain blend': 'settings.worldDetailVersion() >= 32' in files['CityPlan.java'] and ': HoleRegions42.terrainBlend(x, z)' in files['CityPlan.java'],
    'new v32 uses sampler-backed blend': 'HoleRegions42.terrainBlend(climate, x, z)' in files['CityPlan.java'],
    'CityTerrain40 uses versioned blend': 'plan.terrainBlendAt(x, z)' in files['CityTerrain40.java'],
    'only near province borders sample neighbor cells': 'fx < width' in files['HoleRegions42.java'] and 'fz >= CELL - width' in files['HoleRegions42.java'],
    'original climate selection shared with v32': 'return regionInCell(climate, cellX, cellZ);' in files['HoleRegions42.java'],
    'core protective mask maintained': 'CORE_RADIUS) / 192.0' in files['HoleRegions42.java'],
    'frontier spawning from Pass47 intact': 'StartingRegions47.choose(seed' in files['CityPlan.java'],
    'mining/sewer frequency unchanged': city['sewer_underworld'] and city['sewer_cave_frequency']==30,
    'all json parseable': all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for k,v in checks.items(): print(('PASS ' if v else 'FAIL ')+k)
print(f'Pass49 static: {sum(checks.values())}/{len(checks)}; NOT a NeoForge build/client test')
if not all(checks.values()):raise SystemExit(1)
