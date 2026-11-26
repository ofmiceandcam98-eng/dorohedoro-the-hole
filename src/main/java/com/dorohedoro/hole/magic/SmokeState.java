package com.dorohedoro.hole.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Server-owned, persistent Sorcerer progression. Slots are nine presets of three abilities each. */
public record SmokeState(boolean initialized,int smoke,int capacity,int rank,int experience,int preset,
                         List<String> slots,Map<String,Integer> study,Map<String,Integer> mastery,Map<String,Long> cooldowns) {
    public static final int PRESETS=9, SLOTS=3;
    private static final int LEGACY_SLOTS=8;
    public static final SmokeState EMPTY=new SmokeState(false,0,100,1,0,0,blankSlots(),Map.of(),Map.of(),Map.of());
    public static final Codec<SmokeState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.fieldOf("initialized").forGetter(SmokeState::initialized),
        Codec.INT.fieldOf("smoke").forGetter(SmokeState::smoke),
        Codec.INT.fieldOf("capacity").forGetter(SmokeState::capacity),
        Codec.INT.fieldOf("rank").forGetter(SmokeState::rank),
        Codec.INT.fieldOf("experience").forGetter(SmokeState::experience),
        Codec.INT.fieldOf("preset").forGetter(SmokeState::preset),
        Codec.STRING.listOf().fieldOf("slots").forGetter(SmokeState::slots),
        Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("study").forGetter(SmokeState::study),
        Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("mastery").forGetter(SmokeState::mastery),
        Codec.unboundedMap(Codec.STRING,Codec.LONG).optionalFieldOf("cooldowns",Map.of()).forGetter(SmokeState::cooldowns)
    ).apply(i,SmokeState::new));
    public SmokeState {
        slots=List.copyOf(slots);
        study=Map.copyOf(study);
        mastery=Map.copyOf(mastery);
        cooldowns=Map.copyOf(cooldowns);
        // Pass 19.7 stored 9 presets x 8 slots. Retain the first three from
        // EACH preset, rather than flattening indices and scrambling presets.
        // Learned skills/mastery are independent of equipped slots and are preserved.
        if (slots.size() == PRESETS * LEGACY_SLOTS) {
            List<String> migrated = new ArrayList<>(blankSlots());
            for (int p = 0; p < PRESETS; p++) {
                for (int slot = 0; slot < SLOTS; slot++) {
                    migrated.set(p * SLOTS + slot, slots.get(p * LEGACY_SLOTS + slot));
                }
            }
            slots = List.copyOf(migrated);
        } else if (slots.size() != PRESETS * SLOTS) {
            List<String> fixed = new ArrayList<>(blankSlots());
            for (int n = 0; n < Math.min(fixed.size(), slots.size()); n++) fixed.set(n, slots.get(n));
            slots = List.copyOf(fixed);
        }
        preset = Math.clamp(preset, 0, PRESETS - 1);
    }
    public static List<String> blankSlots(){return Collections.nCopies(PRESETS*SLOTS,"-");}
    public SmokeState withSmoke(int n){return new SmokeState(initialized,Math.clamp(n,0,capacity),capacity,rank,experience,preset,slots,study,mastery,cooldowns);}
    public SmokeState withPreset(int p){return new SmokeState(initialized,smoke,capacity,rank,experience,Math.clamp(p,0,PRESETS-1),slots,study,mastery,cooldowns);}
    public SmokeState withSlot(int slot,String skill){
        List<String> copy=new ArrayList<>(slots);
        copy.set(preset*SLOTS+slot,skill);
        return new SmokeState(initialized,smoke,capacity,rank,experience,preset,copy,study,mastery,cooldowns);
    }
    public SmokeState withStudy(String skill,int amount){
        Map<String,Integer> copy=new HashMap<>(study);copy.put(skill,Math.clamp(amount,0,100));
        return new SmokeState(initialized,smoke,capacity,rank,experience,preset,slots,copy,mastery,cooldowns);
    }
    public SmokeState withMastery(String skill,int amount){
        Map<String,Integer> copy=new HashMap<>(mastery);copy.put(skill,Math.clamp(amount,0,1000));
        return new SmokeState(initialized,smoke,capacity,rank,experience,preset,slots,study,copy,cooldowns);
    }
    public SmokeState gainExperience(int amount){
        int x=experience+Math.max(0,amount),r=rank;
        while(r<50 && x>=100*r){x-=100*r;r++;}
        int cap=100+Math.min(49,r-1)*12;
        return new SmokeState(initialized,Math.min(smoke,cap),cap,r,x,preset,slots,study,mastery,cooldowns);
    }
    /** Cooldowns are saved with the character, so disconnects and dimension changes cannot reset them. */
    public SmokeState withCooldown(String skill,long readyAt){
        Map<String,Long> updated=new HashMap<>(cooldowns);updated.put(skill,readyAt);
        return new SmokeState(initialized,smoke,capacity,rank,experience,preset,slots,study,mastery,updated);
    }
    public String equipped(int slot){
        int at=preset*SLOTS+slot;
        return slot>=0&&slot<SLOTS&&at<slots.size()?slots.get(at):"-";
    }
    public boolean learned(SmokeSkill skill){return skill.rankRequired()==1 || study.getOrDefault(skill.id(),0)>=100;}
    public static SmokeState initial(SmokeSkill basic){
        List<String> list=new ArrayList<>(blankSlots());list.set(0,basic.id());
        return new SmokeState(true,100,100,1,0,0,list,Map.of(basic.id(),100),Map.of(),Map.of());
    }
}
