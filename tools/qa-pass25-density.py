from pathlib import Path
import subprocess,tempfile,shutil,statistics,json,re
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
with tempfile.TemporaryDirectory(prefix='doro25_density_') as temp:
 t=Path(temp)
 def put(path,text):
  p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text)
 for file in ['Superblock.java','IrregularBlocks14.java','DenseCity25.java','CityHash.java','CityArchitecture.java','LotChooser.java','LotType.java','Lot.java','Line.java','Rect.java']:
  put('com/dorohedoro/hole/worldgen/city/'+file,(city/file).read_text())
 put('net/minecraft/util/RandomSource.java','''package net.minecraft.util; public interface RandomSource {long nextLong(); int nextInt(int n); boolean nextBoolean(); float nextFloat();}''')
 put('net/minecraft/world/level/levelgen/XoroshiroRandomSource.java','''package net.minecraft.world.level.levelgen; import net.minecraft.util.RandomSource; import java.util.Random; public class XoroshiroRandomSource implements RandomSource {private Random rand; public XoroshiroRandomSource(long seed){rand=new Random(seed);} public long nextLong(){return rand.nextLong();} public int nextInt(int n){return rand.nextInt(n);} public boolean nextBoolean(){return rand.nextBoolean();} public float nextFloat(){return rand.nextFloat();}}''')
 put('com/dorohedoro/hole/worldgen/city/District.java','''package com.dorohedoro.hole.worldgen.city; enum District { RESIDENTIAL,MARKET,INDUSTRIAL,WATERFRONT,DERELICT,GRAVEYARD,OUTSKIRTS}''')
 put('com/dorohedoro/hole/worldgen/city/DiagonalBoulevards14.java','''package com.dorohedoro.hole.worldgen.city; final class DiagonalBoulevards14 {static boolean intersects(long seed,Rect r){return false;}}''')
 put('com/dorohedoro/hole/worldgen/city/CityPlan.java','''package com.dorohedoro.hole.worldgen.city;
class CityPlan {
 final long seed; final CitySettings settings; final District district;
 static final int LANDMARK_REGION=8;
 record LandmarkSite(int blockI,int blockJ,LotType type) {}
 CityPlan(long seed,int version,District district,float density){this.seed=seed;this.settings=new CitySettings(version,density);this.district=district;}
 Line xLine(int i){return new Line(i,i*144+72,4,Line.Kind.AVENUE);}
 Line zLine(int i){return new Line(i,i*104+52,4,Line.Kind.AVENUE);}
 long blockHash(int i,int j){return CityHash.of(seed,0x7A11E5L,i,j);}
 District districtAt(int x,int z){return district;}
 LandmarkSite landmarkSite(int a,int b){return new LandmarkSite(-999,-999,LotType.HOSPITAL);}
}
record CitySettings(int worldDetailVersion,float density) {}
''')
 put('com/dorohedoro/hole/worldgen/city/DensityTest.java','''package com.dorohedoro.hole.worldgen.city;
import java.util.*;
class DensityTest {
 static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
 static boolean intersection(Rect a,Rect b){return a.intersects(b);}
 public static void main(String[] a){
   long[][] metrics=new long[7][4];int checks=0;int changed=0;int total=0;
   for(District d:District.values()) for(int q=0;q<50;q++){
    long seed=q*8831L+23;int i=q%9+3,j=q%7+2;
    Superblock old=Superblock.build(new CityPlan(seed,12,d,0.92F),i,j);
    Superblock next=Superblock.build(new CityPlan(seed,13,d,0.97F),i,j);
    Superblock again=Superblock.build(new CityPlan(seed,13,d,0.97F),i,j);
    require(next.equals(again),"non-deterministic superblock " + d+"/"+seed);checks++;
    require(next.lots().size()>0,"empty block");checks++;
    for(int k=0;k<next.lots().size();k++){
      Lot lot=next.lots().get(k); Rect r=lot.rect();
      require(r.width()>=8 && r.depth()>=8,"unusable plot "+r);checks++;
      require(r.x0()>=next.area().x0()&&r.x1()<=next.area().x1()&&r.z0()>=next.area().z0()&&r.z1()<=next.area().z1(),"out of bound");checks++;
      for(int h=k+1;h<next.lots().size();h++){require(!r.intersects(next.lots().get(h).rect()),"lot overlap");checks++;}
    }
    int d0=d.ordinal();metrics[d0][0]+=old.lots().size(); metrics[d0][1]+=next.lots().size();
    metrics[d0][2]+=old.lots().stream().filter(l->l.type().enclosed).count();
    metrics[d0][3]+=next.lots().stream().filter(l->l.type().enclosed).count();
    if(!old.lots().equals(next.lots()))changed++;
    total++;
   }
   for(District d:District.values()){
      int v=d.ordinal(); System.out.printf("%s oldLots=%d newLots=%d oldEnclosed=%d newEnclosed=%d\\n",d,metrics[v][0],metrics[v][1],metrics[v][2],metrics[v][3]);
      if(DenseCity25.isDenseDistrict(d)){
        require(metrics[v][1] >= metrics[v][0]*1.15,"not enough extra lots for "+d);
        require(metrics[v][3]>metrics[v][2],"not enough extra buildings for "+d);
      } else {
        require(metrics[v][1]==metrics[v][0],"preserved open district lot count changed "+d);
      }
   }
   // The starter quarter retains its named landmarks and plaza.
   Superblock start=Superblock.build(new CityPlan(12345L,13,District.RESIDENTIAL,0.97F),-1,-1);
   for(LotType type:List.of(LotType.PLAZA,LotType.HUNGRY_BUG,LotType.NURSERY,LotType.STARTER_SHELTER,LotType.MARKET_STALLS)){
      require(start.lots().stream().anyMatch(l->l.type()==type),"missing starter landmark: "+type);
   }
   // World profile 14 keeps small residential plots as actual compact housing.
   long tiny=0, tinyHomes=0, tallLots=0, mediumLots=0;
   for(int q=0;q<100;q++){
     Superblock h=Superblock.build(new CityPlan(q*981L+11,14,District.RESIDENTIAL,0.97F),q%7+3,q%9+2);
     Superblock again=Superblock.build(new CityPlan(q*981L+11,14,District.RESIDENTIAL,0.97F),q%7+3,q%9+2);
     require(h.equals(again),"version14 lot mismatch");
     for(Lot lot:h.lots()) {
       Rect r=lot.rect();
       if(r.width()<=15 && r.depth()<=16) {tiny++;if(lot.type()==LotType.TENEMENT)tinyHomes++;}
       else if(r.width()>=20 && r.depth()>=20)tallLots++;
       else mediumLots++;
     }
   }
   require(tiny>=100,"too few small lots");
   require(tinyHomes>tiny/3,"small lots not dominated by housing");
   System.out.printf("PASS25 HOMES: small lots %d, small residential home lots %d, medium lots %d, large lots %d\\n",tiny,tinyHomes,mediumLots,tallLots);
   // Original version 12 remains byte-stable and pass13 is seed sensitive.
   require(!Superblock.build(new CityPlan(12345,13,District.RESIDENTIAL,.97F),4,4).equals(Superblock.build(new CityPlan(54321,13,District.RESIDENTIAL,.97F),4,4)),"seeds don't change city");
   System.out.printf("PASS25 DENSITY: %d checks, %d changed blocks across %d trials, deterministic/no lot overlap; town plaza preserved\\n",checks,changed,total);
 }
}''')
 command=['javac','-d',str(t/'classes'),*map(str,t.rglob('*.java'))]
 p=subprocess.run(command,text=True,capture_output=True)
 print('JAVAC',p.returncode,p.stderr[:3000]);assert p.returncode==0
 p=subprocess.run(['java','-cp',str(t/'classes'),'com.dorohedoro.hole.worldgen.city.DensityTest'],text=True,capture_output=True)
 print(p.stdout, p.stderr[:3000]);assert p.returncode==0
