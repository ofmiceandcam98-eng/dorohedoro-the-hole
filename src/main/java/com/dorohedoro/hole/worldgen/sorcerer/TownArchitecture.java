package com.dorohedoro.hole.worldgen.sorcerer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared building parts: site preparation, storeyed shells, windows, roofs, stair flights, chimneys and furniture.
 * All coordinates are local to a {@link Painter}.
 */
final class TownArchitecture {
    private TownArchitecture() {}

    /** Foundations to natural ground, a floor slab, and a clear volume up to {@code top}. */
    static void site(Painter p, TownPalette pal, int top) {
        int w = p.w(), d = p.d();
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.foundation(x, z, -2, pal.foundation());
        p.fill(0, -1, 0, w - 1, -1, d - 1, pal.foundation());
        p.fill(1, -1, 1, w - 2, -1, d - 2, pal.floor());
        p.air(0, 0, 0, w - 1, top, d - 1);
        for (int z = 0; z < d; z++) for (int x = 0; x < w; x++) p.clearTrees(x, z, top + 1, top + 14);
    }

    /**
     * Perimeter walls for {@code storeys} storeys of {@code sh} blocks (floor slab included), corner trim, string
     * courses at each floor line and interior floors. Returns the wall top y + 1, where the roof starts.
     */
    static int shell(Painter p, TownPalette pal, int storeys, int sh) {
        int w = p.w(), d = p.d(), top = storeys * sh;
        p.walls(0, 0, w - 1, d - 1, 0, top - 1, pal.wall());
        for (int i = 1; i < storeys; i++) {
            int y = i * sh - 1;
            p.fill(1, y, 1, w - 2, y, d - 2, pal.floor());
            p.walls(0, 0, w - 1, d - 1, y, y, pal.accent());
        }
        for (int y = 0; y < top; y++) {
            p.set(0, y, 0, pal.trim());
            p.set(w - 1, y, 0, pal.trim());
            p.set(0, y, d - 1, pal.trim());
            p.set(w - 1, y, d - 1, pal.trim());
        }
        return top;
    }

    /**
     * Windows along every wall of a storey, at a rhythm that skips corners and the door column(s). Tall storeys get
     * two-high lancets.
     */
    static void windows(Painter p, TownPalette pal, int base, int sh, int doorX, int doorWidth, int spacing, boolean ground) {
        int w = p.w(), d = p.d();
        int height = sh >= 5 ? 2 : 1;
        for (int x = 2; x <= w - 3; x += spacing) {
            boolean door = ground && x >= doorX - 1 && x <= doorX + doorWidth;
            for (int h = 1; h <= height; h++) {
                if (!door) p.set(x, base + h, 0, pal.window());
                p.set(x, base + h, d - 1, pal.window());
            }
        }
        for (int z = 2; z <= d - 3; z += spacing) {
            for (int h = 1; h <= height; h++) {
                p.set(0, base + h, z, pal.window());
                p.set(w - 1, base + h, z, pal.window());
            }
        }
    }

    /** A front door with a lintel, the door itself and a probe for QA. */
    static void frontDoor(Painter p, TownPalette pal, int doorX, boolean wide) {
        p.door(doorX, 0, 0, pal.door(), Direction.NORTH, false);
        if (wide) p.door(doorX + 1, 0, 0, pal.door(), Direction.NORTH, true);
    }

