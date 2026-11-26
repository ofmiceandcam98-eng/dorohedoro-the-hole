# Sorcerers' World: master plan

The owner's full brief (2026-11-25) asks for the complete Sorcerers' World: cities, canonical regions, En's Mansion
and other landmarks, society, magic economy, events and QA. This file tracks what is done and what comes next. The
owner's priority for this stage: **reliable procedural placement and several high-quality smaller buildings before the
largest landmarks**. The owner also directed (2026-11-26) that the realm is **not one giant city**: distinct cities
and towns with countryside between.

## Phase status

| Phase | Scope | Status |
|---|---|---|
| 0. Audit and lore bible | Source audit, asset licenses, lore register | **Done for settlements**: the asset manifest, lore register, and the audit below. A full lore pass with chapter references is still to do. |
| 1. Procedural geography | Regions, settlement placement, travel corridors | **Partial**: vanilla terrain and biomes (owner's decision); settlement placement and spacing are done and QA verified. Travel corridors between settlements and landmark reservations are not started. |
| 2. Building library | ~30 meaningful templates | **15 of 30 done**: 12 buildings and 3 open lots, with palette, storey, roof and furnishing variation, functional entrances, loot and villagers. Next 15 are listed below. |
| 3. First complete city | A fully explorable, populated region | **Partial**: cities, towns and hamlets generate with streets, alleys, a plaza, lamps, shops and housing, and pass server QA. Not yet verified by eye in a real client; NPCs are vanilla villagers. |
| 4. Canonical regions | Mastema, Zagan, Berith, Hydra Forest | Not started |
| 5. Flagship landmarks | En's Mansion, Black House, Devil Church, Landlady's Mansion | Not started |
| 6. Society and magic economy | NPC occupations, masks, smoke commerce, factions | Not started (vanilla villager stand-ins only) |
| 7. Events | Blue Night, partnerships, Devil Exams | Not started |
| 8. Polish and release | Performance, multiplayer, regression | Not started |

## Audit of the existing Sorcerers' World (before this pass)

| Part | Where | State found |
|---|---|---|
| Dimension | `data/dorohedoro/dimension/sorcerers_world.json`, `dimension_type/sorcerers_world.json` | Vanilla noise generator, biome source `dorohedoro:sorcerer_lands`, open sky |
| Biomes | `RealmBiomes`, `datagen/RealmBiomeData` | 9 biomes, none with precipitation; city biomes have no trees |
| Biome source | `worldgen/SorcererBiomeSource` | City from the basin noise; water from `continents` and `weirdness`. **See the bug below.** |
| Terrain | `worldgen/noise_settings/sorcerers_world.json` | City basins flattened by `dorohedoro:city_basin` |
| Structures | none | City biomes were empty basins, with no buildings anywhere in the realm |
| Portals | `portal/PortalService` and others | Artificial Door and Devil Gate travel exist (not changed here) |

## Terrain decision: vanilla world (2026-11-26)

The original Sorcerers' World biome source sent every column outside a city basin to `sw_waters`, because the noise
router fixes `continents` and `ridges` at 0. The QA census showed 84% of the realm as `sw_waters`, with no farmland,
woods or Hydra Forest. The owner then directed: **use the vanilla world for the Sorcerers' World and add the buildings.**

For **new worlds**, `dimension/sorcerers_world.json` now uses:
- vanilla overworld noise settings;
- the vanilla multi-noise overworld biomes, wrapped in `dorohedoro:sorcerer_vanilla`. The wrapper is only a marker, so
  settlements can tell this dimension from any vanilla overworld.

What this means in game:
- **Rain:** `SorcererWeatherMixin` zeroes rain and thunder in this dimension, so canon "no ordinary rain" holds even with
  vanilla biomes.
- **Hydra Forest:** vanilla dark forest stands in for it. The `realms/hydra_forest` advancement accepts dark forest in
  the Sorcerers' World.
- **Vanilla structures:** villages, temples, strongholds and so on can now also generate there. Suppressing vanilla
  villages there is an open question for the owner.
- **Saved worlds:** they keep the generator stored at creation (assumed from how `frostbound_regions` was gated; confirm
  on a backup). The old SW biomes stay in the settlement tag so those worlds can get settlements in new chunks.

## Next 15 building templates (to reach 30)

Two more residential (a row house that shares walls; a hillside terrace house split across two levels), a second
apartment type (a courtyard walk-up), a pawn shop, a bottle-and-flask maker, a door workshop, a bathhouse (a
precursor to Berith Onsen work), a tavern, a bakery, a smoke-sick ward (civic), a school annex, a watch post (en
security), a sorcerer's laboratory (occult), an abandoned tenement, and a squatters' yard.

## Next systems, in order

2. Visual review in a real client of a city, a town and a hamlet; tune proportions with the owner.
3. Roads between settlements (travel corridors), plus bus stops later.
4. Landmark reservation in the planner: a cleared district for En's Mansion, sized and approached properly.
5. Replace villager stand-ins with sorcerer townsfolk: models, smoke classes, masks.
