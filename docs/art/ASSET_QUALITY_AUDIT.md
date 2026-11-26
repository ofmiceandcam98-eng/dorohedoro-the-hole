# Asset quality audit (Mojang standard)

Audit of every custom visual asset against vanilla 1.21.1, made on 2026-11-26. Priorities
follow visibility: terrain and city materials first, then lighting, ores, natural blocks, mobs,
survival items, equipment, decoration, rare assets.

**Status key.** *Revised* = redrawn this pass. *Pass* = meets the standard as it is.
*Refine* = acceptable family, specific fixes listed. *Redo* = does not meet the standard.
**In-game key.** *Gallery* = photographed in the real client by `ArtClientQa` in six lighting
and distance conditions (see `VISUAL_QA_REPORT.md`). *Hotbar* = seen at inventory scale in the real client. *Not tested* =
no real-client view yet.

## Summary

| Area | Assets | Revised | Pass | Refine | Redo |
|---|---|---|---|---|---|
| Terrain, building, ores (16x16 cubes and overlays) | 22 | 22 | 0 | 0 | 0 |
| Lighting (Cavern Lamp) | 1 atlas, 3 models, 1 sprite | all | | | |
| Portal and door blocks (32x32) | 9 | 0 | 0 | 0 | 9 |
| Items | 60 | 12 | 20 | 28 | 0 |
| Mobs | 8 models, 10 textures | 0 | 0 | 0 | 8 |
| GUI, particles, magic meshes | 136 | not audited this pass (not world assets; the vanilla comparison does not apply directly) | | | |

The single largest gap is the mobs (see their section). It is a remodel per creature and is
the recommended next pass.

## 1. Terrain

| Asset | Path | Size | References | Problem found | Change | Status | In-game |
|---|---|---|---|---|---|---|---|
| Limestone | `textures/block/limestone.png` | 16x16 | calcite, stone | Flat fill (luminance spread 6, vanilla stone 11) with a few random dark dots | Stone-style horizontal smears in a cream ramp, pits drawn as a dark pixel over a lit lip | Revised | Gallery |
| Hell Rock | `textures/block/hell_rock.png` | 16x16 | blackstone, netherrack | Nearly one flat colour (spread 5) | Red-violet stone smears, two hairline cracks; first redraw striped diagonally when tiled and was redone | Revised | Gallery |
| Ash Soil | `textures/block/ash_soil.png` | 16x16 | coarse_dirt, gravel | Flat fill (spread 6) with sparse specks | Granular clumps, each lighter grain on a darker one, pale ash flecks, charred bits | Revised | Gallery |
| Industrial Debris | `textures/block/industrial_debris.png` | 16x16 | gravel, cobblestone | Two pasted blobs on a flat ground; no chunk shading | Gravel-style chunks of concrete, rusted sheet, brick, steel and plank, each lit on top with a dark gap below | Revised | Gallery |

## 2. Building materials

| Asset | Path | Size | References | Problem found | Change | Status | In-game |
|---|---|---|---|---|---|---|---|
| Sewer Brickwork | `block/sewer_brickwork.png` | 16x16 | stone_bricks, mud_bricks | Flat-filled bricks, no bevel, random light pixels | Four courses with vanilla brick bevels, per-brick tone, one damp streak from a joint | Revised | Gallery |
| Soot-Blackened Brick | `block/soot_blackened_brick.png` | 16x16 | bricks, deepslate_bricks | Near-black flat bricks (mean luminance 62) | Soot heavy on top courses, red-brown clay showing lower down, lighter mortar | Revised | Gallery |
| Seepage Brick | `block/seepage_brick.png` | 16x16 | mossy_stone_bricks | Flat teal bricks, 17 colours of noise | Teal bricks with bevels, water tracks under joints, dried mineral crust | Revised | Gallery |
| Patchwork Masonry | `block/patchwork_masonry.png` | 16x16 | bricks, stone_bricks | Mixed blocks without bevels | Red brick bond repaired brick by brick with grey stone, limestone and concrete plugs, one light direction | Revised | Gallery |
| Smoke-Stained Concrete | `block/smoke_stained_concrete.png` | 16x16 | light_gray_concrete, smooth_stone | Random green and black specks | Near-flat concrete, formwork seam, soot smudges spread evenly | Revised | Gallery |
| Water-Damaged Plaster | `block/water_damaged_plaster.png` | 16x16 | white_terracotta, calcite | Grey-green vertical bars read as pipes, not stains | Trowelled cream plaster, one faint dried blotch. A full-width tide line was tried and dropped: it striped every block of a tall wall | Revised (faint blotch still repeats at close range; see report) | Gallery |
| Rusted Plating | `block/rusted_plating.png` | 16x16 | iron_block, exposed_cut_copper | Flat plates, rivets as single dots | Staggered hull plates with bevels, rivets with shadows, rust running from rivets and along lower edges | Revised | Gallery |
| Rusted Industrial Sheet | `block/rusted_industrial_sheet.png` | 16x16 | exposed_copper | Flat bars with orange dots | Corrugated sheet (4-pixel ridge), grey-green paint on top flaking into rust below. First redraw was noisy and was rebuilt from a ridge phase plus a hand-drawn cover mask | Revised | Gallery |
| Galvanized Plating | `block/galvanized_plating.png` | 16x16 | iron_block, polished_andesite | Three colours, no bevel | Four bevelled panels, spangle as a few angular facets, one white-rust spot | Revised | Gallery |
| Chitin Plating | `block/chitin_plating.png` | 16x16 | moss_block, oxidized_cut_copper | Flat green with a pale cross | Overlapping scutes like shingles, glossy top left, dark lip on the row below | Revised | Gallery |

