# Dorohedoro: The Hole — Lore-Researched World and Mechanics Master Blueprint

**Target:** Minecraft Java 1.21.1 / NeoForge 21.1.265 / Java 21.  **Date:** 2026-10-31.  **Status:** research-backed SPECIFICATION, **not** evidence that features below already exist.  **Mode:** multiplayer-compatible, survival-first sandbox, no mandatory main quest and no removal of vanilla mining, crafting, redstone, farming, trees, ores or End access. FTB Quests deliberately deferred.

## Evidence conventions and non-negotiable design decisions

- **[CANON]** verified narrative identity, behavior, biology or location, supported by URLs at end. **[ADAPTATION]** proposed Minecraft rule, recipe, number, damage value or dimension feature that is NOT asserted to exist in the manga. **[PLANNED]** design not yet implemented. **[SOURCE]** code integrated in previous passes but runtime not tested.
- The actual canon has three realms: **Hole**, **Magic-User World**, and **Hell**. Magic is a biological property of sorcerers; almost every sorcerer has **one innate magic type**. Smoke volume/output and intrinsic magic type/quality are separate. Smoke can be bottled. Devil Tumors are biological organs in sorcerers' brains, needed for most resurrection and related to Black Powder production. Devils are usually transformed former sorcerers, but **Chidaruma is the original Devil**.
- **Correction — rain:** Hole's contaminated rain chiefly weakens and sickens *magic users*. It is NOT canon that every rainstorm inflicts mutation on all humans, nor that water automatically turns into Sludge. Those are possible localized optional gameplay events only; never make normal rain punish vanilla survival players.
- **Correction — names:** Maitake and Aitake are **sorcerer twin characters**, *not canon mushroom food species*. Use **Maîtake Mushroom** only as an explicitly Minecraft-original culinary species under another ID (e.g. `wild_fan_mushroom`); never label it a portrayal of the twins.
- **Correction — equipment:** Kaiman's ordinary combat knives are **not inherently enchanted**; his resistance to direct magic comes from extraordinary personal circumstances. An anti-magic weapon must be a rare new enchantment or separate non-canonical design, not falsely attributed to every knife.
- **Correction — personalities:** Kikurage is a cherished, rare, temperamental En Family creature, not a freely tameable world-spawned cat. Jonson is a **unique character** trained by Kasukabe, not a species; GuraGura is **Shin's former pet and a Devil**, not En's giant guard dog. The Gyoza Fairy is associated with Hungry Bug and ordinarily invisible to humans, not a naturally common kitchen mob.
- Do not make named nonhostile unique characters farmable loot sources. The world has original ambient rodents, roaches, insects, and microbes, all with their own crafted drops; keep those separate from named protagonists.
- All new mobs need original geometry/UV atlas/textures/animations/sounds/spawns/loot/useful recipes and runtime tests. Every new ore and block needs original texture, item model, blockstate, drops, tools/tags, worldgen, recipes and survival acquisition. Never claim a design section is working gameplay.
- World must remain buildable and mineable. All random structures/AI are deterministic where generation requires it, persistent, dimension-scoped, versioned, and tested for cross-chunk safety.

## 1. CORE DIMENSIONS & BIOME GENERATION

### 1A. The Hole — urban biome family

**[CANON]** A polluted, congested human city of cracked industrial buildings, decaying towers, dark corridors, rain contaminated by residual smoke, medical facilities for magic victims, a cemetery, Hungry Bug, and the Lake of Refuse / Department Store. No enormous solid sky roof. [Citations H1, H2, H3]

**[SOURCE]** Hole worldgen has earlier multi-district roads, smog sky, 30%-target sewer cave branches, natural deep caves/ore, irregular blocks, diagonals and ruins; newest floor-plan version 4 is included in this checkpoint. It is NOT yet built/run in Minecraft.

| Block or feature | Properties and texture concept | Functional behavior and survival use |
|---|---|---|
| `rotten_concrete`, `cracked_concrete`, `soot_brick` | Weathered porous masonry, cracked corner variants; original 16×16 atlases | Decorative full block, slabs, stairs, walls; mine with stone pickaxe; reusable stonecutting; worldgen replacement never prevents normal ore generation |
| `rusted_iron_panel`, `rusted_railing`, `corroded_grate` | Orange-brown corrosion, iron lattice, connected railing models | Iron pickaxe harvest; scrap recycling yields iron nuggets; railings retain vanilla collision and navigation tags; grates permit fluid drainage into catchment blocks |
| `sewer_sludge` | Dark opaque slow-flowing viscous fluid, surface film | **[ADAPTATION]** knee-deep slows walkers ~20%; filters/treating removes local hazard; buckets/canisters collect for bitumen and filter recipes; prevent flood spread into spawn and protected buildings |
| `contaminated_rock`, `smoke_salt_crust` | Pale bloom on masonry, black-purple inclusions | Scrape pollution reagent with brush; no instant conversion of natural diamond/iron ore; subtle particles at hotspots |
| `surface_drain`, `service_hatch`, `utility_pipe` | Functional block entities with retained collision rules | Connects drains to existing sewers; hatch opens with hand/tool; custom pipes route small fluid amounts without interfering with redstone |
| `waterproof_tar`, `asphalt_patch`, `clinker_tile` | Street repair, potholes, alley grime | Stonecut/building recipes; roadway detail must never trap players, block doors or force redstone malfunction |
| `abandoned_cabinet`, `supply_crate` | Durable clutter blocks with real container UIs | Loot from food/medical/industrial pools, cannot dupe on reload, unique loot seeded by position |

**District profiles [ADAPTATION]:** soot-black industrial smog/belts; tenement neighborhoods; low-rise commercial and markets; overbuilt central stores; derelict rubble and boarded windows; cemetery; canal/lowland stormwater; sewage treatment; outskirts containing rubble, farms, scrub, vanilla trees and mineral access. Preserve the 30%-target **long, sewer-first** gradual path to caverns, larger deep chambers, ordinary caves/ores, and normal mining. Build architecture with courtyards and non-square parcels but a connected route spine. No repeating checkerboard megablocks.

