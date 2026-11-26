#!/usr/bin/env python3
"""Cavern animation Pass10 offline regression; does NOT certify Minecraft runtime visuals."""
from pathlib import Path
import json, math, sys
from PIL import Image, ImageChops
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'tools'))
import importlib.util
spec=importlib.util.spec_from_file_location('cavern_renderer', ROOT/'tools/render-cavern-motion10.py')
r=importlib.util.module_from_spec(spec);spec.loader.exec_module(r)
MOBS={'sludge_crawler':('SludgeCrawler','drip_0','drip_5'),
      'rust_eater_beetle':('RustEaterBeetle','antenna_l','mandible_r'),
      'cavern_burrower':('CavernBurrower','paw_l_0','paw_r_1'),
      'spore_crawler':('SporeCrawler','spore_sac_0','spore_sac_2')}
checks=0
for mob,(klass,first,last) in MOBS.items():
    d=json.loads((ROOT/'docs/model_geometry'/f'{mob}.json').read_text())
    source=(ROOT/'src/main/java/com/dorohedoro/hole/client/entity'/f'{klass}Model.java').read_text()
    assert first in d['groups'] and last in d['groups'],mob
    assert first in d['part_pivots'] and last in d['part_pivots'],mob
    for k,expression in d['animations'].items():
        assert k.split('.')[0] in d['groups'],(mob,k)
        assert f'{k} = {expression};' in source,(mob,k)
        checks+=1
    for group in d['groups']:
        samples=[r.authored_pose(mob,group,t,'MOVE') for t in [0.0,0.25,0.5,0.75,1.0,1.25]]
        assert all(math.isfinite(v) for ang,off in samples for v in (*ang,*off)),(mob,group)
        # All rig parts must stay within a generous physically-plausible rotation envelope.
        assert all(abs(v)<2 for ang,_ in samples for v in ang),(mob,group)
        checks+=1
    for state in ['MOVE','IDLE','ATTACK']:
        for t in [0.0,.5,1.0,2.0]:
            for group in [first,last,'body','head']:
                ang,off=r.authored_pose(mob,group,t,state)
                assert len(ang)==3 and len(off)==3
                checks+=1
    assert r.authored_pose(mob,first,0.1,'MOVE')!=r.authored_pose(mob,first,0.6,'MOVE'),(mob,first)
    assert r.authored_pose(mob,first,0.3,'MOVE') != r.authored_pose(mob,first,0.3,'IDLE'),mob
    checks+=2
    gf=ROOT/'docs/previews/animation_pass10'/f'{mob}_movement_5s.gif'
    im=Image.open(gf)
    assert im.n_frames==50,(mob,im.n_frames)
    total=0
    for f in range(im.n_frames):
        im.seek(f);total+=im.info.get('duration',0)
    assert total==5000,(mob,total)
    im.seek(0);a=im.convert('RGB').copy(); im.seek(12);b=im.convert('RGB').copy()
    assert ImageChops.difference(a,b).getbbox() is not None,(mob,'static preview')
    checks+=3

# Ensure deliberate surface Beetle spawn behavior was not lost.
for name,w in [('hole_industrial',9),('hole_derelict',6),('hole_outskirts',3),('hole_undercity',18)]:
    path=ROOT/'src/generated/resources/data/dorohedoro/worldgen/biome'/f'{name}.json'
    biome=json.loads(path.read_text()); sp=[s for s in biome['spawners']['creature'] if s['type']=='dorohedoro:rust_eater_beetle']
    assert len(sp)==1 and sp[0]['weight']==w,(name,sp)
    checks+=1
c=Image.open(ROOT/'docs/previews/animation_pass10/cavern_movement_5s.gif')
assert c.n_frames==50
assert sum((c.seek(i) or c.info['duration']) for i in range(c.n_frames))==5000
checks+=2
print(f'PASS {checks} offline cavern animation, source mapping, pose and preview checks')
print('IMPORTANT: No NeoForge compilation, live animation, foot contact, AI pathfinding or multiplayer testing.')
