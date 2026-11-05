package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.*;

/** Test the exact Pass58 production geometry using a block-recording chunk-clipped Canvas. */
public final class IndustrialAnnexFixture58 {
  static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
  public static void main(String[] args){
    int trials=0,annexes=0,seamChecks=0,compatChecks=0;
    for(int seed=0;seed<3600;seed++){
      int x=3+(seed%17),z=3+(seed/7)%19;
      int width=37+seed%38,depth=23+(seed/9)%32;
      Rect yard=new Rect(x,z,x+width+4,z+depth+4);
      Rect original=new Rect(x+1,z+1,x+width-2,z+depth-2);
      Lot lot=new Lot(yard,seed%2==0?LotType.FACTORY:LotType.WAREHOUSE,CityHash.of(seed,seed*7L,seed*17L));
      Canvas full=new Canvas(-1,160,-1,160,41);
      Rect main=IndustrialAnnex58.mainFootprint(full,lot,original);
      IndustrialAnnex58.build(full,lot,original,main,lot.type()==LotType.FACTORY);
      if(!main.equals(original)){
        Rect bay=IndustrialAnnex58.wing(original,main);
        check(bay!=null,"Missing bay on shortened hall");
        check(main.width()>=25,"Hall too narrow");
        check(bay.width()==9&&bay.depth()==14,"Bay dimensions incorrect");
        check(yard.contains(bay.x1(),bay.z1()),"Bay outside parcel");
        int frontZ=bay.z0()+3;
        check(full.get(main.x1(),65,frontZ)==Blocks.SPRUCE_DOOR,"Hall not connected");
        check(full.get(bay.x0()+4,65,bay.z0()+7)==Blocks.SPRUCE_DOOR,"Second room inaccessible");
        for(int xx=main.centerX();xx<=main.x1()-1;xx++)for(int yy=65;yy<=67;yy++)
          check(full.get(xx,yy,frontZ)==Blocks.AIR,"Hall-side passage blocked");
        for(int xx=bay.x0()+1;xx<=bay.x0()+2;xx++)for(int yy=65;yy<=67;yy++)
          check(full.get(xx,yy,frontZ)==Blocks.AIR,"Wing-side passage blocked");
        check(full.get(bay.x1()-1,71,bay.z1()-2)==Blocks.LADDER,"Roof hatch sealed");
        check(full.get(bay.x1()-1,72,bay.z1()-2)==Blocks.LADDER,"No roof exit");
        check(full.get(bay.x1()-2,65,bay.z0()+2)==(lot.type()==LotType.FACTORY?Blocks.SMITHING_TABLE:Blocks.CRAFTING_TABLE),"Wrong front-room workstation");
        check(full.get(bay.x1()-2,65,bay.z1()-3)==(lot.type()==LotType.FACTORY?Blocks.BLAST_FURNACE:Blocks.LOOM),"Wrong second-room station");
        for(String key:full.world.keySet()){
          String[] xyz=key.split(":");
          check(yard.contains(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[2])),"Outside property: "+key);
        }
        if(seed%29==0){
          int split=bay.centerX();
          Canvas left=new Canvas(-1,split,-1,160,41),right=new Canvas(split+1,160,-1,160,41);
          IndustrialAnnex58.build(left,lot,original,main,lot.type()==LotType.FACTORY);
          IndustrialAnnex58.build(right,lot,original,main,lot.type()==LotType.FACTORY);
          Map<String,Block> combined=new HashMap<>(left.world);combined.putAll(right.world);
          check(combined.equals(full.world),"Chunk split changed geometry");
          seamChecks++;
        }
        annexes++;
      } else check(full.world.isEmpty(),"Ineligible plot changed");
      if(seed%7==0){
        Canvas old=new Canvas(-1,160,-1,160,40);
        check(IndustrialAnnex58.mainFootprint(old,lot,original).equals(original),"Old save footprint modified");
        IndustrialAnnex58.build(old,lot,original,main,lot.type()==LotType.FACTORY);
        check(old.world.isEmpty(),"Old save got new buildings");
        compatChecks++;
      }
      trials++;
    }
    check(annexes>900&&seamChecks>30&&compatChecks>400,"Insufficient coverage");
    System.out.printf("PASS Pass58 actual Java geometry: %d industrial scenarios, %d two-room annexes, %d cross-chunk checks, %d legacy v40 unchanged cases%n",trials,annexes,seamChecks,compatChecks);
  }
}
