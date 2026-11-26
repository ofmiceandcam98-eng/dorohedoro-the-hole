"""Hand-authored 16x16 block textures for Dorohedoro: The Hole (Mojang-standard pass).

Every texture here is an explicit pixel map: a palette of named colours and a 16x16 grid of
palette keys. Nothing is noise-generated. Masonry, plating and rubble share one helper,
`masonry()`, which applies the vanilla shading rule (top edge lit, bottom and right edges
shaded, gaps darker under an overhang) to a hand-drawn layout, and then applies hand-placed
detail pixels. See docs/art/ART_DIRECTION_MOJANG_STANDARD.md for the rules these follow.

Run:  py tools/art/author_textures.py            (writes into src/main/resources)
      py tools/art/author_textures.py --out DIR  (writes into DIR for review)
"""
import argparse
import os
import sys

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources',
                    'assets', 'dorohedoro', 'textures', 'block')


def hexrgba(h):
    if h is None:
        return (0, 0, 0, 0)
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def check_grid(name, rows):
    if len(rows) != 16 or any(len(r) != 16 for r in rows):
        bad = [(i, len(r), r) for i, r in enumerate(rows) if len(r) != 16]
        raise SystemExit(f'{name}: grid must be 16x16, got {len(rows)} rows; bad rows {bad}')


def grid(name, palette, rows):
    """A plain hand-drawn grid. '.' is transparent unless the palette defines it."""
    check_grid(name, rows)
    im = Image.new('RGBA', (16, 16))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch not in palette:
                raise SystemExit(f'{name}: key {ch!r} at {x},{y} not in palette')
            im.putpixel((x, y), hexrgba(palette[ch]))
    return im


def masonry(name, layout, ramps, tones, gap, gap_shadow, details=None, palette=None,
            bevel=(1, 0, -1, -1)):
    """Shade a hand-drawn block layout the way vanilla bricks, stone bricks and gravel are shaded.

    layout     16 rows; '#' is mortar/gap, any other key is one stone, brick, plate or chunk.
               The texture tiles, so neighbours wrap around the edges.
    ramps      {ramp: [dark .. light colours]}
    tones      {piece key: (ramp, base index)}
    gap        mortar colour; gap_shadow is used where a piece overhangs the gap from above.
    bevel      (top, left, bottom, right) offsets in ramp steps.
    details    16 rows of overrides: '.' keeps the shaded pixel, '+'/'-' nudge one step,
               any other key is a palette colour placed by hand (stains, cracks, rivets).
    """
    check_grid(name, layout)
    if details:
        check_grid(name + ' details', details)
    top, left, bottom, right = bevel
    im = Image.new('RGBA', (16, 16))
    at = lambda x, y: layout[y % 16][x % 16]
    for y in range(16):
        for x in range(16):
            k = at(x, y)
            d = details[y][x] if details else '.'
            if k == '#':
                c = gap_shadow if at(x, y - 1) != '#' else gap
                if d not in '.+-':
                    c = palette[d]
                im.putpixel((x, y), hexrgba(c))
                continue
            ramp, base = tones[k]
            v = base
            if at(x, y - 1) != k:
                v += top
            elif at(x, y + 1) != k:
                v += bottom
            if at(x - 1, y) != k and at(x, y - 1) == k:
                v += left
            if at(x + 1, y) != k and at(x, y - 1) == k and at(x, y + 1) == k:
                v += right
            if d == '+':
                v += 1
            elif d == '-':
                v -= 1
            r = ramps[ramp]
            c = r[max(0, min(len(r) - 1, v))]
            if d not in '.+-':
                c = palette[d]
            im.putpixel((x, y), hexrgba(c))
    return im


def mirror(im):
    return im.transpose(Image.FLIP_LEFT_RIGHT)


TEXTURES = {}


def texture(path):
    def deco(fn):
        TEXTURES[path] = fn
        return fn
    return deco


# ---------------------------------------------------------------- ores (overlays) ----
# Vanilla ores darken the host rock around each mineral grain; our ores are an overlay on
# vanilla stone/deepslate, so the rim 'h' is drawn in the host's own darkest shade and the
# deepslate overlay is a separate, re-arranged drawing with the deepslate rim colour.
STONE_RIM = '#686868'
DEEPSLATE_RIM = '#2f2f37'

