#!/usr/bin/env python3
"""Pass 65 source checks only; real Minecraft world generation remains unverified."""
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
def content(rel): return (r/rel).read_text()
base='src/main/java/com/dorohedoro/hole/'
biome=content(base+'worldgen/HoleBiomeSource.java')
gen=content(base+'worldgen/HoleChunkGenerator.java')
city='src/main/java/com/dorohedoro/hole/worldgen/city/'
ocean=content(city+'OceanRegions65.java')
sea=content(city+'OceanBathymetry65.java')
plan=content(city+'CityPlan.java')
canvas=content(city+'Canvas.java')
pop=content(city+'CityPopulator.java')
settings=content(city+'CitySettings.java')
preset=json.loads(content('src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
w=preset['dimensions']['minecraft:overworld']['generator']
checks={
'new worlds explicitly use v47':w['city']['world_detail_version']>=47,
'old missing version defaults to zero':'optionalFieldOf("world_detail_version", 0)' in settings,
'v47 version accepted consistently':'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings,
'ocean flag enabled in new preset':w['biome_source']['ocean_regions'] is True,
'ocean flag absent in previous worlds defaults false':'optionalFieldOf("ocean_regions", false)' in biome,
'overworld generator keeps vanilla Nether and End':all(x in preset['dimensions'] for x in ('minecraft:the_end','minecraft:the_nether')),
'vanilla ocean and deep-ocean biomes used':'Biomes.OCEAN' in biome and 'Biomes.DEEP_OCEAN' in biome,
'cold ocean variants possible':'Biomes.COLD_OCEAN' in biome and 'Biomes.DEEP_COLD_OCEAN' in biome,
'old biome source does not expose ocean IDs': 'return frontier.oceanRegions()' in biome and 'Stream.concat(legacy,' in biome,
'climate determines full biome columns':'OceanRegions65.depthAt(sampler, blockX, blockZ)' in biome,
'actual sea floor uses same policy':'OceanRegions65.depthAt(climate, x, z)' in plan and 'plan.oceanDepthAt(minX + dx, minZ + dz)' in sea,
'oceans have deep water':'29.0 * t * t' in ocean and 'depth >= 16' in biome,
'candidate basins avoid regional starts':'LANDING_EXCLUSION = 5200' in ocean and 'BASIN_CENTER_EXCLUSION = 6400' in ocean,
'deterministic large ocean provinces':'CELL = 2048' in ocean and 'CityHash.of(' in ocean,
'bathymetry processes shore columns':'depths[(x - minX) + ((z - minZ) << 4)]' in sea,
'land chunks avoid extra ocean Canvas':'if (!hasOcean && !hasCoast) return;' in sea,
'fresh ocean applied after city surface':gen.index('CityPainter.paint(cityPlan, chunk, protectedStructures);') < gen.index('OceanBathymetry65.paint(cityPlan, chunk, protectedStructures);'),
'ocean gate preserves old worlds':'if (citySettings.worldDetailVersion() >= 47)' in gen and 'if (plan.settings.worldDetailVersion() < 47) return;' in sea,
'ocean painter uses water and replaces skyscrapers':'Blocks.WATER' in sea and 'Blocks.AIR' in sea and 'CLEAR_TOP = 176' in sea,
'ocean placement uses ungraded sea level':'setRaw(x, y, z' in sea and 'public void setRaw(int x, int worldY, int z' in canvas,
'ocean writer honors vanilla structure protection':'protectsVanillaWrite(x, worldY, z)' in canvas,
'urban writers skip submerged columns BEFORE block entity creation':canvas.count('if (isOceanCityWrite(x, adjustedY, z)) return;')==4,
'city and sea painter share ocean depth policy':'terrainPlan.oceanDepthAt(x, z)' in canvas,
'city population avoids submerged spawns':'plan.oceanDepthAt(r.centerX(), r.centerZ()) > 0' in pop,
'stronghold protection still enabled':'VanillaStructureLookup64.forChunk' in gen,
'deep dark guard retained':'DeepDarkBuffer63.protects(' in canvas,
'30 percent sewer branch frequency retained':w['city']['sewer_cave_frequency']==30,
'full XML/JSON content parses':all(json.loads(f.read_text()) is not None for f in (r/'src/main/resources').rglob('*.json')),
'no invented forgotten-city structure':not (r/(city+'ForgottenCity.java')).exists()
}
for label,ok in checks.items(): print(('PASS' if ok else 'FAIL'),label)
print(f'PASS65 checks {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
