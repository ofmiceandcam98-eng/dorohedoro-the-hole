package com.dorohedoro.hole.worldgen;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidStructure;
import net.minecraft.world.level.levelgen.structure.structures.JungleTempleStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;

/**
 * Pass 72: answers "can this structure start here?" cheaply when the answer is provably no in a Hole world, so
 * {@code /locate}, explorer maps and chunk generation stop paying for vanilla's expensive checks.
 *
 * <p>Every rule returns {@code true} only where vanilla's {@code findValidGenerationPoint} is certain to return empty,
 * so generation is unchanged. QA compares both paths candidate by candidate ({@code StructureQa}).
 *
 * <ul>
 *   <li><b>Ocean monument.</b> Vanilla rejects a candidate if any biome in a cube of radius 29 around sea level fails
 *   {@code #required_ocean_monument_surrounding}. That cube reaches y = 32, below {@link HoleBiomeSource#UNDERGROUND_TOP},
 *   where the Hole returns cave biomes. Vanilla still samples all ~3,400 cells first. One sample of the cube's lowest
 *   layer, which lies inside the same cube, proves the rejection.</li>
 *   <li><b>Desert and jungle pyramids.</b> Vanilla computes four corner heights and the center height from the noise
 *   router before it checks the biome at the center's surface. At or above {@code UNDERGROUND_TOP} that biome is the
 *   column's surface biome; below it, it is a cave biome. If neither can pass, the heights are irrelevant. Otherwise
 *   one corner below sea level already fails vanilla's lowest-corner rule.</li>
 * </ul>
 */
public final class StructurePrecheck72 {
    /** Vanilla's radius in {@code OceanMonumentStructure.findGenerationPoint}. */
    private static final int MONUMENT_SURROUNDING_RADIUS = 29;
    /** Structures whose generation point is {@code SinglePieceStructure}'s: lowest corner, then the chunk center surface. */
    private static final Set<Class<?>> CENTER_SURFACE_STRUCTURES = Set.of(DesertPyramidStructure.class, JungleTempleStructure.class);

    /** QA turns this off to time and compare the unmodified vanilla path. */
    public static volatile boolean enabled = !"false".equals(System.getProperty("dorohedoro.structurePrecheck"));

    private StructurePrecheck72() {}

    public static boolean provablyAbsent(Structure structure, Structure.GenerationContext context) {
        if (!enabled) return false;
        if (structure instanceof OceanMonumentStructure) {
            if (!(context.biomeSource() instanceof HoleBiomeSource source)) return false;
            ChunkPos chunk = context.chunkPos();
            int bottom = context.chunkGenerator().getSeaLevel() - MONUMENT_SURROUNDING_RADIUS;
            Holder<Biome> lowest = source.getNoiseBiome(QuartPos.fromBlock(chunk.getBlockX(9)), QuartPos.fromBlock(bottom),
                    QuartPos.fromBlock(chunk.getBlockZ(9)), context.randomState().sampler());
            return !lowest.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING);
        }
        if (CENTER_SURFACE_STRUCTURES.contains(structure.getClass())) {
            // Vanilla's biome check reads the generator's biome source, not the context's.
            if (!(context.chunkGenerator().getBiomeSource() instanceof HoleBiomeSource source)) return false;
            Predicate<Holder<Biome>> valid = context.validBiome();
            ChunkPos chunk = context.chunkPos();
            if (!source.anyUndergroundBiome(valid)
                    && !valid.test(source.surfaceBiome(chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), context.randomState().sampler()))) {
                return true;
            }
            // Vanilla's getLowestY is the minimum of four corner heights, the first at the chunk's minimum corner, so one
            // corner below sea level settles it. The Hole's surface noise is flat just below sea level, so in the
            // frontier biomes this is what rejects every pyramid. It costs one column instead of five plus a disk read.
            // v54 worlds measure city ground instead (SurfaceStructures73), so the noise shortcut no longer holds.
            if (SurfaceStructures73.applies(context)) return false;
            int corner = context.chunkGenerator().getFirstOccupiedHeight(chunk.getMinBlockX(), chunk.getMinBlockZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            return corner < context.chunkGenerator().getSeaLevel();
        }
        return false;
    }
}
