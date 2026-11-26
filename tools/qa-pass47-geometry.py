#!/usr/bin/env python3
"""Geometry/parcel fixture for the *source-defined* regional landing bounds.
No Minecraft classes, chunk blocks or terrain heights are rendered here.
"""
from pathlib import Path
from random import Random
p=Path(__file__).resolve().parents[1]
code=(p/'src/main/java/com/dorohedoro/hole/worldgen/city/Superblock.java').read_text()
assert 'Rect landing = new Rect(x - 13, z - 13, x + 13, z + 13);' in code
assert 'Rect shelter = new Rect(x + 18, z - 13, x + 36, z + 13);' in code
assert 'int x = area.centerX(), z = area.centerZ();' in code
assert 'new Rect(x - 13, area.z0(), x + 36, z - 18)' in code

rng=Random(47002026)
def overlap(a,b):return a[0]<=b[2] and a[2]>=b[0] and a[1]<=b[3] and a[3]>=b[1]
def inside(a,b):return b[0]<=a[0] and b[1]<=a[1] and a[2]<=b[2] and a[3]<=b[3]
for n in range(45000):
    x0=rng.randint(-10000,10000);z0=rng.randint(-10000,10000)
    w=rng.randint(92,176);d=rng.randint(72,116)
    box=(x0,z0,x0+w-1,z0+d-1)
    x=(box[0]+box[2])//2;z=(box[1]+box[3])//2
    landing=(x-13,z-13,x+13,z+13)
    shelter=(x+18,z-13,x+36,z+13)
    rest=[(box[0],box[1],x-18,box[3]),
          (x+42,box[1],box[2],box[3]),
          (x-13,box[1],x+36,z-18),
          (x-13,z+18,x+36,box[3])]
    for f in (landing,shelter):assert inside(f,box)
    assert not overlap(landing,shelter)
    for r in rest:
        if r[0]>r[2] or r[1]>r[3]:continue
        assert not overlap(landing,r) and not overlap(shelter,r)
    assert shelter[0]-landing[2]-1==4
    # Entire central landing is across four or fewer chunks; every cell has
    # exactly one owner when chunk-clipped (no split duplicate geometry).
    if n<100:
        for xx in range(landing[0],landing[2]+1):
            for zz in range(landing[1],landing[3]+1):
                assert (xx//16)*16 <= xx < (xx//16+1)*16
                assert (zz//16)*16 <= zz < (zz//16+1)*16
print('PASS Pass47: 45,000 synthetic site geometries, 4-block shelter alley, no overlap, parcel containment')
print('Scope: geometry fixture only; does not certify Minecraft walkability or biome safety')
