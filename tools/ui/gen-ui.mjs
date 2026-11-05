// Generates the Dorohedoro HUD and menu sprites into the GUI sprite atlas folder.
//   node tools/ui/gen-ui.mjs [previewDir]
// All art is original: grimy riveted iron from the Hole's sewers, smoke-teal trim, blood, gyoza and sludge.
import { Img, noise, mix, rgba } from './png.mjs';
import { writeFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';

const OUT = 'src/main/resources/assets/dorohedoro/textures/gui/sprites';
const preview = process.argv[2];
const made = [];

// ---------------------------------------------------------------- palette
const IRON = { line: '#0b0d0e', deep: '#181c1e', dark: '#25292b', mid: '#4b5254', light: '#737a79', shine: '#a6aca6' };
const RUST = ['#5a2a17', '#7c3a1d', '#9c5126', '#b86b33'];
const RIVET = { hi: '#c9c2b0', mid: '#8c8676', lo: '#3b3a35' };
const PANEL = { base: '#11181a', grime: '#0c1213', fleck: '#1a2427', inset: '#2c4945', insetHi: '#3f6b64' };
const SMOKE_TRIM = '#5fb3a5';

function save(name, img, nine) {
  img.save(join(OUT, name + '.png'));
  if (nine) {
    // Nine-slice so frames stretch to any size without blurring their borders.
    const meta = { gui: { scaling: { type: 'nine_slice', width: img.w, height: img.h, border: nine } } };
    writeFileSync(join(OUT, name + '.png.mcmeta'), JSON.stringify(meta, null, 2) + '\n');
  }
  made.push([name, img]);
}

// ---------------------------------------------------------------- shared pieces

/** Riveted iron frame: black outline, bevelled plate, rust bleeding from the rivets. */
function ironFrame(img, x, y, w, h, t, seed, opts = {}) {
  const { rivets = true, rust = true } = opts;
  img.rect(x, y, w, h, IRON.line);
  for (let j = 1; j < h - 1; j++) for (let i = 1; i < w - 1; i++) {
    const edge = Math.min(i, j, w - 1 - i, h - 1 - j);
    if (edge >= t) continue;
    const top = j < t && j <= i && j <= w - 1 - i;
    const left = i < t && i <= j && i <= h - 1 - j;
    const bottom = h - 1 - j < t && !top;
    const right = w - 1 - i < t && !left;
    let c = IRON.mid;
    if (edge === 1) c = top || left ? IRON.shine : IRON.dark;
    else if (top || left) c = IRON.light;
    else if (bottom || right) c = IRON.dark;
    // Pitted metal: a little noise so the plate is not a flat gradient.
    const n = noise(x + i, y + j, seed);
    if (n > 0.86) c = mix(c, IRON.deep, 0.35);
    else if (n < 0.06) c = mix(c, IRON.shine, 0.25);
    img.px(x + i, y + j, c);
  }
  // Inner lip: one dark line where the plate meets the recess.
  if (t >= 3) img.box(x + t - 1, y + t - 1, w - 2 * t + 2, h - 2 * t + 2, IRON.line);
  if (rust) {
    // Streaks run down from the top edge, like water through a sewer grate.
    for (let i = t; i < w - t; i++) {
      const n = noise(x + i, y, seed + 7);
      if (n > 0.8) {
        const len = 1 + Math.floor(n * 10) % Math.max(1, t - 1);
        for (let k = 0; k < len; k++) img.px(x + i, y + 1 + k, RUST[Math.min(3, k + (n > 0.93 ? 1 : 0))]);
      }
    }
  }
  if (rivets && w >= 14 && h >= 14) {
    const r = Math.max(2, Math.floor(t / 2));
    for (const [rx, ry] of [[r, r], [w - 1 - r, r], [r, h - 1 - r], [w - 1 - r, h - 1 - r]]) rivet(img, x + rx, y + ry, seed);
  }
}

function rivet(img, cx, cy, seed) {
  img.px(cx, cy, RIVET.hi);
  img.px(cx + 1, cy, RIVET.mid);
  img.px(cx, cy + 1, RIVET.mid);
  img.px(cx + 1, cy + 1, RIVET.lo);
  if (noise(cx, cy, seed) > 0.5) img.px(cx + 1, cy + 2, RUST[1]);
}

/** Dark recessed panel with grime flecks and a smoke-teal inset line. */
function recess(img, x, y, w, h, seed, inset = true) {
  for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
    const n = noise(x + i, y + j, seed);
    let c = PANEL.base;
    if (n > 0.93) c = PANEL.fleck;
    else if (n < 0.12) c = PANEL.grime;
    // Soft vignette towards the corners.
    const edge = Math.min(i, j, w - 1 - i, h - 1 - j);
    if (edge < 3) c = mix(c, '#070b0c', (3 - edge) * 0.12);
    img.px(x + i, y + j, c);
  }
  if (inset && w > 6 && h > 6) {
    img.hline(x + 1, x + w - 2, y + 1, PANEL.inset);
    img.vline(x + 1, y + 1, y + h - 2, PANEL.inset);
    img.hline(x + 2, x + w - 2, y + h - 2, '#1d302d');
    img.vline(x + w - 2, y + 2, y + h - 2, '#1d302d');
  }
}

