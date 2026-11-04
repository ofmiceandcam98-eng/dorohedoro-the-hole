package com.dorohedoro.hole.worldgen.city;

import net.minecraft.util.RandomSource;

/** Picks what occupies a lot, by district. */
final class LotChooser {
    private LotChooser() {}

    static LotType choose(CityPlan plan, District district, Rect rect, RandomSource random) {
        float roll = random.nextFloat();
        // Pass 52: climate identifies scenery, NOT development density.
        // The same province can hold proper dense city blocks, small lived-in
        // neighborhoods, derelict lots and real open survival terrain.
        if (plan.settings.worldDetailVersion() >= 35) {
            UrbanPattern52.Fabric fabric = UrbanPattern52.at(plan.seed, rect.centerX(), rect.centerZ());
            // Pass 57: a climate cannot turn an occupied INDUSTRIAL district
            // into a generic village. Preserve open countryside unchanged.
            if (plan.settings.worldDetailVersion() >= 40 && district == District.INDUSTRIAL
                    && fabric != UrbanPattern52.Fabric.OPEN_COUNTRY)
                return IndustrialBelts57.choose(plan.seed, rect, fabric, roll);
            if (fabric != UrbanPattern52.Fabric.DENSE)
                return UrbanPattern52.choose(plan.regionAt(rect.centerX(), rect.centerZ()), fabric, roll);
            // In Pass 52 the OUTSKIRTS district still used mostly VACANT/RUBBLE
            // even where the independent fabric said DENSE. Build actual
            // lived-in rooms and workplaces, without eliminating open plots.
            if (plan.settings.worldDetailVersion() >= 36 && district == District.OUTSKIRTS)
                return UrbanParcels53.denseFringeLot(roll, rect);
        }
        // Pass 47: the same biome can contain occupied blocks, ruins or open
        // country; this is not just the same city density with a new texture.
        if (plan.settings.worldDetailVersion() >= 31 && plan.settings.worldDetailVersion() < 35) {
            HoleRegions42.Region province = plan.regionAt(rect.centerX(), rect.centerZ());
            if (province != HoleRegions42.Region.CITY) {
                return SettlementBands47.choose(province,
                        SettlementBands47.at(plan.seed, rect.centerX(), rect.centerZ()), roll);
            }
        }
        if (plan.settings.worldDetailVersion() >= 26 && plan.settings.worldDetailVersion() < 35) {
            // The city thins into farms, mining outcrops and condemned lots.
            // Building forms remain from the Hole's existing real lot generator.
            HoleRegions42.Region region = plan.regionAt(rect.centerX(), rect.centerZ());
            switch (region) {
                case SNOWFIELDS -> {
                    if (roll < .35F) return LotType.VACANT;
                    if (roll < .54F) return LotType.RUBBLE;
                    if (roll < .70F) return LotType.WAREHOUSE;
                    if (roll < .83F) return LotType.TENEMENT;
                }
                case DEAD_TAIGA -> {
                    if (roll < .39F) return LotType.VACANT;
                    if (roll < .55F) return LotType.NURSERY;
                    if (roll < .66F) return LotType.GREENHOUSE;
                    if (roll < .80F) return LotType.TENEMENT;
                }
                case DUST_BASIN -> {
                    if (roll < .36F) return LotType.VACANT;
                    if (roll < .59F) return LotType.RUBBLE;
                    if (roll < .73F) return LotType.JUNKYARD;
                    if (roll < .83F) return LotType.WAREHOUSE;
                }
                case IRON_BADLANDS -> {
                    if (roll < .30F) return LotType.RUBBLE;
                    if (roll < .55F) return LotType.JUNKYARD;
                    if (roll < .70F) return LotType.VACANT;
                    if (roll < .85F) return LotType.FACTORY;
                }
                case MOUNTAIN_RIM -> {
                    if (roll < .40F) return LotType.RUBBLE;
                    if (roll < .70F) return LotType.VACANT;
                    if (roll < .82F) return LotType.WAREHOUSE;
                    if (roll < .91F) return LotType.GUARD_POST;
                }
                case CITY -> { }
            }
        }
        if (random.nextFloat() > plan.settings.density()) {
            return roll < 0.4F ? LotType.RUBBLE : roll < 0.75F ? LotType.VACANT : LotType.JUNKYARD;
        }
        boolean large = rect.width() >= 20 && rect.depth() >= 20;
        // Pass 25: small lots must support real small HOMES, not pretend tower blocks.
        // Deterministic per-lot roll already comes from the plan's seeded stream.
        if (plan.settings.worldDetailVersion() >= 14 && rect.width() <= 15 && rect.depth() <= 16) {
            if (district == District.RESIDENTIAL) {
                if (roll < .68F) return LotType.TENEMENT;
                if (roll < .87F) return LotType.SHOP; // home above a tiny business
                if (roll < .93F) return LotType.CLINIC;
                return LotType.COMMUNAL_COURT;
            }
            if (district == District.MARKET) {
                if (roll < .38F) return LotType.TENEMENT;
                if (roll < .83F) return LotType.SHOP;
                return LotType.MARKET_STALLS;
            }
            if (district == District.WATERFRONT && roll < .66F) return LotType.TENEMENT;
        }
        // Version 2 establishes recognizable multi-superblock neighborhoods. A repair-strip
        // should not have the same lot mix as a warehouse belt or surviving residential pocket.
        // All selections are seed/coordinate derived; the old layout is retained for saves v0/v1.
        if (plan.settings.worldDetailVersion() >= 2) {
            int focus = CityArchitecture.neighborhood(plan.seed, rect.centerX(), rect.centerZ());
            if (district == District.RESIDENTIAL) {
                if (focus == 0 && roll < .17F) return LotType.SHOP; // mixed storefront street
                if (focus == 1 && roll > .53F) return LotType.APARTMENT; // taller residential pocket
                if (focus == 2 && roll < .14F) return roll < .05F ? LotType.PARK : LotType.GREENHOUSE;
                if (focus == 4 && roll < .12F) return LotType.TENEMENT; // older low-rise blocks
            } else if (district == District.INDUSTRIAL) {
                if (focus == 0 && roll < .21F) return LotType.SHOP; // repair-shop neighborhood
                if (focus == 1 && roll < .40F) return LotType.WAREHOUSE; // freight yards
                if (focus == 2 && roll < .42F) return LotType.FACTORY; // manufacturing belt
                if (focus == 4 && roll < .25F) return LotType.JUNKYARD; // dismantling yards
            } else if (district == District.MARKET) {
                if (focus == 3 && roll < .42F) return LotType.MARKET_STALLS;
                if (focus == 4 && roll < .30F) return LotType.SHOP;
            } else if (district == District.DERELICT) {
                if (focus == 0 && roll < .25F) return LotType.TENEMENT; // abandoned housing rows
                if (focus == 2 && roll < .25F) return LotType.WAREHOUSE; // gutted depots
            } else if (district == District.OUTSKIRTS) {
                if (focus == 5 && roll < .20F) return LotType.TENEMENT; // surviving fringe hamlets
            }
        }
        // Old saves keep their previous lot identity, while new layouts mix frontage types
        // within districts rather than repeating wall-to-wall identical factories.
        if (plan.settings.worldDetailVersion() > 0) {
            if (district == District.INDUSTRIAL) {
                if (roll < .12F) return LotType.SHOP; // repair businesses near factories
                if (roll < .39F) return LotType.FACTORY;
                if (roll < .62F) return LotType.WAREHOUSE;
                if (roll < .81F) return LotType.JUNKYARD;
                if (roll < .90F) return LotType.TENEMENT;
                return LotType.VACANT;
            }
            if (district == District.MARKET && rect.width() >= 19 && roll > .75F) {
                return roll > .88F ? LotType.APARTMENT : LotType.WAREHOUSE;
            }
            if (district == District.DERELICT) {
                // A derelict district contains recognizable *buildings*, not a repeating sea
                // of flat rubble: abandoned tenements, boarded shops and gutted warehouses
                // alongside broken empty lots and scrap yards.
                if (roll < .15F) return LotType.RUBBLE;
                if (roll < .29F) return LotType.JUNKYARD;
                if (roll < .40F) return LotType.VACANT;
                if (roll < .47F) return LotType.PARK;
                if (roll < .54F) return LotType.NURSERY;
                if (roll < .75F) return LotType.TENEMENT;
                if (roll < .87F) return LotType.WAREHOUSE;
                if (roll < .95F) return LotType.SHOP;
                return LotType.FACTORY;
            }
        }
        return switch (district) {
            case RESIDENTIAL -> {
                if (roll < 0.02F) yield LotType.NURSERY;
                if (roll < 0.05F) yield LotType.VACANT;
                if (roll < 0.08F) yield LotType.GREENHOUSE;
                if (roll < 0.11F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.15F) yield LotType.CLINIC;
                if (roll < 0.19F) yield LotType.GUARD_POST;
                if (roll < 0.24F) yield LotType.SHOP;
                yield large || roll > 0.72F ? LotType.APARTMENT : LotType.TENEMENT;
            }
            case MARKET -> {
                if (roll < 0.34F) yield LotType.SHOP;
                if (roll < 0.56F) yield LotType.MARKET_STALLS;
                if (roll < 0.64F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.72F) yield LotType.GREENHOUSE;
                if (roll < 0.78F) yield LotType.WAREHOUSE;
                yield LotType.TENEMENT;
            }
            case INDUSTRIAL -> {
                if (roll < 0.44F) yield LotType.FACTORY;
                if (roll < 0.76F) yield LotType.WAREHOUSE;
                if (roll < 0.90F) yield LotType.JUNKYARD;
                yield LotType.TENEMENT;
            }
            case WATERFRONT -> {
                if (roll < 0.30F) yield LotType.WAREHOUSE;
                if (roll < 0.55F) yield LotType.TENEMENT;
                if (roll < 0.70F) yield LotType.SHOP;
                if (roll < 0.84F) yield LotType.GREENHOUSE;
                yield LotType.JUNKYARD;
            }
            case DERELICT -> {
                if (roll < 0.26F) yield LotType.RUBBLE;
                if (roll < 0.44F) yield LotType.JUNKYARD;
                if (roll < 0.62F) yield LotType.VACANT;
                if (roll < 0.72F) yield LotType.PARK;
                if (roll < 0.80F) yield LotType.NURSERY;
                if (roll < 0.88F) yield LotType.LIVESTOCK_PEN;
                yield LotType.TENEMENT;
            }
            case GRAVEYARD -> {
                if (roll < 0.70F) yield LotType.CEMETERY;
                if (roll < 0.85F) yield LotType.PARK;
                yield LotType.TENEMENT;
            }
            case OUTSKIRTS -> {
                if (roll < 0.34F) yield LotType.VACANT;
                if (roll < 0.52F) yield LotType.RUBBLE;
                if (roll < 0.64F) yield LotType.JUNKYARD;
                if (roll < 0.74F) yield LotType.LIVESTOCK_PEN;
                if (roll < 0.82F) yield LotType.GREENHOUSE;
                if (roll < 0.88F) yield LotType.PARK;
                if (roll < 0.93F) yield LotType.WAREHOUSE;
                yield LotType.TENEMENT;
            }
        };
    }
}
