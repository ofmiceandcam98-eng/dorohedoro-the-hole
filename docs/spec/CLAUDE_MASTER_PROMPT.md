# CLAUDE CODE MASTER HANDOFF — DOROHEDORO: THE HOLE

> **Paste this entire document into Claude Code inside the mod repository.**  Treat this as the project specification and acceptance contract. This is for **Minecraft Java Edition**, not Bedrock. The aim is a real mod, not a datapack-only mock-up, a CreativeMode import, or a texture pack.
>
> **Nonnegotiable player requirement:** **The Hole is the entire playable Overworld**, and **every new character begins inside the Hole**. No normal biome landscape, vanilla village or forest should appear on the Overworld surface. All Minecraft survival functions and necessary resources must still work. All sorcerer magic and spells must be mechanically functional and consistent with the source material. No feature is “complete” without evidence.

## A. YOUR ROLE AND NONNEGOTIABLE WORK RULES

Act as a principal Minecraft Java mod developer, gameplay designer, world-generation engineer, network engineer, asset technical artist, QA automation engineer, and lore researcher. Build and maintain the actual source repository. Prioritize correctness over count of features. If the repository already exists, audit and **preserve existing work**; never overwrite it blindly. Use atomic commits, changelog and tests. Do not claim to have installed software, downloaded files, created visual models, run Minecraft, or passed tests unless you have evidence. If the environment cannot launch Minecraft, state precisely which checks were run and which still require a client or server.

1. FIRST inspect repository, branches, README, installed JDK, Gradle wrapper, Minecraft version, loader, mappings, dependencies, current assets, licenses, tests, and build logs. Preserve valid existing choices; propose a migration if essential.
2. **Suggested version pin:** Minecraft Java **1.21.1**, **NeoForge matching 1.21.1**, **JDK 21**. Pin exact dependencies in Gradle; consult the *version-specific* documentation and installed sources, not memory or an unrelated Minecraft version. GeckoLib is an optional pinned animation dependency; verify matching loader/version. Other dependencies must be justified.
3. Use mod ID `dorohedoro` unless already fixed by repository. Organize source by registries, worldgen, entities, spell engine, mechanics, gameplay, client, network, config, datagen, gametests, assets, and integration tests. Use resource location IDs everywhere.
4. Create/update **`docs/HANDOFF.md`**, **`docs/ROADMAP.md`**, **`docs/LORE_MATRIX.md`**, **`docs/CONTENT_MATRIX.md`**, **`docs/TEST_MATRIX.md`**, **`docs/ASSET_MANIFEST.md`**, and **`CHANGELOG.md`**. Keep these aligned with code every milestone. Maintain a feature status field: `planned`, `implemented`, `build_verified`, `gametest_verified`, `client_verified`, `server_verified`.
5. Separate **anime-confirmed**, **manga-only/possible spoilers**, **lore interpretation**, and **original gameplay extension**. Never label fan-designed spell names as canonical dialogue or canonical spells. Use the official anime references for visuals and a marked lore database for deeper facts.
6. Generate original in-game art, 3D models and sound where needed or use licensed content; do not rip anime audio, soundtrack, proprietary art, or another mod's assets. Keep sources/license notes. Reference pictures may guide fidelity but should not simply be redistributed as in-game textures.
7. Do not add placeholder registered items with no recipes/functions, broken models, empty dimension JSON, fake spell buttons or inert NPCs and mark them finished. If a requested feature is technically blocked, record the blocker, preserve stability and deliver a working alternative clearly labeled as an adaptation.

## B. CANON / CREATIVE TARGET

This is **Dorohedoro** by Q Hayashida. Essential aesthetic and systems: the grimy human city **the Hole**, toxic smoke-tainted rain, clinical experiments on humans, the separate **Sorcerers' World**, individual smoke-generated magical doors, masked sorcerers, Devil involvement, En Family, Cross-Eyes, strange creatures, horror-comedy and food (especially Hungry Bug and gyoza). Humans are not simply sorcerers with empty mana bars. Sorcerers have **one intrinsic magic type** produced by biological smoke organs; output volume varies. Magic is not a generic fantasy spellbook. Black smoke is normal; **Kikurage's smoke is white**. Smoke can be bottled/traded. The system should distinguish innate spells from equipment-activated stolen/bottled smoke. **Risu's Curse** has exceptionally unusual activation conditions; do not give all starting sorcerers free resurrection or ordinary repeatable Curse blasts. **Chota's magic is dispelling through difficult ritual**, not a generic short-cooldown projectile. **Turkey's magic creates living tracking dolls from cooking ingredients**, not ordinary commanded summons. **Dokuga's venom should not be misclassified as an established ordinary smoke spell**. A player-derived skill tree consists of *mastery techniques* for their one magic type, not multiple innate types. Devils are a late-game exception.

Include an optional `spoiler_content` config. At launch prioritize television anime S1+S2-confirmed material; expand with manga-only material only with clear data tags and references. Never spoil late mystery identities in default quests/UI unless story progression uncovers them.

## C. WORLDGEN — THE HOLE MUST REPLACE THE OVERWORLD

### Absolute world behavior