/** A bar fill: shaded body, bright top line, darker base and a moving texture so it reads as liquid. */
function fill(name, w, h, c, texture) {
  const img = new Img(w, h);
  for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
    const t = j / Math.max(1, h - 1);
    let col = t < 0.2 ? c.hi : t < 0.55 ? c.mid : t < 0.85 ? c.base : c.deep;
    if (j === 0) col = c.shine;
    col = texture(i, j, col, c);
    img.px(i, j, col);
  }
  save(name, img);
}

const pulse = (i, j, col, c) => ((i + j * 2) % 11 === 0 && j > 0 && j < 4 ? mix(col, c.shine, 0.35) : col);
const bubbles = (i, j, col, c) => (noise(i, j, 31) > 0.9 && j > 1 ? mix(col, c.shine, 0.45) : col);
const wisps = (i, j, col, c) => {
  const v = Math.sin(i * 0.55 + j * 1.3) + Math.sin(i * 0.21 - j * 0.7);
  return v > 1.2 ? mix(col, c.shine, 0.4) : v < -1.4 ? mix(col, c.deep, 0.35) : col;
};
const sludge = (i, j, col, c) => (noise(i >> 1, j, 77) > 0.82 ? mix(col, c.hi, 0.5) : col);

// ---------------------------------------------------------------- HUD

// Portrait plate, 42x42: the face goes in the 32x32 recess.
{
  const img = new Img(42, 42);
  ironFrame(img, 0, 0, 42, 42, 5, 11);
  recess(img, 5, 5, 32, 32, 12, false);
  // Smoke-teal corner ticks on the recess, a quiet nod to the smoke trim on the bars.
  for (const [x, y, dx, dy] of [[5, 5, 1, 1], [36, 5, -1, 1], [5, 36, 1, -1], [36, 36, -1, -1]]) {
    img.px(x, y, SMOKE_TRIM); img.px(x + dx, y, SMOKE_TRIM); img.px(x, y + dy, SMOKE_TRIM);
  }
  save('hud/portrait_frame', img);
}

// Bar frame, 100x13 nine-slice: 3px iron border around a 94x7 channel... drawn at 100x13, inner 96x9.
{
  const img = new Img(100, 13);
  ironFrame(img, 0, 0, 100, 13, 2, 21, { rivets: false });
  recess(img, 2, 2, 96, 9, 22, false);
  img.px(1, 1, RIVET.mid); img.px(98, 1, RIVET.mid); img.px(1, 11, RIVET.lo); img.px(98, 11, RIVET.lo);
  save('hud/bar_frame', img, 3);
}

// Bar fills: exactly the 96x9 channel, cropped by the HUD to the current value.
const BLOOD = { shine: '#ff9c86', hi: '#e2473c', mid: '#b5201f', base: '#861317', deep: '#4f080b' };
const GYOZA = { shine: '#ffe9b0', hi: '#f2bd62', mid: '#d48a2f', base: '#a6611b', deep: '#5e340c' };
const SMOKE = { shine: '#d6fff6', hi: '#8fe0d0', mid: '#55b0a1', base: '#327b71', deep: '#173f3a' };
const AIR = { shine: '#d8f0ff', hi: '#8cc6f2', mid: '#4a8fd0', base: '#2c62a0', deep: '#152f55' };
const XP = { shine: '#eaffb0', hi: '#b9ea58', mid: '#7fbf23', base: '#558a12', deep: '#2b4a08' };
const POISON = { shine: '#e5ffb8', hi: '#9fd64a', mid: '#6d9e2a', base: '#4b7319', deep: '#24390a' };
const WITHER = { shine: '#8b8f93', hi: '#4d5156', mid: '#2e3135', base: '#1d1f22', deep: '#0b0c0d' };
const FROST = { shine: '#ffffff', hi: '#cfeeff', mid: '#93cdee', base: '#6aa6cf', deep: '#36607e' };
const GOLD = { shine: '#fff6c2', hi: '#ffd75a', mid: '#e8b322', base: '#b3830f', deep: '#5f4306' };
fill('hud/fill_health', 96, 9, BLOOD, pulse);
fill('hud/fill_hunger', 96, 9, GYOZA, bubbles);
fill('hud/fill_smoke', 96, 9, SMOKE, wisps);
fill('hud/fill_air', 96, 9, AIR, bubbles);
fill('hud/fill_poison', 96, 9, POISON, bubbles);
fill('hud/fill_wither', 96, 9, WITHER, pulse);
fill('hud/fill_frozen', 96, 9, FROST, wisps);
fill('hud/fill_absorb', 96, 9, GOLD, pulse);
fill('hud/fill_xp', 180, 5, XP, sludge);
{
  // The pale chunk that drains after a hit, so damage is readable at a glance.
  const img = new Img(96, 9);
  img.rect(0, 0, 96, 9, '#f3e6d6c0');
  img.hline(0, 95, 0, '#ffffffe0');
  save('hud/fill_ghost', img);
}

