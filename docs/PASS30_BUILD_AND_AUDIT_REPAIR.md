# Dorohedoro: The Hole — Pass 30 audit repair handoff

**Base:** Pass 29 full source. **Scope:** fix confirmed source-level problems in the last audit; no new gameplay/content features or world-version changes. **Status:** source-verified only; NOT compiled, installed, or tested inside Minecraft.

## Changes

1. `Canvas.heightOffset(x,z)` now bounds-checks the cached 16×16 column array. For coordinates outside the active chunk, it returns the same deterministic `CityElevation.offset(seed,x,z)` result (or zero with terrain relief disabled). Fixed lot heights remain unchanged.
2. `Canvas.get(x,y,z)` now checks `contains` before looking up block state. Out-of-chunk reads return `Blocks.VOID_AIR.defaultBlockState()` and never query another chunk; callers must explicitly check `contains` before using the read to decide on structural support.
3. `Housing25.residence` now reads the room center only when the center belongs to this chunk. Eliminates the audit's unguarded lookup on buildings straddling chunk seams.
4. `CanalEcology24` now bounds-checks a fishing-supply barrel's offset column before reading that position. Avoids seam-dependent shoreline decoration reads.
5. Deleted 16 conflicting duplicate **manual** resource JSON paths from `src/main/resources` where `src/generated/resources` already owns the same path. Eight were identical spawn egg models, eight were divergent block loot tables. The canonical generated loot retains the registered block drops; four ore loot files retain Silk Touch / Fortune alternatives. Existing datagen should recreate the generated authoritative data.
6. Pinned the official Gradle **9.2.1** distribution SHA256 in `gradle-wrapper.properties`: `72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f`.
7. Added `tools/restore-official-gradle-wrapper.ps1`, which downloads the official Gradle 9.2.1 wrapper JAR **and refuses to install it** unless SHA256 equals `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`.

## Verified in this environment

- Java 21 syntax parser: **143 Java sources, zero parse errors**. No NeoForge API/type-check.
- New Pass 30 dynamic Java boundary harness: **259,200** direct read attempts using the extracted *actual Canvas method bodies* across positive/negative chunk positions, in/out-of-range coordinates, and fixed-lot/terrain relief scenarios; no out-of-chunk chunk-object read.
- Duplicate model/loot paths in main vs generated resources: **0** left; eight authoritative block loot tables validated.
- JSON syntax: **381** current main + generated resources; main PNGs: **134** readable headers.
- Pass 29 housing Java-stub test: **800 configurations passed**.
- Pass 28 mixed lighting Java-stub test: **5,000 light choices, 300 deterministic rooms passed**.
- Pass 24 river geometry: **62/62 varying width profiles passed**.
- Pass 24 canal ecology: **114/114 static checks passed**.
- Pass 22 sewer dungeon: **117/117 static checks passed**.
- Pass 23 waterfront entrances: **26/26 static checks passed**.
- Pass 25 density: **161,596 assertions passed**.

**Known failing legacy test:** `qa-phase4-materials.py` passes 76/86 because it expects 16×16 item PNGs. Newer art uses 32×32 (and different resolutions where needed). This test needs a requirements-driven redesign, not an asset downgrade. Other historical suites from the previous audit have not all been reconciled.

## Still blocked — full build and gameplay

The Gradle wrapper JAR remains **absent** in the archive because this environment could not download it. To restore it safely on an internet-connected **Windows PC**:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\restore-official-gradle-wrapper.ps1
.\gradlew.bat --no-daemon help
.\gradlew.bat --no-daemon runData
.\gradlew.bat --no-daemon build
```

Use Java 21 and run the three Gradle commands **sequentially**. The restore script validates the downloaded JAR, while the wrapper properties validate the Gradle distribution ZIP. If any command fails, retain the full stack trace; do not claim a successful Minecraft build. Check resource duplicate counts and the built JAR after datagen.

Then launch a **fresh save** and a dedicated server, check chunk seams on positive/negative x/z at 10+ seeds, render homes/lamps, traverse sewers and rivers, verify fish and creatures, and test the three dimensions, portals, and magic.

This repair does **not** change the saved `world_detail_version`, does not create or overwrite an existing world save, and does not touch deferred faction/villager, environmental, or Cross-Eye systems.
