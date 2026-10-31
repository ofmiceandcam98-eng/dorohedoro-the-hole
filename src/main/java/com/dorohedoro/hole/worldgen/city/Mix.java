package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A weighted block palette, sampled by position hash. */
public final class Mix {
    private final BlockState[] states;

    private Mix(BlockState[] states) {
        this.states = states;
    }

    /** Alternating blocks and integer weights, for example {@code Mix.of(Blocks.STONE, 3, Blocks.ANDESITE, 1)}. */
    public static Mix of(Object... blocksAndWeights) {
        List<BlockState> expanded = new ArrayList<>();
        for (int i = 0; i < blocksAndWeights.length; i += 2) {
            Object entry = blocksAndWeights[i];
            BlockState state = entry instanceof Block block ? block.defaultBlockState() : (BlockState) entry;
            int weight = (Integer) blocksAndWeights[i + 1];
            for (int n = 0; n < weight; n++) {
                expanded.add(state);
            }
        }
        return new Mix(expanded.toArray(BlockState[]::new));
    }

    public BlockState pick(long hash) {
        return states[CityHash.range(hash, states.length)];
    }
}
