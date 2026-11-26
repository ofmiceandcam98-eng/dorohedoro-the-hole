package com.dorohedoro.hole.worldgen.sorcerer;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.RealmBiomes;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Removes vanilla tree blocks from settlement streets, walks and buildings once a chunk is final.
 *
 * A tree rooted in a neighbouring chunk can be decorated after this chunk's settlement was painted, and drop its crown
 * across a street or a doorstep. A chunk only reaches FULL once all of its neighbours are decorated, so tidying newly
 * generated chunks at that point removes every such intrusion, in any generation order. The work runs on the next level
 * tick (writing during chunk promotion could load chunks re-entrantly) and touches only logs, leaves and vines.
 */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class SettlementTidy {
    private static final ConcurrentLinkedQueue<Long> PENDING = new ConcurrentLinkedQueue<>();

    private SettlementTidy() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(RealmBiomes.SORCERERS_WORLD)) return;
        PENDING.add(event.getChunk().getPos().toLong());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(RealmBiomes.SORCERERS_WORLD)) flush(level);
    }

    /** Tidies every queued chunk that is loaded. QA calls this directly, since it runs inside a single tick. */
    public static void flush(ServerLevel level) {
        int budget = PENDING.size();
        while (budget-- > 0) {
            Long packed = PENDING.poll();
            if (packed == null) return;
            ChunkPos pos = new ChunkPos(packed);
            if (level.getChunkSource().getChunkNow(pos.x, pos.z) == null) continue;
            tidy(level, pos);
        }
    }

    private static void tidy(ServerLevel level, ChunkPos chunk) {
        BoundingBox box = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
        for (StructureStart start : level.structureManager().startsForStructure(chunk, s -> s instanceof SorcererTownStructure)) {
            for (StructurePiece piece : start.getPieces()) {
                if (!piece.getBoundingBox().intersects(box)) continue;
                if (piece instanceof TownRoadsPiece roads) {
                    RoadNet net = roads.net();
                    for (int x = box.minX(); x <= box.maxX(); x++) for (int z = box.minZ(); z <= box.maxZ(); z++) {
                        RoadNet.Hit hit = net.sample(x, z);
                        if (hit.road()) clear(level, x, z, hit.y(), hit.y() + 24);
                    }
                } else if (piece instanceof TownBuildingPiece building) {
                    TownPlanner.Plot plot = building.plot();
                    TownFrame f = plot.frame();
                    int top = plot.floorY() + plot.spec().height() + 14;
                    for (int x = Math.max(f.minX(), box.minX()); x <= Math.min(f.maxX(), box.maxX()); x++)
                        for (int z = Math.max(f.minZ(), box.minZ()); z <= Math.min(f.maxZ(), box.maxZ()); z++)
                            clear(level, x, z, plot.floorY(), top);
                    for (int k = 1; k <= plot.walk(); k++) {
                        int x = plot.doorX() + TownFrame.DX[plot.facing()] * k, z = plot.doorZ() + TownFrame.DZ[plot.facing()] * k;
                        if (box.isInside(x, box.minY(), z)) clear(level, x, z, plot.floorY() - plot.walk(), plot.floorY() + plot.walk() + 16);
                    }
                }
            }
        }
    }

    private static void clear(ServerLevel level, int x, int z, int fromY, int toY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = fromY; y <= toY; y++) {
            pos.set(x, y, z);
            if (Painter.isTree(level.getBlockState(pos))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }
}
