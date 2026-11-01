package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.registry.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only; do not load model or renderer classes on a dedicated server. */
public final class HoleEntityRenderers {
    private HoleEntityRenderers() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(HoleEntityRenderers::renderers);
        modBus.addListener(HoleEntityRenderers::layers);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEWER_ROACH.get(), SewerRoachRenderer::new);
        event.registerEntityRenderer(ModEntities.SCRAP_RAT.get(), ScrapRatRenderer::new);
        event.registerEntityRenderer(ModEntities.SEWER_LEECH.get(), SewerLeechRenderer::new);
        event.registerEntityRenderer(ModEntities.GLOW_MOTH.get(), GlowMothRenderer::new);
        event.registerEntityRenderer(ModEntities.SLUDGE_CRAWLER.get(), SludgeCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.RUST_EATER_BEETLE.get(), RustEaterBeetleRenderer::new);
        event.registerEntityRenderer(ModEntities.CAVERN_BURROWER.get(), CavernBurrowerRenderer::new);
        event.registerEntityRenderer(ModEntities.SPORE_CRAWLER.get(), SporeCrawlerRenderer::new);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SewerRoachModel.LAYER, SewerRoachModel::createBodyLayer);
        event.registerLayerDefinition(ScrapRatModel.LAYER, ScrapRatModel::createBodyLayer);
        event.registerLayerDefinition(SewerLeechModel.LAYER, SewerLeechModel::createBodyLayer);
        event.registerLayerDefinition(GlowMothModel.LAYER, GlowMothModel::createBodyLayer);
        event.registerLayerDefinition(SludgeCrawlerModel.LAYER, SludgeCrawlerModel::createBodyLayer);
        event.registerLayerDefinition(RustEaterBeetleModel.LAYER, RustEaterBeetleModel::createBodyLayer);
        event.registerLayerDefinition(CavernBurrowerModel.LAYER, CavernBurrowerModel::createBodyLayer);
        event.registerLayerDefinition(SporeCrawlerModel.LAYER, SporeCrawlerModel::createBodyLayer);
    }
}
