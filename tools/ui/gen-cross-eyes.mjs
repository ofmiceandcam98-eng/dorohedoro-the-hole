// The Cross-Eyes gang's look, worn over Minecraft's own default player skins (the owner's rule: anything new starts from
// vanilla textures and should look like Mojang added it). Each member wears one of eight vanilla default skins (all
// except Steve, by the owner's choice), referenced by the renderer and never copied, under this overlay. As in the
// series, the members are ordinary people; what marks them is the gang's red cross (the 十 of 十字目) tattooed over each
// eye. The overlay adds:
//   the cross tattoos, drawn on the face itself, centered on the pupil (every default skin has its eyes on row 12,
//   x 9-10 and 13-14, pupils at 10 and 13);
//   a red scarf at the collar, the gang's cross painted on the back, and a red armband on the left arm, each on the
//   skin's outer layer (jacket, left sleeve), so they sit over whatever the default skin wears there.
//   node tools/ui/gen-cross-eyes.mjs                 install textures/entity/cross_eyes_overlay.png
//   node tools/ui/gen-cross-eyes.mjs <previewDir>    candidate only, plus an enlarged review copy
// Deterministic: running it again gives identical files.
import { Img } from './png.mjs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const preview = process.argv[2];

const RED = '#b3241f', RED_DARK = '#7e1a16', SCARF = ['#9a2723', '#7e1c1a', '#5f1412'];
const overlay = new Img(64, 64);
const px = (x, y, c) => overlay.px(x, y, c);
const hrow = (x0, x1, y, c) => { for (let x = x0; x <= x1; x++) px(x, y, c); };

// ---------------- Face (head front, base layer at 8,8): a cross through each eye. Two pixels up and two out to the
// temple in red, two down the cheek in a darker red; the pupil stays at the center and the nose bridge stays clear.
for (const [pupil, out] of [[10, -1], [13, 1]]) {
  px(pupil, 10, RED); px(pupil, 11, RED);
  px(pupil, 13, RED_DARK); px(pupil, 14, RED_DARK);
  px(pupil + out, 12, RED); px(pupil + 2 * out, 12, RED);
}

// ---------------- Body, jacket layer (front at 20,36; top at 20,32; back at 32,36).
// The scarf: across the shoulders, then knotted at the collar.
hrow(22, 25, 32, SCARF[1]); hrow(22, 25, 33, SCARF[0]);
hrow(21, 26, 36, SCARF[1]); hrow(22, 25, 37, SCARF[0]);
px(23, 38, SCARF[1]); px(24, 38, SCARF[2]); px(23, 39, SCARF[2]);
// The back: the gang's cross painted large across the shoulders, straight onto whatever the member wears.
for (let y = 37; y <= 43; y++) { px(35, y, RED); px(36, y, y % 3 === 0 ? RED_DARK : RED); }
for (let x = 33; x <= 38; x++) { px(x, 39, RED); px(x, 40, x % 3 === 0 ? RED_DARK : RED); }

// ---------------- Left arm, sleeve layer (48,48): a red armband around the upper arm.
hrow(48, 63, 54, RED); hrow(48, 63, 55, RED_DARK);

const overlayDest = preview ? path.join(preview, 'textures/entity/cross_eyes_overlay.png')
  : path.join(ROOT, 'src/main/resources/assets/dorohedoro/textures/entity/cross_eyes_overlay.png');
overlay.save(overlayDest);
console.log('wrote', overlayDest);
if (preview) overlay.scaled(10).save(path.join(preview, 'cross-eyes-overlay-10x.png'));