// XP frame, 184x9, and the level plate that sits over its middle.
{
  const img = new Img(184, 9);
  ironFrame(img, 0, 0, 184, 9, 2, 41, { rivets: false });
  recess(img, 2, 2, 180, 5, 42, false);
  for (let k = 1; k < 6; k++) img.vline(2 + k * 30, 2, 6, '#0b0d0e90'); // segment ticks, like gauge markings
  save('hud/xp_frame', img, 3);
}
{
  const img = new Img(26, 13);
  ironFrame(img, 0, 0, 26, 13, 2, 43, { rivets: false });
  recess(img, 2, 2, 22, 9, 44, false);
  img.hline(3, 22, 2, '#3f6b64');
  save('hud/level_plate', img, 3);
}

// Skill sockets, 26x26: worn iron, and a brass version for a focused slot.
for (const [name, focused] of [['hud/slot', false], ['hud/slot_focused', true]]) {
  const img = new Img(26, 26);
  ironFrame(img, 0, 0, 26, 26, 3, focused ? 53 : 51);
  if (focused) {
    // Brass wash over the plate: the slot is primed to cast its focused variant.
    for (let j = 0; j < 26; j++) for (let i = 0; i < 26; i++) {
      const edge = Math.min(i, j, 25 - i, 25 - j);
      if (edge > 0 && edge < 3) img.px(i, j, mix(img.get(i, j), '#c9a050', 0.55));
    }
  }
  recess(img, 3, 3, 20, 20, 52, true);
  save(name, img);
}
{
  const img = new Img(12, 12);
  ironFrame(img, 0, 0, 12, 12, 2, 61, { rivets: false });
  recess(img, 2, 2, 8, 8, 62, false);
  save('hud/key_plate', img);
}
{
  // Chip behind small stat readouts (armor, air).
  const img = new Img(30, 11);
  ironFrame(img, 0, 0, 30, 11, 1, 71, { rivets: false, rust: false });
  recess(img, 1, 1, 28, 9, 72, false);
  save('hud/chip', img, 3);
}

// HUD icons, 9x9. Hand-drawn.
const ICONS = {
  // A stitched heart: Hole doctors sew everything back together.
  health: [['.OO...OO.', 'ORRO.ORHO', 'ORRRORRHO', 'ORRRRRRRO', 'OWRWRWRRO', '.ORRRRRO.', '..ORRRO..', '...ORO...', '....O....'],
    { O: '#2a0507', R: '#c0262a', H: '#ff8f7a', W: '#e8dcc4' }],
  // A gyoza, pleated on top: the Hungry Bug's dumplings keep the Hole fed.
  hunger: [['.........', '...PPP...', '.PWPWPWP.', 'OYYYYYYYO', 'OYYYYYYGO', 'OYYYYYGGO', '.OYYGGGO.', '..OOOOO..', '.........'],
    { O: '#4a2a0e', P: '#f6e3b8', W: '#c99a5a', Y: '#efc977', G: '#c98b3a' }],
  // A curl of sorcerer smoke rising out of a fingertip.
  smoke: [['...SS....', '..S..S...', '..S.SS...', '...SS.SS.', '.SS...S.S', 'S..S..S.S', 'S.SS.SS..', '.S..SS...', '..TT.....'],
    { S: '#8fe0d0', T: '#3d8a7f' }],
  // A dented chest plate.
  armor: [['.OO...OO.', 'OLMO.OMLO', 'OLMMMMMLO', 'OLMMMMMDO', '.OMMDMMO.', '.OMMMMDO.', '.OMMMMMO.', '..OMMMO..', '...OOO...'],
    { O: '#111416', L: '#cfd3d0', M: '#9aa09c', D: '#5f6563' }],
  // A sewer bubble.
  air: [['...OOO...', '..OBBBO..', '.OBWBBBO.', '.OBBBBBO.', '.OBBBBBO.', '.OBBBBDO.', '..OBBDO..', '...OOO...', '.........'],
    { O: '#183453', B: '#5aa0e0', W: '#e8f6ff', D: '#2f6aa8' }],
  // A severed arm with stitches: you have an untreated injury.
  injury: [['......OO.', '.....OSSO', '....OSSO.', '...OSSO..', '..OSWSO..', '.OSSOO...', 'ORRSO....', 'ORRO.....', '.OO......'],
    { O: '#2b0c08', S: '#d9a77f', W: '#e8dcc4', R: '#b8221f' }],
};
for (const [name, [rows, pal]] of Object.entries(ICONS)) {
  const img = new Img(9, 9);
  img.art(0, 0, rows, pal);
  save('hud/icon_' + name, img);
}

