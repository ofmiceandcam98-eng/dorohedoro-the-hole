import com.dorohedoro.hole.worldgen.city.BuildingMass14;
import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.Rect;

/** Dependency-free structural profiling and stair-door preservation tests. */
public final class VerifyBuildingMass14 {
    public static void main(String[] args) {
        int profiles[] = new int[4];
        long cases = 0;
        for (long seed : new long[]{1L,3L,4L,42L,200L,-144L,20261031L,Long.MAX_VALUE}) {
            for (int w=19;w<=52;w++) for (int d=17;d<=51;d++) {
                Rect box = new Rect(-100, -200, -101+w, -201+d);
                for (int i=0;i<18;i++) {
                    long h = CityHash.of(seed, 0xABC14L, w*113+i, d*23+i);
                    int floors=3+i%9;
                    boolean garden=i%5==0;
                    int k=BuildingMass14.profile(h, box, floors, garden);
                    if(k<0||k>3)throw new AssertionError("invalid profile");
                    if(BuildingMass14.profile(h,box,floors,garden)!=k)throw new AssertionError("unstable profile");
                    profiles[k]++;
                    if(k==0){
                        Rect r=BuildingMass14.lRecess(box);
                        validate(r,box);
                        Rect stair = new Rect(box.x0()+1,box.z0()+1,box.x0()+6,box.z0()+4);
                        if(r.intersects(stair))throw new AssertionError("L cut stairwell");
                        if(r.contains(box.centerX(),box.z1()))throw new AssertionError("L cut entrance");
                    }
                    if(k==1){
                        Rect r=BuildingMass14.courtyard(box);
                        validate(r,box);
                        if(r.width()<4||r.depth()<4)throw new AssertionError("small courtyard");
                        if(r.contains(box.centerX(),box.z1()))throw new AssertionError("courtyard eats door");
                        if(r.intersects(new Rect(box.x0()+1,box.z0()+1,box.x0()+6,box.z0()+4)))throw new AssertionError("courtyard eats stairs");
                    }
                    if(k==2){
                        Rect r=BuildingMass14.upperTerrace(box);
                        validate(r,box);
                        if(garden)throw new AssertionError("roof farms sacrificed");
                        if(r.intersects(new Rect(box.x0()+1,box.z0()+1,box.x0()+6,box.z0()+4)))throw new AssertionError("terrace eats stairs");
                    }
                    cases++;
                }
            }
        }
        for(int k=0;k<4;k++) if(profiles[k]<1000)throw new AssertionError("missing mass silhouette "+k+": "+profiles[k]);
        System.out.printf("PASS %,d building cases, L %,d, courtyard %,d, terrace %,d, intact %,d%n",cases,profiles[0],profiles[1],profiles[2],profiles[3]);
        System.out.println("PASS protected stair/door zones, roof farms, parcel bounds and deterministic profiles");
    }
    private static void validate(Rect r,Rect b){
        if (!r.isValid()|| !b.contains(r.x0(),r.z0())||!b.contains(r.x1(),r.z1()))throw new AssertionError("out-of-bounds cut");
    }
}
