package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 44: infrastructure-shaped silhouettes for the inhabited Hole.
 * Rain collection, upper-storey services, industrial roof extraction and
 * sheltered loading zones are building services, not random street clutter.
 * All features are bounded to their owner lot and add no light or loot.
 *
 * Each column is handled independently, using only same-column air tests;
 * no decision depends on the order neighboring chunks were generated.
 */
final class FunctionalFacade44 {
    private FunctionalFacade44() {}

    private static boolean air(Canvas c, Rect lot, int x, int y, int z, Block material) {
        if (!lot.contains(x, z) || !c.contains(x, z) || !c.get(x, y, z).isAir()) return false;
        c.set(x, y, z, material);
        return true;
    }

    /**
     * Apartments keep all existing bedrooms and stairs; rainwater services live
     * outside the south face and on the already accessible rooftop perimeter.
     */
    static void residence(Canvas c, Lot lot, Rect box, int floors, long hash, Tower.Roof roof) {
        Rect parcel = lot.rect();
        if (floors < 3 || box.width() < 14 || box.depth() < 12) return;
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        int pipeX = CityHash.range(hash >>> 21, 2) == 0 ? box.x0() + 2 : box.x1() - 2;
        int front = box.z1() + 1;
        // Existing front-door and footpath headroom remain unobstructed.
        if (parcel.contains(pipeX, front)) {
            for (int y = CityPlan.GROUND_Y + 3; y <= top + 1; y++) {
                air(c, parcel, pipeX, y, front, Blocks.CHAIN);
            }
            air(c, parcel, pipeX, top + 2, front, Blocks.WAXED_WEATHERED_CUT_COPPER);
        }
        // Keep the irregular existing cornice/parapet; one continuous patched
        // wind break makes a legible silhouette rather than rooftop pixel-noise.
        if (CityHash.range(hash >>> 34, 3) != 0) {
            int length = Math.min(box.width() - 7, 4 + CityHash.range(hash >>> 9, 5));
            for (int x = box.x0() + 3; x < box.x0() + 3 + length; x++) {
                air(c, parcel, x, top + 3, box.z1(), Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB);
            }
        }
        // A small roof ventilation/filtration riser; south-west placement avoids
        // the north-west roof stair, existing north-east hut, and garden beds.
        // No residential floor blocks, windows or existing equipment are cleared.
        if (roof != Tower.Roof.PLAIN || box.width() < 17 || box.depth() < 15
                || CityHash.range(hash ^ 0x44A11EFL, 3) != 0) return;
        int x0 = box.x0() + 3;
        int z0 = box.z1() - 6;
        for (int x = x0; x <= x0 + 2; x++) {
            for (int z = z0; z <= z0 + 2; z++) {
                boolean edge = x == x0 || x == x0 + 2 || z == z0 || z == z0 + 2;
                if (!edge) continue; // leave a real open vent, not a solid cube
                for (int y = top + 1; y <= top + 4; y++) {
                    Block brick = y == top + 4 ? Blocks.WAXED_WEATHERED_CUT_COPPER
                            : Blocks.GRAY_TERRACOTTA;
                    air(c, parcel, x, y, z, brick);
                }
            }
        }
        air(c, parcel, x0 + 1, top + 4, z0 + 1, Blocks.IRON_BARS);
        // A usable vanilla rainwater cauldron on the open roof, one per eligible
        // filter stack. It is not a loot chest, lantern, or impassable rooftop row.
        air(c, parcel, x0 + 4, top + 1, z0 + 1, Blocks.CAULDRON);
        // Roofline service conduit links the vent to the wall-facing downpipe
        // only when positioned on the western side; no cables float over streets.
        if (pipeX == box.x0() + 2) {
            for (int z = z0 + 1; z <= front; z++) {
                if (z >= box.z1() - 1 && z <= box.z1()) continue; // keep parapet intact
                air(c, parcel, pipeX, top + 2, z, Blocks.CHAIN);
            }
        }
    }

    /** Covered loading station on industrial frontage; all support posts begin
     * at least three blocks above grade and never enter the cart-sized door.
     * The factory's foundry line, internal aisles, and chimney tops are untouched.
     */
    static void industry(Canvas c, Lot lot, Rect box, int roofY, long hash, boolean factory) {
        Rect parcel = lot.rect();
        int cx = box.centerX();
        if (box.width() >= 17 && box.depth() >= 12 && parcel.z1() >= box.z1() + 3
                && CityHash.range(hash ^ 0x44FACC7L, 3) != 0) {
            int half = Math.min(5, (box.width() - 6) / 2);
            int top = Math.min(roofY - 1, CityPlan.GROUND_Y + 5);
            for (int x = cx - half; x <= cx + half; x++) {
                for (int z = box.z1() + 1; z <= box.z1() + 2; z++) {
                    air(c, parcel, x, top, z, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
                }
            }
            // Suspended braces, at the far corners; door remains at least 3 high.
            for (int x : new int[]{cx - half, cx + half}) {
                for (int y = CityPlan.GROUND_Y + 3; y < top; y++) {
                    air(c, parcel, x, y, box.z1() + 1, Blocks.IRON_BARS);
                }
            }
        }
        // Secondary filtration hood adds a smaller stepped service mass below
        // established large smokestacks rather than yet more identical chimneys.
        if (factory && box.width() >= 21 && box.depth() >= 17
                && CityHash.range(hash >>> 16, 3) == 1) {
            int x0 = box.x0() + 3;
            int z0 = box.z1() - 6;
            for (int x = x0; x < x0 + 4; x++) {
                for (int z = z0; z < z0 + 3; z++) {
                    boolean rim = x == x0 || x == x0 + 3 || z == z0 || z == z0 + 2;
                    if (rim) for (int y = roofY + 1; y <= roofY + 4; y++)
                        air(c, parcel, x, y, z, y == roofY + 4
                                ? Blocks.WAXED_OXIDIZED_CUT_COPPER : Blocks.MUD_BRICKS);
                }
            }
            for (int x = x0 + 1; x <= x0 + 2; x++)
                air(c, parcel, x, roofY + 4, z0 + 1, Blocks.IRON_BARS);
        }
    }
}
