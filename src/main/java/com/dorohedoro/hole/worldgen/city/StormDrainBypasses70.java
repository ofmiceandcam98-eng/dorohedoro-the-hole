package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 70: uncommon large, two-doorway stormwater bypasses parallel to the original
 * seven-wide municipal trunks. Both mouths connect to the same dry trunk walkway;
 * the center runoff is recessed, not an unavoidable swimming or magic obstacle.
 *
 * <p>All site selection is seed/index/absolute-coordinate based. Unlike decorative
 * alcoves this is an alternate traversable route through a distinct, wider culvert.
 * Never replace the municipal center channel, existing shafts, repair bays or pump
 * stations; all writes are additionally checked by Canvas's vanilla-structure guard.
 */
final class StormDrainBypasses70 {
    static final int FIRST_VERSION = 52;
    private static final int SECTION = 160;
    private static final int HALF_LENGTH = 14;
    private static final int REACH = 14;
    private static final int MIN_JUNCTION_DISTANCE = 42;
    private static final long SALT = 0x57027070L;

    private StormDrainBypasses70() {}

    static boolean selected(long seed, boolean xAxis, int trunk, int section, int version) {
        return version >= FIRST_VERSION && CityHash.range(
                CityHash.of(seed, SALT + (xAxis ? 1 : 2), trunk, section), 3) == 0;
    }

    static int center(long seed, boolean xAxis, int trunk, int section) {
        return section * SECTION + 35 + CityHash.range(
                CityHash.of(seed, SALT + (xAxis ? 3 : 4), trunk, section), 90);
    }

    static int side(long seed, boolean xAxis, int trunk, int section) {
        return CityHash.range(CityHash.of(seed, SALT + (xAxis ? 5 : 6), trunk, section), 2) == 0 ? -1 : 1;
    }

    static boolean interferesWithExisting(long seed, boolean xAxis, int trunk,
                                           int along, int version) {
        // Conservative: reject a bypass if any older bay or pump hall occupies
        // its length, irrespective of side. Prevents masking previous workstations,
        // controls and loading doors on either side of the original municipal trunk.
        for (int section = Math.floorDiv(along - HALF_LENGTH - 6, 64);
             section <= Math.floorDiv(along + HALF_LENGTH + 6, 64); section++) {
            if (SewerServiceBays67.kind(seed, xAxis, trunk, section, version) != 0
                    && Math.abs(along - SewerServiceBays67.center(seed, xAxis, trunk, section))
                    <= HALF_LENGTH + 6) return true;
        }
        for (int section = Math.floorDiv(along - HALF_LENGTH - 11, 192);
             section <= Math.floorDiv(along + HALF_LENGTH + 11, 192); section++) {
            if (StormwaterPumpHalls68.selected(seed, xAxis, trunk, section, version)
                    && Math.abs(along - StormwaterPumpHalls68.center(seed, xAxis, trunk, section))
                    <= HALF_LENGTH + 11) return true;
        }
        return false;
    }

    static void paint(Canvas c, CityPlan plan) {
        int version = plan.settings.worldDetailVersion();
        if (!plan.settings.sewerUnderworld() || version < FIRST_VERSION) return;
        List<Line> xTrunks = plan.xLinesNear(c.minX - REACH - 1, c.maxX + REACH + 1);
        List<Line> zTrunks = plan.zLinesNear(c.minZ - REACH - 1, c.maxZ + REACH + 1);
        // The whole 29-long footprint, not just the current chunk, must see the
        // same nearest junctions or chunk seams would disagree on eligibility.
        int crossingReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH + 1;
        List<Line> xCrossing = plan.xLinesNear(c.minX - crossingReach, c.maxX + crossingReach);
        List<Line> zCrossing = plan.zLinesNear(c.minZ - crossingReach, c.maxZ + crossingReach);
        paintAxis(c, plan, true, xTrunks, zCrossing);
        paintAxis(c, plan, false, zTrunks, xCrossing);
    }

