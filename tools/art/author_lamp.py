"""Cavern Lamp: hand-drawn texture atlas, inventory sprite and the three block models.

Built the way vanilla builds its lantern: one body box whose side face shows the frame and
the light (6x7), a 4x4 cap, and a crossed-plane handle, all with explicit UVs into one 16x16
atlas. Ours is aged brass with a pale moth-scale glow, the light source the recipe uses
(Luminescent Scales), so it reads as its own lamp beside a vanilla lantern.

The block's collision shapes in CavernLampBlock are unchanged; every model stays inside them:
standing y0-11, ceiling y5-16, wall y3-13 reaching back to z0.

Run: py tools/art/author_lamp.py
"""
import json
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'dorohedoro')

PAL = {
    '.': None,
    'k': '#2f2618',   # brass, deepest
    'd': '#4f3d22',
    'm': '#765b30',
    'l': '#a2803f',
    'h': '#c9a656',   # brass highlight
    '1': '#6f8f55',   # glow: dim edge of the glass
    '2': '#a3c47a',
    '3': '#cfe8a0',
    '4': '#ecf9c8',
    '5': '#ffffef',   # glow: the scale itself
}

# Atlas regions (x0, y0, x1, y1) in pixels, mirrored into the model UVs below.
SIDE = (0, 2, 6, 9)        # body side, 6x7
TOP = (0, 9, 6, 15)        # body top/bottom, 6x6
CAP_TOP = (1, 10, 5, 14)   # inner 4x4 of TOP
CAP_SIDE = (1, 0, 5, 2)    # cap side, 4x2
HANDLE = (11, 1, 14, 3)    # handle arch, 3x2
ARM_SIDE = (6, 0, 13, 1)   # wall bracket arm, side (7 long)
ARM_TOP = (6, 1, 8, 8)     # wall bracket arm, top (2x7)
PLATE = (8, 7, 12, 12)     # wall plate, front (4x5)
PLATE_SIDE = (12, 7, 13, 12)

ATLAS = [
    '.lhll.mlmmmlm...',   # 0: cap side top row; bracket arm side
    '.dmmd.ml....m...',   # 1: cap side bottom row; arm top; handle arch
    'dlhhldml...m.m..',   # 2: body side: top band with a rivet glint
    'm2332dml........',   # 3: glass, dim at the edges
    'm3443dml........',   # 4
    'l3553dml........',   # 5: the moth scale, brightest
    'm3443dml........',   # 6
    'm2332dmllhlmm...',   # 7: wall plate
    'dmllmk..lmmdd...',   # 8: body side: bottom band
    'dmmmmd..lmmdd...',   # 9: body top/bottom plate
    'mllhld..lmmdd...',   # 10
    'mlhlld..mddkk...',   # 11
    'mllmld..........',   # 12
    'mlllmd..........',   # 13
    'dddddk..........',   # 14
    '................',   # 15
]

ITEM = [
    '................',
    '................',
    '.......mm.......',
    '......m..m......',
    '......d..d......',
    '.......dd.......',
    '......dlld......',
    '.....dlhhld.....',
    '.....m2332d.....',
    '.....m3443d.....',
    '.....l3553d.....',
    '.....m3443d.....',
    '.....m2332d.....',
    '.....dmllmk.....',
    '................',
    '................',
]


def draw(rows):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), [len(r) for r in rows]
    im = Image.new('RGBA', (16, 16))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            c = PAL[ch]
            if c:
                im.putpixel((x, y), (int(c[1:3], 16), int(c[3:5], 16), int(c[5:7], 16), 255))
    return im


def face(uv, cull=None):
    f = {'uv': list(uv), 'texture': '#lamp'}
    if cull:
        f['cullface'] = cull
    return f


def box(frm, to, sides, top=None, bottom=None, cull_down=False):
    faces = {d: face(sides) for d in ('north', 'south', 'east', 'west')}
    if top:
        faces['up'] = face(top)
    if bottom:
        faces['down'] = face(bottom, 'down' if cull_down else None)
    return {'from': frm, 'to': to, 'faces': faces}


def handle(y0, y1):
    """Two crossed planes, the vanilla lantern handle construction."""
    h0, h1 = HANDLE[0], HANDLE[2]
    rot = {'origin': [8, 8, 8], 'axis': 'y', 'angle': 45}
    return [
        {'from': [6.5, y0, 8], 'to': [9.5, y1, 8], 'rotation': rot, 'shade': False,
         'faces': {'north': face((h1, HANDLE[1], h0, HANDLE[3])), 'south': face(HANDLE)}},
        {'from': [8, y0, 6.5], 'to': [8, y1, 9.5], 'rotation': rot, 'shade': False,
         'faces': {'west': face((h1, HANDLE[1], h0, HANDLE[3])), 'east': face(HANDLE)}},
    ]


def lamp(y, cull_down=False):
    """Body (7 tall) and cap (2 tall) with the body's base at height y."""
    return [box([5, y, 5], [11, y + 7, 11], SIDE, TOP, TOP, cull_down),
            box([6, y + 7, 6], [10, y + 9, 10], CAP_SIDE, CAP_TOP, CAP_TOP)]


def model(elements):
    m = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
         'textures': {'particle': 'dorohedoro:block/cavern_lamp',
                      'lamp': 'dorohedoro:block/cavern_lamp'},
         'elements': elements}
    return m


def write_json(path, data):
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def main():
    draw(ATLAS).save(os.path.join(ASSETS, 'textures', 'block', 'cavern_lamp.png'))
    draw(ITEM).save(os.path.join(ASSETS, 'textures', 'item', 'cavern_lamp.png'))
    models = os.path.join(ASSETS, 'models', 'block')
    # Standing: on the floor, handle arch on top (y0-11).
    write_json(os.path.join(models, 'cavern_lamp_standing.json'),
               model(lamp(0, cull_down=True) + handle(9, 11)))
    # Hanging: body high in the cell, hook up to the ceiling (y5-16).
    write_json(os.path.join(models, 'cavern_lamp_hanging.json'),
               model(lamp(5) + handle(14, 16)))
    # Wall: body y3-12, a bracket arm from a plate on the wall (z0) running over the top of
    # the cap, so the support shows from the front (an arm into the cap's side was hidden
    # behind the body and the lamp looked like it floated).
    write_json(os.path.join(models, 'cavern_lamp_wall.json'), model(lamp(3) + [
        {'from': [7, 12, 1], 'to': [9, 13, 8],
         'faces': {'east': face(ARM_SIDE), 'west': face(ARM_SIDE), 'up': face(ARM_TOP),
                   'down': face(ARM_TOP), 'south': face((6, 1, 8, 2))}},
        {'from': [6, 8, 0], 'to': [10, 13, 1],
         'faces': {'south': face(PLATE), 'north': face(PLATE, 'north'), 'up': face(PLATE_SIDE),
                   'down': face(PLATE_SIDE), 'east': face(PLATE_SIDE), 'west': face(PLATE_SIDE)}},
    ]))
    # Inventory: a flat sprite, like vanilla's lantern item.
    write_json(os.path.join(ASSETS, 'models', 'item', 'cavern_lamp.json'),
               {'parent': 'minecraft:item/generated',
                'textures': {'layer0': 'dorohedoro:item/cavern_lamp'}})
    print('wrote cavern lamp atlas, item sprite and 4 models')


if __name__ == '__main__':
    main()
