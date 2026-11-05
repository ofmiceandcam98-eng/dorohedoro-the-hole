package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A weighted block palette, sampled by position hash. */
public final class Mix {
    /** Loose ground. A palette that is mostly these is laid in patches, the way vanilla surfaces are. */
    private static final Set<Block> SOIL = Set.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.GRAVEL,
            Blocks.SAND, Blocks.RED_SAND, Blocks.MUD, Blocks.PACKED_MUD, Blocks.PODZOL, Blocks.GRASS_BLOCK,
            Blocks.MOSS_BLOCK, Blocks.MYCELIUM, Blocks.CLAY, Blocks.SNOW_BLOCK, Blocks.DIRT_PATH);

    private final BlockState[] states;
    private final boolean patchy;

    private Mix(BlockState[] states, boolean patchy) {
        this.states = states;
        this.patchy = patchy;
    }

    /** Alternating blocks and integer weights, for example {@code Mix.of(Blocks.STONE, 3, Blocks.ANDESITE, 1)}. */
    public static Mix of(Object... blocksAndWeights) {
        List<BlockState> expanded = new ArrayList<>();
        int soil = 0;
        for (int i = 0; i < blocksAndWeights.length; i += 2) {
            Object entry = blocksAndWeights[i];
            BlockState state = entry instanceof Block block ? block.defaultBlockState() : (BlockState) entry;
            int weight = (Integer) blocksAndWeights[i + 1];
            for (int n = 0; n < weight; n++) {
                expanded.add(state);
            }
            if (SOIL.contains(state.getBlock()) || state.getBlock().getDescriptionId().endsWith("ash_soil")) soil += weight;
        }
        return new Mix(expanded.toArray(BlockState[]::new), soil * 2 > expanded.size());
    }

    /** True for ground palettes, which v53 worlds sample by patch instead of per block. */
    public boolean patchy() {
        return patchy;
    }

    public BlockState pick(long hash) {
        return states[CityHash.range(hash, states.length)];
    }
}
