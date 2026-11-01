package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Abandoned neighbourhoods, boarded shops, cracked brick shells and partial roof collapse.
 *
 * <p>All decisions use the lot hash and saved city world-detail version. This runs after ordinary
 * buildings have been fully constructed, so existing loot/furnishing logic still works and every
 * destructive pass has an explicit protected entrance, stairwell and street-side buffer.
 * No natural mining, sewer routing, vanilla world features, or existing saved worlds are changed.
 */
final class DerelictBuildings {
    private DerelictBuildings() {}

    static void decorate(Canvas c, CityPlan plan, Lot lot) {
        if (!c.hasWorldDetail()) return;
        // Named/story landmarks, primary hospitals, active agricultural sites and utility entrances
        // are never destroyed merely because their neighbourhood has weathered buildings.
        if (lot.type() == LotType.RUBBLE && lot.district() == District.DERELICT) {
            shellRemnant(c, lot);
            return;
        }
        if (!isEligible(lot.type())) return;
        int state = WorldDecayPolicy.severity(lot.hash(), lot.district());
        if (state == 0) return;

        Rect b = WorldDetail.footprint(c, lot);
        if (b.width() < 10 || b.depth() < 9) return;
        boolean tower = lot.type() != LotType.FACTORY && lot.type() != LotType.WAREHOUSE;
        int roofY = roofHeight(plan, lot);
        // Textures differ by building history: industrial rust, old masonry, soot, or patched stone.
        Block[] repairs = {Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.MOSSY_STONE_BRICKS,
                Blocks.CRACKED_STONE_BRICKS, Blocks.BROWN_TERRACOTTA,
                Blocks.WAXED_WEATHERED_CUT_COPPER};
        int ground = GROUND_Y;
        long hash = lot.hash();

        // A few high, irregular patches instead of identical striping on every building.
        int maxFloor = tower ? Math.max(1, (roofY - STREET_Y) / Tower.STOREY) : 1;
        for (int k = 0; k < maxFloor; k++) {
            int base = tower ? STREET_Y + k * Tower.STOREY : roofY - 4;
            for (int x = b.x0() + 2; x <= b.x1() - 2; x += 2) {
                long h = CityHash.of(hash ^ 0xD3E10A7EL, x, base, b.z1());
                int roll = CityHash.range(h, 100);
                if (roll >= (state == 1 ? 13 : state == 2 ? 26 : 35)) continue;
                // Doorway and its two-block clearance are never boarded or obstructed.
                if (k == 0 && Math.abs(x - b.centerX()) <= 3) continue;
                int y = Math.min(roofY - 1, base + 2);
                c.set(x, y, b.z1(), repairs[CityHash.range(h >>> 9, repairs.length)]);
                if (roll % 3 == 0 && y + 1 < roofY) {
                    c.set(x, y + 1, b.z1(), Blocks.CRACKED_STONE_BRICKS);
                }
            }
        }

        // Board a handful of lower windows and make disused shops immediately recognizable.
        if (state >= 2) {
            for (int x = b.x0() + 2; x < b.x1() - 1; x += 4) {
                if (Math.abs(x - b.centerX()) <= 3) continue;
                if (CityHash.range(CityHash.of(hash, 0xB0A4DL, x), 3) != 0) continue;
                c.set(x, ground + 1, b.z1(), Blocks.DARK_OAK_PLANKS);
                c.set(x, ground + 2, b.z1(), Blocks.SPRUCE_PLANKS);
            }
            if (lot.type() == LotType.SHOP) {
                int x = b.centerX();
                int signX = x + (x + 3 < b.x1() ? 3 : -3);
                c.sign(signX, ground + 1, b.z1() + 1, Direction.SOUTH,
                        Blocks.SPRUCE_WALL_SIGN, "CLOSED", "ABANDONED", "", "");
            }
        }

        // Displaced brickwork and rusted fixtures; do not place rubble in the entrance or stairs.
        int rx = b.x1() - 2, rz = b.z0() + 3;
        if (b.width() >= 13 && b.depth() >= 11) {
            c.set(rx, ground, rz, Blocks.COBBLED_DEEPSLATE_SLAB);
            if (state >= 2) {
                c.set(rx - 1, ground, rz, Blocks.CRACKED_STONE_BRICKS);
                c.set(rx, ground + 1, rz, Blocks.COBWEB);
            }
            if (state >= 3) {
                c.set(b.x1() - 1, ground + 2, b.z0() + 4, Blocks.IRON_BARS);
                c.set(b.x1() - 1, ground + 3, b.z0() + 4, Blocks.CHAIN);
            }
        }

        // In the most ruined buildings, expose one upper corner and perforate the *roof*.
        // This is far from the north-wall stair flight, front door and primary stair landing.
        if (state == 3 && b.width() >= 16 && b.depth() >= 13 && roofY - ground >= 6) {
            int cornerX = b.x1();
            int low = roofY - (tower ? 3 : 4);
            for (int z = b.z0() + 3; z <= b.z0() + 6 && z < b.z1() - 2; z++) {
                for (int y = low; y <= roofY + 1; y++) {
                    long h = CityHash.of(hash ^ 0xBADDFA11L, z, y);
                    if (CityHash.range(h, 5) <= 2) c.set(cornerX, y, z, Blocks.AIR);
                    else if (CityHash.range(h >>> 7, 3) == 0) {
                        c.set(cornerX, y, z, Blocks.CRACKED_DEEPSLATE_BRICKS);
                    }
                }
            }
            // Leave rooftop gardens/tanks intact: they provide survival food and water.
            boolean plainRoof = lot.type() == LotType.SHOP || lot.type() == LotType.WAREHOUSE
                    || lot.type() == LotType.FACTORY || LotPainter.roofFor(hash) == Tower.Roof.PLAIN;
            if (plainRoof) {
                int fromX = b.x1() - 5, fromZ = b.z1() - 6;
                for (int x = fromX; x <= b.x1() - 2; x++) {
                    for (int z = fromZ; z <= b.z1() - 3; z++) {
                        // Hole stays toward the back/right corner; no full-floor destruction.
                        long h = CityHash.of(hash ^ 0xC011A95EL, x, z);
                        if (CityHash.range(h, 5) <= 2) {
                            c.set(x, roofY, z, Blocks.AIR);
                            c.set(x, roofY + 1, z, Blocks.AIR);
                        }
                    }
                }
            }
        }
    }

