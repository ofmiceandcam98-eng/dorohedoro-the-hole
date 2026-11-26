package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

/**
 * Pass 56: a usable, roofed three-wide service connection from the detached
 * industrial workshop into its owning warehouse or factory. The connection is
 * fully inside the industrial parcel, is unlit for nighttime mob spawning,
 * and contains no reward chest. No city streets, other parcels or sewer
 * excavations are altered. Coordinates are purely parcel-derived, so clipped
 * chunk writes produce the same geometry as a monolithic paint.
 */
final class LinkedServiceYards56 {
    private LinkedServiceYards56() {}

    /** The legacy workshop location is preserved for older saves. */
    static Rect shack(Rect yard, Rect main, boolean linked) {
        return new Rect(yard.x0() + 1, yard.z0() + 4,
                main.x0() - (linked ? 4 : 2), yard.z0() + 12);
    }

    /** Three-wide horizontal connection, including a sheltered 3x3 crossing. */
    static Rect passage(Rect yard, Rect shack, Rect main) {
        if (shack.width() < 5 || shack.depth() < 8) return null;
        if (main.x0() - shack.x1() - 1 < 3) return null;
        int z = shack.centerZ();
        Rect passage = new Rect(shack.x1() + 1, z - 1, main.x0() - 1, z + 1);
        if (!passage.isValid() || passage.width() < 3 || passage.depth() != 3) return null;
        if (!yard.contains(passage.x0(), passage.z0())
                || !yard.contains(passage.x1(), passage.z1())) return null;
        // The workshop and hall share the same setback; this access must cross
        // neither the frontal main door nor the north service wall.
        if (z <= main.z0() + 2 || z >= main.z1() - 2) return null;
        return passage;
    }

    static void connect(Canvas c, Lot lot, Rect shack, Rect main) {
        if (!c.hasLinkedServiceYards56()) return;
        Rect crossing = passage(lot.rect(), shack, main);
        if (crossing == null) return;
        int z = crossing.centerZ();
        // The sheltered walk has no obstacles at head height and no artificial
        // light, preserving hostile-spawn opportunities in surrounding alleys.
        c.fill(crossing, STREET_Y, STREET_Y, Blocks.POLISHED_ANDESITE);
        c.clear(crossing, GROUND_Y, GROUND_Y + 3);
        c.fill(crossing, GROUND_Y + 4, GROUND_Y + 4, Blocks.WAXED_WEATHERED_CUT_COPPER);
        // Existing workstations and stacked cargo are only removed from the
        // two-block vestibule leading directly to the factory's west wall.
        c.clear(new Rect(shack.x1() - 1, z, shack.x1() - 1, z), GROUND_Y, GROUND_Y + 2);
        c.clear(new Rect(main.x0() + 1, z, main.x0() + 2, z), GROUND_Y, GROUND_Y + 2);
        c.door(shack.x1(), GROUND_Y, z, Direction.EAST, Blocks.SPRUCE_DOOR);
        c.door(main.x0(), GROUND_Y, z, Direction.WEST, Blocks.SPRUCE_DOOR);
    }
}