ZINC = ['#4f5963', '#6f7c88', '#95a4b0', '#bac8d2', '#dfe9ef']
ZINC_GRID = [
    '................',
    '...h............',
    '..h45h......h...',
    '.h3453h....h34h.',
    '..h2322h..h345h.',
    '...h11h...h2431h',
    '....hh.....h21h.',
    '..........h.hh..',
    '.....hhh........',
    '....h3454h......',
    '...h24543h...hh.',
    '..h232h21h..h45h',
    '...hh.h1h..h3442',
    '.h.....h...h221h',
    'h4h.........hh..',
    'h2h.............',
]
SULFUR = ['#6e5c16', '#a08f21', '#cbbd33', '#e5da55', '#f6f09a']
SULFUR_GRID = [
    '................',
    '.....h.h........',
    '....h5h4h...hh..',
    '...h44343h.h54h.',
    '...h232321h4332h',
    '....h11111hh211h',
    '.....hhhhh..hh..',
    '................',
    '.hh.........h...',
    'h54h......hh5h..',
    'h343h....h4h4hh.',
    '.h21h...h54343h.',
    '..hh....h322321h',
    '.........h1111h.',
    '..........hhhh..',
    '................',
]
SALT = ['#8c7470', '#b39893', '#d4bfb8', '#ecdcd6', '#fbf3f0']
SALT_GRID = [
    '................',
    '..hhhh..........',
    '.h5554hh........',
    '.h5443554h......',
    '.h4432443h...hh.',
    '.h3322332h..h54h',
    '..hhhh221h..h32h',
    '......hhh....hh.',
    '................',
    '.......hhhh.....',
    '..hh..h5544h....',
    '.h54h.h5443h....',
    '.h32h.h4332h.hh.',
    '..hh..h3222hh54h',
    '.......hhhh.h32h',
    '.............hh.',
]
SHALE = ['#17130f', '#241d17', '#33291f', '#57462f', '#6a6676']
# Thin broken seams, highlight on one or two pixels only: fat lenses with a lit top edge
# read as rows of closed eyes in game.
SHALE_GRID = [
    '................',
    '................',
    '..hhhhh.........',
    '.h12114hhhh.....',
    '..hh21111214h...',
    '....hhhhhhhh....',
    '................',
    '................',
    '........hhhhhh..',
    '.hhhh.hh1215112h',
    'h1411h2111hhhhh.',
    '.hh112112hh.....',
    '...hhhhhh.......',
    '................',
    '...........hhh..',
    '..........h13h..',
]


def ore(name, ramp, rows, rim):
    pal = {'.': None, 'h': rim}
    pal.update({str(i + 1): c for i, c in enumerate(ramp)})
    return grid(name, pal, rows)


for _mineral, _ramp, _rows in (('zinc', ZINC, ZINC_GRID), ('sulfur', SULFUR, SULFUR_GRID),
                               ('rock_salt', SALT, SALT_GRID), ('oil_shale', SHALE, SHALE_GRID)):
    TEXTURES[f'ore/{_mineral}'] = (lambda m=_mineral, r=_ramp, g=_rows: ore(m, r, g, STONE_RIM))
    # Deepslate: mirrored arrangement, deepslate rim, like vanilla's separate deepslate ores.
    TEXTURES[f'ore/{_mineral}_deepslate'] = (
        lambda m=_mineral, r=_ramp, g=_rows: mirror(ore(m + '_deepslate', r, g, DEEPSLATE_RIM)))


# ------------------------------------------------------------------- terrain ---------
@texture('limestone')
def limestone():
    # Pale sedimentary stone: stone's soft horizontal smears, a warmer cream ramp, and a
    # few no single features (in game they repeated as a dotted grid).
    pal = {'2': '#a5a189', '3': '#b5b199', '4': '#c3bfa6', '5': '#d0ccb4',
           '6': '#dcd8c1'}
    return grid('limestone', pal, [
        '4443355544433444',
        '3344444432334445',
        '4455543344444333',
        '2233344444555443',
        '4444443322334444',
        '5544444444444325',
        '3332455544433444',
        '4444433344442333',
        '4434444444554444',
        '4444334455443334',
        '3333444443334444',
        '4444555433222344',
        '4443344444445543',
        '3322344434444444',
        '4444455443334433',
        '5544443344444455',
    ])


