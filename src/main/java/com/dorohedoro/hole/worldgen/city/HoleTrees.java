package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rare surviving urban trees. Built entirely from vanilla harvestable logs and leaves, so
 * regular sapling drops, axes and crafting continue to work in a ruined-city Overworld.
 * Each lot's choice and geometry depend on the world seed and lot hash only, not chunk order.
 * A tree always sits >= 3 blocks inside its owning lot (including the crown).
 */
final class HoleTrees {
    private HoleTrees() {}

    static void park(Canvas c, Lot lot) {
        Rect r = lot.rect();
        if (r.width() < 10 || r.depth() < 10) return;
        HoleTreePolicy.Form form = HoleTreePolicy.select(c.seed(), lot.hash(), lot.district(), lot.type());
        if (form == HoleTreePolicy.Form.NONE) return;
        // Park ponds are carved at the center AFTER this call. Put trees in the far corner,
        // outside the largest pond footprint, with a clear route to the park interior.
        int x = r.x0() + 3;
        int z = r.z1() - 3;
        tree(c, r, x, z, lot.hash(), form);
    }

    static void vacant(Canvas c, Lot lot) {
        Rect r = lot.rect();
        if (r.width() < 9 || r.depth() < 9) return;
        HoleTreePolicy.Form form = HoleTreePolicy.select(c.seed(), lot.hash(), lot.district(), lot.type());
        if (form == HoleTreePolicy.Form.NONE) return;
        // Offset the tree away from the lot center; sparse grass and gravel occupy the rest.
        long h = CityHash.mix(lot.hash() ^ 0x1EAF12L);
        int x = CityHash.range(h, 2) == 0 ? r.x0() + 3 : r.x1() - 3;
        int z = CityHash.range(h >>> 12, 2) == 0 ? r.z0() + 3 : r.z1() - 3;
        tree(c, r, x, z, h, form);
    }

    static void rubble(Canvas c, Lot lot) {
        Rect r = lot.rect();
        if (r.width() < 12 || r.depth() < 12) return;
        HoleTreePolicy.Form form = HoleTreePolicy.select(c.seed(), lot.hash(), lot.district(), lot.type());
        if (form == HoleTreePolicy.Form.NONE) return;
        // Collapsed walls run around the rim and rubble is piled near center: use open corner.
        tree(c, r, r.x1() - 3, r.z1() - 3, lot.hash(), form);
    }

    private static void tree(Canvas c, Rect lot, int x, int z, long hash, HoleTreePolicy.Form form) {
        if (!lot.contains(x - 2, z - 2) || !lot.contains(x + 2, z + 2)) return;
        int height = 3 + CityHash.range(hash >>> 17, 3);
        Block trunk = form == HoleTreePolicy.Form.DEAD ? Blocks.STRIPPED_OAK_LOG
                : CityHash.range(hash >>> 7, 4) == 0 ? Blocks.SPRUCE_LOG : Blocks.OAK_LOG;
        // Bare trunks stay low and weathered. No full-height foliage or towering city trees.
        c.fill(x, GROUND_Y, z, x, GROUND_Y + height - 1, z, trunk);
        if (form == HoleTreePolicy.Form.DEAD) {
            Direction side = Direction.from2DDataValue(CityHash.range(hash >>> 23, 4));
            int branchY = GROUND_Y + height - 2;
            c.set(x + side.getStepX(), branchY, z + side.getStepZ(),
                    Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            c.set(x, STREET_Y, z, Blocks.COARSE_DIRT);
            return;
        }
        boolean crooked = form == HoleTreePolicy.Form.CROOKED;
        int leanX = crooked ? (CityHash.range(hash >>> 30, 2) == 0 ? -1 : 1) : 0;
        int crownX = x + leanX;
        int crownZ = z;
        if (crooked) {
            c.set(crownX, GROUND_Y + height - 1, crownZ,
                    trunk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.X));
        }
        Block leaves = trunk == Blocks.SPRUCE_LOG ? Blocks.SPRUCE_LEAVES : Blocks.OAK_LEAVES;
        BlockState leaf = leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        // Tiny patchy crowns: exposed wood dominates, not a bright leaf cube.
        // Only 1-2 layers of ragged foliage, not a complete canopy.
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    long part = CityHash.of(hash, crownX + dx, GROUND_Y + height + dy, crownZ + dz);
                    if (CityHash.range(part, 5) >= 2) continue;
                    int bx = crownX + dx, bz = crownZ + dz;
                    if (lot.contains(bx, bz) && !(dx == 0 && dz == 0 && dy == 0))
                        c.set(bx, GROUND_Y + height - 1 + dy, bz, leaf);
                }
            }
        }
        // The trunk remains visible; a tiny living leaf tip marks the few trees
        // that can still give saplings when deliberately harvested by players.
        c.set(crownX, GROUND_Y + height, crownZ, leaf);
    }
}
