package com.dorohedoro.hole.worldgen.sorcerer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Every road, the plaza and the street lamps of one settlement. One piece covers the whole settlement because the
 * structure uses no terrain adaptation: the road does its own grading. Paving sits at the planned height; below it the
 * column is filled down to natural ground (or carried on pillars as a bridge over water and deep dips), above it the
 * terrain is cut back for headroom, and the verge gets a retaining wall where the road cuts into a slope or an earth
 * shoulder where it runs above it. A column one block below a neighbouring road column gets a half slab so every rise
 * can be walked without jumping.
 */
public final class TownRoadsPiece extends StructurePiece {
    private final int cx, cz, cy, radius, plazaRadius, kind;
    private final long seed;
    private final List<TownPlanner.Road> roads;
    private final List<TownPlanner.Lamp> lamps;
    private RoadNet net;

    public TownRoadsPiece(TownPlanner.Layout layout, long seed) {
        super(SorcererStructures.ROADS_PIECE.get(), 0, box(layout.cx(), layout.cz(), layout.cy(), layout.radius()));
        this.cx = layout.cx();
        this.cz = layout.cz();
        this.cy = layout.cy();
        this.radius = layout.radius();
        this.plazaRadius = layout.plazaRadius();
        this.kind = layout.kind().ordinal();
        this.seed = seed;
        this.roads = layout.roads();
        this.lamps = layout.lamps();
        setOrientation(null);
    }

    public TownRoadsPiece(CompoundTag tag) {
        super(SorcererStructures.ROADS_PIECE.get(), tag);
        this.cx = tag.getInt("CX");
        this.cz = tag.getInt("CZ");
        this.cy = tag.getInt("CY");
        this.radius = tag.getInt("Radius");
        this.plazaRadius = tag.getInt("Plaza");
        this.kind = tag.getInt("Kind");
        this.seed = tag.getLong("Seed");
        List<TownPlanner.Road> r = new ArrayList<>();
        for (Tag t : tag.getList("Roads", Tag.TAG_INT_ARRAY)) {
            int[] a = ((IntArrayTag) t).getAsIntArray();
            List<int[]> pts = new ArrayList<>();
            for (int i = 1; i + 2 < a.length; i += 3) pts.add(new int[]{a[i], a[i + 1], a[i + 2]});
            r.add(new TownPlanner.Road(a[0], pts));
        }
        this.roads = r;
        int[] l = tag.getIntArray("Lamps");
        List<TownPlanner.Lamp> ls = new ArrayList<>();
        for (int i = 0; i + 2 < l.length; i += 3) ls.add(new TownPlanner.Lamp(l[i], l[i + 1], l[i + 2]));
        this.lamps = ls;
        setOrientation(null);
    }

    private static BoundingBox box(int cx, int cz, int cy, int radius) {
        int r = radius + 16;
        return new BoundingBox(cx - r, cy - 48, cz - r, cx + r, cy + 48, cz + r);
    }

    public TownPlanner.Kind kind() {
        return TownPlanner.Kind.values()[kind];
    }

    public RoadNet net() {
        if (net == null) net = new RoadNet(cx, cz, cy, plazaRadius, roads);
        return net;
    }

    public int centreX() {
        return cx;
    }

    public int centreZ() {
        return cz;
    }

    public int centreY() {
        return cy;
    }

