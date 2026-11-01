# Dorohedoro: The Hole — Pass 31 continuation / handoff

Date: 2026-11-01. Base: Pass 30 complete source archive. Target: Minecraft 1.21.1 / NeoForge 21.1.x, Java 21.

## What actually changed

1. **Residential fixture placement:** `ResidentialDetails26` now mounts Pass31 wall torches in the walkable air cell adjacent to their support wall, instead of trying to replace the masonry itself. Both compact and large housing paths were corrected. Ordinary lanterns, the custom Cavern Lamp, and other torches are preserved.
2. **Save compatibility:** A new `world_detail_version=18` is used only by the default new-world preset. `Canvas.hasHousingLighting31()` gates the correction. Existing world profiles through v17 retain the original fixture-generation behavior. `CitySettings` accepts the new version.
3. **Distinct real PNG:** The existing `preserved_smoke_conduit.png` was pixel-edited in place with two narrow brass sealing bands and a slight cyan highlight. It remains 32×32 RGBA and has a different SHA-256 from the original conduit. No new concept art substituted for item textures.
4. **QA artifact hygiene:** The river-geometry regression now deletes its temporary Java stubs and `.class` files when it exits; the release ZIP contains no test-compile artifacts.
5. **Accurate QA:** `qa-phase4-materials.py` checks the current 32×32 RGBA art (not obsolete 16×16 dimensions). `qa-pass29-housing.py` accepts the newer preset while still checking Pass29 architecture. `qa-pass31-residential-torches.py` compiles the actual residential-lighting Java classes against isolated stubs and compares whole-area output to 16×16 chunk output.

## Independent checks in this container

- Java 21 `JavacTask.parse()` — 143 real Java source files, zero syntax errors. This is **NOT a NeoForge type check or build**.
- Pass31 JDK fixture test — 800 varied homes; 622 requested torches; 596 correctly supported; 26 intentionally skipped at chunk boundaries; whole-area and 16×16 generation maps identical; 100 legacy v17 cases preserve old behavior.
- Pass28 light selection — 5,000 hashes and 300 room fixtures passed.
- Pass29 housing — 800 seeds passed.
- Pass30 chunk safety — 259,200 simulated method-body reads passed.
- Phase 4 portal materials — **86/86** source checks passed, including distinct textures.
- River morphology — 62/62 varying-width profiles; canal ecology 114/114; sewer dungeon 117/117; waterfront entrances 26/26; dense city test passed.
- JSON resources — 381 parsed; main-source PNGs — 134 PNG headers verified by Pass30.
- Full historical `tools/qa-*.py` sweep: **20 passed, 14 failed, 34 total**. Exact individual logs in accompanying QA results ZIP. Do not infer all legacy failures are harmless.

## Known failures and limitations

- **Full Gradle/NeoForge build still BLOCKED:** official pinned Gradle wrapper JAR absent. DNS/download unavailable in this container. No Minecraft client/server runtime test was performed. Do not label this JAR-ready.
- **14 legacy QA suites still fail.** Several demand previous preset values (e.g. v14), 16×16 textures, or obsolete source paths (`spawn egg` item models are under generated resources). `qa-phase3-static.py` also fails on the Hell Wastes feature-step layout; `qa-magic-temporality-pass20-3.py` reports `group_injury_healing` false. These are NOT fully resolved, and the last two deserve behavioral/schema review at build time.
- Visual parity for housing, sewers, rivers, fish, light and spawners has **not** been observed in-game.
- This source is a full checkpoint and is meant to be compared/merged against Claude's newer local changes, not overwrite them blindly.

## Required next steps, in order

1. Back up world saves and install official Gradle wrapper with `tools/restore-official-gradle-wrapper.ps1`; its SHA-256 validation must pass.
2. On Java 21, run `gradlew.bat --no-daemon help`, then `runData`, then `build` sequentially; address actual compiler/registry errors and resource generation failures, if any.
3. Investigate failing `group_injury_healing` and Hell Wastes feature-step assumptions as potentially substantive, rather than blindly editing test expectations.
4. Launch fresh v18 world and an existing v17 save in client + dedicated server; walk city street/river/underground boundaries; check lamps, torches, shops, roofs, fish, mobs, portals, loot, and injury/healing.
5. Compare in-game visuals to the user's reference-quality standard before major content expansion. Defer villager/faction overhaul, Ash-Snow, Sludge-Ice, and Cross-Eye camps as previously directed.

## Safe wrapper restoration on Windows

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\restore-official-gradle-wrapper.ps1
.\gradlew.bat --no-daemon help
.\gradlew.bat --no-daemon runData
.\gradlew.bat --no-daemon build
```

Run these **sequentially**, save every stack trace if a command fails, and do not run on your only copy of an existing save.
