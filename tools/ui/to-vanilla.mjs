// Converts 32x32 textures to vanilla's 16x16 scale.
//   node tools/ui/to-vanilla.mjs <srcDir> <outDir> [--item]
// Each 2x2 block keeps its dominant colour rather than an average, so the result stays crisp pixel art instead of a
// blurred thumbnail. Thin features survive (a block that is half opaque stays opaque), and for items the pixels on the
// silhouette edge keep the darkest colour in their block, the way vanilla item outlines read.
import { readdirSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { Img } from './png.mjs';
import { readPng } from './pngread.mjs';

const [src, out, flag] = process.argv.slice(2);
const item = flag === '--item';
mkdirSync(out, { recursive: true });

const lum = c => 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2];
const dist = (a, b) => Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);

let converted = 0;
for (const f of readdirSync(src).filter(n => n.endsWith('.png'))) {
  const img = readPng(join(src, f));
  if (img.w !== 32 || img.h % 32 !== 0) continue; // 32x32 sprites, and 32-wide animation strips
  const res = new Img(16, img.h / 2);
  const opaque = new Array(16 * res.h).fill(false);
  for (let y = 0; y < res.h; y++) for (let x = 0; x < 16; x++) {
    const block = [img.get(2 * x, 2 * y), img.get(2 * x + 1, 2 * y), img.get(2 * x, 2 * y + 1), img.get(2 * x + 1, 2 * y + 1)];
    const solid = block.filter(c => c[3] >= 128);
    if (solid.length < 2) continue;
    // Dominant colour: the one closest to the others (a medoid), which keeps a real palette colour.
    let best = solid[0], bestScore = Infinity;
    for (const c of solid) {
      const score = solid.reduce((s, o) => s + dist(c, o), 0);
      if (score < bestScore) { bestScore = score; best = c; }
    }
    res.px(x, y, [best[0], best[1], best[2], 255]);
    opaque[y * 16 + x] = true;
  }
  if (item) {
    // Outline: an opaque pixel touching transparency takes the darkest colour of its source block.
    for (let y = 0; y < res.h; y++) for (let x = 0; x < 16; x++) {
      if (!opaque[y * 16 + x]) continue;
      const edge = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => {
        const nx = x + dx, ny = y + dy;
        return nx < 0 || ny < 0 || nx >= 16 || ny >= res.h || !opaque[ny * 16 + nx];
      });
      if (!edge) continue;
      const block = [img.get(2 * x, 2 * y), img.get(2 * x + 1, 2 * y), img.get(2 * x, 2 * y + 1), img.get(2 * x + 1, 2 * y + 1)]
        .filter(c => c[3] >= 128);
      const darkest = block.reduce((a, b) => (lum(b) < lum(a) ? b : a));
      res.px(x, y, [darkest[0], darkest[1], darkest[2], 255]);
    }
  }
  res.save(join(out, f));
  converted++;
}
console.log('converted', converted, 'textures from', src, 'to', out);
