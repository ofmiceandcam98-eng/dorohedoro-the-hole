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

/** One bottled sample, fixed at manufacture. Never changes a player's innate magic. */
public final class FilledSmokeFlaskItem extends Item {
    private final MagicType recordedType;
    public FilledSmokeFlaskItem(MagicType recordedType, Properties properties) {
        super(properties);
        this.recordedType = recordedType;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(held);
        if (!(level instanceof ServerLevel)) return InteractionResultHolder.sidedSuccess(held, true);
        if (!(player instanceof ServerPlayer server) || !SmokeEngine.releaseBottledSmoke(server, recordedType)) {
            return InteractionResultHolder.fail(held);
        }
        // One charge per bottle, regardless of the caster's origin or innate type.
        player.getCooldowns().addCooldown(this, 100);
        held.shrink(1);
        ItemStack empty = new ItemStack(ModItems.EMPTY_SMOKE_FLASK.get());
        if (held.isEmpty()) player.setItemInHand(hand, empty);
        else if (!player.getInventory().add(empty)) player.drop(empty, false);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), false);
    }
}
