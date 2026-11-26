package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.registry.ModSmokeParticles;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Pass 20.5: original sprite-animated, staged black smoke effects.
 *
 * The anime/manga's normal sorcerer smoke is BLACK. Magic identity is shown
 * mainly by its transformation or effect at the destination, not by an
 * arbitrary colored projectile. The short off-white/green/red accents below
 * belong to the effect, not the biological smoke.
 *
 * Geometry is time-sliced and broadcast by the server to all nearby players.
 * Four original animated texture families replace generic vanilla dust.
 * This phase does not claim full model morphing or shader/rig animation.
 */
public final class SmokeVisuals {
    private SmokeVisuals() {}

    private static final ParticleOptions SOOT = ModSmokeParticles.SOOT_PLUME.get();
    private static final ParticleOptions CHARCOAL = ModSmokeParticles.SMOKE_FILAMENT.get();
    private static final ParticleOptions BONE = new DustParticleOptions(new Vector3f(.78f, .73f, .59f), 1.0f);
    private static final ParticleOptions SPORE = ModSmokeParticles.FUNGAL_SPORE.get();
    private static final ParticleOptions SCALE = new DustParticleOptions(new Vector3f(.37f, .49f, .29f), .9f);
    private static final ParticleOptions WOUND = new DustParticleOptions(new Vector3f(.59f, .27f, .25f), .8f);
    private static final ParticleOptions RESTORED = new DustParticleOptions(new Vector3f(.85f, .79f, .68f), .85f);
    private static final ParticleOptions CURSED = new DustParticleOptions(new Vector3f(.38f, .21f, .26f), .9f);
    private static final ParticleOptions BARRIER = new DustParticleOptions(new Vector3f(.40f, .43f, .40f), .95f);
    private static final ParticleOptions CLOCK = new DustParticleOptions(new Vector3f(.58f, .55f, .46f), .8f);
    private static final ParticleOptions EMBER = new DustParticleOptions(new Vector3f(.71f, .34f, .16f), .8f);

    private static final int MAX_QUEUED = 3;
    // Main server thread only. Bounds expensive sendParticles calls per visual frame.
    private static final int MAX_PACKET_CALLS_PER_FRAME = 150;
    private static int packetsRemaining;
    private static boolean frameBudgetActive;
    private static final ParticleOptions FIBER = ModSmokeParticles.HEALING_FIBER.get();
    private static final int DURATION_TICKS = 26;
    private static final Map<UUID, ArrayDeque<CastSequence>> ACTIVE = new HashMap<>();

    private record CastSequence(SmokeSkill skill, Vec3 source, Vec3 impact,
                                UUID entityId, boolean focused, int index, int mastery,
                                long startTick, ServerLevel world) {}

