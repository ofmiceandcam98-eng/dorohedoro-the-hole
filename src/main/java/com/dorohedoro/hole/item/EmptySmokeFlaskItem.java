package com.dorohedoro.hole.item;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.magic.SmokeEngine;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Used by Sorcerers to capture their OWN smoke. Humans cannot fill an empty flask. */
public final class EmptySmokeFlaskItem extends Item {
    public EmptySmokeFlaskItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel)) return InteractionResultHolder.sidedSuccess(held, true);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.fail(held);
        MagicType type = SmokeEngine.captureSmoke(server);
        if (type == null) return InteractionResultHolder.fail(held);
        ItemStack filled = new ItemStack(ModItems.smokeFlask(type));
        held.shrink(1);
        if (held.isEmpty()) player.setItemInHand(hand, filled);
        else if (!player.getInventory().add(filled)) player.drop(filled, false);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), false);
    }
}
