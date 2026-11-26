package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.world.level.block.Blocks;

/**
 * Pass 60: finish the public-facing side of the industrial circulation loop.
 * Existing halls have a three-wide, four-high loading doorway in their south
 * wall, but subsequent yard-furnishing passes could leave the approach blocked.
 * Reserve a real three-wide loading apron and (where setback permits) a three-
 * wide transverse path joining the hall's loading entrance to its detached
 * workshop's service court. A small parcel simply gets a direct loading apron.
 *
 * No street, rail, canal or other parcel is touched; no new lights or loot.
 * Writes are reproducible and chunk-clipped through Canvas. v42 saved worlds
 * retain their exact older terrain and building generation.
 */
final class FreightApproaches60 {
    private FreightApproaches60() {}

    static Rect loading(Rect yard, Rect hall) {
        if (!yard.isValid() || !hall.isValid() || hall.width() < 7 || hall.depth() < 7) return null;
        int x = hall.centerX();
        Rect route = new Rect(x - 1, hall.z1() + 1, x + 1, yard.z1());
        if (!route.isValid() || route.width() != 3 || route.depth() < 1
                || !yard.contains(route.x0(), route.z0())
                || !yard.contains(route.x1(), route.z1())) return null;
        return route;
    }

    /** Linking both entries is possible only with an unobstructed 3-deep rear setback. */
    static Rect crossAccess(Rect yard, Rect hall, Rect workshopRoute, Rect loadingRoute) {
        if (workshopRoute == null || loadingRoute == null
                || hall.z1() > yard.z1() - 3
                || workshopRoute.z0() > yard.z1() - 2
                || workshopRoute.x1() + 1 >= loadingRoute.x0()) return null;
        Rect link = new Rect(workshopRoute.x0(), yard.z1() - 2,
                loadingRoute.x1(), yard.z1());
        if (!yard.contains(link.x0(), link.z0()) || !yard.contains(link.x1(), link.z1())
                || link.intersects(hall)) return null;
        return link;
    }

    static void build(Canvas c, Lot lot) {
        if (!c.hasFreightApproaches60()
                || (lot.type() != LotType.FACTORY && lot.type() != LotType.WAREHOUSE)) return;
        Rect yard = lot.rect();
        Rect original = StructuralVolumes14.industrialFootprint(c, lot, WorldDetail.footprint(c, lot));
        Rect hall = IndustrialAnnex58.mainFootprint(c, lot, original);
        // Match the actual warehouse/factory painter's fallback to junkyard.
        if (hall.width() < (lot.type() == LotType.FACTORY ? 9 : 7) || hall.depth() < 7) return;
        Rect loading = loading(yard, hall);
        if (loading == null) return;

        // Applied AFTER UrbanFabric25, derelict yard furnishings, and Pass59's
        // service court. Floor first; then four-high cargo clearance.
        paveAndClear(c, loading);
        // Clear the interior threshold, not the three-wide door blocks.
        int center = hall.centerX();
        c.clear(new Rect(center - 1, hall.z1() - 1, center + 1, hall.z1() - 1),
                GROUND_Y, GROUND_Y + 3);

        // A second, orthogonal working route connects the two entrances only
        // where it does not eat into the hall or narrow another circulation way.
        if (c.hasWorkingServiceCourts59()
                && yard.width() >= 36 && yard.depth() >= 26) {
            Rect workshop = LinkedServiceYards56.shack(yard, hall, true);
            Rect fromWorkshop = WorkingServiceCourts59.approach(yard, hall, workshop);
            Rect link = crossAccess(yard, hall, fromWorkshop, loading);
            if (link != null) paveAndClear(c, link);
        }
    }

    private static void paveAndClear(Canvas c, Rect route) {
        c.fill(route, STREET_Y, STREET_Y, Blocks.POLISHED_ANDESITE);
        c.clear(route, GROUND_Y, GROUND_Y + 3);
    }
}
