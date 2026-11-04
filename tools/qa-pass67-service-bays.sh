#!/usr/bin/env bash
# Compile the actual production Pass67 planner+painter with minimal Minecraft types;
# test deterministic placement, unobstructed main walkways, connected rooms and seams.
set -euo pipefail
cd "$(dirname "$0")/.."
TMP=$(mktemp -d);trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/com/dorohedoro/hole/worldgen/city"
mkdir -p "$PKG" "$TMP/net/minecraft/world/level/block"
# Pass67 regression intentionally uses version 49: the later Pass68 reserve policy
# cannot change that version, and the production Pass68 class is compiled by its own test.
cat > "$PKG/StormwaterPumpHalls68.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
final class StormwaterPumpHalls68 { static boolean reservesServiceBay(long seed, boolean axis, int trunk, int along, int version) { return false; } }
JAVA
cat > "$TMP/net/minecraft/world/level/block/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
public record Block(String name) {}
JAVA
cat > "$TMP/net/minecraft/world/level/block/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class Blocks {
 public static final Block AIR=new Block("air"), STONE_BRICKS=new Block("stone_bricks"), TUFF_BRICKS=new Block("tuff_bricks"),
 POLISHED_ANDESITE=new Block("polished_andesite"), DEEPSLATE_TILES=new Block("deepslate_tiles"),
 POLISHED_TUFF=new Block("polished_tuff"), IRON_BARS=new Block("iron_bars"),
 CRAFTING_TABLE=new Block("crafting_table"), FURNACE=new Block("furnace"), GRINDSTONE=new Block("grindstone"),
 CAULDRON=new Block("cauldron"), COMPOSTER=new Block("composter"), STONECUTTER=new Block("stonecutter");
}
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
 public boolean ocean=false;
 public CityPlan(long seed,int version,boolean sewer){this.seed=seed;settings=new CitySettings(version,sewer);}
 public List<Line> xLinesNear(int a,int b){return lines(a,b);}
 public List<Line> zLinesNear(int a,int b){return lines(a,b);}
 private static List<Line> lines(int a,int b){List<Line> list=new ArrayList<>();
  for(int i=Math.floorDiv(a-40,120)-1;i<=Math.floorDiv(b-40,120)+1;i++){
   int p=i*120+40;if(p>=a&&p<=b)list.add(new Line(i,p,4,i%7==4?Line.Kind.CANAL:Line.Kind.AVENUE));
  }return list;
 }
 public int oceanDepthAt(int x,int z){return ocean?8:0;}
 public int coastalDistanceAt(int x,int z){return -1;}
}
JAVA
cat > "$PKG/Canvas.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.Block;
public final class Canvas {
 public record Pos(int x,int y,int z) {}
 public final Map<Pos,Block> writes=new HashMap<>();
 public final int minX,maxX,minZ,maxZ;
 public Canvas(int minX,int minZ,int maxX,int maxZ){this.minX=minX;this.minZ=minZ;this.maxX=maxX;this.maxZ=maxZ;}
 public boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 public boolean overlaps(Rect r){return r.x0()<=maxX&&r.x1()>=minX&&r.z0()<=maxZ&&r.z1()>=minZ;}
 public void set(int x,int y,int z,Block b){if(contains(x,z))writes.put(new Pos(x,y,z),b);}
 public void fill(int a,int b,int c,int d,int e,int f,Block block){for(int x=a;x<=d;x++)for(int z=c;z<=f;z++)for(int y=b;y<=e;y++)set(x,y,z,block);}
 public Block get(int x,int y,int z){return writes.get(new Pos(x,y,z));}
}
JAVA
cat > "$PKG/VerifyServiceBays67.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.Blocks;
public final class VerifyServiceBays67 {
 static int checks;
 static void ok(boolean b,String msg){checks++;if(!b)throw new AssertionError(msg);}
 static int countKind(int kind){return kind>0?1:0;}
 public static void main(String[]args){
  int repair=0,filter=0,negative=0,minusSide=0,plusSide=0;
  for(long seed=-50;seed<50;seed++)for(int trunk=-12;trunk<=12;trunk++)for(int section=-7;section<=7;section++){
   int k=SewerServiceBays67.kind(seed,true,trunk,section,49);
   ok(k>=0&&k<=2,"invalid kind");
   ok(SewerServiceBays67.kind(seed,true,trunk,section,48)==0,"old save has a service bay");
   ok(k==SewerServiceBays67.kind(seed,true,trunk,section,49),"nondeterministic decision");
   int along=SewerServiceBays67.center(seed,true,trunk,section);
   ok(along>=section*64+16&&along<=section*64+47,"site spills outside its section");
   int side=SewerServiceBays67.side(seed,true,trunk,section);
   ok(side==-1||side==1,"invalid side");
   if(k==1)repair++;if(k==2)filter++;
   if(k>0&&section<0)negative++;
   if(k>0&&side==-1)minusSide++;if(k>0&&side==1)plusSide++;
  }
  ok(repair>5000&&filter>5000&&negative>5000&&minusSide>5000&&plusSide>5000,"selection diversity failure");
  int attempts=0, built=0;
  // Choose real sites with adequate separation from perpendicular trunk lines.
  for(long seed=0;seed<250&&built<40;seed++)for(int section=-8;section<8&&built<40;section++){
   int k=SewerServiceBays67.kind(seed,true,0,section,49);
   int along=SewerServiceBays67.center(seed,true,0,section);
   int side=SewerServiceBays67.side(seed,true,0,section);
   boolean safe=true;
   for(int i=-10;i<=10;i++)if(Math.abs(along-(i*120+40))<39)safe=false;
   if(k==0||!safe)continue;
   CityPlan plan=new CityPlan(seed,49,true);
   int x=40,z=along;
   Canvas full=new Canvas(x-22,z-12,x+22,z+12);
   SewerServiceBays67.paint(full,plan);
   if(full.writes.isEmpty())continue;
   built++;
   // Existing municipal center drain and 2-wide dry walkways must not be touched.
   for(int zz=z-12;zz<=z+12;zz++)for(int d=0;d<=2;d++)for(int ss:new int[]{-1,1}){
    ok(full.get(x+ss*d,49,zz)==null,"service bay destroyed a sewer lane");
    ok(full.get(x+ss*d,50,zz)==null,"service bay blocked main lane clearance");
   }
   int doorway=x+side*3;
   for(int yy=50;yy<=52;yy++)ok(full.get(doorway,yy,z)==Blocks.AIR,"no three-high door to dry sewer lane");
   int atX=x+side*8;
   ok(full.get(atX,50,z-2)==(k==1?Blocks.CRAFTING_TABLE:Blocks.CAULDRON),"fixture one incorrect");
   ok(full.get(atX,50,z+2)==(k==1?Blocks.FURNACE:Blocks.COMPOSTER),"fixture two incorrect");
   ok(full.get(atX,50,z)==Blocks.AIR,"center access to workstations blocked");
   ok(full.get(x+side*10,51,z)!=Blocks.AIR,"back wall missing");
   for(var b:full.writes.values())ok(!Set.of("chest","spawner","lantern","torch").contains(b.name()),"unwanted loot/spawner/lamp");
   // Clip the same generation to independent 16x16 chunks and compare final writes.
   Map<Canvas.Pos,net.minecraft.world.level.block.Block> clipped=new HashMap<>();
   for(int cx=Math.floorDiv(full.minX,16);cx<=Math.floorDiv(full.maxX,16);cx++)
    for(int cz=Math.floorDiv(full.minZ,16);cz<=Math.floorDiv(full.maxZ,16);cz++){
     Canvas part=new Canvas(cx*16,cz*16,cx*16+15,cz*16+15);
     SewerServiceBays67.paint(part,plan);
     part.writes.forEach((p,b)->{if(p.x()>=full.minX&&p.x()<=full.maxX&&p.z()>=full.minZ&&p.z()<=full.maxZ)clipped.put(p,b);});
    }
   ok(full.writes.equals(clipped),"chunk-order/clipping disagreement");
   Canvas old=new Canvas(full.minX,full.minZ,full.maxX,full.maxZ);
   SewerServiceBays67.paint(old,new CityPlan(seed,48,true));
   ok(old.writes.isEmpty(),"legacy version changed");
   Canvas disabled=new Canvas(full.minX,full.minZ,full.maxX,full.maxZ);
   SewerServiceBays67.paint(disabled,new CityPlan(seed,49,false));
   ok(disabled.writes.isEmpty(),"sewer-disabled world changed");
   Canvas sea=new Canvas(full.minX,full.minZ,full.maxX,full.maxZ);
   CityPlan oceanPlan=new CityPlan(seed,49,true);oceanPlan.ocean=true;
   SewerServiceBays67.paint(sea,oceanPlan);
   ok(sea.writes.isEmpty(),"submerged region received service bay");
  }
  ok(built>=25,"could not find enough valid real service bay sites");
  int blockedByCrossing=0;
  // Section zero is close to the crossing at z=40. For site center 16..25,
  // a chunk near z=0 might NOT see the crossing if range were only +/-11;
  // it must nevertheless agree with the adjacent chunk that sees the junction.
  for(long seed=0;seed<500;seed++) {
   if(SewerServiceBays67.kind(seed,true,0,0,49)==0)continue;
   int along=SewerServiceBays67.center(seed,true,0,0);
   if(along>25)continue;
   blockedByCrossing++;
   CityPlan plan=new CityPlan(seed,49,true);
   Canvas spanning=new Canvas(16,0,63,63);
   SewerServiceBays67.paint(spanning,plan);
   ok(spanning.writes.isEmpty(),"nearby crossing not excluded in complete footprint");
   for(int cx=1;cx<=3;cx++)for(int cz=0;cz<=3;cz++){
    Canvas chunk=new Canvas(cx*16,cz*16,cx*16+15,cz*16+15);
    SewerServiceBays67.paint(chunk,plan);
    ok(chunk.writes.isEmpty(),"chunk omitted a nearby shaft/crossing from its lookup");
   }
  }
  ok(blockedByCrossing>20,"insufficient close-junction exclusions tested");
  System.out.println("PASS Pass67 Java checks="+checks+" examinedSites="+built+" excludedCrossings="+blockedByCrossing+
    " repairSelections="+repair+" filterSelections="+filter+" negativeSelections="+negative);
 }
}
JAVA
mkdir -p "$TMP/out"
javac -d "$TMP/out" "$TMP/net/minecraft/world/level/block/"*.java "$PKG/"*.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/{SewerServiceBays67,CityHash,Line,Rect}.java
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifyServiceBays67
