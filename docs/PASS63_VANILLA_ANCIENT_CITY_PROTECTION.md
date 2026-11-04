# Pass 63 — Preserve the vanilla Deep Dark and Ancient Cities

Status: **uncompiled source update**. Java stub fixture and offline source/data QA passed; no Minecraft/NeoForge runtime confirmation. **No separate Dorohedoro Forgotten City**: the user meant the *vanilla Ancient City*.

## Why this was necessary

The custom sewer/gallery system creates deep excavation at roughly Y=-36 to -50. Its changes are applied at Minecraft's SURFACE stage, before the FEATURES stage places vanilla structure pieces. While that ordering helps vanilla structures exist, the excavation could deform supporting terrain and adjacent natural spaces. The user requires the original Deep Dark, sculk, Wardens, and Ancient City to remain discoverable and intact.

## Actual changes

- Add `DeepDarkBuffer63`: same transformed humidity/temperature classification as `HoleBiomeSource` (`humidity < -0.22 && temperature < 0.0`), with nine sampled points around a chunk.
- New `world_detail_version=45` chunks precompute a single protection flag per chunk (not once per block).
- If the chunk is in/near the Deep Dark, `Canvas` does not replace blocks at adjusted Y <= -24. The *municipal upper sewers* near Y=49, city buildings, streets and lanterns are unaffected.
- Guard all block placement pathways: regular blocks, loot-container blocks+NBT, mob spawners+NBT and signs+NBT. These use the same true adjusted Y coordinate, including terrain relief.
- Existing version 0..44 saved worlds retain the previous geometry behavior. Old world biomes use the same Deep Dark climate rule as before.
- No changes to the vanilla dimension definitions, vanilla structure biome tags, loot, Warden mechanics, or sewer branch-frequency selector.

## Limits and tradeoffs

This is **a conservative worldgen buffer, not full collision detection against actual structure bounding boxes**. Deep Dark terrain is protected even when there is no Ancient City there. A deep custom gallery can end at a protected chunk boundary: it remains breakable terrain and does not gate vanilla completion, but this transition must be reviewed in-game. Ancient Cities may extend beyond the Deep Dark center biome, and Minecraft structures (including strongholds, trial chambers, mineshafts) require a separate structure-start/bounding-box collision test after a NeoForge build. Do not claim they are all protected based on this pass.

The project **still lacks true ocean regions**; normal vanilla ocean monuments, ocean ruins, shipwrecks and associated features are NOT yet assured. The End survival route and gameplay are NOT confirmed. The user's goal remains *complete vanilla 1.21.1 survival without techniques*, not a partial substitution with mod features.

## Completed offline tests

1. `bash tools/qa-pass63-buffer.sh`: compiled the real `DeepDarkBuffer63` class against minimal `Climate`/`DensityFunction` fixture stubs and passed 1,137 checks for biome threshold, nearby sampling, version gates, and Y boundaries.
2. `python tools/qa-pass63-source.py`: 21/21 integration checks passed, including all Canvas block-entity write pathways.
3. `bash tools/qa-pass62-all.sh`: vanilla structure eligibility, dimensions, old Pass61 sewer hazards checks passed.
4. `bash tools/qa-pass61-all.sh`: long historical chain passed through Pass43 placement before the runner hit its time limit. Remaining `bash tools/qa-pass43-all.sh` including regional Pass42, Pass41, static and Java fixtures passed in separate execution. See `Pass63_HISTORICAL_REGRESSION.log` plus `Pass63_FOLLOWUP_REGRESSION.log`.
5. JSON preset `world_detail_version` increased to 45; 0..45 `CitySettings` validator/codec accepts old versions. The established 30% cave-branch selection remains.

## Wednesday Minecraft build and QA priorities

- Restore the official Gradle wrapper and compile with Java 21, Minecraft 1.21.1 and correct NeoForge version. Run `runData`, `build`, client, server, then GameTests and scripted source tests.
- At least five different world seeds, different biome starts: `/locate biome minecraft:deep_dark`; `/locate structure minecraft:ancient_city`. Visit and inspect an *actual naturally generated Ancient City*: sculk shriekers, original Warden summoning, loot, surrounding terrain and holes from sewers.
- Use `/locate structure minecraft:stronghold`, `minecraft:mineshaft`, `minecraft:trial_chambers` and verify collision-free layout, looting and ordinary traversal.
- Inspect custom deep gallery where it meets protected Deep Dark. Confirm no blocked critical ladder route, headroom collision, or abrupt unsafe drop. Fix via structure-aware routing if needed.
- Do a no-technique ordinary survival run to the End, defeat the Ender Dragon, follow a gateway and find an End City; separately check vanilla Nether structures, temples and trial chamber content.
- Genuine oceans, coastal terrain and ocean structures need further engineering before a **complete vanilla compatibility** claim.

No source-only test proves that a Warden spawns naturally, that a real stronghold End portal is intact, or that Minecraft can be beaten.
