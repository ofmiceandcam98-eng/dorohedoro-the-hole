#!/usr/bin/env bash
# Compiles the actual three new Pass 43 city classes with a narrow, auditable
# Minecraft API test double. THIS IS NOT A NEOFORGE BUILD or an in-game test.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/com/dorohedoro/hole/worldgen/city"
NB="$TMP/net/minecraft/world/level/block"
NS="$TMP/net/minecraft/world/level/block/state"
ND="$TMP/net/minecraft/core"
mkdir -p "$PKG" "$NB" "$NS" "$ND"
cp "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/"{CityHash,Rect,Line,OccupiedCommons43,Backstreets43,CanalWorksites43}.java "$PKG/"
cat > "$ND/Direction.java" <<'JAVA'
package net.minecraft.core;
public enum Direction { EAST; }
JAVA
cat > "$NB/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
import net.minecraft.world.level.block.state.BlockState;
public class Block {
 public final String id;
 public Block(String s){id=s;}
 public BlockState defaultBlockState(){return new BlockState(this);}
}
JAVA
cat > "$NB/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public class Blocks {
 public static final Block AIR=new Block("air"), COBBLESTONE=new Block("cobble"),
 BROWN_WOOL=new Block("brown_wool"), GRAY_WOOL=new Block("gray_wool"), GREEN_WOOL=new Block("green_wool"),
 ORANGE_WOOL=new Block("orange_wool"), CYAN_WOOL=new Block("cyan_wool"),
 PACKED_MUD=new Block("packed_mud"), CRACKED_STONE_BRICKS=new Block("cracked_stone"),
 SPRUCE_STAIRS=new Block("stairs"), CAULDRON=new Block("cauldron"), COMPOSTER=new Block("composter"),
 STONECUTTER=new Block("stonecutter"), DARK_OAK_FENCE=new Block("dark_fence"), SPRUCE_FENCE=new Block("spruce_fence"),
 SMOKER=new Block("smoker"), LOOM=new Block("loom"), FLETCHING_TABLE=new Block("fletching"),
 DRIED_KELP_BLOCK=new Block("dried_kelp"), WHITE_WOOL=new Block("white_wool"), COBBLED_DEEPSLATE=new Block("cobbled_deepslate"),
 STRIPPED_SPRUCE_LOG=new Block("stripped_log"), PUMPKIN=new Block("pumpkin"), BARREL=new Block("barrel"),
 TUFF_BRICKS=new Block("tuff_bricks"), POLISHED_BLACKSTONE=new Block("polished_blackstone"),
 GRAVEL=new Block("gravel"), COARSE_DIRT=new Block("coarse_dirt"), CRACKED_DEEPSLATE_BRICKS=new Block("cracked_deepslate"),
 GRINDSTONE=new Block("grindstone");
}
JAVA
cat > "$NB/StairBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public class StairBlock {public static final String FACING="facing";}
JAVA
cat > "$NS/BlockState.java" <<'JAVA'
package net.minecraft.world.level.block.state;
import net.minecraft.world.level.block.*;
public class BlockState {
 public final Block block;
 public BlockState(Block b){this.block=b;}
 public BlockState setValue(String s,Object v){return this;}
 public boolean isAir(){return block==Blocks.AIR;}
 public boolean is(Block b){return block==b;}
}
JAVA
cat > "$PKG/TestStubs.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
class Canvas {
 public final int minX,maxX,minZ,maxZ;
 private final Map<String,String> changed = new TreeMap<>();
 Canvas(int minX,int maxX,int minZ,int maxZ){this.minX=minX;this.maxX=maxX;this.minZ=minZ;this.maxZ=maxZ;}
 private String key(int x,int y,int z){return x+","+y+","+z;}
 boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 boolean hasOccupiedCommons43(){return true;}
 long hash(int x,int y,int z){return CityHash.of(24681L,x,y,z);}
 BlockState get(int x,int y,int z){String v=changed.get(key(x,y,z));if(v!=null)return new BlockState(new Block(v));return (y<=64?Blocks.COBBLESTONE:Blocks.AIR).defaultBlockState();}
 void set(int x,int y,int z,Block b){if(contains(x,z))changed.put(key(x,y,z),b.id);}
 void set(int x,int y,int z,BlockState s){set(x,y,z,s.block);}
 void fill(int x,int y,int z,int x1,int y1,int z1,Block b){for(int a=x;a<=x1;a++)for(int d=y;d<=y1;d++)for(int e=z;e<=z1;e++)set(a,d,e,b);}
 void barrel(int x,int y,int z,String table){set(x,y,z,Blocks.BARREL);}
 Map<String,String> result(){return changed;}
}
record Lot(Rect rect,long hash){}
enum District {RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT, DERELICT, GRAVEYARD, OUTSKIRTS}
class CityPlan {
 static final int STREET_Y=64,GROUND_Y=65;
 final long seed=24681L;
 Settings settings=new Settings();
 static class Settings {int worldDetailVersion(){return 27;} int sewerCavernVersion(){return 5;}}
 HoleRegions42.Region regionAt(int x,int z){return HoleRegions42.Region.CITY;}
 District districtAt(int x,int z){return District.MARKET;}
 int xAvenueOffset(Line l,int z){return 0;}
 int zAvenueOffset(Line l,int x){return 0;}
}
class HoleRegions42 { enum Region {CITY} }
class UrbanPattern52 { enum Fabric {DENSE}; static Fabric at(long seed,int x,int z){return Fabric.DENSE;} }
class RiverChannels24 {
 static int centerOffset(long seed,Line l,boolean z,int along){return 0;}
 static int width(long seed,Line l,boolean z,int along){return 5;}
}
JAVA
cat > "$PKG/Test43.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
public class Test43 {
 static void require(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 static Map<String,String> paint(boolean chunks, String type) {
  Map<String,String> out=new TreeMap<>();
  CityPlan plan=new CityPlan();
  List<Lot> lots=List.of(new Lot(new Rect(0,0,63,63),23571L));
  List<Line> x=List.of(new Line(4,105,24,Line.Kind.CANAL));
  List<Line> z=List.of(new Line(7,107,7,Line.Kind.TRAM));
  int step=chunks?16:128;
  for(int bx=0;bx<128;bx+=step) for(int bz=0;bz<128;bz+=step){
   Canvas c=new Canvas(bx,bx+step-1,bz,bz+step-1);
   switch(type){
     case "market" -> OccupiedCommons43.market(c,lots.get(0));
     case "plaza" -> OccupiedCommons43.plaza(c,new Lot(new Rect(0,0,28,28),123L));
     case "streets" -> Backstreets43.paint(c,plan,x,z,lots);
     case "quays" -> CanalWorksites43.paint(c,plan,x,z);
   }
   out.putAll(c.result());
  }
  return out;
 }
 public static void main(String[] args){
  for(String type:List.of("market","plaza","streets","quays")){
    Map<String,String> full=paint(false,type),tiled=paint(true,type);
    require(full.equals(tiled),type+" differs when painted as 16x16 chunks");
    require(full.size()>(type.equals("quays")?4:10),type+" should paint meaningful content");
    System.out.println("PASS "+type+": "+full.size()+" exact chunk-order stable writes");
  }
  Map<String,String> market=paint(false,"market");
  for(int z=0;z<64;z++){
    // A north-south service aisle runs between column pitches.
    for(int x:List.of(9,19,29,39,49,59)) require(!market.containsKey(x+",65,"+z),"blocked merchant aisle");
  }
  Map<String,String> plaza=paint(false,"plaza");
  for(int x=4;x<=24;x++)for(int z=4;z<=24;z++)
   require(!plaza.containsKey(x+",65,"+z),"spawn square obstructed");
  Map<String,String> streets=paint(false,"streets");
  for(String k:streets.keySet()){
    String[] pos=k.split(",");int x=Integer.parseInt(pos[0]),z=Integer.parseInt(pos[2]);
    require(!(x>=0&&x<=63&&z>=0&&z<=63),"lot pavement changed");
    require(!(Math.abs(x-105)<=24||Math.abs(z-107)<=7),"rail/canal street repaved");
  }
  Map<String,String> quays=paint(false,"quays");
  for(String k:quays.keySet())require(!k.split(",")[1].equals("61"),"canal water changed");
  System.out.println("PASS walkable market aisles, spawn 21x21 open, lot/arterial/water excluded");
 }
}
JAVA
find "$TMP" -name '*.java' -print0 | xargs -0 javac --release 21 -d "$TMP/out"
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.Test43
