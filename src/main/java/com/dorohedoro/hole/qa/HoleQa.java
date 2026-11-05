package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleChunkGenerator;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.CityElevation;
import com.dorohedoro.hole.worldgen.city.CityTerrain40;
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
import java.util.Arrays;
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

            // Resolve the actual per-column grade BEFORE hashing: urban relief varies between
            // Y=64 and Y=75, and lots have flattened foundations. A global Y=64 hash
            // accidentally counts naturally generated, mineable soil and ore as city.
            ChunkPos centre = new ChunkPos(level.getSharedSpawnPos());
            Rect area = new Rect(centre.getMinBlockX() - radius * 16, centre.getMinBlockZ() - radius * 16,
                    centre.getMinBlockX() + radius * 16 + 15, centre.getMinBlockZ() + radius * 16 + 15);
            int[] expectedFeet = expectedCityFeet(plan, area);
            JsonObject rawY64Hashes = new JsonObject();
            List<ChunkPos> sorted = new ArrayList<>(positions);
            sorted.sort((a, b) -> a.x != b.x ? Integer.compare(a.x, b.x) : Integer.compare(a.z, b.z));
            for (ChunkPos pos : sorted) {
                LevelChunk chunk = chunks.get(pos);
                String key = pos.x + "," + pos.z;
                hashes.addProperty(key, Long.toHexString(hashCity(chunk, area, expectedFeet)));
                rawY64Hashes.addProperty(key, Long.toHexString(hash(chunk, CITY_LAYER_BOTTOM)));
                fullHashes.addProperty(key, Long.toHexString(hash(chunk, Integer.MIN_VALUE)));
            }
            report.add("chunk_hashes", hashes);
            report.add("chunk_hashes_y64_raw", rawY64Hashes);
            report.add("chunk_hashes_full", fullHashes);
            report.addProperty("chunk_hashes_scope",
                    "Y>=64; compare all non-natural blocks exactly; below each column's actual street/lot ground, normalize only natural soil/rock/ore");

            dumpChunks(chunks);
            checkSpawn(plan);
            checkBiomes();
            report.addProperty("city_expected_feet_y_min", Arrays.stream(expectedFeet).min().orElse(CityPlan.GROUND_Y));
            report.addProperty("city_expected_feet_y_max", Arrays.stream(expectedFeet).max().orElse(CityPlan.GROUND_Y));
            checkConnectivity(plan, area, expectedFeet);
            census(chunks, area, plan);
            checkFluids(chunks);
            checkStairFlights(chunks);
            checkCityMaterials(chunks, area, expectedFeet);
            if (plan.settings.sewerUnderworld()) checkSewerUnderworld(plan);
            // Phase 3: verify the separately registered, full dimensions actually load and produce surface chunks.
            checkRealm(RealmBiomes.SORCERERS_WORLD);
            checkRealm(RealmBiomes.HELL);
            if (Boolean.getBoolean(PROPERTY + ".structures")) {
                report.add("vanilla_structures", StructureQa.run(server, checks));
            }
            write(area);
        }

        /** Generation order is the point of the determinism test: the same chunks are requested in opposite orders. */
        private List<ChunkPos> positions() {
            // Fresh worlds start in a seed-chosen region, so measure around spawn rather than the origin.
            ChunkPos centre = new ChunkPos(level.getSharedSpawnPos());
            List<ChunkPos> list = new ArrayList<>();
            for (int ring = 0; ring <= radius; ring++) {
                for (int x = -ring; x <= ring; x++) {
                    for (int z = -ring; z <= ring; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) == ring) {
                            list.add(new ChunkPos(centre.x + x, centre.z + z));
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
                // A section wholly below the cutoff must not count at all, not even as "empty": one vanilla cave vine
                // landing in a cavern section would otherwise change the city hash.
                if (baseY + 15 < bottom) {
                    continue;
                }
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

        // This token is distinct from normal BlockState registry IDs. Only NATURAL
        // material below the graded city floor is normalized, not engineering blocks.
        private static final int BURIED_NATURAL_FILL = 0x6D12A9;

        /**
         * Compare city and infrastructure blocks even when they are below the elevation
         * of a nearby street (canal walls, water, foundations, stairways, etc.). Vanilla
         * ore decoration may independently replace mineable rock UNDER the graded street
         * depending on neighboring chunk order. Those natural materials do not determine
         * the city's appearance or built structure and must not make the city hash fail.
         *
         * The original fixed-Y=64 fingerprint remains in chunk_hashes_y64_raw so the
         * underground variations are visible rather than silently discarded.
         */
        private static long hashCity(LevelChunk chunk, Rect area, int[] expectedFeet) {
            long hash = 1125899906842597L;
            LevelChunkSection[] sections = chunk.getSections();
            ChunkPos pos = chunk.getPos();
            int[] ground = new int[256];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    ground[x + 16 * z] = expectedFeet[columnIndex(area,
                            pos.getMinBlockX() + x, pos.getMinBlockZ() + z)] - 1;
                }
            }
            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                if (baseY + 15 < CITY_LAYER_BOTTOM) continue;
                // Never short-circuit on section.hasOnlyAir(): different natural
                // blocks BELOW the per-column cutoff could otherwise change the hash.
                for (int y = 0; y < 16; y++) {
                    int worldY = baseY + y;
                    if (worldY < CITY_LAYER_BOTTOM) continue;
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            BlockState state = section.getBlockState(x, y, z);
                            int blockId = worldY < ground[x + 16 * z] && isBuriedNaturalFill(state)
                                    ? BURIED_NATURAL_FILL : Block.getId(state);
                            hash = hash * 31 + blockId;
                        }
                    }
                }
            }
            return hash;
        }

        /** Restrict normalization to natural mineable material, never buildings/water/air. */
        private static boolean isBuriedNaturalFill(BlockState state) {
            return state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)
                    || state.is(BlockTags.BASE_STONE_OVERWORLD)
                    || state.is(BlockTags.DIRT)
                    || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.SAND)
                    || state.is(Blocks.CLAY);
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

        /**
         * Minimal server-side smoke test of Phase 3, without traveling a player or relying on portals.
         * Biome variety/ores at scale and in-game rendering require separate dedicated QA later.
         */
        private void checkRealm(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> realmKey) {
            String prefix = "realm_" + realmKey.location().getPath();
            ServerLevel realm = server.getLevel(realmKey);
            check(prefix + "_registered", realm != null, realm == null ? "missing" : realm.dimension().location().toString());
            if (realm == null) return;
            check(prefix + "_noise_generator", realm.getChunkSource().getGenerator()
                    instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator,
                    realm.getChunkSource().getGenerator().getClass().getSimpleName());
            int validBiomes = 0;
            int surfaceColumns = 0;
            for (int[] coord : new int[][] {{0,0}, {4,4}, {-4,4}, {4,-4}, {-4,-4}}) {
                int x = coord[0] * 16 + 8;
                int z = coord[1] * 16 + 8;
                realm.getChunk(coord[0], coord[1]);
                int y = realm.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                if (y > realm.getMinBuildHeight() && y < realm.getMaxBuildHeight()) {
                    surfaceColumns++;
                }
                if (realm.getBiome(new BlockPos(x, Math.min(y, realm.getMaxBuildHeight() - 1), z))
                        .unwrapKey().map(k -> "dorohedoro".equals(k.location().getNamespace())).orElse(false)) {
                    validBiomes++;
                }
            }
            check(prefix + "_custom_biomes", validBiomes == 5, String.valueOf(validBiomes) + "/5");
            check(prefix + "_surface_columns", surfaceColumns == 5, String.valueOf(surfaceColumns) + "/5");
        }

        /** New-world sewers must be traversable and reach the built-in deep excavation chambers. */
        /**
         * A deep sewer check that knows about the Deep Dark buffer. Below y -24 in chunks near Deep Dark habitat the city
         * writes nothing, on purpose, so vanilla Ancient Cities and sculk stay intact (DeepDarkBuffer63). Such a point is
         * recorded as protected, with the block that is really there, instead of as a carving failure. Anywhere else
         * the original condition must hold.
         */
        private void sewerCheck(CityPlan plan, String name, BlockPos pos, boolean carved) {
            boolean nearDeepDark = plan.protectsDeepDarkNearChunk(pos.getX() & ~15, pos.getZ() & ~15);
            boolean protectedHere = com.dorohedoro.hole.worldgen.city.DeepDarkBuffer63.protects(
                    plan.settings.worldDetailVersion(), pos.getY(), nearDeepDark);
            String block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
            // The city also leaves vanilla underground structures alone (VanillaStructureBuffer64).
            var boxes = com.dorohedoro.hole.worldgen.city.VanillaStructureLookup64.forChunk(level.structureManager(),
                    new net.minecraft.world.level.ChunkPos(pos), plan.settings.worldDetailVersion());
            boolean structureHere = com.dorohedoro.hole.worldgen.city.VanillaStructureBuffer64.protects(
                    plan.settings.worldDetailVersion(), pos.getX(), pos.getY(), pos.getZ(), boxes);
            String nearby = structuresNear(pos);
            protectedHere |= structureHere;
            String detail = pos.toShortString() + " block=" + block
                    + (structureHere ? " (left to vanilla structure: " + nearby + ")" : protectedHere ? " (left to vanilla: Deep Dark buffer)" : carved ? "" : " (not carved, not protected; structures near: " + nearby + ")");
            if (!carved && !protectedHere) detail += " column " + columnProfile(pos.getX(), pos.getZ(), pos.getY() - 12, pos.getY() + 8);
            check(name, carved || protectedHere, detail);
        }

        /** Runs of identical blocks in one column, bottom to top, for diagnosing an uncarved sewer point. */
        private String columnProfile(int x, int z, int y0, int y1) {
            StringBuilder out = new StringBuilder();
            String last = null;
            int start = y0;
            for (int y = y0; y <= y1 + 1; y++) {
                String id = y > y1 ? null : net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, y, z)).getBlock()).getPath();
                if (last != null && !last.equals(id)) {
                    out.append(start).append(start == y - 1 ? "" : ".." + (y - 1)).append(" ").append(last).append("; ");
                    start = y;
                }
                if (last == null || !last.equals(id)) start = last == null ? y0 : start;
                last = id;
            }
            return out.toString();
        }

        /** Vanilla structure starts whose bounding boxes reach this chunk, for diagnosing uncarved sewer points. */
        private String structuresNear(BlockPos pos) {
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            java.util.List<String> names = new java.util.ArrayList<>();
            for (var start : level.structureManager().startsForStructure(new net.minecraft.world.level.ChunkPos(pos), st -> true)) {
                if (start.isValid()) names.add(String.valueOf(registry.getKey(start.getStructure())));
            }
            return names.isEmpty() ? "none" : String.join(",", names);
        }

        private void checkSewerUnderworld(CityPlan plan) {
            // The starter arterial crossings are guaranteed to contain deep chambers.
            // Sample one crossing, rather than accidentally testing an unselected random site.
            int cx = plan.xLine(0).center();
            int cz = plan.zLine(0).center();
            int relief = plan.settings.terrainRelief()
                    ? CityTerrain40.offset(plan, cx + 5, cz + 5) : 0;
            BlockPos chamber = new BlockPos(cx + 5, -30 + relief, cz + 5);
            BlockPos floor = new BlockPos(cx + 5, -37 + relief, cz + 5);
            sewerCheck(plan, "sewer_deep_chamber_carved", chamber, level.getBlockState(chamber).isAir());
            check("sewer_chamber_supported", !level.getBlockState(floor).isAir(), floor.toShortString());
            int drainRelief = plan.settings.terrainRelief()
                    ? CityTerrain40.offset(plan, cx, cz + 10) : 0;
            BlockPos drain = new BlockPos(cx, 29 + drainRelief, cz + 10);
            check("sewer_midlevel_water", level.getFluidState(drain).is(net.minecraft.tags.FluidTags.WATER),
                    drain.toShortString());
            // One intentionally chosen, uncommon cave connection close to the starting quarter.
            // There is still an extended lined sewer and a mined approach before the cave opens.
            int branchX = cx + 12;
            int dir = com.dorohedoro.hole.worldgen.city.SewerCaveBranches.branchDirection(plan.seed, 0, 0);
            int linedZ = cz + dir * 8;
            int linedRelief = plan.settings.terrainRelief()
                    ? CityTerrain40.offset(plan, branchX, linedZ) : 0;
            BlockPos linedPassage = new BlockPos(branchX, -35 + linedRelief, linedZ);
            BlockPos linedWall = new BlockPos(branchX + 2, -35 + linedRelief, linedZ);
            sewerCheck(plan, "sewer_branch_begins_lined", linedPassage, level.getBlockState(linedPassage).isAir()
                    && level.getBlockState(linedWall).is(com.dorohedoro.hole.registry.ModBlocks.SEWER_BRICKWORK.get()));
            int caveZ = cz + dir * 59;
            int caveRelief = plan.settings.terrainRelief()
                    ? CityTerrain40.offset(plan, branchX, caveZ) : 0;
            BlockPos naturalCave = new BlockPos(branchX, -35 + caveRelief, caveZ);
            sewerCheck(plan, "sewer_selective_cave_destination", naturalCave, level.getBlockState(naturalCave).isAir());
            if (plan.settings.sewerCavernVersion() == 1) {
                boolean descent = true;
                String failing = "";
                int previousFloor = Integer.MAX_VALUE;
                for (int step = 43; step <= 59; step++) {
                    int z = cz + dir * step;
                    int offset = plan.settings.terrainRelief()
                            ? CityTerrain40.offset(plan, branchX, z) : 0;
                    int y = com.dorohedoro.hole.worldgen.city.SewerCavernProfile.entranceFloor(
                            plan.seed, branchX, cz, dir, step, plan.settings.terrainRelief()) + offset;
                    BlockPos p = new BlockPos(branchX, y, z);
                    boolean supported = level.getBlockState(p).isSolid();
                    boolean graded = previousFloor == Integer.MAX_VALUE || Math.abs(y - previousFloor) <= 1;
                    previousFloor = y;
                    boolean clear = level.getBlockState(p.above()).isAir()
                            && level.getBlockState(p.above(2)).isAir();
                    if (!supported || !clear || !graded) {
                        descent = false;
                        failing = p.toShortString() + ":supported=" + supported
                                + ",clear=" + clear + ",graded=" + graded;
                        break;
                    }
                }
                check("sewer_cavern_walkable_descent", descent, failing.isEmpty() ? "43..59 clear and supported" : failing);
            }
        }

        private void checkSpawn(CityPlan plan) {
            BlockPos spawn = level.getSharedSpawnPos();
            report.addProperty("spawn", spawn.toShortString());
            // Pass 47+ supports seed-selected regional starts. The headless QA originally
            // assumed every valid world spawned at (0, 65, 0), producing false failures.
            BlockPos plannedSpawn = plan.spawnPoint();
            check("spawn_matches_planned_start", spawn.equals(plannedSpawn),
                    "actual=" + spawn.toShortString() + " expected=" + plannedSpawn.toShortString());
            BlockState feetState = level.getBlockState(spawn);
            boolean feet = passable(feetState);
            boolean head = level.getBlockState(spawn.above()).isAir();
            boolean floor = level.getBlockState(spawn.below()).isFaceSturdy(level, spawn.below(), net.minecraft.core.Direction.UP);
            check("spawn_standable", feet && head && floor, "feet=" + feet + " (" + feetState.getBlock().getDescriptionId() + ") head=" + head + " floor=" + floor);
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
                    || state.is(BlockTags.CROPS) || state.is(Blocks.SHORT_GRASS)
                    // A thin snow layer is walked over like carpet; snowy districts are covered in it.
                    || (state.is(Blocks.SNOW) && state.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) <= 2))
                    && state.getFluidState().isEmpty();
        }

        /**
         * Expected feet Y at each urban column, using the same coordinate-only relief and
         * level lot foundations as Canvas. Otherwise a raised neighborhood (Y=70+) is
         * incorrectly counted as inaccessible merely because an old test checks Y=65.
         * This is QA data only; it does not read or modify generated chunks.
         */
        private int[] expectedCityFeet(CityPlan plan, Rect area) {
            int[] feet = new int[area.area()];
            Arrays.fill(feet, CityPlan.GROUND_Y);
            if (!plan.settings.terrainRelief()) return feet;
            for (int x = area.x0(); x <= area.x1(); x++) {
                for (int z = area.z0(); z <= area.z1(); z++) {
                    feet[columnIndex(area, x, z)] = CityPlan.GROUND_Y + CityTerrain40.offset(plan, x, z);
                }
            }
            // A building uses a level foundation at its LOT center, not the
            // varying heights of the separate neighboring street columns.
            Set<Long> seenLots = new HashSet<>();
            for (int cx = area.x0() >> 4; cx <= area.x1() >> 4; cx++) {
                for (int cz = area.z0() >> 4; cz <= area.z1() >> 4; cz++) {
                    for (Lot lot : plan.lotsIn(new ChunkPos(cx, cz))) {
                        if (!seenLots.add(lot.hash())) continue;
                        Rect r = lot.rect();
                        int lotFeet = CityPlan.GROUND_Y + CityTerrain40.offset(plan, r.centerX(), r.centerZ());
                        for (int x = Math.max(area.x0(), r.x0()); x <= Math.min(area.x1(), r.x1()); x++) {
                            for (int z = Math.max(area.z0(), r.z0()); z <= Math.min(area.z1(), r.z1()); z++) {
                                feet[columnIndex(area, x, z)] = lotFeet;
                            }
                        }
                    }
                }
            }
            return feet;
        }

        /**
         * Flood-fills on foot from spawn at the actual local city elevation, stepping
         * up or down one block at a time. It does not teleport across relief changes
         * or count inaccessible rooftops as ground-floor entrances.
         */
        private void checkConnectivity(CityPlan plan, Rect area, int[] expectedFeet) {
            BitSet visited = new BitSet(area.area());
            ArrayDeque<long[]> queue = new ArrayDeque<>();
            BlockPos spawn = level.getSharedSpawnPos();
            // Core-city QA samples origin-centered chunks even if the player spawns in
            // a distant seed-selected climate. Avoid indexing outside that sample.
            // The real spawn is independently checked by checkSpawn().
            if (!area.contains(spawn.getX(), spawn.getZ())) {
                spawn = new BlockPos(0, CityPlan.GROUND_Y, 0);
                report.addProperty("core_connectivity_anchor", "origin plaza (regional player spawn lies outside the sampled area)");
            }
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
                        // Only a genuine adjacent 1-block step is traversable.
                        // Cap at the predicted street/lot ground level (+/- 2 for
                        // slabs, stairs and small doorstep changes).
                        if (Math.abs(candidate - expectedFeet[index]) <= 2 && walkable(x, candidate, z)) {
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
            // Diagnostic samples identify the actual failed lots instead of only a percentage.
            // This is QA-only: no production generation logic or saved world layout changes.
            JsonArray unreachableLots = new JsonArray();
            Map<LotType, Integer> unreachableTypes = new EnumMap<>(LotType.class);
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
                            } else {
                                unreachableTypes.merge(lot.type(), 1, Integer::sum);
                                if (unreachableLots.size() < 40) {
                                    int x = r.centerX();
                                    int z = r.z1();
                                    JsonObject failure = new JsonObject();
                                    failure.addProperty("type", lot.type().name());
                                    failure.addProperty("district", lot.district().name());
                                    failure.addProperty("lot", r.x0() + "," + r.z0() + ".." + r.x1() + "," + r.z1());
                                    // This is the lot edge, NOT the actual door: building
                                    // footprints have variable south setbacks.
                                    int feetY = expectedFeet[columnIndex(area, x, z)];
                                    failure.addProperty("lot_south_edge_probe", x + "," + feetY + "," + z);
                                    failure.addProperty("expected_lot_feet_y", feetY);
                                    failure.addProperty("south_edge_at_feet", BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, feetY, z)).getBlock()).toString());
                                    failure.addProperty("south_edge_at_head", BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, feetY + 1, z)).getBlock()).toString());
                                    failure.addProperty("outside_at_feet", BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, feetY, z + 1)).getBlock()).toString());
                                    failure.addProperty("inside_at_feet", BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, feetY, z - 1)).getBlock()).toString());
                                    unreachableLots.add(failure);
                                }
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
                    if (!walkable(x, expectedFeet[columnIndex(area, x, z)], z)) {
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
            report.add("unreachable_lot_samples", unreachableLots);
            JsonObject unreachableCount = new JsonObject();
            unreachableTypes.forEach((type, count) -> unreachableCount.addProperty(type.name(), count));
            report.add("unreachable_lot_types", unreachableCount);
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
        /**
         * Every four-step building flight must be climbable both ways. A player moving between steps is half a block
         * up with their head 2.3 blocks above the lower step, so each step needs three blocks of empty collision
         * above it. A block there is what trapped players on stairs before Pass 71.
         */
        private void checkStairFlights(Map<ChunkPos, LevelChunk> chunks) {
            int flights = 0;
            int blocked = 0;
            JsonArray samples = new JsonArray();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        for (int y = 60; y <= 220; y++) {
                            var first = level.getBlockState(cursor.set(x, y, z));
                            if (!(first.getBlock() instanceof net.minecraft.world.level.block.StairBlock)
                                    || first.getValue(net.minecraft.world.level.block.StairBlock.HALF) != net.minecraft.world.level.block.state.properties.Half.BOTTOM) {
                                continue;
                            }
                            var facing = first.getValue(net.minecraft.world.level.block.StairBlock.FACING);
                            boolean flight = true;
                            for (int i = 1; i < 4 && flight; i++) {
                                var step = level.getBlockState(cursor.set(x + i * facing.getStepX(), y + i, z + i * facing.getStepZ()));
                                flight = step.getBlock() instanceof net.minecraft.world.level.block.StairBlock
                                        && step.getValue(net.minecraft.world.level.block.StairBlock.FACING) == facing
                                        && step.getValue(net.minecraft.world.level.block.StairBlock.HALF) == net.minecraft.world.level.block.state.properties.Half.BOTTOM;
                            }
                            if (!flight) continue;
                            flights++;
                            boolean clear = true;
                            for (int i = 0; i < 4 && clear; i++) {
                                for (int up = 1; up <= 3 && clear; up++) {
                                    cursor.set(x + i * facing.getStepX(), y + i + up, z + i * facing.getStepZ());
                                    clear = level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty();
                                }
                            }
                            if (!clear) {
                                blocked++;
                                if (samples.size() < 12) samples.add(x + "," + y + "," + z + " " + facing + " blocked at " + cursor.toShortString()
                                        + " by " + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(cursor).getBlock()));
                            }
                        }
                    }
                }
            }
            report.add("blocked_stair_samples", samples);
            check("stair_flights_walkable", flights > 0 && blocked == 0, blocked + " blocked of " + flights + " flights");
        }

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

        /**
         * Counts ore and replaceable stone ON OR ABOVE the actual city surface.
         * Natural cave material beneath raised ground is intentional survival content,
         * not an ore vein piercing a road. Use the same lot/street surface offsets
         * as generation rather than the old hard-coded world Y=64.
         */
        private void checkCityMaterials(Map<ChunkPos, LevelChunk> chunks, Rect area, int[] expectedFeet) {
            int ore = 0;
            int rock = 0;
            int oreBelow78 = 0;
            int rockBelow78 = 0;
            int ore78to95 = 0;
            int rock78to95 = 0;
            int oreAbove95 = 0;
            int rockAbove95 = 0;
            Map<String, Integer> oreBlocks = new TreeMap<>();
            Map<String, Integer> rockBlocks = new TreeMap<>();
            JsonArray oreSamples = new JsonArray();
            JsonArray rockSamples = new JsonArray();
            for (LevelChunk chunk : chunks.values()) {
                ChunkPos pos = chunk.getPos();
                for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                    for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
                        // Feet level is one above the local foundation surface.
                        // Everything below this point remains valid mineable ground.
                        int bottom = expectedFeet[columnIndex(area, x, z)] - 1;
                        for (int y = bottom; y <= top; y++) {
                            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                            boolean isOre = state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES);
                            boolean isRock = !isOre && state.is(BlockTags.BASE_STONE_OVERWORLD);
                            if (!isOre && !isRock) continue;
                            String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                            if (isOre) {
                                ore++;
                                oreBlocks.merge(name, 1, Integer::sum);
                                if (y < 78) oreBelow78++;
                                else if (y <= 95) ore78to95++;
                                else oreAbove95++;
                                if (oreSamples.size() < 25) oreSamples.add(x + "," + y + "," + z + " " + name);
                            } else {
                                rock++;
                                rockBlocks.merge(name, 1, Integer::sum);
                                if (y < 78) rockBelow78++;
                                else if (y <= 95) rock78to95++;
                                else rockAbove95++;
                                if (rockSamples.size() < 25) rockSamples.add(x + "," + y + "," + z + " " + name);
                            }
                        }
                    }
                }
            }
            JsonObject materials = new JsonObject();
            materials.addProperty("ore_below_y78", oreBelow78);
            materials.addProperty("ore_y78_to_y95", ore78to95);
            materials.addProperty("ore_above_y95", oreAbove95);
            materials.addProperty("rock_below_y78", rockBelow78);
            materials.addProperty("rock_y78_to_y95", rock78to95);
            materials.addProperty("rock_above_y95", rockAbove95);
            JsonObject oresByBlock = new JsonObject();
            oreBlocks.forEach(oresByBlock::addProperty);
            JsonObject rocksByBlock = new JsonObject();
            rockBlocks.forEach(rocksByBlock::addProperty);
            materials.add("ore_blocks", oresByBlock);
            materials.add("rock_blocks", rocksByBlock);
            materials.add("ore_samples", oreSamples);
            materials.add("rock_samples", rockSamples);
            report.add("city_material_diagnostics", materials);
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
