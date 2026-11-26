package com.dorohedoro.hole.worldgen.city;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 25: fully procedural, district-aware urban fabric, generated on demand.
 *
 * All layout choices use the saved world seed, absolute world coordinates and
 * immutable lot geometry. No global RNG, static mutable placement state, saved
 * blueprint or cross-chunk reads. Canvas clips each write to the current chunk.
 *
 * Street activity stays on the outer avenue verges; the carriageway, tram
 * tracks, riverbank corridors, sewer access, all lots and crossings remain
 * navigable. Larger, correlated wear patches replace isolated noise pixels.
 */
final class UrbanFabric25 {
    private static final long WEAR = 0x25A11E001L;
    private static final long SERVICE = 0x25A11E002L;
    private static final long ROOF = 0x25A11E003L;
    private static final int PATCH_PITCH = 17;
    private static final int SERVICE_PITCH = 53;
    private UrbanFabric25() {}

    /** Hashes the absolute position of a local wear patch; no chunk origin dependence. */
    static boolean wearPatch(long seed, int x, int z) {
        int cx = Math.floorDiv(x, PATCH_PITCH), cz = Math.floorDiv(z, PATCH_PITCH);
        long h = CityHash.of(seed, WEAR, cx, cz);
        if (CityHash.range(h, 5) > 1) return false;
        int anchorX = cx * PATCH_PITCH + 3 + CityHash.range(h >>> 8, 11);
        int anchorZ = cz * PATCH_PITCH + 3 + CityHash.range(h >>> 22, 11);
        int rx = 2 + CityHash.range(h >>> 36, 3);
        int rz = 2 + CityHash.range(h >>> 49, 3);
        long dx = x - anchorX, dz = z - anchorZ;
        return dx * dx * rz * rz + dz * dz * rx * rx <= (long)rx * rx * rz * rz;
    }

    /** A deterministically offset street feature every 53 blocks on selected avenues. */
    static int streetFeatureAt(long seed, int avenueIndex, int along) {
        int section = Math.floorDiv(along, SERVICE_PITCH);
        long h = CityHash.of(seed, SERVICE + avenueIndex * 7919L, section);
        return section * SERVICE_PITCH + 8 + CityHash.range(h, 37);
    }

    static void streets(Canvas c, CityPlan plan, List<Line> xLines, List<Line> zLines, List<Lot> lots) {
        for (int x = c.minX; x <= c.maxX; x++) {
            for (int z = c.minZ; z <= c.maxZ; z++) {
                if (inLot(lots, x, z)) continue;
                Line xb = band(xLines, x, z, plan, true);
                Line zb = band(zLines, z, x, plan, false);
                if (xb != null && (xb.kind() == Line.Kind.CANAL || xb.kind() == Line.Kind.TRAM)) continue;
                if (zb != null && (zb.kind() == Line.Kind.CANAL || zb.kind() == Line.Kind.TRAM)) continue;
                if (xb != null && zb != null) continue; // crossing sightlines and access shafts
                // Preserve the main avenue carriageway: only lightly repaired alley paving
                // and existing outer sidewalks may vary.
                if ((xb == null && zb == null) || onOuterSidewalk(plan, xb, zb, x, z)) {
                    if (wearPatch(plan.seed, x, z)) {
                        long h = CityHash.of(plan.seed, WEAR + 1, Math.floorDiv(x, 17), Math.floorDiv(z, 17));
                        c.set(x, CityPlan.STREET_Y, z, wearMaterial(plan.districtAt(x, z), h));
                    }
                }
                Line avenue = xb != null ? xb : zb;
                if (avenue == null || avenue.kind() != Line.Kind.AVENUE) continue;
                boolean northSouth = xb != null;
                int cross = northSouth ? x : z;
                int along = northSouth ? z : x;
                int actualCenter = avenue.center() + (northSouth
                        ? plan.xAvenueOffset(avenue, along) : plan.zAvenueOffset(avenue, along));
                int signed = cross - actualCenter;
                if (Math.abs(signed) != avenue.halfWidth()) continue;
                if (streetFeatureAt(plan.seed, avenue.index(), along) != along) continue;
                // Only one sidewalk edge receives the feature in each section.
                long h = CityHash.of(plan.seed, SERVICE, avenue.index(), Math.floorDiv(along, SERVICE_PITCH));
                if ((signed > 0) != (CityHash.range(h >>> 12, 2) == 0)) continue;
                if (nearJunction(northSouth ? zLines : xLines, along, 13)) continue;
                // Keep the earlier lamp/fixture positions and potential shaft paths clear.
                if (Math.floorMod(along + avenue.center(), 15) == 0
                        || Math.floorMod(along + avenue.center(), 37) == 19) continue;
                Block fixture = serviceFixture(plan.districtAt(x, z), h);
                if (c.hasOccupiedInteriors35() && fixture == Blocks.BARREL) fixture = Blocks.COMPOSTER;
                c.set(x, CityPlan.GROUND_Y, z, fixture);
            }
        }
    }

