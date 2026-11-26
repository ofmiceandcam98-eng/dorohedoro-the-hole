package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import com.dorohedoro.hole.worldgen.sorcerer.Painter;
import com.dorohedoro.hole.worldgen.sorcerer.RoadNet;
import com.dorohedoro.hole.worldgen.sorcerer.SorcererStructures;
import com.dorohedoro.hole.worldgen.sorcerer.TownBuildingPiece;
import com.dorohedoro.hole.worldgen.sorcerer.TownFrame;
import com.dorohedoro.hole.worldgen.sorcerer.TownPlanner;
import com.dorohedoro.hole.worldgen.sorcerer.TownRoadsPiece;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

/**
 * Real-server checks for Sorcerers' World settlements, run as part of {@link HoleQa}:
 * <ul>
 *   <li>the structure is registered, can be placed only in the Sorcerers' World, and how much land settlements take;</li>
 *   <li>for the nearest settlements: no overlapping footprints, no building on a road, every door connected by a walk
 *   to the road, every room reachable from its door, lit, roofed and on a foundation;</li>
 *   <li>road paving is continuous and walkable; villagers, loot containers and shop signs exist;</li>
 *   <li>a hash of every generated block (vegetation aside) per chunk, for forward/reverse determinism comparisons.</li>
 * </ul>
 * Set the environment variable {@code DOROHEDORO_QA_SORCERERS=false} to skip it.
 */
public final class SorcererTownQa {
    public interface Check {
        void check(String name, boolean passed, String detail);
    }

    private final MinecraftServer server;
    private final ServerLevel level;
    private final String order;
    private final Check check;
    private final JsonObject report = new JsonObject();

    private SorcererTownQa(MinecraftServer server, ServerLevel level, String order, Check check) {
        this.server = server;
        this.level = level;
        this.order = order;
        this.check = check;
    }

    public static JsonObject run(MinecraftServer server, String order, Check check) {
        ServerLevel sw = server.getLevel(RealmBiomes.SORCERERS_WORLD);
        JsonObject skipped = new JsonObject();
        if ("false".equalsIgnoreCase(System.getenv("DOROHEDORO_QA_SORCERERS"))) {
            skipped.addProperty("skipped", "DOROHEDORO_QA_SORCERERS=false");
            return skipped;
        }
        check.check("sw_settlement_realm_loaded", sw != null, sw == null ? "no Sorcerers' World" : "ok");
        if (sw == null) return skipped;
        try {
            return new SorcererTownQa(server, sw, order, check).execute();
        } catch (Exception e) {
            Dorohedoro.LOGGER.error("Sorcerers' World settlement QA failed", e);
            check.check("sw_settlement_qa_ran", false, e.toString());
            return skipped;
        }
    }

    private JsonObject execute() {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var holder = registry.getHolder(SorcererStructures.SETTLEMENT_KEY);
        check.check("sw_settlement_registered", holder.isPresent(), SorcererStructures.SETTLEMENT_KEY.location().toString());
        if (holder.isEmpty()) return report;
        Structure structure = holder.get().value();

        // Placement must exist in the Sorcerers' World and nowhere else.
        List<StructurePlacement> placements = level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder.get());
        check.check("sw_settlement_placement_in_realm", !placements.isEmpty(), placements.size() + " placement(s)");
        int elsewhere = 0;
        for (ServerLevel other : server.getAllLevels()) {
            if (other == level) continue;
            elsewhere += other.getChunkSource().getGeneratorState().getPlacementsForStructure(holder.get()).size();
        }
        check.check("sw_settlement_not_in_other_dimensions", elsewhere == 0, elsewhere + " placement(s) outside the Sorcerers' World");
        if (placements.isEmpty() || !(placements.get(0) instanceof RandomSpreadStructurePlacement spread)) return report;

