package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.block.Blocks;

/**
 * Pass 70: the same 30% selected cave branches gradually fray into a wider
 * exposed-rock passage. The original three-wide accessible floor/stair remains
 * untouched; only the rock immediately outside its edges is shaped.
 *
 * <p>This is NOT a guarantee of intersection with an independently noise-carved
 * vanilla cave. The pre-existing procedural destination cavern remains available.
 */
final class SewerCaveTransitions70 {
    static final int FIRST_VERSION = 52;
    private static final long SALT = 0x57027072L;
    private SewerCaveTransitions70() {}

    static int widthAt(int step) {
        if (step < 30) return 2;
        if (step < 42) return 3;
        if (step < 49) return 4;
        return 5;
    }

    /** Called for steps 20..55 after existing branch floor and cavern landing. */
    static void paintStep(Canvas c, long seed, int bx, int z, int step, int floor) {
        if (step < 20 || step > 55) return;
        int radius = widthAt(step);
        for (int lateral = -radius - 1; lateral <= radius + 1; lateral++) {
            int distance = Math.abs(lateral);
            if (distance <= 1) continue; // never replace the three-wide walking/stair lane
            int x = bx + lateral;
            long key = CityHash.of(seed, SALT + step, x, z);
            // Ragged but traversable side margins increase with depth. The last
            // perimeter block stays solid, so not every step becomes a square void.
            boolean sideAir = distance < radius || (distance == radius && CityHash.range(key, 4) != 0);
            if (sideAir) {
                c.set(x, floor - 1, z, CityHash.range(key, 5) == 0
                        ? Blocks.TUFF : Blocks.COBBLED_DEEPSLATE);
                c.set(x, floor, z, CityHash.range(CityHash.mix(key), 6) == 0
                        ? Blocks.TUFF : Blocks.COBBLED_DEEPSLATE);
                c.fill(x, floor + 1, z, x, floor + 3, z, Blocks.AIR);
                // A variable rough ceiling, always outside the primary walk path.
                c.set(x, floor + 4, z, CityHash.range(key, 3) == 0
                        ? Blocks.TUFF : Blocks.DEEPSLATE);
            } else {
                c.fill(x, floor, z, x, floor + 3, z,
                        step < 30 ? Blocks.CRACKED_DEEPSLATE_BRICKS
                                : CityHash.range(key, 4) == 0 ? Blocks.TUFF : Blocks.DEEPSLATE);
            }
        }
    }
}
