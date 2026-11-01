package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.magic.SmokeSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Arrays;

/** Original industrial ledger with status, techniques, practice and nine three-slot loadouts. */
public final class SmokeLedgerScreen extends Screen {
    private enum Tab { STATUS, TECHNIQUES, LOADOUT }
    private Tab tab=Tab.STATUS;
    private int chosenSlot=0;
    private int skillPage=0;
    private String selectedSkill="";
    private int x,y,w=540;
    public SmokeLedgerScreen(){super(Component.literal("Smoke Ledger"));}
    @Override protected void init(){w=Math.min(560,width-24);x=(width-w)/2;y=Math.max(8,(height-325)/2);rebuild();}
    private void add(String label,int atX,int atY,int width,int height,Runnable action){
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(atX,atY,width,height).build());
    }
    public void refresh(){if(minecraft!=null)rebuild();}
    private void rebuild(){
        clearWidgets();int start=x+18;
        add("STATUS",start,y+40,96,22,()->{tab=Tab.STATUS;rebuild();});
        add("TECHNIQUES",start+105,y+40,116,22,()->{tab=Tab.TECHNIQUES;rebuild();});
        add("LOADOUT",start+230,y+40,100,22,()->{tab=Tab.LOADOUT;rebuild();});
        add("CLOSE",x+w-90,y+40,70,22,()->onClose());
        if("none".equals(SmokeClient.innate))return;
        MagicType family=MagicType.fromId(SmokeClient.innate);
        if(family==null)return;
        if(tab==Tab.TECHNIQUES){
            SmokeSkill[] skills=Arrays.stream(SmokeSkill.values()).filter(a->a.magic()==family).toArray(SmokeSkill[]::new);
            int pageCount=Math.max(1,(skills.length+2)/3);
            skillPage=Math.clamp(skillPage,0,pageCount-1);
            for(int i=skillPage*3;i<Math.min(skills.length,(skillPage+1)*3);i++){
                SmokeSkill skill=skills[i];int yy=y+92+(i%3)*62;
                add(skill.display(),start,yy,160,25,()->{selectedSkill=skill.id();rebuild();});
                boolean known=skill.rankRequired()==1||SmokeClient.study(skill.id())>=100;
                if(!known&&SmokeClient.rank>=skill.rankRequired())
                    add("PRACTICE",start+170,yy,112,25,()->SmokeClient.request("train",skill.id()));
                if(known)add("EQUIP SLOT "+(chosenSlot+1),start+170,yy,132,25,()->SmokeClient.request("equip",chosenSlot+":"+skill.id()));
            }
            add("< PREV",start,y+274,85,23,()->{skillPage=Math.max(0,skillPage-1);rebuild();});
            add("NEXT >",start+94,y+274,85,23,()->{skillPage=Math.min(pageCount-1,skillPage+1);rebuild();});
        } else if(tab==Tab.LOADOUT){
            add("< PRESET",start,y+90,110,26,()->SmokeClient.request("preset",""+Math.floorMod(SmokeClient.preset-1,9)));
            add("PRESET >",start+120,y+90,110,26,()->SmokeClient.request("preset",""+((SmokeClient.preset+1)%9)));
            for(int i=0;i<3;i++){
                int index=i, xx=start+i*165, yy=y+140;
                add((chosenSlot==i?"> ":"")+"SLOT "+(i+1),xx,yy,154,25,()->{chosenSlot=index;rebuild();});
                if(chosenSlot==i)add("CLEAR",xx,yy+27,154,20,()->SmokeClient.request("equip",index+":-"));
            }
            add("EQUIP / PRACTICE",start,y+266,180,25,()->{tab=Tab.TECHNIQUES;rebuild();});
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float tick){
        g.fill(0,0,width,height,0xB900080A);
        g.fill(x-3,y-3,x+w+3,y+330,0xFF4D6F6F);
        g.fill(x,y,x+w,y+327,0xFF192224);
        g.fill(x+1,y+1,x+w-1,y+31,0xFF2B3F40);
        g.drawString(font,"THE HOLE  /  SMOKE LEDGER",x+18,y+10,0xFFE1D7BF);
        if("none".equals(SmokeClient.innate)){
            g.drawString(font,"Select a Sorcerer origin to access smoke magic.",x+20,y+100,0xFFCDD7D3);
        }else if(tab==Tab.STATUS){
            g.drawString(font,"INNATE MAGIC: "+SmokeClient.innate.toUpperCase(),x+22,y+92,0xFF88D7CF);
            g.drawString(font,"SMOKE: "+SmokeClient.smoke+" / "+SmokeClient.maxSmoke,x+22,y+120,0xFFE6DAC5);
            g.drawString(font,"SMOKE RANK: "+SmokeClient.rank,x+22,y+148,0xFFE6DAC5);
            g.drawString(font,"RANK XP: "+SmokeClient.xp+" / "+(SmokeClient.rank*100),x+22,y+176,0xFFE6DAC5);
            g.drawString(font,"CURRENT PRESET: "+(SmokeClient.preset+1)+" / 9",x+22,y+204,0xFFE6DAC5);
            g.drawString(font,"Z X C: cast  |  ALT + key: focus that slot",x+22,y+241,0xFFA3C8C6);
            g.drawString(font,"R: next loadout  |  SHIFT+R: previous  |  B: ledger",x+22,y+260,0xFFA3C8C6);
            g.drawString(font,"One innate magic type. 3 slots per preset.",x+22,y+279,0xFFAC9C8D);
            if ("mushroom".equals(SmokeClient.innate))
                g.drawString(font,"Mushroom permanence: small 300 / giant 600 skill mastery",x+22,y+299,0xFFB6C9A2);
        }else if(tab==Tab.TECHNIQUES){
            MagicType family=MagicType.fromId(SmokeClient.innate);
            if(family!=null){
                SmokeSkill[] skills=Arrays.stream(SmokeSkill.values()).filter(a->a.magic()==family).toArray(SmokeSkill[]::new);
                for(int i=skillPage*3;i<Math.min(skills.length,(skillPage+1)*3);i++){
                    SmokeSkill s=skills[i];int yy=y+119+(i%3)*62;
                    g.drawString(font,"Rank "+s.rankRequired()+" | Study "+SmokeClient.study(s.id())+"/100 | Mastery "+SmokeClient.mastery(s.id())+"/1000",x+19,yy,0xFF9DAFAF);
                    g.drawString(font,s.description(),x+19,yy+11,0xFFCAD3CF);
                }
                g.drawString(font,"PAGE "+(skillPage+1)+" / "+Math.max(1,(skills.length+2)/3)+"  |  Selected slot: "+(chosenSlot+1),x+195,y+280,0xFF9DD9D4);
            }
        }else{
            g.drawString(font,"LOADOUT "+(SmokeClient.preset+1)+" / 9  |  3 ABILITY SLOTS",x+269,y+97,0xFF91D8CE);
            for(int i=0;i<3;i++){
                SmokeSkill s=SmokeSkill.fromId(SmokeClient.slot(i));
                String label=s==null?"(empty)":s.display();
                g.drawString(font,label,x+20+i*165,y+169,0xFFDBE6E3);
            }
        }
        super.render(g,mx,my,tick);
    }
}
