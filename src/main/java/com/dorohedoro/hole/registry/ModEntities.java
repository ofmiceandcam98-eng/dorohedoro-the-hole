package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleBiomes;
import com.dorohedoro.hole.entity.ScrapRatEntity;
import com.dorohedoro.hole.entity.SewerLeechEntity;
import com.dorohedoro.hole.entity.GlowMothEntity;
import com.dorohedoro.hole.entity.SludgeCrawlerEntity;
import com.dorohedoro.hole.entity.RustEaterBeetleEntity;
import com.dorohedoro.hole.entity.CavernBurrowerEntity;
import com.dorohedoro.hole.entity.SporeCrawlerEntity;
import com.dorohedoro.hole.entity.SewerRoachEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native 1.21.1 NeoForge entity types and their server-side spawn constraints. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Dorohedoro.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SewerRoachEntity>> SEWER_ROACH = ENTITIES.register(
            "sewer_roach", () -> EntityType.Builder.of(SewerRoachEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 0.52F).clientTrackingRange(8).build("dorohedoro:sewer_roach"));

    public static final DeferredHolder<EntityType<?>, EntityType<ScrapRatEntity>> SCRAP_RAT = ENTITIES.register(
            "scrap_rat", () -> EntityType.Builder.of(ScrapRatEntity::new, MobCategory.AMBIENT)
                    .sized(0.45F, 0.42F).clientTrackingRange(8).build("dorohedoro:scrap_rat"));

    public static final DeferredHolder<EntityType<?>, EntityType<SewerLeechEntity>> SEWER_LEECH = ENTITIES.register(
            "sewer_leech", () -> EntityType.Builder.of(SewerLeechEntity::new, MobCategory.MONSTER)
                    .sized(0.88F, 0.62F).clientTrackingRange(8).build("dorohedoro:sewer_leech"));
    public static final DeferredHolder<EntityType<?>, EntityType<GlowMothEntity>> GLOW_MOTH = ENTITIES.register(
            "glow_moth", () -> EntityType.Builder.of(GlowMothEntity::new, MobCategory.AMBIENT)
                    .sized(0.65F, 0.45F).clientTrackingRange(8).build("dorohedoro:glow_moth"));

    public static final DeferredHolder<EntityType<?>, EntityType<SludgeCrawlerEntity>> SLUDGE_CRAWLER = ENTITIES.register(
            "sludge_crawler", () -> EntityType.Builder.of(SludgeCrawlerEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.63F).clientTrackingRange(8).build("dorohedoro:sludge_crawler"));

    public static final DeferredHolder<EntityType<?>, EntityType<RustEaterBeetleEntity>> RUST_EATER_BEETLE = ENTITIES.register(
            "rust_eater_beetle", () -> EntityType.Builder.of(RustEaterBeetleEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.5F).clientTrackingRange(8).build("dorohedoro:rust_eater_beetle"));

    public static final DeferredHolder<EntityType<?>, EntityType<CavernBurrowerEntity>> CAVERN_BURROWER = ENTITIES.register(
            "cavern_burrower", () -> EntityType.Builder.of(CavernBurrowerEntity::new, MobCategory.MONSTER)
                    .sized(1.15F, 0.9F).clientTrackingRange(8).build("dorohedoro:cavern_burrower"));

    public static final DeferredHolder<EntityType<?>, EntityType<SporeCrawlerEntity>> SPORE_CRAWLER = ENTITIES.register(
            "spore_crawler", () -> EntityType.Builder.of(SporeCrawlerEntity::new, MobCategory.MONSTER)
                    .sized(0.83F, 0.55F).clientTrackingRange(8).build("dorohedoro:spore_crawler"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(ModEntities::attributes);
        modBus.addListener(ModEntities::spawns);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(SEWER_ROACH.get(), SewerRoachEntity.attributes().build());
        event.put(SCRAP_RAT.get(), ScrapRatEntity.attributes().build());
        event.put(SEWER_LEECH.get(), SewerLeechEntity.attributes().build());
        event.put(GLOW_MOTH.get(), Bat.createAttributes().build());
        event.put(SLUDGE_CRAWLER.get(), SludgeCrawlerEntity.attributes().build());
        event.put(RUST_EATER_BEETLE.get(), RustEaterBeetleEntity.attributes().build());
        event.put(CAVERN_BURROWER.get(), CavernBurrowerEntity.attributes().build());
        event.put(SPORE_CRAWLER.get(), SporeCrawlerEntity.attributes().build());
    }

    private static void spawns(RegisterSpawnPlacementsEvent event) {
        // Keep naturally spawned vanilla fish in open canal water when the biome is
        // one of the Hole's districts. Other dimensions and vanilla biomes retain
        // completely unchanged fish spawning. Do not create fish in sewer runoff.
        event.register(EntityType.SALMON, null, null,
                (type, level, reason, pos, random) -> canalFishHabitat(level, pos),
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(EntityType.COD, null, null,
                (type, level, reason, pos, random) -> canalFishHabitat(level, pos),
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(SEWER_ROACH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.INDUSTRIAL, HoleBiomes.DERELICT, HoleBiomes.MARKET, HoleBiomes.RESIDENTIAL, HoleBiomes.WATERFRONT, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA, HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM)) &&
                        net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SCRAP_RAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.RESIDENTIAL, HoleBiomes.MARKET, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT, HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA, HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM)) &&
                        Mob.checkMobSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SEWER_LEECH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.WATERFRONT)) && nextToWater(level, pos) &&
                        net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GLOW_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> ((pos.getY() < 55 && pos.getY() > -58) ||
                         surface(level, pos, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT, HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD, HoleBiomes.OUTSKIRTS)) &&
                        level.isEmptyBlock(pos) && level.getRawBrightness(pos, 0) <= 7,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SLUDGE_CRAWLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.INDUSTRIAL, HoleBiomes.WATERFRONT, HoleBiomes.FROSTBITTEN_INDUSTRIAL, HoleBiomes.SNOWFIELDS, HoleBiomes.DEAD_TAIGA, HoleBiomes.DUST_BASIN, HoleBiomes.IRON_BADLANDS, HoleBiomes.MOUNTAIN_RIM)) && net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(RUST_EATER_BEETLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> rustBeetleHabitat(level, pos) &&
                        Mob.checkMobSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CAVERN_BURROWER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.DERELICT, HoleBiomes.OUTSKIRTS)) && net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SPORE_CRAWLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> (underground(pos) || surface(level, pos, HoleBiomes.DERELICT, HoleBiomes.GRAVEYARD)) && net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Additional habitat restriction ONLY for Hole biomes. Vanilla fish rules still apply. */
    private static boolean canalFishHabitat(net.minecraft.world.level.ServerLevelAccessor level, BlockPos pos) {
        if (!isHoleCityBiome(level, pos)) return true;
        // Natural canal water is Y58..61 plus a 0..11 district terrain offset.
        // This excludes the sewer runnel below the masonry ceiling.
        return pos.getY() >= 58 && pos.getY() <= 74
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getBlockState(pos.above(4)).isAir();
    }

    /**
     * Beetles inhabit underground galleries AND scattered surface scrap around the Hole.
     * The biome gate prevents accidental spawning across vanilla and the other two dimensions.
     * Municipal sewer floors are under Y=55 even when the biome remains a surface district.
     */
    private static boolean rustBeetleHabitat(net.minecraft.world.level.ServerLevelAccessor level, BlockPos pos) {
        if (underground(pos)) return isHoleCityBiome(level, pos);
        if (pos.getY() < 55 || pos.getY() > 188) return false;
        // Avoid fluids and unsupported locations, including standing in polluted water.
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.below()).isEmpty()) return false;
        return level.getBiome(pos).is(HoleBiomes.INDUSTRIAL)
                || level.getBiome(pos).is(HoleBiomes.DERELICT)
                || level.getBiome(pos).is(HoleBiomes.OUTSKIRTS)
                || level.getBiome(pos).is(HoleBiomes.DEAD_TAIGA)
                || level.getBiome(pos).is(HoleBiomes.IRON_BADLANDS)
                || level.getBiome(pos).is(HoleBiomes.MOUNTAIN_RIM);
    }

    /** Y=49 municipal sewers do NOT use the undercity biome: the climate source transitions at Y=40. */
    private static boolean isHoleCityBiome(net.minecraft.world.level.ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(HoleBiomes.UNDERCITY)
                || level.getBiome(pos).is(HoleBiomes.RESIDENTIAL)
                || level.getBiome(pos).is(HoleBiomes.MARKET)
                || level.getBiome(pos).is(HoleBiomes.INDUSTRIAL)
                || level.getBiome(pos).is(HoleBiomes.WATERFRONT)
                || level.getBiome(pos).is(HoleBiomes.DERELICT)
                || level.getBiome(pos).is(HoleBiomes.GRAVEYARD)
                || level.getBiome(pos).is(HoleBiomes.OUTSKIRTS)
                || level.getBiome(pos).is(HoleBiomes.FROSTBITTEN_INDUSTRIAL)
                || level.getBiome(pos).is(HoleBiomes.SNOWFIELDS)
                || level.getBiome(pos).is(HoleBiomes.DEAD_TAIGA)
                || level.getBiome(pos).is(HoleBiomes.DUST_BASIN)
                || level.getBiome(pos).is(HoleBiomes.IRON_BADLANDS)
                || level.getBiome(pos).is(HoleBiomes.MOUNTAIN_RIM);
    }

    /** Surface mobs remain local to the Hole, not generic vanilla biomes or the two other realms. */
    @SafeVarargs
    private static boolean surface(net.minecraft.world.level.ServerLevelAccessor level, BlockPos pos,
                                   net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>... biomes) {
        if (pos.getY() < 55 || pos.getY() > 188) return false;
        for (var biome : biomes) {
            if (level.getBiome(pos).is(biome)) return true;
        }
        return false;
    }

    private static boolean nextToWater(net.minecraft.world.level.ServerLevelAccessor level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) return true;
        // Sewers contain shallow runnels; waterfront canals can sit two blocks below a ledge.
        for (int down = 0; down <= 2; down++) {
            BlockPos floor = pos.below(down);
            if (level.getFluidState(floor).is(FluidTags.WATER)) return true;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(floor.relative(direction)).is(FluidTags.WATER)) return true;
                if (level.getFluidState(floor.relative(direction, 2)).is(FluidTags.WATER)) return true;
            }
        }
        return false;
    }

    private static boolean underground(BlockPos pos) {
        return pos.getY() < 55 && pos.getY() > -55;
    }
}