- Start/create a new world, load `minecraft:overworld` and visually spawn **inside the Hole**, not on normal plains, in an empty flat test world, in an unrelated dimension, or above an enormous city pasted in one location. Prefer a **world preset bound to an Overworld generator** and verify whether it can be selected or safely enforced as the default. **Do not claim automatic world replacement if a player must manually choose a preset**; expose a clear required world-selection/setup flow if the loader cannot safely override world creation. Never silently corrupt existing worlds. Add preflight incompatibility detection and warning for existing vanilla saves/worldgen mods.
- City goes in every direction across new chunks, with connected roads crossing chunk borders. Underlying biome/climate variation may drive *districts* but not normal visible forests/plains/deserts. Spawn safe, lit, navigable, with food, building materials, workbenches and meaningful starter gameplay within reach; beds and respawns work.
- Procedural generation deterministic by seed and coordinates, independently of chunk-generation order; no road seams, cut-off structures, leaking fluids or severe lighting/performance errors. Plan districts ahead of chunks, then populate with structures and loot. Provide adjustable city density, chunk budgets and optional dedicated-server pregeneration. Never load unlimited neighboring chunks during generation.

### Urban district types and landmarks

- Residential slums, alleyways, rooftops, bridges, tram/rail streets, markets, rusty factories, ruined industrial zones, warehouses, hospitals/clinics, magic-victim wards, police/guard posts, canals and polluted waterways, cemeteries, subterranean sewers and tunnels, lab basements, abandoned shops, junkyards, rare parks, underground settlements, caves and ore strata.
- **Hungry Bug** as a recognizable discoverable restaurant; hospital and magic-victim facilities; Nikaido/Caiman-associated locales; a central department store, cemetery for Living Dead Day, Kasukabe research/manor, **Lake of Refuse** as a rare dangerous landmark. En Family raids or magic-user doors can temporarily appear in the Hole. Named landmarks should obey rarity/minimum-distance and persistent placement so they are findable, not millions of blocks apart.
- Buildings require roofs, windows, doors, interior floors, stair routes, collisions, connected terrain, appropriate lighting, spawn control, loot and usable paths. Add several modular building palettes plus prefab variety; do not clone one giant structure endlessly. No structure may seal the only exit from player spawn.
- Below the city: functioning mineshaft/cave-like resource access, stone/deepslate, ores, gravel, clay, water, lava, diamonds, and renewable sources where sensible. Include stairs/hatches to the underground. Surface derelict parks, rooftop gardens, nurseries and market supply crates may provide saplings, seeds, dirt, grass, sand, logs and animals. Show resource acquisition in the guidebook.
- Atmospheric pollution: rain in the Hole may weaken sorcerers and reduce smoke output in *exposed* areas; a sheltered player is protected; provide medicine/protection and configurable severity. Human water contamination should be survivable and repairable, not a forced death spiral. Industrial sounds/particles must be bounded to visible chunks.
- Night/weather/lighting/daytime cycle; nighttime danger; optional Living Dead Day event frequency configurable and not forced permanently.

### Sorcerers' World and other dimensions

- Sorcerers' World is a **genuine separate Minecraft dimension** with its own structures/sky/biomes and no ordinary Hole city replacement, accessed via an **individualized, functional magical door**. Validate return teleport, portal persistence, safe arrival, client/server dimension sync and no cross-dimension duplication. Important sites: En's mansion, sorcerer cities/shops, mask makers, magic smoke industry, Cross-Eyes hideouts, Blue Night festival, magic school and realm-specific fauna.
- Custom magic doors are visually keyed to casters, with configurable textures/geometry, ownership and destination metadata. Allow rare constructed artificial door variants appropriate to lore, but don't make magic travel free for all humans at spawn. Teleport locations must be safe and returnable after save/load.
- Hell and Devil trials are *late-stage optional* worlds/instances with gameplay, not an empty registered dimension.
- Keep Nether and End accessible or supply carefully tested alternative progression paths. Define an explicit compatibility mode so vanilla advancement/stronghold/End paths and modded dimension requirements do not silently break.

## D. SURVIVAL AND VANILLA MINECRAFT COMPATIBILITY — AUDIT EVERYTHING

Preserve: block breaking/placing, wood/stone/iron/diamond/netherite tool progression, mining speed/harvest levels, inventories, hotbar, hunger, armor, hunger saturation, food cooking, furnaces/smokers/blast furnaces, crafting table, stonecutter, smithing table, anvils, grindstone, enchanting, brewing, villagers/trading or lore-themed equivalents, fishing, farming/animals, beds, sleep/respawn, XP, death/drop rules, maps/compasses, rail/minecart, redstone and hoppers, trading UI, tags, enchantments, shield combat, arrows/tridents, status effects, boss bars, commands, advancements and loot tables. Survival gameplay must not deadlock because normal biomes are gone.

**Vanilla resource acquisition audit:**
- Logs, planks, sticks, saplings, leaves, apples -> rooftop gardens, salvage, fenced nurseries/markets, reusable saplings.
- Cobblestone, stone, deepslate, coal, copper, iron, gold, redstone, lapis, diamonds, emeralds -> underground strata, tunnels, merchants and ore veins.
- Dirt, grass, clay, sand, gravel, flint, glass -> derelict lots, canals, rubble, excavations, sifting, kiln/smelting; renewable where needed.
- Wheat seeds, potatoes, carrots, beetroot, sugarcane, mushrooms, pumpkins, eggs, milk, leather, wool, feathers, honey, fish -> urban farms, livestock pens, protected greenhouses, wetlands, traders, mob drops.
- Blaze rods/powder, ender pearls/eyes, obsidian, nether wart, ghast tears, ancient debris, elytra, shulker materials, quartz -> preserve Nether/End pathways or clearly implemented equivalent sources without easy exploits.
- Totems, music discs, name tags, saddle, slimes, spider eye, bones/string, gunpowder, rotten flesh, paper/books -> test access via enemies, treasure, trades, crafting or events.
- Ensure recipes and loot tables use correct **1.21.1 data paths** (`data/<id>/recipe/`, `data/<id>/loot_table/`); no legacy path assumptions. Use vanilla tags when appropriate and datagen for recipes, loot, models, blockstates, item components, lang entries, advancements and tags.

