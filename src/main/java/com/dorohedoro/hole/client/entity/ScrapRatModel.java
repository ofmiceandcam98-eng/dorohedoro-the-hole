package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.ScrapRatEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/scrap_rat.json. */
public final class ScrapRatModel<T extends ScrapRatEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "scrap_rat"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart paw_l_0;
    private final ModelPart paw_l_1;
    private final ModelPart paw_r_0;
    private final ModelPart paw_r_1;
    private final ModelPart tail_0;
    private final ModelPart tail_1;
    private final ModelPart tail_2;
    private final ModelPart tail_3;
    public ScrapRatModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.paw_l_0 = root.getChild("paw_l_0");
        this.paw_l_1 = root.getChild("paw_l_1");
        this.paw_r_0 = root.getChild("paw_r_0");
        this.paw_r_1 = root.getChild("paw_r_1");
        this.tail_0 = root.getChild("tail_0");
        this.tail_1 = root.getChild("tail_1");
        this.tail_2 = root.getChild("tail_2");
        this.tail_3 = root.getChild("tail_3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-3.2F, 2.89F, -5.05F, 6.4F, 1.1F, 8.7F)
                .texOffs(38, 2).addBox(-3.698F, 2.74F, -5.15F, 0.62F, 1.2F, 1.0F)
                .texOffs(46, 2).addBox(3.251F, 2.74F, -5.15F, 0.62F, 1.2F, 1.0F)
                .texOffs(54, 2).addBox(-3.698F, 2.74F, -3.7F, 0.62F, 1.2F, 1.0F)
                .texOffs(62, 2).addBox(3.251F, 2.74F, -3.7F, 0.62F, 1.2F, 1.0F)
                .texOffs(70, 2).addBox(-3.698F, 2.74F, -2.25F, 0.62F, 1.2F, 1.0F)
                .texOffs(78, 2).addBox(3.251F, 2.74F, -2.25F, 0.62F, 1.2F, 1.0F)
                .texOffs(86, 2).addBox(-3.698F, 2.74F, -0.8F, 0.62F, 1.2F, 1.0F)
                .texOffs(94, 2).addBox(3.251F, 2.74F, -0.8F, 0.62F, 1.2F, 1.0F)
                .texOffs(102, 2).addBox(-3.698F, 2.74F, 0.65F, 0.62F, 1.2F, 1.0F)
                .texOffs(110, 2).addBox(3.251F, 2.74F, 0.65F, 0.62F, 1.2F, 1.0F)
                .texOffs(118, 2).addBox(-3.698F, 2.74F, 2.1F, 0.62F, 1.2F, 1.0F)
                .texOffs(126, 2).addBox(3.251F, 2.74F, 2.1F, 0.62F, 1.2F, 1.0F)
                .texOffs(134, 2).addBox(-3.698F, 2.74F, 3.55F, 0.62F, 1.2F, 1.0F)
                .texOffs(142, 2).addBox(3.251F, 2.74F, 3.55F, 0.62F, 1.2F, 1.0F)
                .texOffs(150, 2).addBox(-3.674F, -2.432F, -6.75F, 7.349F, 6.122F, 2.4F)
                .texOffs(176, 2).addBox(-3.994F, -2.21F, -6.47F, 0.65F, 0.95F, 1.2F)
                .texOffs(186, 2).addBox(-3.994F, -1.01F, -6.22F, 0.65F, 0.95F, 1.2F)
                .texOffs(196, 2).addBox(-3.994F, 0.19F, -5.97F, 0.65F, 0.95F, 1.2F)
                .texOffs(206, 2).addBox(3.234F, -2.21F, -6.47F, 0.65F, 0.95F, 1.2F)
                .texOffs(216, 2).addBox(3.234F, -1.01F, -6.22F, 0.65F, 0.95F, 1.2F)
                .texOffs(226, 2).addBox(3.234F, 0.19F, -5.97F, 0.65F, 0.95F, 1.2F)
                .texOffs(236, 2).addBox(-4.076F, -2.877F, -4.9F, 8.151F, 6.567F, 2.4F)
                .texOffs(264, 2).addBox(-4.396F, -2.21F, -4.62F, 0.65F, 0.95F, 1.2F)
                .texOffs(274, 2).addBox(-4.396F, -1.01F, -4.37F, 0.65F, 0.95F, 1.2F)
                .texOffs(284, 2).addBox(-4.396F, 0.19F, -4.12F, 0.65F, 0.95F, 1.2F)
                .texOffs(294, 2).addBox(3.636F, -2.21F, -4.62F, 0.65F, 0.95F, 1.2F)
                .texOffs(304, 2).addBox(3.636F, -1.01F, -4.37F, 0.65F, 0.95F, 1.2F)
                .texOffs(314, 2).addBox(3.636F, 0.19F, -4.12F, 0.65F, 0.95F, 1.2F)
                .texOffs(324, 2).addBox(-2.445F, -3.677F, -4.8F, 4.891F, 0.45F, 1.3F)
                .texOffs(342, 2).addBox(-4.333F, -3.151F, -3.05F, 8.666F, 6.841F, 2.4F)
                .texOffs(370, 2).addBox(-4.653F, -2.21F, -2.77F, 0.65F, 0.95F, 1.2F)
                .texOffs(380, 2).addBox(-4.653F, -1.01F, -2.52F, 0.65F, 0.95F, 1.2F)
                .texOffs(390, 2).addBox(-4.653F, 0.19F, -2.27F, 0.65F, 0.95F, 1.2F)
                .texOffs(400, 2).addBox(3.893F, -2.21F, -2.77F, 0.65F, 0.95F, 1.2F)
                .texOffs(410, 2).addBox(3.893F, -1.01F, -2.52F, 0.65F, 0.95F, 1.2F)
                .texOffs(420, 2).addBox(3.893F, 0.19F, -2.27F, 0.65F, 0.95F, 1.2F)
                .texOffs(430, 2).addBox(-4.396F, -3.199F, -1.2F, 8.792F, 6.889F, 2.4F)
                .texOffs(458, 2).addBox(-4.716F, -2.21F, -0.92F, 0.65F, 0.95F, 1.2F)
                .texOffs(468, 2).addBox(-4.716F, -1.01F, -0.67F, 0.65F, 0.95F, 1.2F)
                .texOffs(478, 2).addBox(-4.716F, 0.19F, -0.42F, 0.65F, 0.95F, 1.2F)
                .texOffs(488, 2).addBox(3.956F, -2.21F, -0.92F, 0.65F, 0.95F, 1.2F)
                .texOffs(498, 2).addBox(3.956F, -1.01F, -0.67F, 0.65F, 0.95F, 1.2F)
                .texOffs(2, 17).addBox(3.956F, 0.19F, -0.42F, 0.65F, 0.95F, 1.2F)
                .texOffs(12, 17).addBox(-2.637F, -3.999F, -1.1F, 5.275F, 0.45F, 1.3F)
                .texOffs(32, 17).addBox(-4.251F, -3.011F, 0.65F, 8.503F, 6.701F, 2.4F)
                .texOffs(60, 17).addBox(-4.571F, -2.21F, 0.93F, 0.65F, 0.95F, 1.2F)
                .texOffs(70, 17).addBox(-4.571F, -1.01F, 1.18F, 0.65F, 0.95F, 1.2F)
                .texOffs(80, 17).addBox(-4.571F, 0.19F, 1.43F, 0.65F, 0.95F, 1.2F)
                .texOffs(90, 17).addBox(3.811F, -2.21F, 0.93F, 0.65F, 0.95F, 1.2F)
                .texOffs(100, 17).addBox(3.811F, -1.01F, 1.18F, 0.65F, 0.95F, 1.2F)
                .texOffs(110, 17).addBox(3.811F, 0.19F, 1.43F, 0.65F, 0.95F, 1.2F)
                .texOffs(120, 17).addBox(-3.928F, -2.625F, 2.5F, 7.857F, 6.315F, 2.4F)
                .texOffs(146, 17).addBox(-4.248F, -2.21F, 2.78F, 0.65F, 0.95F, 1.2F)
                .texOffs(156, 17).addBox(-4.248F, -1.01F, 3.03F, 0.65F, 0.95F, 1.2F)
                .texOffs(166, 17).addBox(-4.248F, 0.19F, 3.28F, 0.65F, 0.95F, 1.2F)
                .texOffs(176, 17).addBox(3.488F, -2.21F, 2.78F, 0.65F, 0.95F, 1.2F)
                .texOffs(186, 17).addBox(3.488F, -1.01F, 3.03F, 0.65F, 0.95F, 1.2F)
                .texOffs(196, 17).addBox(3.488F, 0.19F, 3.28F, 0.65F, 0.95F, 1.2F)
                .texOffs(206, 17).addBox(-2.357F, -3.425F, 2.6F, 4.714F, 0.45F, 1.3F)
                .texOffs(224, 17).addBox(-3.491F, -2.118F, 4.35F, 6.982F, 5.808F, 2.4F)
                .texOffs(248, 17).addBox(-3.811F, -2.21F, 4.63F, 0.65F, 0.95F, 1.2F)
                .texOffs(258, 17).addBox(-3.811F, -1.01F, 4.88F, 0.65F, 0.95F, 1.2F)
                .texOffs(268, 17).addBox(-3.811F, 0.19F, 5.13F, 0.65F, 0.95F, 1.2F)
                .texOffs(278, 17).addBox(3.051F, -2.21F, 4.63F, 0.65F, 0.95F, 1.2F)
                .texOffs(288, 17).addBox(3.051F, -1.01F, 4.88F, 0.65F, 0.95F, 1.2F)
                .texOffs(298, 17).addBox(3.051F, 0.19F, 5.13F, 0.65F, 0.95F, 1.2F)
                , PartPose.offset(0F, 18.01F, 2.05F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(308, 17).addBox(-3.5F, 2.05F, -2.8F, 2.0F, 2.8F, 3.0F)
                .texOffs(322, 17).addBox(1.5F, 2.05F, -2.8F, 2.0F, 2.8F, 3.0F)
                .texOffs(336, 17).addBox(-1.8F, 1.15F, -8.0F, 3.6F, 2.3F, 4.0F)
                .texOffs(356, 17).addBox(-0.8F, 1.65F, -8.6F, 1.6F, 1.2F, 1.0F)
                .texOffs(366, 17).addBox(-4.2F, -4.85F, -2.3F, 2.8F, 3.5F, 1.5F)
                .texOffs(380, 17).addBox(-3.7F, -4.45F, -2.85F, 1.8F, 2.2F, 0.65F)
                .texOffs(390, 17).addBox(-3.3F, -0.55F, -3.8F, 1.2F, 1.15F, 1.4F)
                .texOffs(402, 17).addBox(-5.2F, 2.65F, -8.5F, 3.4F, 0.35F, 0.4F)
                .texOffs(416, 17).addBox(-5.2F, 3.3F, -8.5F, 3.4F, 0.35F, 0.4F)
                .texOffs(430, 17).addBox(1.4F, -4.85F, -2.3F, 2.8F, 3.5F, 1.5F)
                .texOffs(444, 17).addBox(1.9F, -4.45F, -2.85F, 1.8F, 2.2F, 0.65F)
                .texOffs(454, 17).addBox(2.1F, -0.55F, -3.8F, 1.2F, 1.15F, 1.4F)
                .texOffs(466, 17).addBox(1.8F, 2.65F, -8.5F, 3.4F, 0.35F, 0.4F)
                .texOffs(480, 17).addBox(1.8F, 3.3F, -8.5F, 3.4F, 0.35F, 0.4F)
                .texOffs(494, 17).addBox(-3.55F, -4.7F, -3.1F, 1.4F, 0.65F, 0.58F)
                .texOffs(2, 31).addBox(-3.95F, 1.45F, -2.7F, 1.3F, 1.5F, 2.5F)
                .texOffs(16, 31).addBox(-3.45F, -0.65F, -3.2F, 0.37F, 0.35F, 0.45F)
                .texOffs(24, 31).addBox(-6.3F, 1.95F, -9.7F, 3.0F, 0.22F, 0.34F)
                .texOffs(36, 31).addBox(-6.3F, 2.6F, -9.38F, 3.0F, 0.22F, 0.34F)
                .texOffs(48, 31).addBox(-6.3F, 3.25F, -9.06F, 3.0F, 0.22F, 0.34F)
                .texOffs(60, 31).addBox(2.05F, -4.7F, -3.1F, 1.4F, 0.65F, 0.58F)
                .texOffs(70, 31).addBox(2.55F, 1.45F, -2.7F, 1.3F, 1.5F, 2.5F)
                .texOffs(84, 31).addBox(2.35F, -0.65F, -3.2F, 0.37F, 0.35F, 0.45F)
                .texOffs(92, 31).addBox(3.3F, 1.95F, -9.7F, 3.0F, 0.22F, 0.34F)
                .texOffs(104, 31).addBox(3.3F, 2.6F, -9.38F, 3.0F, 0.22F, 0.34F)
                .texOffs(116, 31).addBox(3.3F, 3.25F, -9.06F, 3.0F, 0.22F, 0.34F)
                .texOffs(128, 31).addBox(-0.82F, 1.35F, -9.4F, 1.65F, 1.25F, 0.85F)
                .texOffs(138, 31).addBox(-1.2F, 2.05F, -8.8F, 2.4F, 0.85F, 1.0F)
                .texOffs(150, 31).addBox(-3.1F, -1.85F, -4.9F, 6.2F, 4.5F, 5.4F)
                .texOffs(180, 31).addBox(-2.5F, -2.45F, -5.8F, 5.0F, 2.35F, 3.9F)
                .texOffs(202, 31).addBox(-2.4F, 2.69F, -7.2F, 4.8F, 1.05F, 3.3F)
                .texOffs(224, 31).addBox(-1.4F, 2.3F, -9.0F, 2.8F, 0.8F, 1.6F)
                , PartPose.offset(0F, 18.45F, -4.8F));
        root.addOrReplaceChild("paw_l_0", CubeListBuilder.create()
                .texOffs(238, 31).addBox(-1.9F, -0.2F, -0.63F, 1.5F, 3.3F, 2.4F)
                .texOffs(252, 31).addBox(-2.1F, 3.3F, -1.63F, 2.1F, 1.0F, 3.5F)
                .texOffs(270, 31).addBox(-2.45F, 4.15F, -1.63F, 0.42F, 0.45F, 1.3F)
                .texOffs(280, 31).addBox(-1.97F, 4.15F, -1.75F, 0.42F, 0.45F, 1.3F)
                .texOffs(290, 31).addBox(-1.49F, 4.15F, -1.87F, 0.42F, 0.45F, 1.3F)
                , PartPose.offset(-2.1F, 20.4F, -2.37F));
        root.addOrReplaceChild("paw_l_1", CubeListBuilder.create()
                .texOffs(300, 31).addBox(-1.9F, -0.2F, -0.68F, 1.5F, 3.3F, 2.4F)
                .texOffs(314, 31).addBox(-2.1F, 3.3F, -1.68F, 2.1F, 1.0F, 3.5F)
                .texOffs(332, 31).addBox(-2.45F, 4.15F, -1.58F, 0.42F, 0.45F, 1.3F)
                .texOffs(342, 31).addBox(-1.97F, 4.15F, -1.7F, 0.42F, 0.45F, 1.3F)
                .texOffs(352, 31).addBox(-1.49F, 4.15F, -1.82F, 0.42F, 0.45F, 1.3F)
                , PartPose.offset(-2.1F, 20.4F, 5.68F));
        root.addOrReplaceChild("paw_r_0", CubeListBuilder.create()
                .texOffs(362, 31).addBox(0.2F, -0.2F, -0.63F, 1.5F, 3.3F, 2.4F)
                .texOffs(376, 31).addBox(0.0F, 3.3F, -1.63F, 2.1F, 1.0F, 3.5F)
                .texOffs(394, 31).addBox(0.25F, 4.15F, -1.63F, 0.42F, 0.45F, 1.3F)
                .texOffs(404, 31).addBox(0.73F, 4.15F, -1.75F, 0.42F, 0.45F, 1.3F)
                .texOffs(414, 31).addBox(1.21F, 4.15F, -1.87F, 0.42F, 0.45F, 1.3F)
                , PartPose.offset(2.3F, 20.4F, -2.37F));
        root.addOrReplaceChild("paw_r_1", CubeListBuilder.create()
                .texOffs(424, 31).addBox(0.2F, -0.2F, -0.68F, 1.5F, 3.3F, 2.4F)
                .texOffs(438, 31).addBox(0.0F, 3.3F, -1.68F, 2.1F, 1.0F, 3.5F)
                .texOffs(456, 31).addBox(0.25F, 4.15F, -1.58F, 0.42F, 0.45F, 1.3F)
                .texOffs(466, 31).addBox(0.73F, 4.15F, -1.7F, 0.42F, 0.45F, 1.3F)
                .texOffs(476, 31).addBox(1.21F, 4.15F, -1.82F, 0.42F, 0.45F, 1.3F)
                , PartPose.offset(2.3F, 20.4F, 5.68F));
        root.addOrReplaceChild("tail_0", CubeListBuilder.create()
                .texOffs(486, 31).addBox(-0.85F, -0.8F, 0.0F, 1.7F, 1.6F, 4.0F)
                , PartPose.offset(0.0F, 20.8F, 6.5F));
        root.addOrReplaceChild("tail_1", CubeListBuilder.create()
                .texOffs(2, 46).addBox(-0.7F, -0.66F, 0.0F, 1.4F, 1.32F, 4.0F)
                , PartPose.offset(-0.15F, 20.91F, 9.9F));
        root.addOrReplaceChild("tail_2", CubeListBuilder.create()
                .texOffs(18, 46).addBox(-0.55F, -0.52F, 0.0F, 1.1F, 1.04F, 4.0F)
                , PartPose.offset(-0.3F, 21.02F, 13.3F));
        root.addOrReplaceChild("tail_3", CubeListBuilder.create()
                .texOffs(34, 46).addBox(-0.59F, 0.55F, 0.0F, 0.8F, 0.76F, 4.0F)
                .texOffs(48, 46).addBox(-0.12F, -1.3F, 0.5F, 0.72F, 2.2F, 1.25F)
                , PartPose.offset(-0.26F, 20.2F, 16.7F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        paw_l_0.xRot = Mth.sin(limbSwing * 1.88F + 0.0000F) * motion * 0.56F;
        paw_r_0.xRot = Mth.sin(limbSwing * 1.88F + 3.1416F) * motion * 0.56F;
        paw_l_1.xRot = Mth.sin(limbSwing * 1.88F + 3.1416F) * motion * 0.56F;
        paw_r_1.xRot = Mth.sin(limbSwing * 1.88F + 6.2832F) * motion * 0.56F;
        tail_0.yRot = Mth.sin(ageInTicks * 0.17F - 0.000F) * 0.080F + Mth.sin(limbSwing * 0.91F - 0.000F) * motion * 0.11F;
        tail_0.xRot = Mth.sin(ageInTicks * 0.11F + 0.000F) * 0.06F;
        tail_1.yRot = Mth.sin(ageInTicks * 0.17F - 0.370F) * 0.115F + Mth.sin(limbSwing * 0.91F - 0.450F) * motion * 0.11F;
        tail_1.xRot = Mth.sin(ageInTicks * 0.11F + 0.400F) * 0.06F;
        tail_2.yRot = Mth.sin(ageInTicks * 0.17F - 0.740F) * 0.150F + Mth.sin(limbSwing * 0.91F - 0.900F) * motion * 0.11F;
        tail_2.xRot = Mth.sin(ageInTicks * 0.11F + 0.800F) * 0.06F;
        tail_3.yRot = Mth.sin(ageInTicks * 0.17F - 1.110F) * 0.185F + Mth.sin(limbSwing * 0.91F - 1.350F) * motion * 0.11F;
        tail_3.xRot = Mth.sin(ageInTicks * 0.11F + 1.200F) * 0.06F;
        head.xRot = -0.07F + Mth.sin(ageInTicks * 0.35F) * (0.10F - motion * 0.07F) - attack * 0.19F;
        head.yRot = Mth.sin(ageInTicks * 0.07F) * 0.16F * (1.0F - motion * 0.65F);
        body.xRot = Mth.sin(limbSwing * 3.75F) * motion * 0.07F + recoil * 0.14F;
        paw_l_0.y = 20.400F - Math.max(0.0F, Mth.sin(limbSwing * 1.88F + 0.0000F)) * motion * 0.92F;
        paw_l_1.y = 20.400F - Math.max(0.0F, Mth.sin(limbSwing * 1.88F + 3.1416F)) * motion * 0.92F;
        paw_r_0.y = 20.400F - Math.max(0.0F, Mth.sin(limbSwing * 1.88F + 3.1416F)) * motion * 0.92F;
        paw_r_1.y = 20.400F - Math.max(0.0F, Mth.sin(limbSwing * 1.88F + 6.2832F)) * motion * 0.92F;
    }
}
