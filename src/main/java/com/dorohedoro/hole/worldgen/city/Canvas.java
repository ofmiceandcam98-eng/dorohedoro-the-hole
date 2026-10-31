package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Draws into one chunk, silently dropping anything outside it. A structure drawn through a canvas produces the same
 * result however it is split across chunks, so generators can place blocks in absolute coordinates and never think
 * about borders. Variation must come from {@link #hash}, never from a stateful random, for that to stay true.
 */
public final class Canvas {
    private final ChunkAccess chunk;
    private final long seed;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    public final int minX;
    public final int minZ;
    public final int maxX;
    public final int maxZ;

    public Canvas(ChunkAccess chunk, long seed) {
        this.chunk = chunk;
        this.seed = seed;
        ChunkPos pos = chunk.getPos();
        this.minX = pos.getMinBlockX();
        this.minZ = pos.getMinBlockZ();
        this.maxX = pos.getMaxBlockX();
        this.maxZ = pos.getMaxBlockZ();
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(Rect rect) {
        return rect.x0() <= maxX && rect.x1() >= minX && rect.z0() <= maxZ && rect.z1() >= minZ;
    }

    public long hash(int x, int y, int z) {
        return CityHash.of(seed, x, y, z);
    }

    public void set(int x, int y, int z, BlockState state) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), state, false);
        if (state.hasBlockEntity()) {
            CompoundTag tag = positioned(x, y, z);
            tag.putString("id", "DUMMY");
            chunk.setBlockEntityNbt(tag);
        }
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public void set(int x, int y, int z, Mix mix) {
        if (contains(x, z)) {
            set(x, y, z, mix.pick(hash(x, y, z)));
        }
    }

    public BlockState get(int x, int y, int z) {
        return chunk.getBlockState(cursor.set(x, y, z));
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Mix mix) {
        int fromX = Math.max(x0, minX);
        int toX = Math.min(x1, maxX);
        int fromZ = Math.max(z0, minZ);
        int toZ = Math.min(z1, maxZ);
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = y0; y <= y1; y++) {
                    set(x, y, z, mix.pick(hash(x, y, z)));
                }
            }
        }
    }

    public void fill(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    public void fill(Rect rect, int y0, int y1, Block block) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z1(), block);
    }

    public void clear(Rect rect, int y0, int y1) {
        fill(rect, y0, y1, Blocks.AIR);
    }

    /** The four outer walls of a rectangle. */
    public void walls(Rect rect, int y0, int y1, Mix mix) {
        fill(rect.x0(), y0, rect.z0(), rect.x1(), y1, rect.z0(), mix);
        fill(rect.x0(), y0, rect.z1(), rect.x1(), y1, rect.z1(), mix);
        fill(rect.x0(), y0, rect.z0(), rect.x0(), y1, rect.z1(), mix);
        fill(rect.x1(), y0, rect.z0(), rect.x1(), y1, rect.z1(), mix);
    }

    /** A pane or bar run along one axis, with each piece's connections set directly so no neighbour update is needed. */
    public void paneRun(int x0, int y0, int z0, int x1, int y1, int z1, Block pane) {
        boolean alongX = x1 != x0;
        boolean alongZ = z1 != z0;
        BlockState state = pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, alongZ)
                .setValue(CrossCollisionBlock.SOUTH, alongZ);
        fill(x0, y0, z0, x1, y1, z1, state);
    }

    /** A pane in a wall running along {@code axis}. */
    public void pane(int x, int y, int z, Direction.Axis axis, Block pane) {
        boolean alongX = axis == Direction.Axis.X;
        set(x, y, z, pane.defaultBlockState()
                .setValue(CrossCollisionBlock.EAST, alongX)
                .setValue(CrossCollisionBlock.WEST, alongX)
                .setValue(CrossCollisionBlock.NORTH, !alongX)
                .setValue(CrossCollisionBlock.SOUTH, !alongX));
    }

    public void fenceRun(int x0, int y, int z0, int x1, int z1, Block fence) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                set(x, y, z, fence.defaultBlockState()
                        .setValue(CrossCollisionBlock.EAST, x < x1)
                        .setValue(CrossCollisionBlock.WEST, x > x0)
                        .setValue(CrossCollisionBlock.SOUTH, z < z1)
                        .setValue(CrossCollisionBlock.NORTH, z > z0));
            }
        }
    }

    /** A rectangular fence with a one-block gap at {@code (gapX, gapZ)}. */
    public void fenceRing(Rect rect, int y, Block fence, int gapX, int gapZ) {
        fenceRun(rect.x0(), y, rect.z0(), rect.x1(), rect.z0(), fence);
        fenceRun(rect.x0(), y, rect.z1(), rect.x1(), rect.z1(), fence);
        fenceRun(rect.x0(), y, rect.z0(), rect.x0(), rect.z1(), fence);
        fenceRun(rect.x1(), y, rect.z0(), rect.x1(), rect.z1(), fence);
        set(gapX, y, gapZ, Blocks.AIR);
    }

    public void door(int x, int y, int z, Direction facing, Block door) {
        BlockState lower = door.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        set(x, y, z, lower);
        set(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** A bed whose foot is at {@code (x, y, z)}, with the head one block toward {@code facing}. */
    public void bed(int x, int y, int z, Direction facing, Block bed) {
        BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
        set(x, y, z, foot);
        set(x + facing.getStepX(), y, z + facing.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    public void ladder(int x, int y0, int y1, int z, Direction facing) {
        BlockState state = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        for (int y = y0; y <= y1; y++) {
            set(x, y, z, state);
        }
    }

    public void stair(int x, int y, int z, Direction facing, Block stair) {
        set(x, y, z, stair.defaultBlockState().setValue(StairBlock.FACING, facing));
    }

    public void facing(int x, int y, int z, Direction facing, Block block) {
        set(x, y, z, block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing));
    }

    /** A chest that rolls {@code table} when a player first opens it. */
    public void chest(int x, int y, int z, Direction facing, String table) {
        container(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), "minecraft:chest", table);
    }

    public void barrel(int x, int y, int z, String table) {
        container(x, y, z, Blocks.BARREL.defaultBlockState(), "minecraft:barrel", table);
    }

    private void container(int x, int y, int z, BlockState state, String id, String table) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), state, false);
        CompoundTag tag = positioned(x, y, z);
        tag.putString("id", id);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "chests/" + table));
        tag.putString("LootTable", key.location().toString());
        tag.putLong("LootTableSeed", hash(x, y, z));
        chunk.setBlockEntityNbt(tag);
    }

    /** A wall sign on the face of the block behind it, reading up to four lines. */
    public void sign(int x, int y, int z, Direction facing, Block wallSign, String... lines) {
        if (!contains(x, z)) {
            return;
        }
        chunk.setBlockState(cursor.set(x, y, z), wallSign.defaultBlockState().setValue(WallSignBlock.FACING, facing), false);
        ListTag messages = new ListTag();
        for (int i = 0; i < 4; i++) {
            String line = i < lines.length ? lines[i] : "";
            messages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line), net.minecraft.core.RegistryAccess.EMPTY)));
        }
        CompoundTag side = new CompoundTag();
        side.put("messages", messages);
        side.putString("color", "black");
        side.putBoolean("has_glowing_text", false);
        CompoundTag tag = positioned(x, y, z);
        tag.putString("id", "minecraft:sign");
        tag.put("front_text", side);
        tag.putBoolean("is_waxed", true);
        chunk.setBlockEntityNbt(tag);
    }

    private static CompoundTag positioned(int x, int y, int z) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }
}