### Existing Minecraft mob audit and adaptation

Do not blindly remove every vanilla mob because the world is urban. Define spawn policy by district and dimension:
- Zombies / husks / drowned / zombie villagers -> ordinary Hole undead in sewers, graveyards, toxic canals; special Living Dead Day behavior should be separately implemented, not every zombie globally reskinned.
- Skeletons / strays / bogged -> cemetery/basement or rusted industrial scavengers, with original skins only when properly licensed.
- Spiders / cave spiders / silverfish -> infestations in pipes, sewers and laboratories.
- Slimes / magma cubes -> sewer sludge / industrial leaks and optionally underworld settings; retain required drops.
- Creepers -> rare unstable mutant hazards; protect terrain according to gamerules/claims; no unlimited city destruction.
- Endermen -> rare interdimensional disturbances or in appropriate other dimensions; preserve pearl access.
- Witches -> rogue sorcerer-like encounters **without assuming vanilla potion throwers are canonical sorcerers**.
- Pillagers / vindicators / evokers / ravagers -> raider or faction combat substitutes only with safe vanilla mechanics, inventories, AI, raid compatibility and totem access.
- Villagers / wandering traders / iron golems -> urban citizens, vendors, guards; retain appropriate workstations/trades and pathfinding without overloading dense city AI.
- Bats, cats, rats (custom), roaches, chickens, cows, pigs, sheep, horses, wolves, fish, bees, frogs -> spawn in appropriate interiors/green spaces/supply farms, or supply their resources another reproducible way.
- Nether and End mobs -> leave present in their respective dimensions unless a tested replacement system exists; endgame game progression needs to work.

Create `VANILLA_COMPATIBILITY_MATRIX.csv` listing **every registry ID used in Minecraft 1.21.1 for mobs, crops, workstation blocks, loot-dependent entities and bosses** with `spawn allowed?`, district/dimension, drop access, special behavior, conflicts, and test status. Inspect actual vanilla registries / jar and data packs: do not invent the full ID set by hand. The suggestions above are seed design requirements, not an exhaustive list.

## E. COMPLETE SMOKE MAGIC / SPELL ENGINE

### Canon-first rules

Each player sorcerer has only **one native magic identity**. Magic is typically smoke released through fingers or other biological channels, with variable quantity/strength. Most smoke black, Kikurage white. The client sends **cast intent** only; the server validates origin, innate type, learned mastery, smoke amount, cooldown, range, line of sight, valid world, status, target, gamerules and claim protections. Server deducts smoke **once** atomically and then applies effects. Clients display cosmetics/hud/animation and never directly grant damage or loot. Stored bottled smoke is *consumable equipment magic*, never a second learned innate power.

Common fields for every spell: `id`, `display_name`, `canon_source`, `canon_confidence`, `magic_type`, `tier`, `unlock`, `cast_mode`, `range`, `area`, `targets`, `smoke_cost`, `cast_time`, `cooldown_ticks`, `duration`, `damage/effects`, `LOS`, `PVP_allowed`, `grief_policy`, `animation`, `particles`, `sounds`, `AI_weight`, `loot_and_death_behavior`, `network_payload`, and automated tests. Keep spells data-driven with a codec-backed definition plus a small number of effect executors; don't write a hundred duplicate monolithic event handlers. Targeting: self, raycast entity, raycast block, cone, projectile, AoE, anchored persistent area, ritual altar. Effective range and affected targets must exactly match what UI describes. Provide casting interrupts, dodge/counter interactions, particle culling, and server logging for rejections.

**Design guideline:** Names such as “Mushroom Burst” and “Cutting Wave” are **original Minecraft ability labels**, not canon incantations. Sorcerers' magic isn't a universal learned-spell system. Treat each branch below as a unique inherent *type* with 2–5 gameplay expressions. Every listed branch must be audited against anime/manga; low-confidence entries are optional or add-ons, not lies presented as confirmed anime canon.

### Required character magic types and executable gameplay translations

**01 | En — MUSHROOM CONVERSION** [CANON] `mushroom_magic`: Aimed conversion cloud affects organic mobs or certain materials, fungal spread, temporary mushroom hindrance, mushroom cover/cage, summon distinct fungal minion, rare high-tier mass conversion. NPC En can convert terrain/objects inside explicitly authorized boss arena, but player versions default non-destructive. Transformations must preserve original target UUID, inventory, ownership, health and persistence; reversible timer and dispelling route. Never turn item inventories into mushrooms or create duplication.

**02 | Noi — HEALING** [CANON] `healing_magic`: Contact heal, directed heal, regeneration field, cleanse magic injuries/status, emergency restoration of living teammates. No arbitrary resurrection; healing cannot give free duplicated hearts/inventory. XP/mastery based on actual wounds healed, not spam at full health.

**03 | Shin — DISMEMBERMENT / SEPARATION** [CANON] `dismemberment_magic`: Close smoke severance, cutting cone, targeted immobilization/limb-disabling, advanced dismantle with nonlethal separation/reassembly effect on bosses where appropriate. Combat should be strong without gory limb meshes being compulsory. No deleting entity references, dropping extra body-part loot or instantly killing bosses without phase checks.

