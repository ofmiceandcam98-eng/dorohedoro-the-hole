package com.dorohedoro.hole.worldgen.city;

/**
 * An arterial that runs forever along one axis. Because a line is fully described by its index, roads and canals
 * cannot disagree across a chunk border.
 *
 * @param center    fixed coordinate on the cross axis
 * @param halfWidth blocks from the center to the band's outer edge
 */
public record Line(int index, int center, int halfWidth, Kind kind) {
    public enum Kind {
        AVENUE,
        TRAM,
        CANAL
    }

    public boolean contains(int coordinate) {
        return Math.abs(coordinate - center) <= halfWidth;
    }
}
