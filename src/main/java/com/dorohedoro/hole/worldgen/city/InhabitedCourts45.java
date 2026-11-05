package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 45: a small number of *occupied* public service courts in central-city
 * vacant and damaged parcels. They replace a selected EMPTY lot rather than
 * clobbering an existing tree, residence, clinic or landmark. Two accessible
 * rooms have different jobs, with a continuous public approach to each door.
 *
 * This is physical world structure and usable vanilla workstations, not a new
 * villager profession, decorative chest cache or always-on lighting fixture.
 * Version-gated and deterministic from the parcel; no cross-chunk reads.
 */
final class InhabitedCourts45 {
    private static final long SALT = 0x45C017A5L;
    private InhabitedCourts45() {}

    static boolean eligible(Canvas c, Lot lot, boolean rubble) {
        if (!c.hasInhabitedCourts45()) return false;
        Rect r = lot.rect();
        if (r.width() < 19 || r.depth() < 19) return false;
        if (!c.isHoleCity(r.centerX(), r.centerZ())) return false;
        if (lot.district() == District.OUTSKIRTS || lot.district() == District.GRAVEYARD) return false;
        // Preserve most vacant/ruined places for wood, wildlife, exploration and
        // dark hostile-spawn pockets; stricter in severely derelict districts.
        int probability = lot.district() == District.DERELICT ? 8 : rubble ? 7 : 5;
        return CityHash.range(CityHash.of(lot.hash(), SALT), probability) == 0;
    }

    static void paint(Canvas c, Lot lot, boolean damaged) {
        Rect r = lot.rect();
        long h = CityHash.of(lot.hash(), SALT);
        boolean industrial = lot.district() == District.INDUSTRIAL || lot.district() == District.WATERFRONT;
        boolean market = lot.district() == District.MARKET;
        // Keep a generous open court (>= 7 blocks) to the SOUTH of the structure,
        // preserving paths from the street and natural mob spawning outdoors.
        int width = Math.min(16, r.width() - 4);
        int x0 = r.x0() + 2 + CityHash.range(h >>> 11, Math.max(1, r.width() - width - 3));
        int z0 = r.z0() + 2 + CityHash.range(h >>> 19, 2);
        Rect shed = new Rect(x0, z0, x0 + width - 1, z0 + 7);
        int partition = shed.centerX();
        int leftDoor = (shed.x0() + partition) / 2;
        int rightDoor = (partition + shed.x1()) / 2 + 1;
        // Explicit approach coordinates. Both routes join the open frontage,
        // not a dead end, and furniture never occupies either center aisle.
        int courtyardEnd = r.z1() - 1;
        Block paving = industrial ? Blocks.POLISHED_ANDESITE : market ? Blocks.MUD_BRICKS : Blocks.CRACKED_STONE_BRICKS;
        c.fill(r, STREET_Y, STREET_Y, Blocks.PACKED_MUD);
        for (int z = shed.z1() + 1; z <= courtyardEnd; z++) {
            for (int px : new int[]{leftDoor, rightDoor}) {
                c.set(px, STREET_Y, z, paving);
                c.set(px + 1, STREET_Y, z, paving);
            }
        }
        Block facade = damaged ? Blocks.CRACKED_STONE_BRICKS
                : industrial ? Blocks.MUD_BRICKS : market ? Blocks.BRICKS : Blocks.TUFF_BRICKS;
        Block roof = industrial ? Blocks.WAXED_WEATHERED_CUT_COPPER
                : damaged ? Blocks.COBBLED_DEEPSLATE : Blocks.WAXED_OXIDIZED_CUT_COPPER;
        int top = GROUND_Y + 3;
        c.walls(shed, GROUND_Y, top, Mix.of(facade, 4, Blocks.CRACKED_STONE_BRICKS, 1));
        c.fill(shed.inset(1), GROUND_Y, top, Blocks.AIR);
        c.fill(partition, GROUND_Y, shed.z0() + 1, partition, top, shed.z1() - 1, facade);
        // True separated rooms: working supplies on opposite sides of the wall.
        // Both have separate SOUTH doors from the reachable courtyard.
        c.fill(shed, top + 1, top + 1, roof);
        // A single higher loft bay gives the yard an uneven but coherent skyline;
        // the lower roof remains supported by the other walls and partition.
        if (!damaged && CityHash.range(h >>> 28, 3) != 0) {
            c.fill(partition + 1, top + 2, shed.z0(), shed.x1(), top + 2, shed.z1(), roof);
        }
        c.door(leftDoor, GROUND_Y, shed.z1(), Direction.NORTH, Blocks.SPRUCE_DOOR);
        c.door(rightDoor, GROUND_Y, shed.z1(), Direction.NORTH, Blocks.SPRUCE_DOOR);
        // Workshop / food service bay on the west, wash + household or salvage
        // processing on the east. These blocks are usable in vanilla survival.
        c.set(shed.x0() + 2, GROUND_Y, shed.z0() + 2,
                market ? Blocks.SMOKER : industrial ? Blocks.SMITHING_TABLE : Blocks.CRAFTING_TABLE);
        c.set(shed.x0() + 2, GROUND_Y, shed.z0() + 4,
                industrial ? Blocks.GRINDSTONE : market ? Blocks.COMPOSTER : Blocks.LOOM);
        c.set(shed.x1() - 2, GROUND_Y, shed.z0() + 2, Blocks.CAULDRON);
        c.set(shed.x1() - 2, GROUND_Y, shed.z0() + 4,
                industrial ? Blocks.STONECUTTER : Blocks.FURNACE);
        // Accessible indoor stores, not exposed public treasure chests.
        if (CityHash.range(h >>> 42, 3) == 0) {
            c.barrel(shed.x0() + 1, GROUND_Y, shed.z0() + 1, industrial ? "scrap" : "apartment");
        }
        // The surrounding court is otherwise empty and usable as a spawn-safe
        // (unlit) passage, but the corner holds one real collection point.
        int serviceX = r.x1() - 2;
        int serviceZ = r.z1() - 3;
        if (serviceX > shed.x1() + 1 || serviceZ > shed.z1() + 3) {
            c.set(serviceX, GROUND_Y, serviceZ, industrial ? Blocks.COMPOSTER : Blocks.CAULDRON);
        }
    }
}
