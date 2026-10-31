package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Places the world spawn and detects worlds that are not the Hole. A world created without this mod keeps its original
 * generator in level.dat, so nothing can convert it silently. It is reported instead.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class WorldGuard {
    private WorldGuard() {}

    /** Replaces vanilla's biome-based spawn search, which has nothing to find in a city. */
    @SubscribeEvent
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.getChunkSource().getGenerator() instanceof HoleChunkGenerator generator) {
            BlockPos spawn = generator.plan(level).spawnPoint();
            event.getSettings().setSpawn(spawn, 0.0F);
            event.setCanceled(true);
            Dorohedoro.LOGGER.info("The Hole: world spawn set to {} on seed {}", spawn.toShortString(), level.getSeed());
        }
    }

    public static boolean isHoleWorld(ServerLevel overworld) {
        return overworld.getChunkSource().getGenerator() instanceof HoleChunkGenerator;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (isHoleWorld(overworld)) {
            Dorohedoro.LOGGER.info("The Hole: Overworld generator verified as {}", overworld.getChunkSource().getGenerator().getClass().getSimpleName());
        } else {
            Dorohedoro.LOGGER.warn("The Hole: this world does not use the Hole generator. It was left unchanged. Create a new world for the city.");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !isHoleWorld(player.getServer().overworld())) {
            player.sendSystemMessage(Component.translatable("dorohedoro.world.not_hole").withStyle(ChatFormatting.GOLD));
        }
    }
}
