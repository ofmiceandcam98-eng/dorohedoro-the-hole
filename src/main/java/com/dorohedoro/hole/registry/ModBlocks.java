package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Hole's own blocks. Each drops something useful, so salvage is a real early-game resource. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dorohedoro.MOD_ID);

    /** Heaps of machine scrap. The early-game source of metal, drops scrap and sometimes iron nuggets. */
    public static final DeferredBlock<Block> INDUSTRIAL_DEBRIS = register("industrial_debris", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.5F, 3.0F).sound(SoundType.CHAIN).requiresCorrectToolForDrops()));

    /** Corroded wall sheet used across the industrial districts. Weathered copper does not scavenge into anything useful. */
    public static final DeferredBlock<Block> RUSTED_PLATING = register("rusted_plating", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.5F, 5.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()));

    private ModBlocks() {}

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> holder = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
        return holder;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
