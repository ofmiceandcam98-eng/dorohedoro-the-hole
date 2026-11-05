package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Blocks;

/**
 * Small, chunk-clipped canal-bed ecology for new worldgen profiles. This is not
 * surface farmland or an underwater fantasy forest: patches of silt, submerged
 * weeds and occasional leaf litter give fish places to hide without closing the
 * channel, changing the water level, or blocking culvert entrances.
 *
 * Decorative changes are saved-world gated and fish are spawned separately by
 * CityPopulator. Every decision uses absolute coordinates + seed only.
 */
final class CanalEcology24 {
    private static final long SALT = 0xCA4A124L;

    private CanalEcology24() {}

    static void paint(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line xBand = canalAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line zBand = canalAt(zLines, z);
                boolean xWater = xBand != null && (plan.settings.sewerCavernVersion() >= 5
                        ? RiverChannels24.water(plan.seed, xBand, true, x, z)
                        : Math.abs(x - xBand.center()) <= 4);
                boolean zWater = zBand != null && (plan.settings.sewerCavernVersion() >= 5
                        ? RiverChannels24.water(plan.seed, zBand, false, x, z)
                        : Math.abs(z - zBand.center()) <= 4);
                if (!xWater && !zWater) {
                    // Very sparse service/fishing stations on the quay. These are
                    // usable barrels, not decorative fake entities; roads remain
                    // at least two blocks clear beside the canal railing.
                    boolean xQuay = xBand != null && Math.abs(x - xBand.center()) == 7;
                    boolean zQuay = zBand != null && Math.abs(z - zBand.center()) == 7;
                    if (xQuay != zQuay) {
                        int along = xQuay ? z : x;
                        long marker = CityHash.of(plan.seed, SALT+2,
                                xQuay ? xBand.center() : zBand.center(), Math.floorDiv(along,112));
                        int signed = xQuay ? x-xBand.center() : z-zBand.center();
                        if (Math.floorMod(along,112) == 36
                                && signed == (CityHash.range(marker,2)==0 ? 7 : -7)
                                && c.get(x,65,z).isAir() && !c.get(x,64,z).isAir()) {
                            c.set(x,65,z,Blocks.SPRUCE_FENCE);
                            c.set(x,66,z,Blocks.LANTERN);
                            int bx = xQuay ? x : x+2;
                            int bz = xQuay ? z+2 : z;
                            // v19: supply barrels must rest on a real quay foundation.
                            // v0-v18 keep their existing per-save generation exactly.
                            if (c.contains(bx, bz) && c.get(bx,65,bz).isAir()
                                    && CityEcologyPolicy32.canPlaceCanalSupply(
                                        plan.settings.worldDetailVersion(), !c.get(bx,64,bz).isAir()))
                                c.barrel(bx,65,bz,"canal_supply");
                        }
                    }
                    continue;
                }
                // Shallows have a higher Y59 bed; look at real water above it,
                // rather than relying on the legacy flat Y57 canal floor.
                if (!c.get(x, 61, z).is(Blocks.WATER)) continue;
                int d = xWater ? (plan.settings.sewerCavernVersion() >= 5
                            ? RiverChannels24.distance(plan.seed,xBand,true,x,z) : Math.abs(x-xBand.center()))
                        : (plan.settings.sewerCavernVersion() >= 5
                            ? RiverChannels24.distance(plan.seed,zBand,false,x,z) : Math.abs(z-zBand.center()));
                long h = CityHash.of(plan.seed, SALT, x, z);
                // Plant against the *actual* varying riverbed, avoiding floating
                // moss patches when the river becomes shallow beside its shores.
                int bedY = 57;
                if (plan.settings.sewerCavernVersion() >= 5) {
                    int aBed = xWater ? RiverChannels24.bedY(plan.seed,xBand,true,x,z) : 99;
                    int bBed = zWater ? RiverChannels24.bedY(plan.seed,zBand,false,x,z) : 99;
                    bedY = Math.min(aBed,bBed);
                }
                if (CityHash.range(h, 29) == 0) c.set(x, bedY, z, Blocks.MOSS_BLOCK);
                else if (CityHash.range(h, 11) == 0) c.set(x, bedY, z, Blocks.MUD);

                // A few isolated weed tufts, never a solid carpet over the bed.
                // Vanilla seagrass contains source water and is swim-through.
                if (CityHash.range(h >>> 8, 37) == 0 && d <= 3 && bedY <= 57) {
                    c.set(x, bedY + 1, z, Blocks.SEAGRASS);
                }
                // Lily pads sit on top of open water; don't put them under bridges.
                if (d >= 2 && d <= 4 && CityHash.range(h >>> 16, 180) == 0
                        && c.get(x, 62, z).isAir() && c.get(x, 61, z).is(Blocks.WATER)) {
                    c.set(x, 62, z, Blocks.LILY_PAD);
                }
            }
        }
    }

    private static Line canalAt(List<Line> bands, int coordinate) {
        for (Line band : bands) if (band.kind() == Line.Kind.CANAL && band.contains(coordinate)) return band;
        return null;
    }
}
