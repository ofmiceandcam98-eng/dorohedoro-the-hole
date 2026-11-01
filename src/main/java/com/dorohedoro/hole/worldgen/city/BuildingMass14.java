package com.dorohedoro.hole.worldgen.city;

/** Pure saved-seed building silhouette selection and staircase-safe cutout geometry. */
public final class BuildingMass14 {
    private BuildingMass14() {}

    /** 0=L-plan, 1=courtyard, 2=stepped terrace, 3=unmodified shell. */
    public static int profile(long hash, Rect b, int floors, boolean garden) {
        if (floors < 3 || b.width() < 19 || b.depth() < 17) return 3;
        int candidate = CityHash.range(CityHash.mix(hash ^ 0x14CAFE714L), 4);
        if (candidate == 1 && (b.width() < 25 || b.depth() < 24)) candidate = 0;
        if (candidate == 2 && garden) candidate = 3;
        return candidate;
    }

    public static Rect lRecess(Rect b) {
        int width = Math.min(8, Math.max(4, b.width() / 3));
        int depth = Math.min(8, Math.max(4, b.depth() / 3));
        return new Rect(b.x1() - width + 1, b.z0(), b.x1(), b.z0() + depth - 1);
    }

    public static Rect courtyard(Rect b) {
        return new Rect(b.x0() + 8, b.z0() + 7, b.x1() - 7, b.z1() - 7);
    }

    public static Rect upperTerrace(Rect b) {
        return new Rect(b.x1() - Math.min(7, b.width() / 3),
                b.z1() - Math.min(7, b.depth() / 3), b.x1(), b.z1());
    }
}
