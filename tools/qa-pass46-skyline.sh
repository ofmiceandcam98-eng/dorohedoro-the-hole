#!/usr/bin/env bash
# Compiles and tests the actual skyline calculator with a minimal district stub.
# This is a Java algorithm fixture ONLY, not a NeoForge runtime/build test.
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
pkg="$tmp/com/dorohedoro/hole/worldgen/city"; mkdir -p "$pkg"
cat > "$pkg/District.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
enum District { RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT, DERELICT, GRAVEYARD, OUTSKIRTS }
JAVA
cat > "$pkg/VerifyNeighborhoodSkyline46.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.Random;
public class VerifyNeighborhoodSkyline46 {
  private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
  public static void main(String[] args) {
    Random rng = new Random(46002026L);
    int cases=0, taller=0, shorter=0, same=0, uniqueBands=0;
    boolean[] sawShift = new boolean[5];
    for (long seed : new long[]{0L, 1L, 12345L, -771L}) {
      for (int i=0; i<16000; i++) {
        int x=rng.nextInt(14000)-7000, z=rng.nextInt(14000)-7000;
        Rect box = new Rect(x,z,x+9+rng.nextInt(21),z+9+rng.nextInt(21));
        District district=District.values()[rng.nextInt(District.values().length)];
        boolean apartment=rng.nextBoolean();
        int prior=1+rng.nextInt(12), maximum=2+rng.nextInt(11);
        // Actual baseline floors never exceeds maximum.
        prior=Math.min(maximum, prior);
        long hash=CityHash.of(seed,0x4600123L,x,z);
        int result=NeighborhoodSkyline46.floors(seed,district,box,hash,prior,maximum,apartment);
        int repeat=NeighborhoodSkyline46.floors(seed,district,box,hash,prior,maximum,apartment);
        require(result==repeat,"nondeterministic floor count");
        require(result>=1 && result<=maximum,"invalid floor count");
        require(result<=prior+(box.area()>=270 && box.width()>=15 && box.depth()>=14 ? 2 : 1),"overbuilt parcel");
        if (district==District.OUTSKIRTS || district==District.GRAVEYARD) require(result==prior,"protected open district changed");
        double f=NeighborhoodSkyline46.intensity(seed,x,z);
        require(f>=0 && f<=1,"invalid smooth intensity");
        // Nearby blocks straddling an actual chunk edge must agree smoothly.
        double near=NeighborhoodSkyline46.intensity(seed,x+1,z);
        require(Math.abs(f-near)<0.025,"field jump inside a chunk");
        if(result>prior)taller++; else if(result<prior)shorter++; else same++;
        cases++;
      }
    }
    require(taller>500 && shorter>500,"skyline never forms rising AND declining neighborhoods");
    // No seams where floorDiv changes sign or at exact grid crossings.
    for(int x=-600; x<600; x++)for(int z=-205;z<205;z+=19) {
      double a=NeighborhoodSkyline46.intensity(12345L,x,z);
      double b=NeighborhoodSkyline46.intensity(12345L,x+1,z);
      require(Math.abs(a-b)<0.025,"grid seam");
    }
    require(NeighborhoodSkyline46.intensity(12345, -1, -1)==NeighborhoodSkyline46.intensity(12345,-1,-1),"negative repeat");
    System.out.printf("PASS Pass46 actual Java skyline calculator: %,d seeded parcel scenarios; up=%,d down=%,d same=%,d; no field seams; protected districts unchanged%n",cases,taller,shorter,same);
    System.out.println("Scope: Java logic only, NOT Minecraft rendering, NeoForge compilation or actual chunk-world verification.");
  }
}
JAVA
javac -d "$tmp" \
 src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/Rect.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/NeighborhoodSkyline46.java \
 "$pkg/District.java" "$pkg/VerifyNeighborhoodSkyline46.java"
java -cp "$tmp" com.dorohedoro.hole.worldgen.city.VerifyNeighborhoodSkyline46
