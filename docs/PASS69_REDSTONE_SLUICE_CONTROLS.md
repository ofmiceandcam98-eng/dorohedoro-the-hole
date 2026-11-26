# Pass 69 — Real Vanilla-Redstone Sluice at Stormwater Pump Halls

**Date:** 2026-11-03. **Target:** Minecraft Java 1.21.1, NeoForge 21.1.265. **Release:** source only, NOT compiled against NeoForge and NOT tested inside Minecraft.

## Purpose

Pass 68 constructed rare 14×19 pump galleries with useful repair workstations and a separate optional lever-operated operator-room iron door. Pass 69 adds a small, physically controllable *one-block-wide* runoff outlet to the existing sump. No magic technique, custom fluid type, world tick handler, modded block, or extra spawner is needed. The outlet is an optional interaction: it does not obstruct the three-block-wide public entrance, the main seven-block municipal sewer, the central dry bridge, or the useful repair/filtration equipment.

## Intended vanilla operation (needs in-game verification)

- The original water source sits in the sump at local **distance 13, offset −2, Y=49**.
- Two lined downstream channel cells at **distance 14–15, offset −2, Y=49** start as air and receive flowing water from the upstream source when the gate is open.
- The sticky piston base is at **distance 14, offset −4, Y=49**, facing toward the water outlet; a movable vanilla iron block starts in front of it at **offset −3**.
- A floor lever is at **distance 14, offset −5, Y=50**. It is mounted on a solid stone brick at Y=49 directly adjacent to the piston: lever power travels through that block. Toggling the lever *on* should extend the piston and push the iron block into the channel at offset −2 to stop flow; toggling *off* should retract the block and allow the source to refill the channel.
- The original Pass 68 operator-room lever and iron door are independent. The dry crossing remains at offset 0.

This is the standard piston gate concept, rather than a fake lever or a claimed global water-level simulation. **Whether this exact arrangement powers the piston and updates the fluid in Minecraft must be verified in a real running server/client.** Fluid handling, support and redstone updates cannot be established by a mocked block-state fixture. If wiring or initial fluid state needs adjustment, make the smallest change necessary and capture before/after screenshots or a short clip.

## Changes

1. New `worldgen/city/SluiceValve69.java`: deterministic coordinates for real vanilla sticky-piston, movable iron block, floor lever/power substrate and two-cell runoff.
2. `worldgen/city/StormwaterPumpHalls68.java`: calls Pass 69 sluice only after the standard hall is painted and only when `worldDetailVersion >= 51`. Its original Pass 68 selection/footprint, shaft exclusion and coast protection are unchanged.
3. Fresh-world preset now `world_detail_version: 51` in `normal.json`; `CitySettings` decoder and constructor accept 0..51. Missing versions still decode to zero; versions 0..50 retain the old placement. The existing `sewer_cave_frequency: 30`, vanilla Nether and End, vanilla structure-start and Deep Dark protection are preserved.
4. Added `tools/qa-pass69-sluice-valve.sh`, `tools/qa-pass69-source.py` and `tools/qa-pass69-all.sh`. Updated historical source checks for the new decoder ceiling without changing old-world feature gates. Pass 68 compilation fixture now links its new helper class even though it only checks version 50.

## Checks completed

- **133,203** compiled Java fixture assertions over **531** built halls and **1,332** selected sites, 7 stitched chunk-boundary comparisons, 40 old-world layout comparisons, and 587 negative-coordinate selections.
- **26/26** Pass 69 source checks including preserved vanilla Nether/End definitions, 30% cave-branch config, deferred world content, and old-world gate.
- `bash tools/qa-pass68-pump-halls.sh` passes after fixture dependency repair; Pass 68 uses only version 50, so no newly placed sluice blocks appear.
- Earlier historical suites: see `Pass69_OFFLINE_QA.log` (full log outside project in session; use `bash tools/qa-pass69-all.sh` to reproduce). Do **not** claim all historical tests if the run is still in progress.

## Required Wednesday acceptance tests

1. Restore official checksum-verified Gradle wrapper JAR and build with Java 21 / Gradle 9.2.1 / NeoForge 21.1.265.
2. Fresh `world_detail_version: 51` world, locate at least five naturally generated pump halls. Toggle the floor lever: piston must move the iron block into/out of the runoff and water must stop/start flowing in the downstream two cells. Test both hall orientations and both negative/positive world coordinates. Inspect fluid state both before and after chunk unloading/reloading.
3. Independently toggle the booth's iron door; opening the booth must not control the water gate. Verify the public three-wide entrance, dry bridge and existing crafting workstations work in Survival mode without any magic technique.
4. Turn off all techniques, bring only normal Minecraft armor, weapons, food and torches; explore dark sewers and breakable roach nests, and retreat via access shafts. No unescapable trap.
5. Validate naturally generated Ancient City/Warden/sculk, mineshafts, trial chambers, real strongholds/End portal, ocean structures, Nether portals and the vanilla Ender Dragon progression. Use a real seed-playthrough, not only command-spawned structures.
6. Check worldgen seams, structure proximity, lighting, and reasonable tick/performance behavior before declaring compiled/playable.

**Known limitation:** This pass does not implement changing water levels for an entire sewer network, pumping sounds, connected remote floodgates, or custom hydraulic simulation. It is one genuinely planned, local vanilla piston sluice whose actual function remains subject to Minecraft verification.
