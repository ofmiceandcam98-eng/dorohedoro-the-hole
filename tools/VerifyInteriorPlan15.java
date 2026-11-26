import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.InteriorPlan15;
import com.dorohedoro.hole.worldgen.city.Rect;

/** Standalone deterministic QA for room decisions; runs without NeoForge. */
public final class VerifyInteriorPlan15 {
    public static void main(String[] args) {
        int[] res = new int[4], shops = new int[4], clinic = new int[4];
        long cases = 0;
        long[] seeds = {1,2,42,99,12345,8675309,-4,20261031,0,55555};
        for (long seed : seeds) for(int x=-64;x<=64;x+=3) for(int z=-64;z<=64;z+=3) {
            for(int storey=0;storey<4;storey++) {
                int r=InteriorPlan15.residence(seed,x,z,storey);
                int c=InteriorPlan15.clinic(seed,x,z,storey);
                if(r<0||r>3||c<0||c>3)throw new AssertionError("bad room kind");
                if(r!=InteriorPlan15.residence(seed,x,z,storey)||c!=InteriorPlan15.clinic(seed,x,z,storey))
                    throw new AssertionError("non-deterministic residence/clinic");
                res[r]++;clinic[c]++;cases++;
            }
            int s=InteriorPlan15.shop(seed,x,z);
            if(s<0||s>3||s!=InteriorPlan15.shop(seed,x,z))throw new AssertionError("bad shop");
            shops[s]++;
        }
        for(int width=5;width<=48;width++) for(int depth=5;depth<=48;depth++) {
            Rect room = new Rect(500,500,499+width,499+depth);
            boolean fits=InteriorPlan15.fitsPartition(room);
            if(fits!=(width>=12&&depth>=9))throw new AssertionError("unsafe partition");
            if(fits) {
                int wall=InteriorPlan15.corridorWallZ(room);
                if(wall<=room.z0()||wall>=room.z1() - 1)throw new AssertionError("wall bad");
                int westDoor=room.x0()+3,eastDoor=room.x1()-3;
                if(westDoor>=eastDoor||!room.contains(westDoor,wall)||!room.contains(eastDoor,wall))
                    throw new AssertionError("door overlap");
                if(wall - room.z0()<2)throw new AssertionError("stair aisle blocked");
            }
        }
        for (int i=0;i<4;i++) {
            double proportion=(double)res[i]/cases;
            if(proportion<0.22||proportion>0.28)throw new AssertionError("poor residence variety: "+proportion);
            if((double)clinic[i]/cases<0.22||(double)clinic[i]/cases>0.28)
                throw new AssertionError("poor clinic variety");
        }
        System.out.printf("Pass 15: %,d residential and clinical layouts validated (%d seeds); 1,936 bounding cases checked%n", cases,seeds.length);
        System.out.printf("Residence %d %d %d %d; Shop %d %d %d %d; Clinic %d %d %d %d%n",
          res[0],res[1],res[2],res[3],shops[0],shops[1],shops[2],shops[3],clinic[0],clinic[1],clinic[2],clinic[3]);
        System.out.println("PASS deterministic variety, stable room types, stair corridor and door bounds.");
    }
}
