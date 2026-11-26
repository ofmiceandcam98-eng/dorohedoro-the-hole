package com.dorohedoro.hole.worldgen.sorcerer;

import java.util.List;

/**
 * The road network of one town as a pure function of a column: which road covers it, and at what walking height.
 * The planner uses it to rasterize occupancy and the road piece uses it to paint, so the two can never disagree.
 *
 * Roads are polylines of points {x, z, y}, where y is the feet level of someone standing on the paving. Consecutive
 * points are at most 2 blocks apart in height over a 6 block step, so neighbouring columns differ by at most one block.
 * Where ribbons overlap, the higher road class wins (the plaza, then thoroughfares, streets, alleys), so a branch always
 * meets its parent at the parent's height.
 */
public final class RoadNet {
    /** Road classes. */
    public static final int PLAZA = -1, MAIN = 0, STREET = 1, ALLEY = 2;
    /** Ribbon radius per class, measured from the centreline to a column centre. Gives widths 5, 3 and 2. */
    public static final double[] RADIUS = {2.5, 1.5, 1.0};
    /** Columns this far outside a ribbon count as its verge: lamps, kerbs and retaining walls live there. */
    public static final double VERGE = 1.5;

    private final int cx, cz, cy, plazaRadius;
    private final int count;
    private final int[] cls;
    private final double[] ax, az, ay, bx, bz, by;
    private final int[] minX, minZ, maxX, maxZ;

    public RoadNet(int cx, int cz, int cy, int plazaRadius, List<TownPlanner.Road> roads) {
        this.cx = cx;
        this.cz = cz;
        this.cy = cy;
        this.plazaRadius = plazaRadius;
        int n = 0;
        for (TownPlanner.Road road : roads) n += Math.max(0, road.points().size() - 1);
        count = n;
        cls = new int[n];
        ax = new double[n]; az = new double[n]; ay = new double[n];
        bx = new double[n]; bz = new double[n]; by = new double[n];
        minX = new int[n]; minZ = new int[n]; maxX = new int[n]; maxZ = new int[n];
        int i = 0;
        for (TownPlanner.Road road : roads) {
            List<int[]> p = road.points();
            for (int k = 0; k + 1 < p.size(); k++, i++) {
                int[] a = p.get(k), b = p.get(k + 1);
                cls[i] = road.cls();
                ax[i] = a[0] + 0.5; az[i] = a[1] + 0.5; ay[i] = a[2];
                bx[i] = b[0] + 0.5; bz[i] = b[1] + 0.5; by[i] = b[2];
                int m = (int) Math.ceil(RADIUS[road.cls()] + VERGE) + 1;
                minX[i] = Math.min(a[0], b[0]) - m; maxX[i] = Math.max(a[0], b[0]) + m;
                minZ[i] = Math.min(a[1], b[1]) - m; maxZ[i] = Math.max(a[1], b[1]) + m;
            }
        }
    }

    /** What the column at (x, z) is. */
    public record Hit(boolean road, boolean verge, int cls, int y, double dist) {
        public static final Hit NONE = new Hit(false, false, 99, 0, Double.MAX_VALUE);
    }

    public Hit sample(int x, int z) {
        double px = x + 0.5, pz = z + 0.5;
        double pdx = px - (cx + 0.5), pdz = pz - (cz + 0.5);
        double pd = Math.sqrt(pdx * pdx + pdz * pdz);
        if (pd <= plazaRadius) return new Hit(true, false, PLAZA, cy, pd);

        int bestCls = 99;
        double bestDist = Double.MAX_VALUE, bestY = 0;
        double vergeGap = Double.MAX_VALUE, vergeY = 0;
        int vergeCls = 99;
        for (int i = 0; i < count; i++) {
            if (x < minX[i] || x > maxX[i] || z < minZ[i] || z > maxZ[i]) continue;
            double dx = bx[i] - ax[i], dz = bz[i] - az[i];
            double len2 = dx * dx + dz * dz;
            double t = len2 == 0 ? 0 : ((px - ax[i]) * dx + (pz - az[i]) * dz) / len2;
            t = Math.max(0, Math.min(1, t));
            double qx = ax[i] + dx * t - px, qz = az[i] + dz * t - pz;
            double dist = Math.sqrt(qx * qx + qz * qz);
            double y = ay[i] + (by[i] - ay[i]) * t;
            double r = RADIUS[cls[i]];
            if (dist <= r) {
                if (cls[i] < bestCls || (cls[i] == bestCls && dist < bestDist)) {
                    bestCls = cls[i];
                    bestDist = dist;
                    bestY = y;
                }
            } else if (dist - r <= VERGE && dist - r < vergeGap) {
                vergeGap = dist - r;
                vergeY = y;
                vergeCls = cls[i];
            }
        }
        // The plaza's paved apron counts as verge too.
        if (bestCls == 99 && pd - plazaRadius <= VERGE && pd - plazaRadius < vergeGap) {
            return new Hit(false, true, PLAZA, cy, pd);
        }
        if (bestCls != 99) return new Hit(true, false, bestCls, (int) Math.floor(bestY + 0.5), bestDist);
        if (vergeCls != 99) return new Hit(false, true, vergeCls, (int) Math.floor(vergeY + 0.5), vergeGap);
        return Hit.NONE;
    }

    public int plazaRadius() {
        return plazaRadius;
    }
}
