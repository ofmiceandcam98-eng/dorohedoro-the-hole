package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Pass 23: TWO additional ways to enter an EXISTING municipal sewer from selected
 * canal/river-like waterways: a dry quay maintenance hatch, and a flooded culvert
 * with an upward swim into a dry, waterproof ladder shaft. Neither makes a new
 * underground dimension or an isolated cave. Both join the existing sewer walkways.
 *
 * Rivers in the procedural Hole are currently represented by Line.Kind.CANAL;
 * unrelated vanilla river biome water is NOT scanned or modified by this painter.
 *
 * Every coordinate is derived from the world seed and arterial indices. Canvas
 * clips writes to the current chunk. A saved generator version gate (>=3) prevents
 * changes to existing version 0/1/2 worlds as they generate new chunks.
 */
public final class WaterwaySewerEntrances23 {
    private static final long SALT = 0x56A7E231L;
    private static final int FLOOR = 49;
    private static final int SURFACE = 64;
    private static Block lining() { return ModBlocks.SEWER_BRICKWORK.get(); }

    private WaterwaySewerEntrances23() {}

    /** 0: no access; 1: both bank hatch and submerged inlet, with different banks. */
    public static int selected(long seed, int xIndex, int zIndex, boolean xCanal, boolean zCanal) {
        if (xCanal == zCanal) return 0; // exactly one canal, and one cross-street
        return CityHash.range(CityHash.of(seed, SALT, xIndex, zIndex), 3) == 0 ? 1 : 0;
    }

