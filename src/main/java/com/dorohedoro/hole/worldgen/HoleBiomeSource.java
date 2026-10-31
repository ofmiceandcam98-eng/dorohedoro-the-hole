package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.District;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Maps the city's districts to biomes at street level and picks cave biomes underground. Vanilla's lush, dripstone and
 * deep dark caves stay reachable below the city because their resources have no other source.
 */
public final class HoleBiomeSource extends BiomeSource {
    /** Below this height a column stops being city and uses underground biomes. */
    public static final int UNDERGROUND_TOP = 40;
    private static final int DEEP_DARK_TOP = -24;

    public static final MapCodec<HoleBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(HoleBiomes.RESIDENTIAL),
            RegistryOps.retrieveElement(HoleBiomes.MARKET),
            RegistryOps.retrieveElement(HoleBiomes.INDUSTRIAL),
            RegistryOps.retrieveElement(HoleBiomes.WATERFRONT),
            RegistryOps.retrieveElement(HoleBiomes.DERELICT),
            RegistryOps.retrieveElement(HoleBiomes.GRAVEYARD),
            RegistryOps.retrieveElement(HoleBiomes.OUTSKIRTS),
            RegistryOps.retrieveElement(HoleBiomes.UNDERCITY),
            RegistryOps.retrieveElement(Biomes.LUSH_CAVES),
            RegistryOps.retrieveElement(Biomes.DRIPSTONE_CAVES),
            RegistryOps.retrieveElement(Biomes.DEEP_DARK)
    ).apply(instance, instance.stable(HoleBiomeSource::new)));

    private final Holder<Biome> residential;
    private final Holder<Biome> market;
    private final Holder<Biome> industrial;
    private final Holder<Biome> waterfront;
    private final Holder<Biome> derelict;
    private final Holder<Biome> graveyard;
    private final Holder<Biome> outskirts;
    private final Holder<Biome> undercity;
    private final Holder<Biome> lushCaves;
    private final Holder<Biome> dripstoneCaves;
    private final Holder<Biome> deepDark;

    private HoleBiomeSource(Holder<Biome> residential, Holder<Biome> market, Holder<Biome> industrial, Holder<Biome> waterfront,
                            Holder<Biome> derelict, Holder<Biome> graveyard, Holder<Biome> outskirts, Holder<Biome> undercity, Holder<Biome> lushCaves,
                            Holder<Biome> dripstoneCaves, Holder<Biome> deepDark) {
        this.residential = residential;
        this.market = market;
        this.industrial = industrial;
        this.waterfront = waterfront;
        this.derelict = derelict;
        this.graveyard = graveyard;
        this.outskirts = outskirts;
        this.undercity = undercity;
        this.lushCaves = lushCaves;
        this.dripstoneCaves = dripstoneCaves;
        this.deepDark = deepDark;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(residential, market, industrial, waterfront, derelict, graveyard, outskirts, undercity, lushCaves, dripstoneCaves, deepDark);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockY = QuartPos.toBlock(y);
        int blockZ = QuartPos.toBlock(z);
        if (blockY < UNDERGROUND_TOP) {
            return underground(blockX, blockY, blockZ, sampler);
        }
        return switch (District.at(sampler, blockX, blockZ)) {
            case RESIDENTIAL -> residential;
            case MARKET -> market;
            case INDUSTRIAL -> industrial;
            case WATERFRONT -> waterfront;
            case DERELICT -> derelict;
            case GRAVEYARD -> graveyard;
            case OUTSKIRTS -> outskirts;
        };
    }

    private Holder<Biome> underground(int x, int y, int z, Climate.Sampler sampler) {
        // Rotated and offset so cave biomes do not line up with the districts above them.
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(z + 4099, 0, -x + 7919);
        double wet = sampler.humidity().compute(point);
        double warm = sampler.temperature().compute(point);
        if (y < DEEP_DARK_TOP && wet < -0.22 && warm < 0.0) {
            return deepDark;
        }
        if (wet > 0.30) {
            return lushCaves;
        }
        if (wet < -0.30) {
            return dripstoneCaves;
        }
        return undercity;
    }
}
