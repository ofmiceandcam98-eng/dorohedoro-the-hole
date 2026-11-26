package com.dorohedoro.hole.faction;

import com.dorohedoro.hole.Dorohedoro;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * How two groups in the Hole regard one player, from -100 to 100, saved with the player and kept through death.
 * <ul>
 *   <li><b>Hole residents</b> (villagers, and any mod's villagers that extend them): starts at 0. Trading raises it a
 *       little each day; defending a village from a raid raises it a lot; killing residents, traders or iron golems
 *       lowers it. Standing moves villager prices (see {@link #priceShift}), and residents who hate a player refuse to
 *       trade at all.</li>
 *   <li><b>Cross-Eyes</b> (the sorcerer gang in the Hole's camps): starts at 0, which they treat as hostile. Paying them
 *       tribute in emeralds raises it; attacking or killing them lowers it. At {@link #CROSS_EYES_TOLERATE} they stop
 *       attacking on sight, and at {@link #CROSS_EYES_TRADE} they will trade.</li>
 * </ul>
 * Nothing here is permanent: every route has a way back, through trade or tribute.
 */
public record FactionStanding(int residents, int crossEyes, long day, int tradesToday, int tributesToday) {
    public static final FactionStanding NEUTRAL = new FactionStanding(0, 0, -1, 0, 0);
    public static final int CROSS_EYES_TOLERATE = 20;
    public static final int CROSS_EYES_TRADE = 40;
    public static final int RESIDENTS_REFUSE = -80;
    /** At most this much standing a day from trading with residents, and from tribute to the Cross-Eyes. */
    public static final int DAILY_TRADE_CAP = 5, DAILY_TRIBUTE_CAP = 4;

    public static final Codec<FactionStanding> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("residents").forGetter(FactionStanding::residents),
            Codec.INT.fieldOf("cross_eyes").forGetter(FactionStanding::crossEyes),
            Codec.LONG.optionalFieldOf("day", -1L).forGetter(FactionStanding::day),
            Codec.INT.optionalFieldOf("trades_today", 0).forGetter(FactionStanding::tradesToday),
            Codec.INT.optionalFieldOf("tributes_today", 0).forGetter(FactionStanding::tributesToday)
    ).apply(i, FactionStanding::new));

    private static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Dorohedoro.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FactionStanding>> STANDING =
            TYPES.register("faction_standing", () -> AttachmentType.builder(() -> NEUTRAL).serialize(CODEC).copyOnDeath().build());

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(FactionStanding::registerPayloads);
        FactionEvents.register();
    }

    public static FactionStanding of(ServerPlayer player) {
        FactionStanding s = player.getData(STANDING);
        long today = player.level().getDayTime() / 24000L;
        return s.day == today ? s : new FactionStanding(s.residents, s.crossEyes, today, 0, 0);
    }

    static void store(ServerPlayer player, FactionStanding standing) {
        player.setData(STANDING, standing);
        sync(player);
    }

    /** Changes Hole resident standing. Returns the applied change after clamping. */
    public static int changeResidents(ServerPlayer player, int delta, String reason) {
        FactionStanding s = of(player);
        int next = clamp(s.residents + delta);
        store(player, new FactionStanding(next, s.crossEyes, s.day, s.tradesToday, s.tributesToday));
        report(player, "residents", s.residents, next, reason);
        return next - s.residents;
    }

    public static int changeCrossEyes(ServerPlayer player, int delta, String reason) {
        FactionStanding s = of(player);
        int next = clamp(s.crossEyes + delta);
        store(player, new FactionStanding(s.residents, next, s.day, s.tradesToday, s.tributesToday));
        report(player, "cross_eyes", s.crossEyes, next, reason);
        return next - s.crossEyes;
    }

    /** One more trade today; returns false once today's trade standing is used up. */
    static boolean countTrade(ServerPlayer player) {
        FactionStanding s = of(player);
        if (s.tradesToday >= DAILY_TRADE_CAP) return false;
        store(player, new FactionStanding(s.residents, s.crossEyes, s.day, s.tradesToday + 1, s.tributesToday));
        return true;
    }

    /** One more tribute today; returns false once today's tributes are used up. */
    public static boolean countTribute(ServerPlayer player) {
        FactionStanding s = of(player);
        if (s.tributesToday >= DAILY_TRIBUTE_CAP) return false;
        store(player, new FactionStanding(s.residents, s.crossEyes, s.day, s.tradesToday, s.tributesToday + 1));
        return true;
    }

    private static int clamp(int v) {
        return Math.max(-100, Math.min(100, v));
    }

    /**
     * Price change for a villager trade, as a fraction of its base cost: discounts for trusted players, markups for
     * disliked ones. Vanilla's own reputation and Hero of the Village discounts still apply on top.
     */
    public static float priceShift(int residents) {
        if (residents >= 60) return -0.20F;
        if (residents >= 30) return -0.10F;
        if (residents <= -60) return 0.40F;
        if (residents <= -30) return 0.20F;
        return 0.0F;
    }

    /** The word for a standing value, as a translation key suffix: hated, disliked, neutral, liked, trusted. */
    public static String band(int value) {
        if (value <= -60) return "hated";
        if (value <= -20) return "disliked";
        if (value < 20) return "neutral";
        if (value < 60) return "liked";
        return "trusted";
    }

    /** Tells the player when a change moves them into a new band, so standing is never a hidden number. */
    private static void report(ServerPlayer player, String faction, int before, int after, String reason) {
        if (band(before).equals(band(after))) return;
        player.displayClientMessage(Component.translatable("message.dorohedoro.standing." + faction,
                Component.translatable("standing.dorohedoro." + band(after))), true);
    }

    // ---- Client sync, for the Status tab.

    public record Sync(int residents, int crossEyes) implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "faction_standing"));
        public static final StreamCodec<ByteBuf, Sync> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Sync::residents, ByteBufCodecs.VAR_INT, Sync::crossEyes, Sync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Last standing the server sent. Client side only. */
    public static int clientResidents, clientCrossEyes;

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("faction_1").playToClient(Sync.TYPE, Sync.CODEC, (payload, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) ctx.enqueueWork(() -> {
                clientResidents = payload.residents();
                clientCrossEyes = payload.crossEyes();
            });
        });
    }

    static void sync(ServerPlayer player) {
        FactionStanding s = player.getData(STANDING);
        PacketDistributor.sendToPlayer(player, new Sync(s.residents, s.crossEyes));
    }
}
