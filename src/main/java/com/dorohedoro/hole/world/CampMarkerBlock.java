package com.dorohedoro.hole.world;

import com.dorohedoro.hole.entity.CrossEyesEntity;
import com.dorohedoro.hole.registry.ModBlockEntities;
import com.dorohedoro.hole.registry.ModEntities;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hidden under every Cross-Eye camp the city generates. The city writes blocks only, so the gang itself arrives the first
 * time a player comes within 32 blocks: three or four members appear around the camp fire, tied to the camp, and the
 * marker turns back into the floor block it hides under. It has no item and is never seen.
 */
public final class CampMarkerBlock extends BaseEntityBlock {
    public static final MapCodec<CampMarkerBlock> CODEC = simpleCodec(CampMarkerBlock::new);
    private static final int WAKE_RADIUS = 32;

    public CampMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CAMP_MARKER.get(), CampMarkerBlock::tick);
    }

    private static void tick(Level level, BlockPos pos, BlockState state, Entity marker) {
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 40 != (pos.asLong() & 31)) return;
        Player near = server.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WAKE_RADIUS,
                p -> !p.isSpectator());
        if (near == null) return;
        // The marker sits one block under the camp floor; the gang stands on that floor.
        BlockPos center = pos.above(2);
        int count = 3 + server.getRandom().nextInt(2);
        int spawned = 0;
        for (int i = 0; i < 12 && spawned < count; i++) {
            BlockPos at = center.offset(server.getRandom().nextInt(9) - 4, 0, server.getRandom().nextInt(9) - 4);
            // Stand on a solid floor with two clear blocks above, never inside a wall or the fire.
            BlockPos feet = findFeet(server, at);
            if (feet == null) continue;
            CrossEyesEntity member = ModEntities.CROSS_EYES.get().create(server);
            if (member == null) continue;
            member.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, server.getRandom().nextFloat() * 360F, 0);
            member.finalizeSpawn(server, server.getCurrentDifficultyAt(feet), MobSpawnType.STRUCTURE, null);
            member.setCampHome(center);
            server.addFreshEntityWithPassengers(member);
            spawned++;
        }
        server.setBlock(pos, marker.floor, Block.UPDATE_ALL);
    }

    @Nullable
    private static BlockPos findFeet(ServerLevel level, BlockPos near) {
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos feet = near.above(dy);
            if (level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
                    && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                    && level.getFluidState(feet).isEmpty()) {
                return feet;
            }
        }
        return null;
    }

    /** Remembers the floor block it replaced, so the camp floor is whole again once the gang is out. */
    public static final class Entity extends BlockEntity {
        private BlockState floor = Blocks.DIRT.defaultBlockState();

        public Entity(BlockPos pos, BlockState state) {
            super(ModBlockEntities.CAMP_MARKER.get(), pos, state);
        }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            if (tag.contains("Floor")) {
                floor = net.minecraft.nbt.NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("Floor"));
            }
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            tag.put("Floor", net.minecraft.nbt.NbtUtils.writeBlockState(floor));
        }
    }
}
