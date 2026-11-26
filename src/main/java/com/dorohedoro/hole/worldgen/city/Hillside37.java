package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 37: eroded relief ONLY on sufficiently large undeveloped parcels.
 * This is not the final citywide elevation system. Roads, canals, bridge
 * approaches, building foundations and the spawn quarter retain their
 * existing geometric profile until a hydraulics/accessibility test is ready.
 *
 * Unlike a decorative pile, the height field is sampled from world coordinates
 * plus the immutable lot hash, so every chunk paints the same hillside edge.
 */
final class Hillside37 {
    private Hillside37() {}

    static int height(Rect r, long hash, int x, int z) {
        if (!r.contains(x, z) || r.width() < 20 || r.depth() < 20) return 0;
        // Keep the perimeter accessible at the road's current grade.
        int edge = Math.min(Math.min(x - r.x0(), r.x1() - x),
                Math.min(z - r.z0(), r.z1() - z));
        if (edge < 2) return 0;
        double u = (x - r.x0()) / (double) Math.max(1, r.width() - 1);
        double v = (z - r.z0()) / (double) Math.max(1, r.depth() - 1);
        // The ridge is biased to one end of the parcel so hills don't appear as
        // identical symmetrical pyramid-shaped mounds.
        double drift = (CityHash.range(hash >>> 18, 1201) - 600) / 2400.0;
        double ridge = Math.sin(Math.PI * u) * Math.sin(Math.PI * Math.max(0, Math.min(1, v + drift * (u - .5))));
        int amplitude = Math.min(7, 4 + CityHash.range(hash >>> 7, 4));
        int raw = (int) Math.round(amplitude * Math.max(0, ridge));
        // Never create a two-block step against a level sidewalk.
        return Math.min(raw, Math.max(0, edge - 1));
    }

    /** Returns true only if this parcel was actually converted to a hillside. */
    static boolean vacant(Canvas c, Lot lot) {
        Rect r = lot.rect();
        if (!c.hasHillside37() || r.width() < 20 || r.depth() < 20) return false;
        if (Math.max(Math.abs(r.centerX()), Math.abs(r.centerZ())) < 128) return false;
        if (CityHash.range(lot.hash() >>> 24, 3) == 0) return false;
        c.fill(r, CityPlan.STREET_Y - 1, CityPlan.STREET_Y - 1, Blocks.DIRT);
        for (int x = Math.max(r.x0(), c.minX); x <= Math.min(r.x1(), c.maxX); x++) {
            for (int z = Math.max(r.z0(), c.minZ); z <= Math.min(r.z1(), c.maxZ); z++) {
                int h = height(r, lot.hash(), x, z);
                if (h > 0) c.fill(x, CityPlan.STREET_Y, z, x, CityPlan.STREET_Y + h - 1, z,
                        h > 3 ? Blocks.PACKED_MUD : Blocks.DIRT);
                long noise = CityHash.of(lot.hash() ^ 0x37A7731L, x, z);
                int n = CityHash.range(noise, 17);
                Block surface = h >= 4
                        ? (n < 3 ? Blocks.COBBLESTONE : n < 9 ? Blocks.GRAVEL : Blocks.COARSE_DIRT)
                        : (n < 4 ? Blocks.GRAVEL : n < 12 ? Blocks.COARSE_DIRT : Blocks.PODZOL);
                // Narrow eroded service walk along one parcel edge. It links
                // the surrounding streets and never becomes a chest-lined alley.
                if (x == r.x0() + 1 || x == r.x0() + 2) surface = Blocks.PACKED_MUD;
                c.set(x, CityPlan.STREET_Y + h, z, surface);
                if (h <= 2 && CityHash.range(noise >>> 20, 67) == 0
                        && x > r.x0() + 3 && x < r.x1() - 2 && z > r.z0() + 2 && z < r.z1() - 2) {
                    c.set(x, CityPlan.GROUND_Y + h, z, Blocks.DEAD_BUSH);
                }
            }
        }
        return true;
    }
}
