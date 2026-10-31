# Complete magic system directive

Digest of the owner's magic system master prompt, received 2026-10-31. **Status: on the roadmap, not started.** It expands the earlier specs and replaces nothing. The sandbox rule still holds: no mandatory quests.

## Canon rules to preserve

Sorcerers are biologically different from humans, with smoke-producing organs and a Devil Tumor. Each ordinary sorcerer has one innate type and cannot learn unrelated magic. Smoke carries the effect, and quantity is distinct from quality. Smoke can be stored and used by others. High-quality masks can enhance magic. Black Powder temporarily enhances magic and has exceptional interactions with people who cannot otherwise produce it. Sorcerers make doors when they have enough smoke. Devils are exceptions. Magic can transform creatures and objects, sometimes persistently. Hole rain can severely affect sorcerers. Time, resurrection and curse magic have exceptional rules. The manga outranks secondary summaries. Always separate canon, reasonable implementation, and original extension, in `MAGIC_LORE_REFERENCE.md`.

## Origins

- **Human:** no natural smoke, viable through equipment, machinery, medicine, trade and smoke products. Temporary effects never rewrite origin.
- **Sorcerer:** one innate type, Devil Tumor, smoke organs, and independently generated stats. Type and capacity are separate.
- **Weak Sorcerer:** a sorcerer preset with low output, not a species.
- **Devil:** optional advanced system with permissions. Never required.
- Creation offers Human, Sorcerer, Weak preset, random or manual magic, weighted rarity, and server-restricted rares. Data survives death, logout, restart and travel. No reroll exploit. Safe migration for existing players.

## Smoke

Stats: Maximum Smoke, Current Smoke, Production, Quality, Control, Efficiency, Mastery, Physical Condition, Emission. Regeneration depends on rest, food, injury, masks, environment, exhaustion and enhancement, never on just standing still. At zero smoke, casting and doors fail but normal Minecraft actions continue. Visible smoke is client particles with bounded server volumes. Most smoke is black, with canon exceptions. This is not renamed XP or mana.

## Innate types

One type with several techniques. Modular registry. First playable batch, in order: **Mushroom (En), Healing (Noi), Dismemberment (Shin), Reptile Transformation (Ebisu), Destruction (Fujita), Dispelling (Chota).** Then the exceptional handlers: Time (Nikaido), Curse (Risu), Resurrection (Kikurage), Puppet/Cooking (Turkey), Gravity, Restoration, then flight, barriers, rot, enlargement, reduction, transformation, strength and others in `MAGIC_TYPES.md`, each listing owner, effect, source, rarity, techniques, systems, exploits, status and verification.

Key constraints:

- Healing uses the real health and damage system and does not repair stone or metal.
- Dismemberment is not instant death and must not pretend Slowness is a detached limb. Use an abstract, reversible injury system.
- Reptile transformation preserves inventory and identity.
- Destruction is bounded and respects claims and griefing rules. Fujita's weakness is low output.
- Dispelling never destroys structures, inventories or origins.
- Time magic is never a world rollback. It must never duplicate items, reverse trades, restore consumed resources, rewind bases or undo saves.
- Curse is not a repeatable cast or a default starter.
- Resurrection is distinct from healing and never duplicates inventories.
- Puppets are not an army, and cannot duplicate items, bypass claims or load infinite chunks.
- Restoration cannot create free ores or diamonds.

## Mastery

Novice, Practiced, Experienced, Master, Exceptional, as gameplay ranks and not canon. Improves control, efficiency, precision, range, complexity, area and stability. Capacity and production improve separately. XP only from meaningful successful uses, with diminishing returns against AFK farming. Never adds unrelated types.

## Controls and HUD

Smoke bar, innate icon, selected technique, cooldown, regen feedback, mastery, mask bonuses. Rebindable keys: open menu, next, previous, cast, alternate, create door. Radial menu shows only the player's own techniques with cost, cooldown, targets, availability and mastery needs. Accessibility options. No silent failures.

## Casting pipeline, server-authoritative

Validate player and origin, type, technique, mastery, smoke, cooldown and status, range and sight, world and dimension, protection and PvP, then consume smoke, apply the effect, record persistence, sync, and notify progression and quests. Costs, cooldowns and ranges are data-driven and datapack or config tunable, with registered handlers for complex behaviour and no arbitrary scripting.

## Stored smoke and Black Powder

