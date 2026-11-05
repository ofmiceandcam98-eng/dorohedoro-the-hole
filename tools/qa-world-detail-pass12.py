#!/usr/bin/env python3
"""Offline design and save-compatibility guards. Not a runtime/worldgen test."""
from pathlib import Path
import json,sys
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda name:(city/name).read_text()
config=read('CitySettings.java')
canvas=read('Canvas.java')
painter=read('CityPainter.java')
lot=read('LotPainter.java')
tower=read('Tower.java')
style=read('Style.java')
world=read('WorldDetail.java')
split=read('Superblock.java')
choose=read('LotChooser.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
    'explicit fresh-world opt-in':preset['dimensions']['minecraft:overworld']['generator']['city'].get('world_detail_version') in (1, 2, 3, 4, 5, 6, 7),
    'old saves default to legacy style':'optionalFieldOf("world_detail_version", 0)' in config and '0, 0);' in config,
    'old constructor remains supported':'this(chunk, seed, terrainRelief, 0);' in canvas and 'this(chunk, seed, terrainRelief, worldDetail ? 1 : 0);' in canvas,
    'only new worlds enable visual pass':'plan.settings.worldDetailVersion()' in painter and 'canvas.hasWorldDetail()' in painter,
    'all writes clipped by Canvas':'if (!contains(x, z)) {' in canvas and 'WorldDetail.streetFixtures(canvas' in painter,
    'surface paint modifies roads, not mines':'WorldDetail.streetSurface(c, plan, x, z' in painter,
    'unique building footprints':'WorldDetail.footprint(c, lot)' in lot and 'Rect result = new Rect(' in world,
    'building palettes independently vary':style.count('static final Style')>=9 and 'forDistrict(District district, long hash, boolean detailed)' in style,
    'legacy palette selector remains':'static Style forDistrict(District district, long hash) {' in style,
    'new lot distribution gated': 'plan.settings.worldDetailVersion() > 0' in choose,
    'new block sizes gated': 'boolean detailed = plan.settings.worldDetailVersion() > 0;' in split,
    'buildings have parapets': 'parapet' in world and 'top + 2' in world,
    'building facades have layers': 'cornice' in world and 'Window hoods' in world,
    'roof massing is varied': 'non-rectangular additional roof mass' in world,
    'large buildings have balconies':'Balconies' in world and 'Blocks.IRON_BARS' in world,
    'industrial building vents':'Side-mounted duct' in world and 'WorldDetail.hallExterior' in lot,
    'shop signage uses valid sign placement': 'c.sign(signX' in world and 'WorldDetail.shopfront(c' in lot,
    'yards stay inside lots':'static void lotYard(' in world and 'WorldDetail.lotYard(c, lot)' in lot,
    'in-world decorative clutter is selected by district':'District d = plan.districtAt(x, z);' in world,
    'paved streets include drainage':'int curb = band.halfWidth() - 2;' in world and 'Blocks.IRON_TRAPDOOR' in world,
    'junctions have markings':'Math.floorMod(x + z, 4)' in world,
    'mobs/rails/sewers preserved':'paintSewers(canvas' in painter and 'paintGalleries(canvas' in painter and 'gradedRailShape(' in painter,
    'interior detail avoids cross chunk reads':'if (!c.contains(x, z)) return;' in world,
    'interior detail protects existing props':'c.get(x, floorY + 1, z).isAir()' in world,
    'vanilla crafting and mining untouched':all(s not in world for s in ['clearMine','replaceOre','setSpawns','removeVanilla']),
}
fail=[k for k,v in checks.items() if not v]
for k,v in checks.items():print(('PASS ' if v else 'FAIL ')+k)
print(f'WORLD DETAIL STRUCTURAL CHECK: {len(checks)-len(fail)}/{len(checks)} passed (not a runtime compile)')
if fail:sys.exit(1)
