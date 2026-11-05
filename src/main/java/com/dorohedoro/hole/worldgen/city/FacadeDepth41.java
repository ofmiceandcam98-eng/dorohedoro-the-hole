package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;

/**
 * Pass 41: small but legible exterior relief rather than random micro-clutter.
 * Weathered window sills, upper-storey lintels and restrained shop braces.
 * All details are outside the building shell, above the ground-floor headroom,
 * and are written ONLY into currently empty cells. No windows, doors, roofs,
 * stairways, chests or streetlights are replaced.
 */
final class FacadeDepth41 {
    private FacadeDepth41() {}

    private static boolean putAir(Canvas c, int x, int y, int z, Block block) {
        if (!c.contains(x,z) || !c.get(x,y,z).isAir()) return false;
        c.set(x,y,z,block);
        return true;
    }

    static void residence(Canvas c, Rect box, int floors, long hash, District district) {
        if (floors < 2 || box.width() < 11 || box.depth() < 9) return;
        Block sill = switch (district) {
            case INDUSTRIAL, WATERFRONT -> Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case DERELICT, GRAVEYARD -> Blocks.DEEPSLATE_BRICK_SLAB;
            case MARKET -> Blocks.DARK_OAK_SLAB;
            case OUTSKIRTS -> Blocks.SPRUCE_SLAB;
            default -> Blocks.TUFF_BRICK_SLAB;
        };
        int outside = box.z1() + 1;
        int wall = box.z1();
        // No ground-floor windows or doors are touched. The side-by-side slabs
        // make some existing TWO-high windows read as recessed openings.
        for (int storey = 1; storey < floors; storey++) {
            int y = CityPlan.STREET_Y + storey * Tower.STOREY + 2;
            for (int x = box.x0()+2; x <= box.x1()-2; x++) {
                if (CityHash.range(CityHash.of(hash ^ 0x41FACAD3L, x, storey), 5) > 1) continue;
                if (!c.contains(x,wall) || !c.contains(x,outside)) continue;
                // Use the ACTUAL generated window, not a guessed repeating grid.
                if (!(c.get(x,y,wall).getBlock() instanceof CrossCollisionBlock)) continue;
                putAir(c,x,y-1,outside,sill);
            }
        }
        // A restrained corner ventilation stack gives taller apartment fronts
        // a recognisable service core without adding any nighttime light.
        if (floors >= 4 && box.width() >= 14 && CityHash.range(hash >>> 24, 3) == 0) {
            int x = box.x0()+1;
            int top = CityPlan.STREET_Y + floors * Tower.STOREY;
            for (int y=CityPlan.GROUND_Y+4; y<top; y+=2)
                putAir(c,x,y,outside,Blocks.IRON_BARS);
        }
    }

    static void shop(Canvas c, Rect box, long hash) {
        if (box.width() < 13) return;
        int z = box.z1()+1;
        // Upper awning braces: no new posts at walking height, and the central
        // two-block door is always clear. Sparse damaged/absent sections vary.
        if (CityHash.range(hash ^ 0x41BACE1L, 3) == 0) return;
        for (int x : new int[]{box.x0()+2,box.x1()-2}) {
            if (Math.abs(x-box.centerX())<=2) continue;
            putAir(c,x,CityPlan.GROUND_Y+4,z,Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
        }
    }
}
