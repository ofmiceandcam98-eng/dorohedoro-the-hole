// Minimal RGBA canvas and PNG writer for the generated UI art. No dependencies.
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname } from 'node:path';

const CRC = new Uint32Array(256).map((_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

function crc32(buf) {
  let c = 0xffffffff;
  for (const b of buf) c = CRC[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
}

/** Parses '#rrggbb' or '#rrggbbaa' into [r, g, b, a]. */
export function rgba(hex) {
  const h = hex.replace('#', '');
  return [0, 2, 4, 6].map(i => (i < h.length ? parseInt(h.slice(i, i + 2), 16) : 255));
}

export class Img {
  constructor(w, h) {
    this.w = w;
    this.h = h;
    this.d = new Uint8Array(w * h * 4);
  }

  px(x, y, c) {
    if (x < 0 || y < 0 || x >= this.w || y >= this.h || c == null) return;
    const [r, g, b, a] = typeof c === 'string' ? rgba(c) : c;
    const i = (y * this.w + x) * 4;
    if (a === 255 || this.d[i + 3] === 0) {
      this.d.set([r, g, b, a], i);
      return;
    }
    // Source-over blend for translucent strokes.
    const sa = a / 255, da = this.d[i + 3] / 255, oa = sa + da * (1 - sa);
    for (let k = 0; k < 3; k++) {
      this.d[i + k] = Math.round(([r, g, b][k] * sa + this.d[i + k] * da * (1 - sa)) / oa);
    }
    this.d[i + 3] = Math.round(oa * 255);
  }

  get(x, y) {
    const i = (y * this.w + x) * 4;
    return [...this.d.slice(i, i + 4)];
  }

  rect(x, y, w, h, c) {
    for (let j = y; j < y + h; j++) for (let i = x; i < x + w; i++) this.px(i, j, c);
  }

  hline(x0, x1, y, c) { for (let x = x0; x <= x1; x++) this.px(x, y, c); }
  vline(x, y0, y1, c) { for (let y = y0; y <= y1; y++) this.px(x, y, c); }

  /** One-pixel outline of a rectangle. */
  box(x, y, w, h, c) {
    this.hline(x, x + w - 1, y, c);
    this.hline(x, x + w - 1, y + h - 1, c);
    this.vline(x, y, y + h - 1, c);
    this.vline(x + w - 1, y, y + h - 1, c);
  }

  /** Draws ASCII pixel art: each character maps to a palette colour, '.' is transparent. */
  art(x, y, rows, palette) {
    rows.forEach((row, j) => [...row].forEach((ch, i) => {
      if (ch !== '.' && ch !== ' ') this.px(x + i, y + j, palette[ch]);
    }));
  }

  png() {
    const raw = Buffer.alloc((this.w * 4 + 1) * this.h);
    for (let y = 0; y < this.h; y++) {
      raw[y * (this.w * 4 + 1)] = 0;
      Buffer.from(this.d.buffer, y * this.w * 4, this.w * 4).copy(raw, y * (this.w * 4 + 1) + 1);
    }
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(this.w, 0);
    ihdr.writeUInt32BE(this.h, 4);
    ihdr.set([8, 6, 0, 0, 0], 8);
    return Buffer.concat([
      Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
      chunk('IHDR', ihdr), chunk('IDAT', deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0)),
    ]);
  }

  save(path) {
    mkdirSync(dirname(path), { recursive: true });
    writeFileSync(path, this.png());
  }

  /** Nearest-neighbour enlargement, for reviewing the art. */
  scaled(f) {
    const out = new Img(this.w * f, this.h * f);
    for (let y = 0; y < out.h; y++) for (let x = 0; x < out.w; x++) {
      const i = (Math.floor(y / f) * this.w + Math.floor(x / f)) * 4;
      out.d.set(this.d.slice(i, i + 4), (y * out.w + x) * 4);
    }
    return out;
  }
}

/** Deterministic 0..1 noise from integer coordinates, so regenerating gives identical art. */
export function noise(x, y, seed = 0) {
  let h = (x * 374761393 + y * 668265263 + seed * 2147483647) | 0;
  h = Math.imul(h ^ (h >>> 13), 1274126177);
  return ((h ^ (h >>> 16)) >>> 0) / 4294967295;
}

export function mix(a, b, t) {
  const A = Array.isArray(a) ? a : rgba(a), B = Array.isArray(b) ? b : rgba(b);
  return A.map((v, i) => Math.round(v + (B[i] - v) * t));
}
