package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 18: stronger district identity through landmark lots, ground materials, and street cues.
 *
 * <p>Everything remains seed/coordinate driven and chunk-order independent. The goal is not to add
 * new systems yet, but to make districts read more clearly at a glance while preserving survival.
 */
final class DistrictIdentity18 {
    private static final long DISTRICT_LANDMARK_SALT = 0x18D15EA1L;

    private DistrictIdentity18() {}

    static boolean enabled(int worldDetailVersion) {
        return worldDetailVersion >= 7;
    }

    static Mix lotGround(District district) {
        return switch (district) {
            case RESIDENTIAL -> Mix.of(Blocks.PACKED_MUD, 4, Blocks.COARSE_DIRT, 3, Blocks.COBBLED_DEEPSLATE, 1, Blocks.GRAVEL, 2);
            case MARKET -> Mix.of(Blocks.PACKED_MUD, 4, Blocks.BRICKS, 2, Blocks.GRAVEL, 2, Blocks.COARSE_DIRT, 2);
            case INDUSTRIAL -> Mix.of(Blocks.POLISHED_BLACKSTONE, 2, Blocks.COBBLED_DEEPSLATE, 3, Blocks.GRAVEL, 3,
                    Blocks.WAXED_WEATHERED_CUT_COPPER, 1);
            case WATERFRONT -> Mix.of(Blocks.GRAVEL, 3, Blocks.SPRUCE_PLANKS, 2, Blocks.PACKED_MUD, 2, Blocks.COBBLESTONE, 2);
            case DERELICT -> Mix.of(Blocks.COARSE_DIRT, 4, Blocks.GRAVEL, 3, Blocks.COBBLESTONE, 2, Blocks.CRACKED_STONE_BRICKS, 1);
            case GRAVEYARD -> Mix.of(Blocks.GRAVEL, 3, Blocks.COBBLESTONE, 2, Blocks.CRACKED_STONE_BRICKS, 3, Blocks.MOSSY_COBBLESTONE, 1);
            case OUTSKIRTS -> Mix.of(Blocks.COARSE_DIRT, 4, Blocks.PACKED_MUD, 3, Blocks.GRAVEL, 2, Blocks.PODZOL, 1);
        };
    }

    /**
     * Not every landmark region becomes a district signature block; some remain citywide landmarks
     * like hospitals and department stores. This keeps variety while still producing obvious district anchors.
     */
    static boolean useDistrictLandmark(long seed, int regionX, int regionZ) {
        return CityHash.range(CityHash.of(seed, DISTRICT_LANDMARK_SALT, regionX, regionZ), 3) == 0;
    }

    static LotType landmarkType(District district, long hash) {
        return switch (district) {
            case RESIDENTIAL -> CityHash.range(hash >>> 8, 2) == 0 ? LotType.COMMUNAL_COURT : LotType.CLINIC;
            case MARKET -> LotType.BAZAAR;
            case INDUSTRIAL -> LotType.MACHINE_YARD;
            case WATERFRONT -> LotType.CANAL_DEPOT;
            case DERELICT -> LotType.RUIN_COURT;
            case GRAVEYARD -> LotType.BURIAL_GROUND;
            case OUTSKIRTS -> LotType.FRINGE_YARD;
        };
    }

    static Block edgeFixture(District district, long hash) {
        int roll = CityHash.range(hash, 5);
        return switch (district) {
            case RESIDENTIAL -> roll == 0 ? Blocks.BARREL : Blocks.SPRUCE_FENCE;
            case MARKET -> roll <= 1 ? Blocks.COMPOSTER : Blocks.BARREL;
            case INDUSTRIAL -> roll <= 1 ? Blocks.CAULDRON : Blocks.IRON_BARS;
            case WATERFRONT -> roll == 0 ? Blocks.BARREL : Blocks.STRIPPED_SPRUCE_LOG;
            case DERELICT -> roll == 0 ? Blocks.COBWEB : Blocks.HAY_BLOCK;
            case GRAVEYARD -> roll == 0 ? Blocks.SOUL_LANTERN : Blocks.COBBLESTONE_WALL;
            case OUTSKIRTS -> roll == 0 ? Blocks.HAY_BLOCK : Blocks.SPRUCE_FENCE;
        };
    }

