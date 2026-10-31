# Project audit

Audit date: 2026-10-30. Everything below was observed on the dev machine, not assumed.

## Starting state

| Item | Finding |
| --- | --- |
| Existing mod source | None. No local repo, and none of the 5 GitHub repos on the account match. This repo was started from the official template. |
| `smoke-over-the-hole-wfs3sdqr.jar` (Downloads) | **Rejected as a base.** Fabric, Minecraft 1.21.5, compiled classes only, licence "ARR - CreativeMode.net/mod-license", bundles `creativemode-telemetry.jar`. The spec explicitly excludes a CreativeMode import. Nothing was taken from it. |
| Spec bundle | `Dorohedoro_Handoff_Fresh.zip` copied to `docs/spec/`. The master prompt in the zip is byte-identical to the standalone copy. Row counts match `README_FIRST.md`: 58 spells, 80 mobs, 25 items, 56 assets. |
| Reference sheet | `Dorohedoro_12_Labeled_References.html` links copyrighted images. Used for visual study only, per the spec. Not copied into the repo. |

## Pinned toolchain

| Component | Version | Source |
| --- | --- | --- |
| Minecraft | 1.21.1 | MDK template, spec recommendation |
| NeoForge | 21.1.265 | Newest 21.1.x on the NeoForged maven at audit time |
| ModDevGradle | 2.0.148 | Newest on the maven at audit time |
| Gradle | 9.2.1 (wrapper) | MDK template. Gradle 9.8.2 is current; not upgraded, the template pin is the tested combination |
| JDK | Microsoft OpenJDK 21.0.12.1 | Installed, on `PATH`, `JAVA_HOME` set |
| Parchment | 2024.11.17 for 1.21.1 | MDK template |
| GeckoLib | Not added yet. 4.9.3 is the current 1.21.1 NeoForge release | Add when the first animated entity needs it, per "dependencies must be justified" |

Git 2.54 and `gh` (authenticated) are available. Node 26.8.1 is available for tooling. `python.exe` is only the Windows Store stub, so tools in this repo must not assume Python.

The launcher's `.minecraft/versions/1.21.1` holds only the JSON, not the jar. The exact-version reference is the jar Gradle downloads to `~/.gradle/caches/neoformruntime/artifacts/`.

## What runs

See `docs/TEST_MATRIX.md` for the current evidence. A claim is not made here unless that file has a recorded run.

## Environment constraints that affected the work

- **Memory.** The first builds failed with Windows error 1455, "paging file is too small": 58.8 GB of a 63.9 GB commit limit was in use while `llama-server` (LM Studio) was loaded. Once it was gone, free commit rose to 45 GB and the full decompile succeeded. The pagefile is fixed at 32 GB. `disable_recompilation=true` in `gradle.properties` is the fallback for low-memory sessions. It skips decompilation, so there are no browsable sources in that mode.
- **Path length.** The app's scratch folder is long and is really under `AppData\Local\Packages\Claude_pzs8sxrjxfjjc\LocalCache\...`. `core.longpaths=true` is set on this repo. A `git clone` into the even longer scratchpad failed with "Filename too long".
- **Interrupted Gradle.** An interrupted run left a corrupt `~/.gradle/caches/9.2.1/javaCompile/javaCompile.lock`. Deleting that one file fixed it.
- **Misleading background exit codes.** Background jobs ending in `| tail` report the exit code of `tail`. Always read `BUILD SUCCESSFUL` or a recorded `gradle_exit=` value instead.

## API facts verified against the 1.21.1 sources

| Question | Answer | Where |
| --- | --- | --- |
| Can the terrain generator be extended? | Yes, `NoiseBasedChunkGenerator` is a non-final public class | `NoiseBasedChunkGenerator.java:50` |
| Does overriding `minecraft:normal` affect the dedicated server? | Yes. `level-type` resolves through the `WORLD_PRESET` registry, and the default is `WorldPresets.NORMAL` | `DedicatedServerProperties.java:212` |
| Does the Create World screen list come from tags? | Yes, `WorldPresetTags.NORMAL` and `EXTENDED` | `WorldCreationUiState.java:244` |
| Will a custom overworld trigger the experimental warning? | No, provided the biome source is not a non-standard `MultiNoiseBiomeSource`. To be confirmed in a real client run | `WorldDimensions.java:135` |
| Will adding the Sorcerers' World trigger it? | Expected yes: any extra `LevelStem` gets `Lifecycle.experimental()`. NeoForge shows the warning once per world. Gate 3 must handle this | `WorldDimensions.java:121,180` |
| Can the spawn be set by a mod? | Yes, `EventHooks.onCreateWorldSpawn` runs before vanilla spawn search and can cancel it | `MinecraftServer.java:437` |
| Do GameTests run on the Hole? | No. `GameTestServer` hard-codes `WorldPresets.FLAT`, so generator tests must call the plan directly, and real-world validation needs dedicated server runs | `GameTestServer.java:100` |
| How do strongholds find biomes? | `#minecraft:has_structure/stronghold` is `#minecraft:is_overworld`, so Hole biomes must join that tag | vanilla data |

## Issues found in the spec bundle

| Issue | Handling |
| --- | --- |
| `shin_hammer` in the crafting seed, `heart_hammer` in the asset manifest | Same item. Use `heart_hammer`, matching the master prompt's "heart-hammer" |
| Master prompt says `explosion_magic` and `teleport_magic`; the magic CSV says `explosive` and `teleportation` | Master prompt wins as the authoritative spec |
| Baku and Yaku are `[CANON]` in the master prompt but `source-check` in the magic CSV | Treat as unverified until `docs/LORE_MATRIX.md` has a citation |
| `minecraft:allay`, `minecraft:snow_golem` and others are grouped under "Nether & End progression" in the mob seed | Seed only. `VANILLA_COMPATIBILITY_MATRIX.csv` must be built from the real registry |

## Next smallest Gate 0 task

Add client, server, data and game-test run configurations, then prove each one launches.
