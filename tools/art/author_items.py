"""Hand-authored 16x16 item sprites (Mojang-standard pass): ore drops, ingots and the weakest
mob-drop and smoke sprites.

Vanilla item conventions followed here: the sprite fills most of the 16x16 canvas, the outline
is the darkest shade of the material itself (never pure black), light comes from the top left,
and members of one family share a silhouette (vanilla iron, gold and copper ingots share one
shape; so do ours). Run: py tools/art/author_items.py
"""
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'dorohedoro', 'textures', 'item')

INGOT = [
    '................',
    '................',
    '................',
    '................',
    '................',
    '...oooooooooo...',
    '..o4444443332o..',
    '.o433333333322o.',
    '.o222222222221o.',
    '.o322222222211o.',
    '.o222222222111o.',
    '.o122222221111o.',
    '..oooooooooooo..',
    '................',
    '................',
    '................',
]

# A heavy metallic lump with a smaller one fused on, like vanilla's raw metal family.
RAW_METAL = [
    '................',
    '................',
    '.....oooo.......',
    '....o4433o.ooo..',
    '...o443332o433o.',
    '..o44333221o32o.',
    '..o43332221o21o.',
    '.o433322211oo1o.',
    '.o4332222111oo..',
    'o433222211111o..',
    'o3322221111ooo..',
    'o3222211111o321o',
    '.o2221111oo32211',
    '..oo1111o.o2111o',
    '....oooo...oooo.',
    '................',
]

# A crust of stubby crystals on a rough base.
RAW_CRYSTAL = [
    '................',
    '................',
    '......o.........',
    '.....o4o...o....',
    '..o..o3o..o4o...',
    '.o4o.o3o.o43o...',
    '.o3oo432oo32o.o.',
    '.o32o3322o321o4o',
    'o433332221o2213o',
    'o3332222211o2111',
    'o3322221111o111o',
    '.o2222111111111o',
    '.o21111111111oo.',
    '..oo11111oooo...',
    '....ooooo.......',
    '................',
]

# Three cubic crystals, each lit on the top-left faces.
CUBES = [
    '................',
    '................',
    '....oooooo......',
    '...o444443o.....',
    '..o4444433o.....',
    '..o4444332o.....',
    '..o3333322ooooo.',
    '..o3333221o4443o',
    '..o333221o44433o',
    'ooooo2221o44332o',
    'o4443o11oo3332o.',
    'o44433ooo33321o.',
    'o33322o.o32221o.',
    'o3322o..oooooo..',
    '.oooo...........',
    '................',
]

ITEMS = {
    'zinc_ingot': (INGOT, ['#3b444d', '#6d7a86', '#8c9aa6', '#b3c0ca', '#dde6ec']),
    'brass_ingot': (INGOT, ['#4a3516', '#8a6a2a', '#b08c3c', '#d2b05a', '#efd88f']),
    'raw_zinc': (RAW_METAL, ['#353e46', '#5e6a75', '#7f8d99', '#a5b3be', '#cfdbe3']),
    'raw_sulfur': (RAW_CRYSTAL, ['#4f4210', '#8c7b1d', '#bfae2e', '#ddd24e', '#f4ee96']),
    'rock_salt': (CUBES, ['#6f5a56', '#a68c87', '#cdb8b1', '#e8d9d3', '#faf3f0']),
}

# Sprites with their own palettes (key -> colour). Same conventions: material-coloured outline,
# top-left light, most of the canvas filled.
TUBE = [
    '................',
    '................',
    '................',
    '................',
    '...........oooo.',
    '..........o4432o',
    '..........o4321o',
    '...ooooooo43321o',
    '..o44444443321o.',
    '..o3333333221o..',
    '..o43221111oo...',
    '..o321ooooo.....',
    '..o321o.........',
    '..o432o.........',
    '...ooo..........',
    '................',
]
TUBE_PAL = {'o': '#2c3a3a', '1': '#5a6d68', '2': '#7d918b', '3': '#a4b5ae', '4': '#c8d6cf'}
# Preserved: the same duct cured darker, with brass collars at both ends and the middle.
PRESERVED = [
    '................',
    '................',
    '................',
    '................',
    '...........oooo.',
    '..........oBBbbo',
    '..........o4321o',
    '...ooooooo43321o',
    '..o4444B443321o.',
    '..o3333b33221o..',
    '..o43221b11oo...',
    '..o321ooooo.....',
    '..o321o.........',
    '..oBBbo.........',
    '...ooo..........',
    '................',
]
PRESERVED_PAL = {'o': '#243030', '1': '#47574f', '2': '#62756c', '3': '#82968c', '4': '#a3b5aa',
                 'b': '#8a6a2a', 'B': '#d2b05a'}
