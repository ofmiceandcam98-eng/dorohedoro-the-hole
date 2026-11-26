# Sorcerers' World: handoff

Read this first, then `SORCERERS_WORLD_MASTER_PLAN.md`.

## Where the work is

- Branch `sorcerers-world/towns`, in the worktree `C:\dev\dorohedoro-sorcerers`. It was cut from `pass-70-neoforge-build`
  at 2c21a69.
- When this branch was made, another session had large **uncommitted** changes in `C:\dev\dorohedoro-the-hole`
  (Ash-Snow, hazards, factions, Cross-Eyes). This branch does not contain them.
- Files touched outside the new package, which are the likely merge points:
  - `registry/ModWorldgen.java`: one added line.
  - `qa/HoleQa.java`: one added line.
  - `client/qa/ClientQa.java`: an early return in `visit()` plus the new `visitSorcerers()`. The other session also had
    `ClientQa.java` modified, so expect a small conflict there.
- Everything else is new: `worldgen/sorcerer/*`, `qa/SorcererTownQa.java`, the data files listed in the worldgen doc,
  the loot tables, `tools/gen-sorcerer-loot.mjs`, `tools/qa-sorcerers.sh`, `tools/qa-sorcerer-planner.sh`,
  `tools/fixtures/sorcerer/`, and `docs/sorcerers-world/`.

## Build and test

```bash
./gradlew compileJava                    # compiles cleanly as of this handoff
bash tools/qa-sorcerer-planner.sh 40     # offline planner fixture (javac + java only, about 10 s)
bash tools/qa-sorcerers.sh 12345 4       # real dedicated server, forward and reverse runs, determinism comparison
./gradlew runQaClient -Pqa_visit="sw-city:X:Z:Y;swin-mask_shop:X:Z:Y:YAW"   # screenshots in runs/qaclient/screenshots
```

- Never run two Gradle processes in the same tree at once. `qa-sorcerers.sh` calls Gradle four times in sequence, so do
  not edit sources while it runs: later invocations compile whatever is on disk.
- `DOROHEDORO_QA_SORCERERS=false` skips the settlement section of `HoleQa`.
- `DOROHEDORO_QA_SW_DUMP=x,z` dumps one settlement chunk's settlement-written blocks into the report, for diffing a
  determinism mismatch.

## How it works, in one paragraph

`SorcererTownStructure` reads natural terrain from the noise generator, picks city, town or hamlet from the biome, and
calls `TownPlanner.planViable`. The plan is a road polyline network plus plots. It goes into one `TownRoadsPiece` and
one `TownBuildingPiece` per plot. Pieces paint one chunk at a time, through a `Painter` whose random generator is
re-seeded from the plot seed, so a building split across chunks comes out identical from every side. `RoadNet.sample`
is the single source of truth for "is this column road, and at what height". The planner and the road painter both use
it.

## Rules for adding a building

1. Add a `TownCatalog.Spec`: footprint, door column, height budget (roof and chimney included), max slope, ring weights.
2. Add a painter in `SorcererBuildings`:
   - Start with `site()`.
   - Use `shell()` for storeys, `upstairs()` for flights.
   - Keep furniture off `stairCell()` cells.
   - Give every lit room a `p.probe("room", ...)` and a lantern. Use a hanging lantern only under a ceiling, a
     `tableLantern` under a roof, and `beams()` in tall halls.
   - Draw every decision from `p.rng`, never from the chunk's random generator.
3. Run the planner fixture, then `qa-sorcerers.sh`. QA must show the new type reachable, lit and roofed.

## Release

BETA 0.4 is built from this branch: `C:\dev\curseforge-upload\BETA0.4\` holds the jar, changelog, screenshots, install
script and `UPLOAD STEPS.md`. It does **not** contain the other session's uncommitted Hole work (Ash-Snow, hazards,
factions, Cross-Eyes).

## Known issues, most important first

1. **Vanilla terrain decision.** New worlds use vanilla terrain and biomes, the old `sw_*` biomes only in old saves. That
   saved worlds keep their stored generator is assumed, not tested; test on a backup.
2. In the offline fixture, about 1 in 300 plans still has a handful of 2-block road steps at an odd junction. The road
   is still walkable around them, but the steps look rough.
3. Cities average about 27 buildings on rolling ground and about 22 on rough ground. Density is limited by road
   frontage; landmarks and back-lot courtyards would add more.
4. Not yet seen in a real client by a person: proportions, palettes and lighting still need the owner's review.
5. Townsfolk are vanilla villagers, not sorcerers.
