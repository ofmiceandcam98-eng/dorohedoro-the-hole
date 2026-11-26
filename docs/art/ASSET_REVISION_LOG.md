# Asset revision log

One entry per revised asset: why it changed, what improved, and how it was checked. Numbers are
measured by `tools/art/compare_sheet.py`-style statistics on the 16x16 image: colour count,
mean luminance (L, 0 to 255) and luminance standard deviation (sd, the "spread"). Vanilla
references for scale: stone 4 colours / sd 11, bricks 9 / sd 22, calcite 6 / sd 19, iron ore
9 / sd 20.

Identifiers, registry names, model names, loot, recipes and block states are unchanged unless
an entry says otherwise. Source for every texture is in `tools/art/` and regenerates the PNGs.

## 2026-11-26: Mojang-standard pass 1

### Terrain

| Asset | Before | After | Why and what improved |
|---|---|---|---|
| `block/limestone` | 8 colours, L171, sd 6 | 4 colours, L186, sd 10 | Was a flat fill with random dark dots. Now stone-style horizontal smears in a cream ramp at vanilla stone's spread. First redraw had pits; in game they repeated as a dotted grid, so they were removed. |
| `block/hell_rock` | 7 colours, L80, sd 5 | 6 colours, L78, sd 9 | Was one flat colour. Now red-violet smears. First redraw ran diagonally and striped when tiled; second had hairline cracks that read as stitches in game. Both fixed. |
| `block/ash_soil` | 6 colours, L94, sd 6 | 7 colours, L90, sd 13 | Was flat with sparse specks. Now granular clumps like coarse dirt: each light grain sits on a darker one, plus ash flecks and char. |
| `block/industrial_debris` | 6 colours, L101, sd 20 | 20 colours, L99, sd 29 | Was two pasted blobs on a flat ground. Now gravel-style chunks (concrete, rusted sheet, brick, steel, plank), each lit on top with a dark gap below. First in-game view was much busier than gravel; ramps were pulled closer and desaturated. |

### Building materials

| Asset | Before | After | Why and what improved |
|---|---|---|---|
| `block/sewer_brickwork` | 6 colours, sd 24, flat bricks | 9 colours, sd 26 | Bricks now have the vanilla bevel (lit top row, shaded bottom row and right column), per-brick tone, dark grimy mortar, one damp streak running from a joint down across the course below. |
| `block/soot_blackened_brick` | 9 colours, L62 | 12 colours, L69, sd 12 | Was near-black flat bricks. Soot now thick on top courses and thinner below, where the red-brown clay shows; lighter mortar keeps the bond readable in the dark. The clay red was first too strong: in game the red bricks repeated as a regular pattern across a wall, so it was pulled toward the soot. |
| `block/seepage_brick` | 17 colours of noise | 11 colours | Bevelled teal bricks, saturated water tracks under joints, dried mineral crust. The crust was toned down twice (sheet review, then in game): bright pixels read as random white dots on a wall. |
| `block/patchwork_masonry` | 18 colours, sd 41, no bevels | 15 colours, sd 24 | Now a red brick bond repaired brick by brick with grey stone, limestone and two concrete plugs, all on one light direction. The first redraw used a large ashlar block; tiled, it made a checkerboard, so it was replaced. The second measured sd 37, busier than vanilla cobblestone (29); limestone and brick values were pulled together to sd 24 (vanilla bricks 22). |
| `block/smoke_stained_concrete` | 13 colours, random green and black specks | 5 colours, sd 9 | Near-flat concrete like vanilla's, a formwork seam, soot smudges spread evenly one step darker. Tie holes (a pegboard in game) and a soot band along the top (a stripe on every block of a tall wall) were both tried and dropped. |
| `block/water_damaged_plaster` | 6 colours, grey-green bars read as pipes | 4 colours, sd 6 (sandstone top 6, quartz 6) | Trowelled cream plaster with a faint dried blotch one step darker. A full-width tide line was tried and dropped (it would stripe every block of a tall wall). A stronger blotch read as polka dots in game and was faded; a drip track and darker trowel marks read as dashes and were removed (trowel texture now uses only the three light shades). |
| `block/rusted_plating` | 7 colours, flat | 9 colours, sd 23 | Staggered hull plates with bevels, rivets with shadows, rust running from rivets and blooming on lower edges; rust colours toned so the streaks are not orange bars. |
| `block/rusted_industrial_sheet` | 9 colours, bars with dots | 13 colours, sd 23 | Corrugated: each pixel's colour comes from the ridge phase (lit face, crest, falling face, trough) and a hand-drawn mask of old paint, rust and fresh rust. Paint holds on top and flakes toward the bottom. |
| `block/galvanized_plating` | 3 colours | 7 colours, sd 29 | Four bevelled panels, zinc spangle as a few angular facets, one white-rust spot. |
| `block/chitin_plating` | 3 colours, flat with a cross | 7 colours, sd 29 | Overlapping scutes laid like shingles, glossy top left, dark lip on the row below. |