**Weather implementation:** Use server weather schedule already supplied by Minecraft. In Hole, `rain` normally continues vanilla weather and irrigation. A `tainted_rain` severity field (0–3) can be a **rare district-specific overlay** calculated at 5-s intervals with rain exposure and local smog density; at severity 1 apply `smoke_sickness` to sorcerers for 6 s, reduce their smoke regeneration 40%; at severity 2 impair casting 70%, inflict 0.5 health per 5 s to unprotected sorcerers; severity 3 is a rare Lake-centered event, can contaminate *exposed cauldrons/collectors* or standing non-source puddles, NOT globally convert existing rivers/farms. Humans can get a brief nausea/visual effect only in severe hotspots, never blanket damage every shower. Masks and treated armor reduce exposure but rain remains weather mechanically. Clients receive particle/fog cues; server owns penalties and world changes; gamerule/configurable overlay intensity. Natural vanilla rain is the normal state.

**Ore/underground plan [ADAPTATION]:** existing sulfur/rock-salt/limestone/zinc/oil-shale alongside unaltered vanilla veins. Additional pyrite for sulfur processing, fossil deposits for decorative and bone components, contaminated crystals for industrial filtration and portal stabilization, salvage pockets for recyclable iron/copper. Add natural glow fungi for underground food/light. The rare Magic Residue Crystals are concentrated near smoke dumps, but never generate higher than available vanilla diamonds to trivialize them.

### 1B. Magic-User / Sorcerers' World

**[CANON]** Created by Chidaruma. Unlike the Hole it ordinarily has **no precipitation**. More varied structure types, forests, separate cities, poverty and wealthy districts, devil worship ruins and En's immense mansion. The **Hydra Forest** is a real location connected to Haru and Cross-Eyes Black Powder operations. It is not canon that every tree there emits smoke; that is a custom visual motif. [M1, M2, M3]

**Worldgen [ADAPTATION]:** `sorcerers_world` dimension, terrain with atmospheric woodland and dark stone ridges; `sw_city`, `sw_guild_town`, `sw_hydra_forest`, `sw_mushroom_sprawl`, `sw_abandoned_ruins`, `sw_timberland`, `sw_estate`; large En Estate/Flower Smoke restaurant, smoke flask pawnshops, En Family checkpoints, Black Powder labs, Blue Night carnival grounds. Some **occasional** airborne bridges or supported sky villas in elite districts, not generic random floating islands throughout the lore world. Spawn natural wood, saplings, stone, coal, iron, water sources and farmable soil; preserve ordinary survival.

**Hydra vegetation [ADAPTATION]:** `hydra_wood` dark striped wood family: log, stripped log, planks, slab/stairs/fence/door/trapdoor/sign, sapling, custom leaf drops; `hydra_vein_leaves` foliage with a subtle low-frequency gray smoke mote, not permanent opaque fog; `twisted_root` edible for medicine after treatment; `smoke_lichen` low-luminance decoration; `wild_fan_mushroom` and `pale_capped_mushroom` new **original** fungi independent of the twins' names. Logs equivalent to vanilla for recipe tags, renewable through saplings, trees do not corrupt surrounding terrain.

**No rain** in this dimension under normal rules. Extremely rare rain connected to the Hole entity belongs to a **late narrative-inspired sandbox event**, not default weather. Door travel should preserve persistent linked positions, safety and player ownership; there are player Smoke Doors, craftable Artificial Doors, En Family transit checkpoints and Devil Gates; mounts/carpet provide local travel.

### 1C. Hell

**[CANON]** Chidaruma's realm of Devils and deceased sorcerers, with **horn-shaped mountains**, barrenness and punishment. Not simply a Nether palette swap; giant-versus-tiny scale is an important aesthetic. [D1]

**[ADAPTATION]** `hell_horns`, `hell_ash_valley`, `hell_undercourt`, `hell_devils_precinct`: bleak rocky highlands, bone dust, gravity-defying monuments, occasional devil training facilities, giant bat roosts. Real ores/rocks and pockets of survivable stone maintained. Hell portal requires research/ritual with opt-in client/server safety, no forced progression. Devils are rare named/special entities, not random trash mobs.

## 2. MAGICAL MECHANICS: THE SMOKE SYSTEM

### 2A. Player origins, smoke biology and balance

**[CANON]** Humans and sorcerers are biologically distinct. Each sorcerer generally has **one innate magic type**, smoke veins, a Devil Tumor, a separate output quantity and quality/purity. Bottled smoke can be traded and used. [S1, S2]

**[ADAPTATION] player choice:** Human and Sorcerer origins; both can survive normally, make farms, mine and fight. Sorcerer origin rolled/chosen at creation with immutable `innate_magic_id`, `smoke_capacity`, `smoke_quality`, `output_rate`, `vein_damage`, `mask_attunement`. Human origin has no ordinary smoke bar, but can use equipment and smoke flasks. Sorcerers do NOT unlock arbitrary different magic schools by XP. For multiplayer: persist in server data per player UUID, never trust client claims; backup/recovery on respawn and dimension move.

**Baseline numbers (balance prototypes, not lore):** capacity 100 units (weak 55, exceptional 150), regen 0.6 units/sec out of combat, max output 18 units/sec, purity multiplier range 0.75–1.25, smoke casting 5–30 units depending on native ability, internal 8–60 tick rate limits. Overuse >80% capacity in 10 s gives `smoke_exhaustion` (20–45 s). HUD displays both stored smoke and output/purity; menu shows one assigned innate type and ability applications, not a generic RPG spell tree.

**Smoke types** [CANON inspiration and gameplay adaptation]: Mushroom transformation (En), Healing (Noi), Dismemberment (Shin), Reptilian transformation (Ebisu), Curse (Risu), Destruction (Fujita), Dispel (Chota), Time (Nikaido, rare/limited), Pie transformation (Fukuyama), Metamorphosis (Maitake/Aitake), Resurrection (Kikurage; unique), plus common nonnamed types. Each magic is **one base power with several contextual applications** and source/target limits. Time-rewind: snapshot/limited-radius revert without item duplication; require server transaction audit and forbid restoration of loot picked up by others.

