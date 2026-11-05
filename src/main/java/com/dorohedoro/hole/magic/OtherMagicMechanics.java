package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.config.SmokeWorldConfig;
import com.dorohedoro.hole.character.MagicType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Pass 20.2: mechanical manifestations for non-Mushroom innate magics.
 * No copied animations or imported copyrighted assets. ALL state changes run
 * on the logical server. A visual is not a substitute for the actual effect.
 *
 * Reptile: actual wall-climb motion and size changes for high-level forms.
 * Dismemberment: dedicated sever-injury records, with arm/leg/body penalties;
 *   high-mastery cutting attacks can physically remove a player's arm or leg,
 *   which has server-owned penalties and a synchronized missing-limb silhouette.
 *   Only advanced Healing can regenerate missing limbs.
 * Curse: timed debilitation and hostile hexes.
 * Healing: restores actual health/cleanses other players (SmokeEngine).
 * Protection: temporary collision-enabled black-smoke blocks with saved expiry.
 * Time: a real local enemy-slowing field (not literal time travel).
 * Destruction: authentic block-destroying explosions with PvP attribution,
 *   including player homes. Configurable by server owner, enabled by default.
 */
public final class OtherMagicMechanics {
    private OtherMagicMechanics() {}
    private static final String CLIMB_UNTIL = "dorohedoro_climb_until";
    private static final String FORM_UNTIL = "dorohedoro_apex_until";
    private static final String FORM_AMOUNT = "dorohedoro_apex_size";
    private static final String CURSE_UNTIL = "dorohedoro_curse_until";
    private static final String CURSE_POWER = "dorohedoro_curse_power";
    private static final String CURSE_CASTER = "dorohedoro_curse_caster";
    private static final String SEVER_UNTIL = "dorohedoro_sever_until";
    private static final String SEVER_POWER = "dorohedoro_sever_power";
    private static final String SEVER_ZONE = "dorohedoro_sever_zone";
    private static final String SEVER_MODERN = "dorohedoro_sever_v2";
    private static final String MISSING_LIMBS = "dorohedoro_missing_limbs";
    // A newly removed limb carries its own surgery token; old trophies cannot
    // reattach a different, later injury of the same person and body part.
    private static final String LIMB_TOKEN_PREFIX = "dorohedoro_limb_surgery_token_";
    private static String tokenKey(int limb) { return LIMB_TOKEN_PREFIX + limb; }
    public static String activeLimbToken(ServerPlayer player, int limb) {
        return player.getPersistentData().getString(tokenKey(limb));
    }
    // Server-owned Reptile biological regrowth. Progress is saved on the player,
    // not tied to an individual visual animation or to a client clock.
    private static final String REPTILE_REGROW_PROGRESS = "dorohedoro_reptile_regrow_progress";
    private static final String REPTILE_REGEN_BOOST_UNTIL = "dorohedoro_reptile_regen_boost_until";
    public static final int LEFT_ARM = 1, RIGHT_ARM = 2, LEFT_LEG = 4, RIGHT_LEG = 8;
    private static final int ALL_LIMBS = LEFT_ARM | RIGHT_ARM | LEFT_LEG | RIGHT_LEG;
    private static final ResourceLocation SEVER_LEG_SPEED = ResourceLocation.fromNamespaceAndPath("dorohedoro", "sever_leg_speed");
    private static final ResourceLocation SEVER_ARM_ATTACK = ResourceLocation.fromNamespaceAndPath("dorohedoro", "sever_arm_attack");
    private static final ResourceLocation SEVER_ARM_SWING = ResourceLocation.fromNamespaceAndPath("dorohedoro", "sever_arm_swing");
    private static final ResourceLocation APEX_SCALE = ResourceLocation.fromNamespaceAndPath("dorohedoro", "reptile_apex_scale");
    private static final int MAX_BARRIER_BLOCKS = 1200;
    private static final int MAX_FIELDS = 32;
    private static final ArrayDeque<BarrierStep> BARRIER_STEPS = new ArrayDeque<>();
    private static final List<TimeField> FIELDS = new ArrayList<>();
    private static final DustParticleOptions SOOT = new DustParticleOptions(new Vector3f(.06f,.065f,.07f),1.3f);
    private record BarrierStep(ServerLevel level, UUID caster, BlockPos pos, long start, int delay, long expiry) {}
    private record TimeField(ServerLevel level, Vec3 center, UUID owner, double radius, long expires, int potency) {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(OtherMagicMechanics::entityTick);
        NeoForge.EVENT_BUS.addListener(OtherMagicMechanics::serverTick);
        NeoForge.EVENT_BUS.addListener(OtherMagicMechanics::blockBroken);
        NeoForge.EVENT_BUS.addListener(OtherMagicMechanics::startTracking);
        NeoForge.EVENT_BUS.addListener(OtherMagicMechanics::clonePlayer);
    }

