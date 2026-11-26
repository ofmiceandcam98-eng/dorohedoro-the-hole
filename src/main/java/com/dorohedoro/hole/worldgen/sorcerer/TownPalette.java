package com.dorohedoro.hole.worldgen.sorcerer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Material sets for Sorcerers' World buildings. All vanilla 1.21.1 blocks, deliberately unlike the Hole's soot,
 * rust and patchwork: dark Gothic masonry, verdigris copper roofs, plaster, tuff civic stone, half-timber, the
 * mushroom architecture of En's sphere of influence, and the mud-brick of the poor edge of town.
 */
public record TownPalette(String name, BlockState wall, BlockState accent, BlockState trim, BlockState floor,
                          Block roofStairs, Block roofSlab, BlockState roofFull, BlockState window, Block door,
                          BlockState foundation, BlockState planks, Block planksStairs, Block planksSlab, Block fence,
                          Block wallSign, BlockState paving) {

    private static BlockState log(Block block) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    }

    public static final TownPalette GOTHIC = new TownPalette("gothic",
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            log(Blocks.DARK_OAK_LOG), Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES.defaultBlockState(),
            Blocks.GRAY_STAINED_GLASS_PANE.defaultBlockState(), Blocks.DARK_OAK_DOOR,
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_WALL_SIGN,
            Blocks.POLISHED_DEEPSLATE.defaultBlockState());

    public static final TownPalette VERDIGRIS = new TownPalette("verdigris",
            Blocks.WHITE_TERRACOTTA.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
            log(Blocks.STRIPPED_SPRUCE_LOG), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB,
            Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(),
            Blocks.SPRUCE_DOOR, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_WALL_SIGN,
            Blocks.STONE_BRICKS.defaultBlockState());

    public static final TownPalette TUFF = new TownPalette("tuff",
            Blocks.TUFF_BRICKS.defaultBlockState(), Blocks.POLISHED_TUFF.defaultBlockState(),
            log(Blocks.STRIPPED_DARK_OAK_LOG), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES.defaultBlockState(),
            Blocks.GLASS_PANE.defaultBlockState(), Blocks.SPRUCE_DOOR, Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE,
            Blocks.SPRUCE_WALL_SIGN, Blocks.POLISHED_TUFF.defaultBlockState());

    public static final TownPalette BRICK = new TownPalette("brick",
            Blocks.BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
            log(Blocks.SPRUCE_LOG), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
            Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(),
            Blocks.SPRUCE_DOOR, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_WALL_SIGN,
            Blocks.STONE_BRICKS.defaultBlockState());

    public static final TownPalette TIMBER = new TownPalette("timber",
            Blocks.WHITE_TERRACOTTA.defaultBlockState(), Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState(),
            log(Blocks.DARK_OAK_LOG), Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.GLASS_PANE.defaultBlockState(), Blocks.DARK_OAK_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.DARK_OAK_FENCE,
            Blocks.DARK_OAK_WALL_SIGN, Blocks.COBBLESTONE.defaultBlockState());

    public static final TownPalette MUSHROOM = new TownPalette("mushroom",
            Blocks.MUSHROOM_STEM.defaultBlockState(), Blocks.SMOOTH_QUARTZ.defaultBlockState(),
            log(Blocks.STRIPPED_BIRCH_LOG), Blocks.BIRCH_PLANKS.defaultBlockState(),
            Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_MUSHROOM_BLOCK.defaultBlockState(),
            Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), Blocks.BIRCH_DOOR,
            Blocks.STONE_BRICKS.defaultBlockState(), Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.BIRCH_STAIRS,
            Blocks.BIRCH_SLAB, Blocks.BIRCH_FENCE, Blocks.BIRCH_WALL_SIGN, Blocks.SMOOTH_STONE.defaultBlockState());

    public static final TownPalette PATCHWORK = new TownPalette("patchwork",
            Blocks.MUD_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(),
            log(Blocks.OAK_LOG), Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.GLASS_PANE.defaultBlockState(), Blocks.OAK_DOOR, Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_FENCE,
            Blocks.OAK_WALL_SIGN, Blocks.PACKED_MUD.defaultBlockState());

    public static final TownPalette WORKS = new TownPalette("works",
            Blocks.POLISHED_ANDESITE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.POLISHED_BASALT.defaultBlockState(), Blocks.SMOOTH_STONE.defaultBlockState(),
            Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
            Blocks.WAXED_WEATHERED_CUT_COPPER.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(),
            Blocks.SPRUCE_DOOR, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_WALL_SIGN,
            Blocks.ANDESITE.defaultBlockState());

    static TownPalette pick(TownRandom rng, TownPalette... options) {
        return options[rng.nextInt(options.length)];
    }
}
