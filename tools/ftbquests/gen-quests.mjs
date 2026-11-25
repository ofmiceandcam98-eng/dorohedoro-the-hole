// Generates the optional Dorohedoro FTB Quests book (FTB Quests 2101.1.x SNBT format).
//
//   node tools/ftbquests/gen-quests.mjs
//
// Writes src/main/resources/dorohedoro_compat/ftbquests/quests/: data.snbt, chapter_groups.snbt, chapters/*.snbt and
// lang/en_us.snbt. The mod ships these inside its jar; `/dorohedoro quests install` copies them into
// config/ftbquests/quests when FTB Quests is installed, and a modpack can include the folder directly.
//
// Rules (docs/spec/FTB_QUESTS_DIRECTIVE.md): the book is a guide only. Nothing here gates a recipe, dimension, door or
// ability. Every task uses a real registry ID, and organic play gets credit: item tasks look at the inventory, and
// events only the mod can see (doors, origins, returning from Hell) are hidden advancements in
// data/dorohedoro/advancement/, which FTB re-checks every few seconds.
//
// IDs are stable: 0D0E0B0E + chapter (2 hex) + quest (2 hex) + kind (0 quest, 1 task, 2 reward) + index + 00.
// Never renumber a quest that has shipped; add new ones with new numbers.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const OUT = path.join(ROOT, 'src/main/resources/dorohedoro_compat/ftbquests/quests');

const hex2 = n => n.toString(16).toUpperCase().padStart(2, '0');
const PREFIX = '0D0E0B0E';
const GROUP_ID = PREFIX + '00000001';
const chapterId = c => PREFIX + hex2(c) + '000000';
const questId = (c, q) => PREFIX + hex2(c) + hex2(q) + '0000';
const taskId = (c, q, i) => PREFIX + hex2(c) + hex2(q) + '1' + i.toString(16).toUpperCase() + '00';
const rewardId = (c, q, i) => PREFIX + hex2(c) + hex2(q) + '2' + i.toString(16).toUpperCase() + '00';

// Task and reward shorthands.
const item = (id, count = 1) => ({ type: 'item', id, count });
const adv = (id, title) => ({ type: 'advancement', id, title });
const kill = (id, count) => ({ type: 'kill', id, count });
const give = (id, count = 1) => ({ type: 'item', id, count });
const xp = amount => ({ type: 'xp', amount });
const levels = amount => ({ type: 'xp_levels', amount });

