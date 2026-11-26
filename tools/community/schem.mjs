// Community schematic tools.
//   node tools/community/schem.mjs info <file.schem>              palette, size, block entities
//   node tools/community/schem.mjs slices <file.schem>            ASCII floor plan of every layer
//   node tools/community/schem.mjs convert <in.schem> <out.nbt> [--map map.json] [--rotate 0|90|180|270]
//
// Reads Sponge schematics (WorldEdit .schem, versions 2 and 3) and writes a vanilla 1.21.1 structure template
// (DataVersion 3955): size, palette, blocks (air omitted, so placing it never erases terrain). --map rewrites block
// names or whole states (e.g. a missing mod block to a vanilla one). Rotation is applied to positions only: block
// states keep their facing and the game turns them when the template is placed with the matching rotation.
import { readFileSync, writeFileSync } from 'node:fs';
import { readNbt, writeNbt, plain, tag } from './nbt.mjs';

const DATA_VERSION_1_21_1 = 3955;

export function loadSchem(path) {
  const root = readNbt(readFileSync(path));
  let s = root.value;
  if (s.Schematic) s = s.Schematic.value;
  const version = s.Version ? s.Version.value : 1;
  const W = s.Width.value, H = s.Height.value, L = s.Length.value;
  let palette, data, blockEntities;
  if (version >= 3) {
    const blocks = s.Blocks.value;
    palette = plain(blocks.Palette);
    data = blocks.Data.value;
    blockEntities = blocks.BlockEntities ? plain(blocks.BlockEntities) : [];
  } else {
    palette = plain(s.Palette);
    data = s.BlockData.value;
    blockEntities = s.BlockEntities ? plain(s.BlockEntities) : [];
  }
  const byId = [];
  for (const [state, id] of Object.entries(palette)) byId[id] = state;
  // Varint-encoded block indices, x fastest, then z, then y.
  const states = new Array(W * H * L);
  let i = 0, p = 0;
  while (i < states.length) {
    let value = 0, shift = 0, b;
    do { b = data[p++] & 255; value |= (b & 127) << shift; shift += 7; } while (b & 128);
    states[i++] = byId[value];
  }
  return { version, dataVersion: s.DataVersion ? s.DataVersion.value : null, W, H, L, states, blockEntities,
    at: (x, y, z) => states[x + z * W + y * W * L] };
}

function parseState(s) {
  const m = s.match(/^([^\[]+)(?:\[(.*)\])?$/);
  const props = {};
  if (m[2]) for (const kv of m[2].split(',')) { const [k, v] = kv.split('='); props[k] = v; }
  return { name: m[1], props };
}

const isMain = import.meta.url === new URL('file:///' + process.argv[1].replace(/\\/g, '/')).href;
const cmd = isMain ? process.argv[2] : null, file = process.argv[3];
if (cmd === 'info') {
  const s = loadSchem(file);
  const counts = {};
  for (const st of s.states) counts[st] = (counts[st] || 0) + 1;
  console.log(`Sponge v${s.version}, DataVersion ${s.dataVersion}, size ${s.W} x ${s.H} x ${s.L} (W x H x L)`);
  const mods = new Set(Object.keys(counts).map((k) => k.split(':')[0]).filter((n) => n !== 'minecraft'));
  console.log('namespaces other than minecraft:', [...mods].join(', ') || 'none');
  console.log('block entities:', s.blockEntities.length, s.blockEntities.map((b) => b.Id || b.id).join(', '));
  for (const [k, v] of Object.entries(counts).sort((a, b) => b[1] - a[1])) console.log(String(v).padStart(6), k);
} else if (cmd === 'slices') {
  const s = loadSchem(file);
  const legend = {}; let next = 0;
  const sym = '#=+*%@&$?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  for (let y = 0; y < s.H; y++) {
    console.log(`--- layer y=${y} (x right, z down)`);
    for (let z = 0; z < s.L; z++) {
      let row = '';
      for (let x = 0; x < s.W; x++) {
        const st = s.at(x, y, z);
        const name = parseState(st).name.replace('minecraft:', '');
        if (name === 'air' || name === 'cave_air') { row += '.'; continue; }
        if (!(name in legend)) legend[name] = sym[next++ % sym.length];
        row += legend[name];
      }
      console.log(row);
    }
  }
  console.log('legend:', Object.entries(legend).map(([k, v]) => v + '=' + k).join('  '));
} else if (cmd === 'convert') {
  const out = process.argv[4];
  const args = process.argv.slice(5);
  const mapPath = args.includes('--map') ? args[args.indexOf('--map') + 1] : null;
  const map = mapPath ? JSON.parse(readFileSync(mapPath, 'utf8')) : {};
  const s = loadSchem(file);
  const palette = [], index = new Map(), blocks = [];
  const unmapped = new Set();
  for (let y = 0; y < s.H; y++) for (let z = 0; z < s.L; z++) for (let x = 0; x < s.W; x++) {
    let st = s.at(x, y, z);
    const name = parseState(st).name;
    if (map[st] !== undefined) st = map[st];
    else if (map[name] !== undefined) st = map[name] + (st.includes('[') ? st.slice(st.indexOf('[')) : '');
    if (st === null) continue;
    const parsed = parseState(st);
    if (parsed.name === 'minecraft:air' || parsed.name === 'minecraft:structure_void') continue;
    if (!parsed.name.startsWith('minecraft:') && !parsed.name.startsWith('dorohedoro:')) unmapped.add(parsed.name);
    if (!index.has(st)) {
      index.set(st, palette.length);
      const entry = { Name: tag.str(parsed.name) };
      if (Object.keys(parsed.props).length) entry.Properties = tag.compound(Object.fromEntries(Object.entries(parsed.props).map(([k, v]) => [k, tag.str(v)])));
      palette.push(entry);
    }
    blocks.push({ pos: tag.list('int', [x, y, z]), state: tag.int(index.get(st)) });
  }
  if (unmapped.size) { console.error('unmapped non-vanilla blocks:', [...unmapped].join(', ')); process.exit(2); }
  const root = { name: '', value: {
    DataVersion: tag.int(DATA_VERSION_1_21_1),
    size: tag.list('int', [s.W, s.H, s.L]),
    palette: tag.list('compound', palette),
    blocks: tag.list('compound', blocks),
    entities: tag.list('compound', []),
  } };
  writeFileSync(out, writeNbt(root));
  console.log(`wrote ${out}: ${s.W} x ${s.H} x ${s.L}, ${blocks.length} blocks, ${palette.length} states`);
} else if (isMain) {
  console.log('usage: info|slices|convert <file.schem> ...');
}
