package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.worldgen.HoleBiomes;
import com.dorohedoro.hole.registry.ModEntities;
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

    private static final Air CITY = new Air(0x85938A, 0x9CAAA5, 0x46604A, 0x202C20, 0x6B7443, 0x5A6438, 0.0025F);
    private static final Air SOOT = new Air(0x72776C, 0x8F998F, 0x4A4F3C, 0x1C1E16, 0x5F5F3A, 0x505030, 0.0070F);
    private static final Air BAZAAR = new Air(0x9A957E, 0xB4B2A0, 0x46604A, 0x202C20, 0x72763F, 0x62683A, 0.0015F);
    private static final Air BRACKISH = new Air(0x84978D, 0x9CAC9F, 0x3D6650, 0x162A20, 0x5E7346, 0x4C6039, 0.0020F);
    private static final Air BLIGHT = new Air(0x888677, 0xA49F8C, 0x535A3D, 0x22261A, 0x777044, 0x6A6238, 0.0040F);
    private static final Air GRAVE = new Air(0x798883, 0x99A9A3, 0x3E5247, 0x1A2420, 0x4F6048, 0x44523D, 0.0030F);
    private static final Air FROST = new Air(0x7B8687, 0x9BA7A7, 0x465953, 0x1D2724, 0x737575, 0x636D68, 0.0060F);
    private static final Air BELOW = new Air(0x59635D, 0x798781, 0x35503F, 0x121C14, 0x4F6048, 0x44523D, 0.0F);

    static void bootstrap(BootstrapContext<Biome> context) {
        context.register(HoleBiomes.RESIDENTIAL, biome(context, CITY, spawns -> {
            hunters(spawns, 100, 100, 100, 25);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 10, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 3, 1, 1));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 8, 1, 1));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 5, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        context.register(HoleBiomes.MARKET, biome(context, BAZAAR, spawns -> {
            hunters(spawns, 70, 60, 50, 10);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 17, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 4, 1, 1));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 8, 1, 1));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 5, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        context.register(HoleBiomes.INDUSTRIAL, biome(context, SOOT, spawns -> {
            hunters(spawns, 80, 110, 140, 30);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 19, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 17, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 7, 1, 1));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 4, 1, 1));

            // Scrap feeders are uncommon on industrial streets; they remain more frequent underground.
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 9, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SILVERFISH, 12, 1, 3));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        context.register(HoleBiomes.WATERFRONT, biome(context, BRACKISH, spawns -> {
            hunters(spawns, 90, 70, 70, 15);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 14, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 8, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 20, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 7, 1, 1));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 5, 1, 2));

            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 40, 1, 2));
            spawns.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 3, 1, 2));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 12, 2, 4));
            canalFish(spawns);
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 5, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        context.register(HoleBiomes.DERELICT, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 110, 100, 90, 35);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 14, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 20, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 10, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 3, 1, 1));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 9, 1, 2));

            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 6, 1, 2));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 6, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 6, 2, 3));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 8, 1, 1));
        }));
        context.register(HoleBiomes.GRAVEYARD, biome(context, GRAVE, spawns -> {
            hunters(spawns, 150, 150, 40, 10);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 10, 1, 1));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 9, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 4, 1, 1));

            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 30, 1, 3));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 1, 2));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 16, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 8, 1, 1));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 5, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
        }));
        context.register(HoleBiomes.OUTSKIRTS, biome(context, BLIGHT, spawns -> {
            hunters(spawns, 100, 90, 110, 35);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 8, 1, 1));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 9, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 7, 1, 1));

            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 3, 1, 1));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 2, 4));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 2, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 3, 1, 2));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 16, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 8, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        // Rare, original winter pocket: snow is established in the Hole, permanent cold neighborhoods are not canon.
        context.register(HoleBiomes.FROSTBITTEN_INDUSTRIAL, coldBiome(context, FROST, spawns -> {
            hunters(spawns, 90, 100, 65, 15);
            // Surface canals run through this district too, not just the waterfront.
            // The salmon spawn restriction below keeps them in exposed surface water.
            canalFish(spawns);
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 9, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 10, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 4, 1, 1));

            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 8, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 8, 1, 2));
            // Sewers at Y=49 retain the surface district biome (undercity starts below Y=40).
            // These registrations are depth-gated by ModEntities, so they populate underground
            // without introducing these creatures in inappropriate above-ground neighborhoods.
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 7, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 4, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 7, 1, 2));
        }));
        context.register(HoleBiomes.UNDERCITY, biome(context, BELOW, spawns -> {
            hunters(spawns, 95, 100, 100, 60);
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_ROACH.get(), 65, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.SCRAP_RAT.get(), 30, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SEWER_LEECH.get(), 22, 1, 2));
            spawns.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(ModEntities.GLOW_MOTH.get(), 18, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SLUDGE_CRAWLER.get(), 14, 1, 1));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.RUST_EATER_BEETLE.get(), 18, 1, 2));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.CAVERN_BURROWER.get(), 7, 1, 1));
            spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPORE_CRAWLER.get(), 14, 1, 2));
            spawns.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 2, 4));
            spawns.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 4, 2, 3));
        }));
    }

    /** Fish are naturally replenished in canal water in ALL city districts, not just waterfront. */
    private static void canalFish(MobSpawnSettings.Builder spawns) {
        spawns.addSpawn(MobCategory.WATER_AMBIENT,
                new MobSpawnSettings.SpawnerData(EntityType.SALMON, 12, 2, 3));
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

    private static Biome coldBiome(BootstrapContext<Biome> context, Air air, Consumer<MobSpawnSettings.Builder> spawnList) {
        return biome(context, air, spawnList, 0.0F);
    }

    private static Biome biome(BootstrapContext<Biome> context, Air air, Consumer<MobSpawnSettings.Builder> spawnList) {
        return biome(context, air, spawnList, 0.7F);
    }

    private static Biome biome(BootstrapContext<Biome> context, Air air, Consumer<MobSpawnSettings.Builder> spawnList, float temperature) {
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
                .temperature(temperature)
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
        // After vanilla ores, preserve the canonical ore order across all eight Hole biomes.
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HoleMineralFeatures.SULFUR_PLACED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HoleMineralFeatures.SALT_PLACED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HoleMineralFeatures.LIMESTONE_PLACED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HoleMineralFeatures.ZINC_PLACED);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HoleMineralFeatures.OIL_SHALE_PLACED);
        return generation;
    }
}
