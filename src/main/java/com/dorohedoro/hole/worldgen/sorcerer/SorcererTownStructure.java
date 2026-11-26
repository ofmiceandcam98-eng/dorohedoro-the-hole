package com.dorohedoro.hole.worldgen.sorcerer;

import com.dorohedoro.hole.worldgen.RealmBiomes;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * A Sorcerers' World settlement: a city in the city basin, a town on its edge, a hamlet out in the farmland and
 * woods. The structure set spaces them hundreds of blocks apart, so the land between stays countryside.
 */
public final class SorcererTownStructure extends Structure {
    public static final MapCodec<SorcererTownStructure> CODEC = simpleCodec(SorcererTownStructure::new);

    public SorcererTownStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return SorcererStructures.SETTLEMENT.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Sorcerers' World only. The biome tag lists vanilla biomes, so the dimension is checked by its biome source:
        // a vanilla overworld, the Hole and Hell can never get a settlement.
        if (!(context.biomeSource() instanceof com.dorohedoro.hole.worldgen.SorcererVanillaBiomeSource)
                && !(context.biomeSource() instanceof com.dorohedoro.hole.worldgen.SorcererBiomeSource)) {
            return Optional.empty();
        }
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        Terrain terrain = new Terrain(context, x, z);
        if (terrain.water(x, z)) return Optional.empty();
        int y = terrain.height(x, z);
        if (y <= context.chunkGenerator().getSeaLevel() + 1) return Optional.empty();
        // The centre must be fairly level: the plaza sits there.
        int min = y, max = y;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int sx = x + (int) Math.round(Math.cos(a) * 20), sz = z + (int) Math.round(Math.sin(a) * 20);
            if (terrain.water(sx, sz)) return Optional.empty();
            int h = terrain.height(sx, sz);
            min = Math.min(min, h);
            max = Math.max(max, h);
        }
        if (max - min > 12) return Optional.empty();

        long seed = context.random().nextLong();
        TownPlanner.Kind wanted = kindAt(context, x, y, z, seed);
        // Plan now, so a site too rough or wet for any settlement simply stays countryside.
        TownPlanner.Layout layout = TownPlanner.planViable(seed, x, z, wanted, terrain);
        if (layout == null) return Optional.empty();
        return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder -> {
            builder.addPiece(new TownRoadsPiece(layout, seed));
            for (TownPlanner.Plot plot : layout.plots()) builder.addPiece(new TownBuildingPiece(plot, layout.kind()));
        }));
    }

    private static TownPlanner.Kind kindAt(GenerationContext context, int x, int y, int z, long seed) {
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), context.randomState().sampler());
        if (biome.is(RealmBiomes.SW_CITY)) return TownPlanner.Kind.CITY;
        if (biome.is(RealmBiomes.SW_CITY_EDGE)) return TownPlanner.Kind.TOWN;
        double roll = new TownRandom(seed ^ 0x70C4L).nextDouble();
        // Open grassland carries the cities; woods and hills towns and hamlets; snowfields only hamlets.
        if (biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(Biomes.MEADOW) || biome.is(Biomes.SAVANNA)) {
            return roll < 0.4 ? TownPlanner.Kind.CITY : roll < 0.8 ? TownPlanner.Kind.TOWN : TownPlanner.Kind.HAMLET;
        }
        if (biome.is(Biomes.SNOWY_PLAINS)) return TownPlanner.Kind.HAMLET;
        return roll < 0.35 ? TownPlanner.Kind.TOWN : TownPlanner.Kind.HAMLET;
    }

    /**
     * Natural terrain read straight from the noise generator, sampled every 4 blocks and interpolated, cached for the
     * whole plan. Independent of chunk load order, so a settlement plans identically every time.
     */
    static final class Terrain implements TownPlanner.Terrain {
        private static final int STEP = 4, SPAN = 140;
        private final GenerationContext context;
        private final int ox, oz, n;
        private final int[] heights;
        private final byte[] wet;

        Terrain(GenerationContext context, int cx, int cz) {
            this.context = context;
            this.ox = cx - SPAN;
            this.oz = cz - SPAN;
            this.n = SPAN * 2 / STEP + 2;
            this.heights = new int[n * n];
            this.wet = new byte[n * n];
            Arrays.fill(heights, Integer.MIN_VALUE);
        }

        private int index(int gx, int gz) {
            gx = Math.max(0, Math.min(n - 1, gx));
            gz = Math.max(0, Math.min(n - 1, gz));
            int i = gz * n + gx;
            if (heights[i] == Integer.MIN_VALUE) sample(i, ox + gx * STEP, oz + gz * STEP);
            return i;
        }

        private void sample(int i, int x, int z) {
            NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
            int top = context.heightAccessor().getMaxBuildHeight() - 1, bottom = context.heightAccessor().getMinBuildHeight();
            for (int y = top; y >= bottom; y--) {
                BlockState state = column.getBlock(y);
                if (state.isAir()) continue;
                heights[i] = y + 1;
                wet[i] = (byte) (state.getFluidState().isEmpty() ? 0 : 1);
                return;
            }
            heights[i] = bottom;
            wet[i] = 0;
        }

        @Override
        public int height(int x, int z) {
            double fx = (x - ox) / (double) STEP, fz = (z - oz) / (double) STEP;
            int gx = (int) Math.floor(fx), gz = (int) Math.floor(fz);
            double tx = fx - gx, tz = fz - gz;
            double h00 = heights[index(gx, gz)], h10 = heights[index(gx + 1, gz)];
            double h01 = heights[index(gx, gz + 1)], h11 = heights[index(gx + 1, gz + 1)];
            double h = (h00 * (1 - tx) + h10 * tx) * (1 - tz) + (h01 * (1 - tx) + h11 * tx) * tz;
            return (int) Math.floor(h + 0.5);
        }

        @Override
        public boolean water(int x, int z) {
            int gx = (int) Math.floor((x - ox) / (double) STEP + 0.5), gz = (int) Math.floor((z - oz) / (double) STEP + 0.5);
            return wet[index(gx, gz)] != 0;
        }
    }
}
