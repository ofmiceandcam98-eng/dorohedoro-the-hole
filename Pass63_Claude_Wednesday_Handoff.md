# Pass 63 — Wednesday, November 4, 2026 handoff

Use the **consolidated Pass 63 source ZIP** as the latest baseline, not Pass 60–62.

1. Recover official Gradle wrapper JAR using existing checked recovery flow; compile with Java 21 / Minecraft 1.21.1 / NeoForge. Do not bypass dependency hash checks.
2. Run `bash tools/qa-pass63-all.sh` and remaining historical fixture runners; run `gradlew runData`, `gradlew build`, `gradlew runClient`, and `gradlew runServer` as supported. Resolve real compile errors without deleting existing systems.
3. Verify fresh v45 generation and old v44 saves. New deep-dark safety buffer preserves natural terrain at Y<=-24 and *can interrupt old custom deep gallery paths*. Inspect transitions visually and fix accessibility if needed.
4. Five+ independent seeds: locate naturally generated Ancient Cities, Deep Dark/Wardens, strongholds and functional End portals, mineshafts, trial chambers and standard 1.21.1 land structures. Note missing ocean region support. Verify the 30%-selected branch rule does NOT imply confirmed connectivity to a preexisting natural cave.
5. **No-technique playthrough:** wood/food, Nether portal, blaze rods/pearls/Eyes, End portal, Ender Dragon, End gateway and End City; no cheating or creative rescue when judging acceptance.
6. Capture actual client screenshots with coordinates and seed; compare urban/biome/industrial/sewer generation to Dorohedoro reference material. Preserve furnished rooms, villages, Minecraft survival resources, dark nights and optional dangerous sewers.

The user clarified "Forgotten City" = vanilla Ancient City, **not** a separate Dorohedoro structure. Do not add an invented Forgotten City. Custom Hell and Sorcerers' Realm do not replace Minecraft's original Nether and End.

The offline Java stubs check only the buffer predicate and generation choices. NeoForge build and real Warden/Dragon tests are outstanding.
