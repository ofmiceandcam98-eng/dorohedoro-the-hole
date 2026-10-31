package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.District;
import com.dorohedoro.hole.worldgen.city.Lot;
import com.dorohedoro.hole.worldgen.city.LotType;
import com.dorohedoro.hole.worldgen.city.Rect;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Headless world validation. When started with {@code -Ddorohedoro.qa=true} on a dedicated server it generates a region
 * around spawn, measures it, writes {@code qa/report.json} and {@code qa/map.png} under the server directory, then stops
 * the server. Nothing runs without that property, so this has no effect on normal play.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class HoleQa {
    private static final String PROPERTY = "dorohedoro.qa";
    private static final int CITY_LAYER_BOTTOM = CityPlan.STREET_Y;

    private HoleQa() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }
        MinecraftServer server = event.getServer();
        try {
            new Run(server, Integer.getInteger(PROPERTY + ".radius", 16), System.getProperty(PROPERTY + ".order", "forward")).execute();
        } catch (Exception e) {
            Dorohedoro.LOGGER.error("QA run failed", e);
        } finally {
            server.halt(false);
        }
    }

    private static final class Run {
        private final MinecraftServer server;
        private final ServerLevel level;
        private final int radius;
        private final String order;
        private final JsonObject report = new JsonObject();
        private final JsonArray checks = new JsonArray();
        private int failures;

        Run(MinecraftServer server, int radius, String order) {
            this.server = server;
            this.level = server.overworld();
            this.radius = radius;
            this.order = order;
        }

        void execute() throws IOException {
            report.addProperty("seed", level.getSeed());
            report.addProperty("radius_chunks", radius);
            report.addProperty("order", order);
            boolean isHole = level.getChunkSource().getGenerator() instanceof HoleChunkGenerator;
            check("generator_is_hole", isHole, level.getChunkSource().getGenerator().getClass().getSimpleName());
            if (!isHole) {
                write(null);
                return;
            }
            HoleChunkGenerator generator = (HoleChunkGenerator) level.getChunkSource().getGenerator();
            CityPlan plan = generator.plan(level);

            List<ChunkPos> positions = positions();
            long start = System.nanoTime();
            JsonObject hashes = new JsonObject();
            JsonObject fullHashes = new JsonObject();
            Map<ChunkPos, LevelChunk> chunks = new HashMap<>();
            for (ChunkPos pos : positions) {
                chunks.put(pos, level.getChunk(pos.x, pos.z));
            }
            double seconds = (System.nanoTime() - start) / 1.0e9;
            report.addProperty("generation_seconds", seconds);
            report.addProperty("chunks", positions.size());
            report.addProperty("ms_per_chunk", seconds * 1000.0 / positions.size());

            List<ChunkPos> sorted = new ArrayList<>(positions);
            sorted.sort((a, b) -> a.x != b.x ? Integer.compare(a.x, b.x) : Integer.compare(a.z, b.z));
            for (ChunkPos pos : sorted) {
                hashes.addProperty(pos.x + "," + pos.z, Long.toHexString(hash(chunks.get(pos), CITY_LAYER_BOTTOM)));
                fullHashes.addProperty(pos.x + "," + pos.z, Long.toHexString(hash(chunks.get(pos), Integer.MIN_VALUE)));
            }
            report.add("chunk_hashes", hashes);
            report.add("chunk_hashes_full", fullHashes);

            dumpChunks(chunks);
            checkSpawn(plan);
            checkBiomes();
            Rect area = new Rect(-radius * 16, -radius * 16, radius * 16 + 15, radius * 16 + 15);
            checkConnectivity(plan, area);
            census(chunks, area, plan);
            checkFluids(chunks);
            checkCityMaterials(chunks);
            write(area);
        }

        /** Generation order is the point of the determinism test: the same chunks are requested in opposite orders. */
        private List<ChunkPos> positions() {
            List<ChunkPos> list = new ArrayList<>();
            for (int ring = 0; ring <= radius; ring++) {
                for (int x = -ring; x <= ring; x++) {
                    for (int z = -ring; z <= ring; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) == ring) {
                            list.add(new ChunkPos(x, z));
                        }
                    }
                }
            }
            if ("reverse".equals(order)) {
                Collections.reverse(list);
            }
            return list;
        }

        /**
         * Hashes block states from {@code bottom} upward. The city layer is hashed apart from the deep underground
         * because that is the part this mod's generator owns: vanilla feature placement can vary slightly with order.
         */
        private static long hash(LevelChunk chunk, int bottom) {
            long hash = 1125899906842597L;
            LevelChunkSection[] sections = chunk.getSections();
            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                if (section.hasOnlyAir()) {
                    hash = hash * 31 + 7;
                    continue;
                }
                for (int y = 0; y < 16; y++) {
                    if (baseY + y < bottom) {
                        continue;
                    }
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            hash = hash * 31 + Block.getId(section.getBlockState(x, y, z));
                        }
                    }
                }
            }
            return hash;
        }

        /** Writes every block of the chunks named by {@code dorohedoro.qa.dump} ("x,z;x,z"), for diffing two runs. */
        private void dumpChunks(Map<ChunkPos, LevelChunk> chunks) throws IOException {
            String wanted = System.getProperty(PROPERTY + ".dump", "");
            if (wanted.isEmpty()) {
                return;
            }
            Path directory = server.getServerDirectory().resolve("qa");
            Files.createDirectories(directory);
            for (String entry : wanted.split(";")) {
                String[] parts = entry.split(",");
                ChunkPos pos = new ChunkPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                LevelChunk chunk = chunks.get(pos);
                StringBuilder out = new StringBuilder();
                for (int y = level.getMinBuildHeight(); y < 140; y++) {
                    for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                        for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                            out.append(x).append(' ').append(y).append(' ').append(z).append(' ')
                                    .append(chunk.getBlockState(new BlockPos(x, y, z))).append(System.lineSeparator());
                        }
                    }
                }
                Files.writeString(directory.resolve("dump-" + order + "-" + pos.x + "_" + pos.z + ".txt"), out.toString());
            }
        }

        private void checkSpawn(CityPlan plan) {
            BlockPos spawn = level.getSharedSpawnPos();
            report.addProperty("spawn", spawn.toShortString());
            check("spawn_at_plaza", Math.abs(spawn.getX()) <= 13 && Math.abs(spawn.getZ()) <= 13 && spawn.getY() == CityPlan.GROUND_Y, spawn.toShortString());
            boolean feet = level.getBlockState(spawn).isAir();
            boolean head = level.getBlockState(spawn.above()).isAir();
            boolean floor = level.getBlockState(spawn.below()).isFaceSturdy(level, spawn.below(), net.minecraft.core.Direction.UP);
            check("spawn_standable", feet && head && floor, "feet=" + feet + " head=" + head + " floor=" + floor);
            boolean roofless = level.canSeeSky(spawn);
            check("spawn_open_sky", roofless, String.valueOf(roofless));
        }

        private void checkBiomes() {
            Set<ResourceLocation> surface = new HashSet<>();
            int span = radius * 16;
            for (int x = -span; x <= span; x += 8) {
                for (int z = -span; z <= span; z += 8) {
                    level.getBiome(new BlockPos(x, CityPlan.GROUND_Y, z)).unwrapKey().ifPresent(key -> surface.add(key.location()));
                }
            }
            JsonArray seen = new JsonArray();
            boolean allHole = true;
            for (ResourceLocation id : new java.util.TreeSet<>(surface)) {
                seen.add(id.toString());
                allHole &= id.getNamespace().equals(Dorohedoro.MOD_ID);
            }
            report.add("surface_biomes", seen);
            check("no_vanilla_surface_biome", allHole, seen.toString());
        }

        private boolean walkable(int x, int y, int z) {
            BlockState feet = level.getBlockState(new BlockPos(x, y, z));
            BlockState head = level.getBlockState(new BlockPos(x, y + 1, z));
            BlockPos below = new BlockPos(x, y - 1, z);
            BlockState ground = level.getBlockState(below);
            return passable(feet) && passable(head) && !ground.getCollisionShape(level, below).isEmpty() && ground.getFluidState().isEmpty();
        }

        private static boolean passable(BlockState state) {
            return (state.isAir() || state.is(BlockTags.RAILS) || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS)
                    || state.is(BlockTags.WOOL_CARPETS) || state.is(Blocks.LADDER) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS)
                    || state.is(BlockTags.CROPS) || state.is(Blocks.SHORT_GRASS)) && state.getFluidState().isEmpty();
        }

        /**
         * Flood-fills on foot from spawn across street level, stepping up or down one block at a time. A street column
         * the fill never reaches is a sealed road, a broken crossing or a seam.
         */
        private void checkConnectivity(CityPlan plan, Rect area) {
            int width = area.width();
            BitSet visited = new BitSet(width * area.depth());
            ArrayDeque<long[]> queue = new ArrayDeque<>();
            BlockPos spawn = level.getSharedSpawnPos();
            queue.add(new long[]{spawn.getX(), spawn.getY(), spawn.getZ()});
            visited.set(columnIndex(area, spawn.getX(), spawn.getZ()));
            int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            while (!queue.isEmpty()) {
                long[] here = queue.poll();
                for (int[] step : steps) {
                    int x = (int) here[0] + step[0];
                    int z = (int) here[2] + step[1];
                    if (!area.contains(x, z)) {
                        continue;
                    }
                    int index = columnIndex(area, x, z);
                    if (visited.get(index)) {
                        continue;
                    }
                    int y = (int) here[1];
                    for (int candidate : new int[]{y, y + 1, y - 1}) {
                        if (candidate >= CityPlan.GROUND_Y - 1 && candidate <= CityPlan.GROUND_Y + 3 && walkable(x, candidate, z)) {
                            visited.set(index);
                            queue.add(new long[]{x, candidate, z});
                            break;
                        }
                    }
                }
            }

            Set<Long> lotColumns = new HashSet<>();
            int enclosedTotal = 0;
            int enclosedReached = 0;
            Map<LotType, Integer> lotTypes = new EnumMap<>(LotType.class);
            Map<District, Integer> districtBlocks = new EnumMap<>(District.class);
            Set<Long> seenLots = new HashSet<>();
            for (int cx = area.x0() >> 4; cx <= area.x1() >> 4; cx++) {
                for (int cz = area.z0() >> 4; cz <= area.z1() >> 4; cz++) {
                    for (Lot lot : plan.lotsIn(new ChunkPos(cx, cz))) {
                        if (!seenLots.add(lot.hash())) {
                            continue;
                        }
                        lotTypes.merge(lot.type(), 1, Integer::sum);
                        districtBlocks.merge(lot.district(), lot.rect().area(), Integer::sum);
                        Rect r = lot.rect();
                        for (int x = r.x0(); x <= r.x1(); x++) {
                            for (int z = r.z0(); z <= r.z1(); z++) {
                                lotColumns.add(((long) x << 32) | (z & 0xFFFFFFFFL));
                            }
                        }
                        boolean inside = r.x0() >= area.x0() && r.x1() <= area.x1() && r.z0() >= area.z0() && r.z1() <= area.z1();
                        if (lot.type().enclosed && inside) {
                            enclosedTotal++;
                            Rect interior = r.inset(2);
                            boolean reached = false;
                            for (int x = interior.x0(); x <= interior.x1() && !reached; x++) {
                                for (int z = interior.z0(); z <= interior.z1() && !reached; z++) {
                                    reached = visited.get(columnIndex(area, x, z));
                                }
                            }
                            if (reached) {
                                enclosedReached++;
                            }
                        }
                    }
                }
            }

            int street = 0;
            int reachedStreet = 0;
            JsonArray missed = new JsonArray();
            for (int x = area.x0(); x <= area.x1(); x++) {
                for (int z = area.z0(); z <= area.z1(); z++) {
                    if (lotColumns.contains(((long) x << 32) | (z & 0xFFFFFFFFL))) {
                        continue;
                    }
                    BlockState top = level.getBlockState(new BlockPos(x, CityPlan.STREET_Y, z));
                    if (!top.isSolidRender(level, new BlockPos(x, CityPlan.STREET_Y, z))) {
                        continue;
                    }
                    if (!walkable(x, CityPlan.GROUND_Y, z)) {
                        continue;
                    }
                    street++;
                    if (visited.get(columnIndex(area, x, z))) {
                        reachedStreet++;
                    } else if (missed.size() < 12) {
                        missed.add(x + "," + z);
                    }
                }
            }
            double fraction = street == 0 ? 0.0 : (double) reachedStreet / street;
            report.addProperty("street_columns", street);
            report.addProperty("street_reached_fraction", fraction);
            report.add("street_missed_sample", missed);
            check("streets_reachable_from_spawn", fraction >= 0.995, String.format("%.4f of %d", fraction, street));

            report.addProperty("enclosed_lots", enclosedTotal);
            report.addProperty("enclosed_lots_reached", enclosedReached);
            double enclosedFraction = enclosedTotal == 0 ? 0.0 : (double) enclosedReached / enclosedTotal;
            check("enclosed_lots_enterable", enclosedTotal > 0 && enclosedFraction >= 0.95, enclosedReached + "/" + enclosedTotal);

            JsonObject types = new JsonObject();
            lotTypes.forEach((type, count) -> types.addProperty(type.name(), count));
            report.add("lot_types", types);
            JsonObject districts = new JsonObject();
            districtBlocks.forEach((district, blocks) -> districts.addProperty(district.name(), blocks));
            report.add("district_blocks", districts);
            check("multiple_districts", districtBlocks.size() >= 3, districtBlocks.keySet().toString());
        }

        private static int columnIndex(Rect area, int x, int z) {
            return (x - area.x0()) * area.depth() + (z - area.z0());
        }

        /** Counts what survival needs, so a missing resource fails the run rather than surfacing hours into a playthrough. */
        private void census(Map<ChunkPos, LevelChunk> chunks, Rect area, CityPlan plan) {
            Map<Block, Integer> counts = new HashMap<>();
            for (LevelChunk chunk : chunks.values()) {
                for (LevelChunkSection section : chunk.getSections()) {
                    if (section.hasOnlyAir()) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                Block block = section.getBlockState(x, y, z).getBlock();
                                if (block != Blocks.AIR && block != Blocks.STONE && block != Blocks.DEEPSLATE) {
                                    counts.merge(block, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
            JsonObject census = new JsonObject();
            new TreeMap<>(Map.copyOf(names(counts))).forEach(census::addProperty);
            report.add("block_census", census);

            need(counts, "coal", 1, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
            need(counts, "iron", 1, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
            need(counts, "copper", 1, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
            need(counts, "gold", 1, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
            need(counts, "redstone", 1, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
            need(counts, "lapis", 1, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
            need(counts, "diamond", 1, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
            need(counts, "emerald", 1, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
            need(counts, "dirt", 1, Blocks.DIRT, Blocks.COARSE_DIRT);
            need(counts, "gravel", 1, Blocks.GRAVEL);
            need(counts, "sand", 1, Blocks.SAND);
            need(counts, "clay", 1, Blocks.CLAY);
            need(counts, "sugar_cane", 1, Blocks.SUGAR_CANE);
            need(counts, "water", 1, Blocks.WATER);
            need(counts, "lava", 1, Blocks.LAVA);
        }

        private void need(Map<Block, Integer> counts, String name, int minimum, Block... blocks) {
            int total = 0;
            for (Block block : blocks) {
                total += counts.getOrDefault(block, 0);
            }
            check("resource_" + name, total >= minimum, String.valueOf(total));
        }

        private static Map<String, Integer> names(Map<Block, Integer> counts) {
            Map<String, Integer> names = new HashMap<>();
            counts.forEach((block, count) -> names.put(BuiltInRegistries.BLOCK.getKey(block).toString(), count));
            return names;
        }

        /** Water that is not a source block above the sewers means a canal or pipe is leaking. */
        private void checkFluids(Map<ChunkPos, LevelChunk> chunks) {
            int flowing = 0;
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        for (int y = 46; y <= 66; y++) {
                            var fluid = chunk.getFluidState(x, y, z);
                            if (!fluid.isEmpty() && fluid.getType() != Fluids.WATER && fluid.getType() != Fluids.LAVA) {
                                flowing++;
                            }
                        }
                    }
                }
            }
            check("no_flowing_water_near_surface", flowing == 0, String.valueOf(flowing));
        }

        /** Ore or raw rock at street level or above means a vanilla feature wrote through the city. */
        private void checkCityMaterials(Map<ChunkPos, LevelChunk> chunks) {
            int ore = 0;
            int rock = 0;
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
                        for (int y = CityPlan.STREET_Y; y <= top; y++) {
                            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                            if (state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
                                ore++;
                            } else if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
                                rock++;
                            }
                        }
                    }
                }
            }
            check("no_ore_in_city", ore == 0, String.valueOf(ore));
            check("no_replaceable_rock_in_city", rock == 0, String.valueOf(rock));
        }

        private void check(String name, boolean passed, String detail) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", name);
            entry.addProperty("passed", passed);
            entry.addProperty("detail", detail);
            checks.add(entry);
            if (!passed) {
                failures++;
            }
            Dorohedoro.LOGGER.info("QA {} {}: {}", passed ? "PASS" : "FAIL", name, detail);
        }

        private void write(Rect area) throws IOException {
            Path directory = server.getServerDirectory().resolve("qa");
            Files.createDirectories(directory);
            report.addProperty("failures", failures);
            report.add("checks", checks);
            Files.writeString(directory.resolve("report-" + order + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
            if (area != null) {
                map(area, directory.resolve("map-" + order + ".png"));
            }
            Dorohedoro.LOGGER.info("QA complete: {} failure(s), report in {}", failures, directory.toAbsolutePath());
        }

        private void map(Rect area, Path file) throws IOException {
            BufferedImage image = new BufferedImage(area.width(), area.depth(), BufferedImage.TYPE_INT_RGB);
            for (int x = area.x0(); x <= area.x1(); x++) {
                for (int z = area.z0(); z <= area.z1(); z++) {
                    int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                    BlockPos pos = new BlockPos(x, top, z);
                    BlockState state = level.getBlockState(pos);
                    MapColor color = state.getMapColor(level, pos);
                    int shade = Math.min(255, 150 + (top - CityPlan.STREET_Y) * 3);
                    int rgb = color.col;
                    int r = ((rgb >> 16) & 255) * shade / 255;
                    int g = ((rgb >> 8) & 255) * shade / 255;
                    int b = (rgb & 255) * shade / 255;
                    image.setRGB(x - area.x0(), z - area.z0(), (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255));
                }
            }
            ImageIO.write(image, "png", file.toFile());
        }
    }
}
