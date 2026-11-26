# Pass 41: Entrances, Facade Depth & Smoke-Failure Diagnostics

**Status: source-only. Uncompiled against NeoForge. Not tested in Minecraft.**
**Base:** Pass 40 consolidated source (includes the previous UI, river, hillside, interiors, textures, lantern and technique icon patches).
**Compatibility:** New city/grade/facade behavior is gated behind `world_detail_version=25`. Existing saved worlds with v24 and older preserve the prior lot elevation and facade detailing. The server/client smoke diagnostic guards apply to all versions on a newly installed build.

## Problems addressed

1. **Steep city streets can strand entrances.** Pass 40 raised the surrounding streets but kept an entire building's foundation at its *center* terrain height. Pass 41 instead samples the grade on the southern street-facing lot boundary for new worlds and keeps the building interior flat. This is a local entrance improvement, not a promise that all entrances and side service routes will be step-free.
2. **Repeated 2D facades.** Existing buildings keep their glass/doors intact. Upper storeys gain deterministic window sills and occasional service stacks, while selected shops gain restrained awning braces. This uses vanilla Minecraft blocks, adds zero chests and zero lamps, and never edits ground-floor door cells.
3. **Technique failure without clear evidence.** The server now logs runtime failures from *cosmetic* `SmokeVisuals.cast` after authoritative mechanics have completed. The client catches runtime exceptions while rendering individual mesh cues, reports the skill to `latest.log`, discards the affected cue, and restores its PoseStack. Other exceptions (e.g., server-side spell mechanics, rendering setup, severe VM errors) can still crash. This does not diagnose or prove a fix for Curse Mark.

## File changes

- `src/main/java/com/dorohedoro/hole/worldgen/city/CitySettings.java` — v25 version range and docs
- `src/main/resources/data/minecraft/worldgen/world_preset/normal.json` — new-world v25 preset
- `src/main/java/com/dorohedoro/hole/worldgen/city/CityTerrain40.java` — deterministic southern frontage elevation
- `src/main/java/com/dorohedoro/hole/worldgen/city/Canvas.java` — v25 foundation height and facade feature gate
- `src/main/java/com/dorohedoro/hole/worldgen/city/FacadeDepth41.java` — conditional upper-window and shop depth
- `src/main/java/com/dorohedoro/hole/worldgen/city/LotPainter.java` — facade feature calls
- `src/main/java/com/dorohedoro/hole/magic/SmokeEngine.java` — server VFX exception logger
- `src/main/java/com/dorohedoro/hole/client/magic/SmokeMeshClient.java` — per-cue cosmetic render guard
- `tools/qa-frontage41.sh` — independent real-source terrain fixture
- `tools/qa-facade41.sh` — independent real-source facade fixture
- `tools/qa-pass41-static.py` — JSON/PNG/icon and feature assertions

## Offline checks

- `bash tools/qa-frontage41.sh`: **55,696** synthetic lots across four seeds; **17,308 improved**, **38,388 unchanged**, **0 worsened** at the south center approach; prior worst mismatch **4 blocks**, new sampled center mismatch **0**. Uses real terrain source with a synthetic `CityPlan`, not the real road builder.
- `bash tools/qa-facade41.sh`: real facade source compiled against fixture block classes; **6,300** synthetic buildings (seven district types, three sizes, five floor counts, 60 seeds), without overriding the entrance or adding ground-height clutter.
- `python tools/qa-pass41-static.py`: parsed **95 JSON files**, verified **198 PNGs**, and matched all **64 technique icons** to registered skill IDs.
- ZIP archives subjected to `ZipFile.testzip()` integrity check.

## Important remaining work and risk

- No full NeoForge/Gradle compile. The current local source archive does not contain a usable Gradle wrapper JAR/distribution or resolved NeoForge build dependencies. The source/fixture success above cannot establish mod compatibility.
- Real new-world QA must inspect river crossings, steep lots, front and side entrances, stairs, tram grades, and all room doors. An open exit is not the same as a navigable route to the public street.
- Worldgen quality must still be judged visually: irregular street network, hills, genuine nonstraight watercourses, derelict district distinctness, and purpose-specific furnished interiors; dark night hostile spawning requires actual light-level measurements.
- The original Curse Mark crash remains **unconfirmed/unresolved** without logs and client reproduction. If no `crash-reports` file exists, `logs/latest.log` in the correct Minecraft game directory often contains exceptions; `hs_err_pid*.log` may help for a JVM-native crash.
- Complete item/block texture polish is not finished or approved; preserved from previous passes unchanged here.
- Do NOT publish this source as a compiled CurseForge mod. Keep a backup of existing worlds and install only after a successful build and QA.
