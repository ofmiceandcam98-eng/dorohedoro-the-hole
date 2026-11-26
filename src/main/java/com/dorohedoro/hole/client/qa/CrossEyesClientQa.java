package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.entity.CrossEyesEntity;
import com.dorohedoro.hole.faction.FactionStanding;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.dorohedoro.hole.worldgen.city.CrossEyeCamp56;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client test for the Cross-Eyes and faction standing ({@code -Pqa_mode=crosseyes}): photographs a gang member from four
 * angles, finds and photographs a generated camp, brings the gang by walking in, and checks hostility, tribute,
 * trading, standing changes on a kill, villager prices by resident standing and refusal when hated.
 * Delete {@code runs/qaclient/saves/clientqa-<seed>} first.
 */
final class CrossEyesClientQa {
    private CrossEyesClientQa() {}

    private static int bx, bz, my;
    private static BlockPos camp;
    private static final String[] ANGLES = {"front", "side", "back", "above"};

    // Timeline.
    private static final int MODEL = 60;
    private static final int CAMP = MODEL + 4 * 60 + 100;
    private static final int GANG = CAMP + 640;
    private static final int TRADE = GANG + 400;
    private static final int VILLAGER = TRADE + 200;
    private static final int STATUS = VILLAGER + 220;

    static void script(Minecraft mc, int t) {
        if (t == 2) PacketDistributor.sendToServer(new CharacterNetwork.Submit("human", ""));
        else if (t == 40) {
            closeScreen(mc);
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doWeatherCycle false");
            command(mc, "weather clear");
            command(mc, "time set 6000");
            command(mc, "gamemode spectator @a");
            mc.options.hideGui = true;
            bx = (int) Math.floor(ClientQa.home.x);
            bz = (int) Math.floor(ClientQa.home.z);
            my = (int) Math.floor(ClientQa.home.y) + 30;
        }
        // 1. The model, four angles, on a platform in the sky.
        else if (t == MODEL) {
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", bx - 3, my, bz - 3, bx + 3, my, bz + 3));
            command(mc, String.format(Locale.ROOT, "summon dorohedoro:cross_eyes %d.5 %d %d.5 {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[0f,0f],HandItems:[{id:\"minecraft:iron_sword\",count:1},{}]}", bx, my + 1, bz));
        } else if (t > MODEL && t <= MODEL + 4 * 60 && (t - MODEL) % 60 == 0) {
            int a = (t - MODEL) / 60 - 1;
            if ((t - MODEL) % 60 == 0 && a < 4) {
                shot(mc, "crosseyes-0" + (a + 1) + "-" + ANGLES[a]);
                if (a + 1 < 4) command(mc, camera(a + 1));
            }
        } else if (t == MODEL + 5) command(mc, camera(0));
        // The whole gang: one member in each of the eight vanilla default skins they wear, in a row.
        else if (t == MODEL + 4 * 60 + 5) command(mc, "kill @e[type=dorohedoro:cross_eyes]");
        else if (t == MODEL + 4 * 60 + 50) {
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", bx - 6, my, bz - 3, bx + 6, my, bz + 8));
            for (int i = 0; i < CrossEyesEntity.SKIN_COUNT; i++) {
                command(mc, String.format(Locale.ROOT, "summon dorohedoro:cross_eyes %d.5 %d %d.5 {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[0f,0f],Skin:%d,HandItems:[{id:\"minecraft:iron_sword\",count:1},{}]}", bx - 4 + i, my + 1, bz, i));
            }
        } else if (t == MODEL + 4 * 60 + 55) {
            command(mc, String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 180 8", (double) bx, my + 1.8, bz + 7.0));
        } else if (t == MODEL + 4 * 60 + 85) shot(mc, "crosseyes-04b-all-skins");
        // 2. The nearest generated camp: empty from above, then the gang arrives when a player walks in.
        else if (t == CAMP) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                if (level.getChunkSource().getGenerator() instanceof HoleChunkGenerator generator) {
                    camp = CrossEyeCamp56.findNearest(generator.plan(level), bx, bz, 1600);
                }
                check("camp_exists_within_1600", camp != null, camp == null ? "none" : camp.toShortString());
            });
        } else if (t == CAMP + 10 && camp != null) {
            command(mc, String.format(Locale.ROOT, "tp @a %d %d %d 0 70", camp.getX(), camp.getY() + 60, camp.getZ() - 8));
        } else if (t == CAMP + 300 && camp != null) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                int fires = 0, red = 0, barrels = 0, top = 0;
                for (int x = camp.getX() - 20; x <= camp.getX() + 20; x++) for (int z = camp.getZ() - 20; z <= camp.getZ() + 20; z++)
                    for (int y = camp.getY() - 10; y <= camp.getY() + 50; y++) {
                        var s = level.getBlockState(new BlockPos(x, y, z));
                        if (s.is(Blocks.CAMPFIRE)) { fires++; top = y; }
                        if (s.is(Blocks.RED_CONCRETE)) red++;
                        if (s.is(Blocks.BARREL)) barrels++;
                    }
                check("camp_built", fires == 1 && red == 10 && barrels >= 2, fires + " camp fire, " + red + " red cross blocks, " + barrels + " barrels");
                if (top != 0) camp = new BlockPos(camp.getX(), top, camp.getZ());
            });
        } else if (t == CAMP + 310 && camp != null) command(mc, String.format(Locale.ROOT, "tp @a %d %d %d 0 62", camp.getX(), camp.getY() + 22, camp.getZ() - 14));
        else if (t == CAMP + 420 && camp != null) shot(mc, "crosseyes-05-camp-from-above");
        else if (t == CAMP + 430 && camp != null) {
            // Ground level, outside the gate on the south side, looking in.
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 12", camp.getX() + 0.5, camp.getY() + 1, camp.getZ() + 14.5));
        } else if (t == CAMP + 600 && camp != null) shot(mc, "crosseyes-06-camp-gate");
        // 3. Walk in as a survival player: the gang arrives and attacks at standing 0.
        else if (t == GANG && camp != null) {
            mc.options.hideGui = false;
            command(mc, "gamemode survival @a");
            command(mc, "effect give @a minecraft:resistance 600 4 true");
            command(mc, "effect give @a minecraft:regeneration 600 4 true");
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 10", camp.getX() + 0.5, camp.getY(), camp.getZ() + 8.5));
        } else if (t == GANG + 200 && camp != null) {
            server(mc, (server, player) -> {
                List<CrossEyesEntity> gang = gang(server, camp);
                long hunting = gang.stream().filter(m -> m.getTarget() == player).count();
                check("gang_arrives", gang.size() >= 3, gang.size() + " members");
                check("gang_hostile_at_standing_0", hunting >= 1, hunting + " of " + gang.size() + " targeting the player");
            });
            shot(mc, "crosseyes-07-gang-arrives");
        } else if (t == GANG + 210 && camp != null) {
            server(mc, (server, player) -> {
                List<CrossEyesEntity> gang = gang(server, camp);
                if (gang.isEmpty()) return;
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD, 8));
                for (int i = 0; i < 4; i++) gang.get(0).interact(player, InteractionHand.MAIN_HAND);
                check("tribute_raises_standing", FactionStanding.of(player).crossEyes() == 24,
                        "Cross-Eyes standing " + FactionStanding.of(player).crossEyes() + " after 4 emeralds");
                gang.get(0).interact(player, InteractionHand.MAIN_HAND);
                check("tribute_daily_cap", FactionStanding.of(player).crossEyes() == 24 && player.getMainHandItem().getCount() == 4,
                        "fifth emerald refused; " + player.getMainHandItem().getCount() + " emeralds left");
            });
        } else if (t == GANG + 300 && camp != null) {
            server(mc, (server, player) -> {
                long hunting = gang(server, camp).stream().filter(m -> m.getTarget() == player).count();
                check("tolerated_after_tribute", hunting == 0, hunting + " still targeting the player");
            });
        }
        // 4. Trading at standing 40, then a kill.
        else if (t == TRADE && camp != null) {
            server(mc, (server, player) -> {
                FactionStanding.changeCrossEyes(player, 20, "test");
                List<CrossEyesEntity> gang = gang(server, camp);
                if (gang.isEmpty()) return;
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                gang.get(0).interact(player, InteractionHand.MAIN_HAND);
                check("trades_at_standing_40", player.containerMenu instanceof MerchantMenu,
                        "open menu " + player.containerMenu.getClass().getSimpleName());
            });
        } else if (t == TRADE + 60) shot(mc, "crosseyes-08-trading");
        else if (t == TRADE + 70 && camp != null) {
            closeScreen(mc);
            server(mc, (server, player) -> {
                player.closeContainer();
                List<CrossEyesEntity> gang = gang(server, camp);
                if (gang.isEmpty()) return;
                int ce = FactionStanding.of(player).crossEyes(), res = FactionStanding.of(player).residents();
                gang.get(0).hurt(player.damageSources().playerAttack(player), 1000.0F);
                check("kill_lowers_cross_eyes_raises_residents",
                        FactionStanding.of(player).crossEyes() < ce && FactionStanding.of(player).residents() == res + 3,
                        "Cross-Eyes " + ce + " -> " + FactionStanding.of(player).crossEyes() + ", residents " + res + " -> " + FactionStanding.of(player).residents());
            });
        }
        // 5. Villager prices follow resident standing; a hated player is refused.
        else if (t == VILLAGER) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                Villager v = EntityType.VILLAGER.create(level);
                if (v == null) return;
                v.moveTo(player.getX() + 2, player.getY(), player.getZ(), 0, 0);
                v.setVillagerData(v.getVillagerData().setType(VillagerType.PLAINS).setProfession(VillagerProfession.CLERIC).setLevel(3));
                v.finalizeSpawn(level, level.getCurrentDifficultyAt(v.blockPosition()), MobSpawnType.COMMAND, null);
                v.setNoAi(true);
                level.addFreshEntity(v);
                int neutral = priceOf(v, player);
                FactionStanding.changeResidents(player, 60 - FactionStanding.of(player).residents(), "test");
                int liked = priceOf(v, player);
                FactionStanding.changeResidents(player, -60 - FactionStanding.of(player).residents(), "test");
                int disliked = priceOf(v, player);
                check("villager_prices_follow_standing", liked < neutral && disliked > neutral,
                        "all offers cost " + neutral + " neutral, " + liked + " trusted, " + disliked + " hated");
                long hole = v.getOffers().stream().filter(o -> o.getResult().getItem().builtInRegistryHolder().key().location().getNamespace().equals("dorohedoro")
                        || o.getBaseCostA().getItem().builtInRegistryHolder().key().location().getNamespace().equals("dorohedoro")).count();
                check("cleric_has_hole_trades", hole >= 1, hole + " of " + v.getOffers().size() + " offers use Hole goods");
                FactionStanding.changeResidents(player, -90 - FactionStanding.of(player).residents(), "test");
                log("villager at " + v.blockPosition().toShortString() + " id " + v.getId());
                villagerId = v.getId();
            });
        } else if (t == VILLAGER + 20) {
            // A real right-click from the client: a hated player is turned away.
            var entity = mc.level == null ? null : mc.level.getEntity(villagerId);
            if (entity != null && mc.gameMode != null) mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
        } else if (t == VILLAGER + 60) {
            server(mc, (server, player) -> check("hated_player_refused", !(player.containerMenu instanceof MerchantMenu),
                    "menu " + player.containerMenu.getClass().getSimpleName()));
        }
        // 6. The Status tab with the standing rows.
        else if (t == STATUS) {
            closeScreen(mc);
            server(mc, (server, player) -> {
                FactionStanding.changeResidents(player, 35 - FactionStanding.of(player).residents(), "test");
            });
        } else if (t == STATUS + 20) mc.setScreen(new com.dorohedoro.hole.client.magic.SmokeLedgerScreen());
        else if (t == STATUS + 60) shot(mc, "crosseyes-09-status-standing");
        else if (t == STATUS + 70) {
            log("tour complete, quitting");
            mc.stop();
        }
    }

    private static int villagerId = -1;

    /** Total cost of every offer after the villager prepares prices for this player; closing clears them like vanilla. */
    private static int priceOf(Villager v, ServerPlayer player) {
        v.mobInteract(player, InteractionHand.MAIN_HAND);
        int cost = v.getOffers().stream().mapToInt(o -> o.getCostA().getCount()).sum();
        player.closeContainer();
        return cost;
    }

    private static List<CrossEyesEntity> gang(MinecraftServer server, BlockPos near) {
        return server.overworld().getEntitiesOfClass(CrossEyesEntity.class, new AABB(near).inflate(40), e -> !e.isNoAi());
    }

    private static String camera(int a) {
        double x = bx + 0.5, y = my + 1, z = bz + 0.5;
        return switch (a) {
            case 0 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 180 10", x, y, z + 3.2);
            case 1 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 90 10", x + 3.2, y, z);
            case 2 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 0 10", x, y, z - 3.2);
            default -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 135 40", x + 2.4, y + 2.6, z + 2.4);
        };
    }

    private static void server(Minecraft mc, BiConsumer<MinecraftServer, ServerPlayer> action) {
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) return;
        var id = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) action.accept(server, player);
        });
    }

    private static void check(String name, boolean passed, String detail) {
        log((passed ? "PASS " : "FAIL ") + name + ": " + detail);
    }
}
