package com.dorohedoro.hole.worldgen.city;

/**
 * One building plot. A lot's content is a pure function of this record, so each chunk it overlaps can draw its own
 * slice without any knowledge of its neighbours.
 */
public record Lot(Rect rect, LotType type, District district, long hash) {}