**04 | Ebisu — REPTILE / LIZARD TRANSFORMATION** [CANON] `reptile_magic`: Apply temporary reptile form to a targeted entity, self reptile rage/mobility, claw and bite techniques, instability/mutation effects. Transform entity model safely or use reversible status/visual overlay. Clearly tag role as adaptation when anime doesn't show exact move.

**05 | Fujita — DESTRUCTION / PROJECTILE** [CANON] `destruction_magic`: Short smoke shot, concentrated projectile, ranged blast/burst and strong charge shot. Early Fujita-themed NPC has weak smoke output, not En-level area damage. Use predictable projectiles and hit validation.

**06 | Nikaido — TIME** [CANON, EXTREMELY RARE] `time_magic`: Reserved/unique special progression. Brief localized slow of entities, temporal step, optional safe snapshot-and-revert *of caster only* over bounded ticks, short targeted stasis. True story-specific time abilities may be scripted/one-use with progression costs. NEVER use global tick reversal, change shared world history, undo player inventories, resurrect killed entities wholesale, or revert placed blocks. Time snapshotted state must exclude items/containers/XP to prevent duplication. High-priority anti-exploit tests.

**07 | Risu — CURSE** [CANON, EXCEPTIONAL] `curse_magic`: Do **not** implement as ordinary renewable curse missile. Create rare death-triggered vengeance/Curse manifestation encounter as warranted by story, with safeguards for player death and inventory. Player access only as lore-appropriate rare quest/cursed artifact or endgame mode, clearly distinguished from a born ordinary sorcerer. Needs safe revive/respawn and multiplayer targeting rules.

**08 | Kikurage — REVIVAL** [CANON] `revival_magic`: **White** magic smoke. Rare NPC-assisted resurrection of eligible non-player entities / companions or tightly controlled player downed-state rescue with costs and cooldowns; forbid ordinary arbitrary player resurrection across death with duped equipment. Define exact cases that can be revived, inventory behavior, time window, and ritual.

**09 | Chota — DISPELLING** [CANON] `dispelling_magic`: Complex ritual to undo persistent sorcerer magic such as reptile transformations, mushroom corruption, curse effects where lore and gameplay permit. Requires ritual time, specific reagents, target and interruption rules; not a rapid no-cost projectile.

**10 | Kawajiri / Asu — TELEPORTATION** [CANON] `teleport_magic`: Selected-point blink, marked safe-location teleport, access/assistance with interdimensional travel, portal emergency rescue. Permission/claim/dimension-boundary checks; no teleport inside blocks, walls, unloaded chunks or banned regions.

**11 | Shou — INVISIBILITY** [CANON] `invisibility_magic`: Visual stealth, temporary concealment with detection counters and PvP fairness (particles/sounds close up, server still can target legitimately). Movement/attack may break concealment; do not remove entity from authoritative world.

**12 | Turkey — LIVING DOLL / COOKING** [CANON] `living_doll_magic`: Special ritual recipe at cooking workstation constructs a **non-speaking living tracker doll** mirroring a selected subject. It autonomously searches or points toward valid target as in source; player does not freely command it to fight. Requires target memory/token, ingredients, leash and chunk-awareness. Handle offline targets, privacy, PvP opt-in, loaded chunk budget; no always-on cross-server player tracking.

**13 | Natsuki — PROTECTION** [CANON] `protection_magic`: Strong temporary protective field/bubble, barrier health, limited ally enclosure. Performance-safe shield collision and barrier damage; vulnerability/counter mechanism; protection can't trap a player indefinitely.

**14 | Fukuyama — MEAT PIE TRANSFORMATION** [CANON] `meat_pie_magic`: Transform target into a temporary pie-like incapacitated state, strict anti-dupe/inventory preservation and duration. Humorous magic type; not generic fireball.

**15 | Baku — EXPLOSIVE** [CANON] `explosion_magic`: Targeted blast, remote delayed explosion, mine-like smoke traps; obey mobGriefing, land claims and configured entity/terrain damage.

**16 | Yaku — TRANQUILIZER** [CANON] `tranquilizer_magic`: Smoke dart, drowsiness meter, slowing/sleep-like stun on NPCs, time-limited balanced PvP CC; bosses partially resistant.

**17 | Haru — REVITALIZATION** [SOURCE-CHECK] `revitalization_magic`: Temporary restoration/buff where verified, differentiated from Noi's healing. Confirm exact powers in sources before shipping canonical designation.

**18 | Dr. Omori — SURGERY / TIME-RELATED** [SOURCE-CHECK] `surgery_time_magic`: Medical ritual reducing surgical recovery/cast time and repairing blocked smoke pathways if verified. Not a generic global time rewind. Quest/service implementation preferred.

**19 | Acupuncturist — FACE ALTERATION** [SOURCE-CHECK] `face_transformation_magic`: Temporary appearance/face change on consenting players or NPC disguise, visual model component with identity preserved. Social stealth not permanent identity theft.

**20 | Aitake / Maitake impersonators — METAMORPHOSIS** [SOURCE-CHECK] `metamorphosis_magic`: Controlled disguise/shape change where canon confirmed, duration, disguise detection, no inventory replication.

