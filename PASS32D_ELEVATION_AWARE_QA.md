# Dorohedoro Pass 32D: QA elevation correction (no gameplay changes)

## Evidence from live Codespaces QA, seed 12345, 625 chunks
- 155/177 enclosed lots reported reachable, including 16 shops, 5 tenements and 1 warehouse listed as unreachable.
- QA rejected 94 vanilla ores and 5,232 vanilla base-stone blocks from Y=64 upward.
- Diagnostics recorded ALL of those ores and rocks below Y=78. Selected representative ore/rock coordinates were Y=64–67.

## Verified mismatch
`CityElevation.offset(12345, 198, -172) == 8`, so the city surface there is **Y=72**. Copper ore at `198,67,-172` is below that surface, not evidence of an ore sticking out into a street.

`CityElevation.offset(12345, 144, -191) == 6`, giving surface **Y=70**, while `minecraft:stone` at `144,65,-191` is below that city surface.

For reported shop lot `87,-180..105,-169`, the nominal street feet at the south edge are Y=69. The previous floodfill hard-stopped at Y=68; it could not reach its actual ground floor. Furthermore, QA sampled the lot *boundary* (`lot.z1()`), but the actual building uses a deliberately inset footprint with a variable setback; that was not an observation of the building's door.

## Patch
Only `src/main/java/com/dorohedoro/hole/qa/HoleQa.java` changes:
1. Compute expected feet Y by absolute city relief, overriding lot columns with the level plane height at lot center (the same rule as `Canvas.beginLot`).
2. Validate flood-fill footsteps with a real ±1 step and floor heights within ±2 of local expected level. No teleporting between altitudes.
3. Check public streets' walkable surfaces at actual altitude rather than Y=65.
4. Reject exposed ore/rock at or above each column's real street/lot foundation, **not buried resource rock** below it. Underground survival mining stays intact.
5. Diagnostic lot-south-edge probes record actual expected elevation and explicitly do not pretend the lot boundary is the entrance.
6. Report expected ground-level Y min/max for debugging.

## Required Minecraft verification (not yet run)
Apply patch to current Codespaces branch (which already contains Pass 32A, 32B, and 32C), then run:

```bash
unzip -o Dorohedoro_Pass32D_Elevation_Aware_QA_Only.zip -d .
bash tools/qa-world.sh 12345 12 forward
node -e 'const r=require("./runs/qa/12345/forward/qa/report-forward.json");console.log(JSON.stringify({failures:r.failures,min:r.city_expected_feet_y_min,max:r.city_expected_feet_y_max,materials:r.city_material_diagnostics,types:r.unreachable_lot_types,lots:r.unreachable_lot_samples?.slice(0,12)},null,2))'
```

The QA world script deletes and regenerates its **test world** under `runs/qa/12345/forward/`; do not point it at any saved survival world.

If an enclosed lot or ore/rock check still fails, it is not automatically a false positive. The new local-height samples must guide the *actual worldgen fix*. No generator files, world profile or save data were modified in this pass.

A NeoForge compile or world test has **not** run in the assistant's sandbox. The user has already confirmed successful NeoForge compilation/datagen/JAR packaging of Pass 32B in Codespaces; Pass 32D still needs its own rerun.
