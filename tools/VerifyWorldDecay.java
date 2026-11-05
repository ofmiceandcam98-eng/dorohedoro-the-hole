package com.dorohedoro.hole.worldgen.city;

/** Standalone checks of the exact production decay selector, with an offline stub District enum. */
public final class VerifyWorldDecay {
    public static void main(String[] args) {
        long[] seeds = {1,2,3,42,515,777,8675309,10101,20261031,-999999};
        int samples = 0;
        for (District district : District.values()) {
            long[] cnt = new long[4];
            for (long seed : seeds) {
                for (int x = -50; x <= 50; x++) {
                    for (int z = -50; z <= 50; z++) {
                        long h = CityHash.of(seed, 0xA5B7L, x, z);
                        int mode = WorldDecayPolicy.severity(h, district);
                        if (mode < 0 || mode > 3 || WorldDecayPolicy.severity(h, district) != mode)
                            throw new AssertionError("Invalid or nondeterministic decay selection");
                        cnt[mode]++;
                        samples++;
                    }
                }
            }
            long total = cnt[0] + cnt[1] + cnt[2] + cnt[3];
            double abandoned = 100.0 * (cnt[2] + cnt[3]) / total;
            double collapsed = 100.0 * cnt[3] / total;
            System.out.printf("%-12s %6.2f%% abandoned or collapsed, %5.2f%% collapsed, %d samples%n",
                    district, abandoned, collapsed, total);
            if (district == District.DERELICT && !(abandoned >= 62 && abandoned <= 68 && collapsed >= 25 && collapsed <= 32))
                throw new AssertionError("Derelict neighbourhoods not broken down enough");
            if (district == District.MARKET && (abandoned > 1 || collapsed > 0))
                throw new AssertionError("Active markets should not look post-apocalyptic");
            if (district == District.RESIDENTIAL && !(abandoned >= 1 && abandoned <= 4))
                throw new AssertionError("Residential abandonment distribution changed");
        }
        System.out.println("World decay deterministic distribution passed: " + samples + " cases across all districts.");
    }
}