    private static void paintAxis(Canvas c, CityPlan plan, boolean xAxis,
                                  List<Line> trunks, List<Line> crossings) {
        int start = xAxis ? c.minZ : c.minX;
        int end = xAxis ? c.maxZ : c.maxX;
        int first = Math.floorDiv(start - HALF_LENGTH, SECTION);
        int last = Math.floorDiv(end + HALF_LENGTH, SECTION);
        for (Line trunk : trunks) {
            if (trunk.kind() == Line.Kind.CANAL) continue;
            for (int section = first; section <= last; section++) {
                int version = plan.settings.worldDetailVersion();
                if (!selected(plan.seed, xAxis, trunk.index(), section, version)) continue;
                int along = center(plan.seed, xAxis, trunk.index(), section);
                if (crossings.stream().anyMatch(line ->
                        Math.abs(along - line.center()) < MIN_JUNCTION_DISTANCE)) continue;
                if (interferesWithExisting(plan.seed, xAxis, trunk.index(), along, version)) continue;
                int side = side(plan.seed, xAxis, trunk.index(), section);
                Rect footprint = xAxis
                        ? new Rect(Math.min(trunk.center() + side * 3, trunk.center() + side * REACH),
                                   along - HALF_LENGTH,
                                   Math.max(trunk.center() + side * 3, trunk.center() + side * REACH),
                                   along + HALF_LENGTH)
                        : new Rect(along - HALF_LENGTH,
                                   Math.min(trunk.center() + side * 3, trunk.center() + side * REACH),
                                   along + HALF_LENGTH,
                                   Math.max(trunk.center() + side * 3, trunk.center() + side * REACH));
                if (!c.overlaps(footprint) || coastal(plan, footprint)) continue;
                int character = CityHash.range(CityHash.of(plan.seed, SALT + 7,
                        trunk.index(), section), 3);
                build(c, xAxis, trunk.center(), along, side, character);
            }
        }
    }

    private static boolean coastal(CityPlan plan, Rect footprint) {
        for (int x = footprint.x0(); x <= footprint.x1(); x++) {
            for (int z = footprint.z0(); z <= footprint.z1(); z++) {
                if (plan.oceanDepthAt(x, z) > 0 || plan.coastalDistanceAt(x, z) > 0) return true;
            }
        }
        return false;
    }

    static void build(Canvas c, boolean xAxis, int trunk, int along, int side, int character) {
        var masonry = switch (character) {
            case 0 -> Blocks.STONE_BRICKS;             // old brick interceptor
            case 1 -> Blocks.TUFF_BRICKS;              // older reinforced masonry
            default -> Blocks.DEEPSLATE_BRICKS;       // industrial high-capacity culvert
        };
        var pavement = character == 1 ? Blocks.POLISHED_TUFF : Blocks.DEEPSLATE_TILES;
        // Two independent three-wide openings, twenty blocks apart along the same
        // main artery, connect a 29-long secondary corridor back to the trunk.
        for (int distance = 3; distance <= REACH; distance++) {
            for (int offset = -HALF_LENGTH; offset <= HALF_LENGTH; offset++) {
                int x = x(xAxis, trunk, along, side, distance, offset);
                int z = z(xAxis, trunk, along, side, distance, offset);
                if (!c.contains(x, z)) continue;
                boolean entrance = (Math.abs(offset - 10) <= 1 || Math.abs(offset + 10) <= 1)
                        && distance <= 5;
                if (distance < 6 && !entrance) continue; // preserve original sewer wall
                boolean outer = distance == REACH || Math.abs(offset) == HALF_LENGTH;
                c.fill(x, 48, z, x, 57, z, masonry);
                if (entrance || !outer) {
                    c.set(x, 49, z, pavement);
                    c.fill(x, 50, z, x, entrance ? 53 : 55, z, Blocks.AIR);
                }
                // Three different service architectures, but no lamps or chest clutter.
                if (distance == REACH && Math.floorMod(offset + HALF_LENGTH, 12) == 0) {
                    c.set(x, 53, z, Blocks.IRON_BARS);
                }
                if (distance == REACH - 1 && Math.floorMod(offset + HALF_LENGTH, 9) == 0) {
                    c.set(x, 55, z, Blocks.WAXED_WEATHERED_COPPER);
                }
                // Center run-off is recessed one block; the dry parallel lanes at
                // distance 8 and 12 remain uninterrupted from door to door.
                if (distance == 10 && Math.abs(offset) < HALF_LENGTH) {
                    c.set(x, 48, z, Blocks.WATER);
                    c.set(x, 49, z, Blocks.AIR);
                }
            }
        }
    }

    private static int x(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? trunk + side * distance : along + offset;
    }

    private static int z(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? along + offset : trunk + side * distance;
    }
}
