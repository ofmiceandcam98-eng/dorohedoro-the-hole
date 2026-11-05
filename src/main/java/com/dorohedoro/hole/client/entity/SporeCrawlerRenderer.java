package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SporeCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SporeCrawlerRenderer extends MobRenderer<SporeCrawlerEntity, SporeCrawlerModel<SporeCrawlerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/spore_crawler.png");
    public SporeCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SporeCrawlerModel<>(context.bakeLayer(SporeCrawlerModel.LAYER)), 0.42F);
    }
    @Override public ResourceLocation getTextureLocation(SporeCrawlerEntity entity) { return TEXTURE; }
}
