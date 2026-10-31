# Sandbox directive (supersedes the earlier master prompt where they conflict)

Digest of the owner's "Complete Minecraft Sandbox Conversion" prompt, received 2026-10-31. `CLAUDE_MASTER_PROMPT.md` still applies wherever this does not contradict it, including its canon rules, anti-exploit rules and honesty rules.

## What changed

- **Not a story mod.** No mandatory campaign, quests, chapters or scripted progression. The earlier quest framework and campaign are dropped as required content. Any story content must be optional and separate.
- **Three genuine, infinite, persistent dimensions:** the Hole (the Overworld), the Sorcerers' World, and Hell. Hell is no longer a late optional instance.
- **Portals are top priority**, ahead of NPCs and magic.
- **Sorcerers' World weather is dry.** No random rain or snow. Humans can live there without gaining magic.
- **Hell must not look like the Nether**, and is independent of it in terrain, mobs, architecture and portals.

## Phase order

1. Project audit. 2. Hole sandbox foundation. 3. Sorcerers' World and Hell generation. 4. Portals: Smoke Door, Artificial Door, En Family Door, Devil Gate. 5. Resources and crafting. 6. Structures, NPCs, environment. 7. Combat and magic. 8. Optimization and testing.

## Hard rules

- Every Minecraft system keeps working: blocks, crafting, survival, exploration, farming, redstone, multiplayer. Adapt appearance, never remove capability.
- All players start in the Hole. Any essential resource must be renewable or reliably repeatable. No permanent stranding.
- All portals share one server-authoritative backend. Teleports validate that the destination is loaded and safe, and preserve inventory, health and passengers where supported.
- No progression material may be obtainable only by killing innocent NPCs or only by killing a named Devil. Nothing needed for a first portal may be found only in the destination.
- Portals respect protection, cannot be used to bypass bases or claims, and redstone cannot bypass activation costs or access rules.
- Chidaruma must not be a conventional farmable boss. Devils must not be reskinned zombies.
- Magic never auto-grants on dimension entry. Sorcerers have one innate type.
- No block transformations that can corrupt chunks.

## Hole additions

Rare vegetation only: dead and struggling trees, bushes, weeds, weighted to abandoned lots, courtyards and outskirts, never forests. Add outer urban districts and sparse rural-industrial terrain with natural transitions. Avoid repetitive grids. Wood must not depend on rare trees: furniture, pallets, crates, scrap lumber and traders, plus saplings for tree farms. Underground: stone layers, ore, caves, waterways, sewers, tunnels, hazards, without huge openings under every foundation. Landmarks: Hungry Bug, the Hospital, Kasukabe's laboratory, Lake of Refuse. Rain weakens sorcerers but not ordinary humans to the point of impossibility.

## Sorcerers' World content

Gothic-industrial cities, Zagan City, residential districts, En's Mansion, sorcerer schools, poor districts, markets, Devil Churches, manufacturing, Hydra Forest (with Haru's cabin and Cross-Eyes structures), rural woodland, roads and countryside, gardens, and Mastema's mushroom-devastated region. Not wall-to-wall giant mushrooms. Full forests with saplings and renewable wood. Caves, stone, minerals, ruins, hazards. Magic-reactive minerals are allowed as labelled adaptations.

## Hell content

Hostile and barren: horn-shaped mountains, wastelands, immense rock formations, caverns, unusual stone, giant Devil structures, soul-associated areas. No natural forests. Building, mining and storage must still work, and any dimension-specific rule must be documented and configurable.

## Portal specification

**Shared record per portal:** unique ID, type, owner or faction, source and destination dimension, source and destination coordinates, state, activation cost, lifetime, access restrictions, linked return portal, persistence and cooldown data. Anchors are one-to-one, not Nether 8:1. Prevent duplicate destinations and loops. Provide a safe default destination.

