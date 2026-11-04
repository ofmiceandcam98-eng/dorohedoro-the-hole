package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;

/**
 * Pass 54: local passages turn around the old building fabric rather than
 * crossing every superblock as a single straight or uniformly diagonal lane.
 *
 * <p>Only version-37+ worlds invoke this selector. The arterial, tram, canal,
 * municipal sewer and protected starter-site geometry is not touched. Every
 * longitudinal passage remains at least five blocks wide; periodic 3+-wide
 * cross alleys join both sides to the existing road system. Lot rectangles are
 * deliberately preserved because the existing furnished buildings, entrances,
 * inventories and grading rely on axis-aligned parcel footprints.
 *
 * <p>This class is pure: the same seed and absolute superblock bounds always
 * return the same parcels regardless of chunk order or thread scheduling.
 */
final class CrookedLanes54 {
    private static final long SALT = 0x54C70AEL;
    private static final int MIN_BUILDABLE = 11;

    private CrookedLanes54() {}

    static boolean use(long blockHash, UrbanPattern52.Fabric fabric) {
        int frequency = switch (fabric) {
            case DENSE -> 15;
            case INHABITED -> 12;
            case DERELICT -> 10;
            case OPEN_COUNTRY -> 4;
        };
        return CityHash.range(CityHash.mix(blockHash ^ SALT), 16) < frequency;
    }

    static List<Rect> parcels(Rect area, long blockHash, UrbanPattern52.Fabric fabric) {
        if (area.width() < 55 || area.depth() < 55) return List.of(area);
        boolean dense = fabric == UrbanPattern52.Fabric.DENSE;
        boolean longX = CityHash.range(CityHash.mix(blockHash + SALT), 2) == 0;
        int along0 = longX ? area.x0() : area.z0();
        int along1 = longX ? area.x1() : area.z1();
        int across0 = longX ? area.z0() : area.x0();
        int across1 = longX ? area.z1() : area.x1();
        int alongSpan = along1 - along0;
        int midpoint = (across0 + across1) / 2;
        int half = dense ? 2 : 3;
        int maxShift = Math.min(16, Math.max(0, (across1 - across0 - 2 * MIN_BUILDABLE - 2 * half) / 2));
        int start = midpoint + signed(blockHash ^ 0x5411L, maxShift);
        int end = midpoint + signed(blockHash ^ 0x5412L, maxShift);
        // A middle anchor produces a dogleg or S-bend rather than another long
        // ruler-straight diagonal. Constrain it to retain both parcel banks.
        int middle = midpoint + signed(blockHash ^ 0x5413L, maxShift);
        if (maxShift >= 7) {
            int linearMid = (start + end) / 2;
            if (Math.abs(middle - linearMid) < 5) {
                middle = midpoint + (middle >= linearMid ? -maxShift : maxShift);
            }
        }
        double hinge = .36 + .28 * CityHash.unit(CityHash.mix(blockHash ^ 0x5414L));
        List<Rect> parcels = new ArrayList<>();
        int at = along0;
        int section = 0;
        while (at <= along1) {
            long h = CityHash.of(blockHash ^ SALT, section);
            int span = (dense ? 17 : 20) + CityHash.range(h, dense ? 14 : 16);
            int endAt = Math.min(along1, at + span - 1);
            int crossGap = (dense ? 3 : 4) + CityHash.range(h >>> 17, 3);
            // The last strip is merged into the current section if it cannot
            // contain a usable 11-wide building beyond another cross alley.
            if (along1 - endAt < MIN_BUILDABLE + crossGap) endAt = along1;
            double t = (double) ((at + endAt) / 2 - along0) / Math.max(1, alongSpan);
            int raw = (int) Math.round(t < hinge
                    ? start + (middle - start) * t / hinge
                    : middle + (end - middle) * (t - hinge) / (1.0 - hinge));
            // Allow 1-block repairs and side-step frontage, not a perfect curve.
            int jog = CityHash.range(h >>> 25, 5) == 0
                    ? (CityHash.range(h >>> 31, 2) == 0 ? -1 : 1) : 0;
            int extraHalf = CityHash.range(h >>> 9, 7) == 0 ? 1 : 0;
            int widenedHalf = half + extraHalf;
            int low = across0 + MIN_BUILDABLE + widenedHalf;
            int high = across1 - MIN_BUILDABLE - widenedHalf;
            int center = Math.max(low, Math.min(high, raw + jog));
            Rect first = longX
                    ? new Rect(at, across0, endAt, center - widenedHalf - 1)
                    : new Rect(across0, at, center - widenedHalf - 1, endAt);
            Rect second = longX
                    ? new Rect(at, center + widenedHalf + 1, endAt, across1)
                    : new Rect(center + widenedHalf + 1, at, across1, endAt);
            // Correctness guard: no narrowing into a fake single-block house.
            if (first.width() >= MIN_BUILDABLE && first.depth() >= MIN_BUILDABLE) parcels.add(first);
            if (second.width() >= MIN_BUILDABLE && second.depth() >= MIN_BUILDABLE) parcels.add(second);
            section++;
            at = endAt + crossGap + 1;
        }
        return List.copyOf(parcels);
    }

    private static int signed(long h, int reach) {
        return CityHash.range(CityHash.mix(h), 2 * reach + 1) - reach;
    }
}
