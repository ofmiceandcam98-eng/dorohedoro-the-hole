#!/usr/bin/env python3
"""Offline consistency checks; cannot substitute for NeoForge 1.21.1 build and in-game animation QA."""
from pathlib import Path
from PIL import Image
import json
import math
R=Path(__file__).resolve().parents[1]
G=R/'docs/model_geometry'
J=R/'src/main/java/com/dorohedoro/hole/client/entity'
P=R/'docs/previews/animation_pass08'
ids=['sewer_roach','scrap_rat','sewer_leech','glow_moth','sludge_crawler','rust_eater_beetle','cavern_burrower','spore_crawler']
checks=0
for mob in ids:
    data=json.loads((G/f'{mob}.json').read_text())
    java=(J/f"{data['class']}Model.java").read_text()
    animation=data['animations']
    assert animation, mob
    for key,expression in animation.items():
        group,field=key.rsplit('.',1)
        assert group in data['groups'], (mob,key)
        assert field in ('xRot','yRot','zRot','x','y','z'), (mob,key)
        assert f'{key} = {expression};' in java,(mob,key)
        checks+=1
    gif=Image.open(P/f'{mob}_movement_5s.gif')
    duration=0
    for n in range(gif.n_frames):
        gif.seek(n);duration+=gif.info.get('duration',0)
    assert duration==5000,(mob,duration)
    assert gif.n_frames>=30,(mob,gif.n_frames)
    checks+=2
for mob in ['scrap_rat','cavern_burrower']:
    data=json.loads((G/f'{mob}.json').read_text())
    for side in 'lr':
        for i in range(2):
            key=f'paw_{side}_{i}.y';assert key in data['animations']
            y=float(data['part_pivots'][f'paw_{side}_{i}'][1]);assert y < 0
            checks+=1
for mob in ['sewer_roach','rust_eater_beetle','spore_crawler']:
    data=json.loads((G/f'{mob}.json').read_text())
    for side in 'lr':
        for i in range(3):
            assert f'leg_{side}_{i}.y' in data['animations']
            checks+=1
leech=json.loads((G/'sewer_leech.json').read_text())
for i in range(8):
    for field in ['y','z']:
        assert f'seg_{i}.{field}' in leech['animations']
        checks+=1
for group in ['sewer','cavern']:
    gif=Image.open(P/f'{group}_movement_5s.gif')
    duration=0
    for n in range(gif.n_frames):
        gif.seek(n);duration+=gif.info.get('duration',0)
    assert duration==5000,(group,duration)
    checks+=1
print(f'PASS {checks} movement rig, Java/geometry, foot lift, body wave, and 5-second GIF checks')
