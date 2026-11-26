package com.dorohedoro.hole.world.light;

import com.dorohedoro.hole.entity.GlowMothEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Invisible, air-replaceable, short-lived vanilla block-light emitter used by Glow Moths.
 *
 * Never replaces water, terrain, crops, redstone or player blocks. A scheduled server tick removes
 * stray lights shortly after their moth leaves/dies. Only a moth close to the light can extend it.
 * No BlockItem, loot, collision or visible geometry. Block light spreads with Minecraft's usual
 * solid-block occlusion instead of simply making the moth's texture bright.
 */
public final class GlowMothLightBlock extends Block {
    public static final int LIGHT_LEVEL = 12;
    public static final int EXPIRE_TICKS = 14;

    public GlowMothLightBlock(Properties properties) { super(properties); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // State guards protect player edits and other mods from scheduled stale ticks.
        if (!level.getBlockState(pos).is(this)) return;
        boolean mothNearby = !level.getEntitiesOfClass(GlowMothEntity.class,
                new AABB(pos).inflate(1.4D), GlowMothEntity::isAlive).isEmpty();
        if (mothNearby) {
            level.scheduleTick(pos, this, EXPIRE_TICKS);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
