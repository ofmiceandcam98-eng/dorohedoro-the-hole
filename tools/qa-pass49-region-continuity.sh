#!/usr/bin/env bash
# Java 21 tests REAL region selector and v32 blend against stub climate.
# This does NOT verify actual Minecraft terrain rendering or NeoForge compilation.
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/net/minecraft/world/level/biome" "$tmp/net/minecraft/world/level/levelgen"
cat > "$tmp/net/minecraft/world/level/levelgen/DensityFunction.java" <<'JAVA'
package net.minecraft.world.level.levelgen;
public interface DensityFunction {
    double compute(SinglePointContext p);
    record SinglePointContext(int blockX, int blockY, int blockZ) {}
}
JAVA
cat > "$tmp/net/minecraft/world/level/biome/Climate.java" <<'JAVA'
package net.minecraft.world.level.biome;
import net.minecraft.world.level.levelgen.DensityFunction;
public final class Climate {
    public record Sampler(DensityFunction temperature, DensityFunction humidity) {}
}
JAVA
cat > "$tmp/VerifyRegion49.java" <<'JAVA'
import com.dorohedoro.hole.worldgen.city.HoleRegions42;
import net.minecraft.world.level.biome.Climate;
import java.util.Random;
public class VerifyRegion49 {
    static void require(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
    static int wx(int x, int z) {return x+(int)Math.round(72.0*Math.sin(z/260.0));}
    static int wz(int x, int z) {return z+(int)Math.round(72.0*Math.sin(x/290.0));}
    public static void main(String[] args) {
        Climate.Sampler climate = new Climate.Sampler(p -> 0.24, p -> -0.50);
        int same=0, change=0;
        for (int z=1800; z<11000; z+=211) for (int x=1700; x<11000; x++) {
            int fx=Math.floorMod(wx(x,z),512), fz=Math.floorMod(wz(x,z),512);
            if (fx!=0 || fz<130 || fz>380)continue;
            var left=HoleRegions42.at(climate,x-1,z);
            var right=HoleRegions42.at(climate,x+1,z);
            if (left == right) {
                double old=HoleRegions42.terrainBlend(x,z), modern=HoleRegions42.terrainBlend(climate,x,z);
                require(old<0.02,"historical cell trough not reproduced");
                require(modern>0.98,"same-biome cell creates a false trough");
                same++;
            } else {
                require(HoleRegions42.terrainBlend(climate,x,z)<0.01,"real biome boundary not graded");
                change++;
            }
        }
        require(same>=60 && change>=10,"insufficient both-type boundary fixtures: "+same+", "+change);
        Random rnd=new Random(49);
        for (int n=0;n<100000;n++) {
            int x=rnd.nextInt(30000)-15000,z=rnd.nextInt(30000)-15000;
            double a=HoleRegions42.terrainBlend(climate,x,z);
            require(Double.isFinite(a)&&a>=0&&a<=1,"out of range blend");
            require(a==HoleRegions42.terrainBlend(climate,x,z),"nondeterministic blend");
            if(Math.max(Math.abs(x),Math.abs(z))<=1536)require(a==0,"central core changed");
        }
        System.out.printf("PASS Pass49: %d same-biome boundaries preserved, %d true province edges smoothed; 100,000 random samples deterministic%n", same, change);
        System.out.println("Scope: Java region math against constant stub climate; NOT an actual biome-rendered Minecraft world.");
    }
}
JAVA
javac -d "$tmp/out" \
  "$tmp/net/minecraft/world/level/levelgen/DensityFunction.java" \
  "$tmp/net/minecraft/world/level/biome/Climate.java" \
  src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/HoleRegions42.java \
  "$tmp/VerifyRegion49.java"
java -cp "$tmp/out" VerifyRegion49
