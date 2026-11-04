package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Pass 68: rare, actual walk-in stormwater pumping galleries off municipal trunks.
 *
 * <p>The open municipal lane at offsets 0..2 is NEVER changed. The pump hall has
 * one public three-wide opening, an open wet treatment hall with a safe crossing,
 * a practical repair bench, and a separate operator booth whose vanilla iron door
 * is controlled by a floor lever on the accessible side. The booth is optional;
 * nothing on the progression path requires a technique, key or special item.
 *
 * <p>Generation decisions are independent of chunk order, using immutable seed,
 * axis, trunk index and absolute world-coordinate sections. Each neighboring
 * chunk queries the same wide intersection exclusion margin.
 */
final class StormwaterPumpHalls68 {
    static final int FIRST_VERSION = 50;
    private static final int SECTION = 192;
    private static final int HALF_LENGTH = 9;
    private static final int REACH = 16;
    private static final int MIN_JUNCTION_DISTANCE = 48;
    private static final long SALT = 0x5E7E6801L;

    private StormwaterPumpHalls68() {}

    /** 1/4 of long municipal trunk sections are offered a station site. */
    static boolean selected(long seed, boolean xAxis, int trunkIndex, int section, int version) {
        return version >= FIRST_VERSION
                && CityHash.range(CityHash.of(seed, SALT + (xAxis ? 1 : 2), trunkIndex, section), 4) == 0;
    }

    static int center(long seed, boolean xAxis, int trunkIndex, int section) {
        return section * SECTION + 48
                + CityHash.range(CityHash.of(seed, SALT + (xAxis ? 3 : 4), trunkIndex, section), 96);
    }

    static int side(long seed, boolean xAxis, int trunkIndex, int section) {
        return CityHash.range(CityHash.of(seed, SALT + (xAxis ? 5 : 6), trunkIndex, section), 2) == 0 ? -1 : 1;
    }

    /**
     * Keep Pass67 service bays out of this entire 19-long hall envelope.
     * Conservatively reserve the space even when a pump site is later rejected
     * near a junction or coast: rejection is always chunk-independent.
     */
    static boolean reservesServiceBay(long seed, boolean xAxis, int trunkIndex, int along, int version) {
        if (version < FIRST_VERSION) return false;
        int from = Math.floorDiv(along - HALF_LENGTH - 4, SECTION);
        int to = Math.floorDiv(along + HALF_LENGTH + 4, SECTION);
        for (int section = from; section <= to; section++) {
            if (selected(seed, xAxis, trunkIndex, section, version)
                    && Math.abs(along - center(seed, xAxis, trunkIndex, section)) <= HALF_LENGTH + 4) {
                return true;
            }
        }
        return false;
    }

    static void paint(Canvas c, CityPlan plan) {
        if (plan.settings.worldDetailVersion() < FIRST_VERSION || !plan.settings.sewerUnderworld()) return;
        List<Line> xTrunks = plan.xLinesNear(c.minX - REACH - 1, c.maxX + REACH + 1);
        List<Line> zTrunks = plan.zLinesNear(c.minZ - REACH - 1, c.maxZ + REACH + 1);
        // A crossing 48 blocks from the *station center* must be visible
        // to ALL chunks of its footprint, including at the far end of the hall.
        int crossingReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH + 1;
        List<Line> xCrossing = plan.xLinesNear(c.minX - crossingReach, c.maxX + crossingReach);
        List<Line> zCrossing = plan.zLinesNear(c.minZ - crossingReach, c.maxZ + crossingReach);
        paintAxis(c, plan, true, xTrunks, zCrossing);
        paintAxis(c, plan, false, zTrunks, xCrossing);
    }

    private static void paintAxis(Canvas c, CityPlan plan, boolean xAxis,
                                  List<Line> trunks, List<Line> crossing) {
        int minAlong = xAxis ? c.minZ : c.minX;
        int maxAlong = xAxis ? c.maxZ : c.maxX;
        int minSection = Math.floorDiv(minAlong - HALF_LENGTH, SECTION);
        int maxSection = Math.floorDiv(maxAlong + HALF_LENGTH, SECTION);
        for (Line trunk : trunks) {
            if (trunk.kind() == Line.Kind.CANAL) continue;
            for (int section = minSection; section <= maxSection; section++) {
                if (!selected(plan.seed, xAxis, trunk.index(), section, plan.settings.worldDetailVersion())) continue;
                int along = center(plan.seed, xAxis, trunk.index(), section);
                if (crossing.stream().anyMatch(line ->
                        Math.abs(along - line.center()) < MIN_JUNCTION_DISTANCE)) continue;
                int side = side(plan.seed, xAxis, trunk.index(), section);
                Rect footprint = footprint(xAxis, trunk.center(), along, side);
                if (!c.overlaps(footprint) || reservedCoast(plan, footprint)) continue;
                build(c, xAxis, trunk.center(), along, side, plan.settings.worldDetailVersion());
            }
        }
    }

    private static Rect footprint(boolean xAxis, int trunk, int along, int side) {
        return xAxis
                ? new Rect(Math.min(trunk + side * 3, trunk + side * REACH), along - HALF_LENGTH,
                           Math.max(trunk + side * 3, trunk + side * REACH), along + HALF_LENGTH)
                : new Rect(along - HALF_LENGTH, Math.min(trunk + side * 3, trunk + side * REACH),
                           along + HALF_LENGTH, Math.max(trunk + side * 3, trunk + side * REACH));
    }

