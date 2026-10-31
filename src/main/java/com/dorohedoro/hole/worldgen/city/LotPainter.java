package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Draws enclosed lots. Open-air lots live in {@link OpenLots}, landmarks in {@link Landmarks}. */
final class LotPainter {
    private static final Mix YARD = Mix.of(Blocks.PACKED_MUD, 4, Blocks.COARSE_DIRT, 3, Blocks.COBBLED_DEEPSLATE, 2, Blocks.GRAVEL, 1);
    private static final Block[] JOB_SITES = {
            Blocks.SMOKER, Blocks.BLAST_FURNACE, Blocks.CARTOGRAPHY_TABLE, Blocks.LECTERN, Blocks.SMITHING_TABLE, Blocks.FLETCHING_TABLE,
            Blocks.LOOM, Blocks.STONECUTTER, Blocks.CAULDRON, Blocks.BREWING_STAND, Blocks.COMPOSTER, Blocks.BARREL
    };

    private LotPainter() {}

    static void paint(Canvas c, CityPlan plan, Lot lot) {
        if (!c.overlaps(lot.rect())) {
            return;
        }
        c.fill(lot.rect(), STREET_Y, STREET_Y, YARD);
        switch (lot.type()) {
            case PLAZA -> Landmarks.plaza(c, lot);
            case HUNGRY_BUG -> Landmarks.hungryBug(c, lot);
            case STARTER_SHELTER -> Landmarks.starterShelter(c, lot);
            case HOSPITAL -> Landmarks.hospital(c, lot);
            case DEPARTMENT_STORE -> Landmarks.departmentStore(c, lot);
            case RESEARCH_MANOR -> Landmarks.researchManor(c, lot);
            case LAKE_OF_REFUSE -> Landmarks.lakeOfRefuse(c, lot);
            case TENEMENT -> tenement(c, plan, lot);
            case APARTMENT -> apartment(c, plan, lot);
            case SHOP -> shop(c, lot);
            case CLINIC -> clinic(c, lot);
            case GUARD_POST -> guardPost(c, lot);
            case WAREHOUSE -> warehouse(c, lot);
            case FACTORY -> factory(c, lot);
            case MARKET_STALLS -> OpenLots.marketStalls(c, lot);
            case GREENHOUSE -> OpenLots.greenhouse(c, lot);
            case LIVESTOCK_PEN -> OpenLots.livestockPen(c, lot);
            case NURSERY -> OpenLots.nursery(c, lot);
            case PARK -> OpenLots.park(c, lot);
            case JUNKYARD -> OpenLots.junkyard(c, lot);
            case RUBBLE -> OpenLots.rubble(c, lot);
            case VACANT -> OpenLots.vacant(c, lot);
            case CEMETERY -> OpenLots.cemetery(c, lot);
        }
    }

    static Tower.Roof roofFor(long hash) {
        return switch (CityHash.range(hash >>> 32, 5)) {
            case 0, 1 -> Tower.Roof.GARDEN;
            case 2 -> Tower.Roof.TANK;
            case 3 -> Tower.Roof.ANTENNA;
            default -> Tower.Roof.PLAIN;
        };
    }

    private static void tenement(Canvas c, CityPlan plan, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int floors = Math.min(plan.settings.maxFloors(), 2 + CityHash.range(lot.hash(), 3));
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Tower.build(c, box, floors, style, lot.hash(), roofFor(lot.hash()), (cv, k, in, y) -> dwelling(cv, style, k, in, y));
    }

    private static void apartment(Canvas c, CityPlan plan, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int span = Math.max(1, plan.settings.maxFloors() - 4);
        int floors = Math.min(plan.settings.maxFloors(), 5 + CityHash.range(lot.hash(), span));
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Tower.build(c, box, floors, style, lot.hash(), roofFor(lot.hash()), (cv, k, in, y) -> dwelling(cv, style, k, in, y));
    }

    /** Living quarters: a shared entry on the ground floor, then rooms off a corridor beside the stairs. */
    static void dwelling(Canvas c, Style style, int storey, Rect in, int y) {
        if (storey == 0) {
            c.barrel(in.x1(), y + 1, in.z0() + 2, "apartment");
            c.set(in.x1(), y + 1, in.z0() + 3, Blocks.CRAFTING_TABLE);
            Tower.lamp(c, in.centerX(), y, in.centerZ());
            return;
        }
        int wallZ = in.z0() + 2;
        if (in.depth() < 6) {
            Tower.lamp(c, in.centerX(), y, in.centerZ());
            return;
        }
        c.fill(in.x0(), y + 1, wallZ, in.x1(), y + Tower.STOREY - 1, wallZ, style.wall());
        int roomWidth = 6;
        for (int rx = in.x0(); rx <= in.x1() - 3; rx += roomWidth + 1) {
            int rx1 = Math.min(rx + roomWidth - 1, in.x1());
            room(c, style, new Rect(rx, wallZ + 1, rx1, in.z1()), wallZ, y);
            if (rx1 < in.x1()) {
                c.fill(rx1 + 1, y + 1, wallZ + 1, rx1 + 1, y + Tower.STOREY - 1, in.z1(), style.wall());
            }
        }
    }

