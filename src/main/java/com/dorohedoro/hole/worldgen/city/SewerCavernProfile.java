package com.dorohedoro.hole.worldgen.city;

/**
 * Pure coordinate-hash plan for sewer-linked caverns. No world reads or stateful randomness:
 * the same seed and mouth always yield identical dimensions on every chunk and loading order.
 * Roughly one in six sites is a broad cavern; the others remain modest cave chambers.
 */
public record SewerCavernProfile(int radiusX, int radiusZ, boolean broad) {
    private static final long SALT = 0xC4A1E2026L;

    public static SewerCavernProfile at(long seed, int x, int z) {
        long hash = CityHash.of(seed, SALT, x, z);
        boolean broad = CityHash.range(CityHash.mix(hash), 6) == 0;
        int xRadius = broad ? 20 + CityHash.range(hash, 5) : 14 + CityHash.range(hash, 4);
        int zRadius = broad ? 21 + CityHash.range(CityHash.mix(hash ^ 0x1CE1L), 5)
                : 14 + CityHash.range(CityHash.mix(hash), 5);
        return new SewerCavernProfile(xRadius, zRadius, broad);
    }

    /**
     * Returns a LOCAL generator Y for the finished stair landing. Both endpoints are computed
     * at their real world elevations, then interpolated in world coordinates. Subtracting
     * column relief makes the total drop no more than one block even on district slopes.
     * The earliest landing joins the intact -39 rail passage; the last is at the cavern floor.
     */
    public static int entranceFloor(long seed, int x, int sourceZ, int direction,
                                    int step, boolean terrainRelief) {
        if (step < 43 || step > 59) throw new IllegalArgumentException("Landing step must be 43..59");
        int firstZ = sourceZ + direction * 43;
        int lastZ = sourceZ + direction * 59;
        int start = -39 + (terrainRelief ? CityElevation.offset(seed, x, firstZ) : 0);
        int end = -43 + (terrainRelief ? CityElevation.offset(seed, x, lastZ) : 0);
        int actualY = start + (int) Math.round((end - start) * ((step - 43) / 16.0));
        int offset = terrainRelief ? CityElevation.offset(seed, x, sourceZ + direction * step) : 0;
        return actualY - offset;
    }

    /** New city elevation profile: real stair endpoints and per-column grade
     * must agree with Canvas, otherwise a multi-block subterranean step can
     * be introduced merely by switching world profiles. */
    public static int entranceFloor(CityPlan plan, int x, int sourceZ, int direction, int step) {
        if (step < 43 || step > 59) throw new IllegalArgumentException("Landing step must be 43..59");
        int firstZ = sourceZ + direction * 43;
        int lastZ = sourceZ + direction * 59;
        int start = -39 + CityTerrain40.offset(plan, x, firstZ);
        int end = -43 + CityTerrain40.offset(plan, x, lastZ);
        int actualY = start + (int) Math.round((end - start) * ((step - 43) / 16.0));
        return actualY - CityTerrain40.offset(plan, x, sourceZ + direction * step);
    }
}
