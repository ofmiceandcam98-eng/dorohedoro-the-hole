package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.RustEaterBeetleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class RustEaterBeetleRenderer extends MobRenderer<RustEaterBeetleEntity, RustEaterBeetleModel<RustEaterBeetleEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "textures/entity/rust_eater_beetle.png");
    public RustEaterBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new RustEaterBeetleModel<>(context.bakeLayer(RustEaterBeetleModel.LAYER)), 0.42F);
    }
    @Override public ResourceLocation getTextureLocation(RustEaterBeetleEntity entity) { return TEXTURE; }
}
