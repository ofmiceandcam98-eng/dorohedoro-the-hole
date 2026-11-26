package com.dorohedoro.hole.hazard;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.magic.SmokeEngine;
import com.dorohedoro.hole.registry.ModBlocks;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Environmental poisoning in the Hole. Each player has an exposure level from 0 to 1000 that builds while they are in
 * contact with the Hole's pollution and falls away when they are not:
 *
 * <ul>
 *   <li><b>Ash-Snow</b> underfoot without boots: +8 a second.</li>
 *   <li><b>Sludge-Ice</b> underfoot without boots: +10 a second.</li>
 *   <li><b>Tainted weather</b> in the Hole, under open sky: ash-laden snowfall, +2 a second (none with a helmet); and, for
 *       sorcerers only, the Hole's rain, +4 a second (+2 with a helmet). Canon: the Hole's rain harms sorcerers, not
 *       ordinary humans.</li>
 *   <li>A hit from an <b>ash snowball</b>: +120 at once.</li>
 * </ul>
 *
 * Out of contact it falls by 6 a second, or 12 under a roof. At 150 the player gets a warning naming the cause. At 300,
 * 600 and 900 the {@link HoleToxinEffect} applies at level I, II or III. For sorcerers it also slows smoke regeneration
 * (see {@link #tier}). An Antidote Poultice clears it, milk halves it, and dying resets it. Creative and spectator players
 * are never exposed.
 */
public final class ToxinExposure {
    public static final int MAX = 1000;
    public static final int WARN = 150;
    private static final int[] TIERS = {300, 600, 900};

    private static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Dorohedoro.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> EXPOSURE =
            TYPES.register("toxin_exposure", () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, MAX)).build());

    private ToxinExposure() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(ToxinExposure::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ToxinExposure::tick);
        NeoForge.EVENT_BUS.addListener(ToxinExposure::effectRemoved);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) sync(p);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) sync(p);
        });
    }

    public static int get(ServerPlayer player) {
        return player.getData(EXPOSURE);
    }

    /** 0 when below the first threshold, else the Hole Toxin level (1 to 3). */
    public static int tier(ServerPlayer player) {
        int value = get(player), tier = 0;
        for (int threshold : TIERS) if (value >= threshold) tier++;
        return tier;
    }

    public static void add(ServerPlayer player, int amount, String cause) {
        if (player.isCreative() || player.isSpectator()) return;
        int before = get(player);
        set(player, Math.min(MAX, before + amount));
        if (before < WARN && get(player) >= WARN) warn(player, cause);
        applyEffect(player);
    }

    /** Antidote Poultice: clears exposure and the effect. */
    public static void clear(ServerPlayer player) {
        set(player, 0);
        player.removeEffect(ModEffects.HOLE_TOXIN);
    }

    private static void set(ServerPlayer player, int value) {
        int before = player.getData(EXPOSURE);
        if (before == value) return;
        player.setData(EXPOSURE, value);
        // The HUD shows whole percent, so only send when that changes.
        if (before / 10 != value / 10 || (before == 0) != (value == 0)) sync(player);
    }

    private static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 7) return;
        if (player.isCreative() || player.isSpectator()) {
            if (get(player) > 0) clear(player);
            return;
        }
        String[] cause = new String[1];
        int gain = gain(player, cause);
        int now = get(player);
        if (gain > 0) {
            set(player, Math.min(MAX, now + gain));
            if (now < WARN && get(player) >= WARN) warn(player, cause[0]);
        } else if (now > 0) {
            set(player, Math.max(0, now - (sheltered(player) ? 12 : 6)));
        }
        applyEffect(player);
    }

    private static void applyEffect(ServerPlayer player) {
        int tier = tier(player);
        if (tier > 0) {
            player.addEffect(new MobEffectInstance(ModEffects.HOLE_TOXIN, 60, tier - 1, false, true, true));
        }
    }

    /** Exposure gained this second, with the main cause for the warning message. */
    private static int gain(ServerPlayer player, String[] cause) {
        ServerLevel level = player.serverLevel();
        int gain = 0;
        if (player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            BlockState feet = level.getBlockState(player.blockPosition());
            BlockState under = level.getBlockState(player.getOnPos());
            if (feet.is(ModBlocks.ASH_SNOW.get()) || under.is(ModBlocks.ASH_SNOW.get()) || under.is(ModBlocks.ASH_SNOW_BLOCK.get())) {
                gain += 8;
                cause[0] = "ash_snow";
            }
            if (under.is(ModBlocks.SLUDGE_ICE.get())) {
                gain += 10;
                cause[0] = "sludge_ice";
            }
        }
        if (level.dimension() == Level.OVERWORLD && level.isRaining()) {
            BlockPos head = BlockPos.containing(player.getEyePosition());
            Holder<Biome> biome = level.getBiome(head);
            boolean hole = biome.unwrapKey().map(k -> k.location().getNamespace().equals(Dorohedoro.MOD_ID)).orElse(false);
            if (hole && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, head).getY() <= head.getY()) {
                boolean helmet = !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                boolean sorcerer = SmokeEngine.innate(player) != null;
                Biome.Precipitation weather = biome.value().getPrecipitationAt(head);
                if (weather == Biome.Precipitation.SNOW && !helmet) {
                    gain += 2;
                    if (cause[0] == null) cause[0] = "snowfall";
                }
                if (weather == Biome.Precipitation.RAIN && sorcerer) {
                    gain += helmet ? 2 : 4;
                    if (cause[0] == null) cause[0] = "rain";
                }
            }
        }
        return gain;
    }

    private static boolean sheltered(ServerPlayer player) {
        return !player.level().canSeeSky(BlockPos.containing(player.getEyePosition()));
    }

    private static void warn(ServerPlayer player, String cause) {
        player.displayClientMessage(Component.translatable("message.dorohedoro.toxin." + (cause == null ? "generic" : cause)), true);
    }

    /** Milk clears the effect but only halves the exposure underneath it. */
    private static void effectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect().is(ModEffects.HOLE_TOXIN) && event.getEntity() instanceof ServerPlayer player
                && event.getCure() == EffectCures.MILK) {
            set(player, get(player) / 2);
        }
    }

    // ---- Client sync: the HUD shows exposure as a percentage chip.

    public record ToxinSync(int exposure) implements CustomPacketPayload {
        public static final Type<ToxinSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "toxin_level"));
        public static final StreamCodec<ByteBuf, ToxinSync> CODEC = ByteBufCodecs.VAR_INT.map(ToxinSync::new, ToxinSync::exposure);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Last exposure the server sent, read by the HUD. Client side only. */
    public static int clientExposure;

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("hazard_1").playToClient(ToxinSync.TYPE, ToxinSync.CODEC, (payload, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) ctx.enqueueWork(() -> clientExposure = payload.exposure());
        });
    }

    private static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ToxinSync(get(player)));
    }
}
