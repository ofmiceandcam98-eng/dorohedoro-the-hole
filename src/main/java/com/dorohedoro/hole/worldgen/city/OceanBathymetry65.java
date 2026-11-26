package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

/** Sea-floor and water realization of OceanRegions65; no new poison, mobs, items or mechanics.
 * Uses raw world Y (not graded city Y) so the true surface is always 62.
 * Called after the urban painter so no road/lot can bridge an ocean column.
 * The normal structure protection on Canvas remains in force.
 */
public final class OceanBathymetry65 {
    private static final int CLEAR_TOP = 176;
    private static final int STONE_FLOOR_THICKNESS = 3;
    private OceanBathymetry65() {}

    public static void paint(CityPlan plan, ChunkAccess chunk,
                             List<VanillaStructureBuffer64.Box> structures) {
        if (plan.settings.worldDetailVersion() < 47) return;
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        byte[] depths = new byte[256];
        byte[] coast = new byte[256];
        boolean hasOcean = false;
        boolean hasCoast = false;
        for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
            int depth = plan.oceanDepthAt(minX + dx, minZ + dz);
            int index = dx + (dz << 4);
            depths[index] = (byte) depth;
            hasOcean |= depth > 0;
            if (depth == 0 && plan.settings.worldDetailVersion() >= 48) {
                int shoreDistance = plan.coastalDistanceAt(minX + dx, minZ + dz);
                if (shoreDistance > 0) {
                    coast[index] = (byte) shoreDistance;
                    hasCoast = true;
                }
            }
        }
        if (!hasOcean && !hasCoast) return; // Avoid a second Canvas for inland chunks.
        Canvas canvas = new Canvas(chunk, plan, structures);
        for (int x = canvas.minX; x <= canvas.maxX; x++) {
            for (int z = canvas.minZ; z <= canvas.maxZ; z++) {
                int depth = depths[(x - minX) + ((z - minZ) << 4)] & 0xff;
                if (depth <= 0) {
                    int shoreDistance = coast[(x - minX) + ((z - minZ) << 4)] & 0xff;
                    if (shoreDistance > 0) {
                        // Deliberately uninhabited but useful for early survival: vanilla sand,
                        // gravel, shore dirt; a natural route down to boat-accessible water.
                        // All raw writes honor Pass64 protected vanilla structures.
                        int dryY = CoastalShelf66.drySurfaceY(shoreDistance);
                        for (int y = 57; y < dryY; y++) canvas.setRaw(x, y, z, Blocks.STONE);
                        canvas.setRaw(x, dryY, z, shoreDistance <= CoastalShelf66.BEACH_WIDTH
                                ? Blocks.SAND : shoreDistance <= 11 ? Blocks.GRAVEL : Blocks.COARSE_DIRT);
                        for (int y = dryY + 1; y <= CLEAR_TOP; y++) canvas.setRaw(x, y, z, Blocks.AIR);
                    }
                    continue;
                }
                int bed = OceanRegions65.SEA_SURFACE_Y - depth;
                // Retain deep mining caves and ore below this shallow sea-floor cap.
                for (int y = Math.max(28, bed - STONE_FLOOR_THICKNESS); y < bed; y++) {
                    canvas.setRaw(x, y, z, Blocks.STONE);
                }
                canvas.setRaw(x, bed, z, depth < 6 ? Blocks.GRAVEL :
                        depth < 15 ? Blocks.CLAY : Blocks.GRAVEL);
                for (int y = bed + 1; y <= OceanRegions65.SEA_SURFACE_Y; y++) {
                    canvas.setRaw(x, y, z, Blocks.WATER);
                }
                // Erase city surface, rooftops and ordinary vanilla hills from open water.
                // Tall exceptional terrain above this bound needs client verification.
                for (int y = OceanRegions65.SEA_SURFACE_Y + 1; y <= CLEAR_TOP; y++) {
                    canvas.setRaw(x, y, z, Blocks.AIR);
                }
            }
        }
    }
}
