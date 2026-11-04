package net.minecraft.world.level.levelgen;
public interface DensityFunction {
    double compute(SinglePointContext point);
    record SinglePointContext(int x, int y, int z) {}
}
