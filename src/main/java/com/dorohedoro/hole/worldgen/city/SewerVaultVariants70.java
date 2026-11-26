package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 70: differentiate ordinary sewer trunk wall construction by segment.
 * Work only at the outer shoulder of existing 7-wide tunnels; never overwrite
 * water/drain, 2-wide dry passage, shafts, or intersection maneuvering space.
 */
final class SewerVaultVariants70 {
    private static final int FIRST_VERSION = 52;
    private static final long SALT = 0x57027071L;
    private SewerVaultVariants70() {}

    static int material(long seed, boolean xAxis, int trunk, int along) {
        return CityHash.range(CityHash.of(seed, SALT + (xAxis ? 1 : 2),
                trunk, Math.floorDiv(along, 61)), 3);
    }

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        if (plan.settings.worldDetailVersion() < FIRST_VERSION || !plan.settings.sewerUnderworld()) return;
        for (int x = c.minX; x <= c.maxX; x++) {
            int dx = nearest(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                int dz = nearest(zLines, z);
                if (Math.min(dx, dz) != 3 || (dx <= 14 && dz <= 14)) continue;
                boolean xAxis = dx < dz;
                int along = xAxis ? z : x;
                int trunk = xAxis ? nearestLine(xLines, x) : nearestLine(zLines, z);
                int section = Math.floorMod(along, 61);
                if (section < 6 || section > 54) continue;
                int kind = material(plan.seed, xAxis, trunk, along);
                // Inset service walls only: no new lighting, hazard blocks or fluid.
                c.set(x, 50, z, kind == 0 ? Blocks.STONE_BRICKS
                        : kind == 1 ? Blocks.TUFF_BRICKS : Blocks.DEEPSLATE_BRICKS);
                c.set(x, 51, z, kind == 0 ? Blocks.MOSSY_STONE_BRICKS
                        : kind == 1 ? Blocks.POLISHED_TUFF : Blocks.CRACKED_DEEPSLATE_BRICKS);
                if (section % 11 == 0) c.set(x, 52, z, Blocks.IRON_BARS);
            }
        }
    }

    private static int nearest(List<Line> lines, int p) {
        int best = Integer.MAX_VALUE;
        for (Line line : lines) best = Math.min(best, Math.abs(p - line.center()));
        return best;
    }

    private static int nearestLine(List<Line> lines, int p) {
        int best = Integer.MAX_VALUE, center = 0;
        for (Line line : lines) {
            int distance = Math.abs(p - line.center());
            if (distance < best) { best = distance; center = line.center(); }
        }
        return center;
    }
}
