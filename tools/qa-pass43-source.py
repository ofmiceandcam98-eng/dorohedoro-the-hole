#!/usr/bin/env python3
"""Pass 43 source-only integration, reproducibility and compatibility checks."""
from pathlib import Path
import json, re, sys, hashlib
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/java/com/dorohedoro/hole/worldgen/city'
R=ROOT/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json'
def txt(p):return p.read_text(encoding='utf-8')
checks=[]
def check(name, value):
    checks.append(bool(value));print(('PASS' if value else 'FAIL')+' '+name)
codec=txt(BASE/'CitySettings.java');canvas=txt(BASE/'Canvas.java')
preset=json.loads(txt(R)); version=preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']
check('New world opts in to Pass 43',version>=27)
check('Codec accepts new version with saved worlds defaulting 0',
    'Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)' in codec
    and 'worldDetailVersion > 52' in codec)
check('Existing Pass 42 region version gate retained', 'worldDetailVersion() >= 26' in txt(BASE/'CityPlan.java'))
check('Existing versions gate out Pass 43 civic work', 'hasOccupiedCommons43() { return worldDetailVersion >= 27; }' in canvas)
lot=txt(BASE/'OpenLots.java');land=txt(BASE/'Landmarks.java')
check('Market switch preserves old layout in saved worlds',
    'if (c.hasOccupiedCommons43()) { OccupiedCommons43.market(c, lot); return; }' in lot)
check('Spawn plaza postpass only on new worlds',
    'if (c.hasOccupiedCommons43()) OccupiedCommons43.plaza(c, lot)' in land)
paint=txt(BASE/'CityPainter.java')
check('Backstreets called after existing streets before lots',
      paint.index('UrbanFabric25.streets(')<paint.index('Backstreets43.paint(')<paint.index('LotPainter.paint('))
check('Quays called after existing river painting before lots',
      paint.index('RiverChannels24.paint(')<paint.index('CanalWorksites43.paint(')<paint.index('LotPainter.paint('))
check('Lamp spacing changes are gated, older formula preserved',
      'worldDetailVersion() >= 27' in paint and 'streetlamp43(plan, band' in paint
      and 'Math.floorMod(along + band.center(), lightSpacing) == 0' in paint)
streets=txt(BASE/'Backstreets43.java');quays=txt(BASE/'CanalWorksites43.java');market=txt(BASE/'OccupiedCommons43.java')
check('No new streetlamps/chests in Pass 43 classes',
      not any(re.search(r'c\.(?:chest|sign)\(', s) or 'LANTERN' in s or 'REDSTONE_LAMP' in s
              for s in [streets,quays,market]))
check('Street exclusions explicitly preserve lots/arterials/frontiers',
      all(q in streets for q in ['inLot(lots, x, z)','arterial(plan, xLines, zLines, x, z)', 'Region.CITY']))
check('Quay stations have air and solid bank tests',
      all(q in quays for q in ['c.get(x, CityPlan.STREET_Y, z).isAir()',
                             '!c.get(x, CityPlan.GROUND_Y, z).isAir()',
                             'nearCrossing(zs, along, 24)']))
check('Market uses actual utility workstations with service aisles',
      all(q in market for q in ['Blocks.SMOKER','Blocks.LOOM','Blocks.STONECUTTER',
                               'Blocks.FLETCHING_TABLE','Blocks.COMPOSTER','bx += 10','z += 9']))
check('Starter spawn equipment retained', '"starter_cache"' in land and '"THE HOLE"' in land)
check('Sewer underworld and mining retained in preset',preset['dimensions']['minecraft:overworld']['generator']['city']['sewer_cave_frequency']==30)
check('All JSON parses',all(json.loads(txt(p)) is not None for base in [ROOT/'src/main/resources',ROOT/'src/generated/resources'] for p in base.rglob('*.json')))
print(f'Pass 43 offline source QA: {sum(checks)}/{len(checks)} passed')
print('NOT VERIFIED: Full NeoForge compile, client display, real-world connectivity, mob spawns, save migration.')
sys.exit(0 if all(checks) else 1)
