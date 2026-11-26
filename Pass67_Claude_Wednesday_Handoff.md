# Claude handoff — 2026-11-04 / Pass 67 source

**Start from `Dorohedoro_Pass67_CONSOLIDATED_Source_UNCOMPILED.zip`. Do not start from Pass65/66.** This checkpoint includes all previous versions and is NOT a JAR.

## First task — make real Minecraft build work

- Use JDK 21 and the project Gradle wrapper version 9.2.1. `gradle/wrapper/gradle-wrapper.jar` has been missing in this environment; restore it from **verified official Gradle distribution**, as explained in `RESTORE_GRADLE_WRAPPER_WINDOWS.ps1`, and do not source unverified binaries.
- Reproducible target is Minecraft Java 1.21.1, NeoForge 21.1.265. Run `gradlew.bat --version`, `gradlew.bat clean build`, `gradlew.bat runClient` and actual server/client tests. If dependency versions or API signatures fail, fix source, record each fix and rerun tests. Never imply an offline fixture was equivalent to NeoForge compiling.
- Preserve prior world-detail versions and data migration. Fresh preset is **49**; missing values default to 0. Ensure no Java compile errors introduced by `SewerServiceBays67.paint` in `CityPainter` or `CityPlan` ocean/coastal queries.

## Actual changes in Pass 67

`src/main/java/com/dorohedoro/hole/worldgen/city/SewerServiceBays67.java` contains two actual masonry side rooms with accessible workstations: repair (crafting table/furnace/grindstone) and filtration (cauldron/composter/stonecutter). Rooms are selected by seed, arterial index, and absolute sections. The main sewer water channel (distance 0/1) and dry pathway (distance 2) remain untouched. Nearby crossing shafts, other passages and dungeon footprint are deliberately excluded. Room painting is ordered before existing Pass22 cave/dungeon branches. No lights, extra spawners or exposed loot added.

## Offline verification commands

`bash tools/qa-pass67-service-bays.sh` — 221,155 Java fixture checks, including 44 near-intersection exclusions and chunk clipping.  
`python3 tools/qa-pass67-source.py` — 31/31 source checks.  
`bash tools/qa-pass67-all.sh` — runs the entire historical source suite from Pass66 through Pass41.

## First in-game priorities, ordered

1. **Prove vanilla completion without techniques**: spawn, collect wood, craft, mine, reach Nether, find real stronghold portal, defeat Ender Dragon, reach End islands. Keep vanilla structures and every useful survival resource.
2. Verify actual **Ancient City/Deep Dark + naturally spawned Warden**, abandoned mineshafts, trial chambers, temples, strongholds, and End portals. Pass63/64 safeguards passed offline only, and structures need real world inspection.
3. Test the sewer side bay functionality and nighttime mob survival using ordinary iron tools, torches, food, armor and no techniques, at several positive/negative coordinates and across chunk borders. Inspect rare Pass61 roach nests and real cave junctions. The 30% branch rule is a target; natural cave endpoint connectivity has not been proven.
4. Validate Pass65/66 true oceans and beaches with natural ocean monuments, shipwrecks, ruins, shore ecology, and performance. Ocean biome IDs alone do not prove the structures exist.
5. Review real screenshots for authentic non-grid Dorohedoro buildings, multi-room lived-in interiors, hills, varied biomes, dark enough nighttime city, purposeful service yards. Do not substitute AI-generated illustrations for in-game screenshots.

## Constraints

- No invented custom Forgotten City; user meant vanilla Ancient Cities.
- Avoid unwanted new environmental poison, Ash-Snow, Sludge-Ice, Cross-Eye faction camps, broad villager/currency overhaul until world design approval. Emeralds remain useful.
- No extra lamps, abundant outdoor chests, superficial cosmetics, or one-ability-per-key clutter.
- Keep vanilla survival beatable without smoke techniques. Sewers challenging, not an instant death trap; techniques provide optional advantage.
- User has separately requested source access and easy CurseForge releases; produce a reproducible compiled JAR only after confirming gameplay builds.

See `docs/PASS67_CONNECTED_SEWER_SERVICE_BAYS.md` for implementation and unresolved limitations.
