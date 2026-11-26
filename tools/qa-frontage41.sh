#!/usr/bin/env bash
# Standalone geometry fixture: compiles the *real* terrain hash/elevation sources
# against a small CityPlan fixture, without Gradle or NeoForge downloads.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/src/com/dorohedoro/hole/worldgen/city"
mkdir -p "$PKG" "$TMP/out"
cp "$SRC"/{CityTerrain40,CityElevation,CityHash,Line,Rect}.java "$PKG/"
cat > "$PKG/CityPlan.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
final class CityPlan {
    final long seed;
    final Settings settings = new Settings();
    CityPlan(long seed) {this.seed=seed;}
    HoleRegions42.Region regionAt(int x, int z) {return HoleRegions42.Region.CITY;}
    double terrainBlendAt(int x, int z) {return HoleRegions42.terrainBlend(x, z);}
    static final class Settings {
        boolean terrainRelief(){return true;}
        int worldDetailVersion(){return 25;}
    }
    // A fixture urban map with fixed-location canals and their documented ROW.
    List<Line> xLinesNear(int min,int max){ return canals(min,max); }
    List<Line> zLinesNear(int min,int max){ return canals(min,max); }
    private static List<Line> canals(int min,int max){
        ArrayList<Line> lines=new ArrayList<>();
        for(int i=-12;i<=12;i++){
            int center=i*384+176;
            if(center>=min && center<=max)
                lines.add(new Line(i,center,24,Line.Kind.CANAL));
        }
        return lines;
    }
}
JAVA
cat > "$PKG/HoleRegions42.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
final class HoleRegions42 {
    enum Region {CITY, MOUNTAIN_RIM, IRON_BADLANDS, SNOWFIELDS}
    static double terrainBlend(int x,int z){return 0.0;}
}
JAVA
cat > "$PKG/Pass41FrontageFixture.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
final class Pass41FrontageFixture {
    public static void main(String[] args){
        long[] seeds={12345L,8675309L,42L,99999997L};
        long checks=0, improved=0, unchanged=0, worse=0;
        int worstOld=0, worstNew=0;
        for(long seed:seeds){
            CityPlan plan=new CityPlan(seed);
            for(int x=-1000;x<1000;x+=17)for(int z=-1000;z<1000;z+=17){
                Rect lot=new Rect(x,z,x+15,z+23);
                int old=CityTerrain40.offset(plan,lot.centerX(),lot.centerZ());
                int now=CityTerrain40.frontageOffset(plan,lot);
                // The true door approach is at the outward-facing lot edge.
                int street=CityTerrain40.offset(plan,lot.centerX(),lot.z1()+1);
                int oldDifference=Math.abs(old-street);
                int newDifference=Math.abs(now-street);
                if(newDifference>oldDifference)worse++;
                if(newDifference<oldDifference)improved++;
                else unchanged++;
                worstOld=Math.max(worstOld,oldDifference);
                worstNew=Math.max(worstNew,newDifference);
                // Coordinate-only: deterministic across repeated queries.
                if(now!=CityTerrain40.frontageOffset(plan,lot))throw new AssertionError("nondeterminism");
                // A building door now shares its south approach's level.
                if(newDifference!=0)throw new AssertionError("unmatched frontage");
                checks++;
            }
        }
        if(worse!=0||improved==0)throw new AssertionError("no frontage improvement or regression");
        System.out.println("Pass 41: "+checks+" sampled lots across "+seeds.length+" seeds; improved="+improved+
            "; unchanged="+unchanged+"; worse="+worse+"; old worst gap="+worstOld+
            "; new front gap="+worstNew);
        System.out.println("Scope: synthetic CityPlan fixture only; real Minecraft streets/building doors not simulated.");
    }
}
JAVA
javac -d "$TMP/out" "$PKG"/*.java
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.Pass41FrontageFixture