## 3. Lighting

| Asset | Path | References | Problem found | Change | Status | In-game |
|---|---|---|---|---|---|---|
| Cavern Lamp (block) | `models/block/cavern_lamp_{standing,hanging,wall}.json`, `textures/block/cavern_lamp.png` | lantern, soul_lantern | Standing model floated 1 px above the floor (collision box starts at y0, model at y1). Hanging chain sampled a mostly transparent part of the vanilla chain texture. Separate posts and plates with default UVs, so faces sampled whatever part of the texture they happened to cover. | Rebuilt like the vanilla lantern: one body box (6x7 side: brass frame, glass, pale moth-scale glow), 4x4 cap, crossed-plane handle, explicit UVs into one hand-drawn atlas. Wall version has a wall plate and a bracket arm over the cap, visible from the front; its four collision boxes were extended 1 px lower to cover it. Standing and hanging stay inside their existing collision shapes. Light level, block states and item unchanged. Old `cavern_lamp_frame.png` and `cavern_lamp_glow.png` removed (nothing else used them) | Revised | Gallery (lamp scene) |
| Cavern Lamp (item) | `models/item/cavern_lamp.json`, `textures/item/cavern_lamp.png` | item/lantern | Inventory showed the 3D block model, unlike every vanilla lantern | Flat 16x16 sprite in the vanilla lantern item style | Revised | Hotbar |

## 4. Ores and resources

All eight ore blocks are overlays on vanilla `stone` or `deepslate` (`models/block/ore_overlay.json`).

| Asset | Path | References | Problem found | Change | Status | In-game |
|---|---|---|---|---|---|---|
| Zinc ore overlay | `block/ore/zinc.png`, new `block/ore/zinc_deepslate.png` | iron_ore, copper_ore | Blobs without host darkening; deepslate reused the stone overlay | Irregular 3 to 8 px grains, blue-grey metallic ramp, host-shade rim on some sides; separate deepslate drawing with a deepslate rim | Revised | Gallery |
| Sulfur ore overlay | `block/ore/sulfur.png`, `sulfur_deepslate.png` | gold_ore | Chunky yellow blobs that read as gold | Crusts of stubby lemon crystals | Revised | Gallery |
| Rock salt ore overlay | `block/ore/rock_salt.png`, `rock_salt_deepslate.png` | diamond_ore, quartz ore | Flat pink squares | Cubic crystals, lit top-left faces, overlapping | Revised | Gallery |
| Oil shale ore overlay | `block/ore/oil_shale.png`, `oil_shale_deepslate.png` | coal_ore | Thin black dashes | Laminated dark lenses with a brown top edge and an oily sheen pixel | Revised | Gallery |
| Ore models | `datagen/ModModels.ore()`, `src/generated/.../deepslate_*_ore.json` | | | Deepslate ores now point at their own overlay | Revised | Gallery |

## 5. Portal and door blocks (rare, but placed in the world)

| Asset | Size | Problem found | Required change | Status | In-game |
|---|---|---|---|---|---|
| `reinforced_door_frame`, `generated_door_frame` | 32x32 | Half-size pixels beside vanilla; framed-picture look (a border inside every block, so a wall of them reads as tiles) | Redraw at 16x16 as a chiseled frame stone (references chiseled_stone_bricks, reinforced_deepslate) | Redo | Gallery (shown as the density example) |
| `etched_gate_stone`, `generated_gate_stone` | 32x32 | Same, plus etched line is a single thin stroke | Redraw at 16x16 like chiseled_tuff: carved motif that reads at distance | Redo | Gallery |
| `artificial_door_core`, `generated_artificial_core`, `infernal_door_core`, `generated_infernal_core` | 32x32 | Same; a gem picture in a frame | Redraw at 16x16 with a carved socket and the gem as part of the stone | Redo | Not tested |
| `portal_surface` | 32x32 | Half-size pixels; semi-transparent pixels | Redraw as an animated 16x16 strip like nether_portal / end_gateway | Redo | Not tested |
| `condensed_smoke_barrier` | 32x32 | Half-size pixels; semi-transparent pixels (translucent smoke wall is intentional) | 16x16 strip with stepped alpha like vanilla ice / tinted glass | Redo | Not tested |

## 6. Mobs (largest gap)

Every mob uses a 512x256 atlas and dozens of cubes with fractional sizes. Vanilla mobs use
a handful to a couple of dozen whole-pixel cubes, mostly on 64x64 or 64x32 atlases. Fractional cubes stretch texels unevenly
across faces, so pixels change size from one face to the next. This, more than any colour
choice, is what makes the creatures read as imported models.

