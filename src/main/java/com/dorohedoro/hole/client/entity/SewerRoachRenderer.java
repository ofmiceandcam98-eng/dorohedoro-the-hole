package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.SewerRoachEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SewerRoachRenderer extends MobRenderer<SewerRoachEntity, SewerRoachModel<SewerRoachEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/sewer_roach.png");
    public SewerRoachRenderer(EntityRendererProvider.Context context) {
        super(context, new SewerRoachModel<>(context.bakeLayer(SewerRoachModel.LAYER)), 0.35F);
    }
    @Override
    public ResourceLocation getTextureLocation(SewerRoachEntity entity) { return TEXTURE; }
}
