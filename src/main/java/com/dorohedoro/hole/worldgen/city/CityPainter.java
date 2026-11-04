package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
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

    // Nothing at street level or above may use stone, tuff, deepslate, andesite, granite or diorite. Vanilla ore and
    // rock features replace those blocks up to the build limit, and they would punch ore into the roads and buildings.
    private static final Mix ROAD = Mix.of(Blocks.GRAY_CONCRETE, 9, Blocks.CYAN_TERRACOTTA, 2, Blocks.POLISHED_TUFF, 2, Blocks.POLISHED_ANDESITE, 1, Blocks.COBBLED_DEEPSLATE, 1);
    private static final Mix LOCAL_ROAD = Mix.of(Blocks.COBBLED_DEEPSLATE, 4, Blocks.POLISHED_TUFF, 3, Blocks.GRAY_CONCRETE, 3, Blocks.PACKED_MUD, 2);
    private static final Mix SIDEWALK = Mix.of(Blocks.POLISHED_ANDESITE, 8, Blocks.STONE_BRICKS, 2, Blocks.CRACKED_STONE_BRICKS, 1);
    private static final Mix QUAY = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MOSSY_STONE_BRICKS, 2);
    private static final Mix ROAD_BED = Mix.of(Blocks.GRAVEL, 4, Blocks.DIRT, 4, Blocks.COARSE_DIRT, 2, Blocks.COBBLESTONE, 1, Blocks.SAND, 1);
    private static final Mix DEEP_FILL = Mix.of(Blocks.DIRT, 5, Blocks.GRAVEL, 4, Blocks.STONE, 5, Blocks.CLAY, 1, Blocks.SAND, 1);
    private static final Mix CANAL_MUD = Mix.of(Blocks.CLAY, 4, Blocks.DIRT, 2, Blocks.GRAVEL, 3, Blocks.SAND, 2);
    private static final Mix SEWER_WALL = Mix.of(Blocks.STONE_BRICKS, 5, Blocks.MOSSY_STONE_BRICKS, 3, Blocks.CRACKED_STONE_BRICKS, 2, Blocks.MUD_BRICKS, 1);
    private static final Mix SHAFT_WALL = Mix.of(Blocks.DEEPSLATE_BRICKS, 3, Blocks.STONE_BRICKS, 3, Blocks.CRACKED_DEEPSLATE_BRICKS, 1);
    private static final Mix KIOSK_WALL = Mix.of(Blocks.WAXED_WEATHERED_CUT_COPPER, 3, Blocks.WAXED_OXIDIZED_CUT_COPPER, 2, Blocks.DEEPSLATE_BRICKS, 2);

    private CityPainter() {}

    public static void paint(CityPlan plan, ChunkAccess chunk) {
        paint(plan, chunk, List.of());
    }

    public static void paint(CityPlan plan, ChunkAccess chunk, List<VanillaStructureBuffer64.Box> structures) {
        ChunkPos pos = chunk.getPos();
        Canvas canvas = new Canvas(chunk, plan, structures);
        List<Line> xLines = plan.xLinesNear(canvas.minX - 8, canvas.maxX + 8);
        List<Line> zLines = plan.zLinesNear(canvas.minZ - 8, canvas.maxZ + 8);

        // Pass 42: foundation columns fill down to the old bedrock-side grade.
        // Build solid terrain BEFORE drilling sewers, avoiding floating highlands
        // and avoiding a later surface fill sealing up municipal passages.
        if (plan.settings.worldDetailVersion() >= 26) paintGround(canvas, plan, xLines, zLines);
        if (plan.settings.sewerUnderworld()) {
            // Preserve natural deep cave noise for mining; these chambers are optional underground destinations.
            paintDeepExcavations(canvas, plan);
        }
        paintGalleries(canvas, plan, xLines, zLines);
        paintSewers(canvas, xLines, zLines);
        if (plan.settings.sewerUnderworld() && plan.settings.sewerCavernVersion() >= 4) {
            SewerDetails24.paint(canvas, plan, xLines, zLines);
            // Pass 70: varied outer sewer masonry (not the dry lanes or wastewater).
            SewerVaultVariants70.paint(canvas, plan, xLines, zLines);
            // Pass 67: sparse walk-in repair/filter bays along unobstructed sewer runs.
            SewerServiceBays67.paint(canvas, plan);
            // Pass 68: rare walk-in stormwater pump halls with dry catwalks and an optional lever-operated booth.
            StormwaterPumpHalls68.paint(canvas, plan);
            // Pass 70: uncommon dual-entrance, dry-lane stormwater bypass galleries.
            StormDrainBypasses70.paint(canvas, plan);
        }
        if (plan.settings.sewerUnderworld()) {
            paintMaintenanceTunnels(canvas, xLines, zLines);
            paintCisterns(canvas, plan);
            // Only selected, deliberately long sewer branches reach the natural cave system.
            SewerCaveBranches.paint(canvas, plan);
            if (plan.settings.sewerCavernVersion() >= 2) {
                SewerDungeon22.paint(canvas, plan);
            }
        }
        if (plan.settings.worldDetailVersion() < 26) paintGround(canvas, plan, xLines, zLines);
        if (plan.settings.sewerCavernVersion() >= 5) {
            // River profile is a replacement for the rigid strip painter, not a second
            // water overlay. The plan reserves the existing canal corridor so buildings
            // and roads are not sliced by bends; fish and access routes stay connected.
            RiverChannels24.paint(canvas, plan, xLines, zLines);
        } else {
            paintCanals(canvas, xLines, zLines);
        }
        if (plan.settings.sewerCavernVersion() >= 4) {
            CanalEcology24.paint(canvas, plan, xLines, zLines);
        }
        // Pass 43: sparse functional riverside workshops in dry quarry/quay
        // corridors, after actual channel edges are painted, before open lots.
        if (plan.settings.worldDetailVersion() >= 27)
            CanalWorksites43.paint(canvas, plan, xLines, zLines);
        // Pass 25: coordinate-seeded neighborhood texture and infrastructure.
        // Lot bounds are excluded so generated streets never replace buildings.
        List<Lot> currentLots = plan.lotsIn(pos);
        if (canvas.hasUrbanFabric()) {
            UrbanFabric25.streets(canvas, plan, xLines, zLines, currentLots);
        }
        if (plan.settings.worldDetailVersion() >= 27)
            Backstreets43.paint(canvas, plan, xLines, zLines, currentLots);
        for (Lot lot : currentLots) {
            LotPainter.paint(canvas, plan, lot);
            if (plan.settings.worldDetailVersion() >= 26) RegionalSurface42.openLot(canvas, plan, lot);
        }
        if (canvas.hasWorldDetail()) {
            WorldDetail.streetFixtures(canvas, plan, xLines, zLines);
        }
        // Functional tram shelters at safe stretches, never on tracks or bridges.
        if (plan.settings.worldDetailVersion() >= 26) HoleTransit42.paint(canvas, plan, xLines, zLines);
        if (canvas.hasIrregularCity()) DiagonalBoulevards14.paint(canvas, plan);
        paintShafts(canvas, plan, xLines, zLines);
        // Added after water and surface fixtures: both access routes meet the
        // already-carved municipal sewers without replacing canal generation.
        if (plan.settings.sewerUnderworld() && plan.settings.sewerCavernVersion() >= 3) {
            WaterwaySewerEntrances23.paint(canvas, plan);
        }
        WinterPavement17.paint(canvas, plan);
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

    /** Account for the bounded bend without changing tram tracks, canals, or underground axes. */
    private static Line streetXBand(CityPlan plan, List<Line> lines, int x, int z) {
        for (Line line : lines) {
            if (Math.abs(x - line.center() - plan.xAvenueOffset(line, z)) <= line.halfWidth()) return line;
        }
        return null;
    }

    private static Line streetZBand(CityPlan plan, List<Line> lines, int x, int z) {
        for (Line line : lines) {
            if (Math.abs(z - line.center() - plan.zAvenueOffset(line, x)) <= line.halfWidth()) return line;
        }
        return null;
    }


    private static void paintGround(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line xBand = streetXBand(plan, xLines, x, z);
                Line zBand = streetZBand(plan, zLines, x, z);
                // Clear remnants of vanilla surface terrain above the graded street, then
                // build a compacted road foundation at the same elevation as its surface.
                c.fill(x, STREET_Y + 1, z, x, STREET_Y + 13, z, Blocks.AIR);
                int foundationBottom = plan.settings.worldDetailVersion() >= 26
                        ? FILL_BOTTOM - c.heightOffset(x, z) : FILL_BOTTOM;
                c.fill(x, foundationBottom, z, x, STREET_Y - 5, z, DEEP_FILL);
                c.fill(x, STREET_Y - 4, z, x, STREET_Y - 1, z, ROAD_BED);

                if (xBand != null || zBand != null) {
                    paintArterialColumn(c, plan, x, z, xBand, zBand);
                } else {
                    c.set(x, STREET_Y, z, LOCAL_ROAD);
                }
                if (c.hasWorldDetail()) {
                    WorldDetail.streetSurface(c, plan, x, z, xBand, zBand);
                    if (c.hasArchitectureVariation()) ArchitecturePass13.streetSurface(c, plan, x, z, xBand, zBand);
                    if (c.hasLoreArchitecture()) LoreArchitecture19.streetSurface(c, plan, x, z, xBand, zBand);
                }
                if (plan.settings.worldDetailVersion() >= 26)
                    RegionalSurface42.street(c, plan, x, z, xBand, zBand);
            }
        }
    }

    private static void paintArterialColumn(Canvas c, CityPlan plan, int x, int z, Line xBand, Line zBand) {
        int dx = xBand == null ? Integer.MAX_VALUE : Math.abs(x - xBand.center() - plan.xAvenueOffset(xBand, z));
        int dz = zBand == null ? Integer.MAX_VALUE : Math.abs(z - zBand.center() - plan.zAvenueOffset(zBand, x));
        boolean xCarriageway = xBand != null && dx <= curbOf(xBand);
        boolean zCarriageway = zBand != null && dz <= curbOf(zBand);

        if (xBand != null && zBand != null) {
            c.set(x, STREET_Y, z, ROAD);
            return;
        }
        if (xCarriageway || zCarriageway) {
            c.set(x, STREET_Y, z, ROAD);
            Line band = xCarriageway ? xBand : zBand;
            int offset = xCarriageway ? x - xBand.center() - plan.xAvenueOffset(xBand, z)
                    : z - zBand.center() - plan.zAvenueOffset(zBand, x);
            if (band.kind() == Line.Kind.TRAM && (offset == -2 || offset == 2)) {
                BlockState rail = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE,
                        gradedRailShape(plan, x, z, xCarriageway));
                c.set(x, GROUND_Y, z, rail);
            }
        } else {
            Line band = xBand != null ? xBand : zBand;
            c.set(x, STREET_Y, z, band.kind() == Line.Kind.CANAL ? QUAY : SIDEWALK);
            int edge = xBand != null ? dx : dz;
            int along = xBand != null ? z : x;
            // Protect the starter area while leaving outer streets dark enough
            // for natural Minecraft monster spawning at night.
            int lightSpacing = c.hasOccupiedInteriors35()
                    ? ((Math.abs(x) < 64 && Math.abs(z) < 64) ? 30 : 45) : 15;
            boolean placeLamp = plan.settings.worldDetailVersion() >= 27
                    ? streetlamp43(plan, band, along, xBand != null ? x : z,
                            xBand != null ? plan.xAvenueOffset(band, z) : plan.zAvenueOffset(band, x))
                    : Math.floorMod(along + band.center(), lightSpacing) == 0;
            if (plan.settings.worldDetailVersion() >= 35
                    ? UrbanPattern52.at(plan.seed, x, z) == UrbanPattern52.Fabric.OPEN_COUNTRY
                    : plan.settings.worldDetailVersion() >= 31
                      && plan.regionAt(x, z) != HoleRegions42.Region.CITY
                      && SettlementBands47.at(plan.seed, x, z) == SettlementBands47.Form.OPEN_COUNTRY) {
                placeLamp = false; // dark wilderness; settlement lamps still mark lived-in roads
            }
            if (edge == band.halfWidth() && placeLamp) {
                c.fill(x, GROUND_Y, z, x, GROUND_Y + 3, z, Blocks.SPRUCE_FENCE);
                if (Math.floorMod(along, 3) == 0) {
                    // Some always-lit fixtures keep early survival reasonably safe.
                    c.set(x, GROUND_Y + 4, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                } else {
                    // Inverted daylight sensors switch industrial street lamps on at night.
                    // In-client lighting and first-tick behavior still need runtime verification.
                    c.set(x, GROUND_Y + 4, z, Blocks.REDSTONE_LAMP);
                    c.set(x, GROUND_Y + 5, z, Blocks.DAYLIGHT_DETECTOR.defaultBlockState()
                            .setValue(DaylightDetectorBlock.INVERTED, true));
                }
            }
        }
    }

    /**
     * Uneven, mostly one-sided lamp coverage. Every decision comes from a
     * fixed absolute street segment, not a chunk RNG; side alleys stay dark.
     * Existing v0-v26 saved worlds retain their old lamp positions.
     */
    private static boolean streetlamp43(CityPlan plan, Line band, int along, int cross, int bend) {
        final int segmentSize = 73;
        int distance = along + band.center();
        int section = Math.floorDiv(distance, segmentSize);
        long h = CityHash.of(plan.seed ^ 0x43A401L,
                band.index() * 13L + band.kind().ordinal(), section);
        // The quarter around player spawn retains significantly more protection.
        boolean spawn = Math.abs(along) < 65 && Math.abs(cross) < 65;
        if (!spawn && CityHash.range(h >>> 14, 4) == 0) return false;
        int intended = section * segmentSize + 12 + CityHash.range(h >>> 19, 45);
        if (distance != intended) return false;
        // Only one sidewalk edge per fixture, to eliminate symmetric avenues.
        return (cross - band.center() - bend > 0) == (CityHash.range(h >>> 31, 2) == 0);
    }

    /** Match tram track grade to the deterministic relief (rather than leaving floating flat rails). */
    private static RailShape gradedRailShape(CityPlan plan, int x, int z, boolean alongZ) {
        if (!plan.settings.terrainRelief()) {
            return alongZ ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
        }
        int y = CityTerrain40.offset(plan, x, z);
        if (alongZ) {
            if (CityTerrain40.offset(plan, x, z + 1) > y) return RailShape.ASCENDING_SOUTH;
            if (CityTerrain40.offset(plan, x, z - 1) > y) return RailShape.ASCENDING_NORTH;
            return RailShape.NORTH_SOUTH;
        }
        if (CityTerrain40.offset(plan, x + 1, z) > y) return RailShape.ASCENDING_EAST;
        if (CityTerrain40.offset(plan, x - 1, z) > y) return RailShape.ASCENDING_WEST;
        return RailShape.EAST_WEST;
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


    /**
     * Flood-control caverns occupy selected major junctions deep below the Hole. They are built entirely
     * from absolute seed/coordinate decisions and clipped by Canvas, so neighboring chunks never disagree.
     * The existing lower railway gallery cuts through their centre and makes them reachable from shafts.
     */
    private static void paintDeepExcavations(Canvas c, CityPlan plan) {
        List<Line> xLines = plan.xLinesNear(c.minX - 19, c.maxX + 19);
        List<Line> zLines = plan.zLinesNear(c.minZ - 19, c.maxZ + 19);
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                int i = xLine.index();
                int j = zLine.index();
                boolean starter = (i == -1 || i == 0) && (j == -1 || j == 0);
                if (!starter && CityHash.range(CityHash.of(plan.seed, 0x5E7E20L, i, j), 5) != 0) {
                    continue;
                }
                int cx = xLine.center();
                int cz = zLine.center();
                // Vary cavern radius and profile while keeping every decision coordinate-only.
                int rx = 11 + CityHash.range(CityHash.of(plan.seed, 0x5E7E21L, i, j), 6);
                int rz = 10 + CityHash.range(CityHash.of(plan.seed, 0x5E7E22L, i, j), 6);
                for (int x = Math.max(c.minX, cx - 18); x <= Math.min(c.maxX, cx + 18); x++) {
                    for (int z = Math.max(c.minZ, cz - 18); z <= Math.min(c.maxZ, cz + 18); z++) {
                        double xx = (x - cx) / (double) rx;
                        double zz = (z - cz) / (double) rz;
                        double distance = xx * xx + zz * zz;
                        double irregularity = (CityHash.unit(CityHash.of(plan.seed, 0x5E7E23L, x >> 2, z >> 2)) - 0.5) * 0.20;
                        if (distance > 1.0 + irregularity) continue;
                        int roof = -24 - CityHash.range(CityHash.of(plan.seed, 0x5E7E24L, x >> 2, z >> 2), 4);
                        c.fill(x, -36, z, x, roof, z, Blocks.AIR);
                        // Supported floor, not a void into bedrock; deep galleries remain open afterward.
                        c.set(x, -37, z, CityHash.range(c.hash(x, -37, z), 8) == 0
                                ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_BRICKS);
                        if (Math.abs(x - cx) > 5 && Math.abs(z - cz) > 4
                                && CityHash.range(c.hash(x, -35, z), 240) == 0) {
                            c.set(x, -36, z, Blocks.MOSS_CARPET);
                        }
                    }
                }
                // Real, safely reachable side caches provide useful exploration without mandatory loot gating.
                int sideX = cx + 5;
                int sideZ = cz + 5;
                if (CityHash.range(CityHash.of(plan.seed, 0x5E7E25L, i, j), 3) == 0) {
                    c.barrel(sideX, -36, sideZ, "sewer_cache");
                }
            }
        }
    }

    /**
     * Middle-depth flood-service conduits at a different height from both the municipal sewer
     * and the mining galleries. The vertical access shafts cross all three networks.
     */
    private static void paintMaintenanceTunnels(Canvas c, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            int dx = nearestOffset(xLines, x);
            for (int z = c.minZ; z <= c.maxZ; z++) {
                int dz = nearestOffset(zLines, z);
                int d = Math.min(dx, dz);
                if (d > 2) continue;
                c.fill(x, 28, z, x, 36, z, SEWER_WALL);
                if (d == 2 && Math.floorMod(x + z, 5) == 0) {
                    c.set(x, 31, z, ModBlocks.SEWER_BRICKWORK.get());
                }
                if (d <= 1) {
                    c.fill(x, 30, z, x, 34, z, Blocks.AIR);
                    c.set(x, 29, z, d == 0 ? Blocks.WATER : Blocks.DEEPSLATE_TILES);
                    // Drainage channels sit one block below the dry side paths.
                    if (d == 0) c.set(x, 28, z, Blocks.DEEPSLATE_BRICKS);
                }
                boolean junction = dx <= 2 && dz <= 2;
                if (d == 1 && !junction && Math.floorMod(dx <= 1 ? z : x, 19) == 0) {
                    c.set(x, 34, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
            }
        }
    }

    /**
     * Large brick-lined cisterns at some arterial intersections. They link directly to the
     * upper sewer network, unlike vanilla naturally generated caves that may lead nowhere.
     */
    private static void paintCisterns(Canvas c, CityPlan plan) {
        List<Line> xLines = plan.xLinesNear(c.minX - 10, c.maxX + 10);
        List<Line> zLines = plan.zLinesNear(c.minZ - 10, c.maxZ + 10);
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                if (plan.hasShaft(xLine.index(), zLine.index())) continue; // preserve vertical access kiosks
                if (CityHash.range(CityHash.of(plan.seed, 0xC157E2L, xLine.index(), zLine.index()), 4) != 0) continue;
                int cx = xLine.center(), cz = zLine.center();
                Rect ring = new Rect(cx - 8, cz - 8, cx + 8, cz + 8);
                if (!c.overlaps(ring)) continue;
                for (int x = Math.max(c.minX, ring.x0()); x <= Math.min(c.maxX, ring.x1()); x++) {
                    for (int z = Math.max(c.minZ, ring.z0()); z <= Math.min(c.maxZ, ring.z1()); z++) {
                        int edge = Math.max(Math.abs(x - cx), Math.abs(z - cz));
                        if (edge == 8) {
                            c.fill(x, 45, z, x, 55, z, SEWER_WALL);
                            c.set(x, 50, z, ModBlocks.SEWER_BRICKWORK.get());
                        } else {
                            c.set(x, 45, z, Blocks.DEEPSLATE_BRICKS);
                            c.fill(x, 46, z, x, 54, z, Blocks.AIR);
                            if (edge <= 4) {
                                c.set(x, 46, z, Blocks.WATER);
                            } else {
                                c.set(x, 46, z, Blocks.POLISHED_DEEPSLATE);
                            }
                            c.set(x, 55, z, Blocks.DEEPSLATE_TILES);
                        }
                    }
                }
                // Open cardinal passages into the original seven-block-wide sewer arteries.
                for (int d = -3; d <= 3; d++) {
                    c.fill(cx + d, 50, cz - 8, cx + d, 53, cz - 8, Blocks.AIR);
                    c.fill(cx + d, 50, cz + 8, cx + d, 53, cz + 8, Blocks.AIR);
                    c.fill(cx - 8, 50, cz + d, cx - 8, 53, cz + d, Blocks.AIR);
                    c.fill(cx + 8, 50, cz + d, cx + 8, 53, cz + d, Blocks.AIR);
                }
                // Shallow dry perimeter, safe for footsteps and future maintenance NPCs.
                for (int dx : new int[]{-6, 6}) {
                    for (int dz : new int[]{-6, 6}) {
                        c.set(cx + dx, 47, cz + dz, Blocks.LANTERN);
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
                    // Protect the entire gallery from random cavern noise with a two-block wall.
                    // The branch pass runs afterward and opens only chosen side connections.
                    if (plan.settings.sewerUnderworld() && d <= 3) {
                        c.fill(x, level - 1, z, x, level + 4, z, level < 0
                                ? Blocks.DEEPSLATE_BRICKS : Blocks.STONE_BRICKS);
                    }
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
            // Keep the side corridors of the middle drainage network open to the access shaft.
            // Its water runnel sits at y=29 behind the shaft wall, below these passage openings.
            boolean maintenance = y >= 30 && y <= 34;
            if (!sewer && !gallery && !maintenance) {
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