@texture('hell_rock')
def hell_rock():
    # Dark red-violet rock: vanilla stone's short horizontal smears (no diagonal runs, so
    # it does not stripe when tiled) no cracks: in game they read as repeated stitches.
    pal = {'1': '#3e2a30', '2': '#4b3339', '3': '#573c42', '4': '#64454b',
           '5': '#714f54', '6': '#82595b'}
    return grid('hell_rock', pal, [
        '4445543334455444',
        '3344554444333455',
        '4433344556544433',
        '5544433344445543',
        '3345544433334444',
        '4444432222344555',
        '5543344443245443',
        '4455554433444333',
        '3333445544455444',
        '4421334444334465',
        '4455443321345544',
        '5544455544433444',
        '3334444555443222',
        '4445533444554243',
        '5544333443344255',
        '4433455444333444',
    ])


@texture('ash_soil')
def ash_soil():
    # Grey-brown ash over soil: dirt's granular clumps, each lighter grain sitting on a
    # darker one, plus a few pale ash flecks and charred bits.
    pal = {'c': '#2f2c2b', '1': '#3b3836', '2': '#474340', '3': '#534e49', '4': '#5e5852',
           '5': '#69625b', '6': '#786f66', '7': '#8a8279'}
    return grid('ash_soil', pal, [
        '4534443654435443',
        '5443254443543324',
        '3446544352446534',
        '4534437444364425',
        '6443523635443453',
        '4352445443547434',
        '4544634256443623',
        '3443c43544352454',
        '5356445743443536',
        '4443562443654443',
        '4645344354435374',
        '3442547426344453',
        '4635443534543c44',
        '5443436445276435',
        '4374454634435443',
        '3443543443643534',
    ])


# ----------------------------------------------------------------- masonry ----------
COURSES = [
    'AAAAAAA#BBBBBBB#',
    'AAAAAAA#BBBBBBB#',
    'AAAAAAA#BBBBBBB#',
    '################',
    'CCC#DDDDDDD#CCCC',
    'CCC#DDDDDDD#CCCC',
    'CCC#DDDDDDD#CCCC',
    '################',
    'FFFFFFF#GGGGGGG#',
    'FFFFFFF#GGGGGGG#',
    'FFFFFFF#GGGGGGG#',
    '################',
    'HHH#IIIIIII#HHHH',
    'HHH#IIIIIII#HHHH',
    'HHH#IIIIIII#HHHH',
    '################',
]


@texture('sewer_brickwork')
def sewer_brickwork():
    # Grey-green sewer brick, four courses like vanilla bricks; dark grimy mortar; one damp
    # streak seeping from a joint down across the course below.
    ramps = {'g': ['#3c443f', '#47514b', '#535e57', '#5f6b63', '#6b786f', '#78867c']}
    tones = {k: ('g', t) for k, t in zip('ABCDFGHI', (3, 3, 2, 3, 3, 2, 3, 3))}
    pal = {'s': '#4a5d4c', 't': '#3f5142', 'm': '#2b302d'}
    return masonry('sewer_brickwork', COURSES, ramps, tones, '#343a36', '#2b302d', [
        '................',
        '.....-.......+..',
        '..+.............',
        '.......m.......m',
        '.......-........',
        '..........+.....',
        '-...............',
        '...m.......m....',
        '...........s....',
        '....+.....ts....',
        '..........ts..-.',
        '..........t.....',
        '..........s.....',
        '.....-....s.....',
        '..+.............',
        '...m.......m....',
    ], pal)


@texture('soot_blackened_brick')
def soot_blackened_brick():
    # Fire-house brick: soot is thickest on the top courses and thins toward the bottom,
    # where the original red-brown of the brick shows through. The red is kept close to the
    # soot: in game, strongly red bricks repeated as a regular pattern across a wall.
    ramps = {'k': ['#2b2928', '#353231', '#3f3b39', '#4a4542', '#55504c', '#615a55'],
             'r': ['#3a2f2b', '#463833', '#52413a', '#5e4a42', '#6a534a', '#765c52']}
    tones = {'A': ('k', 2), 'B': ('k', 2), 'C': ('k', 2), 'D': ('k', 3), 'F': ('k', 3),
             'G': ('r', 2), 'H': ('r', 3), 'I': ('k', 3)}
    pal = {'o': '#2a2625'}
    return masonry('soot_blackened_brick', COURSES, ramps, tones, '#5a5450', '#4c4743', [
        '......-.........',
        '..........-.....',
        '................',
        '.......o.......o',
        '................',
        '.....-..........',
        '................',
        '...o.......o....',
        '..+.............',
        '.........+......',
        '................',
        '.......o.......o',
        '......+.........',
        '+...........-...',
        '................',
        '...o.......o....',
    ], pal)


