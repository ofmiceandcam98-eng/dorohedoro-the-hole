package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.config.SmokeWorldConfig;
import com.dorohedoro.hole.magic.SmokeAttachments;
import com.dorohedoro.hole.magic.SmokeEngine;
import com.dorohedoro.hole.portal.PortalCoreBlockEntity;
import com.dorohedoro.hole.portal.PortalKind;
import com.dorohedoro.hole.portal.PortalService;
import com.dorohedoro.hole.portal.SmokeDoors;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Phase 4 in the real client ({@code -Pqa_mode=doors}): Smoke Doors (refused when weak, opened when strong, travel,
 * arrival facing away, closing on time), the Door Linking Key joining two hand-built doors, and a door rebuilding its
 * far end after it was broken. Platforms are built high in the sky in both worlds so nothing generated is disturbed.
 * Delete {@code runs/qaclient/saves/clientqa-<seed>} first.
 */
final class DoorsClientQa {
    private DoorsClientQa() {}

    private static final int Y = 210;
    private static final ResourceKey<Level> SW =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dorohedoro", "sorcerers_world"));
    private static int bx, bz;
    private static BlockPos doorA, doorB;
    /** The lowest block of the Smoke Door's opening, found after it opens. */
    private static BlockPos smokeOpening;

    private static final int SMOKE = 60;
    private static final int LINK = SMOKE + 900;
    private static final int REBUILD = LINK + 500;

