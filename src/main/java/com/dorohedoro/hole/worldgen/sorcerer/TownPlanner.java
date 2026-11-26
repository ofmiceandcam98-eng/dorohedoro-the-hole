package com.dorohedoro.hole.worldgen.sorcerer;

import java.util.ArrayList;
import java.util.List;

/**
 * Plans one Sorcerers' World settlement from a seed and the real terrain. Pure Java: the terrain arrives through
 * {@link Terrain}, so the same code runs inside Minecraft and in the offline fixture.
 *
 * Terrain first. Roads grow outward from a central plaza in 6-block steps; each step picks the heading that climbs
 * least, so streets bend around hills and follow contours instead of a grid. Grades are capped at 2 blocks per step.
 * Thoroughfares sprout streets, streets sprout alleys; a road that meets another either joins it, when the heights
 * agree, or stops short as a dead end. Buildings are then fitted along both sides of every road, axis-aligned and
 * turned to face it, with varied setbacks and gaps, each with a walk from its door down (or up) to the road.
 *
 * Settlements are separate places, not one endless city: cities in the city basin, towns on its edge and in farmland,
 * hamlets in the open country. Everything outside a settlement's radius stays countryside.
 */
public final class TownPlanner {
    public interface Terrain {
        /** Feet level on natural ground: the first air block above the surface. */
        int height(int x, int z);

        boolean water(int x, int z);
    }

    public enum Kind {
        CITY(80, 92, 44, 7),
        TOWN(56, 68, 18, 6),
        HAMLET(32, 42, 8, 4);

        public final int minRadius, maxRadius, maxRoads, plaza;

        Kind(int minRadius, int maxRadius, int maxRoads, int plaza) {
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.maxRoads = maxRoads;
            this.plaza = plaza;
        }
    }

    /** A polyline of {x, z, feetY} points. */
    public record Road(int cls, List<int[]> points) {}

    public record Lamp(int x, int z, int y) {}

    /**
     * A building or open lot. (doorX, doorZ) is the world cell of the door in the front wall; the walk is the run of
     * cells in front of it, along the facing, that lead to the road.
     */
    public record Plot(String type, int doorX, int doorZ, int facing, int floorY, int walk, int roadY, long seed) {
        public TownCatalog.Spec spec() {
            return TownCatalog.spec(type);
        }

        public TownFrame frame() {
            TownCatalog.Spec s = spec();
            return TownFrame.fromDoor(doorX, doorZ, s.w(), s.d(), s.doorX(), facing);
        }
    }

    public record Layout(int cx, int cz, int cy, int radius, Kind kind, int plazaRadius, List<Road> roads,
                         List<Lamp> lamps, List<Plot> plots, RoadNet net) {}

    private static final int STEP = 6, MAX_RISE = 2;
    private static final byte FREE = 0, ROAD = 1, VERGE = 2, LAMP = 3, BUILDING = 4, NEAR = 5, WALK = 6;

    private final TownRandom rng;
    private final int cx, cz, cy, radius, plazaRadius;
    private final Kind kind;
    private final Terrain terrain;
    private final List<Road> roads = new ArrayList<>();
    /** Segments for collision tests while growing: x0, z0, x1, z1, cls, road index. */
    private final List<double[]> segments = new ArrayList<>();
    private final int ox, oz, size;
    private final byte[] occ;
    private RoadNet net;

    private TownPlanner(long seed, int cx, int cz, Kind kind, Terrain terrain) {
        this.rng = new TownRandom(seed);
        this.cx = cx;
        this.cz = cz;
        this.kind = kind;
        this.terrain = terrain;
        this.radius = rng.range(kind.minRadius, kind.maxRadius);
        this.plazaRadius = kind.plaza + rng.nextInt(3);
        this.cy = terrain.height(cx, cz);
        this.ox = cx - radius - 24;
        this.oz = cz - radius - 24;
        this.size = 2 * radius + 49;
        this.occ = new byte[size * size];
    }

    public static Layout plan(long seed, int cx, int cz, Kind kind, Terrain terrain) {
        return new TownPlanner(seed, cx, cz, kind, terrain).run();
    }

    /** Fewest buildings that still make each kind of place. */
    public static int minimumPlots(Kind kind) {
        return switch (kind) {
            case CITY -> 14;
            case TOWN -> 7;
            case HAMLET -> 3;
        };
    }

