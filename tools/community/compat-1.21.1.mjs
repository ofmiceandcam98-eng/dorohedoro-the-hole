// Maps blocks from newer Minecraft versions to their closest 1.21.1 equivalents, keeping orientation.
// remap(name, props) returns { name, props } or null (drop the block). Every target is a vanilla 1.21.1 block.
const keep = (props, keys) => Object.fromEntries(keys.filter((k) => k in props).map((k) => [k, props[k]]));

export function remap(name, props) {
  const wood = (n) => n.replace('minecraft:pale_oak', 'minecraft:birch');
  // Shelves (1.21.9) become open trapdoors: a thin board against the same side of the block.
  const shelf = name.match(/^minecraft:(\w+)_shelf$/);
  if (shelf) {
    const woodName = shelf[1] === 'pale_oak' ? 'birch' : shelf[1];
    return { name: `minecraft:${woodName}_trapdoor`,
      props: { facing: props.facing || 'north', half: 'top', open: 'true', powered: 'false', waterlogged: props.waterlogged || 'false' } };
  }
  // Pale oak (1.21.4) becomes birch, the closest pale wood in 1.21.1.
  if (name.startsWith('minecraft:pale_oak')) return { name: wood(name), props };
  if (name === 'minecraft:pale_moss_block') return { name: 'minecraft:moss_block', props: {} };
  if (name === 'minecraft:pale_moss_carpet') return { name: 'minecraft:moss_carpet', props: {} };
  if (name === 'minecraft:pale_hanging_moss') return null;
  // Copper bars and chains (1.21.9) become iron bars and chains.
  if (/^minecraft:(waxed_)?(exposed_|weathered_|oxidized_)?copper_bars$/.test(name)) {
    return { name: 'minecraft:iron_bars', props: keep(props, ['east', 'north', 'south', 'west', 'waterlogged']) };
  }
  if (/^minecraft:(waxed_)?(exposed_|weathered_|oxidized_)?copper_chain$/.test(name)) {
    return { name: 'minecraft:chain', props: keep(props, ['axis', 'waterlogged']) };
  }
  if (/^minecraft:(waxed_)?(exposed_|weathered_|oxidized_)?copper_lantern$/.test(name)) {
    return { name: 'minecraft:lantern', props: keep(props, ['hanging', 'waterlogged']) };
  }
  if (/^minecraft:(\w+_)?copper_torch$/.test(name) || name === 'minecraft:copper_wall_torch') {
    return { name: name.includes('wall') ? 'minecraft:wall_torch' : 'minecraft:torch', props: keep(props, ['facing']) };
  }
  // Signs and shelves carried text or items as block entities; text is not converted, so signs used as trim stay blank.
  return { name, props };
}

/** Blocks that are new after 1.21.1 and must never reach the output. */
export const NEWER_THAN_1_21_1 = /(_shelf|pale_oak|pale_moss|pale_hanging_moss|copper_bars|copper_chain|copper_lantern|copper_torch|resin|creaking|eyeblossom|leaf_litter|bush$|firefly_bush|cactus_flower|dried_ghast|wildflowers)/;
