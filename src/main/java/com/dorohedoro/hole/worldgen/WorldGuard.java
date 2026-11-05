package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.Heightmap;
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
            BlockPos spawn = safeSpawn(level, generator.plan(level).spawnPoint());
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

    /**
     * A seed-chosen start can put the planned point inside a wall or building. Search outward for the nearest
     * open-air spot with solid footing and two blocks of headroom, so nobody spawns inside a block.
     */
    static BlockPos safeSpawn(ServerLevel level, BlockPos planned) {
        if (standable(level, planned)) {
            return planned;
        }
        for (int r = 1; r <= 32; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int dy = 0; dy <= 8; dy++) {
                        for (int sign = 1; sign >= -1; sign -= 2) {
                            BlockPos feet = new BlockPos(planned.getX() + dx, planned.getY() + dy * sign, planned.getZ() + dz);
                            if (standable(level, feet)) {
                                return feet;
                            }
                        }
                    }
                }
            }
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, planned.getX(), planned.getZ());
        Dorohedoro.LOGGER.warn("The Hole: no open spawn near {}, using the column top at y={}", planned.toShortString(), top);
        return new BlockPos(planned.getX(), top, planned.getZ());
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos head = feet.above();
        BlockPos floor = feet.below();
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                && level.getFluidState(feet).isEmpty()
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.canSeeSky(head);
    }
}

