package net.minecraft.world.level.levelgen;
import net.minecraft.util.RandomSource;
/** Fixture PRNG, deliberately not claimed to match Minecraft's Xoroshiro sequences. */
public final class XoroshiroRandomSource implements RandomSource {
    private final java.util.Random inner;
    public XoroshiroRandomSource(long seed) {inner=new java.util.Random(seed);}
    public int nextInt(int b){return inner.nextInt(b);}
    public long nextLong(){return inner.nextLong();}
    public float nextFloat(){return inner.nextFloat();}
    public boolean nextBoolean(){return inner.nextBoolean();}
}
