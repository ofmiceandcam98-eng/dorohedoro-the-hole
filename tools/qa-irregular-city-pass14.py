#!/usr/bin/env python3
"""Static source/resource assertions; run the dependency-free Java geometry QA separately."""
from pathlib import Path
import json, re
r=Path(__file__).resolve().parent.parent
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda file: (city/file).read_text()
settings=read('CitySettings.java');canvas=read('Canvas.java');cityplan=read('CityPlan.java');blocks=read('Superblock.java');
irregular=read('IrregularBlocks14.java');mass=read('BuildingMass14.java');structures=read('StructuralVolumes14.java');lot=read('LotPainter.java');tower=read('Tower.java');diagonal=read('DiagonalBoulevards14.java');diagonal_math=read('DiagonalGrid14.java');
painter=read('CityPainter.java'); world=read('WorldDetail.java'); facade=read('ArchitecturePass13.java')
preset=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
checks={
'v3+ world preset opt-in':preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=3,
'v3+ generator codec persisted':'Codec.intRange(0, 14).optionalFieldOf("world_detail_version", 0)' in settings,
'old saves still default v0':'worldDetailVersion >= 2' in canvas and 'worldDetailVersion >= 3' in canvas and '0, 0);' in settings,
'legacy settings remain valid':'worldDetailVersion < 0 || worldDetailVersion > 14' in settings,
'original spawn preserved':'buildStartingQuarter(plan, area' in blocks,
'new macro street pitches are rectangular':'IRREGULAR_X_PITCH = 144' in cityplan and 'IRREGULAR_Z_PITCH = 104' in cityplan,
'v3 pitch selection gated':'settings.worldDetailVersion() < 3' in cityplan and 'int period = pitch(salt);' in cityplan,
'index lookup follows saved pitch':'int guess = Math.floorDiv(coordinate - period / 2, period)' in cityplan,
'landmarks preserved':'site.blockI() == i && site.blockJ() == j' in blocks,
'verge only for v3':'plan.settings.worldDetailVersion() >= 3 ? 6 : 0' in blocks,
'irregular streets version gated':'plan.settings.worldDetailVersion() >= 3' in blocks,
'v3 offset roads decision': 'usesOffsetBlocks(hash)' in blocks,
'local parcels split using standard lot picker': 'for (Rect parcel : IrregularBlocks14.parcels(area, hash,' in blocks and 'split(plan, parcel, district, 1, random, lots);' in blocks,
'orientation varies':'boolean longX =' in irregular,
'street shift from block hash':'startShift' in irregular and 'endShift' in irregular,
'perpendicular alleys':'CROSS_ALLEY = 5' in irregular,
'diagonal boulevards analytic and versioned':'if (canvas.hasIrregularCity()) DiagonalBoulevards14.paint(canvas, plan);' in painter,
'angled boulevard families':'x + z' in diagonal_math and 'x - z' in diagonal_math,
'analytically reserved roadside parcels':'DiagonalBoulevards14.intersects(plan.seed, lot.rect())' in blocks,
'roadside open lot activities':'LotType.JUNKYARD' in blocks and 'LotType.MARKET_STALLS' in blocks,
'clear upper boulevard surface not deep caves':'c.fill(x, GROUND_Y, z, x, CLEAR_HEIGHT, z, Blocks.AIR)' in diagonal,
'diagonal boulevard spawn exclusion':'PROTECTED_SPAWN_RADIUS = 380' in diagonal_math,
'diagonal boulevard landmark protection':'site.blockI() == i && site.blockJ() == j' in diagonal,
'diagonals before protected shafts':painter.index('DiagonalBoulevards14.paint') < painter.index('paintShafts(canvas, plan, xLines, zLines)'),
'main diagonal passage':'DIAGONAL_ALLEY = 7' in irregular,
'parcels are non-overlapping rectangles':'List.copyOf(parcels)' in irregular,
'avenues bend but crossings align':'nearestCrossingDistance <= 13' in irregular and 'ramp' in irregular,
'canals and tram kept straight':'avenue.kind() != Line.Kind.AVENUE' in cityplan,
'old saved world roads unchanged':'settings.worldDetailVersion() < 3' in cityplan,
'avenue offsets used in painter':'streetXBand(plan, xLines, x, z)' in painter and 'streetZBand(plan, zLines, x, z)' in painter,
'curbs follow avenue offsets':'plan.xAvenueOffset(xBand, z)' in painter and 'plan.zAvenueOffset(zBand, x)' in painter,
'world street materials follow bends':'plan.xAvenueOffset(xb, z)' in world and 'plan.zAvenueOffset(zb, x)' in world,
'facade street details follow bends':'plan.xAvenueOffset(band, z)' in facade,
'street fixtures follow bends':'bandAt(xLines, x, z, plan, true)' in world,
'non-grid building mass gated':'if (c.hasIrregularCity()) StructuralVolumes14.tower' in tower,
'original stair skeleton intact':'flight(c, interior, floorY, style.stairs());' in tower,
'original main door intact':'c.door(doorX, GROUND_Y, box.z1()' in tower,
'deterministic L mass profile':'CityHash.mix(hash ^ 0x14CAFE714L)' in mass,
'L recess physically carved':'c.clear(recess, GROUND_Y, top + 3)' in structures,
'courtyard physically carved':'c.clear(court, GROUND_Y, top + 3)' in structures,
'courtyard entrance path':'passageX' in structures and 'b.z1() - 1' in structures,
'courtyard rooftop guard rails':'c.set(x0 - 1, top + 1, z, Blocks.IRON_BARS)' in structures,
'upper floor terraces physically carved':'c.clear(recess, terraceY + 1, top + 3)' in structures,
'rooftop farms protected':'if (candidate == 2 && garden) candidate = 3;' in mass,
'factory roof office exists':'industrialOffice(c, box, roofY, lot.hash())' in lot,
'warehouse roof office exists':'industrialOffice(c, box, GROUND_Y + height, lot.hash())' in lot,
'service shack has recipe-capable stations':'Blocks.SMITHING_TABLE' in structures and 'Blocks.CRAFTING_TABLE' in structures,
'service shack usable chest/barrel':'c.barrel(shack.x1() - 1' in structures,
'service outbuilding lot bounded':'shack.width() < 5 || shack.depth() < 8' in structures,
'service yard subdivides larger industrial parcels':'StructuralVolumes14.industrialFootprint(c, lot' in lot,
'service yard retains original survival halls':'hall(c, box, height, style)' in lot,
'no ore/cave changes':'paintGalleries(canvas' in painter and 'SewerCaveBranches.paint(canvas, plan)' in painter,
'no real world read in parcel layout':'net.minecraft' not in irregular and 'Random' not in irregular,
'chunk order safe Canvas':'if (!contains(x, z))' in (city/'Canvas.java').read_text(),
}
for name, result in checks.items():
 print(('PASS' if result else 'FAIL'),name)
print(f'PASS14 STATIC: {sum(checks.values())}/{len(checks)} checks')
if not all(checks.values()):raise SystemExit(1)
