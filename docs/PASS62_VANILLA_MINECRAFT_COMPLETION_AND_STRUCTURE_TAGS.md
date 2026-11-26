# Pass 62 — Preserve Minecraft 1.21.1 survival and progression

Status: **SOURCE ONLY / NOT COMPILED / NOT VERIFIED IN MINECRAFT**. Built on Pass 61. This pass corrects structure eligibility and a headless-QA regional-spawn regression. It does **not** establish full vanilla parity by itself.

## User's non-negotiable requirement
A normal survival player, **using zero Dorohedoro techniques**, must be able to do all normal Minecraft 1.21.1 progression, including reaching and defeating the Ender Dragon. All content included in **Minecraft Java 1.21.1** should remain obtainable and mechanically functional. Later Minecraft releases are *outside* the fixed 1.21.1 target; updating versions requires a separate upgrade.

Clarification (Pass 63): The user meant vanilla **Ancient Cities** when referring to a "Forgotten City". There is no separate custom Forgotten City planned. Preserve the official deep-dark structures containing sculk sensors/shriekers and the Warden.

## Changes delivered

1. `ModDataGen.HoleBiomeTags.vanillaSurvivalStructures()` now appends 19 proper 1.21.1 `minecraft:has_structure/...` biome-tag extensions for region-appropriate *land* structures: desert pyramids, igloos, jungle temples, swamp huts, trail ruins, five village layouts, pillager outposts, woodland mansions, badlands mineshafts, several ruined portals, and trial chambers.
2. Generated datapack JSON in `src/generated/resources/data/minecraft/tags/worldgen/biome/has_structure/` mirrors the datagen policy, with `replace: false`. Running `runData` must preserve those values and the existing vanilla tag members.
3. Existing `minecraft:is_overworld`, `minecraft:stronghold_biased_to`, `minecraft:has_structure/mineshaft`, and deep `minecraft:has_structure/trial_chambers` are retained. Ancient city eligibility is **not** artificially expanded outside vanilla deep-dark biome.
4. Vanilla `minecraft:the_nether` and `minecraft:the_end` dimension definitions in the normal preset are unchanged. Dorohedoro Hell remains a **different** realm, not a substitute for the Nether.
5. `HoleQa.checkSpawn` now verifies the seed-selected spawn (Pass 47+) rather than demanding origin. When headless tests sample the **origin city** while the player spawns in a remote biome, connectivity analysis anchors to the original plaza instead of indexing an out-of-bounds grid. The true regional spawn still gets checked for standability separately.
6. `tools/qa-pass62-vanilla-progression.py` compares the Java datagen policy against actual bundled JSON and asserts preservation of mandatory vanilla dimension/biome/cave relationships. Run `bash tools/qa-pass62-all.sh`.

### Save compatibility, important caveat
This pass **does not** modify `world_detail_version`: biome tags are global datapack rules rather than per-save generator options. Previously generated chunks stay untouched, but **new chunks generated in existing saves may become eligible for these vanilla structures**. Back up existing worlds before updating. Visual collisions with Hole streets and buildings require runtime tests; see below.

## Vanilla 1.21.1 survival requirements — not complete until tested IN GAME

| Category | Acceptance criteria (ordinary survival, no techniques) | Current status |
|---|---|---|
| Surface survival | Spawn on dry, stable terrain; obtain logs, stone, food, wool, wheat/seeds, crafting table, furnace, coal and iron; preserve animal breeding and villager trading | **Unverified** |
| Caves and mining | Mine iron, diamond, redstone, lapis, gold, copper, emerald; find geodes/amethyst, lush caves, dripstone, lava, water, caves and caves opening into sewers; preserve normal mob spawning | **Unverified**; vanilla noise/cave biomes in source |
| Mineshafts | `/locate structure minecraft:mineshaft`; access rails, chests, cave-spider spawners; badlands mineshaft variant if appropriate | New tag eligibility; **unverified** |
| Strongholds | Obtain ender pearls and blaze rods; craft Eyes of Ender; eyes locate a **real** stronghold; portal room contains intact 12-frame ring and can be activated with 12 Eyes | Existing stronghold biome tag; **unverified** |
| Vanilla Nether | Construct and ignite a standard obsidian Nether portal; enter actual `minecraft:the_nether`; find blaze spawners, Nether fortress, bastions, nether wart, soul sand, piglins, ancient debris | Vanilla dimension retained; **unverified** |
| The End | Portal teleports into `minecraft:the_end`; defeat dragon; end exit portal opens, dragon egg and advancement work; outer gateway opens; End city, shulkers, elytra, chorus obtainable | Vanilla dimension retained; **unverified** |
| Deep dark / Warden | `/locate biome minecraft:deep_dark`, `/locate structure minecraft:ancient_city`; find natural sculk shriekers, echo shards; trigger Warden and its original sensing/sonic-boom behavior | Biome source retains vanilla deep dark; **unverified** |
| Trial chambers (1.21) | `/locate structure minecraft:trial_chambers`; trial spawners, vaults, trial keys, Breeze/Bogged, omen events; acquire breeze rods/heavy core and craft mace | Expanded biome eligibility; **unverified** |
| Land landmarks | Desert/jungle temples, igloos, swamp huts, trail ruins, woodland mansions, pillager outposts, villages (where compatible) with original loot/traps/inhabitants | Extended biome eligibility; **unverified** |
| Ocean exploration | Large oceans + beaches; monuments/elder guardians, shipwrecks, ocean ruins, buried treasure; dolphins and ocean wildlife | **NOT SATISFIED**: custom overworld currently has canals, not real ocean biomes. Must implement actual ocean regions and test. Do not fabricate monuments in canals. |
| Custom sewers | Tunnel network and optional roach nests dangerous without techniques; 30% cave-branch selection; real (not fabricated) Minecraft cave links | Existing policy and QA; **full cave-link success not verified** |

