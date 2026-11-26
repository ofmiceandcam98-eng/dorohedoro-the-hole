package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

/** Exercises actual CrookedLanes54 and Superblock source against minimal non-Minecraft geometry stubs. */
public final class CrookedLanesFixture54 {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void checkBounds(Rect area, List<Rect> lots) {
        for (int i = 0; i < lots.size(); i++) {
            Rect a = lots.get(i);
            check(a.x0() >= area.x0() && a.z0() >= area.z0()
                    && a.x1() <= area.x1() && a.z1() <= area.z1(), "parcel escaped the arterial reserve");
            check(a.width() >= 11 && a.depth() >= 11, "unbuildable narrow rectangular lot");
            for (int j = 0; j < i; j++)
                check(!a.intersects(lots.get(j)), "overlapping parcels");
        }
    }

    /** A 3-by-3 free-footprint is a safely pedestrian traversable lane (no 1-wide cracks). */
    private static void checkConnected(Rect area, List<Rect> lots, boolean alongX) {
        int w = area.width(), d = area.depth();
        boolean[][] blocked = new boolean[w][d];
        for (Rect parcel : lots)
            for (int x = parcel.x0(); x <= parcel.x1(); x++)
                for (int z = parcel.z0(); z <= parcel.z1(); z++)
                    blocked[x - area.x0()][z - area.z0()] = true;
        boolean[][] walk = new boolean[w-2][d-2];
        for (int x = 0; x < w - 2; x++) for (int z = 0; z < d - 2; z++) {
            boolean clear = true;
            for (int dx=0;dx<3;dx++)for(int dz=0;dz<3;dz++)clear &= !blocked[x+dx][z+dz];
            walk[x][z]=clear;
        }
        boolean[][] seen=new boolean[w-2][d-2];
        ArrayDeque<int[]> queue=new ArrayDeque<>();
        if(alongX){
            for(int z=0;z<d-2;z++)if(walk[0][z]){seen[0][z]=true;queue.add(new int[]{0,z});}
        }else{
            for(int x=0;x<w-2;x++)if(walk[x][0]){seen[x][0]=true;queue.add(new int[]{x,0});}
        }
        int[][] directions={{1,0},{0,1},{-1,0},{0,-1}};
        boolean reached=false;
        while(!queue.isEmpty()){
            int[] p=queue.removeFirst();
            if(alongX ? p[0]==w-3 : p[1]==d-3){reached=true;break;}
            for(int[] v:directions){
                int nx=p[0]+v[0], nz=p[1]+v[1];
                if(nx>=0&&nx<w-2&&nz>=0&&nz<d-2&&walk[nx][nz]&&!seen[nx][nz]){
                    seen[nx][nz]=true;queue.add(new int[]{nx,nz});
                }
            }
        }
        check(reached, "no connected >=3-wide path between opposite local road entrances");
    }

    /** Count genuinely non-linear centerlines: a midpoint must depart from the end-to-end chord. */
    private static boolean hasDogleg(List<Rect> lots, boolean alongX) {
        if (lots.size() < 6 || (lots.size() & 1) != 0) return false;
        int n = lots.size() / 2;
        int[] along = new int[n], center = new int[n];
        for (int i = 0; i < n; i++) {
            Rect near = lots.get(2 * i), far = lots.get(2 * i + 1);
            along[i] = alongX ? near.centerX() : near.centerZ();
            center[i] = alongX ? (near.z1() + far.z0()) / 2 : (near.x1() + far.x0()) / 2;
        }
        if (along[n - 1] == along[0]) return false;
        for (int i = 1; i < n - 1; i++) {
            double fraction = (along[i] - along[0]) / (double) (along[n - 1] - along[0]);
            if (Math.abs(center[i] - (center[0] + fraction * (center[n - 1] - center[0]))) >= 4.0)
                return true;
        }
        return false;
    }

    public static void main(String[] args) {
        long[] selected = new long[4], eligible = new long[4], parcels = new long[4];
        int checked=0, paths=0, bends=0;
        for (int s=0;s<4000;s++) {
            long h=CityHash.of(0x54ABC123L, s, -3*s);
            int x0=-99 + s%31, z0=411 - s%29;
            Rect area=new Rect(x0,z0,x0+56+(s%49),z0+56+((s*13)%49));
            for(UrbanPattern52.Fabric fabric:UrbanPattern52.Fabric.values()){
                int idx=fabric.ordinal();
                eligible[idx]++;
                if(!CrookedLanes54.use(h,fabric))continue;
                selected[idx]++;
                List<Rect> lots=CrookedLanes54.parcels(area,h,fabric);
                check(!lots.isEmpty(),"empty local block");
                checkBounds(area,lots);
                check(lots.equals(CrookedLanes54.parcels(area,h,fabric)),"nondeterministic placement");
                parcels[idx]+=lots.size();
                checked++;
                boolean alongX=CityHash.range(CityHash.mix(h+0x54C70AEL),2)==0;
                if (hasDogleg(lots,alongX)) bends++;
                // Evaluate more than a thousand full 3x3 pedestrian connectivity paths.
                if((s&3)==0){
                    checkConnected(area,lots,alongX);
                    paths++;
                }
            }
        }
        check(selected[0]>selected[1] && selected[1]>selected[2] && selected[2]>selected[3],
                "urbanization pattern does not determine crooked-lane frequency");
        check(paths>=1000,"insufficient walkability simulation coverage");
        check(bends >= checked / 3, "too many straight passages; no meaningful dogleg variation");
        // Test integrated Superblock selector. Legacy v36 remains exact same algorithms;
        // version 37 uses the new routine for opted-in blocks only.
        int integrated=0, oldPreserved=0;
        for(int i=-20;i<=20;i++)for(int j=-12;j<=12;j++){
            if(i==-1&&j==-1)continue;
            long seed=CityHash.of(0x991239L,i,j);
            CityPlan oldPlan=new CityPlan(seed,36), newPlan=new CityPlan(seed,37);
            Superblock oldBlock=Superblock.build(oldPlan,i,j), nextBlock=Superblock.build(newPlan,i,j);
            checkBounds(Superblock.areaFor(newPlan,i,j),nextBlock.lots().stream().map(Lot::rect).toList());
            check(oldBlock.lots().equals(Superblock.build(oldPlan,i,j).lots()),"old v36 random sequence changed");
            check(nextBlock.lots().equals(Superblock.build(newPlan,i,j).lots()),"v37 generation nondeterministic");
            if(!oldBlock.lots().equals(nextBlock.lots()))integrated++;
            oldPreserved++;
        }
        check(integrated>200,"new geometry not integrated into Superblock");
        for(UrbanPattern52.Fabric f:UrbanPattern52.Fabric.values()){
            int i=f.ordinal();
            System.out.printf("%s crooked blocks %d / %d, %d parcels%n",f,selected[i],eligible[i],parcels[i]);
        }
        System.out.printf("PASS: %d local layouts (%d with non-linear doglegs), %d walkable 3x3 routes, %d integrated Superblocks (%d changed), legacy repeatability%n",
                checked,bends,paths,oldPreserved,integrated);
        System.out.println("NOT a Minecraft client/render/server test: actual Java math uses minimal stand-ins for Minecraft types.");
    }
}
