package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.character.CharacterAttachments;
import com.dorohedoro.hole.character.CharacterProfile;
import com.dorohedoro.hole.character.MagicType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** Smoke casting, learning and progression authority. Clients only request actions. */
public final class SmokeEngine {
    private SmokeEngine() {}
    private static final Map<UUID,Long> TRAINING=new HashMap<>();
    public static void register(){
        NeoForge.EVENT_BUS.addListener(SmokeEngine::tick);
        MushroomSpellVisuals.register();
        OtherMagicMechanics.register();
        LimbReattachmentService.register();
    }
    public static void logout(ServerPlayer player){TRAINING.remove(player.getUUID());SmokeVisuals.logout(player);}
    public static MagicType innate(ServerPlayer p){
        CharacterProfile profile=p.getData(CharacterAttachments.PROFILE);
        return "sorcerer".equals(profile.origin())?MagicType.fromId(profile.magic()):null;
    }
    public static SmokeState state(ServerPlayer p){
        SmokeState s=p.getData(SmokeAttachments.STATE);
        MagicType t=innate(p);
        if(t!=null&&!s.initialized()){
            SmokeSkill basic=SmokeSkill.starter(t);
            if(basic!=null){s=SmokeState.initial(basic);p.setData(SmokeAttachments.STATE,s);}
        }
        return s;
    }
    private static void update(ServerPlayer p,SmokeState s){
        p.setData(SmokeAttachments.STATE,s);
        SmokeNetwork.sync(p);
    }
    public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        SmokeVisuals.tick(p);
        if (p.tickCount % 200 == 40 && OtherMagicMechanics.missingLimbs(p) != 0)
            SmokeMeshNetwork.syncLimbs(p);
        if(p.tickCount%20!=0)return;
        // Humans have no Smoke regeneration, but injured Humans still need
        // accurate read-only wound timers in their client HUD.
        if(innate(p)==null){
            if(OtherMagicMechanics.hasActiveDismemberment(p))SmokeNetwork.sync(p);
            return;
        }
        // Biology-based reptilian regrowth is distinct from another innate
        // magic type. Only the server may advance missing-limb state.
        if (innate(p) == MagicType.REPTILE) OtherMagicMechanics.reptileRegrowthTick(p);
        SmokeState s=state(p);
        int regen=2+Math.max(0,s.rank()-1)/8;
        if(s.smoke()<s.capacity())update(p,s.withSmoke(s.smoke()+regen));
        else SmokeNetwork.sync(p); // Keep HUD cooldowns accurate even at full Smoke.
    }
    public static int remaining(ServerPlayer p,String id){
        long until=state(p).cooldowns().getOrDefault(id,0L);
        return (int)Math.min(Integer.MAX_VALUE,Math.max(0L,until-p.level().getGameTime()));
    }
    public static void preset(ServerPlayer p,int preset){
        if(innate(p)==null||preset<0||preset>=SmokeState.PRESETS)return;
        update(p,state(p).withPreset(preset));
    }
    public static void equip(ServerPlayer p,int slot,String id){
        MagicType family=innate(p);
        if(family==null||slot<0||slot>=SmokeState.SLOTS)return;
        SmokeState s=state(p);
        if("-".equals(id)){update(p,s.withSlot(slot,"-"));return;}
        SmokeSkill skill=SmokeSkill.fromId(id);
        if(skill==null||skill.magic()!=family||!s.learned(skill))return;
        update(p,s.withSlot(slot,id));
    }
    public static void practice(ServerPlayer p,String id){
        MagicType family=innate(p);
        SmokeSkill skill=SmokeSkill.fromId(id);
        if(family==null||skill==null||family!=skill.magic())return;
        SmokeState s=state(p);
        if(s.rank()<skill.rankRequired()){
            p.sendSystemMessage(Component.literal("Smoke rank "+skill.rankRequired()+" required to study "+skill.display()));return;
        }
        if(s.learned(skill))return;
        if(s.smoke()<5){p.displayClientMessage(Component.literal("Need 5 Smoke to practice."),true);return;}
        long now=p.level().getGameTime();
        Long last=TRAINING.get(p.getUUID());
        if(last!=null&&now-last<40)return;
        TRAINING.put(p.getUUID(),now);
        // Practicing is a slow, deliberate acquisition loop; once learned, the skill may be equipped.
        if(p.getRandom().nextFloat()<0.10f){p.sendSystemMessage(Component.literal("The smoke failed to take shape. Try again."));return;}
        int progress=Math.min(100,s.study().getOrDefault(id,0)+5);
        s=s.withSmoke(s.smoke()-5).withStudy(id,progress);
        if(progress>=100)p.sendSystemMessage(Component.literal("Learned: "+skill.display()+". Equip it in the Smoke Ledger."));
        update(p,s);
    }
    /** Capture is possible only for Sorcerers with an assigned, biological innate type.
     * Bottling consumes current smoke and NEVER changes CharacterProfile.magic. */
    public static MagicType captureSmoke(ServerPlayer p) {
        MagicType own = innate(p);
        if (own == null) {
            p.displayClientMessage(Component.literal("Only Sorcerers can produce smoke to bottle."), true);
            return null;
        }
        SmokeState current = state(p);
        final int cost = 25;
        if (current.smoke() < cost) {
            p.displayClientMessage(Component.literal("Need 25 smoke to fill this flask."), true);
            return null;
        }
        update(p, current.withSmoke(current.smoke() - cost));
        return own;
    }

    /** One-use externally stored smoke. Works for a Human or a Sorcerer, but grants
     * no learnable skill, experience, permanent second type, or smoke capacity.
     * The contained sample performs only its associated rank-one prototype effect. */
    public static boolean releaseBottledSmoke(ServerPlayer p, MagicType contained) {
        SmokeSkill sample = SmokeSkill.starter(contained);
        if (sample == null) return false;
        LivingEntity target = selectTarget(p, 14);
        if (needsTarget(sample) && target == null) {
            p.displayClientMessage(Component.literal("No visible target for bottled " + contained.id() + " smoke."), true);
            return false;
        }
        return apply(p, sample, target, false, 1, 0);
    }

    public static void cast(ServerPlayer p,int slot,boolean focused){
        MagicType family=innate(p);
        if(family==null||slot<0||slot>=SmokeState.SLOTS)return;
        SmokeState s=state(p);
        SmokeSkill skill=SmokeSkill.fromId(s.equipped(slot));
        if(skill==null||skill.magic()!=family||!s.learned(skill))return;
        long now=p.level().getGameTime();
        long ready=s.cooldowns().getOrDefault(skill.id(),0L);
        if(ready>now){p.displayClientMessage(Component.literal(skill.display()+" cooldown: "+((ready-now+19)/20)+"s"),true);return;}
        int cost=focused?(int)Math.ceil(skill.smokeCost()*1.5):skill.smokeCost();
        if(s.smoke()<cost){p.displayClientMessage(Component.literal("Insufficient smoke"),true);return;}
        LivingEntity target=selectTarget(p,requiresMelee(skill)?3.25:14);
        if(needsTarget(skill)&&target==null){p.displayClientMessage(Component.literal("No visible target for "+skill.display()),true);return;}
        if(!apply(p,skill,target,focused,s.rank(),s.mastery().getOrDefault(skill.id(),0)))return;
        s=s.withCooldown(skill.id(),now+skill.cooldownTicks())
            .withSmoke(s.smoke()-cost)
            .withMastery(skill.id(),s.mastery().getOrDefault(skill.id(),0)+5)
            .gainExperience(12);
        update(p,s);
    }
    private static boolean requiresMelee(SmokeSkill skill){return switch(skill){
        case SEVERING_TOUCH,TAIL_LASH,PRECISION_NERVE -> true;
        default -> false;
    };}
    private static boolean needsTarget(SmokeSkill skill){return switch(skill){
        case SPORE_PUFF,FUNGAL_BIND,SEVERING_TOUCH,CLEAN_CUT,DISMEMBER_STRIKE,
            CURSE_MARK,HEX_BURDEN,WITHER_SEAL,DILATED_MOMENT,SMOKE_SHOT,
            TAIL_LASH,PRECISION_NERVE,LIMB_LOCK,DISARMING_SMOKE,ANATOMY_REND,
            MARK_OF_DREAD,PERSISTENT_HEX,MISFORTUNE,RUINOUS_CURSE,
            SLOWED_MOMENT,RUPTURE_ARMOR,SHRAPNEL_SMOKE -> true;
        default -> false;
    };}
    /** Aimed terrain contact permits PvP breaching spells to hit EMPTY homes. */
    static Vec3 aimedImpact(ServerPlayer caster, double range) {
        Vec3 from = caster.getEyePosition();
        Vec3 to = from.add(caster.getLookAngle().scale(range));
        BlockHitResult hit = caster.level().clip(new ClipContext(from, to,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : to;
    }

    private static LivingEntity selectTarget(ServerPlayer p,double range){
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),
            e->e!=p && (!(e instanceof Player opponent) || p.canHarmPlayer(opponent)) && e.isAlive() && p.hasLineOfSight(e)
               && eye.distanceToSqr(e.getEyePosition())<=range*range
               && look.dot(e.getEyePosition().subtract(eye).normalize())>0.94)
            .stream().min(Comparator.comparingDouble(e->eye.distanceToSqr(e.getEyePosition()))).orElse(null);
    }
    /**
     * Real direct health damage for offensive spells. This damage source names
     * the Sorcerer as the attacker for kills, PvP attribution and combat logs.
     * No spell may bypass the world's PvP rule simply because an AoE found a
     * player. Support spells never use this method.
     *
     * Vanilla hurt() retains armor/resistance/invulnerability handling;
     * status effects and Dismemberment injuries are separate consequences.
     */
    private static boolean impact(ServerPlayer caster, LivingEntity victim, float baseDamage, double multiplier) {
        if (victim == null || victim == caster || !victim.isAlive()) return false;
        if (victim instanceof Player player && !caster.canHarmPlayer(player)) return false;
        float amount = (float) Math.clamp(baseDamage * multiplier, 1.0, 36.0);
        return victim.hurt(caster.damageSources().indirectMagic(caster, caster), amount);
    }

    private static void effect(LivingEntity target,net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,int ticks,int amp){
        target.addEffect(new MobEffectInstance(effect,ticks,amp));
    }
    private static boolean apply(ServerPlayer p,SmokeSkill skill,LivingEntity target,boolean focused,int rank,int mastery){
        if(!(p.level() instanceof ServerLevel level))return false;
        int duration=(focused?220:130)+Math.min(100,rank*2+mastery/25);
        double multiplier=(focused?1.4:1.0)*(1+Math.min(20,rank-1)*.025)*(1+Math.min(1000,mastery)*.0002);
        // Damage is dealt at impact, separately from status ailments and wounds.
        // The multiplier includes mastery, Smoke rank and Focused casting.
        // All effects execute exclusively on the logical server. The smoke type is fixed by the player profile.
        LivingEntity ally=selectAlly(p,12);
        if(ally==null)ally=p;
        switch(skill){
            // MUSHROOM: infection, controlled spread, and defensive fungal applications.
            case SPORE_PUFF -> {impact(p,target,2.0f,multiplier);effect(target,MobEffects.POISON,duration,0);}
            case SPORE_CLOUD -> {for(LivingEntity e:nearEnemies(p,5)){impact(p,e,1.5f,multiplier);effect(e,MobEffects.POISON,duration,0);}}
            case FUNGAL_BIND -> {impact(p,target,2.5f,multiplier);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration,3);effect(target,MobEffects.WEAKNESS,duration,0);}
            case MYCELIAL_SENSE -> {effect(p,MobEffects.NIGHT_VISION,400,0);effect(p,MobEffects.MOVEMENT_SPEED,duration,0);}
            case SPORE_PURGE -> {p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WITHER);p.heal(2.5f);}
            case FUNGAL_BLOOM -> {for(LivingEntity e:nearEnemies(p,6)){impact(p,e,3.0f,multiplier);effect(e,MobEffects.POISON,duration,0);effect(e,MobEffects.MOVEMENT_SLOWDOWN,duration,1);}}
            case MYCELIUM_GUARD -> {effect(p,MobEffects.ABSORPTION,duration+120,1);effect(p,MobEffects.DAMAGE_RESISTANCE,duration,0);}
            case SPORE_DOMINION -> {for(LivingEntity e:nearEnemies(p,8)){impact(p,e,4.0f,multiplier);effect(e,MobEffects.POISON,duration+100,1);effect(e,MobEffects.MOVEMENT_SLOWDOWN,duration,2);}}
            // REPTILE: physiological transformation, movement, and recovery.
            case SCALE_SKIN -> effect(p,MobEffects.DAMAGE_RESISTANCE,duration,0);
            case PREDATOR_SPRINT -> {effect(p,MobEffects.MOVEMENT_SPEED,duration,1);effect(p,MobEffects.JUMP,duration,0);}
            case REPTILE_RAGE -> {effect(p,MobEffects.DAMAGE_BOOST,duration,1);effect(p,MobEffects.DAMAGE_RESISTANCE,duration,0);}
            case SHED_SKIN -> {p.heal(3);p.removeEffect(MobEffects.POISON);effect(p,MobEffects.REGENERATION,80,0);
                OtherMagicMechanics.boostReptileRegrowth(p, mastery);}
            case WALL_CRAWL -> {effect(p,MobEffects.JUMP,duration,2);effect(p,MobEffects.SLOW_FALLING,duration+60,0);OtherMagicMechanics.beginClimb(p,mastery,focused);}
            case TAIL_LASH -> {impact(p,target,4.5f,multiplier);pushAway(p,target,1.35);}
            case CHAMELEON_HUNT -> {effect(p,MobEffects.INVISIBILITY,duration,0);effect(p,MobEffects.MOVEMENT_SPEED,duration,0);}
            case APEX_FORM -> {effect(p,MobEffects.DAMAGE_BOOST,duration+100,2);effect(p,MobEffects.DAMAGE_RESISTANCE,duration+100,1);effect(p,MobEffects.REGENERATION,duration,0);OtherMagicMechanics.beginApex(p,mastery,focused);}
            // DISMEMBERMENT: region-specific saved injuries (not ordinary potions).
            // Clearing these wounds will not erase Slowness/Weakness from other mods.
            case SEVERING_TOUCH -> {if (impact(p,target,3.5f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case CLEAN_CUT -> {if (impact(p,target,4.5f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case DISMEMBER_STRIKE -> {if (impact(p,target,7.5f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case PRECISION_NERVE -> {if (impact(p,target,3.0f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case LIMB_LOCK -> {if (impact(p,target,2.5f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case DISARMING_SMOKE -> {if (impact(p,target,2.5f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            case SEVERING_WAVE -> {for(LivingEntity e:nearEnemies(p,5))if (impact(p,e,5.0f,multiplier))OtherMagicMechanics.sever(e,mastery,rank,skill);}
            case ANATOMY_REND -> {if (impact(p,target,10.0f,multiplier))OtherMagicMechanics.sever(target,mastery,rank,skill);}
            // HEALING: all ally targeting is cooperative and never alters another magic type.
            case MEND_FLESH -> {p.heal((float)(3*multiplier));if(mastery>=600)OtherMagicMechanics.healDismemberment(p, mastery >= 800);}
            case PURIFY_BLOOD -> {cleanse(p);p.heal(2);if(mastery>=400)OtherMagicMechanics.healDismemberment(p);}
            case RAPID_REGEN -> {effect(p,MobEffects.REGENERATION,duration,1);if(mastery>=600)OtherMagicMechanics.healDismemberment(p, mastery >= 850);}
            case MEND_ALLY -> {ally.heal((float)((4 + mastery / 250.0)*multiplier));if(mastery>=350)OtherMagicMechanics.healDismemberment(ally, mastery >= 750);if(mastery>=500)effect(ally,MobEffects.REGENERATION,100,0);}
            case CLEANSE_ALLY -> {cleanse(ally);OtherMagicMechanics.healDismemberment(ally, mastery >= 900);ally.heal(1.5f + mastery / 350.0f);if(mastery>=500)effect(ally,MobEffects.REGENERATION,100,0);}
            case RESTORATIVE_FIELD -> {for(Player friend:nearPlayers(p,6)){friend.heal((float)(3*multiplier));if(mastery>=500)OtherMagicMechanics.healDismemberment(friend, mastery >= 850);effect(friend,MobEffects.REGENERATION,duration,0);SmokeVisuals.aidRecipient(level,p,friend,skill);}}
            case VITAL_SHELL -> effect(ally,MobEffects.ABSORPTION,duration+100,1);
            case LIFEWEAVE -> {for(Player friend:nearPlayers(p,7)){friend.heal((float)(7*multiplier));cleanse(friend);OtherMagicMechanics.healDismemberment(friend, mastery >= 650);effect(friend,MobEffects.REGENERATION,duration+120,1);SmokeVisuals.aidRecipient(level,p,friend,skill);}}
            // CURSE: ordinary applications are weaker than Risu's exceptional death curse.
            case CURSE_MARK -> {impact(p,target,1.5f,multiplier);effect(target,MobEffects.WEAKNESS,duration,0);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration,0);}
            case HEX_BURDEN -> {impact(p,target,2.0f,multiplier);effect(target,MobEffects.WEAKNESS,duration,1);effect(target,MobEffects.WITHER,80,0);}
            case WITHER_SEAL -> {impact(p,target,2.5f,multiplier);effect(target,MobEffects.WITHER,duration,1);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration,1);}
            case MARK_OF_DREAD -> {impact(p,target,1.5f,multiplier);effect(target,MobEffects.GLOWING,duration,0);effect(target,MobEffects.WEAKNESS,duration,1);}
            case PERSISTENT_HEX -> {impact(p,target,2.5f,multiplier);effect(target,MobEffects.WITHER,duration+120,0);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration,1);}
            case MISFORTUNE -> {impact(p,target,2.0f,multiplier);effect(target,MobEffects.BLINDNESS,Math.min(duration,100),0);effect(target,MobEffects.WEAKNESS,duration,1);}
            case CURSE_PULSE -> {for(LivingEntity e:nearEnemies(p,6)){impact(p,e,2.5f,multiplier);effect(e,MobEffects.WITHER,duration,0);effect(e,MobEffects.WEAKNESS,duration,1);OtherMagicMechanics.curse(e,p,mastery,rank);}}
            case RUINOUS_CURSE -> {impact(p,target,5.0f,multiplier);effect(target,MobEffects.WITHER,duration+200,2);effect(target,MobEffects.WEAKNESS,duration+200,1);}
            // PROTECTION: player-safe support magic.
            case SMOKE_WARD -> effect(p,MobEffects.DAMAGE_RESISTANCE,duration,0);
            case GUARDIAN_VEIL -> {effect(p,MobEffects.ABSORPTION,duration,0);OtherMagicMechanics.raiseBarrier(level,p,mastery,focused,false);}
            case SANCTUARY -> {for(Player friend:nearPlayers(p,5)){effect(friend,MobEffects.DAMAGE_RESISTANCE,duration,0);SmokeVisuals.aidRecipient(level,p,friend,skill);}effect(p,MobEffects.DAMAGE_RESISTANCE,duration,1);OtherMagicMechanics.raiseBarrier(level,p,mastery,focused,true);}
            case GUARD_ALLY -> effect(ally,MobEffects.DAMAGE_RESISTANCE,duration,0);
            case HARDENING_SHELL -> {effect(p,MobEffects.DAMAGE_RESISTANCE,duration,1);effect(p,MobEffects.ABSORPTION,duration,1);}
            case SMOKE_BULWARK -> {for(Player friend:nearPlayers(p,7)){effect(friend,MobEffects.DAMAGE_RESISTANCE,duration,0);effect(friend,MobEffects.ABSORPTION,duration,0);SmokeVisuals.aidRecipient(level,p,friend,skill);}OtherMagicMechanics.raiseBarrier(level,p,mastery,focused,true);}
            case PURGE_BARRIER -> {effect(p,MobEffects.DAMAGE_RESISTANCE,duration+100,1);effect(p,MobEffects.ABSORPTION,duration,0);}
            case LAST_STAND -> {for(Player friend:nearPlayers(p,6)){effect(friend,MobEffects.DAMAGE_RESISTANCE,duration,1);effect(friend,MobEffects.REGENERATION,duration,1);SmokeVisuals.aidRecipient(level,p,friend,skill);}OtherMagicMechanics.raiseBarrier(level,p,mastery,focused,true);}
            // TIME: all fields and bonuses are strictly temporary; neither
            // mastery nor focused mode creates permanent temporal effects.
            // Canonical time travel is reserved for future research.
            case FLEETING_STEP -> effect(p,MobEffects.MOVEMENT_SPEED,duration,1);
            case DILATED_MOMENT -> {impact(p,target,1.5f,multiplier);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration,2);OtherMagicMechanics.createTimeField(level,p,target.position(),mastery,focused);}
            case TEMPORAL_WINDOW -> {effect(p,MobEffects.MOVEMENT_SPEED,duration,2);effect(p,MobEffects.DAMAGE_RESISTANCE,duration,0);}
            case TEMPORAL_AWARENESS -> {effect(p,MobEffects.NIGHT_VISION,400,0);effect(p,MobEffects.MOVEMENT_SPEED,duration,0);}
            case HASTENED_HANDS -> {effect(p,MobEffects.DIG_SPEED,duration,2);effect(p,MobEffects.MOVEMENT_SPEED,duration,1);}
            case SLOWED_MOMENT -> {impact(p,target,2.5f,multiplier);effect(target,MobEffects.MOVEMENT_SLOWDOWN,duration+100,4);effect(target,MobEffects.DIG_SLOWDOWN,duration,1);OtherMagicMechanics.createTimeField(level,p,target.position(),mastery,focused);}
            case TIME_SHELTER -> {effect(p,MobEffects.DAMAGE_RESISTANCE,duration+100,1);effect(p,MobEffects.ABSORPTION,duration,0);}
            case CHRONO_BURST -> {effect(p,MobEffects.MOVEMENT_SPEED,duration+100,2);for(LivingEntity e:nearEnemies(p,7)){impact(p,e,3.5f,multiplier);effect(e,MobEffects.MOVEMENT_SLOWDOWN,duration,2);}OtherMagicMechanics.createTimeField(level,p,p.position(),mastery,focused);}
            // DESTRUCTION: actual crater-forming spells at the appropriate mastery.
            // No special exemption for the caster's home or a PvP opponent's build.
            // At low mastery (or when disabled in server config), attacks still
            // damage entities and knock them back but do not break structures.
            case SMOKE_SHOT -> impact(p,target,3.5f,multiplier);
            case DETONATION -> {
                Vec3 blast = p.position().add(0, 1.1, 0).add(p.getLookAngle().scale(3));
                if (!OtherMagicMechanics.destructiveBlast(level,p,mastery,focused,blast,150,1.35f)) {
                    for(LivingEntity e:nearEnemies(p,4)){impact(p,e,4.5f,multiplier);pushAway(p,e,1.0);}
                }
            }
            case SHOCKWAVE -> {
                Vec3 blast = p.position().add(0, 1.1, 0).add(p.getLookAngle().scale(3));
                if (!OtherMagicMechanics.destructiveBlast(level,p,mastery,focused,blast,450,2.35f)) {
                    for(LivingEntity e:nearEnemies(p,6)){impact(p,e,7.0f,multiplier);pushAway(p,e,1.2);}
                }
            }
            case BREACH_BOLT -> {
                // A precise targeted breach is available to experienced users.
                Vec3 impact = target != null ? target.position() : aimedImpact(p,14);
                if (!OtherMagicMechanics.destructiveBlast(level,p,mastery,focused,impact,350,1.1f)) {
                    if (target == null) return false;
                    impact(p,target,5.5f,multiplier);
                }
            }
            case RUPTURE_ARMOR -> {impact(p,target,3.5f,multiplier);effect(target,MobEffects.WEAKNESS,duration,2);}
            case CONCUSSIVE_WAVE -> {
                Vec3 blast = p.position().add(0, 1.1, 0).add(p.getLookAngle().scale(3));
                if (!OtherMagicMechanics.destructiveBlast(level,p,mastery,focused,blast,300,1.75f)) {
                    for(LivingEntity e:nearEnemies(p,5)){impact(p,e,5.0f,multiplier);pushAway(p,e,1.4);}
                }
            }
            case SHRAPNEL_SMOKE -> {impact(p,target,6.0f,multiplier);for(LivingEntity e:nearEnemies(p,5))if(e!=target)impact(p,e,2.5f,multiplier);}
            case ANNIHILATION_STREAM -> {
                Vec3 impact = target != null ? target.position() : aimedImpact(p,14);
                if (!OtherMagicMechanics.destructiveBlast(level,p,mastery,focused,impact,650,3.0f)) {
                    if (target == null) return false;
                    impact(p,target,15.0f,multiplier);pushAway(p,target,2.0);
                }
            }
        }
        // Persistent, server-owned consequences beyond vanilla one-off debuffs.
        // Permitted PvP opponents are eligible, but the local player is not.
        if (target != null && skill.magic() == MagicType.CURSE) {
            OtherMagicMechanics.curse(target, p, mastery, rank);
        }
        // Every completed cast has a visible family-specific shape, including
        // self-buffs and healing, so effects are never status-only/invisible.
        SmokeVisuals.cast(level,p,skill,target,ally,focused,mastery);
        return true;
    }
    /** Find a living player under the crosshair. Healing never affects hostile entities. */
    private static LivingEntity selectAlly(ServerPlayer p,double range){
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        return p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(range),
            e->e!=p&&e.isAlive()&&p.hasLineOfSight(e)&&
            eye.distanceToSqr(e.getEyePosition())<=range*range&&
            look.dot(e.getEyePosition().subtract(eye).normalize())>0.97)
            .stream().min(Comparator.comparingDouble(e->eye.distanceToSqr(e.getEyePosition()))).orElse(null);
    }
    private static List<Player> nearPlayers(ServerPlayer p,double radius){
        return p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(radius),
            e->e.isAlive()&&e.distanceToSqr(p)<=radius*radius);
    }
    private static void cleanse(LivingEntity living){
        living.removeEffect(MobEffects.POISON);
        living.removeEffect(MobEffects.WITHER);
        // Ordinary detoxification cannot cure an active Dismemberment wound.
        // Only explicit Healing injury-repair techniques may remove its marker.
        if (!OtherMagicMechanics.hasActiveDismemberment(living)) {
            living.removeEffect(MobEffects.WEAKNESS);
            living.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }
    private static void pushAway(ServerPlayer player,LivingEntity enemy,double scale){
        Vec3 delta=enemy.position().subtract(player.position());
        if(delta.lengthSqr()>.001){Vec3 v=delta.normalize().scale(scale);enemy.push(v.x,0.25,v.z);}
    }
    private static List<LivingEntity> nearEnemies(ServerPlayer p,double radius){
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),
            e->e!=p&&(!(e instanceof Player opponent)||p.canHarmPlayer(opponent))&&e.isAlive()&&e.distanceToSqr(p)<=radius*radius);
    }
}
