package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 37: tiny functional vignettes rather than random decorative spam.
 * Every addition is keyed to a room's actual use (food prep, repair,
 * domestic living, shop service or clinical treatment). Existing partitions,
 * stairs, doors, loot and light placement have priority.
 *
 * No mobs, villagers, inventories, block entities or new lights are created.
 */
final class RoomPurpose37 {
    private RoomPurpose37() {}

    private static boolean safe(Rect in, int x, int z) {
        if (!in.contains(x, z)) return false;
        // Keep circulation along the original north stair flight, and the
        // front-facing central entrance to the floor, unobstructed.
        if (z <= in.z0() + 2 && x <= in.x0() + 5) return false;
        return z < in.z1() - 2 || Math.abs(x - in.centerX()) > 1;
    }

    private static void piece(Canvas c, Rect in, int x, int y, int z, Block b) {
        if (!safe(in, x, z) || !c.contains(x, z)) return;
        if (!c.get(x, y, z).isAir() || c.get(x, y - 1, z).isAir()) return;
        c.set(x, y, z, b);
    }

    static void residential(Canvas c, Rect in, int y, int floor, long house, boolean compact) {
        if (in.width() < 8 || in.depth() < 9) return;
        int kind = CityHash.range(CityHash.of(house ^ 0x37A7732L, in.centerX(), in.centerZ(), floor), 4);
        if (floor == 0 || compact) {
            // A small domestic cooking / washing nook, not a second storage
            // room filled with random chests.
            piece(c, in, in.x1() - 1, y + 1, in.z0() + 5,
                    kind % 2 == 0 ? Blocks.SMOKER : Blocks.FURNACE);
            piece(c, in, in.x1() - 1, y + 1, in.z0() + 6,
                    kind == 3 ? Blocks.COMPOSTER : Blocks.CAULDRON);
            piece(c, in, in.x0() + 1, y + 1, in.z1() - 3,
                    kind == 0 ? Blocks.BOOKSHELF : Blocks.SPRUCE_SLAB);
        } else {
            // Occupied upstairs units have personal possessions, repairs or
            // a sitting nook, rather than a generic row of crates.
            piece(c, in, in.x1() - 1, y + 1, in.z1() - 4,
                    switch (kind) {
                        case 0 -> Blocks.LOOM;
                        case 1 -> Blocks.BOOKSHELF;
                        case 2 -> Blocks.CARTOGRAPHY_TABLE;
                        default -> Blocks.SPRUCE_SLAB;
                    });
            piece(c, in, in.x0() + 1, y + 1, in.z1() - 3,
                    kind % 2 == 0 ? Blocks.FLOWER_POT : Blocks.BROWN_CARPET);
        }
    }

    static void shop(Canvas c, Rect in, int y, long shop) {
        if (in.width() < 12 || in.depth() < 12) return;
        int trade = CityHash.range(CityHash.of(shop, 0x37A7733L), 4);
        // Client-facing counters stay by the outside walls; the existing rear
        // service-room doorway is not filled in.
        piece(c, in, in.x0() + 1, y + 1, in.z0() + 5,
                trade == 0 ? Blocks.BOOKSHELF : trade == 1 ? Blocks.LOOM
                        : trade == 2 ? Blocks.SMOKER : Blocks.CARTOGRAPHY_TABLE);
        piece(c, in, in.x1() - 1, y + 1, in.z0() + 5, Blocks.SPRUCE_SLAB);
    }

    static void clinic(Canvas c, Rect in, int y, int floor) {
        if (in.width() < 12 || in.depth() < 12) return;
        // Triage and medical preparation, not extra loot containers.
        piece(c, in, in.x0() + 1, y + 1, in.z0() + 5,
                floor == 0 ? Blocks.LECTERN : Blocks.CAULDRON);
        piece(c, in, in.x1() - 1, y + 1, in.z0() + 5,
                floor == 0 ? Blocks.CAULDRON : Blocks.BREWING_STAND);
    }

    static void workshop(Canvas c, Rect in, int y, boolean factory) {
        if (in.width() < 16 || in.depth() < 14) return;
        // Keep the central warehouse cart aisle clear.
        piece(c, in, in.x1() - 1, y, in.z0() + 6,
                factory ? Blocks.GRINDSTONE : Blocks.FLETCHING_TABLE);
        piece(c, in, in.x0() + 1, y, in.z1() - 4,
                factory ? Blocks.SMITHING_TABLE : Blocks.CRAFTING_TABLE);
    }
}
