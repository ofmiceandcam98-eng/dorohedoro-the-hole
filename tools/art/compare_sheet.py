"""Before/after/vanilla review sheet for block textures.

For each texture: BEFORE (git HEAD or --before dir), AFTER (working tree), the vanilla
references from reference_matrix.json, and a 3x3 tiling of AFTER beside a 3x3 tiling of the
first reference. Ore overlays are composited onto their vanilla host. Nearest-neighbour only.

  py tools/art/compare_sheet.py --vanilla <extracted client jar root> --out sheet.png \
      [--before DIR] [block/limestone block/ore/zinc ...]

The vanilla root is the folder that contains assets/minecraft (extract it from
~/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar; never commit it).
"""
import argparse
import io
import json
import os
import subprocess

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, '..', '..'))
MOD = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'dorohedoro', 'textures')
MOD_REL = 'src/main/resources/assets/dorohedoro/textures'


def load(path):
    im = Image.open(path).convert('RGBA')
    if im.height > im.width:          # animated strip: first frame
        im = im.crop((0, 0, im.width, im.width))
    return im


def load_head(rel):
    try:
        data = subprocess.run(['git', 'show', f'HEAD:{MOD_REL}/{rel}.png'], cwd=REPO,
                              capture_output=True, check=True).stdout
        return load(io.BytesIO(data))
    except subprocess.CalledProcessError:
        return None


def on_host(im, host):
    if host is None:
        return im
    base = host.copy().resize(im.size, Image.NEAREST)
    base.alpha_composite(im)
    return base


def tile(im, n=3):
    t = Image.new('RGBA', (im.width * n, im.height * n))
    for x in range(n):
        for y in range(n):
            t.paste(im, (x * im.width, y * im.height))
    return t


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--vanilla', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--before')
    ap.add_argument('--scale', type=int, default=6)
    ap.add_argument('names', nargs='*')
    a = ap.parse_args()
    matrix = json.load(open(os.path.join(HERE, 'reference_matrix.json')))
    names = a.names or [k for k in matrix if not k.startswith('_')]
    van = lambda rel: load(os.path.join(a.vanilla, 'assets', 'minecraft', 'textures', rel + '.png'))

    s = a.scale
    cell = 16 * s
    big = 16 * 3 * (s // 2)
    rowh = max(cell, big) + 22
    cols = 5
    w = cols * (cell + 8) + 2 * (big + 8) + 8
    sheet = Image.new('RGBA', (w, rowh * len(names) + 8), (38, 38, 42, 255))
    d = ImageDraw.Draw(sheet)
    for i, name in enumerate(names):
        entry = matrix.get(name, {'refs': []})
        host = van(entry['host']) if entry.get('host') else None
        after = load(os.path.join(MOD, name + '.png'))
        if a.before:
            p = os.path.join(a.before, name + '.png')
            before = load(p) if os.path.exists(p) else None
        else:
            before = load_head(name)
        if before is None and name.startswith('block/ore/') and name.endswith('_deepslate'):
            before = load_head(name[:-len('_deepslate')])   # deepslate shared the stone overlay
        y = 4 + i * rowh
        panels = [('before', before), ('after', after)] + [(r.split('/')[-1], van(r)) for r in entry['refs'][:3]]
        for j, (label, im) in enumerate(panels):
            x = 8 + j * (cell + 8)
            d.text((x, y), label if j else f'{name.split("/")[-1]}  before', fill=(235, 235, 235, 255))
            if im is None:
                continue
            if j < 2:
                im = on_host(im, host)
            if im.size != (16, 16):
                d.text((x, y + 10), f'{im.width}x{im.height}', fill=(255, 150, 120, 255))
            sheet.alpha_composite(im.resize((cell, cell), Image.NEAREST), (x, y + 20))
        x0 = 8 + cols * (cell + 8)
        t_after = tile(on_host(after, host)).resize((big, big), Image.NEAREST)
        sheet.alpha_composite(t_after, (x0, y + 20))
        d.text((x0, y), 'after 3x3', fill=(235, 235, 235, 255))
        if entry['refs']:
            t_ref = tile(van(entry['refs'][0])).resize((big, big), Image.NEAREST)
            sheet.alpha_composite(t_ref, (x0 + big + 8, y + 20))
            d.text((x0 + big + 8, y), entry['refs'][0].split('/')[-1] + ' 3x3', fill=(235, 235, 235, 255))
    sheet.save(a.out)
    print('wrote', a.out)


if __name__ == '__main__':
    main()
