package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 40: selected older apartment/tenement roofs grow a genuinely enclosed
 * maintenance shelter, not another set of vents on a flat cuboid. Every
 * shelter has one reachable roof-facing door and useful repair equipment.
 * No loot chests, torches, or extra outdoor streetlights are generated.
 * The north-west roof stair and an open route to its northern entrance remain clear.
 */
final class Skyline40 {
    private Skyline40() {}

    static void rooftop(Canvas c, Rect box, int floors, long seed, Tower.Roof roof) {
        if (!c.hasSkyline40() || roof != Tower.Roof.PLAIN
                || floors < 3 || box.width() < 19 || box.depth() < 16
                || CityHash.range(seed >>> 19, 3) != 0) return;
        int x0 = box.x1() - 9;
        int x1 = box.x1() - 3;
        int z0 = box.z0() + 3;
        int z1 = z0 + 5;
        int top = CityPlan.STREET_Y + floors * Tower.STOREY;
        // Existing roof gardens, cisterns, or mechanical plant retain priority.
        // One-chunk Canvas cannot see neighbors, so only inspect inside the
        // current chunk; placement remains coordinate-deterministic regardless
        // of which portions cross chunk boundaries.
        Block wall = switch (CityHash.range(seed >>> 27, 4)) {
            case 0 -> Blocks.MUD_BRICKS;
            case 1 -> Blocks.WAXED_WEATHERED_CUT_COPPER;
            case 2 -> Blocks.TUFF_BRICKS;
            default -> Blocks.DEEPSLATE_BRICKS;
        };
        Block roofCap = CityHash.range(seed >>> 36, 2) == 0
                ? Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB : Blocks.DEEPSLATE_TILE_SLAB;
        int doorX = (x0 + x1) / 2;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge) continue;
                for (int y = top + 1; y <= top + 4; y++) {
                    if (x == doorX && z == z0 && y <= top + 2) continue;
                    if ((x == x0 || x == x1) && z == z0 + 3 && y == top + 2) {
                        c.set(x, y, z, Blocks.IRON_BARS);
                    } else {
                        c.set(x, y, z, wall);
                    }
                }
            }
        }
        // The roof is a real volume, with a door on its north face opening
        // onto the existing accessible roof deck, not a sealed decoration.
        c.door(doorX, top + 1, z0, Direction.SOUTH, Blocks.SPRUCE_DOOR);
        c.fill(x0 - 1, top + 5, z0 - 1, x1 + 1, top + 5, z1 + 1, roofCap);
        // A workshop or laundry annex: deliberately few pieces and a clear
        // central floor; these are functional vanilla blocks, not dummy props.
        if (CityHash.range(seed >>> 44, 2) == 0) {
            c.set(x1 - 1, top + 1, z1 - 1, Blocks.SMITHING_TABLE);
            c.set(x0 + 1, top + 1, z1 - 1, Blocks.STONECUTTER);
        } else {
            c.set(x1 - 1, top + 1, z1 - 1, Blocks.LOOM);
            c.set(x0 + 1, top + 1, z1 - 1, Blocks.CAULDRON);
        }
        // A staggered, patched parapet breaks the unvaried square silhouette.
        for (int x = box.x0() + 1; x < box.x1(); x++) {
            if (x >= x0 - 1 && x <= x1 + 1) continue;
            if (CityHash.range(CityHash.of(seed, x, 40), 7) == 0) continue;
            if (c.contains(x, box.z1()) && c.get(x, top + 2, box.z1()).isAir())
                c.set(x, top + 2, box.z1(), Blocks.CRACKED_STONE_BRICKS);
        }
    }
}