    /**
     * Plans the requested kind, or the next smaller one when rough ground or water leaves too little buildable land.
     * Returns null when not even a hamlet fits: the site stays countryside.
     */
    public static Layout planViable(long seed, int cx, int cz, Kind kind, Terrain terrain) {
        for (int k = kind.ordinal(); k < Kind.values().length; k++) {
            Kind tryKind = Kind.values()[k];
            Layout layout = plan(seed + k, cx, cz, tryKind, terrain);
            if (layout.plots().size() >= minimumPlots(tryKind)) return layout;
        }
        return null;
    }

    private Layout run() {
        double a0 = rng.nextDouble() * Math.PI * 2;
        grow(RoadNet.MAIN, cx, cz, cy, a0, radius - 6, -1);
        grow(RoadNet.MAIN, cx, cz, cy, a0 + Math.PI + (rng.nextDouble() - 0.5) * 0.8, radius - 6, -1);
        if (kind != Kind.HAMLET && (kind == Kind.CITY || rng.chance(0.45))) {
            double cross = a0 + Math.PI / 2 + (rng.nextDouble() - 0.5) * 0.8;
            grow(RoadNet.MAIN, cx, cz, cy, cross, radius - 10, -1);
            if (kind == Kind.CITY) {
                grow(RoadNet.MAIN, cx, cz, cy, cross + Math.PI + (rng.nextDouble() - 0.5) * 0.6, radius - 10, -1);
            }
        }
        branch();
        net = new RoadNet(cx, cz, cy, plazaRadius, roads);
        rasterize();
        List<Lamp> lamps = placeLamps();
        List<Plot> plots = placePlots();
        return new Layout(cx, cz, cy, radius, kind, plazaRadius, List.copyOf(roads), lamps, plots, net);
    }

    // ---------------------------------------------------------------- roads

    private void branch() {
        for (int ri = 0; ri < roads.size() && roads.size() < kind.maxRoads; ri++) {
            Road road = roads.get(ri);
            if (road.cls() >= RoadNet.ALLEY) continue;
            int child = road.cls() + 1;
            if (kind == Kind.HAMLET && child == RoadNet.ALLEY && rng.chance(0.5)) continue;
            double total = length(road);
            double arc = rng.range(10, 18);
            while (arc < total - 8 && roads.size() < kind.maxRoads) {
                double[] at = along(road, arc);
                double fromCentre = Math.hypot(at[0] - cx, at[1] - cz);
                if (fromCentre > plazaRadius + 5) {
                    int side = rng.chance(0.5) ? 1 : -1;
                    double angle = Math.atan2(at[3], at[2]) + side * (Math.PI / 2 + (rng.nextDouble() - 0.5) * 0.9);
                    int len = child == RoadNet.STREET ? rng.range(36, 80) : rng.range(14, 34);
                    grow(child, at[0], at[1], (int) Math.floor(at[4] + 0.5), angle, len, ri);
                    if (rng.chance(kind == Kind.CITY ? 0.45 : 0.25)) {
                        double[] off = along(road, Math.min(total, arc + 4));
                        grow(child, off[0], off[1], (int) Math.floor(off[4] + 0.5), angle + side * Math.PI,
                                child == RoadNet.STREET ? rng.range(30, 60) : rng.range(12, 26), ri);
                    }
                }
                arc += kind == Kind.CITY ? (road.cls() == RoadNet.MAIN ? rng.range(13, 20) : rng.range(12, 18))
                        : road.cls() == RoadNet.MAIN ? rng.range(16, 26) : rng.range(14, 24);
            }
        }
    }

