package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Indoor micro-layouts for the Hole's new v4 generator profile. All placement is absolute-coordinate
 * deterministic and stays within lot foundations. Key design constraint: the north-wall stair at
 * (in.x0()+1..4,in.z0()) and the south-facing main entry are always left unobstructed.
 * Legacy generator profiles never execute this class, protecting existing saves.
 */
final class InteriorLayouts15 {
    private InteriorLayouts15() {}

    static void residence(Canvas c, Style style, int storey, Rect in, int y) {
        int kind = InteriorPlan15.residence(c.seed(), in.centerX(), in.centerZ(), storey);
        if (storey == 0) {
            lobby(c, in, y, kind);
            return;
        }
        if (!InteriorPlan15.fitsPartition(in)) {
            openStudio(c, in, y, kind);
            return;
        }
        // North circulation aisle links the top of the shared stair with all apartment entries.
        int wallZ = InteriorPlan15.corridorWallZ(in);
        c.fill(in.x0(), y + 1, wallZ, in.x1(), y + 3, wallZ, style.wall());
        int westDoor = in.x0() + 3;
        int eastDoor = in.x1() - 3;
        // No forced doors in tiny or oddly-shaped rooms: the partition is already size gated.
        c.door(westDoor, y + 1, wallZ, Direction.SOUTH, style.door());
        c.door(eastDoor, y + 1, wallZ, Direction.SOUTH, style.door());
        if (kind == 0 || kind == 3) {
            // Two independent flats off an actual shared corridor, rather than one giant hall.
            int splitX = in.centerX();
            c.fill(splitX, y + 1, wallZ + 1, splitX, y + 3, in.z1(), style.wall());
            furnishFlat(c, in.x0() + 1, westDoor, wallZ + 1, in.z1(), y, kind);
            furnishFlat(c, splitX + 1, eastDoor, wallZ + 1, in.z1(), y, kind + 2);
        } else if (kind == 1) {
            // Communal boarding floor: double bunks, separate wash and supply corners.
            c.bed(in.x0() + 1, y + 1, in.z1() - 1, Direction.SOUTH, Blocks.BROWN_BED);
            c.bed(in.x1() - 2, y + 1, in.z1() - 1, Direction.SOUTH, Blocks.GRAY_BED);
            c.barrel(in.x0() + 1, y + 1, wallZ + 2, "apartment");
            c.set(in.x1() - 1, y + 1, wallZ + 2, Blocks.CAULDRON);
            c.set(in.x1() - 1, y + 1, in.z1() - 2, Blocks.LOOM);
        } else {
            // Live-work studios: workbenches near the doorway, sleeping spaces at rear.
            c.set(in.x0() + 1, y + 1, wallZ + 2, Blocks.CRAFTING_TABLE);
            c.set(in.x1() - 1, y + 1, wallZ + 2, Blocks.STONECUTTER);
            c.barrel(in.x1() - 1, y + 1, in.z1() - 1, "apartment");
            c.bed(in.x0() + 1, y + 1, in.z1() - 1, Direction.SOUTH, Blocks.GREEN_BED);
            c.set(in.x0() + 1, y + 2, in.z1() - 3, Blocks.BOOKSHELF);
        }
        // Direct north corridor lighting; no freestanding blocks in the path.
        Tower.lamp(c, in.centerX(), y, in.z0() + 1);
        InteriorDetails22.residence(c, in, y, kind);
        InteriorDetails24.residence(c, in, y, kind);
    }

