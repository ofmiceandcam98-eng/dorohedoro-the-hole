package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.portal.PortalCoreBlockEntity;
import com.dorohedoro.hole.portal.PortalKind;
import com.dorohedoro.hole.portal.PortalService;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.world.BlockProtection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The client test with real FTB mods installed ({@code tools/ftb-client.sh}). Checks, in order:
 * <ol>
 *   <li>the quest book installs from the mod jar and opens in FTB's own screen;</li>
 *   <li>quests complete from ordinary play: items in the inventory and the mod's hidden progress advancements;</li>
 *   <li>FTB Ultimine mines a whole stone-and-deepslate zinc vein with the real Ultimine key (and only one block
 *       without it);</li>
 *   <li>an FTB Chunks claim owned by another team blocks the mod's own block placement and explosions, and stops a
 *       door from opening its far end inside that claim; the same door opens once the claim is gone, and the player
 *       travels through it and back.</li>
 * </ol>
 * Every result is logged as {@code CLIENTQA PASS ...} or {@code CLIENTQA FAIL ...}. Checks that need a mod that is
 * not installed are logged as {@code SKIP}. Quest completion is read from FTB's saved team progress by
 * {@code tools/ftbquests/check-progress.mjs} after the game closes, because FTB writes it when the world is saved on exit.
 */
final class FtbClientQa {
    private FtbClientQa() {}

    private static final String PREFIX = "0D0E0B0E";
    private static final int Y = 230;
    private static final ResourceKey<Level> SORCERERS_WORLD =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dorohedoro", "sorcerers_world"));

    private static int bx, bz, px, pz;
    private static String rivals = "qa_rivals";
    private static BlockPos farCore;
    private static int mineTicks;

    // Timeline, in ticks after joining the world.
    private static final int INSTALL = 60;
    private static final int BOOK = INSTALL + 40;
    private static final int ITEMS = BOOK + 90;
    private static final int ULTIMINE = ITEMS + 220;
    private static final int CLAIMS = ULTIMINE + 300;
    private static final int DOOR = CLAIMS + 140;
    private static final int PROGRESS = DOOR + 520;

    static void script(Minecraft mc, int t) {
        if (t == 2) PacketDistributor.sendToServer(new CharacterNetwork.Submit("fate", ""));
        else if (t == 40) {
            closeScreen(mc);
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doWeatherCycle false");
            command(mc, "weather clear");
            command(mc, "time set 6000");
            bx = (int) Math.floor(ClientQa.home.x) + 48;
            bz = (int) Math.floor(ClientQa.home.z) + 48;
            px = bx + 160;
            pz = bz;
            for (String id : List.of("architectury", "ftblibrary", "ftbteams", "ftbquests", "ftbchunks", "ftbultimine",
                    "ftbxmodcompat", "ftbessentials", "ftbfiltersystem", "playernbtftb")) {
                log("mod " + id + " " + ModList.get().getModContainerById(id)
                        .map(c -> c.getModInfo().getVersion().toString()).orElse("not installed"));
            }
        }
        // 1. The quest book.
        else if (t == INSTALL) command(mc, "dorohedoro quests install");
        else if (t == INSTALL + 20) {
            Path book = mc.gameDirectory.toPath().resolve("config/ftbquests/quests");
            for (String file : List.of("chapter_groups.snbt", "chapters/dorohedoro_welcome.snbt", "chapters/dorohedoro_streets.snbt",
                    "chapters/dorohedoro_doors.snbt", "chapters/dorohedoro_hell.snbt", "lang/en_us.snbt", "data.snbt")) {
                check("quest_file_" + file, Files.isRegularFile(book.resolve(file)), book.resolve(file).toString());
            }
        } else if (t == BOOK) command(mc, "execute as @a run ftbquests open_book");
        else if (t == BOOK + 80) {
            log("screen with the book open: " + (mc.screen == null ? "none" : mc.screen.getClass().getName()));
            check("quest_book_opens", mc.screen != null && mc.screen.getClass().getName().startsWith("dev.ftb."),
                    mc.screen == null ? "no screen" : mc.screen.getClass().getName());
            shot(mc, "ftb-01-book");
        }
        // 2. Quests from ordinary play.
        else if (t == ITEMS) {
            closeScreen(mc);
            for (String give : List.of("scrap_metal 8", "raw_sulfur 4", "rock_salt 4", "smoke_soaked_filter 1", "smoke_residue 3")) {
                command(mc, "give @a dorohedoro:" + give);
            }
        } else if (t == ITEMS + 120) command(mc, "execute as @a run ftbquests open_book");
        else if (t == ITEMS + 200) shot(mc, "ftb-02-book-progress");
        // 3. Ultimine.
        else if (t >= ULTIMINE && t < CLAIMS) ultimine(mc, t - ULTIMINE);
        // 4. Claims.
        else if (t >= CLAIMS && t < DOOR) claims(mc, t - CLAIMS);
        else if (t >= DOOR && t < PROGRESS) door(mc, t - DOOR);
        else if (t == PROGRESS) {
            closeScreen(mc);
            server(mc, (server, player) -> log("progress world " + server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize()));
        } else if (t == PROGRESS + 10) {
            log("tour complete, quitting");
            mc.stop();
        }
    }

