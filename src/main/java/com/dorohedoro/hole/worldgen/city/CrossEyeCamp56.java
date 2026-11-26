package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * v56: a Cross-Eye gang camp on a vacant or rubble lot. Laid out deliberately, the same way every time, scaled to the lot:
 * <ul>
 *   <li>a two-high wall of rusted sheet and patchwork masonry around the lot, with a three-wide gate in the middle of the
 *       south side, where every lot meets its street;</li>
 *   <li>the gang's red plus-shaped cross painted on the wall on both sides of the gate;</li>
 *   <li>a camp fire in the middle with three log seats;</li>
 *   <li>two lean-to shelters against the north wall, each with two bedrolls and a supply barrel;</li>
 *   <li>a hidden {@code camp_marker} under the floor that brings the gang the first time a player comes near.</li>
 * </ul>
 * Rare: one in {@value #RARITY} qualifying lots, only in derelict, outskirts and industrial districts, and never within
 * {@value #MIN_SPAWN_DISTANCE} blocks of the players' starting point.
 */
public final class CrossEyeCamp56 {
    private static final int RARITY = 20;
    private static final int MIN_SPAWN_DISTANCE = 700;
    private static final int MIN_SIZE = 15;
    private static final long SALT = 0xC4055E7EL;

    private CrossEyeCamp56() {}

    static boolean eligible(Canvas c, CityPlan plan, Lot lot) {
        return c.hasCrossEyeCamps56() && eligible(plan, lot);
    }

    /** Whether a lot is a camp, given a v56 or later world. Pure plan math, so callers outside generation can ask too. */
    static boolean eligible(CityPlan plan, Lot lot) {
        if (lot.type() != LotType.VACANT && lot.type() != LotType.RUBBLE) return false;
        Rect r = lot.rect();
        if (r.width() < MIN_SIZE || r.depth() < MIN_SIZE) return false;
        District d = lot.district();
        if (d != District.DERELICT && d != District.OUTSKIRTS && d != District.INDUSTRIAL) return false;
        if (CityHash.range(CityHash.of(lot.hash(), SALT), RARITY) != 0) return false;
        BlockPos spawn = plan.spawnPoint();
        long dx = r.centerX() - spawn.getX(), dz = r.centerZ() - spawn.getZ();
        return dx * dx + dz * dz >= (long) MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;
    }

    /** The center of the nearest camp lot within a square radius, or null. For tests; walks every chunk's lots. */
    public static net.minecraft.core.BlockPos findNearest(CityPlan plan, int x, int z, int radius) {
        if (plan.settings.worldDetailVersion() < 56) return null;
        net.minecraft.core.BlockPos best = null;
        long bestD = Long.MAX_VALUE;
        java.util.Set<Rect> seen = new java.util.HashSet<>();
        for (int cx = (x - radius) >> 4; cx <= (x + radius) >> 4; cx++) for (int cz = (z - radius) >> 4; cz <= (z + radius) >> 4; cz++) {
            for (Lot lot : plan.lotsIn(new net.minecraft.world.level.ChunkPos(cx, cz))) {
                if (!seen.add(lot.rect()) || !eligible(plan, lot)) continue;
                long dx = lot.rect().centerX() - x, dz = lot.rect().centerZ() - z, d = dx * dx + dz * dz;
                if (d < bestD) { bestD = d; best = new net.minecraft.core.BlockPos(lot.rect().centerX(), GROUND_Y, lot.rect().centerZ()); }
            }
        }
        return best;
    }

    static void paint(Canvas c, Lot lot) {
        Rect r = lot.rect();
        int x0 = r.x0() + 1, x1 = r.x1() - 1, z0 = r.z0() + 1, z1 = r.z1() - 1;
        int cx = (x0 + x1) >> 1, cz = (z0 + z1) >> 1;
        // Hard-packed ground, cleared of anything the lot painter would have grown.
        c.fill(new Rect(x0, z0, x1, z1), STREET_Y, STREET_Y, Mix.of(Blocks.COARSE_DIRT, 6, Blocks.GRAVEL, 2, Blocks.PACKED_MUD, 2));
        c.fill(new Rect(x0, z0, x1, z1), GROUND_Y, GROUND_Y + 5, Blocks.AIR);

        // Walls: rusted sheet with masonry posts every fourth block, two high; a three-wide gate on the south side.
        for (int x = x0; x <= x1; x++) {
            wall(c, x, z0, x);
            if (Math.abs(x - cx) > 1) wall(c, x, z1, x);
        }
        for (int z = z0 + 1; z < z1; z++) {
            wall(c, x0, z, z);
            wall(c, x1, z, z);
        }
        // The gang's mark beside the gate, facing the street: a red plus-shaped cross on a three-high panel each side.
        for (int side : new int[]{-1, 1}) {
            int px = cx + side * 3;
            c.fill(px - 1, GROUND_Y, z1, px + 1, GROUND_Y + 2, z1, ModBlocks.RUSTED_INDUSTRIAL_SHEET.get());
            c.fill(px, GROUND_Y, z1, px, GROUND_Y + 2, z1, Blocks.RED_CONCRETE);
            c.set(px - 1, GROUND_Y + 1, z1, Blocks.RED_CONCRETE);
            c.set(px + 1, GROUND_Y + 1, z1, Blocks.RED_CONCRETE);
        }

        // Camp fire with three log seats facing it.
        c.set(cx, GROUND_Y, cz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        BlockState logX = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState logZ = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        c.set(cx, GROUND_Y, cz - 2, logX);
        c.set(cx - 2, GROUND_Y, cz, logZ);
        c.set(cx + 2, GROUND_Y, cz, logZ);

        // Two lean-tos against the north wall, one each side of the middle.
        for (int side : new int[]{-1, 1}) {
            int sx0 = side < 0 ? x0 + 1 : x1 - 4, sx1 = sx0 + 3;
            leanTo(c, sx0, sx1, z0 + 1, side < 0);
        }

        // The gang arrives when a player comes near: the marker sits under the floor south of the fire.
        c.set(cx, STREET_Y - 1, cz + 2, ModBlocks.CAMP_MARKER.get().defaultBlockState());
    }

    private static void wall(Canvas c, int x, int z, int along) {
        BlockState block = Math.floorMod(along, 4) == 0 ? ModBlocks.PATCHWORK_MASONRY.get().defaultBlockState()
                : ModBlocks.RUSTED_INDUSTRIAL_SHEET.get().defaultBlockState();
        c.set(x, GROUND_Y, z, block);
        c.set(x, GROUND_Y + 1, z, block);
    }

    /** A 4-wide, 3-deep shelter: a slab roof on fence posts, two bedrolls and a barrel at the back. */
    private static void leanTo(Canvas c, int x0, int x1, int z0, boolean barrelWest) {
        BlockState slab = Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z0 + 2; z++) c.set(x, GROUND_Y + 2, z, slab);
        c.set(x0, GROUND_Y, z0 + 2, Blocks.DARK_OAK_FENCE);
        c.set(x0, GROUND_Y + 1, z0 + 2, Blocks.DARK_OAK_FENCE);
        c.set(x1, GROUND_Y, z0 + 2, Blocks.DARK_OAK_FENCE);
        c.set(x1, GROUND_Y + 1, z0 + 2, Blocks.DARK_OAK_FENCE);
        // Barrel at one end; a red and a black bedroll in the other three columns, with a gap between them.
        int red = barrelWest ? x0 + 1 : x0, black = red + 2;
        for (int z = z0; z <= z0 + 1; z++) {
            c.set(red, GROUND_Y, z, Blocks.RED_CARPET);
            c.set(black, GROUND_Y, z, Blocks.BLACK_CARPET);
        }
        c.barrel(barrelWest ? x0 : x1, GROUND_Y, z0, "cross_eyes_camp");
    }
}
