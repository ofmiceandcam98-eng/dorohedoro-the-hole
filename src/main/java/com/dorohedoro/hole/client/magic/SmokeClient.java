package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.magic.SmokeNetwork;
import com.dorohedoro.hole.magic.SmokeSkill;
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
    /** Hides the vanilla stat layers the Hole HUD replaces, so nothing is drawn twice. Survival and adventure only;
     * creative, spectator and hidden-GUI views keep vanilla behaviour. Vehicle health and the jump meter stay vanilla. */
    private static void onVanillaHudLayer(RenderGuiLayerEvent.Pre e) {
        Minecraft mc=Minecraft.getInstance();
        if(!com.dorohedoro.hole.client.ui.HoleHud.replacesVanillaStats(mc))return;
        var name=e.getName();
        if(name.equals(VanillaGuiLayers.PLAYER_HEALTH)||name.equals(VanillaGuiLayers.FOOD_LEVEL)
            ||name.equals(VanillaGuiLayers.ARMOR_LEVEL)||name.equals(VanillaGuiLayers.AIR_LEVEL))e.setCanceled(true);
        if(com.dorohedoro.hole.client.ui.HoleHud.replacesVanillaExperience(mc)
            &&(name.equals(VanillaGuiLayers.EXPERIENCE_BAR)||name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)))e.setCanceled(true);
    }

    /** The key currently bound to a technique slot, as the player set it in Controls. */
    public static String keyLabel(int i){
        String key=KEYS[i].getTranslatedKeyMessage().getString();
        return key.length()>2?key.substring(0,2):key;
    }

    private static void renderHud(GuiGraphics g){
        com.dorohedoro.hole.client.ui.HoleHud.render(g);
    }
}