@texture('seepage_brick')
def seepage_brick():
    # Wet teal brick: darker saturated water tracks under the joints and pale mineral
    # crust where the water dried on the mortar.
    ramps = {'t': ['#24403c', '#2d4d48', '#375a54', '#426761', '#4e756e', '#5c847c']}
    tones = {k: ('t', t) for k, t in zip('ABCDFGHI', (3, 2, 3, 3, 2, 3, 3, 2))}
    pal = {'w': '#1f3f3a', 'v': '#28524b', 'c': '#5f7d70', 'C': '#728f82'}
    return masonry('seepage_brick', COURSES, ramps, tones, '#2a3734', '#202b29', [
        '................',
        '....v...........',
        '....v.......-...',
        '...cwc..........',
        '....w..........C',
        '....v..+.......v',
        '...............w',
        '..........cCc..w',
        '...........w...v',
        '...........v....',
        '.+.........v....',
        '...........c....',
        '...........w....',
        '...........v.+..',
        '................',
        '................',
    ], pal)


@texture('patchwork_masonry')
def patchwork_masonry():
    # A red brick wall repaired brick by brick with grey stone, limestone and two short
    # concrete plugs. One brick bond and one light direction; no single dominant block,
    # because an earlier version with a large ashlar tiled into an obvious checkerboard.
    layout = [
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        '################',
        'CCC#DDDDDDD#EE#C',
        'CCC#DDDDDDD#EE#C',
        'CCC#DDDDDDD#EE#C',
        '################',
        'FFFFFFF#GGG#HHH#',
        'FFFFFFF#GGG#HHH#',
        'FFFFFFF#GGG#HHH#',
        '################',
        'III#JJJJJJJ#IIII',
        'III#JJJJJJJ#IIII',
        'III#JJJJJJJ#IIII',
        '################',
    ]
    ramps = {'s': ['#5d5d5b', '#6b6b68', '#797976', '#878784', '#959592', '#a3a3a0'],
             'b': ['#5a3229', '#6b3c31', '#7c473a', '#8c5343', '#9b5f4e', '#a96c59'],
             'l': ['#7e7a66', '#8f8b76', '#a09c86', '#b0ac95', '#bfbba4', '#ccc8b1'],
             'c': ['#6a6b6a', '#767776', '#818281', '#8b8c8b', '#959695', '#a0a1a0']}
    tones = {'A': ('b', 4), 'B': ('l', 1), 'C': ('b', 3), 'D': ('s', 2), 'E': ('c', 2),
             'F': ('b', 4), 'G': ('s', 2), 'H': ('b', 3), 'I': ('l', 1), 'J': ('b', 4)}
    return masonry('patchwork_masonry', layout, ramps, tones, '#5a5650', '#4a4743', [
        '................',
        '..+.......-.....',
        '................',
        '................',
        '.....-..........',
        '................',
        '................',
        '................',
        '...+............',
        '..........+.....',
        '................',
        '................',
        '.......-........',
        '..............+.',
        '................',
        '................',
    ], {})