    static void script(Minecraft mc, int t) {
        if (t == 2) PacketDistributor.sendToServer(new CharacterNetwork.Submit("fate", ""));
        else if (t == 40) {
            closeScreen(mc);
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doWeatherCycle false");
            command(mc, "weather clear");
            command(mc, "time set 6000");
            command(mc, "gamemode creative @a");
            bx = (int) Math.floor(ClientQa.home.x) + 32;
            bz = (int) Math.floor(ClientQa.home.z) + 32;
            platform(mc, "minecraft:overworld", bx, bz);
        }
        // 1. Smoke Door: a fresh rank-1 sorcerer is refused; at rank 5 the door opens.
        else if (t == SMOKE) {
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 55", bx + 0.5, Y, bz + 0.5));
        } else if (t == SMOKE + 20) {
            server(mc, (server, player) -> {
                SmokeWorldConfig.SMOKE_DOOR_SECONDS.set(15);
                int before = SmokeEngine.state(player).smoke();
                SmokeDoors.open(player);
                check("smoke_door_refused_when_weak", SmokeEngine.state(player).smoke() == before,
                        "rank " + SmokeEngine.state(player).rank() + ", smoke unchanged at " + before);
                // Train to rank 5 and fill the smoke reserve, as playing would.
                var s = SmokeEngine.state(player).gainExperience(100 + 200 + 300 + 400 + 10);
                player.setData(SmokeAttachments.STATE, s.withSmoke(s.capacity()));
            });
        } else if (t == SMOKE + 40) {
            server(mc, (server, player) -> {
                int before = SmokeEngine.state(player).smoke();
                SmokeDoors.open(player);
                int spent = before - SmokeEngine.state(player).smoke();
                check("smoke_door_opens_at_rank_5", spent == SmokeWorldConfig.SMOKE_DOOR_COST.get(), "rank " + SmokeEngine.state(player).rank()
                        + ", smoke " + before + " -> " + SmokeEngine.state(player).smoke());
                ServerLevel level = server.overworld();
                for (var p : BlockPos.betweenClosed(bx - 8, Y - 1, bz - 8, bx + 8, Y + 4, bz + 8)) {
                    if (level.getBlockState(p).is(ModBlocks.PORTAL_SURFACE.get()) && (smokeOpening == null || p.getY() < smokeOpening.getY())) {
                        smokeOpening = p.immutable();
                    }
                }
                log("smoke door opening at " + (smokeOpening == null ? "none" : smokeOpening.toShortString()));
            });
        } else if (t == SMOKE + 60) {
            mc.options.hideGui = true;
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 8", bx + 0.5, Y, bz - 3.5));
        } else if (t == SMOKE + 120) shot(mc, "door-01-smoke-door");
        else if (t == SMOKE + 130) {
            // Step into the opening.
            if (smokeOpening != null) command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 0",
                    smokeOpening.getX() + 0.5, smokeOpening.getY(), smokeOpening.getZ() + 0.5));
        } else if (t == SMOKE + 200) {
            server(mc, (server, player) -> {
                check("smoke_door_travels_to_sorcerers_world", player.level().dimension().equals(SW),
                        "player in " + player.level().dimension().location() + " at " + player.blockPosition().toShortString());
                log("arrived facing yaw " + Math.round(player.getYRot()));
            });
        } else if (t == SMOKE + 240) shot(mc, "door-02-smoke-door-exit");
        else if (t == SMOKE + 260) {
            mc.options.hideGui = false;
            command(mc, String.format(Locale.ROOT, "execute in minecraft:overworld run tp @a %.1f %d %.1f 0 40", bx + 0.5, Y + 4, bz - 4.5));
        } else if (t == SMOKE + 20 + 20 * 18) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                int left = 0;
                for (var p : BlockPos.betweenClosed(bx - 4, Y, bz - 4, bx + 4, Y + 4, bz + 6)) {
                    var s = level.getBlockState(p);
                    if (s.is(ModBlocks.PORTAL_SURFACE.get()) || s.is(ModBlocks.CONDENSED_SMOKE_BARRIER.get())) left++;
                }
                check("smoke_door_closes_on_time", left == 0, left + " smoke blocks left 18 seconds after opening (15 s door)");
                SmokeWorldConfig.SMOKE_DOOR_SECONDS.set(90);
            });
        }
        // 2. The Door Linking Key joins two hand-built Artificial Doors, one in each world. Nobody stands in the Sorcerers'
        // World now, so its test area is force-loaded first or nothing can be built there.
        else if (t == LINK - 100) {
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world run forceload add %d %d %d %d", bx - 8, bz - 10, bx + 18, bz + 10));
        } else if (t == LINK) {
            platform(mc, "dorohedoro:sorcerers_world", bx, bz);
            doorA = new BlockPos(bx + 10, Y, bz);
            doorB = new BlockPos(bx + 10, Y, bz);
            frame(mc, "minecraft:overworld", doorA);
            frame(mc, "dorohedoro:sorcerers_world", doorB);
        } else if (t == LINK + 40) {
            server(mc, (server, player) -> {
                ServerLevel hole = server.overworld(), sw = server.getLevel(SW);
                ItemStack key = new ItemStack(ModItems.DOOR_LINKING_KEY.get());
                // The key is used through the core block's own interaction, as a right-click would.
                use(hole, doorA, key, player);
                use(sw, doorB, key, player);
                boolean a = hole.getBlockEntity(doorA) instanceof PortalCoreBlockEntity c && c.active() && doorB.equals(c.destinationCore());
                boolean b = sw != null && sw.getBlockEntity(doorB) instanceof PortalCoreBlockEntity c2 && c2.active() && doorA.equals(c2.destinationCore());
                check("linking_key_links_two_built_doors", a && b, "Hole door linked " + a + ", Sorcerers' World door linked " + b);
            });
        } else if (t == LINK + 60) command(mc, String.format(Locale.ROOT, "execute in minecraft:overworld run tp @a %.1f %d %.1f 0 0", doorA.getX() + 1.0, Y + 1, doorA.getZ() + 0.5));
        else if (t == LINK + 200) {
            server(mc, (server, player) -> {
                boolean there = player.level().dimension().equals(SW) && player.blockPosition().distSqr(doorB) < 16;
                check("linked_doors_travel", there, "player in " + player.level().dimension().location() + " at " + player.blockPosition().toShortString());
                // The X-axis door's landings are south (+z, yaw 0) or north (-z, yaw 180); the player must face away from it.
                int dz = Integer.signum(player.blockPosition().getZ() - doorB.getZ());
                float yaw = net.minecraft.util.Mth.wrapDegrees(player.getYRot());
                check("arrival_faces_away_from_door", dz > 0 ? Math.abs(yaw) < 1 : Math.abs(Math.abs(yaw) - 180) < 1,
                        "landed on side " + dz + ", facing yaw " + Math.round(yaw));
            });
        } else if (t == LINK + 220) {
            mc.options.hideGui = true;
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world run tp @a %.1f %d %.1f 180 10", doorB.getX() + 1.0, Y + 1, doorB.getZ() + 5.5));
        } else if (t == LINK + 300) shot(mc, "door-03-linked-door-in-sorcerers-world");
        // 3. A built door whose far end was destroyed opens a new far end the next time someone steps in.
        else if (t == REBUILD) {
            mc.options.hideGui = false;
            command(mc, "execute in minecraft:overworld run tp @a " + bx + " " + (Y + 3) + " " + bz);
        } else if (t == REBUILD + 20) {
            // Break one frame block of the far door: its core stays, but it is no longer a door.
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world run setblock %d %d %d minecraft:air",
                    doorB.getX() - 1, doorB.getY() + 2, doorB.getZ()));
        } else if (t == REBUILD + 40) {
            command(mc, String.format(Locale.ROOT, "execute in minecraft:overworld run tp @a %.1f %d %.1f 0 0", doorA.getX() + 1.0, Y + 1, doorA.getZ() + 0.5));
        } else if (t == REBUILD + 200) {
            server(mc, (server, player) -> {
                ServerLevel hole = server.overworld();
                BlockPos far = hole.getBlockEntity(doorA) instanceof PortalCoreBlockEntity c && c.active() ? c.destinationCore() : null;
                check("door_reopens_after_far_end_lost", far != null && !far.equals(doorB) && player.level().dimension().equals(SW),
                        "new far end " + (far == null ? "none" : far.toShortString()) + ", player in " + player.level().dimension().location());
            });
        } else if (t == REBUILD + 260) {
            log("tour complete, quitting");
            mc.stop();
        }
    }

    private static void use(ServerLevel level, BlockPos core, ItemStack key, ServerPlayer player) {
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, key);
        var state = level.getBlockState(core);
        state.useItemOn(key, level, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(core), net.minecraft.core.Direction.SOUTH, core, false));
    }

    private static void platform(Minecraft mc, String dimension, int x, int z) {
        command(mc, String.format(Locale.ROOT, "execute in %s run fill %d %d %d %d %d %d minecraft:air", dimension, x - 6, Y, z - 8, x + 16, Y + 8, z + 8));
        command(mc, String.format(Locale.ROOT, "execute in %s run fill %d %d %d %d %d %d minecraft:smooth_stone", dimension, x - 6, Y - 1, z - 8, x + 16, Y - 1, z + 8));
    }

    /** A complete Artificial Door frame with its core at {@code core}, along the X axis. */
    private static void frame(Minecraft mc, String dimension, BlockPos core) {
        command(mc, String.format(Locale.ROOT, "execute in %s run fill %d %d %d %d %d %d dorohedoro:reinforced_door_frame", dimension,
                core.getX() - 1, core.getY(), core.getZ(), core.getX() + 2, core.getY() + 4, core.getZ()));
        command(mc, String.format(Locale.ROOT, "execute in %s run fill %d %d %d %d %d %d minecraft:air", dimension,
                core.getX(), core.getY() + 1, core.getZ(), core.getX() + 1, core.getY() + 3, core.getZ()));
        command(mc, String.format(Locale.ROOT, "execute in %s run setblock %d %d %d dorohedoro:artificial_door_core", dimension,
                core.getX(), core.getY(), core.getZ()));
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
