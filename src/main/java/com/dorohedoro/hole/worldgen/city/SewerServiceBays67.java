package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 67: occasional inhabited-service-scale volumes along the municipal sewer,
 * between the large cisterns/dungeons and the ordinary uninterrupted tunnels.
 *
 * <p>Small repair and filter bays are actual walk-in masonry rooms with useful
 * vanilla workstations. They are open to the dry sewer walkway but never occupy
 * the central drain or the continuous dry walking lane at offset 2. The program
 * does not add lights, mobs, treasure containers, keys or technique dependencies.
 *
 * <p>Placement uses only seed, trunk index, axis and absolute 64-block section.
 * Both the placement and each block coordinate are independent of chunk order.
 */
final class SewerServiceBays67 {
    static final int FIRST_VERSION = 49;
    private static final int SECTION = 64;
    private static final int REACH = 10;
    private static final int HALF_LENGTH = 4;
    private static final int MIN_JUNCTION_DISTANCE = 39;
    private static final long SALT = 0x5E7E6701L;

    private SewerServiceBays67() {}

    /** 0 = ordinary uninterrupted wall, 1 = repair bay, 2 = filtration bay. */
    static int kind(long seed, boolean xAxis, int trunkIndex, int section, int version) {
        if (version < FIRST_VERSION) return 0;
        long key = CityHash.of(seed, SALT + (xAxis ? 1 : 2), trunkIndex, section);
        return CityHash.range(key, 3) == 0 ? 1 + CityHash.range(CityHash.mix(key), 2) : 0;
    }

    /** The room lies wholly within its own 64-block section and is seed-offset. */
    static int center(long seed, boolean xAxis, int trunkIndex, int section) {
        long key = CityHash.of(seed, SALT + (xAxis ? 3 : 4), trunkIndex, section);
        return section * SECTION + 16 + CityHash.range(key, 32);
    }

    static int side(long seed, boolean xAxis, int trunkIndex, int section) {
        return CityHash.range(CityHash.of(seed, SALT + (xAxis ? 5 : 6), trunkIndex, section), 2) == 0 ? -1 : 1;
    }

    static void paint(Canvas c, CityPlan plan) {
        if (plan.settings.worldDetailVersion() < FIRST_VERSION || !plan.settings.sewerUnderworld()) return;
        List<Line> xTrunks = plan.xLinesNear(c.minX - REACH - 1, c.maxX + REACH + 1);
        List<Line> zTrunks = plan.zLinesNear(c.minZ - REACH - 1, c.maxZ + REACH + 1);
        // Every chunk overlapping one bay must see the SAME nearby junctions.
        // A corridor line outside the room footprint can still exclude that site;
        // querying only the current 16-block chunk would create boundary seams.
        int crossReach = MIN_JUNCTION_DISTANCE + HALF_LENGTH;
        List<Line> xCrossing = plan.xLinesNear(c.minX - crossReach, c.maxX + crossReach);
        List<Line> zCrossing = plan.zLinesNear(c.minZ - crossReach, c.maxZ + crossReach);
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
                int kind = kind(plan.seed, xAxis, trunk.index(), section, plan.settings.worldDetailVersion());
                if (kind == 0) continue;
                int along = center(plan.seed, xAxis, trunk.index(), section);
                // A larger Pass68 station owns this segment. Do not let a small bay
                // occupy its walls or operator booth, regardless of chunk order.
                if (StormwaterPumpHalls68.reservesServiceBay(plan.seed, xAxis, trunk.index(),
                        along, plan.settings.worldDetailVersion())) continue;
                int side = side(plan.seed, xAxis, trunk.index(), section);
                // Keep junction shafts, big cisterns, cave exits and dungeon wings untouched.
                if (crossing.stream().anyMatch(line -> Math.abs(along - line.center()) < MIN_JUNCTION_DISTANCE)) {
                    continue;
                }
                int targetX = xAxis ? trunk.center() + side * 8 : along;
                int targetZ = xAxis ? along : trunk.center() + side * 8;
                // Coast/ocean reservation exists only in the newer worlds; do not excavate
                // an underground workshop under a coastal basin or on its dry shelf.
                if (plan.oceanDepthAt(targetX, targetZ) > 0 || plan.coastalDistanceAt(targetX, targetZ) > 0) {
                    continue;
                }
                Rect footprint = xAxis
                        ? new Rect(trunk.center() - REACH, along - HALF_LENGTH,
                                trunk.center() + REACH, along + HALF_LENGTH)
                        : new Rect(along - HALF_LENGTH, trunk.center() - REACH,
                                along + HALF_LENGTH, trunk.center() + REACH);
                if (!c.overlaps(footprint)) continue;
                build(c, xAxis, trunk.center(), along, side, kind);
            }
        }
    }

    private static void build(Canvas c, boolean xAxis, int trunk, int along, int side, int kind) {
        // Existing sewer shell is at offset 3-4. Only pierce it at a three-wide doorway;
        // the lane at offset 2 and the wastewater channel at offsets 0/1 are untouched.
        for (int distance = 3; distance <= REACH; distance++) {
            for (int offset = -HALF_LENGTH; offset <= HALF_LENGTH; offset++) {
                int x = xAxis ? trunk + side * distance : along + offset;
                int z = xAxis ? along + offset : trunk + side * distance;
                if (!c.contains(x, z)) continue;
                boolean doorway = distance <= 4 && Math.abs(offset) <= 1;
                boolean wall = distance == REACH || Math.abs(offset) == HALF_LENGTH;
                if (distance == 3 && !doorway) continue; // keep original sewer wall/pipe texture
                if (distance == 3 && doorway) {
                    c.set(x, 49, z, Blocks.POLISHED_ANDESITE);
                    c.fill(x, 50, z, x, 52, z, Blocks.AIR);
                    continue; // top lip of the existing arched sewer remains intact
                }
                c.fill(x, 48, z, x, 55, z, kind == 1 ? Blocks.STONE_BRICKS : Blocks.TUFF_BRICKS);
                if (!wall) {
                    c.set(x, 49, z, kind == 1 ? Blocks.DEEPSLATE_TILES : Blocks.POLISHED_TUFF);
                    c.fill(x, 50, z, x, 53, z, Blocks.AIR);
                } else if (distance == REACH && Math.abs(offset) == 2) {
                    // Inset ventilation: visually communicates a serviced space without
                    // opening to solid rock or trapping the player.
                    c.set(x, 52, z, Blocks.IRON_BARS);
                }
            }
        }
        // Fixtures hug the back/side walls. The centered 3-wide entry and a
        // two-block walking approach stay accessible to vanilla players.
        if (kind == 1) {
            fixture(c, xAxis, trunk, along, side, 8, -2, Blocks.CRAFTING_TABLE);
            fixture(c, xAxis, trunk, along, side, 8, 2, Blocks.FURNACE);
            fixture(c, xAxis, trunk, along, side, 9, -2, Blocks.GRINDSTONE);
        } else {
            fixture(c, xAxis, trunk, along, side, 8, -2, Blocks.CAULDRON);
            fixture(c, xAxis, trunk, along, side, 8, 2, Blocks.COMPOSTER);
            fixture(c, xAxis, trunk, along, side, 9, 2, Blocks.STONECUTTER);
        }
    }

    private static void fixture(Canvas c, boolean xAxis, int trunk, int along, int side,
                                int distance, int offset, net.minecraft.world.level.block.Block block) {
        int x = xAxis ? trunk + side * distance : along + offset;
        int z = xAxis ? along + offset : trunk + side * distance;
        c.set(x, 50, z, block);
    }
}