Empty and filled bottle, jar, spray can, tank, extraction and transfer equipment. Containers keep type, quantity, quality, source, special properties and usage. Collection consumes the producer's smoke. Using a bottle never grants the innate ability or organs. Works with trading, shops, hoppers, storage, crafting and multiplayer. Black Powder is separate from gunpowder, gives temporary amplification with configurable drawbacks, and comes from rare loot, trading, faction encounters or a documented adapted process, never a fake lore recipe.

## Masks

Improvised, cheap commercial, quality crafted, specialized sorcerer, rare Devil-made. Physical models, multiplayer sync, repair, tooltips. Cheap masks are cosmetic. Devil-made masks are not casually craftable.

## Doors

Smoke Door is a general ability themed by magic and mask, not a new affinity, with thresholds, valid destination and placement. Artificial Door stays usable by humans with no quest. En Family Door is a real structure. Devil Gate stays separate and gives no general Hell teleport to sorcerers. All share the persistent server backend.

## Environment

Hole rain reduces production and control, adds casting penalties and illness on prolonged exposure, with shelter and configurable severity, and does not hit humans with sorcerer sickness. Sorcerers' World is normal for magic and stays dry. Hell does not blanket-disable magic.

## Blocks and entities

Block transformation service records original and new state, source, dimension, position, time and reversion rules. Never transform bedrock, protected blocks, or portal cores without support, never copy container contents, never allow ore duplication, and bound snapshots per cast, chunk, player and server. Permanent changes are supported when explicitly permitted. Entity transformation keeps identity, health rules and equipment, persists across saves, prevents duplication and supports reversal. Player transformation keeps UUID, inventory, permissions, origin and magic data, with disconnect recovery.

## Systems beyond combat

Each type gets a canon-fitting non-combat use: fungal agriculture, medical services, controlled demolition, repair, movement. No generic shared techniques. Smoke tanks with hoppers, smoke machines, dispensers with bottled smoke, redstone doors, with no infinite magic automation.

## NPCs, PvP, performance

NPC sorcerers use the same engine with their own type, smoke, regen, targeting, doors, rain reaction, masks and bottled smoke. Named characters keep canon powers. Bounded casting and effects. The server decides everything. Admin config for player transformation, dismemberment, destruction, friendly fire, protected areas, permanent changes, rarity and production. Bounds on volumes, projectiles, structures, controlled mobs, changes per cast, transformations, snapshots and NPC cast rate. No per-tick scans of loaded blocks.

## FTB Quests

Optional chapters: Discovering Magic, Your Innate Ability, Smoke Production, Masks, Magic and Survival, Magical Doors, Advanced Magic. Never gates ability use. The magic system must work when FTB is absent. See `FTB_QUESTS_DIRECTIVE.md`.

## Its phase order

1 repository and lore audit, 2 sorcerer biology, 3 magic engine, 4 first six types, 5 world interaction, 6 smoke economy and equipment, 7 mastery, 8 magical doors, 9 NPCs and environment, 10 rare powers, 11 FTB Quests, 12 verification.

## Required tests (summary of the 38)

Human has no natural regen, one innate type, persistence, regen per config, exact cost, failure at low smoke, cooldowns, mastery on valid use, no unrelated types, each first-batch type working, rare abilities not exploitable, bottles keep type, storing consumes equal smoke, humans use smoke products, Black Powder effects, mask bonuses, cheap masks weak, rain effects, normal controls intact, Smoke Doors, Artificial Doors for humans, persistence after restart, no inventory duplication, identity preserved, dedicated servers, simultaneous casters, PvP and claims, NPC abilities, FTB records success, no quest gating, assets load, no severe tick degradation, dependencies work.

## Required documents

`MAGIC_SYSTEM.md`, `MAGIC_TYPES.md`, `MAGIC_LORE_REFERENCE.md`, `SMOKE_SYSTEM.md`, `MAGIC_DOORS.md`, `MASKS_AND_EQUIPMENT.md`, `MAGIC_SURVIVAL_BALANCE.md`, `MAGIC_FTB_INTEGRATION.md`, plus `README.md`, `HANDOFF.md`, `IMPLEMENTATION_STATUS.md`, `TEST_RESULTS.md`. Per type, list which techniques work, are partial, or are planned.

## Lore references given

viz.com/dorohedoro and the fandom wiki pages Magic, Magic_Users, Types_of_Magic, Devil_Tumor, Masks, Black_Powder, Magic_User_Door. FTB docs at docs.feed-the-beast.com/docs/mods/suite/Quests, github.com/FTBTeam/FTB-Quests and FTB-XMod-Compat.
