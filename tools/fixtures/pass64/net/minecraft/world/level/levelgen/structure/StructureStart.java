package net.minecraft.world.level.levelgen.structure;
public record StructureStart(Structure structure,BoundingBox boundingBox, boolean valid) {
 public boolean isValid() { return valid; }
 public BoundingBox getBoundingBox() { return boundingBox; }
 public Structure getStructure() { return structure; }
}
