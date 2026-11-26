# Pass 78: the deferred features (2026-11-25/26)

The owner asked for the features held back since Pass 18: Ash-Snow, Sludge-Ice, environmental poisoning, Cross-Eye
camps and the villager/faction overhaul. Worldgen is behind `world_detail_version` **56**; older saves generate exactly as
before. **Art status:** every new look is built on a vanilla texture (see below). In-game previews went to the owner; the
Cross-Eyes look was approved with two changes (canon eye tattoos, no Steve), both made.

Owner rules that shaped this pass:
- New blocks are built on the vanilla block they resemble, and vanilla is the quality bar: Ash-Snow is vanilla snow,
  Sludge-Ice is vanilla ice, and the ash snowball is the vanilla snowball. Each model references the vanilla texture and
  `client/HazardColors` tints it (Ash-Snow a warm ash gray, so it reads as the same snow, sooted; Sludge-Ice an olive
  brown). No vanilla image is copied and no overlay is drawn on them.
- The Sorcerers' World stays clean. Every hazard is limited to the Hole: toxin weather only in the Overworld's Hole
  biomes, hazard worldgen only in the Hole city, smog only in the Overworld.

## Ash-Snow

- **Blocks:**
  - `dorohedoro:ash_snow`: layers 1–8, vanilla `SnowLayerBlock`. It stacks, melts under bright light and drops like snow.
  - `dorohedoro:ash_snow_block`.
- **Drops:** with a shovel, one ash snowball per layer, or the layers themselves with Silk Touch. The block drops 4
  snowballs.
- **Recipes:**
  - 4 ash snowballs make 1 block.
  - 3 blocks in a row make 6 layers.
- **Worldgen, v56:** cold districts are the frostbitten districts and the Snowfields region.
  - Ash-Snow drifts 1–3 layers deep against building walls along the pavement. Road centers stay clear.
  - The old vanilla-snow dusting on cold pavements becomes Ash-Snow.
  - Vanilla snow still blankets the rest, so the gray drifts read as soot settled out of the smog.
- **Hazard:** standing on it barefoot builds Hole Toxin exposure (+8 a second). Any boots stop it.
- **Code:** `ModBlocks.ASH_SNOW`, `worldgen/city/ColdHazards56`, `WinterPavement17` and `RegionalSurface42` (v56
  branches).

## Ash snowball

- `dorohedoro:ash_snowball`, thrown like a snowball by hand or by a dispenser.
- It does no damage, but slows what it hits for 3 seconds. A player hit takes +120 exposure.
- **Code:** `hazard/AshSnowball`, rendered with vanilla's thrown-item renderer.

## Sludge-Ice

- **Block:** `dorohedoro:sludge_ice`, vanilla `IceBlock`. It is slippery, melts into water under bright light, and only
  drops with Silk Touch.
- **Recipe:** it smelts into Coagulated Tar, which is fuel and makes torches.
- **Worldgen, v56:** irregular floes over about half of every canal surface in cold districts. Snow cannot settle on it
  (`#minecraft:snow_layer_cannot_survive_on`), the same as vanilla ice.
- **Hazard:** standing on it barefoot builds exposure (+10 a second). Any boots stop it.

## Environmental poisoning: Hole Toxin

- **Exposure:** 0–1000 per player (`hazard/ToxinExposure`). It is synced to the HUD as a percentage chip next to the
  armor chip and resets on death. Creative and spectator players are never exposed.
- **Sources:**
  - Ash-Snow or Sludge-Ice underfoot without boots.
  - Ash snowball hits.
  - Ash-laden snowfall in the Hole (+2 a second; a helmet stops it).
  - The Hole's rain, for sorcerers only (+4 a second, +2 with a helmet). Canon: the Hole's rain harms sorcerers, not
    humans.
- **Recovery:** it falls by 6 a second out of contact, or 12 a second under a roof.
- **Warning:** at 150 an action-bar message names the cause and the fix.
- **Effect `dorohedoro:hole_toxin`:**
  - Level I (300+): faster hunger.
  - Level II (600+): also −1 attack damage.
  - Level III (900+): also 1 damage every 4 seconds, never below 1 heart. Its damage type bypasses armor and causes no
    knockback.
- **Sorcerers:** each level costs one point of smoke regeneration.
- **Cures:**
  - The Antidote Poultice clears both the exposure and the effect.
  - Milk clears the effect and halves the exposure.

## Cross-Eye camps and the Cross-Eyes

