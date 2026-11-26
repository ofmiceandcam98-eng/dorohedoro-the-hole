package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 43: occupied rather than repeating empty civic space. Uses real vanilla
 * workstations and storage, but no loot chests or artificial street lights.
 * All fixtures sit in the outer service strips; walk-through aisles and the
 * ten-block radius around the starting spawn square remain unobstructed.
 */
final class OccupiedCommons43 {
    private static final Block[] ROOFS = {Blocks.BROWN_WOOL, Blocks.GRAY_WOOL,
            Blocks.GREEN_WOOL, Blocks.ORANGE_WOOL, Blocks.CYAN_WOOL};
    private OccupiedCommons43() {}

    static void plaza(Canvas c, Lot lot) {
        Rect r = lot.rect();
        // Connected, irregular repairs, not pixel-noise and not obstacles.
        for (int x = Math.max(c.minX, r.x0() + 1); x <= Math.min(c.maxX, r.x1() - 1); x++) {
            for (int z = Math.max(c.minZ, r.z0() + 1); z <= Math.min(c.maxZ, r.z1() - 1); z++) {
                long section = CityHash.of(lot.hash() ^ 0x43A7L, Math.floorDiv(x, 5), Math.floorDiv(z, 7));
                long detail = CityHash.of(lot.hash(), x, z);
                if (CityHash.range(section, 9) <= 2 && CityHash.range(detail, 5) != 0) {
                    // Keep the recognizable central landing unaltered for new spawns.
                    if (Math.max(Math.abs(x - r.centerX()), Math.abs(z - r.centerZ())) > 8) {
                        c.set(x, STREET_Y, z, CityHash.range(detail >>> 13, 5) == 0
                                ? Blocks.PACKED_MUD : Blocks.CRACKED_STONE_BRICKS);
                    }
                }
            }
        }
        // Outdoor shared laundry/water and repair points against the western
        // edge, away from starter supplies on the north rim and from lamps.
        int west = r.x0() + 2, north = r.z0() + 6;
        if (r.width() >= 25 && r.depth() >= 25) {
            c.set(west, GROUND_Y, north, Blocks.CAULDRON);
            c.set(west, GROUND_Y, north + 2, Blocks.COMPOSTER);
            c.set(west, GROUND_Y, north + 4, Blocks.STONECUTTER);
            c.set(west + 1, GROUND_Y, north, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST));
        }
    }

    /** Nonuniform market frontage: 3-block+ clear lanes, missing bays and real job sites. */
    static void market(Canvas c, Lot lot) {
        Rect r = lot.rect();
        // Stalls have irregular pitches, but every pitch leaves a 3-4 wide
        // continuous alley. No stalls are placed over the lot boundary.
        int row = 0;
        for (int z = r.z0() + 2; z + 3 <= r.z1() - 2; z += 9, row++) {
            int col = 0;
            for (int bx = r.x0() + 2; bx + 5 <= r.x1() - 2; bx += 10, col++) {
                long h = CityHash.of(lot.hash(), row * 997L, col);
                if (CityHash.range(h >>> 9, 7) == 0) {
                    // A delivery/loading bay, not another identical gazebo.
                    if (col > 0 && CityHash.range(h >>> 21, 2) == 0) {
                        c.set(bx, GROUND_Y, z + 1, Blocks.COMPOSTER);
                    }
                    continue;
                }
                int x = bx + CityHash.range(h >>> 12, 2);
                int width = 3 + CityHash.range(h >>> 28, 3);
                int depth = 2 + CityHash.range(h >>> 36, 2);
                int back = z + depth;
                int end = x + width;
                if (end >= r.x1() - 1 || back >= r.z1() - 1) continue;
                Block cloth = ROOFS[CityHash.range(h, ROOFS.length)];
                Block frame = CityHash.range(h >>> 19, 3) == 0 ? Blocks.DARK_OAK_FENCE : Blocks.SPRUCE_FENCE;
                int top = GROUND_Y + 3 + CityHash.range(h >>> 44, 2);
                for (int pz : new int[]{z, back}) {
                    for (int px : new int[]{x, end}) {
                        c.fill(px, GROUND_Y, pz, px, top - 1, pz, frame);
                    }
                }
                for (int px = x; px <= end; px++) {
                    for (int pz = z; pz <= back; pz++) {
                        // Roof repair patches still give each vendor a continuous canopy.
                        c.set(px, top, pz, (px == end && CityHash.range(h >>> 51, 3) == 0)
                                ? Blocks.GRAY_WOOL : cloth);
                    }
                }
                // A real workstation and useful goods define the stall's purpose.
                int type = CityHash.range(h >>> 5, 5);
                Block station = switch (type) {
                    case 0 -> Blocks.SMOKER;        // cooked food / gyoza vendor
                    case 1 -> Blocks.LOOM;          // textile repair
                    case 2 -> Blocks.STONECUTTER;   // masonry and scrap processing
                    case 3 -> Blocks.FLETCHING_TABLE; // salvage trade
                    default -> Blocks.COMPOSTER;   // produce stand
                };
                c.set(x + 1, GROUND_Y, z + 1, station);
                Block stock = switch (type) {
                    case 0 -> Blocks.DRIED_KELP_BLOCK;
                    case 1 -> Blocks.WHITE_WOOL;
                    case 2 -> Blocks.COBBLED_DEEPSLATE;
                    case 3 -> Blocks.STRIPPED_SPRUCE_LOG;
                    default -> Blocks.PUMPKIN;
                };
                c.set(x + 2, GROUND_Y, z + 1, stock);
                // Only every fourth merchant has proper protected supplies;
                // use barrels rather than a street of exposed loot chests.
                if (width >= 4 && CityHash.range(h >>> 24, 4) == 0) {
                    c.barrel(end - 1, GROUND_Y, back - 1, "market");
                }
            }
        }
    }
}
