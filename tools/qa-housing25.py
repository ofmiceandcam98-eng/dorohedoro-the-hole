#!/usr/bin/env python3
"""Offline, actual Java Housing25 compilation and functional harness with stubbed world APIs.
This IS NOT a Minecraft/NeoForge test or guarantee of visual quality.
"""
from pathlib import Path
import tempfile, re, subprocess, json
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
source=(city/'Housing25.java').read_text()
lot=(city/'LotPainter.java').read_text()
settings=(city/'CitySettings.java').read_text()
canvas=(city/'Canvas.java').read_text()
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
assert preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']==14
assert 'worldDetailVersion >= 14' in canvas and 'Codec.intRange(0, 14)' in settings
assert 'Housing25.floors(false' in lot and 'Housing25.floors(true' in lot
assert lot.count('Housing25.facade(')==2
assert 'Housing25.residence(' in lot and 'Housing25.residentialDetails(' in lot
assert 'case SHOP' in lot and 'box.width() <= 15' in lot
assert source.count('CityHash.of(')>=2 and 'new java.util.Random' not in source
blocks=sorted(set(re.findall(r'Blocks\.([A-Z][A-Z0-9_]+)',source)))
with tempfile.TemporaryDirectory(prefix='doro_housing25_') as d:
 p=Path(d)
 def put(path, data):
  f=p/path; f.parent.mkdir(parents=True,exist_ok=True);f.write_text(data)
 for src in ['Housing25.java','CityHash.java','Rect.java']:
  put('com/dorohedoro/hole/worldgen/city/'+src, (city/src).read_text())
 put('net/minecraft/core/Direction.java','package net.minecraft.core; public enum Direction {SOUTH,NORTH,EAST,WEST;}')
 put('net/minecraft/world/level/block/Block.java','package net.minecraft.world.level.block; public class Block {public final String name;public Block(String name){this.name=name;} public BlockState defaultBlockState(){return new BlockState(this);} }')
 put('net/minecraft/world/level/block/BlockState.java','package net.minecraft.world.level.block; public class BlockState {public final Block block;public BlockState(Block b){this.block=b;}public BlockState setValue(Object k,boolean b){return this;}public boolean isAir(){return block.name.equals("AIR");}}')
 put('net/minecraft/world/level/block/LanternBlock.java','package net.minecraft.world.level.block; public class LanternBlock {public static final Object HANGING=new Object();}')
 put('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block; public class Blocks {public static final Block AIR=new Block("AIR");'+''.join('public static final Block '+b+'=new Block("'+b+'");' for b in blocks if b!='AIR')+'}')
 put('com/dorohedoro/hole/worldgen/city/District.java','package com.dorohedoro.hole.worldgen.city; enum District {RESIDENTIAL,WATERFRONT,MARKET,INDUSTRIAL,DERELICT,GRAVEYARD,OUTSKIRTS;}')
 put('com/dorohedoro/hole/worldgen/city/CityPlan.java','package com.dorohedoro.hole.worldgen.city; class CityPlan {static final int GROUND_Y=65,STREET_Y=64;}')
 put('com/dorohedoro/hole/worldgen/city/Tower.java','package com.dorohedoro.hole.worldgen.city; class Tower {static final int STOREY=4;static void lamp(Canvas c,int x,int y,int z){c.set(x,y+3,z,net.minecraft.world.level.block.Blocks.LANTERN);}}')
 put('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;import net.minecraft.world.level.block.*;import net.minecraft.core.Direction;
class Canvas {
 Map<String,String> blocks=new HashMap<>(); int beds=0; int lights=0; int frontDoorBlocked=0;
 static String key(int x,int y,int z){return x+","+y+","+z;}
 boolean contains(int x,int z){return true;}
 BlockState get(int x,int y,int z){if(y==64||y%4==0)return Blocks.BARREL.defaultBlockState();return new BlockState(new Block(blocks.getOrDefault(key(x,y,z),"AIR")));}
 void set(int x,int y,int z,Block b){set(x,y,z,b.defaultBlockState());}
 void set(int x,int y,int z,BlockState b){blocks.put(key(x,y,z),b.block.name);if(b.block.name.equals("LANTERN")||b.block.name.equals("SEA_LANTERN"))lights++;}
 void bed(int x,int y,int z,Direction dir,Block b){beds++;set(x,y,z,b);}
 void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block b){for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)set(x,y,z,b);}
}
''')
 put('com/dorohedoro/hole/worldgen/city/HousingTest.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;
public class HousingTest {
 static void yes(boolean b,String why){if(!b)throw new RuntimeException(why);}
 public static void main(String... args){
  int layouts=0, totalBeds=0,totalLights=0;Set<Integer> heights=new HashSet<>();Set<Integer> facadeKinds=new HashSet<>();
  for(int size:new int[]{9,11,14,17,22})for(long seed=1;seed<=80;seed++){
   Rect box=new Rect(0,0,size-1,size-1);
   for(boolean apt:new boolean[]{false,true}){
    int floors=Housing25.floors(apt,box,CityHash.of(seed,89123),11);
    int floorsRepeat=Housing25.floors(apt,box,CityHash.of(seed,89123),11);
    yes(floors==floorsRepeat,"not deterministic");yes(floors>=1&&floors<=11,"height bounds");
    if(size==9)yes(floors<=3,"small footprint tower");
    if(size==22&&apt)yes(floors>=6,"large apartment too small");
    heights.add(floors);
    Canvas c=new Canvas();Rect in=box.inset(1);
    if(Housing25.compact(box,floors)){
     for(int storey=0;storey<floors;storey++)Housing25.residence(c,in,64+4*storey,storey,floors,seed);
     yes(c.beds>0,"compact housing lacks bed for "+size+" "+floors);
     yes(c.lights>0,"compact housing lacks light");
     totalBeds+=c.beds;totalLights+=c.lights;
    }
    Housing25.facade(c,box,floors,CityHash.of(seed,89123),District.RESIDENTIAL);
    yes(!c.blocks.containsKey(Canvas.key(box.centerX(),65,box.z1())),"door bottom blocked");
    yes(!c.blocks.containsKey(Canvas.key(box.centerX(),66,box.z1())),"door top blocked");
    // Entrance path outside facade must not contain a solid feature at feet/head height.
    yes(!c.blocks.containsKey(Canvas.key(box.centerX(),65,box.z1()+1)),"front path blocked");
    yes(!c.blocks.containsKey(Canvas.key(box.centerX(),66,box.z1()+1)),"front headroom blocked");
    yes(c.blocks.size()>0,"no decor");layouts++;
   }
  }
  yes(heights.size()>=7,"not enough height variety");
  System.out.printf("HOUSING25 HARNESS: %d configurations, %d bed placements, %d lamps, %d distinct floor counts; seed and door checks passed%n", layouts,totalBeds,totalLights,heights.size());
 }
}
''')
 build=subprocess.run(['javac','-d',str(p/'classes'),*map(str,p.rglob('*.java'))],capture_output=True,text=True)
 assert build.returncode==0,build.stderr
 run=subprocess.run(['java','-cp',str(p/'classes'),'com.dorohedoro.hole.worldgen.city.HousingTest'],capture_output=True,text=True)
 print(run.stdout,run.stderr)
 assert run.returncode==0,run.stderr
print('PASS housing25 source wiring and preset version assertions')
