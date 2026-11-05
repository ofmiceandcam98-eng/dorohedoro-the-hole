# Pass 61 — Sewer survival pressure (fresh-world detail version 44)

**Source checkpoint only; real NeoForge compile and Minecraft tests are still outstanding.**

## Player requirement
The sewers must be *challenging without techniques* but **not technique-required**. Ordinary Minecraft movement, armor, food, shields, torches, blocks, crafting, mining and tactical retreat must remain viable. No new poisoning, Ash-Snow, Sludge-Ice, special class abilities, combat lock, instant-death fall, or mandatory boss is introduced here. Cave, ore, and mining progression is unchanged.

## Implemented
1. The main municipal sewer's former regular lantern cadence of one every 31 blocks is preserved in saved worlds with `world_detail_version <= 43`. New version-44 worlds deterministically power roughly 3/5 of 93-block longitudinal segments. The other 2/5 have missing lanterns. Lanterns in lit segments stay on the previous 31-block sites. Natural Minecraft monster spawning still determines actual encounter rates; nobody receives scripted damage or forced debuffs.
2. Existing closed sewer inspection side bays retain their masonry, separate maintenance workbench and loot-table salvage barrel. In new worlds, about a quarter of those side rooms contain a vanilla **breakable** spawner for the existing `dorohedoro:sewer_roach`, replacing a hanging lantern. The main walkable sewer arteries are not blocked and the spawner is optional to approach. The existing `SewerBranchPolicy` preserves approximately 30% cave branches, 18% inspection bays and 52% ordinary intersections.
3. Both choices use global seed/coordinate hashes. They do not depend on processing order or neighboring chunks. Source version gating preserves previously saved environments.
4. No new items, techniques, status effects, blocks, loot-table registrations, recipes, poisoning or exterior lighting were introduced. Prior cave, workshop, and dungeon systems remain in place.

## Quantitative offline checks
`bash tools/qa-pass61-sewers.sh` compiles the exact production `CityHash`, `SewerBranchPolicy`, and `SewerHazards61` against JDK 21. Samples 6 seeds, 153,600 sewer junction selections and 7,560 lighting sections. Checks old-world choice, deterministic seam equality, cave rate, inspection and nest proportions. `python3 tools/qa-pass61-source.py` checks source wiring and the fresh-world preset. Historical tests are run by `bash tools/qa-pass61-all.sh`.

## Required Minecraft verification (Claude, Wednesday November 4)
1. Restore official verified Gradle wrapper and run a real NeoForge build and client **before declaring success**.
2. Create fresh version-44 worlds across diverse climate seeds. Find surface hatches and confirm sewer entrances and shafts remain usable. Explore at least 10 intersections and verify main floor Y=49 and dry side paths Y=50 onward remain traversable both ways.
3. Confirm dark runs have actual natural hostile spawns at the intended light level. Check 20+ runs before changing hostile spawn weights or adding more spawners. The 93-block dark segments should not be uniformly pitch black where a shaft or nearby structure casts light; adjust based on rendered results.
4. Enter at least 10 inspection bays; verify nest frequencies, breakable spawners, room workbench, loot barrel, and backtracking. Verify spawn restrictions and server synchronization. Verify no spawner overwrites a wall or barrel or places mobs inside blocks.
5. Compare equivalent seeds using version 43 and version 44. Verify earlier saves retain original lamp patterns and receive no new nests in future unexplored chunks. Never change a saved generator version in place.
6. Perform a vanilla-only challenge: no techniques, starter armor/iron tools, food, torches, shield, pickaxe. Confirm expected risk, escape, mineability and salvage. Then compare with techniques enabled; the latter should ease survival but not be mandatory.
7. Repeat across hostile and Peaceful difficulties. Do not require combat to find caves, and allow players to circumvent an optional nest. Preserve natural cave mining access and the 30% *selection* target, which is not proof of physical connection to existing caves.

## Deliberately not yet included
- Pumping stations and player-operated floodgates.
- Artificial environmental poisoning, radiation or toxic snow (deferred per user request).
- New enemy models or additional loottable filler.
- A difficulty slider or automatic health/stat scaling (must first prove baseline in-game).
- Guaranteed connections to pre-existing vanilla caves; selected cave branches still lead to purpose-carved caverns and not necessarily naturally generated cave noise.

The risk curve should derive from practical visibility, known breakable threats and risk/reward decisions, not arbitrary player-stat punishment.
