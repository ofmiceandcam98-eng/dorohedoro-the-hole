package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The starting quarter and the named places. Their layouts are original designs inspired by the source's locations. */
final class Landmarks {
    private static final BlockState POST_LANTERN = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);

    private Landmarks() {}

    /** The spawn square. Everything stays against the rim, because players land anywhere within ten blocks of the center. */
    static void plaza(Canvas c, Lot lot) {
        Rect r = lot.rect();
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.POLISHED_ANDESITE, 4, Blocks.STONE_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 1));
        c.fill(r.centerX() - 3, STREET_Y, r.centerZ() - 3, r.centerX() + 3, STREET_Y, r.centerZ() + 3, Blocks.CHISELED_STONE_BRICKS);
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int x = r.centerX() + i * 12;
                int z = r.centerZ() + j * 12;
                c.fill(x, GROUND_Y, z, x, GROUND_Y + 3, z, Blocks.SPRUCE_FENCE);
                c.set(x, GROUND_Y + 4, z, POST_LANTERN);
            }
        }
        int rim = r.z0();
        c.set(r.centerX() - 3, GROUND_Y, rim, Blocks.CRAFTING_TABLE);
        c.facing(r.centerX() - 2, GROUND_Y, rim, Direction.SOUTH, Blocks.FURNACE);
        // One useful starter supply container, rather than a conspicuous
        // line of treasure chests on the square in new worlds.
        if (c.hasOccupiedInteriors35()) {
            c.barrel(r.centerX() - 1, GROUND_Y, rim, "starter_cache");
            c.set(r.centerX(), GROUND_Y, rim, Blocks.SPRUCE_SLAB);
            c.set(r.centerX() + 1, GROUND_Y, rim, Blocks.COMPOSTER);
        } else {
            c.chest(r.centerX() - 1, GROUND_Y, rim, Direction.SOUTH, "starter_cache");
            c.chest(r.centerX(), GROUND_Y, rim, Direction.SOUTH, "starter_cache");
            c.barrel(r.centerX() + 1, GROUND_Y, rim, "market");
        }
        c.set(r.centerX() + 2, GROUND_Y, rim, Blocks.CRAFTING_TABLE);
        c.fill(r.centerX() + 3, GROUND_Y, rim, r.centerX() + 3, GROUND_Y + 1, rim, Blocks.SPRUCE_LOG);
        c.set(r.centerX() + 3, GROUND_Y + 2, rim, Blocks.SPRUCE_PLANKS);
        c.sign(r.centerX() + 3, GROUND_Y + 1, rim + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "THE HOLE", "Food: Hungry Bug", "north of here", "Mines: road hatches");
        if (c.hasOccupiedCommons43()) OccupiedCommons43.plaza(c, lot);
    }

    /** Pass 47: small provisioned landing in a real fringe neighborhood, not a downtown plaza copy. */
    static void regionalLanding(Canvas c, Lot lot, HoleRegions42.Region region) {
        Rect r = lot.rect();
        // Useful mined materials, not a cosmetic recolor; the rest of the local
        // neighborhood still expresses its existing biome's surface and ecology.
        net.minecraft.world.level.block.Block surface = switch (region) {
            case SNOWFIELDS -> Blocks.POLISHED_ANDESITE;
            case DEAD_TAIGA -> Blocks.MOSSY_STONE_BRICKS;
            case DUST_BASIN -> Blocks.SANDSTONE;
            case IRON_BADLANDS -> Blocks.RED_SANDSTONE;
            case MOUNTAIN_RIM -> Blocks.COBBLESTONE;
            case CITY -> Blocks.CRACKED_STONE_BRICKS;
        };
        c.fill(r, STREET_Y, STREET_Y, surface);
        int x = r.centerX(), z = r.centerZ();
        // Spawn's central 9x9 area is flat, roofless, and unobstructed; supplies
        // live at the rim. No universal high-brightness street grid is added.
        c.fill(x - 5, STREET_Y, z - 5, x + 5, STREET_Y, z + 5, Blocks.CRACKED_STONE_BRICKS);
        int north = r.z0() + 2;
        if (c.hasRoomDressing71()) {
            // v53: just the supply barrel beside the sign. Its loot already holds logs, stone tools, coal and torches,
            // so the old line of loose workstations and the log pillar on open ground only looked like clutter.
            c.barrel(x + 7, GROUND_Y, north, "starter_cache");
        } else {
            c.set(x - 8, GROUND_Y, north, Blocks.CRAFTING_TABLE);
            c.facing(x - 6, GROUND_Y, north, Direction.SOUTH, Blocks.FURNACE);
            c.barrel(x - 4, GROUND_Y, north, "starter_cache");
            c.set(x - 2, GROUND_Y, north, Blocks.CAULDRON);
            c.set(x, GROUND_Y, north, Blocks.SMOKER);
            c.set(x + 2, GROUND_Y, north, Blocks.COMPOSTER);
            c.set(x + 4, GROUND_Y, north, Blocks.CRAFTING_TABLE);
            // Harvestable shelter-repair wood for regions without live trees.
            c.fill(x + 7, GROUND_Y, north, x + 7, GROUND_Y + 2, north, Blocks.SPRUCE_LOG);
            c.set(x + 7, GROUND_Y + 3, north, Blocks.SPRUCE_SLAB);
        }
        String name = switch (region) {
            case SNOWFIELDS -> "FROST DEPOT";
            case DEAD_TAIGA -> "DEADWOOD OUTPOST";
            case DUST_BASIN -> "DUST STOP";
            case IRON_BADLANDS -> "RUST CAMP";
            case MOUNTAIN_RIM -> "RIDGE SHELTER";
            case CITY -> "THE HOLE";
        };
        c.set(x + 8, GROUND_Y + 1, north, Blocks.BRICKS);
        c.sign(x + 8, GROUND_Y + 1, north + 1, Direction.SOUTH,
                Blocks.SPRUCE_WALL_SIGN, name, "BEDS TO EAST", "SEWERS BELOW", "THE HOLE");
        // Very limited lamps mark the first-night supplies but preserve dark
        // approaches and nocturnal mob spawning in surrounding streets.
        for (int dx : new int[]{-10, 10}) {
            c.fill(x + dx, GROUND_Y, z + 10, x + dx, GROUND_Y + 2, z + 10, Blocks.SPRUCE_FENCE);
            c.set(x + dx, GROUND_Y + 3, z + 10, POST_LANTERN);
        }
    }

    static void hungryBug(Canvas c, Lot lot) {
        Rect box = lot.rect();
        Style style = Style.HUNGRY_BUG;
        Tower.build(c, box, 2, style, lot.hash(), Tower.Roof.TANK, (cv, k, in, y) -> {
            if (k == 1) {
                LotPainter.dwelling(cv, style, k, in, y);
                return;
            }
            // Dining room to the south, kitchen behind a counter to the north.
            int counterZ = in.z0() + 5;
            cv.fill(in.x0(), y + 1, counterZ, in.x1() - 3, y + 1, counterZ, Blocks.SMOOTH_STONE);
            for (int x = in.x0() + 1; x <= in.x1() - 3; x += 3) {
                cv.facing(x, y + 1, in.z0() + 3, Direction.SOUTH, Blocks.SMOKER);
                cv.facing(x + 1, y + 1, in.z0() + 3, Direction.SOUTH, Blocks.FURNACE);
            }
            cv.barrel(in.x1(), y + 1, in.z0() + 2, "hungry_bug");
            cv.barrel(in.x1(), y + 1, in.z0() + 3, "hungry_bug");
            cv.chest(in.x1(), y + 1, in.z0() + 4, Direction.WEST, "hungry_bug");
            cv.set(in.x0(), y + 1, in.z0() + 3, Blocks.CAULDRON);
            cv.set(in.x1() - 1, y + 2, counterZ - 1, Blocks.CAKE);
            for (int x = in.x0() + 2; x <= in.x1() - 2; x += 6) {
                if (Math.abs(x - in.centerX()) <= 1) {
                    continue;
                }
                table(cv, x, y + 1, in.z1() - 3);
                Tower.lamp(cv, x, y, in.z1() - 3);
            }
            Tower.lamp(cv, in.centerX(), y, in.z0() + 3);
        }, k -> false);
        // The open-all-hours storefront: glass across the plaza side, a yellow sign band, and a green awning.
        int front = box.z1();
        for (int x = box.x0() + 2; x <= box.x1() - 2; x++) {
            if (Math.abs(x - box.centerX()) > 1) {
                c.pane(x, GROUND_Y + 1, front, Direction.Axis.X, Blocks.GLASS_PANE);
                c.pane(x, GROUND_Y + 2, front, Direction.Axis.X, Blocks.GLASS_PANE);
            }
        }
        c.fill(box.x0() + 1, GROUND_Y + 4, front, box.x1() - 1, GROUND_Y + 5, front, Blocks.YELLOW_TERRACOTTA);
        c.sign(box.centerX() - 1, GROUND_Y + 5, front + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "HUNGRY", "BUG");
        c.sign(box.centerX() + 1, GROUND_Y + 5, front + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN, "GYOZA", "24 OPEN");
        c.fill(box.x0() + 1, GROUND_Y + 3, front + 1, box.x1() - 1, GROUND_Y + 3, front + 1, Blocks.GREEN_WOOL);
    }

    private static void table(Canvas c, int x, int y, int z) {
        c.set(x, y, z, Blocks.SPRUCE_FENCE);
        c.set(x, y + 1, z, Blocks.SPRUCE_PRESSURE_PLATE);
        c.stair(x - 1, y, z, Direction.WEST, Blocks.SPRUCE_STAIRS);
        c.stair(x + 1, y, z, Direction.EAST, Blocks.SPRUCE_STAIRS);
    }

    /** Free beds and basics beside the plaza, so a first night is survivable on every seed. */
    static void starterShelter(Canvas c, Lot lot) {
        Style style = Style.BRICK;
        Tower.build(c, lot.rect(), 2, style, lot.hash(), Tower.Roof.GARDEN, (cv, k, in, y) -> {
            for (int z = in.z0() + 3; z <= in.z1() - 3; z += 2) {
                cv.bed(in.x1() - 1, y + 1, z, Direction.EAST, Blocks.BROWN_BED);
            }
            cv.chest(in.x0(), y + 1, in.z0() + 3, Direction.EAST, "starter_cache");
            cv.set(in.x0(), y + 1, in.z0() + 4, Blocks.CRAFTING_TABLE);
            cv.facing(in.x0(), y + 1, in.z0() + 5, Direction.EAST, Blocks.FURNACE);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
            Tower.lamp(cv, in.centerX(), y, in.z0() + 3);
        }, k -> false);
    }

    static void hospital(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(8);
        c.fill(lot.rect(), STREET_Y, STREET_Y, Mix.of(Blocks.SMOOTH_STONE, 3, Blocks.LIGHT_GRAY_CONCRETE, 2));
        int floors = 5 + CityHash.range(lot.hash(), 3);
        Tower.build(c, box, floors, Style.CLEAN, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> {
            LotPainter.ward(cv, k, in, y);
            cv.chest(in.x1(), y + 1, in.z0() + 4, Direction.WEST, "hospital");
            for (int x = in.x0() + 6; x <= in.x1() - 6; x += 6) {
                Tower.lamp(cv, x, y, in.z0() + 5);
            }
        }, k -> false);
        int roof = STREET_Y + floors * Tower.STOREY + 1;
        LotPainter.redCross(c, box.centerX(), roof + 3, box.z1());
        c.fill(box.centerX(), roof, box.z1(), box.centerX(), roof + 1, box.z1(), Blocks.RED_CONCRETE);
    }

    static void departmentStore(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(10);
        c.fill(lot.rect(), STREET_Y, STREET_Y, Mix.of(Blocks.POLISHED_ANDESITE, 3, Blocks.SMOOTH_STONE, 2));
        int floors = 6;
        Tower.build(c, box, floors, Style.CONCRETE, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            // Sales floors: aisles of stock, kept off the stair corridor and the entrance.
            for (int x = in.x0() + 3; x <= in.x1() - 2; x += 4) {
                if (Math.abs(x - in.centerX()) <= 2) {
                    continue;
                }
                for (int z = in.z0() + 4; z <= in.z1() - 3; z += 2) {
                    cv.barrel(x, y + 1, z, "department");
                }
            }
            for (int x = in.x0() + 4; x <= in.x1() - 3; x += 8) {
                for (int z = in.z0() + 4; z <= in.z1() - 3; z += 8) {
                    Tower.lamp(cv, x, y, z);
                }
            }
        }, k -> false);
        c.fill(box.x0() + 1, STREET_Y + floors * Tower.STOREY - 1, box.z1(), box.x1() - 1, STREET_Y + floors * Tower.STOREY - 1, box.z1(), Blocks.RED_TERRACOTTA);
    }

    static void researchManor(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect box = r.inset(Math.max(4, Math.min(r.width(), r.depth()) / 2 - 14));
        Style style = Style.MANOR;
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.PODZOL, 3, Blocks.COARSE_DIRT, 2, Blocks.MOSS_BLOCK, 1));
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y + 1, fence.z1(), Blocks.AIR);
        Tower.build(c, box, 3, style, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            if (k == 0) {
                for (int x = in.x0() + 6; x <= in.x1() - 2; x += 4) {
                    cv.set(x, y + 1, in.z0() + 3, Blocks.BREWING_STAND);
                    cv.chest(x, y + 1, in.z0() + 4, Direction.SOUTH, "lab");
                }
                Tower.lamp(cv, in.centerX(), y, in.centerZ());
            } else {
                LotPainter.dwelling(cv, style, k, in, y);
            }
        }, k -> false);
    }

    /** A bowl of dark water ringed by refuse. Kept shallow enough to clear the sewers, and sealed against leaks. */
    static void lakeOfRefuse(Canvas c, Lot lot) {
        Rect r = lot.rect();
        int cx = r.centerX();
        int cz = r.centerZ();
        double rx = r.width() / 2.0 - 8.0;
        double rz = r.depth() / 2.0 - 8.0;
        Mix mud = Mix.of(Blocks.MUD, 3, Blocks.SOUL_SOIL, 2, Blocks.CLAY, 1);
        Mix rim = Mix.of(ModBlocks.INDUSTRIAL_DEBRIS.get(), 4, Blocks.BONE_BLOCK, 1, Blocks.SOUL_SOIL, 2, Blocks.GRAVEL, 2);
        for (int x = Math.max(r.x0(), c.minX); x <= Math.min(r.x1(), c.maxX); x++) {
            for (int z = Math.max(r.z0(), c.minZ); z <= Math.min(r.z1(), c.maxZ); z++) {
                double dx = (x - cx) / rx;
                double dz = (z - cz) / rz;
                double d = dx * dx + dz * dz;
                if (d < 1.0) {
                    int depth = 2 + (int) Math.round((1.0 - d) * 4.0);
                    int bed = STREET_Y - depth;
                    c.fill(x, bed - 1, z, x, bed, z, mud);
                    c.fill(x, bed + 1, z, x, STREET_Y - 1, z, Blocks.WATER);
                    c.set(x, STREET_Y, z, Blocks.AIR);
                } else if (d < 1.35) {
                    c.fill(x, STREET_Y - 1, z, x, STREET_Y, z, rim);
                    int mound = CityHash.range(c.hash(x, 0, z), 3);
                    if (mound > 0) {
                        c.fill(x, GROUND_Y, z, x, GROUND_Y + mound - 1, z, rim);
                    }
                }
            }
        }
        c.sign(r.x0() + 1, GROUND_Y, r.z1() - 1, Direction.SOUTH, Blocks.DARK_OAK_WALL_SIGN, "KEEP OUT", "LAKE OF", "REFUSE");
    }
}