- **Worldgen, v56** (`worldgen/city/CrossEyeCamp56`):
  - One in 20 qualifying vacant or rubble lots, at least 15×15, in derelict, outskirts or industrial districts, and at
    least 700 blocks from the starting point.
  - Layout:
    - a two-high wall of rusted sheet and patchwork masonry
    - a three-wide gate on the street side, with a red plus-shaped cross (十) painted on a panel each side
    - a campfire with three log seats
    - two lean-tos with bedrolls and supply barrels (loot table `chests/cross_eyes_camp`)
  - Regional decoration and yard clutter skip camp lots.
- **The gang arrives** the first time a non-spectator player comes within 32 blocks. A hidden `camp_marker` block under
  the floor spawns 3–4 members tied to the camp, then turns back into dirt.
- **`dorohedoro:cross_eyes`** (`entity/CrossEyesEntity`):
  - A humanoid with an iron sword, iron axe or stone sword, 24 health and 2 armor. It stays near its camp and never
    despawns.
  - A member who is hurt alerts the rest. It is not a natural spawn; there is a spawn egg.
  - **Drops:** scrap, iron nuggets, sometimes Smoke Residue (12%, more with Looting), rarely an emerald.
  - **Standing:**
    - Hostile on sight below Cross-Eyes standing 20.
    - Tribute: hand an emerald to a member for +6 standing, up to 4 a day.
    - At 20 they stand down. At 40 they trade, restocking daily:
      - 10 scrap for an emerald
      - a Smoke-Soaked Filter for 2 emeralds
      - Smoke Residue for 3 emeralds
      - a Devil Sigil for 16 emeralds and a gold ingot, once a day: a renewable, pre-Hell source, as the sandbox
        directive asks
    - Hitting one costs 5 standing; killing one costs 10 and gives +3 Hole resident standing.
- **Art:** each member wears one of eight vanilla default player skins (Alex, Ari, Efe, Kai, Makena, Noor, Sunny,
  Zuri; not Steve, by the owner's choice), picked at spawn and saved. Over it, `textures/entity/cross_eyes_overlay.png`
  (from `tools/ui/gen-cross-eyes.mjs`) adds the canon mark: a red cross tattooed through each eye, centered on the pupil
  (all default skins share the eye row). As in the series, members are otherwise ordinary people. The overlay also
  adds a red scarf, the cross painted on the back and a red armband on the skin's outer layers.

## Villager and faction overhaul

- **Standing** (`faction/FactionStanding`): saved per player and kept through death. Each standing runs from −100 to 100
  and is shown in the Status tab (Hated, Disliked, Neutral, Liked, Trusted), with an action-bar message when a
  threshold is crossed.
- **Hole residents:**
  - +1 per villager trade, up to 5 a day.
  - +20 for Hero of the Village.
  - −20 for killing a villager, −10 for a wandering trader or a natural iron golem.
- **Prices:** villager prices shift with standing (`mixin/VillagerPricesMixin`, applied after vanilla's own gossip and
  Hero discounts): −20% at 60+, −10% at 30+, +20% at −30 and below, +40% at −60 and below. At −80 residents refuse to
  trade. Villagers from other mods that extend vanilla's (MCA Reborn, for example) are covered too.
- **Hole trades on vanilla tables** (they buy or sell Hole goods, so emeralds stay worth earning):

  | Profession | Trades |
  |---|---|
  | Toolsmith | Buys scrap and raw zinc |
  | Cleric | Buys leech mucus and spore sacs; sells Antidote Poultices and Medical Salve |
  | Butcher | Buys rat meat; sells Salted Rat Rations |
  | Mason | Buys limestone; sells sewer brickwork |
  | Armorer | Sells galvanized plating |
  | Wandering trader | Sells Smoke-Soaked Filters (common) and Smoke Residue (rare) |
- **Recovery:** every route has a way back, through trade, tribute or defending a village.

## Tests (real client, `./gradlew runQaClient -Pqa_mode=<mode>`; delete `runs/qaclient/saves/clientqa-8675309` first)

- `hazards`: a generated drift and a floe are found and photographed. Also checked: exposure builds barefoot, boots stop
  it, the effect applies, and the antidote clears it.
- `crosseyes`:
  - the member from four angles
  - a generated camp is found, built and photographed
  - the gang arrives and is hostile at 0; tribute makes them stand down; the daily cap holds
  - trading opens at 40, and a kill changes standing
  - villager prices follow standing; Hole trades are present; a hated player is refused
- **Server QA, v56:** seeds 8675309, 12345 and 42 show only the known non-bugs. 8675309 generated in reverse gives
  169/169 identical city-layer chunk hashes.

## Not done yet

- The Cross-Eyes fight in melee only. A smoke technique for them (the Black Powder angle) is future work.
- Standing with the En Family needs the En Family's own content in the Sorcerers' World.
- Iron golems do not yet react to Hole resident standing (vanilla golems still use vanilla gossip).
- Black Powder caches and a "Living Dead" event remain design notes only.
