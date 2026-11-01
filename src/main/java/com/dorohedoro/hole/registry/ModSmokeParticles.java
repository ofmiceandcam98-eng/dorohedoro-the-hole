package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original, animated particle sprites for biological black smoke and material aftermaths. */
public final class ModSmokeParticles {
    private ModSmokeParticles() {}
    private static final DeferredRegister<ParticleType<?>> TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Dorohedoro.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOOT_PLUME =
            TYPES.register("soot_plume", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE_FILAMENT =
            TYPES.register("smoke_filament", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HEALING_FIBER =
            TYPES.register("healing_fiber", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FUNGAL_SPORE =
            TYPES.register("fungal_spore", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) { TYPES.register(bus); }
}
