package com.dorohedoro.hole.client;

import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/**
 * Tints for the hazards built on vanilla textures, so they look like Mojang variants: Ash-Snow is vanilla snow grayed
 * by soot, Sludge-Ice is vanilla ice stained with canal sludge, and the ash snowball is the vanilla snowball grayed the
 * same way. Only tint index 0 is tinted; overlays keep their own colors. Breaking particles pick the tint up too.
 */
public final class HazardColors {
    /** Warm ash gray, multiplied over vanilla's near-white snow. */
    public static final int ASH = 0xACABA5;
    /** Olive-brown, multiplied over vanilla's blue ice. */
    public static final int SLUDGE = 0xC09848;

    private HazardColors() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(HazardColors::blocks);
        modBus.addListener(HazardColors::items);
    }

    private static void blocks(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> tint == 0 ? ASH : -1, ModBlocks.ASH_SNOW.get(), ModBlocks.ASH_SNOW_BLOCK.get());
        event.register((state, level, pos, tint) -> tint == 0 ? SLUDGE : -1, ModBlocks.SLUDGE_ICE.get());
    }

    private static void items(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? 0xFF000000 | ASH : -1,
                ModBlocks.ASH_SNOW.get(), ModBlocks.ASH_SNOW_BLOCK.get(), ModItems.ASH_SNOWBALL.get());
        event.register((stack, tint) -> tint == 0 ? 0xFF000000 | SLUDGE : -1, ModBlocks.SLUDGE_ICE.get());
    }
}