    /** Called only after a successful server-side technique execution. */
    public static void cast(ServerLevel world, ServerPlayer caster, SmokeSkill skill,
                            LivingEntity hostile, LivingEntity ally, boolean focused, int mastery) {
        boolean supportive = isTargetedAid(skill);
        boolean againstEnemy = isTargetedHostile(skill);
        LivingEntity recipient = supportive ? (ally == null ? caster : ally)
            : againstEnemy ? hostile : caster;
        Vec3 impact = recipient == null ? caster.position().add(0, 1.0, 0)
            : atChest(recipient);
        // Structure-targeted spells without a living target still send smoke
        // toward the aimed wall rather than appearing only on the caster.
        if (skill.magic() == MagicType.DESTRUCTION && hostile == null) {
            if (skill == SmokeSkill.DETONATION || skill == SmokeSkill.SHOCKWAVE
                || skill == SmokeSkill.CONCUSSIVE_WAVE)
                impact = caster.position().add(0, 1.1, 0).add(caster.getLookAngle().scale(3));
            else if (skill == SmokeSkill.BREACH_BOLT || skill == SmokeSkill.ANNIHILATION_STREAM)
                impact = SmokeEngine.aimedImpact(caster, 14);
        }
        SmokeMeshNetwork.broadcast(world, caster, recipient, skill, impact, focused, mastery);
        Vec3 source = handPosition(caster);

        ArrayDeque<CastSequence> queue = ACTIVE.computeIfAbsent(caster.getUUID(), key -> new ArrayDeque<>());
        while (queue.size() >= MAX_QUEUED) queue.removeFirst();
        queue.addLast(new CastSequence(skill, source, impact,
            recipient == null ? null : recipient.getUUID(), focused,
            indexInFamily(skill), mastery, world.getGameTime(), world));

        // A visible launch even for self-only abilities: a burst from the hand,
        // not an instantaneous unexplained status change.
        cloud(world, ParticleTypes.SMOKE, source, focused ? 5 : 3, .12, .14, .12);
        cloud(world, SOOT, source, focused ? 9 : 6, .16, .18, .16);
        coil(world, CHARCOAL, source, .19, .48, 7, 0);
        world.playSound(null, caster.blockPosition(), switch (skill.magic()) {
            case MUSHROOM, REPTILE -> SoundEvents.BREWING_STAND_BREW;
            case DISMEMBERMENT -> SoundEvents.PLAYER_ATTACK_SWEEP;
            case HEALING, PROTECTION -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case CURSE -> SoundEvents.ENDERMAN_TELEPORT;
            case TIME -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case DESTRUCTION -> SoundEvents.GENERIC_EXPLODE.value();
        }, SoundSource.PLAYERS, focused ? .72f : .48f, .75f + .03f * indexInFamily(skill));
    }

    /** Time-sliced effects; run from the player's ordinary server tick. */
    public static void tick(ServerPlayer caster) {
        ArrayDeque<CastSequence> queue = ACTIVE.get(caster.getUUID());
        if (queue == null) return;
        Iterator<CastSequence> it = queue.iterator();
        while (it.hasNext()) {
            CastSequence shot = it.next();
            if (shot.world() != caster.level()) { it.remove(); continue; }
            long age = shot.world().getGameTime() - shot.startTick();
            if (age < 0 || age > DURATION_TICKS) { it.remove(); continue; }
            // Send small batches on alternate ticks; avoids large single-tick bursts.
            if (age % 2 != 0) continue;
            int phase = (int) age / 2;
            Vec3 position = shot.impact();
            if (shot.entityId() != null) {
                Entity moving = shot.world().getEntity(shot.entityId());
                if (moving instanceof LivingEntity live && live.isAlive()) position = atChest(live);
            }
            animate(shot.world(), caster.getUUID(), shot.skill(), shot.source(), position,
                phase, shot.index(), shot.focused(), shot.startTick(), shot.mastery());
        }
        if (queue.isEmpty()) ACTIVE.remove(caster.getUUID());
    }

    public static void logout(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
    }

    /** For multiple healing or protection recipients, paint each affected player. */
    public static void aidRecipient(ServerLevel world, ServerPlayer caster, LivingEntity recipient,
                                    SmokeSkill skill) {
        if (recipient == caster) return;
        Vec3 from = handPosition(caster);
        Vec3 to = atChest(recipient);
        line(world, SOOT, from, to, 9);
        if (skill.magic() == MagicType.HEALING) {
            SmokeMeshNetwork.broadcastAid(world, caster, recipient, skill);
            stitch(world, to, .54, 0);
        } else if (skill.magic() == MagicType.PROTECTION) {
            cocoon(world, to, .53, 0);
        }
    }

