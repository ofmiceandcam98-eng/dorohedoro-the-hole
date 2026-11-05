package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Real Pass56 production geometry run against chunk-clipped minimal Canvas fakes. */
public final class LinkedServiceYardsFixture56 {
  static void require(boolean yes,String error){if(!yes)throw new AssertionError(error);}
  static Map<String,Block> painted(Canvas c,Lot lot,Rect shack,Rect main){
    LinkedServiceYards56.connect(c,lot,shack,main);
    return c.world;
  }
  public static void main(String[] args) {
    int tested=0, routes=0, seamChecks=0, disallowed=0;
    for(int seed=0;seed<12000;seed++){
      int x=3+seed%17,z=4+(seed/13)%20;
      int width=36+seed%30, depth=26+(seed/4)%30;
      Rect yard=new Rect(x,z,x+width-1,z+depth-1);
      int west=1+seed%3, north=1+(seed/3)%3, east=1+(seed/7)%3, south=1+(seed/11)%3;
      Rect original=new Rect(yard.x0()+west,yard.z0()+north,yard.x1()-east,yard.z1()-south);
      Rect main=new Rect(original.x0()+10,original.z0(),original.x1(),original.z1());
      Lot lot=new Lot(yard, seed%2==0? LotType.FACTORY:LotType.WAREHOUSE,
          CityHash.of(seed,seed*11L,seed*13L));
      Rect shack=LinkedServiceYards56.shack(yard,main,true);
      require(shack.width()>=5,"Workshop too narrow");
      Rect cross=LinkedServiceYards56.passage(yard,shack,main);
      require(cross!=null,"Expected service passage missing: "+seed);
      require(cross.width()==3 && cross.depth()==3,"Not a genuine three-wide service passage");
      require(shack.x1()+1==cross.x0()&&cross.x1()+1==main.x0(),"Detached passage");
      require(yard.contains(shack.x0(),shack.z0())&&yard.contains(shack.x1(),shack.z1()),"Shack outside lot");
      int row=cross.centerZ();
      Canvas full=new Canvas(0,145,0,145,39);
      // Deliberately put cargo in the factory vestibule: a real passage must clear it.
      full.set(main.x0()+1,CityPlan.GROUND_Y,row,Blocks.BARREL);
      full.set(main.x0()+2,CityPlan.GROUND_Y,row,Blocks.BARREL);
      painted(full,lot,shack,main);
      require(full.get(shack.x1(),CityPlan.GROUND_Y,row)==Blocks.SPRUCE_DOOR,"Workshop side door absent");
      require(full.get(main.x0(),CityPlan.GROUND_Y,row)==Blocks.SPRUCE_DOOR,"Factory side door absent");
      for(int xx=cross.x0();xx<=cross.x1();xx++)for(int zz=cross.z0();zz<=cross.z1();zz++){
        require(full.get(xx,CityPlan.STREET_Y,zz)==Blocks.POLISHED_ANDESITE,"Service path not paved");
        require(full.get(xx,CityPlan.GROUND_Y+4,zz)==Blocks.WAXED_WEATHERED_CUT_COPPER,"Service corridor has no shelter");
        for(int yy=CityPlan.GROUND_Y;yy<=CityPlan.GROUND_Y+3;yy++)
          require(full.get(xx,yy,zz)==Blocks.AIR,"Service corridor obstructed");
      }
      require(full.get(main.x0()+1,CityPlan.GROUND_Y,row)==Blocks.AIR,"Cargo still blocks hall entrance");
      require(full.get(main.x0()+2,CityPlan.GROUND_Y,row)==Blocks.AIR,"Cargo still blocks passage vestibule");
      for(String key:full.world.keySet()){
        String[] s=key.split(":");
        int xx=Integer.parseInt(s[0]),zz=Integer.parseInt(s[2]);
        require(yard.contains(xx,zz),"Wrote outside owner's parcel "+key);
      }
      if(seed%19==0){
        int split=main.x0()-2;
        Canvas left=new Canvas(0,split,0,145,39);
        Canvas right=new Canvas(split+1,145,0,145,39);
        left.set(main.x0()+1,CityPlan.GROUND_Y,row,Blocks.BARREL);
        right.set(main.x0()+1,CityPlan.GROUND_Y,row,Blocks.BARREL);
        left.set(main.x0()+2,CityPlan.GROUND_Y,row,Blocks.BARREL);
        right.set(main.x0()+2,CityPlan.GROUND_Y,row,Blocks.BARREL);
        painted(left,lot,shack,main);painted(right,lot,shack,main);
        Map<String,Block> union=new HashMap<>(left.world);union.putAll(right.world);
        require(union.equals(full.world),"Chunk seam nondeterministic at "+seed);
        seamChecks++;
      }
      if(seed%17==0){
        Rect oldShack=LinkedServiceYards56.shack(yard,main,false);
        require(oldShack.x1()==main.x0()-2,"Old save workshop geometry changed");
        require(LinkedServiceYards56.passage(yard,oldShack,main)==null,"Legacy narrow yard received connector");
        Canvas old=new Canvas(0,145,0,145,38);
        painted(old,lot,shack,main);
        require(old.world.isEmpty(),"Old version changed");
        disallowed++;
      }
      tested++;routes++;
    }
    require(seamChecks>500&&disallowed>500,"Insufficient regression fixtures");
    System.out.printf("PASS Pass56 real Java geometry: %d industrial lots, %d sheltered routes, %d chunk seam checks, %d legacy v38 unchanged cases%n",tested,routes,seamChecks,disallowed);
  }
}
