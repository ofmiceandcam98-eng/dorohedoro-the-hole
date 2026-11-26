# Art direction: the Mojang standard

Every visual asset in Dorohedoro: The Hole has to look like Mojang made it. A custom block placed
beside vanilla stone, brick, deepslate, copper or a lantern must not stand out as "the modded
one". It may look unfamiliar, but it has to be built the same way vanilla art is built.

This rule replaces any earlier direction that asked for more realistic, more detailed or
higher-resolution art. When the two conflict, Minecraft's art direction wins. We adapt
Dorohedoro into Minecraft's style; we do not replace Minecraft's style with Dorohedoro concept
art. The Hole stays grimy and decayed, and the Sorcerers' World stays strange and luxurious in
places, but both get there through materials, architecture and lighting, not by making every
block nearly black.

The rule holds at every scale: pixels, blocks, mobs, buildings, neighbourhoods, whole
dimensions. A perfect texture used in a chaotic palette or on a flat, repetitive building still
fails.

## What vanilla 1.21.1 actually does

The texture numbers were measured from the 1.21.1 client jar with the same statistics used in
`ASSET_REVISION_LOG.md`, not taken from memory.

| Property | Vanilla | What it means for us |
|---|---|---|
| Block texture size | 16x16. Larger only for animation strips (16xN with `.mcmeta`). | Full blocks are 16x16. A 32x32 block has half-size pixels and looks wrong beside vanilla. |
| Colours per block | 4 to 11 (stone 4, cobblestone 6, bricks 9, iron ore 9, copper ore 14) | A material is a short ramp of 4 to 7 shades, plus a few hand-placed accents. |
| Brightness spread (std. dev. of luminance) | Rough materials about 10 to 30 (stone 11, gravel 17, calcite 19, bricks 22, mossy cobblestone 24, cobblestone 29). Smooth finishes are nearly flat (light gray concrete 1, white terracotta 2, sandstone top 6, quartz side 6). | A rough material below about 8 reads as flat paint; anything above 30 reads as noise unless it is meant to be a mix. Smooth finishes (plaster, concrete) stay near-flat. |
| Mean brightness of dark blocks | blackstone 38, nether bricks 29, deepslate bricks 71 | Dark materials are allowed, but they keep visible internal shading. |
| Stone | Short horizontal smears of 2 to 6 pixels, 4 shades, no diagonal runs | Diagonal runs line up across tiles and stripe a wall. |
| Bricks | 4 courses of 4 rows (3 brick + 1 mortar), joints offset every course; each brick lit on its top row, shaded on its bottom row and right column | Every brick gets the same bevel. Variety comes from per-brick tone, not random pixels. |
| Ores | The mineral sits on the exact host stone palette. Grains are 3 to 8 pixel irregular clumps with their own 3 to 5 shade ramp. The host rock is darkened next to some grains, not outlined all round. Deepslate ores are a separate drawing, not the stone overlay. | No glowing dots, no stickers, no reusing the stone layout on deepslate. |
| Metals | Panels with a lit top or left edge and a dark bottom or right edge. Few, flat highlights. | Rust and paint are coherent areas with a shape, not per-pixel speckle. |
| Lanterns | One body box whose side face shows the frame and the light (6x7), a 4x4 cap, a handle made of two crossed planes, explicit UVs into one 16x16 sheet. | Lighting fixtures are built the same way, so they sit at vanilla scale. |
| Item sprites | Fill most of the 16x16 canvas. The outline is the darkest shade of the material, not black. Light from the top left. Members of a family share a silhouette (iron, gold and copper ingots are one shape). | Small sprites with black outlines look like a different game. |
| Mobs | A handful to a couple of dozen cubes, whole-pixel sizes, one texel per model pixel, mostly 64x64 or 64x32 atlases. (From vanilla's entity model classes and textures, not measured by our tools.) | Dozens of fractional cubes smear texels and read as "imported model". |
| Alpha | 0 or 255 only, in blocks and items. | No soft edges, no anti-aliasing. |

## Rules for every asset

1. **Pick references first.** Before drawing, choose at least two vanilla assets with the same
   material or job (a brick next to `bricks` and `tuff_bricks`, an ore next to `iron_ore` and
   `copper_ore`, a lamp next to `lantern` and `soul_lantern`). Record them in
   `tools/art/reference_matrix.json`, which feeds `VANILLA_REFERENCE_MATRIX.md` and the review
   sheet.
2. **16x16, hand-placed pixels.** Draw the texture as a palette and a pixel grid
   (`tools/art/author_textures.py`, `author_items.py`, `author_lamp.py`). Never resize a larger
   image down, never trace a photo, never use generated noise as the finished texture. AI
   images may be used only as rough concept references.
3. **A short ramp.** 4 to 7 shades per material, moved in hue as well as brightness (vanilla
   shadows are slightly cooler or more saturated, not just darker). Accents (stains, rust,
   rivets) get their own 2 or 3 shades.
4. **One light direction.** Top left. Bevels: top lit, bottom and right shaded, gaps darkest
   right under an overhang (the `masonry()` helper does this the same way every time).
5. **Wear is a shape.** A damp streak runs down from a joint. Rust blooms below a rivet and
   along the lower edge. Soot is thickest at the top. Never scatter random dark pixels.
6. **Tile it.** View 3x3 tiles. Fix anything that lines up into stripes or a grid you did not
   intend, and any band that would stripe a tall wall (for example a tide line across the
   whole width). A single distinct feature in one 16x16 tile (a pit, a stain, a tie hole, a
   crack) becomes a regular grid of that feature on a wall: in the first in-game review this
   turned limestone into dotted cloth, concrete into pegboard and plaster into polka dots.
   Wear and damage are low-contrast texture spread across the tile, never one object.
7. **Keep identity and identifiers.** Revise; do not replace. Registry names, model names,
   texture paths, loot, recipes and save data stay the same unless a change is unavoidable,
   and then the change is logged.
8. **Darkness through context.** The Hole is dark because of fog, smog, unlit streets and
   material choices, not because textures are near-black.

## Process (every asset, every time)

1. **Vanilla comparison.** References chosen and recorded.
2. **Technical check.** `py tools/art/asset_lint.py --vanilla <client jar root>` must report
   0 errors. It checks blockstate and model references, missing textures, UV and element
   bounds, PNG format, animation strips and metadata, naming, transparency in full cubes, and
   it warns on high-resolution block and item textures, soft alpha, duplicates and unused
   textures.
3. **Artistic check.** `py tools/art/compare_sheet.py --vanilla <root> --out sheet.png
   block/<name>` shows before, after, the references and both 3x3 tilings at nearest-neighbour
   scale. Compare palette, contrast, pixel density, material read, silhouette and tiling.
4. **In-game check.** `./gradlew runQaClient -Pqa_mode=art` builds a gallery in the real
   client (every custom block as a 3x3 panel beside a 3x3 panel of its reference) and
   photographs it in daylight, close up, at gameplay distance, at night, by torch light and
   enclosed with no sky light, plus a lamp scene and the hotbar. Add the block to
   `ArtClientQa.GROUPS` when you add an asset.
5. **Revise** until it passes, then log the change in `ASSET_REVISION_LOG.md` and the result in
   `VISUAL_QA_REPORT.md`. Do not call an asset done because the PNG loads.

Shaders are not part of the baseline. Vanilla rendering is the reference. If a shader pack is
tested, say which one.

## Where things live

| What | Where |
|---|---|
| Block texture source (hand-drawn grids) | `tools/art/author_textures.py` |
| Item sprite source | `tools/art/author_items.py` |
| Cavern Lamp atlas, sprite and models | `tools/art/author_lamp.py` |
| References | `tools/art/reference_matrix.json`, `docs/art/VANILLA_REFERENCE_MATRIX.md` |
| Technical lint | `tools/art/asset_lint.py` |
| Review sheets | `tools/art/compare_sheet.py` |
| In-game gallery | `src/main/java/com/dorohedoro/hole/client/qa/ArtClientQa.java` |
| Audit, log, results | `docs/art/ASSET_QUALITY_AUDIT.md`, `ASSET_REVISION_LOG.md`, `VISUAL_QA_REPORT.md` |

The vanilla client jar is read from `~/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar`
and extracted to a scratch folder for reference. Vanilla textures are never copied into this
repository.