SHALE_CHUNK = [
    '................',
    '................',
    '.....ooooo......',
    '...oo33232oo....',
    '..o3322223222o..',
    '.o22111211112o..',
    '.o333232233332o.',
    'o2222111222211o.',
    'o1111111111111o.',
    'o33322332333332o',
    'o22211222122111o',
    '.o11111111111oo.',
    '..o222433322o...',
    '...ooooooooo....',
    '................',
    '................',
]
SHALE_PAL = {'o': '#120f0c', '1': '#231d17', '2': '#352c23', '3': '#4a3e31', '4': '#6a6676'}
SIGIL = [
    '................',
    '................',
    '.....oooooo.....',
    '...oo443332oo...',
    '..o44kkkkkk32o..',
    '..o4kkk43kkk2o..',
    '.o44kk4kk3kk32o.',
    '.o4kk4kkkk3kk2o.',
    '.o3kk3kkkk2kk1o.',
    '.o33kk3kk2kk21o.',
    '..o3kkk22kkk1o..',
    '..o32kkkkkk21o..',
    '...oo222111oo...',
    '.....oooooo.....',
    '................',
    '................',
]
SIGIL_PAL = {'o': '#3a2814', '1': '#6e4a22', '2': '#946530', '3': '#b98a44', '4': '#dcb76a', 'k': '#2a2420'}
CLAW = [
    '................',
    '...........oo...',
    '..........o4o...',
    '.........o43o...',
    '........o432o...',
    '.......o4432o...',
    '......o44321o...',
    '.....o44321o....',
    '....o44321o.....',
    '...o44321o......',
    '..o4ddd1o.......',
    '.oddddddo.......',
    '.odddddo........',
    '..oooo..........',
    '................',
    '................',
]
CLAW_PAL = {'o': '#3b3024', '1': '#8f8470', '2': '#b3a88e', '3': '#d4cbb0', '4': '#efe8d2', 'd': '#5a4030'}
ANTENNA = [
    '................',
    '.............oo.',
    '............o2o.',
    '...........o3o..',
    '..........o2o...',
    '.........o3o....',
    '........o2o.....',
    '.......o3o......',
    '......o2o.......',
    '.....o3o........',
    '....o32o........',
    '...o443o........',
    '..o4443o........',
    '..o443o.........',
    '..oooo..........',
    '................',
]
ANTENNA_PAL = {'o': '#2e1d12', '2': '#5a3a24', '3': '#7a5233', '4': '#9a6a42'}
EXTRA = {
    'smoke_conduit': (TUBE, TUBE_PAL),
    'preserved_smoke_conduit': (PRESERVED, PRESERVED_PAL),
    'oil_shale_chunk': (SHALE_CHUNK, SHALE_PAL),
    'devil_sigil': (SIGIL, SIGIL_PAL),
    'burrower_claw': (CLAW, CLAW_PAL),
    'roach_antenna': (ANTENNA, ANTENNA_PAL),
}


def main():
    jobs = {n: (rows, {'o': r[0], '1': r[1], '2': r[2], '3': r[3], '4': r[4]}) for n, (rows, r) in ITEMS.items()}
    jobs.update(EXTRA)
    for name, (rows, pal) in jobs.items():
        assert len(rows) == 16 and all(len(r) == 16 for r in rows), (name, [len(r) for r in rows])
        im = Image.new('RGBA', (16, 16))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch != '.':
                    c = pal[ch]
                    im.putpixel((x, y), (int(c[1:3], 16), int(c[3:5], 16), int(c[5:7], 16), 255))
        im.save(os.path.join(OUT, name + '.png'))
        print('wrote item/' + name)


if __name__ == '__main__':
    main()
