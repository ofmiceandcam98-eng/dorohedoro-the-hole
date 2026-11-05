# Pass 48 — Build preflight and Claude handoff (2026-11-02)

## Base and scope
- Based on `Dorohedoro_Pass47_CONSOLIDATED_Source_UNCOMPILED.zip` (1,073 files).
- This pass contains *only build-readiness updates*. No worldgen, mobs, textures, survival, or spawn algorithms were altered. Do not imply it changes the game's visuals.
- Existing game target remains Minecraft Java 1.21.1 / NeoForge 21.1.265 / Java 21 / ModDevGradle 2.0.148.

## Modifications
1. `gradle.properties`: changed `disable_recompilation` from false to true. NeoForged's ModDevGradle supports the non-decompilation pipeline to reduce memory demand. To require browsable/recompiled sources on a sufficiently capable machine, explicitly pass `-Pdisable_recompilation=false`.
2. `BUILD_AND_TEST_WINDOWS.cmd`: double-clickable Windows entrypoint. Calls existing `tools/verify-neoforge-build.ps1`, which restores the official 9.2.1 Gradle wrapper if absent, checks SHA-256, then runs `help`, `runData`, and `build` with logs in `build/verification`. Needs network and JDK 21. Script does not install Java or Minecraft and does not run the game.
3. `tools/qa-pass48-build-preflight.py`: quick offline checks of the expected pinned Gradle ZIP hash, available Java version, project build scripts, launcher presence, NeoForge target, and low-memory config. Reports a missing wrapper as a pending prerequisite rather than a successful compilation.

## Integrity and security
- Official Gradle 9.2.1 wrapper SHA-256: `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`.
- Official Gradle 9.2.1 `-bin.zip` SHA-256: `72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f`.
- Those hashes are published at https://gradle.org/release-checksums/ and checked by the original build tools. Never use a wrapper downloaded from an untrusted mirror or disable checksum validation.
- The wrapper jar is **not included** in the source archive. Local container networking cannot reach services.gradle.org; no actual download was completed here.

## Offline verification performed
- `python tools/qa-pass48-build-preflight.py`: **0 failed checks**; one *pending* item: Gradle wrapper JAR must be acquired on a network-enabled machine.
- `bash tools/qa-pass47-all.sh`: **PASS**, including previous Pass 41–46 offline regression checks and synthetic spawn/biome and city checks.
- Java 21 available in this workspace.
- **NOT DONE**: Gradle startup, `runData`, Minecraft/NeoForge build, dedicated server tests, game client tests, new screenshots, or verified in-game worldgen.

## How to test on the user's Windows computer
1. Extract the entire consolidated archive; do not copy only the changes ZIP.
2. Ensure JDK **21** is installed and `java -version` reports 21.
3. Double-click `BUILD_AND_TEST_WINDOWS.cmd` in the project root. It uses the official wrapper URL and validates the checksum, then runs `help`, `runData`, and `build`. Internet access is required for Gradle and dependencies. If any stage fails, send the error output and all `build/verification/*.log` files.
4. If compilation succeeds, check `build/libs/*.jar`. This only confirms build, **not** game functionality.
5. Then run `runClient` and `runServer`, followed by real spawn/worldgen sampling in all six starting regions, varied biome settlement densities, chunk-edge terrain, sewage/canals, interior doors, and dark hostile mob spawns. Perform these checks in **newly created worlds**, while testing saved-world backwards compatibility separately.

## Wednesday Claude continuation priorities
- **Highest**: get a real 1.21.1 NeoForge Gradle build with `runData` and build logs; fix concrete compile/registry errors. Do not call a task done based solely on static fixtures.
- After successful compile, systematically verify Pass 47 variable spawn regions, land-use types, and spawn safety in-game. Confirm that every biome remains recognizably part of The Hole.
- Compare screenshot captures to original Nov 1 in-game screenshots, focusing on density, purposeful furnished rooms, irregular streets and waterways, changing skyline, and believable industrial/polluted city identity.
- Keep all earlier constraints intact: no gratuitous decorations, no healthy generic forests, mob spawning at night, mixed but not excessive lights, no exposed chest spam, sewer/cave continuity, snow/mountain/desert/badlands differences, and deferred faction/villager overhauls until world quality is approved.