        List<StructureStart> starts = survey(structure, spread);
        biomeCensus();
        if (starts.isEmpty()) {
            check.check("sw_settlement_found", false, "no settlement in the surveyed area");
            return report;
        }
        // Inspect up to three settlements, preferring one of each kind.
        List<StructureStart> chosen = new ArrayList<>();
        for (TownPlanner.Kind kind : TownPlanner.Kind.values()) {
            for (StructureStart s : starts) {
                if (roads(s) != null && roads(s).kind() == kind) {
                    chosen.add(s);
                    break;
                }
            }
        }
        check.check("sw_settlement_found", true, starts.size() + " in survey, inspecting " + chosen.size());
        JsonArray towns = new JsonArray();
        Map<String, String> hashes = new TreeMap<>();
        for (StructureStart start : chosen) towns.add(inspect(start, hashes));
        report.add("settlements", towns);
        pollution(chosen);
        JsonObject h = new JsonObject();
        hashes.forEach(h::addProperty);
        report.add("chunk_hashes", h);
        report.addProperty("hashed_chunks", hashes.size());
        report.addProperty("hash_scope", "positions the settlement writes (replayed building painters, road and verge paving bands), vegetation excluded");
        return report;
    }

    /** Blocks that belong to the Hole's look. None may appear in the Sorcerers' World. */
    private static final Set<String> HOLE_BLOCKS = Set.of("soot_blackened_brick", "rusted_plating", "rusted_industrial_sheet",
            "industrial_debris", "sewer_brickwork", "seepage_brick", "smoke_stained_concrete", "water_damaged_plaster",
            "patchwork_masonry", "galvanized_plating", "chitin_plating", "cavern_lamp", "ash_soil", "hell_rock",
            "ash_snow", "ash_snow_block", "sludge_ice");

    /**
     * No pollution: Sorcerers' World chunks, in settlements and in open country, hold no Hole or Hell blocks, and every
     * biome there is a Sorcerers' World biome.
     */
    private void pollution(List<StructureStart> settlements) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (StructureStart s : settlements) {
            BoundingBox b = s.getBoundingBox();
            for (int cx = b.minX() >> 4; cx <= b.maxX() >> 4; cx++) for (int cz = b.minZ() >> 4; cz <= b.maxZ() >> 4; cz++) chunks.add(new ChunkPos(cx, cz));
        }
        // Countryside: a ring of chunks 700 blocks out.
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            chunks.add(new ChunkPos(((int) (Math.cos(a) * 700)) >> 4, ((int) (Math.sin(a) * 700)) >> 4));
        }
        Map<String, Integer> found = new TreeMap<>();
        Map<String, Integer> modBlocks = new TreeMap<>();
        Set<String> foreignBiomes = new java.util.TreeSet<>();
        for (ChunkPos c : chunks) {
            LevelChunk chunk = level.getChunk(c.x, c.z);
            LevelChunkSection[] sections = chunk.getSections();
            for (LevelChunkSection section : sections) {
                if (section.hasOnlyAir()) continue;
                for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    if (!Dorohedoro.MOD_ID.equals(key.getNamespace())) continue;
                    modBlocks.merge(key.getPath(), 1, Integer::sum);
                    if (HOLE_BLOCKS.contains(key.getPath())) found.merge(key.getPath(), 1, Integer::sum);
                }
            }
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y += 16) {
                var biome = level.getBiome(new BlockPos(c.getMiddleBlockX(), y, c.getMiddleBlockZ()));
                String id = biome.unwrapKey().map(k -> k.location().toString()).orElse("?");
                // Vanilla biomes are the realm's own now; any other Dorohedoro biome (the Hole, Hell) is pollution.
                if (id.startsWith("dorohedoro:") && !id.startsWith("dorohedoro:sw_")) foreignBiomes.add(id);
            }
        }
        JsonObject out = new JsonObject();
        out.addProperty("chunks_scanned", chunks.size());
        JsonObject mb = new JsonObject();
        modBlocks.forEach(mb::addProperty);
        out.add("mod_blocks_seen", mb);
        report.add("pollution", out);
        check.check("sw_no_hole_blocks", found.isEmpty(), found.isEmpty() ? chunks.size() + " chunks clean" : found.toString());
        check.check("sw_no_hole_or_hell_biomes", foreignBiomes.isEmpty(), foreignBiomes.isEmpty() ? "none" : foreignBiomes.toString());
    }

    private static TownRoadsPiece roads(StructureStart start) {
        for (StructurePiece piece : start.getPieces()) if (piece instanceof TownRoadsPiece r) return r;
        return null;
    }

    /** Every placement cell in a 6 x 6 cell square around the origin: how many settled, of what kind, how much land. */
    private List<StructureStart> survey(Structure structure, RandomSpreadStructurePlacement spread) {
        long seed = level.getChunkSource().getGeneratorState().getLevelSeed();
        int spacing = spread.spacing();
        int cells = 6;
        List<StructureStart> starts = new ArrayList<>();
        Map<String, Integer> kinds = new TreeMap<>();
        double settledArea = 0;
        Set<Long> seen = new HashSet<>();
        for (int i = -cells / 2; i < cells / 2; i++) {
            for (int j = -cells / 2; j < cells / 2; j++) {
                ChunkPos c = spread.getPotentialStructureChunk(seed, i * spacing, j * spacing);
                if (!seen.add(c.toLong())) continue;
                ChunkAccess chunk = level.getChunk(c.x, c.z, ChunkStatus.STRUCTURE_STARTS);
                StructureStart start = chunk.getStartForStructure(structure);
                if (start == null || !start.isValid()) continue;
                starts.add(start);
                TownRoadsPiece r = roads(start);
                if (r != null) {
                    kinds.merge(r.kind().name(), 1, Integer::sum);
                    BoundingBox b = r.getBoundingBox();
                    double radius = (b.getXSpan() - 32) / 2.0;
                    settledArea += Math.PI * radius * radius;
                }
            }
        }
        // Vanilla villages and outposts are refused here: none may start anywhere in the survey area.
        int villages = 0;
        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var refusedTag = net.minecraft.tags.TagKey.create(Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "not_in_sorcerers_world"));
        for (var holder : structures.getTagOrEmpty(refusedTag)) {
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                    net.minecraft.core.HolderSet.direct(holder), BlockPos.ZERO, (cells * spacing) / 2, false);
            if (found != null) villages++;
        }
        check.check("sw_no_vanilla_villages", villages == 0, villages + " refused structure type(s) found near the origin");
        double area = Math.pow(cells * spacing * 16.0, 2);
        JsonObject survey = new JsonObject();
        survey.addProperty("placement_cells", seen.size());
        survey.addProperty("settlements", starts.size());
        JsonObject k = new JsonObject();
        kinds.forEach(k::addProperty);
        survey.add("kinds", k);
        survey.addProperty("surveyed_blocks_square", (int) Math.sqrt(area));
        survey.addProperty("settled_fraction", settledArea / area);
        report.add("survey", survey);
        // Not one endless city: settlements stay well under a fifth of the land.
        check.check("sw_settlements_leave_countryside", settledArea / area < 0.2,
                String.format("%.1f%% of the surveyed land inside settlements", 100 * settledArea / area));
        return starts;
    }

    /** What share of the Sorcerers' World surface each biome takes, on a 64-block grid over 4096 x 4096 blocks. */
    private void biomeCensus() {
        Map<String, Integer> counts = new TreeMap<>();
        int total = 0;
        var source = level.getChunkSource().getGenerator().getBiomeSource();
        var sampler = level.getChunkSource().randomState().sampler();
        for (int x = -2048; x < 2048; x += 64) {
            for (int z = -2048; z < 2048; z += 64) {
                Holder<Biome> b = source.getNoiseBiome(x >> 2, 80 >> 2, z >> 2, sampler);
                counts.merge(b.unwrapKey().map(ResourceKey::location).map(Object::toString).orElse("?"), 1, Integer::sum);
                total++;
            }
        }
        JsonObject census = new JsonObject();
        for (var e : counts.entrySet()) census.addProperty(e.getKey(), Math.round(1000.0 * e.getValue() / total) / 10.0);
        report.add("biome_percent_at_y80", census);
    }

    // ------------------------------------------------------------ one settlement

    private JsonObject inspect(StructureStart start, Map<String, String> hashes) {
        TownRoadsPiece roads = roads(start);
        RoadNet net = roads.net();
        BoundingBox box = start.getBoundingBox();
        JsonObject out = new JsonObject();
        String name = roads.kind().name().toLowerCase() + "@" + roads.centreX() + "," + roads.centreZ();
        out.addProperty("name", name);
        out.addProperty("centre_y", roads.centreY());

        // Generate every chunk of the settlement, in QA order.
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) chunks.add(new ChunkPos(cx, cz));
        if ("reverse".equals(order)) java.util.Collections.reverse(chunks);
        long t0 = System.nanoTime();
        for (ChunkPos c : chunks) level.getChunk(c.x, c.z);
        // The server is not ticking during QA: run the post-generation tree tidy now, as the next tick would.
        com.dorohedoro.hole.worldgen.sorcerer.SettlementTidy.flush(level);
        double seconds = (System.nanoTime() - t0) / 1e9;
        out.addProperty("chunks", chunks.size());
        out.addProperty("ms_per_chunk", seconds * 1000 / chunks.size());

        List<TownBuildingPiece> buildings = new ArrayList<>();
        for (StructurePiece piece : start.getPieces()) if (piece instanceof TownBuildingPiece b) buildings.add(b);
        out.addProperty("buildings", buildings.size());
        Map<String, Integer> types = new TreeMap<>();
        for (TownBuildingPiece b : buildings) types.merge(b.plot().type(), 1, Integer::sum);
        JsonObject t = new JsonObject();
        types.forEach(t::addProperty);
        out.add("types", t);

        // Footprints: never overlapping, never on a road.
        int overlaps = 0, onRoad = 0;
        for (int i = 0; i < buildings.size(); i++) {
            TownFrame a = buildings.get(i).plot().frame();
            for (int j = i + 1; j < buildings.size(); j++) {
                TownFrame b = buildings.get(j).plot().frame();
                if (a.minX() <= b.maxX() && b.minX() <= a.maxX() && a.minZ() <= b.maxZ() && b.minZ() <= a.maxZ()) overlaps++;
            }
            for (int x = a.minX(); x <= a.maxX(); x++) for (int z = a.minZ(); z <= a.maxZ(); z++) if (net.sample(x, z).road()) {
                onRoad++;
                x = a.maxX() + 1;
                break;
            }
        }
        check.check(name + "_no_overlaps", overlaps == 0, overlaps + " overlapping pair(s)");
        check.check(name + "_no_buildings_on_roads", onRoad == 0, onRoad + " building(s) on a road");

        // Doors, rooms, lighting, roofs, foundations.
        int doors = 0, doorFails = 0, rooms = 0, unreachable = 0, dark = 0, open = 0, floating = 0;
        List<String> problems = new ArrayList<>();
        for (TownBuildingPiece b : buildings) {
            Probes probes = new Probes();
            b.paint(probes);
            BlockPos door = probes.get("door").get(0);
            doors++;
            if (!reachesRoad(door, net, b.plot().frame())) {
                doorFails++;
                if (problems.size() < 12) problems.add("no road from door of " + b.plot().type() + " at " + door.toShortString() + " " + walkDump(b));
            }
            for (String kind : List.of("room", "abandoned")) {
                for (BlockPos room : probes.get(kind)) {
                    rooms++;
                    if (!reaches(door, room, 6000, b.plot().frame())) {
                        unreachable++;
                        if (problems.size() < 12) problems.add("unreachable room in " + b.plot().type() + " at " + room.toShortString()
                                + " " + stairDump(b, room.getY() - b.plot().floorY()));
                    }
                    int light = Math.max(level.getBrightness(LightLayer.BLOCK, room), level.getBrightness(LightLayer.BLOCK, room.above()));
                    if (kind.equals("room") && light < 7) {
                        dark++;
                        if (problems.size() < 12) problems.add("dark (" + light + ") room in " + b.plot().type() + " at " + room.toShortString());
                    }
                    boolean covered = false;
                    for (int dy = 2; dy <= 14 && !covered; dy++) covered = !level.getBlockState(room.above(dy)).isAir();
                    boolean openLot = b.plot().spec().category().name().equals("OPEN");
                    if (!covered && !openLot && kind.equals("room")) {
                        open++;
                        if (problems.size() < 12) problems.add("roofless room in " + b.plot().type() + " at " + room.toShortString());
                    }
                }
            }
            TownFrame f = b.plot().frame();
            for (int[] c : new int[][]{{f.minX(), f.minZ()}, {f.maxX(), f.minZ()}, {f.minX(), f.maxZ()}, {f.maxX(), f.maxZ()}}) {
                if (level.getBlockState(new BlockPos(c[0], b.plot().floorY() - 2, c[1])).isAir()) floating++;
            }
        }
        out.addProperty("doors", doors);
        JsonArray entrances = new JsonArray();
        for (TownBuildingPiece b : buildings) {
            JsonObject e = new JsonObject();
            e.addProperty("type", b.plot().type());
            e.addProperty("x", b.plot().doorX());
            e.addProperty("y", b.plot().floorY());
            e.addProperty("z", b.plot().doorZ());
            e.addProperty("facing", b.plot().facing());
            entrances.add(e);
        }
        out.add("entrances", entrances);
        out.addProperty("rooms", rooms);
        check.check(name + "_doors_reach_road", doorFails == 0, (doors - doorFails) + "/" + doors);
        check.check(name + "_rooms_reachable", unreachable == 0, (rooms - unreachable) + "/" + rooms);
        check.check(name + "_rooms_lit", dark == 0, dark + " dark room(s)");
        check.check(name + "_rooms_roofed", open == 0, open + " roofless room(s)");
        check.check(name + "_no_floating_corners", floating == 0, floating + " corner(s) over air");

        // Roads: paving under every centreline sample, headroom above.
        int samples = 0, bad = 0;
        for (int x = box.minX(); x <= box.maxX(); x += 2) {
            for (int z = box.minZ(); z <= box.maxZ(); z += 2) {
                RoadNet.Hit hit = net.sample(x, z);
                if (!hit.road() || hit.dist() > 0.8) continue;
                // The plaza centrepiece (fountain or well, bell, benches) stands on purpose inside 5 blocks of the centre.
                if (hit.cls() == RoadNet.PLAZA && Math.abs(x - roads.centreX()) <= 5 && Math.abs(z - roads.centreZ()) <= 5) continue;
                samples++;
                BlockPos feet = new BlockPos(x, hit.y(), z);
                BlockState under = level.getBlockState(feet.below()), at = level.getBlockState(feet);
                boolean paved = !under.getCollisionShape(level, feet.below()).isEmpty();
                boolean clear = (at.getCollisionShape(level, feet).isEmpty() || at.getCollisionShape(level, feet).max(Direction.Axis.Y) <= 0.5)
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
                if (!paved || !clear) {
                    bad++;
                    if (problems.size() < 16) problems.add("road at " + feet.toShortString() + " cls " + hit.cls() + ": under="
                            + under.getBlock() + " feet=" + at.getBlock() + " head=" + level.getBlockState(feet.above()).getBlock());
                }
            }
        }
        out.addProperty("road_samples", samples);
        JsonArray p = new JsonArray();
        problems.forEach(p::add);
        out.add("problems", p);
        check.check(name + "_roads_walkable", samples > 0 && bad <= samples / 50, (samples - bad) + "/" + samples);

        // Life: villagers, loot, signs.
        AABB aabb = AABB.of(box);
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, aabb);
        int employed = 0;
        for (Villager v : villagers) if (v.getVillagerData().getProfession() != VillagerProfession.NONE) employed++;
        int containers = 0, signs = 0;
        for (ChunkPos c : chunks) {
            LevelChunk chunk = level.getChunk(c.x, c.z);
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (!box.isInside(be.getBlockPos())) continue;
                if (be instanceof RandomizableContainer rc && rc.getLootTable() != null) containers++;
                if (be instanceof SignBlockEntity sign && !sign.getFrontText().getMessage(1, false).getString().isEmpty()) signs++;
            }
            hashes.put(name + ":" + c.x + "," + c.z, Long.toHexString(settlementHash(chunk, buildings, net)));
            if ((c.x + "," + c.z).equals(System.getenv("DOROHEDORO_QA_SW_DUMP"))) report.add("dump_" + c.x + "_" + c.z, settlementDump(chunk, buildings, net));
        }
        out.addProperty("villagers", villagers.size());
        out.addProperty("villagers_with_trade", employed);
        out.addProperty("loot_containers", containers);
        out.addProperty("shop_signs", signs);
        check.check(name + "_has_villagers", villagers.size() > 0, villagers.size() + " villagers, " + employed + " with a trade");
        check.check(name + "_has_loot", containers > 0, containers + " containers");
        return out;
    }

    /** The east stairwell below an unreachable room, local columns w-3..w-2 by z, for diagnosis. */
    private String stairDump(TownBuildingPiece b, int roomY) {
        TownFrame f = b.plot().frame();
        StringBuilder sb = new StringBuilder("stairs[");
        for (int x = f.w() - 3; x <= f.w() - 2; x++) {
            for (int z = f.d() - 7; z <= f.d() - 1; z++) {
                sb.append("x").append(x).append("z").append(z).append(":");
                for (int y = roomY - 5; y <= roomY + 1; y++) {
                    BlockState s = level.getBlockState(new BlockPos(f.worldX(x, z), b.plot().floorY() + y, f.worldZ(x, z)));
                    String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
                    if (s.hasProperty(net.minecraft.world.level.block.StairBlock.FACING)) id += "/" + s.getValue(net.minecraft.world.level.block.StairBlock.FACING).getName().charAt(0);
                    sb.append(id.replace("_planks", "P").replace("_stairs", "S").replace("minecraft:", "")).append(y < roomY + 1 ? "," : "");
                }
                sb.append(" ");
            }
        }
        return sb.append("]").toString();
    }

    /** The blocks along a walk, feet and head, for diagnosing a blocked entrance. */
    private String walkDump(TownBuildingPiece b) {
        TownPlanner.Plot plot = b.plot();
        StringBuilder sb = new StringBuilder("walk[");
        for (int k = 1; k <= plot.walk() + 1; k++) {
            int x = plot.doorX() + TownFrame.DX[plot.facing()] * k, z = plot.doorZ() + TownFrame.DZ[plot.facing()] * k;
            sb.append(k).append(":");
            for (int dy = -2; dy <= 2; dy++) {
                BlockState s = level.getBlockState(new BlockPos(x, plot.floorY() + dy, z));
                String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
                sb.append(dy == 0 ? "[" : "").append(id.length() > 10 ? id.substring(0, 10) : id).append(dy == 0 ? "]" : "").append(dy < 2 ? "," : "");
            }
            sb.append(" ");
        }
        return sb.append("floor=").append(plot.floorY()).append(" road=").append(plot.roadY()).append("]").toString();
    }

    /** From the door outwards to the paving, never back inside the building it belongs to. */
    private boolean reachesRoad(BlockPos door, RoadNet net, TownFrame frame) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(door);
        seen.add(door);
        while (!queue.isEmpty() && seen.size() < 3000) {
            BlockPos at = queue.poll();
            RoadNet.Hit hit = net.sample(at.getX(), at.getZ());
            if (hit.road() && Math.abs(at.getY() - hit.y()) <= 1) return true;
            for (BlockPos next : neighbours(at)) {
                boolean inside = next.getX() >= frame.minX() && next.getX() <= frame.maxX()
                        && next.getZ() >= frame.minZ() && next.getZ() <= frame.maxZ();
                if (!inside && seen.add(next)) queue.add(next);
            }
        }
        return false;
    }

    /** Inside the building only: the search must not spend its budget on the streets outside. */
    private boolean reaches(BlockPos from, BlockPos goal, int limit, TownFrame frame) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(from);
        seen.add(from);
        while (!queue.isEmpty() && seen.size() < limit) {
            BlockPos at = queue.poll();
            if (Math.abs(at.getX() - goal.getX()) + Math.abs(at.getZ() - goal.getZ()) <= 1 && Math.abs(at.getY() - goal.getY()) <= 1) return true;
            for (BlockPos next : neighbours(at)) {
                boolean inside = next.getX() >= frame.minX() && next.getX() <= frame.maxX()
                        && next.getZ() >= frame.minZ() && next.getZ() <= frame.maxZ();
                if (inside && seen.add(next)) queue.add(next);
            }
        }
        return false;
    }

    /** Feet positions a player can move to: one step up (with headroom to jump), or a drop of up to three. */
    private List<BlockPos> neighbours(BlockPos at) {
        List<BlockPos> out = new ArrayList<>(4);
        boolean canJump = passable(at.above(2));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = at.relative(dir);
            for (int dy = 1; dy >= -3; dy--) {
                // Stairs and bottom slabs are walked up without jumping; a full block needs headroom to jump.
                if (dy == 1 && !canJump && !halfStep(side)) continue;
                BlockPos feet = side.above(dy);
                if (standable(feet)) {
                    out.add(feet);
                    break;
                }
                if (dy <= 0 && !passable(feet)) break;
            }
        }
        return out;
    }

    private boolean halfStep(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.StairBlock) return true;
        var shape = state.getCollisionShape(level, pos);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) <= 0.5;
    }

    private boolean standable(BlockPos feet) {
        return passable(feet) && passable(feet.above())
                && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty();
    }

    private boolean passable(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof FenceGateBlock) return true;
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    /**
     * Hash of exactly what the settlement wrote in this chunk: every position a building painter sets (replayed with a
     * recording sink) and the paving band of every road and verge column. Vanilla ore blobs that straddle chunk borders
     * resolve differently with generation order, in vanilla too, so whole-chunk hashes would measure vanilla, not us.
     */
    private long settlementHash(LevelChunk chunk, List<TownBuildingPiece> buildings, RoadNet net) {
        long h = 1125899906842597L;
        for (long p : settlementPositions(chunk, buildings, net)) {
            BlockState state = chunk.getBlockState(BlockPos.of(p));
            // Vanilla vegetation lands on open ground in an order-dependent way: open cells (air, plants, trees, snow)
            // are left out entirely, so only what stands there solidly is compared.
            boolean open = state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.SNOW)
                    || state.is(BlockTags.FLOWERS) || state.getBlock() instanceof net.minecraft.world.level.block.BushBlock
                    || (state.canBeReplaced() && state.getFluidState().isEmpty());
            if (open) continue;
            // A tree turns the grass under its trunk to dirt: natural soil counts as one class.
            boolean soil = state.is(BlockTags.DIRT) && !state.is(Blocks.PACKED_MUD);
            if (soil) {
                h = h * 31 + p;
                h = h * 31 + 2;
                continue;
            }
            h = h * 31 + p;
            // Ore blobs from neighbouring chunks also race; natural rock and ore count as one class.
            boolean rock = state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                    || state.is(Tags.Blocks.ORES);
            h = h * 31 + (open ? 0 : rock ? 1 : Block.getId(state));
        }
        return h;
    }

    private JsonArray settlementDump(LevelChunk chunk, List<TownBuildingPiece> buildings, RoadNet net) {
        JsonArray out = new JsonArray();
        for (long p : settlementPositions(chunk, buildings, net)) {
            BlockState state = chunk.getBlockState(BlockPos.of(p));
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.SNOW)
                    || (state.canBeReplaced() && state.getFluidState().isEmpty() && !state.isAir())) continue;
            out.add(BlockPos.of(p).toShortString() + " " + state);
        }
        return out;
    }

    private java.util.TreeSet<Long> settlementPositions(LevelChunk chunk, List<TownBuildingPiece> buildings, RoadNet net) {
        ChunkPos cp = chunk.getPos();
        BoundingBox chunkBox = new BoundingBox(cp.getMinBlockX(), level.getMinBuildHeight(), cp.getMinBlockZ(),
                cp.getMaxBlockX(), level.getMaxBuildHeight() - 1, cp.getMaxBlockZ());
        java.util.TreeSet<Long> positions = new java.util.TreeSet<>();
        for (TownBuildingPiece b : buildings) {
            if (!b.getBoundingBox().intersects(chunkBox)) continue;
            b.paint(new Painter.Sink() {
                @Override public boolean inside(BlockPos pos) { return chunkBox.isInside(pos); }
                @Override public void set(BlockPos pos, BlockState state) { positions.add(pos.asLong()); }
                @Override public BlockState get(BlockPos pos) { return level.getBlockState(pos); }
                @Override public void loot(BlockPos pos, ResourceKey<LootTable> table) {}
                @Override public void sign(BlockPos pos, String[] lines) {}
                @Override public void villager(BlockPos pos, VillagerProfession profession) {}
            });
        }
        for (int x = chunkBox.minX(); x <= chunkBox.maxX(); x++) for (int z = chunkBox.minZ(); z <= chunkBox.maxZ(); z++) {
            RoadNet.Hit hit = net.sample(x, z);
            // Paving and headroom are always written; what lies below a cutting is natural rock, which vanilla
            // ore and gravel blobs reach in an order-dependent way.
            if (hit.road() || hit.verge()) for (int y = hit.y() - 1; y <= hit.y() + 3; y++) positions.add(BlockPos.asLong(x, y, z));
        }
        return positions;
    }

    private static JsonArray dump(LevelChunk chunk) {
        JsonArray out = new JsonArray();
        LevelChunkSection[] sections = chunk.getSections();
        for (int s = 0; s < sections.length; s++) {
            if (sections[s].hasOnlyAir()) continue;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                BlockState state = sections[s].getBlockState(x, y, z);
                if (state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.SNOW)
                        || (state.canBeReplaced() && state.getFluidState().isEmpty())) continue;
                out.add((chunk.getMinBuildHeight() + s * 16 + y) + " " + x + " " + z + " " + state);
            }
        }
        return out;
    }

    private static long hash(LevelChunk chunk) {
        long h = 1125899906842597L;
        LevelChunkSection[] sections = chunk.getSections();
        for (int s = 0; s < sections.length; s++) {
            LevelChunkSection section = sections[s];
            if (section.hasOnlyAir()) continue;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                BlockState state = section.getBlockState(x, y, z);
                if (state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.SNOW)
                        || (state.canBeReplaced() && state.getFluidState().isEmpty())) continue;
                h = h * 31 + ((long) s << 12 | (long) y << 8 | (long) z << 4 | x);
                // The registry id, not hashCode(): block states hash by identity, which changes every launch.
                h = h * 31 + net.minecraft.world.level.block.Block.getId(state);
            }
        }
        return h;
    }

    /** A sink that only records probe positions. */
    private static final class Probes implements Painter.Sink {
        private final Map<String, List<BlockPos>> probes = new TreeMap<>();

        List<BlockPos> get(String kind) {
            return probes.getOrDefault(kind, List.of());
        }

        @Override public boolean inside(BlockPos pos) { return false; }
        @Override public void set(BlockPos pos, BlockState state) {}
        @Override public BlockState get(BlockPos pos) { return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(); }
        @Override public void loot(BlockPos pos, ResourceKey<LootTable> table) {}
        @Override public void sign(BlockPos pos, String[] lines) {}
        @Override public void villager(BlockPos pos, VillagerProfession profession) {}
        @Override public void probe(String kind, BlockPos pos) { probes.computeIfAbsent(kind, k -> new ArrayList<>()).add(pos); }
    }
}
