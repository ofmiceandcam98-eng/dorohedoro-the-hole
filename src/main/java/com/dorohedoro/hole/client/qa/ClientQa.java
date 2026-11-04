package com.dorohedoro.hole.client.qa;

import com.dorohedoro.hole.Dorohedoro;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Unattended client test. With {@code -Ddorohedoro.clientqa=true} the real game client creates a fresh world through
 * the default world preset, flies a spectator camera to a few viewpoints, saves real screenshots under
 * {@code screenshots/}, and quits. Every screen the client shows is logged, so a warning dialog during world
 * creation is recorded rather than hidden. Without the property this class does nothing.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID, value = Dist.CLIENT)
public final class ClientQa {
    private static final boolean ENABLED = Boolean.getBoolean("dorohedoro.clientqa");
    private static final int RENDER_WAIT = 300;

    private static int stage;
    private static int ticks;
    private static String lastScreen = "";
    private static net.minecraft.world.phys.Vec3 home;

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
            default -> tour(mc);
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

    private static void tour(Minecraft mc) {
        switch (ticks) {
            case 1 -> {
                mc.options.hideGui = true;
                command(mc, "gamerule doDaylightCycle false");
                command(mc, "time set 1000");
                command(mc, "gamemode spectator @a");
            }
            case RENDER_WAIT - 20 -> {
                // The origin chooser opens on first join. Close it so the screenshots show the world.
                if (mc.screen != null) {
                    log("closing " + mc.screen.getClass().getSimpleName() + " before screenshots");
                    mc.setScreen(null);
                }
            }
            case RENDER_WAIT -> shot(mc, "01-spawn-street");
            case RENDER_WAIT + 1 -> command(mc, "execute as @a at @s run tp @s ~ ~40 ~ 135 35");
            case RENDER_WAIT * 2 -> shot(mc, "02-aerial");
            case RENDER_WAIT * 2 + 1 -> command(mc, "execute as @a at @s run tp @s ~ ~110 ~ 135 60");
            case RENDER_WAIT * 3 -> shot(mc, "03-overview");
            case RENDER_WAIT * 3 + 1 -> {
                command(mc, "time set 18000");
                // Return to the exact join point. A relative drop can leave a spectator camera inside the ground,
                // which renders the city from below and looks like a void under the buildings.
                command(mc, String.format(java.util.Locale.ROOT, "tp @a %.2f %.2f %.2f 225 10", home.x, home.y, home.z));
            }
            case RENDER_WAIT * 4 -> shot(mc, "04-night-street");
            case RENDER_WAIT * 4 + 1 -> smokeStorm(mc);
            case RENDER_WAIT * 4 + 12 -> shot(mc, "05-all-magic-smoke");
            case RENDER_WAIT * 4 + 20 -> {
                log("tour complete, quitting");
                mc.stop();
            }
            default -> {
            }
        }
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
        net.minecraft.world.phys.Vec3 at = mc.player.getEyePosition().add(mc.player.getLookAngle().scale(4.0));
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
