package com.dorohedoro.hole.worldgen.city;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Purposeful frontier wear, not biome-colored city veneers. Roads remain usable,
 * established canal water is untouched, and useful vanilla materials (sand,
 * clay, logs, snow and cobble) stay harvestable in derelict outskirts.
 * No artificial lights or surface loot chests are placed here.
 */
final class RegionalSurface42 {
    private RegionalSurface42() {}

    /** Touch ordinary side-street paving only. Arterial paving, tracks and quays stay intact. */
    static void street(Canvas c, CityPlan plan, int x, int z, Line xBand, Line zBand) {
        if (xBand != null || zBand != null) return;
        HoleRegions42.Region region = plan.regionAt(x, z);
        if (region == HoleRegions42.Region.CITY && plan.settings.worldDetailVersion() < 35) return;
        // Open country has real traversable ground and occasional connecting
        // roads, not an endless polished grid in every biome. Tram/canal and
        // major municipal surfaces are protected by the caller.
        if (plan.settings.worldDetailVersion() >= 35
                ? UrbanPattern52.at(plan.seed, x, z) == UrbanPattern52.Fabric.OPEN_COUNTRY
                : plan.settings.worldDetailVersion() >= 31
                  && SettlementBands47.at(plan.seed, x, z) == SettlementBands47.Form.OPEN_COUNTRY) {
            Block natural = switch (region) {
                case SNOWFIELDS -> Blocks.GRAVEL;
                case DEAD_TAIGA -> Blocks.COARSE_DIRT;
                case DUST_BASIN -> Blocks.SAND;
                case IRON_BADLANDS -> Blocks.RED_SAND;
                case MOUNTAIN_RIM -> Blocks.COBBLESTONE;
                case CITY -> Blocks.GRAVEL;
            };
            c.set(x, CityPlan.STREET_Y, z, natural);
            return;
        }
        if (region == HoleRegions42.Region.CITY) return; // preserve old paved urban surfaces
        long hash = c.hash(x, 0x4201, z);
        if (CityHash.range(hash, 9) >= 3) return;
        Block replacement = switch (region) {
            case SNOWFIELDS -> Blocks.CALCITE;
            case DEAD_TAIGA -> Blocks.COARSE_DIRT;
            case DUST_BASIN -> Blocks.SAND;
            case IRON_BADLANDS -> Blocks.RED_SAND;
            case MOUNTAIN_RIM -> Blocks.COBBLESTONE;
            case CITY -> Blocks.GRAVEL;
        };
        c.set(x, CityPlan.STREET_Y, z, replacement);
    }

    /** Only abandoned or open land; occupied shops and room layouts retain their carefully placed floors. */
    static void openLot(Canvas c, CityPlan plan, Lot lot) {
        HoleRegions42.Region region = plan.regionAt(lot.rect().centerX(), lot.rect().centerZ());
        if (region == HoleRegions42.Region.CITY && (plan.settings.worldDetailVersion() < 35
                || UrbanPattern52.at(plan.seed, lot.rect().centerX(), lot.rect().centerZ())
                != UrbanPattern52.Fabric.OPEN_COUNTRY)) return;
        if (lot.type() != LotType.VACANT && lot.type() != LotType.RUBBLE
                && lot.type() != LotType.PARK && lot.type() != LotType.JUNKYARD) return;
        Rect area = lot.rect().inset(2);
        if (area.width() < 6 || area.depth() < 6) return;
        for (int x = Math.max(c.minX, area.x0()); x <= Math.min(c.maxX, area.x1()); x++) {
            for (int z = Math.max(c.minZ, area.z0()); z <= Math.min(c.maxZ, area.z1()); z++) {
                BlockState floor = c.get(x, CityPlan.STREET_Y, z);
                boolean ground = floor.is(Blocks.GRASS_BLOCK) || floor.is(Blocks.DIRT)
                        || floor.is(Blocks.COARSE_DIRT) || floor.is(Blocks.GRAVEL)
                        || floor.is(Blocks.SAND) || floor.is(Blocks.PACKED_MUD)
                        || floor.is(Blocks.RED_SAND);
                if (!ground) continue;
                long h = c.hash(x, 0x4202, z);
                if (CityHash.range(h, 5) >= 2) continue;
                Block next = switch (region) {
                    case SNOWFIELDS -> Blocks.SNOW_BLOCK;
                    case DEAD_TAIGA -> Blocks.PODZOL;
                    case DUST_BASIN -> Blocks.SAND;
                    case IRON_BADLANDS -> CityHash.range(h >>> 16, 4) == 0
                            ? Blocks.ORANGE_TERRACOTTA : Blocks.RED_SAND;
                    case MOUNTAIN_RIM -> Blocks.COBBLESTONE;
                    case CITY -> Blocks.DIRT;
                };
                c.set(x, CityPlan.STREET_Y, z, next);
            }
        }
        if (region == HoleRegions42.Region.DEAD_TAIGA && lot.type() == LotType.VACANT) {
            // Scarce bare spruce yields actual logs for survival, not decorative impostors.
            int x = area.x0() + 2;
            int z = area.z1() - 2;
            long h = CityHash.of(lot.hash(), 42, 1);
            if (CityHash.range(h, 3) == 0) deadSpruce(c, x, z, h);
        }
        if (region == HoleRegions42.Region.SNOWFIELDS && lot.type() == LotType.VACANT) {
            // Natural snow can accumulate where nothing blocks it, not on all roads.
            int x = area.x1() - 2, z = area.z0() + 2;
            if (c.get(x, CityPlan.GROUND_Y, z).isAir()) {
                c.set(x, CityPlan.GROUND_Y, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2));
            }
        }
        if (region == HoleRegions42.Region.MOUNTAIN_RIM && lot.type() == LotType.RUBBLE) {
            // A navigable stone outcrop: no artificial ore payouts, no buried entrances.
            int x = area.x1() - 2, z = area.z1() - 2;
            if (c.get(x, CityPlan.GROUND_Y, z).isAir()) {
                c.fill(x, CityPlan.GROUND_Y, z, x, CityPlan.GROUND_Y + 2, z, Blocks.COBBLESTONE);
            }
        }
    }

    private static void deadSpruce(Canvas c, int x, int z, long hash) {
        if (!c.contains(x, z) || !c.get(x, CityPlan.GROUND_Y, z).isAir()) return;
        int height = 4 + CityHash.range(hash >>> 12, 3);
        for (int y = CityPlan.GROUND_Y; y < CityPlan.GROUND_Y + height; y++) {
            if (!c.get(x, y, z).isAir()) return;
        }
        c.fill(x, CityPlan.GROUND_Y, z, x, CityPlan.GROUND_Y + height - 1, z, Blocks.STRIPPED_SPRUCE_LOG);
        Direction side = Direction.from2DDataValue(CityHash.range(hash >>> 28, 4));
        c.set(x + side.getStepX(), CityPlan.GROUND_Y + height - 2, z + side.getStepZ(),
                Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, side.getAxis()));
    }
}
