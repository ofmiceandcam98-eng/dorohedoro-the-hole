package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Adds a chunk's fixed residents. A lot populates only from the chunk holding its center, so it happens exactly once,
 * and counts are small and bounded so a dense district cannot overload entity ticking.
 */
public final class CityPopulator {
    private static final EntityType<?>[] LIVESTOCK = {EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN};

    private CityPopulator() {}

    public static void populate(WorldGenRegion region, CityPlan plan) {
        ChunkPos chunk = region.getCenter();
        for (Lot lot : plan.lotsIn(chunk)) {
            Rect r = lot.rect();
            if (r.centerX() >> 4 != chunk.x || r.centerZ() >> 4 != chunk.z) {
                continue;
            }
            switch (lot.type()) {
                case LIVESTOCK_PEN -> {
                    EntityType<?> first = LIVESTOCK[CityHash.range(lot.hash(), LIVESTOCK.length)];
                    EntityType<?> second = LIVESTOCK[CityHash.range(lot.hash() >>> 16, LIVESTOCK.length)];
                    spawn(region, first, r.centerX() - 1, r.centerZ());
                    spawn(region, first, r.centerX() + 1, r.centerZ());
                    spawn(region, second, r.centerX(), r.centerZ() - 1);
                    spawn(region, second, r.centerX(), r.centerZ() + 1);
                }
                case SHOP, CLINIC, MARKET_STALLS, STARTER_SHELTER, HUNGRY_BUG -> spawn(region, EntityType.VILLAGER, r.centerX(), r.centerZ());
                case GUARD_POST -> spawn(region, EntityType.IRON_GOLEM, r.centerX(), r.centerZ());
                case GREENHOUSE -> spawn(region, EntityType.BEE, r.centerX(), r.centerZ() - 1);
                case TENEMENT -> {
                    if (CityHash.range(lot.hash() >>> 40, 6) == 0) {
                        spawn(region, EntityType.CAT, r.centerX(), r.centerZ());
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void spawn(WorldGenRegion region, EntityType<?> type, int x, int z) {
        BlockPos pos = new BlockPos(x, GROUND_Y, z);
        if (!region.getBlockState(pos).isAir() || !region.getBlockState(pos.above()).isAir()) {
            return;
        }
        if (!(type.create(region.getLevel()) instanceof Mob mob)) {
            return;
        }
        mob.moveTo(x + 0.5, GROUND_Y, z + 0.5, 0.0F, 0.0F);
        mob.setPersistenceRequired();
        EventHooks.finalizeMobSpawn(mob, region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        region.addFreshEntityWithPassengers(mob);
    }
}
