# Pass 56 — Connected industrial service yards (2026-11-02)

**Status: source-only, uncompiled against NeoForge.** Do not claim this is an installable Minecraft mod. The Gradle wrapper JAR is still absent. The test harness compiles the actual new Java generator helper with minimal Minecraft stand-ins and checks chunk clipping; it does not execute `runData`, `runClient`, `runServer`, or Minecraft itself.

## Why this pass exists

Industrial buildings are central to the recognizable silhouette and everyday functioning of Dorohedoro's Hole. The earlier irregular industrial builder (Pass 14) created a genuine detached shack with crafting/smithing/storage next to an occupied factory or warehouse, but its connecting service strip was only **one block wide**, and there were no linking doors into the hall. That made an apparently important part of the site look usable while behaving more like a sliver of pavement.

This pass creates a usable **covered service connection**, not another decorative roof ornament or collection of exposed loot chests. It preserves Minecraft crafting/storage functionality and does not add light sources that interfere with nighttime hostile spawning.

## Implemented

- `StructuralVolumes14.industrialFootprint`: for newly created version-39 saves, increases the industrial main-hall setback from 8 to 10 blocks **only on the existing large industrial parcels**. World versions 0–38 retain the old geometry. Smaller industrial parcels continue to use the original single-hall layout.
- `StructuralVolumes14.serviceOutbuilding`: on new worlds, moves the detached workshop's east wall back to `main.x0() - 4`, leaving a 3-block passage, while preserving the workshop's existing south-facing public door and normal crafting, smithing, barrel storage.
- `LinkedServiceYards56`: derives the shack and passage from parcel geometry alone, and then places a 3-by-3 sheltered connector with four blocks of headspace, paved floor, and usable west/east-facing doors between shack and main hall. Clears only a two-block internal receiving vestibule to remove one existing cargo/furniture obstruction at the hall wall.
- Both connectors stay **within the owning industrial parcel**. No street, alley, sewer, canal, tram right-of-way, neighboring lot, flora, crop area, biome or mob spawn code is changed. No new lights or loot containers are placed.
- Opt-in is `Canvas.hasLinkedServiceYards56()` for `world_detail_version >= 39`; default for older saved worlds stays zero when not encoded. New world preset selects 39.

## Test outcomes

`bash tools/qa-pass56-all.sh`:

- **20/20** source-level integration and version checks passed.
- The production `LinkedServiceYards56.java` compiled with `javac` against minimal fake Minecraft `Block`/`Canvas` definitions and ran **12,000 industrial lot scenarios**; all 12,000 had a validated 3-wide, 3-deep, roofed connection with clear headspace, properly opened doors on each side and unobstructed main-hall vestibules.
- **632** seeded chunk-clipping fixture comparisons passed; **706** legacy version-38 cases verified that they do not get the new connector.
- All Pass41–55 offline regressions passed after historical QA scripts had their stale maximum world-detail version assertion updated from 38 to 39. The feature gates and legacy visual choices are unchanged.

This is strong evidence for the new generator geometry but **not** for actual NeoForge dependency compatibility, Minecraft block-state correctness, full street walkability, actual building appearance, light engine behavior, mob spawn behavior, or game performance.

## Required Minecraft acceptance (unfinished)

1. Restore Gradle's official wrapper JAR from a verified Gradle source on a network-enabled host; run `gradlew.bat runData` and `gradlew.bat build`, then `gradlew.bat runClient` and dedicated server, and execute available GameTests. No local compile of the complete project has yet passed.
2. Fresh `world_detail_version: 39`: visit at least 30 large industrial lots across varied climates/urban densities and verify real doors, covered sheltered walkways, unobstructed one-player and two-player use, loading entrances, normal workshop workstation interactions, and proper light levels during nighttime.
3. Check that covered passages remain enclosed and correctly supported when the original site is split across chunk borders; verify no structure overwrites canals, train lines, roads, mines, or sewer entrances.
4. Confirm a version-38 saved world remains unchanged on new chunk generation, since modifying the same world's encoded detail version would deliberately change its worldgen and cause seams.
5. Compare actual new in-game imagery against the manga/anime environment references and the user's November 1 screenshots: aim for genuinely cramped, layered industry, not more cosmetic clutter.

## Unchanged development policy

Continue world/city architecture and survival functionality first. Maintain genuinely variable climate **independent** of dense/inhabited/derelict/open fabric. Preserve usable interiors, caves/mining, viable dead-tree wood supply, hostile spawning at night, sparse sensible light, ordinary villagers' vanilla workstations without beginning the deferred villager overhaul, and 30% sewer-to-cave connections. Defer Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning, Cross-Eye camps and citizen/faction overhaul until city/district quality is accepted.
