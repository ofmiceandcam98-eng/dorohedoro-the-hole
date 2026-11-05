package com.dorohedoro.hole.worldgen.city;

import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Runs actual Pass60 geometry with chunk-clipping Canvas, not Minecraft APIs. */
public final class FreightApproachesFixture60 {
    static void require(boolean yes,String m){if(!yes)throw new AssertionError(m);}
    public static void main(String[] args) {
        int lots=0, loading=0, linked=0, cells=0, seams=0, legacy=0, negative=0, ignored=0, paired=0, small=0;
        for(int i=0;i<8200;i++) {
            int x=(i%4==0 ? -90 : 12)+(i%13), z=(i%3==0 ? -70 : 14)+(i%9);
            int w=16+i%77, d=15+(i/7)%60;
            Rect yard=new Rect(x,z,x+w-1,z+d-1);
            Lot lot=new Lot(yard, i%2==0?LotType.FACTORY:LotType.WAREHOUSE,CityHash.of(i,71L,29L));
            Canvas all=new Canvas(-200,240,-200,240,43);
            FreightApproaches60.build(all,lot);
            Rect original=StructuralVolumes14.industrialFootprint(all,lot,WorldDetail.footprint(all,lot));
            Rect hall=IndustrialAnnex58.mainFootprint(all,lot,original);
            Rect path=FreightApproaches60.loading(yard,hall);
            boolean eligible=hall.width()>=(lot.type()==LotType.FACTORY?9:7)&&hall.depth()>=7&&path!=null;
            if(!eligible){require(all.world.isEmpty(),"Unbuildable lot changed");ignored++;continue;}
            loading++;
            require(path.width()==3&&path.z1()==yard.z1(),"Apron doesn't reach frontage");
            require(path.z0()==hall.z1()+1&&path.centerX()==hall.centerX(),"Apron misses 3-wide hall entrance");
            for(int xx=path.x0();xx<=path.x1();xx++)for(int zz=path.z0();zz<=path.z1();zz++){
                require(all.get(xx,64,zz)==Blocks.POLISHED_ANDESITE,"Apron surface missing");
                for(int yy=65;yy<=68;yy++)require(all.get(xx,yy,zz)==Blocks.AIR,"Cargo route blocked");
                cells++;
            }
            for(int xx=hall.centerX()-1;xx<=hall.centerX()+1;xx++)
                for(int yy=65;yy<=68;yy++)require(all.get(xx,yy,hall.z1()-1)==Blocks.AIR,"Hall entrance blocked from within");
            // Ownership: no parcel write may alter a neighbor's property.
            for(String key:all.world.keySet()){
                String[] xyz=key.split(":");
                require(yard.contains(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[2])),"Write escapes industrial yard");
            }
            Rect workshop=LinkedServiceYards56.shack(yard,hall,true);
            Rect side=WorkingServiceCourts59.approach(yard,hall,workshop);
            Rect transverse=yard.width()>=36&&yard.depth()>=26
                    ? FreightApproaches60.crossAccess(yard,hall,side,path):null;
            if(transverse!=null){
                linked++;
                require(!transverse.intersects(hall),"Cross-path intersects industrial hall");
                require(transverse.depth()==3&&transverse.intersects(path)&&transverse.intersects(side),"Courtyard missing connection");
                for(int xx=transverse.x0();xx<=transverse.x1();xx++)for(int zz=transverse.z0();zz<=transverse.z1();zz++){
                    require(all.get(xx,64,zz)==Blocks.POLISHED_ANDESITE,"Crossing not paved");
                    for(int yy=65;yy<=68;yy++)require(all.get(xx,yy,zz)==Blocks.AIR,"Crossing blocked");
                }
            }
            if(side!=null && i%17==0){
                Canvas both=new Canvas(-200,240,-200,240,43);
                WorkingServiceCourts59.build(both,lot);
                FreightApproaches60.build(both,lot);
                for(int zz=side.z0();zz<=side.z1();zz++)for(int xx=side.x0();xx<=side.x1();xx++){
                    require(both.get(xx,64,zz)==Blocks.POLISHED_ANDESITE,"Side route damaged by loading apron");
                    for(int yy=65;yy<=67;yy++)require(both.get(xx,yy,zz)==Blocks.AIR,"Side route blocked");
                }
                int workX=hall.x0()-2,workZ=workshop.z1()+3;
                if(workX>side.x1()+1 && workZ+4<hall.z1()&&yard.contains(workX,workZ+4)){
                    require(both.get(workX,65,workZ)==Blocks.CAULDRON,"Wash pocket destroyed");
                    require(both.get(workX,65,workZ+2)==(lot.type()==LotType.FACTORY?Blocks.SMITHING_TABLE:Blocks.STONECUTTER),"Repair station destroyed");
                }
                paired++;
            }
            if(i%37==0){
                int split=hall.centerX();
                Canvas west=new Canvas(-200,split,-200,240,43),east=new Canvas(split+1,240,-200,240,43);
                FreightApproaches60.build(west,lot);FreightApproaches60.build(east,lot);
                Map<String,Block> stitched=new HashMap<>(west.world);stitched.putAll(east.world);
                require(stitched.equals(all.world),"Chunk seam mismatch");seams++;
            }
            if(i%7==0){
                Canvas old=new Canvas(-200,240,-200,240,42);
                FreightApproaches60.build(old,lot);
                require(old.world.isEmpty(),"Old v42 terrain modified");legacy++;
            }
            if(x<0||z<0)negative++;
            lots++;
        }
        Rect tiny=new Rect(0,0,11,11);
        for(LotType type:LotType.values())if(type!=LotType.FACTORY&&type!=LotType.WAREHOUSE){
            Canvas c=new Canvas(-10,40,-10,40,43);
            FreightApproaches60.build(c,new Lot(tiny,type,123L));
            require(c.world.isEmpty(),"Non-industrial plot changed: "+type);
        }
        for(int k=0;k<600;k++) {
            Rect narrow=new Rect(-13,k,-7+(k%3),k+5);
            Lot smallLot=new Lot(narrow,k%2==0?LotType.WAREHOUSE:LotType.FACTORY,k);
            Canvas c=new Canvas(-50,50,-50,900,43);
            FreightApproaches60.build(c,smallLot);
            require(c.world.isEmpty(),"Small hall must be excluded");small++;
        }
        require(lots>7500&&loading==lots&&linked>100&&seams>150&&legacy>900&&negative>1500&&cells>18000&&paired>140&&small==600,
              "Coverage unexpectedly low: "+lots+"/"+linked+"/"+seams+"/"+cells);
        System.out.printf("PASS Pass60 production Java fixture: %d parcels, %d loading approaches, %d joined courtyards, %d clear cargo cells, %d chunk matches, %d v42 unchanged, %d negative-coordinate lots, %d combined-yard checks, %d tiny lots excluded%n",lots,loading,linked,cells,seams,legacy,negative,paired,small);
    }
}
