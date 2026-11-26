// Minimal NBT reader/writer (Java edition, big-endian), no dependencies.
// Values are kept typed so a file can be read, changed and written back exactly:
//   { type: 'compound', value: { name: {type, value}, ... } } etc.
import { gunzipSync, gzipSync } from 'node:zlib';

const T = { end: 0, byte: 1, short: 2, int: 3, long: 4, float: 5, double: 6, byteArray: 7, string: 8, list: 9,
  compound: 10, intArray: 11, longArray: 12 };
const NAMES = Object.fromEntries(Object.entries(T).map(([k, v]) => [v, k]));

export function readNbt(buffer) {
  const buf = buffer[0] === 0x1f && buffer[1] === 0x8b ? gunzipSync(buffer) : buffer;
  let o = 0;
  const u8 = () => buf.readUInt8(o++);
  const i16 = () => { const v = buf.readInt16BE(o); o += 2; return v; };
  const i32 = () => { const v = buf.readInt32BE(o); o += 4; return v; };
  const i64 = () => { const v = buf.readBigInt64BE(o); o += 8; return v; };
  const str = () => { const n = buf.readUInt16BE(o); o += 2; const s = buf.toString('utf8', o, o + n); o += n; return s; };
  const payload = (type) => {
    switch (type) {
      case T.byte: return buf.readInt8(o++);
      case T.short: return i16();
      case T.int: return i32();
      case T.long: return i64();
      case T.float: { const v = buf.readFloatBE(o); o += 4; return v; }
      case T.double: { const v = buf.readDoubleBE(o); o += 8; return v; }
      case T.byteArray: { const n = i32(); const v = Int8Array.from(buf.subarray(o, o + n)); o += n; return v; }
      case T.string: return str();
      case T.list: { const et = u8(); const n = i32(); const items = []; for (let i = 0; i < n; i++) items.push(payload(et)); return { elementType: NAMES[et], items }; }
      case T.compound: { const out = {}; for (;;) { const t = u8(); if (t === T.end) break; const name = str(); out[name] = { type: NAMES[t], value: payload(t) }; } return out; }
      case T.intArray: { const n = i32(); const v = new Int32Array(n); for (let i = 0; i < n; i++) v[i] = i32(); return v; }
      case T.longArray: { const n = i32(); const v = new BigInt64Array(n); for (let i = 0; i < n; i++) v[i] = i64(); return v; }
      default: throw new Error('bad tag ' + type + ' at ' + o);
    }
  };
  const rootType = u8();
  if (rootType !== T.compound) throw new Error('root is not a compound');
  const rootName = str();
  return { name: rootName, value: payload(T.compound) };
}

export function writeNbt(root, gzip = true) {
  const parts = [];
  const push = (b) => parts.push(b);
  const u8 = (v) => { const b = Buffer.alloc(1); b.writeUInt8(v); push(b); };
  const i16 = (v) => { const b = Buffer.alloc(2); b.writeInt16BE(v); push(b); };
  const i32 = (v) => { const b = Buffer.alloc(4); b.writeInt32BE(v); push(b); };
  const i64 = (v) => { const b = Buffer.alloc(8); b.writeBigInt64BE(BigInt(v)); push(b); };
  const str = (s) => { const d = Buffer.from(s, 'utf8'); const b = Buffer.alloc(2); b.writeUInt16BE(d.length); push(b); push(d); };
  const payload = (type, v) => {
    switch (type) {
      case 'byte': { const b = Buffer.alloc(1); b.writeInt8(v); push(b); return; }
      case 'short': return i16(v);
      case 'int': return i32(v);
      case 'long': return i64(v);
      case 'float': { const b = Buffer.alloc(4); b.writeFloatBE(v); push(b); return; }
      case 'double': { const b = Buffer.alloc(8); b.writeDoubleBE(v); push(b); return; }
      case 'byteArray': i32(v.length); push(Buffer.from(Uint8Array.from(v, (x) => x & 255))); return;
      case 'string': return str(v);
      case 'list': u8(v.items.length ? T[v.elementType] : T.end); i32(v.items.length); for (const it of v.items) payload(v.elementType, it); return;
      case 'compound': for (const [name, tag] of Object.entries(v)) { u8(T[tag.type]); str(name); payload(tag.type, tag.value); } u8(T.end); return;
      case 'intArray': i32(v.length); for (const x of v) i32(x); return;
      case 'longArray': i32(v.length); for (const x of v) i64(x); return;
      default: throw new Error('bad type ' + type);
    }
  };
  u8(T.compound); str(root.name); payload('compound', root.value);
  const raw = Buffer.concat(parts);
  return gzip ? gzipSync(raw) : raw;
}

// Small builders for writing new tags.
export const tag = {
  int: (v) => ({ type: 'int', value: v }),
  str: (v) => ({ type: 'string', value: v }),
  compound: (v) => ({ type: 'compound', value: v }),
  list: (elementType, items) => ({ type: 'list', value: { elementType, items } }),
};

/** Plain JS view of a compound's values (for reading). */
export function plain(v) {
  if (v && typeof v === 'object' && 'type' in v && 'value' in v) return plain(v.value);
  if (v && typeof v === 'object' && 'items' in v && 'elementType' in v) return v.items.map(plain);
  if (v && typeof v === 'object' && !ArrayBuffer.isView(v)) return Object.fromEntries(Object.entries(v).map(([k, x]) => [k, plain(x)]));
  return v;
}
