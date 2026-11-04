package com.dorohedoro.hole.datagen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Block drops and chest tables. Chests are how items with no other Hole source stay obtainable. */
final class ModLoot {
    private ModLoot() {}

    static LootTableProvider provider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockDrops::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(provider -> new Chests(), LootContextParamSets.CHEST)
        ), lookup);
    }

    private static LootPoolSingletonContainer.Builder<?> item(ItemLike item, int min, int max) {
        return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight, int min, int max) {
        return item(item, min, max).setWeight(weight);
    }

    private static final class BlockDrops extends BlockLootSubProvider {
        BlockDrops(HolderLookup.Provider lookup) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), lookup);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.RUSTED_PLATING.get());
            dropSelf(ModBlocks.HELL_ROCK.get());
            dropSelf(ModBlocks.ASH_SOIL.get());
            dropSelf(ModBlocks.SEWER_BRICKWORK.get());
            dropSelf(ModBlocks.LIMESTONE.get());
            dropSelf(ModBlocks.CHITIN_PLATING.get());
            dropSelf(ModBlocks.GALVANIZED_PLATING.get());
            dropSelf(ModBlocks.CAVERN_LAMP.get());
            dropSelf(ModBlocks.SMOKE_STAINED_CONCRETE.get());
            dropSelf(ModBlocks.WATER_DAMAGED_PLASTER.get());
            dropSelf(ModBlocks.SOOT_BLACKENED_BRICK.get());
            dropSelf(ModBlocks.PATCHWORK_MASONRY.get());
            dropSelf(ModBlocks.RUSTED_INDUSTRIAL_SHEET.get());
            dropSelf(ModBlocks.SEEPAGE_BRICK.get());

            add(ModBlocks.ZINC_ORE.get(), createOreDrop(ModBlocks.ZINC_ORE.get(), ModItems.RAW_ZINC.get()));
            add(ModBlocks.OIL_SHALE_ORE.get(), createOreDrop(ModBlocks.OIL_SHALE_ORE.get(), ModItems.OIL_SHALE_CHUNK.get()));
            add(ModBlocks.SULFUR_ORE.get(), createOreDrop(ModBlocks.SULFUR_ORE.get(), ModItems.RAW_SULFUR.get()));
            add(ModBlocks.ROCK_SALT_ORE.get(), createOreDrop(ModBlocks.ROCK_SALT_ORE.get(), ModItems.ROCK_SALT.get()));
            add(ModBlocks.DEEPSLATE_ZINC_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_ZINC_ORE.get(), ModItems.RAW_ZINC.get()));
            add(ModBlocks.DEEPSLATE_OIL_SHALE_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_OIL_SHALE_ORE.get(), ModItems.OIL_SHALE_CHUNK.get()));
            add(ModBlocks.DEEPSLATE_SULFUR_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_SULFUR_ORE.get(), ModItems.RAW_SULFUR.get()));
            add(ModBlocks.DEEPSLATE_ROCK_SALT_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_ROCK_SALT_ORE.get(), ModItems.ROCK_SALT.get()));
            dropSelf(ModBlocks.REINFORCED_DOOR_FRAME.get());
            dropSelf(ModBlocks.ETCHED_GATE_STONE.get());
            dropSelf(ModBlocks.ARTIFICIAL_DOOR_CORE.get());
            dropSelf(ModBlocks.INFERNAL_DOOR_CORE.get());
            add(ModBlocks.INDUSTRIAL_DEBRIS.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(item(ModItems.SCRAP_METAL.get(), 1, 3)))
                    .withPool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.25F)).add(item(Items.IRON_NUGGET, 1, 2)))
                    .withPool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.16F))
                            .add(item(ModItems.SMOKE_SOAKED_FILTER.get(), 1, 1))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())
                    .filter(block -> block != ModBlocks.PORTAL_SURFACE.get()
                            && block != ModBlocks.GENERATED_DOOR_FRAME.get()
                            && block != ModBlocks.GENERATED_GATE_STONE.get()
                            && block != ModBlocks.GENERATED_ARTIFICIAL_CORE.get()
                            && block != ModBlocks.GENERATED_INFERNAL_CORE.get()).toList();
        }
    }

    private static final class Chests implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out) {
            table(out, "starter_cache", 4, 6,
                    entry(Items.BREAD, 10, 2, 4), entry(Items.COOKED_COD, 6, 1, 3), entry(Items.APPLE, 6, 1, 3),
                    entry(Items.OAK_LOG, 10, 3, 6), entry(Items.OAK_SAPLING, 6, 1, 2), entry(Items.WHEAT_SEEDS, 6, 2, 4),
                    entry(Items.CARROT, 4, 1, 3), entry(Items.POTATO, 4, 1, 3), entry(Items.COAL, 8, 2, 5),
                    entry(Items.TORCH, 8, 4, 8), entry(Items.STONE_PICKAXE, 6, 1, 1), entry(Items.STONE_AXE, 4, 1, 1),
                    entry(Items.LEATHER, 4, 1, 3), entry(Items.WATER_BUCKET, 2, 1, 1));
            table(out, "apartment", 1, 4,
                    entry(Items.BREAD, 10, 1, 3), entry(Items.PAPER, 8, 1, 4), entry(Items.BOOK, 4, 1, 2), entry(Items.STRING, 8, 1, 3),
                    entry(Items.STICK, 10, 2, 5), entry(Items.COAL, 6, 1, 3), entry(Items.BONE_MEAL, 4, 1, 3), entry(Items.LEATHER_BOOTS, 2, 1, 1),
                    entry(Items.CANDLE, 4, 1, 2), entry(Items.GLASS_BOTTLE, 4, 1, 2), entry(Items.BOWL, 4, 1, 2), entry(Items.IRON_NUGGET, 4, 1, 4),
                    entry(ModItems.SMOKE_RESIDUE.get(), 1, 1, 1), entry(ModItems.SMOKE_SOAKED_FILTER.get(), 3, 1, 2));
            table(out, "shop", 2, 5,
                    entry(Items.EMERALD, 4, 1, 2), entry(Items.PAPER, 8, 2, 5), entry(Items.INK_SAC, 4, 1, 2), entry(Items.WHITE_WOOL, 6, 1, 3),
                    entry(Items.LEATHER, 6, 1, 3), entry(Items.IRON_INGOT, 3, 1, 2), entry(Items.COPPER_INGOT, 5, 1, 3), entry(Items.GLASS_BOTTLE, 6, 1, 3),
                    entry(Items.NAME_TAG, 1, 1, 1), entry(Items.LEAD, 2, 1, 1), entry(Items.BUCKET, 2, 1, 1), entry(Items.SHEARS, 2, 1, 1));
            table(out, "market", 3, 6,
                    entry(Items.WHEAT, 8, 2, 5), entry(Items.CARROT, 8, 2, 4), entry(Items.POTATO, 8, 2, 4), entry(Items.BEETROOT, 6, 2, 4),
                    entry(Items.BEEF, 5, 1, 3), entry(Items.PORKCHOP, 5, 1, 3), entry(Items.CHICKEN, 5, 1, 3), entry(Items.COD, 5, 1, 3),
                    entry(Items.EGG, 5, 1, 3), entry(Items.MILK_BUCKET, 2, 1, 1), entry(Items.SUGAR_CANE, 5, 1, 3), entry(Items.HONEY_BOTTLE, 2, 1, 1),
                    entry(Items.PUMPKIN_SEEDS, 3, 1, 2), entry(Items.MELON_SEEDS, 3, 1, 2), entry(Items.BROWN_MUSHROOM, 4, 1, 2), entry(Items.RED_MUSHROOM, 4, 1, 2),
                    entry(Items.SWEET_BERRIES, 3, 1, 3), entry(Items.COCOA_BEANS, 2, 1, 2), entry(Items.BAMBOO, 2, 1, 2), entry(Items.CACTUS, 2, 1, 1));
            table(out, "clinic", 2, 4,
                    entry(Items.GLASS_BOTTLE, 8, 1, 3), entry(Items.PAPER, 6, 1, 3), entry(Items.SUGAR, 5, 1, 3), entry(Items.GLISTERING_MELON_SLICE, 2, 1, 1),
                    entry(Items.GOLDEN_APPLE, 1, 1, 1), entry(Items.SPIDER_EYE, 4, 1, 2), entry(Items.WHITE_WOOL, 5, 1, 2), entry(Items.HONEY_BOTTLE, 3, 1, 1));
            table(out, "guard", 2, 4,
                    entry(Items.ARROW, 8, 3, 8), entry(Items.IRON_SWORD, 2, 1, 1), entry(Items.SHIELD, 3, 1, 1), entry(Items.BOW, 3, 1, 1),
                    entry(Items.IRON_INGOT, 6, 1, 3), entry(Items.COOKED_BEEF, 8, 1, 3), entry(Items.GUNPOWDER, 3, 1, 2), entry(Items.SADDLE, 1, 1, 1));
            table(out, "warehouse", 3, 6,
                    entry(Items.OAK_PLANKS, 10, 4, 8), entry(Items.OAK_LOG, 8, 2, 6), entry(Items.SPRUCE_LOG, 6, 2, 6), entry(Items.SAND, 6, 3, 8),
                    entry(Items.GRAVEL, 6, 3, 8), entry(Items.CLAY_BALL, 5, 3, 8), entry(Items.GLASS, 5, 2, 6), entry(Items.RAIL, 5, 3, 8),
                    entry(Items.MINECART, 2, 1, 1), entry(Items.HOPPER, 2, 1, 1), entry(Items.REDSTONE, 4, 2, 6), entry(Items.PAPER, 4, 2, 6));
            table(out, "factory", 3, 5,
                    entry(ModItems.SCRAP_METAL.get(), 12, 2, 6), entry(Items.COAL, 10, 3, 8), entry(Items.IRON_NUGGET, 8, 2, 6), entry(Items.IRON_INGOT, 4, 1, 3),
                    entry(Items.COPPER_INGOT, 6, 1, 4), entry(Items.REDSTONE, 5, 2, 5), entry(Items.PISTON, 2, 1, 1), entry(Items.BUCKET, 3, 1, 1), entry(ModItems.SMOKE_SOAKED_FILTER.get(), 2, 1, 2));
            table(out, "scrap", 2, 4,
                    entry(ModItems.SCRAP_METAL.get(), 12, 2, 5), entry(Items.IRON_NUGGET, 8, 1, 4), entry(Items.STRING, 6, 1, 3), entry(Items.ROTTEN_FLESH, 5, 1, 3),
                    entry(Items.BONE, 5, 1, 3), entry(Items.FLINT, 6, 1, 3), entry(Items.LEATHER, 3, 1, 2), entry(Items.NAME_TAG, 1, 1, 1));
            // Low-value fishery supplies are optional shoreline discoveries, not high-tier dungeon rewards.
            table(out, "canal_supply", 1, 3,
                    entry(Items.STRING, 12, 1, 3), entry(Items.STICK, 12, 1, 4),
                    entry(Items.COD, 8, 1, 2), entry(Items.SALMON, 8, 1, 2),
                    entry(Items.KELP, 7, 1, 3), entry(Items.BONE, 6, 1, 2),
                    entry(Items.INK_SAC, 5, 1, 2), entry(Items.FLINT, 5, 1, 2),
                    entry(Items.FISHING_ROD, 2, 1, 1));
            table(out, "sewer_cache", 2, 5,
                    entry(Items.TORCH, 10, 2, 6), entry(Items.IRON_NUGGET, 8, 1, 4),
                    entry(ModItems.SCRAP_METAL.get(), 12, 1, 5), entry(Items.COPPER_INGOT, 5, 1, 3),
                    entry(Items.BREAD, 5, 1, 2), entry(Items.BUCKET, 2, 1, 1),
                    entry(Items.STRING, 6, 1, 3), entry(ModItems.SMOKE_SOAKED_FILTER.get(), 3, 1, 2));
            // Pass 22 dungeon loot: supplies -> filters -> risky vault rewards.
            table(out, "sewer_maintenance", 2, 4,
                    entry(Items.TORCH, 12, 2, 6), entry(Items.IRON_NUGGET, 10, 2, 6),
                    entry(ModItems.SCRAP_METAL.get(), 12, 2, 6), entry(Items.COPPER_INGOT, 8, 1, 3),
                    entry(Items.REDSTONE, 6, 1, 4), entry(Items.BUCKET, 4, 1, 1),
                    entry(Items.RAIL, 4, 3, 8), entry(Items.BREAD, 5, 1, 3),
                    entry(Items.LADDER, 5, 2, 5), entry(Items.IRON_PICKAXE, 1, 1, 1));
            table(out, "sewer_filters", 2, 4,
                    entry(ModItems.SMOKE_SOAKED_FILTER.get(), 12, 1, 3),
                    entry(ModItems.SMOKE_RESIDUE.get(), 4, 1, 2),
                    entry(ModItems.SCRAP_METAL.get(), 8, 2, 5),
                    entry(Items.GLASS_BOTTLE, 6, 1, 4), entry(Items.IRON_INGOT, 5, 1, 2),
                    entry(Items.CHARCOAL, 8, 2, 5), entry(Items.CLAY_BALL, 6, 2, 6),
                    entry(Items.REDSTONE, 6, 1, 4), entry(Items.SLIME_BALL, 5, 1, 3));
            table(out, "sewer_vault", 3, 5,
                    entry(Items.IRON_INGOT, 9, 2, 5), entry(Items.GOLD_INGOT, 6, 1, 3),
                    entry(Items.REDSTONE, 9, 3, 8), entry(Items.EMERALD, 3, 1, 2),
                    entry(Items.DIAMOND, 1, 1, 1), entry(Items.NAME_TAG, 3, 1, 1),
                    entry(Items.GOLDEN_APPLE, 1, 1, 1), entry(Items.IRON_PICKAXE, 3, 1, 1),
                    entry(ModItems.SMOKE_SOAKED_FILTER.get(), 8, 1, 3),
                    entry(ModItems.SMOKE_RESIDUE.get(), 5, 1, 2),
                    entry(ModItems.SCRAP_METAL.get(), 8, 3, 7));
            table(out, "nursery", 3, 5,
                    entry(Items.OAK_SAPLING, 6, 1, 3), entry(Items.BIRCH_SAPLING, 5, 1, 3), entry(Items.SPRUCE_SAPLING, 5, 1, 3), entry(Items.JUNGLE_SAPLING, 3, 1, 2),
                    entry(Items.ACACIA_SAPLING, 3, 1, 2), entry(Items.DARK_OAK_SAPLING, 3, 1, 4), entry(Items.WHEAT_SEEDS, 8, 2, 5), entry(Items.BEETROOT_SEEDS, 6, 2, 4),
                    entry(Items.BONE_MEAL, 6, 2, 5), entry(Items.DIRT, 6, 3, 8), entry(Items.APPLE, 4, 1, 2), entry(Items.HONEYCOMB, 2, 1, 2));
            table(out, "hungry_bug", 4, 7,
                    entry(Items.BREAD, 10, 2, 4), entry(Items.COOKED_PORKCHOP, 8, 1, 3), entry(Items.COOKED_CHICKEN, 8, 1, 3), entry(Items.COOKED_BEEF, 6, 1, 3),
                    entry(Items.BAKED_POTATO, 8, 2, 4), entry(Items.WHEAT, 8, 2, 5), entry(Items.CARROT, 6, 2, 4), entry(Items.EGG, 4, 1, 3));
            table(out, "crypt", 2, 4,
                    entry(Items.BONE, 10, 2, 5), entry(Items.ROTTEN_FLESH, 8, 2, 4), entry(Items.GOLD_NUGGET, 6, 1, 5), entry(Items.CANDLE, 5, 1, 2),
                    entry(Items.GOLD_INGOT, 2, 1, 1), entry(Items.MUSIC_DISC_13, 1, 1, 1), entry(Items.EMERALD, 2, 1, 1),
                    entry(ModItems.DEVIL_SIGIL.get(), 1, 1, 1));
            table(out, "hospital", 3, 6,
                    entry(Items.GLASS_BOTTLE, 8, 2, 4), entry(Items.GOLDEN_APPLE, 3, 1, 1), entry(Items.GLISTERING_MELON_SLICE, 4, 1, 2), entry(Items.GOLDEN_CARROT, 4, 1, 2),
                    entry(Items.PAPER, 6, 2, 5), entry(Items.BREWING_STAND, 2, 1, 1), entry(Items.TOTEM_OF_UNDYING, 1, 1, 1));
            table(out, "department", 3, 6,
                    entry(Items.LEATHER_CHESTPLATE, 5, 1, 1), entry(Items.IRON_HELMET, 2, 1, 1), entry(Items.CLOCK, 3, 1, 1), entry(Items.COMPASS, 3, 1, 1),
                    entry(Items.MAP, 3, 1, 1), entry(Items.SPYGLASS, 2, 1, 1), entry(Items.DIAMOND, 1, 1, 1), entry(Items.EMERALD, 4, 1, 3),
                    entry(Items.BOOK, 5, 1, 3), entry(Items.WHITE_BANNER, 3, 1, 1), entry(Items.TOTEM_OF_UNDYING, 1, 1, 1), entry(Items.MUSIC_DISC_CAT, 1, 1, 1));
            table(out, "lab", 3, 5,
                    entry(Items.GLASS_BOTTLE, 8, 1, 3), entry(Items.BLAZE_POWDER, 2, 1, 1), entry(Items.NETHER_WART, 3, 1, 2), entry(Items.SPIDER_EYE, 5, 1, 2),
                    entry(Items.FERMENTED_SPIDER_EYE, 3, 1, 1), entry(Items.ENDER_PEARL, 1, 1, 1), entry(Items.GHAST_TEAR, 1, 1, 1), entry(Items.BOOK, 5, 1, 3),
                    entry(ModItems.SMOKE_RESIDUE.get(), 7, 1, 3), entry(ModItems.SMOKE_SOAKED_FILTER.get(), 9, 1, 3),
                    entry(ModItems.DEVIL_SIGIL.get(), 1, 1, 1));
        }

        @SafeVarargs
        private static void table(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out, String name, int minRolls, int maxRolls,
                                  LootPoolSingletonContainer.Builder<?>... entries) {
            LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(minRolls, maxRolls));
            for (LootPoolSingletonContainer.Builder<?> entry : entries) {
                pool.add(entry);
            }
            out.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/" + name)),
                    LootTable.lootTable().withPool(pool));
        }
    }
}
