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
 STICKY_PISTON=new Block("sticky_piston"), IRON_BLOCK=new Block("iron_block");
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
cat > "$PKG/VerifySluice69.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
public final class VerifySluice69 {
 static long checks=0,selected=0,built=0,seams=0,legacy=0,negative=0;
 static void yes(boolean c,String s){checks++;if(!c)throw new AssertionError(s);}
 static int x(boolean axis,int trunk,int along,int side,int d,int off){return axis?trunk+side*d:along+off;}
 static int z(boolean axis,int trunk,int along,int side,int d,int off){return axis?along+off:trunk+side*d;}
 static Canvas.Pos pos(boolean axis,int trunk,int along,int side,int d,int off,int y){
  return new Canvas.Pos(x(axis,trunk,along,side,d,off),y,z(axis,trunk,along,side,d,off)); }
 static Canvas whole(boolean axis,int trunk,int along){
  return axis?new Canvas(trunk-19,along-11,trunk+19,along+11):new Canvas(along-11,trunk-19,along+11,trunk+19);}
 static boolean built(Canvas c){return !c.writes.isEmpty();}
 static Block at(Canvas c,boolean axis,int trunk,int along,int side,int d,int off,int y){
  return c.get(x(axis,trunk,along,side,d,off),y,z(axis,trunk,along,side,d,off)); }
 static boolean free(CityPlan plan,boolean axis,int along){
  var crosses=axis?plan.zLinesNear(along-60,along+60):plan.xLinesNear(along-60,along+60);
  for(Line l:crosses)if(Math.abs(along-l.center())<48)return false;
  return true;
 }
 public static void main(String[] args) {
  for(long seed=-127;seed<=127;seed++)for(int section=-10;section<=10;section++) {
   boolean axis=(seed&1)==0;int trunk=axis?30:40,index=0;
   if(!StormwaterPumpHalls68.selected(seed,axis,index,section,51))continue;
   selected++;
   int along=StormwaterPumpHalls68.center(seed,axis,index,section);
   int side=StormwaterPumpHalls68.side(seed,axis,index,section);
   if(along<0)negative++;
   CityPlan plan=new CityPlan(seed,51,true);
   if(!free(plan,axis,along))continue;
   Canvas c=whole(axis,trunk,along);StormwaterPumpHalls68.paint(c,plan);
   if(!built(c))continue;
   built++;
   // The original main municipal waterway and both side walkways are preserved.
   for(int off=-9;off<=9;off++)for(int d=0;d<=2;d++)
     yes(at(c,axis,trunk,along,side,d,off,49)==null,"municipal lane altered");
   // Control outlet: 2 cells of air above a non-erodible stone bed, never sources.
   for(int d=14;d<=15;d++){
    yes(at(c,axis,trunk,along,side,d,-2,49)==Blocks.AIR,"outlet obstructed");
    yes(at(c,axis,trunk,along,side,d,-2,48)==Blocks.POLISHED_TUFF,"outlet lacks lined bed");
   }
   // Adjacent source remains the ONLY refill point, no self-sustaining downstream source.
   yes(at(c,axis,trunk,along,side,13,-2,49)==Blocks.WATER,"upstream source removed");
   var piston=c.writes.get(pos(axis,trunk,along,side,14,-4,49));
   yes(piston!=null&&piston.block()==Blocks.STICKY_PISTON,"missing sticky piston");
   yes(piston.properties().get(PistonBaseBlock.FACING)==(axis?Direction.SOUTH:Direction.EAST),"piston facing away from outlet");
   yes(at(c,axis,trunk,along,side,14,-3,49)==Blocks.IRON_BLOCK,"piston load is not a movable iron block");
   yes(at(c,axis,trunk,along,side,14,-5,49)==Blocks.STONE_BRICKS,"lever lacks solid powered base next to piston");
   var lever=c.writes.get(pos(axis,trunk,along,side,14,-5,50));
   yes(lever!=null&&lever.block()==Blocks.LEVER&&lever.properties().get(LeverBlock.FACE)==AttachFace.FLOOR,"lever unsupported or disconnected");
   // Three-wide public entrance, central bridge and workbench survive new mechanism.
   for(int off=-1;off<=1;off++)for(int y=50;y<=52;y++)
     yes(at(c,axis,trunk,along,side,3,off,y)==Blocks.AIR,"entrance blocked");
   for(int d=11;d<=15;d++)
     yes(at(c,axis,trunk,along,side,d,0,49)==Blocks.POLISHED_ANDESITE||at(c,axis,trunk,along,side,d,0,49)==Blocks.DEEPSLATE_TILES,"crossing removed");
   yes(at(c,axis,trunk,along,side,10,-7,50)==Blocks.CRAFTING_TABLE,"workshop lost");
   yes(at(c,axis,trunk,along,side,14,-7,50)==Blocks.STONECUTTER,"stonecutter lost");
   // A v50 world must preserve exactly the original paint outputs, and the
   // v51 delta must consist solely of this sluice on an otherwise identical hall.
   if(legacy<40){
    Canvas old=whole(axis,trunk,along);
    StormwaterPumpHalls68.paint(old,new CityPlan(seed,50,true));
    yes(built(old),"v50 pump gallery unexpectedly skipped");
    for(var entry:old.writes.entrySet()) {
      var key=entry.getKey();
      if(key.equals(pos(axis,trunk,along,side,14,-4,49))||key.equals(pos(axis,trunk,along,side,14,-3,49))||
         key.equals(pos(axis,trunk,along,side,14,-5,49))||key.equals(pos(axis,trunk,along,side,14,-5,50))||
         key.equals(pos(axis,trunk,along,side,14,-2,49))||key.equals(pos(axis,trunk,along,side,14,-2,48))||
         key.equals(pos(axis,trunk,along,side,15,-2,49))||key.equals(pos(axis,trunk,along,side,15,-2,48)))continue;
      yes(entry.getValue().equals(c.writes.get(key)),"unexpected existing room change at "+key);
    }
    legacy++;
   }
   if(seams<7) {
    Map<Canvas.Pos,BlockState> stitched=new HashMap<>();
    for(int cx=Math.floorDiv(c.minX,16);cx<=Math.floorDiv(c.maxX,16);cx++)
      for(int cz=Math.floorDiv(c.minZ,16);cz<=Math.floorDiv(c.maxZ,16);cz++) {
       Canvas ch=new Canvas(cx*16,cz*16,cx*16+15,cz*16+15);
       StormwaterPumpHalls68.paint(ch,plan);
       for(var e:ch.writes.entrySet())
        if(e.getKey().x()>=c.minX&&e.getKey().x()<=c.maxX&&e.getKey().z()>=c.minZ&&e.getKey().z()<=c.maxZ)
         stitched.put(e.getKey(),e.getValue());
      }
    yes(stitched.equals(c.writes),"chunk stitching mismatch "+seed+":"+along);
    seams++;
   }
  }
  yes(selected>900,"insufficient seed coverage");
  yes(built>200,"too few pump halls with mechanical sluices");
  yes(seams==7&&legacy==40&&negative>300,"missing boundary, legacy or negative coordinate coverage");
  System.out.println("PASS Pass69 sluices checks="+checks+" selected="+selected+" built="+built+" seams="+seams+" legacy="+legacy+" negative="+negative);
 }
}
JAVA
javac -d "$TMP/out" $(find "$TMP" -name '*.java') src/main/java/com/dorohedoro/hole/worldgen/city/{StormwaterPumpHalls68,SluiceValve69,SewerServiceBays67,CityHash,Line,Rect}.java
java -Xmx1g -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifySluice69