@texture('industrial_debris')
def industrial_debris():
    # Rubble like vanilla gravel: separate chunks, lit on top, dark gaps under each chunk.
    # Concrete, rusted sheet, brick, steel and a scrap of plank.
    layout = [
        'AAAAA#BBBBBBB##A',
        'AAAAAA#BBBBBBB#A',
        'AAAAA##BBBBBB##A',
        '#AAA#CC#####EEE#',
        '#####CCCC#EEEEEE',
        'DDDD#CCCCC#EEEEE',
        'DDDDD#CCC##EEEE#',
        'DDDDDD###FF#EE##',
        '#DDDD##FFFFF####',
        '##DD#GGGGGGGG#HH',
        'II###GGGGGGGG#HH',
        'III#JJJ#######HH',
        'IIII#JJJJ#KKKKK#',
        'IIIII#JJ#KKKKKKK',
        '#III##J#KKKKKKK#',
        '#####LLL####KK##',
    ]
    # Ramps kept close in value and low in saturation: the first in-game view showed the
    # rubble far busier than vanilla gravel beside it.
    ramps = {'c': ['#6a6864', '#7a7873', '#8a8883', '#999792'],
             'r': ['#5e4436', '#71503d', '#845d45', '#946a4f'],
             'b': ['#664a42', '#79574d', '#8b6458', '#9a7064'],
             'm': ['#555b5f', '#646b70', '#737b80', '#838b90'],
             'w': ['#4e4134', '#5f4f3f', '#705e4b', '#7e6a55']}
    tones = {'A': ('c', 2), 'B': ('r', 2), 'C': ('b', 2), 'D': ('c', 1), 'E': ('m', 2),
             'F': ('r', 1), 'G': ('w', 2), 'H': ('b', 1), 'I': ('c', 2), 'J': ('r', 2),
             'K': ('m', 1), 'L': ('c', 2)}
    return masonry('industrial_debris', layout, ramps, tones, '#4d4945', '#3d3a37',
                   bevel=(1, 0, -1, 0))


@texture('smoke_stained_concrete')
def smoke_stained_concrete():
    # Cast concrete: near-flat like vanilla concrete, a formwork seam, and soot smudges spread
    # evenly one step darker. Tie holes (a pegboard on a wall) and a soot band along the top
    # (a stripe every block on a tall wall) were both tried in game and dropped.
    pal = {'2': '#626362', '3': '#717271', '4': '#7f807e', '5': '#8a8b89',
           '6': '#959694', '7': '#a09f9c'}
    return grid('smoke_stained_concrete', pal, [
        '5545556555455565',
        '5555455565555455',
        '4555565545565555',
        '5655545565556545',
        '5655545565555655',
        '5565655456556565',
        '6555565655645556',
        '4444444444444444',
        '6667666666676666',
        '5654565556565465',
        '5565545655556555',
        '5655565456555655',
        '5465556565556545',
        '6555654565655556',
        '5565556556545655',
        '5545655565555565',
    ])


@texture('water_damaged_plaster')
def water_damaged_plaster():
    # Cream plaster: trowel texture in the three light shades only, and one faint dried
    # blotch in the darker two. In game, darker trowel marks and a drip read as dashes and a
    # stronger stain as polka dots; a full-width tide line would stripe a tall wall.
    pal = {'3': '#b5ab90', '4': '#c4bca3', '5': '#cfc8b0',
           '6': '#d9d3bd', '7': '#e3dec9'}
    return grid('water_damaged_plaster', pal, [
        '5565556566556556',
        '6555565556565655',
        '5565655556555565',
        '5565655565565655',
        '6555556565556565',
        '5565565655655556',
        '6555555656565655',
        '6556556565556565',
        '5565565556555656',
        '5655556565644565',
        '6556556565443455',
        '5565565556444455',
        '6556556565554555',
        '5655655556555565',
        '5565655656565556',
        '6556556565655565',
    ])


@texture('rusted_plating')
def rusted_plating():
    # Staggered riveted steel plates (like a hull), rust running down from each rivet
    # and blooming along the lower edges.
    pal = {'1': '#3f3631', '2': '#54473f', '3': '#64564c', '4': '#716359', '5': '#7e7064',
           '6': '#8d7f72', 'r': '#7d4a2e', 'R': '#93593a', 'u': '#5f3f30'}
    return grid('rusted_plating', pal, [
        '6656665666566651',
        '5644454444544631',
        '5245444454444231',
        '5r44454444454r31',
        '5u44444544444u31',
        '5r44444rR4544r31',
        '3u3333urru333u21',
        '1111111111111111',
        '6656665166566656',
        '4454463156444454',
        '4445423152445444',
        '44544r315r444454',
        '44454u315u445444',
        '4544463156444544',
        '333rRu213u2rRr33',
        '1111111111111111',
    ])


