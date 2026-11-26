package com.dorohedoro.hole.client.ui;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * One gray line under a Hole item saying what it is for, from the {@code item.dorohedoro.<id>.use} lang key. Brewing
 * and fuel uses never appear in the recipe book, so without this a player has no way to find them.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID, value = Dist.CLIENT)
public final class ItemUseTooltips {
    private ItemUseTooltips() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!Dorohedoro.MOD_ID.equals(id.getNamespace())) return;
        String key = "item." + Dorohedoro.MOD_ID + "." + id.getPath() + ".use";
        if (I18n.exists(key)) {
            event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }
}
