package com.dorohedoro.hole.worldgen.city;

/** Axis-aligned XZ rectangle. All four bounds are inclusive. */
public record Rect(int x0, int z0, int x1, int z1) {
    public int width() {
        return x1 - x0 + 1;
    }

    public int depth() {
        return z1 - z0 + 1;
    }

    public int area() {
        return width() * depth();
    }

    public int centerX() {
        return (x0 + x1) >> 1;
    }

    public int centerZ() {
        return (z0 + z1) >> 1;
    }

    public boolean contains(int x, int z) {
        return x >= x0 && x <= x1 && z >= z0 && z <= z1;
    }

    public boolean intersects(Rect other) {
        return x0 <= other.x1 && x1 >= other.x0 && z0 <= other.z1 && z1 >= other.z0;
    }

    public Rect inset(int by) {
        return new Rect(x0 + by, z0 + by, x1 - by, z1 - by);
    }

    public boolean isValid() {
        return x1 >= x0 && z1 >= z0;
    }
}
