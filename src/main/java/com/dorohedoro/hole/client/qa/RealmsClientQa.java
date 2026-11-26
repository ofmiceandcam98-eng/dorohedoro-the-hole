package com.dorohedoro.hole.client.qa;

import static com.dorohedoro.hole.client.qa.ClientQa.closeScreen;
import static com.dorohedoro.hole.client.qa.ClientQa.command;
import static com.dorohedoro.hole.client.qa.ClientQa.log;
import static com.dorohedoro.hole.client.qa.ClientQa.shot;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.registry.ModBlocks;
import java.util.List;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Phase 3 in the real client ({@code -Pqa_mode=realms}): finds every Sorcerers' World and Hell biome with vanilla's
 * biome search, photographs each from above, and checks that neither realm can rain or snow, that Hell's ores are in its
 * rock, that Hell Rock mines and drops in survival, and that the Hydra Forest has real trees.
 * Delete {@code runs/qaclient/saves/clientqa-<seed>} first.
 */
final class RealmsClientQa {
    private RealmsClientQa() {}

    /** A biome to find and photograph. {@code biome} is a full id; Hell biomes are the mod's own. */
    private record Stop(String dimension, String biome) {
        String name() {
            return biome.substring(biome.indexOf(':') + 1);
        }
    }

    /**
     * New Sorcerers' Worlds use vanilla Overworld terrain and biomes (owner's direction, 2026-11-26), with vanilla dark
     * forest standing in for the Hydra Forest. Hell keeps its own biomes.
     */
    private static final List<Stop> STOPS = List.of(
            new Stop("sorcerers_world", "minecraft:plains"), new Stop("sorcerers_world", "minecraft:forest"),
            new Stop("sorcerers_world", "minecraft:dark_forest"), new Stop("sorcerers_world", "minecraft:snowy_plains"),
            new Stop("hell", "dorohedoro:hell_wastes"), new Stop("hell", "dorohedoro:hell_horns"),
            new Stop("hell", "dorohedoro:hell_soul_flats"));
    private static final int STEP = 420;
    private static final int TOUR = 80;
    private static final int CHECKS = TOUR + STOPS.size() * STEP;
    private static final BlockPos[] found = new BlockPos[STOPS.size()];

