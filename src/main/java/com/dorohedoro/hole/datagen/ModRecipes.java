package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Salvage recipes. They give a scavenger a route to iron without ever finding an ore vein. */
final class ModRecipes extends RecipeProvider {
    ModRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.IRON_INGOT)
                .requires(ModItems.SCRAP_METAL.get(), 9)
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output, id("iron_ingot_from_scrap"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUSTED_PLATING.get(), 2)
                .pattern("SS").pattern("SS")
                .define('S', ModItems.SCRAP_METAL.get())
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SCRAP_METAL.get()), RecipeCategory.MISC, Items.IRON_NUGGET, 0.1F, 200)
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output, id("iron_nugget_from_smelting_scrap"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path);
    }
}
