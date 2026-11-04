import java.util.*;
import com.dorohedoro.hole.worldgen.city.VanillaStructureBuffer64;
import com.dorohedoro.hole.worldgen.city.VanillaStructureLookup64;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class VanillaStructureFixture64 {
 static int checks;
 static void check(boolean value,String what) {checks++;if(!value)throw new IllegalStateException(what);}
 static StructureStart start(String id, int minX,int minY,int minZ,int maxX,int maxY,int maxZ,boolean valid) {
  return new StructureStart(new Structure(id),new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ),valid);
 }
 static void exactBoundary() {
  var b=new VanillaStructureBuffer64.Box(-2,-38,6,21,-22,24);
  check(b.near(-6,-44,6),"horizontal+vertical boundary");
  check(b.near(25,-16,28),"max outer boundary");
  check(!b.near(26,-16,28),"beyond max x");
  check(!b.near(-7,-44,6),"beyond min x");
  check(!b.near(-2,-45,6),"beyond min y");
  check(!b.near(-2,-15,6),"beyond max y");
  check(b.nearChunk(16,16),"chunk collision");
  check(!b.nearChunk(32,32),"far chunk not buffered");
  check(!VanillaStructureBuffer64.protects(45,0,-32,10,List.of(b)),"legacy unmodified");
  check(VanillaStructureBuffer64.protects(46,0,-32,10,List.of(b)),"fresh world protected");
  check(!VanillaStructureBuffer64.protects(46,0,65,10,List.of(b)),"overlying streets remain");
  check(!VanillaStructureBuffer64.protects(46,0,-32,10,List.of()),"empty references remain editable");
 }
 static void recognized() {
  var starts=new ArrayList<StructureStart>();
  String[] names={"minecraft:stronghold","minecraft:mineshaft","minecraft:mineshaft_mesa","minecraft:trial_chambers","minecraft:ancient_city"};
  for(String name:names) starts.add(start(name,-2,-45,-1,18,-20,17,true));
  starts.add(start("minecraft:desert_pyramid",0,45,0,8,80,8,true));
  starts.add(start("minecraft:village_plains",0,55,0,8,82,8,true));
  starts.add(start("examplemod:stronghold",0,-45,0,8,-20,8,true));
  starts.add(start("minecraft:stronghold",0,-45,0,8,-20,8,false));
  starts.add(start("minecraft:trial_chambers",512,-45,512,520,-20,520,true));
  List<VanillaStructureBuffer64.Box> found=VanillaStructureLookup64.forChunk(new StructureManager(true,starts),new ChunkPos(0,0));
  check(found.size()==5,"only 5 valid, relevant structures protected, got "+found.size());
  for(var box:found) check(VanillaStructureBuffer64.protects(46,3,-30,4,List.of(box)),"all critical starts protected");
  check(VanillaStructureLookup64.forChunk(new StructureManager(false,starts),new ChunkPos(0,0)).isEmpty(),"disabled structure generation");
  check(VanillaStructureLookup64.forChunk(new StructureManager(true,starts),new ChunkPos(32,32)).size()==1,"distant start filtered by local box");
 }
 static void exhaustive() {
  Random random=new Random(0x64323A);
  for(int i=0;i<55000;i++) {
   int bx=random.nextInt(5000)-2500, bz=random.nextInt(5000)-2500,by=random.nextInt(60)-60;
   int w=1+random.nextInt(200), d=1+random.nextInt(200), h=1+random.nextInt(75);
   var box=new VanillaStructureBuffer64.Box(bx,by,bz,bx+w,by+h,bz+d);
   int x=bx+random.nextInt(w+9)-4, y=by+random.nextInt(h+13)-6,z=bz+random.nextInt(d+9)-4;
   check(box.near(x,y,z),"inside true margin "+i);
   check(!box.near(bx+w+5,y,z),"outside horizontal margin "+i);
   check(!box.near(x,by+h+7,z),"outside vertical margin "+i);
   check(!VanillaStructureBuffer64.protects(45,x,y,z,List.of(box)),"legacy preserved "+i);
  }
  for(int i=0;i<300;i++) {
   int mx=i-150;
   var b=new VanillaStructureBuffer64.Box(mx,-47,mx,mx+2,-31,mx+2);
   check(b.near(mx,-47,mx),"negative coordinates "+i);
  }
 }
 public static void main(String[]args){exactBoundary();recognized();exhaustive();System.out.println("PASS Pass64: "+checks+" geometry/registry/version checks (55,000 seeded randomized cases)");}
}
