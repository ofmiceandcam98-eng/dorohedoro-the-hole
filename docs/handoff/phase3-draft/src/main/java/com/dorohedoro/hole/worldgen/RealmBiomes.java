package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Dimension and biome keys for the two realms beyond the Hole. */
public final class RealmBiomes {
    public static final ResourceKey<Level> SORCERERS_WORLD = ResourceKey.create(Registries.DIMENSION, id("sorcerers_world"));
    public static final ResourceKey<Level> HELL = ResourceKey.create(Registries.DIMENSION, id("hell"));

    public static final ResourceKey<Biome> SW_CITY = biome("sw_city");
    public static final ResourceKey<Biome> SW_CITY_EDGE = biome("sw_city_edge");
    public static final ResourceKey<Biome> SW_FARMLAND = biome("sw_farmland");
    public static final ResourceKey<Biome> SW_WOODLAND = biome("sw_woodland");
    public static final ResourceKey<Biome> SW_HYDRA_FOREST = biome("sw_hydra_forest");
    public static final ResourceKey<Biome> SW_BLIGHT = biome("sw_blight");
    public static final ResourceKey<Biome> SW_WATERS = biome("sw_waters");
    public static final ResourceKey<Biome> SW_CAVES = biome("sw_caves");

    public static final ResourceKey<Biome> HELL_WASTES = biome("hell_wastes");
    public static final ResourceKey<Biome> HELL_HORNS = biome("hell_horns");
    public static final ResourceKey<Biome> HELL_SOUL_FLATS = biome("hell_soul_flats");
    public static final ResourceKey<Biome> HELL_CAVERNS = biome("hell_caverns");

    private RealmBiomes() {}

    private static ResourceKey<Biome> biome(String name) {
        return ResourceKey.create(Registries.BIOME, id(name));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name);
    }
}
