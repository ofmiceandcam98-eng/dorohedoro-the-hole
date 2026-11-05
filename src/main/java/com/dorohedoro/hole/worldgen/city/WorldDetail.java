package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Detail pass v1. Extra architectural mass, shop signage, weathered streets and district props.
 *
 * <p>Completely stateless: inputs are the persistent world seed, absolute coordinates and a
 * per-world version flag. No cross-chunk reads, random streams, changes to caves or vanilla ores,
 * and no imports from somebody else's Minecraft build. Existing worlds keep detail version 0.
 * All work is clipped by Canvas to the target chunk.
 */
final class WorldDetail {
    private WorldDetail() {}

    /** Change floorplate sizes in fresh worlds so not every facade ends on the same grid. */
    static Rect footprint(Canvas c, Lot lot) {
        Rect standard = lot.rect().inset(1);
        if (!c.hasWorldDetail() || lot.rect().width() < 17 || lot.rect().depth() < 15) return standard;
        int type = CityHash.range(lot.hash() ^ 0x5A114BCD998L, 7);
        int west = type == 0 || type == 3 ? 2 : 1;
        int east = type == 1 || type == 4 ? 3 : 1;
        int north = type == 2 || type == 5 ? 2 : 1;
        int south = type == 3 || type == 6 ? 3 : 1;
        Rect result = new Rect(lot.rect().x0() + west, lot.rect().z0() + north,
                lot.rect().x1() - east, lot.rect().z1() - south);
        Rect resultV1 = result.width() >= 10 && result.depth() >= 9 ? result : standard;
        return c.hasArchitectureVariation() ? ArchitecturePass13.footprint(c, lot, resultV1) : resultV1;
    }

    /** Distinct silhouettes: parapets, arcades, lintels, service ladders and mechanical roofs. */
    static void tower(Canvas c, Rect b, int floors, Style style, long hash, Tower.Roof roof) {
        int top = STREET_Y + floors * Tower.STOREY;
        int variant = CityHash.range(hash ^ 0xD0A0D0L, 6);
        BlockState cornice = style.trim().pick(hash);
        // Every facade has a base, window lintels, and vertical pilasters. Not just flat walls.
        for (int k = 0; k < floors; k++) {
            int base = STREET_Y + k * Tower.STOREY;
            if (k > 0 && (variant + k) % 3 != 0) {
                for (int x = b.x0() + 1; x < b.x1(); x++) {
                    c.set(x, base + 1, b.z1() + 1, cornice);
                    if (variant % 2 == 0) c.set(x, base + 1, b.z0() - 1, cornice);
                }
            }
            // Window hoods along the front, at alternating spans. Kept above window openings.
            for (int x = b.x0() + 2; x <= b.x1() - 2; x += 4) {
                if ((x + k + variant) % 3 != 0) {
                    c.set(x, base + 4, b.z1() + 1, cornice);
                    if (variant == 2 || variant == 5) c.set(x, base + 4, b.z0() - 1, cornice);
                }
            }
        }
        // Vertical drainpipes on a side/rear wall, leaving all windows and doors unobstructed.
        if (variant != 0) {
            int px = b.x1() + 1;
            int pz = b.z0() + 1;
            for (int y = GROUND_Y + 1; y <= top; y++) c.set(px, y, pz, Blocks.CHAIN);
            c.set(px, top + 1, pz, Blocks.WAXED_WEATHERED_COPPER);
        }
        // Parapet changes roofline while allowing access and existing farms and rooftop machinery.
        if (variant != 1) {
            BlockState parapet = variant >= 4 ? Blocks.IRON_BARS.defaultBlockState() : cornice;
            for (int x = b.x0(); x <= b.x1(); x++) {
                c.set(x, top + 2, b.z0(), parapet);
                if ((x - b.x0()) % 4 != 2) c.set(x, top + 2, b.z1(), parapet);
            }
            for (int z = b.z0() + 1; z < b.z1(); z++) {
                if ((z - b.z0()) % 3 != 1) {
                    c.set(b.x0(), top + 2, z, parapet);
                    c.set(b.x1(), top + 2, z, parapet);
                }
            }
        }
        // A non-rectangular additional roof mass, placed away from the north stair landing.
        if (roof == Tower.Roof.PLAIN && b.width() > 14 && b.depth() > 11) {
            if (variant == 1 || variant == 4) {
                int x = b.x1() - 5, z = b.z1() - 5;
                c.fill(x, top + 1, z, x + 3, top + 4, z + 3, Blocks.WAXED_WEATHERED_CUT_COPPER);
                c.fill(x + 1, top + 2, z + 1, x + 2, top + 4, z + 2, Blocks.AIR);
                c.set(x + 1, top + 5, z + 1, Blocks.IRON_TRAPDOOR);
            } else if (variant == 3) {
                for (int x = b.x0() + 8; x <= b.x1() - 3; x += 3) {
                    c.set(x, top + 1, b.z1() - 3, Blocks.CAULDRON);
                }
            }
        }
        // Balconies are on the outer half of the facade, not across the main door.
        if (floors >= 3 && b.width() >= 13 && (variant == 0 || variant == 3)) {
            int sideX = b.x0() + 2;
            for (int k = 1; k < floors; k += 2) {
                int floorY = STREET_Y + k * Tower.STOREY;
                c.fill(sideX, floorY + 1, b.z1() + 1, sideX + 3, floorY + 1, b.z1() + 1, Blocks.DARK_OAK_SLAB);
                for (int x = sideX; x <= sideX + 3; x++) {
                    c.set(x, floorY + 2, b.z1() + 1, Blocks.IRON_BARS);
                }
            }
        }
        // Building-style transitions also influence roof profiles, rather than using only one kind.
    }

