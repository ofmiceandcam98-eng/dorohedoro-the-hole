package com.dorohedoro.hole.qa;

import com.dorohedoro.hole.Dorohedoro;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
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
