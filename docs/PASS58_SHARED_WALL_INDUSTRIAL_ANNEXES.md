# Pass 58 — shared-wall industrial annexes and working stepped roofs (2026-11-02)

**Status: SOURCE ONLY, NOT NEOFORGE-COMPILED.** New-world detail version **41**.

## Motivation

Pass57 restored coherent biome-independent industry; Pass56 connected large industrial workshops via walkable covered service ways. But most factories and warehouses still rendered as one massive rectangular volume. This change introduces actual non-rectangular industrial massing with a genuinely attached, usable bay rather than decorative props. It follows the owner's art direction: lived-in industrial fabric, randomized biomes and Minecraft survival preserved. Do not implement any deferred Ash-Snow, Sludge-Ice, poison, or villager/faction overhaul.

## What production code does

- `IndustrialAnnex58.mainFootprint` conditionally reserves the eastern 8-block strip of a sufficiently large industrial hall (minimum original 37 wide × 23 deep; seeded ~2/3 eligible plots). The main high hall stays ≥29 blocks wide, and keeps its south cargo opening.
- In the northeastern portion of the former full-height hall footprint, `IndustrialAnnex58.build` creates an **attached lower side wing**, 9×14 blocks, with a real shared wall, walkable internal door leading back into the hall's central aisle, another door dividing two useful rooms, workstations tailored to warehouse/factory use, and a reachable flat maintenance roof/ladder.
- The remainder of the former east strip is **open loading setback**, yielding actual L-shaped factory massing rather than another rectangular shell. The separate western covered workshop/service connection stays intact.
- Added structure is completely contained within its original parcel. Uses deterministic lot hashes, the existing chunk-clipped `Canvas`, no new lighting or public loot chests. Does not alter spawn/roads/canals/tram/sewers/ores, or identify biome types, so it works in all Hole climates.
- Preserves older world-detail versions 0–40 exactly via `Canvas.hasIndustrialAnnex58()`; fresh-world preset now v41, codec/constructor support 0–41, absent version still defaults to 0. Sewer cave frequency cap stays 40, new-world sewer option stays 30%.

## Verification, complete offline

- `python3 tools/qa-pass58-source.py`: **18/18 checks passed**.
- `bash tools/qa-pass58-annex.sh`: **3,600 actual-source Java geometry simulations**, **2,090 attached two-room bays**, **71 split chunk equivalence tests**, **515 old-version no-change cases**. Tested internal entrance, two-room door, clear walk route, workstation presence, ladder/roof opening, and no writes outside lot.
- `bash tools/qa-pass58-all.sh`: **PASS**, including Pass41–57 offline regression suites. Full saved console log: `docs/PASS58_OFFLINE_QA.log`.
- JSON resource validity covered by legacy QA. Archive CRC checked by packaging script.

## LIMITATIONS / next build and in-game QA

**This is not an installable mod and does not prove Minecraft rendering, performance, registry compatibility, server functionality or survival behavior.**

- Local project has `gradle/wrapper/gradle-wrapper.properties` but **no `gradle/wrapper/gradle-wrapper.jar`**. `bash gradlew --version` fails with `Unable to access jarfile .../gradle-wrapper.jar`. Obtain the official wrapper using `RESTORE_GRADLE_WRAPPER_WINDOWS.ps1` on a network-enabled, Java 21 PC and verify checksum as documented by the script; do not use an unofficial JAR.
- Then run `BUILD_AND_TEST_WINDOWS.cmd`, `gradlew runData`, `gradlew build`, `gradlew runClient`, `gradlew runServer`, and dedicated GameTests. Build errors must be fixed before user screenshots are described as showing the new pass.
- In a **new version-41 world**, verify factory and warehouse door paths, worker room furnishings, ladder access, smooth roof transitions, no external parcel leaks, preserved cargo access, the 3-wide western yard, nighttime hostile mob spawns, and chunk-seam behavior.
- Walk six climates (snowfields, dead taiga, dust basin, badlands, mountains, city climate) and compare actual fresh Minecraft screenshots with Q Hayashida / MAPPA Hole visual references. The older November 1 screenshots show an earlier pass, **not** v41.
- Validate ore access, caves/mines, dead trees/log supply, sewer-to-cave ratio, natural wilderness, and purpose-built rooms. Deferred hazards/factions remain deferred.

This report supersedes previous source-only status for Pass58 but does not retroactively certify historic builds.
