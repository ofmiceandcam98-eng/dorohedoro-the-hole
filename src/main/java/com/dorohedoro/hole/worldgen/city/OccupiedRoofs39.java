package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * Small service or laundry areas make otherwise plain roofs look inhabited.
 * Only plain roofs on larger buildings qualify: gardens, tanks and antenna
 * structures retain their original purpose and stay unchanged. The north
 * roof stair/landing remains clear; props are on the far southern perimeter.
 * No light sources, loot containers or extra mobs are introduced.
 */
final class OccupiedRoofs39 {
    private OccupiedRoofs39() {}

    private static void place(Canvas c, int x, int y, int z, Block block) {
        if (!c.contains(x, z) || !c.get(x, y, z).isAir()) return;
        if (c.get(x, y - 1, z).isAir()) return;
        c.set(x, y, z, block);
    }

    static void decorate(Canvas c, Rect roof, int floors, long building, Tower.Roof type) {
        if (type != Tower.Roof.PLAIN || floors < 2 || roof.width() < 15 || roof.depth() < 13) return;
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        int y = top + 1;
        int x = roof.x1() - 4;
        int z = roof.z1() - 4;
        long h = CityHash.of(building ^ 0x39B11D14L, roof.centerX(), roof.centerZ());
        int kind = CityHash.range(h, 3);
        if (kind == 0) {
            // A local maintenance alcove: a repair bench and a patched vent.
            place(c, x, y, z, Blocks.STONECUTTER);
            place(c, x + 2, y, z, Blocks.WAXED_WEATHERED_CUT_COPPER);
            place(c, x + 2, y + 1, z, Blocks.IRON_TRAPDOOR);
        } else if (kind == 1) {
            // Drying area without actual new cloth entities or a large mast.
            place(c, x, y, z, Blocks.SPRUCE_FENCE);
            place(c, x + 3, y, z, Blocks.SPRUCE_FENCE);
            place(c, x, y + 1, z, Blocks.SPRUCE_FENCE);
            place(c, x + 3, y + 1, z, Blocks.SPRUCE_FENCE);
            // Horizontal chain visually reads as a short laundry line.
            BlockState chain = Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.X);
            if (c.contains(x + 1, z) && c.get(x + 1, y + 1, z).isAir())
                c.set(x + 1, y + 1, z, chain);
            if (c.contains(x + 2, z) && c.get(x + 2, y + 1, z).isAir())
                c.set(x + 2, y + 1, z, chain);
        } else {
            // Rainwater / rooftop maintenance corner, no extra storage rows.
            place(c, x, y, z, Blocks.CAULDRON);
            place(c, x + 2, y, z, Blocks.IRON_TRAPDOOR);
        }
    }
}
