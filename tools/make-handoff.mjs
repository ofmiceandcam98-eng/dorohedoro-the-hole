// Rebuilds HANDOFF.md: the brief, then every text file in the repo, so one file carries the whole project.
// Files appear most useful first. Generated data comes last. Re-run with: node tools/make-handoff.mjs
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';
import { extname } from 'node:path';

const BRIEF = 'docs/HANDOFF_BRIEF.md';
const OUTPUT = 'HANDOFF.md';
const binary = new Set(['.png', '.jar', '.zip', '.ogg', '.bundle']);
const fenceFor = { '.java': 'java', '.json': 'json', '.gradle': 'groovy', '.ps1': 'powershell', '.sh': 'bash', '.mjs': 'js', '.toml': 'toml', '.md': 'markdown', '.csv': 'csv', '.properties': 'properties' };

const listed = execFileSync('git', ['ls-files', '--cached', '--others', '--exclude-standard'], { encoding: 'utf8' })
  .split('\n').map((line) => line.trim()).filter(Boolean)
  .filter((file) => file !== OUTPUT && file !== BRIEF && !binary.has(extname(file)) && !file.startsWith('gradle/wrapper/') && !file.includes('.cache/'));

const rank = (file) => {
  if (file.startsWith('docs/TEST_RESULTS') || file.startsWith('docs/IMPLEMENTATION_STATUS') || file.startsWith('docs/ROADMAP')) return 0;
  if (file.startsWith('docs/spec/') && file.endsWith('DIRECTIVE.md')) return 1;
  if (['build.gradle', 'gradle.properties', 'settings.gradle'].includes(file) || file.endsWith('neoforge.mods.toml')) return 2;
  if (file.startsWith('src/main/java/')) return 3;
  if (file.startsWith('docs/handoff/')) return 4;
  if (file.startsWith('tools/')) return 5;
  if (file.startsWith('src/main/resources/') && !file.endsWith('noise_settings/hole.json')) return 6;
  if (file.startsWith('docs/')) return 7;
  if (file.startsWith('src/generated/') || file.endsWith('noise_settings/hole.json')) return 9;
  return 8;
};
const files = listed.sort((a, b) => rank(a) - rank(b) || a.localeCompare(b));

const fenceAround = (text) => {
  const longest = Math.max(2, ...[...text.matchAll(/`{3,}/g)].map((m) => m[0].length));
  return '`'.repeat(longest + 1);
};

const parts = [readFileSync(BRIEF, 'utf8').trimEnd(), '', '## Contents of the rest of this file', '', `${files.length} files follow. Generated JSON is last and can be rebuilt with \`runData\`. Binary files are not included: three PNG textures rebuilt by \`tools/make-textures.mjs\`, and the Gradle wrapper jar, which comes from the NeoForge MDK template.`, ''];
for (const file of files) parts.push(`- \`${file}\``);
parts.push('');
for (const file of files) {
  const text = readFileSync(file, 'utf8').replace(/\r\n/g, '\n');
  const fence = fenceAround(text);
  parts.push(`## FILE: ${file}`, '', fence + (fenceFor[extname(file)] ?? ''), text.trimEnd(), fence, '');
}
writeFileSync(OUTPUT, parts.join('\n'));
console.log(`${OUTPUT}: ${files.length} files, ${Buffer.byteLength(parts.join('\n')) >> 10} KB`);