**Bottling:** `empty_smoke_flask` + sorcerer at `bottling_station` spends 30 smoke → signed `smoke_flask` component `{magic_id, producer_id, purity, quantity}`; allow spray consumption 1–3 uses, non-sorcerer can use simple compatible flask effects, binding remains server-validated. Stored smoke NEVER edits producer innate identity. Stored magic classified as healing/damage/transformation and can be copied by entities only via vetted context. Pay traders in coins/barter; no dupes through shifting containers.

### 2B. Black Powder and Devil Tumors

**[CANON]** Black Powder is illicit, associated with Devil Tumors and Cross-Eyes, boosting or granting smoke ability. [S3, S4] It is **not** a sulfur+charcoal gunpowder potion, nor a standard mineable ore.

**[ADAPTATION] obtain:** Black Powder as rare Cross-Eyes cache loot, smuggler trade or difficult lab production requiring a **rare ethically acquired storyline-neutral seized Devil Tumor specimen** (generic enemy), bottled catalytic smoke, sulfur processor and a reinforced sealed case. The extraction process is deliberately gated by rare materials and anti-duplication checks; no requirement to kill named neutral NPCs. Powder has 1 dose/stack cap 8; 2-minute +35% effect potency, +25% smoke output, then 90 s `withdrawal` (-15% movement, +40% spell cost, slower regen). Danger of addiction only as a reversible mechanical cost—no punitive long-lasting state. Humans using Black Powder gain a weak temporary preset effect, **not** permanent sorcerer biology or a second innate type.

**Devil Tumor:** Never a mundane common mob drop or mineral. In generic hostile sorcerer corpses, a *verified intact tumor* can be extracted with equipment. It belongs to the deceased character for revival (tumor-ID/reference, not freely cloneable). Damage/loss prevents ordinary Kikurage revival. No inventory dupes on death or rollback.

### 2C. Masks

**[CANON]** Store-bought/hand-made masks are common, devil-made masks are special and can bolster magic. [S5]

**Balance prototypes [ADAPTATION]**; attributes modify only the user's **own innate smoke type**, not grant new types:

| Mask | Acquire/recipe path | Armor / Toughness / Magic trait | Behavior |
|---|---|---|---|
| `patchwork_mask` | leather + fur + filter mesh | 1 / 0 / +0% | cheap dust reduction; can be repaired with leather |
| `street_respirator` | treated leather + zinc filter + cloth | 2 / 0 / +0% | -40% local smoke-sickness exposure; consumes filter cartridges |
| `crafted_sorcerer_mask` | leather + brass + woven silk + smoke bottle | 2 / 1 / +8% innate ability | moderate output efficiency; no faction disguise unless social systems allow |
| `devil_made_mask` | Devil NPC commission, reputation, rare catalyst | 3 / 2 / +18% innate ability, -10% cost | unique cosmetic design tied to owner; durable but not invulnerable |
| `en_family_commission_mask` | En Family reputation, elite mask craft | 3 / 2 / +12% innate magic, +10% capacity | grants social access to family doors, NOT automatic ownership or loyalty |
| `cross_eyes_disguise_mask` | cloth + paint + mask frame | 1 / 0 / +0% | faction deception only if observer AI has not identified the player |

**NOI/SHIN/EN masks** are NPC-specific unique art/reward displays, not universally mass-craftable canon replicas that grant their abilities. Enchanted/rare masks can require the blessing of a Devil rather than a craft-table recipe. Interaction with vanilla armor/toughness uses normal component/attribute stacking, balance against helmets and enchantments.

## 3. CUSTOM ENTITIES & FAUNA (MOBS)

All numbers here are **proposed 1.21.1 server-side balance values** against the vanilla 20 HP (10-heart) player baseline. Named characters may use protected or summon-only status. All require proper entity types, model layer, renderer, tracking range, spawn egg (testing), loot table, goals, animation states, registry attributes and GameTests.

### 3A. Six central canonical entities — full implementation contracts

#### Kikurage — unique En Family resurrection companion

- **Lore [CANON]:** small ~70 cm pink chimera, fluffy ears, cloven hind legs, stubby forelegs, tiny horns, devil tail, red mask; temperamental, connected to En and Ebisu; **white resurrection smoke**. Cannot simply heal damaged bodies. For sorcerers, Devil Tumor should be intact. [C1]
- **Model/animation [ADAPTATION]:** 0.65w × 0.72h; ~65 voxel cubes, layered fur UVs, 4-leg hopping, ears flick/tongue, defensive alarm, mask and thorn collar; white smoke during revival, separate shoulder and hind limb joints. Distinct from rabbit-like ordinary fauna.
- **AI:** protected unique faction `EN_FAMILY`; follows En/selected guardian; idle sniffs and plays with roaches, flees threats; does NOT globally spawn or tame with ordinary wheat. Reputation/questless permission + food treats may unlock escorted companion interaction; not an owned fighting pet by default.
- **Prototype:** 24 HP, 2 armor, 0 attack, 0.22 movement, vulnerability to ordinary damage, special protective collar one use reset by owner.
- **Resurrection engine:** Server persists `CorpseRecord` `{owner_UUID, death_position, dimension, death_time, life_state, tumor_integrity, injury_integrity}`. Trigger only explicit player/ally revive at corpse within 6 blocks; check enough body remains, sorcerer tumor intact, owner opt-in; consume **White Smoke Charge** from Kikurage's ability reserve (not a common mob drop); restore body to **injured state** requiring surgical supplies, not full HP; e.g. 4/20 HP, 20 s weakness, damage repaired by medical equipment. Avoid resurrecting a living concurrent body or duping player inventory/XP. Respect `keepInventory`, multiplayer logout, and dimension changes.
- **Loot:** no kill-required loot; reward is `white_smoke_preservation_sample` from a consensual supervised research action once per day, used to craft medical research equipment or a resurrection stabilizer; no infinitely farmed high-power cures. Named NPC doesn't naturally drop Devil Tumors or Black Powder on death.

