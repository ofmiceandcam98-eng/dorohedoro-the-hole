package com.dorohedoro.hole;

import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import com.dorohedoro.hole.registry.ModItems;
import com.dorohedoro.hole.registry.ModWorldgen;
import com.dorohedoro.hole.registry.ModEntities;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import com.dorohedoro.hole.config.SmokeWorldConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** Mod entry point. Registers content on the mod bus; game logic lives in feature packages. */
@Mod(Dorohedoro.MOD_ID)
public final class Dorohedoro {
    public static final String MOD_ID = "dorohedoro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Dorohedoro(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SmokeWorldConfig.SPEC);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModWorldgen.register(modBus);
        ModEntities.register(modBus);
        com.dorohedoro.hole.registry.ModSmokeParticles.register(modBus);
        com.dorohedoro.hole.magic.SmokeMeshNetwork.register(modBus);
        com.dorohedoro.hole.character.CharacterAttachments.register(modBus);
        com.dorohedoro.hole.magic.SmokeAttachments.register(modBus);
        com.dorohedoro.hole.magic.SmokeNetwork.register(modBus);
        com.dorohedoro.hole.magic.SmokeEngine.register();
        com.dorohedoro.hole.character.CharacterNetwork.register(modBus);
        com.dorohedoro.hole.character.CharacterPlayerEvents.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.dorohedoro.hole.client.HoleAtmosphere.register(modBus);
            com.dorohedoro.hole.client.magic.SmokeClient.register(modBus);
            com.dorohedoro.hole.client.magic.SmokeParticleClient.register(modBus);
            com.dorohedoro.hole.client.magic.SmokeMeshClient.register(modBus);
            com.dorohedoro.hole.client.magic.SeveredLimbClient.register();
            com.dorohedoro.hole.client.entity.HoleEntityRenderers.register(modBus);
        }
    }
}
