// Art for the Hole's environmental hazards. The owner's rule: anything new starts from the vanilla texture it resembles
// and should look like Mojang added it. So Ash-Snow is vanilla snow (and the ash snowball the vanilla snowball) tinted
// ash-gray, and Sludge-Ice is vanilla ice tinted sludge-brown. Their models reference the vanilla textures and
// client/HazardColors tints them; no vanilla image is copied and none is drawn here. The only original art left is:
//   mob_effect/hole_toxin    the Hole Toxin effect icon (vanilla has no texture to start from)
//
//   node tools/ui/gen-hazards.mjs                       install into src/main/resources/assets/dorohedoro/textures/
//   node tools/ui/gen-hazards.mjs <previewDir>          candidate only, into previewDir/textures
// Deterministic: running it again gives identical files.
import { Img } from './png.mjs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const preview = process.argv[2];
const TEX = preview ? path.join(preview, 'textures') : path.join(ROOT, 'src/main/resources/assets/dorohedoro/textures');

// ---- Hole Toxin effect icon, 18x18: a drop of toxic sludge, outlined like vanilla effect icons.
function toxinIcon() {
  const img = new Img(18, 18);
  img.art(0, 0, [
    '........OO........',
    '.......OGGO.......',
    '.......OGGO.......',
    '......OGGGGO......',
    '......OGWGGO......',
    '.....OGGWGGGO.....',
    '....OGGGGGGGGO....',
    '....OGGGGGGGDO....',
    '...OGGGGGGGGDDO...',
    '...OGGGBBGGGDDO...',
    '...OGGBDDBGGDDO...',
    '...OGGBDDBGDDDO...',
    '...OGGGBBGGDDDO...',
    '....OGGGGGDDDO....',
    '....OOGGDDDDOO....',
    '......OOOOOO......',
    '..................',
    '..................',
  ], { O: '#1d2408', G: '#8fa33a', W: '#dbe8a2', D: '#5b6a1f', B: '#c8d77a' });
  return img;
}

const out = {
  'mob_effect/hole_toxin': toxinIcon(),
};
for (const [name, img] of Object.entries(out)) img.save(path.join(TEX, name + '.png'));
console.log('wrote', Object.keys(out).join(', '), 'to', TEX);
