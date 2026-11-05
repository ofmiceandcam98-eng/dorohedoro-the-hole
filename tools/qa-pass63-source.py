#!/usr/bin/env python3
"""Source/data integration checks, NOT proof of vanilla structures in Minecraft."""
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1]
city = root / 'src/main/java/com/dorohedoro/hole/worldgen/city'
biome = (root/'src/main/java/com/dorohedoro/hole/worldgen/HoleBiomeSource.java').read_text()
canvas = (city/'Canvas.java').read_text()
plan = (city/'CityPlan.java').read_text()
source = (city/'DeepDarkBuffer63.java').read_text()
settings = (city/'CitySettings.java').read_text()
preset = json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
 'fresh world retains v45 deep-dark safeguard':preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=45,
 'codec accepts new version':'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
 'legacy save defaults to original v0':'optionalFieldOf("world_detail_version", 0)' in settings,
 'underground deep dark still registered':'RegistryOps.retrieveElement(Biomes.DEEP_DARK)' in biome,
 'shared climate predicate between actual biome and guard':'DeepDarkBuffer63.matches(wet, warm)' in biome and 'wet < -0.22 && warm < 0.0' not in biome,
 'original deep dark height selection retained':'y < DEEP_DARK_TOP' in biome and 'DEEP_DARK_TOP = -24' in biome,
 'guard reuses exact transformed coordinates':'z + 4099, 0, -x + 7919' in source and 'z + 4099, 0, -x + 7919' in biome,
 'lookaround samples across nearby chunks':'dx = -48; dx <= 48' in source and 'dz = -48; dz <= 48' in source,
 'per-chunk cached decision':'private final boolean protectDeepDark63;' in canvas and 'terrainPlan.protectsDeepDarkNearChunk(minX, minZ)' in canvas,
 'actual adjusted height checked before placement':'DeepDarkBuffer63.protects(worldDetailVersion, adjustedY, protectDeepDark63)' in canvas and 'if (protectsVanillaWrite(x, adjustedY, z)) return;' in canvas,
 'block writes never bypass protection in Canvas':'chunk.setBlockState(cursor.set(x, adjustedY, z), state, false);' in canvas and canvas.index('if (protectsVanillaWrite(x, adjustedY, z)) return;')<canvas.index('chunk.setBlockState(cursor.set(x, adjustedY, z), state, false);'),
 'container, spawner and sign block entity writes guarded': canvas.count('if (protectsVanillaWrite(x, adjustedY, z)) return;')==4,
 'upper sewer level outside protected Y':'actualY <= -24' in source,
 'legacy generation explicitly gated':'worldDetailVersion >= 45' in source and 'settings.worldDetailVersion() >= 45' in plan,
 'vanilla nether remains':preset['dimensions']['minecraft:the_nether']['type']=='minecraft:the_nether',
 'vanilla end remains':preset['dimensions']['minecraft:the_end']['type']=='minecraft:the_end',
 'ancient city structure tag untouched':not (root/'src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/ancient_city.json').exists(),
 'no mistaken extra forgotten city': 'There is no separate custom Forgotten City planned' in (root/'docs/PASS62_VANILLA_MINECRAFT_COMPLETION_AND_STRUCTURE_TAGS.md').read_text(),
 'vanilla continuation checklist remains':(root/'tools/qa-pass62-vanilla-progression.py').exists(),
 'no ocean parity claim in handoff':'no genuine oceans' in (root/'HANDOFF.md').read_text(),
 'sewer cave percentage unchanged':preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cave_frequency']==30,
}
for key, value in checks.items(): print(('PASS' if value else 'FAIL'),key)
print(f'Pass63 source QA: {sum(checks.values())}/{len(checks)} passed')
if not all(checks.values()):raise SystemExit(1)
