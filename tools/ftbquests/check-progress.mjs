// Reads FTB Quests' saved team progress after a test client closes and checks which Dorohedoro quests were completed.
//   node tools/ftbquests/check-progress.mjs <world folder> <chapter:quest,...expected done> [<chapter:quest,...expected not done>]
// Quest numbers are the `n` and `q` values in gen-quests.mjs. Exits 1 if any expectation fails.
import fs from 'node:fs';
import path from 'node:path';

const [world, doneArg = '', notDoneArg = ''] = process.argv.slice(2);
if (!world) {
  console.error('usage: check-progress.mjs <world folder> <c:q,...> [<c:q,...>]');
  process.exit(2);
}
const hex2 = n => Number(n).toString(16).toUpperCase().padStart(2, '0');
const id = spec => { const [c, q] = spec.split(':'); return '0D0E0B0E' + hex2(c) + hex2(q) + '0000'; };

const dir = path.join(world, 'ftbquests');
const completed = new Set();
for (const file of fs.existsSync(dir) ? fs.readdirSync(dir) : []) {
  if (!file.endsWith('.snbt')) continue;
  let inside = false;
  for (const raw of fs.readFileSync(path.join(dir, file), 'utf8').split(/\r?\n/)) {
    const line = raw.trim();
    if (line.startsWith('completed: {')) { inside = !line.endsWith('}'); continue; }
    if (inside && line === '}') { inside = false; continue; }
    if (inside) completed.add(line.split(':')[0].trim());
  }
}
let failures = 0;
const report = (ok, what) => { console.log(`  ${ok ? 'PASS' : 'FAIL'} ${what}`); if (!ok) failures++; };
for (const spec of doneArg.split(',').filter(Boolean)) report(completed.has(id(spec)), `quest ${spec} (${id(spec)}) completed`);
for (const spec of notDoneArg.split(',').filter(Boolean)) report(!completed.has(id(spec)), `quest ${spec} (${id(spec)}) not completed`);
console.log(`  ${[...completed].filter(k => k.startsWith('0D0E0B0E') && k.endsWith('0000')).length} Dorohedoro quests completed in total`);
process.exit(failures ? 1 : 0);
