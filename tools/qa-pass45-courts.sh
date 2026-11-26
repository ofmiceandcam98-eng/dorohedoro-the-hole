#!/usr/bin/env bash
# Offline Java compilation of the REAL Pass45 production class, with minimal Minecraft stubs.
# Does not compile NeoForge or launch Minecraft.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
P="$TMP/com/dorohedoro/hole/worldgen/city"
B="$TMP/net/minecraft/world/level/block"
D="$TMP/net/minecraft/core"
mkdir -p "$P" "$B" "$D" "$TMP/out"
cp "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/"{InhabitedCourts45,CityHash,Rect}.java "$P/"
cat > "$D/Direction.java" <<'JAVA'
package net.minecraft.core;
public enum Direction { NORTH,SOUTH,EAST,WEST; }
JAVA
cat > "$B/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
public class Block { public final String id; public Block(String id){this.id=id;} }
JAVA
cat > "$B/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class Blocks {
 public static final Block PACKED_MUD=new Block("packed_mud"),
 POLISHED_ANDESITE=new Block("polished_andesite"),MUD_BRICKS=new Block("mud_bricks"),
 CRACKED_STONE_BRICKS=new Block("cracked_stone_bricks"),
 BRICKS=new Block("bricks"), TUFF_BRICKS=new Block("tuff_bricks"),
 WAXED_WEATHERED_CUT_COPPER=new Block("weathered_copper"),
 WAXED_OXIDIZED_CUT_COPPER=new Block("oxidized_copper"),
 COBBLED_DEEPSLATE=new Block("cobbled_deepslate"), AIR=new Block("air"),
 SPRUCE_DOOR=new Block("spruce_door"),
 SMOKER=new Block("smoker"),SMITHING_TABLE=new Block("smithing_table"),
 CRAFTING_TABLE=new Block("crafting_table"),
 GRINDSTONE=new Block("grindstone"),COMPOSTER=new Block("composter"),
 LOOM=new Block("loom"),CAULDRON=new Block("cauldron"),
 STONECUTTER=new Block("stonecutter"),FURNACE=new Block("furnace"),
 BARREL=new Block("barrel");
}
JAVA
cat > "$P/Fixture45.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
enum District { RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT, DERELICT, GRAVEYARD, OUTSKIRTS }
record Lot(Rect rect, District district, long hash) {}
class CityPlan { static final int STREET_Y=64, GROUND_Y=65; }
class Mix { final Block block; Mix(Block b){block=b;} static Mix of(Block a,int n, Block b,int m){return new Mix(a);} }
class Canvas {
 final int minX,maxX,minZ,maxZ; final boolean core,flag;
 final Map<String,String> writes = new HashMap<>();
 Canvas(int minX,int maxX,int minZ,int maxZ,boolean core,boolean flag) {
  this.minX=minX;this.maxX=maxX;this.minZ=minZ;this.maxZ=maxZ;this.core=core;this.flag=flag;
 }
 boolean hasInhabitedCourts45(){return flag;}
 boolean isHoleCity(int x,int z){return core;}
 boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 void set(int x,int y,int z,Block b){if(contains(x,z))writes.put(x+","+y+","+z,b.id);}
 void fill(Rect r,int y0,int y1,Block b) {for(int x=r.x0();x<=r.x1();x++)for(int z=r.z0();z<=r.z1();z++)for(int y=y0;y<=y1;y++)set(x,y,z,b);}
 void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block b){for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++)set(x,y,z,b);}
 void walls(Rect r,int y0,int y1,Mix m){for(int x=r.x0();x<=r.x1();x++)for(int z=r.z0();z<=r.z1();z++)if(x==r.x0()||x==r.x1()||z==r.z0()||z==r.z1())for(int y=y0;y<=y1;y++)set(x,y,z,m.block);}
 void door(int x,int y,int z,Direction d,Block b){set(x,y,z,b);set(x,y+1,z,b);}
 void barrel(int x,int y,int z,String loot){set(x,y,z,Blocks.BARREL);}
}
public class Fixture45 {
 static void yes(boolean condition,String reason){if(!condition)throw new AssertionError(reason);}
 static Map<String,String> paint(boolean tiled,Lot lot,boolean damaged){
  TreeMap<String,String> merged=new TreeMap<>();
  int step=tiled?16:128;
  for(int x=0;x<128;x+=step)for(int z=0;z<128;z+=step){
   Canvas c=new Canvas(x,x+step-1,z,z+step-1,true,true);
   InhabitedCourts45.paint(c,lot,damaged);
   merged.putAll(c.writes);
  }
  return merged;
 }
 public static void main(String[]args) {
  int scanned=0, selected=0, writes=0; Set<String> differentUses = new HashSet<>();
  for(int width:new int[]{19,22,29,37})for(int depth:new int[]{19,24,35})
  for(District district:District.values())for(int seed=0;seed<45;seed++){
   Rect r=new Rect(19,19,19+width-1,19+depth-1);
   Lot lot=new Lot(r,district,CityHash.of(seed,width,depth));
   Canvas active=new Canvas(0,127,0,127,true,true);
   boolean vacancy=InhabitedCourts45.eligible(active,lot,false);
   boolean rubble=InhabitedCourts45.eligible(active,lot,true);
   Canvas old=new Canvas(0,127,0,127,true,false);
   Canvas frontier=new Canvas(0,127,0,127,false,true);
   yes(!InhabitedCourts45.eligible(old,lot,false),"legacy save changed");
   yes(!InhabitedCourts45.eligible(frontier,lot,false),"frontier generated urban court");
   if(district==District.OUTSKIRTS||district==District.GRAVEYARD) yes(!vacancy&&!rubble,"protected district changed");
   scanned++;
   for(boolean damaged:new boolean[]{false,true}) {
    if(!(damaged?rubble:vacancy))continue;
    selected++;
    Map<String,String> full=paint(false,lot,damaged), tiled=paint(true,lot,damaged);
    yes(full.equals(tiled),"chunk seam disagreement");
    int doors=0;
    for(var e:full.entrySet()) {
     String[] parts=e.getKey().split(",");int x=Integer.parseInt(parts[0]), y=Integer.parseInt(parts[1]), z=Integer.parseInt(parts[2]);
     yes(r.contains(x,z),"write beyond parcel");
     if(e.getValue().equals("spruce_door")&&y==65)doors++;
     if(Set.of("smoker","smithing_table","crafting_table","loom","grindstone","stonecutter","furnace").contains(e.getValue()))differentUses.add(e.getValue());
    }
    yes(doors==2,"two individual room doors not found");
    // From each door, all exterior rows to the southern lot street must be unblocked.
    for(var e:full.entrySet())if(e.getValue().equals("spruce_door")&&e.getKey().contains(",65,")) {
     String[] parts=e.getKey().split(",");int x=Integer.parseInt(parts[0]), z=Integer.parseInt(parts[2]);
     for(int pz=z+1;pz<=r.z1();pz++)for(int y=65;y<=67;y++){
      String material=full.get(x+","+y+","+pz);
      yes(material==null||material.equals("air"),"blocked street route "+material);
     }
    }
    writes+=full.size();
   }
  }
  yes(selected>60 && selected<scanned/2,"court density unreasonable");
  yes(differentUses.size()>=6,"not enough genuinely different utility rooms");
  System.out.println("PASS45 Java source fixture: "+scanned+" parcels checked, "+selected+" occupied courts, "+writes+" block writes; chunk order/parcel bounds/independent entry routes pass; "+differentUses.size()+" real workstation types. OFFLINE ONLY.");
 }
}
JAVA
javac -encoding UTF-8 -d "$TMP/out" $(find "$TMP" -name '*.java')
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.Fixture45
