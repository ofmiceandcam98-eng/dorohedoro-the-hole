#!/usr/bin/env python3
"""Pass 31: compile the actual v26+v28 fixture classes with deterministic Minecraft-free stubs.
Tests torch support, door-safe placement, and chunk-vs-whole generation.
This does NOT validate NeoForge APIs or in-game lighting.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
import re, subprocess, sys
root = Path(__file__).resolve().parents[1]
city = root / 'src/main/java/com/dorohedoro/hole/worldgen/city'
sources = {name: (city / (name + '.java')).read_text() for name in
           ('CityHash','Rect','ResidentialAtmosphere28','ResidentialDetails26')}
assert 'ResidentialAtmosphere28.wallTorch(c, west, y + 2' in sources['ResidentialDetails26']
assert 'ResidentialAtmosphere28.wallTorch(c, east, y + 2' in sources['ResidentialDetails26']
assert 'c.hasHousingLighting31()' in sources['ResidentialDetails26']
assert 'hasHousingLighting31() { return worldDetailVersion >= 18; }' in (city/'Canvas.java').read_text()
assert 'Codec.intRange(0, 19).optionalFieldOf("world_detail_version", 0)' in (city/'CitySettings.java').read_text()
import json
normal=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
assert normal['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=18
with TemporaryDirectory(prefix='pass31_lighting_') as tmp:
    root2 = Path(tmp)
    def put(name, content):
        f=root2/name; f.parent.mkdir(parents=True,exist_ok=True); f.write_text(content)
    for name, body in sources.items():
        put(f'com/dorohedoro/hole/worldgen/city/{name}.java', body)
    blocks=sorted(set(re.findall(r'Blocks\.([A-Z_0-9]+)', ''.join(sources.values()))) | {'AIR','STONE'})
    put('net/minecraft/core/Direction.java', '''package net.minecraft.core;
    public enum Direction {EAST(1,0), WEST(-1,0), NORTH(0,-1), SOUTH(0,1);
    private final int x,z; Direction(int x,int z){this.x=x;this.z=z;}
    public int getStepX(){return x;}public int getStepZ(){return z;}}''')
    put('net/minecraft/world/level/block/Block.java', '''package net.minecraft.world.level.block;
    public class Block {public final String name;public Block(String n){name=n;}
    public net.minecraft.world.level.block.state.BlockState defaultBlockState(){return new net.minecraft.world.level.block.state.BlockState(this);}}''')
    put('net/minecraft/world/level/block/state/BlockState.java','''package net.minecraft.world.level.block.state;
    import net.minecraft.world.level.block.Block;
    public class BlockState {public final Block b;public BlockState(Block b){this.b=b;}
    public Block getBlock(){return b;}public boolean isAir(){return b.name.equals("AIR");}
    public <T> BlockState setValue(Object prop,T value){return this;}}''')
    put('net/minecraft/world/level/block/LanternBlock.java','package net.minecraft.world.level.block;public class LanternBlock{public static final Object HANGING=new Object();}')
    put('net/minecraft/world/level/block/WallTorchBlock.java','package net.minecraft.world.level.block;public class WallTorchBlock{public static final Object FACING=new Object();}')
    put('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block;public class Blocks {'+''.join('public static final Block '+x+'=new Block("'+x+'");' for x in blocks)+'}')
    put('com/dorohedoro/hole/registry/ModBlocks.java','''package com.dorohedoro.hole.registry;
    import net.minecraft.world.level.block.Block;public class ModBlocks {public static final Holder CAVERN_LAMP=new Holder();
    public static final class Holder {public Block get(){return new Block("CAVERN_LAMP");}}}''')
    put('com/dorohedoro/hole/worldgen/city/District.java', 'package com.dorohedoro.hole.worldgen.city;enum District {RESIDENTIAL,MARKET,WATERFRONT,INDUSTRIAL,DERELICT,GRAVEYARD,OUTSKIRTS;}')
    put('com/dorohedoro/hole/worldgen/city/CityPlan.java', 'package com.dorohedoro.hole.worldgen.city;class CityPlan {static final int STREET_Y=64;}')
    put('com/dorohedoro/hole/worldgen/city/Tower.java', 'package com.dorohedoro.hole.worldgen.city;class Tower{static final int STOREY=4;}')
    put('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
    import java.util.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.*;
    class Canvas {
      final int minX,minZ,maxX,maxZ;final Rect room;final boolean lighting31;
      final Map<String,String> writes=new HashMap<>();final Map<String,BlockState> state=new HashMap<>();
      Canvas(int x,int z,int s,Rect r,boolean v18){minX=x;minZ=z;maxX=x+s-1;maxZ=z+s-1;room=r;lighting31=v18;}
      static String key(int x,int y,int z){return x+","+y+","+z;}
      boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
      boolean hasAtmosphere28(){return true;}
      boolean hasHousingLighting31(){return lighting31;}
      long hash(int x,int y,int z){return CityHash.of(19327L,x,y,z);}
      BlockState get(int x,int y,int z){
        if(!contains(x,z))throw new AssertionError("cross-chunk read "+key(x,y,z));
        BlockState b=state.get(key(x,y,z)); if(b!=null)return b;
        // Solid floor at y=64, ceiling at y=68, vertical western and eastern support walls.
        if(y==64||y==68||((x==room.x0()||x==room.x1())&&y>=65&&y<=67))return Blocks.STONE.defaultBlockState();
        return Blocks.AIR.defaultBlockState();}
      void set(int x,int y,int z,Block b){set(x,y,z,b.defaultBlockState());}
      void set(int x,int y,int z,BlockState b){if(!contains(x,z))return;state.put(key(x,y,z),b);writes.put(key(x,y,z),b.getBlock().name);}
    }''')
    put('com/dorohedoro/hole/worldgen/city/Pass31FixtureTest.java','''package com.dorohedoro.hole.worldgen.city;
    import java.util.*;
    public class Pass31FixtureTest{
      static void yes(boolean x,String m){if(!x)throw new AssertionError(m);}
      static Map<String,String> generate(Rect r, long seed, boolean large, int size,boolean v18){
        Map<String,String> out=new HashMap<>();
        for(int cx=-128;cx<128;cx+=size)for(int cz=-128;cz<128;cz+=size){
          Canvas c=new Canvas(cx,cz,size,r,v18);
          if(large)ResidentialDetails26.large(c,r,64,0,seed);
          else ResidentialDetails26.compact(c,r,64,0,1,seed);
          out.putAll(c.writes);
        }return out;
      }
      public static void main(String[]args){
        int expected=0, correctlyPlaced=0, skippedAtSeam=0;
        for(int k=0;k<800;k++){
          long seed=CityHash.of(314159L,k);
          int x=-62+(k*31)%104,z=-65+(k*19)%104;
          boolean large=(k%2==0);
          Rect r=new Rect(x,z,x+(large?14:10),z+(large?13:10));
          Map<String,String> whole=generate(r,seed,large,256,true);
          Map<String,String> chunked=generate(r,seed,large,16,true);
          if(k%8==0){
            Map<String,String> oldWhole=generate(r,seed,large,256,false);
            Map<String,String> oldChunked=generate(r,seed,large,16,false);
            yes(oldWhole.equals(oldChunked),"legacy v17 changed across chunks "+k);
            yes(!oldWhole.containsValue("WALL_TORCH"),"legacy v17 fixture behavior changed "+k);
          }
          yes(whole.equals(chunked),"chunk order/seam changed fixture map "+k);
          int v=CityHash.range(CityHash.of(seed^(large?0x2600B2L:0x2600A1L),0,r.centerX(),r.centerZ()),large?5:6);
          boolean planned=large?(v%2==0):true;
          if(planned){
            boolean west=large||(v%2==0);
            int targetX=west?r.x0()+1:r.x1()-1;
            int backX=west?r.x0():r.x1();
            int torchZ=large?r.z0()+4:r.z0()+4;
            boolean seam=Math.floorDiv(backX,16)!=Math.floorDiv(targetX,16);
            String block=chunked.get(Canvas.key(targetX,66,torchZ));
            expected++;
            if(seam){yes(!"WALL_TORCH".equals(block),"cross-chunk unsupported torch "+k);skippedAtSeam++;}
            else{yes("WALL_TORCH".equals(block),"missing supported wall torch "+k+" got "+block);correctlyPlaced++;}
            yes(!"WALL_TORCH".equals(chunked.get(Canvas.key(backX,66,torchZ))),"torch replaced wall "+k);
          }
          yes(!whole.containsValue("WALL_TORCH")||planned,"unplanned torch "+k);
        }
        yes(correctlyPlaced>250,"insufficient supported torches: "+correctlyPlaced);
        yes(skippedAtSeam>10,"seam cases not exercised: "+skippedAtSeam);
        System.out.println("PASS31: 800 varied homes, "+expected+" requested wall torches, "+correctlyPlaced+" correctly supported; "+skippedAtSeam+" deliberately seam-skipped, whole-vs-16-chunk identical; 100 v17 legacy cases unchanged");
      }
    }''')
    java=[str(f) for f in root2.rglob('*.java')]
    build=subprocess.run(['javac','-d',str(root2/'classes'),*java],capture_output=True,text=True)
    if build.returncode:print(build.stderr);sys.exit(build.returncode)
    run=subprocess.run(['java','-cp',str(root2/'classes'),'com.dorohedoro.hole.worldgen.city.Pass31FixtureTest'],capture_output=True,text=True)
    print(run.stdout or run.stderr)
    if run.returncode:sys.exit(run.returncode)
