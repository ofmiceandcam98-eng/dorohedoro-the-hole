package com.dorohedoro.hole.faction;

import com.dorohedoro.hole.entity.CrossEyesEntity;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/** Everything that moves faction standing, and the Hole trades that give emeralds new uses. */
final class FactionEvents {
    private FactionEvents() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(FactionEvents::death);
        NeoForge.EVENT_BUS.addListener(FactionEvents::traded);
        NeoForge.EVENT_BUS.addListener(FactionEvents::hero);
        NeoForge.EVENT_BUS.addListener(FactionEvents::refuse);
        NeoForge.EVENT_BUS.addListener(FactionEvents::villagerTrades);
        NeoForge.EVENT_BUS.addListener(FactionEvents::wandererTrades);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) FactionStanding.sync(p);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) FactionStanding.sync(p);
        });
    }

    private static void death(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof CrossEyesEntity) {
            FactionStanding.changeCrossEyes(player, -10, "killed");
            FactionStanding.changeResidents(player, 3, "cross_eyes_killed");
        } else if (event.getEntity() instanceof WanderingTrader) {
            FactionStanding.changeResidents(player, -10, "trader_killed");
        } else if (event.getEntity() instanceof AbstractVillager) {
            FactionStanding.changeResidents(player, -20, "resident_killed");
        } else if (event.getEntity() instanceof IronGolem golem && !golem.isPlayerCreated()) {
            FactionStanding.changeResidents(player, -10, "golem_killed");
        }
    }

    /** Honest trade earns a little standing each day; the daily cap keeps it from being farmed. */
    private static void traded(TradeWithVillagerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && FactionStanding.countTrade(player)) {
            FactionStanding.changeResidents(player, 1, "traded");
        }
    }

    /** Hero of the Village: the residents remember who defended them. */
    private static void hero(MobEffectEvent.Added event) {
        if (event.getEffectInstance().is(MobEffects.HERO_OF_THE_VILLAGE) && event.getEntity() instanceof ServerPlayer player
                && event.getOldEffectInstance() == null) {
            FactionStanding.changeResidents(player, 20, "raid_defended");
        }
    }

    /** Residents who hate a player will not trade with them. Standing recovers by defending a village. */
    private static void refuse(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof AbstractVillager)) return;
        if (event.getTarget() instanceof WanderingTrader) return;
        if (FactionStanding.of(player).residents() <= FactionStanding.RESIDENTS_REFUSE) {
            player.displayClientMessage(Component.translatable("message.dorohedoro.standing.refused"), true);
            event.setCanceled(true);
        }
    }

    /**
     * Hole goods on the vanilla trade tables, so emeralds stay worth earning. Each trade either buys something the Hole
     * produces or sells something useful for a fair emerald price; none can be chained into a profit loop.
     */
    private static void villagerTrades(VillagerTradesEvent event) {
        var trades = event.getTrades();
        if (event.getType() == VillagerProfession.TOOLSMITH) {
            trades.get(1).add(buy(ModItems.SCRAP_METAL.get(), 12, 16, 2));
            trades.get(2).add(buy(ModItems.RAW_ZINC.get(), 6, 12, 10));
        } else if (event.getType() == VillagerProfession.CLERIC) {
            trades.get(1).add(buy(ModItems.LEECH_MUCUS.get(), 6, 12, 2));
            trades.get(2).add(buy(ModItems.SPORE_SAC.get(), 4, 12, 10));
            trades.get(3).add(sell(ModItems.ANTIDOTE_POULTICE.get(), 2, 3, 8, 10));
            trades.get(3).add(sell(ModItems.MEDICAL_SALVE.get(), 1, 2, 8, 10));
        } else if (event.getType() == VillagerProfession.BUTCHER) {
            trades.get(1).add(buy(ModItems.RAW_RAT_MEAT.get(), 8, 16, 2));
            trades.get(2).add(sell(ModItems.SALTED_RAT_RATION.get(), 2, 3, 12, 5));
        } else if (event.getType() == VillagerProfession.MASON) {
            trades.get(1).add(buy(ModBlocks.LIMESTONE.get(), 10, 16, 2));
            trades.get(2).add(sell(ModBlocks.SEWER_BRICKWORK.get(), 8, 1, 16, 5));
        } else if (event.getType() == VillagerProfession.ARMORER) {
            trades.get(2).add(sell(ModBlocks.GALVANIZED_PLATING.get(), 4, 2, 12, 5));
        }
    }

    private static void wandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(sell(ModItems.SMOKE_SOAKED_FILTER.get(), 1, 4, 3, 1));
        event.getRareTrades().add(sell(ModItems.SMOKE_RESIDUE.get(), 1, 6, 2, 1));
    }

    /** The villager buys {@code count} of an item for one emerald. */
    private static VillagerTrades.ItemListing buy(ItemLike item, int count, int maxTrades, int xp) {
        return new BasicItemListing(new ItemStack(item, count), new ItemStack(Items.EMERALD), maxTrades, xp, 0.05F);
    }

    /** The villager sells {@code count} of an item for {@code emeralds}. */
    private static VillagerTrades.ItemListing sell(ItemLike item, int count, int emeralds, int maxTrades, int xp) {
        return new BasicItemListing(new ItemStack(Items.EMERALD, emeralds), new ItemStack(item, count), maxTrades, xp, 0.05F);
    }

}
