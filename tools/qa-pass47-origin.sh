#!/usr/bin/env bash
# Compiles the actual Pass47 seed selector and land-use field; NOT a Minecraft build.
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
pkg="$tmp/com/dorohedoro/hole/worldgen/city"; mkdir -p "$pkg"
cat > "$pkg/HoleRegions42.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
final class HoleRegions42 {
    enum Region { CITY, SNOWFIELDS, DEAD_TAIGA, DUST_BASIN, IRON_BADLANDS, MOUNTAIN_RIM }
}
JAVA
cat > "$pkg/VerifyStart47.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.Arrays;
public class VerifyStart47 {
    private static void check(boolean a, String message) { if (!a) throw new AssertionError(message); }
    private static boolean mockClimate(long seed, int i, int j, StartingRegions47.Origin origin) {
        // Stable synthetic macro regions. The production probe additionally
        // verifies climate biomes at four landing corners, and lot dimensions.
        int cellX=Math.floorDiv(i,4), cellZ=Math.floorDiv(j,4);
        int choice=CityHash.range(CityHash.of(seed ^ 0x47B101L,cellX,cellZ),5)+1;
        return StartingRegions47.Origin.values()[choice]==origin;
    }
    public static void main(String[] args) {
        int[] preferences=new int[6], actual=new int[6];
        int tested=0, regional=0;
        for(long seed=-15000;seed<15000;seed++) {
            StartingRegions47.Origin preferred=StartingRegions47.preferred(seed);
            preferences[preferred.ordinal()]++;
            final long sourceSeed = seed;
            StartingRegions47.Probe probe=(i,j,origin)->mockClimate(sourceSeed,i,j,origin);
            StartingRegions47.Site a=StartingRegions47.choose(seed,probe);
            StartingRegions47.Site b=StartingRegions47.choose(seed,probe);
            check(a.equals(b),"nonrepeatable seed selection");
            check(a.downtown() || (Math.max(Math.abs(a.blockI()), Math.abs(a.blockJ()))>=17),"core overwritten");
            check(a.downtown() || mockClimate(seed,a.blockI(),a.blockJ(),preferred),"wrong region");
            check(a.downtown() || a.origin()==preferred,"region mismatch");
            actual[a.origin().ordinal()]++;
            if(!a.downtown())regional++;
            tested++;
        }
        for(int count:preferences)check(count>3900 && count<6100,"unbalanced seed selection");
        for(int i=1;i<6;i++)check(actual[i]>3600,"one climate never receives players");
        check(regional>21000,"regional starts too rare");
        int sampled=0,working=0,derelict=0,open=0;
        for(long seed=0;seed<50;seed++) {
            for(int x=-4096;x<4096;x+=127)for(int z=-4096;z<4096;z+=127) {
                double intensity=SettlementBands47.intensity(seed,x,z);
                double next=SettlementBands47.intensity(seed,x+1,z);
                check(intensity>=0 && intensity<=1,"out of range land-use field");
                check(Math.abs(intensity-next)<0.006,"chunk/seam jump");
                switch(SettlementBands47.at(seed,x,z)) {
                    case WORKING -> working++;
                    case DERELICT -> derelict++;
                    case OPEN_COUNTRY -> open++;
                }
                sampled++;
            }
        }
        check(working>2000 && derelict>2000 && open>2000,"settlement type missing");
        // Each region must provide useful occupied buildings in working areas,
        // and leave the majority of open country unbuilt.
        for(HoleRegions42.Region biome:HoleRegions42.Region.values()) {
            if (biome==HoleRegions42.Region.CITY)continue;
            int empty=0, supplied=0;
            for(int j=0;j<1000;j++){
                float roll=j/1000f;
                LotType remote=SettlementBands47.choose(biome,SettlementBands47.Form.OPEN_COUNTRY,roll);
                if(remote==LotType.VACANT || remote==LotType.RUBBLE)empty++;
                LotType town=SettlementBands47.choose(biome,SettlementBands47.Form.WORKING,roll);
                if(town.enclosed)supplied++;
            }
            check(empty>=800,"open frontier too urban");
            check(supplied>=300,"towns have no working buildings");
        }
        System.out.printf("PASS Pass47 Java selectors: %,d seeds, %,d climate starts; preference=%s, chosen=%s%n",tested,regional,Arrays.toString(preferences),Arrays.toString(actual));
        System.out.printf("PASS Pass47 settlement field: %,d points, working=%d derelict=%d open=%d, continuous across chunks%n",sampled,working,derelict,open);
        System.out.println("Scope: synthetic climate and algorithm fixtures only; Minecraft terrain and player spawning NOT verified.");
    }
}
JAVA
javac -d "$tmp" \
  src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/LotType.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/StartingRegions47.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/SettlementBands47.java \
  "$pkg/HoleRegions42.java" "$pkg/VerifyStart47.java"
java -cp "$tmp" com.dorohedoro.hole.worldgen.city.VerifyStart47
