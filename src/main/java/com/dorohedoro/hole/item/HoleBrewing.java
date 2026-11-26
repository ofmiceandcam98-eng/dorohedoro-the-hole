package com.dorohedoro.hole.item;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * Creature parts from the Hole brew into vanilla potions, so every gland and claw has a use. Each starts from an
 * Awkward Potion like any vanilla ingredient, and the results can be extended, strengthened and splashed as usual.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class HoleBrewing {
    private HoleBrewing() {}

    @SubscribeEvent
    public static void register(RegisterBrewingRecipesEvent event) {
        var b = event.getBuilder();
        b.addMix(Potions.AWKWARD, ModItems.SPORE_SAC.get(), Potions.POISON);
        b.addMix(Potions.AWKWARD, ModItems.SLUDGE_GLAND.get(), Potions.SLOWNESS);
        b.addMix(Potions.AWKWARD, ModItems.CORROSIVE_GLAND.get(), Potions.HARMING);
        b.addMix(Potions.AWKWARD, ModItems.BURROWER_CLAW.get(), Potions.STRENGTH);
        b.addMix(Potions.AWKWARD, ModItems.FUNGAL_MEMBRANE.get(), Potions.SLOW_FALLING);
        b.addMix(Potions.AWKWARD, ModItems.METALLIC_CARAPACE.get(), Potions.TURTLE_MASTER);
    }
}