const CHAPTERS = [
  {
    n: 1, file: 'dorohedoro_welcome', icon: 'dorohedoro:scrap_metal',
    title: 'Welcome to the Hole',
    quests: [
      { q: 1, x: 0, y: 0, shape: 'gear', icon: 'dorohedoro:salted_rat_ration',
        title: 'Born in the Hole', subtitle: 'Human or sorcerer, you start here',
        desc: ['The first time you join, the mod asks who you are: a human, or a sorcerer with one innate smoke magic.',
          '', 'Humans can make everything a sorcerer can travel with, including the Artificial Door. Nothing in this book is locked to one origin.',
          '', 'This book is a guide only. You can ignore it completely.'],
        tasks: [adv('dorohedoro:origin/chosen', 'Choose your origin')], rewards: [give('dorohedoro:cooked_rat_meat', 3)] },
      { q: 2, x: 2, y: 0, deps: [1], icon: 'dorohedoro:industrial_debris',
        title: 'Scrap Is Currency', subtitle: 'Industrial Debris is the Hole\'s first metal',
        desc: ['Heaps of Industrial Debris lie in yards and alleys across the city. Mine them with a pickaxe for Scrap Metal and the odd iron nugget.',
          '', 'Scrap smelts into iron nuggets. Four Scrap in a square make two Rusted Plating, a building block that every Reinforced Door Frame needs.'],
        tasks: [item('dorohedoro:scrap_metal', 8)], rewards: [give('minecraft:torch', 8)] },
      { q: 4, x: 4, y: 1, deps: [2], icon: 'dorohedoro:raw_sulfur',
        title: 'Salt and Sulfur', subtitle: 'Minerals under the streets',
        desc: ['Sulfur Ore and Rock Salt Ore are in the stone below the city, with deepslate versions further down.',
          '', 'Sulfur goes into gunpowder; salt preserves food.'],
        tasks: [item('dorohedoro:raw_sulfur', 4), item('dorohedoro:rock_salt', 4)], rewards: [give('minecraft:bread', 4)] },
      { q: 5, x: 6, y: 1, deps: [4],
        title: 'Salted Rations', subtitle: 'Food that keeps',
        desc: ['Scrap Rats run from you, but their meat cooks like any other. Cooked Rat Meat, Rock Salt and Bread make a Salted Rat Ration, one of the most filling foods in the Hole.'],
        tasks: [item('dorohedoro:salted_rat_ration', 2)], rewards: [give('minecraft:wheat_seeds', 8)] },
      { q: 6, x: 4, y: -1, deps: [2], icon: 'dorohedoro:brass_ingot',
        title: 'Zinc and Brass', subtitle: 'The city\'s working metals',
        desc: ['Smelt Raw Zinc from Zinc Ore into Zinc Ingots. One Zinc Ingot and one Copper Ingot make two Brass Ingots.',
          '', 'Brass goes into Cavern Lamps.'],
        tasks: [item('dorohedoro:zinc_ingot', 2), item('dorohedoro:brass_ingot', 2)], rewards: [give('minecraft:copper_ingot', 4)] },
      { q: 7, x: 8, y: 0, deps: [6, 13],
        title: 'Light in the Smog', subtitle: 'A lamp that never needs fuel',
        desc: ['A Cavern Lamp needs Brass, Glass Panes, an Iron Ingot, Redstone and two Luminous Scales from a Glow Moth.',
          '', 'It gives steady light for streets, sewers and homes.'],
        tasks: [item('dorohedoro:cavern_lamp', 1)], rewards: [xp(60)] },
      { q: 8, x: 2, y: 2, deps: [1], optional: true, icon: 'dorohedoro:healing_smoke_flask',
        title: 'First Technique', subtitle: 'Optional: for sorcerers',
        desc: ['If you are a sorcerer, equip a technique in your character record (press B) and cast it with Z, X or C.',
          '', 'Humans can skip this one. Bottled smoke in Smoke Flasks lets anyone use a sorcerer\'s magic.'],
        tasks: [adv('dorohedoro:magic/first_technique', 'Cast a technique')], rewards: [xp(50)] },
    ],
  },
  {
    n: 2, file: 'dorohedoro_streets', icon: 'dorohedoro:sewer_brickwork',
    title: 'Under the Streets',
    quests: [
      { q: 10, x: 0, y: 0, shape: 'gear', icon: 'dorohedoro:sewer_brickwork',
        title: 'Into the Undercity', subtitle: 'The sewers run under every district',
        desc: ['Storm drains and sewer grates lead down into brick tunnels, and some long branches open into caverns.',
          '', 'Reach the undercity below the streets. Bring torches and food.'],
        tasks: [adv('dorohedoro:hole/undercity', 'Reach the undercity')], rewards: [give('minecraft:torch', 16)] },
      { q: 11, x: 2, y: -1, deps: [10],
        title: 'Pest Control', subtitle: 'Sewer Roaches bite',
        desc: ['Sewer Roaches pounce from the dark, and they alert each other when hurt. Their antennae and chitin are useful.'],
        tasks: [kill('dorohedoro:sewer_roach', 5)], rewards: [give('minecraft:cooked_beef', 3)] },
      { q: 12, x: 4, y: -1, deps: [11],
        title: 'Echoes in the Pipes', subtitle: 'Find your way underground',
        desc: ['A Sewer Echo Probe uses Roach Antennae, Redstone, Copper and Iron to sense the sewer around you.'],
        tasks: [item('dorohedoro:sewer_echo_probe', 1)], rewards: [xp(60)] },
      { q: 13, x: 2, y: 1, deps: [10], icon: 'dorohedoro:luminous_scale',
        title: 'Moth Light', subtitle: 'Shear a Glow Moth',
        desc: ['Glow Moths drift through dark places and light the air around them. Use shears on one for Luminous Scales; it grows them back.'],
        tasks: [item('dorohedoro:luminous_scale', 2)], rewards: [give('minecraft:glass_pane', 8)] },
      { q: 14, x: 4, y: 1, deps: [13],
        title: 'Patch Yourself Up', subtitle: 'Leech mucus heals',
        desc: ['Sewer Leeches live in the water. Their mucus, with Paper and a Honey Bottle, makes a Medical Salve.'],
        tasks: [item('dorohedoro:medical_salve', 1)], rewards: [give('minecraft:honey_bottle', 2)] },
      { q: 15, x: 6, y: 1, deps: [14],
        title: 'Antidote', subtitle: 'Spores, slime and poison',
        desc: ['Spore Crawlers poison what they hit. A Spore Sac, Leech Mucus and a Fungal Membrane make two Antidote Poultices, which cure poison.'],
        tasks: [item('dorohedoro:antidote_poultice', 1)], rewards: [xp(80)] },
      { q: 16, x: 6, y: -1, deps: [12], icon: 'minecraft:brewing_stand',
        title: 'Hole Brewing', subtitle: 'Creature parts in the brewing stand',
        desc: ['Creature parts brew from an Awkward Potion: a Spore Sac makes Poison, a Sludge Gland makes Slowness, a Burrower Claw makes Strength, and more.',
          '', 'Brew any potion to finish this quest.'],
        tasks: [adv('minecraft:nether/brew_potion', 'Brew a potion')], rewards: [give('minecraft:glass_bottle', 6)] },
      { q: 17, x: 8, y: 0, deps: [15, 16], optional: true, icon: 'dorohedoro:surgical_reattachment_kit',
        title: 'Back in One Piece', subtitle: 'Optional: reattach a severed limb',
        desc: ['Dismemberment magic can take a limb. A Surgical Reattachment Kit, or a Healing sorcerer, puts the matching uncooked limb back.',
          '', 'The patient crouches to agree. You can do this for a friend or have one do it for you.'],
        tasks: [adv('dorohedoro:medicine/limb_reattached', 'Reattach a limb')], rewards: [xp(100)] },
    ],
  },
  {
    n: 3, file: 'dorohedoro_doors', icon: 'dorohedoro:artificial_door_core',
    title: 'Doors Between Worlds',
    quests: [
      { q: 20, x: 0, y: 0, shape: 'gear',
        title: 'Smoke-Soaked Filter', subtitle: 'Old smoke in old machines',
        desc: ['Mining Industrial Debris sometimes turns up a Smoke-Soaked Filter. They are also in apartment, factory and laboratory chests.',
          '', 'This is where a human\'s road to the Sorcerers\' World starts.'],
        tasks: [item('dorohedoro:smoke_soaked_filter', 1)], rewards: [xp(30)] },
      { q: 21, x: 2, y: 0, deps: [20],
        title: 'Smoke Residue', subtitle: 'Smelt the filter',
        desc: ['Smelting a Smoke-Soaked Filter leaves Smoke Residue, the magical condensate every door machine needs.'],
        tasks: [item('dorohedoro:smoke_residue', 3)], rewards: [give('minecraft:coal', 4)] },
      { q: 22, x: 4, y: -1, deps: [21],
        title: 'Smoke Condenser', subtitle: 'Iron, copper, redstone and residue',
        desc: ['The Smoke Condenser captures and holds smoke. It is the heart of an Artificial Door Core.'],
        tasks: [item('dorohedoro:smoke_condenser', 1)], rewards: [give('minecraft:iron_ingot', 2)] },
      { q: 23, x: 4, y: 1, deps: [21],
        title: 'Preserved Conduit', subtitle: 'A pipe that keeps smoke stable',
        desc: ['Craft Smoke Conduits from Copper, Glass, Iron and Redstone, then treat one with Smoke Residue and Bone Meal to make a Preserved Smoke Conduit.'],
        tasks: [item('dorohedoro:preserved_smoke_conduit', 1)], rewards: [give('minecraft:bone_meal', 6)] },
      { q: 24, x: 6, y: 0, deps: [22, 23],
        title: 'Artificial Door Core', subtitle: 'A door any human can build',
        desc: ['Bone Blocks, a Smoke Condenser, Iron, a Preserved Smoke Conduit and a Redstone Block make the core.',
          '', 'You also need 13 Reinforced Door Frames: Iron Ingots, Rusted Plating and Stone Bricks make four at a time.'],
        tasks: [item('dorohedoro:artificial_door_core', 1), item('dorohedoro:reinforced_door_frame', 13), item('dorohedoro:door_activation_tool', 1)],
        rewards: [xp(120)] },
      { q: 25, x: 8, y: 0, deps: [24], shape: 'hexagon',
        title: 'Open the Door', subtitle: 'Build it, then wake it',
        desc: ['Build a frame 4 blocks wide and 5 tall with a clear 2x3 opening. The core goes in the bottom row, second from the left.',
          '', 'Right-click the core with the Door Activation Tool. Smoke fills the doorway and a matching door forms in the Sorcerers\' World.',
          '', 'Only a real, working door counts. Holding a core does not.'],
        tasks: [adv('dorohedoro:doors/artificial_door_opened', 'Activate an Artificial Door')], rewards: [levels(3)] },
      { q: 26, x: 10, y: 0, deps: [25], shape: 'hexagon', icon: 'minecraft:dark_oak_sapling',
        title: 'The Sorcerers\' World', subtitle: 'It never rains here',
        desc: ['Step through. The Sorcerers\' World has its own cities, farmland, woods and mountains, and no rain.',
          '', 'Your door\'s far end waits for you; walk back through it to return to the Hole.'],
        tasks: [adv('dorohedoro:realms/entered_sorcerers_world', 'Enter the Sorcerers\' World')], rewards: [give('minecraft:bread', 6)] },
      { q: 27, x: 12, y: 0, deps: [26], icon: 'minecraft:dark_oak_log',
        title: 'Hydra Forest', subtitle: 'Real timber at last',
        desc: ['Unlike the Hole, the Sorcerers\' World has full forests. Find the dense Hydra Forest and bring back wood and saplings for a tree farm at home.'],
        tasks: [adv('dorohedoro:realms/hydra_forest', 'Visit the Hydra Forest')], rewards: [give('minecraft:dark_oak_sapling', 4)] },
    ],
  },
  {
    n: 4, file: 'dorohedoro_hell', icon: 'dorohedoro:infernal_door_core',
    title: 'Hell',
    quests: [
      { q: 30, x: 0, y: 0, shape: 'gear',
        title: 'Devil Sigil', subtitle: 'Expensive, but no devil needed',
        desc: ['A Devil Sigil takes Obsidian, Gold, Smoke Residue, a Bone Block and an Emerald. Rare sigils also turn up in laboratory and crypt loot.',
          '', 'Everything for your first gate can be found before you ever reach Hell.'],
        tasks: [item('dorohedoro:devil_sigil', 2)], rewards: [give('minecraft:gold_ingot', 2)] },
      { q: 31, x: 2, y: 0, deps: [30], icon: 'dorohedoro:etched_gate_stone',
        title: 'Gate Stones and Core', subtitle: 'Fifteen stones, one core',
        desc: ['Etched Gate Stone comes four at a time from Bone Blocks, Stone Bricks and a Redstone Block.',
          '', 'The Infernal Door Core takes two Devil Sigils, Bone Blocks, two Etched Gate Stones and a Preserved Smoke Conduit. Craft a Ritual Tool to wake it.'],
        tasks: [item('dorohedoro:etched_gate_stone', 15), item('dorohedoro:infernal_door_core', 1), item('dorohedoro:ritual_tool', 1)],
        rewards: [xp(150)] },
      { q: 32, x: 4, y: 0, deps: [31], shape: 'hexagon',
        title: 'Open the Devil Gate', subtitle: 'Five by five',
        desc: ['Build a 5x5 ring of Etched Gate Stone with a clear 3x3 middle. The Infernal Door Core sits at the center of the bottom row.',
          '', 'Right-click the core with the Ritual Tool.'],
        tasks: [adv('dorohedoro:doors/devil_gate_opened', 'Activate a Devil Gate')], rewards: [levels(3)] },
      { q: 33, x: 6, y: 0, deps: [32], shape: 'hexagon', icon: 'dorohedoro:hell_rock',
        title: 'Into Hell', subtitle: 'Not the Nether',
        desc: ['Hell is barren: ash wastes, horn-shaped mountains, soul flats and deep caverns. Nothing grows there.',
          '', 'Your gate\'s return end stands where you arrive. Remember where it is.'],
        tasks: [adv('dorohedoro:realms/entered_hell', 'Enter Hell')], rewards: [give('minecraft:cooked_beef', 4)] },
      { q: 34, x: 8, y: -1, deps: [33],
        title: 'Hell Rock', subtitle: 'Building stone from the wastes',
        desc: ['Hell Rock is a dark, hard stone that makes a good building block. Ash Soil covers the wastes.'],
        tasks: [item('dorohedoro:hell_rock', 16)], rewards: [give('minecraft:iron_ingot', 2)] },
      { q: 35, x: 8, y: 1, deps: [33], icon: 'minecraft:bone',
        title: 'The Horns', subtitle: 'Mountains like devil horns',
        desc: ['Find the horn-shaped peaks of Hell.'],
        tasks: [adv('dorohedoro:realms/hell_horns', 'Reach the Horns')], rewards: [xp(100)] },
      { q: 36, x: 10, y: 0, deps: [34, 35], shape: 'hexagon', icon: 'dorohedoro:ritual_tool',
        title: 'Safe Return', subtitle: 'Walk back through your gate alive',
        desc: ['Travel back to the Hole through a Devil Gate. Dying and respawning does not count.'],
        tasks: [adv('dorohedoro:realms/returned_from_hell', 'Return from Hell through a gate')], rewards: [levels(5)] },
    ],
  },
];

