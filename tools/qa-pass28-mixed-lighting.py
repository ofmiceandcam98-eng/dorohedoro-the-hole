#!/usr/bin/env python3
"""JDK stub compilation and pure placement logic checks; not Minecraft runtime QA."""
from pathlib import Path
from tempfile import TemporaryDirectory
import re, subprocess, json
root=Path(__file__).resolve().parents[1]; city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
src=(city/'ResidentialAtmosphere28.java').read_text()
assert 'ModBlocks.CAVERN_LAMP.get()' in src and 'Blocks.LANTERN' in src and 'Blocks.WALL_TORCH' in src
assert 'java.util.Random' not in src
assert 'Math.floorDiv(x, 16)' in src
assert (json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version'])>=16
with TemporaryDirectory() as d:
 r=Path(d)
 def put(rel,s):
  p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s)
 for name in ['ResidentialAtmosphere28.java','CityHash.java','Rect.java']:
  put('com/dorohedoro/hole/worldgen/city/'+name,(city/name).read_text())
 b=sorted(set(re.findall(r'Blocks\.([A-Z_0-9]+)',src)) | {'AIR','STONE'})
 put('net/minecraft/core/Direction.java','''package net.minecraft.core;
 public enum Direction { EAST(1,0),WEST(-1,0),NORTH(0,-1),SOUTH(0,1);int x,z;Direction(int x,int z){this.x=x;this.z=z;}public int getStepX(){return x;}public int getStepZ(){return z;} }
 ''')
 put('net/minecraft/world/level/block/Block.java','''package net.minecraft.world.level.block;
 public class Block {public final String name;public Block(String n){name=n;}public net.minecraft.world.level.block.state.BlockState defaultBlockState(){return new net.minecraft.world.level.block.state.BlockState(this);} }
 ''')
 put('net/minecraft/world/level/block/state/BlockState.java','''package net.minecraft.world.level.block.state;
 import net.minecraft.world.level.block.Block;public class BlockState {final Block b;public BlockState(Block b){this.b=b;}public boolean isAir(){return b.name.equals("AIR");}public Block getBlock(){return b;}public BlockState setValue(Object p,boolean v){return this;}public BlockState setValue(Object p,net.minecraft.core.Direction v){return this;} }
 ''')
 put('net/minecraft/world/level/block/LanternBlock.java','package net.minecraft.world.level.block;public class LanternBlock{public static final Object HANGING=new Object();}')
 put('net/minecraft/world/level/block/WallTorchBlock.java','package net.minecraft.world.level.block;public class WallTorchBlock{public static final Object FACING=new Object();}')
 put('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block;public class Blocks {'+''.join('public static final Block '+x+'=new Block("'+x+'");' for x in b)+'}')
 put('com/dorohedoro/hole/registry/ModBlocks.java','''package com.dorohedoro.hole.registry;
 import net.minecraft.world.level.block.Block;public class ModBlocks {public static final Holder CAVERN_LAMP=new Holder();public static final class Holder {public Block get(){return new Block("CAVERN_LAMP");}}}''')
 put('com/dorohedoro/hole/worldgen/city/District.java','package com.dorohedoro.hole.worldgen.city;enum District{RESIDENTIAL,WATERFRONT,MARKET,INDUSTRIAL,DERELICT,GRAVEYARD,OUTSKIRTS;}')
 put('com/dorohedoro/hole/worldgen/city/CityPlan.java','package com.dorohedoro.hole.worldgen.city;class CityPlan{static final int STREET_Y=64;}')
 put('com/dorohedoro/hole/worldgen/city/Tower.java','package com.dorohedoro.hole.worldgen.city;class Tower{static final int STOREY=4;}')
 put('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
 import java.util.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.BlockState;
 class Canvas {final int minX,maxX,minZ,maxZ;Map<String,BlockState> set=new HashMap<>();Canvas(int x0,int z0,int size){minX=x0;maxX=x0+size-1;minZ=z0;maxZ=z0+size-1;}
 boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}static String k(int x,int y,int z){return x+","+y+","+z;}
 BlockState get(int x,int y,int z){if(!contains(x,z))throw new IllegalStateException("out-of-chunk get");return set.getOrDefault(k(x,y,z),y%4==0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());}
 void set(int x,int y,int z,Block b){set(x,y,z,b.defaultBlockState());}void set(int x,int y,int z,BlockState b){if(contains(x,z))set.put(k(x,y,z),b);}}
 ''')
 put('com/dorohedoro/hole/worldgen/city/Pass28Test.java','''package com.dorohedoro.hole.worldgen.city;
 import java.util.*;import net.minecraft.core.Direction;import net.minecraft.world.level.block.*;
 public class Pass28Test {
 static void check(boolean x,String message){if(!x)throw new AssertionError(message);}
 public static void main(String[] args){
 int cavern=0,normal=0;for(long i=1;i<=5000;i++){String n=ResidentialAtmosphere28.lightState(i).getBlock().name;if(n.equals("CAVERN_LAMP"))cavern++;else if(n.equals("LANTERN"))normal++;else throw new AssertionError(n);}
 check(cavern>2500&&cavern<3600,"cavern ratio");check(normal>1400,"normal lanterns present");
 Canvas torch=new Canvas(0,0,16);torch.set(0,65,7,Blocks.STONE);ResidentialAtmosphere28.wallTorch(torch,1,65,7,Direction.EAST);
 check(torch.get(1,65,7).getBlock()==Blocks.WALL_TORCH,"torch with solid support missing");
 Canvas gap=new Canvas(0,0,16);ResidentialAtmosphere28.wallTorch(gap,1,65,7,Direction.EAST);
 check(gap.get(1,65,7).isAir(),"unsupported wall torch placed");
 Canvas seam=new Canvas(16,0,16);ResidentialAtmosphere28.wallTorch(seam,16,65,7,Direction.EAST);
 check(seam.get(16,65,7).isAir(),"seam torch relied on unavailable support");
 Canvas studio=new Canvas(0,0,16);Rect room=new Rect(1,1,12,12);
 for(int k=0;k<300;k++){Canvas c=new Canvas(0,0,16);ResidentialAtmosphere28.residence(c,room,64,k,k+998);
 check(c.get(room.centerX(),65,room.centerZ()).isAir(),"blocked central aisle");
 for(String key:c.set.keySet()){int x=Integer.parseInt(key.split(",")[0]),z=Integer.parseInt(key.split(",")[2]);check(x!=room.centerX(),"central axis changed");check(z>=room.z0()+4,"north staircase clutter");}
 Canvas other=new Canvas(0,0,16);ResidentialAtmosphere28.residence(other,room,64,k,k+998);
 check(c.set.keySet().equals(other.set.keySet()),"seed non-determinism");}
 System.out.println("PASS28 functional stub: 5000 light choices, 300 deterministic rooms, true wall support, no central corridor blockage; Cavern="+cavern+" Vanilla="+normal);
 }
 }''')
 files=[str(f) for f in r.rglob('*.java')]
 compile=subprocess.run(['javac','-d',str(r/'classes'),*files],capture_output=True,text=True)
 print('JDK stub compilation:',compile.returncode)
 if compile.returncode: print(compile.stderr);raise SystemExit(1)
 run=subprocess.run(['java','-cp',str(r/'classes'),'com.dorohedoro.hole.worldgen.city.Pass28Test'],capture_output=True,text=True)
 print(run.stdout or run.stderr)
 if run.returncode:raise SystemExit(run.returncode)