#### Jonson — unique giant cockroach

- **Lore [CANON]:** enormous sewer-mutated cockroach with sneakers, striking humanlike teeth, huge durability; trained by Kasukabe; says “SHOCKING!”, can fly and communicate with insects. [C2]
- **Model:** 1.25w × 2.1h, long segmented limbs, worn sneakers, mandible/beak-like head, armored shell; hinged wings animate during rare flight, scuttling gait, sniffing, call animation. Native male voice bark optional licensed? **record original performance**, never copy broadcast voice assets.
- **AI:** unique `KASUKABE_ALLY` creature, initially territorial but not permanently hostile. Kasukabe has relationship authority; players can earn trust via **sneaker repair** and feeding or faction reputation, not instant shoe-taming. Defends its trusted circle; otherwise tolerates scavengers and preys on hostile threats.
- **Prototype:** 90 HP, 10 armor, 8 melee, 0.32 move, temporary short-range flight leaps; charge attack 3 s windup and 8 s cooldown.
- **Special:** emits SHOCKING subtitle and custom sound on alarm; follows insect scent, can open some simple latches after trust; cannot auto-bypass player protections or locked ownership doors. Rare damaged sneaker interaction grants `worn_jons_sneaker` as cosmetic trophy **without killing**; regular wild roaches still drop Chitin and Antennae for survival recipes.
- **Loot:** avoid naturally killable/respawnable boss farm; passive rewards from assisting Kasukabe: `cockroach_sense_note` → `pest_detector`, `sneaker_pattern` → durable footwear; real physical chitin comes from ordinary roaches, never require Jonson's death.

#### Hole — endgame sentient vengeance entity

- **Lore [CANON]:** emergent being arising from the Lake of Refuse and cumulative resentment/dead; hostile to magic users, sludge and corpse manifestations, anti-magic phenomena. NOT a typical slime and not a generic ritual boss in the manga. [C3]
- **Model:** world-event scale, 4–8 blocks in large phases, procedural sludge panels over structured skeletal core, numerous animated corpse-soul hands, unsettling soundscapes and fog. Performance LOD; bounding box for melee remains tractable.
- **AI:** an **optional late-game world event** near Lake/Department Store triggered by `lake_pressure` + deliberately crafted `refuse_lure` ritual; disable griefing by default. Prioritizes powerful sorcerers, later may attack everyone entering arena; non-casting humans can still be in danger from physical attacks.
- **Prototype:** multi-phase encounter totaling ~1,000 effective HP (not one permanently alive loaded boss), 12 armor, stage damage 7/11/15, movement 0.15–0.32; server scaled to players without trivializing single-player; upper hard caps on spawned adds.
- **Phases:** (I) manifest sludge 100%, hazards + 2 ancient dead; (II) corpse-orb projectiles at 70% health, sludge spreads **temporary blocks** in arena only; (III) sorcerer-tracking anti-magic pulses at 35%, portal echoes and altered gravity; defeat with protection of citizens + expose core, not just grind health. Large build damage optional only; grief default OFF; require cleanups and reset on restart, despawn adds correctly.
- **Loot [ADAPTATION]:** `lake_core_shard` (one per participant via persistent encounter ID) for advanced polluted-water filter / dimensional anchor; `ancient_grudge_essence` for protective medicine and decorative shrine; `refuse_sludge_bucket` for hazardous machinery; avoid wholesale loot farming; no Devil Tumor or Store's Knife as arbitrary corpse loot.

#### Hole Zombies — annual Night of the Living Dead

- **Lore [CANON]:** graveyard-centred annual outbreak from magic residue in Hole, linked to magic-practice victims. Graves include corpse identification plates; exterminators collect tags for prizes, monks use salt. [C4]
- **Model:** 4–8 original victim variants, hospital garments, numbered grave tags, smoke-affected textures, limping/running variation, separate head and limb actions; ordinary vanilla zombies remain possible elsewhere.
- **AI:** `HOLE_UNDEAD`; hunt living characters in event radius and in dark city routes, no automatic friendly faction assumptions. Pre-event town sirens/clock warning; countdown 11 PM; actual rise near midnight. Persistent `LivingDeadEventSavedData` ensures once per chosen Minecraft **year** (e.g. configurable 96 in-game days) per server, lasts one night plus dawn cleanup; server cap per region, scale by online players, never turn whole world into permanent zombie siege. Event off switch for sandbox servers.
- **Prototype:** 24–38 HP depending on victim variant, 2–4 attack, 0.22 movement, rare runner 0.3; no innate destructive explosion.
- **Loot:** `numbered_corpse_plate` 1 guaranteed per event zombie, spent at `cemetery_exterminator` for special food, tools, lamp components or shop decorations; `grave_salt` chance 20% used in undead protection and food; `undead_residue` chance 25% used for anti-zombie ward block / clinical reagent. No forced story/quest progression and no profitable spawn-egg tag duplication.

#### Sorcerers — biological faction NPC family

- **Lore [CANON]:** independent race with smoke organ/veins, Devil Tumor and one innate power; includes ordinary civilians, En Family, Cross-Eyes, elite fighters, masked merchants, and malicious human-experimenters; not all automatically hostile to humans. [S1, S2]
- **Model:** humanoid 1.8–2.1h, original clothing silhouettes per faction and rank, layered masks, unique hand-mouth emission joints; 24+ clothing/mask texture combinations; item-held weapons, smoke effect per ability and output state.
- **Factions:** `EN_FAMILY`, `CROSS_EYES`, `INDEPENDENT`, `CIVILIAN`, `RESEARCHER`, `OUTCAST`; persistent reputation with player, threat/retaliation linked to actions not inherent origin; PvP/friendly fire configurable. Do not treat all sorcerers as mobs for Black Powder farming.
- **Prototype:** weak civilians 18–24 HP/0–1 damage/0.20; street mage 24–36 HP/2–4 damage/0.24; enforcer 44 HP/5–8/0.29; elite 80–140 HP and unique AI, named special spawns strictly curated.
- **AI engine:** patrol/trade, seek shelter under Hole rain, target aggression based on faction/reputation, target smoke and cost through server capability data, use one native type with distinct tactics (line-of-sight, projectile, touch, summon). `DevilTumorState` independent of damage; deaths create corpse records as appropriate. No mandatory kill drop for moral-neutral villagers.
- **Loot:** traders offer `smoke_flask`, `mask_blank`, `smoke_filter`, `ritual_ink`; hostile generic enemies drop `smoke_vial_shards`, masks, and occasional `damaged_smoke_vein` sample, used in inspection device, smoke processing and medical instruments. Rare intact tumor extraction only from eligible generated corpses with checks; no harvest on friendly NPC without major faction consequences.

