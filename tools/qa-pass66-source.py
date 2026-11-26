#!/usr/bin/env python3
"""Pass 66 new-world shore and beach registry integration; source QA, not NeoForge build."""
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
base=r/'src/main/java/com/dorohedoro/hole'
city=base/'worldgen/city'
def read(p):return p.read_text()
policy=read(city/'CoastalShelf66.java')
ocean=read(city/'OceanRegions65.java')
biomes=read(base/'worldgen/HoleBiomeSource.java')
plan=read(city/'CityPlan.java')
canvas=read(city/'Canvas.java')
painter=read(city/'OceanBathymetry65.java')
pop=read(city/'CityPopulator.java')
gen=read(base/'worldgen/HoleChunkGenerator.java')
settings=read(city/'CitySettings.java')
preset=json.loads(read(r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
w=preset['dimensions']['minecraft:overworld']['generator']
checks={
'new preset v48 gated':w['city']['world_detail_version']>=48,
'biome flag coastal shelves true':w['biome_source']['coastal_shelves'] is True,
'ocean flag true':w['biome_source']['ocean_regions'] is True,
'old city record defaults version 0':'optionalFieldOf("world_detail_version", 0)' in settings,
'v48 codec/validator match':'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
'old biome sources default beach mode off':'optionalFieldOf("coastal_shelves", false)' in biomes,
'old ocean flag default false':'optionalFieldOf("ocean_regions", false)' in biomes,
'vanilla beach holder registered':'RegistryOps.retrieveElement(Biomes.BEACH)' in biomes,
'vanilla snowy beach holder registered':'RegistryOps.retrieveElement(Biomes.SNOWY_BEACH)' in biomes,
'old saved biome list excludes beaches':'frontier.coastalShelves()' in biomes and 'Stream.concat(legacy,' in biomes,
'beach assigned only alongside actual ocean':biomes.index('if (frontier.coastalShelves())')>biomes.index('if (frontier.oceanRegions())'),
'beach uses actual ocean sampler':'CoastalShelf66.distanceAt(sampler, blockX, blockZ)' in biomes,
'beach width bounded':'distance <= CoastalShelf66.BEACH_WIDTH' in biomes,
'vanilla deep ocean still present':'Biomes.DEEP_OCEAN' in biomes,
'coast samples same ocean policy':'OceanRegions65.depthAt(climate' in policy,
'starter safety envelope intact':'LANDING_EXCLUSION - WIDTH' in policy,
'seed deterministic no world/chunk access':'Climate.Sampler' in policy and 'ChunkAccess' not in policy,
'grade from sea surface':'OceanRegions65.SEA_SURFACE_Y + 1' in policy,
'gradient 1 to 16 blocks': 'WIDTH = 16' in policy and 'BEACH_WIDTH = 7' in policy,
'surface grade no old world impact':'worldDetailVersion() >= 48' in plan and 'CoastalShelf66.distanceAt(climate' in plan,
'ocean depth unchanged':'settings.worldDetailVersion() >= 47 ? OceanRegions65.depthAt' in plan,
'city painter precomputes coastline mask':'dryCoastColumns66[index] = (byte) shoreDistance' in canvas,
'coast mask suppresses all urban block writers':'dryCoastColumns66[index] & 0xff' in canvas and canvas.count('if (isOceanCityWrite(x, adjustedY, z)) return;')==4,
'coastal painter supports land-only chunks':'if (!hasOcean && !hasCoast) return;' in painter,
'painter includes sand':'Blocks.SAND' in painter,
'painter includes gravel/coarse dirt':'Blocks.GRAVEL' in painter and 'Blocks.COARSE_DIRT' in painter,
'walkable coast clears artificial city towers':'for (int y = dryY + 1; y <= CLEAR_TOP; y++)' in painter,
'no unsolicited underground excavations':'for (int y = 57; y < dryY; y++)' in painter,
'coastal painter uses protected raw world Y':'canvas.setRaw' in painter and 'protectsVanillaWrite(x, worldY, z)' in canvas,
'city populator skips coastal residents':'plan.coastalDistanceAt(r.centerX(), r.centerZ()) > 0' in pop,
'coast/sea paint occurs after urban painter':gen.index('CityPainter.paint(cityPlan, chunk, protectedStructures);')<gen.index('OceanBathymetry65.paint(cityPlan, chunk, protectedStructures);'),
'vanilla underground protection kept':'VanillaStructureLookup64.forChunk' in gen and 'DeepDarkBuffer63.protects' in canvas,
'original End/Nether preserved':all(x in preset['dimensions'] for x in ('minecraft:the_end','minecraft:the_nether')),
'vanilla 30 percent cave outlet policy':w['city']['sewer_cave_frequency']==30,
'no forgotten city invented':not (city/'ForgottenCity.java').exists(),
'no item registry changed':'import com.dorohedoro.hole.items' not in policy,
'full resource JSON parses':all(json.loads(read(p)) is not None for p in (r/'src/main/resources').rglob('*.json')),
}
for k,v in checks.items():print(('PASS' if v else 'FAIL'),k)
print(f'PASS66 source {sum(checks.values())}/{len(checks)}')
if not all(checks.values()):raise SystemExit(1)
