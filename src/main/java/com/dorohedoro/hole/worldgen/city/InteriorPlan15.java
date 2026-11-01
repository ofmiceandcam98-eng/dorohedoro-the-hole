package com.dorohedoro.hole.worldgen.city;

/**
 * Minecraft-free deterministic room selection. Use only per-lot position/seed and storey;
 * do not draw from a shared random stream (chunk construction must be order-independent).
 */
public final class InteriorPlan15 {
    private static final long ROOM_SALT = 0x15C0FFEE77L;
    private InteriorPlan15() {}

    /** 0 tenement cells, 1 dormitory, 2 workshop studios, 3 subdivided family flats. */
    public static int residence(long seed, int x, int z, int storey) {
        return CityHash.range(CityHash.of(CityHash.of(seed, ROOM_SALT, x, z), storey), 4);
    }

    /** 0 groceries, 1 repair trade, 2 food counter, 3 used books/salvage. */
    public static int shop(long seed, int x, int z) {
        return CityHash.range(CityHash.of(seed, ROOM_SALT + 1, x, z), 4);
    }

    /** 0 ward, 1 examination room, 2 laboratory, 3 supply/triage. */
    public static int clinic(long seed, int x, int z, int storey) {
        return CityHash.range(CityHash.of(CityHash.of(seed, ROOM_SALT + 2, x, z), storey), 4);
    }

    /** Safe interior cross corridor must not intersect the north-wall stair flight. */
    public static boolean fitsPartition(Rect room) {
        return room.width() >= 12 && room.depth() >= 9;
    }

    /** For plan variants, corridor remains at north (low z), the tower's stair row. */
    public static int corridorWallZ(Rect room) {
        return room.z0() + 3;
    }
}
