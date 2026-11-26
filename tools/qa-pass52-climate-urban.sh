#!/usr/bin/env bash
# Compile real Pass52 climate and urban policy with narrowly-scoped Minecraft data type stubs.
# Not a NeoForge/client/dedicated-server build.
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/com/dorohedoro/hole/worldgen/city"
mkdir -p "$PKG" "$TMP/net/minecraft/world/level/biome" "$TMP/net/minecraft/world/level/levelgen"
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
  public record Sampler(DensityFunction temperature, DensityFunction humidity) {}
}
JAVA
cat > "$PKG/VerifyPass52.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.biome.Climate;
public class VerifyPass52 {
 static void require(boolean condition,String message){if(!condition) throw new AssertionError(message);}
 static Climate.Sampler stubClimate(long seed) {
   return new Climate.Sampler(
     p -> CityHash.unit(CityHash.of(seed,0x52CE01L,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.4 - .70,
     p -> CityHash.unit(CityHash.of(seed,0x52CE02L,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.4 - .70);
 }
 public static void main(String[] args) {
   EnumMap<HoleRegions42.Region,Integer> cores=new EnumMap<>(HoleRegions42.Region.class);
   EnumMap<UrbanPattern52.Fabric,Integer> habitats=new EnumMap<>(UrbanPattern52.Fabric.class);
   int falseOldCore=0, climaticDense=0, frontierOpen=0, roundTrips=0;
   for(long seed=-1500;seed<1500;seed++) {
     var climate=stubClimate(seed);
     var region=HoleRegions42.atAnywhere(climate,0,0);
     cores.merge(region,1,Integer::sum);
     require(HoleRegions42.at(climate,0,0)==HoleRegions42.Region.CITY,"old city core changed");
     require(UrbanPattern52.at(seed,0,0)==UrbanPattern52.Fabric.DENSE,"city hub empty");
     require(region==HoleRegions42.atAnywhere(climate,0,0),"climate unstable");
     if(region!=HoleRegions42.Region.CITY) {falseOldCore++; climaticDense++;}
     for(int i=-16;i<=16;i++)for(int j=-16;j<=16;j++) {
       int x=i*257 + (int)(seed % 131), z=j*281;
       var fabric=UrbanPattern52.at(seed,x,z);
       habitats.merge(fabric,1,Integer::sum);
       require(fabric==UrbanPattern52.at(seed,x,z),"urban fabric not reproducible");
       var climateBiome=HoleRegions42.atAnywhere(climate,x,z);
       if (fabric!=UrbanPattern52.Fabric.DENSE) {
          float roll=CityHash.unit(CityHash.of(seed, 0x5252L, x, z));
          LotType lot=UrbanPattern52.choose(climateBiome,fabric,roll);
          require(lot!=null && lot!=LotType.PLAZA && lot!=LotType.STARTER_SHELTER,"bad allocation");
          if(fabric==UrbanPattern52.Fabric.OPEN_COUNTRY && climateBiome!=HoleRegions42.Region.CITY)frontierOpen++;
       } else if(climateBiome != HoleRegions42.Region.CITY) climaticDense++;
       roundTrips++;
     }
   }
   for(var region:HoleRegions42.Region.values()) require(cores.getOrDefault(region,0)>20,"never core climate "+region+" "+cores);
   for(var f:UrbanPattern52.Fabric.values())require(habitats.getOrDefault(f,0)>3000,"missing fabric "+f+" "+habitats);
   require(falseOldCore > 1200,"climate never entered the core");
   require(climaticDense > 10000,"dense neighborhoods can't exist in other biomes");
   require(frontierOpen > 10000,"outer climate biome lacks sparse survival terrain");
   // Do not allow cell-edge differences between chunk-independent computations.
   for(long seed=0;seed<25;seed++) for(int x=-1000;x<1000;x+=17) {
       double pressureA=SettlementBands47.intensity(seed,x,2047);
       double pressureB=SettlementBands47.intensity(seed,x,2048);
       require(Math.abs(pressureA-pressureB)<.01,"artificial macrofield chunk seam");
   }
   System.out.printf("PASS Pass52: %d core climates %s, habitats %s, %,d tested samples, %,d diverse-climate dense samples, %,d frontier open samples%n",cores.size(),cores,habitats,roundTrips,climaticDense,frontierOpen);
   System.out.println("STUB climate is a controlled fixture; Minecraft's actual climate noise and world visuals are not verified.");
 }
}
JAVA
javac -d "$TMP/out" \
 "$TMP/net/minecraft/world/level/levelgen/DensityFunction.java" \
 "$TMP/net/minecraft/world/level/biome/Climate.java" \
 src/main/java/com/dorohedoro/hole/worldgen/city/{CityHash,HoleRegions42,SettlementBands47,UrbanPattern52,LotType}.java \
 "$PKG/VerifyPass52.java"
java -Xmx256m -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.VerifyPass52
