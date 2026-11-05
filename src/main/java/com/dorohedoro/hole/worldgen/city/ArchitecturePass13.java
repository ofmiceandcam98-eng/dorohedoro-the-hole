package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 13: building massing and contextual street treatment at multiple scales.
 * Only invoked for the per-save {@code world_detail_version=2} profile.
 *
 * <p>All protrusions are on the building exterior, with stairs and front entrances kept free.
 * Nothing is placed in the street's navigable center; no natural ore/cave blocks are changed.
 */
final class ArchitecturePass13 {
    private ArchitecturePass13() {}

    /** Varied courtyard/front setbacks. This never increases the footprint beyond its lot. */
    static Rect footprint(Canvas c, Lot lot, Rect previous) {
        if (lot.rect().width() < 18 || lot.rect().depth() < 17) return previous;
        int kind = CityArchitecture.setback(c.seed(), lot.rect().centerX(), lot.rect().centerZ(), lot.hash());
        CityArchitecture.Margins m = CityArchitecture.margins(kind);
        Rect r = new Rect(lot.rect().x0() + m.west(), lot.rect().z0() + m.north(),
                lot.rect().x1() - m.east(), lot.rect().z1() - m.south());
        // Preserve the four-stair flight, interiors, and a two-block-wide access route.
        return Tower.fits(r) && r.width() >= 11 && r.depth() >= 10 ? r : previous;
    }

    static void tower(Canvas c, Rect b, int floors, Style style, long hash, Tower.Roof roof) {
        if (b.width() < 12 || b.depth() < 10) return;
        int facade = CityArchitecture.facade(c.seed(), b.centerX(), b.centerZ(), hash);
        int neighborhood = CityArchitecture.neighborhood(c.seed(), b.centerX(), b.centerZ());
        Block accent = switch (neighborhood) {
            case 0 -> Blocks.MUD_BRICKS;
            case 1 -> Blocks.WAXED_WEATHERED_CUT_COPPER;
            case 2 -> Blocks.DARK_OAK_PLANKS;
            case 3 -> Blocks.CRACKED_DEEPSLATE_BRICKS;
            case 4 -> Blocks.GRAY_TERRACOTTA;
            default -> Blocks.WAXED_OXIDIZED_CUT_COPPER;
        };
        switch (facade) {
            case 0 -> projectingBays(c, b, floors, accent);
            case 1 -> porchArcade(c, b, accent);
            case 2 -> verticalRibs(c, b, floors, accent);
            case 3 -> balconyStack(c, b, floors, accent);
            case 4 -> workshopFront(c, b, floors, accent);
            default -> oldBrickBands(c, b, floors, accent);
        }
        roofMassing(c, b, floors, roof, hash, accent);
    }

    /** Narrow vertical bay windows project just one block into the lot's frontage. */
    private static void projectingBays(Canvas c, Rect b, int floors, Block accent) {
        if (floors < 2 || b.width() < 16) return;
        for (int x : new int[]{b.x0() + 3, b.x1() - 3}) {
            for (int k = 1; k < floors; k++) {
                int y = STREET_Y + k * Tower.STOREY;
                c.fill(x - 1, y + 1, b.z1() + 1, x + 1, y + 1, b.z1() + 1, accent);
                c.set(x, y + 2, b.z1() + 1, Blocks.GRAY_STAINED_GLASS);
                c.fill(x - 1, y + 3, b.z1() + 1, x + 1, y + 3, b.z1() + 1, accent);
            }
        }
    }

