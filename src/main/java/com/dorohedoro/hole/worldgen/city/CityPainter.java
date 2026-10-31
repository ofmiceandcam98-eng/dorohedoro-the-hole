package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Turns the plan into blocks for a single chunk: underground levels first, then the ground and streets, then each lot.
 * It writes only inside the chunk and reads nothing but the plan.
 */
public final class CityPainter {
    private static final int FILL_BOTTOM = 56;

    private static final int SEWER_FLOOR = 49;
    private static final int SEWER_CEILING = 54;
    private static final int SEWER_SHELL_RADIUS = 4;
    private static final int SEWER_AIR_RADIUS = 3;

    private static final int CANAL_WATER_RADIUS = 6;
    private static final int CANAL_WATER_TOP = 61;
    private static final int CANAL_BED = 57;

    public static final int[] GALLERY_LEVELS = {12, -36};
    private static final int SHAFT_BOTTOM = GALLERY_LEVELS[GALLERY_LEVELS.length - 1];
    private static final int KIOSK_RADIUS = 3;

    private static final Mix ROAD = Mix.of(Blocks.GRAY_CONCRETE, 9, Blocks.CYAN_TERRACOTTA, 2, Blocks.TUFF, 2, Blocks.ANDESITE, 1, Blocks.COBBLED_DEEPSLATE, 1);
    private static final Mix LOCAL_ROAD = Mix.of(Blocks.COBBLED_DEEPSLATE, 4, Blocks.TUFF, 3, Blocks.GRAVEL, 1, Blocks.GRAY_CONCRETE, 3, Blocks.PACKED_MUD, 2);
    private static final Mix SIDEWALK = Mix.of(Blocks.POLISHED_ANDESITE, 6, Blocks.ANDESITE, 3, Blocks.STONE_BRICKS, 2, Blocks.CRACKED_STONE_BRICKS, 1);
    private static final Mix QUAY = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MOSSY_STONE_BRICKS, 2);
    private static final Mix ROAD_BED = Mix.of(Blocks.GRAVEL, 4, Blocks.DIRT, 4, Blocks.COARSE_DIRT, 2, Blocks.COBBLESTONE, 1, Blocks.SAND, 1);
    private static final Mix DEEP_FILL = Mix.of(Blocks.DIRT, 5, Blocks.GRAVEL, 4, Blocks.STONE, 5, Blocks.CLAY, 1, Blocks.SAND, 1);
    private static final Mix CANAL_MUD = Mix.of(Blocks.CLAY, 4, Blocks.DIRT, 2, Blocks.GRAVEL, 3, Blocks.SAND, 2);
    private static final Mix SEWER_WALL = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.MOSSY_STONE_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MUD_BRICKS, 1);
    private static final Mix SHAFT_WALL = Mix.of(Blocks.DEEPSLATE_BRICKS, 3, Blocks.STONE_BRICKS, 3, Blocks.CRACKED_DEEPSLATE_BRICKS, 1);
    private static final Mix KIOSK_WALL = Mix.of(Blocks.WAXED_WEATHERED_CUT_COPPER, 3, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.DEEPSLATE_BRICKS, 2);

    private CityPainter() {}

    public static void paint(CityPlan plan, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        Canvas canvas = new Canvas(chunk, plan.seed);
        List<Line> xLines = plan.xLinesNear(canvas.minX - 8, canvas.maxX + 8);
        List<Line> zLines = plan.zLinesNear(canvas.minZ - 8, canvas.maxZ + 8);

        paintGalleries(canvas, plan, xLines, zLines);
        paintSewers(canvas, xLines, zLines);
        paintGround(canvas, plan, xLines, zLines);
        paintCanals(canvas, xLines, zLines);
        for (Lot lot : plan.lotsIn(pos)) {
            LotPainter.paint(canvas, plan, lot);
        }
        paintShafts(canvas, plan, xLines, zLines);
    }

    /** Which arterial band, if any, holds a coordinate. */
    private static Line bandAt(List<Line> lines, int coordinate) {
        for (Line line : lines) {
            if (line.contains(coordinate)) {
                return line;
            }
        }
        return null;
    }

    private static void paintGround(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line xBand = bandAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line zBand = bandAt(zLines, z);
                c.fill(x, FILL_BOTTOM, z, x, STREET_Y - 5, z, DEEP_FILL);
                c.fill(x, STREET_Y - 4, z, x, STREET_Y - 1, z, ROAD_BED);

                if (xBand != null || zBand != null) {
                    paintArterialColumn(c, x, z, xBand, zBand);
                } else {
                    c.set(x, STREET_Y, z, LOCAL_ROAD);
                }
            }
        }
    }

    private static void paintArterialColumn(Canvas c, int x, int z, Line xBand, Line zBand) {
        int dx = xBand == null ? Integer.MAX_VALUE : Math.abs(x - xBand.center());
        int dz = zBand == null ? Integer.MAX_VALUE : Math.abs(z - zBand.center());
        boolean xCarriageway = xBand != null && dx <= curbOf(xBand);
        boolean zCarriageway = zBand != null && dz <= curbOf(zBand);

        if (xBand != null && zBand != null) {
            c.set(x, STREET_Y, z, ROAD);
            return;
        }
        if (xCarriageway || zCarriageway) {
            c.set(x, STREET_Y, z, ROAD);
            Line band = xCarriageway ? xBand : zBand;
            int offset = xCarriageway ? x - xBand.center() : z - zBand.center();
            if (band.kind() == Line.Kind.TRAM && (offset == -2 || offset == 2)) {
                BlockState rail = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE,
                        xCarriageway ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                c.set(x, GROUND_Y, z, rail);
            }
        } else {
            Line band = xBand != null ? xBand : zBand;
            c.set(x, STREET_Y, z, band.kind() == Line.Kind.CANAL ? QUAY : SIDEWALK);
            int edge = xBand != null ? dx : dz;
            int along = xBand != null ? z : x;
            if (edge == band.halfWidth() && Math.floorMod(along + band.center(), 15) == 0) {
                c.fill(x, GROUND_Y, z, x, GROUND_Y + 3, z, Blocks.SPRUCE_FENCE);
                c.set(x, GROUND_Y + 4, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
        }
    }

    private static int curbOf(Line band) {
        return band.kind() == Line.Kind.CANAL ? CANAL_WATER_RADIUS - 1 : band.halfWidth() - 2;
    }

    private static void paintCanals(Canvas c, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            Line xBand = bandAt(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line zBand = bandAt(zLines, z);
                boolean xCanal = xBand != null && xBand.kind() == Line.Kind.CANAL && Math.abs(x - xBand.center()) < CANAL_WATER_RADIUS;
                boolean zCanal = zBand != null && zBand.kind() == Line.Kind.CANAL && Math.abs(z - zBand.center()) < CANAL_WATER_RADIUS;
                if (!xCanal && !zCanal) {
                    continue;
                }
                // Where another arterial crosses, it carries on over a deck and the canal runs underneath.
                boolean bridged = (xCanal && zBand != null && !zCanal) || (zCanal && xBand != null && !xCanal);
                paintCanalColumn(c, x, z, bridged, xCanal ? z : x, xCanal ? Math.abs(x - xBand.center()) : Math.abs(z - zBand.center()));
            }
        }
    }

    private static void paintCanalColumn(Canvas c, int x, int z, boolean bridged, int along, int offset) {
        c.set(x, CANAL_BED, z, CANAL_MUD);
        c.fill(x, CANAL_BED + 1, z, x, CANAL_WATER_TOP, z, Blocks.WATER);
        c.fill(x, CANAL_WATER_TOP + 1, z, x, bridged ? STREET_Y - 2 : STREET_Y, z, Blocks.AIR);
        if (bridged) {
            c.set(x, STREET_Y - 1, z, Blocks.DEEPSLATE_TILES);
            return;
        }
        // Reed beds: a raised bank against the quay wall, which is where sugar cane can grow beside the water.
        int segment = Math.floorMod(along, 24);
        if (offset == CANAL_WATER_RADIUS - 1 && segment < 6) {
            c.set(x, CANAL_WATER_TOP, z, segment % 2 == 0 ? Blocks.SAND : Blocks.DIRT);
            c.fill(x, CANAL_WATER_TOP + 1, z, x, CANAL_WATER_TOP + 3, z, Blocks.SUGAR_CANE);
        }
    }

    private static void paintSewers(Canvas c, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            int dx = nearestOffset(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                int dz = nearestOffset(zLines, z);
                int d = Math.min(dx, dz);
                if (d > SEWER_SHELL_RADIUS) {
                    continue;
                }
                c.fill(x, SEWER_FLOOR - 2, z, x, SEWER_CEILING + 1, z, SEWER_WALL);
                if (d <= SEWER_AIR_RADIUS) {
                    c.fill(x, SEWER_FLOOR + 1, z, x, SEWER_CEILING - 1, z, Blocks.AIR);
                    if (d <= 1) {
                        c.set(x, SEWER_FLOOR, z, Blocks.AIR);
                        c.set(x, SEWER_FLOOR - 1, z, Blocks.WATER);
                    }
                }
            }
        }
    }

    private static int nearestOffset(List<Line> lines, int coordinate) {
        int best = Integer.MAX_VALUE;
        for (Line line : lines) {
            best = Math.min(best, Math.abs(coordinate - line.center()));
        }
        return best;
    }

    private static void paintGalleries(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int level : GALLERY_LEVELS) {
            for (int x = c.minX; x <= c.maxX; x++) {
                int dx = nearestOffset(xLines, x);
                for (int z = c.minZ; z <= c.maxZ; z++) {
                    int dz = nearestOffset(zLines, z);
                    int d = Math.min(dx, dz);
                    if (d > 1) {
                        continue;
                    }
                    c.fill(x, level + 1, z, x, level + 3, z, Blocks.AIR);
                    c.set(x, level, z, level < 0 ? Blocks.COBBLED_DEEPSLATE : Blocks.COBBLESTONE);
                    int along = dx <= 1 ? z : x;
                    boolean crossing = dx <= 1 && dz <= 1;
                    if (!crossing && Math.floorMod(along, 8) == 0) {
                        if (d == 1) {
                            c.fill(x, level + 1, z, x, level + 2, z, Blocks.OAK_FENCE);
                        }
                        c.set(x, level + 3, z, Blocks.OAK_PLANKS);
                        if (d == 0 && Math.floorMod(along, 16) == 0) {
                            c.set(x, level + 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                        }
                    }
                    if (d == 0 && !crossing && Math.floorMod(along, 8) != 0) {
                        c.set(x, level + 1, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE,
                                dx == 0 ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST));
                    }
                }
            }
        }
    }

    /** Ladder shafts at selected crossings, joining the street, the sewers and both mine levels. */
    private static void paintShafts(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                if (!plan.hasShaft(xLine.index(), zLine.index())) {
                    continue;
                }
                if (xLine.kind() == Line.Kind.CANAL || zLine.kind() == Line.Kind.CANAL) {
                    continue;
                }
                paintShaft(c, xLine.center(), zLine.center());
            }
        }
    }

    private static void paintShaft(Canvas c, int cx, int cz) {
        Rect ring = new Rect(cx - 2, cz - 2, cx + 2, cz + 2);
        Rect inner = ring.inset(1);
        if (!c.overlaps(new Rect(cx - KIOSK_RADIUS, cz - KIOSK_RADIUS, cx + KIOSK_RADIUS, cz + KIOSK_RADIUS))) {
            return;
        }
        for (int y = SHAFT_BOTTOM + 1; y <= STREET_Y; y++) {
            boolean sewer = y > SEWER_FLOOR - 1 && y < SEWER_CEILING;
            boolean gallery = false;
            for (int level : GALLERY_LEVELS) {
                gallery |= y > level && y <= level + 3;
            }
            if (!sewer && !gallery) {
                c.walls(ring, y, y, SHAFT_WALL);
            }
            c.fill(inner, y, y, Blocks.AIR);
        }
        // The ladder keeps its own backing column so it survives where the shaft passes through open levels.
        c.fill(cx, SHAFT_BOTTOM, cz + 2, cx, STREET_Y - 1, cz + 2, SHAFT_WALL);
        c.ladder(cx, SHAFT_BOTTOM + 1, STREET_Y - 1, cz + 1, Direction.NORTH);
        c.fill(inner, SHAFT_BOTTOM, SHAFT_BOTTOM, Blocks.COBBLED_DEEPSLATE);

        // Street-level hatch house, an island the traffic lanes pass around.
        Rect kiosk = new Rect(cx - KIOSK_RADIUS, cz - KIOSK_RADIUS, cx + KIOSK_RADIUS, cz + KIOSK_RADIUS);
        c.clear(kiosk, GROUND_Y, GROUND_Y + 4);
        c.walls(kiosk, GROUND_Y, GROUND_Y + 3, KIOSK_WALL);
        c.fill(kiosk, GROUND_Y + 4, GROUND_Y + 4, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        c.fill(kiosk, STREET_Y, STREET_Y, Blocks.DEEPSLATE_TILES);
        // The floor is solid apart from one trapdoor over the ladder, so nobody walks into a hundred-block drop.
        c.set(cx, STREET_Y, cz + 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(TrapDoorBlock.HALF, Half.TOP));
        c.door(cx, GROUND_Y, cz - KIOSK_RADIUS, Direction.NORTH, Blocks.SPRUCE_DOOR);
        c.door(cx, GROUND_Y, cz + KIOSK_RADIUS, Direction.SOUTH, Blocks.SPRUCE_DOOR);
        c.door(cx - KIOSK_RADIUS, GROUND_Y, cz, Direction.WEST, Blocks.SPRUCE_DOOR);
        c.door(cx + KIOSK_RADIUS, GROUND_Y, cz, Direction.EAST, Blocks.SPRUCE_DOOR);
        c.set(cx, GROUND_Y + 3, cz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

}
