package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Pass 71 (v53): every storey of a tower is a furnished room with a purpose, laid out the way a person would.
 *
 * <p>Each room picks one feature wall and lines it with a deliberate run of furniture in a fixed order (a kitchen is
 * counter, stove, counter, sink...), then puts at most one or two table groups in the open floor. Everything else stays
 * clear. Walls with a window never get tall pieces. The stair row, the corridor beside it, the doorway aisle and a
 * cross aisle are never furnished, so the whole room stays reachable.
 *
 * <p>Each cell is decided from absolute coordinates and only its own chunk's blocks, so a room split across chunks is
 * furnished the same way from both sides.
 */
final class RoomDressing71 {
    enum Theme { KITCHEN, LIVING, BEDROOM, WORKSHOP, STORAGE, CLINIC, SMOKE_DEN, DINER }

    /** What goes along the feature wall, in order. */
    private enum Piece { COUNTER, STOVE, OVEN, SINK, BARREL, BARRELS, CHEST, SHELF, SHELVES, SOFA, SIDE_TABLE, LECTERN,
        BREWING, BED, ANVIL, GRINDSTONE, WARDROBE, NONE }

    private static final Theme[] GROUND = {Theme.DINER, Theme.KITCHEN, Theme.WORKSHOP, Theme.STORAGE, Theme.CLINIC,
            Theme.KITCHEN, Theme.WORKSHOP};
    private static final Theme[] UPPER = {Theme.LIVING, Theme.BEDROOM, Theme.BEDROOM, Theme.LIVING, Theme.SMOKE_DEN,
            Theme.KITCHEN, Theme.STORAGE, Theme.CLINIC, Theme.WORKSHOP};

    private static Piece[] run(Theme theme) {
        return switch (theme) {
            case KITCHEN -> new Piece[] {Piece.COUNTER, Piece.STOVE, Piece.COUNTER, Piece.SINK, Piece.COUNTER, Piece.BARREL};
            case DINER -> new Piece[] {Piece.COUNTER, Piece.STOVE, Piece.OVEN, Piece.COUNTER, Piece.SINK, Piece.BARRELS};
            case LIVING -> new Piece[] {Piece.SHELVES, Piece.SHELVES, Piece.NONE, Piece.SOFA, Piece.SOFA, Piece.SIDE_TABLE};
            case BEDROOM -> new Piece[] {Piece.SIDE_TABLE, Piece.BED, Piece.NONE, Piece.SIDE_TABLE, Piece.BED, Piece.WARDROBE};
            case WORKSHOP -> new Piece[] {Piece.COUNTER, Piece.ANVIL, Piece.COUNTER, Piece.GRINDSTONE, Piece.NONE, Piece.BARRELS};
            case STORAGE -> new Piece[] {Piece.BARRELS, Piece.BARRELS, Piece.CHEST, Piece.BARREL, Piece.BARRELS, Piece.CHEST};
            case CLINIC -> new Piece[] {Piece.BED, Piece.NONE, Piece.BED, Piece.SINK, Piece.COUNTER, Piece.BREWING};
            case SMOKE_DEN -> new Piece[] {Piece.SHELVES, Piece.LECTERN, Piece.SHELVES, Piece.NONE, Piece.BREWING, Piece.COUNTER};
        };
    }

    private RoomDressing71() {}

    static void dress(Canvas c, Rect box, Rect in, int floors, long hash) {
        if (in.width() < 7 || in.depth() < 7) return;
        for (int k = 0; k < floors; k++) {
            int floorY = STREET_Y + k * Tower.STOREY;
            long h = CityHash.of(hash, 0x71D2E55L, k);
            Theme[] pool = k == 0 ? GROUND : UPPER;
            Room room = new Room(box, in, floorY, pool[CityHash.range(h, pool.length)], h, CityHash.range(h >>> 20, 2) == 0);
            featureWall(c, room);
            tables(c, room);
            lights(c, room);
        }
    }

    /** One storey being furnished. {@code west} picks which side wall carries the furniture run. */
    private record Room(Rect box, Rect in, int floorY, Theme theme, long hash, boolean west) {
        boolean reserved(int x, int z) {
            return z <= in.z0() + 1 || x == box.centerX() || z == in.centerZ();
        }

        int wallX() { return west ? in.x0() : in.x1(); }

        Direction into() { return west ? Direction.EAST : Direction.WEST; }
    }

    // ---------------------------------------------------------------- the feature wall

    private static void featureWall(Canvas c, Room r) {
        Piece[] run = run(r.theme());
        int x = r.wallX();
        Direction into = r.into();
        int k = 0;
        for (int z = r.in().z0() + 2; z <= r.in().z1() && k < 2 * run.length; z++) {
            if (r.reserved(x, z)) continue;
            Piece piece = run[k % run.length];
            k++;
            if (!indoorAir(c, x, r.floorY(), z)) continue;
            boolean solidWall = solidWall(c, x - into.getStepX(), r.floorY() + 2, z - into.getStepZ());
            place(c, r, piece, x, z, into, solidWall);
        }
    }