| Mob | Model | Cubes | Fractional sizes | Atlas | Approved identity to keep | Status |
|---|---|---|---|---|---|---|
| Sewer Leech | `client/entity/SewerLeechModel.java` | 115 | 115 | 512x256 | Substantial body, a few prominent teeth, not rows of needles | Redo |
| Glow Moth | `GlowMothModel.java` (+ `glow_moth_glow.png`) | 285 | 285 | 512x256 | Wing depth, separate fore and hind wing motion, recognisable glow | Redo |
| Rust Eater Beetle | `RustEaterBeetleModel.java` | 54 | 51 | 512x256 | Beetle anatomy, metallic insect palette | Redo |
| Sewer Roach | `SewerRoachModel.java` | 138 | 133 | 512x256 | Cockroach | Redo |
| Scrap Rat | `ScrapRatModel.java` | 124 | 124 | 512x256 | Rat | Redo |
| Sludge Crawler | `SludgeCrawlerModel.java` | 42 | 37 | 512x256 | Slime crawler | Redo |
| Cavern Burrower | `CavernBurrowerModel.java` | 62 | 61 | 512x256 | Failed sorcerer experiment (restored original) | Redo |
| Spore Crawler | `SporeCrawlerModel.java` | 53 | 52 | 512x256 | Fungal crawler | Redo |
| Reptile morph | `entity/reptile_morph.png` | | | 128x128 | Reptile form | Refine (check against player skin density) |

Required for each: remodel in a voxel workflow (Blockbench) at whole-pixel sizes with a 64x64
atlas, keep the approved identity above, keep the entity type, renderer, loot and spawn
rules, and redo the animations on the new pivots (walk, idle, turn, attack, hurt; separate
fore and hind wings for the moth). One creature per session, each checked in the real client
beside comparable vanilla mobs.

## 7. Items

Sizes: all 16x16 except the eight 64x64 limb atlases that belong to 3D item models
(`devil_sigil` was 32x32 and is now 16x16).

| Asset(s) | Problem found | Required change | Status | In-game |
|---|---|---|---|---|
| `raw_zinc`, `raw_sulfur`, `rock_salt` | Tiny scattered bits; weak silhouettes, low canvas fill next to raw_iron | Redrawn as a fused metal lump, a crystal crust and a cluster of cubes, outline in the material's darkest shade | Revised | Hotbar |
| `zinc_ingot`, `brass_ingot` | Small bars (8x4) next to vanilla's 14x8 ingots | Redrawn as one shared cast-bar silhouette, 14x8, family colours | Revised | Hotbar (zinc) |
| `oil_shale_chunk` | Scattered specks, unreadable at slot size | Redrawn as one laminated chunk, coal-sized, with an oily sheen pixel | Revised | Hotbar |
| `devil_sigil` | 32x32, half-size pixels | Redrawn at 16x16: bronze ring, dark field, diamond motif | Revised | Hotbar |
| `smoke_conduit`, `preserved_smoke_conduit` | Identical two-colour pixels, no shading; the two items looked the same | Redrawn as a shaded bent duct; the preserved one is cured darker with brass collars | Revised | Hotbar |
| `burrower_claw`, `roach_antenna` | Thin one-pixel diagonals; low fill | Redrawn: a thick ivory talon on a dark base; one segmented antenna with its socket | Revised | Hotbar |
| Smoke flasks (8) and `empty_smoke_flask` | Near-black bottle outline; vanilla potions outline glass in pale blue-grey | Lighter glass outline, keep the colour-coded contents and stoppers | Refine | Not tested |
| `lime_powder` | Flat thin pile | Fuller mound, compare gunpowder and sugar | Refine | Not tested |
| `bitumen`, `coagulated_tar` | Readable; outlines near black | Outline in a dark brown of the material | Refine | Not tested |
| `cooked_rat_meat`, `raw_rat_meat`, `salted_rat_ration` | Readable; slightly small | Fill closer to vanilla food size | Refine | Not tested |
| `leech_sucker`, `luminous_scale`, `metallic_carapace`, `reinforced_chitin`, `dense_hide` | Readable; outlines near black | Material-coloured outlines | Refine | Not tested |
| `antidote_poultice`, `corrosive_gland`, `door_activation_tool`, `door_linking_key`, `fungal_membrane`, `glow_tonic`, `leech_mucus`, `medical_salve`, `moth_silk`, `rat_fur`, `ritual_tool`, `scrap_metal`, `sewer_echo_probe`, `sewer_sealant`, `sludge_gland`, `smoke_condenser`, `smoke_residue`, `smoke_soaked_filter`, `spore_sac`, `surgical_reattachment_kit` | Silhouettes read, shading follows the light, palette coherent | None now; outline colour review with the flask family | Pass | Not tested |
| Severed and cooked limbs (8, 64x64, 3D models) | Large atlases for small models; cooked left and right are identical pixels (fine if the models mirror) | Check texel density on the held and dropped model in the client | Refine | Not tested |
