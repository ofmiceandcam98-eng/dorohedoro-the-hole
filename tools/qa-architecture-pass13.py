#!/usr/bin/env python3
"""Offline, static source/integration checks for World Detail Pass 13. NOT a gameplay test."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda f:(city/f).read_text()
settings=read('CitySettings.java'); canvas=read('Canvas.java'); plan=read('CityArchitecture.java')
architecture=read('ArchitecturePass13.java'); lot=read('LotPainter.java')
world=read('WorldDetail.java'); tower=read('Tower.java'); painter=read('CityPainter.java')
chooser=read('LotChooser.java'); derelict=read('DerelictBuildings.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
version=preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']
checks={
  'new saves select v2 or later':version>=2,
  'old saves default to version 0':'optionalFieldOf("world_detail_version", 0)' in settings,
  'previous pass version 1 is valid':'worldDetailVersion < 0 || worldDetailVersion > 14' in settings,
  'old API constructor retained':'worldDetail ? 1 : 0' in canvas,
  'new Canvas constructor takes version':'int worldDetailVersion)' in canvas,
  'v2 properties gated':'worldDetailVersion >= 2' in canvas,
  'CityPainter supplies saved version':'plan.settings.worldDetailVersion()' in painter,
  'old detail preserved':'return worldDetailVersion > 0;' in canvas,
  'old block/lots selection maintained':'plan.settings.worldDetailVersion() > 0' in chooser,
  'new neighborhood lot selection v2 only':'plan.settings.worldDetailVersion() >= 2' in chooser,
  'neighborhood size 320':'NEIGHBORHOOD_SIZE = 320' in plan,
  'negative coordinate handling':'Math.floorDiv(x, NEIGHBORHOOD_SIZE)' in plan,
  'local facade profiles count':'FACADE_PROFILES = 6' in plan,
  'roof profiles count':'ROOF_PROFILES = 5' in plan,
  'footprint profiles 8':'return CityHash.range' in plan and ' 8);' in plan,
  'v2 footprint gated':'c.hasArchitectureVariation() ? ArchitecturePass13.footprint' in world,
  'footprint access protected':'Tower.fits(r)' in architecture and 'r.width() >= 11' in architecture,
  'facades on exterior':'b.z1() + 1' in architecture,
  'front doorway center not blocked':'middle three entrance blocks open' in architecture,
  'planted sidewalks protected':'if (xb != null && xb.kind() == Line.Kind.TRAM' in architecture,
  'canals protected':'if (xb != null && xb.kind() == Line.Kind.CANAL' in architecture,
  'junctions unchanged':'if (xb != null && zb != null) return;' in architecture,
  'street painting version gated':'if (c.hasArchitectureVariation()) ArchitecturePass13.streetSurface' in painter,
  'style contextualized for v2':'CityArchitecture.styleHash(c.seed()' in lot,
  'rooftop profiles wired':'ArchitecturePass13.tower(c, box, floors, style, hash, roof)' in tower,
  'roof farmland/water protected':'roof == Tower.Roof.GARDEN || roof == Tower.Roof.TANK' in architecture,
  'storefront profiles wired':'ArchitecturePass13.shopfront(c, box, lot.hash())' in lot,
  'industrial rooftop profiles wired':lot.count('ArchitecturePass13.industrialRoof(')==2,
  'character selection depends on seed': 'CityHash.of(seed, NEIGHBORHOOD_SALT' in plan,
  'no runtime randomness':'RandomSource' not in architecture and 'Math.random' not in architecture,
  'derelict ruin logic retained':'static void decorate(' in derelict,
  'primary sewers remain':'SewerCaveBranches.paint(canvas, plan);' in painter,
  'original mine galleries remain':'paintGalleries(canvas' in painter,
  'stair flight retained':'flight(c, interior, floorY, style.stairs());' in tower,
  'old city detail still accessible':'WorldDetail.tower(c, box, floors, style, hash, roof);' in tower,
}
errors=[]
for name,passed in checks.items():
    print(('PASS' if passed else 'FAIL'),name)
    if not passed:errors.append(name)
print(f'ARCHITECTURE PASS13 STATIC: {len(checks)-len(errors)}/{len(checks)}')
if errors:raise SystemExit(1)