    static void streetSurface(Canvas c, CityPlan plan, int x, int z, Line xb, Line zb, int roll) {
        District d = plan.districtAt(x, z);
        if (xb == null && zb == null) {
            switch (d) {
                case MARKET -> {
                    if (roll <= 1) c.set(x, CityPlan.STREET_Y, z, Blocks.BRICKS);
                    else if (roll == 2) c.set(x, CityPlan.STREET_Y, z, Blocks.PACKED_MUD);
                }
                case INDUSTRIAL -> {
                    if (roll <= 2) c.set(x, CityPlan.STREET_Y, z, Blocks.POLISHED_BLACKSTONE);
                    else if (roll == 3) c.set(x, CityPlan.STREET_Y, z, Blocks.COBBLED_DEEPSLATE);
                }
                case WATERFRONT -> {
                    if (roll <= 1) c.set(x, CityPlan.STREET_Y, z, Blocks.SPRUCE_PLANKS);
                    else if (roll == 2) c.set(x, CityPlan.STREET_Y, z, Blocks.GRAVEL);
                }
                case DERELICT -> {
                    if (roll <= 1) c.set(x, CityPlan.STREET_Y, z, Blocks.GRAVEL);
                    else if (roll == 2) c.set(x, CityPlan.STREET_Y, z, Blocks.COARSE_DIRT);
                }
                case GRAVEYARD -> {
                    if (roll <= 1) c.set(x, CityPlan.STREET_Y, z, Blocks.CRACKED_STONE_BRICKS);
                }
                case OUTSKIRTS -> {
                    if (roll <= 1) c.set(x, CityPlan.STREET_Y, z, Blocks.PACKED_MUD);
                    else if (roll == 2) c.set(x, CityPlan.STREET_Y, z, Blocks.GRAVEL);
                }
                case RESIDENTIAL -> {
                    if (roll == 0) c.set(x, CityPlan.STREET_Y, z, Blocks.PACKED_MUD);
                    else if (roll == 1) c.set(x, CityPlan.STREET_Y, z, Blocks.STONE_BRICKS);
                }
            }
            return;
        }
        if (xb != null && zb != null) {
            if (xb.kind() == Line.Kind.CANAL || zb.kind() == Line.Kind.CANAL || xb.kind() == Line.Kind.TRAM || zb.kind() == Line.Kind.TRAM) {
                return;
            }
            if (Math.abs(x - xb.center() - plan.xAvenueOffset(xb, z)) >= 2 && Math.abs(z - zb.center() - plan.zAvenueOffset(zb, x)) >= 2) {
                switch (d) {
                    case MARKET -> {
                        if (Math.floorMod(x + z, 4) == 0) c.set(x, CityPlan.STREET_Y, z, Blocks.WHITE_TERRACOTTA);
                    }
                    case INDUSTRIAL -> {
                        if (Math.floorMod(x + z, 5) == 0) c.set(x, CityPlan.STREET_Y, z, Blocks.LIGHT_GRAY_CONCRETE);
                    }
                    case GRAVEYARD -> {
                        if (Math.floorMod(x + z, 4) == 0) c.set(x, CityPlan.STREET_Y, z, Blocks.STONE_BRICKS);
                    }
                    default -> {
                        if (Math.floorMod(x + z, 4) == 0) c.set(x, CityPlan.STREET_Y, z, Blocks.LIGHT_GRAY_CONCRETE);
                    }
                }
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
            Block edge = switch (d) {
                case MARKET -> Blocks.SPRUCE_TRAPDOOR;
                case INDUSTRIAL -> Blocks.IRON_TRAPDOOR;
                case WATERFRONT -> Blocks.DARK_OAK_TRAPDOOR;
                case DERELICT, OUTSKIRTS -> Blocks.OAK_TRAPDOOR;
                case GRAVEYARD -> Blocks.STONE_BRICK_SLAB;
                case RESIDENTIAL -> Blocks.IRON_TRAPDOOR;
            };
            c.set(x, CityPlan.STREET_Y, z, edge);
        } else if (offset <= curb && band.kind() != Line.Kind.TRAM && roll <= 2 && Math.floorMod(along + offset, 7) == 0) {
            Block pave = switch (d) {
                case MARKET -> roll == 0 ? Blocks.BRICKS : Blocks.PACKED_MUD;
                case INDUSTRIAL -> roll == 0 ? Blocks.POLISHED_BLACKSTONE : Blocks.COBBLED_DEEPSLATE;
                case WATERFRONT -> roll == 0 ? Blocks.SPRUCE_PLANKS : Blocks.GRAVEL;
                case DERELICT -> roll == 0 ? Blocks.GRAVEL : Blocks.COARSE_DIRT;
                case GRAVEYARD -> roll == 0 ? Blocks.CRACKED_STONE_BRICKS : Blocks.COBBLESTONE;
                case OUTSKIRTS -> roll == 0 ? Blocks.PACKED_MUD : Blocks.GRAVEL;
                case RESIDENTIAL -> roll == 0 ? Blocks.PACKED_MUD : Blocks.STONE_BRICKS;
            };
            c.set(x, CityPlan.STREET_Y, z, pave);
        }
    }
}
