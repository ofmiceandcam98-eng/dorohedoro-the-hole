package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.client.ui.HoleUiTheme;
import com.dorohedoro.hole.client.ui.HoleMenuButton;
import com.dorohedoro.hole.client.ui.SmokeSkillIcon;
import com.dorohedoro.hole.magic.SmokeSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Arrays;
import org.lwjgl.glfw.GLFW;

/** Dedicated, readable pop-up menus. The existing server-owned nine loadouts stay intact. */
public final class SmokeLedgerScreen extends Screen {
    private enum Tab { CHARACTER, TECHNIQUES, LOADOUTS, MEDICAL }
    private Tab tab=Tab.TECHNIQUES;
    private int chosenSlot,skillPage;
    private String selectedSkill="";
    private int x,y,w,h;
    public SmokeLedgerScreen(){super(Component.literal("The Hole - Character & Smoke"));}

    @Override protected void init(){
        w=Math.min(620,width-12); h=Math.min(355,height-12);
        x=(width-w)/2;y=(height-h)/2;
        rebuild();
    }
    private void add(String label,int px,int py,int pw,int ph,Runnable action){
        addRenderableWidget(new HoleMenuButton(label,px,py,pw,ph,action));
    }
    public void refresh(){if(minecraft!=null)rebuild();}
    private void rebuild(){
        if(minecraft==null)return;
        clearWidgets();
        boolean narrow=w<420;
        int menuX=x+(narrow?7:14),mainX=x+(narrow?108:151),mainW=w-(narrow?119:169);
        int sidebarWidth=narrow?96:121;
        int perPage=h<260?1:h<320?2:3;
        int spacing=h<260?54:h<320?65:69;
        String[] names={"CHARACTER","TECHNIQUES","LOADOUTS","MEDICAL"};
        Tab[] tabs=Tab.values();
        for(int i=0;i<tabs.length;i++){
            Tab target=tabs[i];
            add((tab==target?"> ":"  ")+names[i],menuX,y+55+i*(h<260?29:33),sidebarWidth,24,()->{tab=target;rebuild();});
        }
        add("CLOSE  [B / ESC]",menuX,y+h-29,sidebarWidth,21,this::onClose);
        if("none".equals(SmokeClient.innate))return;
        MagicType family=MagicType.fromId(SmokeClient.innate);
        if(family==null)return;
        if(tab==Tab.TECHNIQUES){
            SmokeSkill[] skills=Arrays.stream(SmokeSkill.values()).filter(s->s.magic()==family).toArray(SmokeSkill[]::new);
            int pages=Math.max(1,(skills.length+perPage-1)/perPage);
            skillPage=Math.clamp(skillPage,0,pages-1);
            for(int i=skillPage*perPage;i<Math.min(skills.length,(skillPage+1)*perPage);i++){
                SmokeSkill skill=skills[i];int line=i%perPage,yy=y+86+line*spacing;
                int equipWidth=narrow?62:105;
                add(HoleUiTheme.fit(font,skill.display(),Math.max(16,mainW-equipWidth-29)),
                    mainX+26,yy,Math.max(22,mainW-equipWidth-29),23,()->{selectedSkill=skill.id();rebuild();});
                boolean learned=skill.rankRequired()==1||SmokeClient.study(skill.id())>=100;
                if(learned)add(narrow?"EQUIP":"EQUIP ["+(chosenSlot+1)+"]",mainX+mainW-equipWidth,yy,equipWidth,23,
                    ()->SmokeClient.request("equip",chosenSlot+":"+skill.id()));
                else if(SmokeClient.rank>=skill.rankRequired())add(narrow?"STUDY":"PRACTICE",mainX+mainW-equipWidth,yy,equipWidth,23,
                    ()->SmokeClient.request("train",skill.id()));
            }
            int footerY=y+h-31;
            add("<",mainX,footerY,24,21,()->{skillPage=Math.max(0,skillPage-1);rebuild();});
            add(">",mainX+29,footerY,24,21,()->{skillPage=Math.min(pages-1,skillPage+1);rebuild();});
            for(int i=0;i<3;i++){
                final int slot=i;
                add((chosenSlot==i?"[":"")+(i==0?"Z":i==1?"X":"C")+(chosenSlot==i?"]":""),
                    mainX+mainW-105+i*35, narrow?footerY-26:footerY,32,21,
                    ()->{chosenSlot=slot;rebuild();});
            }
        } else if(tab==Tab.LOADOUTS) {
            // Nine actual server-owned presets; selection remains server-authoritative.
            int gap=3,slotWidth=(mainW-8*gap)/9;
            for(int i=0;i<9;i++){
                int index=i;
                add((SmokeClient.preset==i?"[":"")+(i+1)+(SmokeClient.preset==i?"]":""),
                    mainX+i*(slotWidth+gap),y+92,slotWidth,25,
                    ()->SmokeClient.request("preset",Integer.toString(index)));
            }
            for(int i=0;i<3;i++){
                int idx=i,yy=y+(h<320?124:145)+i*(h<320?47:55);
                add((chosenSlot==i?"> ":"")+(narrow?"SLOT "+(i+1):"SLOT "+(i+1)+"  ["+(i==0?"Z":i==1?"X":"C")+"]"),
                    mainX,yy,narrow?Math.max(55,mainW-75):155,25,
                    ()->{chosenSlot=idx;rebuild();});
                add("CLEAR",mainX+mainW-70,yy,70,25,
                    ()->SmokeClient.request("equip",idx+":-"));
            }
            add(narrow?"TECHNIQUES":"BROWSE TECHNIQUES",mainX,y+h-31,Math.min(180,mainW),21,()->{tab=Tab.TECHNIQUES;rebuild();});
        }
    }
    @Override public boolean keyPressed(int key,int scanCode,int modifiers){
        if(key==GLFW.GLFW_KEY_B){onClose();return true;}
        return super.keyPressed(key,scanCode,modifiers);
    }
    @Override public void render(net.minecraft.client.gui.GuiGraphics g,int mx,int my,float partial){
        // Geometry behind widgets; *text after* Screen.render so the vanilla
        // screen background blur can never blur our labels or question text.
        HoleUiTheme.backdrop(g,width,height,x,y,w,h);
        boolean narrow=w<420;
        HoleUiTheme.section(g,x+(narrow?101:141),y+43,w-(narrow?113:153),h-56);
        g.fill(x+(narrow?100:135),y+43,x+(narrow?102:137),y+h-13,0xFF405D5B);
        super.render(g,mx,my,partial);
        g.drawString(font,"THE HOLE   /   CHARACTER RECORD",x+21,y+13,HoleUiTheme.TEXT);
        int cx=x+(narrow?109:154),tw=w-(narrow?124:178);
        int perPage=h<260?1:h<320?2:3;
        int spacing=h<260?54:h<320?65:69;
        if("none".equals(SmokeClient.innate)){
            g.drawWordWrap(font,Component.literal("Human origin. Smoke techniques require a Sorcerer origin; other survival systems remain available."),cx,y+82,tw,HoleUiTheme.MUTED);
            if(tab==Tab.MEDICAL)g.drawWordWrap(font,Component.literal("Current injury: "+SmokeClient.injury),cx,y+129,tw,HoleUiTheme.WARNING);
            return;
        }
        switch(tab){
            case CHARACTER -> {
                g.drawString(font,"SORCERER / "+SmokeClient.innate.toUpperCase(),cx,y+62,HoleUiTheme.ACCENT);
                g.drawString(font,"SMOKE RANK "+SmokeClient.rank+"   /   PRESET "+(SmokeClient.preset+1)+" OF 9",cx,y+85,HoleUiTheme.TEXT);
                HoleUiTheme.meter(g,font,"SMOKE",SmokeClient.smoke,SmokeClient.maxSmoke,cx,y+112,tw-8,HoleUiTheme.ACCENT);
                HoleUiTheme.meter(g,font,"RANK PROGRESS",SmokeClient.xp,Math.max(100,SmokeClient.rank*100),cx,y+152,tw-8,0xFFD3A26E);
                if(h>=300)g.drawWordWrap(font,Component.literal("One innate smoke family. Train techniques and equip up to three per loadout. The server saves your mastery and loadouts."),cx,y+200,tw,HoleUiTheme.MUTED);
            }
            case TECHNIQUES -> {
                g.drawString(font,"KNOWN SKILLS / PRACTICE",cx,y+59,HoleUiTheme.ACCENT);
                MagicType family=MagicType.fromId(SmokeClient.innate);
                if(family!=null){
                    SmokeSkill[] skills=Arrays.stream(SmokeSkill.values()).filter(s->s.magic()==family).toArray(SmokeSkill[]::new);
                    for(int i=skillPage*perPage;i<Math.min(skills.length,(skillPage+1)*perPage);i++){
                        SmokeSkill s=skills[i];int yy=y+115+(i%perPage)*spacing;
                        String progress="Rank "+s.rankRequired()+"  |  Study "+SmokeClient.study(s.id())+"/100  |  Mastery "+SmokeClient.mastery(s.id())+"/1000";
                        SmokeSkillIcon.draw(g,s,cx+2,yy-29,21);
                        g.drawString(font,HoleUiTheme.fit(font,progress,tw),cx,yy,HoleUiTheme.MUTED);
                        g.drawString(font,HoleUiTheme.fit(font,s.description(),tw),cx,yy+12,HoleUiTheme.TEXT);
                    }
                    if(tw>280)g.drawString(font,"PAGE "+(skillPage+1)+" / "+Math.max(1,(skills.length+perPage-1)/perPage),cx+174,y+h-29,HoleUiTheme.MUTED);
                }
            }
            case LOADOUTS -> {
                g.drawString(font,"ACTIVE LOADOUT  "+(SmokeClient.preset+1)+" / 9",cx,y+64,HoleUiTheme.ACCENT);
                for(int i=0;i<3;i++){
                    SmokeSkill skill=SmokeSkill.fromId(SmokeClient.slot(i));
                    String label=skill==null?"Empty":skill.display();
                    if(!narrow){
                        if(skill!=null)SmokeSkillIcon.draw(g,skill,cx+162,y+149+i*55,22);
                        g.drawString(font,HoleUiTheme.fit(font,label,tw-198),cx+190,y+153+i*55,HoleUiTheme.TEXT);
                    }
                }
                g.drawString(font,"Pick a slot, then browse a technique to equip.",cx,y+h-64,HoleUiTheme.MUTED);
            }
            case MEDICAL -> {
                g.drawString(font,"BODY CONDITION",cx,y+63,HoleUiTheme.ACCENT);
                g.drawWordWrap(font,Component.literal("Current diagnosis: "+SmokeClient.injury),cx,y+93,tw,HoleUiTheme.TEXT);
                g.drawWordWrap(font,Component.literal("Treatment and limb reattachment require the applicable actual in-game items or healing techniques. This panel does not grant abilities."),cx,y+149,tw,HoleUiTheme.MUTED);
            }
        }
    }
}
