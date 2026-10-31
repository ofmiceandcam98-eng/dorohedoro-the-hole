# Implementation status

Status values: `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified`, `server_verified`. `server_verified` means a headless QA run measured it on a real dedicated server. Nothing here is `client_verified`, because the client has not been launched.

| Feature | Status | Notes |
| --- | --- | --- |
| Mod scaffold, `dorohedoro` ID, datagen | `server_verified` | Loads on a dedicated server |
| Overworld replaced through `minecraft:normal` preset | `server_verified` | Create World screen unconfirmed |
| Spawn on the plaza | `server_verified` | |
| Street plan, avenues, tram streets, canals | `server_verified` | Walkability and leak checks. Layout still fairly regular |
| Districts and 8 Hole biomes | `server_verified` | |
| Towers, tenements, apartments, shops, clinics, guard posts | `server_verified` for entry only | Stair flights not individually proven |
| Warehouses, factories | `server_verified` for entry only | |
| Market stalls, greenhouses, pens, nurseries, parks, vacant lots, junkyards, rubble, cemeteries | `implemented` | Appear in lot counts. Not inspected individually |
| Starting quarter with Hungry Bug, shelter, plaza | `implemented` | Layout unseen in a client |
| Hospital, Department Store, Research Manor, Lake of Refuse | `implemented` | Placement exists. Not inspected |
| Sewers, mine galleries, ladder shafts | `implemented` | Not walked |
| Vanilla ores, caves, aquifers underground | `server_verified` | Census only |
| Scrap metal, debris, rusted plating, recipes | `build_verified` | Not crafted in game |
| Chest loot tables | `build_verified` | Not opened in game |
| Villagers, golems, animals, bees, cats in lots | `implemented` | No errors logged. Behaviour unchecked |
| Strongholds, mineshafts, trial chambers | `implemented` | Biome tags only. Not located |
| Headless QA harness | `server_verified` | |
| GameTests | `planned` | None exist |
| Sorcerers' World, Hell | `planned` | Draft in `docs/handoff/phase3-draft/`, never compiled |
| Portals | `planned` | Design in `HANDOFF.md` |
| Sorcerers, smoke, magic, masks | `planned` | `docs/spec/MAGIC_SYSTEM_DIRECTIVE.md` |
| NPCs, factions, trading beyond vanilla villagers | `planned` | |
| FTB Quests | `planned`, on hold | Owner asked not to start |
| Immersion and Compatibility modes, gameplay config | `planned` | Nether and End are untouched vanilla |
