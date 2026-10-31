package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ModModels {
    private ModModels() {}

    static final class Blocks extends BlockStateProvider {
        Blocks(PackOutput output, ExistingFileHelper files) {
            super(output, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void registerStatesAndModels() {
            simpleBlockWithItem(ModBlocks.INDUSTRIAL_DEBRIS.get(), cubeAll(ModBlocks.INDUSTRIAL_DEBRIS.get()));
            simpleBlockWithItem(ModBlocks.RUSTED_PLATING.get(), cubeAll(ModBlocks.RUSTED_PLATING.get()));
        }
    }

    static final class Items extends ItemModelProvider {
        Items(PackOutput output, ExistingFileHelper files) {
            super(output, Dorohedoro.MOD_ID, files);
        }

        @Override
        protected void registerModels() {
            basicItem(ModItems.SCRAP_METAL.get());
        }
    }
}
