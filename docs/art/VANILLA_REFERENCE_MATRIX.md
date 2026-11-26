# Vanilla reference matrix

Each custom asset is compared against vanilla 1.21.1 assets chosen for the same **material or
job**, not for a similar colour. Blocks are machine-readable in `tools/art/reference_matrix.json`
(used by `compare_sheet.py`); the in-game gallery pairs are in `ArtClientQa.GROUPS`.

## Terrain

| Asset | Material and job | References | In-game pair |
|---|---|---|---|
| `block/limestone` | Pale sedimentary stone, mined | calcite, stone, dripstone_block | calcite |
| `block/hell_rock` | Hell's base rock | blackstone, netherrack, basalt_side | blackstone |
| `block/ash_soil` | Ash over soil, surface layer | coarse_dirt, soul_soil, gravel | coarse_dirt |
| `block/industrial_debris` | Rubble pile | gravel, cobblestone, coarse_dirt | gravel |

## Building materials

| Asset | Material and job | References | In-game pair |
|---|---|---|---|
| `block/sewer_brickwork` | Sewer wall brick | stone_bricks, mud_bricks, tuff_bricks | stone_bricks |
| `block/soot_blackened_brick` | Fire-darkened clay brick | bricks, deepslate_bricks, nether_bricks | bricks |
| `block/seepage_brick` | Wet sewer brick | mossy_stone_bricks, prismarine_bricks, mud_bricks | mossy_stone_bricks |
| `block/patchwork_masonry` | Repaired mixed wall | bricks, stone_bricks, mossy_cobblestone | bricks |
| `block/smoke_stained_concrete` | Cast concrete | gray_concrete, light_gray_concrete, smooth_stone | light_gray_concrete |
| `block/water_damaged_plaster` | Interior plaster | calcite, white_terracotta, sandstone_top | white_terracotta |
| `block/rusted_plating` | Riveted steel plate | iron_block, exposed_cut_copper, weathered_copper | iron_block |
| `block/rusted_industrial_sheet` | Corrugated sheet | exposed_copper, iron_bars, weathered_cut_copper | exposed_copper |
| `block/galvanized_plating` | Zinc-coated plate | iron_block, heavy_core, polished_andesite | iron_block |
| `block/chitin_plating` | Insect-shell masonry | moss_block, oxidized_cut_copper, green_shulker_box | moss_block |
| `block/reinforced_door_frame`, `etched_gate_stone` and the door cores | Portal frame stones | chiseled_stone_bricks, chiseled_tuff, reinforced_deepslate | chiseled_stone_bricks, chiseled_tuff |

## Ores (overlays on vanilla stone and deepslate)

| Asset | Host | References |
|---|---|---|
| `block/ore/zinc` | stone | iron_ore, copper_ore |
| `block/ore/zinc_deepslate` | deepslate | deepslate_iron_ore, deepslate_copper_ore |
| `block/ore/sulfur` | stone | gold_ore, nether_quartz_ore |
| `block/ore/sulfur_deepslate` | deepslate | deepslate_gold_ore, deepslate_redstone_ore |
| `block/ore/rock_salt` | stone | diamond_ore, nether_quartz_ore |
| `block/ore/rock_salt_deepslate` | deepslate | deepslate_diamond_ore, deepslate_lapis_ore |
| `block/ore/oil_shale` | stone | coal_ore, iron_ore |
| `block/ore/oil_shale_deepslate` | deepslate | deepslate_coal_ore, deepslate_iron_ore |

## Lighting

| Asset | References |
|---|---|
| `block/cavern_lamp` (atlas) and the standing, hanging and wall models | lantern, soul_lantern (block and template models), chain, wall torch |
| `item/cavern_lamp` | item/lantern, item/soul_lantern |

## Items

| Asset | References |
|---|---|
| `item/raw_zinc` | raw_iron, raw_copper |
| `item/raw_sulfur` | raw_gold, amethyst_shard |
| `item/rock_salt` | quartz, amethyst_shard |
| `item/zinc_ingot`, `item/brass_ingot` | iron_ingot, gold_ingot, copper_ingot |
| `item/bitumen`, `item/coagulated_tar`, `item/oil_shale_chunk` | coal, charcoal, slime_ball |
| Food (`raw_rat_meat`, `cooked_rat_meat`, `salted_rat_ration`) | rabbit, cooked_rabbit, bread |
| Hides and drops (`dense_hide`, `rat_fur`, `metallic_carapace`, `reinforced_chitin`, `luminous_scale`, `moth_silk`) | leather, rabbit_hide, turtle_scute, armadillo_scute, phantom_membrane, string |
| Glands and organics (`sludge_gland`, `corrosive_gland`, `spore_sac`, `leech_mucus`, `fungal_membrane`) | spider_eye, fermented_spider_eye, slime_ball, ink_sac |
| Smoke flasks (eight) and `empty_smoke_flask` | potion, glass_bottle, honey_bottle, experience_bottle |
| Medical (`medical_salve`, `antidote_poultice`, `surgical_reattachment_kit`, `glow_tonic`) | honey_bottle, suspicious_stew, paper, bundle |
| Tools (`door_linking_key`, `door_activation_tool`, `ritual_tool`, `sewer_echo_probe`) | trial_key, compass, brush, recovery_compass |
| Severed and cooked limbs (3D item models) | cooked_mutton, rotten_flesh, bone; model scale vs player arm |

## Mobs

| Mob | Approved identity (keep) | References |
|---|---|---|
| Sewer Leech | Substantial body, a few prominent teeth, not rows of needles | silverfish, endermite, bogged (mouth detail), warden (soft-body texture) |
| Glow Moth | Wing depth, separate fore and hind wing motion, recognisable glow | bat, allay, vex (wing planes), glow squid (emissive layer) |
| Rust Eater Beetle | Beetle anatomy, metallic insect palette | spider, armadillo, breeze (segmented parts) |
| Sewer Roach | Cockroach | silverfish, spider |
| Scrap Rat | Rat | rabbit, fox, ocelot |
| Sludge Crawler | Slime-like crawler | slime, magma cube, silverfish |
| Cavern Burrower | Sorcerer's failed experiment | armadillo, sniffer, ravager |
| Spore Crawler | Fungal crawler | creaking (1.21.2+, reference for bark/fungus texture language only), mooshroom, spider |
| Reptile morph (player form) | Reptile | turtle, axolotl, player skin layout |