### Ores

| Asset | Change |
|---|---|
| `block/ore/zinc`, new `zinc_deepslate` | Irregular 3 to 8 pixel grains in a blue-grey metallic ramp, host-shade rim on some sides. Separate deepslate drawing (mirrored arrangement, deepslate rim). |
| `block/ore/sulfur`, new `sulfur_deepslate` | Lemon crystal crusts instead of gold-like blobs. |
| `block/ore/rock_salt`, new `rock_salt_deepslate` | Overlapping cubic crystals with lit top-left faces. |
| `block/ore/oil_shale`, new `oil_shale_deepslate` | Thin broken dark seams with one or two highlight pixels. Fat lenses with a lit top edge were tried first and read as rows of closed eyes in game. |
| `datagen/ModModels.java`, `src/generated/.../deepslate_*_ore.json` | Deepslate ores now use their own overlay (`block/ore/<mineral>_deepslate`). The generated JSON was edited to match; the next `runData` produces the same files. |

### Lighting

| Asset | Change |
|---|---|
| `models/block/cavern_lamp_{standing,hanging,wall}.json` | Rebuilt on the vanilla lantern construction: one 6x6x7 body, a 4x4x2 cap, a crossed-plane handle (standing) or hook (hanging), explicit UVs into one atlas, `cutout` render type. Fixes: the standing lamp floated 1 px above the floor; the hanging chain sampled a mostly transparent area of the vanilla chain texture; posts and plates sampled arbitrary areas through default UVs. The wall lamp keeps a bracket arm and gains a wall plate. Every model stays inside the unchanged collision shapes. |
| `models/block/cavern_lamp_wall.json`, `world/light/CavernLampBlock.java` | Second change after the close-up review: from the front, the bracket arm (which met the cap's side) was hidden behind the body, so the lamp looked like it floated. The arm now runs from a 4x5 wall plate over the top of the cap, and the body sits 1 px lower (y3-12). The four wall collision boxes were extended down by 1 px (y4 to y3) to keep covering the model. No block state or light change. |
| `textures/block/cavern_lamp.png` | New hand-drawn atlas: aged brass frame and cap, glass with a pale moth-scale glow (the lamp's recipe uses Luminescent Scales). The glow was brightened after the first night view, where it looked weak beside the vanilla lantern. |
| `textures/block/cavern_lamp_frame.png`, `cavern_lamp_glow.png` | Removed. Only the old lamp models used them; `asset_lint.py` confirms nothing else refers to them. |
| `models/item/cavern_lamp.json`, new `textures/item/cavern_lamp.png` | Inventory now shows a flat sprite like vanilla's lantern items instead of the 3D block model. |

### Items

| Asset | Change |
|---|---|
| `item/zinc_ingot`, `item/brass_ingot` | Were 8x4 bars. Now one shared 14x8 cast-bar silhouette (as vanilla shares one ingot shape across metals), material-coloured outline, lit top face. |
| `item/raw_zinc` | Was scattered small bits. Now a fused metal lump with a smaller lump, like the raw metal family. |
| `item/raw_sulfur` | Was scattered bits. Now a crust of stubby crystals. |
| `item/rock_salt` | Was three small pale squares. Now three cubic crystals with lit faces. |
| `item/oil_shale_chunk` | Was scattered specks. Now one laminated dark chunk, coal-sized. |
| `item/devil_sigil` | Was 32x32 (half-size pixels). Now 16x16: bronze ring, dark field, diamond motif. |
| `item/smoke_conduit`, `item/preserved_smoke_conduit` | Were identical two-colour shapes. Now a shaded bent duct; the preserved one is cured darker with brass collars, so the two are told apart in the inventory. |
| `item/burrower_claw` | Was a one-pixel diagonal. Now a thick ivory talon on a dark base. |
| `item/roach_antenna` | Was two one-pixel lines. Now one segmented antenna with its socket, stick-sized. |

### Tooling added

| File | Purpose |
|---|---|
| `tools/art/author_textures.py` | Source of all revised block textures and ore overlays. |
| `tools/art/author_items.py` | Source of the revised item sprites. |
| `tools/art/author_lamp.py` | Cavern Lamp atlas, item sprite and three block models plus the item model. |
| `tools/art/asset_lint.py` | Technical checks (see `ART_DIRECTION_MOJANG_STANDARD.md`). |
| `tools/art/compare_sheet.py`, `tools/art/reference_matrix.json` | Before, after, vanilla and 3x3 tiling sheets. |
| `client/qa/ArtClientQa.java`, `runQaClient -Pqa_mode=art` | In-game gallery and lamp scene in the real client. |

### Unrelated fix made to build

`src/main/java/com/dorohedoro/hole/qa/SorcererTownQa.java` (uncommitted Sorcerers' World towns
work in this worktree) did not compile: a local variable named `net` shadowed the `net.minecraft`
package in a fully qualified `Blocks.PACKED_MUD`. Fixed by importing `Blocks` and using the short
name. No behaviour change.
