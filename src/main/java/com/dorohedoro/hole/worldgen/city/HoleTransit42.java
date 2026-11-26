package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Passenger-scale tram stops along EXISTING connected tram tracks. This is
 * urban function rather than decorative rubble: a public place to find the
 * rails, take shelter, read the district and wait without blocking minecarts.
 * Platforms have clear walkable space, no new lights or loot chests.
 */
final class HoleTransit42 {
    private static final int SPACING = 176;
    private HoleTransit42() {}

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        if (plan.settings.worldDetailVersion() < 26) return;
        for (Line tram : xLines) {
            if (tram.kind() != Line.Kind.TRAM || tram.halfWidth() < 7) continue;
            for (int row = Math.floorDiv(c.minZ - SPACING, SPACING); row <= Math.floorDiv(c.maxZ + SPACING, SPACING); row++) {
                int z = row * SPACING + 70 + CityHash.range(CityHash.of(plan.seed, 0x42BADA2L, tram.index(), row), 17);
                if (z + 5 < c.minZ || z - 5 > c.maxZ) continue;
                if (crossing(zLines, z, 24)) continue;
                int x = tram.center() + tram.halfWidth();
                // Do not tilt a shelter across a steep escarpment.
                if (Math.abs(CityTerrain40.offset(plan, x, z - 4) - CityTerrain40.offset(plan, x, z + 4)) > 1) continue;
                station(c, plan, x, z);
            }
        }
    }

    private static boolean crossing(List<Line> lines, int at, int buffer) {
        for (Line line : lines) if (Math.abs(line.center() - at) <= line.halfWidth() + buffer) return true;
        return false;
    }

    private static void station(Canvas c, CityPlan plan, int edgeX, int z) {
        int y = CityPlan.GROUND_Y;
        long h = CityHash.of(plan.seed, 0x4257A7L, edgeX, z);
        // Roof is narrow and extends along the sidewalk, never over the rails
        // (the nearest rails are 5 blocks closer to the road center).
        for (int dz = -4; dz <= 4; dz++) {
            for (int dx = -3; dx <= 0; dx++) {
                if (dz == -4 || dz == 4 || dx == -3 || dx == 0) {
                    placeIfAir(c, edgeX + dx, y + 4, z + dz, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.defaultBlockState());
                }
            }
        }
        for (int dz : new int[]{-4, 4}) {
            for (int dy = 0; dy <= 3; dy++) {
                placeIfAir(c, edgeX, y + dy, z + dz, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        // Functional seating: stair benches line the far edge, leaving the
        // three-block approach to the actual rail clear for passengers.
        for (int dz : new int[]{-2, 0, 2}) {
            if (CityHash.range(h + dz, 6) == 0) continue;
            placeIfAir(c, edgeX, y, z + dz, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST));
        }
        // A real wall sign at eye height, with an existing support behind it.
        // Only the selected station row gets a sign so signage stays readable.
        int signX = edgeX - 1, signZ = z - 4;
        if (c.contains(signX, signZ) && c.get(signX, y + 2, signZ).isAir()
                && c.get(edgeX, y + 2, signZ).is(Blocks.IRON_BARS)) {
            c.sign(signX, y + 2, signZ, Direction.WEST, Blocks.SPRUCE_WALL_SIGN,
                    "HOLE TRAM", "PUBLIC STOP", "", "");
        }
    }

    private static void placeIfAir(Canvas c, int x, int y, int z, BlockState state) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()) c.set(x, y, z, state);
    }
}
