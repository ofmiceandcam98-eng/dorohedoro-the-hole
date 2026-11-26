# Visual QA report

Real-client review of the Mojang-standard art pass. Every result here comes from screenshots
taken by the actual NeoForge 1.21.1 client (`./gradlew runQaClient -Pqa_mode=art`, scene built by
`client/qa/ArtClientQa.java`), not from previews. Evidence is in `docs/evidence/art-pass1/`. Four runs were made; each fixed what the previous one showed.

**Not covered:** shader packs (none installed; vanilla rendering is the baseline), multiplayer,
the new item sprites in the full inventory screen (hotbar only), mobs (not revised this pass),
portal and door blocks beyond one gallery view (not revised).

## Setup

- Gallery in the sky at Y 200 over the default Hole world (seed 8675309), spectator camera,
  GUI hidden, weather clear, day/night cycle frozen.
- Each custom block as a 3x3 panel, then a 3x3 panel of its vanilla reference, four pairs per
  group (pairs listed in `VANILLA_REFERENCE_MATRIX.md` and in the client log).
- Conditions, per group: **day** (time 6000), **close** (5 blocks), **distance** (34 blocks),
  **night** (time 18000, moonlight only), **torch** (night plus torches in front of the
  panels), **enclosed** (night, ceiling and walls around the gallery: no sky light, torches only).
- Lamp scene: Cavern Lamp standing, hanging and wall-mounted beside the vanilla lantern, soul
  lantern, hanging lantern and wall torch, at day and night, plus close-ups.
- Hotbar shot: Cavern Lamp, lantern, soul lantern, raw sulfur, rock salt, raw iron, iron ingot,
  raw zinc, zinc ingot, in creative mode with the HUD on.

## Run 1 (first redraws): what the client showed

| Asset | Finding | Action |
|---|---|---|
| Limestone | Pits repeated as a dotted grid of vertical dashes across a wall | Pits removed |
| Hell Rock | Hairline cracks read as repeated stitches | Cracks removed |
| Industrial Debris | Far busier and more saturated than gravel beside it | Ramps pulled together and desaturated |
| Patchwork Masonry | Large ashlar block tiled into a checkerboard | Rebuilt as a repaired brick bond |
| Smoke-Stained Concrete | Tie holes made a pegboard grid | Tie holes removed |
| Water-Damaged Plaster | Stain blotch repeated as polka dots | Stain faded to one step darker |
| Deepslate Oil Shale | Lenses with a lit top edge looked like rows of closed eyes | Redrawn as thin broken seams |
| Cavern Lamp | Correct scale beside the lantern; glow weak at night | Glow ramp brightened |
| Enclosed condition | Sky light leaked in from the open back of the gallery | Back wall added to the harness |
| Lamp close-up | Camera missed the lamps | Three dedicated lamp close-ups added |
| Sewer, soot and seepage brick; ash soil; chitin; rusted and galvanized plating; corrugated sheet; zinc, sulfur and rock salt ores | Sit beside their vanilla pairs without standing out; ores match vanilla grain density; dark materials stay readable at night and by torch light | None |
| Cavern Lamp item sprite | Reads as a lantern-family item in the hotbar and in hand | None |

The lesson, now rule 6 in the art direction: a single distinct feature in a 16x16 tile becomes
a grid on a wall. Wear marks must be low-contrast texture, not objects.

## Run 2 (after the run 1 fixes)

0 ERROR lines, 0 exceptions, 49 screenshots.

| Asset | Finding | Action |
|---|---|---|
| Limestone, Hell Rock | Clean stone texture at close range, no repeating marks | None |
| Smoke-Stained Concrete | No pegboard, but the soot band along the top of each block and its short drips still showed as a repeated row | Soot spread evenly instead |
| Water-Damaged Plaster | Blotch faint enough; the drip track still read as small dashes | Drip removed |
| Patchwork Masonry | Reads as a repaired brick wall; measured spread 37, above vanilla cobblestone's 29 | Brick and limestone values pulled together (spread 24) |
| Enclosed condition | Fully enclosed now: torch light only. Dark materials (soot brick, seepage brick, deepslate ores) stay readable | None |
| Cavern Lamp, standing and hanging | Same scale and construction as the vanilla lantern and soul lantern beside it; brass and moth-glow palette distinct but not out of place | None |
| Cavern Lamp, wall | From the front the bracket was hidden behind the body, so the lamp looked like it floated | Arm moved over the cap, wall plate enlarged, wall collision boxes extended 1 px down |
| Hotbar: raw sulfur, rock salt, raw zinc, zinc ingot | Same canvas fill and outline weight as raw iron and iron ingot beside them | None |

