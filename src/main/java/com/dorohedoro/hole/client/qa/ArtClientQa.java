package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Art review scene ({@code -Ddorohedoro.clientqa.mode=art}, {@code ./gradlew runQaClient -Pqa_mode=art}).
 *
 * <p>Builds a gallery in the sky: every custom block as a 3x3 panel, then a 3x3 panel of its vanilla reference, in
 * groups of four pairs. It photographs every group in daylight, close up, from gameplay distance, at night, by torch
 * light, and enclosed under a ceiling (no sky light, torches only). A last group compares the Cavern Lamp standing,
 * hanging and wall-mounted with the vanilla lantern, soul lantern and wall torch, and a final shot shows the item
 * sprites in the hotbar. Screenshots are named {@code qa-art-<condition>-<group>.png}; the log lists each pair.
 * See docs/art/VISUAL_QA_REPORT.md.
 */
final class ArtClientQa {
    private ArtClientQa() {}

    /** Custom block, vanilla reference; four pairs per group. Order matches the report. */
    private static final String[][][] GROUPS = {
        {{"limestone", "calcite"}, {"hell_rock", "blackstone"}, {"ash_soil", "coarse_dirt"},
         {"industrial_debris", "gravel"}},
        {{"sewer_brickwork", "stone_bricks"}, {"soot_blackened_brick", "bricks"},
         {"seepage_brick", "mossy_stone_bricks"}, {"patchwork_masonry", "bricks"}},
        {{"smoke_stained_concrete", "light_gray_concrete"}, {"water_damaged_plaster", "white_terracotta"},
         {"rusted_plating", "iron_block"}, {"rusted_industrial_sheet", "exposed_copper"}},
        {{"galvanized_plating", "iron_block"}, {"chitin_plating", "moss_block"},
         {"zinc_ore", "iron_ore"}, {"deepslate_zinc_ore", "deepslate_iron_ore"}},
        {{"sulfur_ore", "gold_ore"}, {"rock_salt_ore", "diamond_ore"}, {"oil_shale_ore", "coal_ore"},
         {"deepslate_sulfur_ore", "deepslate_gold_ore"}},
        {{"deepslate_rock_salt_ore", "deepslate_diamond_ore"}, {"deepslate_oil_shale_ore", "deepslate_coal_ore"},
         {"reinforced_door_frame", "chiseled_stone_bricks"}, {"etched_gate_stone", "chiseled_tuff"}},
    };
    private static final int GROUP_W = 30;
    private static final int LAMPS = GROUPS.length;   // the lamp scene is the last group
    private static final int Y = 200;

    private static int x0, z0;
    private static final List<Runnable> PLAN = new ArrayList<>();
    private static final List<Integer> AT = new ArrayList<>();

    static void script(Minecraft mc, int t) {
        if (t == 1) {
            PacketDistributor.sendToServer(new CharacterNetwork.Submit("fate", ""));
        } else if (t == 40) {
            closeScreen(mc);
            x0 = (int) Math.floor(ClientQa.home.x) + 64;
            z0 = (int) Math.floor(ClientQa.home.z) + 64;
            plan(mc);
        }
        for (int i = 0; i < AT.size(); i++) {
            if (AT.get(i) == t) PLAN.get(i).run();
        }
    }

    private static int clock = 41;

    private static void at(int wait, Runnable r) {
        clock += wait;
        AT.add(clock);
        PLAN.add(r);
    }

    private static int gx(int g) {
        return x0 + g * GROUP_W;
    }

