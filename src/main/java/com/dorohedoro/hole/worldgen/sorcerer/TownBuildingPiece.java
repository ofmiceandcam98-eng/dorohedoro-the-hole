package com.dorohedoro.hole.worldgen.sorcerer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** One building or open lot, with the walk from its door to the road. */
public final class TownBuildingPiece extends StructurePiece {
    private final TownPlanner.Plot plot;
    private final int kind;

    public TownBuildingPiece(TownPlanner.Plot plot, TownPlanner.Kind kind) {
        super(SorcererStructures.BUILDING_PIECE.get(), 0, box(plot));
        this.plot = plot;
        this.kind = kind.ordinal();
        setOrientation(null);
    }

    public TownBuildingPiece(CompoundTag tag) {
        super(SorcererStructures.BUILDING_PIECE.get(), tag);
        this.plot = new TownPlanner.Plot(tag.getString("Type"), tag.getInt("DoorX"), tag.getInt("DoorZ"),
                tag.getInt("Facing"), tag.getInt("FloorY"), tag.getInt("Walk"), tag.getInt("RoadY"), tag.getLong("Seed"));
        this.kind = tag.getInt("Kind");
        setOrientation(null);
    }

    public TownPlanner.Plot plot() {
        return plot;
    }

    static BoundingBox box(TownPlanner.Plot plot) {
        TownFrame f = plot.frame();
        int minX = f.minX() - 1, maxX = f.maxX() + 1, minZ = f.minZ() - 1, maxZ = f.maxZ() + 1;
        int fx = TownFrame.DX[plot.facing()], fz = TownFrame.DZ[plot.facing()];
        int ex = plot.doorX() + fx * (plot.walk() + 1), ez = plot.doorZ() + fz * (plot.walk() + 1);
        minX = Math.min(minX, ex);
        maxX = Math.max(maxX, ex);
        minZ = Math.min(minZ, ez);
        maxZ = Math.max(maxZ, ez);
        return new BoundingBox(minX, plot.floorY() - 22, minZ, maxX, plot.floorY() + plot.spec().height() + 4, maxZ);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Type", plot.type());
        tag.putInt("DoorX", plot.doorX());
        tag.putInt("DoorZ", plot.doorZ());
        tag.putInt("Facing", plot.facing());
        tag.putInt("FloorY", plot.floorY());
        tag.putInt("Walk", plot.walk());
        tag.putInt("RoadY", plot.roadY());
        tag.putLong("Seed", plot.seed());
        tag.putInt("Kind", kind);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        paint(new WorldSink(level, chunkBox, random));
    }

    /** Paints the walk, then the building. QA calls this with a recording sink to learn the probes. */
    public void paint(Painter.Sink sink) {
        Painter p = new Painter(plot.frame(), plot.floorY(), plot.seed(), sink);
        walk(p);
        SorcererBuildings.paint(plot.type(), p);
        p.probe("door", plot.spec().doorX(), 0, 0);
        p.probe("walk_end", plot.spec().doorX(), plot.roadY() - plot.floorY(), -1 - plot.walk());
    }

    /**
     * Steps from the door to the road. Descending walks reach road height at their last cell; ascending walks start
     * level with the door, so every rise sits on a walk cell and gets a stair block.
     */
    private void walk(Painter p) {
        int walk = plot.walk(), delta = plot.roadY() - plot.floorY(), x = plot.spec().doorX();
        int[] rel = new int[walk + 1];
        for (int k = 0; k < walk; k++) {
            rel[k] = delta < 0 ? (int) Math.round(delta * (k + 1) / (double) walk)
                    : (int) Math.round(delta * k / (double) walk);
        }
        rel[walk] = delta;
        BlockState paving = switch (TownPlanner.Kind.values()[kind]) {
            case CITY -> Blocks.STONE_BRICKS.defaultBlockState();
            case TOWN -> Blocks.COBBLESTONE.defaultBlockState();
            case HAMLET -> Blocks.PACKED_MUD.defaultBlockState();
        };
        BlockState stairs = (kind == TownPlanner.Kind.CITY.ordinal() ? Blocks.STONE_BRICK_STAIRS : Blocks.COBBLESTONE_STAIRS)
                .defaultBlockState();
        for (int k = 0; k < walk; k++) {
            int z = -1 - k, y = rel[k];
            p.foundation(x, z, y - 2, Blocks.COBBLESTONE.defaultBlockState());
            p.set(x, y - 1, z, paving);
            p.air(x, y, z, x, y + 2, z);
            p.clearTrees(x, z, y + 3, y + 16);
            int towardDoor = k == 0 ? 0 : rel[k - 1];
            if (y == towardDoor - 1) {
                p.set(x, y, z, stairs.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH));
            } else if (rel[k + 1] == y + 1) {
                p.set(x, y, z, stairs.setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
            }
        }
    }
}
