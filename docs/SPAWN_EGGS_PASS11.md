# Pass 11 — All eight mob spawn eggs (Minecraft Java 1.21.1 / NeoForge 21.1.265)

## Implementation

Eight `DeferredSpawnEggItem` registrations live in `ModItems.java`, using entity-type suppliers declared in `ModEntities.java`. This is the deferred constructor supplied by NeoForge **1.21.1**, not the later Minecraft/NeoForge data-component recipe. `DeferredSpawnEggItem` handles the standard spawn egg tint and default behavior; each egg uses vanilla's `minecraft:item/template_spawn_egg` item model with an original color pair to identify its mob.

All eight appear in three Creative tab contexts: the dedicated `The Hole: Creatures` tab, the existing `The Hole` tab, and vanilla's `Spawn Eggs` tab (via `BuildCreativeModeTabContentsEvent`). Eggs intentionally have **no Survival crafting recipes**, since they are testing tools, not new progression.

## How to test in-game

1. Extract this source package and build with JDK 21 on a computer with access to the proper Gradle and NeoForge dependencies. Open a terminal in the project root.
2. On Windows run `gradlew.bat runData` and `gradlew.bat runClient`; or `gradlew.bat build` and install the resulting JAR in Minecraft Java **1.21.1 / NeoForge 21.1.265**.
3. Create a disposable **Creative-mode test world**. Open inventory (E), select `The Hole: Creatures` or Minecraft's Spawn Eggs tab. Search for `Spawn Egg` as needed.
4. Place eggs on open ground or in a safe test enclosure. For the Glow Moth, leave room above the floor for its flight. Use a dark cavern to see real moving block-light behavior.
5. To obtain a particular egg using commands, run `/give @s dorohedoro:<mob_id>_spawn_egg`; example: `/give @s dorohedoro:glow_moth_spawn_egg`.
6. To test base entity registrations even without an egg, run `/summon dorohedoro:<mob_id>`, example: `/summon dorohedoro:rust_eater_beetle`.
7. Keep eggs out of normal Survival recipes and loot tables.

## Full item IDs and egg colors

| Mob | ID | Base | Spots |
|---|---|---|---|
| Sewer Cockroach | `dorohedoro:sewer_roach_spawn_egg` | `#453023` | `#BC8350` |
| Scrap Rat | `dorohedoro:scrap_rat_spawn_egg` | `#786B64` | `#D69AA5` |
| Sewer Leech | `dorohedoro:sewer_leech_spawn_egg` | `#54283B` | `#D58B9B` |
| Cavern Glow Moth | `dorohedoro:glow_moth_spawn_egg` | `#829982` | `#EDF4C5` |
| Sludge Crawler | `dorohedoro:sludge_crawler_spawn_egg` | `#435443` | `#B1B96F` |
| Rust-Eater Beetle | `dorohedoro:rust_eater_beetle_spawn_egg` | `#654B3B` | `#CB905A` |
| Cavern Burrower | `dorohedoro:cavern_burrower_spawn_egg` | `#4D5353` | `#A98A66` |
| Spore Crawler | `dorohedoro:spore_crawler_spawn_egg` | `#534364` | `#ADC681` |

## Live validation status

Source added and resource checks can run offline, but **the full NeoForge build and real egg spawning have not been verified here**. Before calling this done, check all eight mob types, both Creative tab memberships, spawner usage, and their animations and lighting in a real Minecraft client.

API reference: https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/common/DeferredSpawnEggItem.html
