package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/** Additional geology; vanilla underground ore and natural caves are deliberately preserved. */
final class HoleMineralFeatures {
    static final ResourceKey<ConfiguredFeature<?, ?>> SULFUR = configured("sulfur_veins");
    static final ResourceKey<ConfiguredFeature<?, ?>> SALT = configured("rock_salt_veins");
    static final ResourceKey<ConfiguredFeature<?, ?>> LIMESTONE = configured("limestone_pockets");
    static final ResourceKey<ConfiguredFeature<?, ?>> ZINC = configured("zinc_veins");
    static final ResourceKey<ConfiguredFeature<?, ?>> OIL_SHALE = configured("oil_shale_veins");
    static final ResourceKey<PlacedFeature> SULFUR_PLACED = placed("sulfur_veins");
    static final ResourceKey<PlacedFeature> SALT_PLACED = placed("rock_salt_veins");
    static final ResourceKey<PlacedFeature> LIMESTONE_PLACED = placed("limestone_pockets");
    static final ResourceKey<PlacedFeature> ZINC_PLACED = placed("zinc_veins");
    static final ResourceKey<PlacedFeature> OIL_SHALE_PLACED = placed("oil_shale_veins");

    private HoleMineralFeatures() {}

    static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        ctx.register(SULFUR, new ConfiguredFeature<>(Feature.ORE,
                oreBothStoneTypes(ModBlocks.SULFUR_ORE.get().defaultBlockState(), 7)));
        ctx.register(SALT, new ConfiguredFeature<>(Feature.ORE,
                oreBothStoneTypes(ModBlocks.ROCK_SALT_ORE.get().defaultBlockState(), 10)));
        ctx.register(LIMESTONE, new ConfiguredFeature<>(Feature.ORE,
                oreBothStoneTypes(ModBlocks.LIMESTONE.get().defaultBlockState(), 18)));
        ctx.register(ZINC, new ConfiguredFeature<>(Feature.ORE,
                oreBothStoneTypes(ModBlocks.ZINC_ORE.get().defaultBlockState(), 7)));
        ctx.register(OIL_SHALE, new ConfiguredFeature<>(Feature.ORE,
                oreBothStoneTypes(ModBlocks.OIL_SHALE_ORE.get().defaultBlockState(), 9)));
    }

    static void placedFeatures(BootstrapContext<PlacedFeature> ctx) {
        place(ctx, SULFUR_PLACED, SULFUR, 6, -48, 27);
        place(ctx, SALT_PLACED, SALT, 6, -8, 48);
        place(ctx, LIMESTONE_PLACED, LIMESTONE, 8, -12, 54);
        place(ctx, ZINC_PLACED, ZINC, 5, -46, 32);
        place(ctx, OIL_SHALE_PLACED, OIL_SHALE, 5, -24, 38);
    }

    private static OreConfiguration oreBothStoneTypes(net.minecraft.world.level.block.state.BlockState state, int size) {
        return new OreConfiguration(List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), state),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), state)), size);
    }

    private static void place(BootstrapContext<PlacedFeature> ctx, ResourceKey<PlacedFeature> key,
                              ResourceKey<ConfiguredFeature<?, ?>> configured, int count, int minY, int maxY) {
        ctx.register(key, new PlacedFeature(ctx.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(configured),
                List.of(CountPlacement.of(count), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)),
                        BiomeFilter.biome())));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path));
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path));
    }
}
