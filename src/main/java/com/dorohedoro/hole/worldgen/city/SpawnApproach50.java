package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 50: actual grading checks for the 4-wide landing-to-shelter aisle.
 * Both buildings use their south-frontage grades (not center samples). The
 * alley between them uses the ordinary terrain field. A terrain jump greater
 * than one block can create an inaccessible approach without stairs, so such
 * sites are rejected before any chunks are generated. This is pure geometry;
 * it does not modify old worlds or add artificial bright spawn lighting.
 */
public final class SpawnApproach50 {
    @FunctionalInterface public interface Elevation { int at(int x, int z); }
    private SpawnApproach50() {}

    public static boolean isWalkable(int x, int z, Elevation terrain) {
        // Exact Pass47 parcel geometry: landing x[-13,+13], shelter
        // x[+18,+36], both z[-13,+13]; four-block east-west aisle.
        int landing = terrain.at(x, z + 14);
        int shelter = terrain.at(x + 27, z + 14);
        int last = landing;
        for (int dx = 14; dx <= 17; dx++) {
            int y = terrain.at(x + dx, z);
            if (Math.abs(y - last) > 1) return false;
            last = y;
        }
        if (Math.abs(shelter - last) > 1) return false;
        // The four-wide public lane immediately west of the landing is a
        // north-south egress. Check the step from the *fixed* plaza grade.
        if (Math.abs(terrain.at(x - 14, z) - landing) > 1) return false;
        // Same protection on the outside of the shelter, to reach the east
        // north-south public lane. No special highway or loot is spawned.
        if (Math.abs(terrain.at(x + 37, z) - shelter) > 1) return false;
        return true;
    }
}
