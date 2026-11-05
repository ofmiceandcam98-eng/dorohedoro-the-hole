#!/usr/bin/env python3
"""Static asset and integration validation only; never substitutes for NeoForge compile or game QA."""
from pathlib import Path
import json, re, sys
root=Path(__file__).resolve().parents[1]
java=root/'src/main/java/com/dorohedoro/hole'
res=root/'src/main/resources'
generated=root/'src/generated/resources'
lib=lambda name:(java/name).read_text(encoding='utf-8')
checks=[]
def check(name, result):
    checks.append((name,bool(result)))
    print(('PASS ' if result else 'FAIL ')+name)
blocks=lib('registry/ModBlocks.java')
lot=lib('worldgen/city/LotPainter.java')
canvas=lib('worldgen/city/Canvas.java')
plan=lib('worldgen/city/CityPlan.java')
settings=lib('worldgen/city/CitySettings.java')
city=lib('worldgen/city/LoreArchitecture19.java')
check('version 8 is persisted and accepted', all(x in settings for x in ['worldDetailVersion > 14','0..14']))
check('old generator versions preserved', 'return worldDetailVersion >= 8;' in canvas)
check('new source opts into lore architecture', 'LoreArchitecture19.styleFor' in lot and 'hasLoreArchitecture()' in lot)
check('district palettes cover all seven zones', all(x in city for x in ['RESIDENTIAL','MARKET','INDUSTRIAL','WATERFRONT','DERELICT','GRAVEYARD','OUTSKIRTS']))
check('new foundation uses region wear', 'Math.floorDiv(x, 5)' in city)
check('street surface has actual curb and drain', 'IRON_TRAPDOOR' in city and 'int curb' in city)
check('no old colorful awning alongside the lore one', 'if (!c.hasLoreArchitecture()) awning(' in lot)
check('old portal fix retained', 'findSafeDestination' in lib('portal/PortalService.java') or 'findSafe' in lib('portal/PortalService.java'))
check('Cavern Lamp uses LanternBlock', 'new net.minecraft.world.level.block.LanternBlock(' in blocks)
check('standing/hanging variants exist', '"hanging": "false"' in (res/'assets/dorohedoro/blockstates/cavern_lamp.json').read_text() and '"hanging": "true"' in (res/'assets/dorohedoro/blockstates/cavern_lamp.json').read_text())
check('Cavern Lamp luminous model exists', all((res/f'assets/dorohedoro/models/block/cavern_lamp_{v}.json').is_file() for v in ['standing','hanging']))
check('Cavern Lamp item model exists', (res/'assets/dorohedoro/models/item/cavern_lamp.json').is_file())
new_blocks=['smoke_stained_concrete','water_damaged_plaster','soot_blackened_brick','patchwork_masonry','rusted_industrial_sheet','seepage_brick']
for b in new_blocks:
    check(f'{b}: registered', b.upper() in blocks)
    check(f'{b}: texture', (res/f'assets/dorohedoro/textures/block/{b}.png').is_file())
    check(f'{b}: blockstate', (generated/f'assets/dorohedoro/blockstates/{b}.json').is_file())
    check(f'{b}: block model', (generated/f'assets/dorohedoro/models/block/{b}.json').is_file())
    check(f'{b}: item model', (generated/f'assets/dorohedoro/models/item/{b}.json').is_file())
    check(f'{b}: recipe', (res/f'data/dorohedoro/recipe/{b}.json').is_file())
    check(f'{b}: self-drop', (generated/f'data/dorohedoro/loot_table/blocks/{b}.json').is_file())
check('full source includes Tensura benchmark', (root/'docs/TENSURA_REINCARNATED_DEPTH_STANDARD.md').is_file())
check('full source includes Tensura depth matrix', (root/'docs/TENSURA_DEPTH_FEATURE_MATRIX.csv').is_file())
check('full source includes older portal fix QA script', (root/'tools/qa-portal-landing-pass18.py').is_file())
invalid=[]
for f in res.rglob('*.json'):
    try: json.loads(f.read_text(encoding='utf-8'))
    except Exception as e: invalid.append(f'{f.relative_to(root)}: {e}')
check('all main source JSON parses', not invalid)
for err in invalid[:15]: print('  '+err)
invalid=[]
for f in (root/'src/generated/resources').rglob('*.json'):
    try: json.loads(f.read_text(encoding='utf-8'))
    except Exception as e: invalid.append(f'{f.relative_to(root)}: {e}')
check('all generated source JSON parses', not invalid)
for err in invalid[:15]: print('  '+err)
passed=sum(result for _,result in checks)
print(f'STATIC PASS19 QA: {passed}/{len(checks)} passed; not a compile, Minecraft run, or visual signoff.')
sys.exit(passed != len(checks))
