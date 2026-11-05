package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.CavernBurrowerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class CavernBurrowerRenderer extends MobRenderer<CavernBurrowerEntity, CavernBurrowerModel<CavernBurrowerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/cavern_burrower.png");
    public CavernBurrowerRenderer(EntityRendererProvider.Context context) {
        super(context, new CavernBurrowerModel<>(context.bakeLayer(CavernBurrowerModel.LAYER)), 0.55F);
    }
    @Override public ResourceLocation getTextureLocation(CavernBurrowerEntity entity) { return TEXTURE; }
}