    /** More shop identity without copying real Minecraft builders' signs or builds. */
    static void shopfront(Canvas c, Rect b, long hash) {
        int x = b.centerX();
        int variant = CityHash.range(hash >>> 23, 5);
        String[] fronts = {"REPAIRS", "NOODLES", "SALVAGE", "SUPPLIES", "GYOZA"};
        int signX = x + (x + 3 < b.x1() ? 3 : -3);
        // Give the sign an actual wall backing instead of attaching it to a window pane.
        c.set(signX, GROUND_Y + 1, b.z1(), Blocks.BRICKS);
        c.sign(signX, GROUND_Y + 1, b.z1() + 1, Direction.SOUTH, Blocks.SPRUCE_WALL_SIGN,
                fronts[variant], "OPEN", "", "");
        if (b.width() >= 13) {
            // Support the awning at its corners so it does not float in midair.
            for (int dx : new int[]{-4, 4}) {
                for (int y = GROUND_Y; y <= GROUND_Y + 2; y++) {
                    c.set(x + dx, y, b.z1() + 1, Blocks.SPRUCE_FENCE);
                }
            }
            if (variant == 1 || variant == 4) {
                c.set(b.x0() + 1, GROUND_Y, b.z1() + 1, Blocks.CAULDRON);
                c.set(b.x1() - 1, GROUND_Y, b.z1() + 1, Blocks.COMPOSTER);
            }
        }
    }

    /** Industrial buildings have different service and skylight profiles from housing. */
    static void hallExterior(Canvas c, Rect b, int roofY, Style style) {
        long h = c.hash(b.centerX(), roofY, b.centerZ());
        int profile = CityHash.range(h, 4);
        for (int x = b.x0() + 2; x < b.x1() - 1; x += 5) {
            c.set(x, roofY + 1, b.z1(), Blocks.IRON_BARS);
            if (profile == 1 || profile == 3) c.set(x, roofY + 2, b.z1(), Blocks.IRON_BARS);
        }
        // Side-mounted duct with an elbow and a blocky weathered metal vent.
        if (b.depth() >= 13) {
            int z = b.z0() + 3;
            c.fill(b.x1() + 1, GROUND_Y + 2, z, b.x1() + 1, GROUND_Y + 5, z, Blocks.WAXED_WEATHERED_CUT_COPPER);
            c.fill(b.x1() + 1, GROUND_Y + 5, z, b.x1() + 1, GROUND_Y + 5, z + 4, Blocks.WAXED_WEATHERED_CUT_COPPER);
            c.set(b.x1() + 1, GROUND_Y + 6, z + 4, Blocks.IRON_TRAPDOOR);
        }
        // High loft windows/roof lights never encroach on the warehouse's central aisle.
        if (profile == 0 && b.width() > 16) {
            for (int x = b.x0() + 4; x < b.x1() - 3; x += 6)
                c.set(x, roofY + 1, b.centerZ(), Blocks.GRAY_STAINED_GLASS);
        }
    }

