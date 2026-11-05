package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Rare functional riverside handling points. These use the existing dry quay,
 * not channel water, keep all bridge junctions clear, and introduce no chests,
 * new lighting or artificial structures that cannot be reached on foot.
 * A municipal quay has a reason to exist: fish smoking, metal maintenance,
 * or refuse processing along an otherwise dead riverbank.
 */
final class CanalWorksites43 {
    private static final int SPACING = 112;
    private CanalWorksites43() {}

    static void paint(Canvas c, CityPlan plan, List<Line> xs, List<Line> zs) {
        if (plan.settings.worldDetailVersion() < 27 || plan.settings.sewerCavernVersion() < 5) return;
        for (Line canal : xs) {
            if (canal.kind() != Line.Kind.CANAL) continue;
            for (int section = Math.floorDiv(c.minZ - SPACING, SPACING);
                 section <= Math.floorDiv(c.maxZ + SPACING, SPACING); section++) {
                long h = CityHash.of(plan.seed ^ 0x43CA111L, canal.index(), section);
                int along = section * SPACING + 25 + CityHash.range(h, 47);
                if (Math.abs(along - c.minZ) > SPACING + 16 && Math.abs(along - c.maxZ) > SPACING + 16) continue;
                if (nearCrossing(zs, along, 24)) continue;
                int side = CityHash.range(h >>> 18, 2) == 0 ? -1 : 1;
                int cross = canal.center() + RiverChannels24.centerOffset(plan.seed, canal, true, along)
                        + side * (RiverChannels24.width(plan.seed, canal, true, along) + 6);
                if (Math.abs(cross - canal.center()) > canal.halfWidth() - 3) continue;
                place(c, plan, true, cross, along, side, h);
            }
        }
        for (Line canal : zs) {
            if (canal.kind() != Line.Kind.CANAL) continue;
            for (int section = Math.floorDiv(c.minX - SPACING, SPACING);
                 section <= Math.floorDiv(c.maxX + SPACING, SPACING); section++) {
                long h = CityHash.of(plan.seed ^ 0x43CA112L, canal.index(), section);
                int along = section * SPACING + 25 + CityHash.range(h, 47);
                if (Math.abs(along - c.minX) > SPACING + 16 && Math.abs(along - c.maxX) > SPACING + 16) continue;
                if (nearCrossing(xs, along, 24)) continue;
                int side = CityHash.range(h >>> 18, 2) == 0 ? -1 : 1;
                int cross = canal.center() + RiverChannels24.centerOffset(plan.seed, canal, false, along)
                        + side * (RiverChannels24.width(plan.seed, canal, false, along) + 6);
                if (Math.abs(cross - canal.center()) > canal.halfWidth() - 3) continue;
                place(c, plan, false, cross, along, side, h);
            }
        }
    }

    private static boolean nearCrossing(List<Line> lines, int along, int buffer) {
        for (Line l : lines) if (Math.abs(l.center() - along) <= l.halfWidth() + buffer) return true;
        return false;
    }

    /**
     * Each column is painted independently so a quay crossing a chunk edge
     * has the exact same footprint regardless of generation order.
     */
    private static void place(Canvas c, CityPlan plan, boolean alongZ, int cross, int along, int side, long h) {
        int x = alongZ ? cross : along;
        int z = alongZ ? along : cross;
        for (int i = -2; i <= 2; i++) {
            int px = x + (alongZ ? 0 : i);
            int pz = z + (alongZ ? i : 0);
            if (!c.contains(px, pz) || c.get(px, CityPlan.STREET_Y, pz).isAir()
                    || !c.get(px, CityPlan.GROUND_Y, pz).isAir()) continue;
            c.set(px, CityPlan.STREET_Y, pz, Blocks.CRACKED_STONE_BRICKS);
        }
        // The existing walkway is left clear on the water side. A single work
        // site is inside the dry quay right-of-way, never a freestanding chest.
        if (!c.contains(x, z) || c.get(x, CityPlan.STREET_Y, z).isAir()
                || !c.get(x, CityPlan.GROUND_Y, z).isAir()
                || !c.get(x, CityPlan.GROUND_Y + 1, z).isAir()) return;
        Block station = switch (CityHash.range(h >>> 24, 3)) {
            case 0 -> Blocks.SMOKER;       // fish-processing point
            case 1 -> Blocks.GRINDSTONE;   // barge/rail repair
            default -> Blocks.COMPOSTER;  // organics collection
        };
        c.set(x, CityPlan.GROUND_Y, z, station);
    }
}