    private static void ultimine(Minecraft mc, int k) {
        if (!ModList.get().isLoaded("ftbultimine")) {
            if (k == 0) log("SKIP ultimine: FTB Ultimine is not installed");
            return;
        }
        KeyMapping key = null;
        for (KeyMapping mapping : mc.options.keyMappings) {
            if (mapping.getName().equals("key.ftbultimine")) key = mapping;
        }
        if (k == 0) {
            closeScreen(mc);
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", bx - 5, Y - 1, bz - 3, bx + 5, Y + 4, bz + 11));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", bx - 5, Y - 1, bz - 3, bx + 5, Y - 1, bz + 11));
            for (int vein : new int[]{0, 6}) {
                command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d dorohedoro:deepslate_zinc_ore", bx, Y, bz + vein, bx + 2, Y, bz + vein + 2));
                command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d dorohedoro:zinc_ore", bx, Y + 1, bz + vein, bx + 2, Y + 1, bz + vein + 2));
            }
            // A short stone and deepslate column for the survival check, beside the platform.
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:deepslate_zinc_ore", bx + 4, Y, bz + 4));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:zinc_ore", bx + 4, Y + 1, bz + 4));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:deepslate_zinc_ore", bx + 4, Y + 2, bz + 4));
            command(mc, "clear @a");
            command(mc, "gamemode creative @a");
            command(mc, "give @a minecraft:iron_pickaxe[minecraft:enchantments={levels:{\"minecraft:efficiency\":5}}]");
            command(mc, standAndLook(0));
        } else if (k == 40) {
            mineTicks = 0;
            log("mining one block of the first vein without the Ultimine key");
            mc.gameMode.startDestroyBlock(new BlockPos(bx, Y + 1, bz + 1), Direction.WEST);
        } else if (k > 40 && k < 80) {
            mine(mc, new BlockPos(bx, Y + 1, bz + 1));
        } else if (k == 90) {
            countVein(mc, "ultimine_control_without_key", 0, 17);
            command(mc, standAndLook(6));
        } else if (k >= 110 && k < 200) {
            if (key == null) {
                if (k == 110) check("ultimine_key_found", false, "no key.ftbultimine mapping");
                return;
            }
            key.setDown(true);
            if (k == 130) shot(mc, "ftb-03-ultimine-outline");
            if (k == 140) {
                log("mining one block of the second vein with the Ultimine key held");
                mc.gameMode.startDestroyBlock(new BlockPos(bx, Y + 1, bz + 7), Direction.WEST);
            } else if (k > 140) {
                mine(mc, new BlockPos(bx, Y + 1, bz + 7));
            }
        } else if (k == 200) {
            if (key != null) key.setDown(false);
            countVein(mc, "ultimine_mines_mixed_vein", 6, 0);
            shot(mc, "ftb-04-ultimine-after");
            // Survival: Ultimine charges hunger per block, so a short column; drops must appear as normal. In survival
            // Ultimine never breaks a block harder than the one mined first, exactly as with vanilla iron ore and its
            // deepslate twin, so the player starts on the (harder) deepslate block at the bottom.
            command(mc, "gamemode survival @a");
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 29", bx + 4.5, Y, bz + 2.5));
        } else if (k >= 220 && k < 280 && key != null) {
            key.setDown(true);
            if (k == 238) shot(mc, "ftb-04b-ultimine-survival-outline");
            if (k == 240) mc.gameMode.startDestroyBlock(new BlockPos(bx + 4, Y, bz + 4), Direction.NORTH);
            else if (k > 240) mine(mc, new BlockPos(bx + 4, Y, bz + 4));
        } else if (k == 285) {
            if (key != null) key.setDown(false);
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                int left = 0;
                for (int y = 0; y < 3; y++) {
                    var state = level.getBlockState(new BlockPos(bx + 4, Y + y, bz + 4));
                    if (state.is(ModBlocks.ZINC_ORE.get()) || state.is(ModBlocks.DEEPSLATE_ZINC_ORE.get())) left++;
                }
                int dropped = 0;
                for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(bx - 2, Y - 2, bz - 2, bx + 8, Y + 6, bz + 8))) {
                    if (item.getItem().is(com.dorohedoro.hole.registry.ModItems.RAW_ZINC.get())) dropped += item.getItem().getCount();
                }
                dropped += player.getInventory().countItem(com.dorohedoro.hole.registry.ModItems.RAW_ZINC.get());
                check("ultimine_survival_column", left == 0, left + " of 3 ore blocks left");
                check("ultimine_survival_drops", dropped >= 3, dropped + " raw zinc dropped or collected");
            });
            command(mc, "gamemode creative @a");
        }
    }

    private static String standAndLook(int vein) {
        // Standing west of the vein, looking east at its upper middle block.
        return String.format(Locale.ROOT, "tp @a %.1f %d %.1f -90 4", bx - 2 + 0.5, Y, bz + vein + 1 + 0.5);
    }

    private static void mine(Minecraft mc, BlockPos pos) {
        if (mc.level == null || mc.level.getBlockState(pos).isAir()) return;
        mc.gameMode.continueDestroyBlock(pos, Direction.WEST);
        if (++mineTicks % 5 == 0 && mc.player != null) mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private static void countVein(Minecraft mc, String name, int vein, int expected) {
        server(mc, (server, player) -> {
            ServerLevel level = server.overworld();
            int left = 0;
            for (int x = 0; x < 3; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 3; z++) {
                var state = level.getBlockState(new BlockPos(bx + x, Y + y, bz + vein + z));
                if (state.is(ModBlocks.ZINC_ORE.get()) || state.is(ModBlocks.DEEPSLATE_ZINC_ORE.get())) left++;
            }
            check(name, left == expected, left + " of 18 ore blocks left, expected " + expected);
        });
    }

    private static void claims(Minecraft mc, int k) {
        if (!ModList.get().isLoaded("ftbchunks")) {
            if (k == 0) log("SKIP claims: FTB Chunks is not installed");
            return;
        }
        int cx = bx, cz = bz + 64;
        if (k == 0) {
            command(mc, "gamemode creative @a");
            command(mc, "ftbteams server create qa_rivals");
        } else if (k == 20) {
            rivals = teamId("qa_rivals");
            log("rival team id " + rivals);
            // A claim by a team the test player is not in, in the Hole and around the door's far end.
            command(mc, String.format(Locale.ROOT, "execute in minecraft:overworld positioned %d %d %d run ftbchunks admin claim_as %s 16", cx, Y, cz, rivals));
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world positioned %d 100 %d run ftbchunks admin claim_as %s 48", px, pz, rivals));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:stone", cx - 1, Y, cz - 1, cx + 1, Y, cz + 1));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:stone", cx - 1, Y, cz - 81, cx + 1, Y, cz - 79));
        } else if (k == 60) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                check("claim_blocks_mod_placement", !BlockProtection.mayPlace(player, level, new BlockPos(cx, Y + 1, cz)),
                        "inside the rival claim");
                check("unclaimed_allows_mod_placement", BlockProtection.mayPlace(player, level, new BlockPos(cx, Y + 1, cz - 80)),
                        "outside any claim");
                // Destruction magic uses vanilla explosions with the caster as source; the claim must keep its blocks.
                level.explode(player, cx + 0.5, Y + 1.0, cz + 0.5, 3.0F, Level.ExplosionInteraction.BLOCK);
                level.explode(player, cx + 0.5, Y + 1.0, cz - 79.5, 3.0F, Level.ExplosionInteraction.BLOCK);
            });
        } else if (k == 70) {
            server(mc, (server, player) -> {
                ServerLevel level = server.overworld();
                int claimed = 0, open = 0;
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    if (level.getBlockState(new BlockPos(cx + x, Y, cz + z)).is(Blocks.STONE)) claimed++;
                    if (level.getBlockState(new BlockPos(cx + x, Y, cz - 80 + z)).is(Blocks.STONE)) open++;
                }
                check("claim_blocks_explosion", claimed == 9, claimed + " of 9 claimed stone blocks survived");
                check("unclaimed_explosion_breaks", open < 9, open + " of 9 unclaimed stone blocks survived");
            });
        }
    }

    private static void door(Minecraft mc, int k) {
        if (k == 0) {
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", px - 4, Y, pz - 3, px + 5, Y + 6, pz + 3));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", px - 4, Y - 1, pz - 3, px + 5, Y - 1, pz + 3));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d dorohedoro:reinforced_door_frame", px - 1, Y, pz, px + 2, Y + 4, pz));
            command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air", px, Y + 1, pz, px + 1, Y + 3, pz));
            command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:artificial_door_core", px, Y, pz));
            command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 10", px + 1.0, Y, pz + 2.5));
        } else if (k == 60) {
            boolean claimsLoaded = ModList.get().isLoaded("ftbchunks");
            server(mc, (server, player) -> {
                BlockPos core = new BlockPos(px, Y, pz);
                PortalService.activate(server.overworld(), core, PortalKind.ARTIFICIAL, player);
                boolean active = server.overworld().getBlockEntity(core) instanceof PortalCoreBlockEntity be && be.active();
                if (claimsLoaded) check("claim_blocks_door_far_end", !active, active ? "door opened into the claim" : "door refused");
                else log("SKIP claim_blocks_door_far_end: FTB Chunks is not installed");
            });
        } else if (k == 70) shot(mc, "ftb-05-door-refused");
        else if (k == 80 && ModList.get().isLoaded("ftbchunks")) {
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world positioned %d 100 %d run ftbchunks admin unclaim_as %s 48", px, pz, rivals));
            // Now the player's own team claims the same ground: their own door must still be allowed to open there.
            String own = ownTeam(mc);
            log("own team " + own);
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world positioned %d 100 %d run ftbchunks admin claim_as %s 48", px, pz, own));
        } else if (k == 120) {
            server(mc, (server, player) -> {
                BlockPos core = new BlockPos(px, Y, pz);
                if (!(server.overworld().getBlockEntity(core) instanceof PortalCoreBlockEntity be)) {
                    check("door_opens", false, "no core block entity");
                    return;
                }
                if (!be.active()) PortalService.activate(server.overworld(), core, PortalKind.ARTIFICIAL, player);
                farCore = be.active() ? be.destinationCore() : null;
                check(ModList.get().isLoaded("ftbchunks") ? "door_opens_into_own_claim" : "door_opens", be.active(),
                        be.active() ? "far end at " + be.destinationCore().toShortString() : "not active");
            });
        } else if (k == 140) shot(mc, "ftb-06-door-open");
        else if (k == 150) command(mc, String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 0", px + 1.0, Y + 1, pz + 0.5));
        else if (k > 150 && k < 300 && k % 20 == 0) {
            // Trace where the player is after crossing, to catch drift away from the far end.
            server(mc, (server, player) -> log("after crossing k=" + k + ": " + player.level().dimension().location() + " "
                    + player.blockPosition().toShortString() + " flying=" + player.getAbilities().flying + " motion=" + player.getDeltaMovement()));
        }
        else if (k == 300) {
            server(mc, (server, player) -> check("door_travel_to_sorcerers_world", player.level().dimension().equals(SORCERERS_WORLD),
                    "player is in " + player.level().dimension().location() + " at " + player.blockPosition().toShortString()));
            shot(mc, "ftb-07-sorcerers-world-far-end");
        } else if (k == 310 && farCore != null) {
            // Back through the generated return door. Its axis matches the source door (X).
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:sorcerers_world run tp @a %.1f %d %.1f 0 0",
                    farCore.getX() + 1.0, farCore.getY() + 1, farCore.getZ() + 0.5));
        } else if (k == 480) {
            server(mc, (server, player) -> check("door_travel_back_to_hole", player.level().dimension().equals(Level.OVERWORLD),
                    "player is in " + player.level().dimension().location() + " at " + player.blockPosition().toShortString()));
        } else if (k == 440) {
            // Finish the earlier door quests. The two quests already earned by opening and using the door must then
            // complete on their own: FTB kept their task progress while their prerequisites were unfinished.
            for (String give : List.of("smoke_condenser 1", "preserved_smoke_conduit 1", "artificial_door_core 1",
                    "reinforced_door_frame 13", "door_activation_tool 1")) {
                command(mc, "give @a dorohedoro:" + give);
            }
        } else if (k == 490) command(mc, "execute as @a run ftbquests open_book");
        else if (k == 515) shot(mc, "ftb-08-book-after-door");
    }

    /** The short name of the test player's own FTB team. */
    private static String ownTeam(Minecraft mc) {
        try {
            Object api = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI").getMethod("api").invoke(null);
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            var team = (java.util.Optional<?>) manager.getClass().getMethod("getTeamForPlayerID", java.util.UUID.class)
                    .invoke(manager, mc.player.getUUID());
            if (team.isPresent()) return (String) team.get().getClass().getMethod("getShortName").invoke(team.get());
        } catch (ReflectiveOperationException e) {
            log("own team lookup failed: " + e);
        }
        return "unknown";
    }

    /** The short name FTB Teams commands accept (getTeamByName), found by display name through FTB Teams' API. */
    private static String teamId(String name) {
        try {
            Object api = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI").getMethod("api").invoke(null);
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            for (Object team : (java.util.Collection<?>) manager.getClass().getMethod("getTeams").invoke(manager)) {
                Object display = team.getClass().getMethod("getName").invoke(team);
                if (name.equals(((net.minecraft.network.chat.Component) display).getString())) {
                    return (String) team.getClass().getMethod("getShortName").invoke(team);
                }
            }
        } catch (ReflectiveOperationException e) {
            log("team lookup failed: " + e);
        }
        return name;
    }

    private static void server(Minecraft mc, java.util.function.BiConsumer<MinecraftServer, ServerPlayer> action) {
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            log("FAIL no integrated server");
            return;
        }
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