    /** Taller rubble lots carry fragments of original buildings, not just flat gravel. */
    private static void shellRemnant(Canvas c, Lot lot) {
        Rect r = lot.rect().inset(2);
        if (r.width() < 11 || r.depth() < 10) return;
        long h = lot.hash();
        int height = 3 + CityHash.range(h >>> 5, 5);
        // North/west corner of a skeletal wall: its broken southern face is open to the lot.
        for (int y = GROUND_Y; y <= GROUND_Y + height; y++) {
            for (int x = r.x0(); x <= Math.min(r.x0() + 6, r.x1() - 3); x++) {
                if (CityHash.range(CityHash.of(h, x, y), 6) != 0 && y < GROUND_Y + height - 1) {
                    c.set(x, y, r.z0(), (x + y) % 3 == 0 ? Blocks.CRACKED_STONE_BRICKS : Blocks.MUD_BRICKS);
                }
            }
            for (int z = r.z0(); z <= Math.min(r.z0() + 6, r.z1() - 3); z++) {
                if (CityHash.range(CityHash.of(h ^ 0xDEADBEEFL, z, y), 5) != 0 && y < GROUND_Y + height) {
                    c.set(r.x0(), y, z, Blocks.CRACKED_DEEPSLATE_BRICKS);
                }
            }
        }
        c.set(r.x0() + 2, GROUND_Y, r.z0() + 2, Blocks.COBBLED_DEEPSLATE_SLAB);
        c.set(r.x0() + 3, GROUND_Y, r.z0() + 2, Blocks.MOSSY_COBBLESTONE);
        c.set(r.x0() + 1, GROUND_Y + 2, r.z0() + 1, Blocks.COBWEB);
    }

    private static boolean isEligible(LotType type) {
        return type == LotType.TENEMENT || type == LotType.APARTMENT || type == LotType.SHOP
                || type == LotType.WAREHOUSE || type == LotType.FACTORY;
    }

    private static int roofHeight(CityPlan plan, Lot lot) {
        long h = lot.hash();
        return switch (lot.type()) {
            case TENEMENT -> STREET_Y + Math.min(plan.settings.maxFloors(), 2 + CityHash.range(h, 3)) * Tower.STOREY;
            case APARTMENT -> STREET_Y + Math.min(plan.settings.maxFloors(),
                    5 + CityHash.range(h, Math.max(1, plan.settings.maxFloors() - 4))) * Tower.STOREY;
            case SHOP -> STREET_Y + (1 + CityHash.range(h, 3)) * Tower.STOREY;
            case WAREHOUSE -> GROUND_Y + 6 + CityHash.range(h, 5);
            case FACTORY -> GROUND_Y + 9 + CityHash.range(h, 6);
            default -> STREET_Y;
        };
    }

}
