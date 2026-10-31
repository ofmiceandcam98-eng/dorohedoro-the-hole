# FTB Quests integration directive

Digest of the owner's FTB Quests master prompt, received 2026-10-31. **Status: deferred. Do not start until the owner asks.** It adds to the sandbox directive and replaces nothing.

## Rules

- **Optional.** Quests are a guide, achievement tracker and resource reward system. Players can ignore them completely. They must never gate crafting, mining, building, farming, redstone, recipes, doors, dimensions, Devil Gates, trading or innate abilities.
- **Credit organic play.** A player who activates an Artificial Door before opening the book should still get credit where technically feasible. Where FTB does not backfill, add a reliable state check or document the limit.
- **Use the real mod.** This project is Java on NeoForge 1.21.1, so the Java FTB Quests applies, not the Bedrock Marketplace add-on. Do not mix SNBT with Bedrock script events. Do not invent API calls. Read the docs and code for the exact target version first.
- **Dependencies:** FTB Quests, FTB Library, FTB Teams, plus whatever the chosen releases need. Evaluate FTB XMod Compat, KubeJS, JEI or REI, FTB Filter System and FTB Chunks, and do not install anything unnecessary. Do not bundle FTB binaries or classes against their licence.
- **Packaging.** Quest data lives under `config/ftbquests/quests/`. Do not assume SNBT inside the mod jar loads automatically. Provide a modpack layout, a dedicated-server install method, and a dependency manifest.
- **Stable quest IDs** so updates do not invalidate progress.
- **Real IDs only.** Check every item, block, biome, entity, structure, advancement and dimension against the actual registries. If something does not exist, implement it properly or defer the quest. No uncompletable objectives.
- **Use the stock FTB UI** with supported theming: rusted industrial textures, dirty concrete and metal, smoke motifs, black, gray, dark red and industrial orange, skull, mask, mushroom, heart and door iconography. No separate GUI. Original or licensed art only.

## Chapters

1. Welcome to the Hole: scrap lumber, table, tools, stone, furnace, cooking, coal, bed, shelter, storage.
2. Surviving the Streets: abandoned building, scrap, urban biome, medical facility, protection, sewers, weapon.
3. Industrial Survival: iron, copper, redstone, hopper, storage, machine, automated furnace, redstone door.
4. Farming and Cooking: seeds, farmland, harvest, cooking station, gyoza, fishing, food farm, sapling, renewable food. No rare magic ingredients for basic food.
5. The Underground: depth, caves, minerals, diamonds, rare resources, underground structure, industrial tunnel. Every task must match real generation.
6. Building a Life: materials, furniture, storage, lighting, farm, reinforced blocks, redstone, automation. No arbitrary house shape.
7. Magic and Smoke: magic material, smoke processing, Smoke Condenser, mask, component, device. Sorcerer-only quests get alternatives or are marked optional. Rewards never grant biological abilities to humans.
8. Doors Between Worlds: door materials, condenser, core, frame, activation, temporary Smoke Door, Sorcerers' World, return, linked pair, second portal. Track real construction and activation. Holding a core does not count.
9. Sorcerers' World: enter, settlement, local materials, trade, biomes, landmark, base, resources.
10. Hydra Forest: discover, timber, saplings, grow a tree, woodland structures, Haru's former cabin if present. No attacking nonhostile characters.
11. Masks and Equipment: mask, protective gear, repair, weapon upgrade, advanced material, crafting station.
12. Combat and Survival: hostile mobs, dangerous encounter, armor, repair, hostile sorcerer, Living Dead Day if enabled, advanced challenges. No named nonhostile kills.
13. Commerce and Industry: trade, sell, bulk production, automated farm, machinery, repeatable production.
14. Hell: Devil Gate materials, Infernal Door Core, assemble, activate, enter, biome, resources, shelter, return safely.
15. Mastery and Sandbox Challenges: bases in all three dimensions, several portals, large automation, tree farm, advanced gear, food farm, rare structures, combat milestones, rare collection, long-term co-op. Mostly independent, with no single ending.

## Tasks, dependencies and rewards

- Prefer native task types: item, submission, kill, advancement, dimension, biome, structure, location, stats, XP, observation. Custom tasks only when needed, through stable FTB API or KubeJS with FTB XMod Compat.
- Custom triggers wanted: door constructed, door activated, Smoke Door used, safe return from Hell, custom tree grown, innate ability used, industrial process completed. Server-authoritative, success only, no duplicates, event-based rather than per-tick scans.
- Dependencies organize the book and never lock the game. Example branches: scrap wood to table to furnace to iron to machinery; smoke materials to condenser to core to activation to Sorcerers' World; Devil materials to gate to Hell to Hell resources.
- Rewards: food, medical items, torches, scrap, iron, copper, blocks, saplings, seeds, ingredients, XP, modest equipment, decoration. Rare items are sparse, no routine diamonds, and no rewards that make mining or trading pointless.
- Repeatables for scrap, crops, food, trading, ores, mobs and manufactured goods, with cooldowns. No loops that return more of an item than they consume.

## Multiplayer

FTB Teams: shared progress, individual and team rewards as appropriate, membership changes handled, no duplicate claims from simultaneous clicks, safe across restarts, and no team hopping or PvP exploit for infinite rewards. Document the difference between team progress and reward ownership.

## Testing

Verify in singleplayer and on a dedicated server with the real installed version: FTB loads, book opens, chapters show, all quest IDs valid, no references to missing items or biomes, dimension, structure and item tracking work, crafting objectives work, portal events count only real successes, Smoke Door, Artificial Door and Hell entry are recognized, forestry objectives work, rewards claim, repeatables reset, reward loops cannot duplicate valuables, team sharing and reward separation work, progress survives restarts and book updates, a player can ignore every quest, no system is gated, and packaging is correct.

## Deliverables

First a working foundation of 20 to 30 quests, expanded only after verification, reaching about 150 to 200 quests. Files: quest configuration, chapters, localization, custom task code or scripts, reward definitions, icons and themes, dependency manifest, install instructions, `FTB_QUESTS_INTEGRATION.md`, `FTB_QUESTS_TEST_RESULTS.md`, updated `HANDOFF.md` and `IMPLEMENTATION_STATUS.md`. A chapter is not complete because its title and icon exist.

## Sources named by the owner

docs.feed-the-beast.com/mod-docs/mods/suite/Quests, github.com/FTBTeam/FTB-Quests, github.com/FTBTeam/FTB-XMod-Compat, the CurseForge FTB Quests pages, kubejs.com/wiki/addons/ftb-xmod-compat, and the FTB Bedrock add-on docs for reference only.
