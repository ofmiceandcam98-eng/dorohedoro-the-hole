package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

/** The lots between four arterials. Everything inside {@link #area} that is not a lot is street. */
public record Superblock(int i, int j, Rect area, List<Lot> lots) {
    private static final int STREET_GAP = 5;
    private static final int ALLEY_GAP = 3;
    private static final int PLAZA_RADIUS = 13;

    static Superblock build(CityPlan plan, int i, int j) {
        Line west = plan.xLine(i);
        Line east = plan.xLine(i + 1);
        Line north = plan.zLine(j);
        Line south = plan.zLine(j + 1);
        // Extra verge only in v3. Slightly bent avenues can never clip a building footprint.
        int verge = plan.settings.worldDetailVersion() >= 8 ? 4 : plan.settings.worldDetailVersion() >= 3 ? 6 : 0;
        Rect area = new Rect(
                west.center() + west.halfWidth() + 1 + verge,
                north.center() + north.halfWidth() + 1 + verge,
                east.center() - east.halfWidth() - 1 - verge,
                south.center() - south.halfWidth() - 1 - verge);

        long hash = plan.blockHash(i, j);
        RandomSource random = new XoroshiroRandomSource(hash);
        District district = plan.districtAt(area.centerX(), area.centerZ());
        List<Lot> lots = new ArrayList<>();

        if (i == -1 && j == -1) {
            buildStartingQuarter(plan, area, district, random, lots);
        } else {
            CityPlan.LandmarkSite site = plan.landmarkSite(
                    Math.floorDiv(i, CityPlan.LANDMARK_REGION), Math.floorDiv(j, CityPlan.LANDMARK_REGION));
            if (site.blockI() == i && site.blockJ() == j) {
                lots.add(new Lot(area, site.type(), district, random.nextLong()));
            } else if (plan.settings.worldDetailVersion() >= 3
                    && IrregularBlocks14.usesOffsetBlocks(hash) && area.width() >= 55 && area.depth() >= 55) {
                // Offset local streets enter the main arterial network at both sides. Their
                // perpendicular 5-block alleys connect the stepped diagonal segments.
                for (Rect parcel : IrregularBlocks14.parcels(area, hash,
                        plan.settings.worldDetailVersion() >= 13 && DenseCity25.isDenseDistrict(district))) {
                    split(plan, parcel, district, 1, random, lots);
                }
            } else {
                split(plan, area, district, 0, random, lots);
            }
        }
        // New-world diagonals require a building-free right-of-way. Preserve
        // landmarks and spawn supplies rather than chopping unique structures.
        if (plan.settings.worldDetailVersion() >= 3 && !(i == -1 && j == -1)) {
            CityPlan.LandmarkSite protectedSite = plan.landmarkSite(
                    Math.floorDiv(i, CityPlan.LANDMARK_REGION), Math.floorDiv(j, CityPlan.LANDMARK_REGION));
            if (protectedSite.blockI() != i || protectedSite.blockJ() != j) {
                // Preserve neighborhood activity along the new boulevard instead of
                // creating long empty lots: reuse actual survival-accessible open lots.
                // The diagonal carriageway itself is cleared and paved afterward.
                lots.replaceAll(lot -> {
                    if (!DiagonalBoulevards14.intersects(plan.seed, lot.rect())) return lot;
                    LotType edgeUse = switch (district) {
                        case INDUSTRIAL, OUTSKIRTS -> LotType.JUNKYARD;
                        case MARKET -> LotType.MARKET_STALLS;
                        case DERELICT -> LotType.RUBBLE;
                        default -> LotType.VACANT;
                    };
                    return new Lot(lot.rect(), edgeUse, district, lot.hash());
                });
            }
        }
        return new Superblock(i, j, area, List.copyOf(lots));
    }

