// Minimal PNG reader for 8-bit, non-interlaced RGBA, RGB, grey and palette images. Returns an Img.
import { inflateSync } from 'node:zlib';
import { readFileSync } from 'node:fs';
import { Img } from './png.mjs';

export function readPng(path) {
  const buf = readFileSync(path);
  let pos = 8, w = 0, h = 0, depth = 0, type = 0, palette = null, trns = null;
  const idat = [];
  while (pos < buf.length) {
    const len = buf.readUInt32BE(pos);
    const kind = buf.toString('ascii', pos + 4, pos + 8);
    const data = buf.subarray(pos + 8, pos + 8 + len);
    if (kind === 'IHDR') {
      w = data.readUInt32BE(0); h = data.readUInt32BE(4); depth = data[8]; type = data[9];
      if (data[12] !== 0) throw new Error(path + ': interlaced PNGs are not supported');
    } else if (kind === 'PLTE') palette = data;
    else if (kind === 'tRNS') trns = data;
    else if (kind === 'IDAT') idat.push(data);
    pos += 12 + len;
  }
  const packed = depth < 8;
  if (depth !== 8 && !(packed && (type === 3 || type === 0))) throw new Error(path + ': unsupported bit depth ' + depth);
  const channels = { 0: 1, 2: 3, 3: 1, 4: 2, 6: 4 }[type];
  const raw = inflateSync(Buffer.concat(idat));
  // Palette and grey images below 8 bits pack several pixels per byte; filters still work bytewise.
  const stride = packed ? Math.ceil(w * depth / 8) : w * channels;
  const bpp = packed ? 1 : channels;
  const out = new Uint8Array(stride * h);
  for (let y = 0; y < h; y++) {
    const f = raw[y * (stride + 1)];
    for (let x = 0; x < stride; x++) {
      const v = raw[y * (stride + 1) + 1 + x];
      const a = x >= bpp ? out[y * stride + x - bpp] : 0;
      const b = y > 0 ? out[(y - 1) * stride + x] : 0;
      const c = x >= bpp && y > 0 ? out[(y - 1) * stride + x - bpp] : 0;
      let p = 0;
      if (f === 1) p = a;
      else if (f === 2) p = b;
      else if (f === 3) p = (a + b) >> 1;
      else if (f === 4) {
        const q = a + b - c, pa = Math.abs(q - a), pb = Math.abs(q - b), pc = Math.abs(q - c);
        p = pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
      }
      out[y * stride + x] = (v + p) & 255;
    }
  }
  const img = new Img(w, h);
  const sample = i => {
    if (!packed) return out[i];
    const y = Math.floor(i / w), x = i % w, bit = x * depth;
    const byte = out[y * stride + (bit >> 3)];
    const v = (byte >> (8 - depth - (bit & 7))) & ((1 << depth) - 1);
    return type === 0 ? Math.round(v * 255 / ((1 << depth) - 1)) : v;
  };
  for (let i = 0; i < w * h; i++) {
    let r, g, bl, al = 255;
    if (type === 6) [r, g, bl, al] = out.subarray(i * 4, i * 4 + 4);
    else if (type === 2) [r, g, bl] = out.subarray(i * 3, i * 3 + 3);
    else if (type === 0) r = g = bl = sample(i);
    else if (type === 4) { r = g = bl = out[i * 2]; al = out[i * 2 + 1]; }
    else {
      const k = sample(i);
      [r, g, bl] = palette.subarray(k * 3, k * 3 + 3);
      al = trns && k < trns.length ? trns[k] : 255;
    }
    img.d.set([r, g, bl, al], i * 4);
  }
  return img;
}
