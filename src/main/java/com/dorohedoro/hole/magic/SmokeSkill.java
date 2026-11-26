package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.character.MagicType;
import java.util.Arrays;

/** Original Dorohedoro-specific techniques, without Tensura implementation or assets. */
public enum SmokeSkill {
    SPORE_PUFF(MagicType.MUSHROOM,"Spore Puff",1,9,35,"Direct spore damage, poison and physical small-mushroom growth"),
    SPORE_CLOUD(MagicType.MUSHROOM,"Spore Cloud",3,18,100,"Area spore damage and poison with actual mushroom growth"),
    FUNGAL_BIND(MagicType.MUSHROOM,"Fungal Bind",6,24,135,"Direct fungal damage and immobilization in spreading mycelium"),

    SCALE_SKIN(MagicType.REPTILE,"Scale Skin",1,9,80,"Reptilian armor protects your body"),
    PREDATOR_SPRINT(MagicType.REPTILE,"Predator Sprint",3,15,100,"Increase speed and jumping ability"),
    REPTILE_RAGE(MagicType.REPTILE,"Reptile Rage",6,24,180,"Combine reptilian strength and resistance"),

    SEVERING_TOUCH(MagicType.DISMEMBERMENT,"Severing Touch",1,11,45,"Melee damage and an arm injury that weakens attacks"),
    CLEAN_CUT(MagicType.DISMEMBERMENT,"Clean Cut",3,18,90,"Cutting damage and leg injury; from 350 mastery, rare chance to sever a leg"),
    DISMEMBER_STRIKE(MagicType.DISMEMBERMENT,"Dismember Strike",6,29,160,"Heavy arm damage; from 400 mastery, can sever an arm permanently"),

    MEND_FLESH(MagicType.HEALING,"Mend Flesh",1,10,70,"Self-heal; treat injuries at 600 mastery, regenerate limbs at 800"),
    PURIFY_BLOOD(MagicType.HEALING,"Purify Blood",3,18,100,"Cleanse yourself; at 400 mastery repairs Dismemberment injuries"),
    RAPID_REGEN(MagicType.HEALING,"Rapid Regeneration",6,30,190,"Temporary regeneration; treat injuries at 600 mastery, limbs at 850"),

    CURSE_MARK(MagicType.CURSE,"Curse Mark",1,12,65,"Direct curse damage with temporary slowing and weakness"),
    HEX_BURDEN(MagicType.CURSE,"Hex Burden",3,19,110,"Direct curse damage plus withering and weakness"),
    WITHER_SEAL(MagicType.CURSE,"Wither Seal",6,29,180,"Direct curse damage and lasting wither effects"),

    SMOKE_WARD(MagicType.PROTECTION,"Smoke Ward",1,10,80,"Grant brief damage resistance"),
    GUARDIAN_VEIL(MagicType.PROTECTION,"Guardian Veil",3,18,115,"Create collision-enabled condensed smoke while gaining absorption"),
    SANCTUARY(MagicType.PROTECTION,"Sanctuary",6,29,180,"Protect nearby players and raise a tangible smoke barricade"),

    FLEETING_STEP(MagicType.TIME,"Fleeting Step",1,12,100,"Brief accelerated movement"),
    DILATED_MOMENT(MagicType.TIME,"Dilated Moment",3,23,150,"Temporal impact damage and a temporary enemy-slowing field"),
    TEMPORAL_WINDOW(MagicType.TIME,"Temporal Window",6,36,260,"Short localized acceleration and resistance"),

