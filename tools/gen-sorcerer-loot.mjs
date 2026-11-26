// Writes the Sorcerers' World settlement loot tables.
//   node tools/gen-sorcerer-loot.mjs
// Output: src/main/resources/data/dorohedoro/loot_table/chests/sorcerer_town/*.json
// Loot is what the room would hold. Smoke goods stay scarce, nothing outruns vanilla progression, emeralds stay useful.
import { mkdirSync, writeFileSync } from 'node:fs';

const dir = 'src/main/resources/data/dorohedoro/loot_table/chests/sorcerer_town';
mkdirSync(dir, { recursive: true });

const e = (name, weight, min = 1, max = 1) => {
  const entry = { type: 'minecraft:item', name, weight };
  if (max > 1 || min > 1) entry.functions = [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min, max } }];
  return entry;
};
const pool = (min, max, entries) => ({ rolls: { type: 'minecraft:uniform', min, max }, entries });
const table = (name, pools) => writeFileSync(`${dir}/${name}.json`, JSON.stringify({
  type: 'minecraft:chest', pools, random_sequence: `dorohedoro:chests/sorcerer_town/${name}`,
}, null, 2) + '\n');

const coins = [e('minecraft:emerald', 6, 1, 3), e('minecraft:iron_nugget', 8, 2, 6), e('minecraft:gold_nugget', 5, 1, 4)];

