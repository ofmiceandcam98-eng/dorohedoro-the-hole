package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Blocks;

/** Draws a lot's contents. */
final class LotPainter {
    private LotPainter() {}

    static void paint(Canvas c, CityPlan plan, Lot lot) {
        Rect r = lot.rect();
        c.fill(r, CityPlan.STREET_Y, CityPlan.STREET_Y, Blocks.PACKED_MUD);
        if (lot.type().enclosed) {
            c.walls(r, CityPlan.GROUND_Y, CityPlan.GROUND_Y + 5, Mix.of(Blocks.MUD_BRICKS, 1));
        }
    }
}
