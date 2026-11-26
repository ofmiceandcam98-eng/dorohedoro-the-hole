package com.dorohedoro.hole.worldgen.sorcerer;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Writes a building in its local frame (see {@link TownFrame}): x along the facade, y = 0 at the ground floor's feet,
 * z from the front wall (0) to the back. Directions are local too: NORTH is out of the front door, SOUTH is the back
 * wall, EAST is +x. Every block state is turned into the world frame on the way out.
 *
 * Painting is a pure function of the plot seed, so a building split across chunks comes out identical from every
 * side; the {@link Sink} decides which writes land (the current chunk) and which are dropped.
 */
public final class Painter {
    public interface Sink {
        boolean inside(BlockPos pos);

        void set(BlockPos pos, BlockState state);

        BlockState get(BlockPos pos);

        void loot(BlockPos pos, ResourceKey<LootTable> table);

        void sign(BlockPos pos, String[] lines);

        void villager(BlockPos pos, VillagerProfession profession);

        /** QA marker: a room's floor centre, the door, the walk end. Ignored by real painting. */
        default void probe(String kind, BlockPos pos) {}
    }

    public final TownFrame frame;
    public final int floorY;
    public final TownRandom rng;
    private final Sink sink;
    private final Rotation rotation;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public Painter(TownFrame frame, int floorY, long seed, Sink sink) {
        this.frame = frame;
        this.floorY = floorY;
        this.rng = new TownRandom(seed);
        this.sink = sink;
        this.rotation = switch (frame.facing()) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public int w() {
        return frame.w();
    }

    public int d() {
        return frame.d();
    }

    public BlockPos pos(int x, int y, int z) {
        return new BlockPos(frame.worldX(x, z), floorY + y, frame.worldZ(x, z));
    }

    public Sink sink() {
        return sink;
    }

    // ------------------------------------------------------------ primitives

    public void set(int x, int y, int z, BlockState state) {
        cursor.set(frame.worldX(x, z), floorY + y, frame.worldZ(x, z));
        if (!sink.inside(cursor)) return;
        sink.set(cursor.immutable(), state.rotate(rotation));
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public BlockState get(int x, int y, int z) {
        BlockPos p = pos(x, y, z);
        return sink.inside(p) ? sink.get(p) : Blocks.AIR.defaultBlockState();
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++)
                for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) set(x, y, z, state);
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    public void air(int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(x0, y0, z0, x1, y1, z1, Blocks.AIR);
    }

    /** Perimeter walls of a rectangle, from y0 to y1. */
    public void walls(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                set(x, y, z0, state);
                set(x, y, z1, state);
            }
            for (int z = z0 + 1; z < z1; z++) {
                set(x0, y, z, state);
                set(x1, y, z, state);
            }
        }
    }

    /**
     * Fills from just under the floor down to natural ground, so no corner floats over a slope and no floor opens onto
     * a cave. Stops at the first solid block or after 20 blocks.
     */
    public void foundation(int x, int z, int topY, BlockState state) {
        for (int y = topY, depth = 0; depth < 20; y--, depth++) {
            BlockPos p = pos(x, y, z);
            if (!sink.inside(p)) return;
            BlockState here = sink.get(p);
            if (depth > 0 && solidGround(here)) return;
            set(x, y, z, state);
        }
    }

    /**
     * Settlements generate after vanilla trees, so a tree that rooted where a house or path now stands leaves a trunk
     * or a crown hanging above it. This removes logs, leaves and vines (nothing else) in a column.
     */
    public void clearTrees(int x, int z, int fromY, int toY) {
        for (int y = fromY; y <= toY; y++) {
            BlockPos p = pos(x, y, z);
            if (!sink.inside(p)) return;
            if (isTree(sink.get(p))) set(x, y, z, Blocks.AIR);
        }
    }

    public static boolean isTree(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.is(Blocks.VINE) || state.is(Blocks.BEE_NEST);
    }

    /** Natural or placed ground that can carry a wall: not air, not fluid, not plants or snow. */
    public static boolean solidGround(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced()
                && !state.is(net.minecraft.tags.BlockTags.LEAVES) && !state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    // ------------------------------------------------------------ shaped blocks

    public static BlockState stair(Block block, Direction facing, boolean top) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState slab(Block block, boolean top) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    public static BlockState facing(Block block, Direction facing) {
        BlockState s = block.defaultBlockState();
        if (s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return s.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        if (s.hasProperty(BlockStateProperties.FACING)) return s.setValue(BlockStateProperties.FACING, facing);
        return s;
    }

    public static BlockState axis(Block block, Direction.Axis axis) {
        BlockState s = block.defaultBlockState();
        return s.hasProperty(BlockStateProperties.AXIS) ? s.setValue(BlockStateProperties.AXIS, axis) : s;
    }

    public void door(int x, int y, int z, Block door, Direction facing, boolean rightHinge) {
        BlockState base = door.defaultBlockState().setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, rightHinge ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
        set(x, y, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(x, y + 1, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** A bed whose head is one step from (x, z) in {@code head}. */
    public void bed(int x, int y, int z, Direction head, Block bed) {
        BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, head).setValue(BedBlock.PART, BedPart.FOOT);
        set(x, y, z, foot);
        set(x + head.getStepX(), y, z + head.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    public void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> table) {
        set(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
        BlockPos p = pos(x, y, z);
        if (sink.inside(p) && table != null) sink.loot(p, table);
    }

    public void barrel(int x, int y, int z, Direction facing, ResourceKey<LootTable> table) {
        set(x, y, z, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, facing));
        BlockPos p = pos(x, y, z);
        if (sink.inside(p) && table != null) sink.loot(p, table);
    }

    public void lantern(int x, int y, int z, boolean hanging) {
        set(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging));
    }

    public void soulLantern(int x, int y, int z, boolean hanging) {
        set(x, y, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging));
    }

    /** A wall sign on the block at (x, y, z), its face looking {@code facing}. */
    public void sign(int x, int y, int z, Block wallSign, Direction facing, String... lines) {
        set(x, y, z, wallSign.defaultBlockState().setValue(WallSignBlock.FACING, facing));
        BlockPos p = pos(x, y, z);
        if (sink.inside(p)) sink.sign(p, lines);
    }

    public void villager(int x, int y, int z, VillagerProfession profession) {
        BlockPos p = pos(x, y, z);
        if (sink.inside(p)) sink.villager(p, profession);
    }

    public void probe(String kind, int x, int y, int z) {
        sink.probe(kind, pos(x, y, z));
    }

    /** Runs {@code body} once per storey with the storey's base y. */
    public void storeys(int count, int height, Consumer<Integer> body) {
        for (int i = 0; i < count; i++) body.accept(i * height);
    }
}