// ---------------------------------------------------------------- menus

// Window frame, 64x64 nine-slice with a 10px border: the Hole's heavy riveted sewer iron.
{
  const img = new Img(64, 64);
  ironFrame(img, 0, 0, 64, 64, 7, 81);
  recess(img, 7, 7, 50, 50, 82, true);
  // Extra rivets along the edges, spaced so the nine-slice repeats look intentional.
  for (const p of [[31, 2], [2, 31], [60, 31], [31, 60]]) rivet(img, p[0], p[1], 83);
  save('menu/window', img, 10);
}
// Inset panel, 24x24 nine-slice: content wells inside the window.
{
  const img = new Img(24, 24);
  img.rect(0, 0, 24, 24, IRON.line);
  recess(img, 1, 1, 22, 22, 91, true);
  save('menu/panel', img, 4);
}
// Title plate, 48x18 nine-slice: brass-edged iron sign.
{
  const img = new Img(48, 18);
  ironFrame(img, 0, 0, 48, 18, 3, 95, { rivets: false });
  recess(img, 3, 3, 42, 12, 96, false);
  img.hline(3, 44, 3, '#3f6b64');
  rivet(img, 1, 7, 97);
  rivet(img, 45, 7, 98);
  save('menu/title_plate', img, 5);
}
// Tabs: a raised iron tab and a selected one that joins the window face.
for (const [name, sel] of [['menu/tab', false], ['menu/tab_selected', true]]) {
  const h = sel ? 28 : 25;
  const img = new Img(28, h);
  ironFrame(img, 0, 0, 28, h + 4, 3, sel ? 101 : 103, { rivets: false });
  if (sel) {
    recess(img, 3, 3, 22, h - 3, 102, false);
    img.hline(3, 24, 3, SMOKE_TRIM);
  } else {
    recess(img, 3, 3, 22, h - 6, 104, false);
  }
  save(name, img);
}
// Buttons: iron plates; hover lights the smoke trim; disabled is dull and flat.
for (const [name, state] of [['menu/button', 0], ['menu/button_hover', 1], ['menu/button_disabled', 2]]) {
  const img = new Img(40, 20);
  ironFrame(img, 0, 0, 40, 20, 3, 111 + state, { rivets: false, rust: state !== 2 });
  for (let j = 3; j < 17; j++) for (let i = 3; i < 37; i++) {
    const n = noise(i, j, 113);
    let c = state === 2 ? '#22282a' : j < 9 ? '#3a4245' : '#30373a';
    if (n > 0.9) c = mix(c, '#000000', 0.2);
    img.px(i, j, c);
  }
  img.hline(3, 36, 3, state === 1 ? SMOKE_TRIM : state === 2 ? '#2c3336' : '#566062');
  if (state === 1) {
    img.hline(3, 36, 16, '#2f6f66');
    img.vline(3, 3, 16, SMOKE_TRIM);
  }
  save(name, img, 4);
}
// Divider rail, 16x4 nine-slice.
{
  const img = new Img(16, 4);
  img.hline(0, 15, 0, IRON.line);
  img.hline(0, 15, 1, IRON.light);
  img.hline(0, 15, 2, IRON.dark);
  img.hline(0, 15, 3, IRON.line);
  save('menu/divider', img, 1);
}

