package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 53: land use controls the SIZE and connectivity of streets and parcels,
 * independently of the region's snow/desert/taiga/mountain climate.
 *
 * <p>The city used to use district labels for lot sizes even when Pass 52's
 * independent urban map assigned a dense neighborhood to an OUTSKIRTS district.
 * That produced a "dense" snow or desert city full of huge empty blocks.
 * The policy below is pure and is only used by new version-36 worlds.
 * Historical superblocks continue through the original splitter unchanged.
 */
final class UrbanParcels53 {
    record Profile(int minLot, int maxLot, int streetGap, float stopChance, boolean dense) {
        Profile {
            if (minLot < 10 || maxLot < minLot || streetGap < 3 || streetGap > 8
                    || stopChance < 0 || stopChance > 1)
                throw new IllegalArgumentException("invalid walkable parcel policy");
        }
    }

    private UrbanParcels53() {}

    static Profile profile(UrbanPattern52.Fabric fabric, District district, int depth) {
        int min = district == District.INDUSTRIAL ? 16 : 11;
        return switch (fabric) {
            case DENSE -> new Profile(min,
                    DenseCity25.isDenseDistrict(district) ? DenseCity25.maxLot(district)
                            : district == District.GRAVEYARD ? 24 : 22,
                    3, 0.22F, true);
            case INHABITED -> new Profile(min, district == District.INDUSTRIAL ? 42 : 34,
                    depth == 0 ? 5 : 3, 0.48F, false);
            case DERELICT -> new Profile(min, district == District.INDUSTRIAL ? 50 : 45,
                    depth == 0 ? 5 : 4, 0.65F, false);
            case OPEN_COUNTRY -> new Profile(min, 78,
                    depth == 0 ? 6 : 4, 0.87F, false);
        };
    }

    /** Dense urban fabric can occur in a district historically named OUTSKIRTS. */
    static LotType denseFringeLot(float roll, Rect rect) {
        if (roll < .24F) return LotType.TENEMENT;
        if (roll < .45F) return LotType.SHOP;
        if (roll < .59F) return LotType.WAREHOUSE;
        // A genuine factory needs enough floor for machines and a clear aisle.
        // Smaller lots receive a repair shop with a furnished upper dwelling.
        if (roll < .71F) return rect.width() >= 18 && rect.depth() >= 16
                ? LotType.FACTORY : LotType.SHOP;
        if (roll < .80F) return LotType.CLINIC;
        if (roll < .88F) return LotType.GREENHOUSE;
        if (roll < .94F) return LotType.JUNKYARD;
        return LotType.VACANT;
    }
}