**21 | Additional observed lesser magics** [AUDIT BEFORE MARKING CANON]: power control, dissolution, muscle enhancement, enlarged size, flight, carpet/transport, giant-growth, elemental/rot, clone-like effect, and uncommon protective/transformation effects. Add all unique *actually depicted* abilities to `LORE_MATRIX.md` with episode/chapter and source verification, then implement appropriate types. **Do not assert every minor entry in a fan list appeared onscreen.**

### Optional manga-expanded repertoire (spoiler-gated, source-verify before implementation)

The wider fan-indexed magic roster includes augmentation, reduction, gravitational control, liquid manipulation, shapeshifting, telepathy, precognition, exorcism, advanced speed, barrier, magic absorption, life absorption, evaporation, clairvoyance, sabotage, restoration, invisibility-like elimination, enhanced strength, healing variants and more. Add content in audited tiers with working mechanics and IDs, or mark unverified/late-manga-only. **No fake canon**. Avoid giving non-sorcerers magic just because they are powerful. **Dokuga has venom/poison characteristics; verify whether a separate magical smoke type is ever explicitly established before using one.**

### Magic counters and interactions

- Magic suppression: blocked/clogged smoke veins, low output, toxic Hole rain, smoke suppression equipment and dispelling rituals, all with cure paths.
- Attack/defense matrix: healing removes debuffs but not all transformation states; dispelling can undo specified magical transformations, shields absorb finite damage, stealth has detection tools; magic immunity/resistance is an individual trait (e.g., Caiman) and must not be generalized.
- Stored smoke: smoke bottle/spray can holds exact type/output, owner provenance, charges/volume, cost and potency. Using bottles consumes stored smoke; never changes innate type. Secure serialization and crafting remainders.
- Partnerships/contracts: optional Blue Night partnership mechanic with mutual consent, pact benefits, social consequences, clean termination and no power cloning. Devil deals/Devil training rare and lore-sensitive.
- Controls: customizable keybind, radial menu, hotbar targeting, spell icon, tooltip, resource HUD, color-blind-safe cues; spell particles normal black plus type-specific impact visualization without implying every smoke type is neon green.
- Events: high-cost magic, interrupted rituals, PvP duels, boss immunity windows, magic interference, shield stack caps, and rain casting are covered by tests.

## F. WEAPONS / COMBAT / ARMOR / MASKS

Weapons must have working collision, attack damage/speed/durability, stamina interactions if enabled, enchant compatibility, swing/projectile animations, tooltips and recipes or loot sources. At minimum:

1. Caiman-inspired paired combat knives (dual wield visually; offhand handling safely; slash combo), kitchen cleaver, scavenged knife, reinforced knife, meat hook, rusted pipe, rebar club, pipe wrench, industrial hatchet, scrap spear.
2. Shin-inspired heart-hammer (heavy melee/short stun; requires proper model), heavy sledgehammer, En-family ceremonial weapons, Cross-Eyes knives and improvised arms.
3. Bullet/firearm themes must be balanced and optional (e.g. compact gun/handgun if justified and workable under gameplay rules), with finite ammo, reload and server-validated projectile physics; don't promise a full gun system without testing.
4. Masks: Caiman's face is anatomy, **not a removable sorcerer mask**. Individual sorcerer masks for En, Shin, Noi, Fujita, Ebisu, Nikaido, Chota and other documented characters. Distinct geometry, slots, visibility, custom armor layers and durability; masks alter modest resistances/status/efficiency, **never grant their owners' unique innate magic**. Mask-making workshop with ingredients and models.
5. Protective equipment: rubber boots, rain cloak (Hole rain defense), respirator, gas mask, industrial armor, sorcerer suit/robes, special high-tier Devil training gear. Preserve vanilla armor values and enchantments.
6. Unique items: black-smoke flask, rare white-smoke flask, bottled smoke labels, magic-user door shard/anchor, smoke-vein testing device, magical identification equipment, mask molds, Devil tumor specimen (rare quest material), smoke suppression medicine, Black Powder, mushrooms, gyoza components, quest documents/contracts.

### Crafting requirement (fully implement real recipe JSONs)

For each item produce unique registry ID, recipe(s), station, vanilla ingredients, optional Dorohedoro ingredient, unlock advancement, loot/vendor source, durability, repair item, tags, creative tab and balance/test row. Guarantee no recipes have impossible circular prerequisites.

Initial reproducible example designs (**proposed gameplay recipes, not canonical formulas**):
- `scrap_metal`: yield from salvaging industrial debris with proper block drop or smelting iron scraps; iron nuggets remain obtainable.
- `combat_knife`: 1 iron ingot + 1 stick + leather or string; repaired with iron.
- `reinforced_knife`: combat knife + iron ingot + leather at smithing/repair station; carry existing durability/data safely.
- `smoke_flask`: glass bottles + iron ingot + copper ingot + leather; store one type of smoke, no free magic generation.
- `mask_blank`: leather + paper + dye + iron nugget; later forge in mask bench with mold and design-specific ingredients.
- `sorcerer_door_anchor`: obsidian + iron + smoke-flask charge + rare sorcerer-world drop; never available without some tested first portal/unlock path.
- `black_powder_packet`: acquired as illicit vendor/event/quest loot; if craftable, dangerous reagents and tested balance. Debuffs/instability, no infinite potion stacking.
- `rain_cloak`: leather + wool + resin/honeycomb; rain defense.
- `gyoza`: wheat flour-equivalent + cooked meat/vegetable ingredients + water; proper food and saturation with cooking station/furnace recipe.
- `healing_bandages`: wool/string + mushroom/medicinal herb; distinct from Noi magic.

