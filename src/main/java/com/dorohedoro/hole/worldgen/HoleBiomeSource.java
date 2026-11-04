package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.District;
import com.dorohedoro.hole.worldgen.city.DeepDarkBuffer63;
import com.dorohedoro.hole.worldgen.city.ColdDistricts17;
import com.dorohedoro.hole.worldgen.city.HoleRegions42;
import com.dorohedoro.hole.worldgen.city.OceanRegions65;
import com.dorohedoro.hole.worldgen.city.CoastalShelf66;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
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

    /** Bundling five registry-provided holders keeps the outer codec below DFU's 16-field limit. */
    private record FrontierBiomes(Holder<Biome> snow, Holder<Biome> taiga, Holder<Biome> desert,
                                   Holder<Biome> badlands, Holder<Biome> mountains,
                                   Holder<Biome> ocean, Holder<Biome> deepOcean,
                                   Holder<Biome> coldOcean, Holder<Biome> deepColdOcean,
                                   Holder<Biome> beach, Holder<Biome> snowyBeach,
                                   boolean oceanRegions, boolean coastalShelves) {}

    private static final MapCodec<FrontierBiomes> FRONTIER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(HoleBiomes.SNOWFIELDS),
            RegistryOps.retrieveElement(HoleBiomes.DEAD_TAIGA),
            RegistryOps.retrieveElement(HoleBiomes.DUST_BASIN),
            RegistryOps.retrieveElement(HoleBiomes.IRON_BADLANDS),
            RegistryOps.retrieveElement(HoleBiomes.MOUNTAIN_RIM),
            RegistryOps.retrieveElement(Biomes.OCEAN),
            RegistryOps.retrieveElement(Biomes.DEEP_OCEAN),
            RegistryOps.retrieveElement(Biomes.COLD_OCEAN),
            RegistryOps.retrieveElement(Biomes.DEEP_COLD_OCEAN),
            RegistryOps.retrieveElement(Biomes.BEACH),
            RegistryOps.retrieveElement(Biomes.SNOWY_BEACH),
            Codec.BOOL.optionalFieldOf("ocean_regions", false).forGetter(FrontierBiomes::oceanRegions),
            Codec.BOOL.optionalFieldOf("coastal_shelves", false).forGetter(FrontierBiomes::coastalShelves)
    ).apply(instance, FrontierBiomes::new));

    public static final MapCodec<HoleBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(HoleBiomes.RESIDENTIAL),
            RegistryOps.retrieveElement(HoleBiomes.MARKET),
            RegistryOps.retrieveElement(HoleBiomes.INDUSTRIAL),
            RegistryOps.retrieveElement(HoleBiomes.WATERFRONT),
            RegistryOps.retrieveElement(HoleBiomes.DERELICT),
            RegistryOps.retrieveElement(HoleBiomes.GRAVEYARD),
            RegistryOps.retrieveElement(HoleBiomes.OUTSKIRTS),
            RegistryOps.retrieveElement(HoleBiomes.UNDERCITY),
            RegistryOps.retrieveElement(HoleBiomes.FROSTBITTEN_INDUSTRIAL),
            RegistryOps.retrieveElement(Biomes.LUSH_CAVES),
            RegistryOps.retrieveElement(Biomes.DRIPSTONE_CAVES),
            RegistryOps.retrieveElement(Biomes.DEEP_DARK),
            FRONTIER_CODEC.forGetter(src -> src.frontier),
            Codec.BOOL.optionalFieldOf("winter_districts", false).forGetter(src -> src.winterDistricts),
            Codec.BOOL.optionalFieldOf("frontier_biomes", false).forGetter(src -> src.frontierBiomes),
            Codec.BOOL.optionalFieldOf("climate_across_city", false).forGetter(src -> src.climateAcrossCity)
    ).apply(instance, instance.stable(HoleBiomeSource::new)));

    private final Holder<Biome> residential;
    private final Holder<Biome> market;
    private final Holder<Biome> industrial;
    private final Holder<Biome> waterfront;
    private final Holder<Biome> derelict;
    private final Holder<Biome> graveyard;
    private final Holder<Biome> outskirts;
    private final Holder<Biome> undercity;
    private final Holder<Biome> frostbittenIndustrial;
    private final boolean winterDistricts;
    private final boolean frontierBiomes;
    private final boolean climateAcrossCity;
    private final FrontierBiomes frontier;
    private final Holder<Biome> lushCaves;
    private final Holder<Biome> dripstoneCaves;
    private final Holder<Biome> deepDark;

    private HoleBiomeSource(Holder<Biome> residential, Holder<Biome> market, Holder<Biome> industrial, Holder<Biome> waterfront,
                            Holder<Biome> derelict, Holder<Biome> graveyard, Holder<Biome> outskirts, Holder<Biome> undercity, Holder<Biome> frostbittenIndustrial, Holder<Biome> lushCaves,
                            Holder<Biome> dripstoneCaves, Holder<Biome> deepDark, FrontierBiomes frontier, boolean winterDistricts, boolean frontierBiomes, boolean climateAcrossCity) {
        this.residential = residential;
        this.market = market;
        this.industrial = industrial;
        this.waterfront = waterfront;
        this.derelict = derelict;
        this.graveyard = graveyard;
        this.outskirts = outskirts;
        this.undercity = undercity;
        this.frostbittenIndustrial = frostbittenIndustrial;
        this.winterDistricts = winterDistricts;
        this.frontierBiomes = frontierBiomes;
        this.climateAcrossCity = climateAcrossCity;
        this.frontier = frontier;
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
        Stream<Holder<Biome>> legacy = Stream.of(residential, market, industrial, waterfront, derelict,
                graveyard, outskirts, undercity, frostbittenIndustrial, lushCaves, dripstoneCaves, deepDark,
                frontier.snow(), frontier.taiga(), frontier.desert(), frontier.badlands(), frontier.mountains());
        // Old saved generators must not gain ocean structure eligibility incidentally.
        return frontier.oceanRegions()
                ? Stream.concat(legacy, frontier.coastalShelves()
                    ? Stream.of(frontier.ocean(), frontier.deepOcean(), frontier.coldOcean(), frontier.deepColdOcean(),
                                frontier.beach(), frontier.snowyBeach())
                    : Stream.of(frontier.ocean(), frontier.deepOcean(), frontier.coldOcean(), frontier.deepColdOcean()))
                : legacy;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockY = QuartPos.toBlock(y);
        int blockZ = QuartPos.toBlock(z);
        if (blockY < UNDERGROUND_TOP) {
            return underground(blockX, blockY, blockZ, sampler);
        }
        // Oceans use real vanilla biome identities for native structure and creature generation.
        // This flag is absent/false for old saved generators and is climate-seed-dependent.
        if (frontier.oceanRegions()) {
            int depth = OceanRegions65.depthAt(sampler, blockX, blockZ);
            if (depth > 0) {
                boolean cold = sampler.temperature().compute(
                        new DensityFunction.SinglePointContext(blockX, 0, blockZ)) < -0.28;
                return depth >= 16
                        ? (cold ? frontier.deepColdOcean() : frontier.deepOcean())
                        : (cold ? frontier.coldOcean() : frontier.ocean());
            }
            // Beach biomes are not substituted for the whole coast: a dry, narrow
            // natural beach keeps the inland Hole climate, flora and roads intact.
            if (frontier.coastalShelves()) {
                int distance = CoastalShelf66.distanceAt(sampler, blockX, blockZ);
                if (distance > 0 && distance <= CoastalShelf66.BEACH_WIDTH) {
                    boolean cold = sampler.temperature().compute(
                            new DensityFunction.SinglePointContext(blockX, 0, blockZ)) < -0.28;
                    return cold ? frontier.snowyBeach() : frontier.beach();
                }
            }
        }
        if (frontierBiomes) {
            switch (climateAcrossCity ? HoleRegions42.atAnywhere(sampler, blockX, blockZ)
                    : HoleRegions42.at(sampler, blockX, blockZ)) {
                case SNOWFIELDS -> { return frontier.snow(); }
                case DEAD_TAIGA -> { return frontier.taiga(); }
                case DUST_BASIN -> { return frontier.desert(); }
                case IRON_BADLANDS -> { return frontier.badlands(); }
                case MOUNTAIN_RIM -> { return frontier.mountains(); }
                case CITY -> { /* Preserve original inhabited neighborhoods. */ }
            }
        }
        if (winterDistricts && ColdDistricts17.isCold(sampler, blockX, blockZ)) return frostbittenIndustrial;
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

    /**
     * The biome of a column at or above {@link #UNDERGROUND_TOP}. Every height there resolves to the same biome, because
     * the surface branch of {@link #getNoiseBiome} reads only x and z.
     */
    public Holder<Biome> surfaceBiome(int blockX, int blockZ, Climate.Sampler sampler) {
        return getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(UNDERGROUND_TOP), QuartPos.fromBlock(blockZ), sampler);
    }

    /** Whether any biome that {@link #underground} can return passes the test. */
    public boolean anyUndergroundBiome(Predicate<Holder<Biome>> test) {
        return test.test(deepDark) || test.test(lushCaves) || test.test(dripstoneCaves) || test.test(undercity);
    }

    private Holder<Biome> underground(int x, int y, int z, Climate.Sampler sampler) {
        // Rotated and offset so cave biomes do not line up with the districts above them.
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(z + 4099, 0, -x + 7919);
        double wet = sampler.humidity().compute(point);
        double warm = sampler.temperature().compute(point);
        if (y < DEEP_DARK_TOP && DeepDarkBuffer63.matches(wet, warm)) {
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