    /** Timber beams across a single-storey hall at {@code y}, for hanging lanterns under a high roof. */
    static void beams(Painter p, TownPalette pal, int y, int... zs) {
        BlockState beam = pal.trim().hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
                ? pal.trim().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X)
                : pal.trim();
        for (int z : zs) p.fill(1, y, z, p.w() - 2, y, z, beam);
    }

    /**
     * Gable roof starting at {@code y}. {@code ridgeZ}: the ridge runs front to back and the gables face the street
     * (the narrow Gothic townhouse look); otherwise the eaves face the street. Eaves overhang only at the front and
     * back so neighbouring buildings never collide. {@code steep} rises two blocks per step. Returns the ridge y.
     */
    static int gable(Painter p, TownPalette pal, int y, boolean ridgeZ, boolean steep) {
        int w = p.w(), d = p.d();
        int a0 = ridgeZ ? 0 : -1, a1 = ridgeZ ? w - 1 : d;
        int b0 = ridgeZ ? -1 : 0, b1 = ridgeZ ? d : w - 1;
        int layers = (a1 - a0 + 2) / 2;
        int ridge = y;
        for (int i = 0; i < layers; i++) {
            int low = a0 + i, high = a1 - i;
            int rise = steep ? 2 * i : i;
            int yy = y + rise;
            for (int b = b0; b <= b1; b++) {
                if (low < high) {
                    Direction up = ridgeZ ? Direction.EAST : Direction.SOUTH;
                    roofCell(p, pal, ridgeZ, low, b, yy, up, steep);
                    roofCell(p, pal, ridgeZ, high, b, yy, up.getOpposite(), steep);
                } else {
                    put(p, ridgeZ, low, yy, b, pal.roofFull());
                    if (steep) put(p, ridgeZ, low, yy + 1, b, Painter.slab(pal.roofSlab(), false));
                }
            }
            ridge = yy + (steep ? 1 : 0);
        }
        // Gable triangles in the end walls.
        int e0 = 0, e1 = ridgeZ ? d - 1 : w - 1;
        for (int a = 0; a <= (ridgeZ ? w - 1 : d - 1); a++) {
            int layer = Math.min(a - a0, a1 - a);
            int under = y + (steep ? 2 * layer : layer);
            for (int yy = y; yy < under; yy++) {
                put(p, ridgeZ, a, yy, e0, pal.wall());
                put(p, ridgeZ, a, yy, e1, pal.wall());
            }
        }
        // A small window high in each gable.
        int mid = ridgeZ ? w / 2 : d / 2;
        int midLayer = Math.min(mid - a0, a1 - mid);
        if ((steep ? 2 * midLayer : midLayer) >= 3) {
            put(p, ridgeZ, mid, y + 1, e0, pal.window());
            put(p, ridgeZ, mid, y + 1, e1, pal.window());
        }
        return ridge;
    }

    private static void roofCell(Painter p, TownPalette pal, boolean ridgeZ, int a, int b, int yy, Direction up, boolean steep) {
        if (steep) {
            put(p, ridgeZ, a, yy, b, pal.roofFull());
            put(p, ridgeZ, a, yy + 1, b, Painter.stair(pal.roofStairs(), up, false));
        } else {
            put(p, ridgeZ, a, yy, b, Painter.stair(pal.roofStairs(), up, false));
        }
    }

    /** a is the across-slope coordinate, b along the ridge. */
    private static void put(Painter p, boolean ridgeZ, int a, int y, int b, BlockState state) {
        if (ridgeZ) p.set(a, y, b, state);
        else p.set(b, y, a, state);
    }

    /** A hipped pyramid over [x0..x1] x [z0..z1], stairs facing up-slope on all four sides. Returns the peak y. */
    static int pyramid(Painter p, TownPalette pal, int x0, int z0, int x1, int z1, int y) {
        int i = 0;
        while (x0 + i <= x1 - i && z0 + i <= z1 - i) {
            int lx = x0 + i, hx = x1 - i, lz = z0 + i, hz = z1 - i, yy = y + i;
            if (lx == hx || lz == hz) {
                p.fill(lx, yy, lz, hx, yy, hz, pal.roofFull());
                return yy;
            }
            for (int x = lx; x <= hx; x++) {
                p.set(x, yy, lz, Painter.stair(pal.roofStairs(), Direction.SOUTH, false));
                p.set(x, yy, hz, Painter.stair(pal.roofStairs(), Direction.NORTH, false));
            }
            for (int z = lz + 1; z < hz; z++) {
                p.set(lx, yy, z, Painter.stair(pal.roofStairs(), Direction.EAST, false));
                p.set(hx, yy, z, Painter.stair(pal.roofStairs(), Direction.WEST, false));
            }
            i++;
        }
        return y + i;
    }

    /** Flat roof with a one-block parapet. Returns the parapet top y. */
    static int flat(Painter p, TownPalette pal, int y) {
        int w = p.w(), d = p.d();
        p.fill(0, y, 0, w - 1, y, d - 1, pal.accent());
        p.walls(0, 0, w - 1, d - 1, y + 1, y + 1, pal.accent());
        for (int x = 1; x < w - 1; x += 2) {
            p.set(x, y + 1, 0, Painter.slab(pal.roofSlab(), false));
            p.set(x, y + 1, d - 1, Painter.slab(pal.roofSlab(), false));
        }
        return y + 1;
    }

    /**
     * A straight flight of {@code rise} stairs climbing in {@code dir} from (x, base, z), headroom cut through the floor
     * above, and a railing on the open side of the stairwell.
     */
    static void flight(Painter p, int x, int z, int base, Direction dir, int rise, Block stairs, BlockState support,
                       Block railing, Direction openSide) {
        for (int i = 0; i < rise; i++) {
            int sx = x + dir.getStepX() * i, sz = z + dir.getStepZ() * i;
            if (i > 0) p.fill(sx, base, sz, sx, base + i - 1, sz, support);
            p.set(sx, base + i, sz, Painter.stair(stairs, dir, false));
            p.air(sx, base + i + 1, sz, sx, base + i + 3, sz);
            if (i < rise - 1 && railing != null) {
                p.set(sx + openSide.getStepX(), base + rise, sz + openSide.getStepZ(), railing);
            }
        }
    }

    /** A brick chimney in a wall column, rising to {@code topY} with a lit campfire: the town's smoke. */
    static void chimney(Painter p, int x, int z, int fromY, int topY) {
        p.fill(x, fromY, z, x, topY, z, Blocks.BRICKS);
        p.set(x, topY + 1, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
    }

    // ------------------------------------------------------------ furniture

    static void table(Painter p, int x, int y, int z, Block fence, Block plate) {
        p.set(x, y, z, fence);
        p.set(x, y + 1, z, plate);
    }

    /** A chair whose back is on the {@code back} side. */
    static void chair(Painter p, int x, int y, int z, Block stairs, Direction back) {
        p.set(x, y, z, Painter.stair(stairs, back, false));
    }

    static void rug(Painter p, int x0, int y, int z0, int x1, int z1, Block carpet) {
        p.fill(x0, y, z0, x1, y, z1, carpet);
    }

    /** A bookcase two blocks high. */
    static void shelves(Painter p, int x0, int y, int z0, int x1, int z1) {
        p.fill(x0, y, z0, x1, y + 1, z1, Blocks.BOOKSHELF);
    }

    /** A potted plant on whatever is at (x, y - 1, z). */
    static void pot(Painter p, int x, int y, int z, TownRandom rng) {
        Block[] pots = {Blocks.POTTED_FERN, Blocks.POTTED_RED_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM,
                Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_ALLIUM, Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_CACTUS};
        p.set(x, y, z, pots[rng.nextInt(pots.length)]);
    }
}