## Wednesday NeoForge build & playtest recipe

1. Restore `gradle/wrapper/gradle-wrapper.jar` from Gradle's official verified distribution (use the existing project recovery script); **verify checksum**, do not substitute unknown JARs.
2. Run `./gradlew runData`; then `python3 tools/qa-pass62-vanilla-progression.py` to ensure datapack generated and Java datagen exactly agree. Run `./gradlew build`, `./gradlew runServer`, and `./gradlew runClient` on Java 21.
3. Confirm **new world** uses the actual Hole world preset; use a reproducible seed record. Check both a dense climate and a rural climate, and at least one seed with a remote starting biome. Run dedicated server `qaServer` / the existing `HoleQa` facility.
4. On a temporary copy with cheats enabled, record `/locate biome minecraft:deep_dark`; `/locate structure minecraft:ancient_city`, `minecraft:mineshaft`, `minecraft:trial_chambers`, `minecraft:stronghold`, `minecraft:desert_pyramid`, `minecraft:igloo`, `minecraft:trail_ruins`, `minecraft:woodland_mansion` and temple/village variants. The command finding a structure is **necessary but not sufficient**: teleport to it and inspect the actual blocks, hostile mobs, loot, and neighboring terrain for collisions.
5. Test an **actual** Warden trigger from naturally generated shriekers in an ancient city (not a spawn egg). Check no hostile city creatures spawn inside ancient cities in violation of vanilla spawn prevention.
6. In normal Survival with techniques disabled, reach the Nether with **ordinary obsidian + flint and steel**, obtain blaze powder, obtain ender pearls, throw Eyes of Ender, enter an intact stronghold portal, defeat the dragon, traverse the gateway, and loot a normal End City. Do not use `/give`, teleport, or prebuilt custom doors in the actual completion run.
7. Test vanilla raids, villager trading, farming, enchanting, brewing, fishing, animal breeding, redstone, and archaeology. Preserve actual obtainable resources; do not merely check recipe JSON existence.
8. Verify oceans and beaches **are missing** or present using biome and terrain inspection. If absent, ocean content remains a hard blocker and must be implemented as genuine Minecraft ocean regions in a subsequent pass.
9. Check interactions with the city surface placer: temples/strongholds/ruins must not be sliced off, blocked by buildings, replaced by sewer tunnels, or obscured by heavy foundations. Audit 5+ seeds and far distances before claiming full parity.
10. Retest existing saves with backups: these resource-tag changes apply to **new chunks** even on older saves. Existing already-generated chunks must remain unchanged.

## Risk / no false claims
* **Not compiled** (Gradle wrapper missing). No real Minecraft generation, no Warden, no dragon fight, and no item drop testing occurred in this environment.
* Existing `HoleBiomeSource` uses custom climates and never returns vanilla ocean biomes. The current land-structure tag changes **cannot** make ocean monuments, shipwrecks or true vanilla ocean ecosystems appear.
* Biome eligibility does not guarantee that any specific structure generates or remains intact: spawn rings, placement spacing, terrain/height checks, worldgen order, custom surface painting and overlapping structures all matter.
* Deep-dark occurrence is defined by a custom humidity/temperature threshold at Y < -24. Ancient-city frequency and Warden sculk progression must be verified in actual generated chunks, not inferred from source.
* Some vanilla temples are mapped to the nearest Hole-appropriate region (e.g., jungle temples in unhealthy taiga). That keeps their survival gameplay potentially accessible but is a deliberate thematic adaptation, not a recreation of all vanilla climates.
* No poisoning, forced-technique gates, new faction AI, or deferred features were implemented.
