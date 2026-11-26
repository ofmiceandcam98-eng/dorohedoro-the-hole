package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.faction.FactionStanding;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hole resident standing shifts villager prices, after vanilla has applied its own gossip and Hero of the Village
 * discounts. Vanilla clears these special prices when trading ends, so the shift never sticks to an offer. Villagers
 * added by other mods that extend {@link Villager} get the same treatment.
 */
@Mixin(Villager.class)
abstract class VillagerPricesMixin {
    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void dorohedoro$standingPrices(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        float shift = FactionStanding.priceShift(FactionStanding.of(serverPlayer).residents());
        if (shift == 0.0F) return;
        for (MerchantOffer offer : ((Villager) (Object) this).getOffers()) {
            int base = offer.getBaseCostA().getCount();
            int delta = Math.round(base * shift);
            // A markup always costs at least one more emerald; a discount on a one-emerald trade stays at one.
            if (delta == 0 && shift > 0) delta = 1;
            offer.addToSpecialPriceDiff(delta);
        }
    }
}
