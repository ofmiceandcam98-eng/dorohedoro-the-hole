package com.dorohedoro.hole.hazard;

import com.dorohedoro.hole.registry.ModEntities;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A packed ball of Ash-Snow. Thrown like a snowball, it does no damage, but the soot in it slows whatever it hits for
 * three seconds, and a player it hits takes on Hole Toxin exposure. Dispensers throw it too.
 */
public final class AshSnowball extends ThrowableItemProjectile {
    public AshSnowball(EntityType<? extends AshSnowball> type, Level level) {
        super(type, level);
    }

    public AshSnowball(Level level, LivingEntity thrower) {
        super(ModEntities.ASH_SNOWBALL.get(), thrower, level);
    }

    public AshSnowball(Level level, double x, double y, double z) {
        super(ModEntities.ASH_SNOWBALL.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ASH_SNOWBALL.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particle = new ItemParticleOption(ParticleTypes.ITEM, getItem());
            for (int i = 0; i < 8; i++) level().addParticle(particle, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            for (int i = 0; i < 4; i++) level().addParticle(ParticleTypes.ASH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Zero damage still knocks back and counts as a hit, like a vanilla snowball.
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), 0.0F);
        if (result.getEntity() instanceof LivingEntity living && !level().isClientSide) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0), getEffectSource());
            if (living instanceof ServerPlayer player) ToxinExposure.add(player, 120, "ash_snowball");
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    /** The ash snowball item: thrown by hand or by a dispenser. */
    public static final class ItemType extends Item implements ProjectileItem {
        public ItemType(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                    0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide) {
                AshSnowball ball = new AshSnowball(level, player);
                ball.setItem(stack);
                ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(ball);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
            AshSnowball ball = new AshSnowball(level, pos.x(), pos.y(), pos.z());
            ball.setItem(stack);
            return ball;
        }
    }
}
