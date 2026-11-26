package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.hazard.ModEffects;
import com.dorohedoro.hole.hazard.ToxinExposure;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.HoleRegions42;
import com.dorohedoro.hole.worldgen.city.Line;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client test for the Pass 78 hazards ({@code -Pqa_mode=hazards}). In a new v56 world it:
 * <ol>
 *   <li>finds a generated Ash-Snow drift near spawn and photographs it;</li>
 *   <li>flies to the nearest cold canal, finds a Sludge-Ice floe and photographs it;</li>
 *   <li>shows the new blocks beside vanilla snow and ice;</li>
 *   <li>checks that exposure builds barefoot on Ash-Snow, that boots stop it, that Hole Toxin applies and that an
 *       Antidote Poultice clears it;</li>
 *   <li>photographs the HUD toxin chip and the effect in the inventory.</li>
 * </ol>
 * Delete {@code runs/qaclient/saves/clientqa-<seed>} first: an old save would be reopened instead of a fresh world.
 */
final class HazardClientQa {
    private HazardClientQa() {}

    private static final int Y = 220;
    private static BlockPos drift, floe, canal;
    private static int bx, bz;

    // Timeline, in ticks after joining.
    private static final int FIND = 300;
    private static final int CANAL = FIND + 270;
    private static final int SAMPLE = CANAL + 450;
    private static final int EXPOSE = SAMPLE + 180;

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
        } else if (t == FIND) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                drift = find(level, ModBlocks.ASH_SNOW.get(), bx, bz, 112);
                check("worldgen_ash_snow_drift", drift != null, drift == null ? "none within 112 blocks" : drift.toShortString());
                int ash = 0;
                for (int x = bx - 64; x <= bx + 64; x++) for (int z = bz - 64; z <= bz + 64; z++) for (int y = 40; y <= 200; y++) {
                    if (level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ASH_SNOW.get())) ash++;
                }
                log("within 64 blocks of spawn: " + ash + " Ash-Snow layers");
                if (level.getChunkSource().getGenerator() instanceof HoleChunkGenerator generator) {
                    CityPlan plan = generator.plan(level);
                    log("plan at spawn: region " + plan.regionAt(bx, bz) + ", frostbitten " + plan.isColdDistrict(bx, bz));
                    canal = nearestColdCanal(plan);
                    log("nearest cold canal: " + (canal == null ? "none within 2000 blocks" : canal.toShortString()));
                }
            });
        } else if (t == FIND + 10 && drift != null) command(mc, lookAt(drift, -3, 3, -4));
        else if (t == FIND + 250 && drift != null) shot(mc, "hazard-01-ash-snow-drift");
        else if (t == CANAL && canal != null) {
            command(mc, String.format(Locale.ROOT, "tp @a %d 120 %d 0 60", canal.getX(), canal.getZ()));
        } else if (t == CANAL + 260 && canal != null) {
            server(mc, (server, player) -> {
                floe = find(server.overworld(), ModBlocks.SLUDGE_ICE.get(), canal.getX(), canal.getZ(), 64);
                check("worldgen_sludge_ice_floe", floe != null, floe == null ? "none within 64 blocks of the canal" : floe.toShortString());
            });
        } else if (t == CANAL + 270 && floe != null) command(mc, lookAt(floe, 5, 7, -7));
        else if (t == CANAL + 440 && floe != null) shot(mc, "hazard-02-sludge-ice-canal");
        // A sample row in the sky: vanilla snow, snow block and ice, then Ash-Snow (block, 3 layers) and Sludge-Ice.
        else if (t == SAMPLE) {
            command(mc, String.format(Locale.ROOT, "tp @a %d %d %d", bx, Y + 3, bz));
        } else if (t == SAMPLE + 40) {
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", bx - 2, Y - 1, bz - 2, bx + 12, Y - 1, bz + 4));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d minecraft:snow[layers=3]", bx, Y, bz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d minecraft:snow_block", bx + 1, Y, bz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d minecraft:ice", bx + 2, Y, bz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:ash_snow_block", bx + 4, Y, bz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:ash_snow[layers=3]", bx + 5, Y, bz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:sludge_ice", bx + 6, Y, bz));
            for (int layers = 1; layers <= 7; layers++) {
                command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:ash_snow[layers=%d]", bx + 3 + layers, Y, bz + 2, layers));
            }
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 165 40", bx + 4.5, Y + 3, bz + 6.5));
        } else if (t == SAMPLE + 170) shot(mc, "hazard-03-blocks-vs-vanilla");
        // Exposure: barefoot on Ash-Snow builds it; boots stop it; the effect applies; the Antidote clears it.
        else if (t == EXPOSE) {
            mc.options.hideGui = false;
            command(mc, "gamemode survival @a");
            command(mc, "clear @a");
            command(mc, "effect clear @a");
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d dorohedoro:ash_snow[layers=2]", bx + 8, Y, bz - 1, bx + 10, Y, bz + 1));
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 10", bx + 9.5, Y, bz + 0.5));
        } else if (t == EXPOSE + 20 * 25) {
            server(mc, (server, player) -> {
                int e = ToxinExposure.get(player);
                check("ash_snow_barefoot_builds_exposure", e >= 150 && e <= 260, e + " after 25 seconds barefoot (8 a second)");
            });
            command(mc, "item replace entity @a armor.feet with minecraft:leather_boots");
        } else if (t == EXPOSE + 20 * 35) {
            server(mc, (server, player) -> {
                int e = ToxinExposure.get(player);
                check("boots_stop_ash_snow", e < 200, e + " after 10 more seconds in boots (should fall by 6 a second)");
                ToxinExposure.add(player, 700, "generic");
            });
        } else if (t == EXPOSE + 20 * 37) {
            server(mc, (server, player) -> {
                var effect = player.getEffect(ModEffects.HOLE_TOXIN);
                check("toxin_effect_applies", effect != null && effect.getAmplifier() >= 1,
                        effect == null ? "no effect at " + ToxinExposure.get(player) : "level " + (effect.getAmplifier() + 1) + " at " + ToxinExposure.get(player));
            });
            command(mc, "give @a dorohedoro:ash_snow 16");
            command(mc, "give @a dorohedoro:ash_snow_block 8");
            command(mc, "give @a dorohedoro:ash_snowball 16");
            command(mc, "give @a dorohedoro:sludge_ice 8");
            command(mc, "give @a dorohedoro:antidote_poultice 2");
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 160 20", bx + 4.5, Y, bz + 3.5));
        } else if (t == EXPOSE + 20 * 40) shot(mc, "hazard-04-hud-toxin-chip");
        else if (t == EXPOSE + 20 * 40 + 5) mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        else if (t == EXPOSE + 20 * 42) shot(mc, "hazard-05-inventory-effect-and-items");
        else if (t == EXPOSE + 20 * 43) {
            closeScreen(mc);
            server(mc, (server, player) -> {
                var stack = new net.minecraft.world.item.ItemStack(com.dorohedoro.hole.registry.ModItems.ANTIDOTE_POULTICE.get());
                stack.finishUsingItem(player.level(), player);
                check("antidote_clears_toxin", ToxinExposure.get(player) == 0 && !player.hasEffect(ModEffects.HOLE_TOXIN),
                        "exposure " + ToxinExposure.get(player) + ", effect " + player.hasEffect(ModEffects.HOLE_TOXIN));
            });
        } else if (t == EXPOSE + 20 * 45) {
            log("tour complete, quitting");
            mc.stop();
        }
    }

    /** A spectator teleport to {@code target + (dx, dy, dz)}, turned to look straight at the target. */
    private static String lookAt(BlockPos target, double dx, double dy, double dz) {
        double x = target.getX() + 0.5 + dx, y = target.getY() + dy, z = target.getZ() + 0.5 + dz;
        double tx = target.getX() + 0.5 - x, ty = target.getY() + 0.3 - (y + 1.62), tz = target.getZ() + 0.5 - z;
        float yaw = (float) Math.toDegrees(Math.atan2(-tx, tz));
        float pitch = (float) Math.toDegrees(-Math.atan2(ty, Math.hypot(tx, tz)));
        return String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f %.1f %.1f", x, y, z, yaw, pitch);
    }

    /** The nearest point on a canal whose surroundings are cold, searching canal lines within 2000 blocks. */
    private static BlockPos nearestColdCanal(CityPlan plan) {
        BlockPos best = null;
        long bestD = Long.MAX_VALUE;
        for (boolean xAxis : new boolean[]{true, false}) {
            int origin = xAxis ? bx : bz;
            for (Line line : xAxis ? plan.xLinesNear(origin - 2000, origin + 2000) : plan.zLinesNear(origin - 2000, origin + 2000)) {
                if (line.kind() != Line.Kind.CANAL) continue;
                for (int along = -2000; along <= 2000; along += 32) {
                    int x = xAxis ? line.center() : bx + along, z = xAxis ? bz + along : line.center();
                    boolean cold = plan.isColdDistrict(x, z) || plan.regionAt(x, z) == HoleRegions42.Region.SNOWFIELDS;
                    if (!cold) continue;
                    long d = (long) (x - bx) * (x - bx) + (long) (z - bz) * (z - bz);
                    if (d < bestD) { bestD = d; best = new BlockPos(x, 0, z); }
                }
            }
        }
        return best;
    }

    /** Nearest block of a kind within a square radius, over the whole surface height band. Loaded chunks only. */
    private static BlockPos find(ServerLevel level, Block block, int cx, int cz, int radius) {
        BlockPos best = null;
        long bestD = Long.MAX_VALUE;
        for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++) {
            if (!level.hasChunk(x >> 4, z >> 4)) continue;
            for (int y = 40; y <= 200; y++) {
                if (!level.getBlockState(new BlockPos(x, y, z)).is(block)) continue;
                long d = (long) (x - cx) * (x - cx) + (long) (z - cz) * (z - cz);
                if (d < bestD) { bestD = d; best = new BlockPos(x, y, z); }
            }
        }
        return best;
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
