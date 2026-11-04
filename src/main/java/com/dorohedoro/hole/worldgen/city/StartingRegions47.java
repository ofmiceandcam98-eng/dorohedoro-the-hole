package com.dorohedoro.hole.worldgen.city;

/**
 * Seed-selected first neighborhood for newly created worlds. The actual region
 * probe uses the same Minecraft climate sampler as biome generation; this pure
 * selector only chooses candidate superblocks, and can be tested with javac.
 * No player can be placed into an unexamined random wilderness block.
 */
public final class StartingRegions47 {
    public enum Origin { CITY, SNOWFIELDS, DEAD_TAIGA, DUST_BASIN, IRON_BADLANDS, MOUNTAIN_RIM }
    public record Site(int blockI, int blockJ, Origin origin) {
        public boolean downtown() { return blockI == -1 && blockJ == -1; }
    }
    @FunctionalInterface
    public interface Probe { boolean eligible(int blockI, int blockJ, Origin preferred); }

    private static final long START_SALT = 0x47A701L;
    public static final int MAX_ATTEMPTS = 1024;
    private StartingRegions47() {}

    public static Origin preferred(long seed) {
        return Origin.values()[CityHash.range(CityHash.of(seed, START_SALT), Origin.values().length)];
    }

    /**
     * Pass 50, v33+ only: still prefer the seed's selected climate, but don't
     * discard every alternate frontier climate when that one is unsuitable.
     * Keep downtown starts deliberately possible when requested. No world-save
     * writes or runtime RNG; older versions continue to call choose().
     */
    public static Site chooseWithRegionalFallback(long seed, Probe probe) {
        Origin requested = preferred(seed);
        if (requested == Origin.CITY) return new Site(-1, -1, Origin.CITY);
        Site initial = choose(seed, probe);
        if (!initial.downtown()) return initial;
        // Rotate deterministically through the other four frontier types.
        // Avoid an unbounded expensive search through climate samples.
        int first = requested.ordinal();
        for (int step = 1; step < Origin.values().length - 1; step++) {
            Origin alternate = Origin.values()[1 + ((first - 1 + step) % (Origin.values().length - 1))];
            Site site = find(seed, alternate, probe, MAX_ATTEMPTS / 4);
            if (site != null) return site;
        }
        return new Site(-1, -1, Origin.CITY);
    }

    private static Site find(long seed, Origin requested, Probe probe, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            long hx = CityHash.of(seed ^ START_SALT, requested.ordinal() + 1L, attempt);
            long hz = CityHash.of(seed ^ (START_SALT << 1), requested.ordinal() + 19L, attempt);
            int i = CityHash.range(hx, 67) - 33;
            int j = CityHash.range(hz, 75) - 37;
            if (Math.max(Math.abs(i), Math.abs(j)) < 17) continue;
            if (probe.eligible(i, j, requested)) return new Site(i, j, requested);
        }
        return null;
    }

    /** Exactly one eligible, repeatable site per seed; unavailable regions safely fall back to the old plaza. */
    public static Site choose(long seed, Probe probe) {
        Origin requested = preferred(seed);
        if (requested == Origin.CITY) return new Site(-1, -1, Origin.CITY);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            long hx = CityHash.of(seed ^ START_SALT, requested.ordinal() + 1L, attempt);
            long hz = CityHash.of(seed ^ (START_SALT << 1), requested.ordinal() + 19L, attempt);
            int i = CityHash.range(hx, 67) - 33;
            int j = CityHash.range(hz, 75) - 37;
            if (Math.max(Math.abs(i), Math.abs(j)) < 17) continue;
            if (probe.eligible(i, j, requested)) return new Site(i, j, requested);
        }
        return new Site(-1, -1, Origin.CITY);
    }
}
