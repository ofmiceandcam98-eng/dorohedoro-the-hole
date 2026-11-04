# Claude handoff — Wednesday, November 4, 2026

**Latest consolidated source: Pass 65 — world detail version 47. SOURCE ONLY, NOT INSTALLABLE.**

Do not start from Pass 64. Unpack the latest `Dorohedoro_Pass65_CONSOLIDATED_Source_UNCOMPILED.zip` and read `docs/PASS65_TRUE_OCEAN_BASINS_AND_VANILLA_WATER_STRUCTURES.md`, followed by earlier Pass64 Ancient City/stronghold structure protection and Pass62 vanilla completion specification.

## First actions

1. Restore official Gradle wrapper JAR with the existing verified `tools/restore-official-gradle-wrapper.ps1`; do not use an unverified downloaded binary. Check JDK 21 and the NeoForge Minecraft 1.21.1 mappings and versions.
2. Run `gradlew.bat build`, `gradlew.bat runData`, `gradlew.bat runClient`, and `gradlew.bat runServer`. Fix Java compile, registries, world presets/biome codecs, generated resources, runtime crashes and tests.
3. Review new `HoleBiomeSource` bundled `FrontierBiomes` codec's extra registry holders/optional boolean. Deserializing it in a real vanilla-overworld preset is essential; source-only QA cannot prove it.
4. Generate fresh v47 worlds; validate the ocean biome IDs and actual water bathymetry, no cut-off lots or block entities, natural shipwrecks, monuments and ocean ruins, plus normal city/sewer/climate generation.
5. Confirm native Ancient Cities (Warden and sculk), mineshafts, trial chambers, strongholds with working End portals, Nether/End travel, Ender Dragon defeat—all **without techniques**.
6. Shorelines, beach/turtle ecology, warm oceans/coral, ocean-only structure completeness, tide-free water continuity, and performance remain **unverified**; log and fix before claiming vanilla parity.

## Offline commands

```bash
bash tools/qa-pass65-oceans.sh
bash tools/qa-pass65-bathymetry.sh
python3 tools/qa-pass65-source.py
bash tools/qa-pass65-all.sh
```

Beware `qa-pass65-all.sh` nests older regression tests and takes time. The console-side offline test environment does not contain the official Gradle wrapper JAR. No one has demonstrated the mod loads or completes Minecraft yet.
