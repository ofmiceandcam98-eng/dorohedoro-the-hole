package com.dorohedoro.hole.client.character;

import com.dorohedoro.hole.character.CharacterNetwork;
import com.dorohedoro.hole.character.PersonalityQuiz;
import com.dorohedoro.hole.client.ui.HoleUiTheme;
import com.dorohedoro.hole.client.ui.HoleMenuButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Arrays;

/** Sorcerer -> quiz OR optional fate; highest-score ties allow restricted choice. */
public final class CharacterCreationScreen extends Screen {
    private enum Stage { ORIGIN, MODE, QUIZ, SUBMITTING, TIE, COMPLETE }
    private Stage stage=Stage.ORIGIN;
    private final int[] order;
    private final int[] answers=new int[PersonalityQuiz.QUESTIONS_PER_RUN];
    private int page=0,panelX,panelY,panelW,panelH;
    private String error="",completion="",origin="";
    private String[] tied=new String[0];

    private CharacterCreationScreen(int[] questions){
        super(Component.literal("The Hole - Character Origin"));
        order=questions;Arrays.fill(answers,-1);
    }
    public static void onServerOpen(String reason,String encodedOrder){
        int[] questions=PersonalityQuiz.parseOrder(encodedOrder);
        if(questions==null)return;
        Minecraft.getInstance().setScreen(new CharacterCreationScreen(questions));
    }
    public static void onServerDecision(String result,String value){
        Minecraft mc=Minecraft.getInstance();
        if(!(mc.screen instanceof CharacterCreationScreen screen))return;
        if("error".equals(result)){
            screen.error=value;
            screen.stage=screen.tied.length>0?Stage.TIE:Stage.MODE;
            screen.rebuild();return;
        }
        screen.error="";
        if("tie".equals(result)){
            screen.tied=value.split(",");
            screen.stage=Stage.TIE;screen.rebuild();return;
        }
        screen.stage=Stage.COMPLETE;
        screen.completion="human".equals(result)?"Human origin confirmed. Your story begins in The Hole.":
            "Your innate smoke has manifested: "+value+". This result is permanent.";
        screen.rebuild();
    }
    @Override protected void init(){
        panelW=Math.min(600,width-24);panelX=(width-panelW)/2;
        panelH=Math.min(335,height-12);panelY=(height-panelH)/2;rebuild();
    }
    private void add(String label,int x,int y,int w,int h,Runnable action){
        addRenderableWidget(new HoleMenuButton(label,x,y,w,h,action));
    }
    private void rebuild(){
        if(minecraft==null)return;
        clearWidgets();int x=panelX+18,w=panelW-36,top=panelY;
        switch(stage){
            case ORIGIN -> {
                int half=(w-12)/2;
                add("HUMAN - Survival & skill",x,top+75,half,36,()->{
                    origin="human";stage=Stage.SUBMITTING;rebuild();
                    PacketDistributor.sendToServer(new CharacterNetwork.Submit("human",""));});
                add("SORCERER - Innate smoke",x+half+12,top+75,half,36,()->{
                    origin="sorcerer";stage=Stage.MODE;rebuild();});
                // Devils remain a later-game ascension path, not a free starting choice.
            }
            case MODE -> {
                add("Take the Personality Test (8 questions)",x,top+88,w,35,()->{stage=Stage.QUIZ;page=0;rebuild();});
                add("Trust Fate (weighted random magic)",x,top+137,w,35,()->{
                    stage=Stage.SUBMITTING;rebuild();
                    PacketDistributor.sendToServer(new CharacterNetwork.Submit("fate",""));});
                add("Back to origins",x,top+197,w,24,()->{stage=Stage.ORIGIN;rebuild();});
            }
            case QUIZ -> {
                PersonalityQuiz.Question q=PersonalityQuiz.question(order[page]);
                int first=panelH>=318?110:(panelH>=265?91:75);
                int step=panelH>=318?36:(panelH>=265?32:25);
                for(int i=0;i<4;i++){
                    final int opt=i;
                    String mark=answers[page]==i?"[X] ":"[ ] ";
                    add(mark+q.options()[i].text(),x,top+first+i*step,w,step-4,()->{answers[page]=opt;rebuild();});
                }
                add("Back",x,top+panelH-47,82,25,()->{
                    if(page==0)stage=Stage.MODE;else page--;rebuild();});
                if(answers[page]>=0)
                    add(page==order.length-1?"Discover my magic":"Next",x+w-152,top+panelH-47,152,25,()->{
                        if(page==order.length-1)submitQuiz();else {page++;rebuild();}
                    });
            }
            case TIE -> {
                int count=tied.length;
                for(int i=0;i<count;i++){
                    final String type=tied[i];
                    add("Choose "+type,x,top+80+i*31,w,28,()->{
                        stage=Stage.SUBMITTING;rebuild();
                        PacketDistributor.sendToServer(new CharacterNetwork.Submit("tie",type));
                    });
                }
            }
            case SUBMITTING -> {}
            case COMPLETE -> add("Enter The Hole",x,top+151,w,28,()->Minecraft.getInstance().setScreen(null));
        }
    }
    private void submitQuiz(){
        StringBuilder encoded=new StringBuilder();
        for(int n:answers){if(n<0||n>3)return;encoded.append(n);}
        stage=Stage.SUBMITTING;rebuild();
        PacketDistributor.sendToServer(new CharacterNetwork.Submit("quiz",encoded.toString()));
    }
    @Override public boolean shouldCloseOnEsc(){return false;}
    @Override public void onClose(){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        // Draw the full panel first. Screen.render may apply Minecraft's
        // background blur, so draw all QUESTION TEXT *after* super.render.
        HoleUiTheme.backdrop(g,width,height,panelX,panelY,panelW,panelH);
        if(stage==Stage.QUIZ){
            int first=panelH>=318?110:(panelH>=265?91:75);
            HoleUiTheme.section(g,panelX+14,panelY+51,panelW-28,first-54);
            g.fill(panelX+16,panelY+panelH-15,panelX+panelW-16,panelY+panelH-11,0xFF203536);
            int progress=(panelW-32)*(page+1)/order.length;
            g.fill(panelX+16,panelY+panelH-15,panelX+16+progress,panelY+panelH-11,HoleUiTheme.ACCENT);
        }
        super.render(g,mx,my,partial);
        g.drawCenteredString(font,"THE HOLE / CHARACTER ORIGIN",width/2,panelY+12,HoleUiTheme.TEXT);
        switch(stage){
            case ORIGIN -> {
                g.drawCenteredString(font,"Who are you in this city?",width/2,panelY+46,0xFF9EDDD0);
                g.drawCenteredString(font,"Devilhood is an earned path.",width/2,panelY+150,0xFF9DADAF);
            }
            case MODE -> {
                g.drawCenteredString(font,"How will your innate smoke emerge?",width/2,panelY+52,0xFF9EDDD0);
                g.drawCenteredString(font,"No free selection. No ordinary rerolls.",width/2,panelY+245,0xFF9DADAF);
            }
            case QUIZ -> {
                g.drawString(font,"SORCERER APTITUDE  /  "+(page+1)+" OF "+order.length,panelX+18,panelY+39,HoleUiTheme.ACCENT);
                g.drawWordWrap(font,Component.literal(PersonalityQuiz.question(order[page]).text()),panelX+22,panelY+63,panelW-44,HoleUiTheme.TEXT);
                g.drawCenteredString(font,"Answer instinctively. Your affinity scores remain hidden.",width/2,panelY+panelH-32,HoleUiTheme.MUTED);
            }
            case SUBMITTING -> g.drawCenteredString(font,"Your smoke is taking shape...",width/2,panelY+122,0xFFE2D5C1);
            case TIE -> {
                g.drawCenteredString(font,"YOUR MAGIC AFFINITIES ARE TIED",width/2,panelY+49,0xFF9EDDD0);
                g.drawCenteredString(font,"Choose between your highest scoring types only.",width/2,panelY+64,0xFF9DADAF);
            }
            case COMPLETE -> {
                g.drawWordWrap(font,Component.literal(completion),panelX+22,panelY+104,panelW-44,0xFF9DE1D5);
                g.drawCenteredString(font,"Saved to your character; there are no rerolls.",width/2,panelY+209,0xFFADB9B3);
            }
        }
        if(!error.isEmpty()) g.drawWordWrap(font,Component.literal(error),panelX+18,panelY+panelH-22,panelW-36,0xFFFF9A8A);
    }
}
