import com.dorohedoro.hole.worldgen.sorcerer.RoadNet;
import com.dorohedoro.hole.worldgen.sorcerer.TownCatalog;
import com.dorohedoro.hole.worldgen.sorcerer.TownFrame;
import com.dorohedoro.hole.worldgen.sorcerer.TownPlanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Offline checks of the Sorcerers' World settlement planner against synthetic terrain. No Minecraft needed.
 *   bash tools/qa-sorcerer-planner.sh
 * Checks: determinism, footprint overlaps, buildings on roads, walk feasibility, road grades, settlement bounds,
 * required services in cities, and how terrain shapes the layout.
 */
public final class TownPlannerFixture {
    static int failures = 0, checks = 0;
    static final int[] servicesSeen = new int[8];

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) {
            failures++;
            if (failures <= 40) System.out.println("FAIL " + what);
        }
    }

    record Land(String name, TownPlanner.Terrain terrain) {}

    public static void main(String[] args) {
        List<Land> lands = List.of(
                new Land("flat", new Terrain(0, 0, 0)),
                new Land("rolling", new Terrain(6, 0, 0)),
                new Land("hillside", new Terrain(3, 0.12, 0)),
                new Land("river", new Terrain(4, 0, 1)),
                new Land("rough", new Terrain(12, 0.05, 0)));
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        Map<String, int[]> totals = new TreeMap<>();
        Map<String, Integer> typeCounts = new TreeMap<>();
        Map<String, Integer> rejected = new TreeMap<>(), downgraded = new TreeMap<>();
        long t0 = System.nanoTime();
        int plans = 0;
        for (Land land : lands) {
            for (TownPlanner.Kind kind : TownPlanner.Kind.values()) {
                for (int s = 0; s < seeds; s++) {
                    long seed = 0x5EED0000L + s * 7919L + kind.ordinal();
                    int cx = 1000 + s * 37, cz = -700 + s * 53;
                    TownPlanner.Layout a = TownPlanner.planViable(seed, cx, cz, kind, land.terrain());
                    TownPlanner.Layout b = TownPlanner.planViable(seed, cx, cz, kind, land.terrain());
                    plans++;
                    String tag = land.name() + "/" + kind + "/" + s;
                    check((a == null) == (b == null), tag + " viability not deterministic");
                    if (a == null) {
                        rejected.merge(land.name() + "/" + kind, 1, Integer::sum);
                        continue;
                    }
                    if (a.kind() != kind) downgraded.merge(land.name() + "/" + kind + "->" + a.kind(), 1, Integer::sum);
                    check(describe(a).equals(describe(b)), tag + " not deterministic");
                    verify(a, land.terrain(), tag, typeCounts);
                    int[] t = totals.computeIfAbsent(land.name() + "/" + a.kind(), k -> new int[4]);
                    t[0]++;
                    t[1] += a.plots().size();
                    t[2] += a.roads().size();
                    t[3] += a.lamps().size();
                }
            }
        }
        double ms = (System.nanoTime() - t0) / 1e6 / (plans * 2);
        System.out.println("terrain/kind                 plans  avg buildings  avg roads  avg lamps");
        for (var e : totals.entrySet()) {
            int[] t = e.getValue();
            System.out.printf("%-28s %5d  %13.1f  %9.1f  %9.1f%n", e.getKey(), t[0], t[1] / (double) t[0], t[2] / (double) t[0], t[3] / (double) t[0]);
        }
        System.out.println("building mix: " + typeCounts);
        System.out.println("downgraded (rough or wet site): " + downgraded);
        System.out.println("rejected, left as countryside: " + rejected);
        System.out.println("cities by number of basic services present (0..5): " + java.util.Arrays.toString(java.util.Arrays.copyOf(servicesSeen, 6)));
        System.out.printf("plan time: %.2f ms per plan (synthetic terrain, no noise sampling)%n", ms);
        System.out.println("checks: " + checks + ", failures: " + failures);
        System.exit(failures == 0 ? 0 : 1);
    }

    static void verify(TownPlanner.Layout layout, TownPlanner.Terrain terrain, String tag, Map<String, Integer> typeCounts) {
        RoadNet net = layout.net();
        List<TownFrame> frames = new ArrayList<>();
        boolean[] seen = new boolean[TownCatalog.SPECS.size()];
        for (TownPlanner.Plot plot : layout.plots()) {
            TownFrame f = plot.frame();
            typeCounts.merge(plot.type(), 1, Integer::sum);
            seen[TownCatalog.SPECS.indexOf(plot.spec())] = true;
            for (TownFrame g : frames) {
                boolean overlap = f.minX() <= g.maxX() && g.minX() <= f.maxX() && f.minZ() <= g.maxZ() && g.minZ() <= f.maxZ();
                check(!overlap, tag + " overlap " + plot.type() + " at " + plot.doorX() + "," + plot.doorZ());
            }
            frames.add(f);
            boolean road = false;
            for (int x = f.minX(); x <= f.maxX() && !road; x++) for (int z = f.minZ(); z <= f.maxZ(); z++) if (net.sample(x, z).road()) road = true;
            check(!road, tag + " " + plot.type() + " on a road");
            // The door's local cell lands where the plot says.
            check(f.worldX(plot.spec().doorX(), 0) == plot.doorX() && f.worldZ(plot.spec().doorX(), 0) == plot.doorZ(), tag + " frame/door mismatch");
            check(plot.walk() >= 1 && plot.walk() <= 8, tag + " walk length " + plot.walk());
            check(Math.abs(plot.floorY() - plot.roadY()) <= plot.walk(), tag + " walk too steep " + plot.floorY() + " vs road " + plot.roadY());
            int ex = plot.doorX() + TownFrame.DX[plot.facing()] * (plot.walk() + 1);
            int ez = plot.doorZ() + TownFrame.DZ[plot.facing()] * (plot.walk() + 1);
            check(net.sample(ex, ez).road(), tag + " walk of " + plot.type() + " does not end on a road");
            for (int k = 1; k <= plot.walk(); k++) {
                int wx = plot.doorX() + TownFrame.DX[plot.facing()] * k, wz = plot.doorZ() + TownFrame.DZ[plot.facing()] * k;
                for (TownFrame g : frames) {
                    boolean inside = wx >= g.minX() && wx <= g.maxX() && wz >= g.minZ() && wz <= g.maxZ();
                    check(!inside, tag + " walk crosses a building");
                }
            }
            check(Math.hypot(plot.doorX() - layout.cx(), plot.doorZ() - layout.cz()) <= layout.radius() + 16, tag + " plot outside settlement");
        }
        for (TownPlanner.Road road : layout.roads()) {
            List<int[]> p = road.points();
            for (int k = 0; k + 1 < p.size(); k++) {
                check(Math.abs(p.get(k + 1)[2] - p.get(k)[2]) <= 2, tag + " road grade " + p.get(k)[2] + "->" + p.get(k + 1)[2]);
            }
            for (int[] q : p) check(Math.hypot(q[0] - layout.cx(), q[1] - layout.cz()) <= layout.radius() + 2, tag + " road leaves settlement");
        }
        // Neighbouring road columns differ by at most one block (otherwise the half-step slabs cannot bridge them).
        int r = layout.radius() + 8, steep = 0, cells = 0;
        for (int x = layout.cx() - r; x < layout.cx() + r; x++) {
            for (int z = layout.cz() - r; z < layout.cz() + r; z++) {
                RoadNet.Hit a = net.sample(x, z);
                if (!a.road()) continue;
                cells++;
                RoadNet.Hit e = net.sample(x + 1, z), s = net.sample(x, z + 1);
                if (e.road() && Math.abs(e.y() - a.y()) > 1) steep++;
                if (s.road() && Math.abs(s.y() - a.y()) > 1) steep++;
            }
        }
        check(steep <= Math.max(2, cells / 200), tag + " " + steep + " road steps over one block in " + cells + " cells");
        if (layout.kind() == TownPlanner.Kind.CITY) {
            int services = 0, required = 0;
            for (int i = 0; i < seen.length; i++) if (TownCatalog.SPECS.get(i).required()) {
                required++;
                if (seen[i]) services++;
            }
            // Hard floors that mean something is broken; typical sizes are printed in the summary table.
            check(services >= required - 2, tag + " city has only " + services + "/" + required + " basic services");
            check(layout.plots().size() >= 12, tag + " city has only " + layout.plots().size() + " buildings");
            servicesSeen[services]++;
        }
        if (layout.kind() == TownPlanner.Kind.HAMLET) check(layout.plots().size() >= 3, tag + " hamlet has " + layout.plots().size() + " buildings");
    }

    static String describe(TownPlanner.Layout l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.radius()).append('|').append(l.cy()).append('|');
        for (TownPlanner.Road r : l.roads()) {
            sb.append(r.cls()).append(':');
            for (int[] p : r.points()) sb.append(p[0]).append(',').append(p[1]).append(',').append(p[2]).append(';');
        }
        for (TownPlanner.Plot p : l.plots()) sb.append(p).append('/');
        return sb.toString();
    }

    /** Smooth value-noise hills, an optional regional tilt, an optional meandering river. */
    static final class Terrain implements TownPlanner.Terrain {
        final double amp, tilt;
        final int river;

        Terrain(double amp, double tilt, int river) {
            this.amp = amp;
            this.tilt = tilt;
            this.river = river;
        }

        double riverDist(int x, int z) {
            double centre = 1000 + Math.sin(z / 40.0) * 30 + 60;
            return Math.abs(x - centre);
        }

        @Override
        public int height(int x, int z) {
            double h = 80 + amp * (Math.sin(x / 23.0) * Math.cos(z / 31.0) + 0.5 * Math.sin((x + z) / 13.0)) + tilt * (x - 1000);
            if (river > 0) {
                double d = riverDist(x, z);
                if (d < 14) h = Math.min(h, 76 + d * 0.3);
            }
            return (int) Math.floor(h);
        }

        @Override
        public boolean water(int x, int z) {
            return river > 0 && riverDist(x, z) < 6;
        }
    }
}