// ---- SNBT writer (FTB style: tabs, no commas between entries, typed numbers) ----
function snbt(value, indent = 0) {
  const pad = '\t'.repeat(indent);
  if (Array.isArray(value)) {
    if (value.length === 0) return '[ ]';
    if (value.every(v => typeof v === 'string')) {
      if (value.length === 1) return `[${snbt(value[0])}]`;
      return '[\n' + value.map(v => pad + '\t' + snbt(v)).join('\n') + '\n' + pad + ']';
    }
    return '[\n' + value.map(v => pad + '\t' + snbt(v, indent + 1)).join('\n') + '\n' + pad + ']';
  }
  if (value && typeof value === 'object' && value.__raw) return value.__raw;
  if (value && typeof value === 'object') {
    const keys = Object.keys(value).sort();
    return '{\n' + keys.map(k => `${pad}\t${/^[a-z_][a-z0-9_.]*$/i.test(k) ? k : JSON.stringify(k)}: ${snbt(value[k], indent + 1)}`).join('\n') + '\n' + pad + '}';
  }
  if (typeof value === 'string') return JSON.stringify(value);
  if (typeof value === 'boolean') return String(value);
  throw new Error('unsupported ' + value);
}
const D = n => ({ __raw: `${Number(n).toFixed(1)}d` });
const L = n => ({ __raw: `${n}L` });
const I = n => ({ __raw: `${n}` });
const stack = (id, count) => ({ count: I(count), id });