    private static Vec3 atChest(LivingEntity e) {
        return e.position().add(0, Math.min(e.getBbHeight() * .6, 1.15), 0);
    }
    private static Vec3 handPosition(ServerPlayer p) {
        Vec3 forward = p.getLookAngle().normalize();
        Vec3 side = forward.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < .001) side = new Vec3(1, 0, 0);
        return p.getEyePosition().add(0, -.36, 0)
            .add(forward.scale(.45)).add(side.normalize().scale(.24));
    }

    /** Charging, travel, contact and dissipate are different animation stages.
     * Mechanical damage occurs in SmokeEngine and is not delayed by visuals. */
    private static void animate(ServerLevel world, UUID casterId, SmokeSkill skill, Vec3 source, Vec3 impact,
                                int phase, int index, boolean focused, long startTick, int mastery) {
        frameBudgetActive = true;
        packetsRemaining = MAX_PACKET_CALLS_PER_FRAME;
        int strength = focused ? 2 : 1;
        Vec3 path = impact.subtract(source);
        Vec3 travel = path.lengthSqr() > .0001 ? path.normalize() : new Vec3(0, 0, 1);
        Vec3 lateral = travel.cross(new Vec3(0, 1, 0));
        if (lateral.lengthSqr() < .0001) lateral = new Vec3(1, 0, 0);
        lateral = lateral.normalize();

        CastPattern pattern = castPattern(skill);
        // Phase 0: thick smoke is expelled from the hand and twists away.
        if (phase == 0) {
            coil(world, CHARCOAL, source.add(travel.scale(.13)), .15 + (focused ? .08 : 0), .46, 7, startTick * .19);
            cloud(world, SOOT, source, 4 * strength, .11, .16, .11);
        }
        // Phase 1: curved smoke streamer, not a perfectly straight neon beam.
        if (phase == 1 && path.lengthSqr() > 1.0
                && (pattern == CastPattern.BOLT || pattern == CastPattern.TOUCH)) {
            ribbon(world, source, impact, lateral, .42, index, 1 + strength);
            cloud(world, SOOT, source.lerp(impact, .65), 3 * strength, .13, .12, .13);
        }
        // Phase 2: contact. A second, darker plume blossoms around the target.
        if (phase == 2) {
            cloud(world, SOOT, impact, 7 * strength, .23, .32, .23);
            coil(world, CHARCOAL, impact.add(0, -.48, 0), .31, .93, 11, index * .20);
            impactSound(world, skill, impact, focused);
        }
        // Individual spells share their innate smoke but vary in casting silhouette.
        castPatternShape(world, pattern, impact, phase, strength);
        // After contact only: magic identity comes from the biological material
        // and physical manifestation, not from different-colored spell beams.
        if (phase >= 2) {
            int volume = Math.max(1, strength * (phase >= 9 ? 1 : 3));
            if (phase % 2 == 0) cloud(world, SOOT, impact, volume, .25 + phase*.014, .24, .25);
            if (phase >= 10 && phase % 2 == 0) cloud(world, CHARCOAL, impact.add(0,.14,0), 2, .5, .3, .5);
        }
        // Keep the physical mushroom scheduler's phase==2 contract intact.
        if (phase < 2) { frameBudgetActive = false; return; }
        switch (skill.magic()) {
            case MUSHROOM -> {
                fungal(world, impact, phase, index, strength);
                MushroomSpellVisuals.animate(world, casterId, skill, impact, phase, index, focused, startTick, mastery);
            }
            case REPTILE -> reptile(world, impact, phase, index, strength);
            case DISMEMBERMENT -> dismember(world, impact, phase, index, strength, lateral, travel);
            case HEALING -> healing(world, impact, phase, index, strength);
            case CURSE -> curse(world, impact, phase, index, strength);
            case PROTECTION -> protection(world, impact, phase, index, strength);
            case TIME -> time(world, impact, phase, index, strength, lateral);
            case DESTRUCTION -> destruction(world, impact, phase, index, strength);
        }
        frameBudgetActive = false;
    }


    /** Seven visual movement archetypes, assigned explicitly to the 64 techniques.
     * Only the cast silhouette changes: innate magic, damage and cooldowns do not. */
    private enum CastPattern { BOLT, TOUCH, AREA, BODY, SHELL, CONSTRUCT, RUPTURE }

    private static CastPattern castPattern(SmokeSkill skill) {
        return switch (skill) {
            case SPORE_PUFF, MEND_ALLY, CLEANSE_ALLY, VITAL_SHELL,
                 CURSE_MARK, HEX_BURDEN, WITHER_SEAL, MARK_OF_DREAD,
                 PERSISTENT_HEX, MISFORTUNE, RUINOUS_CURSE,
                 GUARD_ALLY, DILATED_MOMENT, SLOWED_MOMENT,
                 SMOKE_SHOT, BREACH_BOLT, RUPTURE_ARMOR,
                 SHRAPNEL_SMOKE, ANNIHILATION_STREAM -> CastPattern.BOLT;
            case FUNGAL_BIND, TAIL_LASH, SEVERING_TOUCH, CLEAN_CUT,
                 DISMEMBER_STRIKE, PRECISION_NERVE, LIMB_LOCK,
                 DISARMING_SMOKE, ANATOMY_REND -> CastPattern.TOUCH;
            case SPORE_CLOUD, RESTORATIVE_FIELD, LIFEWEAVE, CURSE_PULSE,
                 SEVERING_WAVE, CHRONO_BURST, SHOCKWAVE, CONCUSSIVE_WAVE -> CastPattern.AREA;
            case MYCELIAL_SENSE, SPORE_PURGE, SCALE_SKIN, PREDATOR_SPRINT,
                 REPTILE_RAGE, SHED_SKIN, WALL_CRAWL, CHAMELEON_HUNT, APEX_FORM,
                 MEND_FLESH, PURIFY_BLOOD, RAPID_REGEN, SMOKE_WARD,
                 HARDENING_SHELL, FLEETING_STEP, TEMPORAL_WINDOW,
                 TEMPORAL_AWARENESS, HASTENED_HANDS -> CastPattern.BODY;
            case MYCELIUM_GUARD, PURGE_BARRIER -> CastPattern.SHELL;
            case FUNGAL_BLOOM, SPORE_DOMINION, GUARDIAN_VEIL,
                 SANCTUARY, SMOKE_BULWARK, LAST_STAND, TIME_SHELTER -> CastPattern.CONSTRUCT;
            case DETONATION -> CastPattern.RUPTURE;
        };
    }

    private static void castPatternShape(ServerLevel world, CastPattern pattern,
                                         Vec3 impact, int phase, int strength) {
        if (phase < 2 || phase > 10) return;
        if (phase >= 8 && phase % 2 == 1) return;
        switch (pattern) {
            case BOLT -> {
                if (phase <= 5) coil(world, CHARCOAL, impact.add(0,-.38,0), .20, .7, 7, phase * .5);
            }
            case TOUCH -> {
                if (phase <= 5) cloud(world, CHARCOAL, impact.add(0,.1,0), 2 * strength, .22,.25,.22);
            }
            case AREA -> {
                double radius = .25 + (phase - 2) * .38;
                ring(world, CHARCOAL, impact.add(0,-.8,0), radius, 13);
                if (phase == 4 || phase == 7) cloud(world, SOOT, impact.add(0,-.5,0), 5*strength, radius,.12,radius);
            }
            case BODY -> {
                if (phase <= 7) coil(world, CHARCOAL, impact.add(0,-.65,0), .39, 1.25, 9, phase * .52);
            }
            case SHELL -> {
                if (phase <= 8) cocoon(world, impact, .43+phase*.032, phase);
            }
            case CONSTRUCT -> {
                if (phase <= 7) ring(world, CHARCOAL, impact.add(0,-.75,0), .38+phase*.19, 13);
            }
            case RUPTURE -> {
                if (phase <= 6) ring(world, SOOT, impact, .27+phase*.35, 12);
            }
        }
    }

    private static void impactSound(ServerLevel world, SmokeSkill skill, Vec3 impact, boolean focused) {
        // Shared through the level, so bystanders receive a genuine audio cue.
        world.playSound(null, net.minecraft.core.BlockPos.containing(impact), switch (skill.magic()) {
            case MUSHROOM -> SoundEvents.BREWING_STAND_BREW;
            case REPTILE -> SoundEvents.PLAYER_ATTACK_STRONG;
            case DISMEMBERMENT -> SoundEvents.PLAYER_ATTACK_SWEEP;
            case HEALING -> SoundEvents.AMETHYST_BLOCK_HIT;
            case CURSE -> SoundEvents.ENDERMAN_TELEPORT;
            case PROTECTION -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case TIME -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case DESTRUCTION -> SoundEvents.GENERIC_EXPLODE.value();
        }, SoundSource.PLAYERS, focused ? .62f : .39f, .72f + .025f * indexInFamily(skill));
    }

    private static void ribbon(ServerLevel world, Vec3 a, Vec3 b, Vec3 right,
                               double width, int index, int strength) {
        // Two twisting, tapering smoke strands form a readable directional cast.
        int samples = Math.min(20, 10 + index);
        for (int i = 0; i < samples; i++) {
            double t = (i + 1.0) / samples;
            double envelope = Math.sin(Math.PI * t);
            double offset = Math.sin(t * 10.8 + index * .61) * width * envelope;
            Vec3 center = a.lerp(b, t).add(right.scale(offset));
            point(world, CHARCOAL, center);
            if (strength > 1 && i % 2 == 0) point(world, SOOT, center.add(right.scale(.11 * envelope)));
        }
    }

    private static void fungal(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // Physical vanilla mushroom blocks are grown by MushroomSpellVisuals.
        // Only the biological black-smoke/spore plume is particle-based here;
        // no misleading neon ring pretending to be a solid mushroom.
        if (phase < 4) {
            cloud(world, SOOT, center.add(0, -.55, 0), 4 * strength, .3, .22, .3);
        } else {
            cloud(world, ParticleTypes.SPORE_BLOSSOM_AIR, center.add(0, -.3, 0),
                3 * strength + (index >= 5 ? 2 : 0), .62, .48, .62);
            cloud(world, SPORE, center.add(0, -.3, 0), 3 * strength, .55, .32, .55);
        }
    }

    private static void reptile(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // Ebisu-inspired changing anatomy: overlapping scale bands rise up the
        // body, followed by claw arcs. Actual transformed geometry remains future work.
        double radius = .38 + index * .014;
        double y = -.55 + phase * .22;
        ring(world, SCALE, center.add(0, y, 0), radius, 11);
        coil(world, SOOT, center.add(0, -.65, 0), radius, 1.4, 12, phase * .25);
        if (phase >= 3 && phase <= 9) for (int claw = -1; claw <= 1; claw++) {
            Vec3 a = center.add(claw * .19, -.20, .35);
            Vec3 b = center.add(claw * .30, .55, .48);
            line(world, claw == 0 ? BONE : SCALE, a, b, 6);
        }
        if (index == 7 && phase % 2 == 0) cloud(world, ParticleTypes.CRIT, center, 3 * strength, .4, .5, .4);
    }

    private static void dismember(ServerLevel world, Vec3 center, int phase, int index, int strength, Vec3 right, Vec3 travel) {
        // Shin-inspired black separation seams: thin, jagged cuts and fleeting
        // red interior edges, not bright fantasy sword beams. No gore or limbs.
        Vec3 base = center.add(0, .1, 0);
        double wide = .33 + phase * .045 + index * .015;
        // The seam faces the caster instead of being fixed to world north.
        Vec3 lo = base.add(right.scale(-wide)).add(0, -.55, 0);
        Vec3 hi = base.add(right.scale(wide)).add(0, .60, 0);
        line(world, CHARCOAL, lo, hi, 9);
        if (phase <= 5) line(world, WOUND, lo.add(right.scale(.035)), hi.add(right.scale(.035)), 5);
        if (phase >= 4 && phase <= 8) line(world, SOOT,
                base.add(right.scale(wide)).add(0,-.45,0),
                base.add(right.scale(-wide)).add(0,.46,0), 7);
        if (index >= 5 && phase == 4) cloud(world, ParticleTypes.SWEEP_ATTACK, center, 1, .2, .15, .2);
    }

    private static void healing(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // Noi-inspired: black smoke wraps wounds; pale stitches close inward.
        // Avoid generic green halos/hearts which don't match the source.
        double radius = .66 - Math.min(phase, 6) * .065;
        ring(world, SOOT, center.add(0, -.2 + phase * .09, 0), radius, 12);
        stitch(world, center, radius, phase);
        if (phase >= 3 && phase <= 9) cloud(world, FIBER, center, 2 * strength, .26, .36, .26);
        if (phase >= 4 && phase <= 9) cloud(world, RESTORED, center, 2 * strength, .33, .43, .33);
        if (index >= 5 && phase == 6) ring(world, RESTORED, center.add(0, .5, 0), .6, 12);
    }

    private static void curse(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // A-Curse-like encroaching black limbs/marks, not a generic pentagram.
        // Risu's canon death-triggered Curse itself is NOT simulated here.
        int tendrils = 3 + (index >= 5 ? 2 : 0);
        for (int arm = 0; arm < tendrils; arm++) {
            double theta = arm * 2 * Math.PI / tendrils + phase * .12;
            Vec3 edge = center.add(Math.cos(theta) * (.3 + phase * .055), -.55 + .13 * phase,
                Math.sin(theta) * (.3 + phase * .055));
            line(world, arm % 2 == 0 ? SOOT : CHARCOAL, center, edge, 5);
        }
        if (phase >= 2) cloud(world, CURSED, center.add(0, .28, 0), 2 * strength, .15, .22, .15);
        if (phase >= 5) ring(world, SOOT, center.add(0, -.7, 0), .45 + index * .02, 10);
    }

    private static void protection(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // Natsuki-inspired temporary cocoon of dense black smoke with seams.
        double radius = .45 + Math.min(phase, 5) * .065 + index * .015;
        cocoon(world, center, radius, phase);
        if (phase >= 4) cloud(world, ParticleTypes.SMOKE, center, 4 * strength, .43, .6, .43);
    }

    private static void time(ServerLevel world, Vec3 center, int phase, int index, int strength, Vec3 right) {
        // Nikaido-inspired eerie apparatus: a pale, offset dial materializes
        // inside black spiraling smoke. This is not real time travel yet.
        Vec3 dial = center.add(0, .23, 0);
        double radius = .38 + index * .025;
        for (int n = 0; n < 12; n++) {
            double theta = (n * Math.PI / 6) + phase * .075;
            Vec3 a = dial.add(right.scale(Math.cos(theta) * radius)).add(0, Math.sin(theta) * radius, 0);
            Vec3 b = dial.add(right.scale(Math.cos(theta) * (radius + .095)))
                    .add(0, Math.sin(theta) * (radius + .095), 0);
            line(world, CLOCK, a, b, 2);
        }
        if (phase >= 3) {
            line(world, CLOCK, dial, dial.add(right.scale(Math.cos(phase * .28) * .27))
                    .add(0, Math.sin(phase * .28) * .27, 0), 5);
            line(world, CHARCOAL, dial, dial.add(right.scale(-Math.sin(phase * .23) * .15))
                    .add(0, Math.cos(phase * .23) * .15, 0), 4);
        }
        coil(world, SOOT, center.add(0, -.55, 0), .28, 1.2, 10, phase * .17);
        if (phase == 5 && index >= 6) cloud(world, ParticleTypes.PORTAL, center, 4 * strength, .3, .4, .3);
    }

    private static void destruction(ServerLevel world, Vec3 center, int phase, int index, int strength) {
        // Fujita-inspired black smoke blast. This VFX never edits blocks;
        // actual mastery-based destruction runs separately on the server.
        double radius = .2 + phase * .16 + index * .02;
        ring(world, SOOT, center, radius, 16);
        if (phase >= 2) ring(world, CHARCOAL, center.add(0, .14, 0), radius * .8, 12);
        if (phase == 3 || phase == 4) {
            cloud(world, ParticleTypes.LARGE_SMOKE, center, 5 * strength, .35, .45, .35);
            cloud(world, EMBER, center, 3 * strength, .28, .28, .28);
        }
        if (index >= 5 && phase >= 5) ring(world, SOOT, center, radius * 1.18, 17);
    }

    private static void stitch(ServerLevel world, Vec3 center, double radius, int phase) {
        for (int i = 0; i < 4; i++) {
            double y = -.55 + i * .32;
            double lateral = radius * (.9 - i * .15);
            line(world, RESTORED, center.add(-lateral, y, .05), center.add(lateral, y + .06, .05), 5);
        }
    }

    private static void cocoon(ServerLevel world, Vec3 center, double radius, int phase) {
        ring(world, BARRIER, center.add(0, -.65, 0), radius, 11);
        ring(world, SOOT, center.add(0, .42, 0), radius * .78, 11);
        for (int i = 0; i < 5; i++) {
            double theta = (i * 2 * Math.PI / 5) + phase * .1;
            Vec3 a = center.add(Math.cos(theta) * radius, -.65, Math.sin(theta) * radius);
            Vec3 b = center.add(Math.cos(theta + .2) * radius * .74, .55, Math.sin(theta + .2) * radius * .74);
            line(world, i % 2 == 0 ? SOOT : CHARCOAL, a, b, 5);
        }
    }

    private static void coil(ServerLevel world, ParticleOptions particle, Vec3 center,
                             double radius, double height, int samples, double offset) {
        for (int i = 0; i < samples; i++) {
            double progress = i / (double) samples;
            double angle = progress * 2.75 * Math.PI + offset;
            point(world, particle, center.add(Math.cos(angle) * radius, progress * height,
                Math.sin(angle) * radius));
        }
    }
    private static void ring(ServerLevel world, ParticleOptions particle, Vec3 center,
                             double radius, int samples) {
        for (int i = 0; i < samples; i++) {
            double theta = i * Math.PI * 2 / samples;
            point(world, particle, center.add(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
    }
    private static void line(ServerLevel world, ParticleOptions particle, Vec3 a, Vec3 b, int samples) {
        for (int i = 0; i < samples; i++) {
            point(world, particle, a.lerp(b, (i + 1.0) / samples));
        }
    }
    private static void point(ServerLevel world, ParticleOptions particle, Vec3 pos) {
        if (frameBudgetActive && packetsRemaining-- <= 0) return;
        world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }
    private static void cloud(ServerLevel world, ParticleOptions particle, Vec3 pos,
                              int count, double dx, double dy, double dz) {
        if (frameBudgetActive && packetsRemaining-- <= 0) return;
        world.sendParticles(particle, pos.x, pos.y, pos.z, count, dx, dy, dz, .012);
    }

    private static int indexInFamily(SmokeSkill skill) {
        int index = 0;
        for (SmokeSkill candidate : SmokeSkill.values()) {
            if (candidate.magic() == skill.magic()) {
                if (candidate == skill) return index;
                index++;
            }
        }
        return 0;
    }
    private static boolean isTargetedAid(SmokeSkill skill) {
        return switch (skill) {
            case MEND_ALLY, CLEANSE_ALLY, VITAL_SHELL, GUARD_ALLY -> true;
            default -> false;
        };
    }
    private static boolean isTargetedHostile(SmokeSkill skill) {
        return switch (skill) {
            case SPORE_PUFF, FUNGAL_BIND, SEVERING_TOUCH, CLEAN_CUT, DISMEMBER_STRIKE,
                 CURSE_MARK, HEX_BURDEN, WITHER_SEAL, DILATED_MOMENT, SMOKE_SHOT,
                 TAIL_LASH, PRECISION_NERVE, LIMB_LOCK, DISARMING_SMOKE, ANATOMY_REND,
                 MARK_OF_DREAD, PERSISTENT_HEX, MISFORTUNE, RUINOUS_CURSE,
                 SLOWED_MOMENT, BREACH_BOLT, RUPTURE_ARMOR, SHRAPNEL_SMOKE,
                 ANNIHILATION_STREAM -> true;
            default -> false;
        };
    }
}
