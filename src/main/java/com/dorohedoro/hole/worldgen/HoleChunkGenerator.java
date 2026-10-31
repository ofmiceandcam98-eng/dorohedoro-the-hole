package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.CityPainter;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.CityPopulator;
import com.dorohedoro.hole.worldgen.city.CitySettings;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * The Overworld generator. Vanilla noise supplies the underground (caves, aquifers, ore veins, bedrock, deepslate), and
 * the city is painted over it at the surface step. Biome decoration and structures still run on top, which is how ores,
 * dungeons, mineshafts and strongholds survive the replacement.
 */
public final class HoleChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<HoleChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings),
            CitySettings.CODEC.optionalFieldOf("city", CitySettings.DEFAULT).forGetter(HoleChunkGenerator::citySettings)
    ).apply(instance, instance.stable(HoleChunkGenerator::new)));

    private final CitySettings citySettings;
    private volatile CityPlan plan;

    public HoleChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, CitySettings citySettings) {
        super(biomeSource, settings);
        this.citySettings = citySettings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public CitySettings citySettings() {
        return citySettings;
    }

    /** The world's plan. Only the seed and the seeded climate go in, so this is safe to build lazily on any thread. */
    public CityPlan plan(long seed, RandomState randomState) {
        CityPlan current = plan;
        if (current == null || current.seed != seed) {
            current = new CityPlan(seed, randomState.sampler(), citySettings);
            plan = current;
        }
        return current;
    }

    public CityPlan plan(ServerLevel level) {
        return plan(level.getSeed(), level.getChunkSource().randomState());
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        super.buildSurface(region, structureManager, random, chunk);
        CityPainter.paint(plan(region.getSeed(), random), chunk);
    }

    /** Carvers run after the surface step and would gouge the city, so all caves come from the noise router. */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        super.spawnOriginalMobs(region);
        CityPopulator.populate(region, plan(region.getSeed(), region.getLevel().getChunkSource().randomState()));
    }
}
