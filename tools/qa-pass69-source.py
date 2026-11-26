#!/usr/bin/env python3
"""Pass69 structural verification; NOT a Minecraft fluid simulation or NeoForge build."""
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
c=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda n:(c/n).read_text()
settings=read('CitySettings.java');pump=read('StormwaterPumpHalls68.java');gate=read('SluiceValve69.java')
painter=read('CityPainter.java');canvas=read('Canvas.java')
preset=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
world=preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'new world version 51':world['world_detail_version']==52,
 'sewer system kept on':world['sewer_underworld'] is True,
 'historic cave branch frequency unchanged':world['sewer_cave_frequency']==30,
 'legacy codec zero default':'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
 'saved worlds accept v50 and v51':'worldDetailVersion > 52' in settings,
 'gate version independently protected':'FIRST_VERSION = 51' in gate,
 'old pump hall version remains v50':'FIRST_VERSION = 50' in pump,
 'pump calls sluice only for new saves':'if (worldDetailVersion >= SluiceValve69.FIRST_VERSION)' in pump,
 'gate called after original water gallery':'SluiceValve69.build(c, xAxis, trunk, along, side);' in pump,
 'mechanical piston real vanilla block':'Blocks.STICKY_PISTON.defaultBlockState()' in gate,
 'piston faces into channel':'Direction.SOUTH : Direction.EAST' in gate,
 'iron block moves in channel':'Blocks.IRON_BLOCK' in gate,
 'strong power through floor':'c.set(powerX, 49, powerZ, Blocks.STONE_BRICKS);' in gate,
 'lever has normal vanilla floor attachment':'Blocks.LEVER.defaultBlockState()' in gate and 'AttachFace.FLOOR' in gate,
 'runoff outlet starts dry':'for (int distance = 14; distance <= 15; distance++)' in gate and 'Blocks.AIR' in gate,
 'no water sources in runoff':'Blocks.WATER' not in gate,
 'pump retains live upstream water source':'offset == 0 ? Blocks.POLISHED_ANDESITE : Blocks.WATER' in pump,
 'central dry crossing not replaced':'offset == 0 ? Blocks.POLISHED_ANDESITE : Blocks.WATER' in pump,
 'gate separate from operator booth':'Iron access door opens' in pump,
 'no additional loot, spawners, lighting':all(s not in gate for s in ('Blocks.TORCH','Blocks.LANTERN','.barrel(','.chest(','.spawner(')),
 'old intersected chunk guard retained':'MIN_JUNCTION_DISTANCE = 48' in pump and 'crossingReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH + 1' in pump,
 'conserves vanilla structure safety':'VanillaStructureBuffer64.protects' in canvas and 'DeepDarkBuffer63.protects' in canvas,
 'pump gate placed after municipal sewer':'paintSewers(canvas, xLines, zLines);' in painter and 'StormwaterPumpHalls68.paint(canvas, plan);' in painter,
 'proper Nether and End retained':all(k in preset['dimensions'] for k in ('minecraft:the_nether','minecraft:the_end')),
 'all JSON valid':all(json.loads(j.read_text()) is not None for j in (r/'src/main/resources').rglob('*.json')),
 'compiled source fixture available':(r/'tools/qa-pass69-sluice-valve.sh').exists(),
}
for name,ok in checks.items():print(('PASS ' if ok else 'FAIL ')+name)
print('Pass69 source checks:',sum(checks.values()),'/',len(checks))
if not all(checks.values()):raise SystemExit(1)
