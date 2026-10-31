package com.dorohedoro.hole.worldgen.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Per-world city tuning, stored with the chunk generator in level.dat. Keeping it there instead of in a config file
 * means editing a config can never put a seam between old and new chunks of an existing world.
 *
 * @param density   chance in {@code [0, 1]} that a lot holds a building rather than open ground
 * @param maxFloors tallest residential tower, in storeys
 */
public record CitySettings(float density, int maxFloors) {
    public static final CitySettings DEFAULT = new CitySettings(0.92F, 11);

    public static final Codec<CitySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("density", DEFAULT.density).forGetter(CitySettings::density),
            Codec.intRange(2, 16).optionalFieldOf("max_floors", DEFAULT.maxFloors).forGetter(CitySettings::maxFloors)
    ).apply(instance, CitySettings::new));
}