const lang = {};
const questIndex = new Map();
for (const ch of CHAPTERS) for (const q of ch.quests) questIndex.set(q.q, ch.n);

function buildTask(ch, q, t, i) {
  const id = taskId(ch.n, q.q, i);
  if (t.title) lang[`task.${id}.title`] = t.title;
  switch (t.type) {
    case 'item': return t.count > 1 ? { id, type: 'item', item: stack(t.id, 1), count: L(t.count) } : { id, type: 'item', item: stack(t.id, 1) };
    case 'advancement': return { id, type: 'advancement', advancement: t.id, criterion: '' };
    case 'kill': return { id, type: 'kill', entity: t.id, value: L(t.count) };
    default: throw new Error(t.type);
  }
}
function buildReward(ch, q, r, i) {
  const id = rewardId(ch.n, q.q, i);
  switch (r.type) {
    case 'item': return { id, type: 'item', item: stack(r.id, r.count) };
    case 'xp': return { id, type: 'xp', xp: I(r.amount) };
    case 'xp_levels': return { id, type: 'xp_levels', xp_levels: I(r.amount) };
    default: throw new Error(r.type);
  }
}

fs.rmSync(OUT, { recursive: true, force: true });
fs.mkdirSync(path.join(OUT, 'chapters'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'lang'), { recursive: true });