#### Devils — Chidaruma and other high-tier beings

- **Lore [CANON]:** Devils are extremely powerful transformed sorcerers (Chidaruma original), largely impervious to ordinary harm, devilmade masks, training and Hell oversight. Chidaruma is world-defining, not a random vanilla boss that drops loot on death. Store's Knife is uniquely significant. [D2, D3]
- **Model:** giants and varied unique masked/horned outlines; Chidaruma ~2.6-block h proportion with white skull and curled horns; custom skeletal rigs and non-standard idle/humor animations; projectiles emitted by gestures, not generic blaze fireballs.
- **AI:** `DEVILS` faction, generally neutral/chaotic observers; can bargain, test, prank or retaliate; rarely attack without narrative encounter. Chidaruma is protected world NPC with nonlethal interaction, no ordinary spawn eggs in survival. Other devils show in Hell, Blue Night, Devil Mask workshop, rare Devil Exam events.
- **Prototype:** Devil trainer 200–350 HP, 14–20 armor, 8–15 attack, 0.30 move, `invulnerable_to_normal_damage` semantic immunity in protected contexts; instead of impossible grind, trials are solved by tasks, stealth, trading or sanctioned arena bouts. Chidaruma has no ordinary HP bar and no loot table.
- **Mechanics:** `devil_mask_commission` reputation + rare offering → mask with bound attribute bonuses; Devil Exam optional long-term activity: heavy equipment (Slowness and strength training), bat care, controlled smoke abstinence, graduated rewards, conversion handled by explicit player choice/rule and irreversible-preview confirmation. No arbitrary permanent Devil transformation from killing a strong mob.
- **Loot:** NO Devil Tumors from Devil deaths; no farmable Chidaruma drops. Rewards from interactions: `devil_mask_seal` for commission and upgrades, `hell_sigil` for safe gate construction, `training_certification` for cosmetic/skill rewards. Strong items are paid/gifted/earned, not body loot.

### 3B. Additional named canon fauna & characters

| Entity | Canon and placement | Prototype mechanics / player benefit |
|---|---|---|
| **Gyoza Fairy** | Invisible tiny sentient gyoza of Hungry Bug; punishes cold neglected gyoza. [C5] | **unique kitchen ambient NPC**, no regular spawn egg in survival; 10 HP, noncombat/0.27 fly-hop, rare visible via `gyoza_incense`. Properly cooked gyoza near fairy gets +1 saturation or 60-s Comfort effect. Kitchen blessing obtained nonlethally and grants gyoza recipe insight, NOT edible fairy drops. |
| **GuraGura** | Devil formerly disguised as **Shin's** dog. [C6] | Unique non-hostile cameo or special event, shape-changing dog/Devil model with separate rigs; no tameable generic giant guard dog and no drops from killing. Contract interaction grants `devil_hound_token` for pet cosmetics. |
| **Giant bats** | Referenced as part of Devil Exam training. [D3] | 60 HP/5 attack, Hell roosts and exam chambers; **ordinary non-named** giant bat variants drop `giant_bat_wing_membrane` for breathable wingsuit/glider components and `bat_brush_fiber` harvested through grooming; do not trivialize elytra. |
| **Cross-Eyes elites / cleaners / named En Family** | Unique named sorcerers with established magic types and alliances | Spawn at home/appropriate faction locations or via curated contracts; no generic hostile named farming, no random assortment of all magic types. |
| **Mushroom Dolls** | En can animate fungal constructs. [M4] | Event-summoned 26 HP golem, 5 melee damage; drop `animated_mycelium` (one valid potion/trade recipe) if non-unique hostile copy; decays when source fades, no unlimited spawning chains. |

### 3C. Original survival fauna — implementation contract and drops

**[SOURCE in earlier passes, runtime not verified]** eight original mobs: Sewer Cockroach, Scrap Rat, Sewer Leech, Glow Moth, Sludge Crawler, Rust-Eater Beetle, Cavern Burrower, Spore Crawler. Their model/animation/drop/spawn code, source preview GIFs, spawn eggs and stage-12/13/14 compatibility must be tested in Minecraft. Distinguish these from unique canon characters. All have at least one functional drop path; high-quality voxel shape/texture and animation are required before release.

| Mob | Behavior | Unique thematic drops and functional uses |
|---|---|---|
| Sewer Cockroach | hostile sewer insect | `reinforced_chitin`: chest armor/industrial filters; `roach_antenna`: sewer echo scanner and damage detector |
| Scrap Rat | neutral scavenger | `rat_fur`: wool/insulation; `rat_meat`: cooked rations; optional nonlethal tame salvaging |
| Sewer Leech | water predator, enlarged model with **six large teeth max** | `leech_mucus`: healing salve/adhesive; `leech_sucker`: sticky boots or climb equipment |
| Glow Moth | passive cavern moth, hinged fore/hindwing animation and moving light | `luminescent_scales`: Night Vision/cavern lamps; `moth_silk`: string/textiles; **shear for scales nonlethally**, light level 12 temporary source with full cleanup and lag tests |
| Sludge Crawler | ooze-heavy sewer mutant | `sludge_gland`: lubricant/slime; `coagulated_tar`: waterproofing, paving, sealant |
| Rust-Eater Beetle | scavenger in caves AND derelict/industrial surface districts | `metallic_carapace`: plating; `corrosive_gland`: legitimate metal recycling; brush nonlethal. No free metal duplication. |
| Cavern Burrower | heavy digging creature | `burrower_claw`: survey pick/tool upgrade; `dense_hide`: leather/heat insulation |
| Spore Crawler | contaminated cavern fungus mutant | `spore_sac`: fungus agriculture/antidote; `fungal_membrane`: dressings/paper and decorative blocks |

