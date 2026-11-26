package com.dorohedoro.hole.worldgen;

import com.mojang.serialization.Codec;
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

/**
 * Biomes of the Sorcerers' World. City land is read back from the terrain's basin noise, routed through the climate
 * depth slot, so city biomes always sit on the flattened ground the noise settings carve out for them.
 */
public final class SorcererBiomeSource extends BiomeSource {
    private static final int UNDERGROUND_TOP = 40;
    private static final double CITY = 0.42;
    private static final double CITY_EDGE = 0.32;

    public static final MapCodec<SorcererBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(RealmBiomes.SW_CITY),
            RegistryOps.retrieveElement(RealmBiomes.SW_CITY_EDGE),
            RegistryOps.retrieveElement(RealmBiomes.SW_FARMLAND),
            RegistryOps.retrieveElement(RealmBiomes.SW_WOODLAND),
            RegistryOps.retrieveElement(RealmBiomes.SW_HYDRA_FOREST),
            RegistryOps.retrieveElement(RealmBiomes.SW_BLIGHT),
            RegistryOps.retrieveElement(RealmBiomes.SW_WATERS),
            RegistryOps.retrieveElement(RealmBiomes.SW_CAVES),
            RegistryOps.retrieveElement(RealmBiomes.SW_FROSTBOUND_HIGHLANDS),
            Codec.BOOL.optionalFieldOf("frostbound_regions", false).forGetter(src -> src.frostboundRegions)
    ).apply(instance, instance.stable(SorcererBiomeSource::new)));

    private final Holder<Biome> city;
    private final Holder<Biome> cityEdge;
    private final Holder<Biome> farmland;
    private final Holder<Biome> woodland;
    private final Holder<Biome> hydraForest;
    private final Holder<Biome> blight;
    private final Holder<Biome> waters;
    private final Holder<Biome> caves;
    private final Holder<Biome> frostboundHighlands;
    private final boolean frostboundRegions;

    private SorcererBiomeSource(Holder<Biome> city, Holder<Biome> cityEdge, Holder<Biome> farmland, Holder<Biome> woodland,
                                Holder<Biome> hydraForest, Holder<Biome> blight, Holder<Biome> waters, Holder<Biome> caves,
                                Holder<Biome> frostboundHighlands, boolean frostboundRegions) {
        this.city = city;
        this.cityEdge = cityEdge;
        this.farmland = farmland;
        this.woodland = woodland;
        this.hydraForest = hydraForest;
        this.blight = blight;
        this.waters = waters;
        this.caves = caves;
        this.frostboundHighlands = frostboundHighlands;
        this.frostboundRegions = frostboundRegions;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(city, cityEdge, farmland, woodland, hydraForest, blight, waters, caves, frostboundHighlands);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);
        if (QuartPos.toBlock(y) < UNDERGROUND_TOP) {
            return caves;
        }
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(blockX, 0, blockZ);
        double basin = sampler.depth().compute(point);
        if (basin > CITY) {
            return city;
        }
        if (basin > CITY_EDGE) {
            return cityEdge;
        }
        double continents = sampler.continentalness().compute(point);
        double weirdness = sampler.weirdness().compute(point);
        if (continents < -0.19 || (continents < 0.25 && Math.abs(weirdness) < 0.035)) {
            return waters;
        }
        double warmth = sampler.temperature().compute(point);
        double moisture = sampler.humidity().compute(point);
        // Remote, dry highlands. Dry frost is a gameplay adaptation; it does not rain or snow here.
        if (frostboundRegions && continents > 0.30 && warmth < -0.32 && weirdness > 0.22) return frostboundHighlands;
        if (warmth > 0.34 && moisture < -0.12) {
            return blight;
        }
        if (moisture > 0.16) {
            return hydraForest;
        }
        return moisture > -0.14 ? woodland : farmland;
    }
}
