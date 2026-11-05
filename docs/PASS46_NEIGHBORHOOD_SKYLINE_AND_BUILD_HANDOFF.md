# Pass 46 — Coherent neighborhood rooflines, protected existing worlds, verified build recovery

**2026-11-02 — Source-only checkpoint. NOT a runnable, NeoForge-compiled mod.**

## What changed and why

The owner's actual November 1, 2026 Minecraft screenshots still show a generic, rectangular skyline even after the previous passes improved facades, service courtyards and functional interiors. Buildings still choose most of their floors independently of their neighbors. Pass 46 addresses the **city-scale silhouette**, not more ornamental filler:

- New `NeighborhoodSkyline46.java` samples a **smooth, seed-deterministic, 192-block neighborhood field**. Its influence persists over several city blocks, replacing an entirely independent height lottery with neighborhoods that have coherent vertical character.
- `LotPainter.tenement` and `LotPainter.apartment` apply modest ±1–2 storey variation to the **existing, size-aware Housing25** floor count. The pre-existing Tower.build renders and furnishes every storey: the higher roofs are real habitable floors, not fake facades. No added lights or outdoor loot.
- Building footprints constrain taller additions: no more than one added floor for small plots, at most two for suitable larger plots; always constrained to the configured maximum. Derelict and industrial neighborhoods remain lower on average. Open outskirts and graveyards remain exactly as before. Market streets retain occupied flats instead of becoming a uniform wall of tall apartments.
- Every seed/coordinate sample is absolute; the height field uses `floorDiv` and `floorMod` to handle negative coordinates and smoothly crosses 16×16 chunk and 192-block field boundaries. No chunk reads, RNG state, cross-lot painting or new workstations.
- **New world-detail version 30 only** in the default fresh-world preset. Existing saves default to their stored detail version; do NOT manually upgrade a save or blend unrelated versioned generator settings across already-generated chunks. The 2026-11-01 screenshots remain a baseline, not screenshots of this new pass.

## Actual Java / offline test results

- `bash tools/qa-pass46-all.sh` PASS, including 11/11 source contracts, 64,000 **actual compiled Java** parcel/seed/district skyline calculations with the small District stub; positive/negative neighborhood variations, no 1-block field jumps, outskirt/graveyard exclusions, size and max-height caps, deterministic repeats, and old-version gating checks.
- Pass45 service courts, Pass44 facades, Pass43 markets, Pass42 new regions and older source-only tests all run through the inherited QA suite. Tests were updated where their **hardcoded max world-detail version of 29** made them reject a legitimate v30 setting; old-save zero default was not relaxed.
- JSON resources parsed in static checks. All ZIP entries CRC checked after packaging.
- **NOT tested:** real NeoForge `runData`, Gradle `build`, game launches, Minecraft light behavior, rooftop routing, large real terrain seeds, client rendering, persistent saves or the original crash. Do not publish/install based on these source checks.

## Build blocker — recovery is prepared, not run

The pinned `gradle/wrapper/gradle-wrapper.properties` requests **Gradle 9.2.1** with distribution SHA-256 `72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f`. Its `gradle-wrapper.jar` is missing, and this environment cannot download `services.gradle.org`. The **already included** `tools/restore-official-gradle-wrapper.ps1` uses Gradle's official 9.2.1 wrapper JAR and matches the official published SHA-256 `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`, verified against Gradle's release-checksums list on 2026-11-02. Never run an arbitrary unverified downloaded JAR.

On a Windows developer PC with **JDK 21**, PowerShell and internet, from the extracted archive's root:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\verify-neoforge-build.ps1
```

This existing script verifies Java 21 and the official JAR hash, restores the missing wrapper if needed, and runs `help`, `runData`, `build`, writing logs under `build/verification`. If it fails, preserve those full logs; don't treat wrapper restoration as fixing possible Java/NeoForge source failures. After a true successful Gradle build, run client/server/GameTests and compare fresh-version-30 skyline screenshots against the five November 1 photos at comparable camera positions.

## Preservation and deferred work

Existing housing interiors, doors, stairs, façades, shops, industrial utilities, markets, tram links, bridges, meandering canals, sewers (including 30% cave-branch target), dark hostile-spawning streets, smog canopy, vanilla survival and emerald currency remain as prior passes. Snowfields/dead taiga/dust basin/iron badlands/mountain rim regions remain registered. Ash-Snow, Sludge-Ice, toxic snowballs, poisoning, Cross-Eye camps, and the villager/faction overhaul are **still deferred** until world and district visual quality is approved.

## Next checks before any claim of visual authenticity

1. Get a real `runData` and `build` result and investigate compile errors; build task correctness is not assured by Java stubs.
2. Make multiple fresh v30 world seeds, inspect downtown, market, industrial, waterfront and derelict district skylines (walking and elevated views). Confirm mixed adjacent roof levels and no overly abrupt uniform high-rise walls.
3. Enter tall v30 tenements and apartments, inspect each real furnished floor and rooftop. Verify door and stair continuity and no facade/roof collision. Check natural mob spawning at night.
4. Only after real in-game visuals are in hand, revisit street irregularity, canal geography, district landmarks, and building-quality problems from the original screenshots.
