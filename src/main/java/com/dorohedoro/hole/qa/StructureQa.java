package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.HoleBiomeSource;
import com.dorohedoro.hole.worldgen.StructurePrecheck72;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Finds vanilla structures in a freshly generated world, the way {@code /locate} does, then loads each one and
 * counts the blocks that prove it is real and usable. Biome tags alone are not evidence that a structure generates.
 */
final class StructureQa {
    private static final int SEARCH_RADIUS_CHUNKS = 100;

    /** Structure, the block that proves it works, and the minimum count of that block. */
    private record Probe(String id, Block marker, int minimum) {}

    private static final Map<ResourceKey<Level>, Probe[]> PROBES = new LinkedHashMap<>();

    static {
        PROBES.put(Level.OVERWORLD, new Probe[]{
                new Probe("minecraft:stronghold", Blocks.END_PORTAL_FRAME, 12),
                new Probe("minecraft:ancient_city", Blocks.SCULK_SHRIEKER, 1),
                new Probe("minecraft:trial_chambers", Blocks.TRIAL_SPAWNER, 1),
                new Probe("minecraft:mineshaft", Blocks.RAIL, 1),
                new Probe("minecraft:monument", Blocks.PRISMARINE, 1),
                new Probe("minecraft:shipwreck", null, 0),
                new Probe("minecraft:ocean_ruin_cold", null, 0),
                new Probe("minecraft:ocean_ruin_warm", null, 0),
                new Probe("minecraft:desert_pyramid", null, 0),
                new Probe("minecraft:jungle_pyramid", null, 0),
                new Probe("minecraft:igloo", null, 0),
                new Probe("minecraft:trail_ruins", null, 0),
                new Probe("minecraft:mansion", null, 0),
                new Probe("minecraft:pillager_outpost", null, 0),
                new Probe("minecraft:village_plains", null, 0),
                new Probe("minecraft:village_desert", null, 0),
                new Probe("minecraft:village_savanna", null, 0),
                new Probe("minecraft:village_snowy", null, 0),
                new Probe("minecraft:village_taiga", null, 0),
                new Probe("minecraft:ruined_portal", null, 0),
                new Probe("minecraft:swamp_hut", null, 0),
                new Probe("minecraft:buried_treasure", null, 0)
        });
        PROBES.put(Level.NETHER, new Probe[]{
                new Probe("minecraft:fortress", Blocks.NETHER_BRICKS, 1),
                new Probe("minecraft:bastion_remnant", null, 0)
        });
        PROBES.put(Level.END, new Probe[]{
                new Probe("minecraft:end_city", Blocks.PURPUR_BLOCK, 1)
        });
    }

    private StructureQa() {}

