package com.dorohedoro.hole.character;

import com.dorohedoro.hole.Dorohedoro;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Automatically persists and copies the player's origin and magic on death. */
public final class CharacterAttachments {
    private CharacterAttachments() {}
    private static final DeferredRegister<AttachmentType<?>> TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Dorohedoro.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CharacterProfile>> PROFILE =
        TYPES.register("character_profile", () -> AttachmentType.builder(() -> CharacterProfile.UNSELECTED)
            .serialize(CharacterProfile.CODEC).copyOnDeath().build());

    public static void register(IEventBus bus) { TYPES.register(bus); }
}
