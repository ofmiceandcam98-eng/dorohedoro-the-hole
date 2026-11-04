#!/usr/bin/env python3
"""Pass70 implementation and compatibility contract; not a Minecraft build."""
from pathlib import Path
import json

root=Path(__file__).resolve().parents[1]
java=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda name:(java/name).read_text()
settings=read('CitySettings.java')
painter=read('CityPainter.java')
branch=read('SewerCaveBranches.java')
bypass=read('StormDrainBypasses70.java')
vault=read('SewerVaultVariants70.java')
transition=read('SewerCaveTransitions70.java')
canvas=read('Canvas.java')
policy=read('SewerBranchPolicy.java')
world=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
city=world['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'fresh worlds select version 52': city['world_detail_version']==52,
 'world has sewer underworld enabled': city['sewer_underworld'] is True,
 'cave selection target unchanged': city['sewer_cave_frequency']==30,
 'saved worlds continue using old zero default': 'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
 'constructor version matches codec': 'worldDetailVersion > 52' in settings,
 'new storm feature gated': 'FIRST_VERSION = 52' in bypass and 'version >= FIRST_VERSION' in bypass,
 'new vault feature gated': 'FIRST_VERSION = 52' in vault and 'worldDetailVersion() < FIRST_VERSION' in vault,
 'new cave feature gated': 'FIRST_VERSION = 52' in transition and 'worldDetailVersion() >= SewerCaveTransitions70.FIRST_VERSION' in branch,
 'original main sewer still painted': 'paintSewers(canvas, xLines, zLines);' in painter,
 'vault variants after original municipal detail': painter.index('SewerDetails24.paint') < painter.index('SewerVaultVariants70.paint'),
 'vault before repair bays': painter.index('SewerVaultVariants70.paint') < painter.index('SewerServiceBays67.paint'),
 'new bypass after pump hall': painter.index('StormwaterPumpHalls68.paint') < painter.index('StormDrainBypasses70.paint'),
 'cave transitions added after intact walkable mouth': branch.index('SewerCaveTransitions70.paintStep') > branch.index('SewerCavernProfile.entranceFloor'),
 'two independent bypass entrances': 'Math.abs(offset - 10)' in bypass and 'Math.abs(offset + 10)' in bypass,
 'continuous dry lanes retained': 'distance == 10' in bypass and 'Blocks.WATER' in bypass and 'Blocks.AIR' in bypass,
 'pass67 repair bays protected': 'SewerServiceBays67.kind' in bypass and 'SewerServiceBays67.center' in bypass,
 'pass68 pump halls protected': 'StormwaterPumpHalls68.selected' in bypass and 'StormwaterPumpHalls68.center' in bypass,
 'crossing exclusion chunk-stable': 'crossingReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH + 1' in bypass,
 'coast and ocean reservations preserved': 'plan.oceanDepthAt' in bypass and 'plan.coastalDistanceAt' in bypass,
 'canal trunks excluded': 'trunk.kind() == Line.Kind.CANAL' in bypass,
 'central sewer and dry lanes untouched by vault': 'Math.min(dx, dz) != 3' in vault,
 'whole 3-wide cave descent preserved': 'distance <= 1) continue' in transition,
 'gradual cave widening': all(x in transition for x in ('step < 30','step < 42','step < 49')),
 'cave branch kind unchanged': 'return SewerBranchPolicy.kind(seed, xIndex, zIndex, caveFrequency)' in branch,
 'preserve vanilla structure buffer': 'VanillaStructureBuffer64.protects' in canvas,
 'preserve ancient-city deep dark protection': 'DeepDarkBuffer63.protects' in canvas,
 'Nether and End definitions remain': all(k in world['dimensions'] for k in ('minecraft:the_nether','minecraft:the_end')),
 'no loot, lights, spawners added to three features': all(
     key not in (bypass+vault+transition) for key in ('.barrel(','.chest(','.spawner(', 'Blocks.LANTERN','Blocks.TORCH', 'Blocks.REDSTONE_LAMP')),
 'source fixtures exist': (root/'tools/qa-pass70-sewer-architecture.sh').exists(),
 'all resource JSON decodes': all(json.loads(p.read_text()) is not None for p in (root/'src/main/resources').rglob('*.json')),
}
for label,passed in checks.items():print(('PASS ' if passed else 'FAIL ')+label)
print(f'Pass70 source checks: {sum(checks.values())}/{len(checks)}')
if not all(checks.values()):raise SystemExit(1)
