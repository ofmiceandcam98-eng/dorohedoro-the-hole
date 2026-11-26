package com.dorohedoro.hole.worldgen;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.city.District;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** Biome keys. Each district reads as its own biome, and everything below {@link #UNDERCITY} is mining ground. */
public final class HoleBiomes {
    public static final ResourceKey<Biome> RESIDENTIAL = key("hole_residential");
    public static final ResourceKey<Biome> MARKET = key("hole_market");
    public static final ResourceKey<Biome> INDUSTRIAL = key("hole_industrial");
    public static final ResourceKey<Biome> WATERFRONT = key("hole_waterfront");
    public static final ResourceKey<Biome> DERELICT = key("hole_derelict");
    public static final ResourceKey<Biome> GRAVEYARD = key("hole_graveyard");
    public static final ResourceKey<Biome> OUTSKIRTS = key("hole_outskirts");
    public static final ResourceKey<Biome> UNDERCITY = key("hole_undercity");
    public static final ResourceKey<Biome> FROSTBITTEN_INDUSTRIAL = key("hole_frostbitten_industrial");

    // Pass 42: full F3-visible identities for the inhabited outskirts.
    public static final ResourceKey<Biome> SNOWFIELDS = key("hole_snowfields");
    public static final ResourceKey<Biome> DEAD_TAIGA = key("hole_dead_taiga");
    public static final ResourceKey<Biome> DUST_BASIN = key("hole_dust_basin");
    public static final ResourceKey<Biome> IRON_BADLANDS = key("hole_iron_badlands");
    public static final ResourceKey<Biome> MOUNTAIN_RIM = key("hole_mountain_rim");

    private HoleBiomes() {}

    public static ResourceKey<Biome> forDistrict(District district) {
        return switch (district) {
            case RESIDENTIAL -> RESIDENTIAL;
            case MARKET -> MARKET;
            case INDUSTRIAL -> INDUSTRIAL;
            case WATERFRONT -> WATERFRONT;
            case DERELICT -> DERELICT;
            case GRAVEYARD -> GRAVEYARD;
            case OUTSKIRTS -> OUTSKIRTS;
        };
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, name));
    }
}
