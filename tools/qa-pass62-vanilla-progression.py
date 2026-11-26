#!/usr/bin/env python3
"""Pass 62: source/data consistency checks ONLY, never substitutes Minecraft runtime testing."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main/java/com/dorohedoro/hole'
GENERATED = ROOT / 'src/generated/resources/data/minecraft/tags/worldgen/biome'
PRESET = ROOT / 'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'
checks = []
def check(name, valid):
    checks.append((name, bool(valid)))
    print(('PASS' if valid else 'FAIL') + ' ' + name)

s = (SRC / 'datagen/ModDataGen.java').read_text()
biome_src = (SRC / 'worldgen/HoleBiomeSource.java').read_text()
biome_registry = (SRC / 'worldgen/HoleBiomes.java').read_text()
chunk_src = (SRC / 'worldgen/HoleChunkGenerator.java').read_text()
generator_settings = json.loads((ROOT / 'src/main/resources/data/dorohedoro/worldgen/noise_settings/hole_sewers.json').read_text())
preset = json.loads(PRESET.read_text())
key_to_id = dict(re.findall(r'public static final ResourceKey<Biome> (\w+) = key\("(\w+)"\);', biome_registry))
assert len(key_to_id) >= 14, 'Missing biome keys'

# Parse the exact tag() ... .add(...) blocks from the same datagen source runData uses.
groups = re.findall(r'tag\(hasStructure\("([a-z_]+)"\)\)\.add\((.*?)\);', s, re.DOTALL)
expected = {}
for tag, group in groups:
    ids = [key_to_id[k] for k in re.findall(r'HoleBiomes\.(\w+)', group)]
    if tag in expected:
        raise AssertionError('Duplicate tag declaration: ' + tag)
    expected[tag] = {'dorohedoro:'+x for x in ids}
# Legacy datagen also adds the existing underground biome to trial chambers.
expected['trial_chambers'].add('dorohedoro:hole_undercity')

must_exist = {
    'desert_pyramid','igloo','jungle_temple','swamp_hut','trail_ruins',
    'village_desert','village_snowy','village_taiga','village_plains','village_savanna',
    'pillager_outpost','woodland_mansion','mineshaft_mesa',
    'ruined_portal_desert','ruined_portal_mountain',
    'ruined_portal_swamp','ruined_portal_jungle','ruined_portal_standard',
    'trial_chambers'
}
check('19 land structure tags declared in datagen', must_exist <= expected.keys())
check('Minecraft 1.21.1 resource locations used for biome extensions',
      'ResourceLocation.fromNamespaceAndPath("minecraft", "has_structure/" + id)' in s)
check('Generated data pack included in compiled resources',
      "srcDir('src/generated/resources')" in (ROOT/'build.gradle').read_text())
check('Datagen HoleBiomeTags provider enabled', 'new HoleBiomeTags(' in s)
check('Structure tag extensions never override vanilla', all(
    (GENERATED/'has_structure'/f'{tag}.json').exists() and
    json.loads((GENERATED/'has_structure'/f'{tag}.json').read_text()).get('replace') is False
    for tag in expected))
check('Generated structure tags match Java datagen definitions', all(
    set(json.loads((GENERATED/'has_structure'/f'{tag}.json').read_text())['values']) == ids
    for tag, ids in expected.items()))
check('All placed structure biomes are registered', all(
    bid.split(':', 1)[1] in key_to_id.values() for ids in expected.values() for bid in ids))
check('No custom overwrites of ancient-city biome tag',
      'ancient_city' not in expected and not (GENERATED/'has_structure/ancient_city.json').exists())
check('No fake ocean structure eligibility on canal-only biomes', not any(x in expected for x in
      ('shipwreck','shipwreck_beached','ocean_monument','ocean_ruin_cold','ocean_ruin_warm','buried_treasure')))
check('Vanilla strongholds permitted by Hole overworld tags',
      all('dorohedoro:' + x in json.loads((GENERATED/f'{tag}.json').read_text())['values']
          for tag in ('is_overworld','stronghold_biased_to')
          for x in ('hole_undercity','hole_dust_basin','hole_snowfields','hole_iron_badlands')))
check('Vanilla mineshafts eligible across core and outer regions',
      all('dorohedoro:'+x in json.loads((GENERATED/'has_structure/mineshaft.json').read_text())['values']
          for x in ('hole_residential','hole_undercity','hole_dead_taiga','hole_mountain_rim')))
check('Trial chambers still permitted in underground Hole biome',
      'dorohedoro:hole_undercity' in json.loads((GENERATED/'has_structure/trial_chambers.json').read_text())['values'])
check('Vanilla deep-dark remains in actual underground biome source',
      'RegistryOps.retrieveElement(Biomes.DEEP_DARK)' in biome_src and
      'return deepDark;' in biome_src and 'blockY < UNDERGROUND_TOP' in biome_src)
check('Noise-based underground and Minecraft carver compatibility documented',
      'extends NoiseBasedChunkGenerator' in chunk_src and
      'super.buildSurface(region, structureManager, random, chunk);' in chunk_src)
check('Vanilla caves and aquifers still configured',
      generator_settings.get('aquifers_enabled') is True and
      generator_settings.get('noise',{}).get('min_y') == -64)
check('Default overworld generator retains Hole settings',
      preset['dimensions']['minecraft:overworld']['generator']['type']=='dorohedoro:hole')
check('Vanilla Nether still present, not replaced with Dorohedoro Hell',
      preset['dimensions']['minecraft:the_nether']['generator']['settings']=='minecraft:nether' and
      preset['dimensions']['minecraft:the_nether']['type']=='minecraft:the_nether')
check('Vanilla End and dragon dimension still present',
      preset['dimensions']['minecraft:the_end']['generator']['settings']=='minecraft:end' and
      preset['dimensions']['minecraft:the_end']['type']=='minecraft:the_end')
check('Vanilla realm portals are not hijacked by custom door types',
      all(x in (SRC/'portal/PortalKind.java').read_text() for x in ('ARTIFICIAL(', 'DEVIL(', '"sorcerers_world"','"hell"')))
qa_source = (SRC/'qa/HoleQa.java').read_text()
check('Wednesday runtime QA accepts regional seed-selected starts',
      'spawn.equals(plannedSpawn)' in qa_source and 'plan.spawnPoint()' in qa_source)
check('Core QA connectivity does not index out of bounds for regional spawn',
      'if (!area.contains(spawn.getX(), spawn.getZ()))' in qa_source and
      'core_connectivity_anchor' in qa_source)
check('Original cave-branch and 1.21 trial structure rules retained',
      preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cave_frequency']==30 and
      'tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(HoleBiomes.UNDERCITY)' in s)

fails=[c for c,ok in checks if not ok]
print(f'Pass 62 source/data checks: {len(checks)-len(fails)}/{len(checks)} passed; {len(expected)} new structure biome tags')
if fails: raise SystemExit('FAIL: '+', '.join(fails))
