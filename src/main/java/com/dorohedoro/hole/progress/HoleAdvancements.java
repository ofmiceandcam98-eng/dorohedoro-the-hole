package com.dorohedoro.hole.progress;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.character.CharacterAttachments;
import com.dorohedoro.hole.character.CharacterProfile;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Hidden progress markers for things only the mod's own code can see happen: a door opening, a safe return from Hell,
 * the first technique cast. They are ordinary advancements with an {@code impossible} criterion and no display, so they
 * never show a toast or a tab; they exist so that quest books (FTB Quests advancement tasks) and datapacks can tell
 * what a player has really done. Advancements are saved per player and can only be earned once, so a marker is never
 * counted twice. The files are in {@code data/dorohedoro/advancement/}.
 */
public final class HoleAdvancements {
    public static final String ORIGIN_CHOSEN = "origin/chosen";
    public static final String ORIGIN_HUMAN = "origin/human";
    public static final String ORIGIN_SORCERER = "origin/sorcerer";
    public static final String FIRST_TECHNIQUE = "magic/first_technique";
    public static final String LIMB_REATTACHED = "medicine/limb_reattached";
    public static final String ARTIFICIAL_DOOR_OPENED = "doors/artificial_door_opened";
    public static final String DEVIL_GATE_OPENED = "doors/devil_gate_opened";
    public static final String TRAVELED_BY_DOOR = "doors/traveled_by_door";
    public static final String RETURNED_FROM_HELL = "realms/returned_from_hell";

    private HoleAdvancements() {}

    /** Grants the marker {@code dorohedoro:<path>} to {@code player}. Does nothing if it is already earned. */
    public static void award(ServerPlayer player, String path) {
        AdvancementHolder holder = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, path));
        if (holder == null) {
            Dorohedoro.LOGGER.warn("Missing progress advancement dorohedoro:{}", path);
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    /** Origin markers, also granted on login so players who chose before these markers existed still get credit. */
    public static void awardOrigin(ServerPlayer player) {
        CharacterProfile profile = player.getData(CharacterAttachments.PROFILE);
        if (!profile.selected()) return;
        award(player, ORIGIN_CHOSEN);
        award(player, "sorcerer".equals(profile.origin()) ? ORIGIN_SORCERER : ORIGIN_HUMAN);
    }
}