    private static void room(Canvas c, Style style, Rect r, int wallZ, int y) {
        c.door(r.centerX(), y + 1, wallZ, Direction.SOUTH, style.door());
        if (r.depth() >= 3 && r.width() >= 3) {
            c.bed(r.x0(), y + 1, r.z1() - 1, Direction.SOUTH, style.bed());
            switch (CityHash.range(c.hash(r.x0(), y, r.z0()), 4)) {
                case 0 -> c.chest(r.x1(), y + 1, r.z1(), Direction.NORTH, "apartment");
                case 1 -> c.barrel(r.x1(), y + 1, r.z1(), "apartment");
                case 2 -> c.set(r.x1(), y + 1, r.z1(), Blocks.CRAFTING_TABLE);
                default -> c.facing(r.x1(), y + 1, r.z1(), Direction.NORTH, Blocks.FURNACE);
            }
        }
        Tower.lamp(c, r.centerX(), y, r.centerZ());
    }

    private static void shop(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.marketStalls(c, lot);
            return;
        }
        int floors = 1 + CityHash.range(lot.hash(), 3);
        Style style = Style.forDistrict(lot.district(), lot.hash());
        Block site = JOB_SITES[CityHash.range(lot.hash() >>> 12, JOB_SITES.length)];
        Tower.build(c, box, floors, style, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> {
            if (k > 0) {
                dwelling(cv, style, k, in, y);
                return;
            }
            for (int z = in.z0() + 3; z <= in.z1() - 2; z++) {
                cv.barrel(in.x1() - 1, y + 1, z, "shop");
            }
            cv.set(in.x1(), y + 1, in.centerZ(), site);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
        });
        awning(c, box, lot.hash());
    }

    private static void awning(Canvas c, Rect box, long hash) {
        Block[] cloth = {Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.GREEN_WOOL, Blocks.CYAN_WOOL, Blocks.YELLOW_WOOL};
        Block a = cloth[CityHash.range(hash >>> 20, cloth.length)];
        for (int x = box.x0(); x <= box.x1(); x++) {
            c.set(x, GROUND_Y + 3, box.z1() + 1, Math.floorMod(x, 2) == 0 ? a : Blocks.WHITE_WOOL);
        }
    }

    private static void clinic(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        int floors = 2 + CityHash.range(lot.hash(), 2);
        Tower.build(c, box, floors, Style.CLEAN, lot.hash(), Tower.Roof.PLAIN, (cv, k, in, y) -> ward(cv, k, in, y));
        redCross(c, box.centerX(), GROUND_Y + floors * Tower.STOREY - 3, box.z1());
    }

    /** Beds along the south wall, with supplies at the east end. */
    static void ward(Canvas c, int storey, Rect in, int y) {
        for (int x = in.x0(); x <= in.x1() - 1; x += 2) {
            if (Math.abs(x - in.centerX()) > 1) {
                c.bed(x, y + 1, in.z1() - 1, Direction.SOUTH, Blocks.WHITE_BED);
            }
        }
        c.set(in.x1(), y + 1, in.z0() + 2, Blocks.BREWING_STAND);
        c.chest(in.x1(), y + 1, in.z0() + 3, Direction.WEST, "clinic");
        Tower.lamp(c, in.centerX(), y, in.centerZ());
        Tower.lamp(c, in.x0() + 2, y, in.centerZ());
        Tower.lamp(c, in.x1() - 2, y, in.centerZ());
    }

    static void redCross(Canvas c, int x, int y, int z) {
        c.fill(x - 1, y, z, x + 1, y, z, Blocks.RED_CONCRETE);
        c.fill(x, y - 1, z, x, y + 1, z, Blocks.RED_CONCRETE);
    }

    private static void guardPost(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (!Tower.fits(box)) {
            OpenLots.rubble(c, lot);
            return;
        }
        Tower.build(c, box, 2, Style.SOOT, lot.hash(), Tower.Roof.ANTENNA, (cv, k, in, y) -> {
            cv.chest(in.x1(), y + 1, in.z0() + 2, Direction.WEST, "guard");
            cv.set(in.x1() - 1, y + 1, in.z1() - 1, Blocks.SMITHING_TABLE);
            cv.bed(in.x0(), y + 1, in.z1() - 1, Direction.SOUTH, Blocks.BLACK_BED);
            Tower.lamp(cv, in.centerX(), y, in.centerZ());
        });
    }

    /** A tall single-room shed with aisles of crates. */
    private static void hall(Canvas c, Rect box, int height, Style style) {
        int roofY = GROUND_Y + height;
        c.fill(box, STREET_Y, STREET_Y, style.floor());
        c.walls(box, GROUND_Y, roofY, style.wall());
        c.fill(box, roofY, roofY, style.trim());
        // High slit windows, then a cart-sized opening in the south wall.
        for (int x = box.x0() + 2; x < box.x1() - 1; x += 4) {
            c.pane(x, roofY - 2, box.z0(), Direction.Axis.X, Blocks.IRON_BARS);
            c.pane(x, roofY - 2, box.z1(), Direction.Axis.X, Blocks.IRON_BARS);
        }
        for (int z = box.z0() + 2; z < box.z1() - 1; z += 4) {
            c.pane(box.x0(), roofY - 2, z, Direction.Axis.Z, Blocks.IRON_BARS);
            c.pane(box.x1(), roofY - 2, z, Direction.Axis.Z, Blocks.IRON_BARS);
        }
        int doorX = box.centerX();
        c.fill(doorX - 1, GROUND_Y, box.z1(), doorX + 1, GROUND_Y + 3, box.z1(), Blocks.AIR);
        Rect in = box.inset(1);
        for (int x = in.x0() + 2; x < in.x1(); x += 6) {
            for (int z = in.z0() + 2; z < in.z1(); z += 6) {
                c.set(x, roofY - 1, z, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
        }
    }

    private static void warehouse(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 7 || box.depth() < 7) {
            OpenLots.junkyard(c, lot);
            return;
        }
        int height = 6 + CityHash.range(lot.hash(), 5);
        hall(c, box, height, Style.forDistrict(lot.district(), lot.hash()));
        Rect in = box.inset(1);
        for (int x = in.x0() + 1; x <= in.x1() - 1; x += 3) {
            if (Math.abs(x - box.centerX()) <= 1) {
                continue;
            }
            for (int z = in.z0() + 1; z <= in.z1() - 3; z++) {
                long h = c.hash(x, STREET_Y, z);
                int stack = 1 + CityHash.range(h, 3);
                for (int s = 0; s < stack; s++) {
                    c.set(x, GROUND_Y + s, z, Blocks.SPRUCE_PLANKS);
                }
                if (CityHash.range(h >>> 16, 9) == 0) {
                    c.barrel(x, GROUND_Y + stack, z, "warehouse");
                }
            }
        }
        c.chest(in.x0(), GROUND_Y, in.z1(), Direction.EAST, "warehouse");
    }

    private static void factory(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 9 || box.depth() < 9) {
            OpenLots.junkyard(c, lot);
            return;
        }
        int height = 9 + CityHash.range(lot.hash(), 6);
        Style style = lot.hash() < 0 ? Style.RUST : Style.SOOT;
        hall(c, box, height, style);
        Rect in = box.inset(1);
        int roofY = GROUND_Y + height;

        // Furnace lines along the north wall, with scrap heaped along the west wall.
        for (int x = in.x0() + 1; x <= in.x1() - 1; x += 2) {
            c.facing(x, GROUND_Y, in.z0(), Direction.SOUTH, CityHash.range(c.hash(x, 1, in.z0()), 3) == 0 ? Blocks.BLAST_FURNACE : Blocks.FURNACE);
        }
        for (int z = in.z0() + 2; z <= in.z1() - 3; z++) {
            c.set(in.x0(), GROUND_Y, z, com.dorohedoro.hole.registry.ModBlocks.INDUSTRIAL_DEBRIS.get());
        }
        c.set(in.x1(), GROUND_Y, in.z1() - 3, Blocks.ANVIL);
        c.chest(in.x1(), GROUND_Y, in.z1() - 2, Direction.WEST, "factory");

        // Smokestacks are the district's skyline.
        int stacks = 1 + CityHash.range(lot.hash() >>> 8, 3);
        for (int i = 0; i < stacks; i++) {
            int x = box.x0() + 3 + i * Math.max(5, (box.width() - 6) / Math.max(1, stacks));
            if (x + 1 > box.x1() - 2) {
                break;
            }
            int z = box.z0() + 3;
            int stackTop = roofY + 16 + CityHash.range(lot.hash() >>> (16 + i), 26);
            c.fill(x - 1, roofY + 1, z - 1, x + 1, stackTop, z + 1, Mix.of(Blocks.BRICKS, 3, Blocks.DEEPSLATE_BRICKS, 2, Blocks.CRACKED_DEEPSLATE_BRICKS, 1));
            c.fill(x, roofY + 1, z, x, stackTop, z, Blocks.AIR);
            c.fill(x - 1, stackTop - 2, z - 1, x + 1, stackTop - 2, z + 1, Blocks.WAXED_OXIDIZED_CUT_COPPER);
            c.set(x, stackTop - 2, z, Blocks.AIR);
        }
        // Roof pipework running the length of the hall.
        int pipeZ = box.z1() - 3;
        c.fill(box.x0() + 1, roofY + 1, pipeZ, box.x1() - 1, roofY + 2, pipeZ, Blocks.WAXED_OXIDIZED_COPPER);
    }
}
