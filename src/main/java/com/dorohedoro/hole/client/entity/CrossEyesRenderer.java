package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.CrossEyesEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * A Cross-Eyes member: one of Minecraft's default player skins (all but Steve; referenced from the game's own assets and
 * never copied) with the gang's marks over it. As in the series, the members are ordinary people marked by a red cross
 * tattooed over each eye; the overlay also adds a red scarf, the cross painted on the back and a red armband.
 */
public final class CrossEyesRenderer extends HumanoidMobRenderer<CrossEyesEntity, PlayerModel<CrossEyesEntity>> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "cross_eyes"), "main");
    private static final ResourceLocation OVERLAY =
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/cross_eyes_overlay.png");
    private static final ResourceLocation[] SKINS = new ResourceLocation[CrossEyesEntity.SKIN_COUNT];

    static {
        String[] names = {"alex", "ari", "efe", "kai", "makena", "noor", "sunny", "zuri"};
        for (int i = 0; i < SKINS.length; i++) {
            SKINS[i] = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/" + names[i] + ".png");
        }
    }

    public CrossEyesRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(LAYER), false), 0.5F);
        addLayer(new GangLayer(this));
    }

    /** The wide-armed player model, so the default skins' outer layers (jackets, sleeves) render as they do on players. */
    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(CrossEyesEntity entity) {
        return SKINS[Math.floorMod(entity.skin(), SKINS.length)];
    }

    /** Draws the gang's overlay over the skin, on the same posed model, the way vanilla layers a villager's outfit. */
    private static final class GangLayer extends RenderLayer<CrossEyesEntity, PlayerModel<CrossEyesEntity>> {
        GangLayer(RenderLayerParent<CrossEyesEntity, PlayerModel<CrossEyesEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int light, CrossEyesEntity entity, float limbSwing,
                float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) return;
            renderColoredCutoutModel(getParentModel(), OVERLAY, poseStack, buffer, light, entity, -1);
        }
    }
}
