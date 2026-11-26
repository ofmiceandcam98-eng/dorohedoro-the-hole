package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.worldgen.city.CityPlan;
import com.dorohedoro.hole.worldgen.city.CityTerrain40;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Pass 73 (v54): desert pyramids and jungle temples can generate in Hole worlds.
 *
 * <p>Vanilla only places them where the lowest of four footprint corners is at or above sea level. It reads that
 * height from the noise router, but in the Hole the noise surface is a flat layer just below sea level and the real
 * ground is the city painted on top later. So no pyramid could ever start. In v54 worlds the corner heights are the
 * city's own ground instead.
 *
 * <p>A temple also needs a clear site. The footprint must sit inside one city block: no street or canal crosses it, and
 * no corner is in the sea or on the shore. Anything else reports below sea level, so vanilla rejects it as it would
 * any wet site. The city then keeps its buildings out of the accepted footprint (see VanillaStructureLookup64), so the
 * temple stands in a clear lot on city ground.
 */
public final class SurfaceStructures73 {
    public static final int FIRST_VERSION = 54;
    /** Keeps temples back from the beach so they never sit on sand at the waterline. */
    private static final int SHORE_MARGIN = 12;
    /** Gap between a footprint and the nearest street or canal edge. */
    private static final int STREET_MARGIN = 2;

    private SurfaceStructures73() {}

    /**
     * The lowest city-ground height under a {@code width} by {@code depth} footprint at the chunk's minimum corner, or
     * {@code null} when this is not a v54 Hole world and vanilla's own rule applies.
     */
    public static Integer cityLowestY(Structure.GenerationContext context, int width, int depth) {
        if (!(context.chunkGenerator() instanceof HoleChunkGenerator generator)
                || generator.citySettings().worldDetailVersion() < FIRST_VERSION) {
            return null;
        }
        CityPlan plan = generator.plan(context.seed(), context.randomState());
        ChunkPos chunk = context.chunkPos();
        int x0 = chunk.getMinBlockX(), z0 = chunk.getMinBlockZ(), x1 = x0 + width, z1 = z0 + depth;
        int wet = generator.getSeaLevel() - 1;
        if (!plan.xLinesNear(x0 - STREET_MARGIN, x1 + STREET_MARGIN).isEmpty()
                || !plan.zLinesNear(z0 - STREET_MARGIN, z1 + STREET_MARGIN).isEmpty()) {
            return wet;
        }
        int lowest = Integer.MAX_VALUE;
        for (int x : new int[] {x0, x1}) {
            for (int z : new int[] {z0, z1}) {
                int coast = plan.coastalDistanceAt(x, z);
                if (plan.oceanDepthAt(x, z) > 0 || (coast > 0 && coast <= SHORE_MARGIN)) return wet;
                lowest = Math.min(lowest, CityPlan.GROUND_Y + CityTerrain40.offset(plan, x, z));
            }
        }
        return lowest;
    }

    /** True in worlds where {@link #cityLowestY} replaces vanilla's corner heights. */
    public static boolean applies(Structure.GenerationContext context) {
        return context.chunkGenerator() instanceof HoleChunkGenerator generator
                && generator.citySettings().worldDetailVersion() >= FIRST_VERSION;
    }
}
