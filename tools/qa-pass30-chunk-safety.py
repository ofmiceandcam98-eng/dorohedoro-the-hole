#!/usr/bin/env python3
"""Pass30 cross-chunk read and resource regression. Uses the exact source method bodies
compiled in a local Java harness with a minimal ChunkAccess stub.
Does not replace NeoForge compilation or in-game worldgen tests.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
import subprocess, json, re, zlib, struct, sys
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
s=(city/'Canvas.java').read_text()

def method(signature):
  start=s.index(signature); i=s.index('{',start); depth=1;j=i+1
  while depth:
    if s[j]=='{':depth+=1
    if s[j]=='}':depth-=1
    j+=1
  return s[start:j]
get=method('public BlockState get(int x, int y, int z)')
offset=method('public int heightOffset(int x, int z)')
contains=method('public boolean contains(int x, int z)')
assert 'if (!contains(x, z)) return Blocks.VOID_AIR.defaultBlockState();' in get
assert 'if (!contains(x, z)) return terrainRelief ? CityElevation.offset(seed, x, z) : 0;' in offset
housing=(city/'Housing25.java').read_text()
assert 'if (c.contains(in.centerX(), in.centerZ())' in housing
assert 'c.contains(bx, bz) && c.get(bx,65,bz)' in (city/'CanalEcology24.java').read_text()
# Ensure generated models/loot are not superseded by earlier manual resource copies.
a=root/'src/main/resources';b=root/'src/generated/resources'
leftover=[]
for p in a.rglob('*'):
 if p.is_file() and (b/p.relative_to(a)).exists(): leftover.append(str(p.relative_to(a)))
assert not leftover, f'Duplicate resource paths: {leftover}'
loot=list((b/'data/dorohedoro/loot_table/blocks').glob('*.json'))
assert len(loot)>=8
for name in ('cavern_lamp','limestone','chitin_plating','galvanized_plating','zinc_ore','sulfur_ore','rock_salt_ore','oil_shale_ore'):
 p=b/f'data/dorohedoro/loot_table/blocks/{name}.json'
 assert p.exists() and json.loads(p.read_text())['type']=='minecraft:block',name

with TemporaryDirectory(prefix='pass30_java_') as tmp:
 p=Path(tmp); package=p/'CityChunkTest.java'
 code='''import java.util.*;
class BlockState { final String kind; BlockState(String v){kind=v;} }
class Block { final String kind; Block(String name){kind=name;} BlockState defaultBlockState(){return new BlockState(kind);} }
class Blocks { static final Block VOID_AIR = new Block("VOID_AIR"); }
class Cursor { Cursor set(int x,int y,int z){return this;} }
class TestChunk { int accesses; BlockState getBlockState(Cursor c){accesses++;return new BlockState("STONE");} }
class CityElevation { static int offset(long seed,int x,int z){return Math.floorMod((int)(seed+x*31L+z*67L),7)-3;} }
class ChunkCanvas {
 TestChunk chunk=new TestChunk(); Cursor cursor=new Cursor();
 long seed;boolean terrainRelief;Integer fixedLotHeight=null;
 int minX,minZ,maxX,maxZ;
 byte[] columnHeights=new byte[256];
 ChunkCanvas(int chunkX,int chunkZ,long seed,boolean relief){
  this.minX=chunkX*16;this.maxX=minX+15;this.minZ=chunkZ*16;this.maxZ=minZ+15;
  this.seed=seed;this.terrainRelief=relief;
  for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)
    columnHeights[(x-minX)+((z-minZ)<<4)]=(byte)(relief?CityElevation.offset(seed,x,z):0);
 }
 int worldY(int x,int y,int z){return y+heightOffset(x,z);}
 ''' + offset+'\n'+contains+'\n'+get+'''\n}
public class CityChunkTest {
 static void yes(boolean b,String message){if(!b)throw new AssertionError(message);}
 public static void main(String[] ignored){
  long readAttempts=0;
  for(int cx=-7;cx<=7;cx++)for(int cz=-7;cz<=7;cz++)for(boolean relief:new boolean[]{true,false}){
   ChunkCanvas c=new ChunkCanvas(cx,cz,cx*117L+cz*31L,relief);
   for(int i=-4;i<20;i++)for(int j=-4;j<20;j++){
    int x=c.minX+i,z=c.minZ+j;
    int expected=relief?CityElevation.offset(c.seed,x,z):0;
    yes(c.heightOffset(x,z)==expected,"offset wrong at "+x+","+z);
    int old=c.chunk.accesses;
    BlockState state=c.get(x,65,z);
    if(c.contains(x,z)){
       yes(c.chunk.accesses==old+1,"local read not performed");
       yes(state.kind.equals("STONE"),"invalid local state");
    } else {
       yes(c.chunk.accesses==old,"outside world read performed");
       yes(state.kind.equals("VOID_AIR"),"outside sentinel is not void air");
    }
    readAttempts++;
   }
   c.fixedLotHeight=2;
   yes(c.heightOffset(c.minX-999,c.minZ-999)==2,"fixed lot height changed");
  }
  System.out.println("PASS30 Java: "+readAttempts+" real-method read attempts across positive/negative chunk seams, deterministic relief and safe outside sentinel");
 }
}'''
 package.write_text(code)
 aout=subprocess.run(['javac','-d',str(p),str(package)],capture_output=True,text=True)
 if aout.returncode: raise AssertionError('Java compile failure: '+aout.stderr)
 bout=subprocess.run(['java','-cp',str(p),'CityChunkTest'],capture_output=True,text=True)
 if bout.returncode:raise AssertionError('Java runtime failure: '+bout.stdout+' '+bout.stderr)
 print(bout.stdout.strip())

jsons=list(root.glob('src/**/resources/**/*.json'))
for f in jsons: json.loads(f.read_text())
pngs=list(root.glob('src/main/resources/**/*.png'))
for f in pngs:
 dat=f.read_bytes()
 assert dat[:8]==b'\x89PNG\r\n\x1a\n' and len(dat)>24
print('PASS30: %d JSON resources, %d packaged main PNGs; 0 overlapping resource paths; 8 canonical block loot tables; housing/canal guards' % (len(jsons),len(pngs)))
