#!/usr/bin/env python3
"""Source + JSON contracts; no client or Gradle runtime implied."""
from pathlib import Path
import json
p=Path(__file__).resolve().parents[1]
c=p/'src/main/java/com/dorohedoro/hole/worldgen/city'
a={k:(c/k).read_text() for k in ('CitySettings.java','Canvas.java','LotPainter.java','NeighborhoodSkyline46.java')}
preset=json.loads((p/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
version=preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']
checks={
 'new world profile includes v30 skyline':version>=30,
 'old worlds default to 0':'optionalFieldOf("world_detail_version", 0)' in a['CitySettings.java'],
 'v0..v33 codec range matches validation':'Codec.intRange(0, 52)' in a['CitySettings.java'] and 'worldDetailVersion > 52' in a['CitySettings.java'],
 'version-gated skyline':'hasNeighborhoodSkyline46() { return worldDetailVersion >= 30; }' in a['Canvas.java'],
 'both residential types use clustering':a['LotPainter.java'].count('NeighborhoodSkyline46.floors(')==2,
 'city-only applies to both residences':a['LotPainter.java'].count('c.isHoleCity(lot.rect().centerX(), lot.rect().centerZ())')==2,
 'real story count drives existing Tower builds':a['LotPainter.java'].count('Tower.build(c, box, floors, style')>=3,
 'user deferred biome hazards remain deferred':all(s not in a['NeighborhoodSkyline46.java'] for s in ['POISON','SLUDGE_ICE','ASH_SNOW']),
 'original Phase45 two-room service court preserved':(c/'InhabitedCourts45.java').exists() and 'hasInhabitedCourts45()' in a['Canvas.java'],
 'official wrapper recovery available':(p/'tools/restore-official-gradle-wrapper.ps1').exists(),
 'all JSON parse':all(json.loads(f.read_text()) is not None for f in (p/'src').rglob('*.json')),
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL'),k)
print(f"Pass46 source: {sum(checks.values())}/{len(checks)} checks; no NeoForge/client compile attempted")
assert all(checks.values())
