#!/usr/bin/env python3
"""Pass67 source integration checks; not a NeoForge build or in-game assertion."""
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
def get(p):return p.read_text()
bay=get(city/'SewerServiceBays67.java')
painter=get(city/'CityPainter.java')
canvas=get(city/'Canvas.java')
settings=get(city/'CitySettings.java')
sewer=get(city/'SewerDetails24.java')
hazards=get(city/'SewerHazards61.java')
vanilla=get(city/'VanillaStructureBuffer64.java')
preset=json.loads(get(root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
gen=preset['dimensions']['minecraft:overworld']['generator']
checks={
'new preset includes Pass67 detail version 49':gen['city']['world_detail_version']>=49,
'sewer enabled in new worlds':gen['city']['sewer_underworld'] is True,
'v49 codec accepts stored worlds':'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in settings,
'v49 constructor accepts and rejects beyond max':'worldDetailVersion > 52' in settings,
'old saves without version load as zero':'optionalFieldOf("world_detail_version", 0)' in settings,
'no change to sewer cave frequency':gen['city']['sewer_cave_frequency']==30,
'after original sewer shell':'paintSewers(canvas, xLines, zLines);' in painter and painter.index('paintSewers(canvas, xLines, zLines);')<painter.index('SewerServiceBays67.paint(canvas, plan);'),
'after masonry decoration':painter.index('SewerDetails24.paint(canvas, plan, xLines, zLines);')<painter.index('SewerServiceBays67.paint(canvas, plan);'),
'before cave branches and dungeons':painter.index('SewerServiceBays67.paint(canvas, plan);')<painter.index('SewerCaveBranches.paint(canvas, plan);')<painter.index('SewerDungeon22.paint(canvas, plan);'),
'legacy generation version gate':'if (plan.settings.worldDetailVersion() < FIRST_VERSION' in bay and 'FIRST_VERSION = 49' in bay,
'no sewer in disabled worlds':'!plan.settings.sewerUnderworld()' in bay,
'coordinate-only station choice':'CityHash.of(seed,' in bay and 'section * SECTION' in bay,
'avoid canal corridors':'trunk.kind() == Line.Kind.CANAL' in bay,
'avoid shafts and dungeon intersections':'MIN_JUNCTION_DISTANCE = 39' in bay and 'crossing.stream().anyMatch' in bay,
'cross-chunk exclusion query reaches full danger radius':'int crossReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH' in bay and 'zCrossing = plan.zLinesNear(c.minZ - crossReach' in bay,
'minimal 3-high doorway':'c.fill(x, 50, z, x, 52, z, Blocks.AIR)' in bay,
'service walkway preserved':'distance = 3;' in bay and 'distance <= REACH' in bay,
'dry room floor at same level as sewer':'c.set(x, 49, z,' in bay,
'rooms 4-high clear':'c.fill(x, 50, z, x, 53, z, Blocks.AIR)' in bay,
'no loot or hostile spawners added':'.spawner(' not in bay and '.chest(' not in bay and '.barrel(' not in bay,
'no new municipal lamps':'Blocks.LANTERN' not in bay and 'Blocks.TORCH' not in bay,
'vanilla vanilla repair workstations':all('Blocks.'+x in bay for x in ('CRAFTING_TABLE','FURNACE','GRINDSTONE')),
'vanilla filter workstations':all('Blocks.'+x in bay for x in ('CAULDRON','COMPOSTER','STONECUTTER')),
'ocean and coastal reservations':'plan.oceanDepthAt(targetX, targetZ)' in bay and 'plan.coastalDistanceAt(targetX, targetZ)' in bay,
'protection on all ordinary block writes':'protectsVanillaWrite(x, adjustedY, z)' in canvas and 'VanillaStructureBuffer64.protects' in canvas,
'deepdark protection retained':'DeepDarkBuffer63.protects' in canvas,
'no custom Forgotten City':not (city/'ForgottenCity.java').exists(),
'old lantern hazard remains':'SewerHazards61.lampAt' in sewer and 'FIRST_VERSION = 44' in hazards,
'overworld Nether and End remain':all(x in preset['dimensions'] for x in ('minecraft:overworld','minecraft:the_nether','minecraft:the_end')),
'valid JSON resources':all(json.loads(get(p)) is not None for p in (root/'src/main/resources').rglob('*.json')),
'full source fixture present':(root/'tools/qa-pass67-service-bays.sh').exists(),
}
for name,ok in checks.items():print(('PASS ' if ok else 'FAIL ')+name)
print(f'Pass67 source integration: {sum(checks.values())}/{len(checks)}')
if not all(checks.values()):raise SystemExit(1)
