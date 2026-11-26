package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.magic.OtherMagicMechanics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Cosmetic model hiding driven only by a server-supplied injury mask.
 * Does not modify players' bodies, stats or save data on the client.
 * Arm/leg and sleeve/pants visibility is restored after each render call. */
public final class SeveredLimbClient {
    private SeveredLimbClient() {}
    private static final Map<Integer, Integer> LOST = new HashMap<>();
    private static final Map<PlayerRenderer, boolean[]> SAVED = new IdentityHashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel currentLevel;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SeveredLimbClient::preRender);
        NeoForge.EVENT_BUS.addListener(SeveredLimbClient::postRender);
        NeoForge.EVENT_BUS.addListener(SeveredLimbClient::renderArm);
        NeoForge.EVENT_BUS.addListener(SeveredLimbClient::renderHand);
        NeoForge.EVENT_BUS.addListener(SeveredLimbClient::clientTick);
    }

    public static void receive(int entityId, int mask) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || entityId < 0 || (mask & ~15) != 0) return;
        resetIfChanged(mc);
        if (mask == 0) LOST.remove(entityId); else LOST.put(entityId, mask);
    }
    private static void resetIfChanged(Minecraft mc) {
        if (currentLevel != mc.level) {
            LOST.clear(); SAVED.clear(); currentLevel = mc.level;
        }
    }
    public static int missing(int id) { return LOST.getOrDefault(id, 0); }
    private static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        resetIfChanged(mc);
        if (mc.level != null && mc.level.getGameTime() % 100 == 0)
            LOST.keySet().removeIf(id -> mc.level.getEntity(id) == null);
    }
    private static boolean[] capture(PlayerModel<?> model) {
        return new boolean[] {
            model.leftArm.visible, model.rightArm.visible,
            model.leftLeg.visible, model.rightLeg.visible,
            model.leftSleeve.visible, model.rightSleeve.visible,
            model.leftPants.visible, model.rightPants.visible
        };
    }
    private static void apply(PlayerModel<?> model, boolean[] saved, int mask) {
        model.leftArm.visible = saved[0] && (mask & OtherMagicMechanics.LEFT_ARM) == 0;
        model.rightArm.visible = saved[1] && (mask & OtherMagicMechanics.RIGHT_ARM) == 0;
        model.leftLeg.visible = saved[2] && (mask & OtherMagicMechanics.LEFT_LEG) == 0;
        model.rightLeg.visible = saved[3] && (mask & OtherMagicMechanics.RIGHT_LEG) == 0;
        model.leftSleeve.visible = saved[4] && (mask & OtherMagicMechanics.LEFT_ARM) == 0;
        model.rightSleeve.visible = saved[5] && (mask & OtherMagicMechanics.RIGHT_ARM) == 0;
        model.leftPants.visible = saved[6] && (mask & OtherMagicMechanics.LEFT_LEG) == 0;
        model.rightPants.visible = saved[7] && (mask & OtherMagicMechanics.RIGHT_LEG) == 0;
    }
    private static void preRender(RenderPlayerEvent.Pre event) {
        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<?> model = renderer.getModel();
        // Guard against renderer reuse and preserve original vanilla/other-mod visibility.
        boolean[] old = capture(model);
        SAVED.put(renderer, old);
        int mask = missing(event.getEntity().getId())
            | LimbAnimationClient.hiddenDuringRegrowth(event.getEntity().getId(),
                event.getEntity().level().getGameTime());
        if (mask != 0) apply(model, old, mask);
    }
    private static void postRender(RenderPlayerEvent.Post event) {
        PlayerRenderer renderer = event.getRenderer();
        boolean[] saved = SAVED.remove(renderer);
        if (saved != null) apply(renderer.getModel(), saved, 0);
    }
    private static boolean handMissing(int mask, HumanoidArm arm) {
        return (mask & (arm == HumanoidArm.LEFT
            ? OtherMagicMechanics.LEFT_ARM : OtherMagicMechanics.RIGHT_ARM)) != 0;
    }
    private static void renderArm(RenderArmEvent event) {
        if (handMissing(missing(event.getPlayer().getId())
            | LimbAnimationClient.hiddenDuringRegrowth(event.getPlayer().getId(),
                event.getPlayer().level().getGameTime()), event.getArm())) event.setCanceled(true);
    }
    private static void renderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        HumanoidArm hand = mc.player.getMainArm();
        if (event.getHand() == InteractionHand.OFF_HAND) {
            hand = hand == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        }
        if (handMissing(missing(mc.player.getId())
            | LimbAnimationClient.hiddenDuringRegrowth(mc.player.getId(),
                mc.player.level().getGameTime()), hand)) event.setCanceled(true);
    }
}
