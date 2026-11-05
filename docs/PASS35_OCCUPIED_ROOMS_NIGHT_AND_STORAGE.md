# Pass 35 — Purposeful interiors, reduced street storage, darker nights

**Source candidate; NOT COMPILED, NOT RUN IN MINECRAFT.** Continue local preinstallation review. Do not publish this candidate to CurseForge or install without the NeoForge build/client QA and the outstanding magic-crash investigation.

## Design intent

- **Apartments and tenements:** Furnished living/sleeping, kitchen/pantry, wash, utility/work and shared access spaces. Previous interior planners already provide several rooms off shared stair halls. V20 supplements them with functional furnishings and, only when space permits, full-height partitions and actual doors. Smaller rooms remain studios rather than unsafe or unusable 2-block closets.
- **Shops:** Sales floor and back-stock/work area; vanilla profession workstations (loom, lectern, fletching, cartography, smithing, smoker, crafting) used where appropriate. No special villager AI, economics, inventories or spawning introduced.
- **Clinic:** Reception, medical supplies and screened ward with vanilla lectern, brewing stand and cauldron.
- **Factories and warehouses:** Maintenance/work stations on perimeters; preserve central cart aisle and access doors.
- **Lived-in appearance:** Beds, shelves, carpets, stools (stairs/slabs), flower pots, and benches support genuine room use; don't add meaningless item piles. Existing generator provides further details.
- **Exterior storage:** One of about eleven market stalls retains a loot chest in V20; greenhouse/nursery and salvage loot moves into barrels; dock decorative chest lines replaced with timber; starter plaza keeps one loot-bearing starter barrel rather than multiple exposed starter chests.
- **Lights:** Street posts move from ~15-block spacing to ~45 on outer streets, ~30 near the starter area, unless the avenue geometry alters counts; smaller number of residential porch fixtures and industrial hanging lights. Occupied interiors retain their normal lighting for practical use. This is intended to produce dark street sections without adding artificial spawners.
- **Mob spawning:** Hole biomes already register zombies, skeletons, spiders, creepers etc. Natural monster spawning requires non-Peaceful difficulty, `doMobSpawning=true`, sufficiently dark eligible positions, appropriate distance from the player, and mob caps. **Night spawning has NOT been measured or confirmed.**

## Versioning

`world_detail_version=20` in the default *new world* preset opts into this work. Existing saved worlds with earlier version numbers keep their old generation layout and lighting. **Do not change the version of an already-existing world** for QA: use fresh test seeds.

## Required tests before installing or publishing

1. `bash tools/verify-neoforge-build.sh` in a Java 21 NeoForge environment; catch any compile errors and regenerate data.
2. `bash tools/qa-world.sh 12345 12 forward`, followed by reverse generation and city hash comparison, on a V20-generated fresh world.
3. Repeat seed `8675309`; verify deterministic structural walls/doors and the QA entry-access checks. Test with small and large lots, including chunk-seam homes.
4. Walk at least ten compact homes and ten tall apartment floors: check rooms have doors, useful interiors, accessible stair flights and no invisible/floating or occluded bed halves.
5. Sample 20 market stalls and starter supplies; count *visible* chests (goal: roughly 1 per 11 market stalls plus useful supply storage rather than a continuous display). Verify loot tables still roll.
6. At night on Survival or Adventure and Normal difficulty, with `doMobSpawning=true`, inspect block light in unlit streets and count natural hostile spawns beyond 24 blocks from the player; measure contrasting residential well-lit areas. Confirm ordinary vanilla spawn rules, no unwanted mob spawn-increase hacks.
7. Compare visual daylight and nighttime screenshots for legibility and actual mood. Keep spawn safe without brightening all districts.
8. Existing saved version-19 worlds must load consistently with unchanged generation profile; test restart/save, not merely creation.
9. Resolve Curse Mark crash and UI legibility separately before a final combined build; older Pass 33 UI source remains an uncompiled prototype.

## Honest limitations

The source patch is currently static-review only. We have not compiled it or measured real block-light levels, mob populations, perf/TPS, actual pathfinding, visual quality, or save integrity. This patch does **not** implement the previously requested broad river, hill, lantern model, icon art, texture rework or crash fix. Those remain separate workstreams for the consolidated release.
