package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SewerLeechEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/sewer_leech.json. */
public final class SewerLeechModel<T extends SewerLeechEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "sewer_leech"), "main");
    private final ModelPart root;
    private final ModelPart mouth;
    private final ModelPart seg_0;
    private final ModelPart seg_1;
    private final ModelPart seg_2;
    private final ModelPart seg_3;
    private final ModelPart seg_4;
    private final ModelPart seg_5;
    private final ModelPart seg_6;
    private final ModelPart seg_7;
    public SewerLeechModel(ModelPart root) {
        this.root = root;
        this.mouth = root.getChild("mouth");
        this.seg_0 = root.getChild("seg_0");
        this.seg_1 = root.getChild("seg_1");
        this.seg_2 = root.getChild("seg_2");
        this.seg_3 = root.getChild("seg_3");
        this.seg_4 = root.getChild("seg_4");
        this.seg_5 = root.getChild("seg_5");
        this.seg_6 = root.getChild("seg_6");
        this.seg_7 = root.getChild("seg_7");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("seg_0", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-2.093F, -3.368F, 0.527F, 2.87F, 0.333F, 1.705F)
                .texOffs(16, 2).addBox(-2.47F, -2.892F, 0.31F, 1.885F, 0.286F, 1.317F)
                .texOffs(28, 2).addBox(-2.977F, -2.177F, 0.0F, 5.954F, 5.55F, 3.952F)
                .texOffs(52, 2).addBox(-2.382F, -3.249F, 0.279F, 4.763F, 0.952F, 3.255F)
                .texOffs(74, 2).addBox(-3.211F, -1.166F, 0.543F, 0.78F, 3.608F, 2.325F)
                .texOffs(86, 2).addBox(2.522F, -1.166F, 0.543F, 0.78F, 3.608F, 2.325F)
                .texOffs(98, 2).addBox(-1.5F, -3.11F, 0.0F, 3.0F, 0.4F, 1.0F)
                .texOffs(110, 2).addBox(-1.7F, 2.79F, 0.0F, 3.4F, 0.5F, 1.6F)
                , PartPose.offset(0F, 20.31F, -6.2F));
        root.addOrReplaceChild("seg_1", CubeListBuilder.create()
                .texOffs(126, 2).addBox(-0.585F, 1.845F, 1.552F, 1.17F, 1.785F, 1.24F)
                .texOffs(138, 2).addBox(-2.457F, -3.569F, 0.529F, 3.37F, 0.333F, 1.705F)
                .texOffs(154, 2).addBox(-2.99F, -3.272F, 0.312F, 1.885F, 0.286F, 1.317F)
                .texOffs(166, 2).addBox(-1.966F, 1.607F, 1.862F, 0.611F, 1.725F, 0.744F)
                .texOffs(174, 2).addBox(1.966F, 1.607F, 1.862F, 0.611F, 1.428F, 0.744F)
                .texOffs(182, 2).addBox(-3.42F, -2.557F, 0.002F, 7.176F, 5.99F, 3.952F)
                .texOffs(210, 2).addBox(-2.703F, -3.629F, 0.282F, 5.741F, 0.952F, 3.255F)
                .texOffs(234, 2).addBox(-3.654F, -1.546F, 0.545F, 0.78F, 3.894F, 2.325F)
                .texOffs(246, 2).addBox(3.301F, -1.546F, 0.545F, 0.78F, 3.894F, 2.325F)
                .texOffs(258, 2).addBox(-2.5F, -3.49F, 0.002F, 5.0F, 0.4F, 1.0F)
                , PartPose.offset(0F, 20.69F, -2.87F));
        root.addOrReplaceChild("seg_2", CubeListBuilder.create()
                .texOffs(274, 2).addBox(-2.821F, -3.808F, 0.522F, 3.869F, 0.333F, 1.705F)
                .texOffs(290, 2).addBox(-3.51F, -3.332F, 0.305F, 1.885F, 0.286F, 1.317F)
                .texOffs(302, 2).addBox(-3.965F, -2.617F, -0.005F, 8.398F, 6.431F, 3.952F)
                .texOffs(332, 2).addBox(-3.125F, -3.689F, 0.274F, 6.718F, 0.952F, 3.255F)
                .texOffs(358, 2).addBox(-4.199F, -1.606F, 0.538F, 0.78F, 4.18F, 2.325F)
                .texOffs(370, 2).addBox(3.978F, -1.606F, 0.538F, 0.78F, 4.18F, 2.325F)
                .texOffs(382, 2).addBox(-2.5F, -3.55F, -0.005F, 5.0F, 0.4F, 1.0F)
                .texOffs(398, 2).addBox(-1.7F, 2.35F, -0.005F, 3.4F, 0.5F, 1.6F)
                , PartPose.offset(0F, 20.75F, 0.47F));
        root.addOrReplaceChild("seg_3", CubeListBuilder.create()
                .texOffs(414, 2).addBox(-0.585F, 1.595F, 1.547F, 1.17F, 1.785F, 1.24F)
                .texOffs(426, 2).addBox(-3.185F, -3.819F, 0.525F, 4.368F, 0.333F, 1.705F)
                .texOffs(444, 2).addBox(-4.03F, -3.522F, 0.307F, 1.885F, 0.286F, 1.317F)
                .texOffs(456, 2).addBox(-2.548F, 1.357F, 1.857F, 0.611F, 1.725F, 0.744F)
                .texOffs(464, 2).addBox(2.548F, 1.357F, 1.857F, 0.611F, 1.428F, 0.744F)
                .texOffs(472, 2).addBox(-4.651F, -2.807F, -0.002F, 9.62F, 6.695F, 3.952F)
                .texOffs(2, 17).addBox(-3.689F, -3.879F, 0.277F, 7.696F, 0.952F, 3.255F)
                .texOffs(30, 17).addBox(-4.885F, -1.796F, 0.54F, 0.78F, 4.352F, 2.325F)
                .texOffs(42, 17).addBox(4.514F, -1.796F, 0.54F, 0.78F, 4.352F, 2.325F)
                .texOffs(54, 17).addBox(-2.5F, -3.74F, -0.003F, 5.0F, 0.4F, 1.0F)
                , PartPose.offset(0F, 20.94F, 3.8F));
        root.addOrReplaceChild("seg_4", CubeListBuilder.create()
                .texOffs(70, 17).addBox(-3.185F, -3.728F, 0.527F, 4.368F, 0.333F, 1.705F)
                .texOffs(88, 17).addBox(-4.03F, -3.252F, 0.31F, 1.885F, 0.286F, 1.317F)
                .texOffs(100, 17).addBox(-4.213F, -2.537F, 0.0F, 8.398F, 6.255F, 3.952F)
                .texOffs(130, 17).addBox(-3.374F, -3.609F, 0.279F, 6.718F, 0.952F, 3.255F)
                .texOffs(156, 17).addBox(-4.447F, -1.526F, 0.543F, 0.78F, 4.065F, 2.325F)
                .texOffs(168, 17).addBox(3.73F, -1.526F, 0.543F, 0.78F, 4.065F, 2.325F)
                .texOffs(180, 17).addBox(-2.5F, -3.47F, 0.0F, 5.0F, 0.4F, 1.0F)
                .texOffs(196, 17).addBox(-1.7F, 2.43F, 0.0F, 3.4F, 0.5F, 1.6F)
                , PartPose.offset(0F, 20.67F, 7.13F));
        root.addOrReplaceChild("seg_5", CubeListBuilder.create()
                .texOffs(212, 17).addBox(-0.585F, 1.845F, 1.553F, 1.17F, 1.785F, 1.24F)
                .texOffs(224, 17).addBox(-2.821F, -3.569F, 0.529F, 3.869F, 0.333F, 1.705F)
                .texOffs(240, 17).addBox(-3.51F, -3.272F, 0.313F, 1.885F, 0.286F, 1.317F)
                .texOffs(252, 17).addBox(-2.257F, 1.607F, 1.862F, 0.611F, 1.725F, 0.744F)
                .texOffs(260, 17).addBox(2.257F, 1.607F, 1.862F, 0.611F, 1.428F, 0.744F)
                .texOffs(268, 17).addBox(-3.765F, -2.557F, 0.003F, 7.176F, 5.814F, 3.952F)
                .texOffs(296, 17).addBox(-3.047F, -3.629F, 0.282F, 5.741F, 0.952F, 3.255F)
                .texOffs(320, 17).addBox(-3.999F, -1.546F, 0.545F, 0.78F, 3.779F, 2.325F)
                .texOffs(332, 17).addBox(2.956F, -1.546F, 0.545F, 0.78F, 3.779F, 2.325F)
                .texOffs(344, 17).addBox(-2.5F, -3.49F, 0.003F, 5.0F, 0.4F, 1.0F)
                , PartPose.offset(0F, 20.69F, 10.46F));
        root.addOrReplaceChild("seg_6", CubeListBuilder.create()
                .texOffs(360, 17).addBox(-2.457F, -3.328F, 0.532F, 3.37F, 0.333F, 1.705F)
                .texOffs(376, 17).addBox(-2.99F, -2.852F, 0.315F, 1.885F, 0.286F, 1.317F)
                .texOffs(388, 17).addBox(-3.21F, -2.137F, 0.005F, 5.954F, 5.374F, 3.952F)
                .texOffs(412, 17).addBox(-2.614F, -3.209F, 0.284F, 4.763F, 0.952F, 3.255F)
                .texOffs(434, 17).addBox(-3.444F, -1.126F, 0.548F, 0.78F, 3.493F, 2.325F)
                .texOffs(446, 17).addBox(2.289F, -1.126F, 0.548F, 0.78F, 3.493F, 2.325F)
                .texOffs(458, 17).addBox(-2.5F, -3.07F, 0.005F, 5.0F, 0.4F, 1.0F)
                .texOffs(474, 17).addBox(-1.7F, 2.83F, 0.005F, 3.4F, 0.5F, 1.6F)
                , PartPose.offset(0F, 20.27F, 13.79F));
        root.addOrReplaceChild("seg_7", CubeListBuilder.create()
                .texOffs(490, 17).addBox(-2.093F, -3.419F, 0.525F, 2.87F, 0.333F, 1.705F)
                .texOffs(2, 32).addBox(-2.47F, -3.122F, 0.308F, 1.885F, 0.286F, 1.317F)
                .texOffs(14, 32).addBox(-1.674F, 1.757F, 1.858F, 0.611F, 1.725F, 0.744F)
                .texOffs(22, 32).addBox(1.674F, 1.757F, 1.858F, 0.611F, 1.428F, 0.744F)
                .texOffs(30, 32).addBox(-2.514F, -2.407F, -0.002F, 4.732F, 4.934F, 3.952F)
                .texOffs(52, 32).addBox(-2.041F, -3.479F, 0.277F, 3.786F, 0.952F, 3.255F)
                .texOffs(72, 32).addBox(-2.748F, -1.396F, 0.54F, 0.78F, 3.207F, 2.325F)
                .texOffs(84, 32).addBox(1.763F, -1.396F, 0.54F, 0.78F, 3.207F, 2.325F)
                .texOffs(96, 32).addBox(-1.5F, -3.34F, -0.003F, 3.0F, 0.4F, 1.0F)
                , PartPose.offset(0F, 20.54F, 17.13F));
        root.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(108, 32).addBox(-5.2F, -3.842F, -4.655F, 10.4F, 6.069F, 4.65F)
                .texOffs(144, 32).addBox(-3.9F, -2.771F, -5.895F, 7.8F, 4.046F, 2.17F)
                .texOffs(170, 32).addBox(-4.94F, -4.199F, -5.585F, 9.88F, 1.309F, 2.635F)
                .texOffs(200, 32).addBox(-4.94F, 0.799F, -5.585F, 9.88F, 1.309F, 2.635F)
                .texOffs(230, 32).addBox(-5.07F, -3.604F, -6.05F, 1.235F, 4.998F, 2.17F)
                .texOffs(244, 32).addBox(3.835F, -3.604F, -6.05F, 1.235F, 4.998F, 2.17F)
                .texOffs(258, 32).addBox(-2.99F, -2.473F, -7.135F, 5.98F, 3.451F, 1.24F)
                .texOffs(278, 32).addBox(-2.99F, 2.048F, -5.43F, 0.442F, 1.725F, 0.589F)
                .texOffs(286, 32).addBox(-0.975F, 2.048F, -5.43F, 0.442F, 2.083F, 0.589F)
                .texOffs(294, 32).addBox(1.04F, 2.048F, -5.43F, 0.442F, 2.439F, 0.589F)
                .texOffs(302, 32).addBox(-4.68F, -4.496F, -6.67F, 9.36F, 0.928F, 2.093F)
                .texOffs(332, 32).addBox(-4.68F, 1.87F, -6.67F, 9.36F, 0.773F, 2.093F)
                .texOffs(362, 32).addBox(-3.15F, -2.37F, -9.44F, 6.3F, 3.8F, 0.48F)
                .texOffs(382, 32).addBox(-2.1F, -1.72F, -9.7F, 4.2F, 2.4F, 0.43F)
                .texOffs(398, 32).addBox(-5.4F, -3.87F, -6.63F, 10.8F, 0.65F, 0.95F)
                .texOffs(426, 32).addBox(-5.4F, 1.78F, -6.63F, 10.8F, 0.75F, 0.95F)
                .texOffs(454, 32).addBox(-5.4F, -3.87F, -6.63F, 0.75F, 6.4F, 0.95F)
                .texOffs(462, 32).addBox(4.65F, -3.87F, -6.63F, 0.75F, 6.4F, 0.95F)
                .texOffs(470, 32).addBox(-4.9F, -3.57F, -7.58F, 9.8F, 0.65F, 0.95F)
                .texOffs(2, 48).addBox(-4.9F, 1.48F, -7.58F, 9.8F, 0.75F, 0.95F)
                .texOffs(28, 48).addBox(-4.9F, -3.57F, -7.58F, 0.75F, 5.8F, 0.95F)
                .texOffs(36, 48).addBox(4.15F, -3.57F, -7.58F, 0.75F, 5.8F, 0.95F)
                .texOffs(44, 48).addBox(-4.35F, -3.17F, -8.6F, 8.7F, 0.65F, 0.95F)
                .texOffs(68, 48).addBox(-4.35F, 1.08F, -8.6F, 8.7F, 0.75F, 0.95F)
                .texOffs(92, 48).addBox(-4.35F, -3.17F, -8.6F, 0.75F, 5.0F, 0.95F)
                .texOffs(100, 48).addBox(3.6F, -3.17F, -8.6F, 0.75F, 5.0F, 0.95F)
                .texOffs(108, 48).addBox(-5.55F, -2.32F, -6.48F, 0.51F, 3.45F, 1.5F)
                .texOffs(118, 48).addBox(-5.91F, -1.75F, -6.48F, 0.51F, 3.15F, 1.5F)
                .texOffs(128, 48).addBox(-6.27F, -1.18F, -6.48F, 0.51F, 2.85F, 1.5F)
                .texOffs(138, 48).addBox(5.05F, -2.32F, -6.48F, 0.51F, 3.45F, 1.5F)
                .texOffs(148, 48).addBox(5.41F, -1.75F, -6.48F, 0.51F, 3.15F, 1.5F)
                .texOffs(158, 48).addBox(5.77F, -1.18F, -6.48F, 0.51F, 2.85F, 1.5F)
                .texOffs(168, 48).addBox(-2.65F, -3.44F, -10.78F, 1.15F, 1.5F, 1.1F)
                .texOffs(180, 48).addBox(-2.32F, -1.94F, -11.2F, 0.52F, 0.65F, 0.83F)
                .texOffs(188, 48).addBox(-2.65F, 0.48F, -10.78F, 1.15F, 1.38F, 1.1F)
                .texOffs(200, 48).addBox(-2.32F, -0.1F, -11.19F, 0.5F, 0.68F, 0.82F)
                .texOffs(208, 48).addBox(1.6F, -3.44F, -10.78F, 1.15F, 1.5F, 1.1F)
                .texOffs(220, 48).addBox(1.93F, -1.94F, -11.2F, 0.52F, 0.65F, 0.83F)
                .texOffs(228, 48).addBox(1.6F, 0.48F, -10.78F, 1.15F, 1.38F, 1.1F)
                .texOffs(240, 48).addBox(1.93F, -0.1F, -11.19F, 0.5F, 0.68F, 0.82F)
                .texOffs(248, 48).addBox(-4.42F, -1.82F, -10.83F, 0.97F, 0.91F, 1.12F)
                .texOffs(258, 48).addBox(-4.23F, -1.63F, -11.24F, 0.58F, 0.5F, 0.8F)
                .texOffs(266, 48).addBox(3.45F, -1.82F, -10.83F, 0.97F, 0.91F, 1.12F)
                .texOffs(276, 48).addBox(3.64F, -1.63F, -11.24F, 0.58F, 0.5F, 0.8F)
                , PartPose.offset(0F, 21.32F, -5.42F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        seg_0.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 0.000F) * (0.038F + motion * 0.14F);
        seg_0.xRot = Mth.sin(ageInTicks * 0.22F - 0.000F) * (0.025F + motion * 0.08F);
        seg_1.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 0.550F) * (0.052F + motion * 0.14F);
        seg_1.xRot = Mth.sin(ageInTicks * 0.22F - 0.420F) * (0.025F + motion * 0.08F);
        seg_2.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 1.100F) * (0.066F + motion * 0.14F);
        seg_2.xRot = Mth.sin(ageInTicks * 0.22F - 0.840F) * (0.025F + motion * 0.08F);
        seg_3.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 1.650F) * (0.080F + motion * 0.14F);
        seg_3.xRot = Mth.sin(ageInTicks * 0.22F - 1.260F) * (0.025F + motion * 0.08F);
        seg_4.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 2.200F) * (0.094F + motion * 0.14F);
        seg_4.xRot = Mth.sin(ageInTicks * 0.22F - 1.680F) * (0.025F + motion * 0.08F);
        seg_5.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 2.750F) * (0.108F + motion * 0.14F);
        seg_5.xRot = Mth.sin(ageInTicks * 0.22F - 2.100F) * (0.025F + motion * 0.08F);
        seg_6.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 3.300F) * (0.122F + motion * 0.14F);
        seg_6.xRot = Mth.sin(ageInTicks * 0.22F - 2.520F) * (0.025F + motion * 0.08F);
        seg_7.yRot = Mth.sin(ageInTicks * (entity.isInWater() ? 0.32F : 0.16F) - 3.850F) * (0.136F + motion * 0.14F);
        seg_7.xRot = Mth.sin(ageInTicks * 0.22F - 2.940F) * (0.025F + motion * 0.08F);
        mouth.xRot = Mth.sin(ageInTicks * 0.21F) * 0.12F - attack * 0.55F + recoil * 0.25F;
        mouth.yRot = Mth.sin(ageInTicks * 0.13F) * 0.04F;
        seg_0.y = 20.310F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 0.000F)) * motion * 0.55F;
        seg_0.z = -6.200F + Mth.sin(limbSwing * 1.72F - 0.000F) * motion * 0.40F;
        seg_1.y = 20.690F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 0.600F)) * motion * 0.55F;
        seg_1.z = -2.870F + Mth.sin(limbSwing * 1.72F - 0.600F) * motion * 0.40F;
        seg_2.y = 20.750F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 1.200F)) * motion * 0.55F;
        seg_2.z = 0.470F + Mth.sin(limbSwing * 1.72F - 1.200F) * motion * 0.40F;
        seg_3.y = 20.940F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 1.800F)) * motion * 0.55F;
        seg_3.z = 3.800F + Mth.sin(limbSwing * 1.72F - 1.800F) * motion * 0.40F;
        seg_4.y = 20.670F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 2.400F)) * motion * 0.55F;
        seg_4.z = 7.130F + Mth.sin(limbSwing * 1.72F - 2.400F) * motion * 0.40F;
        seg_5.y = 20.690F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 3.000F)) * motion * 0.55F;
        seg_5.z = 10.460F + Mth.sin(limbSwing * 1.72F - 3.000F) * motion * 0.40F;
        seg_6.y = 20.270F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 3.600F)) * motion * 0.55F;
        seg_6.z = 13.790F + Mth.sin(limbSwing * 1.72F - 3.600F) * motion * 0.40F;
        seg_7.y = 20.540F - Math.max(0.0F, Mth.sin(limbSwing * 1.72F - 4.200F)) * motion * 0.55F;
        seg_7.z = 17.130F + Mth.sin(limbSwing * 1.72F - 4.200F) * motion * 0.40F;
    }
}
