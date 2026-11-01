# Dorohedoro: The Hole — Pass 32A: real-build verification handoff

This is a **build-verification infrastructure patch**, not a content expansion. Baseline is Pass 32 source.

## Why
- Gradle wrapper JAR was absent, preventing any actual Gradle build in the available offline environment.
- Earlier syntax and resource checks did **not** compile NeoForge or launch Minecraft.
- Making build errors reproducible and accessible is a necessary prerequisite to testing the procedural city, sewers, housing, rivers and magic.

## Added
- `tools/verify-neoforge-build.sh`: validates Java 21, verifies a downloaded official Gradle 9.2.1 wrapper JAR by exact SHA-256, pins distribution checksum, runs `help`, `runData`, `build` **sequentially**, saving logs under `build/verification/`.
- `tools/verify-neoforge-build.ps1`: equivalent Windows pipeline. Calls the existing official wrapper restore script if necessary.
- `.github/workflows/neoforge-build.yml`: manually triggered GitHub Actions job, Java 21, logs as artifacts on failure, JAR artifact on successful build.

## How to run
**Windows:** In the mod source folder run `powershell -NoProfile -ExecutionPolicy Bypass -File tools/verify-neoforge-build.ps1`. Requires Java 21, connectivity and sufficient RAM/disk. This executes mod build configuration; only run on trusted source.

**Linux/macOS:** Run `bash tools/verify-neoforge-build.sh` in the source folder.

**GitHub:** Push this source to a repository, open **Actions > NeoForge build verification > Run workflow**. Inspect `neoforge-build-logs` and successful `dorohedoro-neoforge-jar` artifacts.

**Important:** `runData` may overwrite `src/generated/resources`; make a backup or use a clean checkout and review generated diffs. CI does not run a Minecraft server, does not confirm worldgen or quality, and does not prove gameplay. A build failure is useful diagnostic evidence, not a passing release.

## Local test results
- Verified build scripts' syntax and pinned wrapper checksum from official Gradle source.
- Confirmed Java 21 exists locally, but DNS lookup for `services.gradle.org` fails, so the real Gradle steps could not be executed here.
- No mod gameplay code or world-generation profiles were changed in this patch.

## Next action after first CI run
1. Inspect exact `runData`/build errors; resolve real NeoForge API and registry failures before any more features.
2. Once `build` succeeds, run a dedicated server with new world and backup test save; capture crashes and worldgen metrics.
3. Inspect in-game building size diversity, furnished homes, water flow, fish, sewer entrances, and creature encounters.