    /** Mastery changes how long genuine reptilian locomotion remains usable. */
    public static void beginClimb(ServerPlayer caster, int mastery, boolean focused) {
        long ticks = 100 + Math.min(450, mastery) + (focused ? 100 : 0);
        caster.getPersistentData().putLong(CLIMB_UNTIL, caster.level().getGameTime() + ticks);
    }

    /** Vanilla player SCALE attribute changes the model AND physical dimensions.
     * In a cramped building, the size upgrade is skipped for safety. */
    public static void beginApex(ServerPlayer caster, int mastery, boolean focused) {
        long duration = 120 + Math.min(380, mastery / 2) + (focused ? 120 : 0);
        caster.getPersistentData().putLong(FORM_UNTIL, caster.level().getGameTime() + duration);
        double size = mastery >= 600 ? 0.27 : mastery >= 250 ? 0.16 : 0.09;
        caster.getPersistentData().putDouble(FORM_AMOUNT, size);
        refreshScale(caster);
    }

    private static void refreshScale(ServerPlayer caster) {
        AttributeInstance attribute = caster.getAttribute(Attributes.SCALE);
        if (attribute == null) return;
        long now = caster.level().getGameTime();
        boolean active = now < caster.getPersistentData().getLong(FORM_UNTIL);
        AttributeModifier existing = attribute.getModifier(APEX_SCALE);
        if (!active) {
            if (existing != null) attribute.removeModifier(APEX_SCALE);
        } else if (existing == null) {
            // Never push a player's bounding box through a wall to enlarge them.
            if (caster.level().noCollision(caster, caster.getBoundingBox().inflate(.24, .24, .24))) {
                attribute.addTransientModifier(new AttributeModifier(APEX_SCALE,
                    caster.getPersistentData().getDouble(FORM_AMOUNT), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    /**
     * A separate, server-owned physical injury rather than ordinary Weakness
     * and Slowness potion effects. Each technique wounds a different region.
     * The most powerful mastered strikes leave injuries until a Healing caster
     * treats them; ordinary strikes recover naturally.
     */
    public static void sever(LivingEntity target, int mastery, int rank, SmokeSkill skill) {
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        long now = level.getGameTime();
        int duration = Math.min(3600, 180 + mastery * 2 + rank * 10);
        int power = mastery >= 700 ? 3 : mastery >= 350 ? 2 : 1;
        String zone = switch (skill) {
            case CLEAN_CUT, LIMB_LOCK, SEVERING_WAVE -> "leg";
            case ANATOMY_REND -> "body";
            default -> "arm";
        };
        // Only the two most devastating techniques can create unhealing wounds.
        // Basic touch, debuff and area techniques always recover on their own.
        boolean lasting = (skill == SmokeSkill.ANATOMY_REND && mastery >= 650)
            || (skill == SmokeSkill.DISMEMBER_STRIKE && mastery >= 900);
        CompoundTag data = target.getPersistentData();
        boolean previousActive = hasActiveDismemberment(target);
        int previousPower = previousActive ? Math.max(1, data.getInt(SEVER_POWER)) : 0;
        long previousExpiry = previousActive ? data.getLong(SEVER_UNTIL) : 0L;
        long expiry = lasting || previousExpiry == Long.MAX_VALUE
            ? Long.MAX_VALUE : Math.max(previousExpiry, now + duration);
        data.putLong(SEVER_UNTIL, expiry);
        if (!previousActive || power >= previousPower) {
            data.putInt(SEVER_POWER, power);
            data.putString(SEVER_ZONE, zone);
        }
        data.putBoolean(SEVER_MODERN, true);
        // These rolls are a supplement to direct health damage, not a substitute.
        // The strongest cuts can take an arm or leg, but no cast removes more
        // than one limb, and a type is never duplicated once lost.
        attemptLimbLoss(target, mastery, skill, zone);
        refreshInjuryModifiers(target);
        injuryMark(level, target);
        if (target instanceof ServerPlayer player) SmokeNetwork.sync(player);
        if (target instanceof ServerPlayer player) {
            String label = data.getString(SEVER_ZONE);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Dismemberment injury (" + label + "): "
                + (expiry == Long.MAX_VALUE ? "requires Healing treatment" : "will recover with time")), true);
        }
    }

    /** Persistent biological damage is independent of temporary wound timers. */
    public static int missingLimbs(LivingEntity target) {
        return target.getPersistentData().getInt(MISSING_LIMBS) & ALL_LIMBS;
    }
    public static boolean hasActiveDismemberment(LivingEntity patient) {
        return missingLimbs(patient) != 0
            || patient.getPersistentData().getLong(SEVER_UNTIL) > patient.level().getGameTime();
    }

    /** Selected high-tier techniques can remove exactly one available limb.
     * All randomization is performed by the logical server after a valid cast.
     * Probabilities are mod balance values, not purported canonical statistics. */
    private static void attemptLimbLoss(LivingEntity target, int mastery, SmokeSkill skill, String zone) {
        if (!(target instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) return;
        double chance = switch (skill) {
            case CLEAN_CUT -> mastery >= 350 ? 0.015 + (mastery - 350) * 0.00007 : 0;
            case DISMEMBER_STRIKE -> mastery >= 400 ? 0.05 + (mastery - 400) * 0.00016 : 0;
            case SEVERING_WAVE -> mastery >= 550 ? 0.015 + (mastery - 550) * 0.00009 : 0;
            case ANATOMY_REND -> mastery >= 600 ? 0.10 + (mastery - 600) * 0.00030 : 0;
            default -> 0;
        };
        if (chance <= 0 || player.getRandom().nextDouble() >= chance) return;
        int eligible = switch (zone) {
            case "leg" -> LEFT_LEG | RIGHT_LEG;
            case "arm" -> LEFT_ARM | RIGHT_ARM;
            default -> ALL_LIMBS;
        };
        int free = eligible & ~missingLimbs(player);
        if (free == 0) return;
        int limb = chooseLimb(player, free);
        player.getPersistentData().putInt(MISSING_LIMBS, missingLimbs(player) | limb);
        player.getPersistentData().putString(tokenKey(limb), UUID.randomUUID().toString());
        if (player.level() instanceof ServerLevel level) {
            Vec3 position = player.position().add(0,
                (limb == LEFT_ARM || limb == RIGHT_ARM ? .76 : .25) * player.getBbHeight(), 0);
            level.sendParticles(SOOT, position.x, position.y, position.z, 35, .18, .19, .18, .025);
            level.sendParticles(ParticleTypes.SMOKE, position.x, position.y, position.z, 24, .17, .16, .17, .02);
        }
        // Exactly one non-stackable pickupable limb is spawned for the newly
        // removed limb. No drops occur on failed damage or duplicate cuts.
        int dropEntityId = SeveredLimbDrops.spawn(player, limb);
        SmokeMeshNetwork.syncLimbs(player);
        SmokeMeshNetwork.limbEffect(player, limb, false, dropEntityId);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "Your " + limbName(limb) + " was severed! Advanced Healing is required to regrow it."), false);
    }

    private static int chooseLimb(ServerPlayer target, int candidates) {
        int count = Integer.bitCount(candidates);
        int roll = target.getRandom().nextInt(count);
        for (int limb : new int[] {LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG}) {
            if ((candidates & limb) != 0 && roll-- == 0) return limb;
        }
        throw new IllegalStateException("No available limb in candidate mask");
    }
    private static String limbName(int limb) {
        return switch (limb) {
            case LEFT_ARM -> "left arm";
            case RIGHT_ARM -> "right arm";
            case LEFT_LEG -> "left leg";
            case RIGHT_LEG -> "right leg";
            default -> "limb";
        };
    }

    /**
     * Remove ONLY the Dismemberment injury and its own named attribute
     * modifiers. Poison, Time effects, independent Slowness/Weakness and
     * unrelated curses are NOT cleared here.
     *
     * Legacy Pass 20.3 wounds used vanilla Slowness/Weakness and therefore
     * cannot track provenance. Those are cleared only when curing a legacy
     * wound, preserving the old-save repair behavior.
     */
    public static boolean healDismemberment(LivingEntity patient) {
        return healDismemberment(patient, false);
    }

    /** Clear ordinary injuries; remove actual missing limbs ONLY for a
     * sufficiently mastered regenerative Healing technique. */
    public static boolean healDismemberment(LivingEntity patient, boolean restoreLimbs) {
        if (!(patient.level() instanceof ServerLevel level)) return false;
        CompoundTag data = patient.getPersistentData();
        boolean activeWound = data.getLong(SEVER_UNTIL) > level.getGameTime();
        int missingBefore = missingLimbs(patient);
        boolean oldStyle = activeWound && !data.getBoolean(SEVER_MODERN);
        clearInjuryData(data);
        if (restoreLimbs && missingBefore != 0) {
            data.remove(MISSING_LIMBS);
            for (int limb : new int[] {LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG})
                data.remove(tokenKey(limb));
        }
        refreshInjuryModifiers(patient);
        boolean restored = restoreLimbs && missingBefore != 0;
        if (!activeWound && !restored) {
            if (missingBefore != 0 && patient instanceof ServerPlayer player) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "This Healing technique cannot regrow your missing limbs yet."), true);
            }
            return false;
        }
        if (oldStyle) {
            patient.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            patient.removeEffect(MobEffects.WEAKNESS);
        }
        Vec3 at = patient.position().add(0, patient.getBbHeight() * .65, 0);
        level.sendParticles(ParticleTypes.HEART, at.x, at.y, at.z, restored ? 26 : 9, .27, .35, .27, .02);
        level.sendParticles(SOOT, at.x, at.y, at.z, restored ? 24 : 7, .23, .26, .23, .005);
        if (patient instanceof ServerPlayer player) {
            SmokeNetwork.sync(player);
            if (restored) {
                SmokeMeshNetwork.syncLimbs(player);
                for (int limb : new int[]{LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG})
                    if ((missingBefore & limb) != 0) SmokeMeshNetwork.limbEffect(player, limb, true);
            }
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                restored ? "Your missing limbs have been regenerated." :
                    (missingBefore != 0 ? "Wounds treated; missing limbs still need advanced Healing." :
                     "Your Dismemberment injury has been repaired.")), true);
        }
        return true;
    }

    /** Shed Skin's advanced mastery activates accelerated regeneration, but
     * never grants Healing magic or immediately resets an injured model. */
    public static void boostReptileRegrowth(ServerPlayer caster, int mastery) {
        if (SmokeEngine.innate(caster) != MagicType.REPTILE || mastery < 650
            || missingLimbs(caster) == 0) return;
        caster.getPersistentData().putLong(REPTILE_REGEN_BOOST_UNTIL,
            caster.level().getGameTime() + 2400L);
        caster.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "Reptilian tissue regrowth accelerated. Keep your Smoke reserves supplied."), true);
    }

    /** Called once per second on the server. At mastery 850+ Reptile can
     * regenerate slowly without a Healing Sorcerer. Mastered Shed Skin
     * (650+) briefly enables a faster, Smoke-consuming recovery path.
     * All four limb bits remain permanent until a complete regrowth finishes.
     * Progress survives logout/death (and pausing), never client authority.
     */
    public static void reptileRegrowthTick(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (SmokeEngine.innate(player) != MagicType.REPTILE) return;
        int missing = missingLimbs(player);
        if (missing == 0) {
            data.remove(REPTILE_REGROW_PROGRESS);
            data.remove(REPTILE_REGEN_BOOST_UNTIL);
            return;
        }
        if (player.isSpectator() || !player.isAlive()) return;
        SmokeState state = SmokeEngine.state(player);
        int shed = state.mastery().getOrDefault(SmokeSkill.SHED_SKIN.id(), 0);
        int apex = state.mastery().getOrDefault(SmokeSkill.APEX_FORM.id(), 0);
        int biologicalMastery = Math.max(shed, apex);
        boolean accelerated = shed >= 650
            && player.level().getGameTime() < data.getLong(REPTILE_REGEN_BOOST_UNTIL);
        if (!accelerated && biologicalMastery < 850) return;
        // Costs real Smoke every second. If empty, regrowth pauses rather
        // than silently healing for free or deleting saved progress.
        if (state.smoke() < 2) return;
        player.setData(SmokeAttachments.STATE, state.withSmoke(state.smoke() - 2));
        int required = accelerated
            ? Math.max(1600, 2400 - Math.max(0, shed - 650) * 2)
            : Math.max(5500, 9000 - (biologicalMastery - 850) * 20);
        int progress = Math.min(12000, data.getInt(REPTILE_REGROW_PROGRESS) + 20);
        if (progress < required) {
            data.putInt(REPTILE_REGROW_PROGRESS, progress);
            if (player.tickCount % 100 == 0) SmokeNetwork.sync(player);
            return;
        }
        data.putInt(REPTILE_REGROW_PROGRESS, 0);
        // One limb per successful cycle; never clear all injuries at once.
        int limb = (missing & LEFT_ARM) != 0 ? LEFT_ARM
            : (missing & RIGHT_ARM) != 0 ? RIGHT_ARM
            : (missing & LEFT_LEG) != 0 ? LEFT_LEG : RIGHT_LEG;
        int remaining = missing & ~limb;
        if (remaining == 0) data.remove(MISSING_LIMBS);
        else data.putInt(MISSING_LIMBS, remaining);
        data.remove(tokenKey(limb));
        refreshInjuryModifiers(player);
        SmokeMeshNetwork.syncLimbs(player);
        SmokeMeshNetwork.limbEffect(player, limb, true);
        SmokeNetwork.sync(player);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "Reptile magic regenerated your " + limbName(limb) + "."), false);
    }

    /** Restore exactly one limb following successful owner-verified medical treatment.
     * Unlike general Healing regeneration, this does not cure any other missing limb.
     * The caller must already have validated the original physical item.
     */
    public static boolean reattachRecoveredLimb(ServerPlayer patient, int limb) {
        if (!(patient.level() instanceof ServerLevel level) ||
            Integer.bitCount(limb) != 1 || (limb & ALL_LIMBS) == 0 ||
            (missingLimbs(patient) & limb) == 0) return false;
        CompoundTag data = patient.getPersistentData();
        int remaining = missingLimbs(patient) & ~limb;
        if (remaining == 0) data.remove(MISSING_LIMBS);
        else data.putInt(MISSING_LIMBS, remaining);
        data.remove(tokenKey(limb));
        // Only treat the current wound when it matches the reattached region.
        String zone = data.getString(SEVER_ZONE);
        boolean arm = (limb & (LEFT_ARM | RIGHT_ARM)) != 0;
        if ((arm && "arm".equals(zone)) || (!arm && "leg".equals(zone)))
            clearInjuryData(data);
        data.remove(REPTILE_REGROW_PROGRESS); // Restart future recovery for other limbs.
        refreshInjuryModifiers(patient);
        SmokeMeshNetwork.syncLimbs(patient);
        SmokeMeshNetwork.limbEffect(patient, limb, true);
        SmokeNetwork.sync(patient);
        Vec3 at = patient.position().add(0, patient.getBbHeight() * (arm ? .72 : .30), 0);
        level.sendParticles(ParticleTypes.HEART, at.x, at.y, at.z, 16, .18, .25, .18, .015);
        level.sendParticles(SOOT, at.x, at.y, at.z, 20, .2, .23, .2, .008);
        patient.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "Your " + limbName(limb) + " has been reattached!"), false);
        return true;
    }

    private static void clearInjuryData(CompoundTag data) {
        data.remove(SEVER_UNTIL);
        data.remove(SEVER_POWER);
        data.remove(SEVER_ZONE);
        data.remove(SEVER_MODERN);
    }

    /** Reassert transient modifiers after a save reload without overwriting
     * other mods' effects. Different injuries have different combat penalties. */
    private static void refreshInjuryModifiers(LivingEntity patient) {
        CompoundTag data = patient.getPersistentData();
        int missing = missingLimbs(patient);
        boolean active = data.getLong(SEVER_UNTIL) > patient.level().getGameTime()
            && data.getBoolean(SEVER_MODERN);
        String zone = data.contains(SEVER_ZONE) ? data.getString(SEVER_ZONE) : "leg";
        int power = Math.clamp(data.getInt(SEVER_POWER), 1, 3);
        boolean leg = active && ("leg".equals(zone) || "body".equals(zone));
        boolean arm = active && ("arm".equals(zone) || "body".equals(zone));
        int absentLegs = Integer.bitCount(missing & (LEFT_LEG | RIGHT_LEG));
        int absentArms = Integer.bitCount(missing & (LEFT_ARM | RIGHT_ARM));
        double legPenalty = Math.min(0.90, Math.max(leg ? .08 + power * .09 : 0,
            absentLegs == 2 ? .82 : absentLegs == 1 ? .42 : 0)) * -1;
        double armPenalty = Math.min(0.90, Math.max(arm ? .06 + power * .09 : 0,
            absentArms == 2 ? .78 : absentArms == 1 ? .36 : 0)) * -1;
        updateModifier(patient, Attributes.MOVEMENT_SPEED, SEVER_LEG_SPEED, legPenalty);
        updateModifier(patient, Attributes.ATTACK_DAMAGE, SEVER_ARM_ATTACK, armPenalty);
        updateModifier(patient, Attributes.ATTACK_SPEED, SEVER_ARM_SWING, armPenalty);
    }

    private static void updateModifier(LivingEntity patient, Holder<Attribute> attribute,
                                       ResourceLocation id, double penalty) {
        AttributeInstance instance = patient.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier current = instance.getModifier(id);
        if (penalty == 0) {
            if (current != null) instance.removeModifier(id);
        } else if (current == null || Math.abs(current.amount() - penalty) > 0.00001) {
            if (current != null) instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, penalty,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** A compact, safe status field for client HUD display. Never gives the
     * client authority over injury severity, healing, or persistence. */
    public static String injuryStatus(LivingEntity patient) {
        if (!hasActiveDismemberment(patient)) return "none";
        int missing = missingLimbs(patient);
        if (missing != 0) {
            List<String> lost = new ArrayList<>();
            for (int limb : new int[] {LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG})
                if ((missing & limb) != 0) lost.add(limbName(limb));
            return "Missing " + String.join(", ", lost) + " (Healing required)";
        }
        CompoundTag data = patient.getPersistentData();
        String zone = data.contains(SEVER_ZONE) ? data.getString(SEVER_ZONE) : "leg";
        long remaining = data.getLong(SEVER_UNTIL) - patient.level().getGameTime();
        return zone + (data.getLong(SEVER_UNTIL) == Long.MAX_VALUE
            ? " (treatment needed)" : " (" + Math.max(1, (remaining + 19) / 20) + "s)");
    }

    /** Hexes are temporary and nonlethal even on other players. */
    public static void curse(LivingEntity target, ServerPlayer caster, int mastery, int rank) {
        int duration = Math.min(1800, 150 + mastery + rank * 5);
        CompoundTag data = target.getPersistentData();
        data.putUUID(CURSE_CASTER, caster.getUUID());
        data.putLong(CURSE_UNTIL, Math.max(data.getLong(CURSE_UNTIL), target.level().getGameTime() + duration));
        data.putInt(CURSE_POWER, mastery >= 700 ? 2 : mastery >= 250 ? 1 : 0);
    }

    /** An area is truly affected for its lifetime, rather than slowed once. */
    public static void createTimeField(ServerLevel level, ServerPlayer caster, Vec3 center, int mastery, boolean focused) {
        if (FIELDS.size() >= MAX_FIELDS) FIELDS.remove(0);
        int duration = Math.min(340, 75 + mastery / 5 + (focused ? 55 : 0));
        double radius = Math.min(8, 2.7 + mastery / 270.0 + (focused ? 1.2 : 0));
        FIELDS.add(new TimeField(level, center, caster.getUUID(), radius,
            level.getGameTime() + duration, mastery >= 500 ? 3 : 1));
    }

    /** Safely erect real collidable blocks. No player-owned walls are replaced,
     * no entity is encased, and protected regions may deny placement. */
    public static void raiseBarrier(ServerLevel level, ServerPlayer caster, int mastery,
                                    boolean focused, boolean wide) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        Vec3 dir = caster.getLookAngle();
        Vec3 flat = new Vec3(dir.x, 0, dir.z);
        if (flat.lengthSqr() < .01) flat = new Vec3(0, 0, 1);
        flat = flat.normalize();
        Vec3 side = new Vec3(-flat.z, 0, flat.x);
        Vec3 anchor = caster.position().add(flat.scale(3.2));
        int width = wide ? (mastery >= 500 ? 2 : 1) : 1;
        int height = mastery >= 600 && wide ? 3 : 2;
        long now = level.getGameTime();
        // Protection never leaves permanent magic structures. Greater mastery
        // raises duration and size, but every generated barrier must expire.
        long expiry = now + Math.min(1800L, 240L + mastery * 2L + (focused ? 160L : 0L));
        for (int y = 0; y < height; y++) for (int x = -width; x <= width; x++) {
            BlockPos pos = BlockPos.containing(anchor.add(side.scale(x)).add(0, y, 0));
            if (BARRIER_STEPS.size() >= 96) return;
            if (isOpenForBarrier(level, caster, pos)) {
                BARRIER_STEPS.addLast(new BarrierStep(level, caster.getUUID(), pos,
                    now, y * 3 + Math.abs(x) * 2, expiry));
            }
        }
    }

    private static boolean isOpenForBarrier(ServerLevel level, ServerPlayer caster, BlockPos pos) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos)
            && level.hasChunkAt(pos) && level.isEmptyBlock(pos)
            && level.clip(new ClipContext(caster.getEyePosition(), Vec3.atCenterOf(pos),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getType() != HitResult.Type.BLOCK
            && caster.mayUseItemAt(pos, Direction.UP, new ItemStack(ModBlocks.CONDENSED_SMOKE_BARRIER.get()))
            && level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), LivingEntity::isAlive).isEmpty();
    }

    /** High-mastery smoke causes REAL vanilla block explosions, not particles
     * or debris-only deletion. No distinction between wilderness, worldgen
     * structures, opponents' houses or the caster's own buildings.
     * Vanilla explosion resistance and NeoForge explosion events still apply.
     * Source is the actual Sorcerer for attribution. FIRE disabled; item drops
     * are handled by Minecraft's explosion rules. The caller must ensure
     * the proper offensive spell has been cast, and smoke/cooldowns paid.
     *
     * Return false to use the less destructive base spell damage at lower
     * mastery or when the world owner disables block explosions. */
    public static boolean destructiveBlast(ServerLevel level, ServerPlayer caster,
                                            int mastery, boolean focused,
                                            Vec3 impact, int masteryThreshold,
                                            float baseRadius) {
        if (mastery < masteryThreshold || !SmokeWorldConfig.TERRAIN_DAMAGE.get()) return false;
        if (!level.getWorldBorder().isWithinBounds(BlockPos.containing(impact))) return false;
        if (!level.hasChunkAt(BlockPos.containing(impact))) return false;
        // Power is the vanilla explosion strength, NOT the exact crater radius.
        // A 1000-mastery caster has significantly more destructive force.
        float power = Math.min(4.2f, baseRadius + Math.min(1000, mastery) / 900.0f
            + (focused ? 0.45f : 0.0f));
        level.explode(caster, impact.x, impact.y, impact.z, power, Level.ExplosionInteraction.BLOCK);
        return true;
    }

    private static void entityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();
        if (entity instanceof ServerPlayer player) {
            refreshScale(player);
            if (now < player.getPersistentData().getLong(CLIMB_UNTIL)
                && player.horizontalCollision && !player.onGround() && !player.isShiftKeyDown()
                && !player.isInWaterOrBubble() && !player.isSpectator()) {
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, Math.max(velocity.y, .14), velocity.z);
                player.hurtMarked = true;
            }
        }
        if (!(entity instanceof LivingEntity living) || living.tickCount % 10 != 0) return;
        CompoundTag tag = living.getPersistentData();
        if (tag.contains(SEVER_UNTIL) && now >= tag.getLong(SEVER_UNTIL)) {
            clearInjuryData(tag);
            if (living instanceof ServerPlayer expired) SmokeNetwork.sync(expired);
        }
        // Vanilla attribute modifiers are transient, but the wound is saved
        // in entity data; refresh after respawns/reconnects and when it expires.
        refreshInjuryModifiers(living);
        if (hasActiveDismemberment(living) && living.tickCount % 20 == 0) {
            injuryMark(level, living);
        }
        if (now < tag.getLong(CURSE_UNTIL)) {
            if (living.tickCount % 40 == 0) {
                // The caster must still be present and PvP permitted. Attribute
                // periodic injury to its original Sorcerer, not a generic world
                // damage source (including kills and combat-log attribution).
                // The legacy nonlethal limit applies to this extra lingering
                // attrition only; direct Curse hits can defeat an opponent.
                ServerPlayer owner = tag.hasUUID(CURSE_CASTER)
                    ? level.getServer().getPlayerList().getPlayer(tag.getUUID(CURSE_CASTER)) : null;
                if (owner != null && owner.level() == level
                    && (!(living instanceof Player opponent) || owner.canHarmPlayer(opponent))
                    && living.getHealth() > 2.0f) {
                    living.hurt(owner.damageSources().indirectMagic(owner, owner),
                        Math.min(1.5f, .4f + tag.getInt(CURSE_POWER) * .35f));
                }
                smokeMark(level, living, true);
            }
        }
    }

    /** Black, thin severing seam near the injured anatomy. Distinct from a
     * curse mark; uses only built-in particles and remains a prototype VFX. */
    private static void injuryMark(ServerLevel level, LivingEntity entity) {
        String zone = entity.getPersistentData().getString(SEVER_ZONE);
        if (zone.isEmpty() && missingLimbs(entity) != 0) zone = "body";
        double y = entity.getBbHeight() * ("leg".equals(zone) ? .28 : "arm".equals(zone) ? .72 : .52);
        Vec3 point = entity.position().add(0, y, 0);
        level.sendParticles(SOOT, point.x, point.y, point.z,
            "body".equals(zone) ? 14 : 8, .14, .12, .14, .002);
        for (int i = -2; i <= 2; i++) {
            level.sendParticles(SOOT, point.x + i * .12, point.y + i * .025, point.z,
                1, .01, .01, .01, 0);
        }
    }

    private static void smokeMark(ServerLevel level, LivingEntity entity, boolean curse) {
        Vec3 p = entity.position().add(0, entity.getBbHeight()*.7, 0);
        level.sendParticles(SOOT, p.x, p.y, p.z, curse ? 10 : 6, .28, .3, .28, .01);
        if (curse) level.sendParticles(ParticleTypes.SOUL, p.x,p.y,p.z,2,.2,.2,.2,.006);
    }

    private static void serverTick(ServerTickEvent.Post event) {
        Iterator<BarrierStep> steps = BARRIER_STEPS.iterator();
        while (steps.hasNext()) {
            BarrierStep next = steps.next();
            if (next.level().getServer() != event.getServer()) { steps.remove(); continue; }
            if (next.level().getGameTime() - next.start() < next.delay()) continue;
            steps.remove();
            ServerPlayer caster = next.level().getServer().getPlayerList().getPlayer(next.caster());
            if (caster == null || caster.level() != next.level()) continue;
            if (!next.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) continue;
            if (!isOpenForBarrier(next.level(), caster, next.pos())) continue;
            BarrierData data = BarrierData.get(next.level());
            // Never place an untracked barrier: untracked blocks could last
            // forever across reloads or when the saved-data cap is reached.
            if (!data.canTrack()) continue;
            if (next.level().setBlock(next.pos(), ModBlocks.CONDENSED_SMOKE_BARRIER.get().defaultBlockState(), 3)) {
                data.track(next.pos(), next.expiry());
                Vec3 p = Vec3.atCenterOf(next.pos());
                next.level().sendParticles(SOOT,p.x,p.y,p.z,8,.4,.4,.4,.01);
            }
        }
        Iterator<TimeField> it = FIELDS.iterator();
        while (it.hasNext()) {
            TimeField field = it.next();
            if (field.level().getServer() != event.getServer() || field.level().getGameTime() >= field.expires()) {
                it.remove();continue;
            }
            if (field.level().getGameTime() % 10 != 0) continue;
            double r = field.radius();
            AABB area = new AABB(field.center().add(-r,-r,-r), field.center().add(r,r,r));
            ServerPlayer owner = field.level().getServer().getPlayerList().getPlayer(field.owner());
            if (owner == null || owner.level() != field.level()) { it.remove(); continue; }
            for (LivingEntity affected: field.level().getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != owner && entity.isAlive()
                        && (!(entity instanceof Player opponent) || owner.canHarmPlayer(opponent)))) {
                if (affected.position().distanceToSqr(field.center()) > r*r) continue;
                affected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, field.potency()));
                affected.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 24, 1));
            }
            field.level().sendParticles(ParticleTypes.PORTAL, field.center().x, field.center().y,
                field.center().z, 8, r*.65, .6, r*.65, .002);
            field.level().sendParticles(SOOT, field.center().x, field.center().y,
                field.center().z, 12, r*.65, .8, r*.65, .0);
        }
        if (event.getServer().getTickCount() % 20 == 0) {
            for (ServerLevel level : event.getServer().getAllLevels()) BarrierData.get(level).expire(level);
        }
    }

    /** Send the actual mask to a client whenever it starts tracking a player.
     * Clients only render this value and cannot change it. */
    private static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer watcher
            && event.getTarget() instanceof ServerPlayer target) {
            SmokeMeshNetwork.sendLimbsTo(watcher, target);
        }
    }

    /** Preserve supernatural injuries across vanilla death/respawn, as well as
     * ordinary reconnects, until healed with a proper regenerative technique. */
    private static void clonePlayer(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag old = event.getOriginal().getPersistentData();
        CompoundTag current = event.getEntity().getPersistentData();
        if (old.contains(MISSING_LIMBS)) current.putInt(MISSING_LIMBS, old.getInt(MISSING_LIMBS));
        for (int limb : new int[] {LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG}) {
            if (old.contains(tokenKey(limb)))
                current.putString(tokenKey(limb), old.getString(tokenKey(limb)));
        }
        if (old.contains(REPTILE_REGROW_PROGRESS)) current.putInt(REPTILE_REGROW_PROGRESS, old.getInt(REPTILE_REGROW_PROGRESS));
        if (old.contains(REPTILE_REGEN_BOOST_UNTIL)) current.putLong(REPTILE_REGEN_BOOST_UNTIL, old.getLong(REPTILE_REGEN_BOOST_UNTIL));
        if (old.contains(SEVER_UNTIL)) current.putLong(SEVER_UNTIL, old.getLong(SEVER_UNTIL));
        if (old.contains(SEVER_POWER)) current.putInt(SEVER_POWER, old.getInt(SEVER_POWER));
        if (old.contains(SEVER_ZONE)) current.putString(SEVER_ZONE, old.getString(SEVER_ZONE));
        if (old.contains(SEVER_MODERN)) current.putBoolean(SEVER_MODERN, old.getBoolean(SEVER_MODERN));
    }

    private static void blockBroken(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level) BarrierData.get(level).forget(event.getPos());
    }

    /** Saved expiry is per world, survives restarts, and only removes its own
     * special barrier block. Every newly placed barrier is tracked, even at
     * maximum mastery. Never deletes structures or unrelated blocks. */
    public static final class BarrierData extends SavedData {
        private static final String ID = "dorohedoro_condensed_smoke_barriers";
        private final java.util.Map<Long, Long> active = new java.util.HashMap<>();
        private static BarrierData load(CompoundTag tag, HolderLookup.Provider registries) {
            BarrierData data = new BarrierData();
            ListTag list = tag.getList("barriers", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size() && i < MAX_BARRIER_BLOCKS; i++) {
                CompoundTag row = list.getCompound(i);
                data.active.put(row.getLong("pos"), row.getLong("expiry"));
            }
            return data;
        }
        public static BarrierData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BarrierData::new, BarrierData::load), ID);
        }
        @Override public CompoundTag save(CompoundTag out, HolderLookup.Provider registries) {
            ListTag rows = new ListTag();
            active.forEach((pos,expiry) -> {
                CompoundTag item = new CompoundTag();
                item.putLong("pos",pos);
                item.putLong("expiry",expiry);
                rows.add(item);
            });
            out.put("barriers",rows);
            return out;
        }
        public boolean canTrack() { return active.size() < MAX_BARRIER_BLOCKS; }
        public void track(BlockPos pos, long expiry) {
            if (!canTrack()) return;
            active.put(pos.asLong(), expiry);
            setDirty();
        }
        public void forget(BlockPos pos) { if (active.remove(pos.asLong()) != null) setDirty(); }
        public void expire(ServerLevel level) {
            long now = level.getGameTime();
            boolean changed = false;
            Iterator<java.util.Map.Entry<Long,Long>> iter = active.entrySet().iterator();
            while (iter.hasNext()) {
                java.util.Map.Entry<Long,Long> record = iter.next();
                BlockPos pos = BlockPos.of(record.getKey());
                if (!level.hasChunkAt(pos)) continue;
                if (!level.getBlockState(pos).is(ModBlocks.CONDENSED_SMOKE_BARRIER.get())) {
                    iter.remove(); changed = true;
                } else if (now >= record.getValue()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(),3);
                    iter.remove(); changed = true;
                }
            }
            if (changed) setDirty();
        }
    }
}
