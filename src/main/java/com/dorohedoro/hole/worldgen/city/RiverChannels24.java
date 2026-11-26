package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 24 naturalized canal-rivers. The municipal ROW is reserved by the old
 * city plan; the actual water meanders INSIDE it while its banks rise in
 * stepped, seed-dependent terraces. No building lots are carved.
 *
 * The original street grid only owns the outer ROW, not the river's center:
 * every geometry decision is world-coordinate/seed based. Chunk order cannot
 * alter the bends, gravel bars, river depth or slope of either shore.
 *
 * Physical limitation: these are connected urban canal-rivers, not yet a full
 * natural watershed. Surface level follows the original column relief; in
 * elevated districts, level changes require real fluid-flow client QA.
 */
final class RiverChannels24 {
    private static final long BEND_X = 0x714EA003L, BEND_Z = 0x714EA004L;
    private static final long WIDTH_X = 0x714EA005L, WIDTH_Z = 0x714EA006L;
    private static final long COAST = 0x714EA007L;
    private static final long BED = 0x714EA008L;
    // The ROW itself is now supplied by Line.halfWidth(). Older profiles keep nine.
    private static final int WATER_TOP = 61;
    private RiverChannels24() {}

    /** Smooth stations produce bends that are identical on both sides of a chunk seam. */
    private static double station(long seed, long salt, int index, int along, int period) {
        int section = Math.floorDiv(along, period);
        double t = Math.floorMod(along, period) / (double)period;
        double ease = t * t * (3.0 - 2.0 * t);
        long a = CityHash.of(seed, salt + index * 9973L, section, 0);
        long b = CityHash.of(seed, salt + index * 9973L, section + 1, 0);
        double v0 = (CityHash.range(a, 2001) - 1000) / 1000.0;
        double v1 = (CityHash.range(b, 2001) - 1000) / 1000.0;
        return v0 + (v1 - v0) * ease;
    }

    static int centerOffset(long seed, Line line, boolean alongZ, int along) {
        long salt = alongZ ? BEND_X : BEND_Z;
        if (line.halfWidth() < 15) {
            return (int)Math.round(3.0 * station(seed, salt, line.index(), along, 80));
        }
        if (line.halfWidth() >= 24) {
            // v24: a visible curving thalweg with a secondary riffle; the plan
            // reserves the full ROW before laying any lots. Keeps 4+ block banks.
            double longBend = station(seed, salt ^ 0x40F120L, line.index(), along, 158);
            double shortBend = station(seed, salt ^ 0x40F121L, line.index(), along, 63);
            return Math.max(-17, Math.min(17,
                    (int)Math.round(14.0 * longBend + 3.0 * shortBend)));
        }
        // New v21 waterway reserve: bends travel 8-10 blocks laterally, with a
        // separate smaller wave so a whole street-length is not one straight run.
        // This is a fixed world-coordinate field, never a random walk or chunk RNG.
        double broad = station(seed, salt, line.index(), along, 112);
        double local = station(seed, salt ^ 0x36B1EC0L, line.index(), along, 47);
        return Math.max(-10, Math.min(10, (int)Math.round(8.4 * broad + 2.0 * local)));
    }

    static int width(long seed, Line line, boolean alongZ, int along) {
        // Two smooth frequency bands: long reaches widen/narrow over 100+ blocks,
        // while smaller 40-block riffles add subtle coves and constrictions.
        // Channel diameter varies between 7 and 15 blocks, not just its path.
        double broad = station(seed, alongZ ? WIDTH_X : WIDTH_Z, line.index(), along, 144);
        double local = station(seed, (alongZ ? WIDTH_X : WIDTH_Z) ^ 0x5E6D123L,
                line.index(), along, 40);
        int desired = 5 + (int)Math.round(2.3 * broad + 0.7 * local);
        int radius = Math.max(3, Math.min(line.halfWidth() >= 24 ? 10 : line.halfWidth() >= 15 ? 8 : 7, desired));
        // Preserve at least a two-block bank on both edges of the reservation.
        // This is the key difference from merely snaking water across existing lots.
        int corridorLimit = line.halfWidth() - 2 - Math.abs(centerOffset(seed, line, alongZ, along));
        return Math.max(3, Math.min(radius, corridorLimit));
    }

    static int signedDistance(long seed, Line line, boolean alongZ, int x, int z) {
        int along = alongZ ? z : x;
        int cross = alongZ ? x : z;
        return cross - line.center() - centerOffset(seed, line, alongZ, along);
    }

    static int distance(long seed, Line line, boolean alongZ, int x, int z) {
        return Math.abs(signedDistance(seed, line, alongZ, x, z));
    }

    static boolean water(long seed, Line line, boolean alongZ, int x, int z) {
        return distance(seed, line, alongZ, x, z) <= width(seed, line, alongZ, alongZ ? z : x);
    }

    /** Actual solid bottom Y for habitat planting: shore shallows are higher. */
    static int bedY(long seed, Line line, boolean alongZ, int x, int z) {
        int d = distance(seed, line, alongZ, x, z);
        int w = width(seed, line, alongZ, alongZ ? z : x);
        // 2-block shallows on outer edge, and a deeper navigable core.
        if (d >= w - 1) return 59;
        if (d >= w - 2) return 58;
        long h = CityHash.of(seed, BED + line.index(), Math.floorDiv(alongZ ? z : x, 36));
        return d <= 1 && CityHash.range(h, 4) != 0 ? 56 : 57;
    }

    private record Cross(Line line, boolean alongZ, int signed, int edge, int bed) {}

