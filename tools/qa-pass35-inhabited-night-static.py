#!/usr/bin/env python3
"""Source-contract checks only. Not a substitute for Minecraft/NeoForge runtime QA."""
from pathlib import Path
import json, re
ROOT = Path(__file__).resolve().parents[1]
CITY = ROOT / 'src/main/java/com/dorohedoro/hole/worldgen/city'
def load(name): return (CITY / name).read_text(encoding='utf-8')
checks=[]
def check(name, ok):
    checks.append((name, bool(ok)))
    print(('PASS' if ok else 'FAIL') + ' ' + name)

settings=load('CitySettings.java')
canvas=load('Canvas.java')
rooms=load('OccupiedInteriors35.java')
lot=load('LotPainter.java')
openlots=load('OpenLots.java')
city=load('CityPainter.java')
biome=(ROOT/'src/main/java/com/dorohedoro/hole/datagen/HoleBiomeData.java').read_text()
preset=json.loads((ROOT/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
new_version=preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']
version_range = re.search(r'Codec.intRange\(0,\s*(\d+)\)\.optionalFieldOf\("world_detail_version"', settings)
check('new-world profile retains Pass35 features',new_version>=20 and version_range is not None and int(version_range.group(1)) >= new_version)
check('older worlds keep version-gated generation','worldDetailVersion >= 20' in canvas and 'hasOccupiedInteriors35()' in canvas)
check('full-height interior partitions and door', 'floorY + 3; y++' in rooms and 'c.door(doorX' in rooms)
check('homes and shop floor receive room-purpose furnishing', lot.count('OccupiedInteriors35.home(')>=3 and 'OccupiedInteriors35.shop(' in lot)
check('clinical and industrial roles included', 'OccupiedInteriors35.clinic(' in lot and lot.count('OccupiedInteriors35.industrial(')==2)
check('vanilla job-site fixtures have actual use', all(s in rooms for s in ['Blocks.SMOKER','Blocks.LOOM','Blocks.CRAFTING_TABLE','Blocks.CAULDRON','Blocks.LECTERN']))
check('exposed market chest density reduced', 'CityHash.range(h >>> 30, 11) == 0' in openlots)
check('survival loot still present in market and starter', 'c.chest(x + 3' in openlots and 'starter_cache' in load('Landmarks.java'))
check('exterior lamps reduced, near spawn still lit', 'lightSpacing' in city and '? 30 : 45' in city)
check('factory lights and porch lights sparse', 'Math.floorMod(x / 6 + z / 6, 4)' in lot and 'CityHash.range(h >>> 10, 4)' in load('Housing25.java'))
check('normal hostile spawn registrations retained',all('EntityType.'+s in biome for s in ['ZOMBIE','SKELETON','SPIDER','CREEPER']))
print(f'{sum(v for _,v in checks)}/{len(checks)} source checks passed')
raise SystemExit(0 if all(v for _,v in checks) else 1)
