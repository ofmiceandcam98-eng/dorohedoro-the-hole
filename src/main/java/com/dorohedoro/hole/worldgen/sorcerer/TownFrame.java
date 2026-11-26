package com.dorohedoro.hole.worldgen.sorcerer;

/**
 * Local-to-world transform for a building footprint. Buildings are authored facing north: local x runs east, local z
 * runs from the front wall (z = 0, door side) to the back, and the street lies at z = -1. {@code facing} is the world
 * direction the door looks out of: 0 north, 1 east, 2 south, 3 west. The matching block-state rotation is a clockwise
 * turn by {@code facing} quarter turns, which is what Minecraft's {@code Rotation} values encode.
 *
 * Pure arithmetic, no Minecraft types, so the planner and the offline fixture share it with the painter.
 */
public record TownFrame(int minX, int minZ, int w, int d, int facing) {
    public static final int[] DX = {0, 1, 0, -1};
    public static final int[] DZ = {-1, 0, 1, 0};

    /** World size of the footprint along x. */
    public int sizeX() {
        return (facing & 1) == 0 ? w : d;
    }

    /** World size of the footprint along z. */
    public int sizeZ() {
        return (facing & 1) == 0 ? d : w;
    }

    public int maxX() {
        return minX + sizeX() - 1;
    }

    public int maxZ() {
        return minZ + sizeZ() - 1;
    }

    public int worldX(int x, int z) {
        return switch (facing) {
            case 0 -> minX + x;
            case 1 -> minX + (d - 1 - z);
            case 2 -> minX + (w - 1 - x);
            default -> minX + z;
        };
    }

    public int worldZ(int x, int z) {
        return switch (facing) {
            case 0 -> minZ + z;
            case 1 -> minZ + x;
            case 2 -> minZ + (d - 1 - z);
            default -> minZ + (w - 1 - x);
        };
    }

    /** The frame whose local cell (doorX, 0) lands on world cell (dx, dz). */
    public static TownFrame fromDoor(int dx, int dz, int w, int d, int doorX, int facing) {
        return switch (facing) {
            case 0 -> new TownFrame(dx - doorX, dz, w, d, 0);
            case 1 -> new TownFrame(dx - d + 1, dz - doorX, w, d, 1);
            case 2 -> new TownFrame(dx - (w - 1 - doorX), dz - d + 1, w, d, 2);
            default -> new TownFrame(dx, dz - (w - 1 - doorX), w, d, 3);
        };
    }
}
