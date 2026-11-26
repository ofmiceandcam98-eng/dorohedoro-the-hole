package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.core.Direction;
import com.dorohedoro.hole.registry.ModBlocks;

/**
 * Seeded v15 residential refinement. Adds optional wall-hugging furnishings and
 * practical secondary lights without closing the north stair or south approach.
 * Every decision derives from house hash, storey and world coordinates.
 */
final class ResidentialDetails26 {
    private ResidentialDetails26() {}

    static void compact(Canvas c, Rect r, int y, int storey, int floors, long house) {
        if (r.width() < 7 || r.depth() < 7) return;
        long h = CityHash.of(house ^ 0x2600A1L, storey, r.centerX(), r.centerZ());
        int v = CityHash.range(h, 6);
        // Walkway remains open along center x and along the north stair.
        int west = r.x0() + 1, east = r.x1() - 1;
        int rear = r.z0() + 3, front = r.z1() - 2;
        if (rear >= front) return;
        // Functional kitchenette, wall cabinet or washstand, never free-floating.
        put(c, east, y+1, rear, switch(v % 3) {
            case 0 -> Blocks.CRAFTING_TABLE;
            case 1 -> Blocks.SMOKER;
            default -> Blocks.CAULDRON;
        });
        if (r.depth() >= 9) put(c, east, y+1, rear+2, v % 2 == 0 ? Blocks.BARREL : Blocks.BOOKSHELF);
        // Day-to-day clutter stays on the walls, different per flat.
        if (r.width() >= 9) {
            put(c, west, y+1, front, v % 3 == 0 ? Blocks.LOOM : Blocks.SPRUCE_SLAB);
            if (v % 2 == 1) put(c, west, y+2, front, Blocks.FLOWER_POT);
        }
        if (r.depth() >= 10) {
            put(c, east, y+1, front, v % 2 == 0 ? Blocks.GRAY_CARPET : Blocks.BROWN_CARPET);
        }
        // A second light only when there is a real ceiling above the fixture.
        if (r.width() >= 10 && r.depth() >= 9) light(c, east-1, y, front-1);
        // Older compact housing gets a supplementary wall torch by the side wall,
        // keeping beds, doorways and the central walking aisle clear.
        if (c.hasAtmosphere28()) {
            // Pass31 fixes the v16/v17 wall-coordinate mistake in NEW worlds.
            // Keep v16/v17 output unchanged for existing saves and chunk seams.
            if (c.hasHousingLighting31()) {
                // Torches occupy the AIR cell immediately inside the room;
                // the boundary wall behind each one is the actual support.
                if ((v & 1) == 0) ResidentialAtmosphere28.wallTorch(c, west, y + 2, rear + 1, Direction.EAST);
                else if (r.width() >= 9) ResidentialAtmosphere28.wallTorch(c, east, y + 2, rear + 1, Direction.WEST);
            } else {
                if ((v & 1) == 0) ResidentialAtmosphere28.wallTorch(c, r.x0(), y + 1, rear + 1, Direction.EAST);
                else if (r.width() >= 9) ResidentialAtmosphere28.wallTorch(c, r.x1(), y + 1, rear + 1, Direction.WEST);
            }
        } else {
            if ((v & 1) == 0) wallTorch(c, west, y + 2, rear + 1, Direction.EAST);
            else if (r.width() >= 9) wallTorch(c, east, y + 2, rear + 1, Direction.WEST);
        }
    }

    static void large(Canvas c, Rect r, int y, int storey, long house) {
        if (r.width() < 12 || r.depth() < 11) return;
        long h = CityHash.of(house ^ 0x2600B2L, storey, r.centerX(), r.centerZ());
        int v = CityHash.range(h, 5);
        // Rear side-wall wash station, dresser and small display shelving.
        int east = r.x1()-1, west = r.x0()+1;
        put(c, east, y+1, r.z1()-3, v == 0 ? Blocks.CAULDRON : Blocks.BARREL);
        put(c, west, y+1, r.z1()-4, v % 2 == 0 ? Blocks.BOOKSHELF : Blocks.SPRUCE_SLAB);
        if (r.depth() >= 14) put(c, east, y+1, r.z1()-6, Blocks.FLOWER_POT);
        // Shared stair corridor and both apartment doors must stay unobstructed.
        light(c, east-1, y, r.z1()-2);
        if ((v & 1) == 0) {
            if (c.hasAtmosphere28()) {
                if (c.hasHousingLighting31()) ResidentialAtmosphere28.wallTorch(c, west, y + 2, r.z0() + 4, Direction.EAST);
                else ResidentialAtmosphere28.wallTorch(c, r.x0(), y + 1, r.z0() + 4, Direction.EAST);
            } else wallTorch(c, west, y + 2, r.z0() + 4, Direction.EAST);
        }
    }

    private static void put(Canvas c, int x, int y, int z, Block b) {
        if (c.contains(x,z) && c.get(x,y,z).isAir() && !c.get(x,y-1,z).isAir()) c.set(x,y,z,b);
    }
    private static void light(Canvas c, int x, int y, int z) {
        if (c.contains(x,z) && c.get(x,y+3,z).isAir() && !c.get(x,y+4,z).isAir()) {
            if (c.hasAtmosphere28()) ResidentialAtmosphere28.hang(c, x, y, z, c.hash(x, y, z));
            else c.set(x,y+3,z,ModBlocks.CAVERN_LAMP.get().defaultBlockState().setValue(LanternBlock.HANGING,true));
        }
    }
    private static void wallTorch(Canvas c, int x, int y, int z, Direction outward) {
        // Valid torch only with a solid room-side backing wall. No floating or
        // unsupported fixtures, and never overwrite a prior furnishing.
        int supportX = x - outward.getStepX();
        int supportZ = z - outward.getStepZ();
        if (c.contains(x,z) && c.contains(supportX,supportZ)
                && c.get(x,y,z).isAir() && !c.get(supportX,y,supportZ).isAir()) {
            c.set(x,y,z,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,outward));
        }
    }

}
