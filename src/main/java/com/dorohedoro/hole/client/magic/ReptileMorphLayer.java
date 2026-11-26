package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.Dorohedoro;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** A 3D anatomical overlay (snout, jaw, scales, claws, tail), animated
 * with the vanilla player's body pose. No replacement of the player skin. */
public final class ReptileMorphLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"reptile_morph"), "main");
    private static final ResourceLocation SKIN = ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/entity/reptile_morph.png");
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tail;

    public ReptileMorphLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,
                            ModelPart root) {
        super(parent);
        head=root.getChild("head"); body=root.getChild("body");
        leftArm=root.getChild("left_arm"); rightArm=root.getChild("right_arm");
        tail=body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition r=mesh.getRoot();
        // Dragon/reptilian proportions, intentionally separate from humanoid skin.
        r.addOrReplaceChild("head",CubeListBuilder.create()
            .texOffs(0,0).addBox(-3.4f,-5.9f,-7.8f,6.8f,2.8f,5.5f)
            .texOffs(26,0).addBox(-3.0f,-3.2f,-7.2f,6f,1.2f,4.6f)
            .texOffs(48,0).addBox(-4f,-8f,-3f,2f,3f,2f)
            .texOffs(56,0).addBox(2f,-8f,-3f,2f,3f,2f),PartPose.ZERO);
        PartDefinition torso=r.addOrReplaceChild("body",CubeListBuilder.create()
            .texOffs(0,16).addBox(-4.5f,0f,-2.3f,9f,12f,4.6f)
            .texOffs(28,16).addBox(-2.5f,3f,-2.6f,5f,7f,.9f),PartPose.ZERO);
        // Segmented tail extends behind the torso; strong swinging silhouette.
        PartDefinition t=torso.addOrReplaceChild("tail",CubeListBuilder.create()
            .texOffs(0,42).addBox(-2f,-1f,0f,4f,4f,8f),PartPose.offset(0,11,2.4f));
        t.addOrReplaceChild("mid", CubeListBuilder.create()
            .texOffs(24,42).addBox(-1.45f,-1.2f,0f,2.9f,2.9f,7f),PartPose.offset(0,0,7.8f));
        t.addOrReplaceChild("tip", CubeListBuilder.create()
            .texOffs(46,42).addBox(-.9f,-.8f,0f,1.8f,1.8f,6f),PartPose.offset(0,0,14.6f));
        r.addOrReplaceChild("left_arm",CubeListBuilder.create()
            .texOffs(0,56).addBox(-1.2f,0,-2.45f,4.4f,10f,4.9f)
            .texOffs(26,56).addBox(-1.9f,9f,-3.1f,1f,3.2f,1.4f)
            .texOffs(32,56).addBox(.6f,9f,-3.1f,1f,3.2f,1.4f),PartPose.ZERO);
        r.addOrReplaceChild("right_arm",CubeListBuilder.create()
            .texOffs(0,72).addBox(-3.2f,0,-2.45f,4.4f,10f,4.9f)
            .texOffs(26,72).addBox(-1.1f,9f,-3.1f,1f,3.2f,1.4f)
            .texOffs(32,72).addBox(1.4f,9f,-3.1f,1f,3.2f,1.4f),PartPose.ZERO);
        return LayerDefinition.create(mesh,128,128);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float yaw, float pitch) {
        float power=SmokeMeshClient.morphPower(player.getId(),player.level().getGameTime());
        if(power<=0 || player.isInvisible())return;
        PlayerModel<AbstractClientPlayer> base=getParentModel();
        head.copyFrom(base.head); body.copyFrom(base.body);
        leftArm.copyFrom(base.leftArm); rightArm.copyFrom(base.rightArm);
        tail.yRot=(float)Math.sin(ageInTicks*.21f)*(.15f+.16f*power);
        tail.xRot=.12f+(float)Math.cos(ageInTicks*.14f)*.06f;
        pose.pushPose();
        float scale=1.0f + power*.08f;
        pose.scale(scale,scale,scale);
        var sink=buffers.getBuffer(RenderType.entityCutoutNoCull(SKIN));
        head.render(pose,sink,light, OverlayTexture.NO_OVERLAY);
        body.render(pose,sink,light, OverlayTexture.NO_OVERLAY);
        int severed=SeveredLimbClient.missing(player.getId());
        if ((severed & com.dorohedoro.hole.magic.OtherMagicMechanics.LEFT_ARM) == 0)
            leftArm.render(pose,sink,light,OverlayTexture.NO_OVERLAY);
        if ((severed & com.dorohedoro.hole.magic.OtherMagicMechanics.RIGHT_ARM) == 0)
            rightArm.render(pose,sink,light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