    /** A deep ground-level lintel and side piers, but no blocks in front of the door. */
    private static void porchArcade(Canvas c, Rect b, Block accent) {
        for (int x : new int[]{b.x0() + 2, b.x1() - 2}) {
            c.fill(x, GROUND_Y, b.z1() + 1, x, GROUND_Y + 3, b.z1() + 1, accent);
        }
        for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
            c.set(x, GROUND_Y + 4, b.z1() + 1, accent);
        }
    }

    private static void verticalRibs(Canvas c, Rect b, int floors, Block accent) {
        int top = STREET_Y + floors * Tower.STOREY;
        for (int x = b.x0() + 2; x <= b.x1() - 2; x += 5) {
            // Skip ground floor and keep main entrance clear.
            c.fill(x, GROUND_Y + 4, b.z1() + 1, x, top, b.z1() + 1, accent);
            for (int y = GROUND_Y + 6; y <= top; y += 8)
                c.set(x, y, b.z1() + 1, Blocks.IRON_TRAPDOOR);
        }
    }

    /** External balconies are placed well away from the central front door. */
    private static void balconyStack(Canvas c, Rect b, int floors, Block accent) {
        if (floors < 3 || b.width() < 15) return;
        for (int k = 1; k < floors; k += 2) {
            int y = STREET_Y + k * Tower.STOREY;
            c.fill(b.x1() - 5, y + 1, b.z1() + 1, b.x1() - 2, y + 1, b.z1() + 1, accent);
            c.set(b.x1() - 5, y + 2, b.z1() + 1, Blocks.IRON_BARS);
            c.set(b.x1() - 2, y + 2, b.z1() + 1, Blocks.IRON_BARS);
        }
    }

    private static void workshopFront(Canvas c, Rect b, int floors, Block accent) {
        for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
            c.set(x, GROUND_Y + 4, b.z1() + 1, accent);
        }
        for (int k = 1; k < floors; k++) {
            int y = STREET_Y + k * Tower.STOREY;
            c.fill(b.x0() + 2, y + 1, b.z1() + 1, b.x0() + 4, y + 1, b.z1() + 1, Blocks.IRON_TRAPDOOR);
            c.fill(b.x1() - 4, y + 1, b.z1() + 1, b.x1() - 2, y + 1, b.z1() + 1, Blocks.IRON_TRAPDOOR);
        }
    }

    private static void oldBrickBands(Canvas c, Rect b, int floors, Block accent) {
        for (int k = 1; k <= floors; k += 2) {
            int y = STREET_Y + k * Tower.STOREY;
            for (int x = b.x0() + 1; x < b.x1(); x++) {
                if (Math.floorMod(x + k, 5) != 0) c.set(x, y, b.z1() + 1, accent);
            }
        }
    }

    /** Profile-specific roof *silhouettes* rather than only extra roof color. */
    private static void roofMassing(Canvas c, Rect b, int floors, Tower.Roof roof, long hash, Block accent) {
        if (roof == Tower.Roof.GARDEN || roof == Tower.Roof.TANK) return;
        int top = STREET_Y + floors * Tower.STOREY;
        int profile = CityArchitecture.roof(c.seed(), b.centerX(), b.centerZ(), hash);
        int x0 = b.x1() - 6;
        int z0 = b.z1() - 5;
        if (b.width() < 16 || b.depth() < 13) return;
        switch (profile) {
            case 0 -> { // stepped mechanical penthouse, leaving north-west roof stair landing free
                c.fill(x0, top + 2, z0, x0 + 4, top + 3, z0 + 3, accent);
                c.fill(x0 + 1, top + 4, z0 + 1, x0 + 3, top + 5, z0 + 2, accent);
                c.set(x0 + 2, top + 3, z0 + 4, Blocks.IRON_TRAPDOOR);
            }
            case 1 -> { // rear chimney pair and high connecting duct
                c.fill(x0, top + 2, z0, x0 + 1, top + 7, z0 + 1, Blocks.BRICKS);
                c.fill(x0 + 3, top + 2, z0 + 2, x0 + 4, top + 4, z0 + 3, Blocks.WAXED_WEATHERED_COPPER);
                c.fill(x0 + 1, top + 5, z0, x0 + 3, top + 5, z0, accent);
            }
            case 2 -> { // irregular crown and tiny service skylights
                for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
                    if (Math.floorMod(x - b.x0(), 6) < 4) c.set(x, top + 3, b.z1(), accent);
                }
                c.set(x0 + 1, top + 2, z0 + 1, Blocks.GRAY_STAINED_GLASS);
                c.set(x0 + 3, top + 2, z0 + 1, Blocks.GRAY_STAINED_GLASS);
            }
            case 3 -> { // freestanding mechanical frame
                for (int x : new int[]{x0, x0 + 4})
                    for (int z : new int[]{z0, z0 + 3})
                        c.fill(x, top + 2, z, x, top + 5, z, Blocks.IRON_BARS);
                c.fill(x0, top + 6, z0, x0 + 4, top + 6, z0 + 3, Blocks.WAXED_OXIDIZED_CUT_COPPER);
            }
            default -> { // roof vent banks
                for (int x = x0; x <= x0 + 4; x += 2) {
                    c.fill(x, top + 2, z0 + 1, x, top + 4, z0 + 1, accent);
                    c.set(x, top + 5, z0 + 1, Blocks.IRON_TRAPDOOR);
                }
            }
        }
    }

    /** Distinctive shop overhangs and utilities, with the middle three entrance blocks open. */
    static void shopfront(Canvas c, Rect b, long hash) {
        if (b.width() < 12) return;
        int focus = CityArchitecture.neighborhood(c.seed(), b.centerX(), b.centerZ());
        int shape = CityArchitecture.facade(c.seed(), b.centerX(), b.centerZ(), hash);
        Block trim = focus % 2 == 0 ? Blocks.WAXED_WEATHERED_CUT_COPPER : Blocks.DARK_OAK_PLANKS;
        if (shape % 2 == 0) {
            // A small layered shop canopy, set high above the walkable door approach.
            for (int x = b.x0() + 2; x <= b.x1() - 2; x++) {
                c.set(x, GROUND_Y + 4, b.z1() + 1, trim);
                if (Math.floorMod(x - b.x0(), 4) == 0)
                    c.set(x, GROUND_Y + 5, b.z1() + 1, Blocks.IRON_TRAPDOOR);
            }
        } else {
            // Shuttered side displays. No extra block in front of the central door.
            for (int x : new int[]{b.x0() + 2, b.x1() - 2}) {
                c.set(x, GROUND_Y + 1, b.z1() + 1, Blocks.DARK_OAK_TRAPDOOR);
                c.set(x, GROUND_Y + 2, b.z1() + 1, Blocks.DARK_OAK_TRAPDOOR);
                c.set(x, GROUND_Y + 3, b.z1() + 1, trim);
            }
        }
    }

    /** Three distinctive warehouse roof treatments; roof aisle and cart entrance remain untouched. */
    static void industrialRoof(Canvas c, Rect b, int roofY, long hash) {
        if (b.width() < 14 || b.depth() < 12) return;
        int profile = CityArchitecture.roof(c.seed(), b.centerX(), b.centerZ(), hash) % 3;
        if (profile == 0) { // staggered factory saw-tooth crest
            for (int x = b.x0() + 3; x <= b.x1() - 3; x += 6) {
                c.fill(x, roofY + 1, b.z0() + 2, x, roofY + 3, b.z1() - 2, Blocks.WAXED_WEATHERED_CUT_COPPER);
                c.fill(x + 1, roofY + 1, b.z0() + 2, x + 1, roofY + 1, b.z1() - 2, Blocks.GRAY_STAINED_GLASS);
            }
        } else if (profile == 1) { // crane gantry on east edge
            int x = b.x1() - 3;
            for (int z = b.z0() + 2; z <= b.z1() - 2; z += 5)
                c.fill(x, roofY + 1, z, x, roofY + 4, z, Blocks.IRON_BARS);
            c.fill(x, roofY + 5, b.z0() + 2, x, roofY + 5, b.z1() - 2, Blocks.WAXED_OXIDIZED_CUT_COPPER);
        } else { // staggered rooftop vent plenum
            for (int z = b.z0() + 3; z < b.z1() - 2; z += 5) {
                c.fill(b.x1() - 4, roofY + 1, z, b.x1() - 2, roofY + 3, z + 1, Blocks.GRAY_TERRACOTTA);
                c.set(b.x1() - 3, roofY + 2, z + 2, Blocks.IRON_TRAPDOOR);
            }
        }
    }

    /** Neighborhood-specific asphalt wear without blocking roads, streetlamps or rails. */
    static void streetSurface(Canvas c, CityPlan plan, int x, int z, Line xb, Line zb) {
        if (xb != null && xb.kind() == Line.Kind.CANAL || zb != null && zb.kind() == Line.Kind.CANAL) return;
        if (xb != null && xb.kind() == Line.Kind.TRAM || zb != null && zb.kind() == Line.Kind.TRAM) return;
        int theme = CityArchitecture.neighborhood(plan.seed, x, z);
        long h = CityHash.of(plan.seed, 0x57EE713L, Math.floorDiv(x, 5), Math.floorDiv(z, 5));
        if (xb == null && zb == null) {
            // A coherent local road material, but patches remain sparse instead of a grid of tiles.
            if (CityHash.range(h, 23) == 0 && Math.floorMod(x + z, 5) == 0)
                c.set(x, STREET_Y, z, theme % 2 == 0 ? Blocks.MUD_BRICKS : Blocks.CRACKED_STONE_BRICKS);
            return;
        }
        if (xb != null && zb != null) return; // existing intersection crossings own their markings
        Line band = xb != null ? xb : zb;
        int fromCenter = xb != null ? Math.abs(x - band.center() - plan.xAvenueOffset(band, z))
                : Math.abs(z - band.center() - plan.zAvenueOffset(band, x));
        int along = xb != null ? z : x;
        // Sidewalk brick strips, always level with the existing walkable surface.
        if (fromCenter == band.halfWidth() - 1 && Math.floorMod(along, 17) < 4) {
            c.set(x, STREET_Y, z, theme < 3 ? Blocks.STONE_BRICKS : Blocks.MUD_BRICKS);
        } else if (fromCenter < band.halfWidth() - 2 && CityHash.range(h, 38) == 0
                && Math.floorMod(along, 5) == 0) {
            c.set(x, STREET_Y, z, Blocks.GRAY_TERRACOTTA);
        }
    }
}
