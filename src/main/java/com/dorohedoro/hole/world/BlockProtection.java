package com.dorohedoro.hole.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Asks the world whether a player may change a block, the same way a block placed by hand is asked. Every block the mod
 * places for a player (mushroom growth, smoke barriers, the far end of a door) goes through here first.
 *
 * <p>Claim mods such as FTB Chunks cancel NeoForge's {@code BlockEvent.EntityPlaceEvent} for non-members, so posting
 * that event is enough to respect claims without depending on any claim mod. Spawn protection, the world border and
 * adventure mode are checked as well.
 *
 * <p>Claim mods judge a placement by the dimension the acting entity stands in. When the block is in another dimension
 * (the far end of a door), the event is posted with a NeoForge fake player that carries the real player's identity and
 * stands in the target dimension, so the claim there is the one consulted. FTB Chunks treats that fake player as the
 * player for their own team's claims ({@code allow_fake_players_by_id}, on by default) and refuses it everywhere else.
 */
public final class BlockProtection {
    private BlockProtection() {}

    /** True if {@code actor} may put a block at {@code pos}. Call before placing; nothing in the world changes. */
    public static boolean mayPlace(ServerPlayer actor, ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.mayInteract(actor, pos) || !actor.getAbilities().mayBuild) {
            return false;
        }
        Player asker = actor.level() == level ? actor : FakePlayerFactory.get(level, actor.getGameProfile());
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        return !EventHooks.onBlockPlace(asker, snapshot, Direction.UP);
    }
}
