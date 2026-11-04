package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 43: repair history of the narrow LOCAL lanes between actual parcels.
 * Existing rail, canal, junction and diagonal-boulevard right-of-way are not
 * painted here; the sidewalk is never obstructed with extra items or lights.
 * Coherent 6x9- and 19x23-block repairs break the unbroken asphalt sheet
 * visible in the Nov 1 Minecraft screenshots, while leaving traversability
 * unchanged and sewer shafts accessible for the later shaft painter.
 */
final class Backstreets43 {
    private Backstreets43() {}

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines,
                      List<Lot> lots) {
        if (plan.settings.worldDetailVersion() < 27) return;
        for (int x = c.minX; x <= c.maxX; x++) {
            for (int z = c.minZ; z <= c.maxZ; z++) {
                if (plan.settings.worldDetailVersion() >= 35
                        ? UrbanPattern52.at(plan.seed, x, z) != UrbanPattern52.Fabric.DENSE
                        : plan.regionAt(x, z) != HoleRegions42.Region.CITY) continue;
                if (inLot(lots, x, z) || arterial(plan, xLines, zLines, x, z)) continue;
                // No edit to standing infrastructure or water at the walking Y.
                if (!c.get(x, CityPlan.GROUND_Y, z).isAir()) continue;
                long broad = CityHash.of(plan.seed ^ 0x4357A1L,
                        Math.floorDiv(x, 19), Math.floorDiv(z, 23));
                long medium = CityHash.of(plan.seed ^ 0x4357A2L,
                        Math.floorDiv(x, 6), Math.floorDiv(z, 9));
                long grains = CityHash.of(plan.seed ^ 0x4357A3L, x, z);
                int aged = CityHash.range(broad, 7);
                int patched = CityHash.range(medium, 7);
                if (aged < 2 && patched < 5) {
                    Block material = switch (plan.districtAt(x, z)) {
                        case MARKET, RESIDENTIAL -> patched < 2 ? Blocks.CRACKED_STONE_BRICKS : Blocks.COBBLED_DEEPSLATE;
                        case INDUSTRIAL, WATERFRONT -> patched < 2 ? Blocks.TUFF_BRICKS : Blocks.POLISHED_BLACKSTONE;
                        case DERELICT, GRAVEYARD -> patched < 3 ? Blocks.PACKED_MUD : Blocks.GRAVEL;
                        case OUTSKIRTS -> patched < 3 ? Blocks.COARSE_DIRT : Blocks.COBBLESTONE;
                    };
                    c.set(x, CityPlan.STREET_Y, z, material);
                } else if (aged >= 4 && patched == 0 && CityHash.range(grains, 5) != 0) {
                    // Municipal strip repairs in otherwise dirty lanes.
                    c.set(x, CityPlan.STREET_Y, z, Blocks.CRACKED_DEEPSLATE_BRICKS);
                }
            }
        }
    }

    private static boolean inLot(List<Lot> lots, int x, int z) {
        for (Lot lot : lots) if (lot.rect().contains(x, z)) return true;
        return false;
    }

    private static boolean arterial(CityPlan plan, List<Line> xs, List<Line> zs, int x, int z) {
        for (Line l : xs) if (Math.abs(x - l.center() - plan.xAvenueOffset(l, z)) <= l.halfWidth()) return true;
        for (Line l : zs) if (Math.abs(z - l.center() - plan.zAvenueOffset(l, x)) <= l.halfWidth()) return true;
        return false;
    }
}
