// Compares two QA reports for the same seed and reports chunks whose contents differ.
//   node tools/qa-compare.mjs <report-a.json> <report-b.json>
import { readFileSync } from 'node:fs';
const [a, b] = process.argv.slice(2).map((p) => JSON.parse(readFileSync(p, 'utf8')));
if (a.seed !== b.seed) { console.error('seeds differ'); process.exit(2); }
function diff(key) {
  const x = a[key], y = b[key];
  const keys = Object.keys(x).filter((k) => k in y);
  const bad = keys.filter((k) => x[k] !== y[k]);
  console.log(key + ': ' + (keys.length - bad.length) + '/' + keys.length + ' identical' + (bad.length ? ', differing e.g. ' + bad.slice(0, 6).join(' ') : ''));
  return bad.length;
}
const city = diff('chunk_hashes');
if (a.chunk_hashes_y64_raw && b.chunk_hashes_y64_raw) {
  // Informational: legacy fixed-Y=64 hash includes buried vanilla geology.
  diff('chunk_hashes_y64_raw');
}
diff('chunk_hashes_full');
// Only city/infrastructure order differences fail the determinism test.
process.exit(city ? 1 : 0);
