package com.dorohedoro.hole.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server/world config: landscape damage is intentionally ON by default.
 * A Sorcerer can destroy their own builds with high-level spells; none of
 * the magic branches knows who owns a structure. SERVER config is synced. */
public final class SmokeWorldConfig {
    private SmokeWorldConfig() {}
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue TERRAIN_DAMAGE = BUILDER
        .comment("Allow mastered Destruction magic to blow up terrain, houses and player buildings.",
                 "Default: true. Accidental self-destruction is intentional gameplay.",
                 "Disable here for peaceful servers. Independent of mobGriefing.")
        .define("destruction.terrainDamage", true);
    public static final ModConfigSpec.IntValue SMOKE_DOOR_MIN_RANK = BUILDER
        .comment("Lowest sorcerer rank that can open a Smoke Door. Weak sorcerers cannot make doors of their own.")
        .defineInRange("smokeDoor.minRank", 5, 1, 50);
    public static final ModConfigSpec.IntValue SMOKE_DOOR_COST = BUILDER
        .comment("Smoke spent to open a Smoke Door.")
        .defineInRange("smokeDoor.smokeCost", 120, 1, 1000);
    public static final ModConfigSpec.IntValue SMOKE_DOOR_SECONDS = BUILDER
        .comment("How long a Smoke Door and its exit stay open, in seconds.")
        .defineInRange("smokeDoor.secondsOpen", 90, 10, 600);
    public static final ModConfigSpec.IntValue SMOKE_DOOR_COOLDOWN = BUILDER
        .comment("Seconds before the same sorcerer can open another Smoke Door.")
        .defineInRange("smokeDoor.cooldownSeconds", 60, 0, 3600);
    public static final ModConfigSpec SPEC = BUILDER.build();
}
