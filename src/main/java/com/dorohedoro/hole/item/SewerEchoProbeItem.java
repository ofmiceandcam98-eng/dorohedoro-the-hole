package com.dorohedoro.hole.item;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Uses roach antennae to reveal nearby threats for 12 seconds; no magical powers granted. */
public final class SewerEchoProbeItem extends Item {
    public SewerEchoProbeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (level instanceof ServerLevel) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(player.blockPosition()).inflate(12.0D),
                    entity -> entity != player && entity.isAlive() && entity.getType().getCategory() == MobCategory.MONSTER);
            int count = 0;
            for (LivingEntity target : targets) {
                if (count++ >= 24) break;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0, false, false, true));
            }
            player.getCooldowns().addCooldown(this, 600);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
