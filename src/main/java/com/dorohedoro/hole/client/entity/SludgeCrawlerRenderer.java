package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SludgeCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SludgeCrawlerRenderer extends MobRenderer<SludgeCrawlerEntity, SludgeCrawlerModel<SludgeCrawlerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/sludge_crawler.png");
    public SludgeCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SludgeCrawlerModel<>(context.bakeLayer(SludgeCrawlerModel.LAYER)), 0.42F);
    }
    @Override public ResourceLocation getTextureLocation(SludgeCrawlerEntity entity) { return TEXTURE; }
}