    private void grow(int cls, double sx, double sz, int sy, double angle, int maxLen, int parent) {
        List<int[]> pts = new ArrayList<>();
        pts.add(new int[]{(int) Math.floor(sx), (int) Math.floor(sz), sy});
        int index = roads.size();
        double x = sx, z = sz, a = angle, heading = angle;
        int y = sy, len = 0, waterRun = 0;
        boolean joined = false;
        double[] deltas = {-0.4, -0.2, 0, 0.2, 0.4};
        while (len < maxLen) {
            double bestCost = Double.MAX_VALUE, bx = 0, bz = 0, ba = 0;
            int bh = 0;
            boolean bw = false;
            double[] bHit = null;
            for (double delta : deltas) {
                double jitter = rng.nextDouble();
                double na = a + delta;
                double nx = x + Math.cos(na) * STEP, nz = z + Math.sin(na) * STEP;
                if (Math.hypot(nx - cx, nz - cz) > radius - 4) continue;
                int h = terrain.height((int) Math.floor(nx), (int) Math.floor(nz));
                boolean w = terrain.water((int) Math.floor(nx), (int) Math.floor(nz));
                if (w && waterRun >= 4) continue;
                int rise = Math.abs(h - y);
                if (!w && rise > 6) continue;
                double cost = rise * 1.6 + Math.abs(delta) * 2.5 + Math.abs(angleDiff(na, heading)) * 1.2 + jitter * 1.5;
                if (w) cost += 4;
                double[] hit = collision(nx, nz, cls, index, parent, len);
                // Check between the step's ends too, so a segment cannot cut across another road's ribbon.
                if (hit == null) hit = collision(x + (nx - x) / 3, z + (nz - z) / 3, cls, index, parent, len);
                if (hit == null) hit = collision(x + (nx - x) * 2 / 3, z + (nz - z) * 2 / 3, cls, index, parent, len);
                if (hit != null) cost += 0.5;
                if (cost < bestCost) {
                    bestCost = cost;
                    bx = nx; bz = nz; ba = na; bh = h; bw = w; bHit = hit;
                }
            }
            if (bestCost == Double.MAX_VALUE) break;
            if (bHit != null) {
                // Join the other road where the heights agree, otherwise stop short as a dead end.
                if (cls > RoadNet.MAIN && len >= 8 && Math.abs(bHit[2] - y) <= MAX_RISE) {
                    pts.add(new int[]{(int) Math.floor(bHit[0]), (int) Math.floor(bHit[1]), (int) Math.floor(bHit[2] + 0.5)});
                    joined = true;
                }
                break;
            }
            int target = bw ? bh + 2 : bh;
            // A junction or the plaza edge would otherwise get a two-block lip: the first step rises at most one,
            // and anything still beside the plaza stays level with it.
            int rise = len < STEP ? 1 : MAX_RISE;
            int ny = Math.max(y - rise, Math.min(y + rise, target));
            if (Math.hypot(bx - cx, bz - cz) <= plazaRadius + 3) {
                if (Math.abs(y - cy) > rise) break; // would reach the plaza over a lip: end here
                ny = cy;
            }
            if (!bw && Math.abs(bh - ny) > 5) break; // would need a deep cutting or a tall embankment
            pts.add(new int[]{(int) Math.floor(bx), (int) Math.floor(bz), ny});
            x = bx; z = bz; a = ba; y = ny;
            len += STEP;
            waterRun = bw ? waterRun + 1 : 0;
            heading += (rng.nextDouble() - 0.5) * 0.25;
        }
        if (!joined) {
            // No bridge to nowhere.
            while (pts.size() > 1 && terrain.water(pts.get(pts.size() - 1)[0], pts.get(pts.size() - 1)[1])) {
                pts.remove(pts.size() - 1);
            }
        }
        smooth(pts, joined, cx, cz, cy, plazaRadius + 3);
        if (joined && pts.size() >= 2) {
            int[] last = pts.get(pts.size() - 1), prev = pts.get(pts.size() - 2);
            if (Math.abs(last[2] - prev[2]) > MAX_RISE) pts.remove(pts.size() - 1);
        }
        int minPoints = cls == RoadNet.STREET ? 3 : 2;
        if (pts.size() < minPoints) return;
        roads.add(new Road(cls, pts));
        for (int k = 0; k + 1 < pts.size(); k++) {
            int[] p = pts.get(k), q = pts.get(k + 1);
            segments.add(new double[]{p[0] + 0.5, p[1] + 0.5, q[0] + 0.5, q[1] + 0.5, cls, index, p[2], q[2]});
        }
    }

    /** Light smoothing that keeps the first point (and a join's last point) fixed and the grade limit intact. */
    private static void smooth(List<int[]> pts, boolean fixedEnd, int cx, int cz, int cy, int level) {
        int n = pts.size();
        if (n < 3) return;
        int end = fixedEnd ? n - 1 : n;
        for (int i = 1; i < end; i++) {
            int prev = pts.get(i - 1)[2];
            int next = i + 1 < n ? pts.get(i + 1)[2] : pts.get(i)[2];
            int avg = (int) Math.floor((prev + 2 * pts.get(i)[2] + next) / 4.0 + 0.5);
            int rise = i == 1 ? 1 : MAX_RISE;
            int[] q = pts.get(i);
            boolean plaza = Math.hypot(q[0] - cx, q[1] - cz) <= level && Math.abs(prev - cy) <= rise;
            q[2] = plaza ? cy : Math.max(prev - rise, Math.min(prev + rise, avg));
        }
    }

