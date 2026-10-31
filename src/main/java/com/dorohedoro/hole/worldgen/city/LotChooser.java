package com.dorohedoro.hole.worldgen.city;

import net.minecraft.util.RandomSource;

/** Picks what occupies a lot, by district. */
final class LotChooser {
    private LotChooser() {}

    static LotType choose(CityPlan plan, District district, Rect rect, RandomSource random) {
        float roll = random.nextFloat();
        if (random.nextFloat() > plan.settings.density()) {
            return roll < 0.5F ? LotType.RUBBLE : LotType.JUNKYARD;
        }
        boolean large = rect.width() >= 20 && rect.depth() >= 20;
        return switch (district) {
            case RESIDENTIAL -> {
                if (roll < 0.04F) yield LotType.NURSERY;
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
                if (roll < 0.28F) yield LotType.RUBBLE;
                if (roll < 0.46F) yield LotType.JUNKYARD;
                if (roll < 0.62F) yield LotType.PARK;
                if (roll < 0.76F) yield LotType.NURSERY;
                if (roll < 0.86F) yield LotType.LIVESTOCK_PEN;
                yield LotType.TENEMENT;
            }
            case GRAVEYARD -> {
                if (roll < 0.70F) yield LotType.CEMETERY;
                if (roll < 0.85F) yield LotType.PARK;
                yield LotType.TENEMENT;
            }
        };
    }
}
