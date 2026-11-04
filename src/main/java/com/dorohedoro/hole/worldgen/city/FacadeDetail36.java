package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;

/**
 * Pass 36: sparse purposeful facade depth on the building side of the front
 * setback: rainwater downpipes, window lintels and patchwork repair plates.
 * Only air blocks are filled. No doors, windows, stairs or ceiling shapes are
 * removed, and there are no random external barrels/chests or extra lamps.
 */
final class FacadeDetail36 {
    private FacadeDetail36() {}

    private static void air(Canvas c, int x, int y, int z, Block block) {
        if (c.contains(x, z) && c.get(x, y, z).isAir()) c.set(x, y, z, block);
    }

    static void residence(Canvas c, Rect box, int floors, long hash, District district) {
        if (box.width() < 11 || box.depth() < 9) return;
        int front = box.z1() + 1;
        int wall = box.z1();
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        int doorX = box.centerX();
        Block trim = switch (district) {
            case INDUSTRIAL, WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case DERELICT, GRAVEYARD -> Blocks.DEEPSLATE_TILE_SLAB;
            case MARKET -> Blocks.DARK_OAK_SLAB;
            case OUTSKIRTS -> Blocks.SPRUCE_SLAB;
            default -> Blocks.TUFF_BRICK_SLAB;
        };
        // Eaves / narrow sills: only on floors with existing window openings.
        // Never cover the main entrance with a full-size solid block.
        for (int floor = 0; floor < floors; floor++) {
            int windowY = CityPlan.STREET_Y + floor * Tower.STOREY + 2;
            for (int x = box.x0() + 2; x <= box.x1() - 2; x++) {
                if (Math.abs(x - doorX) <= 1 && floor == 0) continue;
                if (CityHash.range(CityHash.of(hash, x, floor), 6) != 0) continue;
                // A lintel is at least two blocks off the ground: no sidewalk obstruction.
                if (!c.contains(x, wall) || !c.contains(x, front)) continue;
                if (c.get(x, windowY, wall).getBlock() instanceof CrossCollisionBlock) {
                    air(c, x, windowY + 1, front, trim);
                }
            }
        }
        // Downpipes at the outside corners create a long 3D vertical instead
        // of disconnected micro-noise. They are purely ornamental, not loot.
        int edgeX = CityHash.range(hash >>> 10, 2) == 0 ? box.x0() : box.x1();
        if (Math.abs(edgeX - doorX) >= 3) {
            for (int y = CityPlan.GROUND_Y + 2; y < top + 1; y++) {
                if (y % 5 != 0) air(c, edgeX, y, front, Blocks.IRON_BARS);
            }
        }
        // One matching roof maintenance vent, not a pile of chest props.
        if (floors >= 2 && box.width() >= 14 && box.depth() >= 12
                && CityHash.range(hash >>> 20, 3) == 0) {
            int x = box.x0() + 3;
            int z = box.z1() - 3;
            air(c, x, top + 3, z, Blocks.WAXED_WEATHERED_CUT_COPPER);
            air(c, x, top + 4, z, Blocks.IRON_TRAPDOOR);
        }
    }

    static void shop(Canvas c, Rect box, long hash) {
        if (box.width() < 10 || box.depth() < 8) return;
        int z = box.z1() + 1;
        // Thin metal awning above an accessible 2-high door; no bright lanterns.
        for (int x = box.x0() + 1; x <= box.x1() - 1; x++) {
            if (CityHash.range(CityHash.of(hash, x, 36), 8) == 0) continue; // repaired/absent sections
            air(c, x, CityPlan.GROUND_Y + 3, z, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
        }
    }
}