    private static void lobby(Canvas c, Rect in, int y, int kind) {
        // Shared street-level access, including the stair, always takes priority over clutter.
        c.barrel(in.x1(), y + 1, in.z0() + 2, "apartment");
        c.set(in.x1(), y + 1, in.z0() + 3, Blocks.CRAFTING_TABLE);
        c.set(in.x0(), y + 1, in.z1() - 2, kind == 2 ? Blocks.SMITHING_TABLE : Blocks.BOOKSHELF);
        c.set(in.x0(), y + 1, in.z1() - 3, kind == 1 ? Blocks.SMOKER : Blocks.CAULDRON);
        if (in.width() >= 11 && in.depth() >= 10) {
            c.set(in.x1() - 1, y + 1, in.z1() - 2, Blocks.LECTERN);
            c.set(in.x1() - 1, y + 1, in.z1() - 3, Blocks.FLOWER_POT);
        }
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        InteriorDetails22.lobby(c, in, y, kind);
        InteriorDetails24.lobby(c, in, y, kind);
    }

    private static void openStudio(Canvas c, Rect in, int y, int kind) {
        c.bed(in.x0(), y + 1, in.z1() - 1, Direction.SOUTH, Blocks.GRAY_BED);
        c.set(in.x1(), y + 1, in.z1() - 1, kind % 2 == 0 ? Blocks.CRAFTING_TABLE : Blocks.SMOKER);
        c.barrel(in.x1(), y + 1, in.z0() + 2, "apartment");
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        InteriorDetails22.residence(c, in, y, kind);
        InteriorDetails24.residence(c, in, y, kind);
    }

    private static void furnishFlat(Canvas c, int x0, int doorX, int z0, int z1, int y, int kind) {
        int start = Math.min(x0, doorX);
        int end = Math.max(x0, doorX);
        // Keep storage and beds behind room doors and out of the main circulation aisle.
        c.bed(start, y + 1, z1 - 1, Direction.SOUTH, kind % 2 == 0 ? Blocks.BROWN_BED : Blocks.LIGHT_GRAY_BED);
        c.barrel(end, y + 1, z1 - 1, "apartment");
        if (z1 - z0 >= 3) {
            c.set(start, y + 1, z0 + 1, kind % 2 == 0 ? Blocks.SMOKER : Blocks.CAULDRON);
        }
    }

    static void shop(Canvas c, Rect in, int y, Block jobSite) {
        int kind = InteriorPlan15.shop(c.seed(), in.centerX(), in.centerZ());
        // Always keep a two-block path from the south entrance to the stairway.
        // All fixtures hug the west/east walls, shelving from the north no closer than z0+3.
        for (int z = in.z0() + 4; z <= in.z1() - 3; z += 3) {
            c.barrel(in.x1(), y + 1, z, "shop");
            if (kind == 0 || kind == 3) c.set(in.x0(), y + 1, z, Blocks.BOOKSHELF);
            else c.set(in.x0(), y + 1, z, kind == 1 ? Blocks.SMITHING_TABLE : Blocks.SMOKER);
        }
        c.set(in.x1(), y + 1, in.z0() + 2, jobSite);
        // Larger shops have a rear service room rather than only wall-side shelves.
        // A centered doorway keeps the public sales floor connected to its stock room.
        if (in.width() >= 14 && in.depth() >= 15) {
            int serviceZ = in.z1() - 5;
            c.fill(in.x0() + 1, y + 1, serviceZ, in.x1() - 1, y + 3, serviceZ,
                    kind == 2 ? Blocks.STRIPPED_SPRUCE_WOOD : Blocks.TUFF_BRICKS);
            c.door(in.centerX(), y + 1, serviceZ, Direction.NORTH, Blocks.SPRUCE_DOOR);
            c.barrel(in.x0() + 1, y + 1, in.z1() - 1, "shop");
            c.barrel(in.x1() - 1, y + 1, in.z1() - 1, "shop");
            c.set(in.x0() + 1, y + 1, in.z1() - 2,
                    kind == 2 ? Blocks.SMOKER : Blocks.CRAFTING_TABLE);
            // The service room is behind the door; do not leave a counter directly
            // in its opening or in the two-block south-entrance route.
        } else if (in.width() >= 11 && in.depth() >= 11) {
            c.fill(in.x1() - 3, y + 1, in.z1() - 3, in.x1() - 1, y + 1, in.z1() - 3, Blocks.SPRUCE_SLAB);
            c.set(in.x1() - 2, y + 1, in.z1() - 4, kind == 2 ? Blocks.CAULDRON : Blocks.CRAFTING_TABLE);
        }
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        InteriorDetails22.shop(c, in, y, kind);
        InteriorDetails24.shop(c, in, y, kind);
    }

