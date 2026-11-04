# Pass 70 takeover log (Claude, 2026-11-04)

Running log of the takeover. Each entry is something that actually ran.

## Setup

- Workspace `C:\dev\dorohedoro-the-hole` was cloned from GitHub. Branch `pass-70-neoforge-build` sits on top of the owner's `pass-32a-build-test` (Pass 32E), with the Pass 70 `SOURCE/` tree laid over it.
- The takeover package is extracted at `C:\dev\takeover-pass70`.
- The Gradle wrapper jar was kept from Pass 32E. Its SHA-256 is `423cb469…e8d9`, which matches services.gradle.org's published value for 9.2.1.
- Java 21.0.12.1 (Microsoft), Gradle 9.2.1, NeoForge 21.1.265, `disable_recompilation=true`.

## Build: passed

Real NeoForge compile errors found and fixed in commit `d08d849`:

1. `SluiceValve69` imported `PistonBaseBlock` from `block`. In 1.21.1 it is in `block.piston`.
2. `HoleMenuButton` did not implement `updateWidgetNarration`, which every `AbstractButton` must.
3. `LotPainter.tenement` and `apartment` lambdas captured `floors` after it was reassigned. A `final` copy now captures it, with the same behaviour.

`runData` passed and regenerated the `surgical_reattachment_kit` recipe and advancement. `build` passed: `build/libs/dorohedoro-0.1.1-alpha.1.jar`, 1,376,062 bytes. The jar is intact, all 286 classes parse with `javap`, all 192 top-level classes are present, and the metadata targets 1.21.1 and NeoForge 21.1.265 or later.

## Runtime

Seed 12345, radius 8, dedicated `qaServer` run:

- **Boot passed.** The server booted, generated 289 chunks, verified the Hole generator, set spawn to 3871, 85, -2816 (randomized start), and shut down cleanly.
- **Real bug:** 3 log lines read `Failed to create block entity minecraft:barrel` / `Invalid block entity`. A barrel's data is left behind where a later step placed a different block.
- **6 QA failures, triage pending:** street reachability 0.9841, enclosed lots 40 of 102, 2 districts, and 3 sewer checks.

## Status

See below as it fills in.

## Overnight findings (2026-11-04, about 05:00)

- **Fixed:** barrel block entities left stale by a later write (`Canvas.set`). The log now has 0 `Invalid block entity` lines. Commit `3bbdfbf`.
- **Fixed:** QA harness now centres on the randomized spawn, not the origin. Commit `3bbdfbf`.
- **Pushed** branch `pass-70-neoforge-build`. Opened PR #1 into `gate-0-1-hole-world`. CI now runs on push and pull requests.
- **3 seeds on a dedicated server (12345, 42, 8675309):** 0 ERROR lines and no crashes in the server logs.
- **Spawn:** seed 8675309's planned spawn failed the QA `isAir` feet check. Added `WorldGuard.safeSpawn`, which searches for footing plus two blocks of open-sky headroom. On that seed the planned spot already passed the new check, so the feet block is probably non-colliding, like snow or a carpet. Not yet confirmed.
- **Vanilla structures, seed 8675309, located and inspected by StructureQa:**
  - Stronghold at 4992, 96 has at least 12 End portal frames.
  - Ancient City at 5104, 96 has sculk shriekers.
  - Trial chambers at 4688, 672 have trial spawners.
  - Mineshaft at 4512, 816 has rails.
- **Performance bug found:** searching for `minecraft:monument` stalled for more than 10 minutes, all inside `HoleBiomeSource.getNoiseBiome`, then `CoastalShelf66.distanceAt`, then `OceanRegions65.depthAt`. Cartographer explorer maps, buried-treasure maps and `/locate` use this same search, so the stall can freeze survival servers.
- **Open:** 3 sewer checks at the origin crossing. Seed 42 is a hillside start where the ore and rock in-city checks fire, which may be natural terrain. The district-count check is too strict for small radii.

## Next steps (resume here after a context reset)

1. Read `/c/dev/p70-cache-run.txt`. It should show city hashes for seed 12345 unchanged against `/c/dev/report-12345-before-cache.json`, and monument and other search times. Then commit the OceanRegions65 cache, safeSpawn and StructureQa, and push.
2. Client test: build the jar, install it to `C:\Users\Cam\Dorohedoro-Minecraft\mods` after backing up the old one to `old-jars`, launch through the Minecraft Launcher with computer-use, create a new world, and take real screenshots.
3. CurseForge: mod project 1777423 (`dorohedoro-the-hole`). Check approval and upload the new jar as Alpha. Check whether a modpack project exists; on 2026-11-01 the account had none. Never create a duplicate.
4. Write the debrief in `docs/OVERNIGHT_DEBRIEF_2026-11-04.md` and in chat.

## CurseForge state (checked 2026-11-04, public profile noremacxxi)

- The account owns exactly **1 project**: mod "Dorohedoro: The Hole", ID 1777423, slug `dorohedoro-the-hole`. It is **approved and public**, released 2026-11-01, with 17 downloads.
- There is **no modpack project** on the account, so it was not created. That needs the owner's decision.
- The author dashboard session expired. GitHub OAuth asked for a password, so I stopped. **The owner must sign in to CurseForge** before any upload.
- CI on `pass-70-neoforge-build` passed: https://github.com/ofmiceandcam98-eng/dorohedoro-the-hole/actions/runs/41816727987

## State at about 05:40 (resume here)

- Pushed `1266424` to `pass-70-neoforge-build`. PR #1 is open. The previous CI run was green.
- The vanilla structure survey for seed 12345 is in `docs/evidence/pass70/structures-seed-12345.txt`. Nearly every structure was found. Missing: monument, warm ocean ruin, desert and jungle pyramids, buried treasure.
- **Open performance bug:** the `minecraft:monument` search took 708 s, and the desert and jungle pyramid searches about 120 s each. Next step: time `HoleBiomeSource.getNoiseBiome` directly, for example by calling it 100k times from QA, and find the hot path. Suspects are `CoastalShelf66.distanceAt`, which runs about 40 or more `depthAt` probes, and the district and climate sampling. Any fix must keep the city hashes identical against `/c/dev/report-12345-before-cache.json`, using `node tools/qa-compare.mjs`.
- Then: build the jar, install it to `C:\Users\Cam\Dorohedoro-Minecraft\mods` after backing up the old one to `old-jars`, run a client test through the Minecraft Launcher with computer-use, update `docs/OVERNIGHT_DEBRIEF_2026-11-04.md`, and send the user the debrief.
- CurseForge needs the owner to sign in. There is no modpack project.