lang[`chapter_group.${GROUP_ID}.title`] = 'Dorohedoro: The Hole';
let total = 0;
CHAPTERS.forEach((ch, order) => {
  const cid = chapterId(ch.n);
  lang[`chapter.${cid}.title`] = ch.title;
  const quests = ch.quests.map(q => {
    total++;
    const id = questId(ch.n, q.q);
    lang[`quest.${id}.title`] = q.title;
    if (q.subtitle) lang[`quest.${id}.quest_subtitle`] = q.subtitle;
    lang[`quest.${id}.quest_desc`] = q.desc;
    const out = { id, x: D(q.x), y: D(q.y), tasks: q.tasks.map((t, i) => buildTask(ch, q, t, i)) };
    if (q.rewards?.length) out.rewards = q.rewards.map((r, i) => buildReward(ch, q, r, i));
    if (q.deps?.length) out.dependencies = q.deps.map(d => {
      if (!questIndex.has(d)) throw new Error('missing dependency ' + d);
      return questId(questIndex.get(d), d);
    });
    if (q.shape) out.shape = q.shape;
    if (q.optional) out.optional = true;
    if (q.icon) out.icon = { id: q.icon };
    return out;
  });
  const chapter = {
    default_hide_dependency_lines: false, default_quest_shape: '', filename: ch.file, group: GROUP_ID,
    icon: { id: ch.icon }, id: cid, order_index: I(order), quest_links: [], quests,
  };
  fs.writeFileSync(path.join(OUT, 'chapters', ch.file + '.snbt'), snbt(chapter) + '\n');
});

