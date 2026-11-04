package com.dorohedoro.hole.worldgen.city;

import com.dorohedoro.hole.registry.ModBlocks;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * New-world sewer branches. Most tunnels remain fully constructed sewer infrastructure.
 * Selected intersections have short closed inspection bays, while some lower
 * service branches travel through masonry, broken drains, and abandoned mine passages
 * before entering naturally shaped deep caverns. These are not immediate sewer/cave openings.
 *
 * <p>Every choice comes only from the seed, arterial indexes and absolute coordinates.
 * Each write is clipped through Canvas, so processing order and chunk boundaries do not
 * change layout. The natural deep Minecraft cave noise remains intact outside these volumes.
 */
public final class SewerCaveBranches {

    private SewerCaveBranches() {}

    /** 0 = cave descent, 1-2 = closed inspection bay, others = ordinary sewers. */
    public static int branchKind(long seed, int xIndex, int zIndex) {
        return SewerBranchPolicy.kind(seed, xIndex, zIndex);
    }

    public static int branchKind(long seed, int xIndex, int zIndex, int caveFrequency) {
        return SewerBranchPolicy.kind(seed, xIndex, zIndex, caveFrequency);
    }

    public static boolean connectsToCave(long seed, int xIndex, int zIndex) {
        return SewerBranchPolicy.connectsToCave(seed, xIndex, zIndex);
    }

    public static boolean connectsToCave(long seed, int xIndex, int zIndex, int caveFrequency) {
        return SewerBranchPolicy.connectsToCave(seed, xIndex, zIndex, caveFrequency);
    }

    public static int branchDirection(long seed, int xIndex, int zIndex) {
        return SewerBranchPolicy.direction(seed, xIndex, zIndex);
    }

    public static void paint(Canvas c, CityPlan plan) {
        // Cave branches reach at most 75 blocks from their source intersection.
        // Including the entire possible footprint prevents seams at chunk borders.
        List<Line> xLines = plan.xLinesNear(c.minX - 100, c.maxX + 100);
        List<Line> zLines = plan.zLinesNear(c.minZ - 100, c.maxZ + 100);
        for (Line xLine : xLines) {
            for (Line zLine : zLines) {
                int type = branchKind(plan.seed, xLine.index(), zLine.index(), plan.settings.sewerCaveFrequency());
                int cx = xLine.center();
                int cz = zLine.center();
                // These are deliberately offset from the main shaft, leaving the access ladder safe.
                int bx = cx + 12;
                int direction = branchDirection(plan.seed, xLine.index(), zLine.index());
                if (type == 0) {
                    boolean extended = plan.settings.sewerCavernVersion() >= 1;
                    int footprint = extended ? 27 : 21;
                    int reach = extended ? 86 : 78;
                    if (c.overlaps(new Rect(bx - footprint, Math.min(cz, cz + reach * direction),
                            bx + footprint, Math.max(cz, cz + reach * direction)))) {
                        paintCaveApproach(c, plan, bx, cz, direction, extended);
                    }
                } else if (type == 1 || type == 2) {
                    if (c.overlaps(new Rect(bx - 6, Math.min(cz, cz + direction * 21),
                            bx + 6, Math.max(cz, cz + direction * 21)))) {
                        paintClosedInspectionBay(c, plan.seed, bx, cz, direction,
                                SewerHazards61.infested(plan.seed, xLine.index(), zLine.index(),
                                        type, plan.settings.worldDetailVersion()));
                    }
                }
            }
        }
    }

