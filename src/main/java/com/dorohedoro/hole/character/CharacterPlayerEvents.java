package com.dorohedoro.hole.character;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Server-owned origin selection on login, persisted across death and reconnects. */
public final class CharacterPlayerEvents {
    private CharacterPlayerEvents() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener(CharacterPlayerEvents::onJoin);
        NeoForge.EVENT_BUS.addListener(CharacterPlayerEvents::onRespawn);
        NeoForge.EVENT_BUS.addListener(CharacterPlayerEvents::onLogout);
    }
    private static void onJoin(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getEntity() instanceof ServerPlayer player) { CharacterNetwork.begin(player); com.dorohedoro.hole.magic.SmokeNetwork.sync(player); com.dorohedoro.hole.magic.SmokeMeshNetwork.syncLimbs(player); }
    }
    private static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if(e.getEntity() instanceof ServerPlayer player) { CharacterNetwork.begin(player); com.dorohedoro.hole.magic.SmokeNetwork.sync(player); com.dorohedoro.hole.magic.SmokeMeshNetwork.syncLimbs(player); }
    }
    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer player) { CharacterNetwork.abandon(player); com.dorohedoro.hole.magic.SmokeEngine.logout(player); }
    }
}