    static void script(Minecraft mc, int t) {
        if (t == 2) PacketDistributor.sendToServer(new CharacterNetwork.Submit("human", ""));
        else if (t == 40) {
            closeScreen(mc);
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doWeatherCycle false");
            command(mc, "time set 6000");
            // Rain in the Hole: the realms must still be dry.
            command(mc, "weather rain");
            command(mc, "gamemode spectator @a");
            mc.options.hideGui = true;
            server(mc, (server, player) -> {
                for (String dim : List.of("sorcerers_world", "hell")) {
                    ServerLevel level = server.getLevel(key(dim));
                    if (level == null) { check("realm_loaded_" + dim, false, "missing"); continue; }
                    long wet = level.registryAccess().registryOrThrow(Registries.BIOME).holders()
                            .filter(h -> h.key().location().getNamespace().equals("dorohedoro") && h.key().location().getPath().startsWith(dim.equals("hell") ? "hell_" : "sw_"))
                            .filter(h -> h.value().hasPrecipitation()).count();
                    check("realm_never_rains_" + dim, wet == 0, wet + " biomes with rain or snow");
                }
                // How often each biome appears: 64 x 64 surface samples, 256 blocks apart, straight from the biome source.
                for (String dim : List.of("sorcerers_world", "hell")) {
                    ServerLevel level = server.getLevel(key(dim));
                    if (level == null) continue;
                    var source = level.getChunkSource().getGenerator().getBiomeSource();
                    var sampler = level.getChunkSource().randomState().sampler();
                    java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
                    double[] lo = {9, 9, 9, 9, 9}, hi = {-9, -9, -9, -9, -9};
                    for (int i = -32; i < 32; i++) for (int j = -32; j < 32; j++) {
                        int x = i * 256, z = j * 256;
                        var b = source.getNoiseBiome(x >> 2, 80 >> 2, z >> 2, sampler);
                        counts.merge(b.unwrapKey().map(k -> k.location().getPath()).orElse("?"), 1, Integer::sum);
                        var point = new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, 0, z);
                        double[] v = {sampler.depth().compute(point), sampler.continentalness().compute(point),
                                sampler.temperature().compute(point), sampler.humidity().compute(point), sampler.weirdness().compute(point)};
                        for (int k = 0; k < 5; k++) { lo[k] = Math.min(lo[k], v[k]); hi[k] = Math.max(hi[k], v[k]); }
                    }
                    log("biome share in " + dim + " (4096 samples): " + counts);
                    log("noise ranges in " + dim + ": depth " + fmt(lo[0], hi[0]) + ", continents " + fmt(lo[1], hi[1])
                            + ", temperature " + fmt(lo[2], hi[2]) + ", humidity " + fmt(lo[3], hi[3]) + ", weirdness " + fmt(lo[4], hi[4]));
                }
            });
        } else if (t >= TOUR && t < CHECKS) {
            int n = (t - TOUR) / STEP, k = (t - TOUR) % STEP;
            Stop stop = STOPS.get(n);
            if (k == 0) {
                server(mc, (server, player) -> {
                    ServerLevel level = server.getLevel(key(stop.dimension));
                    if (level == null) return;
                    ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(stop.biome));
                    var hit = level.findClosestBiome3d(h -> h.is(biome), new BlockPos(n * 400, 80, 0), 4000, 32, 64);
                    if (hit == null) {
                        check("biome_found_" + stop.dimension + "_" + stop.name(), false, "none within 4000 blocks");
                        found[n] = null;
                        return;
                    }
                    BlockPos p = hit.getFirst();
                    int y = level.getChunk(p.getX() >> 4, p.getZ() >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING, p.getX() & 15, p.getZ() & 15) + 1;
                    found[n] = new BlockPos(p.getX(), y, p.getZ());
                    check("biome_found_" + stop.dimension + "_" + stop.name(), true, found[n].toShortString());
                });
            } else if (k == 10 && found[n] != null) {
                BlockPos p = found[n];
                int above = stop.dimension.equals("hell") ? 18 : 34;
                command(mc, String.format(Locale.ROOT, "execute in dorohedoro:%s run tp @a %d %d %d 30 32",
                        stop.dimension, p.getX() - 12, p.getY() + above, p.getZ() - 20));
            } else if (k == STEP - 20 && found[n] != null) shot(mc, "realm-" + (n < 9 ? "0" : "") + (n + 1) + "-" + stop.name());
        }
        // Checks that need the player standing in the realm.
        else if (t == CHECKS) {
            // The weather itself, long after /weather rain: it rains in the Hole, and must not in the Sorcerers' World.
            server(mc, (server, player) -> {
                ServerLevel sw = server.getLevel(key("sorcerers_world"));
                if (sw == null) return;
                boolean holeRain = server.overworld().isRaining();
                check("realm_dry_while_hole_rains_sorcerers_world", holeRain && !sw.isRaining() && sw.getRainLevel(1F) == 0F,
                        "Hole raining " + holeRain + ", here raining " + sw.isRaining() + ", rain level " + sw.getRainLevel(1F));
            });
            int hydra = STOPS.indexOf(new Stop("sorcerers_world", "minecraft:dark_forest"));
            server(mc, (server, player) -> {
                if (found[hydra] == null) return;
                ServerLevel level = server.getLevel(key("sorcerers_world"));
                int logs = count(level, found[hydra], 24, 50, 140, Blocks.DARK_OAK_LOG, Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG);
                check("hydra_forest_has_trees", logs >= 20, logs + " logs within 24 blocks");
            });
            int wastes = STOPS.indexOf(new Stop("hell", "dorohedoro:hell_wastes"));
            server(mc, (server, player) -> {
                if (found[wastes] == null) return;
                ServerLevel hell = server.getLevel(key("hell"));
                BlockPos c = found[wastes];
                int ores = count(hell, c, 32, -60, c.getY(), Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
                int rock = count(hell, c, 8, c.getY() - 12, c.getY(), ModBlocks.HELL_ROCK.get());
                check("hell_ores_generate", ores >= 20, ores + " ore blocks within 32 blocks");
                check("hell_is_hell_rock", rock >= 200, rock + " Hell Rock blocks just under the surface");
            });
        } else if (t == CHECKS + 20) {
            int wastes = STOPS.indexOf(new Stop("hell", "dorohedoro:hell_wastes"));
            if (found[wastes] == null) return;
            BlockPos c = found[wastes];
            command(mc, "gamemode survival @a");
            command(mc, String.format(Locale.ROOT, "execute in dorohedoro:hell run tp @a %d %d %d", c.getX(), c.getY() + 2, c.getZ()));
            command(mc, "give @a minecraft:iron_pickaxe");
        } else if (t == CHECKS + 80) {
            server(mc, (server, player) -> {
                if (!player.level().dimension().location().getPath().equals("hell")) {
                    check("hell_rock_mines_in_survival", false, "player not in Hell");
                    return;
                }
                ServerLevel hell = (ServerLevel) player.level();
                BlockPos target = null;
                for (int dy = 0; dy > -12 && target == null; dy--) {
                    BlockPos p = player.blockPosition().offset(1, dy, 0);
                    if (hell.getBlockState(p).is(ModBlocks.HELL_ROCK.get())) target = p;
                }
                if (target == null) { check("hell_rock_mines_in_survival", false, "no Hell Rock next to the player"); return; }
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
                boolean broke = player.gameMode.destroyBlock(target);
                int drops = hell.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(2),
                        e -> e.getItem().is(ModBlocks.HELL_ROCK.get().asItem())).size();
                check("hell_rock_mines_in_survival", broke && drops >= 1, "broken " + broke + ", " + drops + " Hell Rock dropped");
            });
        } else if (t == CHECKS + 100) {
            mc.options.hideGui = false;
            command(mc, "gamemode spectator @a");
        } else if (t == CHECKS + 260) shot(mc, "realm-11-hell-from-the-ground");
        else if (t == CHECKS + 270) {
            command(mc, "weather clear");
            log("tour complete, quitting");
            mc.stop();
        }
    }

    private static String fmt(double lo, double hi) {
        return String.format(Locale.ROOT, "%.2f..%.2f", lo, hi);
    }

    private static ResourceKey<Level> key(String dimension) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dorohedoro", dimension));
    }

    private static int count(ServerLevel level, BlockPos c, int r, int y0, int y1, net.minecraft.world.level.block.Block... blocks) {
        int n = 0;
        for (int x = c.getX() - r; x <= c.getX() + r; x++) for (int z = c.getZ() - r; z <= c.getZ() + r; z++) for (int y = y0; y <= y1; y++) {
            var s = level.getBlockState(new BlockPos(x, y, z));
            for (var b : blocks) if (s.is(b)) { n++; break; }
        }
        return n;
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
