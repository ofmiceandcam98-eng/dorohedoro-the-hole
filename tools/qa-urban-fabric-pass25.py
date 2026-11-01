from pathlib import Path
import re, subprocess, shutil, json, tempfile
r=Path(__file__).resolve().parents[1]; city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'; tmp=Path(tempfile.mkdtemp(prefix='pass25_urban_qa_'))
def write(rel,t):
 p=tmp/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(t)
for f in ['UrbanFabric25.java','CityHash.java','Line.java','Rect.java','LotType.java','Lot.java']:
 write('com/dorohedoro/hole/worldgen/city/'+f,(city/f).read_text())
text=(city/'UrbanFabric25.java').read_text()
blocks=sorted(set(re.findall(r'Blocks\.([A-Z_0-9]+)',text)))
write('net/minecraft/world/level/block/Block.java','package net.minecraft.world.level.block; public class Block {public boolean isAir(){return this==Blocks.AIR;}}')
write('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block; public class Blocks {\n'+'\n'.join('public static final Block '+name+'=new Block();' for name in sorted(set(blocks+['AIR'])))+'\n}')
write('com/dorohedoro/hole/worldgen/city/District.java','package com.dorohedoro.hole.worldgen.city; enum District {RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT, DERELICT, GRAVEYARD, OUTSKIRTS}')
write('com/dorohedoro/hole/worldgen/city/Tower.java','package com.dorohedoro.hole.worldgen.city; final class Tower {enum Roof {PLAIN, TANK, GARDEN, ANTENNA}}')
write('com/dorohedoro/hole/worldgen/city/CityPlan.java','''package com.dorohedoro.hole.worldgen.city;
class CityPlan {long seed; static final int STREET_Y=64, GROUND_Y=65;
 CityPlan(long s){seed=s;} int xAvenueOffset(Line l,int z){return 0;}int zAvenueOffset(Line l,int x){return 0;}
 District districtAt(int x,int z){return District.values()[CityHash.range(CityHash.of(seed,Math.floorDiv(x,128),Math.floorDiv(z,128)),District.values().length)];}}
''')
write('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.block.Block;import net.minecraft.world.level.block.Blocks;
import java.util.*;
class Canvas {final int minX,maxX,minZ,maxZ;private long seed;final Map<String,Block> writes=new HashMap<>();
 Canvas(int minX,int minZ,int size,long seed){this.minX=minX;this.maxX=minX+size-1;this.minZ=minZ;this.maxZ=minZ+size-1;this.seed=seed;}
 boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;} long seed(){return seed;}
 void set(int x,int y,int z,Block block){if(contains(x,z))writes.put(x+","+y+","+z,block);}
 void fill(int x,int y,int z,int x1,int y1,int z1,Block block){for(int a=x;a<=x1;a++)for(int b=y;b<=y1;b++)for(int cc=z;cc<=z1;cc++)set(a,b,cc,block);}
 Block get(int x,int y,int z){if(y==64) return Blocks.COBBLED_DEEPSLATE;return writes.getOrDefault(x+","+y+","+z,Blocks.AIR);}
}''')
write('com/dorohedoro/hole/worldgen/city/FabricTest.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;
class FabricTest {
 static void check(boolean valid,String msg){if(!valid)throw new AssertionError(msg);}
 static Map<String,net.minecraft.world.level.block.Block> canvas(long seed,int originX,int originZ,int size){
  Canvas c=new Canvas(originX,originZ,size,seed); CityPlan plan=new CityPlan(seed);
  List<Line> xl=List.of(new Line(0,20,4,Line.Kind.AVENUE),new Line(1,80,7,Line.Kind.TRAM));
  List<Line> zl=List.of(new Line(0,70,4,Line.Kind.AVENUE),new Line(1,110,9,Line.Kind.CANAL));
  List<Lot> lots=List.of(new Lot(new Rect(30,30,45,45),LotType.SHOP,District.MARKET,23L));
  UrbanFabric25.streets(c,plan,xl,zl,lots);return c.writes;
 }
 public static void main(String[] args){
  int patches=0; for(int x=-300;x<300;x++)for(int z=-300;z<300;z++)if(UrbanFabric25.wearPatch(1234L,x,z))patches++;
  check(patches>1000&&patches<100000,"unbalanced surface patches: "+patches);
  Map<String,net.minecraft.world.level.block.Block> whole=canvas(1234L,0,0,128);
  Map<String,net.minecraft.world.level.block.Block> chunked=new HashMap<>();
  for(int x=0;x<128;x+=16)for(int z=0;z<128;z+=16) chunked.putAll(canvas(1234L,x,z,16));
  check(whole.equals(chunked),"chunk order or boundary inconsistency");
  for(int x=0;x<128;x+=16)for(int z=0;z<128;z+=16) check(canvas(1234L,x,z,16).equals(canvas(1234L,x,z,16)),"non-deterministic");
  check(!whole.equals(canvas(5678L,0,0,128)),"seed not affecting streets");
  for(String p:whole.keySet()){
   String[] xyz=p.split(",");int x=Integer.parseInt(xyz[0]),z=Integer.parseInt(xyz[2]);
   check(!(x>=30&&x<=45&&z>=30&&z<=45),"lot overwritten");
   check(!(x>=73&&x<=87),"tram overwritten");
   check(!(z>=101&&z<=119),"river overwritten");
  }
  // Roofs and yards are independently reproducible on chunk boundaries.
  Canvas y=new Canvas(0,0,128,1234L); Lot l=new Lot(new Rect(8,8,40,40),LotType.SHOP,District.MARKET,9876L);
  UrbanFabric25.yard(y,l);check(!y.writes.isEmpty(),"yard not placed");
  Map<String,net.minecraft.world.level.block.Block> splitYard=new HashMap<>();
  for(int xx=0;xx<128;xx+=16)for(int zz=0;zz<128;zz+=16){
   Canvas part=new Canvas(xx,zz,16,1234L);UrbanFabric25.yard(part,l);splitYard.putAll(part.writes);
  }
  check(y.writes.equals(splitYard),"yard changes at chunk edges");
  Canvas roof=new Canvas(0,0,128,1234L);
  UrbanFabric25.roof(roof,new Rect(8,8,30,30),84,999L,Tower.Roof.PLAIN);
  check(!roof.writes.isEmpty(),"roof empty");
  Map<String,net.minecraft.world.level.block.Block> splitRoof=new HashMap<>();
  for(int xx=0;xx<128;xx+=16)for(int zz=0;zz<128;zz+=16){
   Canvas part=new Canvas(xx,zz,16,1234L);
   UrbanFabric25.roof(part,new Rect(8,8,30,30),84,999L,Tower.Roof.PLAIN);
   splitRoof.putAll(part.writes);
  }
  check(roof.writes.equals(splitRoof),"roof changes at chunk edges");
  Canvas farm=new Canvas(0,0,128,1234L);UrbanFabric25.roof(farm,new Rect(8,8,30,30),84,999L,Tower.Roof.GARDEN);
  check(farm.writes.isEmpty(),"rooftop farm damaged");
  Set<Integer> featurePositions=new HashSet<>();for(int a=-400;a<400;a++){int n=UrbanFabric25.streetFeatureAt(1234L,3,a);if(a==n)featurePositions.add(a);}
  check(featurePositions.size()>=12,"too few street service sites");
  System.out.printf("PASS: seeded patch coverage=%.2f%%, whole=%d, split=%d, feature sites=%d; varied seed, lot/tram/river protections, roof/yard invariants\\n",100.0*patches/360000,whole.size(),chunked.size(),featurePositions.size());
 }
}
''')
compiler=subprocess.run(['javac','-d',str(tmp/'classes'),*map(str,tmp.rglob('*.java'))],capture_output=True,text=True)
print('stub compile exit:',compiler.returncode,compiler.stderr[:1000]);assert compiler.returncode==0
result=subprocess.run(['java','-cp',str(tmp/'classes'),'com.dorohedoro.hole.worldgen.city.FabricTest'],capture_output=True,text=True)
print(result.stdout, result.stderr[:1200]);assert result.returncode==0
jsons=list((r/'src/main/resources').rglob('*.json'))
for f in jsons:json.loads(f.read_text())
print('parsed JSON:',len(jsons))

shutil.rmtree(tmp,ignore_errors=True)
