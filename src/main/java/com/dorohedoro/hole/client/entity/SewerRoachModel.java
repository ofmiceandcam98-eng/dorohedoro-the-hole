package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SewerRoachEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/sewer_roach.json. */
public final class SewerRoachModel<T extends SewerRoachEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "sewer_roach"), "main");
    private final ModelPart root;
    private final ModelPart antenna_l;
    private final ModelPart antenna_r;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leg_l_0;
    private final ModelPart leg_l_1;
    private final ModelPart leg_l_2;
    private final ModelPart leg_r_0;
    private final ModelPart leg_r_1;
    private final ModelPart leg_r_2;
    public SewerRoachModel(ModelPart root) {
        this.root = root;
        this.antenna_l = root.getChild("antenna_l");
        this.antenna_r = root.getChild("antenna_r");
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leg_l_0 = root.getChild("leg_l_0");
        this.leg_l_1 = root.getChild("leg_l_1");
        this.leg_l_2 = root.getChild("leg_l_2");
        this.leg_r_0 = root.getChild("leg_r_0");
        this.leg_r_1 = root.getChild("leg_r_1");
        this.leg_r_2 = root.getChild("leg_r_2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-5.0F, -2.46F, -9.33F, 10.0F, 3.0F, 5.0F)
                .texOffs(36, 2).addBox(-4.0F, 1.54F, -6.33F, 8.0F, 2.0F, 14.0F)
                .texOffs(84, 2).addBox(-3.664F, -1.86F, -5.33F, 7.328F, 4.7F, 2.4F)
                .texOffs(110, 2).addBox(-4.164F, -3.31F, -5.33F, 4.004F, 1.2F, 2.1F)
                .texOffs(130, 2).addBox(0.15F, -3.31F, -5.33F, 4.014F, 1.2F, 2.1F)
                .texOffs(150, 2).addBox(-4.284F, -3.36F, -5.33F, 0.66F, 0.55F, 1.85F)
                .texOffs(160, 2).addBox(3.684F, -3.36F, -5.33F, 0.66F, 0.55F, 1.85F)
                .texOffs(170, 2).addBox(-2.964F, -3.54F, -4.98F, 0.52F, 0.26F, 1.55F)
                .texOffs(180, 2).addBox(2.364F, -3.54F, -4.98F, 0.52F, 0.26F, 1.55F)
                .texOffs(190, 2).addBox(-4.204F, -1.86F, -3.58F, 8.408F, 4.7F, 2.4F)
                .texOffs(218, 2).addBox(-4.704F, -3.31F, -3.58F, 4.544F, 1.2F, 2.1F)
                .texOffs(238, 2).addBox(0.15F, -3.31F, -3.58F, 4.554F, 1.2F, 2.1F)
                .texOffs(258, 2).addBox(-4.824F, -3.36F, -3.58F, 0.66F, 0.55F, 1.85F)
                .texOffs(268, 2).addBox(4.224F, -3.36F, -3.58F, 0.66F, 0.55F, 1.85F)
                .texOffs(278, 2).addBox(-4.744F, -1.86F, -1.83F, 9.488F, 4.7F, 2.4F)
                .texOffs(308, 2).addBox(-5.244F, -3.31F, -1.83F, 5.084F, 1.2F, 2.1F)
                .texOffs(330, 2).addBox(0.15F, -3.31F, -1.83F, 5.094F, 1.2F, 2.1F)
                .texOffs(352, 2).addBox(-5.364F, -3.36F, -1.83F, 0.66F, 0.55F, 1.85F)
                .texOffs(362, 2).addBox(4.764F, -3.36F, -1.83F, 0.66F, 0.55F, 1.85F)
                .texOffs(372, 2).addBox(-4.044F, -3.54F, -1.48F, 0.52F, 0.26F, 1.55F)
                .texOffs(382, 2).addBox(3.444F, -3.54F, -1.48F, 0.52F, 0.26F, 1.55F)
                .texOffs(392, 2).addBox(-5.284F, -1.86F, -0.08F, 10.568F, 4.7F, 2.4F)
                .texOffs(424, 2).addBox(-5.784F, -3.31F, -0.08F, 5.624F, 1.2F, 2.1F)
                .texOffs(446, 2).addBox(0.15F, -3.31F, -0.08F, 5.634F, 1.2F, 2.1F)
                .texOffs(468, 2).addBox(-5.904F, -3.36F, -0.08F, 0.66F, 0.55F, 1.85F)
                .texOffs(478, 2).addBox(5.304F, -3.36F, -0.08F, 0.66F, 0.55F, 1.85F)
                .texOffs(2, 22).addBox(-5.176F, -1.86F, 1.67F, 10.352F, 4.7F, 2.4F)
                .texOffs(34, 22).addBox(-5.676F, -3.31F, 1.67F, 5.516F, 1.2F, 2.1F)
                .texOffs(56, 22).addBox(0.15F, -3.31F, 1.67F, 5.526F, 1.2F, 2.1F)
                .texOffs(78, 22).addBox(-5.796F, -3.36F, 1.67F, 0.66F, 0.55F, 1.85F)
                .texOffs(88, 22).addBox(5.196F, -3.36F, 1.67F, 0.66F, 0.55F, 1.85F)
                .texOffs(98, 22).addBox(-4.476F, -3.54F, 2.02F, 0.52F, 0.26F, 1.55F)
                .texOffs(108, 22).addBox(3.876F, -3.54F, 2.02F, 0.52F, 0.26F, 1.55F)
                .texOffs(118, 22).addBox(-4.636F, -1.86F, 3.42F, 9.272F, 4.7F, 2.4F)
                .texOffs(148, 22).addBox(-5.136F, -3.31F, 3.42F, 4.976F, 1.2F, 2.1F)
                .texOffs(168, 22).addBox(0.15F, -3.31F, 3.42F, 4.986F, 1.2F, 2.1F)
                .texOffs(188, 22).addBox(-5.256F, -3.36F, 3.42F, 0.66F, 0.55F, 1.85F)
                .texOffs(198, 22).addBox(4.656F, -3.36F, 3.42F, 0.66F, 0.55F, 1.85F)
                .texOffs(208, 22).addBox(-4.096F, -1.86F, 5.17F, 8.192F, 4.7F, 2.4F)
                .texOffs(236, 22).addBox(-4.596F, -3.31F, 5.17F, 4.436F, 1.2F, 2.1F)
                .texOffs(256, 22).addBox(0.15F, -3.31F, 5.17F, 4.446F, 1.2F, 2.1F)
                .texOffs(276, 22).addBox(-4.716F, -3.36F, 5.17F, 0.66F, 0.55F, 1.85F)
                .texOffs(286, 22).addBox(4.116F, -3.36F, 5.17F, 0.66F, 0.55F, 1.85F)
                .texOffs(296, 22).addBox(-3.396F, -3.54F, 5.52F, 0.52F, 0.26F, 1.55F)
                .texOffs(306, 22).addBox(2.796F, -3.54F, 5.52F, 0.52F, 0.26F, 1.55F)
                .texOffs(316, 22).addBox(-3.556F, -1.86F, 6.92F, 7.112F, 4.7F, 2.4F)
                .texOffs(342, 22).addBox(-4.056F, -3.31F, 6.92F, 3.896F, 1.2F, 2.1F)
                .texOffs(360, 22).addBox(0.15F, -3.31F, 6.92F, 3.906F, 1.2F, 2.1F)
                .texOffs(378, 22).addBox(-4.176F, -3.36F, 6.92F, 0.66F, 0.55F, 1.85F)
                .texOffs(388, 22).addBox(3.576F, -3.36F, 6.92F, 0.66F, 0.55F, 1.85F)
                , PartPose.offset(0F, 17.46F, 0.33F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(398, 22).addBox(-4.0F, -2.48F, -4.5F, 8.0F, 5.0F, 5.0F)
                .texOffs(428, 22).addBox(-4.5F, -3.18F, -5.0F, 9.0F, 2.0F, 3.2F)
                .texOffs(458, 22).addBox(-2.5F, 0.52F, -5.9F, 5.0F, 2.0F, 2.3F)
                .texOffs(478, 22).addBox(-4.4F, -1.28F, -5.3F, 1.3F, 1.1F, 1.1F)
                .texOffs(490, 22).addBox(-3.0F, 1.72F, -6.5F, 1.2F, 2.1F, 3.5F)
                .texOffs(2, 36).addBox(3.2F, -1.28F, -5.3F, 1.3F, 1.1F, 1.1F)
                .texOffs(14, 36).addBox(1.8F, 1.72F, -6.5F, 1.2F, 2.1F, 3.5F)
                .texOffs(30, 36).addBox(-4.5F, -1.43F, -5.6F, 1.1F, 1.65F, 0.7F)
                .texOffs(40, 36).addBox(-4.7F, -1.63F, -4.95F, 1.15F, 1.8F, 0.45F)
                .texOffs(50, 36).addBox(-1.95F, 2.32F, -6.2F, 0.45F, 0.42F, 1.4F)
                .texOffs(60, 36).addBox(-1.95F, 2.6F, -6.55F, 0.45F, 0.42F, 1.4F)
                .texOffs(70, 36).addBox(-1.95F, 2.88F, -6.9F, 0.45F, 0.42F, 1.4F)
                .texOffs(80, 36).addBox(-1.95F, 3.16F, -7.25F, 0.45F, 0.42F, 1.4F)
                .texOffs(90, 36).addBox(3.5F, -1.43F, -5.6F, 1.1F, 1.65F, 0.7F)
                .texOffs(100, 36).addBox(3.6F, -1.63F, -4.95F, 1.15F, 1.8F, 0.45F)
                .texOffs(110, 36).addBox(1.05F, 2.32F, -6.2F, 0.45F, 0.42F, 1.4F)
                .texOffs(120, 36).addBox(1.05F, 2.6F, -6.55F, 0.45F, 0.42F, 1.4F)
                .texOffs(130, 36).addBox(1.05F, 2.88F, -6.9F, 0.45F, 0.42F, 1.4F)
                .texOffs(140, 36).addBox(1.05F, 3.16F, -7.25F, 0.45F, 0.42F, 1.4F)
                .texOffs(150, 36).addBox(-4.2F, -3.78F, -4.6F, 8.4F, 0.92F, 3.6F)
                .texOffs(180, 36).addBox(-4.4F, -3.83F, -5.75F, 8.8F, 0.45F, 0.45F)
                .texOffs(204, 36).addBox(-3.05F, 2.42F, -6.75F, 6.1F, 0.6F, 1.05F)
                .texOffs(226, 36).addBox(-3.15F, 2.47F, -7.7F, 0.75F, 0.6F, 1.35F)
                .texOffs(236, 36).addBox(-2.8F, 2.47F, -8.55F, 0.75F, 0.6F, 1.35F)
                .texOffs(246, 36).addBox(2.25F, 2.47F, -7.7F, 0.75F, 0.6F, 1.35F)
                .texOffs(256, 36).addBox(2.6F, 2.47F, -8.55F, 0.75F, 0.6F, 1.35F)
                , PartPose.offset(0F, 18.48F, -4.8F));
        root.addOrReplaceChild("antenna_l", CubeListBuilder.create()
                .texOffs(266, 36).addBox(1.0F, -0.56F, -4.0F, 1.0F, 1.1F, 4.0F)
                .texOffs(280, 36).addBox(0.0F, -0.76F, -10.0F, 1.0F, 1.0F, 7.0F)
                .texOffs(300, 36).addBox(-0.9F, -0.66F, -14.3F, 0.8F, 0.8F, 5.0F)
                .texOffs(316, 36).addBox(-0.2F, -0.26F, -9.7F, 0.7F, 0.64F, 2.5F)
                .texOffs(328, 36).addBox(-0.8F, -0.04F, -11.7F, 0.59F, 0.55F, 2.5F)
                .texOffs(340, 36).addBox(-1.4F, 0.18F, -13.7F, 0.48F, 0.46F, 2.5F)
                .texOffs(352, 36).addBox(-2.0F, 0.4F, -15.7F, 0.37F, 0.37F, 2.5F)
                , PartPose.offset(-3.8F, 16.56F, -7.0F));
        root.addOrReplaceChild("antenna_r", CubeListBuilder.create()
                .texOffs(364, 36).addBox(-1.89F, -0.56F, -4.0F, 1.0F, 1.1F, 4.0F)
                .texOffs(378, 36).addBox(-0.89F, -0.76F, -10.0F, 1.0F, 1.0F, 7.0F)
                .texOffs(398, 36).addBox(0.21F, -0.66F, -14.3F, 0.8F, 0.8F, 5.0F)
                .texOffs(414, 36).addBox(-0.29F, -0.26F, -9.7F, 0.7F, 0.64F, 2.5F)
                .texOffs(426, 36).addBox(0.31F, -0.04F, -11.7F, 0.59F, 0.55F, 2.5F)
                .texOffs(438, 36).addBox(0.91F, 0.18F, -13.7F, 0.48F, 0.46F, 2.5F)
                .texOffs(450, 36).addBox(1.51F, 0.4F, -15.7F, 0.37F, 0.37F, 2.5F)
                , PartPose.offset(3.69F, 16.56F, -7.0F));
        root.addOrReplaceChild("leg_l_0", CubeListBuilder.create()
                .texOffs(462, 36).addBox(-5.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(480, 36).addBox(-7.3F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(496, 36).addBox(-7.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(2, 48).addBox(-8.1F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(16, 48).addBox(-7.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(24, 48).addBox(-7.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(32, 48).addBox(-7.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(40, 48).addBox(-7.85F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(-4.5F, 19.5F, -6.45F));
        root.addOrReplaceChild("leg_l_1", CubeListBuilder.create()
                .texOffs(56, 48).addBox(-5.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(74, 48).addBox(-7.3F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(90, 48).addBox(-7.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(102, 48).addBox(-8.1F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(116, 48).addBox(-7.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(124, 48).addBox(-7.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(132, 48).addBox(-7.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(140, 48).addBox(-7.85F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(-4.5F, 19.5F, -0.45F));
        root.addOrReplaceChild("leg_l_2", CubeListBuilder.create()
                .texOffs(156, 48).addBox(-5.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(174, 48).addBox(-7.3F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(190, 48).addBox(-7.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(202, 48).addBox(-8.1F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(216, 48).addBox(-7.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(224, 48).addBox(-7.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(232, 48).addBox(-7.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(240, 48).addBox(-7.85F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(-4.5F, 19.5F, 5.55F));
        root.addOrReplaceChild("leg_r_0", CubeListBuilder.create()
                .texOffs(256, 48).addBox(0.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(274, 48).addBox(3.4F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(290, 48).addBox(5.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(302, 48).addBox(5.5F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(316, 48).addBox(6.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(324, 48).addBox(6.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(332, 48).addBox(6.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(340, 48).addBox(6.55F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(4.4F, 19.5F, -6.45F));
        root.addOrReplaceChild("leg_r_1", CubeListBuilder.create()
                .texOffs(356, 48).addBox(0.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(374, 48).addBox(3.4F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(390, 48).addBox(5.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(402, 48).addBox(5.5F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(416, 48).addBox(6.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(424, 48).addBox(6.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(432, 48).addBox(6.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(440, 48).addBox(6.55F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(4.4F, 19.5F, -0.45F));
        root.addOrReplaceChild("leg_r_2", CubeListBuilder.create()
                .texOffs(456, 48).addBox(0.0F, -0.2F, -0.55F, 5.0F, 1.6F, 2.0F)
                .texOffs(474, 48).addBox(3.4F, 0.9F, -0.75F, 3.3F, 1.5F, 2.0F)
                .texOffs(490, 48).addBox(5.9F, 1.8F, -0.75F, 1.2F, 4.3F, 1.5F)
                .texOffs(2, 59).addBox(5.5F, 5.7F, -1.15F, 1.7F, 0.8F, 2.3F)
                .texOffs(16, 59).addBox(6.7F, 2.6F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(24, 59).addBox(6.8F, 3.45F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(32, 59).addBox(6.9F, 4.3F, -1.05F, 1.0F, 0.47F, 0.85F)
                .texOffs(40, 59).addBox(6.55F, 6.0F, -1.55F, 1.2F, 0.5F, 3.1F)
                , PartPose.offset(4.4F, 19.5F, 5.55F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        leg_l_0.zRot = -1 * (Mth.sin(limbSwing * 2.15F + 0.0000F) * motion * 0.48F + attack * 0.16F);
        leg_l_0.xRot = Mth.cos(limbSwing * 2.15F + 0.0000F) * motion * 0.22F;
        leg_l_1.zRot = -1 * (Mth.sin(limbSwing * 2.15F + 3.1416F) * motion * 0.48F + attack * 0.16F);
        leg_l_1.xRot = Mth.cos(limbSwing * 2.15F + 3.1416F) * motion * 0.22F;
        leg_l_2.zRot = -1 * (Mth.sin(limbSwing * 2.15F + 0.0000F) * motion * 0.48F + attack * 0.16F);
        leg_l_2.xRot = Mth.cos(limbSwing * 2.15F + 0.0000F) * motion * 0.22F;
        leg_r_0.zRot = 1 * (Mth.sin(limbSwing * 2.15F + 3.1416F) * motion * 0.48F + attack * 0.16F);
        leg_r_0.xRot = Mth.cos(limbSwing * 2.15F + 3.1416F) * motion * 0.22F;
        leg_r_1.zRot = 1 * (Mth.sin(limbSwing * 2.15F + 0.0000F) * motion * 0.48F + attack * 0.16F);
        leg_r_1.xRot = Mth.cos(limbSwing * 2.15F + 0.0000F) * motion * 0.22F;
        leg_r_2.zRot = 1 * (Mth.sin(limbSwing * 2.15F + 3.1416F) * motion * 0.48F + attack * 0.16F);
        leg_r_2.xRot = Mth.cos(limbSwing * 2.15F + 3.1416F) * motion * 0.22F;
        antenna_l.yRot = Mth.sin(ageInTicks * 0.21F) * 0.30F + attack * 0.34F;
        antenna_r.yRot = -Mth.sin(ageInTicks * 0.19F + 0.8F) * 0.28F - attack * 0.34F;
        head.xRot = attack * -0.32F + recoil * 0.14F;
        head.yRot = Mth.sin(ageInTicks * 0.07F) * 0.085F;
        body.xRot = Mth.sin(limbSwing * 2.15F) * motion * 0.045F + recoil * 0.09F;
        leg_l_0.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 0.0000F)) * motion * 0.75F;
        leg_l_1.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 3.1416F)) * motion * 0.75F;
        leg_l_2.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 0.0000F)) * motion * 0.75F;
        leg_r_0.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 3.1416F)) * motion * 0.75F;
        leg_r_1.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 0.0000F)) * motion * 0.75F;
        leg_r_2.y = 19.500F - Math.max(0.0F, Mth.sin(limbSwing * 2.15F + 3.1416F)) * motion * 0.75F;
    }
}