    private static void plan(Minecraft mc) {
        int x1 = gx(LAMPS) + GROUP_W;
        at(1, () -> {
            command(mc, "gamemode spectator @a");
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doWeatherCycle false");
            command(mc, "gamerule doMobSpawning false");
            command(mc, "weather clear");
            command(mc, "time set 6000");
            command(mc, cmd("tp @a %d %d %d 180 10", x0 + 14, Y + 1, z0 + 14));
        });
        // Let the area load around the camera, then force it loaded so every fill succeeds.
        at(100, () -> command(mc, cmd("forceload add %d %d %d %d", x0 - 4, z0 - 4, x1 + 4, z0 + 40)));
        at(80, () -> {
            command(mc, cmd("fill %d %d %d %d %d %d minecraft:smooth_stone", x0 - 3, Y, z0 - 2, x1 + 2, Y, z0 + 40));
            for (int g = 0; g < GROUPS.length; g++) {
                for (int i = 0; i < GROUPS[g].length; i++) {
                    int px = gx(g) + 1 + i * 7;
                    String mod = GROUPS[g][i][0], van = GROUPS[g][i][1];
                    command(mc, cmd("fill %d %d %d %d %d %d dorohedoro:%s", px, Y + 1, z0, px + 2, Y + 3, z0, mod));
                    command(mc, cmd("fill %d %d %d %d %d %d minecraft:%s", px + 3, Y + 1, z0, px + 5, Y + 3, z0, van));
                    log(String.format(Locale.ROOT, "art pair group %d slot %d: left dorohedoro:%s, right minecraft:%s",
                            g + 1, i + 1, mod, van));
                }
            }
            buildLamps(mc);
        });
        at(200, () -> mc.options.hideGui = true);

        String[] conditions = {"day", "close", "distance", "night", "torch", "enclosed"};
        for (String c : conditions) {
            at(1, () -> setUp(mc, c));
            for (int g = 0; g <= LAMPS; g++) {
                final int gg = g;
                at(2, () -> camera(mc, c, gg));
                at(g == 0 ? 160 : 90, () -> shot(mc, "art-" + c + "-" + (gg == LAMPS ? "lamps" : "g" + (gg + 1))));
            }
        }
        // Lamp close-ups: the standing trio, the hanging pair, the wall lamp beside a wall torch. Night, by
        // their own light.
        at(1, () -> {
            command(mc, "fill " + (x0 - 3) + " " + (Y + 6) + " " + (z0 - 2) + " " + (x1 + 2) + " " + (Y + 6) + " " + (z0 + 16) + " minecraft:air");
            command(mc, "time set 18000");
        });
        int lg = gx(LAMPS);
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 35", lg + 6.0, Y + 1.5, z0 + 5.5)));
        at(120, () -> shot(mc, "art-lamps-standing"));
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 -12", lg + 14.0, Y + 1.5, z0 + 5.5)));
        at(90, () -> shot(mc, "art-lamps-hanging"));
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 10", lg + 20.5, Y + 2.0, z0 + 4.5)));
        at(90, () -> shot(mc, "art-lamps-wall"));
        at(1, () -> command(mc, "time set 6000"));
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 35", lg + 6.0, Y + 1.5, z0 + 5.5)));
        at(90, () -> shot(mc, "art-lamps-standing-day"));
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 -12", lg + 14.0, Y + 1.5, z0 + 5.5)));
        at(90, () -> shot(mc, "art-lamps-hanging-day"));
        at(2, () -> command(mc, cmd("tp @a %.1f %.1f %.1f 180 10", lg + 20.5, Y + 2.0, z0 + 4.5)));
        at(90, () -> shot(mc, "art-lamps-wall-day"));
        // Item sprites at real hotbar scale.
        at(10, () -> {
            command(mc, "gamemode creative @a");
            command(mc, "clear @a");
            String[] items = {"dorohedoro:cavern_lamp", "minecraft:lantern", "minecraft:soul_lantern",
                "dorohedoro:raw_sulfur", "dorohedoro:rock_salt", "minecraft:raw_iron", "minecraft:iron_ingot",
                "dorohedoro:raw_zinc", "dorohedoro:zinc_ingot"};
            for (String item : items) command(mc, "give @a " + item);
            command(mc, cmd("tp @a %d %d %d 180 10", gx(LAMPS) + 10, Y + 1, z0 + 9));
            mc.options.hideGui = false;
        });
        at(120, () -> shot(mc, "art-hotbar"));
        at(2, () -> {
            command(mc, "clear @a");
            String[] items = {"dorohedoro:oil_shale_chunk", "minecraft:coal", "dorohedoro:devil_sigil",
                "dorohedoro:smoke_conduit", "dorohedoro:preserved_smoke_conduit", "dorohedoro:burrower_claw",
                "minecraft:bone", "dorohedoro:roach_antenna", "dorohedoro:brass_ingot"};
            for (String item : items) command(mc, "give @a " + item);
        });
        at(60, () -> shot(mc, "art-hotbar-2"));
        at(20, () -> {
            command(mc, cmd("forceload remove %d %d %d %d", x0 - 4, z0 - 4, x1 + 4, z0 + 40));
            log("art review complete, quitting");
            mc.stop();
        });
    }

