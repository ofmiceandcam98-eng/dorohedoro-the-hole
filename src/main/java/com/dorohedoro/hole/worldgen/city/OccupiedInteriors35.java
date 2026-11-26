package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 35: inhabited, purpose-led interior details, gated to world_detail_version 20.
 *
 * A small residence remains a single-room studio when there is insufficient
 * floor space; larger homes are separated into kitchen/living and sleeping
 * rooms behind usable doors. Existing upper flats already have independent
 * access off their shared north corridor; deeper flats now also gain private
 * bedroom partitions when there is sufficient headroom and walkable area.
 *
 * Layout is coordinate-only and chunk clipped. No extra villagers, block
 * entities or unscripted loot are created. Furnishing never replaces a door,
 * bed, loot barrel, workbench or the stair circulation aisle.
 */
final class OccupiedInteriors35 {
    private OccupiedInteriors35() {}

    /** Places only supported furniture in existing empty cells. */
    private static void put(Canvas c, int x, int y, int z, Block b) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()
                && !c.get(x, y - 1, z).isAir()) c.set(x, y, z, b);
    }

    /**
     * Full-height interior partition with a two-high, usable door. Never intersects
     * stairs on the north side (z0) or the exterior front entrance (z1).
     */
    private static void divide(Canvas c, Rect in, int floorY, int wallZ, int doorX, Block wall) {
        if (wallZ <= in.z0() + 3 || wallZ >= in.z1() - 2) return;
        if (doorX <= in.x0() || doorX >= in.x1()) return;
        for (int x = in.x0(); x <= in.x1(); x++) {
            if (x == doorX) continue;
            for (int y = floorY + 1; y <= floorY + 3; y++) {
                // Respect previously placed interior furniture and ceiling lights.
                if (c.contains(x, wallZ) && c.get(x, y, wallZ).isAir()) c.set(x, y, wallZ, wall);
            }
        }
        c.door(doorX, floorY + 1, wallZ, Direction.NORTH, Blocks.SPRUCE_DOOR);
    }

    static void home(Canvas c, Rect in, int floorY, int storey, long house, boolean compact) {
        long h = CityHash.of(house ^ 0x35A6D000L, in.centerX(), in.centerZ(), storey);
        int variant = CityHash.range(h, 4);
        // A two-storey compact house has a lived-in kitchen and lounge below,
        // and a sleeping space with wash facilities above. Single-storey houses
        // retain a bed, so no partition is added if it would obstruct it.
        if (compact) {
            if (in.width() >= 10 && in.depth() >= 12) {
                int wallZ = in.z0() + in.depth() / 2;
                divide(c, in, floorY, wallZ, in.centerX() + 1,
                        variant % 2 == 0 ? Blocks.SPRUCE_PLANKS : Blocks.TUFF_BRICKS);
            }
            // Workstations double as believable domestic fixtures and remain
            // usable by players and by ordinary vanilla villagers.
            put(c, in.x0(), floorY + 1, in.z0() + 3,
                    storey == 0 ? Blocks.CRAFTING_TABLE : Blocks.LOOM);
            put(c, in.x1(), floorY + 1, in.z1() - 3,
                    storey == 0 ? Blocks.SMOKER : Blocks.CAULDRON);
            put(c, in.x0(), floorY + 1, in.z1() - 3,
                    variant == 0 ? Blocks.FLOWER_POT : Blocks.BOOKSHELF);
            put(c, in.x1(), floorY + 1, in.z0() + 4, Blocks.SPRUCE_SLAB);
            return;
        }
        if (storey == 0) { // shared mail, waiting and utility hall
            put(c, in.x1() - 1, floorY + 1, in.z1() - 2, Blocks.LECTERN);
            put(c, in.x0() + 1, floorY + 1, in.z1() - 2, Blocks.SPRUCE_STAIRS);
            put(c, in.x0() + 1, floorY + 1, in.z1() - 4, Blocks.BROWN_CARPET);
            return;
        }
        int layoutKind = InteriorPlan15.residence(c.seed(), in.centerX(), in.centerZ(), storey);
        if ((layoutKind == 0 || layoutKind == 3) && in.width() >= 16 && in.depth() >= 14) {
            // Two already separated flats: add a kitchen / sleeping divide to
            // each sufficiently wide half. Shared corridor is at z0..z0+3.
            int split = in.centerX();
            privateFlat(c, new Rect(in.x0() + 1, in.z0() + 4, split - 1, in.z1() - 1), floorY, variant);
            privateFlat(c, new Rect(split + 1, in.z0() + 4, in.x1() - 1, in.z1() - 1), floorY, variant + 1);
        }
        // Variety across rented rooms: textiles, communal meal preparation,
        // and household repairs. No random crate or chest spam.
        put(c, in.x1() - 1, floorY + 1, in.z1() - 4, variant == 0 ? Blocks.LOOM : Blocks.BOOKSHELF);
        put(c, in.x0() + 1, floorY + 1, in.z1() - 3, variant == 1 ? Blocks.CAULDRON : Blocks.SPRUCE_SLAB);
    }

    private static void privateFlat(Canvas c, Rect r, int y, int variant) {
        if (r.width() < 6 || r.depth() < 9) return;
        int z = r.z1() - 4;
        divide(c, r, y, z, r.centerX(), Blocks.SPRUCE_PLANKS);
        // Kitchen before the internal door; bed is behind it in the existing
        // room planner. Furniture is restricted to the two exterior corners.
        put(c, r.x0(), y + 1, r.z0() + 1,
                variant % 2 == 0 ? Blocks.SMOKER : Blocks.FURNACE);
        put(c, r.x1(), y + 1, r.z0() + 1, Blocks.CRAFTING_TABLE);
        put(c, r.x0(), y + 1, r.z1() - 2, Blocks.FLOWER_POT);
    }

    static void shop(Canvas c, Rect in, int y, long shop) {
        long h = CityHash.of(shop ^ 0x35A6D001L, in.centerX(), in.centerZ());
        int trade = CityHash.range(h, 5);
        // Sales floor stays open on the south-center approach. The original
        // v4 room planner already provides a separate rear stockroom with door.
        put(c, in.x0() + 1, y + 1, in.z0() + 4,
                switch (trade) {
                    case 0 -> Blocks.FLETCHING_TABLE;
                    case 1 -> Blocks.CARTOGRAPHY_TABLE;
                    case 2 -> Blocks.SMOKER;
                    case 3 -> Blocks.LOOM;
                    default -> Blocks.STONECUTTER;
                });
        put(c, in.x1() - 1, y + 1, in.z0() + 4, Blocks.SPRUCE_SLAB);
        put(c, in.x0() + 1, y + 1, in.z1() - 3, Blocks.BOOKSHELF);
        if (in.width() >= 14 && in.depth() >= 15) {
            // Private prep/stockroom behind the existing partition, not an
            // outdoor treasure chest. Supplies live on perimeter workstations.
            put(c, in.x1() - 2, y + 1, in.z1() - 2,
                    trade == 2 ? Blocks.BLAST_FURNACE : Blocks.CRAFTING_TABLE);
            put(c, in.x0() + 2, y + 1, in.z1() - 2, Blocks.SPRUCE_SLAB);
        }
    }

    static void clinic(Canvas c, Rect in, int y, int storey) {
        // Medical purpose: reception, disinfection and records outside the
        // screened rear treatment ward; actual vanilla brewing/cauldron/lectern
        // stations give the props a genuine survival interaction.
        put(c, in.x0() + 1, y + 1, in.z0() + 4, Blocks.LECTERN);
        put(c, in.x1() - 1, y + 1, in.z0() + 4, Blocks.BREWING_STAND);
        if (in.depth() >= 15) {
            put(c, in.x0() + 2, y + 1, in.z1() - 2, Blocks.CAULDRON);
            put(c, in.x1() - 2, y + 1, in.z1() - 2, Blocks.WHITE_CARPET);
        }
    }

    static void industrial(Canvas c, Rect in, int y, boolean factory, long site) {
        if (in.width() < 15 || in.depth() < 15) return;
        // Functional stations on side perimeter preserve the original center
        // cart lane; no new sealed rooms inside packed warehouse stacks.
        put(c, in.x1() - 1, y, in.z0() + 4,
                factory ? Blocks.SMITHING_TABLE : Blocks.CARTOGRAPHY_TABLE);
        put(c, in.x1() - 1, y, in.z0() + 5,
                factory ? Blocks.GRINDSTONE : Blocks.FLETCHING_TABLE);
        put(c, in.x0() + 1, y, in.z1() - 4,
                factory ? Blocks.CAULDRON : Blocks.CRAFTING_TABLE);
    }
}
