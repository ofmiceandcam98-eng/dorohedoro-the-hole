# Dorohedoro: The Hole — Pass 20.1 physical Mushroom magic

**Status: SOURCE ONLY — NOT COMPILED — NOT TESTED IN MINECRAFT.**

## Owner-directed behavior

The old Pass 20 visual-only BlockDisplay mushroom growth was **replaced**. Mushroom magic now uses actual vanilla `minecraft:brown_mushroom`, `minecraft:red_mushroom`, `minecraft:mushroom_stem`, `minecraft:brown_mushroom_block`, and `minecraft:red_mushroom_block` blocks in the world.

- Small mushrooms use vanilla support/light requirements, vanilla harvesting, natural random ticks/bone meal, and **vanilla non-collision** (small mushrooms in Minecraft do not have collision boxes).
- Giant mushrooms are genuine solid blocks with regular collision, normal harvesting and normal Minecraft drops. They grow over several ticks instead of appearing instantly. Low mastery produces a 3×3 cap on a 3-block stem; mastery 300+ permits a 5×5 cap on a 5-block stem where space allows, falling back safely to the smaller form in narrow streets.
- Small mushroom techniques spawn clusters when a suitable vanilla-supported patch of ground is available. `Fungal Bloom` and `Spore Dominion` can also spawn giant mushrooms. Focused `Spore Dominion` may produce two.
- **Skill-specific mastery** determines whether magical growth expires or remains forever. Different Mushroom techniques can reach permanence independently.
- Mushroom spells still retain their pre-existing combat/status behavior; physical growth is the additional visual/world consequence.

### Mastery thresholds

| Growth | 0–149 mastery | 150–299 | 300–599 | 600–1000 |
|---|---|---|---|---|
| Small mushroom | 30 seconds | 2 minutes | Permanent | Permanent |
| Giant mushroom | 45 seconds | 45 seconds | 3 minutes | Permanent |

Mastery comes from the casting skill's saved 0–1000 mastery score; an ordinary cast currently grants +5 mastery in the existing engine.

## Persistence and cleanup

- Blocks persist through world saves because they're **real blocks**.
- Temporary growth's position, block ID and expiration tick are stored per dimension in `dorohedoro_mushroom_growth.dat` using the Minecraft 1.21.1 `SavedData` API.
- Temporary growth expires on loaded chunks; unloaded chunks are never forced to load. Expired entries are checked again when their chunks load.
- Player-harvested temporary blocks unregister their expiry entry (with ordinary vanilla drops).
- Permanent growth is not registered for removal and persists just like any other block.
- Naturally spreading small mushrooms are vanilla-created blocks and are not tracked by the original cast's timer.

## Placement and safety

- Default vanilla `mobGriefing` gamerule must be enabled; disabling it prevents new magical mushroom block creation but does not delete old growth.
- All placement targets must be air and in already-loaded chunks; nothing overwrites structures, claimed building blocks, fences, etc.
- The owner must pass vanilla placement restrictions. Full compatibility with external land-claim mods requires in-game verification or optional claim integration.
- Giant growth requires a sturdy base, 3×3 cap clearance, and no living entity intersecting the stem; staged growth re-checks occupancy before placing a block.
- Growth batches and temporary blocks are capped to bound memory and block updates.
- In an unsuitable environment (too bright for a small mushroom, no valid floor, tight indoor space) the spell's existing smoke/particle effect still plays, but the physical mushroom may be skipped.

## Non-goals and limitations

- No Minecraft client, server, NeoForge or Gradle build was executed.
- No claim that all spells or the save data passed runtime testing.
- Small vanilla mushrooms are intentionally non-solid; forcing collision would stop them acting like vanilla mushrooms.
- The basic 3×3 or 5×5 giant shape is a stylized use of vanilla blocks, not yet a fully procedural En-scale mushroom system with varied cap geometry.
- Existing worlds/saves and the corrected `PortalService.Frame.stand()` offset are preserved in source.
- The 64 technique definitions, 8 innate specialties, three active slots per loadout, nine presets, and group Healing effects remain intact.

## Required tests on the owner's Windows PC (not performed here)

1. Backup your world; compile and run in a *test world* with Minecraft 1.21.1 / NeoForge 21.1.x.
2. Test casting all eight Mushroom techniques, especially Fungal Bloom and Spore Dominion, with mastery 0/150/300/600.
3. Check the small mushroom survival rules on dark soil, bright pavement, mycelium and indoor floors.
4. Check giant solid collision, block breaking and vanilla drops, and try to grow through houses and players.
5. Save, quit, reconnect, and confirm temporary mushroom expiration and permanent growth.
6. Check multiplayer spectators see the same blocks, and `mobGriefing=false` disables new physical growth.
7. Check NBT data load and that unrelated/harvested player blocks never disappear on expiry.
8. Check expected smoke animations and smoke resource/cooldown behavior with three-slot presets.
