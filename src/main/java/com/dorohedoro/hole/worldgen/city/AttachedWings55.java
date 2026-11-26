package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 55: a minority of larger occupied parcels replace a monotonous rectangular
 * rear wall with a smaller, genuinely usable attached service wing. The room has
 * a walk-through connection to the main building; its lower roof is reachable
 * from the next storey. Every placement remains inside the original footprint,
 * so narrow streets, front doors, transit and neighboring lots are untouched.
 *
 * <p>Pure seeded geometry; the only writes are through chunk-clipped Canvas.
 */
final class AttachedWings55 {
    private static final int REAR_RECESS = 6;
    private AttachedWings55() {}

    static boolean eligible(Canvas c, Lot lot, Rect original, int mainFloors) {
        if (!c.hasAttachedServiceWings55() || mainFloors < 2) return false;
        if (lot.type() != LotType.TENEMENT && lot.type() != LotType.APARTMENT
                && lot.type() != LotType.SHOP) return false;
        if (original.width() < 17 || original.depth() < 19) return false;
        // Never attach an annex to a wall that StructuralVolumes14 may carve
        // away for an L-court or open internal court. Protect all floor counts.
        int structural = CityHash.range(CityHash.mix(lot.hash() ^ 0x14CAFE714L), 4);
        if (structural == 0 || structural == 1) return false;
        // Roughly one in six viable parcels; ordinary homes remain prevalent.
        return CityHash.range(CityHash.mix(lot.hash() ^ 0x55A771A9L), 3) == 0;
    }

    static Rect mainFootprint(Canvas c, Lot lot, Rect original, int mainFloors) {
        if (!eligible(c, lot, original, mainFloors)) return original;
        // Keep the entire south frontage, ground-floor entrance, and stair footprint.
        return new Rect(original.x0(), original.z0() + REAR_RECESS,
                original.x1(), original.z1());
    }

    static Rect wingFootprint(Rect original, Rect main, long hash) {
        if (original.z0() == main.z0()) return null;
        // Rear-east position avoids Tower's north-west staircase (x0+2 ... x0+5).
        // Subtle offsets allow unequal party-wall positions without invading alleys.
        int x0 = main.x0() + Math.max(8, main.width() / 2)
                + CityHash.range(CityHash.mix(hash ^ 0x55A150L), 2);
        int x1 = Math.min(main.x1() - 1, x0 + 7);
        if (x1 - x0 < 6) return null;
        return new Rect(x0, original.z0(), x1, main.z0());
    }

    static int wingFloors(int mainFloors, long hash) {
        return mainFloors >= 3 && CityHash.range(CityHash.mix(hash ^ 0x55F1002L), 3) == 0 ? 2 : 1;
    }

    static void build(Canvas c, Lot lot, Rect original, Rect main, int mainFloors, Style style) {
        if (original.equals(main)) return;
        Rect wing = wingFootprint(original, main, lot.hash());
        if (wing == null) return;
        int floors = wingFloors(mainFloors, lot.hash());
        int yRoof = STREET_Y + floors * Tower.STOREY;

        c.fill(wing, STREET_Y, STREET_Y, style.floor());
        c.walls(wing, GROUND_Y, yRoof - 1, style.wall());
        c.clear(wing.inset(1), GROUND_Y, yRoof - 1);
        for (int k = 0; k < floors; k++) {
            int floorY = STREET_Y + k * Tower.STOREY;
            if (k > 0) c.fill(wing.inset(1), floorY, floorY, style.floor());
            // Wall windows are high so exterior worktables and sleeping spaces fit.
            int wx = wing.centerX();
            c.pane(wx, floorY + 2, wing.z0(), Direction.Axis.X, style.glass());
            c.pane(wing.x1(), floorY + 2, wing.z0() + 2, Direction.Axis.Z, style.glass());
            furnish(c, wing.inset(1), floorY, lot, k);
            // An indoor door from the main building, with clear approaches on BOTH sides.
            link(c, wing, main, floorY);
        }
        c.fill(wing, yRoof, yRoof, Blocks.WAXED_WEATHERED_CUT_COPPER);
        // The low/stepped roof is useful: a maintenance deck accessed from the
        // next full-height main-building floor, not an inaccessible decoration.
        for (int x = wing.x0(); x <= wing.x1(); x++)
            c.set(x, yRoof + 1, wing.z0(), Blocks.IRON_BARS);
        for (int z = wing.z0() + 1; z < wing.z1(); z++) {
            c.set(wing.x0(), yRoof + 1, z, Blocks.IRON_BARS);
            c.set(wing.x1(), yRoof + 1, z, Blocks.IRON_BARS);
        }
        // Practical roof work: rain collection and a real maintenance bench.
        c.set(wing.x0() + 1, yRoof + 1, wing.z0() + 1, Blocks.CAULDRON);
        c.set(wing.x1() - 1, yRoof + 1, wing.z0() + 1, Blocks.STONECUTTER);
        link(c, wing, main, yRoof);
    }

    private static void furnish(Canvas c, Rect in, int floorY, Lot lot, int storey) {
        int y = floorY + 1;
        // First floor is a purposeful living-support room; upper floor, if any,
        // supplies a compact sleeping/rest area (no isolated dummy volume).
        if (storey == 1) {
            c.bed(in.x0(), y, in.z0() + 1, Direction.SOUTH, Blocks.BROWN_BED);
            c.set(in.x1(), y, in.z0() + 1, Blocks.CRAFTING_TABLE);
            c.set(in.x1(), y, in.z0() + 3, Blocks.BOOKSHELF);
            return;
        }
        switch (CityHash.range(CityHash.mix(lot.hash() ^ 0x55E500L), 3)) {
            case 0 -> { // Shared laundry/wash room.
                c.set(in.x0(), y, in.z0() + 1, Blocks.CAULDRON);
                c.set(in.x1(), y, in.z0() + 1, Blocks.LOOM);
                c.barrel(in.x1(), y, in.z0() + 3, "apartment");
            }
            case 1 -> { // Workshop/repair annex.
                c.set(in.x0(), y, in.z0() + 1, Blocks.SMITHING_TABLE);
                c.set(in.x1(), y, in.z0() + 1, Blocks.STONECUTTER);
                c.set(in.x1(), y, in.z0() + 3, Blocks.CRAFTING_TABLE);
            }
            default -> { // Food preparation and dry storage, no exposed chests.
                c.set(in.x0(), y, in.z0() + 1, Blocks.SMOKER);
                c.set(in.x1(), y, in.z0() + 1, Blocks.COMPOSTER);
                c.barrel(in.x1(), y, in.z0() + 3, "shop");
            }
        }
    }

    /** Door through the common north wall; roof deck gets an upper-floor door. */
    private static void link(Canvas c, Rect wing, Rect main, int floorY) {
        int x = wing.centerX();
        // Clear only a short entrance corridor; never disturb west-side stairs.
        c.clear(new Rect(x, wing.z1() - 1, x, wing.z1() - 1), floorY + 1, floorY + 2);
        c.clear(new Rect(x, main.z0() + 1, x, main.z0() + 2), floorY + 1, floorY + 2);
        // The wall is shared: north wall of main / south wall of annex.
        c.door(x, floorY + 1, main.z0(), Direction.NORTH, Blocks.SPRUCE_DOOR);
    }
}
