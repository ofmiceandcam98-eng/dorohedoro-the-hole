package com.dorohedoro.hole.hazard;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's status effects. */
public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Dorohedoro.MOD_ID);

    public static final DeferredHolder<MobEffect, HoleToxinEffect> HOLE_TOXIN = EFFECTS.register("hole_toxin", HoleToxinEffect::new);

    private ModEffects() {}

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