Use the actual **1.21.1 recipe schema and item data components**. Write GameTests for missing ingredients, output, container return, item component copy, stack limits, dupes, reload/datapack validation and recipe book visibility.

## G. NPC, CREATURE AND BOSS CONTENT MATRIX

**Friendly/neutral:** Caiman (distinct lizard head, paired knives, high physical ability and relevant magical resistance), Nikaido (Hungry Bug chef, martial fighter and rare lore magic with secrets gated), Kasukabe (human scientist/quest giver), Dr. Vaux/hospital staff, ordinary Hole citizens, vendors, shopkeepers, healers, sorcerer mask craftsmen, guards, laborers, students, and food-service NPCs. Make NPC routine schedules and dialogue practical for survival and quest delivery, not just stationary cosmetic statues.

**Sorcerers/factions:** En Family: En (boss/leader), Shin and Noi (coordinated combat duo), Fujita, Ebisu, Chota, Kikurage, Turkey, Shou and other documented associates. Cross-Eyes: appropriate members/underpowered sorcerers, Black Powder network, leader quests. Unaligned magic users, masked traveling sorcerers, gang members, bounty hunters, shapechanged victims. Distinguish NPC allegiance from species.

**Creatures:** Johnson (large roach, signature behavior), lesser roaches/rats (original ambient mobs), mutated humans, mushroom creatures and fungal thralls, magical test subjects, lab mutants, raiders, sewer hazards and Living Dead Day undead. Create original Minecraft-compatible models and animation. Avoid generic reskinned zombies as the *only* Dorohedoro creatures. Have a spawning rule and meaningful drop for every new mob, a path to remove persistent mobs and no uncontrolled breeding/summon loops.

**Boss fights / milestones:** En: mushroom cover, conversion hazards, minions and phase changes; Shin+Noi: mobility melee plus active healing/rescue of teammate; rogue transformation sorcerer, Cross-Eyes raid captain, rare Devil challenge, Lake of Refuse supernatural event as late content. Every boss has activation conditions, cooldown/telegraph, fair line-of-sight, target selection, non-cheese-but-achievable mechanics, drop tables, progression reward, despawn/reset and multiplayer health scaling. Avoid designing named lore characters as kill-on-sight mobs everywhere.

**AI architecture:** server-authoritative goals, navigation through stairs/doors/alleys and interiors, target selection/faction hostility, day schedules, attack windup, ranged projectile, casting, retreat, allies rescue, trade/interact, client animation state sync; avoid pathfinding every entity every tick across a city. Test server lag during NPC rushes and after unloading/reloading city chunks.

## H. FACTIONS, CIVILIAN LIFE, EVENTS, RPG PROGRESSION

- Origins human/sorcerer chosen once per character, story-safe respec only via admin/config or rare lore-justified mechanism. Sorcerer innate type chosen/randomized with weighted rarities and disclosed odds; no mandatory legendary roll; sane human gameplay parity via equipment, agility and resistance.
- Progression tracks: exploration, scavenging, combat, crafting, magic mastery, relationships, faction reputation and quest milestones. No infinite XP by self-heal, alt account farming or harmless block spam. Data persists UUID-based and server controlled.
- Reputation: Hole Residents, En Family, Cross-Eyes, optional neutral sorcerers, hospital/merchants; changes from theft, violence, trades, contracts, protection. NPCs react meaningfully, guards respond. Avoid permanent irreversible hostility without recovery path.
- Blue Night festival and sorcerer partnerships, Living Dead Day cemetery defense, hazardous rainstorms, sorcerer raids into the Hole, clandestine Black Powder trade, gyoza cooking festivals, bounty hunts, lab rescue events, sorcerer mask contests and hidden Devil trials. All event schedules configurable, deterministic and recover gracefully across reboot.
- Quest framework: data-driven objectives, NPC/dialogue tags, server-tracked progress, reward claims once, branching/optional decisions. Base campaign starts in Hole and introduces survival, Nikaido/Caiman, magical experiments and door travel. Don't force the player to literally be Caiman or overwrite named character arcs. Spoiler boundaries respected.
- Economy: scavenged scrap, trade tokens/currency, special reagents, authentic-vibe food, repair/material barter. No duplicated rewards or broken ingredient economics. Integrate inventories and trading APIs.

## I. MODELS, TEXTURES, ANIMATIONS, AUDIO, USER INTERFACE

- Art direction: readable voxel Minecraft silhouette with Dorohedoro's filthy industrial palette, weathering, concrete/rust textures, masks, black smoke and grotesque humor. Distinguish Hole vs Sorcerers' World architecturally. Do not make every attack neon green.
- Pipeline: Blockbench `*.bbmodel` source -> exported GeckoLib/other version-compatible entity animation/model JSON, 16x/32x texture PNGs, compiled resource assets and renderer registrations. Attach source/editable files and naming conventions. Check UV overlaps, texture alpha, pivot transforms, visible parts, handedness, walking cycles, cast cues, hitbox/eye height, LOD/culling and missing texture purple-black debug.
- Original sound categories: city wind/rain, pipes, electrical buzz, shop ambience, distant siren, footsteps, mask toggle, gyoza cooking, different spell casts/impacts, portal transitions, boss telegraphs, roach calls. Only license-safe audio. Use proper `.ogg` and `sounds.json`, attenuation and categories.
- UI: origin chooser, one-innate-magic selector/rarity explanation, smoke meter, spell hotbar/radial menu, spell/curse/cooldown indicator, reputation tab, quest tracker/guidebook, teleport return info, clear errors and settings. Must run with gamepad/keyboard remapping, GUI scaling and multiplayer.
- Translation keys for every UI element/item/mob/spell; descriptive accessibility labels; 16:9 and smaller screen test; particle-density config; subtitles for audio cues.

