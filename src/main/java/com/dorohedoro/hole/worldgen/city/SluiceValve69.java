package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * Pass 69: an independently operable vanilla-redstone sluice at a Pass68 pump hall.
 *
 * <p>At floor level Y=49 the existing sump holds water sources at distance 11..13,
 * offset -2. This adds a one-block-wide runoff at distance 14..15, offset -2.
 * A STICKY piston sits at offset -4 facing -3 -> -2. With the lever OFF, its
 * iron block rests in -3 and source water can refill the runoff. Lever ON
 * strongly powers the solid floor beneath it, powering the adjacent piston,
 * pushing that same iron block into the runoff cell -2 to close the channel.
 * No custom tick code, magic technique, modded blocks, or artificial entity AI.
 *
 * <p>This is separate from the Pass68 operator-booth lever; neither gate blocks
 * the mandatory dry central footbridge at offset 0 or the municipal lanes.
 * A Java fixture validates geometry and block states; actual piston/fluid update
 * behavior must still be tested in NeoForge Minecraft 1.21.1.
 */
final class SluiceValve69 {
    static final int FIRST_VERSION = 51;

    private SluiceValve69() {}

    static void build(Canvas c, boolean xAxis, int trunk, int along, int side) {
        // The run-off channel is lower than the safe walking floor (Y=50 feet).
        // Its two empty cells are intentionally NOT sources: they refill from
        // the upstream source at distance 13 only while the sluice is open.
        for (int distance = 14; distance <= 15; distance++) {
            int x = x(xAxis, trunk, along, side, distance, -2);
            int z = z(xAxis, trunk, along, side, distance, -2);
            c.set(x, 48, z, Blocks.POLISHED_TUFF);
            c.set(x, 49, z, Blocks.AIR);
            c.fill(x, 50, z, x, 53, z, Blocks.AIR);
        }

        int pistonX = x(xAxis, trunk, along, side, 14, -4);
        int pistonZ = z(xAxis, trunk, along, side, 14, -4);
        Direction pushTowardChannel = xAxis ? Direction.SOUTH : Direction.EAST;
        c.set(pistonX, 49, pistonZ, Blocks.STICKY_PISTON.defaultBlockState()
                .setValue(PistonBaseBlock.FACING, pushTowardChannel));

        // Retracted block sits one step from the outlet; piston extension
        // moves it into the channel cell, and retraction uncovers the water.
        int movingX = x(xAxis, trunk, along, side, 14, -3);
        int movingZ = z(xAxis, trunk, along, side, 14, -3);
        c.set(movingX, 49, movingZ, Blocks.IRON_BLOCK);

        // A floor-mounted lever powers its attached full floor block at Y=49,
        // immediately adjacent to the Y=49 piston. This is the standard
        // vanilla strong-power route, not a disconnected decorative switch.
        int powerX = x(xAxis, trunk, along, side, 14, -5);
        int powerZ = z(xAxis, trunk, along, side, 14, -5);
        c.set(powerX, 49, powerZ, Blocks.STONE_BRICKS);
        c.set(powerX, 50, powerZ, Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.FLOOR));
    }

    private static int x(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? trunk + side * distance : along + offset;
    }

    private static int z(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? along + offset : trunk + side * distance;
    }
}
