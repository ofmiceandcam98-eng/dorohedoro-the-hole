# Pass 37 — Occupied interiors & contained urban hills (UNCOMPILED)

This is a source-only extension of the cumulative Pass 36 state; **no remote Codespaces files or Minecraft installs were changed**. Do not publish this archive as a CurseForge build.

## Delivered this pass

- `Hillside37.java`: deterministic eroded hills **inside suitable undeveloped vacant lots only**. A minimum 20×20 footprint is required; the spawn quarter (<128 world-coordinate radius by Chebyshev distance) and parcels that fail seeded selection retain earlier terrain. Ridges reach 0–7 blocks with smooth 1-block walkable steps and flat lot boundaries. The central city, roads, canals, bridges and sewer elevation model are unchanged. This is NOT yet full-scale citywide hills/valleys.
- `RoomPurpose37.java`: adds support-checked blocks selected by functional purpose: cooking/washing/sitting corners for residences; shop counters and profession stations; clinic triage and brewing/cleaning corners; warehouse/factory repair stations. Existing furniture, stairs, windows, doors and storage take priority; adds no lights, chests, or villagers.
- `LotPainter.java`: integrates the room-use pass across tenements, apartments, upper shop flats, shops, clinics, factories and warehouses.
- `OpenLots.java`: routes eligible v22+ vacant lots to the naturalized terrain module before planting older ground-level vegetation, avoiding buried trees.
- `Canvas.java`, `CitySettings.java`, `normal.json`: v22 new-world-only gate, preserving saved world versions.
- Updated QA scripts to accept newer profile numbers without disabling prior Pass35/36 checks.

## Offline verification (not NeoForge compilation)

| Check | Result |
|---|---|
| Pass 35 historical static regression | 11/11 passed |
| Pass 36 model/river/facade static regression | 18/18 passed |
| Pass 37 static checks + Java fixture compilation | 12/12 passed |
| Sampled hills | 305,320 cells over 200 test parcels, 0 cliff jumps (>1 block) |
| All project resource JSON files | 95 parse successfully |

The Java fixture compiles the **actual new Java sources against minimal locally generated stubs**. This verifies Java syntax and limited method signatures only. It does not exercise real Minecraft APIs or worldgen; the full NeoForge build may uncover additional issues.

## Outstanding before any release

1. Full Gradle/NeoForge build; update and re-run data generator if the new custom source requires resource changes.
2. Headless worldgen QA for at least two seeds in forward and reverse chunk order, including river water and lot height seams.
3. Actual Minecraft 1.21.1 test of city vistas and hill profiles, interior accessibility and night mob spawns.
4. Diagnose the technique use crash using `crash-reports` or `logs/latest.log`; do not assume it is fixed.
5. Actual vanilla-compatible PNG texture refinements and 56 individually recognizable magic technique icons; not delivered in Pass 37.
6. Complete larger hill, valley and organic watershed generation once bridges, drainage and foundations have a coordinated grade model.
7. Finalize and test minimal HUD, first-join questionnaire and responsive character menus.
8. Keep villager AI, jobs, trading and faction overhaul deferred; vanilla workstations may still be used as decor.

## Rollback

`world_detail_version` remains per world: existing v21 worlds retain the v21 hills/rooms rules. For production, never merge uncompiled Pass37 code over the last successful build without passing the smoke/GUI and city generation QA gates above.
