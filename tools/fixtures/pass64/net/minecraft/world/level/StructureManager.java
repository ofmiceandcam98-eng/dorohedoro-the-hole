package net.minecraft.world.level;
import java.util.List;import java.util.function.Predicate;import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
public class StructureManager {
 private final boolean enabled;private final List<StructureStart> starts;
 public StructureManager(boolean enabled,List<StructureStart> starts) {this.enabled=enabled;this.starts=starts;}
 public boolean shouldGenerateStructures() {return enabled;}
 public RegistryAccess registryAccess(){return new RegistryAccess();}
 public List<StructureStart> startsForStructure(ChunkPos chunk,Predicate<Structure> predicate) {
   List<StructureStart> result=new ArrayList<>();
   for(var s:starts) if(predicate.test(s.getStructure())) result.add(s);
   return result;
 }
}
