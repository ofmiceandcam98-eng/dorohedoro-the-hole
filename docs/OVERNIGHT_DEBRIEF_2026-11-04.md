# Overnight debrief, 2026-11-04

Everything below actually ran on this PC or on GitHub's servers. Anything unproven is listed under **Not verified**.

## TL;DR

- **Pass 70 now compiles for real against NeoForge 21.1.265**, on this PC and in GitHub CI. It had 3 compile errors, all fixed.
- **It runs on a real dedicated server:** fresh worlds on 4 seeds, 0 errors in the logs, no crashes from the mod.
- **Vanilla structures were actually located and inspected:** a stronghold with a full End portal, an Ancient City with sculk shriekers, trial chambers with trial spawners, and a mineshaft.
- **Fixed 3 real bugs:** broken barrels at generation, the spawn safety check, and a severe stall in ocean and coast lookups that could freeze servers.
- **The source is on GitHub** in branch `pass-70-neoforge-build`, with PR #1 into your default branch. CI is green.
- **CurseForge:** your mod is **approved and public**, with 17 downloads. **Not uploaded yet**: your CurseForge sign-in expired, and I don't enter passwords. **You have no modpack project**, so there was nothing to update. Decisions are below.

## What I fixed

| # | Bug | Where | Effect |
| --- | --- | --- | --- |
| 1 | Compile: wrong `PistonBaseBlock` package | `SluiceValve69` | Pass 70 would not compile |
| 2 | Compile: missing `updateWidgetNarration` | `HoleMenuButton` | Same |
| 3 | Compile: lambdas used a reassigned `floors` variable | `LotPainter` | Same |
| 4 | `Invalid block entity minecraft:barrel` on every world | `Canvas.set` | Barrels and loot went missing, with log spam. Now 0 |
| 5 | Planned spawn could fail the standing check | `WorldGuard.safeSpawn` | Spawn now searches for footing, headroom and sky |
| 6 | **Monument search stalled for over 10 minutes** | `OceanRegions65` cache | Cartographer maps, dolphins and `/locate` could freeze a server. A cache now gives identical values |
| 7 | QA harness measured the origin, not the randomized spawn | `HoleQa` | Street and building checks were measuring the wrong area |
| 8 | CI only ran by hand | `.github/workflows/neoforge-build.yml` | Now runs on pushes and pull requests |

Fix 6 changes speed, not results: the cache returns the same values, so worlds generate identically and old saves are untouched.

## Verified, with evidence

- **Build:** `runData` and `build` pass and produce `dorohedoro-0.1.1-alpha.1.jar`. All 286 classes check out and the metadata is correct. GitHub CI passed.
- **Server runs**, on seeds 12345, 42 and 8675309, plus the rerun in progress: 0 `ERROR` lines and no crashes from the mod.
- **Structures, seed 8675309, located within about 1 km of spawn and inspected:**
  - Stronghold at 4992, 96: at least 12 End portal frames
  - Ancient City at 5104, 96: sculk shriekers present
  - Trial chambers at 4688, 672: trial spawners present
  - Mineshaft at 4512, 816: rails present
- Randomized starts work. Spawns differ per seed: 3871, -2816 / 2160, 1875 / 4599, 823.

## Not verified yet

- **The game client:** no screenshots yet. Planned once the server reruns finish.
- **Ocean monuments, temples, villages, the Nether fortress, bastions and End cities:** the search for these is what stalled. Rerun pending after fix 6.
- **Sewers:** 3 QA checks at the origin's starter crossing still fail. The probable cause is that the code deliberately leaves Deep Dark and Ancient City space untouched, so the chamber may be left uncarved on purpose. That's not proven.
- Seed 42's hillside start trips the "ore and rock in city" checks, probably because the terrain there is natural hillside.
- No full survival playthrough: Ender Dragon, Nether progression and Wardens summoned naturally were not played.

## What I need from you

1. **Sign in to CurseForge** (authors.curseforge.com). Then I can upload the new jar to project 1777423 as Alpha.
2. **Modpack:** you don't have a modpack project. Should I create one, or did you mean one that lives on another account?
3. **Merge PR #1** when you're happy, or tell me to.

## Update: full vanilla structure survey, seed 12345, after fix 6

`tools/qa-world.sh` ran with `QA_STRUCTURES=true` on a real dedicated server. Raw output: `docs/evidence/pass70/structures-seed-12345.txt`. The log had 0 `ERROR` lines and no crash.

**The cache does not change worlds:** the city-layer chunk hashes matched the pre-fix run on **441 of 441** chunks.

| Result | Structures |
| --- | --- |
| **Verified by marker blocks** | Stronghold (12 or more End portal frames), Ancient City (sculk shriekers), trial chambers (trial spawners), mineshaft (rails), Nether fortress (nether bricks), End city (purpur). The End dragon fight exists |
| **Found** | Shipwreck, cold ocean ruin, igloo, trail ruins, woodland mansion, pillager outpost, all 5 village types (plains, desert, savanna, snowy, taiga), ruined portal, swamp hut, bastion remnant |
| **Not found within 100 chunks** | Ocean monument, warm ocean ruin, desert pyramid, jungle pyramid, buried treasure |

**Still a real bug: the monument search takes 708 seconds.** It took about 12 minutes and found nothing. The desert and jungle pyramid searches each took about 2 minutes. A cartographer's ocean explorer map or `/locate` would freeze a server for that long. The cache fixed the other searches, but the monument still searches without success across every candidate. Next step: profile `HoleBiomeSource.getNoiseBiome` for deep-ocean candidates, and check why no deep ocean biome qualifies for monuments.