    /** Service lanes and yard edges change with neighborhood and lot identity. */
    static void yard(Canvas c, Lot lot) {
        if (!lot.type().enclosed || lot.rect().width() < 15 || lot.rect().depth() < 13) return;
        if (lot.type() == LotType.STARTER_SHELTER || lot.type() == LotType.HUNGRY_BUG
                || lot.type() == LotType.HOSPITAL || lot.type() == LotType.RESEARCH_MANOR
                || lot.type() == LotType.DEPARTMENT_STORE) return;
        Rect r = lot.rect();
        long h = CityHash.of(c.seed(), SERVICE + 1, r.centerX(), r.centerZ());
        // Outside the structural footprint (which has at least one block setback).
        // Never create clutter on the south face, where the building door is placed.
        int x = CityHash.range(h, 2) == 0 ? r.x0() : r.x1();
        int z = r.z0() + 3 + CityHash.range(h >>> 17, Math.max(1, r.depth() - 8));
        int count = 1 + CityHash.range(h >>> 29, 3);
        for (int i = 0; i < count; i++) {
            int pz = z + i;
            if (pz >= r.z1() - 3) break;
            // A chunk-local read is safe here; don't inspect adjacent chunks.
            if (!c.contains(x, pz)) continue;
            if (!c.get(x, CityPlan.GROUND_Y, pz).isAir()
                    || c.get(x, CityPlan.STREET_Y, pz).isAir()) continue;
            Block fixture = yardFixture(lot.district(), CityHash.mix(h + i * 97L));
            if (c.hasOccupiedInteriors35() && fixture == Blocks.BARREL) fixture = Blocks.CAULDRON;
            c.set(x, CityPlan.GROUND_Y, pz, fixture);
        }
    }

    /** Extra silhouette only on plain roofs, never on rooftop farms or stair access. */
    static void roof(Canvas c, Rect box, int roofY, long hash, Tower.Roof roof) {
        if (roof != Tower.Roof.PLAIN || box.width() < 15 || box.depth() < 13) return;
        long h = CityHash.of(hash, ROOF, box.centerX(), box.centerZ());
        // Roof flight occupies the north wall, while earlier roof machinery uses
        // the south-east. Choose the south-west instead to avoid overwriting it.
        int x = box.x0() + 4, z = box.z1() - 4;
        switch (CityHash.range(h, 4)) {
            case 0 -> { // air-handling unit and vent stack
                c.fill(x, roofY + 1, z, x + 1, roofY + 2, z + 1, Blocks.WAXED_WEATHERED_CUT_COPPER);
                c.set(x, roofY + 3, z, Blocks.IRON_TRAPDOOR);
            }
            case 1 -> { // low shared rooftop utility shed
                c.fill(x, roofY + 1, z, x + 1, roofY + 2, z + 1, Blocks.DEEPSLATE_BRICKS);
                c.fill(x, roofY + 3, z, x + 1, roofY + 3, z + 1, Blocks.DEEPSLATE_TILE_SLAB);
            }
            case 2 -> { // abandoned pipe riser
                c.fill(x, roofY + 1, z, x, roofY + 3, z, Blocks.WAXED_OXIDIZED_COPPER);
                c.set(x + 1, roofY + 3, z, Blocks.IRON_BARS);
            }
            default -> { // short telecom/service mast
                c.fill(x, roofY + 1, z, x, roofY + 4, z, Blocks.IRON_BARS);
                c.set(x, roofY + 5, z, Blocks.LIGHTNING_ROD);
            }
        }
    }

    private static boolean inLot(List<Lot> lots, int x, int z) {
        for (Lot lot : lots) if (lot.rect().contains(x, z)) return true;
        return false;
    }

