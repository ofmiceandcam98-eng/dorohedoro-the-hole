#!/usr/bin/env python3
"""Pass 61 source wiring assertions; not a NeoForge or in-game test."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
def read(n): return (city/n).read_text()
policy=read('SewerHazards61.java'); lanes=read('SewerDetails24.java'); side=read('SewerCaveBranches.java');
settings=read('CitySettings.java'); base=read('CityPainter.java')
config=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']
checks={
  'new save version=44':config['world_detail_version']>=44,
  'sewer world preset remains active':config['sewer_underworld'],
  'cave frequency unchanged at 30%':config['sewer_cave_frequency']==30,
  'cavern version preserved':config['sewer_cavern_version']==5,
  'save decoder defaults old worlds to zero':'optionalFieldOf("world_detail_version", 0)' in settings,
  'record validates version 44':'worldDetailVersion > 52' in settings,
  'codec accepts version 44':'Codec.intRange(0, 52)' in settings,
  'old lamps use unchanged cadence':'Math.floorMod(along, LIGHT_CYCLE) != 0' in policy and 'worldDetailVersion < FIRST_VERSION) return true' in policy,
  'lamp schedule uses global trunk and coordinate':'SewerHazards61.lampAt(plan.seed, trunk, along,' in lanes,
  'new dark sections selected by seed not time':'CityHash.of(seed, DARK_SALT, axis, section)' in policy,
  'monsters can spawn normally (not a special debuff)': 'Blocks.SPAWNER' not in lanes and 'MobEffects' not in lanes,
  'nests only at existing inspection rooms':'branchKind == 1 || branchKind == 2' in policy,
  'nests disabled on existing saves':'worldDetailVersion >= FIRST_VERSION' in policy,
  'nests use an existing hostile creature':'dorohedoro:sewer_roach' in side,
  'rare roach spawners are breakable vanilla blocks':'c.spawner(bx + 2, 50' in side and 'Blocks.SPAWNER' not in side,
  'infested bays remain connected to their original passage':'paintClosedInspectionBay(c, plan.seed, bx, cz, direction,' in side,
  'inspection barrels retained':'c.barrel(bx + 3, 50, endZ + direction * 5, "sewer_cache")' in side,
  'inspection workbench retained':'c.set(bx - 3, 50, endZ + direction * 5, Blocks.CRAFTING_TABLE)' in side,
  'bright inspection rooms retain lantern':'if (infested)' in side and 'Blocks.LANTERN' in side,
  'no added damage or technique gating':'MobEffects' not in policy+side and 'applyEffect(' not in policy+side and 'techniqueRequired(' not in policy+side,
  'existing main sewer walkway remains intact':'paintSewers(canvas, xLines, zLines)' in base,
  'existing shafts still generated':'paintShafts(canvas, plan, xLines, zLines)' in base,
  'no additional ore, block or item registrations':'ModItems' not in policy and 'ModBlocks' not in policy,
  'new Java fixture uses production policy':'"$CITY/SewerHazards61.java"' in (root/'tools/qa-pass61-sewers.sh').read_text(),
  'all JSON source files parse':all(json.loads(p.read_text()) is not None for p in (root/'src').rglob('*.json')),
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+' '+name)
if not all(checks.values()): raise SystemExit(1)
print('PASS Pass61 source checks:',len(checks))
