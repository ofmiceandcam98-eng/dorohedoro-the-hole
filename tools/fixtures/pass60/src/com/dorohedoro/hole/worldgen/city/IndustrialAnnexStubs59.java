package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
class CityPlan { static final int STREET_Y=64, GROUND_Y=65; }
record Lot(Rect rect, LotType type, long hash) {}
record Mix(Block material) {static Mix of(Block a,int aWeight,Block b,int bWeight){return new Mix(a);}}
class Canvas {
  final int x0,x1,z0,z1, version;
  final Map<String,Block> world=new HashMap<>();
  Canvas(int x0,int x1,int z0,int z1,int version){this.x0=x0;this.x1=x1;this.z0=z0;this.z1=z1;this.version=version;}
  boolean hasIndustrialAnnex58(){return version>=41;}
  boolean hasWorkingServiceCourts59(){return version>=42;}
  boolean hasFreightApproaches60(){return version>=43;}
  boolean hasLinkedServiceYards56(){return version>=39;}
  static String key(int x,int y,int z){return x+":"+y+":"+z;}
  void set(int x,int y,int z,Block b){if(x>=x0&&x<=x1&&z>=z0&&z<=z1)world.put(key(x,y,z),b);}
  Block get(int x,int y,int z){return world.get(key(x,y,z));}
  void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block b){for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++)set(x,y,z,b);}
  void fill(Rect r,int y0,int y1,Block b){fill(r.x0(),y0,r.z0(),r.x1(),y1,r.z1(),b);}
  void fill(Rect r,int y0,int y1,Mix mix){fill(r,y0,y1,mix.material());}
  void clear(Rect r,int y0,int y1){fill(r,y0,y1,Blocks.AIR);}
  void walls(Rect r,int y0,int y1,Mix m){fill(r.x0(),y0,r.z0(),r.x1(),y1,r.z0(),m.material());fill(r.x0(),y0,r.z1(),r.x1(),y1,r.z1(),m.material());fill(r.x0(),y0,r.z0(),r.x0(),y1,r.z1(),m.material());fill(r.x1(),y0,r.z0(),r.x1(),y1,r.z1(),m.material());}
  void pane(int x,int y,int z,Direction.Axis a,Block b){set(x,y,z,b);}
  void barrel(int x,int y,int z,String loot){set(x,y,z,Blocks.BARREL);}
  void door(int x,int y,int z,Direction direction,Block b){set(x,y,z,b);set(x,y+1,z,b);}
  void ladder(int x,int y0,int y1,int z,Direction facing){for(int y=y0;y<=y1;y++)set(x,y,z,Blocks.LADDER);}
}

class WorldDetail {
  static Rect footprint(Canvas c, Lot lot) {
    Rect r=lot.rect();
    // Production WorldDetail varies the front setback. Keep two realistic cases
    // to exercise both direct cargo access and full three-wide transverse courts.
    int south=CityHash.range(CityHash.mix(lot.hash()^0x51A77L),7)==0?3:1;
    return new Rect(r.x0()+1,r.z0()+1,r.x1()-1,r.z1()-south);
  }
}
class StructuralVolumes14 {
  static Rect industrialFootprint(Canvas c, Lot lot, Rect original) {
    if(lot.rect().width()<36||lot.rect().depth()<26||original.width()<31||original.depth()<20) return original;
    return new Rect(original.x0()+10,original.z0(),original.x1(),original.z1());
  }
}
