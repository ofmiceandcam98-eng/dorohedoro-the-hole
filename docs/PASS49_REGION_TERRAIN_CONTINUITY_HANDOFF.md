# Pass 49 — Fix false 512-block terrain troughs (November 2, 2026)

**Status: SOURCE ONLY. This is NOT a compiled Minecraft 1.21.1 / NeoForge mod or approved world render.** Based directly on the Pass48 consolidated source. No earlier source or required gameplay systems intentionally removed.

## Observed code defect and why it matters

Pass42's `HoleRegions42.terrainBlend(int x, int z)` fades mountain, badlands and snowy relief to zero along **every** 512-block warped sampling-cell edge, even when the two adjacent cells choose the same region. This mathematically creates repetitive flat cuts through landscapes, directly contradicting the goal of varied, believable terrain. It is a source-level issue; no fresh screenshot from Pass47/48 yet exists to establish its in-game severity.

## Targeted correction for new worlds only

- Saved `world_detail_version` **32** opts into region-aware blending. Versions **0–31 keep their exact prior blend** and preserved legacy spawn selection.
- `HoleRegions42.terrainBlend(Climate.Sampler, x, z)` samples adjacent climate cells **only inside the 88-block edge band**. A transition is added if the neighbor resolves to a **different** province. Same-region neighboring cells now retain relief across their shared border.
- `regionInCell` centralizes the already-existing selection conditions for all six regions. The city-core protection remains; the old numerical `terrainBlend(int,int)` stays unchanged.
- `CityPlan.terrainBlendAt` dispatches by saved world version and `CityTerrain40.offset` uses that dispatch. All versions keep existing floodplain clearance, city grading, rail construction, sewer tunnels, landing areas, sources of useful wood, and dark-night logic.
- The current new world preset uses 32; historical `level.dat` world settings are not silently rewritten.
- Adjusted only previous test scripts' *upper codec version* assertions from 31 to 32, retaining their checks for missing/default version 0 and feature-specific gates.

## Checks completed offline

- `bash tools/qa-pass49-all.sh` passes the current 13 new static contracts; Java-21 compilation of the actual `HoleRegions42` with stub Minecraft sampler, examining 319 matching-cell edges (no false fade), 76 different-region edges (transition retained), and 100,000 deterministic/random positions. The existing Pass41–47 test hierarchy also passes.
- No internet route to official Gradle distribution in this execution environment; `gradle/wrapper/gradle-wrapper.jar` is **still absent**. No full compile, actual terrain or FPS measurements, runClient, server, saving/reloading or multiplayer QA performed.
- The sampled constant-climate fixture is useful for regression, but **does not demonstrate** real Minecraft climate frequencies or visual quality.

## Installation / testing handoff

1. Back up saves and test **a new world only** with the `normal` preset at v32. Do **not** manually raise the setting in existing v31 saves; it would cause generation seams.
2. On a networked Windows Java 21 development PC, use `BUILD_AND_TEST_WINDOWS.cmd`, which restores the pinned official wrapper with checksum verification if needed; run `runData`, `build`, `runClient`, and a dedicated server test. Fix errors from logs rather than claiming the source compiles.
3. Compare paired screenshots in the *same biome* on both sides of 512-block climate cells (including the warped cell edges), and at **actual** different-biome boundaries. Look for unwanted trenches, 1-block sheer discontinuities, floating roads/rail, ocean/canal leaks, or disjointed foundations.
4. Verify 6 different first-neighborhood types; confirm walking from the starting plaza to the furnished shelter, existing rooms, streets, sewers and survival wood. Inspect snowy and mountain starts as priorities.
5. Review user-supplied November 1 screenshots of skyline, canal, market, street lighting, along with in-game new-build captures. The Hole's overall visual authenticity still needs explicit user approval.

### Deferred content remains deferred

No villager/faction overhaul, Ash-Snow, Sludge-Ice poisoning, Cross-Eye camps or cosmetic filler was added in this pass.