    /**
     * The fixed quarter around the origin: an open plaza ringed by the things a new player needs, on every seed.
     * Road gaps of {@value #ALLEY_GAP} around the plaza keep it connected to the rest of the street grid.
     */
    private static void buildStartingQuarter(CityPlan plan, Rect area, District district, RandomSource random, List<Lot> lots) {
        int inner = PLAZA_RADIUS + ALLEY_GAP + 1;
        lots.add(new Lot(new Rect(-PLAZA_RADIUS, -PLAZA_RADIUS, PLAZA_RADIUS, PLAZA_RADIUS), LotType.PLAZA, district, random.nextLong()));

        Rect northStrip = new Rect(area.x0(), area.z0(), area.x1(), -inner);
        Rect southStrip = new Rect(area.x0(), inner, area.x1(), area.z1());
        Rect westStrip = new Rect(area.x0(), -PLAZA_RADIUS, -inner, PLAZA_RADIUS);
        Rect eastStrip = new Rect(inner, -PLAZA_RADIUS, area.x1(), PLAZA_RADIUS);

        place(plan, northStrip, new Rect(-15, -inner - 15, 15, -inner), LotType.HUNGRY_BUG, district, random, lots);
        place(plan, southStrip, new Rect(-15, inner, 15, inner + 15), LotType.NURSERY, district, random, lots);
        place(plan, eastStrip, new Rect(inner, -PLAZA_RADIUS, inner + 14, PLAZA_RADIUS), LotType.STARTER_SHELTER, district, random, lots);
        place(plan, westStrip, new Rect(-inner - 14, -PLAZA_RADIUS, -inner, PLAZA_RADIUS), LotType.MARKET_STALLS, district, random, lots);
    }

    private static void place(CityPlan plan, Rect strip, Rect special, LotType type, District district, RandomSource random, List<Lot> lots) {
        lots.add(new Lot(special, type, district, random.nextLong()));
        Rect[] rest = {
                new Rect(strip.x0(), strip.z0(), special.x0() - ALLEY_GAP - 1, strip.z1()),
                new Rect(special.x1() + ALLEY_GAP + 1, strip.z0(), strip.x1(), strip.z1()),
                new Rect(special.x0(), strip.z0(), special.x1(), special.z0() - ALLEY_GAP - 1),
                new Rect(special.x0(), special.z1() + ALLEY_GAP + 1, special.x1(), strip.z1())
        };
        for (Rect piece : rest) {
            if (piece.isValid() && piece.width() >= 8 && piece.depth() >= 8) {
                split(plan, piece, district, 1, random, lots);
            }
        }
    }

    private static void split(CityPlan plan, Rect rect, District district, int depth, RandomSource random, List<Lot> lots) {
        boolean detailed = plan.settings.worldDetailVersion() > 0;
        // Opt-in v13 only: high-density housing/shopfronts and narrower inner streets.
        // Retain larger working yards and keep graveyard/outskirts less built-up.
        boolean dense = plan.settings.worldDetailVersion() >= 13 && DenseCity25.isDenseDistrict(district);
        int minLot = district == District.INDUSTRIAL ? 16 : 11;
        int maxLot = dense ? DenseCity25.maxLot(district) : switch (district) {
            case INDUSTRIAL, OUTSKIRTS -> 44;
            case DERELICT, GRAVEYARD -> 34;
            default -> 26;
        };
        if (detailed && depth > 0) {
            // Historical RNG draw count remains identical for all pre-v13 worlds.
            int span = dense ? 7 : 13;
            maxLot += random.nextInt(span) - (span - 1) / 2;
            maxLot = Math.max(minLot + 1, maxLot);
        }
        // 3-wide lanes are passable on foot, unlike 1-wide slits. Main tram,
        // canal-rivers and arterial approaches retain their existing right of way.
        int gap = dense ? DenseCity25.localStreetWidth(depth) : depth == 0 ? STREET_GAP : ALLEY_GAP;
        boolean canCutX = rect.width() >= minLot * 2 + gap;
        boolean canCutZ = rect.depth() >= minLot * 2 + gap;
        boolean smallEnough = rect.width() <= maxLot && rect.depth() <= maxLot;

        float stopChance = dense ? 0.22F : detailed ? 0.57F : 0.35F;
        if ((!canCutX && !canCutZ) || (smallEnough && random.nextFloat() < stopChance)) {
            lots.add(new Lot(rect, LotChooser.choose(plan, district, rect, random), district, random.nextLong()));
            return;
        }

        boolean cutX = canCutX && (!canCutZ || rect.width() > rect.depth() || (rect.width() == rect.depth() && random.nextBoolean()));
        if (cutX) {
            int cut = rect.x0() + minLot + random.nextInt(rect.width() - minLot * 2 - gap + 1);
            split(plan, new Rect(rect.x0(), rect.z0(), cut - 1, rect.z1()), district, depth + 1, random, lots);
            split(plan, new Rect(cut + gap, rect.z0(), rect.x1(), rect.z1()), district, depth + 1, random, lots);
        } else {
            int cut = rect.z0() + minLot + random.nextInt(rect.depth() - minLot * 2 - gap + 1);
            split(plan, new Rect(rect.x0(), rect.z0(), rect.x1(), cut - 1), district, depth + 1, random, lots);
            split(plan, new Rect(rect.x0(), cut + gap, rect.x1(), rect.z1()), district, depth + 1, random, lots);
        }
    }
}