    private static Line band(List<Line> lines, int coordinate, int along, CityPlan plan, boolean xAxis) {
        for (Line line : lines) {
            int offset = xAxis ? plan.xAvenueOffset(line, along) : plan.zAvenueOffset(line, along);
            if (Math.abs(coordinate - line.center() - offset) <= line.halfWidth()) return line;
        }
        return null;
    }

    private static boolean onOuterSidewalk(CityPlan plan, Line xb, Line zb, int x, int z) {
        Line line = xb != null ? xb : zb;
        if (line == null) return false;
        int cross = xb != null ? x : z, along = xb != null ? z : x;
        int bend = xb != null ? plan.xAvenueOffset(line, along) : plan.zAvenueOffset(line, along);
        return Math.abs(cross - line.center() - bend) == line.halfWidth();
    }

    private static boolean nearJunction(List<Line> lines, int coordinate, int radius) {
        for (Line line : lines) if (Math.abs(coordinate - line.center()) <= line.halfWidth() + radius)
            return true;
        return false;
    }

    private static Block wearMaterial(District district, long h) {
        int v = CityHash.range(h, 4);
        return switch (district) {
            case RESIDENTIAL -> v == 0 ? Blocks.PACKED_MUD : Blocks.COBBLED_DEEPSLATE;
            case MARKET -> v == 0 ? Blocks.MOSSY_STONE_BRICKS : Blocks.CRACKED_STONE_BRICKS;
            case INDUSTRIAL -> v == 0 ? Blocks.GRAVEL : Blocks.COBBLED_DEEPSLATE;
            case WATERFRONT -> v == 0 ? Blocks.MOSSY_STONE_BRICKS : Blocks.PACKED_MUD;
            case DERELICT -> v == 0 ? Blocks.COARSE_DIRT : Blocks.GRAVEL;
            case GRAVEYARD -> v == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.CRACKED_STONE_BRICKS;
            case OUTSKIRTS -> v == 0 ? Blocks.COARSE_DIRT : Blocks.PACKED_MUD;
        };
    }

    private static Block serviceFixture(District district, long h) {
        int v = CityHash.range(h >>> 28, 3);
        return switch (district) {
            case RESIDENTIAL -> v == 0 ? Blocks.BARREL : v == 1 ? Blocks.CAULDRON : Blocks.SPRUCE_FENCE;
            case MARKET -> v == 0 ? Blocks.SMOKER : v == 1 ? Blocks.BARREL : Blocks.COMPOSTER;
            case INDUSTRIAL -> v == 0 ? Blocks.WAXED_WEATHERED_CUT_COPPER : v == 1 ? Blocks.CAULDRON : Blocks.IRON_BARS;
            case WATERFRONT -> v == 0 ? Blocks.STRIPPED_SPRUCE_LOG : v == 1 ? Blocks.BARREL : Blocks.IRON_BARS;
            case DERELICT -> v == 0 ? Blocks.COBWEB : v == 1 ? Blocks.IRON_BARS : Blocks.CAULDRON;
            case GRAVEYARD -> v == 0 ? Blocks.COBBLESTONE_WALL : v == 1 ? Blocks.SOUL_LANTERN : Blocks.CRACKED_STONE_BRICKS;
            case OUTSKIRTS -> v == 0 ? Blocks.COMPOSTER : v == 1 ? Blocks.HAY_BLOCK : Blocks.BARREL;
        };
    }

    private static Block yardFixture(District district, long h) {
        return switch (district) {
            case RESIDENTIAL -> CityHash.range(h, 2) == 0 ? Blocks.BARREL : Blocks.CAULDRON;
            case MARKET -> CityHash.range(h, 2) == 0 ? Blocks.COMPOSTER : Blocks.BARREL;
            case INDUSTRIAL -> CityHash.range(h, 2) == 0 ? Blocks.WAXED_WEATHERED_CUT_COPPER : Blocks.IRON_BARS;
            case WATERFRONT -> CityHash.range(h, 2) == 0 ? Blocks.SPRUCE_PLANKS : Blocks.BARREL;
            case DERELICT -> CityHash.range(h, 2) == 0 ? Blocks.COBWEB : Blocks.CAULDRON;
            case GRAVEYARD -> CityHash.range(h, 2) == 0 ? Blocks.COBBLESTONE_WALL : Blocks.MOSSY_COBBLESTONE;
            case OUTSKIRTS -> CityHash.range(h, 2) == 0 ? Blocks.COMPOSTER : Blocks.HAY_BLOCK;
        };
    }
}
