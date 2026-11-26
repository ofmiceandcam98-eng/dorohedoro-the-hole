package com.dorohedoro.hole.worldgen.sorcerer;

import java.util.List;

/**
 * Footprints and placement rules for every Sorcerers' World building. Pure data so the planner (and its offline
 * fixture) do not need Minecraft. The painters that give each id its architecture and interior live in
 * {@link SorcererBuildings}.
 *
 * Weights are per placement ring: core (near the plaza), middle, outer. Ward towns sit in the city basin and lean
 * commercial and dense; hamlets sit on the basin edge and lean poor, small and industrial.
 */
public final class TownCatalog {
    private TownCatalog() {}

    public enum Category { RESIDENTIAL, APARTMENT, COMMERCIAL, FOOD, HOSPITALITY, INDUSTRIAL, CIVIC, WEALTHY, POOR, OPEN }

    /**
     * @param w        local width (along the facade)
     * @param d        local depth (front to back)
     * @param doorX    door column in local x
     * @param height   tallest the painter can build above the ground floor's feet level, roof and chimneys included
     * @param maxSlope largest terrain rise across the footprint the foundation is allowed to absorb
     * @param ward     weights core/middle/outer in a Ward
     * @param hamlet   weights core/middle/outer in a Hamlet
     * @param required a Ward should contain at least one, so every town has its basic services
     */
    public record Spec(String id, Category category, int w, int d, int doorX, int height, int maxSlope,
                       int[] ward, int[] hamlet, boolean required) {}

    public static final List<Spec> SPECS = List.of(
            new Spec("townhouse", Category.RESIDENTIAL, 7, 9, 3, 24, 6, new int[]{3, 6, 3}, new int[]{3, 3, 3}, false),
            new Spec("crooked_house", Category.RESIDENTIAL, 8, 8, 3, 20, 6, new int[]{1, 3, 2}, new int[]{1, 3, 3}, false),
            new Spec("tenement", Category.APARTMENT, 11, 12, 5, 24, 4, new int[]{5, 2, 0}, new int[]{0, 0, 0}, false),
            new Spec("mask_shop", Category.COMMERCIAL, 9, 10, 4, 17, 5, new int[]{4, 1, 0}, new int[]{1, 0, 0}, true),
            new Spec("smoke_parlor", Category.COMMERCIAL, 9, 9, 4, 21, 5, new int[]{4, 1, 0}, new int[]{2, 0, 0}, true),
            new Spec("eatery", Category.FOOD, 11, 9, 5, 14, 5, new int[]{4, 2, 0}, new int[]{3, 1, 0}, true),
            new Spec("boarding_house", Category.HOSPITALITY, 12, 10, 5, 21, 4, new int[]{3, 1, 0}, new int[]{1, 0, 0}, true),
            new Spec("repair_workshop", Category.INDUSTRIAL, 11, 11, 5, 16, 5, new int[]{0, 2, 4}, new int[]{3, 3, 3}, false),
            new Spec("sorcerer_manor", Category.WEALTHY, 15, 13, 7, 30, 4, new int[]{0, 2, 1}, new int[]{0, 0, 0}, false),
            new Spec("mushroom_villa", Category.WEALTHY, 11, 11, 5, 20, 5, new int[]{0, 1, 1}, new int[]{0, 0, 0}, false),
            new Spec("shanty", Category.POOR, 6, 7, 2, 8, 6, new int[]{0, 0, 4}, new int[]{0, 6, 6}, false),
            new Spec("clinic", Category.CIVIC, 10, 10, 4, 17, 5, new int[]{2, 1, 0}, new int[]{1, 0, 0}, true),
            // Community build (Frit), a landmark: one per city or capital, in the core.
            new Spec("victorian_exchange", Category.COMMERCIAL, 26, 20, 10, 30, 4, new int[]{6, 0, 0}, new int[]{0, 0, 0}, false),
            // Open lots. The "door" is the gap in the front railing.
            new Spec("market_stalls", Category.OPEN, 11, 7, 5, 6, 3, new int[]{3, 1, 0}, new int[]{1, 0, 0}, false),
            new Spec("courtyard", Category.OPEN, 9, 8, 4, 8, 3, new int[]{1, 2, 1}, new int[]{0, 1, 1}, false),
            new Spec("herb_garden", Category.OPEN, 8, 7, 3, 5, 3, new int[]{0, 1, 2}, new int[]{1, 2, 2}, false));

    /** Landmarks appear at most once per settlement, and only in cities and capitals. */
    public static final java.util.Set<String> LANDMARKS = java.util.Set.of("victorian_exchange");

    public static Spec spec(String id) {
        for (Spec spec : SPECS) if (spec.id.equals(id)) return spec;
        throw new IllegalArgumentException("Unknown Sorcerers' World building " + id);
    }
}
