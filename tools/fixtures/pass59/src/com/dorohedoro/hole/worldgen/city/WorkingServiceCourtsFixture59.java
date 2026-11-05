package com.dorohedoro.hole.worldgen.city;

import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Exercises production Pass59 placement with a chunk-clipped, block-recording Canvas. */
public final class WorkingServiceCourtsFixture59 {
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    public static void main(String[] args) {
        int trials=0, active=0, approachCells=0, pockets=0, seamChecks=0, legacy=0;
        for(int seed=0;seed<6500;seed++) {
            int x=5+seed%23,z=5+(seed/17)%23;
            int width=36+seed%49,depth=26+(seed/13)%42;
            Rect yard=new Rect(x,z,x+width-1,z+depth-1);
            Lot lot=new Lot(yard,seed%2==0?LotType.FACTORY:LotType.WAREHOUSE,CityHash.of(seed,seed*7L,seed*17L));
            Canvas full=new Canvas(-2,160,-2,160,42);
            WorkingServiceCourts59.build(full,lot);
            Rect base=StructuralVolumes14.industrialFootprint(full,lot,WorldDetail.footprint(full,lot));
            Rect main=IndustrialAnnex58.mainFootprint(full,lot,base);
            Rect shack=LinkedServiceYards56.shack(yard,main,true);
            Rect route=WorkingServiceCourts59.approach(yard,main,shack);
            if(route!=null) {
                active++;
                check(route.width()==3,"Court path isn't three wide");
                check(route.z1()==yard.z1(),"Court path doesn't reach street frontage");
                check(route.z0()==shack.z1()+1,"Court path doesn't start outside the workshop door");
                check(route.centerX()==shack.centerX(),"Court path misses workshop south entrance");
                for(int xx=route.x0();xx<=route.x1();xx++) for(int zz=route.z0();zz<=route.z1();zz++) {
                    check(full.get(xx,64,zz)==Blocks.POLISHED_ANDESITE,"Broken walking surface");
                    for(int yy=65; yy<=67;yy++)
                        check(full.get(xx,yy,zz)==Blocks.AIR,"Obstructed walking corridor");
                    approachCells++;
                }
                int wx=main.x0()-2,wz=shack.z1()+3;
                if(wx>route.x1()+1 && wz+4<main.z1()&&yard.contains(wx,wz+4)){
                    check(full.get(wx,65,wz)==Blocks.CAULDRON,"Missing wash facility");
                    check(full.get(wx,65,wz+2)==(lot.type()==LotType.FACTORY?Blocks.SMITHING_TABLE:Blocks.STONECUTTER),"Missing repair worksite");
                    check(full.get(wx,65,wz+4)==Blocks.COMPOSTER,"Missing composting facility");
                    check(full.get(wx,68,wz)==Blocks.WAXED_WEATHERED_CUT_COPPER,"Missing sheltered workbench cover");
                    pockets++;
                }
                for(String pos:full.world.keySet()){
                    String[] xyz=pos.split(":");
                    check(yard.contains(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[2])),"Write outside parcel: "+pos);
                }
                if(seed%31==0){
                    int split=route.centerX();
                    Canvas left=new Canvas(-2,split,-2,160,42),right=new Canvas(split+1,160,-2,160,42);
                    WorkingServiceCourts59.build(left,lot);WorkingServiceCourts59.build(right,lot);
                    Map<String,Block> combined=new HashMap<>(left.world);combined.putAll(right.world);
                    check(combined.equals(full.world),"Cross-chunk mismatch");
                    seamChecks++;
                }
            } else check(full.world.isEmpty(),"Ineligible parcel changed");
            if(seed%9==0){
                Canvas old=new Canvas(-2,160,-2,160,41);
                WorkingServiceCourts59.build(old,lot);
                check(old.world.isEmpty(),"v41 or earlier save unexpectedly modified");legacy++;
            }
            trials++;
        }
        // Tiny or non-industrial plots must never receive the service court.
        int rejects=0,negative=0;
        for(int i=0;i<1400;i++) {
            int x=-180+(i%9),z=-160+(i%11);
            int width=8+i%28,depth=7+(i/7)%19;
            Rect yard=new Rect(x,z,x+width-1,z+depth-1);
            Lot lot=new Lot(yard,i%2==0?LotType.FACTORY:LotType.WAREHOUSE,CityHash.of(i,11L,99L));
            Canvas c=new Canvas(-200,200,-200,200,42);
            WorkingServiceCourts59.build(c,lot);
            check(c.world.isEmpty(),"Undersized industrial lot mutated: "+yard);
            rejects++;
        }
        for(int i=0;i<500;i++) {
            int x=-120-(i%11),z=-95-(i%13);
            Rect yard=new Rect(x,z,x+51+(i%29),z+38+(i%14));
            Lot lot=new Lot(yard,LotType.FACTORY,CityHash.of(i,-11L,-99L));
            Canvas c=new Canvas(-220,200,-220,200,42);
            WorkingServiceCourts59.build(c,lot);
            Rect base=StructuralVolumes14.industrialFootprint(c,lot,WorldDetail.footprint(c,lot));
            Rect main=IndustrialAnnex58.mainFootprint(c,lot,base);
            Rect shack=LinkedServiceYards56.shack(yard,main,true);
            Rect route=WorkingServiceCourts59.approach(yard,main,shack);
            check(route != null && c.get(route.centerX(),65,route.z1())==Blocks.AIR,"Negative-world street access missing");
            negative++;
        }
        check(active>3000&&pockets>3000&&seamChecks>100&&legacy>700&&rejects==1400&&negative==500,"Insufficient coverage");
        System.out.printf("PASS Pass59 Java fixture: %d lots, %d walking courts, %d clear approach blocks, %d work pockets, %d cross-chunk matches, %d old-world unchanged, %d small lots rejected, %d negative-coordinate courts%n",trials,active,approachCells,pockets,seamChecks,legacy,rejects,negative);
    }
}
