package com.dorohedoro.hole.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Original underground creature. Distinctive drops have survival crafting applications. */
public final class CavernBurrowerEntity extends Monster {
    public CavernBurrowerEntity(EntityType<? extends CavernBurrowerEntity> type, Level level) { super(type, level); xpReward=4; }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D).add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, .85D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 7.0F));
        targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // Sounds: vanilla events that fit the creature, instead of the generic hostile grunt.
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return net.minecraft.sounds.SoundEvents.HOGLIN_AMBIENT; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return net.minecraft.sounds.SoundEvents.HOGLIN_HURT; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return net.minecraft.sounds.SoundEvents.HOGLIN_DEATH; }
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { playSound(net.minecraft.sounds.SoundEvents.GRAVEL_STEP, 0.3F, 1.0F); }
    @Override public float getVoicePitch() { return super.getVoicePitch() * 1.15F; }
    @Override protected float getSoundVolume() { return 0.7F; }
}
