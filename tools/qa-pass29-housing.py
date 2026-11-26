#!/usr/bin/env python3
"""Real Pass 29 Java compiled with Minecraft-free block stubs plus chunk consistency tests.
This is not NeoForge type checking or an in-game visual check.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
import re,subprocess,json,sys
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
src=(city/'HousingDetail29.java').read_text()
settings=(city/'CitySettings.java').read_text()
normal=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
assert normal['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=17
assert 'Codec.intRange(0, 19).optionalFieldOf("world_detail_version", 0)' in settings
assert 'hasHousing29()' in (city/'LotPainter.java').read_text()
assert 'java.util.Random' not in src
with TemporaryDirectory() as tmp:
 p=Path(tmp)
 def write(name,content):
  q=p/name;q.parent.mkdir(parents=True,exist_ok=True);q.write_text(content)
 for name in ('CityHash','Rect','HousingPlan29','HousingDetail29'):
  write(f'com/dorohedoro/hole/worldgen/city/{name}.java',(city/(name+'.java')).read_text())
 blocks=sorted(set(re.findall(r'Blocks\.([A-Z_0-9]+)',src)) | {'STONE','AIR','GLASS_PANE'})
 write('net/minecraft/core/Direction.java','package net.minecraft.core; public enum Direction { WEST, EAST, NORTH, SOUTH }')
 write('net/minecraft/world/level/block/Block.java','package net.minecraft.world.level.block; public class Block {public final String name;public Block(String n){name=n;}public net.minecraft.world.level.block.state.BlockState defaultBlockState(){return new net.minecraft.world.level.block.state.BlockState(this);}}')
 write('net/minecraft/world/level/block/CrossCollisionBlock.java','package net.minecraft.world.level.block; public class CrossCollisionBlock extends Block {public CrossCollisionBlock(String n){super(n);}}')
 write('net/minecraft/world/level/block/TrapDoorBlock.java','package net.minecraft.world.level.block;public class TrapDoorBlock {public static final Object OPEN=new Object(),FACING=new Object();}')
 write('net/minecraft/world/level/block/state/BlockState.java','package net.minecraft.world.level.block.state;import net.minecraft.world.level.block.Block;public class BlockState {private Block b;public BlockState(Block b){this.b=b;}public Block getBlock(){return b;}public boolean isAir(){return b.name.equals("AIR");}public <T> BlockState setValue(Object prop,T value){return this;}}')
 write('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block;public class Blocks {'+''.join('public static final Block '+name+'=new '+('CrossCollisionBlock' if name=='GLASS_PANE' else 'Block')+'("'+name+'");' for name in blocks)+'}')
 write('com/dorohedoro/hole/worldgen/city/Tower.java','package com.dorohedoro.hole.worldgen.city;class Tower {static final int STOREY=4;enum Roof {PLAIN,TANK,GARDEN,ANTENNA}}')
 write('com/dorohedoro/hole/worldgen/city/District.java','package com.dorohedoro.hole.worldgen.city;enum District { RESIDENTIAL,WATERFRONT,MARKET,INDUSTRIAL,DERELICT,GRAVEYARD,OUTSKIRTS }')
 write('com/dorohedoro/hole/worldgen/city/CityPlan.java','package com.dorohedoro.hole.worldgen.city;class CityPlan {static final int STREET_Y=64,GROUND_Y=65;}')
 write('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.*;
class Canvas {
final int x0,z0,size;final Map<String,String> writes=new HashMap<>();final Map<String,BlockState> state=new HashMap<>();
Canvas(int x,int z,int s){x0=x;z0=z;size=s;}
boolean contains(int x,int z){return x>=x0&&x<x0+size&&z>=z0&&z<z0+size;}
static String key(int x,int y,int z){return x+","+y+","+z;}
BlockState get(int x,int y,int z){if(!contains(x,z))throw new AssertionError("unsafe cross-chunk read");return state.getOrDefault(key(x,y,z), y%4==0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());}
void set(int x,int y,int z,Block b){set(x,y,z,b.defaultBlockState());}
void set(int x,int y,int z,BlockState s){if(!contains(x,z))return;state.put(key(x,y,z),s);writes.put(key(x,y,z),s.getBlock().name);}
}''')
 write('com/dorohedoro/hole/worldgen/city/Pass29Test.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;import net.minecraft.world.level.block.*;
public class Pass29Test {
static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
static Map<String,String> render(long seed,Rect r,int floors,int size){
Map<String,String> out=new HashMap<>();int startX=-64,startZ=-64,end=160;
for(int xx=startX;xx<end;xx+=size)for(int zz=startZ;zz<end;zz+=size){
 Canvas c=new Canvas(xx,zz,size);HousingDetail29.facade(c,r,floors,seed,District.RESIDENTIAL);
 for(int f=0;f<floors;f++)HousingDetail29.furnish(c,r.inset(1),64+f*4,f,seed);
 out.putAll(c.writes);
}return out;
}
public static void main(String[] args){
 int[] roof=new int[5],front=new int[5],household=new int[7];
 for(int k=0;k<800;k++){
  long seed=CityHash.of(1221L,k);int w=9+k%9,d=7+(k/11)%10;int x=-30+(k%6)*23,z=-28+(k%5)*25;
  Rect r=new Rect(x,z,x+w-1,z+d-1);int floors=1+k%3;
  roof[HousingPlan29.roofline(seed,r,floors)]++;front[HousingPlan29.frontage(seed,r,floors)]++;
  household[HousingPlan29.household(seed,r,floors)]++;
  check(HousingPlan29.roofline(seed,r,floors)==HousingPlan29.roofline(seed,r,floors),"roof nondeterminism");
  check(HousingDetail29.chooseRoof(r,floors,seed,Tower.Roof.ANTENNA)==(HousingPlan29.compact(r,floors)?Tower.Roof.PLAIN:Tower.Roof.ANTENNA),"roof type");
  Map<String,String> whole=render(seed,r,floors,256),chunked=render(seed,r,floors,16);
  check(whole.equals(chunked),"chunk seam: seed "+k+" whole="+whole.size()+" chunked="+chunked.size());
  int door=r.centerX(),face=r.z1()+1;
  for(int yy=65;yy<=67;yy++){String block=whole.get(Canvas.key(door,yy,face));check(block==null||block.endsWith("CARPET"),"entrance obstructed by "+block);}
  Rect inside=r.inset(1); for(int f=0;f<floors;f++){String center=whole.get(Canvas.key(inside.centerX(),65+f*4,inside.centerZ()));check(center==null,"room center obstructed by "+center);}
 }
 for(int n:roof)check(n>60,"roofline diversity too low");
 for(int n:front)check(n>60,"facade diversity too low");
 for(int n:household)check(n>55,"household diversity too low");
 System.out.println("PASS29 housing: 800 seeds, chunk-consistent write maps, 5 rooflines, 5 fronts, 7 household identities; roof="+Arrays.toString(roof)+" front="+Arrays.toString(front)+" households="+Arrays.toString(household));
}
}''')
 cmd=['javac','-d',str(p/'classes')]+[str(f) for f in p.rglob('*.java')]
 a=subprocess.run(cmd,capture_output=True,text=True)
 if a.returncode:print(a.stderr);sys.exit(a.returncode)
 b=subprocess.run(['java','-cp',str(p/'classes'),'com.dorohedoro.hole.worldgen.city.Pass29Test'],capture_output=True,text=True)
 print(b.stdout or b.stderr)
 if b.returncode:sys.exit(b.returncode)
