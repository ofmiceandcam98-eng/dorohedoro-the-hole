# Project Acceptance Standard: Tensura: Reincarnated-level Depth

**Status: mandatory DESIGN BENCHMARK, NOT an implemented feature list.**
**Reference reviewed:** Tensura: Reincarnated (ManasMods) and its official community wiki, https://tensura.wiki.gg/ (reviewed 2026-10-31). Wiki has ~1,045 pages, some incomplete; this memo is a researched system inventory, NOT a claim that every wiki feature was audited. Do not import Tensura code, textures, UI screens, models, music, dialogue or other assets without permission. The creators explicitly prohibit derivative versions and asset reuse without authorization. Respect all Dorohedoro rights separately.

## User's required bar
- The finished mod must have **comparable systemic depth**, replayability, discovery, progression, first-join character selection, long-form mastery and world responsiveness to Tensura: Reincarnated, not merely a big collection of decor.
- The overworld must be an authentic Dorohedoro-inspired, densely built, polluted urban Hole; architectural plausibility, faithful atmospherics and explorable buildings come first.
- The experience must remain real, full survival Minecraft: wood/food/crops/mining/crafting/beds/vanilla XP and multiplayer should continue to work.
- Every system must persist across reloads/death/reconnect appropriately and be authoritative on the server. No client-trusted stat inflation, duplicate item progression or singleplayer-only mechanics.
- Give Claude a complete source project in EVERY handoff; source files, assets, build config, generated data, tests, known failures, change log. Never a prompt-only or patch-only handoff.
- Do not conflate *Tensura EP* with *vanilla XP* or assert Tensura offers a simple conventional character level: its wiki documents EP = AP + MP plus skill/gear mastery. Our optional character levels are a deliberate adaptation, not a copy.

## Systems researched in Tensura (confirmed behavior, high-level only)
1. **Initial identity decision:** a race-picker UI appears when spawning into a world. Race affects starting power/difficulty. Players receive a Unique Skill at spawn. [Getting Started]
2. **Persistent power economy:** Existence Points (EP) combine Magicule (MP) and Aura (AP). Kills award default ~3% of target's EP, with diminishing returns on weaker enemies and an adjustable death penalty. [Existence Points]
3. **Skill learning AND mastery:** unlearned abilities can be trained, mastered abilities improve and unlock effects; abilities are assigned to an action bar/presets. [Ability Usage]
4. **Hundreds of differentiated abilities:** the wiki lists >250 abilities across Skills, Magics, Battlewills; Common, Extra, Unique, Ultimate, etc. [Abilities]
5. **Race paths and major awakenings:** substantial milestones, evolution trees, rare conditions, boss gates and alternate high-level paths. [Races, Awakening]
6. **Gear evolution:** equipped and used gear accumulates EP and crosses tier thresholds; effects/engravings further differentiate builds. [Gear Evolution, Engravings]
7. **Active combat:** dodge window, cooldown, multiple ability inputs, passive toggles, mastery-dependent modes. [Dodging, Ability Usage]
8. **World progression:** custom biomes with power-related hazards, dungeons, villages, loot tiers, bosses, structures and exploration loops. [Biomes, Structures]
9. **Civilian/faction depth:** functional villagers with schedules, professions, restocking, reputation-dependent prices, trading, protection and consequences. [Dwarf]
10. **Player/admin safety controls:** adjustable gamerules, explicit reset mechanics, permissions and control of PvP/griefing. [Gamerules, Commands, Reset Scrolls]

## Adaptation, NOT a duplicate: Dorohedoro-specific design

