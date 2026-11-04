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
