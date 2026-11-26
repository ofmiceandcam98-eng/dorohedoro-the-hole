package com.dorohedoro.hole.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Biomes of Hell. The horn regions match the terrain's region noise, routed through the climate depth slot. */
public final class HellBiomeSource extends BiomeSource {
    private static final int UNDERGROUND_TOP = 54;

    public static final MapCodec<HellBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(RealmBiomes.HELL_WASTES),
            RegistryOps.retrieveElement(RealmBiomes.HELL_HORNS),
            RegistryOps.retrieveElement(RealmBiomes.HELL_SOUL_FLATS),
            RegistryOps.retrieveElement(RealmBiomes.HELL_CAVERNS)
    ).apply(instance, instance.stable(HellBiomeSource::new)));

    private final Holder<Biome> wastes;
    private final Holder<Biome> horns;
    private final Holder<Biome> soulFlats;
    private final Holder<Biome> caverns;

    private HellBiomeSource(Holder<Biome> wastes, Holder<Biome> horns, Holder<Biome> soulFlats, Holder<Biome> caverns) {
        this.wastes = wastes;
        this.horns = horns;
        this.soulFlats = soulFlats;
        this.caverns = caverns;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(wastes, horns, soulFlats, caverns);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (QuartPos.toBlock(y) < UNDERGROUND_TOP) {
            return caverns;
        }
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(QuartPos.toBlock(x), 0, QuartPos.toBlock(z));
        double regions = sampler.depth().compute(point);
        if (regions > 0.0) {
            return horns;
        }
        return sampler.humidity().compute(point) > 0.12 ? soulFlats : wastes;
    }
}