    public List<TownPlanner.Lamp> lamps() {
        return lamps;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", cx);
        tag.putInt("CZ", cz);
        tag.putInt("CY", cy);
        tag.putInt("Radius", radius);
        tag.putInt("Plaza", plazaRadius);
        tag.putInt("Kind", kind);
        tag.putLong("Seed", seed);
        ListTag list = new ListTag();
        for (TownPlanner.Road road : roads) {
            int[] a = new int[1 + road.points().size() * 3];
            a[0] = road.cls();
            for (int i = 0; i < road.points().size(); i++) System.arraycopy(road.points().get(i), 0, a, 1 + i * 3, 3);
            list.add(new IntArrayTag(a));
        }
        tag.put("Roads", list);
        int[] l = new int[lamps.size() * 3];
        for (int i = 0; i < lamps.size(); i++) {
            l[i * 3] = lamps.get(i).x();
            l[i * 3 + 1] = lamps.get(i).z();
            l[i * 3 + 2] = lamps.get(i).y();
        }
        tag.putIntArray("Lamps", l);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        RoadNet n = net();
        for (int z = chunkBox.minZ(); z <= chunkBox.maxZ(); z++) {
            for (int x = chunkBox.minX(); x <= chunkBox.maxX(); x++) {
                if (Math.abs(x - cx) > radius + 16 || Math.abs(z - cz) > radius + 16) continue;
                column(level, chunkBox, n, x, z);
            }
        }
        for (TownPlanner.Lamp lamp : lamps) {
            if (chunkBox.isInside(lamp.x(), chunkBox.minY() + 1, lamp.z())) lamp(level, chunkBox, lamp);
        }
        centrepiece(level, chunkBox);
    }

