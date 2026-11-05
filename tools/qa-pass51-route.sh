#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
cat > "$tmp/VerifySpawn51.java" <<'JAVA'
import com.dorohedoro.hole.worldgen.city.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class VerifySpawn51 {
  static void check(boolean okay, String name) {if(!okay) throw new AssertionError(name);}
  public static void main(String[] args) {
    int tests=0,oldFalsePositives=0;
    for(int x=-120;x<=120;x+=3) for(int z=-120;z<=120;z+=4) {
      final int xx=x, zz=z;
      check(SpawnRoute51.accessible(x,z,(a,b)->7),"flat path rejected");
      // Complete pedestrian frontage: all 28 positions, not only the 4-wide cross alley.
      for(int bad=1;bad<=26;bad++) {
        final int block=bad;
        SpawnApproach50.Elevation e=(a,b)-> (a==xx+block && b==zz+14)?12:7;
        check(!SpawnRoute51.accessible(x,z,e),"unobserved south frontage blockage x+"+bad);
        if(bad==20 && SpawnApproach50.isWalkable(x,z,e)) oldFalsePositives++;
      }
      check(!SpawnRoute51.accessible(x,z,(a,b)-> (a==xx-14 && b==zz+14)?12:7),"western step missed");
      check(!SpawnRoute51.accessible(x,z,(a,b)-> (a==xx+37 && b==zz+14)?12:7),"eastern step missed");
      for(int delta=15;delta<=17;delta++) {
        final int step=delta;
        check(!SpawnRoute51.accessible(x,z,(a,b)-> (a==xx+27 && b==zz+step)?12:7),"blocked turn to shelter missed");
      }
      // Gradual one-block rise every 8 blocks on the frontage is legal;
      // the entrance elevations need not match as long as each step is walkable.
      SpawnApproach50.Elevation ramp=(a,b)->7+Math.floorDiv(Math.max(0,a-xx),8);
      check(SpawnRoute51.accessible(x,z,ramp),"walkable stepped grade rejected");
      tests++;
    }
    check(oldFalsePositives>1000,"Pass50 gap not reproduced");
    // Ensure regional seed selection retains six legitimate outcomes. The new
    // v34 grading is a probe refinement, not a change to the random selector.
    AtomicInteger[] found=new AtomicInteger[6];
    for(int i=0;i<6;i++)found[i]=new AtomicInteger();
    for(long seed=-12000;seed<12000;seed++) {
      var expected=StartingRegions47.preferred(seed);
      var site=StartingRegions47.chooseWithRegionalFallback(seed,(i,j,o)->true);
      check(site.origin()==expected,"seed preference changed");
      found[site.origin().ordinal()].incrementAndGet();
    }
    for(int i=0;i<6;i++)check(found[i].get()>2000,"unreachable region "+i);
    System.out.printf("PASS Pass51: %,d geometric fixtures; %,d reproduced Pass50 false positives; 24,000 seed preferences%n",tests,oldFalsePositives);
    System.out.println("Actual Minecraft terrain sampling, collisions, and world spawn remain unverified.");
  }
}
JAVA
javac -d "$tmp/classes" \
 src/main/java/com/dorohedoro/hole/worldgen/city/CityHash.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/StartingRegions47.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/SpawnApproach50.java \
 src/main/java/com/dorohedoro/hole/worldgen/city/SpawnRoute51.java \
 "$tmp/VerifySpawn51.java"
java -cp "$tmp/classes" VerifySpawn51
