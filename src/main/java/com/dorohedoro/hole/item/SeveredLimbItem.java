package com.dorohedoro.hole.item;

import com.dorohedoro.hole.magic.LimbReattachmentService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Recoverable physical limb. Its unique injury token is in CUSTOM_DATA, not the name. */
public final class SeveredLimbItem extends Item {
    private final int limb;

    public SeveredLimbItem(int limb, Properties properties) {
        super(properties);
        this.limb = limb;
    }

    public int limb() { return limb; }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player)) return InteractionResult.PASS;
        if (user.level().isClientSide) return InteractionResult.SUCCESS;
        if (!(user instanceof ServerPlayer medic) || !(target instanceof ServerPlayer patient)) return InteractionResult.FAIL;
        return LimbReattachmentService.attempt(medic, patient, stack, hand)
            ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
