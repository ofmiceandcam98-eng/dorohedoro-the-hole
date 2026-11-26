// Converts the permission-cleared community builds into 1.21.1 structure templates under
//   src/main/resources/data/dorohedoro/structure/sorcerer_city/<id>.nbt
//   node tools/community/convert-builds.mjs <folder with the downloaded originals>
// Each build: compatibility remap (compat-1.21.1.mjs), then small adaptations listed below (an opening in a railing
// for the street entrance, and so on), then a check that every block exists in 1.21.1. The originals are not
// committed; their SHA-256 is recorded in docs/sorcerers-world/SORCERERS_WORLD_ASSET_MANIFEST.md.
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { loadSchem } from './schem.mjs';
import { writeNbt, tag } from './nbt.mjs';
import { remap, NEWER_THAN_1_21_1 } from './compat-1.21.1.mjs';

const BUILDS = [
  {
    id: 'victorian_exchange',
    file: 'victorian_town_building.schem',
    sha256: '3f1a6d0ae47aac8f5421adb0d99f89d8adf20a4f61bf308f28c937407489f331',
    credit: 'Victorian Town Building by Frit (planetminecraft.com/project/victorian-town-building), free to use and modify',
    // Front (porch) is already at z = 0, facing north, as the mod's buildings are authored.
    // The porch stair is entered from the west behind a railing: open the railing at x = 10 for the street walk.
    carve: [[10, 0, 0], [10, 1, 0]],
  },
];

const parse = (s) => {
  const m = s.match(/^([^\[]+)(?:\[(.*)\])?$/);
  const props = {};
  if (m[2]) for (const kv of m[2].split(',')) { const [k, v] = kv.split('='); props[k] = v; }
  return { name: m[1], props };
};

const folder = process.argv[2];
const outDir = 'src/main/resources/data/dorohedoro/structure/sorcerer_city';
mkdirSync(outDir, { recursive: true });
for (const b of BUILDS) {
  const raw = readFileSync(join(folder, b.file));
  const sha = createHash('sha256').update(raw).digest('hex');
  if (sha !== b.sha256) throw new Error(`${b.file}: SHA-256 ${sha} does not match the audited original`);
  const s = loadSchem(join(folder, b.file));
  const carve = new Set(b.carve.map((p) => p.join(',')));
  const palette = [], index = new Map(), blocks = [];
  for (let y = 0; y < s.H; y++) for (let z = 0; z < s.L; z++) for (let x = 0; x < s.W; x++) {
    if (carve.has(`${x},${y},${z}`)) continue;
    const { name, props } = parse(s.at(x, y, z));
    if (name === 'minecraft:air' || name === 'minecraft:cave_air' || name === 'minecraft:structure_void') continue;
    const r = remap(name, props);
    if (!r) continue;
    if (NEWER_THAN_1_21_1.test(r.name) || !r.name.startsWith('minecraft:')) throw new Error(`${b.id}: ${r.name} is not a 1.21.1 vanilla block`);
    const key = r.name + JSON.stringify(Object.entries(r.props).sort());
    if (!index.has(key)) {
      index.set(key, palette.length);
      const entry = { Name: tag.str(r.name) };
      if (Object.keys(r.props).length) entry.Properties = tag.compound(Object.fromEntries(Object.entries(r.props).map(([k, v]) => [k, tag.str(v)])));
      palette.push(entry);
    }
    blocks.push({ pos: tag.list('int', [x, y, z]), state: tag.int(index.get(key)) });
  }
  const out = join(outDir, b.id + '.nbt');
  const bytes = writeNbt({ name: '', value: {
    DataVersion: tag.int(3955),
    size: tag.list('int', [s.W, s.H, s.L]),
    palette: tag.list('compound', palette),
    blocks: tag.list('compound', blocks),
    entities: tag.list('compound', []),
  } });
  writeFileSync(out, bytes);
  console.log(`${b.id}: ${s.W} x ${s.H} x ${s.L}, ${blocks.length} blocks, ${palette.length} states -> ${out}`);
  console.log(`  original sha256 ${sha}; converted sha256 ${createHash('sha256').update(bytes).digest('hex')}`);
  console.log(`  credit: ${b.credit}`);
}