    /** Minimal edge furniture that doesn't obstruct stairs, beds, doors or the central aisle. */
    static void roomDetails(Canvas c, Rect in, int floorY, int storey, long hash) {
        if (in.width() < 10 || in.depth() < 9) return;
        long h = CityHash.of(hash, in.x0(), storey, in.z0());
        int x = in.x1() - 1, z = in.z1() - 2;
        if (!c.contains(x, z)) return; // Canvas.get must only read this chunk.
        int roll = CityHash.range(h, 5);
        if (roll == 0 && c.get(x, floorY + 1, z).isAir()) c.set(x, floorY + 1, z, Blocks.BROWN_CARPET);
        if (roll == 1 && c.get(x, floorY + 1, z).isAir()) c.set(x, floorY + 1, z, Blocks.GRAY_CARPET);
        if (roll == 2 && c.get(x, floorY + 3, z).isAir()) c.set(x, floorY + 3, z, Blocks.CHAIN);
        if (roll == 3 && c.get(x, floorY + 3, z).isAir()) c.set(x, floorY + 3, z, Blocks.IRON_TRAPDOOR);
        // Secondary ceiling fixture away from the stairwell and main central light.
        int bx = in.x1() - 3, bz = in.z1() - 2;
        if (c.contains(bx, bz) && CityHash.range(h >>> 8, 4) == 0
                && c.get(bx, floorY + 3, bz).isAir()) {
            c.set(bx, floorY + 3, bz, Blocks.IRON_TRAPDOOR);
        }
    }

    /**
     * Perimeter detail without occupying building interior or front door approaches.
     * Yard clutter stays inside the lot, so neighboring plots can never overwrite one another.
     */
    static void lotYard(Canvas c, Lot lot) {
        if (!lot.type().enclosed) return;
        if (lot.type() == LotType.HUNGRY_BUG || lot.type() == LotType.STARTER_SHELTER
                || lot.type() == LotType.HOSPITAL || lot.type() == LotType.DEPARTMENT_STORE
                || lot.type() == LotType.RESEARCH_MANOR) return;
        Rect r = lot.rect();
        if (r.width() < 13 || r.depth() < 12) return;
        int x = r.x0(), z = r.z0();
        long h = lot.hash();
        int kind = CityHash.range(h >>> 12, 5);
        // A single courtyard corner: spare crates, a rain barrel, muddy equipment or an abandoned cart.
        switch (kind) {
            case 0 -> {
                c.set(x, GROUND_Y, z, Blocks.SPRUCE_PLANKS);
                c.set(x, GROUND_Y + 1, z, Blocks.SPRUCE_SLAB);
                c.set(x + 1, GROUND_Y, z, Blocks.COMPOSTER);
            }
            case 1 -> {
                c.set(x, GROUND_Y, z, Blocks.CAULDRON);
                c.set(x, GROUND_Y, z + 1, Blocks.IRON_BARS);
            }
            case 2 -> {
                c.set(x, GROUND_Y, z, Blocks.COMPOSTER);
                c.set(x + 1, GROUND_Y, z, Blocks.COARSE_DIRT);
            }
            case 3 -> {
                c.set(x, GROUND_Y, z, Blocks.IRON_BARS);
                c.set(x, GROUND_Y + 1, z, Blocks.CHAIN);
            }
            default -> c.set(x, GROUND_Y, z, Blocks.HAY_BLOCK);
        }
        // Different front-yard surfaces visibly soften the regular rectangle of the street grid.
        for (int step = 0; step < 3; step++) {
            int xx = r.x1() - step;
            if (Math.abs(xx - r.centerX()) <= 2) continue; // do not block door access
            if (CityHash.range(h >>> (24 + step * 3), 3) == 0) {
                c.set(xx, STREET_Y, r.z1(), Blocks.PACKED_MUD);
            }
        }
    }

