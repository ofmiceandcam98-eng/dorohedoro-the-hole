# Sorcerers' World: gameplay systems in settlements

Status words follow the master plan: implemented, partially implemented, designed, blocked, untested.

| System | Status | What exists | What it is not |
|---|---|---|---|
| Settlements (city, town, hamlet) | **Implemented**, server-QA verified | See the worldgen doc. Separate places with countryside between; 0.5% of the surveyed land is settled on seed 12345. | Not one continuous city. No canonical named cities yet. |
| Furnished interiors | **Implemented**, server-QA verified | Every room has furniture with a purpose, workstations, beds and light. QA proves each room is reachable from its door and lit. | No custom furniture blocks. Vanilla blocks only. |
| Loot | **Implemented** | 11 tables matched to rooms (pantry, kitchen, workshop, clinic, study, mask shop, smoke parlor, lodging, market, house, shanty). Smoke goods scarce: `empty_smoke_flask` weight 5, `smoke_residue` weight 1 in smoke parlors only. | No masks or Black Powder items, because they do not exist yet. |
| Townsfolk | **Partially implemented** (interim) | Vanilla villagers, persistent, spawned beside matching workstations: leatherworker (mask shop), cleric (smoke parlor, clinic), butcher (eatery), librarian (boarding house, manor), toolsmith and armorer (workshop), farmer, fisherman, cartographer. Beds give homes and the plaza bell is the meeting point, so vanilla schedules, trading and emerald prices work. | Not the planned sorcerer NPC system (smoke classes, factions, En Family staff, guards). They use vanilla villager models. |
| Shops and trade | **Partially implemented** | Vanilla trades through the professions above; emeralds keep their value. Shop signs: MASKS, SMOKE, EATERY, BOARDING, CLINIC, REPAIRS, TENEMENT. | No custom mask or smoke merchant trade tables yet. |
| Survival resources | **Implemented** | Ripe herb gardens on watered farmland, composters, market produce, pantries; ores and mining are untouched outside foundations; every workstation is usable. | |
| Lighting and night | **Implemented** | Lanterns in every lit room; street lamps every 12 to 16 blocks; plaza lamps; the smoke-column fire. Abandoned shanties are deliberately dark. | No custom night atmosphere for settlements beyond the existing sky. |
| Reputation, factions, En Family access | **Designed only** | Master plan. | |
| En's Mansion, Mastema, Zagan, Berith, Hydra Forest sites, Black House, Devil Church | **Not started** | | |
| Blue Night, partnerships, Devil Exams | **Not started** | | |
| Doors and portals into settlements | **Unchanged** | Existing portal system. Settlements do not yet register as door destinations. | |
