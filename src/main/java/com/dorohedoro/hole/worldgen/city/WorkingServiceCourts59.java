package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.world.level.block.Blocks;

/**
 * Pass 59: the space between a west-side workshop and its industrial hall is
 * an actual service court, not an impassable strip of decorations. The
 * three-wide approach leads from the workshop's south entrance to the public
 * frontage. A wall-side wash/repair bench keeps the middle of that approach
 * empty. Both structures already belong to the same parcel; never cross
 * property lines or overwrite other buildings' entrances.
 *
 * Intentionally unlit, with no exterior reward chests. Every world write is
 * deterministic and chunk-clipped by Canvas, and only fresh v42 worlds opt in.
 */
final class WorkingServiceCourts59 {
    private WorkingServiceCourts59() {}

    static Rect approach(Rect yard, Rect main, Rect shack) {
        if (shack.width() < 5 || shack.depth() < 8) return null;
        if (LinkedServiceYards56.passage(yard, shack, main) == null) return null;
        int center = shack.centerX();
        Rect route = new Rect(center - 1, shack.z1() + 1, center + 1, yard.z1());
        if (!route.isValid() || route.depth() < 7
                || !yard.contains(route.x0(), route.z0())
                || !yard.contains(route.x1(), route.z1())
                || route.x1() >= main.x0()) return null;
        return route;
    }

    static void build(Canvas c, Lot lot) {
        if (!c.hasWorkingServiceCourts59()
                || (lot.type() != LotType.FACTORY && lot.type() != LotType.WAREHOUSE)
                || lot.rect().width() < 36 || lot.rect().depth() < 26) return;
        Rect base = StructuralVolumes14.industrialFootprint(c, lot, WorldDetail.footprint(c, lot));
        Rect main = IndustrialAnnex58.mainFootprint(c, lot, base);
        Rect shack = LinkedServiceYards56.shack(lot.rect(), main, true);
        Rect route = approach(lot.rect(), main, shack);
        if (route == null) return;

        // Paint last after optional yard clutter. The work route must stay free
        // of barrels, debris and low overhead blocks even in messy districts.
        c.fill(route, STREET_Y, STREET_Y, Blocks.POLISHED_ANDESITE);
        c.clear(route, GROUND_Y, GROUND_Y + 2);

        // The workshop's existing south-facing door leads onto the route.
        // Clear the immediate threshold without replacing its door blocks.
        int center = shack.centerX();
        c.clear(new Rect(center, shack.z1() + 1, center, shack.z1() + 2),
                GROUND_Y, GROUND_Y + 2);

        // A narrow, purposeful wash-and-repair pocket against the hall wall.
        // It is separated from the three-wide route, so workstations never
        // become obstacles along the only exit to the street.
        int workX = main.x0() - 2;
        int workZ = shack.z1() + 3;
        if (workX > route.x1() + 1 && workZ + 4 < main.z1()
                && lot.rect().contains(workX, workZ + 4)) {
            Rect workArea = new Rect(workX, workZ, workX, workZ + 4);
            c.fill(workArea, STREET_Y, STREET_Y, Blocks.COBBLED_DEEPSLATE);
            c.clear(workArea, GROUND_Y, GROUND_Y + 2);
            c.set(workX, GROUND_Y, workZ, Blocks.CAULDRON);
            c.set(workX, GROUND_Y, workZ + 2,
                    lot.type() == LotType.FACTORY ? Blocks.SMITHING_TABLE : Blocks.STONECUTTER);
            c.set(workX, GROUND_Y, workZ + 4, Blocks.COMPOSTER);
            // The hall wall supports a small roof above the workstations;
            // the pedestrian route remains open to the sky and unlit.
            c.fill(workX, GROUND_Y + 3, workZ - 1,
                    main.x0() - 1, GROUND_Y + 3, workZ + 4,
                    Blocks.WAXED_WEATHERED_CUT_COPPER);
        }
    }
}
