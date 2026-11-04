package com.dorohedoro.hole.worldgen.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Per-world city tuning, stored with the chunk generator in level.dat. Keeping it there instead of in a config file
 * means editing a config can never put a seam between old and new chunks of an existing world.
 *
 * @param density   chance in {@code [0, 1]} that a lot holds a building rather than open ground
 * @param maxFloors tallest residential tower, in storeys
 * @param terrainRelief use terraced city elevations; false for previously-created worlds
 * @param sewerUnderworld add sewers and selective cave access while preserving deep Minecraft caverns
 * @param sewerCaveFrequency percentage of intersections with long cave-leading passages; absent in
 *                           existing worlds means the exact original one-in-eleven selector
 * @param worldDetailVersion 52 adds dual-access stormwater bypass galleries, graded fractured cave exits and district-like sewer vault masonry; 51 adds lever-operated sticky-piston overflow sluices to existing pump halls; 50 adds rare connected stormwater pumping galleries with a manual operator booth and protected dry walkways; 49 adds sparse walk-in maintenance and filtration bays; 48 reserves walkable vanilla beaches and shores for new ocean worlds; 47 adds distant true vanilla ocean basins and compatible sea-floor painter; 46 protects real vanilla stronghold/mineshaft/trial-chamber/ancient-city starts from custom city and sewer writes; 45 shields deep-dark habitat; 44 adds dark sewer stretches and optional breakable roach nests for ordinary survival; 43 keeps cargo-door approaches usable and links workshop and hall courts where setbacks permit; 42 adds industrial street-to-workshop service courts and practical unlit wash/repair pockets; 41 adds accessible side-bay industrial halls with attached working rooms and reachable lower roofs; 40 preserves coherent industrial neighborhood land-use across all Hole climates while retaining open-country survival, 39 connects detached industrial workshops to their halls by covered three-wide service passages; 38 adds purposeful attached service wings and accessible stepped rear terraces; 37 adds seed-shaped curved local backstreets while reserving arterial, canal and tram corridors; 36 makes superblock parcel geometry respond to independent neighborhood density, allowing dense buildings in any climate while leaving larger broken plots and wilderness; 35 decouples regional climate from urban density so The Hole can span snow, desert, badlands and other Minecraft terrain without forcing every climate into sparse outskirts; 34 verifies the complete southern pedestrian route to regional shelter entrances; 33 verifies regional landing terrain access and tries other fringe regions before downtown fallback; 32 removes artificial relief troughs between identical climate provinces; 31 adds seed-variable first neighborhoods with regional starter facilities; 30 introduces coherent, district-aware residential skyline clustering; 29 adds functional two-room service courts on a minority of urban empty lots; 28 adds infrastructure-based residential facades and industrial loading roofs; 27 adds inhabited nonuniform market stalls, coherent backstreet repairs, sparse quay jobs and irregular streetlight spacing; 26 adds distinct polluted frontier climates and tram shelters; 25 aligns new building foundations with their street-facing entrances; 24 adds canal-safe macro terrain and skyline variety; 23 adds lived-in room and plain rooftop detailing; 22 adds parcel-contained hills and additional room-purpose furnishings; 21 adds a wider reserved meandering waterway and additional facade detail; 20 adds purposeful lived-in rooms, sparse night lighting and reduced exterior chests; 19 protects large-world fish performance and verifies canal supply foundations; 18 corrects wall-mounted residential fixtures on new saves; 17 adds compact-house roof silhouettes and furnished rooms; 16 adds mixed-source room lighting and procedural lived-in furniture; 15 adds furnished residential lighting and room-use variations; 14 adds size-aware fully furnished housing and residential lighting; 13 adds compact procedural lots and tighter local alleys; 12 adds coordinate-seeded neighborhood fabric, service yards and rooflines; 11 adds river morphology; 10 adds supplementary furnishing; 9 adds more detailed interior furnishing; 0 keeps historic architecture, 1 adds the original detail pass, 2 adds multiscale architectural variation, 3 adds irregular blocks and structural massing, 4 adds varied interior room plans, 5 adds scarce urban vegetation, 6 adds sparse frost and optional cold districts, 7 adds district signature landmarks/materials, 8 introduces lore-grounded dense concrete/brick architecture and coherent street surfaces
 * @param sewerCavernVersion 0 retains original cave geometry for saved worlds, 1 adds connected
 *                           walkable chamber entrances and broader cavern-size variation; 2 adds separate sewer dungeons; 3 adds riverbank and submerged drain entrances; 4 adds canal ecology and municipal sewer detailing; 5 adds meandering canal-river channels
 */
public record CitySettings(float density, int maxFloors, boolean terrainRelief, boolean sewerUnderworld, int sewerCaveFrequency, int sewerCavernVersion, int worldDetailVersion) {
    // Existing worlds did not encode the terrain flag. They remain at their original
    // elevation to avoid seams where previously generated chunks meet new chunks.
    public static final CitySettings DEFAULT = new CitySettings(0.92F, 11, false, false, SewerBranchPolicy.LEGACY_CAVE_FREQUENCY, 0, 0);

    public CitySettings {
        if (worldDetailVersion < 0 || worldDetailVersion > 52) {
            throw new IllegalArgumentException("worldDetailVersion must be 0..52");
        }
        if (sewerCavernVersion < 0 || sewerCavernVersion > 5) {
            throw new IllegalArgumentException("sewerCavernVersion must be 0..5");
        }
        if (sewerCaveFrequency < 0 || sewerCaveFrequency > 40) {
            throw new IllegalArgumentException("sewerCaveFrequency must be 0..40");
        }
    }

    public static final Codec<CitySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("density", DEFAULT.density).forGetter(CitySettings::density),
            Codec.intRange(2, 16).optionalFieldOf("max_floors", DEFAULT.maxFloors).forGetter(CitySettings::maxFloors),
            Codec.BOOL.optionalFieldOf("terrain_relief", false).forGetter(CitySettings::terrainRelief),
            Codec.BOOL.optionalFieldOf("sewer_underworld", false).forGetter(CitySettings::sewerUnderworld),
            Codec.intRange(0, 40).optionalFieldOf("sewer_cave_frequency", SewerBranchPolicy.LEGACY_CAVE_FREQUENCY)
                    .forGetter(CitySettings::sewerCaveFrequency),
            Codec.intRange(0, 5).optionalFieldOf("sewer_cavern_version", 0)
                    .forGetter(CitySettings::sewerCavernVersion),
            Codec.intRange(0, 52).optionalFieldOf("world_detail_version", 0)
                    .forGetter(CitySettings::worldDetailVersion)
    ).apply(instance, CitySettings::new));
}
