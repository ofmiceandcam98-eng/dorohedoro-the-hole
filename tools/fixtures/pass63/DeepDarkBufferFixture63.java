import com.dorohedoro.hole.worldgen.city.DeepDarkBuffer63;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class DeepDarkBufferFixture63 {
    private static int checks=0;
    private static void verify(boolean condition, String note) {
        if (!condition) throw new AssertionError(note);
        checks++;
    }
    private static Climate.Sampler fixed(double humidity, double temperature) {
        return new Climate.Sampler(
            p -> humidity,
            p -> temperature
        );
    }
    public static void main(String[] args) {
        verify(DeepDarkBuffer63.matches(-0.23, -0.01), "deep dark criteria");
        verify(!DeepDarkBuffer63.matches(-0.22, -0.01), "strict humid threshold");
        verify(!DeepDarkBuffer63.matches(-0.23, 0.0), "strict warm threshold");
        verify(DeepDarkBuffer63.nearDeepDark(fixed(-0.5, -0.5), 100, -200), "covered region");
        verify(!DeepDarkBuffer63.nearDeepDark(fixed(0.5, 0.5), 100, -200), "uncovered region");
        // An isolated dark biome at an offset neighbor must protect the chunk.
        Climate.Sampler oneNearby = new Climate.Sampler(
            p -> p.x() == -200 + 8 - 48 + 4099 ? -0.5 : 0.5,
            p -> -0.3
        );
        // Transform: sampled coordinate uses z + 4099, not x.
        verify(DeepDarkBuffer63.nearDeepDark(oneNearby, 100, -200), "offset neighborhood sample");
        for (int v : new int[] {0,29,40,44}) {
            for (int y : new int[] {-63, -51, -40, -24, -23, 49, 65}) {
                verify(!DeepDarkBuffer63.protects(v, y, true), "old-save geometry preserved");
            }
        }
        for (int y : new int[] {-63, -51, -40, -24}) {
            verify(DeepDarkBuffer63.protects(45, y, true), "new deep dark buffer");
            verify(!DeepDarkBuffer63.protects(45, y, false), "non-deep-dark terrain unchanged");
        }
        for (int y : new int[] {-23, 0, 12, 49, 65, 99}) {
            verify(!DeepDarkBuffer63.protects(45, y, true), "upper sewer/surface untouched");
        }
        // Seed/co-ordinate independence: fixed climate always gives the same selection.
        for (int x=-512; x<=512; x+=32) for (int z=-512;z<=512;z+=32) {
            verify(DeepDarkBuffer63.nearDeepDark(fixed(-0.4,-0.1), x,z), "global deep dark terrain");
        }
        System.out.println("PASS Pass63 deep-dark buffer Java fixture: "+checks+" checks");
    }
}