    private static void place(Canvas c, Room r, Piece piece, int x, int z, Direction into, boolean solidWall) {
        int y = r.floorY() + 1;
        long h = CityHash.of(r.hash(), 0x9E3L, x, z);
        switch (piece) {
            case COUNTER -> {
                Block body = switch (r.theme()) {
                    case DINER -> Blocks.DARK_OAK_PLANKS;
                    case WORKSHOP -> Blocks.WAXED_WEATHERED_CUT_COPPER;
                    case CLINIC -> Blocks.POLISHED_DIORITE;
                    case SMOKE_DEN -> Blocks.POLISHED_BLACKSTONE;
                    default -> Blocks.SPRUCE_PLANKS;
                };
                put(c, x, y, z, body.defaultBlockState());
                // Something on most counters, and a shelf above a few of them where the wall is blank.
                if (CityHash.range(h, 100) < 60) put(c, x, y + 1, z, trinket(r.theme(), h >>> 8));
                if (solidWall && CityHash.range(h >>> 24, 100) < 30) {
                    put(c, x, y + 2, z, Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, into)
                            .setValue(BlockStateProperties.HALF, Half.TOP));
                }
            }
            case STOVE -> put(c, x, y, z, face(Blocks.SMOKER, into));
            case OVEN -> put(c, x, y, z, face(Blocks.FURNACE, into));
            case SINK -> put(c, x, y, z, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            case BARREL -> put(c, x, y, z, barrel());
            case BARRELS -> {
                put(c, x, y, z, barrel());
                if (solidWall) put(c, x, y + 1, z, barrel());
            }
            case CHEST -> put(c, x, y, z, face(Blocks.CHEST, into));
            case SHELF -> put(c, x, y, z, Blocks.BOOKSHELF.defaultBlockState());
            case SHELVES -> {
                put(c, x, y, z, Blocks.BOOKSHELF.defaultBlockState());
                if (solidWall) put(c, x, y + 1, z, Blocks.BOOKSHELF.defaultBlockState());
            }
            // The sofa's tall back goes against the wall.
            case SOFA -> put(c, x, y, z, stair(r.theme() == Theme.SMOKE_DEN ? Blocks.DARK_OAK_STAIRS : Blocks.SPRUCE_STAIRS,
                    into.getOpposite()));
            case SIDE_TABLE -> sideTable(c, x, y, z, Blocks.SPRUCE_SLAB, h, r.theme());
            case LECTERN -> put(c, x, y, z, face(Blocks.LECTERN, into));
            case BREWING -> put(c, x, y, z, Blocks.BREWING_STAND.defaultBlockState());
            case ANVIL -> put(c, x, y, z, face(Blocks.CHIPPED_ANVIL, into.getClockWise()));
            case GRINDSTONE -> put(c, x, y, z, face(Blocks.GRINDSTONE, into)
                    .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR));
            case WARDROBE -> {
                put(c, x, y, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                if (solidWall) put(c, x, y + 1, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
            case BED -> {
                // Head against the wall, foot pointing into the room.
                int fx = x + into.getStepX();
                if (!r.reserved(fx, z) && indoorAir(c, fx, r.floorY(), z)) {
                    Block bed = r.theme() == Theme.CLINIC ? Blocks.WHITE_BED : BEDS[CityHash.range(h, BEDS.length)];
                    c.bed(fx, y, z, into.getOpposite(), bed);
                }
            }
            case NONE -> {
            }
        }
    }

    private static final Block[] BEDS = {Blocks.GRAY_BED, Blocks.BROWN_BED, Blocks.LIGHT_GRAY_BED, Blocks.RED_BED,
            Blocks.GREEN_BED};

    // ---------------------------------------------------------------- the open floor

    /** One table with chairs in the open half of a living room, den or diner; two in a big diner. */
    private static void tables(Canvas c, Room r) {
        if (r.theme() != Theme.LIVING && r.theme() != Theme.SMOKE_DEN && r.theme() != Theme.DINER) return;
        Rect in = r.in();
        if (in.width() < 9 || in.depth() < 9) return;
        // Across the room from the feature wall, halfway between the doorway aisle and the far wall.
        int tx = r.west() ? (r.box().centerX() + in.x1()) / 2 : (in.x0() + r.box().centerX()) / 2;
        int[] rows = r.theme() == Theme.DINER ? new int[] {(in.centerZ() + in.z1()) / 2, (in.z0() + 2 + in.centerZ()) / 2}
                : new int[] {(in.centerZ() + in.z1()) / 2};
        for (int tz : rows) {
            if (nearAisle(r, tx, tz) || tz <= in.z0() + 3 || tz >= in.z1()) continue;
            table(c, r, tx, tz);
        }
    }

    private static boolean nearAisle(Room r, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (r.reserved(x + dx, z + dz)) return true;
            }
        }
        return false;
    }

