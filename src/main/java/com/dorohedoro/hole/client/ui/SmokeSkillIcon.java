package com.dorohedoro.hole.client.ui;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.magic.SmokeSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.EnumMap;

/** Original 24px nearest-neighbor technique PNGs. Purely client-side rendering. */
public final class SmokeSkillIcon {
    private static final EnumMap<SmokeSkill, ResourceLocation> ICONS = new EnumMap<>(SmokeSkill.class);
    static {
        for(SmokeSkill skill:SmokeSkill.values())
            ICONS.put(skill,ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,
                "textures/gui/techniques/"+skill.id()+".png"));
    }
    private SmokeSkillIcon() {}
    public static void draw(GuiGraphics graphics,SmokeSkill skill,int x,int y,int size){
        if(skill==null)return;
        graphics.blit(ICONS.get(skill),x,y,size,size,0f,0f,24,24,24,24);
    }
}
