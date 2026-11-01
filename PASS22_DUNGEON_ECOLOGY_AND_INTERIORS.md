# Pass 22 — Sewer dungeon, creature ecology and furnished interiors

**Date:** 2026-11-01  
**Base:** Pass 21.5 full source, preserving all previous PNGs and gameplay systems.  
**Status:** SOURCE-INTEGRATED, **NOT COMPILED OR IN-GAME TESTED**.

## Sewer dungeon gameplay / geography

- Versioned dungeon additions: `sewer_cavern_version=2` only for new worlds; old saved worlds retain original profile `0`. The original street-access shafts, central sewages, cisterns, galleries and cave branches remain; dungeon wing is an optional connected offshoot.
- One predictable discoverable entrance near starter intersection (subject to shaft availability), others are deterministic, uncommon selections at shaft intersections.
- Layout: dry sewer entry -> central pump hub -> north machine workshop / south filtration plant / west encounter vault. A miner can uncover a hidden breakable service cache; no impossible key gate.
- Readable practical roles: repair/salvage (tools, copper, rails); hazardous filtration (filters, residues); defended late cache (advanced supplies). Three new loot tables.
- Three vanilla Minecraft spawners per dungeon variant, using correctly structured block entity NBT. Ambient categories spawn one at a time; hostile categories two with nearby caps. Breakable and player-proximity activated.
- Dungeon variants (pump / filters / vault):
  1. Scrap Rat / Sewer Leech / Sewer Cockroach
  2. Rust-Eater Beetle / Sludge Crawler / Cavern Burrower
  3. Glow Moth / Sewer Leech / Spore Crawler
  4. Sewer Cockroach / Sludge Crawler / Spore Crawler

## All eight existing creatures spawn naturally in the world

Natural tables are present for **all eight** mobs in all nine Hole biomes (eight surface-district biomes and the undercity); surface spawn predicates gate them to appropriate locations. This is necessary because the existing biome source changes to undercity only below Y=40 and the new dungeon floor is around Y=49. Relying only on `hole_undercity` would miss the dungeon.

Surface habitats:

| Creature | Above-ground biomes / behavior |
|---|---|
| Sewer Cockroach | Residential, market, industrial, waterfront, derelict, frost industrial; light-limited hostile |
| Scrap Rat | Every city district, often markets and industrial; ambient |
| Sewer Leech | Wet waterfront locations near water; hostile |
| Glow Moth | Low-light industrial, waterfront, derelict, graveyard and outskirts; ambient |
| Sludge Crawler | Industrial, waterfront, frost industrial; hostile |
| Rust-Eater Beetle | Industrial, derelict and outskirts scrap; non-hostile |
| Cavern Burrower | Derelict and outskirts, dark conditions; hostile |
| Spore Crawler | Derelict and graveyard, dark conditions; hostile |

Underground (roughly Y=-54..54): all eight have registrations in the nine Hole biome tables, with water required for natural leech spawns. Spawners are a supplemental, explicit dungeon population mechanism. Spawn categories, caps, lighting and fluid rules still need real gameplay assessment.

## More interior furniture (new worlds only)

`world_detail_version=9` calls `InteriorDetails22` through existing `InteriorLayouts15`: expanded apartment lobbies, residences, shops, clinics and industrial halls, with counters, shelves, appliances, small workstations and lived-in accents. Placement is deterministic, is clipped per chunk, tests empty spaces and requires a non-air block beneath furniture. Existing beds, doors, loot and room boundaries are not replaced. No new furniture assets are fabricated; all use recognized vanilla Minecraft blocks at current visual quality.

## Checks completed in this environment

- `python tools/qa-pass22-dungeon-ecology.py`: **117 / 117** structural checks pass, including a schematic room-connection flood fill and all eight mob references in all nine Hole biome data files.
- `java -cp tools ParsePass22` (after compiling helper with Java 21): **131 Java sources parsed with zero syntax errors** (not type-checked).
- `python tools/qa-highquality-mobs.py`: **1007 source/asset checks** pass.
- **396 JSON resources** parse correctly, including 9 biome files, 3 new loot tables.
- Original Pass15/early sewer QA scripts have outdated strict assertions (`world_detail_version` equal to 4, sewer_cavern_version equal to 1, and 16x16 PNG sizes). Their older failures do **not** indicate new runtime failure; use the dedicated Pass22 suite for this updated profile. Some early fauna scripts also assume 16x16 item textures, which were expanded to 32x32 in Pass21.
- **NOT verified:** NeoForge compile, runData registry bootstrap, real NPC/mob spawn rates, spawner block entity ticking, world generation appearance, entity collision/pathfinding, chest content, portal travel, in-game lighting, server performance, or dungeon survival playtesting.

## Required next testing on a Java 21 Windows Minecraft dev installation

1. Back up all worlds and previous source; unpack this version separately.
2. Run the pinned vanilla 1.21.1 noise generator if instructed by the realm handoff; then `gradlew.bat --no-daemon runData` and `gradlew.bat --no-daemon build` (sequential, not simultaneous). Check compile against NeoForge 21.1.265.
3. On a **new save** check shafts lead to sewers and that dungeon entrances are open and 3 blocks wide. Verify every room/loot container and hidden wall can be reached and broken.
4. Use peaceful + normal/hard comparisons, daylight vs nighttime, multiple district boundaries, spawn eggs and natural mobs. For kind-variant spawners verify entities display, spawn, obey caps, despawn naturally as expected, and cannot spawn inside solid walls. Check no runaway ambient rat/moth population.
5. Test actual room renders and pathfinding at several chunk edges. Compare new furniture with earlier version and confirm old worlds preserve their existing worldgen settings.
6. Keep the **deferred** Ash-Snow/Sludge-Ice, environmental poisoning, Cross-Eye camps, villager/faction overhaul, FTB quests and screenshots-specific techniques outside this pass.

**Important:** Passing source QA cannot establish that this is a working compiled Minecraft mod, nor that its rendered visual quality matches high-end releases. The quality gate remains actual client screenshots and playtests.
