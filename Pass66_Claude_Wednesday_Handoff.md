# Claude handoff — Wednesday, November 4, 2026

**Start from Pass66 consolidated source, world-detail v48. SOURCE ONLY, NOT PLAYABLE.** Extract `Dorohedoro_Pass66_CONSOLIDATED_Source_UNCOMPILED.zip`. The last known official Gradle wrapper JAR is missing, and the user explicitly wants Claude to restore it and handle the NeoForge Java 1.21.1 build Wednesday. Do NOT start from Pass65 or claim previous offline tests prove game functionality.

## First order of work

1. Restore/checksum-verify official Gradle wrapper with `tools/restore-official-gradle-wrapper.ps1`; use Java 21, NeoForge 21.1.x for Minecraft 1.21.1, ensure dependencies match `gradle.properties` / `build.gradle`. Run `gradlew.bat build`, `gradlew.bat runData`, `gradlew.bat runClient`, `gradlew.bat runServer`. Fix compile/registry/codec errors and crashes. DO NOT remove important vanilla biomes or structures to make compile pass.
2. Review `HoleBiomeSource.FRONTIER_CODEC` registry-provided holders and optional `coastal_shelves` flag; verify `Biomes.BEACH`, `Biomes.SNOWY_BEACH`, ocean holder list and old-world missing-field defaults serialize and deserialize correctly. These have only source QA, not an actual 1.21.1 registry test.
3. Start multiple new normal **v48** worlds. In-game compare beaches, ocean floors, city streets and high-relief coastline. Confirm beach biome IDs and coherent sand/gravel/dirt shoreline; no buildings, orphaned block-entity data, invalid resident spawns, chunk seams, inaccessible coast or severe chunk stalls. Test negative coordinates and multiple seeds; profile chunkgen time.
4. `/locate structure minecraft:ocean_monument` and inspect actual monument, loot, guardians; similarly genuine shipwrecks and ocean ruins. Do NOT treat `/locate` alone as proof. Confirm ocean ecology and beach turtle behavior in survival; warm ocean and coral are still unimplemented.
5. Vanilla completion is mandatory: natural Ancient City and Warden, stronghold End portal with eyes of ender, mineshafts, trial chambers, Nether, End and Ender Dragon victory **without Dorohedoro techniques**. Test 30% sewer cave branch promise separately; actual cave connection still unproven.
6. Return real Minecraft screenshots for city and coast, sewer navigation, Warden habitat, clean save/load and client/server tests. Only then decide next shape/difficulty pass. Do not implement deferred Ash-Snow, Sludge-Ice, poisoning, toxic snowballs, Cross-Eye camps, or villager overhaul before visual city approval.

## Offline test evidence / commands

`bash tools/qa-pass66-coasts.sh`: compiled production Java coastal and ocean bathymetry classes with minimal Minecraft stubs, found 255 dry shoreline columns and 119 sand-beach cells; old v47 unchanged.

`python3 tools/qa-pass66-source.py`: **37/37 passed**. `bash tools/qa-pass65-all.sh` and earlier Pass60 family passed through Pass47 source in a long chain before the execution time limit; `bash tools/qa-pass47-all.sh` separately passed Pass41–47. No failed assertions. For full end-to-end coverage repeat `bash tools/qa-pass66-all.sh` in a shell with no 3-minute timeout; watch Gradle and Minecraft runtime separately.

See `docs/PASS66_WALKABLE_VANILLA_BEACHES_AND_COASTAL_ACCESS.md` and the Pass62 vanilla-completion contract, Pass63/64 structure-protection reports. World generation, coral, full ocean structures, turtle nesting, sea monsters, Warden and gameplay are **NOT currently verified**.
