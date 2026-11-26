package com.dorohedoro.hole.worldgen.city;

/**
 * New-world (v34+) pedestrian grade guard for the regional starting quarter.
 *
 * The shelter opens SOUTH, not onto the four-block east-west service alley.
 * Pass 50 checked the alley, but that did not follow the route a player needs
 * from the plaza to the shelter door. The new check traces the *actual south
 * frontage*: plaza south exit (x,z+14) through an open public lane to the
 * shelter's south-facing front step (x+27,z+14).
 *
 * This rejects grade discontinuities, not every possible worldgen obstruction;
 * a live Minecraft collision/pathing check is still required. Do not use on
 * older saved worlds: rejecting a site can change the selected world spawn.
 */
public final class SpawnRoute51 {
    private SpawnRoute51() {}

    /** Pure, deterministic absolute-coordinate height test (no world reads). */
    public static boolean accessible(int x, int z, SpawnApproach50.Elevation elevation) {
        final int frontageZ = z + 14;
        final int landing = elevation.at(x, frontageZ);
        final int shelter = elevation.at(x + 27, frontageZ);
        // South-facing entrances join a continuous public lane. Examine ALL
        // columns, including the previously untested x+18..+26 doorway approach.
        int previous = landing;
        for (int dx = 1; dx <= 27; dx++) {
            int current = elevation.at(x + dx, frontageZ);
            if (Math.abs(current - previous) > 1) return false;
            previous = current;
        }
        if (previous != shelter) return false;
        // The plaza/shelter lots have level foundations fixed to their south
        // frontages. Check where these flat platforms meet the public lanes.
        if (Math.abs(elevation.at(x - 14, frontageZ) - landing) > 1) return false;
        if (Math.abs(elevation.at(x + 37, frontageZ) - shelter) > 1) return false;
        // At each entrance, preserve a short two-wide turn to the outside lane
        // (there are no generated lots in the z+14..+17 service corridor).
        for (int dx : new int[]{0, 27}) {
            int last = elevation.at(x + dx, frontageZ);
            for (int dz = 15; dz <= 17; dz++) {
                int next = elevation.at(x + dx, z + dz);
                if (Math.abs(next - last) > 1) return false;
                last = next;
            }
        }
        return true;
    }
}