**Future expansion required:** sewer fish, sludge crabs, fungal mites, subterranean snails; every new creature must ship with an original 3D model, texture, animation states, natural spawn conditions, gameplay drop table and ≥2 practical item uses or one major compelling one. A mesh without survival gameplay is not an implemented mob.

## 4. FLORA & INVASIVE FUNGI (BLOCKS)

### En's mushroom conversion — actual canon power, Minecraft protection rules

**[CANON]** En's ability transforms organisms/objects into fungus. It is NOT simply a naturally spreading vanilla mushroom ecosystem. [M4]

**[ADAPTATION]** `mycelial_conversion` block state spread by En or characters actually bearing Mushroom Magic, not random world decay. On contact, targeted eligible replaceable blocks mark `fungus_claim` source/purity, spread BFS up to 5 blocks/second in 8-block radius (max 100 active blocks per encounter); no air-to-fungus conversion outside surfaces; exclude bedrock, commands, chests, nether portals, protected claims, rare ore and custom machinery. Config allow effects on player-built blocks only if griefing enabled. Light and floor compatibility; every converted block has rollback snapshot, fixed TTL (60–180 s) unless intentionally cultivated; no permanent tick storm. Direct spell can cause `mushroom_incubation` to entities, damaging only when buildup threshold crossed. Dispel or antidote reverses after-cost. Conversion never grants generic sorcerers En's ability.

**Fungal block set:** `en_mycelium`, `en_fruit_body`, `mushroom_armor`, `mushroom_doll_root`, `sickly_spore_bloom`, `fan_mushroom_cap`, `pale_capped_mushroom`, `hydra_smoke_lichen`. Harvest by hand/shears for food/dye/fertilizer/building material. Replantable in trays/appropriate light/moisture, some non-spreading decorative counterparts. Animated mushrooms only in En zones or combat events.

### Food corrections and original recipes

**IMPORTANT:** Maitake and Aitake **are sorcerer twins who use metamorphosis**, not established canon mushroom species. [M5] Use real edible mushroom species (`wild_fan_mushroom`, `wood_ear_fungus`, `hydra_oyster_mushroom`) as clearly **original survival food plants**, not fake canon named twin mushrooms.

| Item/recipe prototype | Ingredients and workstation | Food/status effect/benefit |
|---|---|---|
| `wild_fan_mushroom_soup` | 2 wild fan mushrooms + bowl + rock salt | 6 hunger, 6.0 saturation, 90 s Comfort (+minor natural regeneration only when fed) |
| `hydra_fungus_stir_fry` | hydra oyster + carrot + cooked meat or tofu alternative + pot | 8 hunger, 10 saturation, 30 s Spore Resistance 1 |
| `medicinal_fungal_broth` | wood ear + clean water + leech extract + salt, brewing pot | 4 hunger, 4 saturation, reduce `mushroom_incubation` level by 1 |
| `special_gyoza` | dough wrapper (wheat) + pork/cabbage or mushroom filling + salt, skillet | 8 hunger, 9 saturation; near fairy qualifies for a rare Blessing effect |
| `fungal_glow_paste` | glow scales + mushroom pulp, workstation | placeable cavern lamp fuel 15 min; no free permanent moving light block entity farm |

## 5. LIVING ITEMS, TOOLS, & WEAPONS

### Fukuyama and the meat pies

**[CANON]** Fukuyama's innate Pie Magic transforms objects/people into meat pies; it is not established that every pie moves and hops as a living animal. [W1] Treat **Living Meat Pie** as an explicitly original **rare culinary mishap** inspired by that magic, not an asserted manga fact.

**[ADAPTATION]** `meat_pie` cooked recipe at food counter: 7 hunger, 8.4 saturation, 8 s eating, normal food stack 16. `animated_meat_pie` created only by Pie Magic + rare residue, if dropped spawns a tiny 6 HP neutral pie entity hopping within 8 blocks until collected or consumed; its inventory sprite can subtly blink, but it MUST NOT autonomously teleport between slots or steal inventory. 8 hunger, 9.6 saturation, 30 s Comfort; 10% chance 5 s Nausea if not fully cooked. Can be collected into its item form by right click, not replicated by unload/reload. No forced ingestion of transformed humans, no horrible food farming loop.

### Magic Carpet and broom (transport)

**[CANON]** Magic carpets are smoke-fueled flying taxis; they can run out and fall. Smoke-fueled flying brooms also exist. [W2, W3]

**[ADAPTATION]** `smoke_carpet` craft silk + wool + galvanized frame + smoke motor + 1 smoke flask (3×3 recipe), 2 riders, 5.5 blocks/sec cruising, lift 2 blocks/sec, descent max 3 blocks/sec, 1000 fuel points; burn ~1/s idle hover, 2/s flight, 3/s boost. Air control WASD/space/crouch, client prediction with server validation; out-of-fuel automatic **controlled descent** (no instant death) and visible warning. Cannot take passengers across dimensions without both consenting; unload rider resets to ground safely. `smoke_broom` 1 rider, cheaper/faster, less fuel. Vehicle cannot clip walls or load unlimited chunks or fly into protected region with no permission. Fuel burn uses consumed **bottled smoke**, accessible to humans too.

### Knives, weapons, equipment

**[CANON]** Kaiman fights with mundane combat knives, mastery is his, not an inherent enchantment. His magic immunity is an unusual personal condition with limits against secondary/physical effects. [W4, W5]

