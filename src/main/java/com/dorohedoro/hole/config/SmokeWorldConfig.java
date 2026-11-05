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
    public static final ModConfigSpec SPEC = BUILDER.build();
}
