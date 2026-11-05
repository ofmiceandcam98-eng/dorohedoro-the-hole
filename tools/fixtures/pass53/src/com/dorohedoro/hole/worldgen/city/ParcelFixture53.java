package com.dorohedoro.hole.worldgen.city;
import java.util.*;

public final class ParcelFixture53 {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        long[] area=new long[4], counts=new long[4], seen=new long[4], grid=new long[4];
        long totalLots=0;
        for(long seed=0;seed<18;seed++){
            CityPlan p=new CityPlan(seed*0xA3B47D91L+12345L,36);
            for(int i=-24;i<=24;i++)for(int j=-24;j<=24;j++){
                if(i==-1&&j==-1)continue;
                Rect whole=Superblock.areaFor(p,i,j);
                UrbanPattern52.Fabric fabric=UrbanPattern52.at(p.seed,whole.centerX(),whole.centerZ());
                Superblock sb=Superblock.build(p,i,j);
                // Geometries never escape arterial rights-of-way or overlap other parcels.
                for(int k=0;k<sb.lots().size();k++){
                    Rect r=sb.lots().get(k).rect();
                    check(r.x0()>=whole.x0() && r.x1()<=whole.x1() && r.z0()>=whole.z0() && r.z1()<=whole.z1(),"lot exceeds arterial-protected block");
                    check(r.width()>=11 && r.depth()>=11,"unusable thin parcel");
                    for(int l=0;l<k;l++)check(!r.intersects(sb.lots().get(l).rect()),"overlapping parcels");
                }
                int id=fabric.ordinal();
                for(Lot lot:sb.lots()){area[id]+=lot.rect().area();counts[id]++;}
                seen[id]++;
                grid[id]+=whole.area();
                totalLots+=sb.lots().size();
                if((i+j)%17==0)check(sb.lots().equals(Superblock.build(p,i,j).lots()),"not deterministic");
            }
        }
        for(UrbanPattern52.Fabric f:UrbanPattern52.Fabric.values()){
            int id=f.ordinal();
            check(seen[id]>300,"missing city fabric " + f);
            double mean=area[id]/(double)counts[id];
            double coverage=area[id]/(double)grid[id];
            System.out.printf("%s: %d blocks %d lots, mean %.1f blocks/lot, plot coverage %.2f%n",f,seen[id],counts[id],mean,coverage);
        }
        double denseArea=area[0]/(double)counts[0], openArea=area[3]/(double)counts[3];
        check(openArea>denseArea*1.4,"open countryside still has dense city parcel sizes");
        check(counts[0] > seen[0]*3,"dense neighborhoods insufficiently subdivided");
        check(counts[3]<seen[3]*12,"countryside has too many sidewalk alleys");
        // Verified policy thresholds at both sides of each urbanization boundary.
        for(District district:District.values()){
            for(UrbanPattern52.Fabric f:UrbanPattern52.Fabric.values()){
                for(int depth=0;depth<4;depth++){
                    UrbanParcels53.Profile policy=UrbanParcels53.profile(f,district,depth);
                    check(policy.streetGap()>=3 && policy.maxLot()>=policy.minLot(),"unsafe policy");
                }
            }
        }
        for(int i=0;i<10_000;i++){
            LotType lot=UrbanParcels53.denseFringeLot((i+.5f)/10_000f,new Rect(2,2,15,16));
            check(lot!=LotType.RUBBLE && lot!=LotType.PLAZA,"dense outer city became rubble/plaza");
        }
        check(UrbanParcels53.denseFringeLot(.65F,new Rect(0,0,12,12))==LotType.SHOP,
            "tiny frontage produced unworkable factory");
        check(UrbanParcels53.denseFringeLot(.65F,new Rect(0,0,22,18))==LotType.FACTORY,
            "suitable industrial plot lost factory");
        System.out.printf("PASS: %d seeded blocks, %d parcels; boundaries, determinism, size hierarchy, walkable gaps and occupied fringe lots%n",Arrays.stream(seen).sum(),totalLots);
        System.out.println("NOT a Minecraft/NeoForge build: fixture uses placeholder line geometry, district map, and random source.");
    }
}