    SMOKE_SHOT(MagicType.DESTRUCTION,"Smoke Shot",1,10,40,"Direct projectile-like smoke damage"),
    DETONATION(MagicType.DESTRUCTION,"Detonation",3,20,100,"Direct blast damage; mastery 150+ can demolish structures"),
    SHOCKWAVE(MagicType.DESTRUCTION,"Shockwave",6,31,170,"Heavy area blast damage; mastery 450+ shatters buildings"),
    MYCELIAL_SENSE(MagicType.MUSHROOM,"Mycelial Sense",2,12,100,"Detect movement in dark fungal surroundings"),
    SPORE_PURGE(MagicType.MUSHROOM,"Spore Purge",5,20,130,"Use regenerative spores to cleanse yourself"),
    FUNGAL_BLOOM(MagicType.MUSHROOM,"Fungal Bloom",7,25,140,"Area damage and poison beneath a growing giant mushroom"),
    MYCELIUM_GUARD(MagicType.MUSHROOM,"Mycelium Guard",10,28,170,"Coat your body in protective mycelium"),
    SPORE_DOMINION(MagicType.MUSHROOM,"Spore Dominion",18,48,280,"Strong area damage and poison with solid giant mushroom growth"),
    SHED_SKIN(MagicType.REPTILE,"Shed Skin",2,13,120,"Shed skin and cleanse poison; at 650 mastery accelerate severed-limb regrowth"),
    WALL_CRAWL(MagicType.REPTILE,"Wall Crawl",5,18,150,"Scale real walls while active, leap higher and descend safely"),
    TAIL_LASH(MagicType.REPTILE,"Tail Lash",7,22,100,"Direct reptilian melee damage and knockback"),
    CHAMELEON_HUNT(MagicType.REPTILE,"Chameleon Hunt",11,26,190,"Brief camouflage and hunting speed"),
    APEX_FORM(MagicType.REPTILE,"Apex Form",19,51,320,"Alter combat anatomy and size; at 850+ biological mastery limbs slowly regrow"),
    PRECISION_NERVE(MagicType.DISMEMBERMENT,"Precision Nerve",2,14,90,"Precise direct damage and an arm coordination injury"),
    LIMB_LOCK(MagicType.DISMEMBERMENT,"Limb Lock",5,22,130,"Direct damage and a leg injury that limits movement"),
    DISARMING_SMOKE(MagicType.DISMEMBERMENT,"Disarming Smoke",8,27,160,"Direct damage and an arm injury impairing attack speed"),
    SEVERING_WAVE(MagicType.DISMEMBERMENT,"Severing Wave",12,35,200,"Area cutting damage; from 550 mastery, can sever an opponent's leg"),
    ANATOMY_REND(MagicType.DISMEMBERMENT,"Anatomy Rend",20,56,320,"Devastating damage; from 600 mastery, may sever an arm or leg"),
    MEND_ALLY(MagicType.HEALING,"Mend Ally",2,13,70,"Heal a player; injuries at 350 mastery, severed limbs at 750"),
    CLEANSE_ALLY(MagicType.HEALING,"Cleanse Ally",5,19,120,"Cleanse a player; treat wounds, regrow limbs at 900 mastery"),
    RESTORATIVE_FIELD(MagicType.HEALING,"Restorative Field",8,31,170,"Area heal; treat injuries at 500 mastery, regrow limbs at 850"),
    VITAL_SHELL(MagicType.HEALING,"Vital Shell",12,28,190,"Grant an ally or yourself temporary absorption"),
    LIFEWEAVE(MagicType.HEALING,"Lifeweave",20,55,320,"Major group healing; at 650 mastery regrow missing limbs"),
    MARK_OF_DREAD(MagicType.CURSE,"Mark of Dread",2,14,100,"Direct curse damage, visibility marking and weakness"),
    PERSISTENT_HEX(MagicType.CURSE,"Persistent Hex",5,24,150,"Direct curse damage and lingering withering debilitation"),
    MISFORTUNE(MagicType.CURSE,"Misfortune",8,27,150,"Direct curse damage, temporary blindness and weakness"),
    CURSE_PULSE(MagicType.CURSE,"Curse Pulse",12,36,220,"Area curse damage with wither and a lingering hostile mark"),
    RUINOUS_CURSE(MagicType.CURSE,"Ruinous Curse",20,54,320,"Powerful direct curse damage plus a debilitating wither"),
    GUARD_ALLY(MagicType.PROTECTION,"Guard Ally",2,14,80,"Shield an ally or yourself"),
    HARDENING_SHELL(MagicType.PROTECTION,"Hardening Shell",5,19,135,"Gain absorption and resistance"),
    SMOKE_BULWARK(MagicType.PROTECTION,"Smoke Bulwark",8,30,190,"Protect nearby allies and raise tangible condensed smoke"),
    PURGE_BARRIER(MagicType.PROTECTION,"Purge Barrier",11,25,170,"Briefly strengthen defenses with resistance and a smoke shell"),
    LAST_STAND(MagicType.PROTECTION,"Last Stand",19,52,320,"Restore team defenses and raise a solid smoke barrier"),
    TEMPORAL_AWARENESS(MagicType.TIME,"Temporal Awareness",2,12,120,"Heightened perception and speed"),
    HASTENED_HANDS(MagicType.TIME,"Hastened Hands",5,19,170,"Accelerate movement and mining for a short time"),
    SLOWED_MOMENT(MagicType.TIME,"Slowed Moment",8,28,185,"Temporal impact damage and stronger temporary slowing"),
    TIME_SHELTER(MagicType.TIME,"Time Shelter",12,32,230,"Temporary defensive temporal pocket"),
    CHRONO_BURST(MagicType.TIME,"Chrono Burst",20,56,360,"Area temporal burst damage, speed and a temporary slowing field"),
    BREACH_BOLT(MagicType.DESTRUCTION,"Breach Bolt",2,14,75,"Focused direct damage; mastery 350+ breaches structures"),
    RUPTURE_ARMOR(MagicType.DESTRUCTION,"Rupture Armor",5,22,130,"Direct damage plus armor-breaking combat debilitation"),
    CONCUSSIVE_WAVE(MagicType.DESTRUCTION,"Concussive Wave",8,31,160,"Area blast damage and knockback; mastery 300+ damages structures"),
    SHRAPNEL_SMOKE(MagicType.DESTRUCTION,"Shrapnel Smoke",12,39,180,"Direct strike damage with lower-damage splash on nearby enemies"),
    ANNIHILATION_STREAM(MagicType.DESTRUCTION,"Annihilation Stream",20,58,350,"Catastrophic direct damage; mastery 650+ destroys terrain");

    private final MagicType magic;
    private final String display;
    private final int rankRequired, smokeCost, cooldownTicks;
    private final String description;
    SmokeSkill(MagicType magic,String display,int rankRequired,int smokeCost,int cooldownTicks,String description){
        this.magic=magic; this.display=display; this.rankRequired=rankRequired;
        this.smokeCost=smokeCost; this.cooldownTicks=cooldownTicks; this.description=description;
    }
    public MagicType magic(){return magic;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public String display(){return display;}
    public int rankRequired(){return rankRequired;}
    public int smokeCost(){return smokeCost;}
    public int cooldownTicks(){return cooldownTicks;}
    public String description(){return description;}
    public static SmokeSkill fromId(String id){return Arrays.stream(values()).filter(s->s.id().equals(id)).findFirst().orElse(null);}
    public static SmokeSkill starter(MagicType magic){
        return Arrays.stream(values()).filter(s->s.magic==magic && s.rankRequired==1).findFirst().orElse(null);
    }
}
