# Pass 43 — Inhabited streets and recognizable Hole civic space

**2026-11-02 — SOURCE-ONLY / UNCOMPILED — not an installable JAR**  
**Baseline:** Pass 42 consolidated archive (which retains Pass 41 and previous features).

## What was reviewed

Revisited original actual Minecraft screenshots sent on November 1, 2026, not illustration mockups:

- `Screenshot 2026-11-01 185410.png`: wide tram street, uniform lamp and facade pacing.
- `Screenshot 2026-11-01 185425.png`: repeated road fixtures and relatively unbroken carriageway.
- `Screenshot 2026-11-01 185606.png`: broad, straight-looking canal intersection and largely empty quay.
- `Screenshot 2026-11-01 185647.png`: varied heights but rectilinear main streets dominate through smog.
- `image(20261101-234541).png`: market pitches and great stretches of uniform public square paving.

Those are **older in-game screenshots**, not proof of how Pass 42 or 43 renders. Existing road layout is already seeded and partially irregular, but the visible municipal corridors and plazas still look too modular. The smog itself should not be exchanged for a bright ordinary vanilla sky.

## Implemented in source, gated by `world_detail_version >= 27`

1. **Occupied civic market:** `OccupiedCommons43.market` replaces the identical-stall array in new worlds with a 10×9-pitch but offset and different-size merchant fronts, variable roof elevations and patched cloth, several delivery gaps, sheltered real smoker/loom/stonecutter/fletching table/composter job sites, and sparing supplies in barrels rather than open chests. The 3+ block walking aisles stay clear.
2. **Wear instead of empty spawn plaza:** `OccupiedCommons43.plaza` adds block-scale coherent stone repair and functional western-edge communal water/repair/compost facilities. The starting area retains the starter-cache/restaurant/workstation arrangement; no blocks obstruct the center 21×21 spawn movement area.
3. **Connected backstreets that look maintained in patches:** `Backstreets43` paints coordinated medium- and broad-scale repair patches on local lanes *only*. It explicitly skips all lots, trams, canals, arterial bands and the new frontier regions. The changes are flush paving, not collision objects, new lighting or loot crates.
4. **Sparse working waterfront:** `CanalWorksites43` places a few fish-processing, maintenance or organics-handling jobs on solid dry quay segments, away from junctions, using only existing vanilla workstations. Retains usable water, bridges, flowing water profiles and mineral collection; it does not build an inaccessible fake staircase or add bright lamps.
5. **Less synthetic lighting pattern:** new `CityPainter.streetlamp43` varies lamp stations in deterministic 73-block segments and selects only one sidewalk side rather than paired, exactly periodic rows. A starter-center exception retains more coverage; the original lamp formula remains active in all existing `world_detail_version <= 26` worlds. **In-game light measurements and night hostile spawn testing remain necessary.**
6. **Save-safe opt-in:** `CitySettings` now accepts 27, the default world preset opts into 27 for *new* worlds, while older saved settings still default their absent version to 0. All earlier version-specific generation paths remain gated as before.

## Verified in this session

- **Pass 43 integration static QA**: 16/16 checks (`python tools/qa-pass43-source.py`).
- **Pass 43 Java placement fixture**: compiles the actual new Java source against a deliberately minimal test double and compares full-area painting to 16×16 chunk painting, identically: market 1,211 block writes; plaza 90; backstreets 680; quays 6 in the chosen synthetic sample. Checks protected 21×21 spawn interior, protected merchant lanes, no lot/arterial repaving and no modifications at water level 61. (`bash tools/qa-pass43-placement.sh`)
- **Pass 42 static QA**: 26/26; the pre-existing v26 test was updated to allow a newer preset while still verifying all v26 features.
- **Pass 42 climate Java fixture**: pass, 6 selector categories, coordinate determinism (not real climate data).
- **Pass 41 static QA**: 95 JSON, 198 decoded PNG textures, all 64 technique icons consistent.
- **Pass 39 Java geometry fixture**: pass, 1,792 synthetic layout runs. Only the historical test's assumed *exact* v23 preset/maximum codec version was made forward-compatible; its source checks for v23 feature gates remain.
- **Pass 35 static checks**: 11/11; useful workstations, night light-source constraints.

**These tests DO NOT prove a NeoForge compile, actual 3D appearance, train clearance, world generation, seamless terrain, residential navigability, night-spawn rates or successful runtime use of new workstations.**

## Important unresolved / next step priority

1. Restore the official Gradle wrapper JAR (missing from original archive), JDK 21, `runData`, `build`, `runClient`, dedicated `runServer`/GameTests. Never claim a working mod JAR before these pass.
2. Create **new world** with v27 and compare exact viewpoints from the five old screenshots: spawn square, market, canals, tram streets, skyline. Capture both ground-level and elevated shots with HUD hidden, daylight and dark.
3. Walk every vendor aisle; check door headroom; ride/check minecart tram gradients, especially at mountain region boundaries. Confirm lights activate at night as intended, not by assumption; measure hostile spawning and protective lighting near spawn.
4. Validate all five recognizable climate regions and transition paths, including sloped roads, underground galleries and weather. Ensure all materials retain survival purpose and all homes still have usable room connections.
5. Fix any source incompatibilities or terrain carve problems found in real client, then rerun deterministic seed tests and asset reviews. Only then begin broader architecture/connected utilities/facade silhouette overhaul.

## Status of legacy systems

All 198 PNGs, 64 technique sprites, Sorcerers' World, Hell, existing magic, rooms, sewage/cave ratios, canal/rivers, late-night lighting and existing vanilla survival mechanics were left in the source archive. This pass did **not** implement deferred Ash-Snow, Sludge-Ice, toxic snowballs, Cross-Eye camps or faction/villager overhaul. An earlier crash remains unresolved until a real `latest.log` and reproducible client session are available.

No public/real-time Minecraft rendering was possible here. **Do not publish Pass 43 to CurseForge or install it expecting a functioning build.**
