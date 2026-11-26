#!/usr/bin/env bash
# Compiles REAL facade source against focused stand-ins for Minecraft's blocks.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
PKG="$TMP/src/com/dorohedoro/hole/worldgen/city"
BLOCK="$TMP/src/net/minecraft/world/level/block"
mkdir -p "$PKG" "$BLOCK" "$TMP/out"
cp "$ROOT/src/main/java/com/dorohedoro/hole/worldgen/city/"{FacadeDepth41,CityHash,Rect}.java "$PKG/"
cat > "$BLOCK/Block.java" <<'JAVA'
package net.minecraft.world.level.block;
public class Block {}
JAVA
cat > "$BLOCK/CrossCollisionBlock.java" <<'JAVA'
package net.minecraft.world.level.block;
public class CrossCollisionBlock extends Block {}
JAVA
cat > "$BLOCK/Blocks.java" <<'JAVA'
package net.minecraft.world.level.block;
public class Blocks {
    public static final Block AIR=new Block(), WAXED_WEATHERED_CUT_COPPER_SLAB=new Block(),
        DEEPSLATE_BRICK_SLAB=new Block(),DARK_OAK_SLAB=new Block(),SPRUCE_SLAB=new Block(),
        TUFF_BRICK_SLAB=new Block(),IRON_BARS=new Block();
}
JAVA
cat > "$PKG/Fixtures.java" <<'JAVA'
package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.*;
final class CityPlan {static final int STREET_Y=64, GROUND_Y=65;}
final class Tower {static final int STOREY=4;}
enum District {INDUSTRIAL, WATERFRONT, DERELICT, GRAVEYARD, MARKET, OUTSKIRTS, RESIDENTIAL;}
final class Canvas {
    record Cell(int x,int y,int z){}
    static final class State {
        final Block block;
        State(Block b){block=b;}
        boolean isAir(){return block==Blocks.AIR;}
        Block getBlock(){return block;}
    }
    final HashMap<Cell,State> blocks=new HashMap<>();
    boolean contains(int x,int z){return true;}
    State get(int x,int y,int z){return blocks.getOrDefault(new Cell(x,y,z),new State(Blocks.AIR));}
    void set(int x,int y,int z,Block b){blocks.put(new Cell(x,y,z),new State(b));}
}
public class Fixtures {
    public static void main(String[] args) {
        long tested=0, details=0;
        for(District district:District.values())for(int w=11;w<=25;w+=7)
        for(int floors=1;floors<=9;floors+=2)for(long seed=0;seed<60;seed++){
            Canvas c=new Canvas();
            Rect box=new Rect(100,100,100+w-1,116);
            // Three existing window panes per upper storey; a door at front center.
            for(int f=1;f<floors;f++)for(int x=103;x<100+w-2;x+=4)
                c.set(x,CityPlan.STREET_Y+f*4+2,box.z1(),new CrossCollisionBlock());
            Canvas.Cell door=new Canvas.Cell(box.centerX(),CityPlan.GROUND_Y,box.z1()+1);
            c.set(door.x(),door.y(),door.z(),new Block());
            FacadeDepth41.residence(c,box,floors,seed,district);
            if(c.get(door.x(),door.y(),door.z()).isAir())throw new AssertionError("door overwritten");
            for(Canvas.Cell pos:c.blocks.keySet()) {
                if(pos.z()==box.z1()+1 && pos.y()>CityPlan.GROUND_Y+1) details++;
                if(pos.z()==box.z1()+1 && pos.y()<=CityPlan.GROUND_Y+1 && !pos.equals(door))
                    throw new AssertionError("sidewalk height blocked");
            }
            tested++;
        }
        if(details<100)throw new AssertionError("no facade depth placed");
        System.out.println("Pass41 facades: "+tested+" mock buildings across district/size/floor/seed, elevated accents="+details);
        System.out.println("Scope: block and collision fixture, not NeoForge render or walkability test.");
    }
}
JAVA
javac -d "$TMP/out" $(find "$TMP/src" -name '*.java')
java -cp "$TMP/out" com.dorohedoro.hole.worldgen.city.Fixtures
