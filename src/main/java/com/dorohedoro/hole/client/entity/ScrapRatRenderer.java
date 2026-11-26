package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.ScrapRatEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ScrapRatRenderer extends MobRenderer<ScrapRatEntity, ScrapRatModel<ScrapRatEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/scrap_rat.png");
    public ScrapRatRenderer(EntityRendererProvider.Context context) {
        super(context, new ScrapRatModel<>(context.bakeLayer(ScrapRatModel.LAYER)), 0.22F);
    }
    @Override
    public ResourceLocation getTextureLocation(ScrapRatEntity entity) { return TEXTURE; }
}