## Run 3

0 ERROR lines, 0 exceptions, 50 screenshots (adds a second hotbar shot).

| Asset | Finding | Action |
|---|---|---|
| Smoke-Stained Concrete | Clean: seam and even smudges, no rows or grids | None |
| Patchwork Masonry | Reads as a repaired brick wall beside vanilla bricks | None |
| Cavern Lamp, wall | The arm now shows running back over the cap from the front; the lamp no longer looks like it floats | None |
| Hotbar 2: oil shale chunk, devil sigil, smoke conduit, preserved smoke conduit, burrower claw, roach antenna, brass ingot beside coal and bone | Same scale and outline weight as coal and bone; the two conduits are told apart by the brass collars | None |
| Ores under torch light | Zinc, sulfur, rock salt and oil shale sit beside iron, gold, diamond and coal ore at matching grain density | None |
| Water-Damaged Plaster | Darker trowel pixels still read as small dashes | Trowel texture rebuilt from the three light shades only |
| Soot-Blackened Brick | The clay-red bricks repeat as a regular red pattern across a wall | Red pulled toward the soot |
| Seepage Brick | Mineral crust still shows as bright specks at distance | Crust toned down |

## Run 4 (final)

0 ERROR lines, 0 exceptions, 50 screenshots. This run shows every asset as it now stands in
the repository.

| Asset | Finding | Status |
|---|---|---|
| Soot-Blackened Brick | The clay red now reads as uneven firing, not a pattern | Pass |
| Seepage Brick | Crust no longer specks at distance | Pass |
| Water-Damaged Plaster | Dashes gone. **Residual:** the single faint blotch still shows as soft spots at a regular interval at close range. Mild, but a plain wall of it will show the repeat. | Pass with a known residual. Next step: drop the blotch or spread it across the tile, then re-run |
| All other revised blocks, ores, the Cavern Lamp and the item sprites | Unchanged since runs 2 and 3, where they passed | Pass |

Evidence in `docs/evidence/art-pass1/`:

- `final-*.jpg`: run 4 screenshots for every condition (day, close, distance, night, torch,
  enclosed), the lamp close-ups and both hotbars.
- `before-after-*.jpg`: run 1 (first redraw) beside run 4 (final), same camera.
- `sheet-*.png`: before (git HEAD), after, vanilla references and 3x3 tilings, nearest-neighbour.

## Handoff

**Done this pass (all checked in the real client):** 14 building and
terrain textures, 8 ore overlays (4 new deepslate variants, wired through datagen), the
Cavern Lamp (atlas, three models, item sprite, wall collision), 11 item sprites. Tooling:
hand-drawn texture sources, technical lint, review sheets, the in-game gallery mode.

**Not done, in priority order:**

1. **Mobs.** All eight need a remodel at whole-pixel cube sizes with 64x64 atlases and
   re-pivoted animations, keeping the approved identities (see `ASSET_QUALITY_AUDIT.md` section
   6). This is the biggest remaining gap. One creature per session, Sewer Leech and Glow Moth
   first (most visible). Add a mob row to `ArtClientQa` that spawns each beside its vanilla
   references with `NoAI`.
2. **Portal and door blocks.** Nine 32x32 textures to redraw at 16x16 (section 5 of the audit).
3. **Item refinements.** Smoke flask glass outlines, food sizes, material-coloured outlines on
   drops (section 7).
4. **Architecture at scale.** The textures now sit beside vanilla, but the same standard
   applies to block palettes and buildings. Next useful check: run the normal city tour
   (`./gradlew runQaClient`) and the Sorcerers' World visits with the new textures and look for
   palette chaos and repetition across whole streets, not single blocks.
5. Shader-pack comparison (none installed here).

**How to check any future asset:** see the process in `ART_DIRECTION_MOJANG_STANDARD.md`.
In short: `py tools/art/asset_lint.py --vanilla <root>` (0 errors), `py tools/art/compare_sheet.py
--vanilla <root> --out sheet.png block/<name>`, add the block to `ArtClientQa.GROUPS`, then
`./gradlew runQaClient -Pqa_mode=art` and read the screenshots in `runs/qaclient/screenshots/`.
The vanilla root is the 1.21.1 client jar
(`~/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar`) extracted to any
scratch folder; never commit it.
