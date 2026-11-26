package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.magic.OtherMagicMechanics;
import com.dorohedoro.hole.registry.ModSmokeParticles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayDeque;
import java.util.Iterator;

/** One-shot visual models for limbs being cut off and tissue regrowing.
 * Uses the affected player's own Minecraft skin instead of a duplicated skin
 * texture. Server-owned LimbState is solely responsible for actual anatomy.
 * Drawn in the world, visible to every player receiving the sever/regrow cue.
 */
public final class LimbAnimationClient {
    private LimbAnimationClient() {}
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "limb_effects"), "main");
    private static ModelPart leftArm, rightArm, leftLeg, rightLeg;
    private static final ArrayDeque<LimbCue> EVENTS = new ArrayDeque<>();
    private static net.minecraft.client.multiplayer.ClientLevel activeLevel;
    private static final int MAX = 64;
    /** The ItemEntity ID is sent by the server; the flesh mesh follows its
     * actual physics for the first frames, then yields to the collectible.
     * This is a cosmetic link, never an item or injury authority. */
    private record LimbCue(int entityId, int limb, boolean regrowing, int dropId,
                           long start, Vec3 origin, Vec3 side, float yaw) {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        // Correct vanilla 64x64 skin texel addresses. Slim skins are approximated
        // as 4px arms until a separate slim-width model is added.
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32,48)
            .addBox(-2f,-6f,-2f,4f,12f,4f), PartPose.ZERO);
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40,16)
            .addBox(-2f,-6f,-2f,4f,12f,4f), PartPose.ZERO);
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16,48)
            .addBox(-2f,-6f,-2f,4f,12f,4f), PartPose.ZERO);
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0,16)
            .addBox(-2f,-6f,-2f,4f,12f,4f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64,64);
    }

    public static void bake(ModelPart root) {
        leftArm = root.getChild("left_arm"); rightArm = root.getChild("right_arm");
        leftLeg = root.getChild("left_leg"); rightLeg = root.getChild("right_leg");
    }

    public static void receive(int entityId, int limb, int kind, int dropId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || kind < 0 || kind > 1 || entityId < 0
            || limb != OtherMagicMechanics.LEFT_ARM && limb != OtherMagicMechanics.RIGHT_ARM
            && limb != OtherMagicMechanics.LEFT_LEG && limb != OtherMagicMechanics.RIGHT_LEG) return;
        if (mc.level != activeLevel) { EVENTS.clear(); activeLevel = mc.level; }
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) return;
        boolean arm = limb == OtherMagicMechanics.LEFT_ARM || limb == OtherMagicMechanics.RIGHT_ARM;
        boolean left = limb == OtherMagicMechanics.LEFT_ARM || limb == OtherMagicMechanics.LEFT_LEG;
        double angle = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        Vec3 side = right.scale(left ? -.26 : .26);
        Vec3 point = player.position().add(side).add(0,
            player.getBbHeight() * (arm ? .69 : .29),0);
        if (EVENTS.size() >= MAX) EVENTS.removeFirst();
        EVENTS.addLast(new LimbCue(entityId, limb, kind == 1, kind == 1 ? -1 : dropId,
            mc.level.getGameTime(), point, right.scale(left ? -1 : 1), player.getYRot()));
        // Black smoke masks the separation/reconnection; no red particle spam.
        for (int i=0; i<15; i++) {
            double phi = i * 2.399963229728653;
            double r = (i % 4 + 1) * .035;
            mc.level.addParticle(ParticleTypes.SMOKE,
                point.x + Math.cos(phi)*r, point.y + Math.sin(phi)*r,
                point.z + Math.sin(phi*.5)*r, 0, .013, 0);
        }
    }

    /** One particle update per client tick, NOT per rendered frame. Healing
     * fibers spiral from the stump toward the growing tip. Cosmetic only. */
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || activeLevel != mc.level || EVENTS.isEmpty()) return;
        long now = mc.level.getGameTime();
        for (LimbCue cue : EVENTS) {
            if (!cue.regrowing()) continue;
            long age = now - cue.start();
            if (age < 0 || age >= 26) continue;
            Entity entity = mc.level.getEntity(cue.entityId());
            if (!(entity instanceof AbstractClientPlayer player)) continue;
            if (player.distanceToSqr(mc.gameRenderer.getMainCamera().getPosition()) > 48 * 48) continue;
            boolean arm = cue.limb() == OtherMagicMechanics.LEFT_ARM
                    || cue.limb() == OtherMagicMechanics.RIGHT_ARM;
            Vec3 center = player.position().add(cue.side().scale(.26))
                    .add(0, player.getBbHeight() * (arm ? .69 : .29), 0);
            double progress = Math.clamp(age / 26.0, 0, 1);
            for (int j = 0; j < 3; j++) {
                double phi = age * .60 + j * (Math.PI * 2 / 3.0);
                double radius = .12 * (1.0 - .45 * progress);
                double height = .30 - .60 * progress;
                mc.level.addParticle(ModSmokeParticles.HEALING_FIBER.get(),
                    center.x + Math.cos(phi) * radius,
                    center.y + height,
                    center.z + Math.sin(phi) * radius,
                    -.004 * Math.cos(phi), -.009, -.004 * Math.sin(phi));
            }
        }
    }

    /** Temporarily hide newly regrowing vanilla limbs behind the rebuilding
     * mesh. A limb is never hidden on the authority of a client cast request. */
    public static int hiddenDuringRegrowth(int entityId, long now) {
        if (Minecraft.getInstance().level != activeLevel) return 0;
        int mask = 0;
        for (LimbCue e : EVENTS) {
            long age = now - e.start();
            if (e.regrowing() && e.entityId() == entityId && age >= 0 && age < 18)
                mask |= e.limb();
        }
        return mask;
    }

    public static void renderLevel(RenderLevelStageEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || e.getPoseStack() == null) return;
        if (mc.level != activeLevel) { EVENTS.clear(); activeLevel = mc.level; return; }
        if (leftArm == null || EVENTS.isEmpty()) return;
        Vec3 camera = e.getCamera().getPosition();
        PoseStack pose = e.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Iterator<LimbCue> iter = EVENTS.iterator();
        while(iter.hasNext()) {
            LimbCue cue = iter.next();
            long age = mc.level.getGameTime() - cue.start();
            int lifetime = cue.regrowing() ? 26 : 18;
            if (age < 0 || age >= lifetime) { iter.remove(); continue; }
            Entity entity = mc.level.getEntity(cue.entityId());
            if (!(entity instanceof AbstractClientPlayer player) || player.isInvisible()) continue;
            if (cue.origin().distanceToSqr(camera) > 96*96) continue;
            ModelPart part = switch(cue.limb()) {
                case OtherMagicMechanics.LEFT_ARM -> leftArm;
                case OtherMagicMechanics.RIGHT_ARM -> rightArm;
                case OtherMagicMechanics.LEFT_LEG -> leftLeg;
                default -> rightLeg;
            };
            Vec3 pos;
            float t = age / (float) lifetime;
            float size;
            if (cue.regrowing()) {
                // Follow the patient's body while fibers reconstruct the limb.
                pos = player.position().add(cue.side().scale(.26)).add(0,
                    player.getBbHeight() * (cue.limb() <= OtherMagicMechanics.RIGHT_ARM ? .69 : .29),0);
                size = Math.max(.06f, Math.min(1, (float)Math.pow(t/.77f,1.8)));
            } else {
                // The cosmetic detached body part tracks the ACTUAL dropped
                // limb item rather than simulating unrelated flight physics.
                // Entity packets can arrive after the animation cue, so use
                // the original launch impulse as a bounded fallback.
                Entity realDrop = cue.dropId() < 0 ? null : mc.level.getEntity(cue.dropId());
                if (realDrop instanceof ItemEntity item && item.isAlive()) {
                    pos = item.position().add(0, .15, 0);
                } else {
                    // Matches SeveredLimbDrops' .35 side offset, .21 horizontal
                    // launch impulse and .22 initial vertical speed (approximately).
                    pos = cue.origin().add(cue.side().scale(.09 + age * .21))
                        .add(0, Math.max(-.30, age * .22 - age * age * .020), 0);
                }
                // 0–10 ticks: recognizable skin-mapped limb; 11–18 ticks:
                // shrink into the always-real, pickupable named item.
                size = t < .56f ? 1f : Math.max(0f, 1f - (t-.56f)/.44f);
            }
            pose.pushPose();
            pose.translate(pos.x-camera.x,pos.y-camera.y,pos.z-camera.z);
            pose.mulPose(Axis.YP.rotationDegrees(-cue.yaw()));
            if (!cue.regrowing()) {
                pose.mulPose(Axis.XP.rotationDegrees(age*21));
                pose.mulPose(Axis.ZP.rotationDegrees(age*14));
            } else {
                pose.mulPose(Axis.ZP.rotationDegrees((1-size)*18f));
            }
            if (cue.regrowing()) {
                // Anchor the proximal end (shoulder/hip). Length and width
                // grow at different rates instead of inflating a whole limb.
                pose.translate(0, -(1f-size)*.375f, 0);
                float width = .40f + .60f*size;
                pose.scale(width, size, width);
            } else {
                pose.scale(size,size,size);
            }
            var skin = player.getSkin().texture();
            var type = RenderType.entityCutoutNoCull(skin);
            part.render(pose,buffers.getBuffer(type),0xF000F0,OverlayTexture.NO_OVERLAY);
            pose.popPose();
            buffers.endBatch(type);
        }
    }
}
