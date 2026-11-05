package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Compiles real AttachedWings55 geometry against minimal Canvas and Block fakes. */
public final class AttachedWingsFixture55 {
  static void require(boolean yes,String error){if(!yes)throw new AssertionError(error);}
  public static void main(String[] args) {
    Style style = new Style(new Mix(Blocks.BOOKSHELF),new Mix(Blocks.CRAFTING_TABLE),Blocks.IRON_BARS);
    int checked=0,eligible=0,wings=0,one=0,two=0,split=0;
    Set<Integer> variants=new HashSet<>();
    for(int seed=0;seed<4000;seed++){
      long hash=CityHash.of(seed,seed*17L+43,seed*37L-29);
      LotType type= switch(seed%4){case 0->LotType.TENEMENT;case 1->LotType.APARTMENT;case 2->LotType.SHOP;default->LotType.FACTORY;};
      Lot lot=new Lot(new Rect(5,7,54,61),type,hash);
      int width= 13 + seed%24, depth=12 + (seed/7)%27;
      Rect old=new Rect(10,14,10+width-1,14+depth-1);
      int floors=2+seed%5;
      Canvas c=new Canvas(0,127,0,127,38);
      Rect main=AttachedWings55.mainFootprint(c,lot,old,floors);
      require(main.x0()>=old.x0() && main.x1()<=old.x1() && main.z0()>=old.z0() && main.z1()<=old.z1(),"Main escaped parcel");
      if(!main.equals(old)){
        eligible++;
        require(AttachedWings55.eligible(c,lot,old,floors),"Selected wing not eligible");
        Rect wing=AttachedWings55.wingFootprint(old,main,hash);
        require(wing!=null,"Null wing");
        require(wing.x0()>=old.x0()&&wing.x1()<=old.x1()&&wing.z0()>=old.z0()&&wing.z1()==main.z0(),"Wing outside previous footprint");
        require(main.depth()>=13&&main.width()>=17,"Main too small");
        require(wing.x0()>=main.x0()+8&&wing.x1()<=main.x1()-1,"Wing blocks west staircase or frontage");
        require(wing.width()>=7&&wing.depth()==7,"Wing lacks usable room");
        int count=AttachedWings55.wingFloors(floors,hash);
        require(count<floors,"Wing roof is not stepped");
        variants.add(count);
        c=new Canvas(0,127,0,127,38);
        AttachedWings55.build(c,lot,old,main,floors,style);
        require(!c.world.isEmpty(),"Selected geometry did not render");
        for(String key:c.world.keySet()) {
          String[] parts=key.split(":");
          int x=Integer.parseInt(parts[0]),z=Integer.parseInt(parts[2]);
          require(old.contains(x,z),"Writes outside original footprint: "+key);
        }
        int x=wing.centerX(), z=main.z0();
        for(int k=0;k<count;k++){
          int y=CityPlan.GROUND_Y+k*Tower.STOREY;
          require(c.get(x,y,z)==Blocks.SPRUCE_DOOR && c.get(x,y+1,z)==Blocks.SPRUCE_DOOR,"Room entry door missing");
          require(c.get(x,y,z-1)==Blocks.AIR&&c.get(x,y+1,z-1)==Blocks.AIR,"Wing approach blocked");
          require(c.get(x,y,z+1)==Blocks.AIR&&c.get(x,y+1,z+1)==Blocks.AIR,"Main approach blocked");
          require(c.get(wing.x0()+1,y,wing.z0()+1)!=null,"Furnishings missing");
        }
        int ry=CityPlan.STREET_Y+4*count;
        require(c.get(x,ry,main.z0())==Blocks.WAXED_WEATHERED_CUT_COPPER,"Missing roof deck");
        require(c.get(x,ry+1,main.z0())==Blocks.SPRUCE_DOOR,"Roof terrace missing access");
        require(c.get(wing.x0()+1,ry+1,wing.z0()+1)==Blocks.CAULDRON,"No rainwater access");
        if(count==1) one++;else two++;
        if(split++<350) {
          Canvas a=new Canvas(0,23,0,127,38), b=new Canvas(24,127,0,127,38);
          AttachedWings55.build(a,lot,old,main,floors,style);
          AttachedWings55.build(b,lot,old,main,floors,style);
          Map<String,net.minecraft.world.level.block.Block> combined=new HashMap<>(a.world);
          combined.putAll(b.world);
          require(combined.equals(c.world),"Chunk clipping changed result");
        }
        wings++;
      } else {
        AttachedWings55.build(c,lot,old,main,floors,style);
        require(c.world.isEmpty(),"Unselected lot altered");
      }
      Canvas legacy=new Canvas(0,127,0,127,37);
      require(AttachedWings55.mainFootprint(legacy,lot,old,floors).equals(old),"v37 saves changed");
      checked++;
    }
    require(eligible>120,"Not enough larger plots receive wings");
    require(one>0&&two>0,"Upper-room and low-rise variants absent");
    System.out.printf("PASS Pass55 actual Java geometry: %d parcels, %d service wings (%d single-storey, %d double-storey), %d split chunk checks, legacy v37 unchanged%n",checked,eligible,one,two,Math.min(split,350));
  }
}
