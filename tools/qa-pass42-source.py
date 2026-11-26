#!/usr/bin/env python3
"""Pass 42 source/resource integrity only, not Minecraft or NeoForge gameplay verification."""
import json, re, sys
from pathlib import Path
P=Path(__file__).resolve().parents[1]
J=P/'src/main/java/com/dorohedoro/hole'
R=P/'src/main/resources'
G=P/'src/generated/resources'
regions=['snowfields','dead_taiga','dust_basin','iron_badlands','mountain_rim']
checks=[]
def ok(label,test):
    checks.append((label,bool(test)))
    print(('PASS' if test else 'FAIL')+' '+label)

def read(p): return p.read_text(encoding='utf-8')

preset=json.loads(read(R/'data/minecraft/worldgen/world_preset/normal.json'))
gen=preset['dimensions']['minecraft:overworld']['generator']
ok('Pass 42 is opt-in for NEW world presets',gen['city']['world_detail_version']>=26 and gen['biome_source']['frontier_biomes'] is True)
ok('World starts in Hole',gen['type']=='dorohedoro:hole' and gen['biome_source']['type']=='dorohedoro:hole_districts')
ok('30 percent sewer cave branches preserved',gen['city']['sewer_cave_frequency']==30 and gen['city']['sewer_underworld'])
ok('Northern/southern portal dimensions retained', (R/'data/dorohedoro/dimension/sorcerers_world.json').exists() if (R/'data/dorohedoro/dimension/sorcerers_world.json').exists() else 'sorcerer_lands' in '\n'.join(p.name for p in (R/'data/dorohedoro/dimension').glob('*')))
bs=read(J/'worldgen/HoleBiomeSource.java')
biomes=read(J/'worldgen/HoleBiomes.java')
codec=read(J/'worldgen/city/CitySettings.java')
ok('Old saved worlds: optional frontier flag defaults off', 'optionalFieldOf("frontier_biomes", false)' in bs)
ok('Old saved worlds: version default still zero', 'optionalFieldOf("world_detail_version", 0)' in codec)
ok('Both old city + regional biomes collected', 'frontier.snow()' in bs and 'frostbittenIndustrial' in bs)
ok('Frontier climate and city share deterministic selector', 'HoleRegions42.at(sampler, blockX, blockZ)' in bs and 'HoleRegions42.at(climate, x, z)' in read(J/'worldgen/city/CityPlan.java'))
ok('All five regions registered in datagen', all('HoleBiomes.'+n.upper() in read(J/'datagen/HoleBiomeData.java') for n in regions))
ok('All five biome keys registered', all('"hole_'+n+'"' in biomes for n in regions))
for name in regions:
    path=G/f'data/dorohedoro/worldgen/biome/hole_{name}.json'
    try:
        j=json.loads(read(path)); valid=j['temperature']<2 and 'features' in j and len(j['features'])>=6 and j.get('spawners') is not None
    except (OSError,KeyError,ValueError) as e:valid=False
    ok(f'{name}: fallback biome registry JSON present',valid)
for tag in ['is_overworld','stronghold_biased_to','has_structure/mineshaft']:
    data=json.loads(read(G/f'data/minecraft/tags/worldgen/biome/{tag}.json'))
    ok(f'{tag}: all new biomes allowed',all('dorohedoro:hole_'+n in data['values'] for n in regions))
paint=read(J/'worldgen/city/CityPainter.java')
terrain=read(J/'worldgen/city/CityTerrain40.java')
material=read(J/'worldgen/city/RegionalSurface42.java')
ok('Mountain cap fits signed byte column cache', '>= 26 ? 106 : 36' in terrain)
ok('Highland solid foundations painted before sewers',paint.index('>= 26) paintGround')<paint.index('paintSewers(canvas') and 'FILL_BOTTOM - c.heightOffset(x, z)' in paint)
ok('Old city surface painter retained for saved versions', '< 26) paintGround' in paint)
ok('Region material system does not generate lamps/chests',not re.search(r'c\.(?:chest|barrel)\(',material) and 'LANTERN' not in material)
ok('Open lots use existing useful vanilla resources', all(t in material for t in ['Blocks.SAND','Blocks.RED_SAND','Blocks.STRIPPED_SPRUCE_LOG','Blocks.SNOW_BLOCK','Blocks.COBBLESTONE']))
ok('Original purposeful interiors still called', 'LotPainter.paint(canvas, plan, lot)' in paint)
ok('Frontier spawn biome habitat checks',all('HoleBiomes.'+n.upper() in read(J/'registry/ModEntities.java') for n in regions))
# Parse every written JSON including the generated resource tree.
fail=[]
for root in [R,G]:
    for p in root.rglob('*.json'):
        try:json.loads(read(p))
        except Exception as e:fail.append((str(p.relative_to(P)),str(e)))
ok('All source and generated JSON parses',not fail)
print(f'Pass 42 offline structural checks: {sum(t for _,t in checks)}/{len(checks)} passed. JSON errors: {fail[:5]}')
print('NOT VERIFIED: Minecraft/NeoForge compile, runData agreement, runtime terrain, client visual QA, gameplay or performance.')
if not all(t for _,t in checks): sys.exit(1)
