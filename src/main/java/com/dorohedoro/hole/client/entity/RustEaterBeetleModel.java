package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.RustEaterBeetleEntity;
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

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/rust_eater_beetle.json. */
public final class RustEaterBeetleModel<T extends RustEaterBeetleEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "rust_eater_beetle"), "main");
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
    private final ModelPart mandible_l;
    private final ModelPart mandible_r;
    public RustEaterBeetleModel(ModelPart root) {
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
        this.mandible_l = root.getChild("mandible_l");
        this.mandible_r = root.getChild("mandible_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(2, 2).addBox(-4.5F, -1.85F, -7.88F, 9.0F, 4.0F, 5.0F)
                .texOffs(34, 2).addBox(-5.1F, -2.85F, -3.88F, 5.0F, 3.0F, 11.0F)
                .texOffs(70, 2).addBox(-4.6F, -3.65F, -2.88F, 0.9F, 1.0F, 9.0F)
                .texOffs(94, 2).addBox(0.1F, -2.85F, -3.88F, 5.0F, 3.0F, 11.0F)
                .texOffs(130, 2).addBox(3.8F, -3.65F, -2.88F, 0.9F, 1.0F, 9.0F)
                .texOffs(154, 2).addBox(-4.9F, -3.25F, -2.88F, 9.8F, 0.52F, 1.65F)
                .texOffs(182, 2).addBox(-0.18F, -3.63F, -2.88F, 0.36F, 0.42F, 1.75F)
                .texOffs(192, 2).addBox(-4.9F, -3.25F, -1.08F, 9.8F, 0.52F, 1.65F)
                .texOffs(220, 2).addBox(-0.18F, -3.63F, -1.08F, 0.36F, 0.42F, 1.75F)
                .texOffs(230, 2).addBox(-4.9F, -3.25F, 0.72F, 9.8F, 0.52F, 1.65F)
                .texOffs(258, 2).addBox(-0.18F, -3.63F, 0.72F, 0.36F, 0.42F, 1.75F)
                .texOffs(268, 2).addBox(-4.9F, -3.25F, 2.52F, 9.8F, 0.52F, 1.65F)
                .texOffs(296, 2).addBox(-0.18F, -3.63F, 2.52F, 0.36F, 0.42F, 1.75F)
                .texOffs(306, 2).addBox(-4.9F, -3.25F, 4.32F, 9.8F, 0.52F, 1.65F)
                .texOffs(334, 2).addBox(-0.18F, -3.63F, 4.32F, 0.36F, 0.42F, 1.75F)
                .texOffs(344, 2).addBox(-4.9F, -3.25F, 6.12F, 9.8F, 0.52F, 1.65F)
                .texOffs(372, 2).addBox(-0.18F, -3.63F, 6.12F, 0.36F, 0.42F, 1.75F)
                .texOffs(382, 2).addBox(-4.0F, -0.65F, -5.88F, 8.0F, 4.3F, 2.45F)
                .texOffs(408, 2).addBox(-4.25F, -0.65F, -3.78F, 8.5F, 4.3F, 2.45F)
                .texOffs(436, 2).addBox(-4.433F, -0.65F, -1.68F, 8.866F, 4.3F, 2.45F)
                .texOffs(464, 2).addBox(-4.5F, -0.65F, 0.42F, 9.0F, 4.3F, 2.45F)
                .texOffs(2, 20).addBox(-4.433F, -0.65F, 2.52F, 8.866F, 4.3F, 2.45F)
                .texOffs(30, 20).addBox(-4.25F, -0.65F, 4.62F, 8.5F, 4.3F, 2.45F)
                , PartPose.offset(0F, 17.85F, 0.88F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(58, 20).addBox(-3.2F, -2.1F, -5.0F, 6.4F, 4.5F, 5.5F)
                .texOffs(88, 20).addBox(-3.3F, -1.1F, -6.0F, 1.0F, 1.1F, 1.1F)
                .texOffs(98, 20).addBox(2.3F, -1.1F, -6.0F, 1.0F, 1.1F, 1.1F)
                , PartPose.offset(0F, 19.1F, 1.0F));
        root.addOrReplaceChild("leg_l_0", CubeListBuilder.create()
                .texOffs(108, 20).addBox(-4.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(124, 20).addBox(-5.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(134, 20).addBox(-5.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(-4F, 19.9F, -4.45F));
        root.addOrReplaceChild("leg_l_1", CubeListBuilder.create()
                .texOffs(144, 20).addBox(-4.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(160, 20).addBox(-5.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(170, 20).addBox(-5.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(-4F, 19.9F, 0.55F));
        root.addOrReplaceChild("leg_l_2", CubeListBuilder.create()
                .texOffs(180, 20).addBox(-4.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(196, 20).addBox(-5.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(206, 20).addBox(-5.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(-4F, 19.9F, 5.55F));
        root.addOrReplaceChild("leg_r_0", CubeListBuilder.create()
                .texOffs(216, 20).addBox(0.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(232, 20).addBox(3.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(242, 20).addBox(4.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(4F, 19.9F, -4.45F));
        root.addOrReplaceChild("leg_r_1", CubeListBuilder.create()
                .texOffs(252, 20).addBox(0.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(268, 20).addBox(3.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(278, 20).addBox(4.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(4F, 19.9F, 0.55F));
        root.addOrReplaceChild("leg_r_2", CubeListBuilder.create()
                .texOffs(288, 20).addBox(0.0F, -0.2F, -0.55F, 4.0F, 1.4F, 1.6F)
                .texOffs(304, 20).addBox(3.0F, 1.1F, -0.55F, 0.9F, 3.4F, 1.1F)
                .texOffs(314, 20).addBox(4.4F, 3.0F, -1.05F, 0.8F, 0.65F, 1.9F)
                , PartPose.offset(4F, 19.9F, 5.55F));
        root.addOrReplaceChild("antenna_l", CubeListBuilder.create()
                .texOffs(324, 20).addBox(-0.3F, -0.2F, -0.5F, 0.55F, 0.65F, 3.5F)
                .texOffs(338, 20).addBox(-1.3F, -0.4F, -1.7F, 0.5F, 0.45F, 2.2F)
                .texOffs(350, 20).addBox(-1.6F, -0.4F, -3.0F, 0.5F, 0.45F, 2.2F)
                .texOffs(362, 20).addBox(-1.9F, -0.4F, -4.3F, 0.5F, 0.45F, 2.2F)
                , PartPose.offset(-2.4F, 16.3F, -7.3F));
        root.addOrReplaceChild("mandible_l", CubeListBuilder.create()
                .texOffs(374, 20).addBox(-0.5F, 0.0F, -2.7F, 1.3F, 2.4F, 4.5F)
                , PartPose.offset(-3.1F, 19.9F, -4.8F));
        root.addOrReplaceChild("antenna_r", CubeListBuilder.create()
                .texOffs(392, 20).addBox(-0.3F, -0.2F, -0.5F, 0.55F, 0.65F, 3.5F)
                .texOffs(406, 20).addBox(0.4F, -0.4F, -1.7F, 0.5F, 0.45F, 2.2F)
                .texOffs(418, 20).addBox(0.7F, -0.4F, -3.0F, 0.5F, 0.45F, 2.2F)
                .texOffs(430, 20).addBox(1.0F, -0.4F, -4.3F, 0.5F, 0.45F, 2.2F)
                , PartPose.offset(2.4F, 16.3F, -7.3F));
        root.addOrReplaceChild("mandible_r", CubeListBuilder.create()
                .texOffs(442, 20).addBox(-0.8F, 0.0F, -2.7F, 1.3F, 2.4F, 4.5F)
                , PartPose.offset(3.1F, 19.9F, -4.8F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        leg_l_0.zRot = -1 * motion * Mth.sin(limbSwing * 2.76F + 0.0000F) * 0.49F;
        leg_l_0.xRot = motion * Mth.cos(limbSwing * 2.76F + 0.0000F) * 0.30F;
        leg_l_0.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 0.0000F)) * 1.10F;
        leg_l_0.yRot = -1 * motion * Mth.cos(limbSwing * 2.76F + 0.0000F) * 0.09F;
        leg_l_1.zRot = -1 * motion * Mth.sin(limbSwing * 2.76F + 3.1416F) * 0.49F;
        leg_l_1.xRot = motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.30F;
        leg_l_1.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 3.1416F)) * 1.10F;
        leg_l_1.yRot = -1 * motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.09F;
        leg_l_2.zRot = -1 * motion * Mth.sin(limbSwing * 2.76F + 0.0000F) * 0.49F;
        leg_l_2.xRot = motion * Mth.cos(limbSwing * 2.76F + 0.0000F) * 0.30F;
        leg_l_2.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 0.0000F)) * 1.10F;
        leg_l_2.yRot = -1 * motion * Mth.cos(limbSwing * 2.76F + 0.0000F) * 0.09F;
        leg_r_0.zRot = 1 * motion * Mth.sin(limbSwing * 2.76F + 3.1416F) * 0.49F;
        leg_r_0.xRot = motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.30F;
        leg_r_0.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 3.1416F)) * 1.10F;
        leg_r_0.yRot = 1 * motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.09F;
        leg_r_1.zRot = 1 * motion * Mth.sin(limbSwing * 2.76F + 6.2832F) * 0.49F;
        leg_r_1.xRot = motion * Mth.cos(limbSwing * 2.76F + 6.2832F) * 0.30F;
        leg_r_1.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 6.2832F)) * 1.10F;
        leg_r_1.yRot = 1 * motion * Mth.cos(limbSwing * 2.76F + 6.2832F) * 0.09F;
        leg_r_2.zRot = 1 * motion * Mth.sin(limbSwing * 2.76F + 3.1416F) * 0.49F;
        leg_r_2.xRot = motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.30F;
        leg_r_2.y = 19.900F - motion * Math.max(0.0F, Mth.sin(limbSwing * 2.76F + 3.1416F)) * 1.10F;
        leg_r_2.yRot = 1 * motion * Mth.cos(limbSwing * 2.76F + 3.1416F) * 0.09F;
        body.y = 17.850F + motion * (0.10F + 0.16F * Mth.cos(limbSwing * 5.52F));
        body.zRot = motion * Mth.sin(limbSwing * 2.76F) * 0.075F;
        body.xRot = motion * Mth.cos(limbSwing * 5.52F) * 0.065F + recoil * 0.12F;
        head.xRot = Mth.sin(ageInTicks * 0.23F) * 0.07F - attack * 0.30F + motion * Mth.sin(limbSwing * 2.76F + 0.4F) * 0.09F;
        head.yRot = Mth.sin(ageInTicks * 0.18F) * 0.11F + motion * Mth.sin(limbSwing * 1.38F) * 0.05F;
        antenna_l.xRot = Mth.sin(ageInTicks * 0.42F + 0.10F) * 0.18F + motion * Mth.sin(limbSwing * 2.76F - 0.70F) * 0.21F;
        antenna_l.yRot = -1 * (0.10F + Mth.sin(ageInTicks * 0.32F + 0.50F) * 0.20F);
        mandible_l.yRot = -1 * (0.08F + attack * 0.32F + Mth.sin(ageInTicks * 0.82F) * 0.045F);
        antenna_r.xRot = Mth.sin(ageInTicks * 0.42F + 1.20F) * 0.18F + motion * Mth.sin(limbSwing * 2.76F - 1.60F) * 0.21F;
        antenna_r.yRot = 1 * (0.10F + Mth.sin(ageInTicks * 0.32F + 1.20F) * 0.20F);
        mandible_r.yRot = 1 * (0.08F + attack * 0.32F + Mth.sin(ageInTicks * 0.82F) * 0.045F);
    }
}