## J. ARCHITECTURE, NETWORKING, SECURITY AND PERFORMANCE

- Use `DeferredRegister` or official registries for blocks/items/entities/menus/sounds/particles, data components for ItemStack metadata, attachments for player/entity data (configure persistence/copy-on-death), `SavedData` for world/faction/event/landmark data, JSON/datapack codecs where appropriate, proper packet payloads for client visual events and server-authoritative spell use. Validate only IDs/coordinates/settings sent from client; never accept client-sent damage/healing/smoke quantity/target effects/teleport destination without server validation.
- Keep worldgen deterministic and bounded. Server tick time target: measure against baseline hardware and report p50/p95 tick latency at 1/4/8 players, mob load and chunk traversal. Do not invent performance metrics. Lazy init, unload cleanup, finite entity caps; avoid pathfinding to unloaded chunks, per-tick full-world scans, looping self-spawn, and unbounded particles.
- Claim/anti-grief compatibility: non-destructive by default for mushroom magic, explosions, temporary structures, giant growth and time magic. Configurable claims interaction where available, honor `mobGriefing` and team PvP. No spells editing containers, bedrock, portals, spawn-protected zones, command blocks or another player's protected build without explicit permission.
- Handle death/logout/dimension travel/portal reentry while casting; prevent casting twice due to network retransmit; idempotent event and reward handling; mitigate infinite potion, storage-smoke and respawn clone exploits. Null-safe handling for offline player targets and delayed tasks.
- Compatible dedicated server: do not load client-only classes server-side; use separate visual and logical classes; handshake for required mod version and payload protocol. Default multiplayer works with matching client+server mod versions. Test LAN, localhost dedicated, and optional higher-latency link. Test 2+ simultaneous casters, conflicts, disconnect, duels and bosses.
- Config: world preset enforcement/setup, existing world guard, vanilla progression mode, spoilers, dimension access, magic rarity, smoke regeneration, PvP, terrain effects, pollution severity, performance caps, event cadence, NPC density, server admin overrides, debug flags.
- API mistakes are critical: pin Minecraft/NeoForge mappings, consult 1.21.1 docs and Gradle sources, don't paste API snippets from 1.21.5+/26.x without porting; no placeholders or deprecation hacks merely to compile.

## K. AUTOMATED + ACTUAL GAME ACCEPTANCE MATRIX

Build incremental tests and a reproducible QA script. For each test record command, environment, pass/fail, screenshot/log, timestamp or commit, caveats.

**Build/data:** `./gradlew build`, data generation, client run, dedicated server run, no unresolved models/lang/sounds/recipes/loot, jar loads without crash.

**Worldgen:** create 10+ seeded worlds (configurable) and inspect spawn; every spawn in Hole; no vanilla surface biome regions; roads connected crossing chunks in X/Z; buildings navigable/interiors usable; city keeps generating far from origin; no orphan building/road seams; resources/mine entrances/reachable food; deterministic same seed results; save/reload stable; safe spawn light; weather shelters work. If world preset selection required, test user setup and make that requirement unmissable in menu/docs.

**Survival:** full path from first spawn to wooden/stone/iron gear, farm, food, shelter, enchant/brew, locate diamonds, portal/dimension progression, trading, ender pearls/blaze, death/respawn, bed sleeping, inventory recovery, common vanilla mob drops. Verify resource balance across several seeds.

**Magic:** one innate type per sorcerer; human cannot cast innate; all spell IDs registered; smoke spent exactly once; insufficient smoke rejected; regen/negative clamp; casting cooldown; ray/LOS correct; friendly fire and claims; combat damage matches armor/effects; potion interaction; non-spoiler unlocks; models/animations in sync. For each canonical type make at least one **mechanically unique** working acceptance test, not merely a test that item registry exists. Additional targeted tests: En mushroom conversion temporary and reversible; Noi restores actual missing HP; Shin disables/mutilates safely without duplication; Ebisu transformation restores appearance and health; Fujita projectile hits at range; Nikaido cannot time-rewind items; Kikurage white smoke and restricted revival; Chota ritual reverses the exact tagged effects; Turkey dolls track without cross-server surveillance; Shou concealment ends correctly; Asu teleport has safe return; Risu special trigger avoids player death duplication; Chota does not become universal instant dispel; smoke flask consumes exactly one dose.

**NPCs/events:** mobs spawn/despawn by district, pathfind in interiors, bosses switch phases, loot/rewards only once, faction hostility and trade react, named NPCs aren't generic aggressive spawns, Living Dead Day starts/stops and survives restart, quest progress persists.

**Portal/dimensions:** spawn in Overworld Hole, sorcerer door travel and two-way return, old coordinates safe, dimension-specific mob spawns, no lost players after restart, no duplicated worlds/entities/items.

**Multiplayer:** 2/4/8 clients near boss; consistent HUD magic reserves, animation and particles; network reordering/rate-limits; PvP on/off; logout during transformation/rewind; duplicate cast requests; dimension crossings; boss kills and individual loot; server save and crash/restart recovery.

**Performance:** compare vanilla-mod-free baseline and loaded mod on same machine/hardware; measure tick duration, chunk generation, heap usage, entity count, particles, multiplayer load. Set and meet explicit measurable budgets appropriate to test environment; fix severe spikes instead of declaring quality from a compile.

