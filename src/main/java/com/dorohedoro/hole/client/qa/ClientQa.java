package com.dorohedoro.hole.client.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.client.character.CharacterCreationScreen;
import com.dorohedoro.hole.client.magic.SmokeLedgerScreen;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Unattended client test. With {@code -Ddorohedoro.clientqa=true} the real game client creates a fresh world through
 * the default world preset, goes through the real origin screen, flies a spectator camera around the city and into
 * furnished rooms, then switches to survival to photograph the HUD and every tab of the character record. Screenshots
 * land in {@code screenshots/}, every screen change is logged, and the client quits when done. Without the property
 * this class does nothing.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID, value = Dist.CLIENT)
public final class ClientQa {
    private static final boolean ENABLED = Boolean.getBoolean("dorohedoro.clientqa");
    /** Ticks to let chunks load and render after a long teleport. */
    private static final int RENDER_WAIT = 300;
    /** Ticks to let a screen or HUD change settle. */
    private static final int UI_WAIT = 40;

    private static int stage;
    private static int ticks;
    private static String lastScreen = "";
    private static Vec3 home;

    private ClientQa() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        String screen = mc.screen == null ? "none" : mc.screen.getClass().getSimpleName();
        if (!screen.equals(lastScreen)) {
            log("screen -> " + screen);
            lastScreen = screen;
        }
        ticks++;
        mc.options.pauseOnLostFocus = false;
        // First launch shows accessibility onboarding, which would otherwise wait forever for a click.
        if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
            mc.options.onboardAccessibility = false;
            mc.options.save();
            mc.setScreen(new TitleScreen());
            return;
        }
        switch (stage) {
            case 0 -> {
                if (mc.screen instanceof TitleScreen && ticks > 60) {
                    createWorld(mc);
                    stage = 1;
                    ticks = 0;
                }
            }
            case 1 -> {
                if (mc.level != null && mc.player != null) {
                    log("joined world at " + mc.player.blockPosition().toShortString());
                    home = mc.player.position();
                    stage = 2;
                    ticks = 0;
                } else if (ticks > 20 * 900) {
                    log("TIMED OUT waiting for the world to open; last screen " + screen);
                    mc.stop();
                }
            }
            default -> script(mc);
        }
    }

    private static void createWorld(Minecraft mc) {
        long seed = Long.getLong("dorohedoro.clientqa.seed", 8675309L);
        String name = "clientqa-" + seed;
        log("creating world " + name + " with the default preset");
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.NORMAL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(seed, true, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.NORMAL).value().createWorldDimensions(),
                mc.screen);
    }

    // Timeline, in ticks after joining.
    private static final int ORIGIN = 40;
    private static final int TOUR = ORIGIN + 120;
    private static final int ROOMS = TOUR + RENDER_WAIT * 4 + 40;
    private static final int HUD = ROOMS + RENDER_WAIT * 3 + 40;
    private static final int MENU = HUD + RENDER_WAIT + 20;

    private static void script(Minecraft mc) {
        int t = ticks;
        // Origin: photograph the real choice screen, then let fate pick a smoke family through the real packet.
        if (t == 1) {
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "time set 1000");
        } else if (t == ORIGIN) {
            if (mc.screen instanceof CharacterCreationScreen) {
                shot(mc, "00-origin-choice");
            } else {
                log("origin screen did not open; screen was " + lastScreen);
            }
        } else if (t == ORIGIN + 2) {
            PacketDistributor.sendToServer(new CharacterNetwork.Submit("fate", ""));
        } else if (t == ORIGIN + 60) {
            shot(mc, "00-origin-result");
        } else if (t == ORIGIN + 70) {
            closeScreen(mc);
            command(mc, "gamemode spectator @a");
            mc.options.hideGui = true;
        }
        // City tour from the spawn point, then every smoke type at once.
        else if (t == TOUR) shot(mc, "01-spawn-street");
        else if (t == TOUR + 1) command(mc, "execute as @a at @s run tp @s ~ ~40 ~ 135 35");
        else if (t == TOUR + RENDER_WAIT) shot(mc, "02-aerial");
        else if (t == TOUR + RENDER_WAIT + 1) command(mc, "execute as @a at @s run tp @s ~ ~110 ~ 135 60");
        else if (t == TOUR + RENDER_WAIT * 2) shot(mc, "03-overview");
        else if (t == TOUR + RENDER_WAIT * 2 + 1) {
            command(mc, "time set 18000");
            command(mc, tpHome(225, 10));
        } else if (t == TOUR + RENDER_WAIT * 3) shot(mc, "04-night-street");
        else if (t == TOUR + RENDER_WAIT * 3 + 1) smokeStorm(mc);
        else if (t == TOUR + RENDER_WAIT * 3 + 12) shot(mc, "05-all-magic-smoke");
        else if (t == TOUR + RENDER_WAIT * 3 + 20) command(mc, "time set 1000");
        // Inside towers. Coordinates are stair flights found by the server QA on this seed; the camera stands in the
        // room beside each flight.
        else if (t == ROOMS) command(mc, "tp @a 4501.5 79.2 865.5 -40 18");
        else if (t == ROOMS + RENDER_WAIT) shot(mc, "06-room-a");
        else if (t == ROOMS + RENDER_WAIT + 1) command(mc, "tp @a 4506.5 79.2 867.5 130 8");
        else if (t == ROOMS + RENDER_WAIT + 60) shot(mc, "07-stairs-a");
        else if (t == ROOMS + RENDER_WAIT + 61) command(mc, "tp @a 4574.5 79.2 876.5 -40 18");
        else if (t == ROOMS + RENDER_WAIT * 2) shot(mc, "08-room-b");
        else if (t == ROOMS + RENDER_WAIT * 2 + 1) command(mc, "tp @a 4505.5 83.2 866.5 -40 18");
        else if (t == ROOMS + RENDER_WAIT * 3) shot(mc, "08b-room-a-upstairs");
        // Survival HUD: armor, a few levels, a fresh hit (the draining damage ghost) and absorption.
        else if (t == HUD) {
            mc.options.hideGui = false;
            command(mc, "gamemode survival @a");
            command(mc, tpHome(160, 5));
            command(mc, "item replace entity @a armor.chest with minecraft:iron_chestplate");
            command(mc, "item replace entity @a armor.head with minecraft:chainmail_helmet");
            command(mc, "xp add @a 7 levels");
            command(mc, "xp add @a 20 points");
        } else if (t == HUD + RENDER_WAIT - 40) {
            command(mc, "effect give @a minecraft:absorption 60 0");
            command(mc, "damage @a 7 minecraft:generic");
        } else if (t == HUD + RENDER_WAIT - 34) shot(mc, "09-hud-survival");
        // Every tab of the character record.
        else if (t == MENU) mc.setScreen(new SmokeLedgerScreen());
        else if (t == MENU + UI_WAIT) shot(mc, "10-menu-status");
        else if (t == MENU + UI_WAIT + 1) key(mc, GLFW.GLFW_KEY_2);
        else if (t == MENU + UI_WAIT * 2) shot(mc, "11-menu-techniques");
        else if (t == MENU + UI_WAIT * 2 + 1) key(mc, GLFW.GLFW_KEY_3);
        else if (t == MENU + UI_WAIT * 3) shot(mc, "12-menu-loadouts");
        else if (t == MENU + UI_WAIT * 3 + 1) key(mc, GLFW.GLFW_KEY_4);
        else if (t == MENU + UI_WAIT * 4) shot(mc, "13-menu-body");
        // The item and block art at vanilla scale: an inventory of mod items and a wall of mod blocks.
        else if (t == MENU + UI_WAIT * 4 + 10) {
            closeScreen(mc);
            command(mc, "clear @a");
            for (String id : ITEMS) command(mc, "give @a dorohedoro:" + id);
            // The sample wall floats in the air above spawn so it can never break into anything the city built.
            for (int i = 0; i < BLOCKS.length; i++) {
                int bx = (int) Math.floor(home.x) - 4 + i % 8, by = (int) Math.floor(home.y) + WALL_LIFT + i / 8;
                int bz = (int) Math.floor(home.z) - 4;
                var before = mc.level.getBlockState(new net.minecraft.core.BlockPos(bx, by, bz));
                if (!before.isAir()) log("block wall replaces " + before + " at " + bx + "," + by + "," + bz);
                command(mc, String.format(Locale.ROOT, "setblock %d %d %d dorohedoro:%s", bx, by, bz, BLOCKS[i]));
            }
        } else if (t == MENU + UI_WAIT * 5) {
            mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        } else if (t == MENU + UI_WAIT * 6) shot(mc, "14-items");
        else if (t == MENU + UI_WAIT * 6 + 1) {
            closeScreen(mc);
            mc.options.hideGui = true;
            command(mc, "gamemode spectator @a");
            command(mc, String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 180 0", home.x - 0.5, home.y + WALL_LIFT, home.z + 1.5));
        } else if (t == MENU + UI_WAIT * 8) shot(mc, "15-blocks");
        // Every mod mob, frozen on a platform in daylight, photographed close up one by one.
        else if (t == MOBS) lineUpMobs(mc);
        else if (t > MOBS && t <= MOBS + MOB_SHOT * MOB_VIEWS && (t - MOBS) % MOB_SHOT == 0) {
            int n = (t - MOBS) / MOB_SHOT - 1, i = n / ANGLES.length, a = n % ANGLES.length;
            shot(mc, String.format(Locale.ROOT, "16-mob-%d-%s-%d-%s", i + 1, MOB_IDS[i], a + 1, ANGLES[a]));
            if (n + 1 < MOB_VIEWS) command(mc, mobCamera((n + 1) / ANGLES.length, (n + 1) % ANGLES.length));
        } else if (t == MOBS + MOB_SHOT * MOB_VIEWS + 1) {
            // The glow moth at night: its markings should shine on their own.
            command(mc, "time set 18000");
            command(mc, mobCamera(3, 0));
        } else if (t == MOBS + MOB_SHOT * MOB_VIEWS + 40) shot(mc, "17-glow-moth-night");
        else if (t == MOBS + MOB_SHOT * MOB_VIEWS + 50) {
            log("tour complete, quitting");
            mc.stop();
        }
    }

    /** Height of the sample block wall above spawn, clear of every building and fixture. */
    private static final int WALL_LIFT = 14;

    private static final String[] ITEMS = {"scrap_metal", "smoke_residue", "smoke_condenser", "smoke_conduit",
            "door_activation_tool", "door_linking_key", "devil_sigil", "ritual_tool", "raw_sulfur", "rock_salt",
            "reinforced_chitin", "roach_antenna", "rat_fur", "raw_rat_meat", "cooked_rat_meat", "salted_rat_ration",
            "sewer_echo_probe", "medical_salve", "luminous_scale", "moth_silk", "glow_tonic", "zinc_ingot", "brass_ingot",
            "bitumen", "sludge_gland", "spore_sac", "antidote_poultice", "empty_smoke_flask", "healing_smoke_flask",
            "time_smoke_flask", "severed_left_arm", "cooked_right_leg", "surgical_reattachment_kit", "metallic_carapace",
            "burrower_claw", "dense_hide"};
    private static final String[] BLOCKS = {"industrial_debris", "rusted_plating", "hell_rock", "ash_soil",
            "sewer_brickwork", "sulfur_ore", "rock_salt_ore", "limestone", "chitin_plating", "zinc_ore", "oil_shale_ore",
            "galvanized_plating", "deepslate_sulfur_ore", "deepslate_rock_salt_ore", "deepslate_zinc_ore",
            "deepslate_oil_shale_ore", "smoke_stained_concrete", "water_damaged_plaster", "soot_blackened_brick",
            "patchwork_masonry", "rusted_industrial_sheet", "seepage_brick", "etched_gate_stone", "reinforced_door_frame",
            "condensed_smoke_barrier", "artificial_door_core", "infernal_door_core", "portal_surface"};

    private static final int MOBS = MENU + UI_WAIT * 8 + 20;
    private static final int MOB_SHOT = 24;
    /** Every mob from the front, its left side, behind, and above at three-quarters. */
    private static final String[] ANGLES = {"front", "side", "back", "above"};
    private static final String[] MOB_IDS = {"sewer_roach", "scrap_rat", "sewer_leech", "glow_moth", "sludge_crawler",
            "rust_eater_beetle", "cavern_burrower", "spore_crawler"};
    private static final int MOB_VIEWS = 8 * 4;

    private static int mobX(int i) { return (int) Math.floor(home.x) + 3 + i * 6; }
    private static int mobY() { return (int) Math.floor(home.y) + 30; }
    private static int mobZ() { return (int) Math.floor(home.z) + 13; }

    private static void lineUpMobs(Minecraft mc) {
        command(mc, "time set 6000");
        int x0 = (int) Math.floor(home.x), y = mobY(), z0 = (int) Math.floor(home.z) + 10;
        command(mc, String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:smooth_stone", x0, y, z0, x0 + 50, y, z0 + 6));
        for (int i = 0; i < MOB_IDS.length; i++) {
            command(mc, String.format(Locale.ROOT, "summon dorohedoro:%s %.1f %d %.1f {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[0f,0f]}",
                    MOB_IDS[i], mobX(i) + 0.5, y + 1, mobZ() + 0.5));
        }
        command(mc, mobCamera(0, 0));
    }

    /** Camera for mob {@code i} at view {@code a}: 2.8 blocks out, looking back at it. */
    private static String mobCamera(int i, int a) {
        double x = mobX(i) + 0.5, z = mobZ() + 0.5, y = mobY() + 1;
        return switch (a) {
            case 0 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 180 24", x, y, z + 2.8);
            case 1 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 90 24", x + 2.8, y, z);
            case 2 -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 0 24", x, y, z - 2.8);
            default -> String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 135 52", x + 2.0, y + 2.4, z + 2.0);
        };
    }

    private static String tpHome(float yaw, float pitch) {
        return String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f %.0f %.0f", home.x, home.y, home.z, yaw, pitch);
    }

    private static void closeScreen(Minecraft mc) {
        if (mc.screen != null) {
            log("closing " + mc.screen.getClass().getSimpleName());
            mc.setScreen(null);
        }
    }

    private static void key(Minecraft mc, int key) {
        if (mc.screen != null) mc.screen.keyPressed(key, 0, 0);
    }

    private static void command(Minecraft mc, String command) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            log("no integrated server for: " + command);
            return;
        }
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
    }

    private static void shot(Minecraft mc, String name) {
        String pos = mc.player == null ? "?" : mc.player.blockPosition().toShortString();
        Screenshot.grab(mc.gameDirectory, "qa-" + name + ".png", mc.getMainRenderTarget(),
                message -> log("screenshot " + name + " at " + pos + ": " + message.getString()));
    }

    private static void log(String message) {
        Dorohedoro.LOGGER.info("CLIENTQA {}", message);
    }

    /**
     * Sends one smoke cue for every magic type at once, in front of the camera. Several magic types rendering in
     * the same frame is what used to crash SmokeMeshClient with "Not building!".
     */
    private static void smokeStorm(Minecraft mc) {
        if (mc.player == null) {
            return;
        }
        Vec3 at = mc.player.getEyePosition().add(mc.player.getLookAngle().scale(4.0));
        java.util.EnumSet<com.dorohedoro.hole.character.MagicType> seen = java.util.EnumSet.noneOf(com.dorohedoro.hole.character.MagicType.class);
        int sent = 0;
        for (com.dorohedoro.hole.magic.SmokeSkill skill : com.dorohedoro.hole.magic.SmokeSkill.values()) {
            if (seen.add(skill.magic())) {
                com.dorohedoro.hole.client.magic.SmokeMeshClient.receive(mc.player.getId() + ",-1," + at.x + "," + at.y + "," + at.z
                        + ",500," + skill.id() + ",0");
                sent++;
            }
        }
        log("smoke storm: sent " + sent + " magic types in one frame");
    }
}
