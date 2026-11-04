package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.magic.SmokeNetwork;
import com.dorohedoro.hole.magic.SmokeSkill;
import com.dorohedoro.hole.client.ui.HoleUiTheme;
import com.dorohedoro.hole.client.ui.SmokeSkillIcon;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Client-only HUD, keybinds and synchronized view. No client authority over magic. */
public final class SmokeClient {
    private SmokeClient(){}
    private static final String CATEGORY="key.categories.dorohedoro";
    private static final KeyMapping LEDGER=new KeyMapping("key.dorohedoro.smoke_ledger",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,CATEGORY);
    private static final int[] DEFAULT={GLFW.GLFW_KEY_Z,GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_C};
    private static final KeyMapping CYCLE=new KeyMapping("key.dorohedoro.smoke_cycle",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,CATEGORY);
    private static final KeyMapping[] KEYS=new KeyMapping[3];
    private static final int[] COOLDOWNS=new int[3];
    private static final String[] EQUIPPED=new String[27];
    private static final Map<String,Integer> STUDY=new HashMap<>(),MASTERY=new HashMap<>();
    public static String innate="none";
    public static int smoke,maxSmoke,rank,xp,preset;
    public static String injury="none";
    private static final boolean[] FOCUSED=new boolean[3];
    private static int uiTicks=0;
    static{for(int i=0;i<3;i++)KEYS[i]=new KeyMapping("key.dorohedoro.smoke_slot_"+(i+1),InputConstants.Type.KEYSYM,DEFAULT[i],CATEGORY);Arrays.fill(EQUIPPED,"-");}
    public static void register(IEventBus modBus){
        modBus.addListener(SmokeClient::registerKeys);
        modBus.addListener(SmokeClient::registerHud);
        NeoForge.EVENT_BUS.addListener(SmokeClient::onTick);
        NeoForge.EVENT_BUS.addListener(SmokeClient::onVanillaHudLayer);
    }
    private static void registerKeys(RegisterKeyMappingsEvent event){event.register(LEDGER);event.register(CYCLE);for(KeyMapping k:KEYS)event.register(k);}
    private static void registerHud(RegisterGuiLayersEvent event){
        event.registerAbove(VanillaGuiLayers.HOTBAR,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.dorohedoro.hole.Dorohedoro.MOD_ID,"smoke_hud"),(graphics,delta)->renderHud(graphics));
    }
    public static void receive(String data){
        try{
            String[] parts=data.split("\\|",-1);if(parts.length!=10&&parts.length!=11)return;
            innate=parts[0];smoke=Integer.parseInt(parts[1]);maxSmoke=Integer.parseInt(parts[2]);rank=Integer.parseInt(parts[3]);xp=Integer.parseInt(parts[4]);preset=Integer.parseInt(parts[5]);
            String[] slots=parts[6].split(",");
            if (slots.length == EQUIPPED.length) System.arraycopy(slots,0,EQUIPPED,0,EQUIPPED.length);
            // Defensive migration for a server still using the eight-slot payload.
            else if (slots.length == 72) {
                for(int p=0;p<9;p++)for(int i=0;i<3;i++)EQUIPPED[p*3+i]=slots[p*8+i];
            }
            loadMap(STUDY,parts[7]);loadMap(MASTERY,parts[8]);
            String[] times=parts[9].split(",");for(int i=0;i<Math.min(COOLDOWNS.length,times.length);i++)COOLDOWNS[i]=Integer.parseInt(times[i]);
            injury=parts.length==11 ? parts[10] : "none";
        }catch(RuntimeException ignored){/* Malformed response never crashes client UI. */}
        if(Minecraft.getInstance().screen instanceof SmokeLedgerScreen screen)screen.refresh();
    }
    private static void loadMap(Map<String,Integer> dest,String value){
        dest.clear();if(value.isEmpty())return;
        for(String pair:value.split(",")){String[] e=pair.split(":",2);if(e.length==2)dest.put(e[0],Integer.parseInt(e[1]));}
    }
    public static int study(String id){return STUDY.getOrDefault(id,0);}
    public static int mastery(String id){return MASTERY.getOrDefault(id,0);}
    public static String slot(int i){int n=preset*3+i;return n>=0&&n<EQUIPPED.length?EQUIPPED[n]:"-";}
    public static int cooldown(int i){return COOLDOWNS[i];}
    public static boolean focused(int i){return i>=0&&i<FOCUSED.length&&FOCUSED[i];}
    public static void request(String action,String arg){PacketDistributor.sendToServer(new SmokeNetwork.Request(action,arg));}
    private static void onTick(ClientTickEvent.Post event){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null){
            innate="none";injury="none";smoke=0;maxSmoke=0;rank=0;xp=0;preset=0;
            Arrays.fill(EQUIPPED,"-");Arrays.fill(COOLDOWNS,0);Arrays.fill(FOCUSED,false);return;
        }
        // UI timer is purely visual; the server always decides whether a cast is ready.
        if(++uiTicks%20==0)for(int i=0;i<COOLDOWNS.length;i++)COOLDOWNS[i]=Math.max(0,COOLDOWNS[i]-20);
        while(LEDGER.consumeClick()){
            if(mc.screen==null){request("status","");mc.setScreen(new SmokeLedgerScreen());}
        }
        if(mc.screen!=null)return;
        while(CYCLE.consumeClick()) {
            if("none".equals(innate)) continue;
            boolean backward=InputConstants.isKeyDown(mc.getWindow().getWindow(),GLFW.GLFW_KEY_LEFT_SHIFT)
                ||InputConstants.isKeyDown(mc.getWindow().getWindow(),GLFW.GLFW_KEY_RIGHT_SHIFT);
            // Server is authoritative about the active loadout. Snapshot arrives immediately.
            int next=Math.floorMod(preset+(backward?-1:1),9);
            request("preset",Integer.toString(next));
            // HUD changes only after a server snapshot acknowledges the preset.
        }
        for(int i=0;i<KEYS.length;i++)while(KEYS[i].consumeClick()){
            if("none".equals(innate))continue;
            boolean alt=InputConstants.isKeyDown(mc.getWindow().getWindow(),GLFW.GLFW_KEY_LEFT_ALT)||InputConstants.isKeyDown(mc.getWindow().getWindow(),GLFW.GLFW_KEY_RIGHT_ALT);
            if(alt){
                FOCUSED[i]=!FOCUSED[i];
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Smoke slot "+(i+1)+": "+(FOCUSED[i]?"Focused":"Standard")),true);
            }else request("cast",i+":"+(FOCUSED[i]?"focused":"standard"));
        }
    }
    /** Replace only vanilla hunger/hearts during survival play, never stack duplicate meters.
     * Native armor/air/experience retain their existing Minecraft presentation.
     * Guarded so Creative, Spectator and menus are not unexpectedly stripped. */
    private static void onVanillaHudLayer(RenderGuiLayerEvent.Pre e) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.player.isCreative() || mc.player.isSpectator())return;
        if(mc.options.hideGui)return;
        if(e.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
            || e.getName().equals(VanillaGuiLayers.FOOD_LEVEL))e.setCanceled(true);
    }

    private static void renderHud(GuiGraphics g){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null||mc.options.hideGui||mc.player.isSpectator())return;
        boolean smokeUser=!"none".equals(innate);
        // No boxed panels or permanent text labels.  Icons + 4px meters only.
        // Native heart/food stacks are hidden only where these replacement bars display.
        int x=9,y=9;
        if(!mc.player.isCreative()){
            meter(g, 0, mc.player.getHealth(), mc.player.getMaxHealth(), x,y,104,0xFFD36B64);
            meter(g, 1, mc.player.getFoodData().getFoodLevel(),20,x,y+12,104,0xFFD4AD68);
            y+=24;
        }
        if(smokeUser)meter(g,2,smoke,maxSmoke,x,y,104,0xFF6FC1B2);
        if(!smokeUser)return;
        // Quick access only; the B pop-up contains names, costs and mastery.
        int sy=y+22;
        for(int i=0;i<3;i++){
            int yy=sy+i*29;
            SmokeSkill skill=SmokeSkill.fromId(slot(i));
            int bg=FOCUSED[i]?0xFFBBA075:0xFF516660;
            g.fill(x,yy,x+27,yy+27,bg);
            g.fill(x+1,yy+1,x+26,yy+26,0xF0182729);
            if(skill!=null){
                SmokeSkillIcon.draw(g,skill,x+2,yy+2,23);
                if(COOLDOWNS[i]>0){
                    g.fill(x+2,yy+2,x+25,yy+25,0xB0091116);
                    String seconds=Integer.toString((COOLDOWNS[i]+19)/20);
                    g.drawCenteredString(mc.font,seconds,x+14,yy+10,0xFFFFFFFF);
                }
            }else{
                g.drawCenteredString(mc.font,"-",x+14,yy+10,0xFF8D9B99);
            }
            g.drawString(mc.font,i==0?"Z":i==1?"X":"C",x+31,yy+9,0xFFE6E9E3);
        }
        if(!"none".equals(injury)){
            // A single warning icon; full injury names remain in the Medical tab.
            g.fill(x,sy+89,x+8,sy+97,0xFFDCA47C);
            g.drawString(mc.font,"!",x+1,sy+88,0xFF202629);
        }
    }
    private static void meter(GuiGraphics g,int kind,float value,float max,int x,int y,int width,int color){
        int bx=x+14,by=y+3;int barWidth=width-14;
        g.fill(bx,by,bx+barWidth,by+5,0xB00A1619);
        int px=(int)Math.round(barWidth*Math.clamp(value/Math.max(1f,max),0f,1f));
        if(px>0)g.fill(bx,by,bx+px,by+5,color);
        // Tiny pixel icons, 9x9 with silhouettes that differ without labels.
        if(kind==0){
            g.fill(x+1,y+1,x+4,y+3,color);g.fill(x+6,y+1,x+9,y+3,color);
            g.fill(x,y+3,x+10,y+6,color);g.fill(x+2,y+6,x+8,y+8,color);
            g.fill(x+4,y+8,x+6,y+9,color);
        }else if(kind==1){
            g.fill(x+2,y+2,x+8,y+6,color);g.fill(x+1,y+3,x+9,y+5,color);
            g.fill(x+5,y+6,x+7,y+8,color);g.fill(x+7,y+7,x+10,y+9,0xFFE9E4D3);
        }else{
            g.fill(x+5,y,x+8,y+3,color);g.fill(x+3,y+2,x+6,y+5,color);
            g.fill(x+5,y+5,x+9,y+7,color);g.fill(x+2,y+7,x+6,y+9,color);
        }
    }
}
