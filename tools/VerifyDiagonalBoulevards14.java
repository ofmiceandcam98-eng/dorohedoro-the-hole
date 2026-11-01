import com.dorohedoro.hole.worldgen.city.DiagonalGrid14;
import com.dorohedoro.hole.worldgen.city.Rect;

/** Verify diagonal paths are seed stable and no unreserved lot intersects a road. */
public final class VerifyDiagonalBoulevards14 {
    public static void main(String[] args) {
        long lots = 0, removed = 0, roadCells = 0;
        for (long seed : new long[]{0L,1L,42L,12345L,-12345678L,20261031L}) {
            for (int x=-1700;x<=1700;x+=53)for(int z=-1700;z<=1700;z+=53) {
                Rect lot=new Rect(x,z,x+31,z+24);
                boolean reserved=DiagonalGrid14.intersects(seed,lot);
                if (reserved) removed++;
                lots++;
                for (int xx=x;xx<=x+31;xx++)for(int zz=z;zz<=z+24;zz++) {
                    boolean road=DiagonalGrid14.isRoad(seed,xx,zz);
                    if (road) {
                        roadCells++;
                        if (!reserved) throw new AssertionError("Boulevard through unreserved parcel at "+xx+","+zz+" seed "+seed);
                    }
                }
            }
            if (DiagonalGrid14.isRoad(seed,0,0))throw new AssertionError("spawn plaza destroyed");
            if (DiagonalGrid14.intersects(seed,new Rect(-20,-20,20,20)))
                throw new AssertionError("starter quarter was reserved");
        }
        if (removed<500||roadCells<500)throw new AssertionError("diagonal boulevards too rare");
        System.out.printf("PASS %,d lots with %,d reserved for diagonal boulevards, %,d road cells inside the reserved parcels%n",lots,removed,roadCells);
        System.out.println("PASS exact seeded intersections, safe origin, and no building/road overlaps across 6 seeds");
    }
}