@texture('rusted_industrial_sheet')
def rusted_industrial_sheet():
    # Corrugated sheet: a 4-pixel ridge period lit on the left face; old grey-green paint
    # flaking off the top half, rust taking over the bottom half.
    # The pixel colour comes from the ridge phase (lit face, crest, falling face, trough)
    # and a hand-drawn mask of what covers the steel there: old paint (P), rust (R) or
    # fresh orange rust (O). Paint holds on the top half and flakes off toward the bottom.
    phase = '5642'
    ramps = {'P': {'5': '#5f6b5f', '6': '#728071', '4': '#4c564d', '2': '#3c453e'},
             'R': {'5': '#7a5640', '6': '#8d6549', '4': '#664836', '2': '#46322a'},
             'O': {'5': '#9a5835', '6': '#b06a3d', '4': '#80492e', '2': '#5a3424'}}
    mask = [
        '----------------',
        'PPPPPPPPPPPPPPPP',
        'PPPPPPPPPPRRPPPP',
        'PPPPPPPPPRRRPPPP',
        'PPRRPPPPPPRPPPPP',
        'PPRRPPPPPPPPPPRP',
        'PPPRPPPPPPPPPRRR',
        'PPPPPRRPPPPRRRRR',
        'RPPRRRRRPPRRRRRR',
        'RRRRRRRRRRRRRRRO',
        'RRRRRORRRRRRRROO',
        'RRRRROORRRRRRRRO',
        'ORRRRROORRRORRRR',
        'OORRRRRRRRROORRR',
        'ORRRRRRRRRRROORR',
        'RRRRRRRRRRRRRRRR',
    ]
    check_grid('rusted_industrial_sheet', mask)
    im = Image.new('RGBA', (16, 16))
    for y, row in enumerate(mask):
        for x, m in enumerate(row):
            c = '#3a2a22' if m == '-' else ramps[m][phase[x % 4]]
            im.putpixel((x, y), hexrgba(c))
    return im


@texture('galvanized_plating')
def galvanized_plating():
    # Four bevelled zinc-coated panels; the spangle crystals are a few angular facets one
    # step lighter or darker, not speckle.
    layout = [
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        'AAAAAAA#BBBBBBB#',
        '################',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        'BBBBBBB#AAAAAAA#',
        '################',
    ]
    ramps = {'z': ['#5d676d', '#6c767c', '#7a848a', '#868f95', '#929ba0', '#a1a9ad',
                   '#b2b9bc']}
    tones = {'A': ('z', 3), 'B': ('z', 3)}
    pal = {'w': '#b7bab3'}
    return masonry('galvanized_plating', layout, ramps, tones, '#4d565c', '#454d52', [
        '................',
        '.++.......-.....',
        '.+++.....--.....',
        '..++....--......',
        '.....-.....++...',
        '....--....+++...',
        '................',
        '................',
        '................',
        '..--.......+....',
        '..---.....++w...',
        '...--......+....',
        '......+.........',
        '.....++....--...',
        '................',
        '................',
    ], pal, bevel=(2, 1, -1, -1))


@texture('chitin_plating')
def chitin_plating():
    # Overlapping insect scutes laid like shingles: each scute glossy at the top-left,
    # rounded at the bottom, casting a dark lip onto the row below.
    pal = {'1': '#22302a', '2': '#2f4036', '3': '#3c5143', '4': '#4a6150', '5': '#5a735e',
           '6': '#6d8870', '7': '#8aa38a'}
    return grid('chitin_plating', pal, [
        '6776655416776655',
        '7665554417665554',
        '6655544416655544',
        '5554444315554443',
        '5544443315544433',
        '4444433214444332',
        '2444332112444331',
        '1222221111222221',
        '5541677665541677',
        '4417665554417665',
        '4416655544416655',
        '4315554444315554',
        '3315544443315544',
        '3214444433214444',
        '2112444332112444',
        '1111222221111222',
    ])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', default=ROOT)
    ap.add_argument('only', nargs='*')
    a = ap.parse_args()
    for path, fn in TEXTURES.items():
        if a.only and path not in a.only:
            continue
        im = fn()
        dest = os.path.join(a.out, path + '.png')
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        im.save(dest)
        print('wrote', os.path.relpath(dest))


if __name__ == '__main__':
    sys.exit(main())
