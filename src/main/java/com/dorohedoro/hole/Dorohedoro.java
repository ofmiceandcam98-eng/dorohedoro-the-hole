package com.dorohedoro.hole;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** Mod entry point. Registers content on the mod bus; game logic lives in feature packages. */
@Mod(Dorohedoro.MOD_ID)
public final class Dorohedoro {
    public static final String MOD_ID = "dorohedoro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Dorohedoro(IEventBus modBus, ModContainer container) {
        LOGGER.info("Dorohedoro: The Hole loading");
    }
}
