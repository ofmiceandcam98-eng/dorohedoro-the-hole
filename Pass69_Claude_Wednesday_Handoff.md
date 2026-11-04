# Claude Handoff — Pass 69 (2026-11-03)

**Use `Dorohedoro_Pass69_CONSOLIDATED_Source_UNCOMPILED.zip`, not Pass 68 or an older checkpoint.** Archive contains the complete Java 1.21.1 NeoForge mod source; no compiled .jar. Developer asked Claude to handle Gradle wrapper recovery and NeoForge build on **Wednesday Nov 4, 2026**.

## FIRST: make the mod build and test in a real Minecraft environment

- Install Java 21 (verify `java -version`). Ensure the **official, checksum-verified** `gradle/wrapper/gradle-wrapper.jar` exists; repository ships recovery instructions `tools/restore-official-gradle-wrapper.ps1` and `RESTORE_GRADLE_WRAPPER_WINDOWS.ps1` if needed. Do not substitute an unverified JAR.
- Use repo's Gradle wrapper (9.2.1) and pinned NeoForge 21.1.265. Run `gradlew.bat --version`, `gradlew.bat clean build --stacktrace`, and Minecraft client/server run tasks, recording ALL errors and exact fixes. Fix actual compilation/API errors, not just the offline stubs. Do not silently remove Vanilla/Minecraft content or alter protected old version paths.
- Run `bash tools/qa-pass69-all.sh` (historical suite can be lengthy). Run `bash tools/qa-pass69-sluice-valve.sh` first for targeted checks. Use `tools/verify-neoforge-build.ps1` / `.sh` as applicable. Report separate results for source checks, actual NeoForge compile and in-game playtest.

## Pass 69 code

- `src/main/java/com/dorohedoro/hole/worldgen/city/SluiceValve69.java`: physically placed vanilla lever, strongly powered stone base, sticky piston, iron block and two-cell runoff from existing wet sump. Gate toggled ON should push iron block into runoff; OFF should retract and allow water to flow. Real Minecraft water/redstone behavior unproven.
- `StormwaterPumpHalls68.java`: invokes sluice on **new version 51 worlds only**. Earlier (0–50) placements retain their original geometry. `CitySettings.java` codec and `world_preset/normal.json` updated to 51. Do not change `sewer_cave_frequency=30` or the previous version gates.
- Safety: the central dry crossing and 3-wide public entrance must remain open; outlet is on the side, not in the main sewer lanes. The earlier repair bay reservation, coast guards, Deep Dark and real vanilla structure-start guards must remain working.
- Detailed mechanics, local coordinates, tests and caveats: `docs/PASS69_REDSTONE_SLUICE_CONTROLS.md`.

## Explicit in-game acceptance (do not skip)

1. Locate real, naturally generated Pass 68/69 pump halls on fresh world. Check run-off piston and water changes under both lever states; check orientation, chunk reload and server tick behavior. If the lever does not reliably power the piston, debug strong-power path; if water does not update properly, fix confined source/outlet geometry. Only then call sluice functional.
2. Check old version 50 world save has no new valve; new v51 only. Back up existing worlds first.
3. Test no-techniques Survival exploration of sewers with normal armor, food, torches, crafting/furnaces; ensure challenge without impossible barriers.
4. **Vanilla must be beatable**: locate a naturally generated stronghold/End portal, enter Nether, reach and defeat Ender Dragon, and inspect outer End. Also verify Deep Dark/Ancient Cities/Wardens, mineshafts, temples, trial chambers, villagers and ocean monuments/ruins/shipwrecks.
5. Test actual Minecraft block entities and redstone on chunk seams, custom oceans and coastlines, no critical structure clipping, no loss of ores and cave mining.

Do not announce a playable build until it compiles with real NeoForge and is tested in-game. If all source tests pass but gameplay fails, identify the failure honestly with screenshots/logs and apply focused fixes.
