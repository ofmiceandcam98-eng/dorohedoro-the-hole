#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/net/minecraft/world/level/levelgen" "$TMP/net/minecraft/world/level/biome" "$TMP/com/dorohedoro/hole/worldgen/city"
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
cat > "$TMP/com/dorohedoro/hole/worldgen/city/CheckOcean65.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.biome.Climate;
public class CheckOcean65 {
  static void require(boolean valid, String message) {if(!valid)throw new AssertionError(message);}
  static Climate.Sampler climate(long seed) {
    return new Climate.Sampler(
      p -> CityHash.unit(CityHash.of(seed,17,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-0.9,
      p -> CityHash.unit(CityHash.of(seed,19,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-0.9,
      p -> CityHash.unit(CityHash.of(seed,23,Math.floorDiv(p.blockX(),1024),Math.floorDiv(p.blockZ(),1024)))*1.8-0.9);
  }
  public static void main(String[] args) {
    int wet=0, deep=0, dry=0, coast=0, checks=0, nearStart=0, neg=0;
    for(long seed=1;seed<=70;seed++){
      var c=climate(seed);
      for(int x=-14000;x<=14000;x+=315)for(int z=-14000;z<=14000;z+=343){
        int d=OceanRegions65.depthAt(c,x,z);
        require(d>=0 && d<=30,"depth bounds "+d);
        require(d==OceanRegions65.depthAt(c,x,z),"non-repeatable ocean");
        if(Math.max(Math.abs(x),Math.abs(z))<OceanRegions65.LANDING_EXCLUSION) {
          require(d==0,"ocean spawned at possible starting region");nearStart++;
        }
        if(d>0) {wet++; if(d>=16)deep++;if(x<0&&z<0)neg++;}
        else dry++;
        checks++;
      }
      // Shore ramp: adjacent blocks cannot jump to a 30-block-deep sea.
      for(int x=5300;x<10500;x+=59) {
        int z=6500;
        int d=OceanRegions65.depthAt(c,x,z);
        int next=OceanRegions65.depthAt(c,x+1,z);
        require(Math.abs(d-next)<=2,"one-block depth discontinuity "+x+" "+d+"/"+next);
        coast++;
      }
    }
    require(wet>8000,"ocean never generated "+wet);
    require(deep>3500,"deep oceans not viable "+deep);
    require(dry>60000,"ocean swallowed all survival land");
    require(neg>1000,"negative coordinates omitted");
    System.out.printf("PASS OceanRegions65: %,d coordinate/seed checks; %,d wet, %,d deep, %,d dry, %,d protected-start samples, %,d shore-continuity checks%n",checks,wet,deep,dry,nearStart,coast);
    System.out.println("Controlled climate stub only; actual NeoForge runtime structures and oceans remain UNVERIFIED.");
  }
}
JAVA
javac -d "$TMP/out" \
 "$TMP/net/minecraft/world/level/levelgen/DensityFunction.java" \
 "$TMP/net/minecraft/world/level/biome/Climate.java" \
 src/main/java/com/dorohedoro/hole/worldgen/city/{CityHash,OceanRegions65}.java \
 "$TMP/com/dorohedoro/hole/worldgen/city/CheckOcean65.java"
java -Xmx320m -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.CheckOcean65
