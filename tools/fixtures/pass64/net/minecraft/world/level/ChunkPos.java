package net.minecraft.world.level;
public record ChunkPos(int x,int z) {public int getMinBlockX() {return x*16;} public int getMinBlockZ() {return z*16;}}
