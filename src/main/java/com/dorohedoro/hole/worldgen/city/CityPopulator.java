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
        if (plan.settings.sewerCavernVersion() >= 4) {
            spawnStarterCanalSchool(region, plan, chunk);
        }
        for (Lot lot : plan.lotsIn(chunk)) {
            Rect r = lot.rect();
            if (r.centerX() >> 4 != chunk.x || r.centerZ() >> 4 != chunk.z) {
                continue;
            }
            switch (lot.type()) {
                case LIVESTOCK_PEN -> {
                    EntityType<?> first = LIVESTOCK[CityHash.range(lot.hash(), LIVESTOCK.length)];
                    EntityType<?> second = LIVESTOCK[CityHash.range(lot.hash() >>> 16, LIVESTOCK.length)];
                    spawn(region, plan, first, r.centerX() - 1, r.centerZ());
                    spawn(region, plan, first, r.centerX() + 1, r.centerZ());
                    spawn(region, plan, second, r.centerX(), r.centerZ() - 1);
                    spawn(region, plan, second, r.centerX(), r.centerZ() + 1);
                }
                // Boarded or gutted stores no longer receive a cheerful shopkeeper.
                case SHOP -> {
                    if (plan.settings.worldDetailVersion() == 0
                            || WorldDecayPolicy.severity(lot.hash(), lot.district()) < 2) {
                        spawn(region, plan, EntityType.VILLAGER, r.centerX(), r.centerZ());
                    }
                }
                case CLINIC, MARKET_STALLS, BAZAAR, STARTER_SHELTER, HUNGRY_BUG -> spawn(region, plan, EntityType.VILLAGER, r.centerX(), r.centerZ());
                case GUARD_POST -> spawn(region, plan, EntityType.IRON_GOLEM, r.centerX(), r.centerZ());
                case GREENHOUSE -> spawn(region, plan, EntityType.BEE, r.centerX(), r.centerZ() - 1);
                case TENEMENT -> {
                    if (CityHash.range(lot.hash() >>> 40, 6) == 0) {
                        spawn(region, plan, EntityType.CAT, r.centerX(), r.centerZ());
                    }
                }
                default -> {
                }
            }
        }
    }

    /**
     * Bounded, seed-deterministic seed populations: roughly one in five eligible
     * river/canal chunks receives 1-2 persistent salmon (occasional cod). Vanilla natural
     * water-ambient spawning does the long-term replenishment. Schooling is a bonus,
     * not a dense per-chunk population or an entity spawn in sewer water.
     *
     * Only read from the center chunk; every selected fish is owned by that chunk.
     * Do not touch arbitrary vanilla river biomes or other dimensions.
     */
    static void spawnStarterCanalSchool(WorldGenRegion region, CityPlan plan, ChunkPos chunk) {
        long sample = CityHash.of(plan.seed, 0xF15CA24L, chunk.x, chunk.z);
        if (CityHash.range(sample, 5) != 0) return;
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        int x = minX + 8;
        int z = minZ + 8;
        boolean eligible = false;
        for (Line line : plan.xLinesNear(minX, chunk.getMaxBlockX())) {
            if (line.kind() != Line.Kind.CANAL) continue;
            int target = line.center() + (plan.settings.sewerCavernVersion() >= 5
                    ? RiverChannels24.centerOffset(plan.seed, line, true, z) : 0);
            int candidate = Math.max(minX + 1, Math.min(chunk.getMaxBlockX() - 1, target));
            if (Math.abs(candidate - target) <= 4 && (plan.settings.sewerCavernVersion() < 5
                    || RiverChannels24.water(plan.seed, line, true, candidate, z))) {
                x = candidate;
                eligible = true;
                break;
            }
        }
        if (!eligible) {
            for (Line line : plan.zLinesNear(minZ, chunk.getMaxBlockZ())) {
                if (line.kind() != Line.Kind.CANAL) continue;
                int target = line.center() + (plan.settings.sewerCavernVersion() >= 5
                        ? RiverChannels24.centerOffset(plan.seed, line, false, x) : 0);
                int candidate = Math.max(minZ + 1, Math.min(chunk.getMaxBlockZ() - 1, target));
                if (Math.abs(candidate - target) <= 4 && (plan.settings.sewerCavernVersion() < 5
                        || RiverChannels24.water(plan.seed, line, false, x, candidate))) {
                    z = candidate;
                    eligible = true;
                    break;
                }
            }
        }
        if (!eligible) return;
        int waterY = 59 + (plan.settings.terrainRelief()
                ? CityElevation.offset(plan.seed, x, z) : 0);
        // Verify actual water in the generated world, avoiding dry blocks and the
        // culvert bulkhead / canal bank. Under bridges, fish still have water.
        if (!region.getFluidState(new BlockPos(x, waterY, z)).is(net.minecraft.tags.FluidTags.WATER)) return;
        int count = 1 + CityHash.range(sample >>> 20, 2);
        for (int i = 0; i < count; i++) {
            int px = x + (i == 0 ? 0 : 1);
            int pz = z;
            if (px > chunk.getMaxBlockX()) px = x - 1;
            BlockPos at = new BlockPos(px, waterY, pz);
            if (!region.getFluidState(at).is(net.minecraft.tags.FluidTags.WATER)) continue;
            EntityType<?> fish = (CityHash.range(sample >>> 32, 9) == 0)
                    ? EntityType.COD : EntityType.SALMON;
            if (!(fish.create(region.getLevel()) instanceof Mob mob)) continue;
            mob.moveTo(px + 0.5, waterY + 0.2, pz + 0.5, 0.0F, 0.0F);
            // Do not make naturally replenished canal fish immortal in new worlds:
            // an explored city can contain thousands of canal chunks. Persistent
            // pre-v19 schools remain unchanged for existing saves.
            if (CityEcologyPolicy32.keepStarterFishPersistent(plan.settings.worldDetailVersion()))
                mob.setPersistenceRequired();
            EventHooks.finalizeMobSpawn(mob, region, region.getCurrentDifficultyAt(at),
                    MobSpawnType.CHUNK_GENERATION, null);
            region.addFreshEntityWithPassengers(mob);
        }
    }

    private static void spawn(WorldGenRegion region, CityPlan plan, EntityType<?> type, int x, int z) {
        int groundY = GROUND_Y + (plan.settings.terrainRelief()
                ? CityElevation.offset(region.getSeed(), x, z) : 0);
        BlockPos pos = new BlockPos(x, groundY, z);
        if (!region.getBlockState(pos).isAir() || !region.getBlockState(pos.above()).isAir()) {
            return;
        }
        if (!(type.create(region.getLevel()) instanceof Mob mob)) {
            return;
        }
        mob.moveTo(x + 0.5, groundY, z + 0.5, 0.0F, 0.0F);
        mob.setPersistenceRequired();
        EventHooks.finalizeMobSpawn(mob, region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        region.addFreshEntityWithPassengers(mob);
    }
}
