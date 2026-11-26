# Overnight debrief, 2026-11-04

Everything below ran on this PC or on GitHub's servers. Anything unproven is listed under **Not verified**.

## TL;DR

- **Your crash is fixed and proven.** The Nov 1 crashes were "Not building!" in the smoke magic renderer. The same test fails without the fix and is clean with it.
- **The new jar is installed** in your Dorohedoro launcher profile. The old jar is backed up in `old-jars`. Open the launcher, pick "Dorohedoro" and press Play.
- **Pass 70 compiles against NeoForge 21.1.265**, on this PC and in GitHub CI. It had 3 compile errors, all fixed.
- **It runs on a real dedicated server and the real game client:** fresh worlds on 4 seeds, 0 errors, and real screenshots.
- **Vanilla structures were located and inspected:** stronghold with a full End portal, Ancient City, trial chambers, mineshaft, Nether fortress and End city.
- **CurseForge is not uploaded yet.** Your sign-in expired and I don't enter passwords. You have no modpack project.

## Your two messages

- **"Spawned really weird":** that was the automated test camera, not your spawn. The test flies a spectator camera around for screenshots. One step dropped it 150 blocks and it stopped 12 blocks under the street. Spectators see through blocks, so the city looked like it was floating over a void. The real spawn is 4599, 81, 823, standing on the street (screenshot 01). The test now returns to the exact spawn point, and the rerun was correct.
- **"And crashed":** the window closing was the test quitting on purpose when it finished ("tour complete, quitting"). Nothing crashed today: there are 0 crash reports. Your real crashes were on Nov 1 (`crash-2026-11-01_17.45.12` and `_18.40.52`), and that is the bug fixed below.

## What I fixed

| # | Bug | Where | Effect |
| --- | --- | --- | --- |
| 1 | **Client crash "Not building!"** while magic smoke was on screen | `SmokeMeshClient` | Your Nov 1 crashes. Fixed and proven by a before-and-after test |
| 2 | Compile: wrong `PistonBaseBlock` package | `SluiceValve69` | Pass 70 would not compile |
| 3 | Compile: missing `updateWidgetNarration` | `HoleMenuButton` | Same |
| 4 | Compile: lambdas used a reassigned `floors` variable | `LotPainter` | Same |
| 5 | `Invalid block entity minecraft:barrel` on every world | `Canvas.set` | Barrels and loot went missing, with log spam. Now 0 |
| 6 | Planned spawn could fail the standing check | `WorldGuard.safeSpawn` | Spawn now searches for footing, headroom and sky |
| 7 | Coast and ocean lookups recomputed constantly | `OceanRegions65` cache | Most `/locate` and explorer-map searches now finish in under a second. Worlds are identical: 441 of 441 chunk hashes matched |
| 8 | QA harness measured the origin, not the randomized spawn | `HoleQa` | Checks now measure the area you actually spawn in |
| 9 | CI only ran by hand | GitHub workflow | Now runs on every push and pull request |

New test tools: `runQaClient` creates a world in the real client, takes screenshots and quits. `QA_STRUCTURES=true tools/qa-world.sh` locates and inspects vanilla structures on a dedicated server.

### The crash, explained

Minecraft 1.21 changed how render buffers work: asking for a new buffer type closes the previous one. The smoke renderer fetched 7 buffers at the start of each frame and drew into them later, so it drew into closed buffers. That's the "Not building!" crash. It now fetches each buffer right before drawing.

**Proof**, in `docs/evidence/pass70/client/`: the test fires all 8 magic types in one frame.

- **Old renderer:** 6 of 8 failed with "Not building!". Pass 70 catches the error and skips the effect; your Nov 1 build crashed instead.
- **Fixed renderer:** 0 failures, 0 errors, and all 8 meshes are drawn (screenshot 05).

## Verified, with evidence

- **Build:** `runData` and `build` pass. Jar SHA-256 `e8dad782…c243f8`, 1,389,854 bytes. GitHub CI passed.
- **Real client**, seed 8675309, screenshots in `docs/evidence/pass70/client/`: spawn street, aerial, overview, night street and all magic smoke at once. 0 `ERROR` lines.
- **Dedicated server** on seeds 12345, 42 and 8675309: 0 `ERROR` lines and no crashes.
- **Structures verified by their blocks:** stronghold (12 or more End portal frames), Ancient City (sculk shriekers), trial chambers (trial spawners), mineshaft (rails), Nether fortress (nether bricks) and End city (purpur). The dragon fight exists.
- **Structures found:** shipwreck, cold ocean ruin, igloo, trail ruins, woodland mansion, pillager outpost, all 5 village types, ruined portal, swamp hut and bastion remnant. Raw output: `docs/evidence/pass70/structures-seed-12345.txt`.
- **Randomized starts work.** Spawns differ per seed: 3871, -2816 / 2160, 1875 / 4599, 823.

## Open problems, in priority order

1. **The ocean monument search is extremely slow:** more than 10 minutes, and nothing found. If a cartographer's ocean explorer map or `/locate monument` is used, the server freezes for that long. Stack samples show the time is in vanilla's monument placement check and chunk loading against our biome source. I didn't attempt a rushed worldgen change overnight.
2. **The desert and jungle pyramid searches take about 2 minutes.** That's the same kind of problem, but milder.
3. **Not found within 100 chunks of spawn:** monument, warm ocean ruin, desert pyramid, jungle pyramid and buried treasure. They may just be far away on that seed, or blocked by the city biomes. Not proven either way.
4. **Sewers:** 3 QA checks at the origin's starter crossing fail. The code deliberately leaves Deep Dark and Ancient City space untouched, so that chamber may be uncarved on purpose. Not proven.
5. Seed 42's hillside start trips the "ore and rock in city" checks, probably because the terrain is natural hillside.
6. No full survival playthrough: the Ender Dragon, Nether progression and natural Warden summons were not played.

## What I need from you

1. **Play-test** with the launcher's "Dorohedoro" profile. Cast every magic type near each other. Before this fix that would crash.
2. **Sign in to CurseForge** (authors.curseforge.com) so I can upload the new jar to project 1777423 as Alpha.
3. **Modpack:** you don't have a modpack project. Should I create one, or does it live on another account?
4. **Merge the GitHub PR** when you're happy, or tell me to.
