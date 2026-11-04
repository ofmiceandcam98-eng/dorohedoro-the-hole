#!/usr/bin/env bash
# Compiles the production Pass 44 facade source with a minimal block/chunk fixture.
# No Minecraft client or NeoForge runtime is involved.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
P="$TMP/com/dorohedoro/hole/worldgen/city"
B="$TMP/net/minecraft/world/level/block"
D="$TMP/net/minecraft/core"
mkdir -p "$P" "$B" "$D" "$TMP/out"
cp "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/"{FunctionalFacade44,CityHash,Rect}.java "$P/"
cat > "$D/Direction.java" <<'JAVA'
package net.minecraft.core;
public enum Direction { NORTH,SOUTH,EAST,WEST; }
JAVA
cat > "$B/ChainBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public class ChainBlock extends Block { public ChainBlock(){super("chain");} }
JAVA
cat > "$B/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
public class Block { public final String id; public Block(String id){this.id=id;} }
JAVA
cat > "$B/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class Blocks {
 public static final Block AIR = new Block("air"), BRICKS=new Block("bricks"),
 WAXED_WEATHERED_CUT_COPPER = new Block("weathered_copper"),
 WAXED_OXIDIZED_CUT_COPPER = new Block("oxidized_copper"),
 WAXED_OXIDIZED_CUT_COPPER_SLAB = new Block("oxidized_slab"),
 WAXED_WEATHERED_CUT_COPPER_SLAB = new Block("weathered_slab"),
 GRAY_TERRACOTTA=new Block("gray_terracotta"), CAULDRON=new Block("cauldron"),
 MUD_BRICKS=new Block("mud_bricks"), CHAIN=new ChainBlock(),
 IRON_BARS=new Block("iron_bars"), WALL=new Block("wall");
}
JAVA
cat > "$P/Fixtures.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.block.*;
import java.util.*;
record Lot(Rect rect, long hash){}
enum Roof { PLAIN, TANK, GARDEN, ANTENNA }
class Tower { enum Roof { PLAIN, TANK, GARDEN, ANTENNA } static final int STOREY=4; }
class CityPlan { static final int STREET_Y=64, GROUND_Y=65; }
class Canvas {
 final int minX,maxX,minZ,maxZ;
 final Rect footprint;
 final int buildingTop;
 final Map<String,Block> writes = new HashMap<>();
 Canvas(int a,int b,int c,int d,Rect fp,int top){minX=a;maxX=b;minZ=c;maxZ=d;footprint=fp;buildingTop=top;}
 boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 record State(Block block){boolean isAir(){return block==Blocks.AIR;}}
 private String key(int x,int y,int z){return x+","+y+","+z;}
 State get(int x,int y,int z) {
  if(!contains(x,z)) throw new AssertionError("off-chunk read");
  Block own=writes.get(key(x,y,z));if(own!=null)return new State(own);
  boolean inside=footprint.contains(x,z);
  boolean wall=inside && (x==footprint.x0()||x==footprint.x1()||z==footprint.z0()||z==footprint.z1());
  if (y<=CityPlan.STREET_Y || (inside && y==buildingTop) || (wall && y<=buildingTop+1)) return new State(Blocks.WALL);
  return new State(Blocks.AIR);
 }
 void set(int x,int y,int z,Block b){if(!contains(x,z)) throw new AssertionError("off-chunk write");writes.put(key(x,y,z),b);}
}
public class Fixtures {
 static void require(boolean x,String message){if(!x)throw new AssertionError(message);}
 static Map<String,String> render(boolean tiled,Lot lot,Rect box,int floors,Tower.Roof roof,boolean industry,boolean factory) {
  int top=industry ? 75 : CityPlan.STREET_Y+floors*Tower.STOREY;
  Map<String,String> all=new TreeMap<>();
  int step=tiled?16:128;
  for(int bx=0;bx<128;bx+=step)for(int bz=0;bz<128;bz+=step){
   Canvas c=new Canvas(bx,bx+step-1,bz,bz+step-1,box,top);
   if(industry)FunctionalFacade44.industry(c,lot,box,top,lot.hash(),factory);
   else FunctionalFacade44.residence(c,lot,box,floors,lot.hash(),roof);
   for(var e:c.writes.entrySet())all.put(e.getKey(),e.getValue().id);
  }
  return all;
 }
 public static void main(String[]args){
  int tested=0, details=0, roofRisers=0, loading=0;
  for(int width:new int[]{12,17,23,33})for(int depth:new int[]{11,16,26})
  for(int floors:new int[]{2,3,7})for(int seed=0;seed<40;seed++) {
   Rect parcel=new Rect(12,12,12+width+6,12+depth+6);
   Rect box=new Rect(14,14,14+width-1,14+depth-1);
   Lot lot=new Lot(parcel,CityHash.of(seed,width,depth));
   Tower.Roof roof=Tower.Roof.values()[seed%4];
   for(int which=0;which<3;which++) {
    boolean industry=which!=0, factory=which==2;
    Map<String,String> full=render(false,lot,box,floors,roof,industry,factory);
    Map<String,String> tiles=render(true,lot,box,floors,roof,industry,factory);
    require(full.equals(tiles),"chunk order differs");
    int top=industry?75:CityPlan.STREET_Y+floors*4;
    for(String key:full.keySet()) {
     String[] xyz=key.split(",");int x=Integer.parseInt(xyz[0]),y=Integer.parseInt(xyz[1]),z=Integer.parseInt(xyz[2]);
     require(parcel.contains(x,z),"outside lot");
     require(y>CityPlan.GROUND_Y+2,"blocked walking headroom");
     require(!(x==box.centerX()&&z==box.z1()&&y<=CityPlan.GROUND_Y+3),"blocked doorway");
     if (y>top+2) roofRisers++;
     if(industry && z>box.z1())loading++;
    }
    details+=full.size();tested++;
   }
  }
  require(details>300,"no real block changes");
  require(roofRisers>100,"no raised roof silhouette");
  require(loading>100,"no factory loading canopy");
  System.out.println("PASS Pass44 Java fixture: "+tested+" seeded building scenarios; "+details+" block writes, "+roofRisers+" raised roof cells, "+loading+" frontage canopy cells");
  System.out.println("PASS exact 16x16 tiling, lot containment, 3-block pedestrian headroom, entrance exclusion");
  System.out.println("WARNING source fixture only, not a NeoForge compilation or client test");
 }
}
JAVA
javac --release 21 -d "$TMP/out" $(find "$TMP" -name '*.java')
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.Fixtures
