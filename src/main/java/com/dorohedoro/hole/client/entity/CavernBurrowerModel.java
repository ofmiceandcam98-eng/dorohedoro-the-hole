package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.CavernBurrowerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/cavern_burrower.json. */
public final class CavernBurrowerModel<T extends CavernBurrowerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "cavern_burrower"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart paw_l_0;
    private final ModelPart paw_l_1;
    private final ModelPart paw_r_0;
    private final ModelPart paw_r_1;
    public CavernBurrowerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.paw_l_0 = root.getChild("paw_l_0");
        this.paw_l_1 = root.getChild("paw_l_1");
        this.paw_r_0 = root.getChild("paw_r_0");
        this.paw_r_1 = root.getChild("paw_r_1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-5.0F, -3.92F, -6.67F, 10.0F, 1.3F, 1.2F)
                .texOffs(30, 2).addBox(-5.0F, -3.92F, -3.67F, 10.0F, 1.3F, 1.2F)
                .texOffs(58, 2).addBox(-5.0F, -3.92F, -0.67F, 10.0F, 1.3F, 1.2F)
                .texOffs(86, 2).addBox(-5.0F, -3.92F, 2.33F, 10.0F, 1.3F, 1.2F)
                .texOffs(114, 2).addBox(-5.0F, -3.92F, 5.33F, 10.0F, 1.3F, 1.2F)
                .texOffs(142, 2).addBox(-5.8F, -4.82F, -7.67F, 11.6F, 0.78F, 1.5F)
                .texOffs(174, 2).addBox(-5.8F, -4.5F, -5.57F, 11.6F, 0.78F, 1.5F)
                .texOffs(206, 2).addBox(-5.8F, -4.82F, -3.47F, 11.6F, 0.78F, 1.5F)
                .texOffs(238, 2).addBox(-5.8F, -4.5F, -1.37F, 11.6F, 0.78F, 1.5F)
                .texOffs(270, 2).addBox(-5.8F, -4.82F, 0.73F, 11.6F, 0.78F, 1.5F)
                .texOffs(302, 2).addBox(-5.8F, -4.5F, 2.83F, 11.6F, 0.78F, 1.5F)
                .texOffs(334, 2).addBox(-5.8F, -4.82F, 4.93F, 11.6F, 0.78F, 1.5F)
                .texOffs(366, 2).addBox(-5.371F, -4.62F, -9.67F, 10.742F, 9.2F, 3.15F)
                .texOffs(400, 2).addBox(-5.829F, -4.37F, -6.97F, 11.659F, 8.99F, 3.15F)
                .texOffs(436, 2).addBox(-6.123F, -4.12F, -4.27F, 12.247F, 8.78F, 3.15F)
                .texOffs(474, 2).addBox(-6.195F, -3.87F, -1.57F, 12.39F, 8.57F, 3.15F)
                .texOffs(2, 20).addBox(-6.03F, -3.62F, 1.13F, 12.06F, 8.36F, 3.15F)
                .texOffs(40, 20).addBox(-5.661F, -3.37F, 3.83F, 11.322F, 8.15F, 3.15F)
                .texOffs(76, 20).addBox(-5.161F, -3.12F, 6.53F, 10.322F, 7.94F, 3.15F)
                , PartPose.offset(0F, 16.42F, 1.67F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(110, 20).addBox(-5.0F, -3.75F, -7.5F, 10.0F, 6.0F, 8.0F)
                .texOffs(150, 20).addBox(-3.2F, 0.25F, -11.5F, 6.4F, 3.5F, 5.0F)
                .texOffs(178, 20).addBox(-2.0F, -0.45F, -12.3F, 4.0F, 2.3F, 1.6F)
                .texOffs(194, 20).addBox(-4.9F, -2.35F, -7.5F, 1.15F, 1.15F, 1.1F)
                .texOffs(206, 20).addBox(3.8F, -2.35F, -7.5F, 1.15F, 1.15F, 1.1F)
                .texOffs(218, 20).addBox(-5.2F, -1.15F, -10.8F, 1.4F, 2.7F, 3.3F)
                .texOffs(234, 20).addBox(-5.2F, -2.75F, -9.0F, 1.1F, 0.62F, 1.4F)
                .texOffs(246, 20).addBox(3.6F, -1.15F, -10.8F, 1.4F, 2.7F, 3.3F)
                .texOffs(262, 20).addBox(3.8F, -2.75F, -9.0F, 1.1F, 0.62F, 1.4F)
                .texOffs(274, 20).addBox(-2.9F, -1.55F, -12.2F, 1.5F, 1.2F, 3.4F)
                .texOffs(290, 20).addBox(1.3F, -1.55F, -12.2F, 1.5F, 1.2F, 3.4F)
                , PartPose.offset(0F, 18.75F, 2.5F));
        root.addOrReplaceChild("paw_l_0", CubeListBuilder.create()
                .texOffs(306, 20).addBox(-3.7F, -0.2F, -0.15F, 3.3F, 5.5F, 4.0F)
                .texOffs(326, 20).addBox(-4.1F, 3.9F, -1.65F, 4.1F, 1.7F, 6.0F)
                .texOffs(352, 20).addBox(-3.8F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(366, 20).addBox(-2.65F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(380, 20).addBox(-1.5F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(394, 20).addBox(-4.2F, 4.0F, -3.15F, 0.7F, 0.65F, 3.9F)
                .texOffs(408, 20).addBox(-3.1F, 4.0F, -3.25F, 0.7F, 0.65F, 3.9F)
                .texOffs(422, 20).addBox(-2.0F, 4.0F, -3.35F, 0.7F, 0.65F, 3.9F)
                , PartPose.offset(-3.8F, 19.2F, -3.85F));
        root.addOrReplaceChild("paw_l_1", CubeListBuilder.create()
                .texOffs(436, 20).addBox(-3.7F, -0.2F, -0.15F, 3.3F, 5.5F, 4.0F)
                .texOffs(456, 20).addBox(-4.1F, 3.9F, -1.65F, 4.1F, 1.7F, 6.0F)
                .texOffs(482, 20).addBox(-3.8F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(496, 20).addBox(-2.65F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(2, 38).addBox(-1.5F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(16, 38).addBox(-4.2F, 4.0F, -2.95F, 0.7F, 0.65F, 3.9F)
                .texOffs(30, 38).addBox(-3.1F, 4.0F, -3.05F, 0.7F, 0.65F, 3.9F)
                .texOffs(44, 38).addBox(-2.0F, 4.0F, -3.15F, 0.7F, 0.65F, 3.9F)
                , PartPose.offset(-3.8F, 19.2F, 6.15F));
        root.addOrReplaceChild("paw_r_0", CubeListBuilder.create()
                .texOffs(58, 38).addBox(0.4F, -0.2F, -0.15F, 3.3F, 5.5F, 4.0F)
                .texOffs(78, 38).addBox(0.0F, 3.9F, -1.65F, 4.1F, 1.7F, 6.0F)
                .texOffs(104, 38).addBox(0.3F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(118, 38).addBox(1.45F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(132, 38).addBox(2.6F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(146, 38).addBox(1.0F, 4.0F, -3.15F, 0.7F, 0.65F, 3.9F)
                .texOffs(160, 38).addBox(2.1F, 4.0F, -3.25F, 0.7F, 0.65F, 3.9F)
                .texOffs(174, 38).addBox(3.2F, 4.0F, -3.35F, 0.7F, 0.65F, 3.9F)
                , PartPose.offset(4.0F, 19.2F, -3.85F));
        root.addOrReplaceChild("paw_r_1", CubeListBuilder.create()
                .texOffs(188, 38).addBox(0.4F, -0.2F, -0.15F, 3.3F, 5.5F, 4.0F)
                .texOffs(208, 38).addBox(0.0F, 3.9F, -1.65F, 4.1F, 1.7F, 6.0F)
                .texOffs(234, 38).addBox(0.3F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(248, 38).addBox(1.45F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(262, 38).addBox(2.6F, 4.2F, -4.35F, 0.8F, 1.2F, 3.3F)
                .texOffs(276, 38).addBox(1.0F, 4.0F, -2.95F, 0.7F, 0.65F, 3.9F)
                .texOffs(290, 38).addBox(2.1F, 4.0F, -3.05F, 0.7F, 0.65F, 3.9F)
                .texOffs(304, 38).addBox(3.2F, 4.0F, -3.15F, 0.7F, 0.65F, 3.9F)
                , PartPose.offset(4.0F, 19.2F, 6.15F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        paw_l_0.xRot = motion * Mth.sin(limbSwing * 2.08F + 0.0000F) * 0.69F - attack * 0.82F;
        paw_l_0.y = 19.200F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.08F + 0.0000F)) * 1.22F;
        paw_l_0.zRot = -1 * (motion * Mth.cos(limbSwing * 2.08F + 0.0000F) * 0.08F + attack * Mth.sin(ageInTicks * 0.73F) * 0.23F);
        paw_l_1.xRot = motion * Mth.sin(limbSwing * 2.08F + 3.1416F) * 0.51F - attack * -0.16F;
        paw_l_1.y = 19.200F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.08F + 3.1416F)) * 0.85F;
        paw_l_1.zRot = -1 * (motion * Mth.cos(limbSwing * 2.08F + 3.1416F) * 0.08F + attack * Mth.sin(ageInTicks * 0.73F) * 0.23F);
        paw_r_0.xRot = motion * Mth.sin(limbSwing * 2.08F + 3.1416F) * 0.69F - attack * 0.82F;
        paw_r_0.y = 19.200F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.08F + 3.1416F)) * 1.22F;
        paw_r_0.zRot = 1 * (motion * Mth.cos(limbSwing * 2.08F + 3.1416F) * 0.08F + attack * Mth.sin(ageInTicks * 0.73F) * 0.23F);
        paw_r_1.xRot = motion * Mth.sin(limbSwing * 2.08F + 6.2832F) * 0.51F - attack * -0.16F;
        paw_r_1.y = 19.200F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.08F + 6.2832F)) * 0.85F;
        paw_r_1.zRot = 1 * (motion * Mth.cos(limbSwing * 2.08F + 6.2832F) * 0.08F + attack * Mth.sin(ageInTicks * 0.73F) * 0.23F);
        body.y = 16.420F + motion * (0.22F + 0.23F * Mth.cos(limbSwing * 4.16F));
        body.xRot = motion * Mth.cos(limbSwing * 2.08F + 0.6F) * 0.082F + recoil * 0.13F;
        body.zRot = motion * Mth.sin(limbSwing * 2.08F) * 0.095F;
        head.xRot = Mth.sin(ageInTicks * 0.08F) * 0.035F + motion * Mth.sin(limbSwing * 2.08F + 1.0F) * 0.13F - attack * 0.39F;
        head.yRot = Mth.sin(ageInTicks * 0.13F) * 0.065F + motion * Mth.sin(limbSwing * 1.04F) * 0.07F;
        head.y = 18.750F + motion * Mth.sin(limbSwing * 2.08F + 0.9F) * 0.22F;
    }
}
