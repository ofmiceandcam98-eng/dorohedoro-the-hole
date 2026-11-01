import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.SewerBranchPolicy;

/** Standalone deterministic sewer-selector regression, without Minecraft dependencies. */
public final class VerifySewerBranchFrequency {
    private static final long SALT = 0x5E4E4D2026L;

    public static void main(String[] args) {
        long[] seeds = {1L, 2L, 3L, 42L, 555L, 777L, 8675309L, 12345L, -987654321L, 20261031L};
        long[] counts = new long[3];
        long[] legacy = new long[3];
        long cases = 0L;
        for (long seed : seeds) {
            for (int x = -60; x <= 60; x++) {
                for (int z = -60; z <= 60; z++) {
                    int original = x == 0 && z == 0 ? 0 : CityHash.range(CityHash.of(seed, SALT, x, z), 11);
                    int old = SewerBranchPolicy.kind(seed, x, z);
                    int oldWithProfile = SewerBranchPolicy.kind(seed, x, z, 9);
                    if (old != original || oldWithProfile != original)
                        throw new AssertionError("Older save branch changed at " + seed + ":" + x + "," + z);
                    legacy[original == 0 ? 0 : (original <= 2 ? 1 : 2)]++;
                    int kind = SewerBranchPolicy.kind(seed, x, z, 30);
                    if (kind < 0 || kind > 3) throw new AssertionError("Bad branch kind: " + kind);
                    if (SewerBranchPolicy.kind(seed, x, z, 30) != kind)
                        throw new AssertionError("Non-deterministic branch");
                    if (SewerBranchPolicy.connectsToCave(seed, x, z, 30) != (kind == 0))
                        throw new AssertionError("Wrong cave predicate");
                    int direction = SewerBranchPolicy.direction(seed, x, z);
                    if (direction != -1 && direction != 1) throw new AssertionError("Bad direction");
                    counts[kind == 0 ? 0 : (kind <= 2 ? 1 : 2)]++;
                    cases++;
                }
            }
            if (!SewerBranchPolicy.connectsToCave(seed, 0, 0, 30))
                throw new AssertionError("Starter cave route disappeared");
        }
        double cave = 100.0 * counts[0] / cases;
        double inspection = 100.0 * counts[1] / cases;
        double normal = 100.0 * counts[2] / cases;
        if (cave < 28.5 || cave > 31.5 || inspection < 16.5 || inspection > 19.5 || normal < 50.0 || normal > 53.5)
            throw new AssertionError("Unexpected distribution");
        if (legacy[0] >= counts[0]) throw new AssertionError("Cave routes not increased");
        System.out.printf("Validated %,d decisions across %d seeds%n", cases, seeds.length);
        System.out.printf("Legacy: caves %.2f%%, inspection %.2f%%, normal %.2f%%%n",
                100.0 * legacy[0] / cases, 100.0 * legacy[1] / cases, 100.0 * legacy[2] / cases);
        System.out.printf("New: caves %.2f%%, inspection %.2f%%, normal %.2f%%%n", cave, inspection, normal);
        System.out.println("Legacy layout preserved, deterministic new selector, starter cave, and cave transitions unchanged.");
    }
}