**Type A, Smoke Door.** Sorcerer ability. Target a valid surface, spend smoke, spawn an animated door. Validate space, stable floor, destination dimension, smoke, protection and portal conflicts. Suggested defaults, all configurable: high capacity requirement, significant activation cost, about 90 seconds active, short cooldown. Weak sorcerers cannot make independent doors. Connects the Hole and Sorcerers' World with a paired exit while active. Visual styles by owner: Shin heart, hammer and stitches; Noi bandages; En mushrooms; Ebisu skull; Nikaido cassette. Custom styles for player sorcerers.

**Type B, Artificial Door.** For humans. Required content: Reinforced Door Frame, Smoke Condenser, Smoke Conduit, Artificial Door Core, Door Activation Tool, Door Linking Key, animated door block or entity, recipes and loot. Proposed recipes, marked as gameplay adaptations:

```text
Smoke Condenser      I R I / C S C / I R I   I iron ingot, R redstone dust, C copper ingot, S Smoke Residue
Artificial Door Core B C B / I M I / B R B   B bone block, C Smoke Condenser, I iron ingot, M Preserved Smoke Conduit, R redstone block
```

Smoke Residue comes from sorcerer-related drops, abandoned laboratories or traders. Frame is 4 wide by 5 tall with a 2 by 3 opening and the core in the bottom edge:

```text
F F F F
F . . F
F . . F
F . . F
F C F F
```

Sequence: craft parts, build frame, install core, activate with tool, smoke emits and a physical door forms, the exit is created or located, walk through. Persistent across restarts, saved IDs and links, several per world, optional ownership and access control. Must not look like a Nether portal.

**Type C, En Family Door.** Naturally generated interactive structure in the Sorcerers' World, repeatable travel to the Hole, guarded by reputation but never the only route.

**Type D, Devil Gate.** Reaches Hell. Parts: Devil Sigil, Etched Gate Stone, Infernal Door Core, ritual tool, reinforced frame blocks. Example: 5 by 5 outer frame, 3 by 3 interior. Sigil comes from rare loot, bartering, exploration finds, or renewable high-cost trade, never only from killing a named Devil. Not consumed by routine use unless a harder mode is configured. Safe landing area, connected return gate, interaction or cooldown against loops. All materials for the first gate must be obtainable before entering Hell.

## Economy and systems

Vanilla ore progression worthwhile in all three dimensions. All wood types functional, scrap wood craftable into basic wooden materials. Gyoza is a repeatable useful food. Currency abstraction, repeatable trades, no dialogue chains. Trees are real features with saplings, growth, bone meal, leaf decay and farming. Vanilla fantasy mobs do not auto-spawn in inappropriate regions; provide replacements or configurable vanilla spawning with resource alternatives. NPCs are inhabitants: routines, trading, restocking, workplaces, threat reactions, factions. Structures are not indestructible.

## Modes and options

- **Dorohedoro Immersion Mode** (default): the three realms are primary, and essential materials stay available by alternate routes.
- **Minecraft Compatibility Mode:** Nether and End stay available with normal vanilla progression.

Configurable: world size and biome weights, urban and tree density, dimension difficulty, hostile sorcerer rates, vanilla mob spawning, smoke availability, portal costs and lifetimes, Hell entry difficulty, building protection, PvP, explosions, NPC griefing, respawn rules, keep inventory, mob drops, resource generation, weather, Nether and End compatibility. Existing gamerules are honoured, not duplicated.

## Required documents

`README.md`, `HANDOFF.md`, `LORE_REFERENCE.md`, `WORLD_GENERATION.md`, `PORTAL_SYSTEM.md`, `SURVIVAL_BALANCE.md`, `CRAFTING_RECIPES.md`, `IMPLEMENTATION_STATUS.md`, `TEST_RESULTS.md`. Include exact recipe, block, item and dimension IDs, and mark canon versus adaptation. Update the handoff after every major feature.

## Lore references given

viz.com/dorohedoro, and the Dorohedoro fandom wiki pages Magic_User_Door, En_Family_Door, Hydra_Forest, Hell, Magic and Category:The_Magic_User_World.
