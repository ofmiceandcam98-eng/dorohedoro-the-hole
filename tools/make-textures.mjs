// Generates the mod's block and item textures as 16x PNGs, using only Node built-ins.
// All art here is procedural and original. Re-run with: node tools/make-textures.mjs
import { deflateSync } from 'node:zlib';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = crcTable[(c ^ b) & 255] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type), data]);
  const sum = Buffer.alloc(4);
  sum.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, sum]);
}
function png(size, pixel) {
  const rows = [];
  for (let y = 0; y < size; y++) {
    const row = Buffer.alloc(1 + size * 4);
    for (let x = 0; x < size; x++) row.set(pixel(x, y), 1 + x * 4);
    rows.push(row);
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(size, 0);
  header.writeUInt32BE(size, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(Buffer.concat(rows))),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
const noise = (x, y, s) => {
  let h = Math.imul((x * 73856093) ^ (y * 19349663) ^ (s * 83492791), 0x5bd1e995);
  h ^= h >>> 13;
  h = Math.imul(h, 0x5bd1e995);
  return ((h ^ (h >>> 15)) >>> 0) / 4294967295;
};
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const save = (path, data) => {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, data);
};
const root = 'src/main/resources/assets/dorohedoro/textures';

// Riveted sheet steel, streaked with rust running down from the seams.
save(`${root}/block/rusted_plating.png`, png(16, (x, y) => {
  const steel = [92, 74, 62];
  const rust = [146, 76, 38];
  const dark = [48, 38, 34];
  const streak = noise(x, 0, 5) * 0.7 + noise(x, y >> 2, 6) * 0.3;
  let c = mix(steel, rust, Math.min(1, streak * 1.1 + y / 40));
  c = mix(c, [c[0] + 14, c[1] + 10, c[2] + 8], noise(x, y, 7) * 0.5);
  if (y === 0 || y === 15 || x === 0 || x === 15) c = dark;
  if ((x === 2 || x === 13) && (y === 2 || y === 13)) c = [176, 150, 120];
  return [...c, 255];
}));

// A compacted heap of pipe ends, gears and sheet offcuts.
save(`${root}/block/industrial_debris.png`, png(16, (x, y) => {
  const cell = noise(x >> 2, y >> 2, 11);
  const palette = [[70, 62, 56], [112, 72, 44], [58, 66, 62], [134, 96, 58], [40, 36, 34]];
  let c = palette[Math.floor(cell * palette.length) % palette.length];
  c = mix(c, [24, 20, 18], noise(x, y, 12) * 0.45);
  if ((x & 3) === 0 && noise(x, y, 13) > 0.55) c = [22, 18, 16];
  return [...c, 255];
}));

// A twisted shard of salvaged metal.
save(`${root}/item/scrap_metal.png`, png(16, (x, y) => {
  const inShard = Math.abs(x - y) < 5 && x + y > 7 && x + y < 24 && noise(x, y, 21) > 0.12;
  if (!inShard) return [0, 0, 0, 0];
  const edge = Math.abs(x - y) === 4;
  const base = mix([120, 112, 104], [150, 84, 44], noise(x, y, 22));
  return [...(edge ? mix(base, [40, 34, 30], 0.6) : base), 255];
}));
console.log('textures written');
