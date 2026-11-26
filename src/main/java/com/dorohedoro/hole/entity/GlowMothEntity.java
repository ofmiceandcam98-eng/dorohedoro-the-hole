package com.dorohedoro.hole.entity;

import com.dorohedoro.hole.registry.ModItems;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.world.light.GlowMothLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Peaceful flying cavern insect. Uses vanilla bat flight but original model and drops. */
public final class GlowMothEntity extends Bat {
    private static final int SHEAR_COOLDOWN = 6000; // Five minutes: farmable without slaughter.
    private int scaleCooldown;

    public GlowMothEntity(EntityType<? extends GlowMothEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && scaleCooldown > 0) scaleCooldown--;
        // 0.5-second cadence: at most a handful of temporary vanilla light blocks per flying moth.
        if (!level().isClientSide && tickCount % 10 == 0 && level() instanceof ServerLevel server) {
            BlockPos pos = blockPosition();
            if (server.isLoaded(pos) && server.getBlockState(pos).isAir()) {
                server.setBlock(pos, ModBlocks.GLOW_MOTH_LIGHT.get().defaultBlockState(), 3);
                server.scheduleTick(pos, ModBlocks.GLOW_MOTH_LIGHT.get(), GlowMothLightBlock.EXPIRE_TICKS);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.SHEARS)) return super.mobInteract(player, hand);
        if (scaleCooldown > 0) return InteractionResult.PASS;
        if (!level().isClientSide) {
            scaleCooldown = SHEAR_COOLDOWN;
            ItemStack scales = new ItemStack(ModItems.LUMINOUS_SCALE.get(), 1 + random.nextInt(2));
            if (!player.addItem(scales)) spawnAtLocation(scales);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MothScaleCooldown", scaleCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        scaleCooldown = Math.max(0, Math.min(SHEAR_COOLDOWN, tag.getInt("MothScaleCooldown")));
    }

    // Sounds: vanilla events that fit the creature, instead of the generic hostile grunt.
    @Override public net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return net.minecraft.sounds.SoundEvents.BAT_HURT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return net.minecraft.sounds.SoundEvents.BAT_DEATH; }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 1.5F; }
    @Override protected float getSoundVolume() { return 0.4F; }
}
