package net.minecraft.world.level.biome;
import net.minecraft.world.level.levelgen.DensityFunction;
public final class Climate {
    private Climate() {}
    public record Sampler(DensityFunction humidity, DensityFunction temperature) {}
}
