#!/usr/bin/env python3
"""Offline resource/patch integrity smoke test; NOT a NeoForge compile."""
import json, re, sys
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
java=ROOT/'src/main/java/com/dorohedoro/hole'
resources=ROOT/'src/main/resources'
for p in resources.rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as ex: raise AssertionError(f'Invalid JSON {p}: {ex}')
json_count=len(list(resources.rglob('*.json')))
skill_text=(java/'magic/SmokeSkill.java').read_text()
# Registered enum skill IDs appear as explicit string pairs.
skills={x.lower() for x in re.findall(r'^\s*([A-Z][A-Z_]+)\(MagicType\.', skill_text, re.M)}
icons={p.stem for p in (resources/'assets/dorohedoro/textures/gui/techniques').glob('*.png')}
assert len(icons)==64, len(icons)
assert len(skills)==64, (len(skills), sorted(icons-skills), sorted(skills-icons))
assert skills==icons, (sorted(skills-icons),sorted(icons-skills))
pngs=list(resources.rglob('*.png'))
for p in pngs:
    try:
        with Image.open(p) as image:
            image.verify()
    except Exception as ex: raise AssertionError(f'Bad PNG: {p} {ex}')
assert json.loads((resources/'data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version'] >= 25
assert 'CityTerrain40.frontageOffset(terrainPlan, lot)' in (java/'worldgen/city/Canvas.java').read_text()
assert 'Smoke VFX failure' in (java/'magic/SmokeEngine.java').read_text()
assert 'Smoke mesh render failed' in (java/'client/magic/SmokeMeshClient.java').read_text()
print(f'Pass41 static QA: {json_count} valid JSON files, {len(pngs)} decoded PNGs, 64/64 technique icons, profile and smoke instrumentation present')
print('Scope: does not compile Minecraft/NeoForge classes or check gameplay.')