    static JsonObject run(MinecraftServer server, JsonArray checks) {
        JsonObject report = new JsonObject();
        Registry<Structure> registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<ResourceKey<Level>, Probe[]> entry : PROBES.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) {
                report.addProperty(entry.getKey().location().toString(), "dimension missing");
                continue;
            }
            BlockPos origin = entry.getKey() == Level.OVERWORLD ? level.getSharedSpawnPos() : BlockPos.ZERO;
            for (Probe probe : entry.getValue()) {
                report.add(probe.id(), probe(level, registry, probe, origin));
            }
        }
        ServerLevel overworld = server.overworld();
        report.add("biome_timing", biomeTiming(overworld));
        JsonObject precheck = new JsonObject();
        for (String id : new String[]{"minecraft:monument", "minecraft:desert_pyramid", "minecraft:jungle_pyramid"}) {
            precheck.add(id, comparePrecheck(overworld, registry, id, checks));
        }
        report.add("precheck_comparison", precheck);
        ServerLevel end = server.getLevel(Level.END);
        report.addProperty("end_dragon_fight_present", end != null && end.getDragonFight() != null);
        return report;
    }

    private static JsonObject probe(ServerLevel level, Registry<Structure> registry, Probe probe, BlockPos origin) {
        JsonObject result = new JsonObject();
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(probe.id()));
        Holder.Reference<Structure> holder = registry.getHolder(key).orElse(null);
        if (holder == null) {
            result.addProperty("status", "not registered");
            return result;
        }
        long started = System.nanoTime();
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(holder), origin, SEARCH_RADIUS_CHUNKS, false);
        result.addProperty("search_ms", (System.nanoTime() - started) / 1_000_000);
        if (found == null) {
            result.addProperty("status", "not found within " + SEARCH_RADIUS_CHUNKS + " chunks");
            Dorohedoro.LOGGER.info("QA structure {} NOT FOUND near {}", probe.id(), origin.toShortString());
            return result;
        }
        BlockPos pos = found.getFirst();
        result.addProperty("pos", pos.toShortString());
        result.addProperty("distance", (int) Math.sqrt(pos.distSqr(new BlockPos(origin.getX(), pos.getY(), origin.getZ()))));
        ChunkAccess startChunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
        StructureStart start = startChunk.getStartForStructure(holder.value());
        boolean valid = start != null && start.isValid();
        result.addProperty("start_valid", valid);
        if (valid && probe.marker() != null) {
            int count = countMarker(level, start.getBoundingBox(), probe.marker(), probe.minimum());
            result.addProperty("marker", net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(probe.marker()).toString());
            result.addProperty("marker_count", count);
            result.addProperty("status", count >= probe.minimum() ? "verified" : "found, markers missing");
        } else {
            result.addProperty("status", valid ? "found" : "located, start invalid");
        }
        Dorohedoro.LOGGER.info("QA structure {} at {}: {}", probe.id(), pos.toShortString(), result.get("status").getAsString());
        return result;
    }

    /** Cost of one HoleBiomeSource lookup at street level and underground, near spawn and across the locate range. */
    private static JsonObject biomeTiming(ServerLevel level) {
        JsonObject result = new JsonObject();
        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        BlockPos spawn = level.getSharedSpawnPos();
        // Spawn neighborhood, then a 50,000-block square like a 100-cell locate, at street level and in the caves.
        int[][] cases = {{spawn.getX(), spawn.getZ(), 4, 64}, {spawn.getX(), spawn.getZ(), 400, 64}, {spawn.getX(), spawn.getZ(), 400, 0}};
        String[] names = {"street_near_spawn_ns", "street_wide_ns", "underground_wide_ns"};
        int samples = 100_000;
        for (int c = 0; c < cases.length; c++) {
            int side = 316;
            long started = System.nanoTime();
            int sink = 0;
            for (int i = 0; i < samples; i++) {
                int bx = cases[c][0] + (i % side - side / 2) * cases[c][2];
                int bz = cases[c][1] + (i / side - side / 2) * cases[c][2];
                sink += source.getNoiseBiome(QuartPos.fromBlock(bx), QuartPos.fromBlock(cases[c][3]), QuartPos.fromBlock(bz), sampler).hashCode();
            }
            long ns = (System.nanoTime() - started) / samples;
            result.addProperty(names[c], ns);
            Dorohedoro.LOGGER.info("QA biome timing {} = {} ns per lookup ({})", names[c], ns, sink & 1);
        }
        return result;
    }

    /**
     * Runs vanilla's own placement test on real candidate chunks twice, with and without {@link StructurePrecheck72},
     * and fails QA if a single answer or position differs. Also explains why candidates are rejected.
     */
    private static JsonObject comparePrecheck(ServerLevel level, Registry<Structure> registry, String id, JsonArray checks) {
        JsonObject result = new JsonObject();
        Holder.Reference<Structure> holder = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(id))).orElse(null);
        if (holder == null) {
            result.addProperty("status", "not registered");
            return result;
        }
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        RandomState randomState = level.getChunkSource().randomState();
        RandomSpreadStructurePlacement placement = null;
        for (StructurePlacement candidate : state.getPlacementsForStructure(holder)) {
            if (candidate instanceof RandomSpreadStructurePlacement spread) placement = spread;
        }
        if (placement == null) {
            result.addProperty("status", "no random-spread placement");
            return result;
        }
        Structure structure = holder.value();
        BlockPos spawn = level.getSharedSpawnPos();
        int spacing = placement.spacing();
        int originCellX = Math.floorDiv(spawn.getX() >> 4, spacing);
        int originCellZ = Math.floorDiv(spawn.getZ() >> 4, spacing);
        int half = 10;
        long vanillaNs = 0;
        long precheckNs = 0;
        int candidates = 0;
        int presentVanilla = 0;
        int presentPrecheck = 0;
        int skipped = 0;
        int mismatches = 0;
        String firstMismatch = "";
        Map<String, Integer> reasons = new java.util.TreeMap<>();
        HoleBiomeSource hole = generator.getBiomeSource() instanceof HoleBiomeSource h ? h : null;
        boolean wasEnabled = StructurePrecheck72.enabled;
        try {
            for (int cx = originCellX - half; cx <= originCellX + half; cx++) {
                for (int cz = originCellZ - half; cz <= originCellZ + half; cz++) {
                    ChunkPos chunk = placement.getPotentialStructureChunk(state.getLevelSeed(), cx, cz);
                    candidates++;
                    StructurePrecheck72.enabled = false;
                    long t0 = System.nanoTime();
                    Optional<Structure.GenerationStub> vanilla = structure.findValidGenerationPoint(context(level, generator, randomState, state, chunk, structure));
                    long t1 = System.nanoTime();
                    StructurePrecheck72.enabled = true;
                    boolean provedAbsent = StructurePrecheck72.provablyAbsent(structure, context(level, generator, randomState, state, chunk, structure));
                    Optional<Structure.GenerationStub> fast = structure.findValidGenerationPoint(context(level, generator, randomState, state, chunk, structure));
                    long t2 = System.nanoTime();
                    vanillaNs += t1 - t0;
                    precheckNs += t2 - t1;
                    if (vanilla.isPresent()) presentVanilla++;
                    if (fast.isPresent()) presentPrecheck++;
                    if (provedAbsent) skipped++;
                    boolean same = vanilla.isPresent() == fast.isPresent()
                            && (vanilla.isEmpty() || vanilla.get().position().equals(fast.get().position()));
                    if (!same && mismatches++ == 0) firstMismatch = chunk.toString();
                    if (hole != null) {
                        String surface = hole.surfaceBiome(chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), randomState.sampler())
                                .unwrapKey().map(k -> k.location().getPath()).orElse("?");
                        String outcome = vanilla.isPresent() ? "valid" : provedAbsent ? "proved_absent" : "rejected_by_vanilla";
                        reasons.merge(surface + ":" + outcome, 1, Integer::sum);
                    }
                }
            }
        } finally {
            StructurePrecheck72.enabled = wasEnabled;
        }
        result.addProperty("candidates", candidates);
        result.addProperty("vanilla_ms", vanillaNs / 1_000_000);
        result.addProperty("precheck_ms", precheckNs / 1_000_000);
        result.addProperty("present_vanilla", presentVanilla);
        result.addProperty("present_precheck", presentPrecheck);
        result.addProperty("proved_absent", skipped);
        result.addProperty("mismatches", mismatches);
        JsonObject reasonJson = new JsonObject();
        reasons.forEach(reasonJson::addProperty);
        result.add("center_biome_outcomes", reasonJson);
        JsonObject check = new JsonObject();
        check.addProperty("name", "structure_precheck_matches_vanilla_" + id.substring(id.indexOf(':') + 1));
        check.addProperty("passed", mismatches == 0);
        check.addProperty("detail", candidates + " candidates, " + mismatches + " mismatches" + (mismatches > 0 ? ", first " + firstMismatch : "")
                + "; vanilla " + vanillaNs / 1_000_000 + " ms, with precheck " + precheckNs / 1_000_000 + " ms");
        checks.add(check);
        Dorohedoro.LOGGER.info("QA precheck {}: {}", id, result);
        return result;
    }

    /** The same context StructureCheck builds when /locate asks whether a chunk can hold a structure. */
    private static Structure.GenerationContext context(ServerLevel level, ChunkGenerator generator, RandomState randomState,
                                                       ChunkGeneratorStructureState state, ChunkPos chunk, Structure structure) {
        return new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(), randomState,
                level.getServer().getStructureManager(), state.getLevelSeed(), chunk, level, structure.biomes()::contains);
    }

    /** Loads the structure's chunks and counts marker blocks, stopping once the minimum is met. */
    private static int countMarker(ServerLevel level, BoundingBox box, Block marker, int minimum) {
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int target = Math.max(minimum, 1);
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                ChunkAccess chunk = level.getChunk(cx, cz);
                for (int x = Math.max(box.minX(), cx << 4); x <= Math.min(box.maxX(), (cx << 4) + 15); x++) {
                    for (int z = Math.max(box.minZ(), cz << 4); z <= Math.min(box.maxZ(), (cz << 4) + 15); z++) {
                        for (int y = box.minY(); y <= box.maxY(); y++) {
                            if (chunk.getBlockState(cursor.set(x, y, z)).is(marker) && ++count >= target && marker != Blocks.END_PORTAL_FRAME) {
                                return count;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }
}
