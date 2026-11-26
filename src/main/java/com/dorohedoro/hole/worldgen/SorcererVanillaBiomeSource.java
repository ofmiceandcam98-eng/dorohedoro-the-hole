package com.dorohedoro.hole.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * The Sorcerers' World for new worlds: vanilla overworld terrain and biomes, by the owner's direction (2026-11-26). It
 * delegates everything to the wrapped vanilla source; the wrapper type exists only so that Sorcerers' World features
 * (settlements) can tell this dimension apart from any vanilla overworld and never generate anywhere else.
 */
public final class SorcererVanillaBiomeSource extends BiomeSource {
    public static final MapCodec<SorcererVanillaBiomeSource> CODEC =
            BiomeSource.CODEC.fieldOf("delegate").xmap(SorcererVanillaBiomeSource::new, s -> s.delegate);

    private final BiomeSource delegate;

    private SorcererVanillaBiomeSource(BiomeSource delegate) {
        this.delegate = delegate;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return delegate.possibleBiomes().stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return delegate.getNoiseBiome(x, y, z, sampler);
    }

    @Override
    public void addDebugInfo(List<String> info, BlockPos pos, Climate.Sampler sampler) {
        delegate.addDebugInfo(info, pos, sampler);
    }
}
