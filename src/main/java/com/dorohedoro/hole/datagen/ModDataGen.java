package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.worldgen.HoleBiomes;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Wires up {@code runData}. Output lands in {@code src/generated/resources}. */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class ModDataGen {
    private ModDataGen() {}

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        RegistrySetBuilder registries = new RegistrySetBuilder()
                 .add(Registries.CONFIGURED_FEATURE, ctx -> {
                    RealmBiomeData.configuredFeatures(ctx);
                    HoleMineralFeatures.configuredFeatures(ctx);
                })
                 .add(Registries.PLACED_FEATURE, ctx -> {
                    RealmBiomeData.placedFeatures(ctx);
                    HoleMineralFeatures.placedFeatures(ctx);
                })
                .add(Registries.BIOME, ctx -> {
                    HoleBiomeData.bootstrap(ctx);
                    RealmBiomeData.biomes(ctx);
                });
        DatapackBuiltinEntriesProvider entries =
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), registries, Set.of(Dorohedoro.MOD_ID));
        generator.addProvider(event.includeServer(), entries);
        generator.addProvider(event.includeServer(), ModLoot.provider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ModRecipes(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new HoleBlockTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModModels.Blocks(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModModels.Items(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(),
                new HoleBiomeTags(output, entries.getRegistryProvider(), event.getExistingFileHelper()));
    }

    private static final class HoleBlockTags extends BlockTagsProvider {
        HoleBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.INDUSTRIAL_DEBRIS.get(), ModBlocks.RUSTED_PLATING.get(), ModBlocks.HELL_ROCK.get(), ModBlocks.SEWER_BRICKWORK.get(),
                    ModBlocks.REINFORCED_DOOR_FRAME.get(), ModBlocks.ETCHED_GATE_STONE.get(),
                    ModBlocks.ARTIFICIAL_DOOR_CORE.get(), ModBlocks.INFERNAL_DOOR_CORE.get(),
                    ModBlocks.GENERATED_DOOR_FRAME.get(), ModBlocks.GENERATED_GATE_STONE.get(),
                    ModBlocks.GENERATED_ARTIFICIAL_CORE.get(), ModBlocks.GENERATED_INFERNAL_CORE.get(),
                    ModBlocks.SULFUR_ORE.get(), ModBlocks.ROCK_SALT_ORE.get(),
                    ModBlocks.LIMESTONE.get(), ModBlocks.CHITIN_PLATING.get(),
                    ModBlocks.ZINC_ORE.get(), ModBlocks.OIL_SHALE_ORE.get(),
                    ModBlocks.GALVANIZED_PLATING.get(), ModBlocks.CAVERN_LAMP.get(),
                    ModBlocks.SMOKE_STAINED_CONCRETE.get(), ModBlocks.WATER_DAMAGED_PLASTER.get(), ModBlocks.SOOT_BLACKENED_BRICK.get(), ModBlocks.PATCHWORK_MASONRY.get(), ModBlocks.RUSTED_INDUSTRIAL_SHEET.get(), ModBlocks.SEEPAGE_BRICK.get());
            tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.ASH_SOIL.get());
        }
    }

    /** Hole biomes join the vanilla tags that place strongholds, mineshafts and trial chambers. */
    private static final class HoleBiomeTags extends BiomeTagsProvider {
        HoleBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BiomeTags.IS_OVERWORLD).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA,
                    HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM, HoleBiomes.UNDERCITY);
            tag(BiomeTags.STRONGHOLD_BIASED_TO).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA,
                    HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM, HoleBiomes.UNDERCITY);
            tag(BiomeTags.HAS_MINESHAFT).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA,
                    HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM, HoleBiomes.UNDERCITY,
                    RealmBiomes.SW_CITY, RealmBiomes.SW_CITY_EDGE, RealmBiomes.SW_FARMLAND,
                    RealmBiomes.SW_WOODLAND, RealmBiomes.SW_HYDRA_FOREST, RealmBiomes.SW_BLIGHT,
                    RealmBiomes.SW_WATERS, RealmBiomes.SW_CAVES, RealmBiomes.SW_FROSTBOUND_HIGHLANDS);
            tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(HoleBiomes.UNDERCITY);
            vanillaSurvivalStructures();
        }

        /**
         * Java 1.21.1 vanilla structures are gated by biome tags. Custom Hole climates
         * otherwise silently exclude many standard exploration/progression locations.
         * Tags append to vanilla data; never replace vanilla structures or the original tags.
         *
         * No artificial ancient city tag: those must remain in vanilla DEEP_DARK so
         * naturally generated sculk shriekers, the Warden and atmosphere stay coherent.
         * No ocean structure tags: the custom world currently has canals, not actual oceans.
         * Don't claim those structures are supported until genuine ocean areas exist.
         */
        private void vanillaSurvivalStructures() {
            // Dry and snowy settlements; vanilla villager systems remain intact.
            tag(hasStructure("village_desert")).add(HoleBiomes.DUST_BASIN);
            tag(hasStructure("village_snowy")).add(HoleBiomes.SNOWFIELDS);
            tag(hasStructure("village_taiga")).add(HoleBiomes.DEAD_TAIGA);
            tag(hasStructure("village_savanna")).add(HoleBiomes.IRON_BADLANDS);
            tag(hasStructure("village_plains")).add(HoleBiomes.OUTSKIRTS, HoleBiomes.RESIDENTIAL);

            // Vanilla temples, archaeology and small ruins survive in suitable Hole climates.
            tag(hasStructure("desert_pyramid")).add(HoleBiomes.DUST_BASIN);
            tag(hasStructure("igloo")).add(HoleBiomes.SNOWFIELDS);
            tag(hasStructure("jungle_temple")).add(HoleBiomes.DEAD_TAIGA);
            tag(hasStructure("swamp_hut")).add(HoleBiomes.WATERFRONT);
            tag(hasStructure("trail_ruins")).add(HoleBiomes.DEAD_TAIGA);

            // Larger exploration sites and raids in the more isolated regions.
            tag(hasStructure("woodland_mansion")).add(HoleBiomes.DEAD_TAIGA);
            tag(hasStructure("pillager_outpost")).add(
                    HoleBiomes.OUTSKIRTS, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA,
                    HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM);
            tag(hasStructure("mineshaft_mesa")).add(HoleBiomes.IRON_BADLANDS);
            tag(hasStructure("ruined_portal_desert")).add(HoleBiomes.DUST_BASIN);
            tag(hasStructure("ruined_portal_mountain")).add(HoleBiomes.MOUNTAIN_RIM);
            tag(hasStructure("ruined_portal_swamp")).add(HoleBiomes.WATERFRONT);
            tag(hasStructure("ruined_portal_jungle")).add(HoleBiomes.DEAD_TAIGA);
            tag(hasStructure("ruined_portal_standard")).add(
                    HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS,
                    HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS,
                    HoleBiomes.DEAD_TAIGA, HoleBiomes.IRON_BADLANDS);
            tag(hasStructure("trial_chambers")).add(
                    HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL,
                    HoleBiomes.WATERFRONT, HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD,
                    HoleBiomes.OUTSKIRTS, HoleBiomes.FROSTBITTEN_INDUSTRIAL,
                    HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA, HoleBiomes.DUST_BASIN,
                    HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM);
        }

        private static TagKey<Biome> hasStructure(String id) {
            return TagKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "has_structure/" + id));
        }
    }
}
