package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Pass 65: rare, genuinely broad ocean basins beyond possible first neighborhoods.
 * Deterministic from Minecraft's seeded climate sampler and absolute coordinates.
 * Vanilla ocean biome IDs are used so native shipwreck/monument/ocean-ruin rules can apply.
 * No water is placed by this policy; OceanBathymetry65 shapes that water in buildSurface.
 */
public final class OceanRegions65 {
    public static final int CELL = 2048;
    public static final int SEA_SURFACE_Y = 62;
    public static final int LANDING_EXCLUSION = 5200;
    private static final int BASIN_CENTER_EXCLUSION = 6400;
    private static final int MAX_RADIUS = 1040;
    private static final long SALT = 0x65A0CE9L;

    private OceanRegions65() {}

    /** Zero on land; 1..30 in open water. Ocean beds are SEA_SURFACE_Y - depth. */
    public static int depthAt(Climate.Sampler climate, int x, int z) {
        if (Math.max(Math.abs((long) x), Math.abs((long) z)) < LANDING_EXCLUSION) return 0;
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        int depth = 0;
        // Only the central and immediately adjacent basin centers can reach this column.
        // The inexpensive distance rejection avoids unnecessary climate samples.
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
            for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
                int bx = cx * CELL + CELL / 2;
                int bz = cz * CELL + CELL / 2;
                if (Math.max(Math.abs((long) bx), Math.abs((long) bz)) < BASIN_CENTER_EXCLUSION) continue;
                double dx = (double) x - bx;
                double dz = (double) z - bz;
                if (Math.abs(dx) > MAX_RADIUS || Math.abs(dz) > MAX_RADIUS) continue;
                long salt = cachedBasinSalt(climate, cx, cz, bx, bz);
                if (CityHash.range(salt, 4) != 0) continue;
                int radius = 760 + CityHash.range(salt >>> 7, 280);
                // Low-amplitude coastline undulation. Never slice the playable deep-ocean center.
                double angle = Math.atan2(dz, dx);
                double shore = radius + 32.0 * Math.sin(angle * 5.0 + (salt & 15))
                        + 21.0 * Math.cos(angle * 9.0 + ((salt >>> 8) & 15));
                double distance = Math.hypot(dx, dz);
                if (distance >= shore) continue;
                double t = Math.min(1.0, (shore - distance) / 390.0);
                int candidate = 1 + (int) Math.round(29.0 * t * t * (3.0 - 2.0 * t));
                depth = Math.max(depth, candidate);
            }
        }
        return depth;
    }

    /**
     * Basin salts depend only on the world and the basin cell, but computing one takes three climate samples, and
     * coastline lookups ask for the same handful of basins thousands of times per structure search. This cache
     * returns identical values, so generation is unchanged. It only removes the repeated sampling that made
     * /locate, explorer maps and dolphins stall for minutes.
     */
    private static final java.util.concurrent.ConcurrentHashMap<Long, Long> BASIN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int BASIN_CACHE_LIMIT = 262_144;
    private static final ThreadLocal<Object[]> LAST_WORLD = new ThreadLocal<>();

    /** A cheap identity for the world behind a sampler, remembered per thread for the sampler object in use. */
    private static long worldFingerprint(Climate.Sampler climate) {
        Object[] last = LAST_WORLD.get();
        if (last != null && last[0] == climate) {
            return (Long) last[1];
        }
        DensityFunction.SinglePointContext probe = new DensityFunction.SinglePointContext(104_729, 0, -130_363);
        long fingerprint = Double.doubleToLongBits(climate.temperature().compute(probe))
                ^ Long.rotateLeft(Double.doubleToLongBits(climate.continentalness().compute(probe)), 29);
        LAST_WORLD.set(new Object[]{climate, fingerprint});
        return fingerprint;
    }

    private static long cachedBasinSalt(Climate.Sampler climate, int cx, int cz, int bx, int bz) {
        long key = CityHash.of(worldFingerprint(climate), cx, cz);
        Long cached = BASIN_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        long salt = basinSalt(climate, cx, cz, bx, bz);
        if (BASIN_CACHE.size() > BASIN_CACHE_LIMIT) {
            BASIN_CACHE.clear();
        }
        BASIN_CACHE.put(key, salt);
        return salt;
    }

    private static long basinSalt(Climate.Sampler climate, int cx, int cz, int bx, int bz) {
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(bx, 0, bz);
        long continent = Double.doubleToLongBits(climate.continentalness().compute(point));
        long humidity = Double.doubleToLongBits(climate.humidity().compute(point));
        long temp = Double.doubleToLongBits(climate.temperature().compute(point));
        return CityHash.of(CityHash.mix(continent ^ Long.rotateLeft(humidity, 19)
                ^ Long.rotateLeft(temp, 37) ^ SALT), cx, cz);
    }
}
