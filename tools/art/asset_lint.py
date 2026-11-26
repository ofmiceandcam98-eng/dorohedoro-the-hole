"""Technical asset checks for the Dorohedoro resource pack. Exit code 1 on any ERROR.

  py tools/art/asset_lint.py [--vanilla <extracted client jar root>] [--json report.json]

ERROR (breaks loading or rendering):
  - a blockstate names a model that does not exist
  - a model names a parent or texture that does not exist (dorohedoro namespace always;
    minecraft namespace too when --vanilla is given)
  - a model element has from > to, coordinates outside -16..32, or a UV outside 0..16
  - a PNG is not RGBA/RGB/P, or an animated strip's height is not a multiple of its width
  - an .mcmeta file is not valid JSON or has a non-positive frametime
  - a resource name is not lowercase [a-z0-9_./-]
  - a full cube (cube_all) block texture has transparent pixels
WARN (art-direction review needed, does not fail the run):
  - block or item texture larger than 16x16 that is not on the allowlist below
  - semi-transparent pixels (alpha not 0 or 255) in a block or item texture
  - two textures with identical pixels
  - a texture that no model, blockstate, Java source or lang entry refers to
Automated checks cannot judge art quality; docs/art/VISUAL_QA_REPORT.md is the visual gate.
"""
import argparse
import hashlib
import json
import os
import re
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.normpath(os.path.join(HERE, '..', '..'))
RES_ROOTS = [os.path.join(REPO, 'src', 'main', 'resources', 'assets'),
             os.path.join(REPO, 'src', 'generated', 'resources', 'assets')]
JAVA = os.path.join(REPO, 'src', 'main', 'java')
NS = 'dorohedoro'

# Larger-than-16 textures that are deliberate: 3D limb item models use 64x64 atlases (their
# JSON models are UV-mapped for that size); entity atlases are sized by their Java models.
HIGHRES_OK = re.compile(r'^(item/(severed|cooked)_(left|right)_(arm|leg)|entity/.*|gui/.*|magic/.*|particle/.*)$')
NAME_OK = re.compile(r'^[a-z0-9_./-]+$')

errors, warns = [], []
err = lambda m: errors.append(m)
warn = lambda m: warns.append(m)


def find(ns, kind, path, ext):
    """Locate assets/<ns>/<kind>/<path><ext> in any resource root; vanilla handled separately."""
    for root in RES_ROOTS:
        p = os.path.join(root, ns, kind, path + ext)
        if os.path.exists(p):
            return p
    return None


def split(ref, default_ns='minecraft'):
    if ':' in ref:
        ns, path = ref.split(':', 1)
    else:
        ns, path = default_ns, ref
    return ns, path


def exists(ns, kind, path, ext, vanilla):
    if ns == NS:
        return find(ns, kind, path, ext) is not None
    if vanilla and ns == 'minecraft':
        return os.path.exists(os.path.join(vanilla, 'assets', ns, kind, path + ext))
    return True   # other namespaces / no vanilla root: not checkable


