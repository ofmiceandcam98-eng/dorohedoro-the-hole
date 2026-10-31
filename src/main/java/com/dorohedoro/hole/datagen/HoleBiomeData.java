package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.worldgen.HoleBiomes;
import java.util.function.Consumer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** The Hole's biomes. They differ in air, water and what hunts there, and all carry the full vanilla ore table. */
final class HoleBiomeData {
    private HoleBiomeData() {}

    private record Air(int fog, int sky, int water, int waterFog, int grass, int foliage, float ash) {}

    private static final Air CITY = new Air(0x59665A, 0x47544C, 0x46604A, 0x202C20, 0x6B7443, 0x5A6438, 0.0025F);
    private static final Air SOOT = new Air(0x4C4F45, 0x3A3D36, 0x4A4F3C, 0x1C1E16, 0x5F5F3A, 0x505030, 0.0070F);
    private static final Air BAZAAR = new Air(0x6A6650, 0x55564A, 0x46604A, 0x202C20, 0x72763F, 0x62683A, 0.0015F);
    private static final Air BRACKISH = new Air(0x55685F, 0x46574F, 0x3D6650, 0x162A20, 0x5E7346, 0x4C6039, 0.0020F);
    private static final Air BLIGHT = new Air(0x5B5A49, 0x484A3D, 0x535A3D, 0x22261A, 0x777044, 0x6A6238, 0.0040F);
    private static final Air GRAVE = new Air(0x4F5752, 0x3C4440, 0x3E5247, 0x1A2420, 0x4F6048, 0x44523D, 0.0030F);
    private static final Air BELOW = new Air(0x2E3430, 0x262B28, 0x35503F, 0x121C14, 0x4F6048, 0x44523D, 0.0F);

    static void bootstrap(BootstrapContext<Biome> context) {
        context.register(HoleBiomes.RESIDENTIAL, biome(context, CITY, spawns -> hunters(spawns, 100, 100, 100, 25)));
        context.register(HoleBiomes.MARKET, biome(context, BAZAAR, spawns -> hunters(spawns, 70, 60, 50, 10)));
        context.register(HoleBiomes.INDUSTRIAL, biome(context, SOOT, spawns -> {
            hunters(spawns, 80, 110, 140, 30);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SILVERFISH, 12, 1, 3));
        }));
        context.register(HoleBiomes.WATERFRONT, biome(context, BRACKISH, spawns -> {
            hunters(spawns, 90, 70, 70, 15);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 40, 1, 2));
            spawns.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 3, 1, 2));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 12, 2, 4));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 8, 1, 3));
        }));
        context.register(HoleBiomes.DERELICT, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 110, 100, 90, 35);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 6, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 6, 2, 3));
        }));
        context.register(HoleBiomes.GRAVEYARD, biome(context, GRAVE, spawns -> {
            hunters(spawns, 150, 150, 40, 10);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 30, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 1, 2));
        }));
        context.register(HoleBiomes.OUTSKIRTS, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 100, 90, 110, 35);
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 3, 1, 2));
        }));
        context.register(HoleBiomes.UNDERCITY, biome(context, BELOW, spawns -> {
            hunters(spawns, 95, 100, 100, 60);
            spawns.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 2, 4));
            spawns.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 4, 2, 3));
        }));
    }

    /**
     * The shared monster table. Creepers are rare and witches, slimes and endermen stay present so their drops stay
     * obtainable: redstone-brewing ingredients, slime balls and ender pearls all come from here.
     */
    private static void hunters(MobSpawnSettings.Builder spawns, int zombies, int skeletons, int spiders, int creepers) {
        BiomeDefaultFeatures.caveSpawns(spawns);
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, zombies, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, skeletons, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, spiders, 2, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, creepers, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 60, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    private static Biome biome(BootstrapContext<Biome> context, Air air, Consumer<MobSpawnSettings.Builder> spawnList) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawnList.accept(spawns);

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(air.fog)
                .skyColor(air.sky)
                .waterColor(air.water)
                .waterFogColor(air.waterFog)
                .grassColorOverride(air.grass)
                .foliageColorOverride(air.foliage)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
        if (air.ash > 0.0F) {
            effects.ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, air.ash));
        }

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.7F)
                .downfall(0.6F)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation(context).build())
                .build();
    }

    /**
     * Follows {@code OverworldBiomes.globalOverworldGeneration} and vanilla's mountain biomes in the same order, because
     * the feature sorter crashes on any ordering conflict with the vanilla cave biomes this world also uses. Carvers and
     * surface lakes are omitted because they would cut the city, and water springs because they could flood basements.
     */
    private static BiomeGenerationSettings.Builder generation(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(features, carvers);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        generation.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addExtraEmeralds(generation);
        BiomeDefaultFeatures.addInfestedStone(generation);
        return generation;
    }
}