    /** The nearest other-road point within clearance of (x, z): {x, z, y}; or null. */
    private double[] collision(double x, double z, int cls, int self, int parent, int len) {
        double[] best = null;
        double bestDist = Double.MAX_VALUE;
        for (double[] s : segments) {
            int road = (int) s[5];
            if (road == self) continue;
            if (road == parent && len < 12) continue;
            double clearance = RoadNet.RADIUS[cls] + RoadNet.RADIUS[(int) s[4]] + 3;
            double dx = s[2] - s[0], dz = s[3] - s[1];
            double len2 = dx * dx + dz * dz;
            double t = len2 == 0 ? 0 : ((x - s[0]) * dx + (z - s[1]) * dz) / len2;
            t = Math.max(0, Math.min(1, t));
            double qx = s[0] + dx * t, qz = s[1] + dz * t;
            double dist = Math.hypot(qx - x, qz - z);
            if (dist <= clearance && dist < bestDist) {
                bestDist = dist;
                best = new double[]{qx, qz, s[6] + (s[7] - s[6]) * t};
            }
        }
        if (best == null && Math.hypot(x - cx, z - cz) <= plazaRadius + RoadNet.RADIUS[cls] + 1 && len >= 12) {
            best = new double[]{cx, cz, cy};
        }
        return best;
    }

    private static double angleDiff(double a, double b) {
        double d = (a - b) % (Math.PI * 2);
        if (d > Math.PI) d -= Math.PI * 2;
        if (d < -Math.PI) d += Math.PI * 2;
        return d;
    }

    private static double length(Road road) {
        double total = 0;
        List<int[]> p = road.points();
        for (int k = 0; k + 1 < p.size(); k++) total += Math.hypot(p.get(k + 1)[0] - p.get(k)[0], p.get(k + 1)[1] - p.get(k)[1]);
        return total;
    }

    /** Position, unit direction and height at an arc length: {x, z, dx, dz, y}. */
    private static double[] along(Road road, double arc) {
        List<int[]> p = road.points();
        for (int k = 0; k + 1 < p.size(); k++) {
            int[] a = p.get(k), b = p.get(k + 1);
            double seg = Math.hypot(b[0] - a[0], b[1] - a[1]);
            if (arc <= seg || k + 2 == p.size()) {
                double t = seg == 0 ? 0 : Math.min(1, arc / seg);
                double dx = seg == 0 ? 1 : (b[0] - a[0]) / seg, dz = seg == 0 ? 0 : (b[1] - a[1]) / seg;
                return new double[]{a[0] + 0.5 + (b[0] - a[0]) * t, a[1] + 0.5 + (b[1] - a[1]) * t, dx, dz, a[2] + (b[2] - a[2]) * t};
            }
            arc -= seg;
        }
        int[] a = p.get(0);
        return new double[]{a[0] + 0.5, a[1] + 0.5, 1, 0, a[2]};
    }

    // ---------------------------------------------------------------- occupancy

    private void rasterize() {
        for (int gz = 0; gz < size; gz++) {
            for (int gx = 0; gx < size; gx++) {
                RoadNet.Hit hit = net.sample(ox + gx, oz + gz);
                if (hit.road()) occ[gz * size + gx] = ROAD;
                else if (hit.verge()) occ[gz * size + gx] = VERGE;
            }
        }
    }

    private byte at(int x, int z) {
        int gx = x - ox, gz = z - oz;
        if (gx < 0 || gz < 0 || gx >= size || gz >= size) return BUILDING;
        return occ[gz * size + gx];
    }

    private void mark(int x, int z, byte value) {
        int gx = x - ox, gz = z - oz;
        if (gx >= 0 && gz >= 0 && gx < size && gz < size) occ[gz * size + gx] = value;
    }

    // ---------------------------------------------------------------- lamps

