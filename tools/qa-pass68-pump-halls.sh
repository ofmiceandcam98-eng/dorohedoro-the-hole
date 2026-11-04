#!/usr/bin/env bash
# Compile the real Pass68 worldgen planner and painter against Minecraft-type stubs;
# test stable placement, room use, dry sewer lanes, long seams and old save isolation.
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
cat > "$TMP/net/minecraft/world/level/block/PistonBaseBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class PistonBaseBlock { public static final Object FACING="facing"; }
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
cat > "$PKG/VerifyPump68.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
public final class VerifyPump68 {
 static long checks=0;
 static void ok(boolean yes,String message){checks++;if(!yes)throw new AssertionError(message);}
 static int x(boolean axis,int trunk,int along,int side,int distance,int offset){return axis?trunk+side*distance:along+offset;}
 static int z(boolean axis,int trunk,int along,int side,int distance,int offset){return axis?along+offset:trunk+side*distance;}
 static boolean crossingFree(CityPlan p,boolean axis,int along){
  List<Line> lines=axis?p.zLinesNear(along-60,along+60):p.xLinesNear(along-60,along+60);
  return lines.stream().noneMatch(line->Math.abs(along-line.center())<48);
 }
 static Canvas full(boolean axis,long seed,int section,int trunk,int index,CityPlan p){
  int along=StormwaterPumpHalls68.center(seed,axis,index,section),side=StormwaterPumpHalls68.side(seed,axis,index,section);
  int a=trunk+side*3,b=trunk+side*16;
  int low=Math.min(a,b),hi=Math.max(a,b);
  return axis?new Canvas(low,along-9,hi,along+9):new Canvas(along-9,low,along+9,hi);
 }
 public static void main(String[]args){
  int selected=0, valid=0, near=0, negative=0, seams=0, shore=0;
  for(long seed=-110;seed<110;seed++)for(int section=-18;section<=18;section++){
   boolean axis=(seed&1)==0;
   int trunk=axis?30:40,index=0,version=50;
   if(!StormwaterPumpHalls68.selected(seed,axis,index,section,version))continue;
   selected++;
   int along=StormwaterPumpHalls68.center(seed,axis,index,section);
   int side=StormwaterPumpHalls68.side(seed,axis,index,section);
   ok(along>=section*192+48&&along<section*192+144,"site escaped absolute section");
   ok(side==1||side==-1,"invalid side");
   ok(StormwaterPumpHalls68.selected(seed,axis,index,section,version),"selection nondeterministic");
   ok(StormwaterPumpHalls68.reservesServiceBay(seed,axis,index,along,version),"Pass67 bay not reserved");
   ok(!StormwaterPumpHalls68.reservesServiceBay(seed,axis,index,along,49),"legacy section reserved");
   if(along<0)negative++;
   if(!crossingFree(new CityPlan(seed,version,true),axis,along))continue;
   CityPlan plan=new CityPlan(seed,version,true);
   Canvas canvas=full(axis,seed,section,trunk,index,plan);
   StormwaterPumpHalls68.paint(canvas,plan);
   if(canvas.writes.isEmpty())continue;
   valid++;
   // Integration: the production Pass67 bay planner must reserve this entire hall
   // rather than carving a competing repair room into the same footprint.
   if(valid<=35){
    Canvas small=full(axis,seed,section,trunk,index,plan);
    SewerServiceBays67.paint(small,plan);
    ok(small.writes.isEmpty(),"Pass67 bay overlaps reserved Pass68 pump footprint");
   }
   // Existing municipal water and both TWO-BLOCK dry walkways are untouched.
   for(int offset=-9;offset<=9;offset++)for(int d=0;d<=2;d++) {
    ok(canvas.get(x(axis,trunk,along,side,d,offset),49,z(axis,trunk,along,side,d,offset))==null,"main sewer floor overwritten");
   }
   for(int d=3;d<=15;d++){
    // The central route remains clear all the way into the pump hall.
    if(d>=4)ok(canvas.get(x(axis,trunk,along,side,d,0),50,z(axis,trunk,along,side,d,0))==Blocks.AIR,
      "hall central route blocked at distance="+d);
   }
   for(int offset=-1;offset<=1;offset++)for(int y=50;y<=52;y++)
    ok(canvas.get(x(axis,trunk,along,side,3,offset),y,z(axis,trunk,along,side,3,offset))==Blocks.AIR,"3-wide doorway obstructed");
   for(int d=11;d<=13;d++)for(int off=-2;off<=2;off++)
    ok(canvas.get(x(axis,trunk,along,side,d,off),49,z(axis,trunk,along,side,d,off))==(off==0?Blocks.POLISHED_ANDESITE:Blocks.WATER),"sump or dry bridge failed");
   int[][] fixtures={{10,-7},{12,-7},{14,-7},{12,7},{14,7}};
   var blocks=List.of(Blocks.CRAFTING_TABLE,Blocks.FURNACE,Blocks.STONECUTTER,Blocks.CAULDRON,Blocks.GRINDSTONE);
   for(int i=0;i<fixtures.length;i++)ok(canvas.get(x(axis,trunk,along,side,fixtures[i][0],fixtures[i][1]),50,
     z(axis,trunk,along,side,fixtures[i][0],fixtures[i][1]))==blocks.get(i),"missing useful station "+i);
   int dx=x(axis,trunk,along,side,8,5), dz=z(axis,trunk,along,side,8,5);
   var bottom=canvas.writes.get(new Canvas.Pos(dx,50,dz));
   var upper=canvas.writes.get(new Canvas.Pos(dx,51,dz));
   ok(bottom!=null && bottom.block()==Blocks.IRON_DOOR&&bottom.properties().get(DoorBlock.HALF)==DoubleBlockHalf.LOWER,"iron door lower half missing");
   ok(upper!=null && upper.block()==Blocks.IRON_DOOR&&upper.properties().get(DoorBlock.HALF)==DoubleBlockHalf.UPPER,"iron door upper half missing");
   var lever=canvas.writes.get(new Canvas.Pos(x(axis,trunk,along,side,8,4),50,z(axis,trunk,along,side,8,4)));
   ok(lever!=null&&lever.block()==Blocks.LEVER&&lever.properties().get(LeverBlock.FACE)==AttachFace.FLOOR,"operator lever missing");
   if(valid<=3){
    int a=canvas.minX,b=canvas.maxX,c=canvas.minZ,d=canvas.maxZ;
    Map<Canvas.Pos,net.minecraft.world.level.block.state.BlockState> stitched=new HashMap<>();
    for(int cx=Math.floorDiv(a,16);cx<=Math.floorDiv(b,16);cx++)for(int cz=Math.floorDiv(c,16);cz<=Math.floorDiv(d,16);cz++){
      Canvas ch=new Canvas(cx*16,cz*16,cx*16+15,cz*16+15);
      StormwaterPumpHalls68.paint(ch,plan);
      for(var ent:ch.writes.entrySet())if(ent.getKey().x()>=a&&ent.getKey().x()<=b&&ent.getKey().z()>=c&&ent.getKey().z()<=d)stitched.put(ent.getKey(),ent.getValue());
    }
    ok(stitched.equals(canvas.writes),"chunk boundary mismatch at seed="+seed+" section="+section);
    seams++;
   }
   if(valid<=4){
    Canvas old=full(axis,seed,section,trunk,index,new CityPlan(seed,49,true));
    StormwaterPumpHalls68.paint(old,new CityPlan(seed,49,true));
    ok(old.writes.isEmpty(),"old 49 world changed");
    Canvas noSewers=full(axis,seed,section,trunk,index,new CityPlan(seed,50,false));
    StormwaterPumpHalls68.paint(noSewers,new CityPlan(seed,50,false));
    ok(noSewers.writes.isEmpty(),"sewer-disabled world changed");
    CityPlan ocean=new CityPlan(seed,50,true);ocean.ocean=true;
    Canvas sea=full(axis,seed,section,trunk,index,ocean);
    StormwaterPumpHalls68.paint(sea,ocean);ok(sea.writes.isEmpty(),"ocean basin excavated");
    CityPlan coast=new CityPlan(seed,50,true);coast.coast=true;
    Canvas shoreCanvas=full(axis,seed,section,trunk,index,coast);
    StormwaterPumpHalls68.paint(shoreCanvas,coast);ok(shoreCanvas.writes.isEmpty(),"beach excavated");
    shore++;
   }
  }
  ok(selected>1000,"too few station selections");
  ok(valid>200,"too few buildable pump stations");
  ok(negative>200,"too few negative-coordinate selections");
  ok(seams==3,"chunk stitching missing");
  // Deliberately force crossing near an otherwise valid site; all touching chunks
  // must skip the same site even though the crossing lies well outside a chunk.
  long blocked=0;
  for(long seed=0;seed<200;seed++)for(int section=-2;section<=2;section++){
   if(!StormwaterPumpHalls68.selected(seed,true,0,section,50))continue;
   int along=StormwaterPumpHalls68.center(seed,true,0,section);
   CityPlan forced=new CityPlan(seed,50,true);forced.alwaysCross=true;
   // The fixed crossing at z=40 blocks centers only within its 48 radius.
   if(Math.abs(along-40)>=48)continue;
   Canvas whole=full(true,seed,section,30,0,forced);
   StormwaterPumpHalls68.paint(whole,forced);
   ok(whole.writes.isEmpty(),"near-junction pump generated");
   blocked++;
  }
  ok(blocked>5,"insufficient forced crossing cases");
  System.out.println("PASS Pass68 Java checks="+checks+" selected="+selected+" built="+valid+" negatives="+negative+" seams="+seams+" coastalGuards="+shore+" rejectedAtCrossing="+blocked);
 }
}
JAVA
javac -d "$TMP/out" $(find "$TMP" -name '*.java') src/main/java/com/dorohedoro/hole/worldgen/city/{StormwaterPumpHalls68,SluiceValve69,SewerServiceBays67,CityHash,Line,Rect}.java
java -Xmx1g -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifyPump68