### 1. First-join character-selection screen — mandatory
- Trigger when an uninitialized player first enters a world with this mod, BEFORE ordinary exploration. Render on the client but lock decision server-side.
- Distinct **HUMAN** and **SORCERER** identity choices in a fully custom, accessible interface (original graphics and UI components, not a copy of Tensura's screenshot).
- Each choice shows: origin, strengths, weaknesses, starting resources, difficulty, long-term strengths, examples of skills, lore.
- Human: no innate smoke magic; viable through fighting styles, surgery/medicine, scavenging, traps, anti-magic equipment, tools, teamwork, and eventual equipment mastery. Do not make Human a knowingly inferior choice.
- Sorcerer: innate smoke production, one *personal magic specialty* at start (or a defined identity-appropriate random option); later growth emphasizes output, smoke capacity, control, accuracy, stamina, and creative techniques, not universal access to everyone else's powers.
- Do NOT put Cross-Eye in race selection (faction, not species). Do NOT offer Devil as a common starting species; devil transformation belongs to exceptional, lore-constrained later progression if implemented.
- Confirm choice via explicit two-step confirmation. Persist per server player UUID in NeoForge saved attachment/data with migration/version key, reconnect-safe. Server checks only one start choice unless approved reset/admin action. First-join screen reappears if choice was not saved or interrupted; it cannot be bypassed to give duplicate starting items.
- New-world worldgen and dimensions initialize regardless of first-join UI. New save or server join should not hang if the client screen cannot open; reconnect resumes selection.

### 2. Persistent character power, with clear meaningful loops
- Offer a visible **Character Rank 1-100** (proposed design, configurable; neither anime canon nor a claim about Tensura). Separately track actual proficiencies. Leveling gains should come from exploration, combat, crafting expertise, quest discoveries and meaningful milestones; no infinite XP loop by exploiting placing/breaking blocks.
- Do not replace Minecraft vanilla XP. Allocate XP as additive mod progress with server-side anti-farm diminishing returns, configurable death consequence and reset policy.
- Track Human: Combat Technique, Survival Expertise, Medicine, Toolcraft and Field Knowledge.
- Track Sorcerer: Smoke Capacity/Regeneration, Smoke Control, Magic Specialization Mastery, Precision and Resistance. Sorcerer also receives ordinary survival attributes (not invulnerable to mundane threats).
- A clear status screen shows race, rank, ranks until milestones, defenses, stats, acquired skills, resource costs, cooldowns, unlock paths and discoveries.
- Mastery based on successful real uses with diminishing returns for spamming empty abilities; distinct thresholds produce a visible gameplay difference (not purely 1% bonuses).

### 3. Ability ecosystem with real tradeoffs
- A dedicated categorized ability menu: known/unknown/training/active/passive/cooldown/mastered plus search and filters; HUD keybindable ability slots, loadouts and ability descriptions.
- Each action has a validated server-side target, range, smoke/stamina cost, cooldown, damage or status rules, particles/sound and a clear failure reason.
- Sorcerer's personal magic speciality matters. Spell techniques are branches inside that specialty; limitations and counterplay encourage diverse builds.
- Human specialization trees: brawler, blades, ranged, traps, field medic, mechanic/scavenger, infiltration. Equipment/craft unlocks make Humans effective against stronger magic users.
- Example specialization *categories* inspired by the lore: transformation effects, fungal growth, restoration/healing, spatial/door techniques, protective smoke, material manipulation. **Do not blindly grant canon-character signature powers to everyone.**
- At least two distinct endgame-capable builds per origin, without a compulsory single meta path. Skills should have mastery milestones and meaningful countermeasures.

### 4. Meaningful equipment growth and economy
- Gear classes: scrap/field gear, salvaged industrial, refined custom alloys, protective gear, masks/respirators, smoke-conducting items (for eligible characters), unique rare artifacts. Each rarity/tier must have a survival acquisition path, repair cost, stats, counterplay and recipe.
- Equipment proficiency grows from actual wear/use; wear thresholds unlock practical improvements or reforges. Avoid indefinite exponential scaling and unbounded stats.
- Existing craft materials (sulfur, zinc, bitumen, rat fur, leech materials, membranes, metal carapaces, chitin) each get >=2 worthwhile uses as either consumable, equipment or building material. No infinite craft->uncraft multiplication.
- Every new block (including six Pass19 construction materials) remains survival craftable. The Cavern Lamp must use true three-dimensional hanging/standing lantern behavior and have a working lighting recipe.

### 5. The world must justify its size
- Seven districts aren't mere palette swaps: layout grammar, building types, signs, landmarks, underground connections, hazards and loot must communicate district identity. Residence / Market / Industrial / Waterfront / Derelict / Graveyard / Outskirts.
- Real purpose-specific buildings: homes with kitchens/beds/bathrooms, shops with sales/storage, workshops with tools, clinics with beds/medicine, factories with machines, sewer maintenance hubs with readable access, transport/rail districts with traversable platforms.
- Non-linear discoveries: diaries, local maps, rare crafting schematics, clinics, creature habitats, useful functional devices, hazardous zones, hidden lore, meaningful routes to portals.
- Traversal progression: city -> caves/sewers -> Sorcerers' World -> Hell. Distinct mechanics and incentives, not just same world with a new sky.
- New materials reflect Dorohedoro's grimy architecture; use original/permissioned art only. Every district must be visited at street, roof, interior and underground levels during QA.

### 6. Societies & faction consequences — later phase only
- Once architecture is approved, introduce believable inhabitants, professions, schedules, trades, witness/rep effects, security, raids and territory. Cross-Eyes, sorcerer society and devils must have lore-appropriate identity and consequences.
- Preserve vanilla villagers' emerald economy rather than making old trade content pointless. Support real server multiplayer reputations and exploit resistance.
- Avoid implementing full NPC/faction overhaul before the user's approval of architecture AND the intervening environment-hazard phase.

### 7. Deep survival and boss progression
- Environmental challenges (later, after architecture approval): Ash-Snow, Sludge-Ice, poison exposure, filters/antidotes, shelter and biome-specific equipment. Fair warnings and cures; no unavoidable spawn deaths.
- Creatures have ecological niches, locomotion, clear models, drops, AI telegraphs and sustainable ingredients. Sewer Leech is relatively large and NOT cartoonishly full of teeth.
- Elite encounters/bosses must have learnable phases, counterplay, unique rewards, sensible difficulty range and server-safe behavior. No pointless HP sponges.
- Late-game milestones are earned through multi-system achievements (explore, craft, mastery, combat, story, choice), not grinding one mob thousands of times.

### 8. Multiplayer, administration and accessibility
- Stats and decisions server-owned, saved per player, synced to client; dedicated-server safe common code, GUI client-only and absent from server loading.
- Data migration on upgrade; safe new-world settings; old save settings remain valid. Version config options: start choice, ranks, growth rates, anti-farm decay, PvP smoke damage, block griefing, reset requirements, ability UI keybinds, accessibility.
- Server restart, disconnect mid-selection, death, switch dimension, death during casting, inventory close, permissions, two players selecting simultaneously and player data recovery all tested.
- Accessible UI: readable high contrast, scalable, tooltips, keyboard navigation, controller fallback where feasible, no exclusive dependence on tiny icons.

## ACCEPTANCE GATES (none are satisfied by documentation alone)
A. **Visual/worldgen gate (current)**: paired samples from multiple seeds; district look is distinctive, coherent and lore-appropriate; no mosaic buildings, huge empty avenues, fake doors, repeated corridors, inaccessible rooms, broken survival resources. Full new-gen plus old-save test.
B. **Survival/environment gate**: real recipes, hazard counters, balanced exploration, no dupe or bypass.
C. **Origin selection gate**: selection GUI appears reliably on first join, can't double grant, persists, both Human and Sorcerer playable, tested client+dedicated server.
D. **Progression gate**: rank, stats, activity gain, diminishing returns, masteries, UI, death/reconnect persistence and admin configs work; no smoke for Human.
E. **Combat/skills gate**: at least two branches per origin with meaningful action gameplay, hotkeys, per-ability resources, cooldowns, effects and respec/reset safety.
F. **Equipment/world/reputation gate**: tiered gear, real encounters and exploration incentives; functional citizens, factions and reputation only once earlier prerequisites complete.
G. **Release gate**: Java 21 / Minecraft 1.21.1 / NeoForge 21.1.265, runData, build, inspect JAR, client+server integration, all survival recipes; asset provenance and CurseForge rights review; save migration/backups.

## Delivery / tracking standard
- For each feature maintain: design contract, data structures, source files, cross-system dependencies, recipes, images/models, config fields, tests, screenshots, balance notes, known failures, release label and status (implemented vs proposed).
- Every future handoff includes the entire verified source tree, this standard, the latest versioned changelog, and all currently unfinished milestones.
- Old reported portal screenshot is OLD and not evidence of an active portal bug. Pass 18 landing correction remains in source.

## Primary public reference links (source verification as of 2026-10-31)
- Home: https://tensura.wiki.gg/
- Getting Started: https://tensura.wiki.gg/wiki/Getting_Started
- EP / MP / AP: https://tensura.wiki.gg/wiki/Existence_Points
- Ability Usage: https://tensura.wiki.gg/wiki/Mechanics/Ability_Usage
- Abilities: https://tensura.wiki.gg/wiki/Abilities
- Races: https://tensura.wiki.gg/wiki/Races
- Awakening: https://tensura.wiki.gg/wiki/Races/Awakening
- Gear Evolution: https://tensura.wiki.gg/wiki/Gear_Evolution
- Engravings: https://tensura.wiki.gg/wiki/Engravings
- Dodging: https://tensura.wiki.gg/wiki/Dodging
- Structures: https://tensura.wiki.gg/wiki/Structures
- Biomes: https://tensura.wiki.gg/wiki/Biomes
- Dwarves/Rep: https://tensura.wiki.gg/wiki/Mobs/Dwarf
- Gamerules: https://tensura.wiki.gg/wiki/Config/Gamerules
- CurseForge original mod & permissions: https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated

## 2026-11-01 clarified lore requirement (Pass 19.1)
- Three central classifications: Humans, Magic Users (Sorcerers), Devils; ONLY Human and Sorcerer are standard first-join options. Show Devil as locked late-game ascension preview, based on the Devil Exam. Chidaruma is a unique born-Devil exception.
- Sorcerers possess one inborn magic type; mastery is techniques of that type rather than gaining every power. Separate smoke output, precision and rarity.
- Do not treat hybrid/cursed appearance or the unique entity Hole as routine race choices.
- See `docs/DOROHEDORO_SPECIES_MAGIC_DEVILHOOD_PASS19_1.md`; documentation only, not implemented.
