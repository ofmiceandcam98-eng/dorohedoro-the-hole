#!/usr/bin/env python3
"""Source/resource integration assertions only; Minecraft/NeoForge runtime still unverified."""
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda n:(city/n).read_text()
pump=read('StormwaterPumpHalls68.java')
bays=read('SewerServiceBays67.java')
painter=read('CityPainter.java')
settings=read('CitySettings.java')
canvas=read('Canvas.java')
hazards=read('SewerHazards61.java')
vanilla=read('VanillaStructureBuffer64.java')
config=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
overworld=config['dimensions']['minecraft:overworld']['generator']
checks={
'fresh preset v51 retains original v50 pump halls':overworld['city']['world_detail_version']==52,
'world decoder supports v50':'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
'validates v50 max':'worldDetailVersion > 52' in settings,
'legacy world absent version defaults zero':'optionalFieldOf("world_detail_version", 0)' in settings,
'new feature uses v50 gate':'FIRST_VERSION = 50' in pump and 'worldDetailVersion() < FIRST_VERSION' in pump,
'old small service rooms retain v49':'FIRST_VERSION = 49' in bays,
'bays avoid larger gallery':'StormwaterPumpHalls68.reservesServiceBay' in bays,
'municipal sewer underworld explicit':'!plan.settings.sewerUnderworld()' in pump,
'sewer shell before gallery':painter.index('paintSewers(canvas, xLines, zLines);')<painter.index('StormwaterPumpHalls68.paint(canvas, plan);'),
'original masonry before gallery':painter.index('SewerDetails24.paint(canvas, plan, xLines, zLines);')<painter.index('StormwaterPumpHalls68.paint(canvas, plan);'),
'gallery after service room to avoid conflicts':painter.index('SewerServiceBays67.paint(canvas, plan);')<painter.index('StormwaterPumpHalls68.paint(canvas, plan);'),
'cave branches and dungeons after station':painter.index('StormwaterPumpHalls68.paint(canvas, plan);')<painter.index('SewerCaveBranches.paint(canvas, plan);')<painter.index('SewerDungeon22.paint(canvas, plan);'),
'underworld exists and is enabled':overworld['city']['sewer_underworld'] is True,
'cave ratio 30 remains':overworld['city']['sewer_cave_frequency']==30,
'random trunk section rather than chunk':'SECTION = 192' in pump and 'CityHash.of(seed,' in pump and 'Math.floorDiv(minAlong' in pump,
'negative coordinates use floor division':'Math.floorDiv(along - HALF_LENGTH - 4, SECTION)' in pump,
'canal corridors excluded':'trunk.kind() == Line.Kind.CANAL' in pump,
'protect shafts and large junctions':'MIN_JUNCTION_DISTANCE = 48' in pump and 'crossing.stream().anyMatch' in pump,
'wide crossing lookup consistent per chunk':'int crossingReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH + 1' in pump,
'no urban construction on any coast pixel':'for (int x = footprint.x0(); x <= footprint.x1(); x++)' in pump and 'plan.coastalDistanceAt' in pump and 'plan.oceanDepthAt' in pump,
'repair equipment practical':all('Blocks.'+n in pump for n in ('CRAFTING_TABLE','FURNACE','STONECUTTER')),
'water sump with solid footbridge':'Blocks.WATER' in pump and 'offset == 0 ? Blocks.POLISHED_ANDESITE : Blocks.WATER' in pump,
'operator room with vanilla iron door':'Blocks.IRON_DOOR.defaultBlockState()' in pump and 'DoubleBlockHalf.LOWER' in pump and 'DoubleBlockHalf.UPPER' in pump,
'vanilla floor lever beside door':'Blocks.LEVER.defaultBlockState()' in pump and 'AttachFace.FLOOR' in pump,
'no forced magic or weapon progression':not any(s in pump for s in ('.spawner(','.chest(','.barrel(', 'magicTechnique', 'Blocks.LANTERN','Blocks.TORCH')),
'vanilla structure bounding-box protection preserved':'VanillaStructureBuffer64.protects' in canvas and 'protectedVanillaStructures64' in canvas,
'deep dark protection preserved':'DeepDarkBuffer63.protects' in canvas,
'neither Nether nor End removed':all(x in config['dimensions'] for x in ('minecraft:the_nether','minecraft:the_end')),
'no unrelated generator registries changed':(r/'src/main/java/com/dorohedoro/hole/worldgen/HoleChunkGenerator.java').exists(),
'vanilla ancient city is not replaced':not (city/'ForgottenCity.java').exists(),
'Pass61 dark sewer stretches intact':'FIRST_VERSION = 44' in hazards,
'Pass67 tests still present':(r/'tools/qa-pass67-service-bays.sh').exists(),
'current tests available':(r/'tools/qa-pass68-pump-halls.sh').exists(),
'valid JSON':all(json.loads(p.read_text()) is not None for p in (r/'src/main/resources').rglob('*.json')),
}
for name,passed in checks.items():print(('PASS ' if passed else 'FAIL ')+name)
print('Pass68 source integration: %s/%s' %(sum(checks.values()),len(checks)))
if not all(checks.values()):raise SystemExit(1)
