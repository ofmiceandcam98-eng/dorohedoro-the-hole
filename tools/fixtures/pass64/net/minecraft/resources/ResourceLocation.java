package net.minecraft.resources;
public record ResourceLocation(String namespace, String path) {
 public String getNamespace() { return namespace; }
 public String getPath() { return path; }
}
