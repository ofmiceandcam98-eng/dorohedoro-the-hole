# Dorohedoro — Fauna and Geological Resource Batch 01

**Project:** Minecraft Java 1.21.1 / NeoForge 21.1.265.  **Status:** Source integrated + offline checks, not Minecraft compiled or play-tested.

## First original mob models (not named canon characters)

| ID | Behavior and design | Natural environment | Custom drops | Usable survival loop |
| --- | --- | --- | --- | --- |
| `dorohedoro:sewer_roach` | Original dark armored, six-legged roach voxel model; segmented shell, animated legs and antennae; 14 health, 3 attack, light armor; AI pursues players | Dark Hole Undercity / sewer-adjacent caves, depth -54 through 54 with biome-specific spawn weighting | Guaranteed 1–2 Reinforced Chitin; 35% Roach Antenna | Four chitin + four iron nuggets + stone bricks => four Chitin Plating blocks; two antennae + copper + redstone + iron/stick => Sewer Echo Probe with 12-block detection radius, reveals up to 24 nearby monsters for 12s, 30s cooldown, 64 durability |
| `dorohedoro:scrap_rat` | Original low-poly voxel rat with pink nose, ears, paw gait and moving tail; 8 health, runs from danger, no attacks | Hole Undercity underground (Y < 66); does not magically turn into a different species | Guaranteed 1–2 Fur and 1–2 Raw Rat Meat | Four fur => one wool; raw meat smelts/smokes to cooked meat; cooked meat + rock salt + bread => salted ration |

The roach is a **generic original species**, not Jonson. Both models are implemented as `HierarchicalModel` with `LayerDefinition` parts and custom 64×64 textures. Entities, renderers, attributes, biome spawn lists and spawn rules are registered. Confirm all these facts at runtime.

### Geology and original block textures

| Registered block | Natural location | Drop / processing | Direct player benefit |
| --- | --- | --- | --- |
| `dorohedoro:sulfur_ore` | Natural stone and deepslate, roughly Y -48..27 | Raw Sulfur (fortune/silk touch through ore loot) | 2 Sulfur + Charcoal + Redstone => 2 Gunpowder (noncanonical industrial adaptation); vanilla gunpowder unaffected |
| `dorohedoro:rock_salt_ore` | Natural stone and deepslate, roughly Y -8..48 | Rock Salt | Salted Rat Ration, potential later preserving/brine recipes |
| `dorohedoro:limestone` | Natural stone and deepslate, roughly Y -12..54 | Limestone block, can smelt to Lime Powder | Lime + stone bricks + gravel => additional Sewer Brickwork, cheap construction |
| `dorohedoro:chitin_plating` | Crafting, not an ore | Drops itself | Durable decorative survival-building masonry from roach drops |

Configured and placed features are added after existing vanilla ore features in every Hole biome so normal Minecraft diamonds, iron, redstone, copper, etc. stay available. Underworld sewers and natural caves are preserved. Item models, blockstates, cube models, custom 16×16 source art, entity textures, entity loot tables, and recipes are in the source/archive.

## Offline validation (executed 2026-10-31)

- `check_fauna_resources.py` — 93/93 registration / art / model / table / recipe / worldgen checks passed; all JSON parsed.
- Java 21 parser: 58 source files, 0 parsing errors.
- `./gradlew --no-daemon compileJava` **blocked**: Gradle 9.2.1 distribution cannot be downloaded in this execution environment; NO compiling or in-game behavior verified.

### Do not claim these have been tested until checked in Minecraft

1. Run `./gradlew --no-daemon runData`, `./gradlew --no-daemon build`, `./gradlew --no-daemon runClient` and `./gradlew --no-daemon runServer` **sequentially, never simultaneously**. Check compile errors and fix them from actual NeoForge 1.21.1 mappings.
2. On a newly created world, `/summon dorohedoro:sewer_roach` and `/summon dorohedoro:scrap_rat` (use `~ ~ ~` at a clear position). Inspect both original models, UVs, animation and hitboxes. Test AI goals; rats should flee and roaches should attack. If command IDs fail, fix registry binding before anything else.
3. Observe *natural* spawn rates in sewer/deep cave Undercity biome for many in-game minutes; profile category caps, heightmap placement and Y/rule interactions. Test both dark and well-lit sewers. Confirm none spawn in the Sorcerers' World or Hell from this biome list.
4. Kill specimens with normal weapons, Looting, Fire Aspect, and Silk Touch-relevant ore tooling. Confirm roach Chitin and Antennae, Rat Meat and Fur, correct count distribution. Ensure entity drops load from `data/dorohedoro/loot_table/entities/`.
5. Craft/use Chitin Plating, Sewer Echo Probe, wool, cooked rat, salted ration, industrial gunpowder, lime powder and sewer brickwork in survival; ensure all furnace/smoker recipes work with actual 1.21.1 recipe JSON.
6. Mine Sulfur, Rock Salt and Limestone at expected elevations with Silk Touch and Fortune; check correct textures, blockstate/item models, loot output, pickaxe requirements, vanilla ores and natural caves unaffected.
7. Check creative inventory visibility (tab includes new items). Spawn several hundred mobs over time and watch TPS, AI, despawning and any texture errors; test dedicated server with two connected clients and verify the models and drops synchronize.
8. On server restart, confirm items and entities persist appropriately, no broken food, recipes, or block references. Test existing-world load without changing saved city generator settings.

## Requirements for **all future** mobs and ores

Every new mob MUST receive (a) a distinct original 3D/voxel model, UV texture and animation, (b) purposeful behavior, biome, spawn rules and performance limits, (c) at least one actual thematic drop with a functional food, recipe, potion or equipment use, (d) balanced loot tables, (e) nonlethal alternatives for gentle animals where feasible, (f) no essential progression blocked behind killing named friendly characters, and (g) verified singleplayer/dedicated multiplayer tests. Every new ore/minable material MUST have unique block and item art, mineable tags, proper drops, recipes/processing and survival usefulness; do not remove standard Minecraft ores or cave systems.

## Planned next content batches (NOT implemented here)

- **Batch 02:** Sewer Leech (medical reagent), Glow Moth (renewable light/brewing), Oil Shale, Zinc and related alloys; verify API and design before coding.
- **Batch 03:** Sludge Crawler, Rust-Eater Beetle, Spore Crawler, Cavern Burrower, large cavern habitats. No placeholder loot or unfinished entities counted as done.
- Keep FTB Quests optional/on hold; do not move away from the three-dimensional survival sandbox goal.