    /**
     * Distinct, *closed* shallow sewer side-chamber. This never connects to natural caves.
     * The hall takes 11 blocks to reach the inspection room and the end is masonry-sealed.
     */
    private static void paintClosedInspectionBay(Canvas c, long seed, int bx, int cz,
                                                  int direction, boolean infested) {
        for (int step = 0; step <= 12; step++) {
            int z = cz + direction * step;
            for (int x = bx - 2; x <= bx + 2; x++) {
                c.fill(x, 48, z, x, 54, z, ModBlocks.SEWER_BRICKWORK.get());
                if (Math.abs(x - bx) <= 1 && step < 12) {
                    c.fill(x, 50, z, x, 53, z, Blocks.AIR);
                }
                if (Math.abs(x - bx) <= 1 && step < 12) {
                    c.set(x, 49, z, Blocks.POLISHED_ANDESITE);
                }
            }
        }
        int endZ = cz + direction * 12;
        for (int x = bx - 5; x <= bx + 5; x++) {
            for (int z = Math.min(endZ, endZ + direction * 8); z <= Math.max(endZ, endZ + direction * 8); z++) {
                c.fill(x, 48, z, x, 55, z, Blocks.STONE_BRICKS);
                if (Math.abs(x - bx) <= 4 && z != endZ + direction * 8) {
                    c.fill(x, 50, z, x, 54, z, Blocks.AIR);
                    c.set(x, 49, z, Blocks.DEEPSLATE_TILES);
                }
            }
        }
        // Keep this room clearly part of the sewer network and sealed at its far wall.
        c.set(bx - 3, 50, endZ + direction * 5, Blocks.CRAFTING_TABLE);
        c.barrel(bx + 3, 50, endZ + direction * 5, "sewer_cache");
        if (infested) {
            // Optional encounter: spawner can be broken with ordinary mining tools.
            // Behind the side-bay entrance, away from the continuous sewer walkway.
            // The established loot barrel and crafting table remain obtainable.
            c.spawner(bx + 2, 50, endZ + direction * 6, "dorohedoro:sewer_roach");
        } else {
            c.set(bx, 54, endZ + direction * 5,
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }

    /** A long lower-level side branch. First intact infrastructure, then damage, then rock. */
    private static void paintCaveApproach(Canvas c, CityPlan plan, int bx, int cz, int direction, boolean extended) {
        long seed = plan.seed;
        for (int step = 0; step <= 49; step++) {
            int z = cz + direction * step;
            // Short staircase down from the -36 rail gallery: accessible one-block rises only.
            int floor = -36 - Math.min(3, Math.max(0, (step - 15) / 8));
            for (int x = bx - 2; x <= bx + 2; x++) {
                int lateral = Math.abs(x - bx);
                // The first 20 blocks are fully brick sewer; after that maintenance masonry
                // gives way gradually to fractured deepslate, then a natural mining passage.
                if (step < 19) {
                    c.fill(x, floor - 1, z, x, floor + 4, z, ModBlocks.SEWER_BRICKWORK.get());
                } else if (step < 31) {
                    c.fill(x, floor - 1, z, x, floor + 4, z,
                            CityHash.range(CityHash.of(seed, x, step, z), 5) == 0
                                    ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_BRICKS);
                } else {
                    c.fill(x, floor - 1, z, x, floor + 4, z, Blocks.DEEPSLATE);
                }
                if (lateral <= 1) {
                    c.fill(x, floor + 1, z, x, floor + 3, z, Blocks.AIR);
                    c.set(x, floor, z, step < 19 ? Blocks.DEEPSLATE_TILES
                            : step < 31 ? Blocks.CRACKED_DEEPSLATE_TILES : Blocks.COBBLED_DEEPSLATE);
                }
            }
            if (step == 11 || step == 23) {
                c.set(bx, floor + 3, z,
                        Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
        // The approach is long enough to read visually as a real sewer/mining route.
        // A carved end cavern makes the destination dependable regardless of noise luck.
        int mouthZ = cz + direction * 59;
        long site = CityHash.of(seed, 0xC4A1E2026L, bx, mouthZ);
        SewerCavernProfile profile = extended ? SewerCavernProfile.at(seed, bx, mouthZ) : null;
        int rx = extended ? profile.radiusX() : 14 + CityHash.range(site, 4);
        int rz = extended ? profile.radiusZ() : 14 + CityHash.range(CityHash.mix(site), 5);
        for (int x = Math.max(c.minX, bx - rx); x <= Math.min(c.maxX, bx + rx); x++) {
            for (int z = Math.max(c.minZ, mouthZ - rz); z <= Math.min(c.maxZ, mouthZ + rz); z++) {
                double dx = (x - bx) / (double) rx;
                double dz = (z - mouthZ) / (double) rz;
                double irregularity = (CityHash.unit(CityHash.of(seed, 0xC4A1E2L, x >> 2, z >> 2)) - 0.5) * 0.28;
                double r2 = dx * dx + dz * dz;
                if (r2 > 0.93 + irregularity) continue;
                double taper = Math.max(0.12, 1.0 - Math.max(0.0, r2));
                int halfHeight = 3 + (int) Math.round(8.0 * Math.sqrt(taper));
                // Deep natural cave with a patchy floor; never turn it into a Nether or cave biome reskin.
                c.fill(x, -39 - halfHeight / 3, z, x, -39 + halfHeight, z, Blocks.AIR);
                if (r2 < 0.72) {
                    c.set(x, -40 - halfHeight / 3, z,
                            CityHash.range(c.hash(x, -40, z), 9) == 0 ? Blocks.TUFF : Blocks.DEEPSLATE);
                }
            }
        }
        // The cave entrance opens from the far end of the graded approach, not the sewer.
        // Keep old worlds byte-for-byte compatible: their 49..59 landing retains its original Y.
        // Version 1 descends in four one-block drops from gallery floor Y=-39 to the natural
        // chamber floor Y=-43. Each block has three clear blocks above it, avoiding the old
        // 4-block fall straight into a cavern at the end of the passage.
        for (int step = extended ? 43 : 49; step <= 59; step++) {
            int z = cz + direction * step;
            int floor = extended ? (c.hasMacroRelief40()
                    ? SewerCavernProfile.entranceFloor(plan, bx, cz, direction, step)
                    : SewerCavernProfile.entranceFloor(seed, bx, cz, direction, step, c.hasTerrainRelief())) : -39;
            for (int x = bx - 1; x <= bx + 1; x++) {
                c.fill(x, floor + 1, z, x, floor + 3, z, Blocks.AIR);
                c.set(x, floor, z, Blocks.COBBLED_DEEPSLATE);
            }
        }
        // Pass 70: outside the protected 3-wide descent, widen and weather the
        // last masonry into a progressively rough cave passage. This is still
        // an ordinary mineable route and needs no techniques or special items.
        if (plan.settings.worldDetailVersion() >= SewerCaveTransitions70.FIRST_VERSION) {
            for (int step = 20; step <= 55; step++) {
                int floor = step < 43 ? -36 - Math.min(3, Math.max(0, (step - 15) / 8))
                        : c.hasMacroRelief40()
                          ? SewerCavernProfile.entranceFloor(plan, bx, cz, direction, step)
                          : SewerCavernProfile.entranceFloor(seed, bx, cz, direction, step,
                                  c.hasTerrainRelief());
                SewerCaveTransitions70.paintStep(c, seed, bx, cz + direction * step, step, floor);
            }
        }
    }
}