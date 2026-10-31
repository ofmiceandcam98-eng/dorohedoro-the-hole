package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleBiomes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        RegistrySetBuilder registries = new RegistrySetBuilder().add(Registries.BIOME, HoleBiomeData::bootstrap);
        DatapackBuiltinEntriesProvider entries =
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), registries, Set.of(Dorohedoro.MOD_ID));
        generator.addProvider(event.includeServer(), entries);
        generator.addProvider(event.includeServer(),
                new HoleBiomeTags(output, entries.getRegistryProvider(), event.getExistingFileHelper()));
    }

    /** Hole biomes join the vanilla tags that place strongholds, mineshafts and trial chambers. */
    private static final class HoleBiomeTags extends BiomeTagsProvider {
        HoleBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BiomeTags.IS_OVERWORLD).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.UNDERCITY);
            tag(BiomeTags.STRONGHOLD_BIASED_TO).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.UNDERCITY);
            tag(BiomeTags.HAS_MINESHAFT).add(HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT,
                    HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.UNDERCITY);
            tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(HoleBiomes.UNDERCITY);
        }
    }
}
