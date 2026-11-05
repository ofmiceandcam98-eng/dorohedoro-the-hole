import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.IrregularBlocks14;
import com.dorohedoro.hole.worldgen.city.Rect;
import java.util.List;

/** Pure-Java geometric + path connectivity verification; no game dependencies. */
public final class VerifyIrregularBlocks14 {
    public static void main(String[] ignored) {
        int tested = 0, irregular = 0, totalParcels = 0;
        int minEmpty = Integer.MAX_VALUE, maxEmpty = 0;
        int minVariation = Integer.MAX_VALUE;
        for (long seed : new long[]{0L, 1L, 2L, 42L, 1997L, 20261031L, -1776L, 8675309L, -998877L, Long.MAX_VALUE}) {
            for (int bx = -24; bx <= 24; bx++) for (int bz = -24; bz <= 24; bz++) {
                long hash = CityHash.of(seed, 0x14ABCDL, bx, bz);
                tested++;
                if (!IrregularBlocks14.usesOffsetBlocks(hash)) continue;
                irregular++;
                Rect full = new Rect(0,0,91,83);
                List<Rect> parcels = IrregularBlocks14.parcels(full, hash);
                if (!parcels.equals(IrregularBlocks14.parcels(full, hash))) throw new AssertionError("non-deterministic layout");
                totalParcels += parcels.size();
                boolean[][] occupied = new boolean[92][84];
                for (Rect r : parcels) {
                    if (!r.isValid() || !full.contains(r.x0(),r.z0()) || !full.contains(r.x1(),r.z1()))
                        throw new AssertionError("parcel outside block");
                    for (int x=r.x0(); x<=r.x1(); x++) for (int z=r.z0(); z<=r.z1(); z++) {
                        if (occupied[x][z]) throw new AssertionError("overlapping parcels");
                        occupied[x][z]=true;
                    }
                }
                // All empty cells must have access to one of the outside arterial borders.
                // This is stronger than merely knowing each diagonal segment is 7 wide.
                boolean[][] seen = new boolean[92][84];
                int[] qx = new int[92*84], qz = new int[92*84];
                int tail = 0, head = 0;
                for (int x=0; x<92; x++) for (int z=0; z<84; z++) {
                    if ((x==0||x==91||z==0||z==83)&&!occupied[x][z]&&!seen[x][z]) {
                        seen[x][z]=true; qx[tail]=x; qz[tail++]=z;
                    }
                }
                while (head<tail) {
                    int x=qx[head], z=qz[head++];
                    for (int k=0;k<4;k++) {
                        int xx=x+(k==0?1:k==1?-1:0), zz=z+(k==2?1:k==3?-1:0);
                        if (xx<0||xx>91||zz<0||zz>83||occupied[xx][zz]||seen[xx][zz])continue;
                        seen[xx][zz]=true;qx[tail]=xx;qz[tail++]=zz;
                    }
                }
                int empty = 0;
                for (int x=0; x<92; x++) for (int z=0; z<84; z++) if (!occupied[x][z]) {
                    empty++;
                    if (!seen[x][z]) throw new AssertionError("stranded street cell at "+x+","+z);
                }
                minEmpty=Math.min(minEmpty,empty);maxEmpty=Math.max(maxEmpty,empty);
                if (empty<1000||empty>5500) throw new AssertionError("implausible urban density: "+empty);
                int[] degrees = new int[84];
                for (Rect r: parcels) degrees[Math.min(83,r.centerZ())]++;
                int nonzero=0;
                for(int v: degrees) if(v>0)nonzero++;
                minVariation=Math.min(minVariation,nonzero);
            }
        }
        int maxBend=0;
        for (int idx=-10;idx<=10;idx++) for (int z=-600;z<=600;z++) {
            int a=IrregularBlocks14.avenueOffset(42L,123456L,idx,z,50);
            int b=IrregularBlocks14.avenueOffset(42L,123456L,idx,z+1,50);
            if (Math.abs(a)>4 || Math.abs(b-a)>1) throw new AssertionError("disconnected avenue bend");
            maxBend=Math.max(maxBend,Math.abs(a));
            if(IrregularBlocks14.avenueOffset(42L,123456L,idx,z,13)!=0)throw new AssertionError("intersection shifted");
        }
        System.out.printf("PASS %,d seeds/sites, %,d irregular blocks (%.1f%%)%n",tested,irregular,100.0*irregular/tested);
        System.out.printf("PASS %,d parcels nonoverlap; all side alleys connected to arterials%n",totalParcels);
        System.out.printf("PASS street area range %,d–%,d cells, parcel variation >=%d rows%n",minEmpty,maxEmpty,minVariation);
        System.out.printf("PASS ±%d block smooth avenue bends; crossing alignment preserved%n",maxBend);
    }
}
