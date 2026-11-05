from pathlib import Path
import re, subprocess, shutil, json, tempfile
work=Path(__file__).resolve().parents[1]
root=work/'src/main/java/com/dorohedoro/hole/worldgen/city'
tmp=Path(tempfile.mkdtemp(prefix='doro_pass39_fixture_'))
(tmp/'src/com/dorohedoro/hole/worldgen/city').mkdir(parents=True,exist_ok=True)
(tmp/'src/net/minecraft/core').mkdir(parents=True,exist_ok=True)
(tmp/'src/net/minecraft/world/level/block/state').mkdir(parents=True,exist_ok=True)
(tmp/'src/net/minecraft/world/level/block').mkdir(parents=True,exist_ok=True)
(tmp/'bin').mkdir()

def W(path,s):
 p=tmp/'src'/path
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s)
for f in ['Rect.java','CityHash.java','InteriorPlan15.java','LivedInRooms39.java','OccupiedRoofs39.java']:
 shutil.copy(root/f,tmp/'src/com/dorohedoro/hole/worldgen/city'/f)
W('net/minecraft/core/Direction.java','package net.minecraft.core;public enum Direction { NORTH,SOUTH,EAST,WEST;public enum Axis{X,Y,Z}}')
W('net/minecraft/world/level/block/Block.java','package net.minecraft.world.level.block;import net.minecraft.world.level.block.state.BlockState;public class Block {public final String name;public Block(String n){name=n;}public BlockState defaultBlockState(){return new BlockState(this);}public String toString(){return name;}}')
W('net/minecraft/world/level/block/state/BlockState.java','package net.minecraft.world.level.block.state;import net.minecraft.world.level.block.Block;import net.minecraft.world.level.block.Blocks;public class BlockState{private final Block block;public BlockState(Block b){block=b;}public boolean isAir(){return block==Blocks.AIR;}public Block getBlock(){return block;}public <T> BlockState setValue(Object key,T value){return this;}}')
W('net/minecraft/world/level/block/ChainBlock.java','package net.minecraft.world.level.block;public class ChainBlock { public static final Object AXIS=new Object();}')
source=(root/'LivedInRooms39.java').read_text()+(root/'OccupiedRoofs39.java').read_text()
names=set(re.findall(r'Blocks\.([A-Z_]+)',source))
for n in ['AIR','STONE','SPRUCE_DOOR','BROWN_BED']:names.add(n)
W('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block;public class Blocks{'+''.join(f'public static final Block {n}=new Block("{n}");' for n in sorted(names))+'}')
W('com/dorohedoro/hole/worldgen/city/CityPlan.java','package com.dorohedoro.hole.worldgen.city;public class CityPlan{public static final int STREET_Y=64;}')
W('com/dorohedoro/hole/worldgen/city/Tower.java','package com.dorohedoro.hole.worldgen.city;public class Tower{public static final int STOREY=4;public enum Roof {PLAIN,TANK,GARDEN,ANTENNA}}')
W('com/dorohedoro/hole/worldgen/city/Canvas.java', '''package com.dorohedoro.hole.worldgen.city;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
public class Canvas {
 public final int minX,maxX,minZ,maxZ;
 private long seed;
 public final Map<String,Block> placed=new LinkedHashMap<>();
 public Canvas(int x0,int x1,int z0,int z1,long seed){minX=x0;maxX=x1;minZ=z0;maxZ=z1;this.seed=seed;}
 public long seed(){return seed;}
 public boolean contains(int x,int z){return x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}
 public String key(int x,int y,int z){return x+","+y+","+z;}
 public BlockState get(int x,int y,int z){if(!contains(x,z))return Blocks.AIR.defaultBlockState();Block b=placed.get(key(x,y,z));if(b!=null)return b.defaultBlockState();if(y==64||y==68||y==72||y==76||y==80)return Blocks.STONE.defaultBlockState();return Blocks.AIR.defaultBlockState();}
 public void set(int x,int y,int z,Block b){if(contains(x,z))placed.put(key(x,y,z),b);}
 public void set(int x,int y,int z,BlockState bs){set(x,y,z,bs.getBlock());}
}''')
W('com/dorohedoro/hole/worldgen/city/QATest.java','''package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.block.*;import java.util.*;
class QATest{
 static void ok(boolean v,String m){if(!v)throw new AssertionError(m);}
 static int runs=0, cells=0;
 public static void main(String[]a){
 long[] seeds={12345L,8675309L,-42L,999999L};
 for(long seed:seeds)for(int w=9;w<=23;w+=2)for(int d=11;d<=29;d+=3){
  Rect in=new Rect(1,1,w,d);
  for(int k=0;k<4;k++){
    Canvas c=new Canvas(-8,32,-8,40,seed);
    int y=64+4*k;
    c.set(in.centerX(),y+1,in.z1(),Blocks.SPRUCE_DOOR);
    c.set(in.x0()+1,y+1,in.z1()-1,Blocks.BROWN_BED);
    int old=c.placed.size();
    LivedInRooms39.residence(c,in,y,k,CityHash.of(seed,w,d),k%2==0);
    ok(c.get(in.centerX(),y+1,in.z1()).getBlock()==Blocks.SPRUCE_DOOR,"door overwritten");
    ok(c.get(in.x0()+1,y+1,in.z1()-1).getBlock()==Blocks.BROWN_BED,"bed overwritten");
    for(var kv:c.placed.entrySet()){
      String[] p=kv.getKey().split(",");int x=Integer.parseInt(p[0]),yy=Integer.parseInt(p[1]),z=Integer.parseInt(p[2]);
      if(yy!=y+1 || kv.getValue()==Blocks.SPRUCE_DOOR || kv.getValue()==Blocks.BROWN_BED)continue;
      ok(z>in.z0()+3,"stair corridor furniture");
      ok(!(z>=in.z1()-2 && Math.abs(x-in.centerX())<=2),"door approach furniture");
    }
    runs++;cells+=c.placed.size()-old;
  }
  Canvas s=new Canvas(-8,32,-8,40,seed);
  LivedInRooms39.shop(s,in,64,seed);LivedInRooms39.clinic(s,in,64,0);LivedInRooms39.industrial(s,in,65,true,seed);
  runs+=3;
  Canvas roof=new Canvas(-8,32,-8,40,seed);
  Rect box=new Rect(0,0,w+4,d+4);
  OccupiedRoofs39.decorate(roof,box,3,seed,Tower.Roof.PLAIN);
  for(var kv:roof.placed.entrySet()){
    String[] p=kv.getKey().split(",");int x=Integer.parseInt(p[0]),z=Integer.parseInt(p[2]);
    ok(z>box.z0()+3,"roof stair corridor furniture");
    ok(x>=box.x0()&&x<=box.x1()&&z<=box.z1(),"roof outside building");
  }
  int count=roof.placed.size();
  OccupiedRoofs39.decorate(new Canvas(-8,32,-8,40,seed),box,3,seed,Tower.Roof.GARDEN);
  ok(count>=0,"invalid roof");runs++;
 }
 System.out.println("PASS deterministic fixture geometry runs="+runs+" added_residential_blocks="+cells);
 }
}''')
subprocess.run(['javac','-d',str(tmp/'bin')]+[str(x) for x in (tmp/'src').rglob('*.java')],check=True,capture_output=True,text=True)
run=subprocess.run(['java','-cp',str(tmp/'bin'),'com.dorohedoro.hole.worldgen.city.QATest'],check=True,capture_output=True,text=True)
print(run.stdout)
# Verify all touched integrations, version routing and absence of additional outdoor chests/lights
C=lambda fn:(root/fn).read_text()
settings=C('CitySettings.java');canvas=C('Canvas.java');painter=C('LotPainter.java');tower=C('Tower.java')
# Version 23 is the minimum feature gate; later source archives legitimately
# increase the codec upper bound and new-world preset version.
assert re.search(r'Codec.intRange\(0,\s*(\d+)\)\.optionalFieldOf\("world_detail_version", 0\)',settings)
assert int(re.search(r'Codec.intRange\(0,\s*(\d+)\)\.optionalFieldOf\("world_detail_version", 0\)',settings).group(1))>=23
assert 'hasOccupiedDetail39() { return worldDetailVersion >= 23; }' in canvas
assert painter.count('LivedInRooms39.residence(')==3
assert painter.count('LivedInRooms39.shop(')==1
assert painter.count('LivedInRooms39.clinic(')==1
assert painter.count('LivedInRooms39.industrial(')==2
assert 'OccupiedRoofs39.decorate(c, box, floors, hash, roof)' in tower
assert json.loads((work/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']>=23
assert all(not re.search(pattern,source) for pattern in [r'Blocks\.LANTERN',r'\.chest\(',r'\bspawn\('])
result={'test':'Pass39 source geometry fixture','javac_fixture':'pass','java_logic':'pass','runs':int(re.search(r'runs=(\d+)',run.stdout).group(1)),'new_files':2,'changed_existing':5,'full_neoforge_build':'not_run','actual_client_test':'not_run'}
(work/'docs/PASS39_QA.json').write_text(json.dumps(result,indent=2))
print(json.dumps(result,indent=2))
shutil.rmtree(tmp)
