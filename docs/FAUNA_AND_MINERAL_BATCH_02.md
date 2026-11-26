# Fauna and mineral content — Batch 02

Target: Minecraft Java 1.21.1 / NeoForge 21.1.265. **Source integrated / offline validated, not compiled or runtime tested.**

## New Minecraft mobs (original designs, not canon named creatures)

| Mob ID | Model and behavior | Survival reward | Mechanics / implementation |
|---|---|---|---|
| `dorohedoro:sewer_leech` | Segmented burgundy leech with short front feelers and a broad sucker. Original 64×64 texture and animated `HierarchicalModel`. Hostile sewer-edge predator (8 HP; 1.5 base attack) | Always 1–2 Leech Mucus; 45% Leech Sucker | Spawn only in Undercity, between -54 and 54, in dark areas near water. Brief Slowness (3 seconds) when successfully striking. Mucus + paper + honey -> **Medical Salve** (5 seconds Regeneration I); Suckers + mucus -> slimeball; mucus also needed for inexpensive industrial **Sewer Sealant**. |
| `dorohedoro:glow_moth` | Olive/pale-green subterranean moth with two flapping wings, light accents and antennae; original 64×64 atlas. Peaceful, inherits vanilla bat ceiling/flying movement | Always 1–2 Luminescent Scales; 60% Moth Silk | Undercity ambient spawns Y -57…34 under light 8. **Shears** harvest 1–2 scales without harming the moth, cooldown 6000 ticks (5 min), persisted in NBT. 2 Scales + honey + glow berries -> **Cavern Glow Tonic** (Night Vision I 90 seconds); scales + brass + iron + panes + redstone -> **Cavern Lamp** (light level 13); 2 Silk -> 2 string. |

Monster loot tables live under `src/main/resources/data/dorohedoro/loot_table/entities/`. These are extra gameplay species, not replacements for Jonson or the Gyoza Fairy. Native spawn placements, renderer + layer registration, sound defaults and dedicated-server isolation still need Minecraft tests.

## Geological materials

| Ore | Worldgen | Processed outputs | Why mine it |
|---|---|---|---|
| Zinc Ore | Overworld Hole biome veins, 5 attempts/chunk between Y -46 and 32, size 7 | Raw Zinc -> Zinc Ingot (furnace or blast furnace) -> 2 Brass (with one Copper) or Galvanized Plating | Durable industrial architecture, progression toward renewable Cavern Lamps and later valves |
| Oil Shale Ore | Overworld Hole biome veins, 5 attempts/chunk between Y -24 and 38, size 9 | Oil Shale Chunk -> Bitumen (furnace) -> Sewer Sealant | Long-term machinery waterproofing/industrial buildings, with extra non-mob recipe through slime balls and clay |

The two ore features use the same stone + deepslate replaceable block tags as existing new ores; normal cave noise, vanilla diamonds and other ore features are not modified. Original 16×16 textures for each block and every new item have been added. Generated-resource snapshots are included because this environment cannot run `runData`; actual generated outputs should replace them after a successful `runData` on the developer machine.

### Critical build/testing notes

1. Run **sequentially**: `./gradlew --no-daemon runData`, `./gradlew --no-daemon build`, `./gradlew --no-daemon runClient`, `./gradlew --no-daemon runServer`. Capture any API errors and fix source. Do not treat parser checks as a compile.
2. Fresh Hole save `/summon dorohedoro:sewer_leech ~ ~ ~` near water and `/summon dorohedoro:glow_moth ~ ~ ~` in a tall underground chamber; examine texture/model UVs, animations, drops, AI, water-adjacency/light spawn constraints, hitboxes and TPS. Confirm leech slowing and moth flying.
3. Right-click glow moth holding shears; check 1–2 scale acquisition, 6000 tick cooldown, no duplicate on reconnect/restart and no infinite farming.
4. Test Medical Salve and Cavern Glow Tonic at full health/hunger: the first applies Regeneration (100 ticks), second applies Night Vision (1800 ticks); ensure they consume normally and do not grant unfair permanent effects. Test lamp light 13 on client/server and correct break/drop; test alternate slime recipe without mobs.
5. Mine Zinc and Oil Shale in both stone/deepslate at intended Y; verify Fortune and Silk Touch, raw drops, blast/furnace/normal progression, recipe book and creative tab; test quartz, iron, diamonds and normal mining remain unchanged.
6. Confirm all four custom mobs only spawn in supported Hole underground biomes, not in unrelated worlds, Sorcerers' World or Hell, and their rates are reasonable; evaluate idle AI and server performance.
7. Existing worlds retain 30% sewer cave frequency profile and existing portal materials. Do not claim no seams or correct in-world save compatibility until tested.

## API research

Grounded in NeoForge 1.21.1 official recipe docs and 1.21.1 javadocs for Bat (subclassing vanilla flight), spawn placements, `FoodProperties.Builder.effect`, custom entity layers, and 1.21.1 loot tables. These references inform source choices; client rendering, registry loading and datapack parsing require the actual game runtime to confirm.
