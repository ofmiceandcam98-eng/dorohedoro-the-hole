import com.dorohedoro.hole.worldgen.city.CityHash;
import com.dorohedoro.hole.worldgen.city.IrregularBlocks14;
import com.dorohedoro.hole.worldgen.city.DiagonalGrid14;
import com.dorohedoro.hole.worldgen.city.Rect;

/** Pure source-derived schematic coordinates, not an in-game world renderer. */
public final class PreviewIrregularBlocks14 {
    private static final long X_SALT=0x51ED27L, Z_SALT=0x2F0B1DL;
    private static final long BLOCK_SALT=0x7A11E5L;
    private static int center(long seed,long salt,int i,int pitch){
        long h=CityHash.of(seed,salt,i);
        int reach=(i==0||i==-1)?12:32;
        return i*pitch + pitch/2 + CityHash.range(h,reach*2+1)-reach;
    }
    private static int radius(long seed,long salt,int i){
        long h=CityHash.of(seed,salt,i);
        int type=CityHash.range(h>>>16,6);
        if ((i==0||i==-1)&&type==5)type=3;
        return type<3?6:type<5?7:9;
    }
    public static void main(String[] argv) {
        long seed=20261031L;
        int[] cx=new int[5],cz=new int[5], rx=new int[5],rz=new int[5];
        for(int i=0;i<5;i++){
            cx[i]=center(seed,X_SALT,i,144);cz[i]=center(seed,Z_SALT,i,104);
            rx[i]=radius(seed,X_SALT,i);rz[i]=radius(seed,Z_SALT,i);
            System.out.printf("ROAD,X,%d,%d,%d%n",i,cx[i],rx[i]);
            System.out.printf("ROAD,Z,%d,%d,%d%n",i,cz[i],rz[i]);
        }
        for(int bx=0;bx<4;bx++)for(int bz=0;bz<4;bz++){
            long hash=CityHash.of(seed,BLOCK_SALT,bx,bz);
            Rect area=new Rect(cx[bx]+rx[bx]+7,cz[bz]+rz[bz]+7,
                    cx[bx+1]-rx[bx+1]-7,cz[bz+1]-rz[bz+1]-7);
            var parcels=IrregularBlocks14.usesOffsetBlocks(hash)&&area.width()>=55&&area.depth()>=55?
                    IrregularBlocks14.parcels(area,hash):java.util.List.of(area);
            for(Rect p:parcels)System.out.printf("%s,%d,%d,%d,%d,%d,%d%n",
                    DiagonalGrid14.intersects(seed,p)?"OPEN":"LOT",bx,bz,p.x0(),p.z0(),p.x1(),p.z1());
        }
        for(int x=cx[0];x<=cx[4];x++)for(int z=cz[0];z<=cz[4];z++)
            if(DiagonalGrid14.isRoad(seed,x,z)) System.out.printf("DIAG,%d,%d%n",x,z);
    }
}