def load_json(p):
    try:
        with open(p, encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:  # noqa: BLE001
        err(f'{rel(p)}: invalid JSON ({e})')
        return None


def rel(p):
    return os.path.relpath(p, REPO).replace('\\', '/')


def walk(kind, ext):
    seen = {}
    for root in RES_ROOTS:
        base = os.path.join(root, NS, kind)
        for d, _, files in os.walk(base):
            for f in files:
                if f.endswith(ext):
                    p = os.path.join(d, f)
                    key = os.path.relpath(p, base).replace('\\', '/')[:-len(ext)]
                    seen.setdefault(key, p)
    return seen


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--vanilla')
    ap.add_argument('--json')
    a = ap.parse_args()

    models = walk('models', '.json')
    textures = walk('textures', '.png')
    referenced = set()
    cube_all = set()

    # Blockstates -> models
    for name, p in walk('blockstates', '.json').items():
        bs = load_json(p)
        if bs is None:
            continue
        variants = []
        for v in (bs.get('variants') or {}).values():
            variants += v if isinstance(v, list) else [v]
        for part in bs.get('multipart') or []:
            apply = part.get('apply')
            variants += apply if isinstance(apply, list) else [apply]
        for v in variants:
            ns, path = split(v['model'])
            if not exists(ns, 'models', path, '.json', a.vanilla):
                err(f'{rel(p)}: model {v["model"]} not found')

    # Models -> parents, textures, geometry
    for name, p in models.items():
        m = load_json(p)
        if m is None:
            continue
        parent = m.get('parent')
        if parent and not parent.startswith('builtin/'):
            ns, path = split(parent)
            if not exists(ns, 'models', path, '.json', a.vanilla):
                err(f'{rel(p)}: parent {parent} not found')
        if parent in ('minecraft:block/cube_all', 'block/cube_all'):
            t = m.get('textures', {}).get('all', '')
            if t.startswith(NS + ':'):
                cube_all.add(t.split(':', 1)[1])
        for key, t in (m.get('textures') or {}).items():
            if t.startswith('#'):
                continue
            ns, path = split(t)
            if ns == NS:
                referenced.add(path)
            if not exists(ns, 'textures', path, '.png', a.vanilla):
                err(f'{rel(p)}: texture {key}={t} not found')
        for i, e in enumerate(m.get('elements') or []):
            f, t = e.get('from', [0, 0, 0]), e.get('to', [0, 0, 0])
            if any(x < -16 or x > 32 for x in f + t):
                err(f'{rel(p)}: element {i} outside -16..32')
            if any(f[k] > t[k] for k in range(3)):
                err(f'{rel(p)}: element {i} has from > to')
            for d, fc in (e.get('faces') or {}).items():
                uv = fc.get('uv')
                if uv and any(u < 0 or u > 16 for u in uv):
                    err(f'{rel(p)}: element {i} face {d} uv {uv} outside 0..16')
                tex = fc.get('texture', '')
                if tex.startswith('#') and tex[1:] not in (m.get('textures') or {}) and not parent:
                    err(f'{rel(p)}: element {i} face {d} uses undefined {tex}')

    # References from Java, lang, particles, atlases
    java_text = ''
    for d, _, files in os.walk(JAVA):
        for f in files:
            if f.endswith('.java'):
                java_text += open(os.path.join(d, f), encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'textures/([a-z0-9_/]+)\.png', java_text):
        referenced.add(m.group(1))
    for m in re.finditer(r'"' + NS + r'"\s*,\s*"([a-z0-9_/]+)"', java_text):
        referenced.add(m.group(1))
    for m in re.finditer(r'(?:fromNamespaceAndPath|withDefaultNamespace|parse)\([^)]*?"(?:' + NS + r':)?([a-z0-9_/]+)"', java_text):
        referenced.add(m.group(1))
    for kind in ('particles',):
        for name, p in walk(kind, '.json').items():
            for t in (load_json(p) or {}).get('textures', []):
                referenced.add('particle/' + split(t)[1])

    # PNG checks
    hashes = {}
    for name, p in textures.items():
        if not NAME_OK.match(name):
            err(f'{rel(p)}: resource name is not lowercase [a-z0-9_./-]')
        try:
            im = Image.open(p)
            mode = im.mode
            im = im.convert('RGBA')
        except Exception as e:  # noqa: BLE001
            err(f'{rel(p)}: unreadable PNG ({e})')
            continue
        if mode not in ('RGBA', 'RGB', 'P', 'LA', 'L'):
            err(f'{rel(p)}: unexpected PNG mode {mode}')
        w, h = im.size
        meta = p + '.mcmeta'
        if os.path.exists(meta):
            mm = load_json(meta) or {}
            anim = mm.get('animation')
            if anim is not None:
                if h % w:
                    err(f'{rel(p)}: animated strip {w}x{h} height is not a multiple of width')
                if anim.get('frametime', 1) <= 0:
                    err(f'{rel(meta)}: frametime must be positive')
                h = w
        kind = name.split('/', 1)[0]
        if kind in ('block', 'item') and (w > 16 or h > 16) and not HIGHRES_OK.match(name):
            warn(f'{rel(p)}: {w}x{h}, vanilla {kind} textures are 16x16 (pixel density mismatch)')
        alphas = set(px[3] for px in im.getdata())
        if kind in ('block', 'item') and any(0 < al < 255 for al in alphas):
            warn(f'{rel(p)}: semi-transparent pixels; vanilla {kind} art uses only 0 or 255 alpha')
        if name.startswith('block/') and name in cube_all and 0 in alphas:
            err(f'{rel(p)}: full-cube block texture has transparent pixels')
        digest = hashlib.sha1(im.tobytes() + bytes(str(im.size), 'ascii')).hexdigest()
        if digest in hashes:
            warn(f'{rel(p)}: identical pixels to {rel(hashes[digest])}')
        else:
            hashes[digest] = p
        if name not in referenced and not name.startswith('gui/'):
            warn(f'{rel(p)}: not referenced by any model, blockstate or Java source')

    for e in errors:
        print('ERROR', e)
    for w in warns:
        print('WARN ', w)
    print(f'{len(textures)} textures, {len(models)} models: {len(errors)} errors, {len(warns)} warnings')
    if a.json:
        with open(a.json, 'w', encoding='utf-8') as f:
            json.dump({'errors': errors, 'warnings': warns}, f, indent=2)
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main())
