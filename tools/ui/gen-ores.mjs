// Mineral overlays for the Hole's ores. Each ore model draws vanilla stone or deepslate and lays one of these
// transparent 16x16 overlays on top, so the rock matches vanilla exactly (resource packs included) and only the
// mineral is ours.   node tools/ui/gen-ores.mjs
import { Img } from './png.mjs';
import { join } from 'node:path';

const OUT = 'src/main/resources/assets/dorohedoro/textures/block/ore';

// Cluster shapes. H highlight, M body, S shade, D dark rim, the way vanilla ore specks are lit from the top left.
const SHAPES = {
  blob: ['.HM.', 'HMMS', 'MMSD', '.SD.'],
  tall: ['HM', 'MS', 'SD'],
  wide: ['HMM', 'MSD'],
  chip: ['HMS', '.SD'],
  crystal: ['HHM', 'HMS', 'MSD'],
  shard: ['.H.', 'HMS', '.SD'],
};

function cluster(img, x, y, shape, pal) {
  img.art(x, y, SHAPES[shape], pal);
}

const ORES = {
  // Yellow sulfur nuggets: brimstone from the vents under the city.
  sulfur: {
    pal: { H: '#fff27a', M: '#e3c832', S: '#b8971c', D: '#6e5a12' },
    at: [['blob', 2, 2], ['tall', 10, 1], ['wide', 11, 6], ['blob', 4, 9], ['chip', 12, 12], ['tall', 1, 12], ['chip', 7, 5]],
  },
  // Cubic pink-white salt crystals.
  rock_salt: {
    pal: { H: '#ffffff', M: '#f1d7d3', S: '#c9a8a4', D: '#8a6f6c' },
    at: [['crystal', 1, 1], ['crystal', 9, 3], ['shard', 5, 7], ['crystal', 11, 10], ['crystal', 2, 11], ['shard', 7, 13]],
  },
  // Bluish metal flecks.
  zinc: {
    pal: { H: '#eef6fa', M: '#b6c7cf', S: '#8297a2', D: '#4e5b63' },
    at: [['wide', 1, 2], ['blob', 8, 1], ['tall', 4, 6], ['blob', 10, 8], ['chip', 1, 11], ['wide', 6, 13], ['chip', 13, 4]],
  },
};

for (const [name, ore] of Object.entries(ORES)) {
  const img = new Img(16, 16);
  for (const [shape, x, y] of ore.at) cluster(img, x, y, shape, ore.pal);
  img.save(join(OUT, name + '.png'));
}

// Oil shale is a rock, not a speckled ore: dark bituminous seams run through it, with a few oily glints.
{
  const img = new Img(16, 16);
  const seams = [
    ['DSSSD...', 0, 3], ['.DSSSSSD', 8, 4],
    ['SSD.....', 0, 8], ['..DSSSSSSD', 4, 9], ['DS', 14, 8],
    ['.DSSSD', 1, 13], ['DSSD', 10, 13], ['SD', 14, 12],
  ];
  const pal = { H: '#8d7a62', M: '#3b2f27', S: '#261e19', D: '#140f0c' };
  for (const [row, x, y] of seams) img.art(x, y, [row], pal);
  for (const [x, y] of [[2, 3], [11, 4], [6, 9], [3, 13], [11, 13]]) img.px(x, y, pal.H); // glints along the seams
  img.save(join(OUT, 'oil_shale.png'));
}
console.log('ore overlays written to', OUT);