    private List<Lamp> placeLamps() {
        List<Lamp> lamps = new ArrayList<>();
        for (Road road : roads) {
            if (road.cls() > RoadNet.STREET) continue;
            double total = length(road);
            int side = rng.chance(0.5) ? 1 : -1;
            for (double arc = rng.range(4, 9); arc < total - 2; arc += rng.range(12, 16)) {
                double[] at = along(road, arc);
                double off = RoadNet.RADIUS[road.cls()] + 1.0;
                int lx = (int) Math.floor(at[0] - at[3] * side * off);
                int lz = (int) Math.floor(at[1] + at[2] * side * off);
                side = -side;
                if (at(lx, lz) != VERGE) continue;
                RoadNet.Hit hit = net.sample(lx, lz);
                if (!hit.verge()) continue;
                mark(lx, lz, LAMP);
                lamps.add(new Lamp(lx, lz, hit.y()));
            }
        }
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + Math.PI / 4;
            int lx = (int) Math.floor(cx + 0.5 + Math.cos(a) * (plazaRadius + 1));
            int lz = (int) Math.floor(cz + 0.5 + Math.sin(a) * (plazaRadius + 1));
            if (at(lx, lz) == VERGE) {
                mark(lx, lz, LAMP);
                lamps.add(new Lamp(lx, lz, cy));
            }
        }
        return lamps;
    }

    // ---------------------------------------------------------------- plots

    private List<Plot> placePlots() {
        List<Plot> plots = new ArrayList<>();
        int[] counts = new int[TownCatalog.SPECS.size()];
        // First pass lines every road; the second fills the gaps a bend or a big building left behind.
        for (int pass = 0; pass < 2; pass++) placePlots(plots, counts, pass);
        return plots;
    }

    private void placePlots(List<Plot> plots, int[] counts, int pass) {
        for (Road road : roads) {
            double total = length(road);
            for (int side = 1; side >= -1; side -= 2) {
                String last = "", beforeLast = "";
                double arc = pass == 0 ? rng.range(2, 6) : rng.range(1, 3);
                while (arc < total - 3) {
                    double[] at = along(road, arc);
                    // Unit normal from the road towards the plot side.
                    double nx = -at[3] * side, nz = at[2] * side;
                    int facing = cardinal(-nx, -nz);
                    double ring = Math.hypot(at[0] - cx, at[1] - cz) / radius;
                    int ringIndex = ring < 0.38 ? 0 : ring < 0.7 ? 1 : 2;
                    Plot placed = null;
                    for (int attempt = 0; attempt < (pass == 0 ? 3 : 4) && placed == null; attempt++) {
                        TownCatalog.Spec spec = choose(ringIndex, counts, last, beforeLast);
                        if (spec == null) break;
                        if (pass == 1 && spec.w() > 9) continue;
                        placed = fitAnySetback(spec, road, at, nx, nz, facing);
                    }
                    if (placed == null) {
                        arc += 2;
                        continue;
                    }
                    plots.add(placed);
                    counts[TownCatalog.SPECS.indexOf(placed.spec())]++;
                    beforeLast = last;
                    last = placed.type();
                    int gap = switch (placed.spec().category()) {
                        case WEALTHY -> rng.range(3, 6);
                        case POOR -> rng.range(0, 2);
                        default -> rng.range(1, 4);
                    };
                    arc += placed.spec().w() + gap;
                }
            }
        }
    }

    private TownCatalog.Spec choose(int ring, int[] counts, String last, String beforeLast) {
        double total = 0;
        double[] weights = new double[TownCatalog.SPECS.size()];
        for (int i = 0; i < weights.length; i++) {
            TownCatalog.Spec spec = TownCatalog.SPECS.get(i);
            double w = kind == Kind.HAMLET ? spec.hamlet()[ring] : spec.ward()[ring];
            if (kind == Kind.TOWN && spec.category() == TownCatalog.Category.APARTMENT) w *= 0.4;
            if (w > 0 && spec.required() && counts[i] == 0 && kind != Kind.HAMLET) w *= 6;
            if (spec.id().equals(last)) w *= 0.15;
            else if (spec.id().equals(beforeLast)) w *= 0.4;
            weights[i] = w;
            total += w;
        }
        double roll = rng.nextDouble() * total;
        if (total <= 0) return null;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) return TownCatalog.SPECS.get(i);
        }
        return TownCatalog.SPECS.get(weights.length - 1);
    }

    /**
     * Facades are axis-aligned but roads bend, so one corner of a building often sits nearer the road than its door.
     * Try the preferred setback first, then step back until the footprint clears the verge.
     */
    private Plot fitAnySetback(TownCatalog.Spec spec, Road road, double[] at, double nx, double nz, int facing) {
        int first = spec.category() == TownCatalog.Category.WEALTHY ? rng.range(3, 4)
                : spec.category() == TownCatalog.Category.OPEN ? 1 : rng.range(1, 2);
        for (int setback = first; setback <= first + 4; setback++) {
            Plot plot = fit(spec, road, at, nx, nz, facing, setback);
            if (plot != null) return plot;
        }
        return null;
    }

    private Plot fit(TownCatalog.Spec spec, Road road, double[] at, double nx, double nz, int facing, int setback) {
        double off = RoadNet.RADIUS[road.cls()] + 0.5 + setback;
        int outX = (int) Math.floor(at[0] + nx * off), outZ = (int) Math.floor(at[1] + nz * off);
        int fx = TownFrame.DX[facing], fz = TownFrame.DZ[facing];
        int doorX = outX - fx, doorZ = outZ - fz;
        TownFrame frame = TownFrame.fromDoor(doorX, doorZ, spec.w(), spec.d(), spec.doorX(), facing);
        if (Math.hypot((frame.minX() + frame.maxX()) / 2.0 - cx, (frame.minZ() + frame.maxZ()) / 2.0 - cz) > radius + 6) return null;

        for (int z = frame.minZ() - 1; z <= frame.maxZ() + 1; z++) {
            for (int x = frame.minX() - 1; x <= frame.maxX() + 1; x++) {
                byte v = at(x, z);
                boolean inside = x >= frame.minX() && x <= frame.maxX() && z >= frame.minZ() && z <= frame.maxZ();
                if (inside ? v != FREE : (v != FREE && v != VERGE && v != NEAR)) return null;
            }
        }

        int[] xs = {frame.minX(), frame.maxX(), frame.minX(), frame.maxX(), (frame.minX() + frame.maxX()) / 2, doorX};
        int[] zs = {frame.minZ(), frame.minZ(), frame.maxZ(), frame.maxZ(), (frame.minZ() + frame.maxZ()) / 2, doorZ};
        int[] hs = new int[xs.length];
        for (int i = 0; i < xs.length; i++) {
            if (terrain.water(xs[i], zs[i])) return null;
            hs[i] = terrain.height(xs[i], zs[i]);
        }
        int[] sorted = hs.clone();
        java.util.Arrays.sort(sorted);
        if (sorted[sorted.length - 1] - sorted[0] > spec.maxSlope()) return null;

        // Walk from the door towards the road until the paving starts.
        int walk = 0, wx = outX, wz = outZ;
        RoadNet.Hit hit = null;
        while (walk <= 8) {
            byte v = at(wx, wz);
            if (v == ROAD) {
                hit = net.sample(wx, wz);
                break;
            }
            if (v != FREE && v != VERGE && v != NEAR) return null;
            walk++;
            wx += fx;
            wz += fz;
        }
        if (hit == null || !hit.road() || walk < 1) return null;
        int roadY = hit.y();
        int floorY = sorted[sorted.length / 2];
        floorY = Math.max(roadY - walk, Math.min(roadY + walk, floorY));

        for (int z = frame.minZ() - 1; z <= frame.maxZ() + 1; z++) {
            for (int x = frame.minX() - 1; x <= frame.maxX() + 1; x++) {
                boolean inside = x >= frame.minX() && x <= frame.maxX() && z >= frame.minZ() && z <= frame.maxZ();
                if (inside) mark(x, z, BUILDING);
                else if (at(x, z) == FREE) mark(x, z, NEAR);
            }
        }
        for (int k = 0, x = outX, z = outZ; k < walk; k++, x += fx, z += fz) mark(x, z, WALK);
        return new Plot(spec.id(), doorX, doorZ, facing, floorY, walk, roadY, rng.nextLong());
    }

    /** 0 north, 1 east, 2 south, 3 west: the cardinal closest to the vector. */
    static int cardinal(double x, double z) {
        if (Math.abs(x) >= Math.abs(z)) return x >= 0 ? 1 : 3;
        return z >= 0 ? 2 : 0;
    }
}
