package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleBiomeSource;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the generator and biome source codecs that world presets refer to. */
public final class ModWorldgen {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Dorohedoro.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, Dorohedoro.MOD_ID);

    public static final Supplier<MapCodec<HoleChunkGenerator>> HOLE_GENERATOR =
            CHUNK_GENERATORS.register("hole", () -> HoleChunkGenerator.CODEC);
    public static final Supplier<MapCodec<HoleBiomeSource>> HOLE_BIOME_SOURCE =
            BIOME_SOURCES.register("hole_districts", () -> HoleBiomeSource.CODEC);

    private ModWorldgen() {}

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
    }
}
