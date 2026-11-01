package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Main 7-wide municipal sewer now reads as a masonry sewer, not an empty tunnel:
 * continuous shaped vault shoulders, oxidised service pipes, occasional grated
 * access over the shallow wastewater channel, dry textured walkways and lighting.
 *
 * The center drain remains water at Y48; clear, unobstructed dry routes stay at
 * d=2. Crossroads are left fully open, as are downstream access shafts and
 * dungeon corridor links (which are carved after this pass).
 */
final class SewerDetails24 {
    private static final long SALT = 0x5E7E240AL;
    private SewerDetails24() {}

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            int dx = nearest(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                int dz = nearest(zLines, z);
                int d = Math.min(dx, dz);
                if (d > 3) continue;
                // Keep the whole municipal intersection clear, including shafts.
                if (dx <= 9 && dz <= 9) continue;
                boolean northSouth = dx < dz;
                int along = northSouth ? z : x;
                int signed = northSouth ? signedOffset(xLines, x) : signedOffset(zLines, z);
                long h = CityHash.of(plan.seed, SALT, Math.floorDiv(along, 24),
                        northSouth ? x : z);
                if (d == 2) {
                    // Original floor at Y49 is solid; no fluid introduced into the walkway.
                    c.set(x, 49, z, CityHash.range(CityHash.of(plan.seed, SALT+1, x, z), 9) == 0
                            ? Blocks.MOSSY_STONE_BRICKS : Blocks.DEEPSLATE_TILES);
                }
                if (d == 3) {
                    // A rounded shoulder near ceiling height, leaving 3-high walking clearance.
                    Direction inward = northSouth
                            ? (signed > 0 ? Direction.WEST : Direction.EAST)
                            : (signed > 0 ? Direction.NORTH : Direction.SOUTH);
                    c.set(x, 53, z, Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, inward)
                            .setValue(StairBlock.HALF, Half.TOP));
                    // Real-looking pipe traces follow walls for a few dozen blocks at a time.
                    // At Y52 they are above the walkable path, on the farthest side.
                    int section = Math.floorMod(along, 24);
                    if (CityHash.range(h, 4) <= 1 && section >= 2 && section <= 20) {
                        c.set(x, 52, z, (section % 11 == 0)
                                ? Blocks.WAXED_OXIDIZED_COPPER
                                : Blocks.WAXED_WEATHERED_COPPER);
                    } else if (section % 17 == 0) {
                        c.set(x, 52, z, Blocks.IRON_BARS);
                    }
                }
                // Crossable inspection gratings allow sight of water below without
                // damming it; the continuous d=2 walkway always remains open.
                if (d == 0 && Math.floorMod(along, 49) == 0) {
                    c.set(x, 49, z, Blocks.IRON_TRAPDOOR.defaultBlockState()
                            .setValue(TrapDoorBlock.HALF, Half.TOP));
                }
                // Hanging lamps light the real route while leaving a full 3-block
                // clearance from Y50..52 and keeping dungeon junctions uncluttered.
                if (d == 0 && Math.floorMod(along, 31) == 0) {
                    c.set(x, 53, z, Blocks.LANTERN.defaultBlockState()
                            .setValue(LanternBlock.HANGING, true));
                }
            }
        }
    }

    private static int nearest(List<Line> lines, int p) {
        int best=Integer.MAX_VALUE;
        for (Line line:lines) best=Math.min(best,Math.abs(p-line.center()));
        return best;
    }
    private static int signedOffset(List<Line> lines, int p) {
        int best=Integer.MAX_VALUE, signed=0;
        for (Line line:lines) {
            int o=p-line.center();
            if (Math.abs(o)<best) {best=Math.abs(o);signed=o;}
        }
        return signed;
    }
}