    private static boolean reservedCoast(CityPlan plan, Rect footprint) {
        // Unlike checking just the midpoint, this rejects a room if even one
        // corner of its whole footprint enters the ocean or its dry beach shelf.
        for (int x = footprint.x0(); x <= footprint.x1(); x++) {
            for (int z = footprint.z0(); z <= footprint.z1(); z++) {
                if (plan.oceanDepthAt(x, z) > 0 || plan.coastalDistanceAt(x, z) > 0) return true;
            }
        }
        return false;
    }

    private static void build(Canvas c, boolean xAxis, int trunk, int along, int side, int worldDetailVersion) {
        for (int distance = 3; distance <= REACH; distance++) {
            for (int offset = -HALF_LENGTH; offset <= HALF_LENGTH; offset++) {
                int x = x(xAxis, trunk, along, side, distance, offset);
                int z = z(xAxis, trunk, along, side, distance, offset);
                if (!c.contains(x, z)) continue;
                boolean doorway = distance <= 4 && Math.abs(offset) <= 1;
                if (distance == 3 && !doorway) continue; // leave existing sewer masonry
                if (distance == 3 && doorway) {
                    c.set(x, 49, z, Blocks.POLISHED_ANDESITE);
                    c.fill(x, 50, z, x, 52, z, Blocks.AIR);
                    continue;
                }
                boolean outside = distance == REACH || Math.abs(offset) == HALF_LENGTH;
                boolean boothDivider = offset == 5 && distance >= 6 && distance < REACH;
                boolean boothDoor = boothDivider && distance == 8;
                boolean pane = boothDivider && distance == 12;
                c.fill(x, 48, z, x, 56, z, Blocks.STONE_BRICKS);
                if (!outside && !boothDivider) {
                    c.set(x, 49, z, Blocks.DEEPSLATE_TILES);
                    c.fill(x, 50, z, x, 55, z, Blocks.AIR);
                } else if (boothDoor) {
                    c.set(x, 49, z, Blocks.POLISHED_ANDESITE);
                    c.fill(x, 50, z, x, 52, z, Blocks.AIR);
                } else if (pane) {
                    c.set(x, 52, z, Blocks.IRON_BARS);
                }
                // Slightly higher covered hall than the narrow arched sewer,
                // with weathered ironwork placed outside the head-clear path.
                if (outside && distance == REACH && Math.abs(offset) == 6) {
                    c.set(x, 52, z, Blocks.IRON_BARS);
                }
            }
        }
        // Recessed, enclosed 1-deep sump: keep 1-wide central crossing dry.
        for (int distance = 11; distance <= 13; distance++) {
            for (int offset = -2; offset <= 2; offset++) {
                int x = x(xAxis, trunk, along, side, distance, offset);
                int z = z(xAxis, trunk, along, side, distance, offset);
                c.set(x, 48, z, Blocks.POLISHED_TUFF);
                c.set(x, 49, z, offset == 0 ? Blocks.POLISHED_ANDESITE : Blocks.WATER);
                c.fill(x, 50, z, x, 54, z, Blocks.AIR);
            }
        }
        // Repair bench lies off the dry route; all three vanilla workstations work.
        fixture(c, xAxis, trunk, along, side, 10, -7, Blocks.CRAFTING_TABLE);
        fixture(c, xAxis, trunk, along, side, 12, -7, Blocks.FURNACE);
        fixture(c, xAxis, trunk, along, side, 14, -7, Blocks.STONECUTTER);
        // A distinct optional operator booth, without treasure or technique locks.
        fixture(c, xAxis, trunk, along, side, 12, 7, Blocks.CAULDRON);
        fixture(c, xAxis, trunk, along, side, 14, 7, Blocks.GRINDSTONE);
        // Pass69: a separate, mechanically operable one-block overflow channel.
        // It uses vanilla sticky-piston motion; older saves retain the Pass68 sump.
        if (worldDetailVersion >= SluiceValve69.FIRST_VERSION) {
            SluiceValve69.build(c, xAxis, trunk, along, side);
        }
        // Iron access door opens with a nearby vanilla floor lever.
        int doorX = x(xAxis, trunk, along, side, 8, 5);
        int doorZ = z(xAxis, trunk, along, side, 8, 5);
        Direction facing = xAxis ? Direction.NORTH : Direction.WEST;
        c.set(doorX, 50, doorZ, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        c.set(doorX, 51, doorZ, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        int leverX = x(xAxis, trunk, along, side, 8, 4);
        int leverZ = z(xAxis, trunk, along, side, 8, 4);
        c.set(leverX, 50, leverZ, Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.FLOOR));
    }

    private static int x(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? trunk + side * distance : along + offset;
    }

    private static int z(boolean axis, int trunk, int along, int side, int distance, int offset) {
        return axis ? along + offset : trunk + side * distance;
    }

    private static void fixture(Canvas c, boolean axis, int trunk, int along, int side,
                                int distance, int offset, net.minecraft.world.level.block.Block block) {
        c.set(x(axis, trunk, along, side, distance, offset), 50,
                z(axis, trunk, along, side, distance, offset), block);
    }
}
