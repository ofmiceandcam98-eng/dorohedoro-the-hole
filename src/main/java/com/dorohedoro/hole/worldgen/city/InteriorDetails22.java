package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 22: small-scale lived-in furnishing for new city layouts only.
 * No new entity systems, furniture registries or custom block entities. Positions derive
 * solely from room bounds and are clipped to the target chunk by Canvas.
 * Never overwrite the structural stair, existing beds, stored loot or a room partition.
 */
final class InteriorDetails22 {
    private InteriorDetails22() {}

    /** Set only genuinely empty cells: preserves every existing placed furniture item. */
    private static void empty(Canvas c, int x, int y, int z, Block item) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()
                && !c.get(x, y - 1, z).isAir()) c.set(x, y, z, item);
    }

    static void lobby(Canvas c, Rect r, int y, int kind) {
        if (!c.hasExpandedFurniture() || r.width() < 10 || r.depth() < 9) return;
        // Mail and notice corner, dirty wash station and a tiny waiting area at opposite walls.
        empty(c, r.x1(), y + 2, r.z0() + 2, Blocks.BOOKSHELF);
        empty(c, r.x1(), y + 2, r.z0() + 3, Blocks.BOOKSHELF);
        empty(c, r.x0(), y + 2, r.z1() - 3, Blocks.FLOWER_POT);
        empty(c, r.x1() - 2, y + 1, r.z1() - 2, Blocks.DARK_OAK_STAIRS);
        empty(c, r.x1() - 2, y + 1, r.z1() - 3, Blocks.BROWN_CARPET);
        if (kind % 2 == 0) empty(c, r.x0() + 1, y + 1, r.z1() - 3, Blocks.LECTERN);
    }

    static void residence(Canvas c, Rect r, int y, int kind) {
        if (!c.hasExpandedFurniture() || r.width() < 10 || r.depth() < 9) return;
        // Hallway furniture hugs perimeter so the northern stair and doorway stay walkable.
        empty(c, r.x1(), y + 2, r.z1() - 2, Blocks.BOOKSHELF);
        empty(c, r.x0(), y + 2, r.z1() - 2, Blocks.BOOKSHELF);
        empty(c, r.x1(), y + 1, r.z1() - 3, Blocks.FLOWER_POT);
        if (kind == 1) empty(c, r.x0() + 2, y + 1, r.z1() - 3, Blocks.GRAY_CARPET);
        if (kind == 2) empty(c, r.x1() - 2, y + 1, r.z1() - 2, Blocks.CRAFTING_TABLE);
    }

    static void shop(Canvas c, Rect r, int y, int kind) {
        if (!c.hasExpandedFurniture() || r.width() < 10 || r.depth() < 10) return;
        // Tall storage bays, small customer seating, shop-specific counter clutter.
        for (int z = r.z0() + 4; z <= r.z1() - 4; z += 3) {
            empty(c, r.x0(), y + 2, z, kind == 2 ? Blocks.SMOKER : Blocks.BOOKSHELF);
            empty(c, r.x1(), y + 2, z, Blocks.BARREL);
        }
        empty(c, r.x0() + 2, y + 1, r.z1() - 2, kind == 1 ? Blocks.GRINDSTONE : Blocks.CRAFTING_TABLE);
        empty(c, r.x0() + 2, y + 2, r.z1() - 2, kind == 2 ? Blocks.FLOWER_POT : Blocks.CANDLE);
        empty(c, r.x1() - 2, y + 1, r.z0() + 4, Blocks.GRAY_CARPET);
    }

    static void clinic(Canvas c, Rect r, int y, int kind) {
        if (!c.hasExpandedFurniture() || r.width() < 10 || r.depth() < 10) return;
        // Extra medical supply racks and sterilization: keep ward door and beds intact.
        for (int z = r.z0() + 5; z < r.z1() - 5; z += 4) {
            empty(c, r.x0(), y + 2, z, Blocks.WHITE_STAINED_GLASS);
            empty(c, r.x1(), y + 2, z, Blocks.BOOKSHELF);
        }
        empty(c, r.x0() + 2, y + 1, r.z0() + 3, Blocks.CAULDRON);
        empty(c, r.x0() + 2, y + 2, r.z0() + 3, Blocks.FLOWER_POT);
        empty(c, r.x1() - 2, y + 1, r.z0() + 4, kind == 2 ? Blocks.BREWING_STAND : Blocks.WHITE_CARPET);
    }

    static void industrial(Canvas c, Rect r, int y, long hash) {
        if (!c.hasExpandedFurniture() || r.width() < 14 || r.depth() < 14) return;
        for (int z = r.z0() + 3; z <= r.z1() - 4; z += 5) {
            empty(c, r.x0(), y, z, Blocks.IRON_BLOCK);
            empty(c, r.x0(), y + 1, z, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
            empty(c, r.x1(), y, z, Blocks.SPRUCE_SLAB);
        }
        empty(c, r.x1() - 1, y, r.z1() - 2, Blocks.SMITHING_TABLE);
    }
}
