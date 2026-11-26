# Dorohedoro: The Hole — authoritative Wednesday handoff, Pass 68

**As of Tuesday, November 3, 2026.** Use **Pass68 complete source ZIP**, not the Pass67 patch or any older baseline.

## Your first task: make the mod actually compile and run

1. Set up **JDK 21** and restore the missing `gradle/wrapper/gradle-wrapper.jar` from the verified official Gradle 9.2.1 distribution/source; use the provided checksum-verified Windows recovery scripts. Check `gradle/wrapper/gradle-wrapper.properties` and `build.gradle` first. Do not silently use arbitrary Internet binaries.
2. Compile and fix real **NeoForge 21.1.265** API/registry/data errors, then run `runData`, client and dedicated server as applicable. Do not claim a build is successful until its log shows success and an installable JAR exists.
3. Reproduce offline source/unit checks: `bash tools/qa-pass68-pump-halls.sh`, `python3 tools/qa-pass68-source.py`, `bash tools/qa-pass67-all.sh`. If the historical chain is slow, split at `bash tools/qa-pass50-all.sh`. All official references are documented in `docs/PASS68_STORMWATER_PUMP_HALLS_AND_MANUAL_CONTROLS.md`.

## What changed since Pass67

- `StormwaterPumpHalls68.java`: 14×19 large stormwater hall off the municipal sewer, 3-wide dry entry, contained sump + solid footbridge, useful repair workstations, optional operator booth with adjacent lever/iron door. Deterministic long trunk sections, canal/junction/coast exclusions, chunk independent, no forced techniques or added lamps/chests/spawners.
- `SewerServiceBays67.java`: v50-only reservation so smaller service rooms do not overlap the new halls.
- `CityPainter.java`: painter call after service bays, before maintenance/cistern/cave/dungeon later operations.
- `CitySettings`/normal world preset: fresh detail version **50**, old settings default 0, version max now 50. Pass41–67 history/source checks adapted for the new maximum, not legacy worldgen behavior.
- New QA scripts, docs, and this handoff. **Source only, uncompiled against NeoForge.**

## In-game acceptance (must not skip)

- On a **new v50** world, inspect 10 varied seeds with coherent city layouts, different climates, nighttime mob spawns, furnished/accessible homes/shops/factories and practical loot, safe land starting regions, real ocean and coastline, and mineral/cave generation. Compare negative coordinates and 16x16 chunk seams.
- In **no-techniques Survival** with iron armor/tools/torches/food: enter municipal sewers, survive darkness and creatures, find a repair bay and a pump hall, confirm real usable interiors; operate the optional iron door via lever, move over the dry bridge, retreat without getting stuck, check can exit the sewer. Ensure the lever powers the door in *real Minecraft*; source fixture only asserts block placement.
- Verify **30% selected sewer cave junctions actually connect** to traversable vanilla caves — selection itself is not enough. Ensure they do not excavate existing Ancient Cities, stronghold End portal rooms, trial chambers, or mineshafts; check real structure starts/rooms, not just `/locate` output.
- Preserve all vanilla survival and advancement mechanisms, including Nether travel/fortresses/blazes, stronghold Eyes of Ender and End portal, Ender Dragon/End Cities/shulkers, Warden/sculk in naturally generating Deep Dark Ancient Cities, Netherite, ocean monuments/shipwrecks/ruins, trial chambers/Breeze/vaults, villages/temples/ruins. Player can beat Minecraft without magic.
- Test backups of prior worldDetailVersion 49 saves; new structural geometry MUST NOT be retroactively added to old saves.

## Current unresolved issues / do not make claims yet

- `gradle-wrapper.jar` absent in checkpoint; no completed NeoForge or actual client build. Offline Java stubs are not proof Minecraft blockstates compile or operate; real door lever activation is unverified.
- Real vanilla End run, Warden spawns, ocean structure spawn and cave link quality still unproven.
- Hall floors may be affected by terrain-relief offsets; protected vanilla structure volumes may clip custom rooms. Verify visual/intersection QA before adding more content.
- No real dynamic pumping or floodgate; Pass68's lever is for an optional operator-room **iron door** only.
- **No custom Forgotten City project**: user meant vanilla Ancient Cities.

Preserve prior user constraints: survival-first, irregular densely inhabited The Hole in all climates with genuine purpose per building; few exterior chests/lights, night mob threat; no healthy trees, functional dead timber; no premature villager/faction overhaul, Ash-Snow/Sludge-Ice environmental effects or poisoning; lore-accurate magic with only 3 equipped techniques; every vanilla mechanic should remain usable.
