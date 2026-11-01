package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SludgeCrawlerEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/sludge_crawler.json. */
public final class SludgeCrawlerModel<T extends SludgeCrawlerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "sludge_crawler"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart drip_0;
    private final ModelPart drip_1;
    private final ModelPart drip_2;
    private final ModelPart drip_3;
    private final ModelPart drip_4;
    private final ModelPart drip_5;
    private final ModelPart head;
    private final ModelPart tentacle_l;
    private final ModelPart tentacle_r;
    public SludgeCrawlerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.drip_0 = root.getChild("drip_0");
        this.drip_1 = root.getChild("drip_1");
        this.drip_2 = root.getChild("drip_2");
        this.drip_3 = root.getChild("drip_3");
        this.drip_4 = root.getChild("drip_4");
        this.drip_5 = root.getChild("drip_5");
        this.head = root.getChild("head");
        this.tentacle_l = root.getChild("tentacle_l");
        this.tentacle_r = root.getChild("tentacle_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-5.0F, -3.45F, -5.68F, 10.0F, 1.2F, 2.0F)
                .texOffs(30, 2).addBox(-4.6F, -3.85F, -3.68F, 9.2F, 1.2F, 2.0F)
                .texOffs(58, 2).addBox(-5.0F, -4.25F, -1.68F, 10.0F, 1.2F, 2.0F)
                .texOffs(86, 2).addBox(-4.6F, -3.45F, 0.32F, 9.2F, 1.2F, 2.0F)
                .texOffs(114, 2).addBox(-5.0F, -3.85F, 2.32F, 10.0F, 1.2F, 2.0F)
                .texOffs(142, 2).addBox(-4.6F, -4.25F, 4.32F, 9.2F, 1.2F, 2.0F)
                .texOffs(170, 2).addBox(-5.1F, -3.25F, -5.18F, 10.2F, 0.8F, 1.95F)
                .texOffs(200, 2).addBox(-4.95F, -3.6F, -3.53F, 9.9F, 0.8F, 1.95F)
                .texOffs(228, 2).addBox(-4.8F, -3.95F, -1.88F, 9.6F, 0.8F, 1.95F)
                .texOffs(256, 2).addBox(-4.65F, -3.25F, -0.23F, 9.3F, 0.8F, 1.95F)
                .texOffs(284, 2).addBox(-4.5F, -3.6F, 1.42F, 9.0F, 0.8F, 1.95F)
                .texOffs(310, 2).addBox(-4.35F, -3.95F, 3.07F, 8.7F, 0.8F, 1.95F)
                .texOffs(336, 2).addBox(-4.2F, -3.25F, 4.72F, 8.4F, 0.8F, 1.95F)
                .texOffs(362, 2).addBox(-4.7F, -3.0F, -6.68F, 9.4F, 5.0F, 3.1F)
                .texOffs(394, 2).addBox(-5.0F, -2.65F, -4.23F, 10.0F, 5.5F, 3.1F)
                .texOffs(426, 2).addBox(-5.3F, -3.0F, -1.78F, 10.6F, 5.0F, 3.1F)
                .texOffs(460, 2).addBox(-5.0F, -2.65F, 0.67F, 10.0F, 5.5F, 3.1F)
                .texOffs(2, 16).addBox(-4.7F, -3.0F, 3.12F, 9.4F, 5.0F, 3.1F)
                , PartPose.offset(0F, 19.15F, 0.68F));
        root.addOrReplaceChild("tentacle_l", CubeListBuilder.create()
                .texOffs(34, 16).addBox(-3.0F, -0.3F, -2.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(56, 16).addBox(-4.6F, 1.5F, -4.0F, 3.0F, 3.0F, 7.0F)
                .texOffs(80, 16).addBox(-4.8F, 0.8F, -3.5F, 1.1F, 0.36F, 5.1F)
                .texOffs(100, 16).addBox(-4.8F, 1.62F, -3.5F, 1.1F, 0.36F, 5.1F)
                .texOffs(120, 16).addBox(-4.8F, 2.44F, -3.5F, 1.1F, 0.36F, 5.1F)
                , PartPose.offset(-4F, 19.3F, 0.0F));
        root.addOrReplaceChild("tentacle_r", CubeListBuilder.create()
                .texOffs(140, 16).addBox(0.0F, -0.3F, -2.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(162, 16).addBox(2.0F, 1.5F, -4.0F, 3.0F, 3.0F, 7.0F)
                .texOffs(186, 16).addBox(3.1F, 0.8F, -3.5F, 1.1F, 0.36F, 5.1F)
                .texOffs(206, 16).addBox(3.1F, 1.62F, -3.5F, 1.1F, 0.36F, 5.1F)
                .texOffs(226, 16).addBox(3.1F, 2.44F, -3.5F, 1.1F, 0.36F, 5.1F)
                , PartPose.offset(4F, 19.3F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(246, 16).addBox(-4.0F, -2.5F, -3.5F, 8.0F, 5.0F, 4.0F)
                .texOffs(274, 16).addBox(-2.9F, -1.0F, -4.1F, 1.2F, 1.2F, 0.8F)
                .texOffs(284, 16).addBox(1.8F, -1.0F, -4.1F, 1.2F, 1.2F, 0.8F)
                .texOffs(294, 16).addBox(-2.3F, 1.0F, -4.3F, 4.6F, 1.0F, 0.8F)
                .texOffs(310, 16).addBox(-2.1F, 1.5F, -4.85F, 0.6F, 0.85F, 0.48F)
                .texOffs(318, 16).addBox(-1.0F, 1.5F, -4.85F, 0.6F, 0.85F, 0.48F)
                .texOffs(326, 16).addBox(0.1F, 1.5F, -4.85F, 0.6F, 0.85F, 0.48F)
                .texOffs(334, 16).addBox(1.2F, 1.5F, -4.85F, 0.6F, 0.85F, 0.48F)
                , PartPose.offset(0F, 19.5F, -2.0F));
        root.addOrReplaceChild("drip_0", CubeListBuilder.create()
                .texOffs(342, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 1.4F, 0.7F)
                , PartPose.offset(-3.65F, 20.8F, -3.65F));
        root.addOrReplaceChild("drip_1", CubeListBuilder.create()
                .texOffs(350, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 1.9F, 0.7F)
                , PartPose.offset(-2.15F, 20.8F, 0.35F));
        root.addOrReplaceChild("drip_2", CubeListBuilder.create()
                .texOffs(358, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 2.4F, 0.7F)
                , PartPose.offset(-0.65F, 20.8F, 4.35F));
        root.addOrReplaceChild("drip_3", CubeListBuilder.create()
                .texOffs(366, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 1.4F, 0.7F)
                , PartPose.offset(0.85F, 20.8F, -3.65F));
        root.addOrReplaceChild("drip_4", CubeListBuilder.create()
                .texOffs(374, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 1.9F, 0.7F)
                , PartPose.offset(2.35F, 20.8F, 0.35F));
        root.addOrReplaceChild("drip_5", CubeListBuilder.create()
                .texOffs(382, 16).addBox(-0.35F, 0.2F, -0.35F, 0.7F, 2.4F, 0.7F)
                , PartPose.offset(3.85F, 20.8F, 4.35F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        body.y = 19.150F + motion * (0.20F + 0.28F * Mth.cos(limbSwing * 1.72F)) + Mth.sin(ageInTicks * 0.11F) * 0.055F;
        body.z = 0.680F - motion * 0.65F * Mth.sin(limbSwing * 0.86F);
        body.xRot = Mth.sin(ageInTicks * 0.14F) * 0.045F + motion * Mth.sin(limbSwing * 1.72F) * 0.115F + recoil * 0.10F;
        body.zRot = Mth.sin(ageInTicks * 0.12F) * 0.035F + motion * Mth.sin(limbSwing * 0.86F) * 0.13F;
        head.xRot = Mth.sin(ageInTicks * 0.17F) * 0.055F + motion * Mth.sin(limbSwing * 1.72F - 0.8F) * 0.11F - attack * 0.42F;
        head.yRot = Mth.sin(ageInTicks * 0.08F) * 0.075F + motion * Mth.sin(limbSwing * 0.86F - 1.1F) * 0.11F;
        head.y = 19.50F + motion * 0.20F * Mth.sin(limbSwing * 1.72F - 0.7F);
        tentacle_l.xRot = Mth.sin(ageInTicks * 0.26F + 0.30F) * 0.13F + motion * Mth.sin(limbSwing * 1.72F - 0.60F) * 0.49F - attack * 0.65F;
        tentacle_l.yRot = 1 * (0.14F + motion * Mth.sin(limbSwing * 0.86F - 0.7F) * 0.27F);
        tentacle_l.zRot = 1 * motion * Mth.cos(limbSwing * 1.72F - 0.8F) * 0.11F;
        tentacle_r.xRot = Mth.sin(ageInTicks * 0.26F + 2.90F) * 0.13F + motion * Mth.sin(limbSwing * 1.72F - 1.80F) * 0.49F - attack * 0.65F;
        tentacle_r.yRot = -1 * (0.14F + motion * Mth.sin(limbSwing * 0.86F - 0.7F) * 0.27F);
        tentacle_r.zRot = -1 * motion * Mth.cos(limbSwing * 1.72F - 0.8F) * 0.11F;
        drip_0.xRot = motion * Mth.sin(limbSwing * 1.72F - 0.000F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 0.000F) * 0.04F;
        drip_0.zRot = Mth.sin(ageInTicks * 0.19F - 0.000F) * (0.06F + motion * 0.10F);
        drip_1.xRot = motion * Mth.sin(limbSwing * 1.72F - 0.380F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 0.570F) * 0.04F;
        drip_1.zRot = Mth.sin(ageInTicks * 0.19F - 0.510F) * (0.06F + motion * 0.10F);
        drip_2.xRot = motion * Mth.sin(limbSwing * 1.72F - 0.760F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 1.140F) * 0.04F;
        drip_2.zRot = Mth.sin(ageInTicks * 0.19F - 1.020F) * (0.06F + motion * 0.10F);
        drip_3.xRot = motion * Mth.sin(limbSwing * 1.72F - 1.140F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 1.710F) * 0.04F;
        drip_3.zRot = Mth.sin(ageInTicks * 0.19F - 1.530F) * (0.06F + motion * 0.10F);
        drip_4.xRot = motion * Mth.sin(limbSwing * 1.72F - 1.520F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 2.280F) * 0.04F;
        drip_4.zRot = Mth.sin(ageInTicks * 0.19F - 2.040F) * (0.06F + motion * 0.10F);
        drip_5.xRot = motion * Mth.sin(limbSwing * 1.72F - 1.900F) * 0.16F + Mth.sin(ageInTicks * 0.17F + 2.850F) * 0.04F;
        drip_5.zRot = Mth.sin(ageInTicks * 0.19F - 2.550F) * (0.06F + motion * 0.10F);
    }
}
