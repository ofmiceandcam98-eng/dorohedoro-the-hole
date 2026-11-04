package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.GlowMothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public final class GlowMothRenderer extends MobRenderer<GlowMothEntity, GlowMothModel<GlowMothEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,
            "textures/entity/glow_moth.png");
    public GlowMothRenderer(EntityRendererProvider.Context context) {
        super(context, new GlowMothModel<>(context.bakeLayer(GlowMothModel.LAYER)), 0.28F);
        addLayer(new Glow(this));
    }

    /** The pale wing markings render full-bright, like spider eyes, so the moth actually glows at night. */
    private static final class Glow extends net.minecraft.client.renderer.entity.layers.EyesLayer<GlowMothEntity, GlowMothModel<GlowMothEntity>> {
        private static final net.minecraft.client.renderer.RenderType GLOW = net.minecraft.client.renderer.RenderType.eyes(
                ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/glow_moth_glow.png"));

        Glow(net.minecraft.client.renderer.entity.RenderLayerParent<GlowMothEntity, GlowMothModel<GlowMothEntity>> parent) {
            super(parent);
        }

        @Override
        public net.minecraft.client.renderer.RenderType renderType() {
            return GLOW;
        }
    }
    @Override public ResourceLocation getTextureLocation(GlowMothEntity entity) { return TEXTURE; }
    /** The moth itself stays bright in caves while its temporary light block illuminates surroundings. */
    @Override protected int getBlockLightLevel(GlowMothEntity entity, BlockPos pos) {
        return Math.max(12, super.getBlockLightLevel(entity, pos));
    }
}