    /** Mixed surfaces, actual junction markings, and occasional industrial drainage. */
    static void streetSurface(Canvas c, CityPlan plan, int x, int z, Line xb, Line zb) {
        // Never touch city interiors, vanilla tunnels, underground ores, canal beds or rail states.
        int roll = CityHash.range(CityHash.of(plan.seed, 0x51E27D111L, Math.floorDiv(x, 6), Math.floorDiv(z, 6)), 30);
        if (c.hasDistrictIdentity()) {
            DistrictIdentity18.streetSurface(c, plan, x, z, xb, zb, roll);
            return;
        }
        if (xb == null && zb == null) {
            if (roll == 0 && (x + z) % 4 != 0) c.set(x, STREET_Y, z, Blocks.PACKED_MUD);
            else if (roll == 1 && Math.floorMod(x * 3 + z * 5, 7) == 0)
                c.set(x, STREET_Y, z, Blocks.POLISHED_BLACKSTONE);
            return;
        }
        if (xb != null && zb != null) {
            // High-contrast junction markings, but not across tram lines or waterways.
            if (xb.kind() != Line.Kind.CANAL && zb.kind() != Line.Kind.CANAL
                    && xb.kind() != Line.Kind.TRAM && zb.kind() != Line.Kind.TRAM
                    && Math.abs(x - xb.center() - plan.xAvenueOffset(xb, z)) >= 2
                    && Math.abs(z - zb.center() - plan.zAvenueOffset(zb, x)) >= 2) {
                if (Math.floorMod(x + z, 4) == 0) c.set(x, STREET_Y, z, Blocks.LIGHT_GRAY_CONCRETE);
            }
            return;
        }
        Line band = xb != null ? xb : zb;
        if (band.kind() == Line.Kind.CANAL) return;
        int offset = xb != null ? Math.abs(x - xb.center() - plan.xAvenueOffset(xb, z))
                : Math.abs(z - zb.center() - plan.zAvenueOffset(zb, x));
        int along = xb != null ? z : x;
        int curb = band.halfWidth() - 2;
        if (offset == curb + 1 && Math.floorMod(along, 11) == 4) {
            c.set(x, STREET_Y, z, Blocks.IRON_TRAPDOOR);
        } else if (offset <= curb && band.kind() != Line.Kind.TRAM
                && roll <= 2 && Math.floorMod(along + offset, 7) == 0) {
            c.set(x, STREET_Y, z, roll == 0 ? Blocks.PACKED_MUD : Blocks.POLISHED_BLACKSTONE);
        }
    }

    /** Rare small props on the outermost sidewalk; route center and tram traffic remain open. */
    static void streetFixtures(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines) {
        for (int x = c.minX; x <= c.maxX; x++) {
            for (int z = c.minZ; z <= c.maxZ; z++) {
                Line xb = bandAt(xLines, x, z, plan, true);
                Line zb = bandAt(zLines, z, x, plan, false);
                if ((xb == null) == (zb == null)) continue;
                Line band = xb != null ? xb : zb;
                int side = xb != null ? Math.abs(x - xb.center() - plan.xAvenueOffset(xb, z))
                        : Math.abs(z - zb.center() - plan.zAvenueOffset(zb, x));
                if (side != band.halfWidth()) continue;
                int along = xb != null ? z : x;
                long h = CityHash.of(plan.seed, 0xE53114DL, x, z);
                if (Math.floorMod(along + band.center(), 37) != 19) continue;
                // Keep existing streetlight positions, sewer stairways, canals and active paths clear.
                if (Math.floorMod(along + band.center(), 15) == 0) continue;
                if (band.kind() == Line.Kind.CANAL) continue;
                District d = plan.districtAt(x, z);
                Block prop = c.hasDistrictIdentity() ? DistrictIdentity18.edgeFixture(d, h) : switch (d) {
                    case INDUSTRIAL, WATERFRONT -> Blocks.CAULDRON;
                    case MARKET -> Blocks.COMPOSTER;
                    case DERELICT, OUTSKIRTS -> Blocks.HAY_BLOCK;
                    case GRAVEYARD -> Blocks.COBWEB;
                    case RESIDENTIAL -> Blocks.SPRUCE_FENCE;
                };
                if (CityHash.range(h, 5) != 0) c.set(x, GROUND_Y, z, prop);
                else c.set(x, GROUND_Y, z, Blocks.IRON_BARS);
            }
        }
    }

    private static Line bandAt(List<Line> lines, int coordinate, int along, CityPlan plan, boolean xAxis) {
        for (Line line : lines) {
            int bend = xAxis ? plan.xAvenueOffset(line, along) : plan.zAvenueOffset(line, along);
            if (Math.abs(coordinate - line.center() - bend) <= line.halfWidth()) return line;
        }
        return null;
    }
}