    private static Cross channel(long seed, Line line, boolean alongZ, int x, int z) {
        if (line == null) return null;
        int along = alongZ ? z : x;
        int signed = signedDistance(seed, line, alongZ, x, z);
        return new Cross(line, alongZ, signed,
                Math.abs(signed) - width(seed, line, alongZ, along),
                bedY(seed, line, alongZ, x, z));
    }

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line northSouth = canalAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line eastWest = canalAt(zLines, z);
                if (northSouth == null && eastWest == null) continue;
                Cross a = channel(plan.seed, northSouth, true, x, z);
                Cross b = channel(plan.seed, eastWest, false, x, z);
                boolean aWet = a != null && a.edge <= 0;
                boolean bWet = b != null && b.edge <= 0;
                boolean bridge = (northSouth != null && crossingRoad(zLines, z))
                        || (eastWest != null && crossingRoad(xLines, x));
                if (aWet || bWet) {
                    // At intersections use the deeper of the two incoming riverbeds,
                    // making confluences continuous rather than a raised dam.
                    int bed = aWet && bWet ? Math.min(a.bed, b.bed) : (aWet ? a.bed : b.bed);
                    long h = CityHash.of(plan.seed, BED, x, z);
                    c.set(x, bed, z, CityHash.range(h, 11) == 0 ? Blocks.GRAVEL
                            : CityHash.range(h >>> 8, 9) == 0 ? Blocks.CLAY : Blocks.MUD);
                    c.fill(x, bed + 1, z, x, WATER_TOP, z, Blocks.WATER);
                    c.fill(x, 62, z, x, 64, z, Blocks.AIR);
                    if (bridge) {
                        // Preserve the existing live street above an open culvert.
                        c.set(x, 63, z, Blocks.DEEPSLATE_TILES);
                        c.set(x, 64, z, Blocks.GRAY_CONCRETE);
                    }
                    continue;
                }
                if (bridge) continue; // The approaches retain their stable road deck.
                Cross bank = a == null ? b : b == null ? a : a.edge <= b.edge ? a : b;
                int edge = bank.edge;
                if (edge <= 0) continue;
                District district = plan.districtAt(x, z);
                long detail = CityHash.of(plan.seed, COAST + bank.line.index(),
                        Math.floorDiv(bank.alongZ ? z : x, 48), bank.signed > 0 ? 1 : -1);
                // A few stretches are repaired municipal embankments; others are
                // sloped earth. Curved banks are visibly different on each side.
                boolean engineered = (district == District.INDUSTRIAL || district == District.WATERFRONT)
                        ? CityHash.range(detail, 5) != 0
                        : district == District.MARKET && CityHash.range(detail, 4) == 0;
                int wobble = CityHash.range(detail >>> 17, 4) == 0 ? 1 : 0;
                int top;
                if (engineered) {
                    // Real quay walls sit high above the water rather than being
                    // artificial sandy slopes; a maintenance shelf interrupts them.
                    top = edge == 1 && CityHash.range(detail, 3) == 0 ? 62 : 64;
                } else {
                    // Y61 wet beach, Y62 gravel shelf, Y63 embankment, Y64 street.
                    // On selected bends one additional block is eroded or raised.
                    top = Math.min(64, 60 + Math.min(4, edge) + (edge < 4 ? wobble : 0));
                    if (edge >= 4) top = 64;
                }
                Block bankBlock = engineered ? quayStone(detail, x, z)
                        : naturalBank(detail, edge, top, x, z);
                c.fill(x, Math.max(57, top - 2), z, x, top, z, bankBlock);
                if (top < 64) c.fill(x, top + 1, z, x, 64, z, Blocks.AIR);
                // Sparse two-high reeds at genuinely wet soil; no carpets of weeds
                // and no plants on retaining walls, bridges or sewage channels.
                if (!engineered && edge == 1 && top == 61
                        && (bankBlock == Blocks.MUD || bankBlock == Blocks.COARSE_DIRT)
                        && CityHash.range(CityHash.of(plan.seed, COAST + 9, x, z), 49) == 0) {
                    c.set(x, 62, z, Blocks.SHORT_GRASS); // above damp earth, never a solid obstruction
                }
            }
        }
    }

    private static Block naturalBank(long detail, int edge, int top, int x, int z) {
        long h = CityHash.of(detail, x, z);
        int roll = CityHash.range(h, 13);
        if (top <= 62) {
            if (roll <= 4) return Blocks.MUD;
            if (roll <= 8) return Blocks.GRAVEL;
            if (roll <= 10) return Blocks.CLAY;
            return Blocks.COARSE_DIRT;
        }
        if (edge >= 3) {
            if (roll <= 4) return Blocks.PACKED_MUD;
            if (roll <= 8) return Blocks.COARSE_DIRT;
            if (roll <= 10) return Blocks.GRAVEL;
            return Blocks.MOSSY_COBBLESTONE;
        }
        return roll % 2 == 0 ? Blocks.GRAVEL : Blocks.MUD;
    }

    private static Block quayStone(long detail, int x, int z) {
        int roll = CityHash.range(CityHash.of(detail, x, z), 11);
        if (roll < 2) return Blocks.MOSSY_STONE_BRICKS;
        if (roll < 5) return Blocks.CRACKED_STONE_BRICKS;
        if (roll == 10) return Blocks.WAXED_WEATHERED_CUT_COPPER;
        return Blocks.STONE_BRICKS;
    }

    private static Line canalAt(List<Line> lines, int coord) {
        for (Line line : lines) if (line.kind() == Line.Kind.CANAL &&
                Math.abs(coord - line.center()) <= line.halfWidth()) return line;
        return null;
    }

    private static boolean crossingRoad(List<Line> lines, int coord) {
        for (Line line : lines) if (line.kind() != Line.Kind.CANAL && line.contains(coord)) return true;
        return false;
    }
}