    /** Lamp scene: a brick back wall, a beam to hang from, and the lamps in front of it. */
    private static void buildLamps(Minecraft mc) {
        int g = gx(LAMPS);
        command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone_bricks", g, Y + 1, z0, g + 22, Y + 5, z0));
        command(mc, cmd("fill %d %d %d %d %d %d minecraft:dark_oak_planks", g + 10, Y + 5, z0 + 1, g + 17, Y + 5, z0 + 3));
        command(mc, cmd("setblock %d %d %d dorohedoro:cavern_lamp", g + 3, Y + 1, z0 + 2));
        command(mc, cmd("setblock %d %d %d minecraft:lantern", g + 6, Y + 1, z0 + 2));
        command(mc, cmd("setblock %d %d %d minecraft:soul_lantern", g + 8, Y + 1, z0 + 2));
        command(mc, cmd("setblock %d %d %d dorohedoro:cavern_lamp[hanging=true]", g + 12, Y + 4, z0 + 2));
        command(mc, cmd("setblock %d %d %d minecraft:lantern[hanging=true]", g + 15, Y + 4, z0 + 2));
        command(mc, cmd("setblock %d %d %d dorohedoro:cavern_lamp[wall=true,facing=south]", g + 19, Y + 3, z0 + 1));
        command(mc, cmd("setblock %d %d %d minecraft:wall_torch[facing=south]", g + 21, Y + 3, z0 + 1));
        log("art lamps, left to right: cavern lamp standing, lantern, soul lantern, cavern lamp hanging, lantern hanging, "
                + "cavern lamp on wall, wall torch");
    }

    private static void setUp(Minecraft mc, String c) {
        int x1 = gx(LAMPS) + GROUP_W;
        switch (c) {
            case "night" -> command(mc, "time set 18000");
            case "torch" -> {
                for (int x = x0; x < gx(LAMPS); x += 4) {
                    command(mc, cmd("setblock %d %d %d minecraft:torch", x, Y + 1, z0 + 3));
                }
            }
            case "enclosed" -> {
                // A ceiling and end walls: the panels get no sky light, only the torches.
                command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone", x0 - 3, Y + 6, z0 - 2, x1 + 2, Y + 6, z0 + 16));
                command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone", x0 - 3, Y + 1, z0 - 2, x0 - 3, Y + 5, z0 + 16));
                command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone", x1 + 2, Y + 1, z0 - 2, x1 + 2, Y + 5, z0 + 16));
                command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone", x0 - 3, Y + 1, z0 + 16, x1 + 2, Y + 5, z0 + 16));
                command(mc, cmd("fill %d %d %d %d %d %d minecraft:stone", x0 - 3, Y + 1, z0 - 2, x1 + 2, Y + 5, z0 - 2));
            }
            default -> {
            }
        }
        log("art condition " + c);
    }

    private static void camera(Minecraft mc, String c, int g) {
        int cx = gx(g) + (g == LAMPS ? 11 : 14);
        switch (c) {
            case "close" -> command(mc, cmd("tp @a %d %d %.1f 180 8", g == LAMPS ? cx : gx(g) + 7, Y + 1, z0 + 4.5));
            case "distance" -> command(mc, cmd("tp @a %d %d %d 180 6", cx, Y + 3, z0 + 34));
            default -> command(mc, cmd("tp @a %d %d %d 180 8", cx, Y + 1, z0 + (g == LAMPS ? 10 : 13)));
        }
    }

    private static String cmd(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}
