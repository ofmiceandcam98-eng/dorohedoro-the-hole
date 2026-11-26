package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.Dorohedoro;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SmokeAttachments {
    private SmokeAttachments() {}
    private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,Dorohedoro.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<SmokeState>> STATE=TYPES.register("smoke_state",
        ()->AttachmentType.builder(()->SmokeState.EMPTY).serialize(SmokeState.CODEC).copyOnDeath().build());
    public static void register(IEventBus bus){TYPES.register(bus);}
}
