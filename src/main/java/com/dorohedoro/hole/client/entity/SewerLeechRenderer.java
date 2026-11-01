package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SewerLeechEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SewerLeechRenderer extends MobRenderer<SewerLeechEntity, SewerLeechModel<SewerLeechEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,
            "textures/entity/sewer_leech.png");
    public SewerLeechRenderer(EntityRendererProvider.Context context) {
        super(context, new SewerLeechModel<>(context.bakeLayer(SewerLeechModel.LAYER)), 0.25F);
    }
    @Override public ResourceLocation getTextureLocation(SewerLeechEntity entity) { return TEXTURE; }
}