**Save integrity:** back up; load/reload, force restart, incompatible world presets warning, migration tests; never modify existing vanilla worlds silently; config updates don't delete saved origin/reputation; server world duplication safe.

## L. DEVELOPMENT GATES / DELIVERABLES

**Gate 0 — Audit & boot:** project compiles, runClient/runServer, mod ID, registries, configs, datagen, game tests, documentation and Git history.

**Gate 1 — THE WORLD FIRST:** Hole is actual Overworld via properly selected/enforced preset; safe player spawn; endless roads/districts; functional interiors, resources, mobs, mining, starter loot and survival loop; 10-seed validation. DO NOT proceed to giant character asset collections if Gate 1 is failing.

**Gate 2 — Spell vertical slice:** Human/Sorcerer origin, persistent innate type, smoke meter, one En conversion attack, one Noi heal, one mask, one smoke flask and one custom enemy; server authority, cross-client sync and tests.

**Gate 3 — Sorcerers' World:** working safe two-way door, generated distinct realm and initial structures; lore-specific rain difference.

**Gate 4 — Full core repertoire:** all anime-confirmed active magic types in section E with differentiated actual skills, effects, limitations, assets, UI and tests; priority to observed S1/S2 content. Source-check and document every optional/manga-only type.

**Gate 5 — World inhabitants:** NPCs, masks, equipment, mobs, bosses, Hungry Bug, faction AI, vendors, gyoza and quests.

**Gate 6 — World events, advanced magic and Devil content:** Living Dead Day, Blue Night, partnerships, Black Powder economy, advanced bosses, rare time/curse/revival, optional Hell.

**Gate 7 — Beta qualification:** complete QA matrices, visual model audit, performance profiling, dedicated multiplayer tests, reproduction fixes, build ZIP/JAR, dependency/install guide and known issues. No completion claim unless tests demonstrated.

**After each work session:** report `Completed / Files changed / Build command+result / Tests run+result / Bugs/blockers / Next smallest verified milestone`. Update `docs/HANDOFF.md` so work survives context resets. Make progress on actual code now, not just brainstorming. Each milestone must leave the repository buildable.

## M. AUTHORITATIVE REFERENCE LINKS / ASSET RESEARCH QUEUE

Sources below are **research and APIs**, not permission to copy copyrighted content. Keep per-asset provenance in `docs/ASSET_MANIFEST.md`; official artwork should guide new models.

**Dorohedoro anime (S1/S2 official) and character visual designs**
- Official anime site, cast, S1/S2 visuals/episodes: https://dorohedoro.net/
- Manga/anime synopsis and season pages: https://dorohedoro.net/sp/
- Dorohedoro magic and biological smoke (fan-edited; verify details with anime/manga): https://dorohedoro.fandom.com/wiki/Magic
- Named magic type roster (fan-edited, contains spoilers and uncertain classifications): https://dorohedoro.fandom.com/wiki/Types_of_Magic
- Magic doors/design references: https://dorohedoro.fandom.com/wiki/Magic_User_Door
- Locations reference index: https://dorohedoro.fandom.com/wiki/Category:Locations
- Objects, masks, flasks and equipment index: https://dorohedoro.fandom.com/wiki/Category:Objects
- Official full-body anime reference filenames (if accessible; credit and use for study, **not imported textures**): https://dorohedoro.net/images/character/character1_full.png (Caiman); .../character2_full.png (Nikaido); .../character3_full.png (En); .../character4_full.png (Shin); .../character5_full.png (Noi); .../character6_full.png (Fujita); .../character7_full.png (Ebisu).

**Minecraft 1.21.1 and NeoForge (USE VERSIONED DOCUMENTS)**
- Getting started / Java 21: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- Registries, data-driven registration and custom spell registry guidance: https://docs.neoforged.net/docs/1.21.1/concepts/registries/
- Attachments for player/entity data and death-copy rules: https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- SavedData for world/faction/event info: https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
- Server/client payloads: https://docs.neoforged.net/docs/1.21.1/networking/payload/
- Recipes and data generation: https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/
- Loot tables and paths: https://docs.neoforged.net/docs/1.21.1/resources/server/loottables/
- GameTests: https://docs.neoforged.net/docs/1.21.1/misc/gametest/
- Minecraft vanilla recipes/data reference: https://minecraft.wiki/w/Recipe_(Java_Edition)
- Minecraft vanilla Java game jar in *locally owned installation* is the exact-version reference for all vanilla registry IDs, block/item behavior, recipes, mob attributes, loot and asset conventions. Inspect it via Gradle-generated sources and game's installed data. Do not redistribute Mojang files.

**Models / animations / auxiliary tooling**
- GeckoLib project, version/loader matching: https://modrinth.com/mod/Geckolib
- Compatible player animation library if needed: https://modrinth.com/mod/player-animation-library
- Blockbench: https://www.blockbench.net/
- When using other mods as examples, inspect license/attribution. Do not copy closed code/assets from unrelated mods.

**MANDATORY FIRST ACTION FOR CLAUDE:** Audit the repository and environment. Write `docs/PROJECT_AUDIT.md` listing exact Minecraft/loader/JDK versions, what already runs, what is missing, and next smallest Gate 0 task. Then implement and verify that task, update handoff, and continue working through the gates. No invented evidence and no changing the world requirement into “a city somewhere in the Overworld.”