    private void set(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!clip.isInside(pos)) return;
        level.setBlock(pos, state, 2);
        if (state.getBlock() instanceof StairBlock || state.getBlock() instanceof net.minecraft.world.level.block.CrossCollisionBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.WallBlock) {
            level.getChunk(pos).markPosForPostprocessing(pos);
        }
    }

    private void column(WorldGenLevel level, BoundingBox clip, RoadNet n, int x, int z) {
        RoadNet.Hit hit = n.sample(x, z);
        if (!hit.road() && !hit.verge()) return;
        int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        boolean wet = surface > ground;
        int y = hit.y();
        int h = TownRandom.hash(seed, x, z);
        TownPlanner.Kind k = kind();
        if (hit.road()) {
            boolean bridge = wet || ground < y - 6;
            BlockState paving = bridge ? deck(k) : paving(k, hit, h);
            if (!bridge) {
                for (int yy = y - 2; yy >= ground && yy >= y - 24; yy--) set(level, clip, x, yy, z, fill(k, yy, y));
            } else if (Math.floorMod(x, 4) == 0 && Math.floorMod(z, 4) == 0) {
                for (int yy = y - 2; yy >= ground - 1 && yy >= y - 40; yy--) set(level, clip, x, yy, z, Blocks.STONE_BRICKS.defaultBlockState());
            }
            set(level, clip, x, y - 1, z, paving);
            int clearTop = Math.min(Math.max(y + 3, surface), y + 14);
            for (int yy = y; yy <= clearTop; yy++) {
                BlockPos pos = new BlockPos(x, yy, z);
                if (clip.isInside(pos) && !level.getBlockState(pos).isAir()) set(level, clip, x, yy, z, Blocks.AIR.defaultBlockState());
            }
            // Vanilla trees generate before settlements: no trunks or crowns left hanging over the street.
            for (int yy = clearTop + 1; yy <= y + 24; yy++) {
                BlockPos pos = new BlockPos(x, yy, z);
                if (clip.isInside(pos) && Painter.isTree(level.getBlockState(pos))) set(level, clip, x, yy, z, Blocks.AIR.defaultBlockState());
            }
            if (hit.cls() != RoadNet.PLAZA) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    RoadNet.Hit nb = n.sample(x + dir.getStepX(), z + dir.getStepZ());
                    if (nb.road() && nb.y() == y + 1) {
                        set(level, clip, x, y, z, halfStep(k, paving));
                        break;
                    }
                }
                if (bridge && hit.dist() > RoadNet.RADIUS[hit.cls()] - 0.75 && level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                    set(level, clip, x, y, z, (k == TownPlanner.Kind.CITY ? Blocks.STONE_BRICK_WALL : Blocks.SPRUCE_FENCE).defaultBlockState());
                }
            }
        } else {
            if (ground > y + 1) {
                // The road cuts into the slope: a retaining wall faces the street.
                BlockState wall = (h & 7) == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                        : k == TownPlanner.Kind.HAMLET ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
                int top = Math.min(ground - 1, y + 10);
                for (int yy = y; yy <= top; yy++) set(level, clip, x, yy, z, wall);
            } else if (ground < y - 1 && !wet && ground >= y - 6) {
                // The road runs above the ground: an earth shoulder.
                for (int yy = y - 2; yy >= ground; yy--) set(level, clip, x, yy, z, Blocks.DIRT.defaultBlockState());
                set(level, clip, x, y - 1, z, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
    }

    private static BlockState paving(TownPlanner.Kind k, RoadNet.Hit hit, int h) {
        int roll = Math.floorMod(h, 10);
        if (hit.cls() == RoadNet.PLAZA) {
            int ring = (int) hit.dist();
            if (k == TownPlanner.Kind.HAMLET) return (roll < 6 ? Blocks.PACKED_MUD : Blocks.GRAVEL).defaultBlockState();
            if (ring % 3 == 0) return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            return (roll < 7 ? Blocks.STONE_BRICKS : roll < 9 ? Blocks.CRACKED_STONE_BRICKS : Blocks.MOSSY_STONE_BRICKS).defaultBlockState();
        }
        boolean kerb = hit.cls() == RoadNet.MAIN && hit.dist() > RoadNet.RADIUS[RoadNet.MAIN] - 0.6;
        return switch (k) {
            case CITY -> switch (hit.cls()) {
                case RoadNet.MAIN -> kerb ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : (roll < 5 ? Blocks.POLISHED_ANDESITE : roll < 8 ? Blocks.ANDESITE : Blocks.STONE).defaultBlockState();
                case RoadNet.STREET -> (roll < 6 ? Blocks.STONE_BRICKS : roll < 8 ? Blocks.CRACKED_STONE_BRICKS : Blocks.MOSSY_STONE_BRICKS).defaultBlockState();
                default -> (roll < 6 ? Blocks.COBBLESTONE : roll < 8 ? Blocks.MOSSY_COBBLESTONE : Blocks.GRAVEL).defaultBlockState();
            };
            case TOWN -> switch (hit.cls()) {
                case RoadNet.MAIN -> (roll < 6 ? Blocks.STONE_BRICKS : roll < 8 ? Blocks.COBBLESTONE : Blocks.CRACKED_STONE_BRICKS).defaultBlockState();
                case RoadNet.STREET -> (roll < 6 ? Blocks.COBBLESTONE : roll < 8 ? Blocks.GRAVEL : Blocks.MOSSY_COBBLESTONE).defaultBlockState();
                default -> (roll < 5 ? Blocks.GRAVEL : roll < 8 ? Blocks.COBBLESTONE : Blocks.PACKED_MUD).defaultBlockState();
            };
            case HAMLET -> (roll < 6 ? Blocks.DIRT_PATH : roll < 8 ? Blocks.GRAVEL : Blocks.PACKED_MUD).defaultBlockState();
        };
    }

    private static BlockState deck(TownPlanner.Kind k) {
        return (k == TownPlanner.Kind.CITY ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState();
    }

    private static BlockState fill(TownPlanner.Kind k, int yy, int roadY) {
        return (roadY - yy <= 3 ? Blocks.DIRT : Blocks.STONE).defaultBlockState();
    }

    /** The slab that matches a paving block, for half steps. */
    private static BlockState halfStep(TownPlanner.Kind k, BlockState paving) {
        Block b = paving.getBlock();
        Block slab = b == Blocks.STONE_BRICKS || b == Blocks.CRACKED_STONE_BRICKS || b == Blocks.POLISHED_DEEPSLATE ? Blocks.STONE_BRICK_SLAB
                : b == Blocks.MOSSY_STONE_BRICKS ? Blocks.MOSSY_STONE_BRICK_SLAB
                : b == Blocks.POLISHED_ANDESITE ? Blocks.POLISHED_ANDESITE_SLAB
                : b == Blocks.ANDESITE ? Blocks.ANDESITE_SLAB
                : b == Blocks.SMOOTH_STONE ? Blocks.SMOOTH_STONE_SLAB
                : b == Blocks.STONE ? Blocks.STONE_SLAB
                : b == Blocks.MOSSY_COBBLESTONE ? Blocks.MOSSY_COBBLESTONE_SLAB
                : b == Blocks.SPRUCE_PLANKS ? Blocks.SPRUCE_SLAB
                : b == Blocks.PACKED_MUD ? Blocks.MUD_BRICK_SLAB
                : Blocks.COBBLESTONE_SLAB;
        return slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
    }

    private void lamp(WorldGenLevel level, BoundingBox clip, TownPlanner.Lamp lamp) {
        int x = lamp.x(), z = lamp.z(), y = lamp.y();
        int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        for (int yy = y - 1; yy >= ground && yy >= y - 8; yy--) set(level, clip, x, yy, z, Blocks.COBBLESTONE.defaultBlockState());
        TownPlanner.Kind k = kind();
        BlockState post = switch (k) {
            case CITY -> Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState();
            case TOWN -> Blocks.SPRUCE_FENCE.defaultBlockState();
            case HAMLET -> Blocks.OAK_FENCE.defaultBlockState();
        };
        int height = k == TownPlanner.Kind.CITY ? 3 : 2;
        for (int i = 0; i < height; i++) set(level, clip, x, y + i, z, post);
        set(level, clip, x, y + height, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /** The plaza's smoke fountain and its bell, or a hamlet's well. */
    private void centrepiece(WorldGenLevel level, BoundingBox clip) {
        int y = cy;
        if (Math.abs(clip.minX() + 8 - cx) > 24 || Math.abs(clip.minZ() + 8 - cz) > 24) return;
        if (kind() == TownPlanner.Kind.HAMLET) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                boolean rim = dx != 0 || dz != 0;
                set(level, clip, cx + dx, y - 2, cz + dz, Blocks.COBBLESTONE.defaultBlockState());
                set(level, clip, cx + dx, y - 1, cz + dz, rim ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.WATER.defaultBlockState());
                if (rim) set(level, clip, cx + dx, y, cz + dz, Blocks.COBBLESTONE_WALL.defaultBlockState());
                if (Math.abs(dx) == 1 && Math.abs(dz) == 1) set(level, clip, cx + dx, y + 1, cz + dz, Blocks.OAK_FENCE.defaultBlockState());
                set(level, clip, cx + dx, y + 2, cz + dz, Blocks.OAK_SLAB.defaultBlockState());
            }
            set(level, clip, cx, y, cz, Blocks.AIR.defaultBlockState());
            set(level, clip, cx + 3, y, cz, bell());
            return;
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
            set(level, clip, cx + dx, y - 2, cz + dz, Blocks.STONE_BRICKS.defaultBlockState());
            if (rim) {
                set(level, clip, cx + dx, y - 1, cz + dz, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, clip, cx + dx, y, cz + dz, Blocks.STONE_BRICK_SLAB.defaultBlockState());
            } else {
                set(level, clip, cx + dx, y - 1, cz + dz, Blocks.WATER.defaultBlockState());
            }
        }
        // The smoke column: a dark pillar with a fire on top, visible across town.
        for (int yy = y - 1; yy <= y + 2; yy++) set(level, clip, cx, yy, cz, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        set(level, clip, cx, y + 3, cz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        set(level, clip, cx + 4, y, cz, bell());
        // Benches facing the fountain.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int bx = cx + dir.getStepX() * 4, bz = cz + dir.getStepZ() * 4;
            if (dir == Direction.EAST) continue;
            Direction side = dir.getClockWise();
            for (int s = -1; s <= 1; s++) {
                set(level, clip, bx + side.getStepX() * s, y, bz + side.getStepZ() * s,
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dir));
            }
        }
    }

    private static BlockState bell() {
        return Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR);
    }
}
