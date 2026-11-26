package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.Dorohedoro;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Server-only, one-way instructions for cosmetic mesh effects.
 * Damage and block mutations are handled independently by SmokeEngine. */
public final class SmokeMeshNetwork {
    private SmokeMeshNetwork() {}

    public record Cue(String data) implements CustomPacketPayload {
        public static final Type<Cue> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "smoke_mesh_cue"));
        public static final StreamCodec<ByteBuf, Cue> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Cue::data, Cue::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record LimbState(int entityId, int missingMask) implements CustomPacketPayload {
        public static final Type<LimbState> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "severed_limbs"));
        public static final StreamCodec<ByteBuf, LimbState> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LimbState::entityId,
            ByteBufCodecs.VAR_INT, LimbState::missingMask, LimbState::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** One-shot sever/regrowth cue. Synced mask remains the only authority for
     * body state; these packets cannot be sent from client to server. */
    public record LimbEffect(int entityId, int limb, int kind, int dropEntityId) implements CustomPacketPayload {
        public static final Type<LimbEffect> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "limb_animation"));
        public static final StreamCodec<ByteBuf, LimbEffect> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LimbEffect::entityId,
            ByteBufCodecs.VAR_INT, LimbEffect::limb,
            ByteBufCodecs.VAR_INT, LimbEffect::kind,
            ByteBufCodecs.VAR_INT, LimbEffect::dropEntityId, LimbEffect::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Drop ID is cosmetic, but must match the real ItemEntity spawned by the server.
     * Regrowth never has a dropped item, hence -1. */
    public static void limbEffect(ServerPlayer target, int limb, boolean regrowth) {
        limbEffect(target, limb, regrowth, -1);
    }
    public static void limbEffect(ServerPlayer target, int limb, boolean regrowth, int dropEntityId) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
            new LimbEffect(target.getId(), limb, regrowth ? 1 : 0,
                regrowth ? -1 : dropEntityId));
    }

    public static void syncLimbs(ServerPlayer target) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
            new LimbState(target.getId(), OtherMagicMechanics.missingLimbs(target)));
    }

    public static void sendLimbsTo(ServerPlayer watcher, ServerPlayer target) {
        PacketDistributor.sendToPlayer(watcher,
            new LimbState(target.getId(), OtherMagicMechanics.missingLimbs(target)));
    }

    public static void register(IEventBus bus) { bus.addListener(SmokeMeshNetwork::payloads); }
    private static void payloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("smoke_mesh_2");
        reg.playToClient(LimbEffect.TYPE, LimbEffect.CODEC, (packet, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT)
                ctx.enqueueWork(() -> com.dorohedoro.hole.client.magic.LimbAnimationClient.receive(
                    packet.entityId(), packet.limb(), packet.kind(), packet.dropEntityId()));
        });
        reg.playToClient(LimbState.TYPE, LimbState.CODEC, (packet, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT)
                ctx.enqueueWork(() -> com.dorohedoro.hole.client.magic.SeveredLimbClient.receive(
                    packet.entityId(), packet.missingMask()));
        });
        reg.playToClient(Cue.TYPE, Cue.CODEC, (packet, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT)
                ctx.enqueueWork(() -> com.dorohedoro.hole.client.magic.SmokeMeshClient.receive(packet.data()));
        });
    }

    public static void broadcast(ServerLevel level, ServerPlayer caster, LivingEntity recipient,
                                 SmokeSkill skill, Vec3 impact, boolean focused, int mastery) {
        // All eight biological smoke families now have a distinct visual.
        // This packet never authorizes a gameplay cast or terrain mutation.
        switch (skill.magic()) {
            case MUSHROOM, REPTILE, DISMEMBERMENT, HEALING, CURSE, PROTECTION, TIME, DESTRUCTION -> { }
            default -> { return; }
        }
        if (impact == null || !Double.isFinite(impact.x) || !Double.isFinite(impact.y)
            || !Double.isFinite(impact.z)) return;
        int entityId = recipient == null ? -1 : recipient.getId();
        String data = caster.getId() + "," + entityId + "," + impact.x + ","
            + impact.y + "," + impact.z + "," + Math.clamp(mastery, 0, 1000)
            + "," + skill.id() + "," + (focused ? "1" : "0");
        Cue cue = new Cue(data);
        // Viewers see the SAME animation. No client can request a free cast.
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(impact) <= 96 * 96
                || viewer.distanceToSqr(caster) <= 96 * 96) {
                PacketDistributor.sendToPlayer(viewer, cue);
            }
        }
    }

    /** The same physical healing animation must appear on every recipient in a group. */
    public static void broadcastAid(ServerLevel level, ServerPlayer caster,
                                    LivingEntity patient, SmokeSkill skill) {
        if (skill.magic() == com.dorohedoro.hole.character.MagicType.HEALING)
            broadcast(level, caster, patient, skill,
                patient.position().add(0, patient.getBbHeight() * .60, 0), false, 0);
    }
}
