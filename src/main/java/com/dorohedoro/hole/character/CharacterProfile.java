package com.dorohedoro.hole.character;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Immutable, server-owned character identity; automatically serialized with the player. */
public record CharacterProfile(String origin, String magic, String quizAnswers,
                               String selectionMode, boolean smokeIdentified, int level) {
    public static final CharacterProfile UNSELECTED = new CharacterProfile("none", "none", "", "none", false, 1);
    public static final Codec<CharacterProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("origin").forGetter(CharacterProfile::origin),
        Codec.STRING.fieldOf("magic").forGetter(CharacterProfile::magic),
        Codec.STRING.fieldOf("quiz_answers").forGetter(CharacterProfile::quizAnswers),
        Codec.STRING.fieldOf("selection_mode").forGetter(CharacterProfile::selectionMode),
        Codec.BOOL.fieldOf("smoke_identified").forGetter(CharacterProfile::smokeIdentified),
        Codec.INT.fieldOf("level").forGetter(CharacterProfile::level)
    ).apply(i, CharacterProfile::new));

    public boolean selected() { return !origin.equals("none"); }
}
