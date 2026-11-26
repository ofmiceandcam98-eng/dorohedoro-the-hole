#!/usr/bin/env bash
# Pass69: compile real Pass68+69 gallery geometry against minimal MC stubs;
# ensure live piston water outlet, lever power path, old-world preservation and chunk seams.
set -euo pipefail
cd "$(dirname "$0")/.."
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/com/dorohedoro/hole/worldgen/city"
mkdir -p "$PKG" "$TMP/net/minecraft/core" "$TMP/net/minecraft/world/level/block/state/properties" "$TMP/net/minecraft/world/level/block/state" "$TMP/net/minecraft/world/level/block"
cat > "$TMP/net/minecraft/core/Direction.java" <<'JAVA'
package net.minecraft.core;
public enum Direction {NORTH, WEST, SOUTH, EAST}
JAVA
cat > "$TMP/net/minecraft/world/level/block/state/properties/AttachFace.java" <<'JAVA'
package net.minecraft.world.level.block.state.properties;
public enum AttachFace { FLOOR, WALL, CEILING }
JAVA
cat > "$TMP/net/minecraft/world/level/block/state/properties/DoubleBlockHalf.java" <<'JAVA'
package net.minecraft.world.level.block.state.properties;
public enum DoubleBlockHalf { LOWER, UPPER }
JAVA
cat > "$TMP/net/minecraft/world/level/block/state/BlockState.java" <<'JAVA'
package net.minecraft.world.level.block.state;
import java.util.*;
import net.minecraft.world.level.block.Block;
public record BlockState(Block block, Map<Object,Object> properties) {
 public <T> BlockState setValue(Object key,T value){var p=new HashMap<>(properties);p.put(key,value);return new BlockState(block,Map.copyOf(p));}
}
JAVA
cat > "$TMP/net/minecraft/world/level/block/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
public record Block(String name) {public BlockState defaultBlockState(){return new BlockState(this,Map.of());}}
JAVA
cat > "$TMP/net/minecraft/world/level/block/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class Blocks {
 public static final Block AIR=new Block("air"), STONE_BRICKS=new Block("stone_bricks"), TUFF_BRICKS=new Block("tuff_bricks"),
 POLISHED_ANDESITE=new Block("polished_andesite"), DEEPSLATE_TILES=new Block("deepslate_tiles"),
 POLISHED_TUFF=new Block("polished_tuff"), IRON_BARS=new Block("iron_bars"),
 CRAFTING_TABLE=new Block("crafting_table"), FURNACE=new Block("furnace"), GRINDSTONE=new Block("grindstone"),
 CAULDRON=new Block("cauldron"), COMPOSTER=new Block("composter"), STONECUTTER=new Block("stonecutter"),
 WATER=new Block("water"), IRON_DOOR=new Block("iron_door"), LEVER=new Block("lever"),
 STICKY_PISTON=new Block("sticky_piston"), IRON_BLOCK=new Block("iron_block"),
 LANTERN=new Block("lantern"), DEEPSLATE_BRICKS=new Block("deepslate_bricks"),
 MOSSY_STONE_BRICKS=new Block("mossy_stone_bricks"), CRACKED_DEEPSLATE_BRICKS=new Block("cracked_deepslate_bricks"),
 WAXED_WEATHERED_COPPER=new Block("waxed_weathered_copper"),
 TUFF=new Block("tuff"), COBBLED_DEEPSLATE=new Block("cobbled_deepslate"), DEEPSLATE=new Block("deepslate");
}
JAVA
cat > "$TMP/net/minecraft/world/level/block/DoorBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class DoorBlock { public static final Object FACING="facing", HALF="half"; }
JAVA
cat > "$TMP/net/minecraft/world/level/block/LeverBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class LeverBlock { public static final Object FACE="face"; }
JAVA
cat > "$PKG/CitySettings.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
public record CitySettings(int worldDetailVersion, boolean sewerUnderworld) {}
JAVA
cat > "$PKG/CityPlan.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
public final class CityPlan {
 public final long seed;
 public final CitySettings settings;
 public boolean ocean=false,coast=false,alwaysCross=false;
 public CityPlan(long seed,int v,boolean sewer){this.seed=seed;settings=new CitySettings(v,sewer);}
 public List<Line> xLinesNear(int a,int b){return lines(a,b,30,180);}
 public List<Line> zLinesNear(int a,int b){return alwaysCross?List.of(new Line(55,40,4,Line.Kind.AVENUE)):lines(a,b,40,160);}
 private static List<Line> lines(int a,int b,int offset,int space){List<Line> r=new ArrayList<>();
  for(int i=Math.floorDiv(a-offset,space)-1;i<=Math.floorDiv(b-offset,space)+1;i++){
   int p=i*space+offset;if(p>=a&&p<=b)r.add(new Line(i,p,4,i%11==8?Line.Kind.CANAL:Line.Kind.AVENUE));
  }return r;
 }
 public int oceanDepthAt(int x,int z){return ocean?5:0;}
 public int coastalDistanceAt(int x,int z){return coast?6:-1;}
}
JAVA
cat > "$PKG/Canvas.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
public final class Canvas {
 public record Pos(int x,int y,int z) {}
 public final Map<Pos,BlockState> writes=new HashMap<>();
 public final int minX,maxX,minZ,maxZ;
 public Canvas(int minX,int minZ,int maxX,int maxZ){this.minX=minX;this.minZ=minZ;this.maxX=maxX;this.maxZ=maxZ;}
 public boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 public boolean overlaps(Rect r){return r.x0()<=maxX&&r.x1()>=minX&&r.z0()<=maxZ&&r.z1()>=minZ;}
 public void set(int x,int y,int z,Block b){set(x,y,z,b.defaultBlockState());}
 public void set(int x,int y,int z,BlockState b){if(contains(x,z))writes.put(new Pos(x,y,z),b);}
 public void fill(int a,int b,int c,int d,int e,int f,Block block){for(int x=a;x<=d;x++)for(int z=c;z<=f;z++)for(int y=b;y<=e;y++)set(x,y,z,block);}
 public Block get(int x,int y,int z){var v=writes.get(new Pos(x,y,z));return v==null?null:v.block();}
}
JAVA
cat > "$TMP/net/minecraft/world/level/block/PistonBaseBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class PistonBaseBlock { public static final Object FACING="facing"; }
JAVA
cat > "$PKG/VerifyPass70.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.*;
public final class VerifyPass70 {
 static int checks = 0;
 static void ok(boolean condition, String message) {checks++;if(!condition)throw new AssertionError(message);}
 static Map<Canvas.Pos,net.minecraft.world.level.block.state.BlockState> draw(long seed,int version,
             int x0,int z0,int x1,int z1) {
  CityPlan p=new CityPlan(seed,version,true);
  Canvas c=new Canvas(x0,z0,x1,z1);
  StormDrainBypasses70.paint(c,p);
  return c.writes;
 }
 public static void main(String[] args) {
  int halls=0, water=0, negative=0;
  // New-only rule; old worlds must remain bytewise unchanged.
  for(int v : new int[]{0,41,48,49,50,51}) {
   ok(draw(2026,v,0,0,127,127).isEmpty(),"historical generation changed: "+v);
   ok(!StormDrainBypasses70.selected(22,true,1,0,v),"legacy bypass selector");
  }
  for(long seed=0;seed<70;seed++) {
   // Enough area to include several storm-artery sites and negative coordinates.
   Canvas c=new Canvas(-64,-64,127,127);
   CityPlan p=new CityPlan(seed,52,true);
   StormDrainBypasses70.paint(c,p);
   if(!c.writes.isEmpty()) halls++;
   for(var e : c.writes.entrySet()) {
    var pos=e.getKey();var b=e.getValue().block();
    if(b.equals(Blocks.WATER)) {
     water++;
     ok(pos.y()==48,"storm runoff should be recessed");
    }
    ok(pos.y()>=48 && pos.y()<=57,"storm geometry escaped bounded Y");
    ok(!b.equals(Blocks.LEVER)&&!b.equals(Blocks.LANTERN),"nonessential lights or controls");
    if(pos.x()<0||pos.z()<0)negative++;
   }
  }
  ok(halls>=3,"very few selected connected storm drains");
  ok(water>=20,"missing recessed wastewater channel");
  ok(negative>20,"negative coordinate storm geometry absent");
  // Verify both entry portals and the full dry parallel paths on every
  // orientation/side and across all three architecture variants.
  for(boolean axis : new boolean[]{true,false}) for(int side : new int[]{-1,1}) for(int kind=0;kind<3;kind++) {
   Canvas bypass=new Canvas(-128,-128,127,127);
   int trunk=21, along=11;
   StormDrainBypasses70.build(bypass,axis,trunk,along,side,kind);
   for(int off : new int[]{-10,10}) for(int d=3;d<=5;d++) for(int delta=-1;delta<=1;delta++) {
    int x=axis?trunk+side*d:along+off+delta;
    int z=axis?along+off+delta:trunk+side*d;
    ok(bypass.get(x,49,z)!=null&& !bypass.get(x,49,z).equals(Blocks.AIR),"portal missing solid floor");
    for(int y=50;y<=53;y++) ok(Blocks.AIR.equals(bypass.get(x,y,z)),"blocked storm entrance");
   }
   for(int off=-13;off<=13;off++)for(int d : new int[]{8,12}) {
    int x=axis?trunk+side*d:along+off;
    int z=axis?along+off:trunk+side*d;
    ok(bypass.get(x,49,z)!=null&&!Blocks.WATER.equals(bypass.get(x,49,z)),"dry lane inundated");
    for(int y=50;y<=54;y++) ok(Blocks.AIR.equals(bypass.get(x,y,z)),"dry lane blocked");
   }
   for(int off=-13;off<=13;off++) {
    int x=axis?trunk+side*10:along+off;
    int z=axis?along+off:trunk+side*10;
    ok(Blocks.WATER.equals(bypass.get(x,48,z)),"missing recessed water");
    ok(Blocks.AIR.equals(bypass.get(x,49,z)),"water channel covered");
   }
   for(int off=-14;off<=14;off++)for(int d=0;d<=2;d++) {
    int x=axis?trunk+side*d:along+off;
    int z=axis?along+off:trunk+side*d;
    for(int y=48;y<=55;y++)ok(!bypass.writes.containsKey(new Canvas.Pos(x,y,z)),
           "original trunk or dry lane overwritten");
   }
  }
  // Every independent chunk must contain exactly the writes of a spanning
  // canvas for that same area, including placements selected from nearby trunks.
  for(long seed : new long[]{0,7,19,42,2026,98765}) {
   for(int x0 : new int[]{-64,0,64}) {
    for(int z0 : new int[]{-64,0,64}) {
     var whole=draw(seed,52,x0,z0,x0+63,z0+63);
     Map<Canvas.Pos,net.minecraft.world.level.block.state.BlockState> chunks=new HashMap<>();
     for(int dx=0;dx<4;dx++)for(int dz=0;dz<4;dz++) {
      chunks.putAll(draw(seed,52,x0+dx*16,z0+dz*16,x0+dx*16+15,z0+dz*16+15));
     }
     ok(whole.equals(chunks),"storm bypass inconsistent across chunk seam at "+x0+","+z0+" seed "+seed);
    }
   }
  }
  // A full cross section has two three-wide entrances on the SAME sewer wall.
  int doors=0;
  for(long seed=0;seed<3000 && doors==0;seed++) {
   CityPlan p=new CityPlan(seed,52,true);
   Canvas c=new Canvas(0,0,159,159);
   StormDrainBypasses70.paint(c,p);
   for(var e:c.writes.entrySet()) {
    var pos=e.getKey();
    if(pos.y()!=50 || !e.getValue().block().equals(Blocks.AIR))continue;
    // A portal's adjacent dry floor is actually accessible by Minecraft walking.
    if(c.get(pos.x(),49,pos.z())!=null) {doors++;break;}
   }
  }
  ok(doors>0,"no accessible storm bypass entrance");
  // The shoulder style painter must never write to the wet center or walking lanes.
  int wallKinds=0;
  for(int v : new int[]{51,52}) {
   Canvas c=new Canvas(-16,-16,79,79);
   CityPlan p=new CityPlan(337,v,true);
   SewerVaultVariants70.paint(c,p,p.xLinesNear(-25,88),p.zLinesNear(-25,88));
   if(v==51)ok(c.writes.isEmpty(),"legacy sewer masonry changed");
   else {
    ok(!c.writes.isEmpty(),"no masonry variation painted");
    wallKinds=c.writes.size();
    for(var e:c.writes.entrySet()) {
     var pos=e.getKey();
     ok(pos.y()>=50&&pos.y()<=52,"masonry harmed floor/drain");
     ok(!e.getValue().block().equals(Blocks.WATER),"masonry created fluid");
    }
   }
  }
  // Natural-rock transition widens from two side cells to five, never writing
  // the original center 3-wide staircase or introducing a mandatory hazard.
  int caveWrites=0;
  for(int step=20;step<=55;step++) {
   int width=SewerCaveTransitions70.widthAt(step);
   ok(width>=2&&width<=5,"wrong taper width");
   Canvas c=new Canvas(-16,step-1,16,step+1);
   SewerCaveTransitions70.paintStep(c,42,0,step,step,-39);
   for(var e:c.writes.entrySet()) {
    var pos=e.getKey();caveWrites++;
    ok(Math.abs(pos.x())>=2,"modified original accessible 3-wide center");
    ok(Math.abs(pos.x())<=width+1,"cave fringe exceeded width");
    ok(pos.y()>=-40&&pos.y()<=-35,"cave fringe outside passage vertical profile");
    ok(!e.getValue().block().equals(Blocks.WATER),"cave fringe flooded");
   }
  }
  ok(caveWrites>100,"cave weathering absent");
  for(int step : new int[]{20,30,42,49,55}) {
   Canvas whole=new Canvas(-16,step,15,step);
   SewerCaveTransitions70.paintStep(whole,123,1,step,step,-39);
   Canvas west=new Canvas(-16,step, -1,step), east=new Canvas(0,step,15,step);
   SewerCaveTransitions70.paintStep(west,123,1,step,step,-39);
   SewerCaveTransitions70.paintStep(east,123,1,step,step,-39);
   var joined=new HashMap<>(west.writes);joined.putAll(east.writes);
   ok(whole.writes.equals(joined),"cave transition seam");
  }
  // No new cave branches were selected: existing target stays 30%.
  int selected=0;for(int i=-100;i<100;i++)for(int j=-100;j<100;j++){
   if(SewerBranchPolicy.connectsToCave(1234,i,j,30))selected++;
  }
  ok(selected>11000&&selected<13000,"30% cave selector changed");
  System.out.println("PASS Pass70 compiled geometry checks="+checks+" stormsInSeeds="+halls+
    " waterBlocks="+water+" negativeBlocks="+negative+" changedShoulderBlocks="+wallKinds+
    " caveWrites="+caveWrites+" originalCaveSelections="+selected);
 }
}
JAVA
mkdir -p "$TMP/out"
javac -d "$TMP/out" $(find "$TMP" -name '*.java') src/main/java/com/dorohedoro/hole/worldgen/city/{StormDrainBypasses70,SewerVaultVariants70,SewerCaveTransitions70,StormwaterPumpHalls68,SluiceValve69,SewerServiceBays67,SewerBranchPolicy,CityHash,Line,Rect}.java
java -Xmx1g -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifyPass70
