from pathlib import Path
import re,subprocess,tempfile, shutil, atexit
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
tmp=Path(__file__).resolve().parents[1]/'tools/.tmp_pass24_stub_compile';shutil.rmtree(tmp,ignore_errors=True);tmp.mkdir()
# Avoid shipping fake Minecraft classes / compiled test artifacts in release ZIPs.
atexit.register(lambda: shutil.rmtree(tmp, ignore_errors=True))
def write(rel,text):
 p=tmp/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text)
for filename in ('RiverChannels24.java','CityHash.java','Line.java'):
 write('com/dorohedoro/hole/worldgen/city/'+filename,(city/filename).read_text())
river=(city/'RiverChannels24.java').read_text()
blocks=sorted(set(re.findall(r'Blocks\.([A-Z_]+)',river)))
write('net/minecraft/world/level/block/Block.java','package net.minecraft.world.level.block; public class Block {}')
write('net/minecraft/world/level/block/Blocks.java','package net.minecraft.world.level.block; public class Blocks {\n'+'\n'.join('public static final Block '+b+'=new Block();' for b in blocks)+'\n}')
write('com/dorohedoro/hole/worldgen/city/Canvas.java','''package com.dorohedoro.hole.worldgen.city;
import net.minecraft.world.level.block.Block;
class Canvas {int minX,maxX,minZ,maxZ; void set(int x,int y,int z,Block b) {} void fill(int x,int y,int z,int x2,int y2,int z2,Block b) {}}''')
write('com/dorohedoro/hole/worldgen/city/CityPlan.java','''package com.dorohedoro.hole.worldgen.city;
class CityPlan {long seed; District districtAt(int x,int z) {return District.RESIDENTIAL;}}''')
write('com/dorohedoro/hole/worldgen/city/District.java','''package com.dorohedoro.hole.worldgen.city;
enum District {RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT}''')
write('com/dorohedoro/hole/worldgen/city/RiverTest.java','''package com.dorohedoro.hole.worldgen.city;
public final class RiverTest {
 public static void main(String[] args) {
   int all=0; int varied=0; int minWidth=99,maxWidth=0;
   for(long seed=-15;seed<=15;seed++) for(boolean alongZ:new boolean[]{true,false}) {
     Line l=new Line((int)seed,24,9,Line.Kind.CANAL);
     int min=99,max=0; int prevOffset=0,prevWidth=0;
     for(int along=-2048;along<=2048;along++) {
       int w=RiverChannels24.width(seed,l,alongZ,along);
       int o=RiverChannels24.centerOffset(seed,l,alongZ,along);
       if (w<3 || w>7 || Math.abs(o)>3 || w+Math.abs(o)>7) throw new IllegalStateException("breached lot bank at "+along);
       if (along>-2048 && (Math.abs(o-prevOffset)>1 || Math.abs(w-prevWidth)>1)) throw new IllegalStateException("discontinuous shoreline at "+along);
       min=Math.min(min,w);max=Math.max(max,w);prevOffset=o;prevWidth=w;
     }
     all++;if(max>min)varied++;minWidth=Math.min(minWidth,min);maxWidth=Math.max(maxWidth,max);
   }
   System.out.printf("profiles=%d varied=%d min_diameter=%d max_diameter=%d\\n",all,varied,minWidth*2+1,maxWidth*2+1);
   if(varied!=all || maxWidth<=minWidth) throw new IllegalStateException("width variation not visible");
   Line line=new Line(4,24,9,Line.Kind.CANAL);
   for(int i=-320;i<=320;i++){int off=RiverChannels24.centerOffset(928373L,line,true,i); int w=RiverChannels24.width(928373L,line,true,i);System.out.printf("%d,%d,%d\\n",i,24+off,w);}
 }
}''')
p=subprocess.run(['javac','-d',str(tmp/'classes'),*map(str,tmp.rglob('*.java'))],capture_output=True,text=True)
print('STUB COMPILE:',p.returncode, p.stderr[:2500]);assert p.returncode==0
q=subprocess.run(['java','-cp',str(tmp/'classes'),'com.dorohedoro.hole.worldgen.city.RiverTest'],capture_output=True,text=True)
print('GEOMETRY TEST:',q.returncode,q.stderr[:1000]);assert q.returncode==0
lines=q.stdout.splitlines();print(lines[0]);(r/'docs/PASS24_RIVER_GEOMETRY_SAMPLES.csv').write_text('along,centerX,halfWidth\n'+'\n'.join(lines[1:])+'\n')
