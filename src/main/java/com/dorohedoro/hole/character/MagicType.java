package com.dorohedoro.hole.character;

import java.util.Locale;

/** One innate magic specialty per Sorcerer. IDs must remain stable in save files. */
public enum MagicType {
    MUSHROOM("Mushroom", 3),
    REPTILE("Reptile", 3),
    DISMEMBERMENT("Dismemberment", 3),
    HEALING("Healing", 4),
    CURSE("Curse", 2),
    PROTECTION("Protection", 4),
    TIME("Time", 1),
    DESTRUCTION("Destruction", 4);

    private final String display;
    private final int fateWeight;
    MagicType(String display, int fateWeight) { this.display = display; this.fateWeight = fateWeight; }
    public String display() { return display; }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public int fateWeight() { return fateWeight; }
    public static MagicType fromId(String id) {
        for (MagicType type : values()) if (type.id().equals(id)) return type;
        return null;
    }
}
