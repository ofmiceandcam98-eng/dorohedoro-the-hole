package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Additional floor-based, low-clutter micro-furnishing, guarded by the new
 * world detail version. Never replace walls, doors, beds or inventories. All
 * new fixtures stay at the room perimeter to preserve north stair, south entry,
 * rear-ward doorway and main circulation spine.
 */
final class InteriorDetails24 {
    private InteriorDetails24() {}

    private static void place(Canvas c, int x, int y, int z, Block block) {
        if (c.contains(x,z) && c.get(x,y,z).isAir() && !c.get(x,y-1,z).isAir()) {
            c.set(x,y,z,block);
        }
    }

    static void lobby(Canvas c, Rect r, int y, int kind) {
        if (!c.hasPass24Interiors() || r.width()<11 || r.depth()<10) return;
        // Parcel office / washstand / worn waiting bench in different corners.
        place(c,r.x0()+1,y+1,r.z1()-4,Blocks.BARREL);
        place(c,r.x0()+1,y+2,r.z1()-4,Blocks.FLOWER_POT);
        place(c,r.x1()-2,y+1,r.z0()+4,Blocks.SPRUCE_SLAB);
        place(c,r.x1()-2,y+1,r.z0()+5,kind%2==0 ? Blocks.BROWN_CARPET : Blocks.GRAY_CARPET);
    }

    static void residence(Canvas c, Rect r, int y, int kind) {
        if (!c.hasPass24Interiors() || r.width()<11 || r.depth()<11) return;
        // Mismatched furniture for lived-in apartments, with no central corridor obstructions.
        place(c,r.x0(),y+1,r.z1()-4,kind%2==0 ? Blocks.LOOM : Blocks.COMPOSTER);
        place(c,r.x1(),y+1,r.z1()-4,Blocks.CRAFTING_TABLE);
        place(c,r.x0()+2,y+1,r.z1()-3,Blocks.GRAY_CARPET);
        place(c,r.x1()-2,y+1,r.z1()-3,Blocks.BROWN_CARPET);
    }

    static void shop(Canvas c, Rect r, int y, int kind) {
        if (!c.hasPass24Interiors() || r.width()<12 || r.depth()<12) return;
        // Sidewall stock and low packing surfaces; central shopper route stays open.
        place(c,r.x0()+1,y+1,r.z0()+5,kind==2 ? Blocks.SMOKER : Blocks.SPRUCE_SLAB);
        place(c,r.x1()-1,y+1,r.z0()+6,Blocks.BARREL);
        place(c,r.x0()+1,y+1,r.z0()+7,Blocks.CRAFTING_TABLE);
        place(c,r.x1()-2,y+1,r.z1()-3,Blocks.BROWN_CARPET);
    }

    static void clinic(Canvas c, Rect r, int y, int kind) {
        if (!c.hasPass24Interiors() || r.width()<12 || r.depth()<12) return;
        place(c,r.x0()+1,y+1,r.z0()+5,Blocks.CAULDRON);
        place(c,r.x0()+1,y+1,r.z0()+6,Blocks.WHITE_CARPET);
        place(c,r.x1()-1,y+1,r.z0()+5,Blocks.LECTERN);
        place(c,r.x1()-1,y+1,r.z0()+6,kind%2==0 ? Blocks.BREWING_STAND : Blocks.WHITE_CARPET);
    }

    static void industrial(Canvas c, Rect r, int y, long hash) {
        if (!c.hasPass24Interiors() || r.width()<16 || r.depth()<16) return;
        place(c,r.x1()-1,y,r.z1()-4,Blocks.SMITHING_TABLE);
        place(c,r.x1()-1,y,r.z1()-5,Blocks.STONECUTTER);
        place(c,r.x0()+1,y,r.z1()-4,Blocks.BARREL);
        place(c,r.x0()+1,y+1,r.z1()-4,Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
    }
}
