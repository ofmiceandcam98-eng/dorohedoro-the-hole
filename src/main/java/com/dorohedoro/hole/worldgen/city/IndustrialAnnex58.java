package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 58: some substantial industrial halls lose the eastern strip of their
 * full-height shell and use the space for a low attached, internally connected
 * two-room machine/dispatch bay. This is real multivolume geometry rather than
 * facade detailing. The other end of the former east strip remains an open
 * loading setback. Writes are parcel-contained and chunk-clipped by Canvas.
 * No added lamps or exposed reward chests; works across all Hole climates.
 */
final class IndustrialAnnex58 {
    private IndustrialAnnex58() {}

    /** Exact legacy rectangle returned without a new-world v41 setting. */
    static Rect mainFootprint(Canvas c, Lot lot, Rect original) {
        if (!c.hasIndustrialAnnex58() || !eligible(lot, original)) return original;
        return new Rect(original.x0(), original.z0(), original.x1() - 8, original.z1());
    }

    private static boolean eligible(Lot lot, Rect original) {
        return (lot.type() == LotType.WAREHOUSE || lot.type() == LotType.FACTORY)
                && original.width() >= 37 && original.depth() >= 23
                && CityHash.range(CityHash.mix(lot.hash() ^ 0x1AD58A11L), 3) != 0;
    }

    /** Shared west wall is already the eastern wall of the main factory. */
    static Rect wing(Rect original, Rect main) {
        if (main.x1() == original.x1() || main.x1() + 8 != original.x1()
                || main.z0() + 18 > main.z1()) return null;
        return new Rect(main.x1(), main.z0() + 3, original.x1(), main.z0() + 16);
    }

    static void build(Canvas c, Lot lot, Rect original, Rect main, boolean factory) {
        if (!c.hasIndustrialAnnex58() || !eligible(lot, original)) return;
        Rect bay = wing(original, main);
        if (bay == null || !lot.rect().contains(bay.x0(), bay.z0())
                || !lot.rect().contains(bay.x1(), bay.z1())) return;

        // Lower roof = visibly connected and useful, but never an additional
        // empty tower. The south part of the old hall's strip stays open.
        int roofY = GROUND_Y + 6;
        c.fill(bay, STREET_Y, STREET_Y, Blocks.POLISHED_ANDESITE);
        c.walls(bay, GROUND_Y, roofY - 1,
                Mix.of(Blocks.BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 2));
        c.fill(bay, roofY, roofY, Blocks.WAXED_WEATHERED_CUT_COPPER);
        c.clear(bay.inset(1), GROUND_Y, roofY - 1);

        // Two distinctly usable rooms, joined by an ordinary working door.
        int divider = bay.z0() + 7;
        c.fill(bay.x0() + 1, GROUND_Y, divider,
                bay.x1() - 1, roofY - 1, divider, Blocks.MUD_BRICKS);
        int partitionX = bay.x0() + 4;
        c.door(partitionX, GROUND_Y, divider, Direction.SOUTH, Blocks.SPRUCE_DOOR);

        // Old factory east wall is shared, not doubled. Carve one internal
        // entrance and a corridor back to the existing central cargo aisle.
        int entryZ = bay.z0() + 3;
        c.clear(new Rect(main.centerX(), entryZ, main.x1() - 1, entryZ),
                GROUND_Y, GROUND_Y + 2);
        c.clear(new Rect(bay.x0() + 1, entryZ, bay.x0() + 2, entryZ),
                GROUND_Y, GROUND_Y + 2);
        c.door(main.x1(), GROUND_Y, entryZ, Direction.EAST, Blocks.SPRUCE_DOOR);

        // Dispatch or machine equipment is against outside walls so the
        // two-room path stays open. No loot chests placed in public yards.
        c.set(bay.x1() - 2, GROUND_Y, bay.z0() + 2,
                factory ? Blocks.SMITHING_TABLE : Blocks.CRAFTING_TABLE);
        c.set(bay.x1() - 2, GROUND_Y, bay.z0() + 4,
                factory ? Blocks.STONECUTTER : Blocks.COMPOSTER);
        c.set(bay.x1() - 2, GROUND_Y, bay.z1() - 3,
                factory ? Blocks.BLAST_FURNACE : Blocks.LOOM);
        c.barrel(bay.x0() + 2, GROUND_Y, bay.z1() - 2, "warehouse");
        c.pane(bay.x1(), GROUND_Y + 3, bay.z0() + 5,
                Direction.Axis.Z, Blocks.IRON_BARS);
        c.pane(bay.x1(), GROUND_Y + 3, bay.z1() - 3,
                Direction.Axis.Z, Blocks.IRON_BARS);

        // Useful reachable maintenance roof with edge railings. Ladder is
        // installed last so the roof cannot seal its exit.
        for (int x = bay.x0() + 1; x <= bay.x1(); x++) {
            c.set(x, roofY + 1, bay.z0(), Blocks.IRON_BARS);
            c.set(x, roofY + 1, bay.z1(), Blocks.IRON_BARS);
        }
        for (int z = bay.z0() + 1; z < bay.z1(); z++)
            c.set(bay.x1(), roofY + 1, z, Blocks.IRON_BARS);
        c.set(bay.x1() - 2, roofY + 1, bay.z0() + 2, Blocks.CAULDRON);
        int ladderX = bay.x1() - 1;
        int ladderZ = bay.z1() - 2;
        c.ladder(ladderX, GROUND_Y, roofY + 1, ladderZ, Direction.WEST);
    }
}
