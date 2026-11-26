package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 39: occupancy cues for PURPOSE-BUILT rooms. Builds on the existing
 * partition/door/furnishing planner; does not add another layer of partitions.
 *
 * Assumptions verified by the source layout: the stair/corridor are at the
 * north (small Z), the building's street door is south center, and existing
 * workstations and beds are always more important than decor. Only a few
 * secondary floor and surface props are placed, and only on air cells with
 * support below. There are no new lamps, loot containers or inhabitants.
 *
 * This helper never searches neighboring chunks and never uses a mutable RNG.
 * Every cell decision is independent of chunk painting order.
 */
final class LivedInRooms39 {
    private LivedInRooms39() {}

    /** Reserves the north stair/corridor and the south entrance approach. */
    private static boolean circulationFree(Rect in, int x, int z) {
        if (!in.contains(x, z)) return false;
        if (z <= in.z0() + 3) return false;
        if (z >= in.z1() - 2 && Math.abs(x - in.centerX()) <= 2) return false;
        return true;
    }

    private static void floor(Canvas c, Rect in, int x, int y, int z, Block block) {
        if (!circulationFree(in, x, z) || !c.contains(x, z)) return;
        if (!c.get(x, y, z).isAir()) return;
        if (c.get(x, y - 1, z).isAir()) return;
        c.set(x, y, z, block);
    }

    /** An actual occupied cabinet can have a utensil, plant or notice on it. */
    private static void objectOnFixture(Canvas c, Rect in, int x, int y, int z, Block object) {
        if (!circulationFree(in, x, z) || !c.contains(x, z)) return;
        Block below = c.get(x, y - 1, z).getBlock();
        if (below != Blocks.BOOKSHELF && below != Blocks.CRAFTING_TABLE
                && below != Blocks.SMITHING_TABLE && below != Blocks.SPRUCE_PLANKS
                && below != Blocks.SPRUCE_SLAB && below != Blocks.BARREL) return;
        if (c.get(x, y, z).isAir()) c.set(x, y, z, object);
    }

    static void residence(Canvas c, Rect in, int baseY, int storey, long building, boolean compact) {
        if (in.width() < 9 || in.depth() < 11) return;
        long h = CityHash.of(building ^ 0x39B11D10L, in.centerX(), in.centerZ(), storey);
        int mood = CityHash.range(h, 4);
        int floorY = baseY + 1;
        int west = in.x0() + 1, east = in.x1() - 1;
        int front = in.z1() - 3, rear = in.z0() + 5;
        // Households rather than generic cells: a worn sitting area, a book,
        // mended clothes, or a water basin. A single-floor shopkeeper's home
        // differs from the lobby of an upper-floor tenement.
        if (storey == 0 && !compact) {
            floor(c, in, west, floorY, front, mood % 2 == 0 ? Blocks.SPRUCE_STAIRS : Blocks.DARK_OAK_STAIRS);
            floor(c, in, east, floorY, front, Blocks.BROWN_CARPET);
            objectOnFixture(c, in, east, floorY + 1, rear, Blocks.FLOWER_POT);
            return;
        }
        // Flat / studio: old worldgen provides the beds and cooktops; these
        // additional lived-in clues remain near existing perimeter furniture.
        floor(c, in, west, floorY, front,
                mood == 0 ? Blocks.BROWN_CARPET : mood == 1 ? Blocks.GRAY_CARPET
                        : mood == 2 ? Blocks.GREEN_CARPET : Blocks.LIGHT_GRAY_CARPET);
        floor(c, in, east, floorY, rear,
                mood % 2 == 0 ? Blocks.SPRUCE_SLAB : Blocks.DARK_OAK_SLAB);
        objectOnFixture(c, in, west, floorY + 1, rear, Blocks.FLOWER_POT);
        if (in.depth() >= 14) {
            floor(c, in, west, floorY, in.z1() - 5,
                    mood == 1 ? Blocks.LOOM : mood == 3 ? Blocks.CARTOGRAPHY_TABLE
                            : Blocks.OAK_PRESSURE_PLATE);
        }
    }

    static void shop(Canvas c, Rect in, int baseY, long building) {
        if (in.width() < 12 || in.depth() < 13) return;
        int trade = InteriorPlan15.shop(c.seed(), in.centerX(), in.centerZ());
        int y = baseY + 1;
        int x = in.x1() - 1;
        int back = in.z0() + 5;
        // Distinct service areas: groceries/food, repairs, and books have
        // different recognizable furniture. Front approach remains clear.
        Block station = switch (trade) {
            case 0 -> Blocks.COMPOSTER;
            case 1 -> Blocks.GRINDSTONE;
            case 2 -> Blocks.SMOKER;
            default -> Blocks.LECTERN;
        };
        floor(c, in, x, y, back, station);
        floor(c, in, in.x0() + 1, y, in.z1() - 3,
                trade == 2 ? Blocks.BROWN_CARPET : Blocks.SPRUCE_SLAB);
        objectOnFixture(c, in, in.x0() + 1, y + 1, back,
                trade == 0 ? Blocks.FLOWER_POT : Blocks.POTTED_DEAD_BUSH);
    }

    static void clinic(Canvas c, Rect in, int baseY, int storey) {
        if (in.width() < 12 || in.depth() < 12) return;
        int y = baseY + 1;
        // Patient ward and triage must stay usable. Carpets imply washable
        // paths, cauldron/disinfection and records use vanilla workstations.
        floor(c, in, in.x0() + 1, y, in.z1() - 3, Blocks.LIGHT_GRAY_CARPET);
        floor(c, in, in.x1() - 1, y, in.z0() + 5,
                storey == 0 ? Blocks.CAULDRON : Blocks.BREWING_STAND);
        objectOnFixture(c, in, in.x0() + 1, y + 1, in.z0() + 5, Blocks.FLOWER_POT);
    }

    static void industrial(Canvas c, Rect in, int floorY, boolean factory, long building) {
        if (in.width() < 16 || in.depth() < 14) return;
        long h = CityHash.of(building ^ 0x39B11D13L, in.centerX(), in.centerZ());
        // A cart-wide central aisle and side service doors remain unoccupied.
        int z = in.z0() + 5 + CityHash.range(h, Math.max(1, in.depth() - 11));
        floor(c, in, in.x0() + 1, floorY, z,
                factory ? Blocks.STONECUTTER : Blocks.CRAFTING_TABLE);
        floor(c, in, in.x1() - 1, floorY, z,
                factory ? Blocks.SMITHING_TABLE : Blocks.COMPOSTER);
        // No decorative street chests, lamps or rows of exterior barrels.
    }
}
