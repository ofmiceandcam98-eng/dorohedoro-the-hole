# Pass 40 — Citywide relief, river bends, and functional skyline rooms

**Status: SOURCE PROTOTYPE, NOT COMPILED IN NEOFORGE, NOT INSTALLED OR PUBLISHED.**

## Intent and non-negotiable preservation
- Affects world profiles with `world_detail_version >= 24` only. Do not change legacy profiles; old save chunks should not have version seams.
- Keep spawn plaza level, keep water at one consistent local elevation through river ROW/floodplains, and never place buildings inside the wider river ROW.
- Keep night-spawning opportunities, sparse street lights, fewer exposed chests, vanilla survival and mines, existing furnished interiors, and three-slot technique HUD.
- Do not start the deferred villager/faction, Ash-Snow/Sludge-Ice, or unrelated magic progression overhauls.

## Geometry changes
1. `CityTerrain40`: new coordinate-seeded multiscale macro-relief up to 28 observed blocks in the four-seed fixture, with broad ~300-block and medium ~155-block spatial knots. The canal/floodplain envelope is masked to zero and gradually rises over 140 blocks outside the quay; spawn core stays level. Existing `CityElevation.offset` remains unchanged; the new terrain blends its unrounded equivalent to avoid two-block rounding discontinuities.
2. `Canvas`, `CityPainter`, `CityPopulator`, and headless `HoleQa`: all relevant surface and spawn elevation calculations now route through version-aware `CityTerrain40`, with an extended flat building foundation in new worlds.
3. `RiverChannels24` / `CityPlan`: v24 river ROWs can widen to 24 blocks per side (adaptive narrowings when two arterials are close). Max bend is ±17 and within-row bank clearance is maintained. Existing 0–23 plans are unchanged.
4. `SewerCaveBranches` / `SewerCavernProfile`: new-profile cave-landings use the same actual surface field as Canvas; older cavern landing behavior is preserved.
5. `Skyline40` / `Tower`: new-world qualifying large plain roofs can gain small enclosed repair/laundry annexes, with a usable door, roof corridor, vanilla workstations, and asymmetrical parapets. No added streetlights or exposed treasure chests; selected roofs remain empty for movement and existing tanks/farms keep priority.
6. New-world preset set to `world_detail_version: 24`; codec accepts v0–v24.

## Offline tests (fixture-scoped)
- Compiled actual `CityHash`, `CityElevation`, `CityTerrain40` and `Line` with a line-plan fixture: 486,360 sampled positions across seeds 12345, 8675309, 42, and 20261101. No >1 block neighboring height change, no spawn-height violations, no canal/quay level violations; observed relief 0–28 blocks.
- Compiled actual `RiverChannels24` with simple mock Minecraft blocks and canvas: 151,236 station samples; all channel centers wet and bank limits respected, observed max bend 16 blocks, and no >1 block centerline movement between neighbors.
- Separate seeded arterial-spacing fixture: 57,200 adjacent line pairs, min corridor clearance 16 blocks, zero collisions.
- Compiled actual `Skyline40` with mock canvas: 2,979 shelters placed during 16,000 candidate tests; no collisions with simulated NW staircase approach.

## Not validated / release blockers
- Full Gradle / NeoForge 1.21.1 21.1.265 compile or runData, and headless generation QA after this pass.
- Real in-client visual quality, water hydraulics, road grade across intersections, lot entrance access and jump-height constraints, cave tunnel headroom, and all 24-version saved-world behavior.
- Large terrain changes may create inaccessible entrances on buildings with level foundations; these require Minecraft-world walkability QA and possibly entrance ramps/grading. The fixture proves only neighboring **terrain column** slope bounds.
- Distinct building footprints and facades still need an in-game art acceptance pass. The skyline addition addresses only selected plain roofs.
- Remaining high priorities: polish actual PNG textures (some are still placeholders), diagnose technique crash from `crash-reports/*.txt` or `logs/latest.log`, and validate HUD/menu inside Minecraft at multiple GUI scales.
- This patch must not go to CurseForge before build + visual + spawn + survival + cast/save/reload acceptance.

## Next QA checklist (when installs are approved)
1. Compile and runData; verify no registries, blockstates, missing textures, or world codec errors.
2. Use separate fresh new world seeds 12345 and 8675309, forward/reverse chunk generation hashes; preserve old save backup.
3. Fly 500+ blocks along two new rivers; inspect whether banks look natural and water remains continuous, plus bridges and fish.
4. Walk uphill streets, cross tram grades, enter 20 different homes and shops, and reach roof doors; log all 2-block entrance discontinuities.
5. Verify furnished rooms are internally separated, purpose-led, navigable, and not chest clutter.
6. Inspect lit/dark town at midnight on Normal difficulty; confirm natural hostile mob spawning and no accidental full-night illumination.
7. Test wall lamps, UI visibility, each three-slot technique, persistence, and crash-report output.
