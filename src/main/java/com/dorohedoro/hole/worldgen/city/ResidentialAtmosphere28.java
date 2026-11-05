package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Version-16 residential detail pass. This is a deterministic grammar, not a
 * fixed house template. Earlier world profiles never enter this class.
 *
 * Fixtures are deliberately mixed: our Cavern Lantern is the signature, but
 * normal lanterns and well-supported wall torches remain part of The Hole.
 */
final class ResidentialAtmosphere28 {
    private ResidentialAtmosphere28() {}

    static BlockState lightState(long hash) {
        Block type = CityHash.range(hash ^ 0x28C0FFEE51L, 10) < 6
                ? ModBlocks.CAVERN_LAMP.get() : Blocks.LANTERN;
        return type.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    /** Use only genuine air directly under a ceiling, never overwrite shelves. */
    static void hang(Canvas c, int x, int floorY, int z, long h) {
        int y = floorY + Tower.STOREY - 1;
        if (!c.contains(x, z) || !c.get(x, y, z).isAir() || c.get(x, y + 1, z).isAir()) return;
        c.set(x, y, z, lightState(h));
    }

    /** A wall torch has a solid back wall, not an interior air block or window. */
    static void wallTorch(Canvas c, int x, int y, int z, Direction outward) {
        int backX = x - outward.getStepX(), backZ = z - outward.getStepZ();
        // Never depend on a neighboring chunk's ungenerated wall state.
        // The canonical 16-block chunk seam is skipped in every generation order.
        if (Math.floorDiv(x, 16) != Math.floorDiv(backX, 16)
                || Math.floorDiv(z, 16) != Math.floorDiv(backZ, 16)) return;
        if (!c.contains(x,z) || !c.contains(backX,backZ)) return;
        if (!c.get(x,y,z).isAir() || c.get(backX,y,backZ).isAir()) return;
        // Avoid mounting on glass panes or open windows; the attachment face
        // must be an ordinary full building surface.
        Block back = c.get(backX,y,backZ).getBlock();
        if (back == Blocks.GLASS_PANE || back == Blocks.IRON_BARS
                || back == Blocks.YELLOW_STAINED_GLASS_PANE) return;
        c.set(x,y,z,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,outward));
    }

    /**
     * Compact homes feel inhabited. Fill only spare perimeter cells, after
     * the older room furnishers. Keep the central aisle, stairs and doors free.
     */
    static void residence(Canvas c, Rect room, int y, int storey, long house) {
        if (room.width() < 8 || room.depth() < 9) return;
        long h = CityHash.of(house ^ 0x28A702L, room.centerX(), room.centerZ(), storey);
        int type = CityHash.range(h, 6);
        int west = room.x0() + 1, east = room.x1() - 1;
        int rear = room.z0() + 4, front = room.z1() - 2;
        if (rear >= front) return;

        // Occupations and household routines, not randomized clutter dumped
        // in the doorway: books/desk, repair corner, food storage, washstand.
        switch (type) {
            case 0 -> { put(c, west, y+1, front, Blocks.LECTERN);
                        put(c, west, y+1, front-1, Blocks.BOOKSHELF); }
            case 1 -> { put(c, east, y+1, rear, Blocks.STONECUTTER);
                        put(c, east, y+1, rear+1, Blocks.BARREL); }
            case 2 -> { put(c, west, y+1, front, Blocks.SPRUCE_SLAB);
                        put(c, west, y+2, front, Blocks.FLOWER_POT); }
            case 3 -> { put(c, east, y+1, front, Blocks.CAULDRON);
                        put(c, east, y+1, front-1, Blocks.BARREL); }
            case 4 -> { put(c, west, y+1, rear, Blocks.BOOKSHELF);
                        put(c, east, y+1, front, Blocks.SMOKER); }
            default -> { put(c, west, y+1, front, Blocks.LOOM);
                         put(c, east, y+1, rear, Blocks.CRAFTING_TABLE); }
        }
        // Household soft furnishings are sparse and deliberately away from
        // the north staircase. A single rug variation is enough for small rooms.
        if (room.width() >= 10 && room.depth() >= 11 && CityHash.range(h >>> 8, 3) != 0) {
            put(c, room.centerX() + 1, y+1, room.centerZ() + 1,
                    CityHash.range(h >>> 16, 2)==0 ? Blocks.BROWN_CARPET : Blocks.GRAY_CARPET);
        }
    }

    /** Small supplementary trim above windows, never at the entrance height. */
    static void eaves(Canvas c, Rect b, int floors, long house, District district) {
        if (b.width() < 10) return;
        long h = CityHash.of(house, b.centerX(), b.centerZ());
        Block trim = switch (district) {
            case RESIDENTIAL -> Blocks.MUD_BRICK_SLAB;
            case WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case MARKET -> Blocks.DARK_OAK_SLAB;
            case INDUSTRIAL -> Blocks.POLISHED_DEEPSLATE_SLAB;
            case DERELICT, GRAVEYARD -> Blocks.STONE_BRICK_SLAB;
            case OUTSKIRTS -> Blocks.SPRUCE_SLAB;
        };
        // The facade's front has a one-block setback, and previous decoration
        // is never overwritten. Each building gets a different lintel pattern.
        for (int floor=0; floor<floors; floor++) {
            int base = CityPlan.STREET_Y + floor*Tower.STOREY;
            for (int x=b.x0()+2; x<=b.x1()-2; x+=3) {
                if (CityHash.range(CityHash.of(h,x,floor), 4) == 0) continue;
                int z=b.z1()+1;
                // Avoid cross-chunk lookups; the wall underneath is guaranteed
                // by the Tower skeleton for this facade coordinate.
                if (c.contains(x,z) && c.get(x,base+4,z).isAir()) c.set(x,base+4,z,trim);
            }
        }
    }

    private static void put(Canvas c, int x, int y, int z, Block b) {
        if (!c.contains(x,z) || !c.get(x,y,z).isAir() || c.get(x,y-1,z).isAir()) return;
        c.set(x,y,z,b);
    }
}