    public static void paint(Canvas c, CityPlan plan) {
        List<Line> xLines = plan.xLinesNear(c.minX - 44, c.maxX + 44);
        List<Line> zLines = plan.zLinesNear(c.minZ - 44, c.maxZ + 44);
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                boolean xCanal = xLine.kind() == Line.Kind.CANAL;
                boolean zCanal = zLine.kind() == Line.Kind.CANAL;
                if (selected(plan.seed, xLine.index(), zLine.index(), xCanal, zCanal) == 0) continue;
                int bank = CityHash.range(CityHash.of(plan.seed, SALT + 1, xLine.index(), zLine.index()), 2) == 0 ? 1 : -1;
                // Dry and wet accesses are separated along the river by 48 blocks,
                // and sited on opposite banks so entrances are visually distinct.
                Site quay = new Site(xLine.center(), zLine.center(), xCanal, bank, 24);
                Site culvert = new Site(xLine.center(), zLine.center(), xCanal, -bank, -24);
                if (quay.intersects(c)) dryHatch(c, quay);
                if (culvert.intersects(c)) floodedCulvert(c, culvert);
            }
        }
    }

    /** World coordinates: r=distance outward from canal midline, t=along its flow. */
    private record Site(int crossingX, int crossingZ, boolean xCanal, int bank, int along) {
        int x(int r, int t) { return xCanal ? crossingX + bank*r : crossingX + along + t; }
        int z(int r, int t) { return xCanal ? crossingZ + along + t : crossingZ + bank*r; }
        boolean intersects(Canvas c) {
            Rect rect = new Rect(Math.min(x(1, -3), x(10, 3)),
                    Math.min(z(1, -3), z(10, 3)),
                    Math.max(x(1, -3), x(10, 3)),
                    Math.max(z(1, -3), z(10, 3)));
            return c.overlaps(rect);
        }
    }

    private static void block(Canvas c, Site s, int r, int t, int y, Block b) {
        c.set(s.x(r, t), y, s.z(r, t), b);
    }

    private static void range(Canvas c, Site s, int r0, int r1, int t0, int t1, int y0, int y1, Block b) {
        for (int r = r0; r <= r1; r++) for (int t = t0; t <= t1; t++) {
            c.fill(s.x(r,t), y0, s.z(r,t), s.x(r,t), y1, s.z(r,t), b);
        }
    }

    /** Dry, three-wide corridor opens onto the existing d=2/3 bank next to the sewage runnel. */
    private static void connection(Canvas c, Site s) {
        range(c, s, 2, 9, -2, 2, FLOOR - 1, FLOOR + 5, lining());
        range(c, s, 2, 8, -1, 1, FLOOR + 1, FLOOR + 4, Blocks.AIR);
        range(c, s, 2, 8, -1, 1, FLOOR, FLOOR, Blocks.POLISHED_DEEPSLATE);
        // Distinguish a drainage channel from a generic mining corridor.
        for (int r=3; r<=7; r+=3) {
            block(c,s,r,-2,FLOOR+2, Blocks.WAXED_WEATHERED_COPPER);
            block(c,s,r,2,FLOOR+2, Blocks.WAXED_WEATHERED_COPPER);
        }
        // Do not block the central sewer's flowing runnel at r<=1.
    }

    private static void shaft(Canvas c, Site s, boolean hatch) {
        // 3x3 watertight shaft. A ladder is held by the outer masonry r=9;
        // its backing is available even beside flooded masonry at r=7.
        range(c,s,7,9,-1,1,FLOOR-1,SURFACE,lining());
        range(c,s,8,8,0,0,FLOOR+1,SURFACE-1,Blocks.AIR);
        Direction facing = s.xCanal
                ? (s.bank > 0 ? Direction.WEST : Direction.EAST)
                : (s.bank > 0 ? Direction.NORTH : Direction.SOUTH);
        for (int y=FLOOR+1; y<SURFACE; y++) {
            c.set(s.x(8,0),y,s.z(8,0),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
        }
        if (hatch) {
            // Trapdoor directly flush with pedestrian quay; no mandatory iron key.
            c.set(s.x(8,0),SURFACE,s.z(8,0),Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                    .setValue(TrapDoorBlock.HALF, Half.TOP));
        } else {
            // The underwater version deliberately has NO street hatch.
            block(c,s,8,0,SURFACE,Blocks.DEEPSLATE_TILES);
        }
        // Open the lower wall into the existing lined sewer connection.
        range(c,s,7,8,0,0,FLOOR+1,FLOOR+3,Blocks.AIR);
        // Ladder is retained at r=8, while r=7 becomes the entrance to the corridor.
        for(int y=FLOOR+1;y<=FLOOR+3;y++) {
            c.set(s.x(8,0),y,s.z(8,0),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
        }
    }

    private static void dryHatch(Canvas c, Site s) {
        connection(c,s);
        shaft(c,s,true);
        // Raised municipal access surround, copper stains and a visible lamp.
        for(int t : new int[]{-1,1}) {
            block(c,s,8,t,SURFACE,Blocks.POLISHED_DEEPSLATE);
            block(c,s,9,t,SURFACE+1,Blocks.IRON_BARS);
        }
        block(c,s,9,0,SURFACE,Blocks.WAXED_OXIDIZED_CUT_COPPER);
        block(c,s,9,2,SURFACE+1,Blocks.LANTERN);
        c.barrel(s.x(5,1), FLOOR+1, s.z(5,1), "sewer_maintenance");
    }

    private static void floodedCulvert(Canvas c, Site s) {
        connection(c,s);
        shaft(c,s,false);
        // A brick-lined drain opening at the water's edge. The municipal water
        // surface is y=61; the rising flooded pocket at r=7 stops at y=61.
        // r=8 (the ladder shaft) remains solid at y<=61 so no sewer flooding.
        range(c,s,5,7,-2,2,57,64,lining());
        range(c,s,5,6,-1,1,59,61,Blocks.WATER);
        // Pass 24 river profiles curve independently of the municipal grid.
        // Keep a short surface-water neck from the live river to this inlet;
        // otherwise a wide meander could strand the entrance in dry masonry.
        if (c.hasRiverMorphology()) {
            range(c,s,0,4,-1,1,59,61,Blocks.WATER);
            range(c,s,0,4,-1,1,62,62,Blocks.AIR);
        }
        // Air rises inside the r=6 pocket; the r=7 bulkhead remains
        // intact below waterline so the r=8 ladder NEVER loses a rung.
        range(c,s,6,6,0,0,62,63,Blocks.AIR); // rise above surface at y=61
        range(c,s,7,7,0,0,62,63,Blocks.AIR); // cross OVER the dry bulkhead
        // The ladder stays air-filled all the way down to the sewer.
        Direction facing = s.xCanal
                ? (s.bank > 0 ? Direction.WEST : Direction.EAST)
                : (s.bank > 0 ? Direction.NORTH : Direction.SOUTH);
        for(int y=62;y<=63;y++) {
            c.set(s.x(8,0),y,s.z(8,0),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
        }
        // Worn headwall, safe hand-openable access, hanging chain and shore marker.
        for(int t : new int[]{-2,2}) {
            block(c,s,5,t,61,Blocks.CRACKED_STONE_BRICKS);
            block(c,s,6,t,62,Blocks.WAXED_OXIDIZED_CUT_COPPER);
        }
        block(c,s,5,0,62,Blocks.CHAIN);
        c.barrel(s.x(5,1),FLOOR+1,s.z(5,1),"sewer_filters");
    }
}
