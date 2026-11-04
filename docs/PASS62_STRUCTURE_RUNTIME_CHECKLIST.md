# Vanilla 1.21.1 structure verification: commands & checklist

Source eligibility is **not** runtime confirmation. Execute on a fresh *Dorohedoro: The Hole* preset world after a real NeoForge build. Record seed, X/Z, structure presence, intact entrance, internal loot/mobs and collisions. Test at least five seeds.

**Overworld underground:**

```mcfunction
/locate structure minecraft:stronghold
/locate structure minecraft:mineshaft
/locate structure minecraft:ancient_city
/locate structure minecraft:trial_chambers
/locate biome minecraft:deep_dark
```

**Overworld surface/archaeology:**

```mcfunction
/locate structure minecraft:desert_pyramid
/locate structure minecraft:jungle_pyramid
/locate structure minecraft:igloo
/locate structure minecraft:swamp_hut
/locate structure minecraft:trail_ruins
/locate structure minecraft:woodland_mansion
/locate structure minecraft:pillager_outpost
/locate structure minecraft:ruined_portal
/locate structure minecraft:village_desert
/locate structure minecraft:village_plains
/locate structure minecraft:village_snowy
/locate structure minecraft:village_taiga
```

**Vanilla Nether and End (inside each realm):**

```mcfunction
/locate structure minecraft:fortress
/locate structure minecraft:bastion_remnant
/locate structure minecraft:end_city
```

**Ocean content gap (must not mark passed until proper ocean generation exists):**

```mcfunction
/locate structure minecraft:monument
/locate structure minecraft:shipwreck
/locate structure minecraft:ocean_ruin_cold
/locate structure minecraft:ocean_ruin_warm
/locate structure minecraft:buried_treasure
```

**Full survival End test**: save a new world, no techniques and no cheats, start with empty inventory, gather wood and food, find lava/obsidian, ignite ordinary Nether portal, defeat blazes and collect pearls, throw crafted Eyes, locate a stronghold, activate intact portal, defeat Ender Dragon, follow End gateway and find/loot an End City.

**Deep dark test**: find a *natural* Ancient City, interact with its sculk shriekers until the Warden emerges, confirm biome, sculk and damage mechanics; do not use a spawn egg or `/summon` to fake success.

**Trial chamber test**: fight Breeze and Bogged, activate trial/ominous spawners and vaults, obtain required items for a mace (Heavy Core from ominous vault and Breeze Rod), and craft the mace in ordinary survival.

**Clarification (Pass 63):** User meant the VANILLA Ancient City when saying "Forgotten City". There is no separate custom Forgotten City in scope. Test the vanilla Deep Dark, ancient cities, sculk sensors/shriekers, and Wardens in Minecraft.
