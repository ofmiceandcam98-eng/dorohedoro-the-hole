package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SporeCrawlerEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/spore_crawler.json. */
public final class SporeCrawlerModel<T extends SporeCrawlerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "spore_crawler"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg_l_0;
    private final ModelPart leg_l_1;
    private final ModelPart leg_l_2;
    private final ModelPart leg_r_0;
    private final ModelPart leg_r_1;
    private final ModelPart leg_r_2;
    private final ModelPart spore_sac_0;
    private final ModelPart spore_sac_1;
    private final ModelPart spore_sac_2;
    public SporeCrawlerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg_l_0 = root.getChild("leg_l_0");
        this.leg_l_1 = root.getChild("leg_l_1");
        this.leg_l_2 = root.getChild("leg_l_2");
        this.leg_r_0 = root.getChild("leg_r_0");
        this.leg_r_1 = root.getChild("leg_r_1");
        this.leg_r_2 = root.getChild("leg_r_2");
        this.spore_sac_0 = root.getChild("spore_sac_0");
        this.spore_sac_1 = root.getChild("spore_sac_1");
        this.spore_sac_2 = root.getChild("spore_sac_2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-4.0F, -0.45F, -6.5F, 8.0F, 5.5F, 12.0F)
                .texOffs(46, 2).addBox(-3.0F, -2.15F, -5.5F, 6.0F, 2.5F, 2.3F)
                .texOffs(68, 2).addBox(-4.1F, -3.95F, -5.5F, 8.2F, 1.6F, 3.0F)
                .texOffs(96, 2).addBox(-3.6F, -2.35F, -5.5F, 7.2F, 0.7F, 2.3F)
                .texOffs(122, 2).addBox(-3.0F, -2.15F, -2.5F, 6.0F, 2.5F, 2.3F)
                .texOffs(144, 2).addBox(-4.1F, -3.95F, -2.5F, 8.2F, 1.6F, 3.0F)
                .texOffs(172, 2).addBox(-3.6F, -2.35F, -2.5F, 7.2F, 0.7F, 2.3F)
                .texOffs(198, 2).addBox(-3.0F, -2.15F, 0.5F, 6.0F, 2.5F, 2.3F)
                .texOffs(220, 2).addBox(-4.1F, -3.95F, 0.5F, 8.2F, 1.6F, 3.0F)
                .texOffs(248, 2).addBox(-3.6F, -2.35F, 0.5F, 7.2F, 0.7F, 2.3F)
                .texOffs(274, 2).addBox(-3.0F, -2.15F, 3.5F, 6.0F, 2.5F, 2.3F)
                .texOffs(296, 2).addBox(-4.1F, -3.95F, 3.5F, 8.2F, 1.6F, 3.0F)
                .texOffs(324, 2).addBox(-3.6F, -2.35F, 3.5F, 7.2F, 0.7F, 2.3F)
                .texOffs(350, 2).addBox(-3.25F, -4.15F, -5.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(362, 2).addBox(-0.6F, -4.15F, -5.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(374, 2).addBox(2.05F, -4.15F, -5.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(386, 2).addBox(-3.25F, -4.15F, -2.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(398, 2).addBox(-0.6F, -4.15F, -2.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(410, 2).addBox(2.05F, -4.15F, -2.15F, 1.6F, 0.24F, 1.3F)
                .texOffs(422, 2).addBox(-3.25F, -4.15F, 0.85F, 1.6F, 0.24F, 1.3F)
                .texOffs(434, 2).addBox(-0.6F, -4.15F, 0.85F, 1.6F, 0.24F, 1.3F)
                .texOffs(446, 2).addBox(2.05F, -4.15F, 0.85F, 1.6F, 0.24F, 1.3F)
                .texOffs(458, 2).addBox(-3.25F, -4.15F, 3.85F, 1.6F, 0.24F, 1.3F)
                .texOffs(470, 2).addBox(-0.6F, -4.15F, 3.85F, 1.6F, 0.24F, 1.3F)
                .texOffs(482, 2).addBox(2.05F, -4.15F, 3.85F, 1.6F, 0.24F, 1.3F)
                , PartPose.offset(0F, 16.95F, 0.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(2, 24).addBox(-3.0F, -2.0F, -4.5F, 6.0F, 4.0F, 5.0F)
                .texOffs(28, 24).addBox(-3.2F, -1.5F, -5.5F, 1.0F, 0.95F, 1.2F)
                .texOffs(38, 24).addBox(2.2F, -1.5F, -5.5F, 1.0F, 0.95F, 1.2F)
                .texOffs(48, 24).addBox(-1.8F, 0.7F, -5.8F, 0.5F, 0.8F, 0.6F)
                .texOffs(56, 24).addBox(-0.85F, 0.7F, -5.8F, 0.5F, 0.8F, 0.6F)
                .texOffs(64, 24).addBox(0.1F, 0.7F, -5.8F, 0.5F, 0.8F, 0.6F)
                .texOffs(72, 24).addBox(1.05F, 0.7F, -5.8F, 0.5F, 0.8F, 0.6F)
                , PartPose.offset(0F, 20.0F, 0.5F));
        root.addOrReplaceChild("leg_l_0", CubeListBuilder.create()
                .texOffs(80, 24).addBox(-4.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(96, 24).addBox(-5.0F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(108, 24).addBox(-4.6F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(-3.3F, 20.1F, -4.3F));
        root.addOrReplaceChild("leg_l_1", CubeListBuilder.create()
                .texOffs(118, 24).addBox(-4.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(134, 24).addBox(-5.0F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(146, 24).addBox(-4.6F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(-3.3F, 20.1F, 0.7F));
        root.addOrReplaceChild("leg_l_2", CubeListBuilder.create()
                .texOffs(156, 24).addBox(-4.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(172, 24).addBox(-5.0F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(184, 24).addBox(-4.6F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(-3.3F, 20.1F, 5.7F));
        root.addOrReplaceChild("leg_r_0", CubeListBuilder.create()
                .texOffs(194, 24).addBox(0.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(210, 24).addBox(2.7F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(222, 24).addBox(3.5F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(3.7F, 20.1F, -4.3F));
        root.addOrReplaceChild("leg_r_1", CubeListBuilder.create()
                .texOffs(232, 24).addBox(0.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(248, 24).addBox(2.7F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(260, 24).addBox(3.5F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(3.7F, 20.1F, 0.7F));
        root.addOrReplaceChild("leg_r_2", CubeListBuilder.create()
                .texOffs(270, 24).addBox(0.0F, -0.2F, -0.7F, 4.0F, 1.6F, 1.4F)
                .texOffs(286, 24).addBox(2.7F, 1.1F, -0.7F, 1.2F, 3.6F, 1.4F)
                .texOffs(298, 24).addBox(3.5F, 1.3F, -0.7F, 0.72F, 0.65F, 1.3F)
                , PartPose.offset(3.7F, 20.1F, 5.7F));
        root.addOrReplaceChild("spore_sac_0", CubeListBuilder.create()
                .texOffs(308, 24).addBox(-1.05F, -0.7F, -1.2F, 2.1F, 1.3F, 2.4F)
                , PartPose.offset(-1.45F, 12.6F, -2.8F));
        root.addOrReplaceChild("spore_sac_1", CubeListBuilder.create()
                .texOffs(324, 24).addBox(-1.05F, -0.7F, -1.2F, 2.1F, 1.3F, 2.4F)
                , PartPose.offset(0.8F, 12.6F, 0.40000000000000036F));
        root.addOrReplaceChild("spore_sac_2", CubeListBuilder.create()
                .texOffs(340, 24).addBox(-1.05F, -0.7F, -1.2F, 2.1F, 1.3F, 2.4F)
                , PartPose.offset(3.05F, 12.6F, 3.6000000000000005F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        leg_l_0.zRot = -1 * motion * Mth.sin(limbSwing * 2.35F + 0.0000F) * 0.53F;
        leg_l_0.xRot = motion * Mth.cos(limbSwing * 2.35F + 0.0000F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 0.0000F) * 0.03F;
        leg_l_0.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 0.0000F)) * 0.95F;
        leg_l_1.zRot = -1 * motion * Mth.sin(limbSwing * 2.35F + 2.1800F) * 0.53F;
        leg_l_1.xRot = motion * Mth.cos(limbSwing * 2.35F + 2.1800F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 2.1800F) * 0.03F;
        leg_l_1.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 2.1800F)) * 0.95F;
        leg_l_2.zRot = -1 * motion * Mth.sin(limbSwing * 2.35F + 4.3600F) * 0.53F;
        leg_l_2.xRot = motion * Mth.cos(limbSwing * 2.35F + 4.3600F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 4.3600F) * 0.03F;
        leg_l_2.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 4.3600F)) * 0.95F;
        leg_r_0.zRot = 1 * motion * Mth.sin(limbSwing * 2.35F + 1.4000F) * 0.53F;
        leg_r_0.xRot = motion * Mth.cos(limbSwing * 2.35F + 1.4000F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 1.4000F) * 0.03F;
        leg_r_0.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 1.4000F)) * 0.95F;
        leg_r_1.zRot = 1 * motion * Mth.sin(limbSwing * 2.35F + 3.5800F) * 0.53F;
        leg_r_1.xRot = motion * Mth.cos(limbSwing * 2.35F + 3.5800F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 3.5800F) * 0.03F;
        leg_r_1.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 3.5800F)) * 0.95F;
        leg_r_2.zRot = 1 * motion * Mth.sin(limbSwing * 2.35F + 5.7600F) * 0.53F;
        leg_r_2.xRot = motion * Mth.cos(limbSwing * 2.35F + 5.7600F) * 0.23F + Mth.sin(ageInTicks * 0.43F + 5.7600F) * 0.03F;
        leg_r_2.y = 20.100F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.35F + 5.7600F)) * 0.95F;
        body.xRot = Mth.sin(ageInTicks * 0.16F) * 0.10F + motion * Mth.sin(limbSwing * 2.35F) * 0.16F + attack * 0.31F;
        body.zRot = Mth.sin(ageInTicks * 0.11F) * 0.055F + motion * Mth.sin(limbSwing * 1.175F) * 0.13F + recoil * 0.15F;
        body.y = 16.950F + Mth.sin(ageInTicks * 0.14F) * 0.12F + motion * Mth.cos(limbSwing * 2.35F) * 0.29F;
        head.xRot = Mth.sin(ageInTicks * 0.17F) * 0.09F - attack * 0.40F + motion * Mth.sin(limbSwing * 2.35F - 0.7F) * 0.13F;
        head.yRot = Mth.sin(ageInTicks * 0.11F) * 0.11F + motion * Mth.sin(limbSwing * 1.175F) * 0.12F;
        spore_sac_0.xRot = Mth.sin(ageInTicks * 0.20F + 0.000F) * 0.09F + motion * Mth.sin(limbSwing * 2.35F - 0.000F) * 0.15F + attack * 0.28F;
        spore_sac_0.zRot = Mth.sin(ageInTicks * 0.17F + 0.000F) * 0.10F + attack * Mth.sin(ageInTicks * 1.3F + 0.000F) * 0.21F;
        spore_sac_1.xRot = Mth.sin(ageInTicks * 0.20F + 0.800F) * 0.09F + motion * Mth.sin(limbSwing * 2.35F - 0.700F) * 0.15F + attack * 0.28F;
        spore_sac_1.zRot = Mth.sin(ageInTicks * 0.17F + 1.200F) * 0.10F + attack * Mth.sin(ageInTicks * 1.3F + 1.000F) * 0.21F;
        spore_sac_2.xRot = Mth.sin(ageInTicks * 0.20F + 1.600F) * 0.09F + motion * Mth.sin(limbSwing * 2.35F - 1.400F) * 0.15F + attack * 0.28F;
        spore_sac_2.zRot = Mth.sin(ageInTicks * 0.17F + 2.400F) * 0.10F + attack * Mth.sin(ageInTicks * 1.3F + 2.000F) * 0.21F;
    }
}
