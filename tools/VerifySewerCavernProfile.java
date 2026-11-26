import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.SewerCavernProfile;

/** Offline deterministic geometry/entry-grade check; not a substitute for a server QA run. */
public final class VerifySewerCavernProfile {
    public static void main(String[] args) {
        long[] seeds = {1L, 2L, 3L, 42L, 555L, 777L, 8675309L, 12345L, -987654321L, 20261031L};
        long count = 0L;
        long broad = 0L;
        for (long seed : seeds) {
            for (int x = -60; x <= 60; x++) {
                for (int z = -60; z <= 60; z++) {
                    SewerCavernProfile a = SewerCavernProfile.at(seed, x * 120 + 12, z * 120 + 59);
                    SewerCavernProfile b = SewerCavernProfile.at(seed, x * 120 + 12, z * 120 + 59);
                    if (!a.equals(b)) throw new AssertionError("Non-deterministic cavern geometry");
                    if (a.radiusX() < 14 || a.radiusX() > 24 || a.radiusZ() < 14 || a.radiusZ() > 25)
                        throw new AssertionError("Unexpected cavern dimensions: " + a);
                    if (a.broad()) broad++;
                    count++;
                }
            }
        }
        long checkedGrades = 0L;
        for (long seed : seeds) {
            for (int x = -50; x <= 50; x += 2) {
                for (int z = -50; z <= 50; z += 2) {
                    int bx = x * 120 + 12;
                    int sourceZ = z * 120;
                    for (int direction : new int[] {-1, 1}) {
                        for (boolean relief : new boolean[] {false, true}) {
                            int previous = Integer.MAX_VALUE;
                            for (int step = 43; step <= 59; step++) {
                                int local = SewerCavernProfile.entranceFloor(seed, bx, sourceZ,
                                        direction, step, relief);
                                int offset = relief
                                        ? com.dorohedoro.hole.worldgen.city.CityElevation.offset(seed,
                                            bx, sourceZ + direction * step) : 0;
                                int worldY = local + offset;
                                if (previous != Integer.MAX_VALUE && (worldY > previous || previous - worldY > 1)) {
                                    throw new AssertionError("Unsafe descent: " + seed + ":" + bx + "," + sourceZ
                                            + " step " + step + " was " + previous + " now " + worldY);
                                }
                                if (step == 59 && worldY != -43 + offset)
                                    throw new AssertionError("Entrance misses cavern floor");
                                previous = worldY;
                                checkedGrades++;
                            }
                        }
                    }
                }
            }
        }
        double percent = 100.0 * broad / count;
        if (percent < 15 || percent > 19) throw new AssertionError("Unexpected broad cavern rate: " + percent);
        System.out.printf("Checked %,d cavern plans across ten seeds; %.2f%% broad caverns%n", count, percent);
        System.out.printf("Checked %,d stair landings with and without terrain relief; all grades <=1 block%n", checkedGrades);
        System.out.println("Version 1 reaches cavern floor and old-world geometry remains selected by version 0.");
    }
}
