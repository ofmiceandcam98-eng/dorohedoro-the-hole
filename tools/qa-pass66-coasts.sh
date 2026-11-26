#!/usr/bin/env bash
# Compile the real ocean block painter against tiny Minecraft-type stubs.
set -euo pipefail
cd "$(dirname "$0")/.."
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/com/dorohedoro/hole/worldgen/city"
mkdir -p "$PKG" "$TMP/net/minecraft/world/level/chunk" "$TMP/net/minecraft/world/level/block" "$TMP/net/minecraft/world/level/biome" "$TMP/net/minecraft/world/level/levelgen" "$TMP/net/minecraft/world/level"
cat > "$TMP/net/minecraft/world/level/levelgen/DensityFunction.java" <<'JAVA'
package net.minecraft.world.level.levelgen;
public interface DensityFunction {
  double compute(SinglePointContext p);
  record SinglePointContext(int blockX, int blockY, int blockZ) {}
}
JAVA
cat > "$TMP/net/minecraft/world/level/biome/Climate.java" <<'JAVA'
package net.minecraft.world.level.biome;
import net.minecraft.world.level.levelgen.DensityFunction;
public final class Climate {
  public record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness) {}
}
JAVA
cat > "$TMP/net/minecraft/world/level/ChunkPos.java" <<'JAVA'
package net.minecraft.world.level;
public record ChunkPos(int x,int z) {
  public int getMinBlockX() {return x*16;}
  public int getMinBlockZ() {return z*16;}
}
JAVA
cat > "$TMP/net/minecraft/world/level/block/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
public record Block(String id) {}
JAVA
cat > "$TMP/net/minecraft/world/level/block/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public final class Blocks {
  public static final Block STONE=new Block("stone"), GRAVEL=new Block("gravel"), SAND=new Block("sand"), COARSE_DIRT=new Block("coarse_dirt"),
   CLAY=new Block("clay"), WATER=new Block("water"), AIR=new Block("air");
}
JAVA
cat > "$TMP/net/minecraft/world/level/chunk/ChunkAccess.java" <<'JAVA'
package net.minecraft.world.level.chunk;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import java.util.*;
public final class ChunkAccess {
  private final ChunkPos pos;
  public final Map<Long,Block> writes=new HashMap<>();
  public ChunkAccess(int cx,int cz){pos=new ChunkPos(cx,cz);}
  public ChunkPos getPos(){return pos;}
  public static long pack(int x,int y,int z){return ((long)(x & 0x1ffffff)<<37)|((long)(z & 0x1ffffff)<<10)|(y&1023);}
  public void put(int x,int y,int z,Block block){writes.put(pack(x,y,z),block);}
  public Block get(int x,int y,int z){return writes.get(pack(x,y,z));}
}
JAVA
cat > "$PKG/CitySettings.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
public record CitySettings(int worldDetailVersion) {}
JAVA
cat > "$PKG/VanillaStructureBuffer64.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
public final class VanillaStructureBuffer64 {public record Box() {}}
JAVA
cat > "$PKG/CityPlan.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.biome.Climate;
public final class CityPlan {
  public final CitySettings settings;
  public final Climate.Sampler climate;
  public CityPlan(Climate.Sampler climate,int version){this.settings=new CitySettings(version);this.climate=climate;}
  public int oceanDepthAt(int x,int z){return settings.worldDetailVersion()>=47?OceanRegions65.depthAt(climate,x,z):0;}
  public int coastalDistanceAt(int x,int z){return settings.worldDetailVersion()>=48?CoastalShelf66.distanceAt(climate,x,z):-1;}
}
JAVA
cat > "$PKG/Canvas.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Block;
import java.util.*;
public final class Canvas {
  private final ChunkAccess chunk;
  public final int minX,minZ,maxX,maxZ;
  public Canvas(ChunkAccess chunk,CityPlan plan,List<VanillaStructureBuffer64.Box> structures){
    this.chunk=chunk; minX=chunk.getPos().getMinBlockX(); minZ=chunk.getPos().getMinBlockZ();
    maxX=minX+15;maxZ=minZ+15;
  }
  public void setRaw(int x,int y,int z,Block state){chunk.put(x,y,z,state);}
}
JAVA
cat > "$PKG/VerifyBathymetry65.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Blocks;
public class VerifyBathymetry65 {
  static void check(boolean b,String message){if(!b) throw new AssertionError(message);}
  static Climate.Sampler climate(long seed){return new Climate.Sampler(
    p->CityHash.unit(CityHash.of(seed,17,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-.9,
    p->CityHash.unit(CityHash.of(seed,19,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-.9,
    p->CityHash.unit(CityHash.of(seed,23,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-.9);}
  public static void main(String[]args){
    Climate.Sampler c=climate(83054L);
    CityPlan plan=new CityPlan(c,47);
    int oceanX=0,oceanZ=0, shoreX=0,shoreZ=0;
    // Find real samples in the actual production region policy, not a fabricated rectangular ocean mask.
    search:for(int cx=-700;cx<700;cx+=3)for(int cz=-700;cz<700;cz+=3){
      int x=cx*16+8,z=cz*16+8;
      int d=plan.oceanDepthAt(x,z);
      if(d>=20&&oceanX==0){oceanX=cx;oceanZ=cz;}
      if(d>0&&d<7&&shoreX==0){shoreX=cx;shoreZ=cz;}
      if(oceanX!=0&&shoreX!=0)break search;
    }
    check(oceanX!=0&&shoreX!=0,"failed to discover ocean and shoreline chunks");
    int chunks=0,wet=0,dry=0,waterChecked=0;
    for(int[] site:new int[][]{{oceanX,oceanZ},{shoreX,shoreZ},{shoreX+1,shoreZ},
        {oceanX+1,oceanZ},{0,0},{-17,-21}}){
      var chunk=new ChunkAccess(site[0],site[1]);
      OceanBathymetry65.paint(plan,chunk,List.of());
      chunks++;
      for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++){
        int x=site[0]*16+dx,z=site[1]*16+dz;
        int depth=plan.oceanDepthAt(x,z);
        if(depth==0){
          check(chunk.get(x,62,z)==null,"writer touched land column");dry++;
        } else {
          check(chunk.get(x,62,z)==Blocks.WATER,"ocean is not real water at sea surface");
          check(chunk.get(x,63,z)==Blocks.AIR,"surface above sea not clear");
          check(chunk.get(x,100,z)==Blocks.AIR,"ocean still has floating city buildings");
          check(chunk.get(x,62-depth,z)==(depth<6?Blocks.GRAVEL:depth<15?Blocks.CLAY:Blocks.GRAVEL),"sea bed missing");
          wet++;waterChecked++;
        }
      }
      var old=new ChunkAccess(site[0],site[1]);
      OceanBathymetry65.paint(new CityPlan(c,46),old,List.of());
      check(old.writes.isEmpty(),"historical worlds unexpectedly altered");
    }
    check(wet>256,"sea feature insufficient");
    // Pass 66: discover a real dry beach adjoining a live ocean from the production policy.
    int landX=Integer.MIN_VALUE,landZ=0,landDistance=-1;
    beach:for(int x=shoreX*16-48;x<=shoreX*16+48;x++)
      for(int z=shoreZ*16-48;z<=shoreZ*16+48;z++) {
        int dist=CoastalShelf66.distanceAt(c,x,z);
        if(dist>0&&dist<=CoastalShelf66.BEACH_WIDTH){landX=x;landZ=z;landDistance=dist;break beach;}
      }
    check(landX!=Integer.MIN_VALUE,"no dry beach discovered around an actual ocean rim");
    check(plan.oceanDepthAt(landX,landZ)==0,"coast must be dry");
    check(landDistance==CoastalShelf66.distanceAt(c,landX,landZ),"coastal generation nondeterministic");
    int cx=Math.floorDiv(landX,16),cz=Math.floorDiv(landZ,16);
    var beachChunk=new ChunkAccess(cx,cz);
    var beachPlan=new CityPlan(c,48);
    OceanBathymetry65.paint(beachPlan,beachChunk,List.of());
    int verified=0,beachVerified=0;
    for(int x=cx*16;x<cx*16+16;x++)for(int z=cz*16;z<cz*16+16;z++) {
      int dist=CoastalShelf66.distanceAt(c,x,z);
      if(dist>0){
        int y=CoastalShelf66.drySurfaceY(dist);
        check(beachChunk.get(x,y,z)==(dist<=7?Blocks.SAND:dist<=11?Blocks.GRAVEL:Blocks.COARSE_DIRT),"missing reserved dry coast block");
        check(beachChunk.get(x,y+1,z)==Blocks.AIR,"coastal building not cleared");
        check(beachChunk.get(x,57,z)==Blocks.STONE,"dry coastline foundation absent");
        verified++;if(dist<=7)beachVerified++;
      }
    }
    check(verified>0&&beachVerified>0,"no actual new beach blocks drawn");
    var previousOceanWorld=new ChunkAccess(cx,cz);
    OceanBathymetry65.paint(new CityPlan(c,47),previousOceanWorld,List.of());
    check(previousOceanWorld.get(landX,CoastalShelf66.drySurfaceY(landDistance),landZ)==null,"v47 ocean world's dry coast changed");
    check(CoastalShelf66.distanceAt(c,0,0)==-1,"starter region incorrectly reserved for beaches");
    check(CoastalShelf66.drySurfaceY(1)==63 && CoastalShelf66.drySurfaceY(16)==66,"beach slope heights");
    System.out.printf("PASS CoastalShelf66: %d dry land columns, %d beach columns, deterministic height and legacy v47 no-op%n",verified,beachVerified);

    System.out.printf("PASS OceanBathymetry65: %d real chunk geometries, %d wet columns, %d untouched land columns, %d old-world no-op checks, %d surface water observations%n",chunks,wet,dry,chunks,waterChecked);
    System.out.println("Canvas and Minecraft classes are minimal stubs; full NeoForge integration remains unverified.");
  }
}
JAVA
javac -d "$TMP/out" $(find "$TMP" -name '*.java') \
 src/main/java/com/dorohedoro/hole/worldgen/city/{CityHash,OceanRegions65,CoastalShelf66,OceanBathymetry65}.java
java -Xmx512m -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifyBathymetry65
