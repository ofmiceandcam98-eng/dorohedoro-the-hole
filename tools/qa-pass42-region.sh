#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/src/net/minecraft/world/level/biome" "$TMP/src/net/minecraft/world/level/levelgen" "$TMP/classes"
cat > "$TMP/src/net/minecraft/world/level/levelgen/DensityFunction.java" <<'JAVA'
package net.minecraft.world.level.levelgen;
public interface DensityFunction {
    double compute(SinglePointContext p);
    record SinglePointContext(int blockX, int blockY, int blockZ) { }
}
JAVA
cat > "$TMP/src/net/minecraft/world/level/biome/Climate.java" <<'JAVA'
package net.minecraft.world.level.biome;
import net.minecraft.world.level.levelgen.DensityFunction;
public final class Climate {
    private Climate() { }
    public record Sampler(DensityFunction temperature, DensityFunction humidity) { }
}
JAVA
cat > "$TMP/src/TestRegion42.java" <<'JAVA'
import com.dorohedoro.hole.worldgen.city.HoleRegions42;
import com.dorohedoro.hole.worldgen.city.HoleRegions42.Region;
import net.minecraft.world.level.biome.Climate;
import java.util.EnumSet;

public class TestRegion42 {
    private static Climate.Sampler sample(double temp,double humid) {
        return new Climate.Sampler(p -> temp, p -> humid);
    }
    private static void check(boolean condition,String message) {
        if(!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        Climate.Sampler climate=sample(-.1,.2);
        check(HoleRegions42.at(climate,0,0)==Region.CITY,"spawn quarter not preserved");
        check(HoleRegions42.at(climate,1500,0)==Region.CITY,"urban core not preserved");
        check(HoleRegions42.terrainBlend(0,0)==0.0,"urban relief changed");
        for(int x=-10240;x<=10240;x+=59)
            for(int z=-10240;z<=10240;z+=127) {
                double b=HoleRegions42.terrainBlend(x,z);
                check(b>=0.0&&b<=1.0,"terrain blend out of range");
                check(HoleRegions42.at(climate,x,z)==HoleRegions42.at(climate,x,z),"nondeterministic region");
            }
        EnumSet<Region> seen=EnumSet.noneOf(Region.class);
        Climate.Sampler[] tests={sample(-.6,.0),sample(-.1,.25),sample(.1,-.5),sample(.25,-.5),sample(.4,.0)};
        for(Climate.Sampler t:tests)
            for(int x=-6144;x<=6144;x+=512)
                for(int z=-6144;z<=6144;z+=512) seen.add(HoleRegions42.at(t,x,z));
        check(seen.containsAll(EnumSet.allOf(Region.class)),"Missing region(s): "+seen);
        System.out.println("PASS compiled region selector with Java 21 and stub climate API; all 6 region classifications seen");
        System.out.println("PASS origin preserved, deterministic repeat checks, 0..1 terrain blends");
        System.out.println("Scope: unit tests only. Does not test real Minecraft climate, chunk generator or NeoForge.");
    }
}
JAVA
javac -d "$TMP/classes" \
 "$TMP/src/net/minecraft/world/level/levelgen/DensityFunction.java" \
 "$TMP/src/net/minecraft/world/level/biome/Climate.java" \
 "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java" \
 "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/HoleRegions42.java" \
 "$TMP/src/TestRegion42.java"
java -cp "$TMP/classes" TestRegion42
