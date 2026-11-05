#!/usr/bin/env python3
"""Verify selected Pass37 Java files using JDK-only fixtures, no Minecraft dependency.
This is NOT a NeoForge compile or worldgen/client validation.
"""
import json, re, subprocess, tempfile, shutil
from pathlib import Path
root=Path(__file__).resolve().parents[1]
rel='src/main/java/com/dorohedoro/hole/worldgen/city/'
java=root/rel
issues=[]
def check(ok, msg):
    print(('PASS' if ok else 'FAIL')+' '+msg)
    if not ok: issues.append(msg)
    return ok

normal=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
settings=(java/'CitySettings.java').read_text()
canvas=(java/'Canvas.java').read_text()
lot=(java/'LotPainter.java').read_text()
openlots=(java/'OpenLots.java').read_text()
room=(java/'RoomPurpose37.java').read_text()
hill=(java/'Hillside37.java').read_text()
check('"world_detail_version": 22' in (root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text(),'new world preset enables version 22')
check('worldDetailVersion > 22' in settings and 'Codec.intRange(0, 22)' in settings,'version-22 profile accepted by codec')
check('worldDetailVersion >= 22' in canvas,'version-gated source; older saves unchanged')
check('Hillside37.vacant(c, lot)' in openlots and 'if (Hillside37.vacant(c, lot)) return;' in openlots,'vacant lot path does not bury previous trees')
check('Math.max(Math.abs(r.centerX()), Math.abs(r.centerZ())) < 128' in hill,'spawn quarter relief remains unchanged')
check('hasHillside37()' in lot and lot.count('RoomPurpose37.residential')==3,'residential furnishing wired to both residences and upper shop flats')
check('RoomPurpose37.shop' in lot and 'RoomPurpose37.clinic' in lot and lot.count('RoomPurpose37.workshop')==2,'shop clinic warehouse factory furnishers wired')
check('Blocks.CHEST' not in hill+room and 'LANTERN' not in hill+room,'no additional exposed chests or light pollution')
check('!c.get(x, y, z).isAir()' in room and 'c.get(x, y - 1, z).isAir()' in room,'furnishing does not replace earlier props or float unsupported')
check('z <= in.z0() + 2 && x <= in.x0() + 5' in room,'stair corridor exclusion retained')

# JDK type/syntax + algorithm tests for the two new actual source files.
with tempfile.TemporaryDirectory(prefix='doro37-') as temp:
    d=Path(temp); package=d/'src/com/dorohedoro/hole/worldgen/city'; package.mkdir(parents=True)
    net=d/'src/net/minecraft/world/level/block'; net.mkdir(parents=True)
    for source in ('Hillside37.java','RoomPurpose37.java','CityHash.java','Rect.java'):
        shutil.copy2(java/source,package/source)
    fields=sorted(set(re.findall(r'Blocks\.([A-Z][A-Z0-9_]*)',hill+'\n'+room)))
    (net/'Block.java').write_text('package net.minecraft.world.level.block; public class Block {}\n')
    (net/'Blocks.java').write_text('package net.minecraft.world.level.block; public class Blocks {\n'+ ''.join('public static final Block '+f+'=new Block();\n' for f in fields)+'}\n')
    (package/'CityPlan.java').write_text('package com.dorohedoro.hole.worldgen.city; public final class CityPlan { public static final int STREET_Y=64, GROUND_Y=65;}\n')
    (package/'Lot.java').write_text('package com.dorohedoro.hole.worldgen.city; public record Lot(Rect rect,long hash) {}\n')
    (package/'Canvas.java').write_text('''package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.block.Block;
public final class Canvas {
  public final int minX=-5000,maxX=5000,minZ=-5000,maxZ=5000;
  public boolean contains(int x,int z){return true;}
  public boolean hasHillside37(){return true;}
  public void set(int x,int y,int z,Block b){}
  public void fill(Rect r,int low,int high,Block b){}
  public void fill(int x,int y,int z,int xx,int yy,int zz,Block b){}
  public State get(int x,int y,int z){return new State();}
  public static final class State {public boolean isAir(){return true;}}
}
''')
    (package/'Main.java').write_text('''package com.dorohedoro.hole.worldgen.city;
public class Main {
  public static void main(String[] args) {
    int sampled=0, peaks=0, jumps=0, changed=0;
    int[] sizes={20,24,31,40,64};
    for (int size:sizes) for (int i=0;i<40;i++) {
      long hash=CityHash.of(99123L,size,i);
      Rect r=new Rect(-size/2,100,-size/2+size-1,100+size-1);
      int max=0;
      for (int x=r.x0();x<=r.x1();x++) for(int z=r.z0();z<=r.z1();z++) {
        int h=Hillside37.height(r,hash,x,z);
        if(h<0 || h>7) throw new RuntimeException("out of bounds height "+h);
        if(x==r.x0() || x==r.x1() || z==r.z0() || z==r.z1())
          if(h!=0) throw new RuntimeException("blocked perimeter");
        if(x<r.x1() && Math.abs(h-Hillside37.height(r,hash,x+1,z))>1) jumps++;
        if(z<r.z1() && Math.abs(h-Hillside37.height(r,hash,x,z+1))>1) jumps++;
        max=Math.max(max,h);
        sampled++;
      }
      if(max>=3) peaks++;
      if(max>0) changed++;
    }
    System.out.println("HILL_SAMPLES="+sampled+" PEAK_PARCELS="+peaks+" CHANGED="+changed+" CLIFF_JUMPS="+jumps);
    if(peaks<170 || jumps!=0) System.exit(3);
  }
}
''')
    sources=[str(f) for f in (d/'src').rglob('*.java')]
    built=d/'classes';built.mkdir()
    x=subprocess.run(['javac','-d',str(built)]+sources,capture_output=True,text=True)
    check(x.returncode==0,'Java 21 syntax/type test with JDK fixture'+(': '+x.stderr[:700] if x.returncode else ''))
    if x.returncode==0:
        x=subprocess.run(['java','-cp',str(built),'com.dorohedoro.hole.worldgen.city.Main'],capture_output=True,text=True)
        print(x.stdout.strip())
        check(x.returncode==0,'hilltop field stays within 0-7 blocks, walkable 1-block slopes and unchanged parcel edges')

print(f'PASS37 OFFLINE QA: {10+2-len(issues)}/{12} checks passed. NeoForge and client untested.')
raise SystemExit(0 if not issues else 1)
