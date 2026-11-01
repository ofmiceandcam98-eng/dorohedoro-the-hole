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
        // Rebuild reclaimed underground masonry: all ingredients are available in the Hole.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SEWER_BRICKWORK.get(), 4)
                .requires(Items.STONE_BRICKS, 4).requires(Items.MUD).requires(Items.CHARCOAL)
                .unlockedBy("has_bricks", has(Items.STONE_BRICKS))
                .save(output, id("sewer_brickwork"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SCRAP_METAL.get()), RecipeCategory.MISC, Items.IRON_NUGGET, 0.1F, 200)
                .unlockedBy("has_scrap", has(ModItems.SCRAP_METAL.get()))
                .save(output, id("iron_nugget_from_smelting_scrap"));
        // Recover old magical smoke from contaminated filters found in the Hole.
        // This consumes a salvaged material: coal alone never manufactures sorcerer smoke.
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SMOKE_SOAKED_FILTER.get()),
                RecipeCategory.MISC, ModItems.SMOKE_RESIDUE.get(), 0.35F, 200)
                .unlockedBy("has_smoke_filter", has(ModItems.SMOKE_SOAKED_FILTER.get()))
                .save(output, id("smoke_residue_from_filter"));
        // Deliberately expensive, noncanonical survival alternative to a rare found Devil Sigil.
        // All materials are available before Hell, without mandatory NPC murder or quests.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEVIL_SIGIL.get())
                .pattern("OGO").pattern("SBS").pattern("OEO")
                .define('O', Items.OBSIDIAN).define('G', Items.GOLD_INGOT)
                .define('S', ModItems.SMOKE_RESIDUE.get()).define('B', Items.BONE_BLOCK)
                .define('E', Items.EMERALD)
                .unlockedBy("has_smoke", has(ModItems.SMOKE_RESIDUE.get()))
                .save(output, id("devil_sigil"));
        // Noncanonical but survival-compatible laboratory equipment based on Kasukabe's artificial door.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMOKE_CONDENSER.get())
                .pattern("IRI").pattern("CSC").pattern("IRI")
                .define('I', Items.IRON_INGOT).define('R', Items.REDSTONE).define('C', Items.COPPER_INGOT)
                .define('S', ModItems.SMOKE_RESIDUE.get())
                .unlockedBy("has_smoke", has(ModItems.SMOKE_RESIDUE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SMOKE_CONDUIT.get(), 2)
                .pattern("CGC").pattern("IRI").pattern("CGC")
                .define('C', Items.COPPER_INGOT).define('G', Items.GLASS).define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE).unlockedBy("has_copper", has(Items.COPPER_INGOT)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PRESERVED_SMOKE_CONDUIT.get())
                .requires(ModItems.SMOKE_CONDUIT.get()).requires(ModItems.SMOKE_RESIDUE.get()).requires(Items.BONE_MEAL)
                .unlockedBy("has_conduit", has(ModItems.SMOKE_CONDUIT.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REINFORCED_DOOR_FRAME.get(), 4)
                .pattern("IRI").pattern("RSR").pattern("IRI")
                .define('I', Items.IRON_INGOT).define('R', ModBlocks.RUSTED_PLATING.get())
                .define('S', Items.STONE_BRICKS).unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ARTIFICIAL_DOOR_CORE.get())
                .pattern("BCB").pattern("IMI").pattern("BRB")
                .define('B', Items.BONE_BLOCK).define('C', ModItems.SMOKE_CONDENSER.get())
                .define('I', Items.IRON_INGOT).define('M', ModItems.PRESERVED_SMOKE_CONDUIT.get())
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_condenser", has(ModItems.SMOKE_CONDENSER.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DOOR_ACTIVATION_TOOL.get())
                .pattern(" RI").pattern(" SR").pattern("S  ")
                .define('I', Items.IRON_INGOT).define('R', Items.REDSTONE).define('S', Items.STICK)
                .unlockedBy("has_redstone", has(Items.REDSTONE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DOOR_LINKING_KEY.get())
                .pattern(" II").pattern(" RI").pattern("S  ")
                .define('I', Items.IRON_INGOT).define('R', Items.REDSTONE).define('S', Items.STICK)
                .unlockedBy("has_redstone", has(Items.REDSTONE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ETCHED_GATE_STONE.get(), 4)
                .pattern("BSB").pattern("SRS").pattern("BSB")
                .define('B', Items.BONE_BLOCK).define('S', Items.STONE_BRICKS).define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_bone", has(Items.BONE_BLOCK)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.INFERNAL_DOOR_CORE.get())
                .pattern("BSB").pattern("RCR").pattern("BSB")
                .define('B', Items.BONE_BLOCK).define('S', ModItems.DEVIL_SIGIL.get())
                .define('R', ModBlocks.ETCHED_GATE_STONE.get()).define('C', ModItems.PRESERVED_SMOKE_CONDUIT.get())
                .unlockedBy("has_sigil", has(ModItems.DEVIL_SIGIL.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RITUAL_TOOL.get())
                .pattern(" RS").pattern(" BR").pattern("B  ")
                .define('R', Items.REDSTONE).define('S', Items.GOLD_INGOT).define('B', Items.BONE)
                .unlockedBy("has_sigil", has(ModItems.DEVIL_SIGIL.get())).save(output);

        // Every mob resource has a real survival use rather than being decorative loot.
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_RAT_MEAT.get()),
                RecipeCategory.FOOD, ModItems.COOKED_RAT_MEAT.get(), 0.2F, 200)
                .unlockedBy("has_rat_meat", has(ModItems.RAW_RAT_MEAT.get())).save(output, id("cooked_rat_meat"));
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.RAW_RAT_MEAT.get()),
                RecipeCategory.FOOD, ModItems.COOKED_RAT_MEAT.get(), 0.2F, 100)
                .unlockedBy("has_rat_meat", has(ModItems.RAW_RAT_MEAT.get())).save(output, id("smoked_rat_meat"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SALTED_RAT_RATION.get())
                .requires(ModItems.COOKED_RAT_MEAT.get()).requires(ModItems.ROCK_SALT.get())
                .requires(Items.BREAD).unlockedBy("has_salt", has(ModItems.ROCK_SALT.get()))
                .save(output, id("salted_rat_ration"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Items.WHITE_WOOL)
                .requires(ModItems.RAT_FUR.get(), 4).unlockedBy("has_rat_fur", has(ModItems.RAT_FUR.get()))
                .save(output, id("wool_from_rat_fur"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHITIN_PLATING.get(), 4)
                .pattern("CIC").pattern("ISI").pattern("CIC")
                .define('C', ModItems.REINFORCED_CHITIN.get()).define('I', Items.IRON_NUGGET)
                .define('S', Items.STONE_BRICKS)
                .unlockedBy("has_chitin", has(ModItems.REINFORCED_CHITIN.get()))
                .save(output, id("chitin_plating"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SEWER_ECHO_PROBE.get())
                .pattern("ARA").pattern("RCR").pattern("ISI")
                .define('A', ModItems.ROACH_ANTENNA.get()).define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT).define('I', Items.IRON_NUGGET).define('S', Items.STICK)
                .unlockedBy("has_antenna", has(ModItems.ROACH_ANTENNA.get()))
                .save(output, id("sewer_echo_probe"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.GUNPOWDER, 2)
                .requires(ModItems.RAW_SULFUR.get(), 2).requires(Items.CHARCOAL).requires(Items.REDSTONE)
                .unlockedBy("has_sulfur", has(ModItems.RAW_SULFUR.get()))
                .save(output, id("industrial_gunpowder"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.LIMESTONE.get()),
                RecipeCategory.MISC, ModItems.LIME_POWDER.get(), 0.1F, 200)
                .unlockedBy("has_limestone", has(ModBlocks.LIMESTONE.get())).save(output, id("lime_from_limestone"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SEWER_BRICKWORK.get(), 4)
                .requires(ModItems.LIME_POWDER.get(), 2).requires(Items.STONE_BRICKS, 2)
                .requires(Items.GRAVEL).unlockedBy("has_lime", has(ModItems.LIME_POWDER.get()))
                .save(output, id("sewer_bricks_from_lime"));
        // Original collected limb can be restored by an experienced Healing
        // Sorcerer, or by any player using this single-use surgical kit.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SURGICAL_REATTACHMENT_KIT.get())
                .pattern("ISI").pattern("SMS").pattern("ISI")
                .define('I', Items.IRON_NUGGET).define('S', Items.STRING)
                .define('M', ModItems.MEDICAL_SALVE.get())
                .unlockedBy("has_medical_salve", has(ModItems.MEDICAL_SALVE.get()))
                .save(output, id("surgical_reattachment_kit"));
        // Batch 02: each new animal drop and mineral contributes a functioning survival recipe.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MEDICAL_SALVE.get())
                .requires(ModItems.LEECH_MUCUS.get(), 2).requires(Items.PAPER).requires(Items.HONEY_BOTTLE)
                .unlockedBy("has_leech", has(ModItems.LEECH_MUCUS.get())).save(output, id("medical_salve"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.SLIME_BALL)
                .requires(ModItems.LEECH_SUCKER.get(), 2).requires(ModItems.LEECH_MUCUS.get())
                .unlockedBy("has_sucker", has(ModItems.LEECH_SUCKER.get())).save(output, id("slime_from_leech"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GLOW_TONIC.get())
                .requires(ModItems.LUMINOUS_SCALE.get(), 2).requires(Items.HONEY_BOTTLE).requires(Items.GLOW_BERRIES)
                .unlockedBy("has_scale", has(ModItems.LUMINOUS_SCALE.get())).save(output, id("glow_tonic"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STRING, 2)
                .requires(ModItems.MOTH_SILK.get(), 2)
                .unlockedBy("has_silk", has(ModItems.MOTH_SILK.get())).save(output, id("string_from_moth_silk"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CAVERN_LAMP.get(), 2)
                .pattern("GBG").pattern("SLS").pattern("GIG")
                .define('G', Items.GLASS_PANE).define('B', ModItems.BRASS_INGOT.get())
                .define('S', ModItems.LUMINOUS_SCALE.get()).define('L', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_scale", has(ModItems.LUMINOUS_SCALE.get())).save(output, id("cavern_lamp"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_ZINC.get()), RecipeCategory.MISC,
                ModItems.ZINC_INGOT.get(), 0.6F, 200)
                .unlockedBy("has_zinc", has(ModItems.RAW_ZINC.get())).save(output, id("zinc_ingot"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.RAW_ZINC.get()), RecipeCategory.MISC,
                ModItems.ZINC_INGOT.get(), 0.6F, 100)
                .unlockedBy("has_zinc", has(ModItems.RAW_ZINC.get())).save(output, id("blast_zinc_ingot"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BRASS_INGOT.get(), 2)
                .requires(ModItems.ZINC_INGOT.get()).requires(Items.COPPER_INGOT)
                .unlockedBy("has_zinc", has(ModItems.ZINC_INGOT.get())).save(output, id("brass_ingot"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.OIL_SHALE_CHUNK.get()), RecipeCategory.MISC,
                ModItems.BITUMEN.get(), 0.3F, 200)
                .unlockedBy("has_shale", has(ModItems.OIL_SHALE_CHUNK.get())).save(output, id("bitumen_from_shale"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SEWER_SEALANT.get(), 2)
                .requires(ModItems.BITUMEN.get()).requires(ModItems.LEECH_MUCUS.get()).requires(Items.SLIME_BALL)
                .unlockedBy("has_bitumen", has(ModItems.BITUMEN.get())).save(output, id("sewer_sealant"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SEWER_SEALANT.get(), 2)
                .requires(ModItems.BITUMEN.get()).requires(Items.SLIME_BALL, 2).requires(Items.CLAY_BALL)
                .unlockedBy("has_bitumen", has(ModItems.BITUMEN.get())).save(output, id("sewer_sealant_from_slime"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GALVANIZED_PLATING.get(), 4)
                .pattern("ZIZ").pattern("IBI").pattern("ZIZ")
                .define('Z', ModItems.ZINC_INGOT.get()).define('I', Items.IRON_INGOT)
                .define('B', ModItems.SEWER_SEALANT.get())
                .unlockedBy("has_zinc", has(ModItems.ZINC_INGOT.get())).save(output, id("galvanized_plating"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.SLIME_BALL, 2)
                .requires(ModItems.SEWER_SEALANT.get()).requires(ModItems.LEECH_SUCKER.get())
                .unlockedBy("has_sealant", has(ModItems.SEWER_SEALANT.get())).save(output, id("slime_from_sealant"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path);
    }
}
