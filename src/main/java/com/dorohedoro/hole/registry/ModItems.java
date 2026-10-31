package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dorohedoro.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dorohedoro.MOD_ID);

    /** Salvaged metal. Nine craft into an iron ingot, so scavenging alone can reach iron tools. */
    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.registerSimpleItem("scrap_metal");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("hole", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dorohedoro.hole"))
            .icon(() -> new ItemStack(SCRAP_METAL.get()))
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
