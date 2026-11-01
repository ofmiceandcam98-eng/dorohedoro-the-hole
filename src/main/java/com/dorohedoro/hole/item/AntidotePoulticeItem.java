package com.dorohedoro.hole.item;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Crafted from spore and leech products. Restores survival utility by curing Poison. */
public final class AntidotePoulticeItem extends Item {
    public AntidotePoulticeItem(Properties props) { super(props); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack remaining = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) entity.removeEffect(MobEffects.POISON);
        return remaining;
    }
}
