# Pass 59 — Walkable industrial service courts (source only)

Date: 2026-11-02. Minecraft Java 1.21.1 / NeoForge. Fresh-world generation profile **42**.

## Intent and results

The Pass 56 workshop/hall connection provided an east–west sheltered passage, but the workshop's south-facing door still led into an outdoor service strip without a reliable route to the property's street frontage. Pass 59 makes **one working courtyard shared by the two buildings on an existing industrial property**, rather than attempting to cross independently generated neighboring lots.

- When an existing large industrial property has the west workshop and accessible Pass 56 connector, a **three-block-wide, clear, surfaced pedestrian/cart approach** now runs from the workshop's south door to the south edge of the same lot.
- A work area sits on the hall-wall side, **outside the walking corridor**. It has a cauldron, smithing table (factory) or stonecutter (warehouse), and composter under a narrow weathered-copper rain cover.
- The route is finished **after old yard detailing**, so earlier scattered objects cannot block the new path. No light sources, no exposed loot chests, no new mobs or hazards.
- The courtyard only writes **inside the existing owner lot**, retains all warehouse and factory interiors, and does not move main avenues, canals, trams, or sewers.
- Old saved `world_detail_version` 0–41 are unaffected. Only newly created version-42 worlds opt in.

Implementation: `src/main/java/com/dorohedoro/hole/worldgen/city/WorkingServiceCourts59.java`, integrated near the end of `LotPainter.paint`, guarded by `Canvas.hasWorkingServiceCourts59()`, with codec and world preset updated in `CitySettings.java` and `normal.json`.

## Offline verification

- `python3 tools/qa-pass59-source.py`: **21/21 checks**.
- `bash tools/qa-pass59-courts.sh`: compiles the actual Pass 59 production Java class and dependencies (`Rect`, `CityHash`, `LinkedServiceYards56`, `IndustrialAnnex58`) against small Minecraft API stubs. **6,500 sampled industrial lots; 650,286 verified approach cells; 6,500 work pockets; 210 chunk-split equivalence cases; 723 old-version unchanged cases; 1,400 undersized lots rejected; 500 negative-coordinate valid courts.**
- Pass 58 through Pass 45 regression suites completed in the first run (`Pass59_OFFLINE_QA.log` in the working session), then the Pass 44/43/42/41 and earlier required static checks were run separately because the combined recursive suite exceeded the execution timeout (`Pass59_LEGACY_TAIL_QA.log`). No test failures reported across these separate runs.
- JSON resources parse and the consolidated archive is CRC-checked.

**Limitations:** Minecraft/NeoForge project is NOT compiled, no client/server run, no game screenshots produced from Pass 59, no certainty of collision behavior in a live world. Java geometry stubs simulate chunk clipping but are not a substitute for worldgen integration.

## Wednesday Nov 4 Claude handoff: first real build

1. Use the Pass 59 consolidated project as **the newest source**. Do not start from Pass 58 or older. Do not change existing saves merely to expose new worldgen features.
2. Restore the official **verified Gradle wrapper JAR**. Existing `gradle/wrapper/gradle-wrapper.properties` and `tools/verify-neoforge-build.ps1`/`BUILD_AND_TEST_WINDOWS.cmd` describe the wrapper recovery and checksum verification path. Use JDK 21 and an internet-connected Windows machine; don't trust arbitrary wrapper mirrors.
3. Run `BUILD_AND_TEST_WINDOWS.cmd` or, once wrapper is restored, `gradlew.bat --version`, `gradlew.bat runData`, `gradlew.bat build`. Fix real compilation/data-generation errors before adding new features.
4. Run Minecraft client and dedicated server. Generate fresh v42 worlds with multiple seeds and inspect *dense + sparse snowy/desert/badlands/mountain/taiga city and outskirts*, all six spawn regions, roofs, streets, courtyards, and entrances. Verify genuine nighttime hostile spawns.
5. Inspect larger industrial factory and warehouse lots with west workshop. Confirm a real 3-wide walking route from its south entrance to the street and a usable east–west covered door connection; inspect negative-coordinate lots and chunk seams. Verify work pocket under its copper rain cover; check spawned structures do not occlude player doors.
6. Visually compare against the user's **actual Nov 1 Minecraft screenshots**, not generated concept art, and source manga/anime art direction. Prioritize recognizable Hole streets and purposeful interiors, not ornamental clutter.
7. Only after playable visual review, continue neighboring-lot shared-wall research; cross-lot generation must preserve deterministic chunk clipping and avoid writes into another owner's lot.

**Deferred user-approved order remains:** First achieve distinguishable architecture, streets and furnished rooms; only then introduce Ash-Snow, Sludge-Ice, toxic snowballs, environmental poisoning, Cross-Eye camps, and later villager/faction overhaul. Preserve Minecraft survival, renewable wood from dead trees, emerald economy, mining/caves, sewer-to-cave connections, darkness at night, and old saves.
