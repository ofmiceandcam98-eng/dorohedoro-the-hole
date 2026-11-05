package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

/**
 * Biomes and ores of the Sorcerers' World and Hell. No Sorcerers' World biome has precipitation: weather is shared
 * between dimensions in vanilla, so the biome flag is the only thing that keeps that world dry during a Hole storm.
 */
final class RealmBiomeData {
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_COAL_C = configured("hell_ore_coal");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_IRON_C = configured("hell_ore_iron");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_COPPER_C = configured("hell_ore_copper");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_GOLD_C = configured("hell_ore_gold");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_REDSTONE_C = configured("hell_ore_redstone");
    private static final ResourceKey<ConfiguredFeature<?, ?>> HELL_DIAMOND_C = configured("hell_ore_diamond");

    private static final ResourceKey<ConfiguredFeature<?, ?>> FROST_PATCH_CONFIGURED = configured("sw_frost_snow_patch");
    private static final ResourceKey<PlacedFeature> FROST_PATCH_PLACED = placed("sw_frost_snow_patch");

    private static final ResourceKey<PlacedFeature> HELL_COAL = placed("hell_ore_coal");
    private static final ResourceKey<PlacedFeature> HELL_IRON = placed("hell_ore_iron");
    private static final ResourceKey<PlacedFeature> HELL_COPPER = placed("hell_ore_copper");
    private static final ResourceKey<PlacedFeature> HELL_GOLD = placed("hell_ore_gold");
    private static final ResourceKey<PlacedFeature> HELL_REDSTONE = placed("hell_ore_redstone");
    private static final ResourceKey<PlacedFeature> HELL_DIAMOND = placed("hell_ore_diamond");