fs.writeFileSync(path.join(OUT, 'chapter_groups.snbt'), snbt({ chapter_groups: [{ id: GROUP_ID }] }) + '\n');
// Book settings for a standalone book (a modpack, or a world with no quest book yet). Merging into an existing book
// never touches that book's own data.snbt.
fs.writeFileSync(path.join(OUT, 'data.snbt'), snbt({
  default_autoclaim_rewards: 'disabled', default_consume_items: false, default_quest_disable_jei: false,
  default_quest_shape: 'circle', default_reward_team: false, detection_delay: I(20), disable_gui: false,
  drop_book_on_death: false, drop_loot_crates: false, emergency_items_cooldown: I(300), fallback_locale: 'en_us',
  grid_scale: D(0.5), hide_excluded_quests: false, icon: { id: 'dorohedoro:artificial_door_core' }, lock_message: '',
  pause_game: false, progression_mode: 'flexible', show_lock_icons: false, verify_on_load: false, version: I(13),
}) + '\n');
// FTB does not care about key order; sorting keeps diffs readable.
const langOut = '{\n' + Object.keys(lang).sort().map(k => `\t${k}: ${Array.isArray(lang[k]) ? snbt(lang[k], 1) : snbt(lang[k])}`).join('\n') + '\n}\n';
fs.writeFileSync(path.join(OUT, 'lang', 'en_us.snbt'), langOut);
// The install command reads this list to know which files are ours.
fs.writeFileSync(path.join(OUT, 'manifest.txt'), ['chapter_groups.snbt', ...CHAPTERS.map(c => 'chapters/' + c.file + '.snbt'), 'lang/en_us.snbt', 'data.snbt'].join('\n') + '\n');
console.log(`wrote ${CHAPTERS.length} chapters, ${total} quests, ${Object.keys(lang).length} lang entries to ${OUT}`);
