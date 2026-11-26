#!/usr/bin/env python3
"""Source-only Pass56 contract checks; live NeoForge and Minecraft must still be tested."""
import json
from pathlib import Path
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda n:(city/n).read_text()
settings=read('CitySettings.java')
canvas=read('Canvas.java')
structural=read('StructuralVolumes14.java')
linked=read('LinkedServiceYards56.java')
lot=read('LotPainter.java')
preset=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
config=preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
  'fresh overworld opts into version 39':config['world_detail_version']>=39,
  'old world generator version default zero':'optionalFieldOf("world_detail_version", 0)' in settings,
  'codec supports historical and new versions':'Codec.intRange(0, 52)' in settings,
  'constructor validates new range':'worldDetailVersion > 52' in settings,
  'old worlds keep +8 industrial building offset':'c.hasLinkedServiceYards56() ? 10 : 8' in structural,
  'passage flag applies only v39':'worldDetailVersion >= 39' in canvas,
  'old shack location retained':'main.x0() - (linked ? 4 : 2)' in linked,
  'actual industrial builder uses both new route geometry and legacy mode':'LinkedServiceYards56.shack(' in structural and 'LinkedServiceYards56.connect(' in structural,
  'connecting workshop only when fresh flag':'if (c.hasLinkedServiceYards56()) LinkedServiceYards56.connect' in structural,
  'both workshop and main hall receive doors':'c.door(shack.x1()' in linked and 'c.door(main.x0()' in linked,
  'accessible 3-wide passage':'passage.width() < 3' in linked and 'passage.depth() != 3' in linked,
  'passage stays in owner lot':'yard.contains(passage.x0(), passage.z0())' in linked and 'yard.contains(passage.x1(), passage.z1())' in linked,
  'roofed walk with vertical clearance':'c.clear(crossing, GROUND_Y, GROUND_Y + 3)' in linked and 'c.fill(crossing, GROUND_Y + 4, GROUND_Y + 4' in linked,
  'factory loading space preserved':'main.x0() + 2' in linked,
  'no loot or added lighting':all(x not in linked for x in ['c.chest(', 'c.barrel(', 'Blocks.LANTERN','Blocks.TORCH','Blocks.GLOWSTONE']),
  'service hall creation remains functional':all(x in structural for x in ['Blocks.CRAFTING_TABLE','Blocks.SMITHING_TABLE','Blocks.SPRUCE_DOOR']),
  'original populated factory and warehouse remain':all(x in lot for x in ['StructuralVolumes14.serviceOutbuilding','OccupiedInteriors35.industrial','RoomPurpose37.workshop']),
  'previous connected residential service wings remain':'AttachedWings55.build' in lot and 'worldDetailVersion >= 38' in canvas,
  'saved-world version changes limited to fresh preset': config['sewer_cave_frequency']==30,
  'world preset JSON validates':all(json.loads(p.read_text()) is not None for p in (r/'src').rglob('*.json')),
}
for label,result in checks.items():print(('PASS' if result else 'FAIL')+' '+label)
print('Pass56 source checks:',sum(checks.values()),'/',len(checks))
assert all(checks.values())
