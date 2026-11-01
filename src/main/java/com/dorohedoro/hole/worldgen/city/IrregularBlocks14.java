package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;

/**
 * New-world local street parcels. Unlike the old rectangular BSP street lattice, each
 * superblock contains a stepped diagonal passage with irregularly offset side alleys.
 * This is pure, seed-based layout math: no world/chunk reads or mutable random sources.
 *
 * <p>The one-block-per-step center shifts connect across the wide perpendicular
 * passages, providing traversable routes between two main streets. No parcel enters
 * either street corridor. Some blocks intentionally retain the prior layout for
 * realistic mixed-age neighborhoods, not a repeated copy of one pattern.
 */
public final class IrregularBlocks14 {
    private static final long SALT = 0x14B10C4D5L;
    private static final int CROSS_ALLEY = 5;
    private static final int DIAGONAL_ALLEY = 7;
    private static final int MIN_PARCEL = 10;

    private IrregularBlocks14() {}

    public static boolean usesOffsetBlocks(long hash) {
        return CityHash.range(CityHash.mix(hash ^ SALT), 8) != 0;
    }

    /** Returns non-overlapping rectangular parcels surrounding connected, oblique streets. */
    public static List<Rect> parcels(Rect area, long hash) {
        return parcels(area, hash, false);
    }

    /** Dense districts use a narrow, fully connected local passage, with original
     *  seven-wide paths retained for older saved worlds and open districts. */
    public static List<Rect> parcels(Rect area, long hash, boolean dense) {
        if (area.width() < 55 || area.depth() < 55) return List.of(area);
        int crossAlley = dense ? 3 : CROSS_ALLEY;
        int diagonalAlley = dense ? 5 : DIAGONAL_ALLEY;
        int minParcel = dense ? 11 : MIN_PARCEL;
        boolean longX = CityHash.range(CityHash.mix(hash + SALT), 2) == 0;
        int along0 = longX ? area.x0() : area.z0();
        int along1 = longX ? area.x1() : area.z1();
        int across0 = longX ? area.z0() : area.x0();
        int across1 = longX ? area.z1() : area.x1();
        int extent = along1 - along0 + 1;
        int midpoint = (across0 + across1) / 2;
        int crossReach = Math.min(16, (across1 - across0) / 5);
        int startShift = CityHash.range(CityHash.mix(hash ^ 0x51D4L), crossReach * 2 + 1) - crossReach;
        int endShift = CityHash.range(CityHash.mix(hash ^ 0x95B1L), crossReach * 2 + 1) - crossReach;
        // Ensure the central passage actually turns, not a flat horizontal line.
        if (Math.abs(endShift - startShift) < 7) endShift = startShift > 0 ? -crossReach : crossReach;
        int start = midpoint + startShift;
        int end = midpoint + endShift;

        List<Rect> parcels = new ArrayList<>();
        int at = along0;
        int section = 0;
        while (at <= along1) {
            long h = CityHash.mix(hash + section * 0x9E3779B97F4A7C15L);
            int width = (dense ? 16 : 17) + CityHash.range(h, dense ? 13 : 11);
            int to = Math.min(along1, at + width - 1);
            // A final sliver narrower than a useful building joins the preceding alley.
            if (along1 - to < minParcel + crossAlley) to = along1;
            int sample = (at + to) / 2;
            int center = start + (int)Math.round((double)(end - start) * (sample - along0) / Math.max(1, extent - 1));
            // Slightly widen the passage in old market/industrial patches.
            int half = diagonalAlley / 2 + (CityHash.range(h >>> 11, 5) == 0 ? 1 : 0);
            Rect near = longX ? new Rect(at, across0, to, center - half - 1)
                    : new Rect(across0, at, center - half - 1, to);
            Rect far = longX ? new Rect(at, center + half + 1, to, across1)
                    : new Rect(center + half + 1, at, across1, to);
            if (near.width() >= minParcel && near.depth() >= minParcel) parcels.add(near);
            if (far.width() >= minParcel && far.depth() >= minParcel) parcels.add(far);
            section++;
            at = to + crossAlley + 1;
        }
        return List.copyOf(parcels);
    }

    /** Vary avenue alignment by a few blocks between intersections; canals/trams stay straight. */
    public static int avenueOffset(long seed, long axisSalt, int index, int along, int nearestCrossingDistance) {
        if (nearestCrossingDistance <= 13) return 0;
        int section = Math.floorDiv(along, 28);
        int t = Math.floorMod(along, 28);
        int left = CityHash.range(CityHash.of(seed, axisSalt, index, section), 9) - 4;
        int right = CityHash.range(CityHash.of(seed, axisSalt, index, section + 1), 9) - 4;
        int offset = Math.round(left + (right - left) * t / 28.0F);
        // Fade to centered crossings, so roads always join safely at intersections.
        int ramp = Math.min(9, nearestCrossingDistance - 13);
        return Math.round(offset * ramp / 9.0F);
    }
}