// Tab icons, 16x16.
const TAB_ICONS = {
  // A sorcerer's mask: the character sheet.
  status: [[
    '................',
    '.....OOOOOO.....',
    '...OOBBBBBBOO...',
    '..OBBBBBBBBBBO..',
    '..OBWWBBBBWWBO..',
    '.OBBOOWBBWOOBBO.',
    '.OBOOOWBBWOOOBO.',
    '.OBBOOBBBBOOBBO.',
    '.OBBBBBBBBBBBBO.',
    '..OBBBBTTBBBBO..',
    '..OBBBTOOTBBBO..',
    '...OBBTOOTBBO...',
    '....OBBTTBBO....',
    '.....OBBBBO.....',
    '......OOOO......',
    '................'],
    { O: '#0f1112', B: '#e3dccb', W: '#ffffff', T: '#9c1c1c' }],
  // Smoke billowing from an open palm: the techniques you can learn.
  techniques: [[
    '.....SSS........',
    '...SS...SS..SS..',
    '..S..SS...SS..S.',
    '..S.S..S.....S..',
    '...S...S..SSS...',
    '....SSS..S......',
    '.......SS.......',
    '......S.........',
    '...H..S..H......',
    '..HKH.S.HKH.....',
    '..HKHHHHHKH..H..',
    '..HKKKKKKKH.HKH.',
    '..HKKKKKKKKHKH..',
    '...HKKKKKKKKH...',
    '....HHHHHHHH....',
    '................'],
    { S: '#8fe0d0', H: '#3b2416', K: '#c58e66' }],
  // Three iron sockets marked for Z, X and C: your quick-cast loadouts.
  loadouts: [[
    '................',
    '.OOOO.OOOO.OOOO.',
    '.OLLO.OLLO.OLLO.',
    '.OTTO.ORRO.OGGO.',
    '.OTTO.ORRO.OGGO.',
    '.OOOO.OOOO.OOOO.',
    '................',
    '..II...II...II..',
    '..II...II...II..',
    '................',
    '.WWWWWWWWWWWWWW.',
    '.WDDDDDDDDDDDDW.',
    '.WDTTTTDDDDDDDW.',
    '.WDDDDDDDDDDDDW.',
    '.WWWWWWWWWWWWWW.',
    '................'],
    { O: '#0f1112', L: '#9aa09c', T: '#5fb3a5', R: '#b5201f', G: '#d48a2f', I: '#454b4d', W: '#666d6c', D: '#11181a' }],
  // A severed arm, stitched: your body, injuries and how to mend them.
  body: [[
    '............OO..',
    '...........OSSO.',
    '..........OSSSO.',
    '.........OSSSO..',
    '........OSSSO...',
    '.......OSSSO....',
    '......OSWSO.....',
    '.....OSSWWO.....',
    '....OSSSSO......',
    '...ORRSSO.......',
    '..ORRRRO........',
    '.ORRRRO.........',
    '.ORRO...........',
    '..OO............',
    '................',
    '................'],
    { O: '#2b0c08', S: '#d9a77f', W: '#f0e6d2', R: '#a8201c' }],
};
for (const [name, [rows, pal]] of Object.entries(TAB_ICONS)) {
  const img = new Img(16, 16);
  img.art(0, 0, rows, pal);
  save('menu/icon_' + name, img);
}

console.log('generated', made.length, 'sprites into', OUT);

if (preview) {
  // One contact sheet at 4x, for reviewing the art in a single image.
  const scale = 4, pad = 6;
  let x = pad, y = pad, rowH = 0;
  const sheetW = 1400;
  const placed = [];
  for (const [name, img] of made) {
    const w = img.w * scale, h = img.h * scale;
    if (x + w + pad > sheetW) { x = pad; y += rowH + pad; rowH = 0; }
    placed.push([img, x, y]);
    x += w + pad;
    rowH = Math.max(rowH, h);
  }
  const sheet = new Img(sheetW, y + rowH + pad);
  sheet.rect(0, 0, sheet.w, sheet.h, '#5b6366');
  for (const [img, px, py] of placed) {
    const s = img.scaled(scale);
    for (let j = 0; j < s.h; j++) for (let i = 0; i < s.w; i++) {
      const c = s.get(i, j);
      if (c[3] > 0) sheet.px(px + i, py + j, c);
    }
  }
  mkdirSync(preview, { recursive: true });
  sheet.save(join(preview, 'ui-contact-sheet.png'));
  console.log('preview', join(preview, 'ui-contact-sheet.png'));
}
