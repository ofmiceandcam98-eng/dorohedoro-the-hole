# Pass 50 — Accessible regional starts and alternate-climate fallback

**Date:** 2026-11-02  
**Basis:** Pass 49 consolidated uncompiled source, Minecraft 1.21.1 / NeoForge.  
**Status:** Source-only. Not compiled against NeoForge, and not yet tested in Minecraft.

## Why this pass was needed

The v31+ start selection previously accepted a regional starting superblock after checking the biome at five coordinates and excluding unique landmarks. That was not a guarantee that its landing's fixed-height foundation actually connected to the sheltered bed and nearby public lanes: a mountain or badlands slope could require jumping an unplanned two-block step. The selection also returned downtown immediately if the first-choice climate had no eligible location, even if another outer biome had an eligible site. Both are detectable risks in source, not documented player-reported bugs.

## Source changes

- Fresh-world **world_detail_version 33** now checks a regional starter's actual `CityTerrain40.offset` values at the four-block cross-aisle between the landing and furnished shelter, and at the immediate west/east public-lane entry points. `SpawnApproach50` accepts only one-block-or-smaller step changes at these measured transitions. This is deliberately **not** a complete A* walkability test of every surrounding road.
- For a seed whose preferred outer biome has no acceptable site, `StartingRegions47.chooseWithRegionalFallback` tries the other outer biomes in deterministic rotating order before using downtown. The preferred-city start remains a legitimate choice. The alternate search is bounded to 256 trials per biome; first preference retains the old 1,024 candidate trials. The selector is deterministic and never depends on generation order or player login.
- Pass 31 and 32 saved-world values call the original `StartingRegions47.choose`, unchanged. The v33 new-world preset is the sole opt-in. Older world JSON codecs still default to version zero for missing fields. **Do not manually upgrade an existing world to 33; doing so may create old/new chunk seams or change the effective world spawn.**
- Updated older offline *test assertions* to allow version 33; the earlier generation code, survival balance, portal foundations, item textures, lighting, tunnels, building interiors and biome identities were not otherwise altered.

## Offline verification

Run `bash tools/qa-pass50-all.sh` from the project root. This invokes:

- `tools/qa-pass50-source.py`: 15/15 source contracts, including version gates, starting-quarter geometry references, cave frequency and JSON parsing.
- `tools/qa-pass50-safety.sh`: javac compiles and executes actual `StartingRegions47`, `SpawnApproach50`, and `CityHash` under Java 21 with synthetic probes. Checked 36,000 seeds, including 29,957 synthetic cases where the preferred outer biome was forced unavailable but another was available, 1,943 grade fixtures, and all-impossible fallback scenarios.
- `tools/qa-pass49-all.sh`: pre-existing Pass 41–49 offline suites, which all passed.

These tests **do not** prove that a real NeoForge `Climate.Sampler` yields eligible locations across all climates, that the client reaches the shelters correctly, or that the landscape and city visually meet the user's goals.

## Blockers and on-device acceptance tests

1. On a networked Java 21 development PC, run `BUILD_AND_TEST_WINDOWS.cmd` to restore the official checksum-verified Gradle wrapper and run `runData`/`build`. This archive still lacks the wrapper JAR. Fix actual Gradle/NeoForge compilation errors from the logs.
2. Run a fresh v33 world for each selected region: CITY, SNOWFIELDS, DEAD_TAIGA, DUST_BASIN, IRON_BADLANDS, MOUNTAIN_RIM. Confirm actual world spawn matches surface, the landings are not submerged, and every shelter/bed is physically reachable on foot without mining or two-block jumps. Confirm cave/shaft access and nearby useful wood/resources.
3. Check several rugged starts, particularly mountain ridges and river-facing superblocks; examine real step heights along all public approach routes, not only the sampled three transitions. Never generalize synthetic success into world acceptance.
4. Compare daytime/nighttime city and frontier screenshots with the user's November 1 in-game screenshots. Inspect inhabited rooms and nighttime mob spawning; do not add excessive lamps or cosmetic filler.
5. Test save/reload/dedicated server/multiplayer and previous-version worlds for stable spawn locations. Never run the v33 preset over an existing save as a migration mechanism.

## Priority after the code/build gate

Address actual visual feedback from in-game screenshots (dense Hole neighborhoods, distinctly polluted frontier settlements, irregular streets, working interiors and intentional landmarks) and only then revisit deferred toxic snow, Ash-Snow, Sludge-Ice, Cross-Eyes or villager/faction overhauls.