    static void clinic(Canvas c, Rect in, int y, int storey) {
        int kind = InteriorPlan15.clinic(c.seed(), in.centerX(), in.centerZ(), storey);
        int rearZ = in.z1() - 1;
        // Big clinical floors get a fully traversable rear ward / treatment room.
        // Unlike an arbitrary dividing wall, this has a real doorway aligned with
        // the center approach, and the shared northern stair corridor stays free.
        if (in.width() >= 14 && in.depth() >= 15) {
            int wardZ = in.z1() - 6;
            c.fill(in.x0() + 1, y + 1, wardZ, in.x1() - 1, y + 3, wardZ,
                    Blocks.WHITE_STAINED_GLASS);
            c.door(in.centerX(), y + 1, wardZ, Direction.NORTH, Blocks.SPRUCE_DOOR);
            c.barrel(in.x1() - 1, y + 1, in.z1() - 2, "clinic");
        }
        if (kind == 0 || kind == 3) {
            for (int x = in.x0() + 1; x < in.x1() - 2; x += 4) {
                c.bed(x, y + 1, rearZ, Direction.SOUTH, Blocks.WHITE_BED);
            }
            c.chest(in.x1(), y + 1, in.z0() + 3, Direction.WEST, "clinic");
        } else if (kind == 1) {
            c.bed(in.x1() - 2, y + 1, rearZ, Direction.SOUTH, Blocks.WHITE_BED);
            c.set(in.x0(), y + 1, in.z1() - 2, Blocks.CAULDRON);
            c.set(in.x0(), y + 1, in.z1() - 3, Blocks.LECTERN);
            c.barrel(in.x1(), y + 1, in.z0() + 3, "clinic");
        } else {
            c.set(in.x1(), y + 1, in.z0() + 3, Blocks.BREWING_STAND);
            c.set(in.x1(), y + 1, in.z0() + 4, Blocks.CAULDRON);
            c.set(in.x0(), y + 1, in.z1() - 2, Blocks.CRAFTING_TABLE);
            c.chest(in.x1(), y + 1, rearZ, Direction.WEST, "clinic");
        }
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        InteriorDetails22.clinic(c, in, y, kind);
        InteriorDetails24.clinic(c, in, y, kind);
    }

    static void industrial(Canvas c, Rect in, int y, int roofY, boolean factory, long hash) {
        if (in.width() < 12 || in.depth() < 12) return;
        // Tool alcoves against side walls and hanging maintenance lights. The central
        // cart corridor remains empty, and neither an overhead lamp nor a catwalk seals it.
        int z = in.z0() + 3 + CityHash.range(hash >>> 9, Math.max(1, in.depth() - 7));
        c.set(in.x1(), y, z, factory ? Blocks.STONECUTTER : Blocks.CRAFTING_TABLE);
        c.set(in.x1(), y, z + 1, factory ? Blocks.ANVIL : Blocks.COMPOSTER);
        c.barrel(in.x0(), y, z, factory ? "factory" : "warehouse");
        c.set(in.x0() + 1, roofY - 1, z, Blocks.CHAIN);
        // Variable rear-side storage bay, not a complete transverse barrier.
        if (in.width() >= 17 && in.depth() >= 17) {
            int bayX = in.x1() - 3;
            c.set(bayX, y, in.z0() + 2, Blocks.BARREL);
            c.set(bayX, y + 1, in.z0() + 2, Blocks.SPRUCE_TRAPDOOR);
        }
        InteriorDetails22.industrial(c, in, y, hash);
        InteriorDetails24.industrial(c, in, y, hash);
    }
}