    private RealmBiomeData() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }

    /** Vanilla ores do not replace hell rock, so Hell declares its own veins. Mining stays worthwhile in every realm. */
    static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, FROST_PATCH_CONFIGURED, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SNOW)));
        ore(context, HELL_COAL_C, Blocks.DEEPSLATE_COAL_ORE, 14);
        ore(context, HELL_IRON_C, Blocks.DEEPSLATE_IRON_ORE, 9);
        ore(context, HELL_COPPER_C, Blocks.DEEPSLATE_COPPER_ORE, 10);
        ore(context, HELL_GOLD_C, Blocks.DEEPSLATE_GOLD_ORE, 9);
        ore(context, HELL_REDSTONE_C, Blocks.DEEPSLATE_REDSTONE_ORE, 8);
        ore(context, HELL_DIAMOND_C, Blocks.DEEPSLATE_DIAMOND_ORE, 7);
    }

    private static void ore(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, Block ore, int size) {
        FeatureUtils.register(context, key, Feature.ORE, new OreConfiguration(
                List.of(OreConfiguration.target(new BlockMatchTest(ModBlocks.HELL_ROCK.get()), ore.defaultBlockState())), size));
    }

    static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        vein(context, features, HELL_COAL, HELL_COAL_C, 24, -40, 200);
        vein(context, features, HELL_IRON, HELL_IRON_C, 16, -50, 120);
        vein(context, features, HELL_COPPER, HELL_COPPER_C, 12, -40, 120);
        vein(context, features, HELL_GOLD, HELL_GOLD_C, 8, -60, 70);
        vein(context, features, HELL_REDSTONE, HELL_REDSTONE_C, 8, -60, 40);
        vein(context, features, HELL_DIAMOND, HELL_DIAMOND_C, 5, -60, 24);
        // Ground frost is generated as a feature; Sorcerer sky remains precipitation-free.
        PlacementUtils.register(context, FROST_PATCH_PLACED, features.getOrThrow(FROST_PATCH_CONFIGURED), List.of(
                CountPlacement.of(45), InSquarePlacement.spread(),
                HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), BiomeFilter.biome()));
    }

    private static void vein(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> features,
                             ResourceKey<PlacedFeature> key, ResourceKey<ConfiguredFeature<?, ?>> configured, int count, int minY, int maxY) {
        PlacementUtils.register(context, key, features.getOrThrow(configured), List.of(
                CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)), BiomeFilter.biome()));
    }

    static void biomes(BootstrapContext<Biome> context) {
        sorcerers(context);
        hell(context);
    }

    private record Air(int fog, int sky, int water, int waterFog, int grass, int foliage) {}

    private static final Air OPEN_SKY = new Air(0xB9C6C4, 0x8FB4C4, 0x3F76E4, 0x050533, 0x7DA24E, 0x5F8F3E);
    private static final Air CITY_AIR = new Air(0xA9A7A0, 0x8D9CA6, 0x3F6D9E, 0x050533, 0x76924E, 0x5C7E3C);
    private static final Air DEEP_WOOD = new Air(0x7D9184, 0x6E8E86, 0x3D6A6E, 0x06242A, 0x3E6B38, 0x2F5A2D);
    private static final Air FROST_AIR = new Air(0xAFB9BF, 0xB8C9D7, 0x577A91, 0x0B1830, 0xABB6AA, 0x8F9F92);
    private static final Air BLIGHT_AIR = new Air(0x9C8E8E, 0x8A7F86, 0x5A5A6E, 0x1B1420, 0x7A6E56, 0x6C5F4C);

    private static void sorcerers(BootstrapContext<Biome> context) {
        context.register(RealmBiomes.SW_CITY, sorcererBiome(CITY_AIR, sorcererSpawns(false), city(context)));
        context.register(RealmBiomes.SW_CITY_EDGE, sorcererBiome(CITY_AIR, sorcererSpawns(true), countryside(context)));
        context.register(RealmBiomes.SW_FARMLAND, sorcererBiome(OPEN_SKY, sorcererSpawns(true), countryside(context)));
        context.register(RealmBiomes.SW_WOODLAND, sorcererBiome(OPEN_SKY, sorcererSpawns(true), woodland(context)));
        context.register(RealmBiomes.SW_HYDRA_FOREST, sorcererBiome(DEEP_WOOD, sorcererSpawns(true), hydraForest(context)));
        // High-altitude dry frost. The Sorcerers' World canonically has no ordinary precipitation.
        context.register(RealmBiomes.SW_FROSTBOUND_HIGHLANDS,
                sorcererBiome(FROST_AIR, sorcererSpawns(false), frostHighlands(context), -0.4F));
        context.register(RealmBiomes.SW_BLIGHT, sorcererBiome(BLIGHT_AIR, sorcererSpawns(false), blight(context)));

        MobSpawnSettings.Builder waterLife = new MobSpawnSettings.Builder();
        waterLife.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 4, 1, 2));
        waterLife.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 12, 3, 5));
        waterLife.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 8, 2, 4));
        context.register(RealmBiomes.SW_WATERS, sorcererBiome(OPEN_SKY, waterLife, waters(context)));

        // Only the deep underground keeps vanilla's hostile cave life. Its drops stay reachable for anyone living here.
        MobSpawnSettings.Builder caveLife = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.caveSpawns(caveLife);
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 60, 2, 3));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 60, 1, 3));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 20, 1, 2));
        caveLife.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 40, 2, 3));
        context.register(RealmBiomes.SW_CAVES, sorcererBiome(OPEN_SKY, caveLife, countryside(context)));
    }

    private static MobSpawnSettings.Builder sorcererSpawns(boolean farmAnimals) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        if (farmAnimals) {
            BiomeDefaultFeatures.farmAnimals(spawns);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 4, 2, 4));
        }
        return spawns;
    }

    private static Biome sorcererBiome(Air air, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation) {
        return sorcererBiome(air, spawns, generation, 0.8F);
    }

    private static Biome sorcererBiome(Air air, MobSpawnSettings.Builder spawns, BiomeGenerationSettings.Builder generation, float temperature) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(temperature)
                .downfall(0.4F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(air.fog).skyColor(air.sky).waterColor(air.water).waterFogColor(air.waterFog)
                        .grassColorOverride(air.grass).foliageColorOverride(air.foliage)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    /** The vanilla feature order every Sorcerers' World biome shares, which is what keeps the feature sorter happy. */
    private static BiomeGenerationSettings.Builder base(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        return generation;
    }

    private static void minerals(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
    }

    private static BiomeGenerationSettings.Builder city(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder countryside(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder woodland(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        BiomeDefaultFeatures.addOtherBirchTrees(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder hydraForest(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
        minerals(generation);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder frostHighlands(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FROST_PATCH_PLACED);
        return generation;
    }

    private static BiomeGenerationSettings.Builder blight(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        return generation;
    }

    private static BiomeGenerationSettings.Builder waters(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = base(context);
        minerals(generation);
        return generation;
    }

    private static final Air ASH = new Air(0x3A3532, 0x1C1816, 0x2E2A2A, 0x0E0C0C, 0x4A4438, 0x3E382E);
    private static final Air HORN_AIR = new Air(0x4A2E2A, 0x241210, 0x2E2A2A, 0x0E0C0C, 0x4A4438, 0x3E382E);
    private static final Air SOUL_AIR = new Air(0x2E3A40, 0x141C20, 0x2E3A40, 0x0E1416, 0x3A4A48, 0x2E3E3C);
    private static final Air BLACK_AIR = new Air(0x161413, 0x0C0A0A, 0x1E1C1C, 0x080707, 0x2E2A26, 0x26221E);

    private static void hell(BootstrapContext<Biome> context) {
        context.register(RealmBiomes.HELL_WASTES, hellBiome(ASH, 0.004F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_HORNS, hellBiome(HORN_AIR, 0.002F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_SOUL_FLATS, hellBiome(SOUL_AIR, 0.006F, hellGeneration(context)));
        context.register(RealmBiomes.HELL_CAVERNS, hellBiome(BLACK_AIR, 0.0F, hellGeneration(context)));
    }

    private static Biome hellBiome(Air air, float ash, BiomeGenerationSettings.Builder generation) {
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(air.fog).skyColor(air.sky).waterColor(air.water).waterFogColor(air.waterFog)
                .grassColorOverride(air.grass).foliageColorOverride(air.foliage)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        if (ash > 0.0F) {
            effects.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, ash));
        }
        // Devils replace this empty table in Phase 7. Reskinned vanilla undead are ruled out by the spec.
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(1.2F)
                .downfall(0.0F)
                .specialEffects(effects.build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(generation.build())
                .build();
    }

    private static BiomeGenerationSettings.Builder hellGeneration(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        for (ResourceKey<PlacedFeature> vein : List.of(HELL_COAL, HELL_IRON, HELL_COPPER, HELL_GOLD, HELL_REDSTONE, HELL_DIAMOND)) {
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, vein);
        }
        generation.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
        return generation;
    }
}
