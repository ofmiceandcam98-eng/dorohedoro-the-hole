package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A building's materials. Blocks here end up above street level, so none may be raw stone, tuff, deepslate, andesite,
 * granite or diorite: see the note on {@link CityPainter}.
 */
record Style(Mix wall, Mix trim, Mix floor, Block stairs, Block door, Block glass, Block bed, Block sign, Block fence) {
    static final Style BRICK = new Style(
            Mix.of(Blocks.MUD_BRICKS, 5, Blocks.BRICKS, 2, Blocks.PACKED_MUD, 2, Blocks.BROWN_TERRACOTTA, 2),
            Mix.of(Blocks.STRIPPED_DARK_OAK_LOG, 2, Blocks.DARK_OAK_PLANKS, 1),
            Mix.of(Blocks.SPRUCE_PLANKS, 3, Blocks.DARK_OAK_PLANKS, 1),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style CONCRETE = new Style(
            Mix.of(Blocks.GRAY_CONCRETE, 4, Blocks.LIGHT_GRAY_CONCRETE, 2, Blocks.CYAN_TERRACOTTA, 2, Blocks.GRAY_TERRACOTTA, 2),
            Mix.of(Blocks.POLISHED_TUFF, 2, Blocks.POLISHED_ANDESITE, 1),
            Mix.of(Blocks.SMOOTH_STONE, 2, Blocks.SPRUCE_PLANKS, 3),
            Blocks.STONE_BRICK_STAIRS, Blocks.DARK_OAK_DOOR, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_BED, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    static final Style SOOT = new Style(
            Mix.of(Blocks.DEEPSLATE_BRICKS, 5, Blocks.CRACKED_DEEPSLATE_BRICKS, 2, Blocks.POLISHED_BLACKSTONE_BRICKS, 2, Blocks.BLACK_TERRACOTTA, 1),
            Mix.of(Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.WAXED_WEATHERED_CUT_COPPER, 2),
            Mix.of(Blocks.DARK_OAK_PLANKS, 3, Blocks.POLISHED_BLACKSTONE, 1),
            Blocks.DARK_OAK_STAIRS, Blocks.IRON_DOOR, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.BLACK_BED, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    static final Style RUST = new Style(
            Mix.of(ModBlocks.RUSTED_PLATING.get(), 5, Blocks.WAXED_WEATHERED_CUT_COPPER, 3, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.BROWN_TERRACOTTA, 2),
            Mix.of(Blocks.WAXED_OXIDIZED_COPPER, 2, Blocks.POLISHED_BLACKSTONE, 1),
            Mix.of(Blocks.POLISHED_BLACKSTONE, 2, Blocks.SPRUCE_PLANKS, 3),
            Blocks.DARK_OAK_STAIRS, Blocks.SPRUCE_DOOR, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.IRON_BARS);

    static final Style CLEAN = new Style(
            Mix.of(Blocks.WHITE_CONCRETE, 5, Blocks.LIGHT_GRAY_CONCRETE, 2, Blocks.WHITE_TERRACOTTA, 2),
            Mix.of(Blocks.CYAN_CONCRETE, 2, Blocks.LIGHT_BLUE_TERRACOTTA, 1),
            Mix.of(Blocks.SMOOTH_QUARTZ, 3, Blocks.LIGHT_GRAY_CONCRETE, 2),
            Blocks.QUARTZ_STAIRS, Blocks.BIRCH_DOOR, Blocks.GLASS_PANE, Blocks.WHITE_BED, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_FENCE);

    static final Style HUNGRY_BUG = new Style(
            Mix.of(Blocks.ORANGE_TERRACOTTA, 5, Blocks.RED_TERRACOTTA, 2, Blocks.TERRACOTTA, 2),
            Mix.of(Blocks.GREEN_TERRACOTTA, 3, Blocks.LIME_TERRACOTTA, 1),
            Mix.of(Blocks.CHERRY_PLANKS, 1, Blocks.SPRUCE_PLANKS, 2, Blocks.MANGROVE_PLANKS, 1),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_DOOR, Blocks.GLASS_PANE, Blocks.RED_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style MANOR = new Style(
            Mix.of(Blocks.POLISHED_BLACKSTONE_BRICKS, 4, Blocks.DEEPSLATE_TILES, 3, Blocks.NETHER_BRICKS, 2),
            Mix.of(Blocks.CRIMSON_PLANKS, 2, Blocks.DARK_PRISMARINE, 1),
            Mix.of(Blocks.DARK_OAK_PLANKS, 2, Blocks.CRIMSON_PLANKS, 1),
            Blocks.DARK_OAK_STAIRS, Blocks.CRIMSON_DOOR, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_BED, Blocks.CRIMSON_WALL_SIGN, Blocks.DARK_OAK_FENCE);

    // Extra muted, original Hole palettes. All use crafted/decorative states above road level
    // so vanilla ore replacement cannot generate stone ores in building facades.
    static final Style PATCHWORK = new Style(
            Mix.of(Blocks.TERRACOTTA, 3, Blocks.BROWN_TERRACOTTA, 2, Blocks.MUD_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 1),
            Mix.of(Blocks.DARK_OAK_PLANKS, 3, Blocks.STRIPPED_SPRUCE_LOG, 1),
            Mix.of(Blocks.SPRUCE_PLANKS, 4, Blocks.PACKED_MUD, 1),
            Blocks.SPRUCE_STAIRS, Blocks.DARK_OAK_DOOR, Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.BROWN_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style PLASTER = new Style(
            Mix.of(Blocks.WHITE_TERRACOTTA, 2, Blocks.LIGHT_GRAY_TERRACOTTA, 5, Blocks.TERRACOTTA, 2, Blocks.GRAY_TERRACOTTA, 1),
            Mix.of(Blocks.POLISHED_ANDESITE, 2, Blocks.STONE_BRICKS, 1),
            Mix.of(Blocks.SPRUCE_PLANKS, 4, Blocks.SMOOTH_STONE, 1),
            Blocks.STONE_BRICK_STAIRS, Blocks.SPRUCE_DOOR, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.GRAY_BED, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_FENCE);

    static final Style SHEET_METAL = new Style(
            Mix.of(Blocks.WAXED_EXPOSED_CUT_COPPER, 2, Blocks.GRAY_TERRACOTTA, 3,
                    Blocks.POLISHED_BLACKSTONE_BRICKS, 2, ModBlocks.RUSTED_PLATING.get(), 3),
            Mix.of(Blocks.WAXED_WEATHERED_CUT_COPPER, 2, Blocks.IRON_BLOCK, 1),
            Mix.of(Blocks.POLISHED_BLACKSTONE, 2, Blocks.SPRUCE_PLANKS, 3),
            Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_DOOR, Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.GRAY_BED, Blocks.DARK_OAK_WALL_SIGN, Blocks.IRON_BARS);

    static Style forDistrict(District district, long hash, boolean detailed) {
        if (!detailed) return forDistrict(district, hash);
        int roll = CityHash.range(hash >>> 13, 7);
        return switch (district) {
            case RESIDENTIAL -> switch (roll) {
                case 0, 1 -> PATCHWORK;
                case 2 -> PLASTER;
                case 3, 4 -> BRICK;
                default -> CONCRETE;
            };
            case MARKET -> switch (roll) {
                case 0 -> SHEET_METAL;
                case 1, 2 -> PLASTER;
                case 3 -> PATCHWORK;
                case 4 -> CONCRETE;
                default -> BRICK;
            };
            case INDUSTRIAL -> switch (roll) {
                case 0, 1, 2 -> SHEET_METAL;
                case 3, 4 -> SOOT;
                case 5 -> RUST;
                default -> PATCHWORK;
            };
            case WATERFRONT -> roll < 3 ? SHEET_METAL : roll == 3 ? PLASTER : RUST;
            case DERELICT -> roll < 3 ? PATCHWORK : roll < 5 ? SOOT : PLASTER;
            case GRAVEYARD -> roll < 3 ? SOOT : roll < 5 ? PLASTER : PATCHWORK;
            case OUTSKIRTS -> roll < 3 ? PATCHWORK : roll < 5 ? PLASTER : RUST;
        };
    }

    static Style forDistrict(District district, long hash) {
        return switch (district) {
            case RESIDENTIAL -> CityHash.range(hash, 3) == 0 ? CONCRETE : BRICK;
            case MARKET -> CityHash.range(hash, 3) == 0 ? BRICK : CONCRETE;
            case INDUSTRIAL -> CityHash.range(hash, 2) == 0 ? RUST : SOOT;
            case WATERFRONT -> CityHash.range(hash, 3) == 0 ? RUST : CONCRETE;
            case DERELICT -> CityHash.range(hash, 2) == 0 ? SOOT : BRICK;
            case GRAVEYARD -> SOOT;
            case OUTSKIRTS -> CityHash.range(hash, 2) == 0 ? RUST : BRICK;
        };
    }
}