**[ADAPTATION]** `combat_knife`: crafted iron + leather/worn handle, 5 damage, 1.9 attack speed, 165 durability, repairable; `bayonet_knife` steel-plating + knife gives 6 damage, 1.8 speed, 320 durability; `cross_eyes_throwing_knife` recoverable projectile 3.5 damage, 2-second retrieval lock; `disruptor_edge` is a **rare original weapon upgrade** made from anti-smoke crystals, only interrupts an active spell for 2 s, **does not universally dispel all magic** and is not presented as Kaiman's canon weapon. Armor remains comparable to vanilla equipment.

**Important named specialty gear:** Devil Training Armor (extremely heavy, sustained training), Smoke Inspection Device, Devil Mask Workshop, Blue Night Contract paper, Devil Gate sigils, Store's Kitchen Knife as narrative/creative-only singular artifact; avoid randomly putting all of them in regular mob loot.

## 6. CUSTOM STATUS EFFECTS & PLAYER MUTATIONS

All numbers are Minecraft adaptations. Effects use NeoForge `MobEffect` registration and server-side ticking, not just tooltip descriptions; cure paths must exist. Do not silently assign an immutable origin change by drinking a potion.

| Effect | ID / duration / level | Server behavior, cure and balance |
|---|---|---|
| **Smoke Sickness** | `smoke_sickness` 6–30 s, I–III | Sorcerers exposed to Hole tainted rain: -20/40/70% smoke regeneration, slight weakness at II+, damage only at III; medical filter, clean shelter and brew cure; normal humans at worst harmless HUD blur in very severe area |
| **Mushroom Incubation** | `mushroom_incubation` 15–40 s, I–III | Stage I spore particles; II -10% sprint, small fungus bursts; III 1 damage/3 s only while dangerous source persists; `antifungal_poultice` / dispel removes; no permanent grief on player death |
| **Reptilian Transformation** | `reptilian_transformation` 2–8 min | Rare Ebisu smoke flask: minor armor/swim buffs, appearance overlay and movement tradeoff, NOT Kaiman's personal universal magic immunity; reversible by medicine/dispel, no new innate spell set |
| **Withdrawal** | `withdrawal` 90 s I–II | Post-Black Powder: +40% casting cost, -15% movement, reduced natural regeneration; duration capped, reversible by rest/medicine, no permanent addiction flag |
| **Smoke Exhaustion** | `smoke_exhaustion` 20–45 s | Cast output spike: max output -50%, no zero-HP collapse, restored by rest/food, masks modify efficiency only |
| **Gyoza Comfort** | `gyoza_comfort` 1–3 min | Small non-combat benefit from well prepared gyoza and fairy bonus; clear food hygiene feedback; no invincibility |
| **Pollution Exposure** | `pollution_exposure` 5–30 s | Local sludge/hotspot only: minor mining fatigue/hunger, blocked by correct filters/boots; this is **an additional gameplay interpretation**, not canonical universal mutation rain |

### Mutation rules

`reptilian_transformation` and fungus are temporary entity appearance/component overlays with original model layers. Do not award Kaiman's exceptional immunity to all users; if a sandbox-only origin `kaiman_like` exists, it should be deliberately chosen with visible tradeoff, not a casual potion reward. Expose opt-in player appearances rather than forced irreversible bodily changes on PvP targets. Mutations cannot persist improperly through death/logoff and must synchronize client model/attributes.

## Additional world contents from research (for subsequent content batches)

**Buildings and landmarks:** Hungry Bug and gyoza kitchen, Magic Victim Ward and Hospital, Kasukabe Research Manor with spatially strange interiors, Central Department Store over Lake, main cemetery and extermination pavilion, Noshinto Gong ring, En Mansion/estate, Blue Night carnival, Smoke Flask pawnshops, mask shops, En restaurant, Cross-Eyes warehouses, Hydra Forest research house, Devil Churches/Devil Exam bat roosts, Hell courts. Building interiors should include *real interconnected rooms*, rooms tied to occupation, access to basements but sewer caves kept natural. Optional named residents only after a proper NPC scheduler/questless interaction framework.

**Cultural/economic:** social reputation of En Family and Cross-Eyes; poverty/elite access; cooking, masks, medical services, smoke trade, resurrection as high-cost service, black-powder trafficking; consent in multiplayer for contracts, no forced permanent puppet control, Blue Night optional festival, Living Dead Day recurring event. Major story characters have uniqueness and continuity; do not respawn 30 Kens every chunk.

**Environmental flora:** native trees, scrub, street vegetables and rooftop crops in Hole, twisted forest timber and smoke-dependent fungi in Sorcerers World, Hell horn scrub and bat fodder; each tree uses vanilla log tags and renewable saplings, edible fungi have real recipes. Regular chickens/cows/wheat remain possible through districts or farming plots so player isn't punished for mod choice.

**Survival crafting dependency paths:** sulfur → industrial chemistry; salt → cooking/preservation/anti-undead; zinc → brass/galvanized panels and mechanical valves; limestone → mortar/concrete; oil shale → bitumen/sealants; metallic beetle shells → plating; leeches → salves/adhesives; glow moth scales → lamps/Night Vision; burrower claw → excavation/survey; spore crawler → cultivation/antidote. Magic smoke items and realm travel need several attainable routes rather than mandatory rare mobs. For every recipe, record shape/ingredients, block tags, station, output count, experience and unlock discovery; include matching recipes and datagen assets.

**Accessibility & survival:** do not auto-damage all players in every Hole shower; keep renewable basic wood/stone/iron/food. Use bounded particle budget, controlled mob density, safe light cleanup, chunk-safe generation, no permanently broken spawn points, no obligatory main quest, and test on dedicated multiplayer.

## Implement, validate and release gates