    private static void table(Canvas c, Room r, int x, int z) {
        int y = r.floorY() + 1;
        if (!indoorAir(c, x, r.floorY(), z)) return;
        Block top = r.theme() == Theme.SMOKE_DEN ? Blocks.POLISHED_BLACKSTONE_SLAB : Blocks.DARK_OAK_SLAB;
        sideTable(c, x, y, z, top, CityHash.of(r.hash(), 0x7AB1EL, x, z), r.theme());
        Block chair = r.theme() == Theme.SMOKE_DEN ? Blocks.DARK_OAK_STAIRS : Blocks.SPRUCE_STAIRS;
        // Chairs on the two long sides only, backs away from the table.
        for (Direction d : new Direction[] {Direction.NORTH, Direction.SOUTH}) {
            int cx = x + d.getStepX(), cz = z + d.getStepZ();
            if (!r.reserved(cx, cz)) put(c, cx, y, cz, stair(chair, d));
        }
        if (r.theme() != Theme.DINER) {
            Block carpet = r.theme() == Theme.SMOKE_DEN ? Blocks.PURPLE_CARPET : Blocks.BROWN_CARPET;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int rx = x + dx, rz = z + dz;
                    if (!r.reserved(rx, rz) && indoorAir(c, rx, r.floorY(), rz)) put(c, rx, y, rz, carpet.defaultBlockState());
                }
            }
        }
    }

    private static void lights(Canvas c, Room r) {
        Rect in = r.in();
        int y = r.floorY() + 3;
        BlockState lamp = switch (r.theme()) {
            case SMOKE_DEN -> Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
            case CLINIC, WORKSHOP -> ModBlocks.CAVERN_LAMP.get().defaultBlockState().setValue(LanternBlock.HANGING, true);
            default -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        };
        // One fixture per roughly 6x6 bay, hung from the ceiling over the middle of the room.
        for (int x = in.x0() + 3; x <= in.x1() - 2; x += 6) {
            for (int z = in.z0() + 4; z <= in.z1() - 1; z += 6) {
                if (!c.contains(x, z) || !c.get(x, y + 1, z).isSolid()) continue;
                put(c, x, y, z, lamp);
            }
        }
    }

    // ---------------------------------------------------------------- pieces

    private static void sideTable(Canvas c, int x, int y, int z, Block slab, long h, Theme theme) {
        put(c, x, y, z, slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        if (CityHash.range(h >>> 12, 100) < 70) put(c, x, y + 1, z, trinket(theme, h >>> 16));
    }

    /** Small things that sit on counters, tables and shelves. */
    private static BlockState trinket(Theme theme, long h) {
        int roll = CityHash.range(h, 6);
        if (theme == Theme.SMOKE_DEN) {
            return switch (roll) {
                case 0, 1 -> candle(Blocks.PURPLE_CANDLE, h, true);
                case 2 -> candle(Blocks.BLACK_CANDLE, h, true);
                case 3 -> Blocks.POTTED_CRIMSON_FUNGUS.defaultBlockState();
                default -> Blocks.POTTED_WARPED_FUNGUS.defaultBlockState();
            };
        }
        return switch (roll) {
            case 0 -> Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState();
            case 1 -> Blocks.POTTED_FERN.defaultBlockState();
            case 2 -> candle(Blocks.WHITE_CANDLE, h, false);
            case 3 -> Blocks.LANTERN.defaultBlockState();
            case 4 -> Blocks.POTTED_RED_MUSHROOM.defaultBlockState();
            default -> Blocks.FLOWER_POT.defaultBlockState();
        };
    }

    private static BlockState candle(Block candle, long h, boolean lit) {
        return candle.defaultBlockState().setValue(CandleBlock.CANDLES, 1 + CityHash.range(h >>> 6, 3))
                .setValue(CandleBlock.LIT, lit);
    }

    private static BlockState barrel() {
        return Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP);
    }

    private static BlockState face(Block block, Direction facing) {
        return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    private static BlockState stair(Block stairs, Direction facing) {
        return stairs.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    // ---------------------------------------------------------------- checks

    /** Only fills empty space, so earlier passes' furniture is never overwritten. */
    private static void put(Canvas c, int x, int y, int z, BlockState state) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()) c.set(x, y, z, state);
    }

    /** A cell with floor under it, ceiling over it and nothing in it: inside the building, not a cut-out court. */
    private static boolean indoorAir(Canvas c, int x, int floorY, int z) {
        if (!c.contains(x, z)) return false;
        return c.get(x, floorY, z).isSolid() && c.get(x, floorY + Tower.STOREY, z).isSolid()
                && c.get(x, floorY + 1, z).isAir() && c.get(x, floorY + 2, z).isAir();
    }

    /** True when the wall behind a cell is solid at window height, so tall furniture won't cover a window. */
    private static boolean solidWall(Canvas c, int x, int y, int z) {
        if (!c.contains(x, z)) return false;
        BlockState s = c.get(x, y, z);
        return s.isSolid() && s.canOcclude();
    }
}