table('house', [
  pool(2, 5, [e('minecraft:bread', 10, 1, 3), e('minecraft:baked_potato', 6, 1, 3), e('minecraft:apple', 6, 1, 3),
    e('minecraft:string', 5, 1, 3), e('minecraft:paper', 6, 1, 4), e('minecraft:candle', 4, 1, 2),
    e('minecraft:book', 3), e('minecraft:leather', 3, 1, 2), e('minecraft:white_wool', 3, 1, 2), e('minecraft:coal', 5, 1, 4)]),
  pool(0, 1, coins),
]);
table('pantry', [
  pool(2, 5, [e('minecraft:wheat', 8, 2, 6), e('minecraft:potato', 8, 1, 5), e('minecraft:carrot', 8, 1, 5),
    e('minecraft:beetroot', 5, 1, 4), e('minecraft:egg', 5, 1, 3), e('minecraft:sugar', 4, 1, 3),
    e('minecraft:bowl', 4, 1, 3), e('minecraft:dried_kelp', 4, 2, 6), e('dorohedoro:rock_salt', 4, 1, 3)]),
]);
table('kitchen', [
  pool(3, 6, [e('minecraft:cooked_porkchop', 6, 1, 3), e('minecraft:cooked_chicken', 6, 1, 3), e('minecraft:bread', 8, 1, 4),
    e('minecraft:mushroom_stew', 4), e('minecraft:beetroot_soup', 3), e('minecraft:wheat', 6, 2, 5),
    e('minecraft:bowl', 5, 2, 4), e('minecraft:coal', 5, 2, 6), e('minecraft:charcoal', 4, 1, 4), e('dorohedoro:rock_salt', 4, 1, 4)]),
  pool(0, 1, coins),
]);
table('mask_shop', [
  pool(3, 6, [e('minecraft:leather', 10, 1, 4), e('minecraft:string', 8, 2, 5), e('minecraft:paper', 6, 2, 6),
    e('minecraft:bone', 5, 1, 3), e('minecraft:black_dye', 5, 1, 3), e('minecraft:white_dye', 5, 1, 3),
    e('minecraft:red_dye', 4, 1, 3), e('minecraft:purple_dye', 3, 1, 2), e('minecraft:carved_pumpkin', 2),
    e('minecraft:shears', 2), e('dorohedoro:moth_silk', 3, 1, 3)]),
  pool(1, 2, coins),
]);
table('smoke_parlor', [
  pool(2, 4, [e('minecraft:glass_bottle', 10, 1, 4), e('dorohedoro:empty_smoke_flask', 5, 1, 2),
    e('dorohedoro:smoke_soaked_filter', 3), e('minecraft:glowstone_dust', 3, 1, 3), e('minecraft:redstone', 3, 1, 4),
    e('minecraft:blaze_powder', 1), e('minecraft:nether_wart', 2, 1, 3), e('dorohedoro:smoke_residue', 1)]),
  pool(1, 2, coins),
]);
table('workshop', [
  pool(3, 6, [e('minecraft:iron_ingot', 6, 1, 4), e('minecraft:copper_ingot', 7, 2, 6), e('dorohedoro:zinc_ingot', 4, 1, 3),
    e('dorohedoro:brass_ingot', 3, 1, 2), e('dorohedoro:scrap_metal', 8, 2, 6), e('minecraft:chain', 4, 1, 3),
    e('minecraft:iron_pickaxe', 1), e('minecraft:iron_shovel', 1), e('minecraft:shears', 2), e('minecraft:flint_and_steel', 2),
    e('minecraft:coal', 6, 2, 8), e('minecraft:lantern', 2)]),
  pool(0, 1, coins),
]);
table('clinic', [
  pool(2, 5, [e('dorohedoro:medical_salve', 6, 1, 2), e('dorohedoro:antidote_poultice', 5, 1, 2), e('minecraft:paper', 6, 1, 4),
    e('minecraft:glass_bottle', 6, 1, 3), e('minecraft:string', 5, 1, 3), e('minecraft:honey_bottle', 3),
    e('minecraft:golden_carrot', 1), e('minecraft:white_wool', 4, 1, 3), e('dorohedoro:lime_powder', 3, 1, 3)]),
  pool(0, 1, coins),
]);
table('study', [
  pool(2, 5, [e('minecraft:book', 8, 1, 3), e('minecraft:paper', 8, 2, 6), e('minecraft:feather', 5, 1, 3),
    e('minecraft:ink_sac', 5, 1, 3), e('minecraft:writable_book', 3), e('minecraft:map', 3), e('minecraft:compass', 2),
    e('minecraft:clock', 1), e('minecraft:candle', 4, 1, 3), e('minecraft:amethyst_shard', 2, 1, 3)]),
  pool(1, 2, [e('minecraft:emerald', 6, 2, 5), e('minecraft:gold_ingot', 3, 1, 2), e('minecraft:gold_nugget', 5, 2, 6)]),
]);
table('shanty', [
  pool(1, 4, [e('minecraft:rotten_flesh', 4, 1, 3), e('minecraft:stick', 8, 2, 6), e('minecraft:string', 5, 1, 3),
    e('minecraft:bone', 4, 1, 2), e('minecraft:potato', 6, 1, 3), e('minecraft:coal', 4, 1, 3), e('minecraft:bowl', 3),
    e('dorohedoro:scrap_metal', 6, 1, 4), e('minecraft:iron_nugget', 5, 1, 4), e('minecraft:leather', 2)]),
]);
table('lodging', [
  pool(1, 4, [e('minecraft:bread', 8, 1, 3), e('minecraft:paper', 5, 1, 3), e('minecraft:map', 3), e('minecraft:candle', 4, 1, 2),
    e('minecraft:string', 4, 1, 2), e('minecraft:white_wool', 4, 1, 2), e('minecraft:compass', 1), e('minecraft:torch', 6, 2, 6)]),
  pool(0, 1, coins),
]);
table('market', [
  pool(3, 6, [e('minecraft:melon_slice', 6, 2, 6), e('minecraft:pumpkin', 3), e('minecraft:carrot', 7, 2, 6),
    e('minecraft:potato', 7, 2, 6), e('minecraft:wheat', 7, 2, 6), e('minecraft:cod', 5, 1, 4), e('minecraft:salmon', 4, 1, 3),
    e('minecraft:sugar_cane', 4, 1, 4), e('minecraft:egg', 4, 1, 4), e('minecraft:wheat_seeds', 5, 2, 6),
    e('minecraft:beetroot_seeds', 3, 1, 4), e('dorohedoro:rock_salt', 3, 1, 3)]),
  pool(0, 1, coins),
]);
console.log('wrote 11 loot tables to ' + dir);
