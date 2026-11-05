package net.minecraft.core;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
public class RegistryAccess {
 public FakeRegistry registryOrThrow(Object o) { return new FakeRegistry(); }
 public static class FakeRegistry {
  public ResourceLocation getKey(Structure s) {
   String[] id=s.name().split(":",2);
   return new ResourceLocation(id[0],id[1]);
  }
 }
}
