# Dorohedoro: The Hole — Pass 32: Purpose-first ecology and audit validity

**Baseline:** Pass 31 full source (source-only). **Target:** Minecraft 1.21.1 / NeoForge 21.1.x.
**Release state:** NOT COMPILED; NO Minecraft client/server world test.

## Actual functional changes

1. **Control long-term canal entity load.** The v19 default new-world profile no longer forces every generated starter salmon/cod to persist permanently. Natural water-ambient spawning and the existing low-count seed schools continue to provide fish. This reduces avoidable entity retention across thousands of explored urban chunks. Entity tick savings in a running Minecraft server are **not measured**.
2. **Prevent unsupported waterfront supply barrels.** New-world quay supplies require a solid foundation directly beneath their placement column. Legacy profiles retain their original decisions.
3. **Protect existing saves.** Both decisions live in pure `CityEcologyPolicy32` and are gated on the saved world-detail version. Earlier v0-v18 profiles retain their policies; new default worlds use version 19.
4. **Remove unneeded art change.** Restored `preserved_smoke_conduit.png` to Pass 30 exactly, rather than invent another visual variant.
5. **Make QA match actual behavior.** Fixed Hell biome QA, where omitting unused feature steps is correct and six Hell ores at underground-ore step plus a lava spring are the meaningful checks. Replaced obsolete Healing source-string expectations with checks of the current mastery-gated limb-regrowth rules. Updated historic housing and canal QA for the new profile. Phase 4 QA now checks that preserved conduits are *really used by both portal cores*, rather than requiring a cosmetic difference.

## Executed checks

- `tools/qa-pass32-meaningful-ecology.py` — **PASS**: compiled production Java policy with Java 21; 25 generator-version profiles checked for both fish persistence and support-required supply barrels.
- `tools/qa-phase3-static.py` — **89/89 PASS**: Hell generation data has the intended 6 custom ore veins and lava-spring stage; no unrelated vegetation or generic monster spawns.
- `tools/qa-magic-temporality-pass20-3.py` — **PASS**: direct ally and group Healing keep separate mastery requirements for regrowing limbs.
- `tools/qa-phase4-materials.py` — **86/86 PASS** (same icon for both conduits remains a warning, not an invented gameplay failure).
- `tools/qa-pass24-canal-ecology.py` — **114/114 PASS**.
- `tools/qa-pass29-housing.py` — **800 seed cases PASS**, compared whole-area to chunk-local generation.
- `tools/qa-pass31-residential-torches.py` — **800 residential configs PASS**, including legacy-profile regression.
- `tools/qa-pass22-dungeon-ecology.py` — **117/117 PASS**.
- `tools/qa-pass23-waterway-entrances.py` — **26/26 PASS**.
- `tools/qa-pass28-mixed-lighting.py` — **PASS**: 5000 lighting hashes, 300 interior configurations.
- `tools/ParsePass22.java` — **144 Java source files parsed**, 0 syntax errors (NOT NeoForge compilation).
- **381 JSON resources parsed**; **134 main-source PNGs verified**.

## Remaining release blockers

- `gradle/wrapper/gradle-wrapper.jar` is absent from this archive; full NeoForge `runData`, Gradle `build`, client launch and server launch were **not performed**. A checksum-verified official restoration script exists in `tools/restore-official-gradle-wrapper.ps1`.
- Existing world saves must be backed up. **No in-game tests** of fish despawning, flow/river quality, sewer navigation, furniture usability or magic correctness have been performed. Simulated/test checks are not a substitute for gameplay.
- Legacy historical QA scripts not listed here may still fail from obsolete assumptions or real regressions. This report does not claim the full historic suite is green.
- `smoke_conduit` and `preserved_smoke_conduit` currently share an icon; their distinct *crafting uses* are verified. Texture differentiation remains intentionally deferred until it serves a clear usability/art goal.
- Defer Ash-Snow, Sludge-Ice, faction/villager overhaul and Cross-Eye camps until previously approved city/district quality is reached.

## Next build/test priorities

Restore verified wrapper, `gradlew.bat --no-daemon runData`, then `gradlew.bat --no-daemon build`, one at a time. Check fresh v19 and old v18 worlds side-by-side, measure fish/entity counts over 500+ waterfront chunks, visually inspect supported quay supplies, and verify sewer access and residence interiors before extending gameplay.