| Gate | Artifact | Test |
|---|---|---|
| Canon registry | `docs/spec/canon_entities.csv`, lore URLs | Every canon claim sourced; adaptation labeled; no invented twin mushrooms, universal Black Rain curse or magic knives |
| Worldgen | per-world codec, dimension data, biomes, structures, ores, roads | 10+ fresh seeds; neighborhood views; chunk order; roads and cave access; replay/restart no seams; preserve world mining/farming |
| Blocks/items | Registry, 16×16 art, 3D blockstate/model, tag, loot, recipe | Datagen, survival workbench and smelting, placed and mined, no missing texture; not arbitrary placeholders |
| Creatures | Unique named AI vs original fauna, 3D model, animation, eggs, drops | Real `runClient` and `runServer`, visible models, pathing/idle/attack, correctly gated loot, no named-farm incentives |
| Magic | persistent origin/type/output/purity, cost/regen, flasks | Server-authority, smoke stolen/duplicated cases, save/load, rain and masking, no ability cross-bleed |
| Resurrection | Corpse/tumor state, Kikurage companion interaction | No duplicated inventory, proper injury state, no revive with missing tumor, offline/respawn/relog/restart |
| Weather | Hole local overlay, normal rain retained | Weather, farms, sorcerer illness, protective filter, no blanket civilian damage, other dims unaffected |
| Events | annual Living Dead Night, boss optional, Blue Night optional | Event cooldown/restart, non-grief default, mob cap, time jumps, reward redemption once |
| Vehicles/portals | smoke carpet, broom, Artificial Door, Smoke Door, Devil Gate | server position/ownership, no flight wall clipping/dupe, fuel and safe fallback, paired return |
| Performance | budgeted conversion and lighting | 5–20 players dedicated server, 100+ mobs, chunk churn, scheduled light expiry, no server freeze |

**Present source baseline:** Pass 14 world generation + Pass 15 interior work; earlier sources include eight original mobs and custom underground materials, but **full NeoForge compile and Minecraft gameplay remain unverified** in this environment. Many entities and features in this document are **PLANNED ONLY**. This blueprint must NEVER be reported as a release checklist completed merely because the document exists.

## Research sources and verification links

Community wiki information is useful but not guaranteed perfect; prefer manga chapters / VIZ editions when a canon dispute matters. Primary citation paths below were independently searched Oct 31, 2026. Do not present raw fan speculation as undisputed lore.

- [H1 — Category:Hole](https://dorohedoro.fandom.com/wiki/Category:Hole) — rain, neighborhoods, human victims, Lake, locations.
- [H2 — Central Department Store](https://dorohedoro.fandom.com/wiki/Central_Department_Store) — market and Lake relationship.
- [H3 — Night of the Living Dead](https://dorohedoro.fandom.com/wiki/Night_of_the_Living_Dead) — yearly event, monks, salt.
- [M1 — The Magic User World](https://dorohedoro.fandom.com/wiki/Category:The_Magic_User_World) — landscape and architecture.
- [M2 — Magic User World weather](https://dorohedoro.fandom.com/wiki/Category:The_Magic_User_World) — normally no rain.
- [M3 — Hydra Forest](https://dorohedoro.fandom.com/wiki/Hydra_Forest) — locale/associations.
- [M4 — En](https://dorohedoro.fandom.com/wiki/En) — mushroom transformation and constructs.
- [M5 — Maitake](https://dorohedoro.fandom.com/wiki/Maitake) / [Aitake](https://dorohedoro.fandom.com/wiki/Aitake) — twin sorcerers.
- [S1 — Magic](https://dorohedoro.fandom.com/wiki/Magic) — smoke biology and one innate type.
- [S2 — Magic Users](https://dorohedoro.fandom.com/wiki/Magic_Users) — source and output levels, society.
- [S3 — Black Powder](https://dorohedoro.fandom.com/wiki/Black_Powder) — manufacture and use.
- [S4 — Devil Tumor](https://dorohedoro.fandom.com/wiki/Devil_Tumor) — resurrection, black powder.
- [S5 — Masks](https://dorohedoro.fandom.com/wiki/Masks) — quality and devil commission.
- [C1 — Kikurage](https://dorohedoro.fandom.com/wiki/Kikurage) — anatomy, character, White Smoke.
- [C2 — Jonson](https://dorohedoro.fandom.com/wiki/Jonson) — unique cockroach, Kasukabe, speech.
- [C3 — Hole](https://dorohedoro.fandom.com/wiki/Hole) — sludge, anti-sorcerer powers.
- [C4 — Night of Living Dead](https://dorohedoro.fandom.com/wiki/Night_of_the_Living_Dead) — cemetery outbreak.
- [C5 — Gyoza Fairy](https://dorohedoro.fandom.com/wiki/The_Gyoza_Fairy) — invisible Hungry Bug sprite.
- [C6 — GuraGura](https://dorohedoro.fandom.com/wiki/GuraGura) — Shin's dog / Devil.
- [D1 — Hell](https://dorohedoro.fandom.com/wiki/Hell) — horn mountains and scale.
- [D2 — Devils](https://dorohedoro.fandom.com/wiki/Devils) — devils and origin.
- [D3 — Devil Exam](https://dorohedoro.fandom.com/wiki/Devil_Exam) — heavy armor/bats/abstinence.
- [W1 — Fukuyama](https://dorohedoro.fandom.com/wiki/Fukuyama) — Meat Pie Magic.
- [W2 — Magic Carpet](https://dorohedoro.fandom.com/wiki/Magic_Carpet) — smoke-powered taxi.
- [W3 — Magic Broom](https://dorohedoro.fandom.com/wiki/Magic_Broom) — smoke-fuel vehicle.
- [W4 — Combat Knives](https://dorohedoro.fandom.com/wiki/Combat_Knives) — nonmagic weapons.
- [W5 — Kaiman](https://dorohedoro.fandom.com/wiki/Kaiman) — unusual immunity.
- [W6 — Blue Night Festival](https://dorohedoro.fandom.com/wiki/Blue_Night_Festival) — partner contract process.
- [W7 — Smoke Flask](https://dorohedoro.fandom.com/wiki/Smoke_Flask) — bottled powers.
- [W8 — Magic User Door](https://dorohedoro.fandom.com/wiki/Magic_User_Door) — unique designs and travel.
- [W9 — En Family Door](https://dorohedoro.fandom.com/wiki/En_Family_Door) — communal door.
- [W10 — Chidaruma](https://dorohedoro.fandom.com/wiki/Chidaruma) — original Devil.
