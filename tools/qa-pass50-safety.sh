#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
cat > "$tmp/VerifySpawn50.java" <<'JAVA'
import com.dorohedoro.hole.worldgen.city.*;
import java.util.Arrays;
public class VerifySpawn50 {
  static void ok(boolean b,String why) { if (!b) throw new AssertionError(why); }
  public static void main(String[] unused) {
    int[] requested = new int[6], located = new int[6];
    int fallback = 0;
    for (long seed=-18000;seed<18000;seed++) {
      StartingRegions47.Origin first=StartingRegions47.preferred(seed);
      requested[first.ordinal()]++;
      StartingRegions47.Probe all=(i,j,origin)-> Math.floorMod(i*37+j*13, 11)==0;
      var legacy=StartingRegions47.choose(seed,all);
      var modern=StartingRegions47.chooseWithRegionalFallback(seed,all);
      ok(modern.equals(StartingRegions47.chooseWithRegionalFallback(seed,all)), "nonrepeatable");
      ok(legacy.equals(modern), "available first-choice region changed");
      located[modern.origin().ordinal()]++;
      if (first==StartingRegions47.Origin.CITY) ok(modern.downtown(), "city start lost");
      if (first != StartingRegions47.Origin.CITY) {
        // Force the preference to be missing. One other outer type is valid.
        var viable=StartingRegions47.Origin.values()[1+(first.ordinal()%5)];
        StartingRegions47.Probe only=(i,j,origin)-> origin==viable && Math.floorMod(i*17+j*19, 7)==0;
        var s=StartingRegions47.chooseWithRegionalFallback(seed,only);
        ok(s.origin()==viable && !s.downtown(), "frontier fallback incorrectly chose downtown");
        ok(s.equals(StartingRegions47.chooseWithRegionalFallback(seed,only)), "fallback nondeterministic");
        fallback++;
      }
    }
    ok(fallback>25000,"insufficient alternate checks");
    for (int i=0;i<6;i++) ok(located[i]>3500,"missing start region "+i);
    for (long seed=1;seed<500;seed++) {
      var nowhere=StartingRegions47.chooseWithRegionalFallback(seed,(i,j,o)->false);
      ok(nowhere.downtown(),"must preserve safe downtown final fallback");
    }
    // Test actual pure Java height predicate with smooth and impassable grades.
    int checked=0;
    for (int x=-100;x<100;x+=3) for(int z=-100;z<100;z+=7) {
      final int px=x;
      ok(SpawnApproach50.isWalkable(x,z,(a,b)->8),"flat landing refused");
      ok(SpawnApproach50.isWalkable(x,z,(a,b)->Math.floorDiv(a,100000)),"near flat refused");
      ok(!SpawnApproach50.isWalkable(x,z,(a,b)->a>=px+14?10:0),"cliff accepted");
      ok(!SpawnApproach50.isWalkable(x,z,(a,b)->a==px-14?10:0),"blocked west passage accepted");
      ok(!SpawnApproach50.isWalkable(x,z,(a,b)->a==px+37?10:0),"blocked east passage accepted");
      checked++;
    }
    System.out.printf("PASS Pass50: 36,000 seed preferences, %,d frontier fallbacks and %,d grade scenarios%n",fallback,checked);
    System.out.println("Scope: actual pure Java selector and grade predicate; terrain/biome Minecraft runtime NOT verified.");
  }
}
JAVA
javac -d "$tmp/out" \
  src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/StartingRegions47.java \
  src/main/java/com/dorohedoro/hole/worldgen/city/SpawnApproach50.java \
  "$tmp/VerifySpawn50.java"
java -cp "$tmp/out" VerifySpawn50
