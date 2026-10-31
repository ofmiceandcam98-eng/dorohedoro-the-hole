package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lots with no building, or only glass over soil. These are where wood, saplings, crops, animals and loose earth come
 * from now that the Overworld has no countryside, so what each one offers matters as much as how it looks.
 */
final class OpenLots {
    private OpenLots() {}

    static Mix debris() {
        return Mix.of(ModBlocks.INDUSTRIAL_DEBRIS.get(), 5, Blocks.GRAVEL, 3, ModBlocks.RUSTED_PLATING.get(), 2, Blocks.SAND, 1, Blocks.COBBLESTONE, 2);
    }

    private static void soil(Canvas c, Rect r) {
        c.fill(r, STREET_Y - 2, STREET_Y - 1, Blocks.DIRT);
        c.fill(r, STREET_Y, STREET_Y, Blocks.GRASS_BLOCK);
    }

    /** A small hardwood. Leaves are persistent so a lone tree never decays away before it can yield saplings. */
    static void tree(Canvas c, int x, int z, int height, Block log, Block leaves) {
        BlockState foliage = leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int top = GROUND_Y + height - 1;
        for (int y = top - 2; y <= top + 1; y++) {
            int radius = y >= top ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    boolean corner = Math.abs(dx) == radius && Math.abs(dz) == radius;
                    if (!corner || CityHash.range(c.hash(x + dx, y, z + dz), 2) == 0) {
                        c.set(x + dx, y, z + dz, foliage);
                    }
                }
            }
        }
        c.fill(x, GROUND_Y, z, x, top, z, log);
    }

    static void marketStalls(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Block[][] goods = {
                {Blocks.HAY_BLOCK, Blocks.MELON},
                {Blocks.PUMPKIN, Blocks.DRIED_KELP_BLOCK},
                {Blocks.BARREL, Blocks.COMPOSTER},
                {Blocks.HONEYCOMB_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK}
        };
        Block[] cloth = {Blocks.RED_WOOL, Blocks.GREEN_WOOL, Blocks.ORANGE_WOOL, Blocks.CYAN_WOOL};
        int index = 0;
        for (int x = r.x0() + 1; x + 4 <= r.x1() - 1; x += 7) {
            for (int z = r.z0() + 1; z + 3 <= r.z1() - 1; z += 6) {
                long h = CityHash.mix(lot.hash() + index++);
                Block roof = cloth[CityHash.range(h, cloth.length)];
                Block[] stock = goods[CityHash.range(h >>> 16, goods.length)];
                for (int y = GROUND_Y; y <= GROUND_Y + 2; y++) {
                    c.set(x, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x + 4, y, z, Blocks.SPRUCE_FENCE);
                    c.set(x, y, z + 3, Blocks.SPRUCE_FENCE);
                    c.set(x + 4, y, z + 3, Blocks.SPRUCE_FENCE);
                }
                for (int dx = 0; dx <= 4; dx++) {
                    c.fill(x + dx, GROUND_Y + 3, z, x + dx, GROUND_Y + 3, z + 3, dx % 2 == 0 ? roof : Blocks.WHITE_WOOL);
                }
                c.set(x + 1, GROUND_Y, z + 1, stock[0]);
                c.set(x + 2, GROUND_Y, z + 1, stock[1]);
                c.chest(x + 3, GROUND_Y, z + 1, Direction.SOUTH, "market");
            }
        }
    }

    /** Soil under glass: crops, sugar cane, pumpkins, melons, mushrooms and bees, with a water channel feeding it all. */
    static void greenhouse(Canvas c, Lot lot) {
        Rect box = lot.rect().inset(1);
        if (box.width() < 9 || box.depth() < 7) {
            nursery(c, lot);
            return;
        }
        int roofY = GROUND_Y + 5;
        c.fill(box, STREET_Y - 1, STREET_Y - 1, Blocks.DIRT);
        c.fill(box, STREET_Y, STREET_Y, Blocks.DIRT);
        c.walls(box, GROUND_Y, roofY, Mix.of(Blocks.GLASS, 1));
        c.fill(box, roofY, roofY, Blocks.GLASS);
        for (int x = box.x0(); x <= box.x1(); x += 4) {
            c.fill(x, GROUND_Y, box.z0(), x, roofY, box.z0(), Blocks.DARK_OAK_LOG);
            c.fill(x, GROUND_Y, box.z1(), x, roofY, box.z1(), Blocks.DARK_OAK_LOG);
            c.fill(x, roofY, box.z0(), x, roofY, box.z1(), Blocks.DARK_OAK_LOG);
        }
        Rect in = box.inset(1);
        int aisleZ = in.centerZ();
        BlockState wet = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
        for (int x = in.x0(); x <= in.x1(); x++) {
            int column = x - in.x0();
            boolean channel = column % 5 == 2;
            Block crop = crops[Math.floorMod(column / 5 + CityHash.range(lot.hash(), 4), crops.length)];
            for (int z = in.z0(); z <= in.z1(); z++) {
                if (z == aisleZ && !channel) {
                    c.set(x, STREET_Y, z, Blocks.MOSS_BLOCK);
                } else if (channel) {
                    c.set(x, STREET_Y, z, Blocks.WATER);
                } else if (column % 5 == 3 && z < aisleZ) {
                    // Cane rooted beside the channel.
                    c.set(x, STREET_Y, z, Blocks.SAND);
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + 2, z, Blocks.SUGAR_CANE);
                } else if (crop instanceof CropBlock cropBlock) {
                    c.set(x, STREET_Y, z, wet);
                    c.set(x, GROUND_Y, z, cropBlock.getStateForAge(CityHash.range(c.hash(x, STREET_Y, z), cropBlock.getMaxAge() + 1)));
                }
            }
        }
        // The aisle bridges every channel so the whole house stays walkable.
        for (int x = in.x0(); x <= in.x1(); x++) {
            c.set(x, STREET_Y, aisleZ, Blocks.MOSS_BLOCK);
        }
        // Stems with their fruit beside them at the two aisle ends.
        BlockState pumpkinStem = Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE);
        BlockState melonStem = Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE);
        c.set(in.x0(), STREET_Y, in.z1(), wet);
        c.set(in.x0(), GROUND_Y, in.z1(), pumpkinStem);
        c.set(in.x0() + 1, STREET_Y, in.z1(), Blocks.DIRT);
        c.set(in.x0() + 1, GROUND_Y, in.z1(), Blocks.PUMPKIN);
        c.set(in.x1(), STREET_Y, in.z1(), wet);
        c.set(in.x1(), GROUND_Y, in.z1(), melonStem);
        c.set(in.x1() - 1, STREET_Y, in.z1(), Blocks.DIRT);
        c.set(in.x1() - 1, GROUND_Y, in.z1(), Blocks.MELON);
        c.facing(box.x0() + 1, GROUND_Y + 3, box.z0() + 1, Direction.SOUTH, Blocks.BEE_NEST);
        c.set(in.x1() - 1, GROUND_Y, aisleZ + 1, Blocks.COMPOSTER);
        c.chest(in.x0(), GROUND_Y, aisleZ, Direction.EAST, "nursery");

        // The door sits at the east end of the central aisle.
        c.door(box.x1(), GROUND_Y, aisleZ, Direction.WEST, Blocks.DARK_OAK_DOOR);
    }

    /** Fenced grass the city's animals live in. The animals themselves are added by {@link CityPopulator}. */
    static void livestockPen(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.SPRUCE_FENCE, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y, fence.z1(), Blocks.SPRUCE_FENCE_GATE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.SOUTH));
        c.set(fence.x0() + 1, GROUND_Y, fence.z0() + 1, Blocks.HAY_BLOCK);
        c.set(fence.x0() + 2, GROUND_Y, fence.z0() + 1, Blocks.HAY_BLOCK);
        c.set(fence.x1() - 1, GROUND_Y, fence.z0() + 1, Blocks.COMPOSTER);
        c.fill(fence.x0() + 2, STREET_Y, fence.z1() - 2, fence.x0() + 3, STREET_Y, fence.z1() - 2, Blocks.WATER);
    }

    static void nursery(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.SPRUCE_FENCE, fence.centerX(), fence.z1());
        Rect in = fence.inset(1);
        Block[] saplings = {Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.CHERRY_SAPLING};
        for (int x = in.x0(); x <= in.x1(); x += 2) {
            c.set(x, GROUND_Y, in.z0(), saplings[Math.floorMod(x, saplings.length)]);
        }
        tree(c, in.x0() + 2, in.centerZ(), 5, Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        if (in.width() >= 11) {
            tree(c, in.x1() - 2, in.centerZ(), 6, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
        }
        c.chest(in.x1(), GROUND_Y, in.z1(), Direction.WEST, "nursery");
        c.set(in.x0(), GROUND_Y, in.z1(), Blocks.COMPOSTER);
    }

    static void park(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Block[][] trees = {{Blocks.OAK_LOG, Blocks.OAK_LEAVES}, {Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES}, {Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES}};
        int count = 2 + r.area() / 180;
        for (int i = 0; i < count; i++) {
            long h = CityHash.mix(lot.hash() + i);
            int x = r.x0() + 3 + CityHash.range(h, Math.max(1, r.width() - 6));
            int z = r.z0() + 3 + CityHash.range(h >>> 16, Math.max(1, r.depth() - 6));
            Block[] kind = trees[CityHash.range(h >>> 32, trees.length)];
            tree(c, x, z, 5 + CityHash.range(h >>> 40, 3), kind[0], kind[1]);
        }
        // A pond rimmed with sand and clay, which keeps both obtainable without digging canals.
        if (r.width() >= 14 && r.depth() >= 14) {
            int px = r.centerX();
            int pz = r.centerZ();
            c.fill(px - 4, STREET_Y - 1, pz - 3, px + 4, STREET_Y, pz + 3, Blocks.SAND);
            c.fill(px - 3, STREET_Y - 2, pz - 2, px + 3, STREET_Y - 2, pz + 2, Blocks.CLAY);
            c.fill(px - 3, STREET_Y - 1, pz - 2, px + 3, STREET_Y, pz + 2, Blocks.WATER);
        }
        for (int x = r.x0() + 1; x <= r.x1() - 1; x += 2) {
            for (int z = r.z0() + 1; z <= r.z1() - 1; z += 2) {
                long h = c.hash(x, GROUND_Y, z);
                if (c.contains(x, z) && c.get(x, GROUND_Y, z).isAir() && c.get(x, STREET_Y, z).is(Blocks.GRASS_BLOCK) && CityHash.range(h, 5) == 0) {
                    c.set(x, GROUND_Y, z, CityHash.range(h >>> 16, 4) == 0 ? Blocks.DANDELION : Blocks.SHORT_GRASS);
                }
            }
        }
    }

    /**
     * Overgrown empty ground: patchy soil, weeds and the odd dead or struggling tree. This is where the Hole's rare
     * vegetation belongs. Most lots stay treeless, and even a living tree here is small and thin.
     */
    static void vacant(Canvas c, Lot lot) {
        Rect r = lot.rect();
        c.fill(r, STREET_Y - 1, STREET_Y - 1, Blocks.DIRT);
        c.fill(r, STREET_Y, STREET_Y, Mix.of(Blocks.COARSE_DIRT, 5, Blocks.DIRT, 3, Blocks.GRASS_BLOCK, 3, Blocks.GRAVEL, 1, Blocks.PODZOL, 1));
        for (int x = Math.max(r.x0(), c.minX); x <= Math.min(r.x1(), c.maxX); x++) {
            for (int z = Math.max(r.z0(), c.minZ); z <= Math.min(r.z1(), c.maxZ); z++) {
                long h = c.hash(x, GROUND_Y, z);
                if (!c.get(x, STREET_Y, z).is(Blocks.GRASS_BLOCK) && !c.get(x, STREET_Y, z).is(Blocks.PODZOL)) {
                    continue;
                }
                int roll = CityHash.range(h, 12);
                if (roll < 3) {
                    c.set(x, GROUND_Y, z, Blocks.SHORT_GRASS);
                } else if (roll == 3) {
                    c.set(x, GROUND_Y, z, Blocks.DEAD_BUSH);
                } else if (roll == 4 && CityHash.range(h >>> 16, 4) == 0) {
                    c.set(x, GROUND_Y, z, Blocks.SWEET_BERRY_BUSH);
                }
            }
        }
        int roll = CityHash.range(lot.hash() >>> 8, 10);
        if (roll < 4) {
            deadTree(c, r.centerX(), r.centerZ(), lot.hash());
        } else if (roll == 4) {
            tree(c, r.centerX(), r.centerZ(), 4, Blocks.OAK_LOG, Blocks.OAK_LEAVES);
        }
    }

    /** A bare trunk with a few stubs of branch. It yields a little wood but no saplings. */
    static void deadTree(Canvas c, int x, int z, long hash) {
        int height = 4 + CityHash.range(hash, 4);
        c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, Blocks.STRIPPED_OAK_LOG);
        Direction[] sides = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < 3; i++) {
            Direction side = sides[CityHash.range(hash >>> (i * 8 + 4), sides.length)];
            int y = GROUND_Y + height - 1 - i * 2;
            if (y > GROUND_Y) {
                c.set(x + side.getStepX(), y, z + side.getStepZ(), Blocks.STRIPPED_OAK_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, side.getAxis()));
            }
        }
    }

    private static void heap(Canvas c, int cx, int cz, int radius, Mix mix) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                int height = radius - Math.max(Math.abs(x - cx), Math.abs(z - cz)) - CityHash.range(c.hash(x, 0, z), 2);
                if (height > 0) {
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, mix);
                }
            }
        }
    }

    static void junkyard(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect fence = r.inset(1);
        Mix debris = debris();
        int heaps = 1 + r.area() / 120;
        for (int i = 0; i < heaps; i++) {
            long h = CityHash.mix(lot.hash() ^ (i * 31L));
            int radius = 2 + CityHash.range(h, 3);
            int x = r.x0() + 2 + radius + CityHash.range(h >>> 16, Math.max(1, r.width() - 4 - radius * 2));
            int z = r.z0() + 2 + radius + CityHash.range(h >>> 32, Math.max(1, r.depth() - 4 - radius * 2));
            heap(c, x, z, radius, debris);
        }
        // Stacked salvage timber: the only logs in the district.
        for (int x = fence.x0() + 1; x <= Math.min(fence.x1() - 1, fence.x0() + 4); x++) {
            c.fill(x, GROUND_Y, fence.z0() + 1, x, GROUND_Y + 1, fence.z0() + 1, Blocks.OAK_LOG);
        }
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y + 1, fence.z1(), Blocks.AIR);
        c.chest(fence.x1() - 1, GROUND_Y, fence.z1() - 1, Direction.WEST, "scrap");
    }

    static void rubble(Canvas c, Lot lot) {
        Rect r = lot.rect();
        Rect shell = r.inset(1);
        Mix broken = Mix.of(Blocks.CRACKED_STONE_BRICKS, 3, Blocks.COBBLESTONE, 3, Blocks.MOSSY_COBBLESTONE, 1, Blocks.BRICKS, 2);
        Mix ground = Mix.of(Blocks.COARSE_DIRT, 4, Blocks.GRAVEL, 3, Blocks.DIRT, 3, Blocks.SAND, 1, Blocks.COBBLESTONE, 1);
        c.fill(r, STREET_Y, STREET_Y, ground);
        for (int x = shell.x0(); x <= shell.x1(); x++) {
            for (int z = shell.z0(); z <= shell.z1(); z++) {
                boolean edge = x == shell.x0() || x == shell.x1() || z == shell.z0() || z == shell.z1();
                if (!edge || !c.contains(x, z)) {
                    continue;
                }
                int height = CityHash.range(c.hash(x, 0, z), 4);
                if (height > 0 && Math.abs(x - shell.centerX()) > 1) {
                    c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, broken);
                }
            }
        }
        heap(c, shell.centerX() - 1, shell.centerZ(), Math.min(3, shell.width() / 4), broken);
        c.set(shell.x0() + 1, GROUND_Y, shell.z0() + 1, Blocks.RED_MUSHROOM);
        c.set(shell.x0() + 2, GROUND_Y, shell.z0() + 1, Blocks.BROWN_MUSHROOM);
    }

    static void cemetery(Canvas c, Lot lot) {
        Rect r = lot.rect();
        soil(c, r);
        Mix plot = Mix.of(Blocks.PODZOL, 3, Blocks.COARSE_DIRT, 2, Blocks.GRASS_BLOCK, 3);
        c.fill(r, STREET_Y, STREET_Y, plot);
        Rect fence = r.inset(1);
        c.fenceRing(fence, GROUND_Y, Blocks.IRON_BARS, fence.centerX(), fence.z1());
        c.set(fence.centerX(), GROUND_Y, fence.z1(), Blocks.AIR);
        Rect in = fence.inset(1);
        for (int x = in.x0(); x <= in.x1(); x += 3) {
            if (Math.abs(x - in.centerX()) <= 1) {
                continue;
            }
            for (int z = in.z0(); z <= in.z1() - 1; z += 3) {
                long h = c.hash(x, GROUND_Y, z);
                Block marker = CityHash.range(h, 3) == 0 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.COBBLESTONE_WALL;
                c.set(x, GROUND_Y, z, marker);
                c.set(x, STREET_Y, z + 1, Blocks.PODZOL);
            }
        }
        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
        c.fill(in.centerX(), GROUND_Y, in.centerZ(), in.centerX(), GROUND_Y + 2, in.centerZ(), Blocks.DARK_OAK_FENCE);
        c.set(in.centerX(), GROUND_Y + 3, in.centerZ(), lantern);
        if (in.depth() >= 9) {
            c.barrel(in.centerX(), GROUND_Y, in.z0(), "crypt");
        }
    }
}
