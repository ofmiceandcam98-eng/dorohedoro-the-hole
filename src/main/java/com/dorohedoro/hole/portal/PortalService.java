package com.dorohedoro.hole.portal;

import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/** Shared, server-only one-to-one travel path. A source frame creates a matching, linked exit in the destination. */
public final class PortalService {
    private static final Map<UUID, Long> TRAVEL_COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 80;
    private static final int HEIGHT = 5;
    /** How far from the matching spot the far end may stand, and the spacing of the columns tried. */
    private static final int SEARCH_RADIUS = 32;
    private static final int SEARCH_STEP = 4;
    private PortalService() {}

    public record Frame(BlockPos core, Direction.Axis axis, PortalKind kind, boolean generated) {
        public BlockPos at(int column, int height) {
            int delta = column - kind.coreColumn;
            return axis == Direction.Axis.X ? core.offset(delta, height, 0) : core.offset(0, height, delta);
        }
        public BlockPos stand(int sign) {
            return axis == Direction.Axis.X ? core.offset(0, 0, sign) : core.offset(sign, 0, 0);
        }
    }

    public static void activate(ServerLevel source, BlockPos corePos, PortalKind kind, Player actor) {
        ResourceKey<Level> targetKey = kind.target(source);
        if (targetKey == null) { notify(actor, "This door cannot connect from this dimension."); return; }
        ServerLevel target = source.getServer().getLevel(targetKey);
        if (target == null) { notify(actor, "Destination realm is not registered. Check Phase 3 first."); return; }
        if (!(source.getBlockEntity(corePos) instanceof PortalCoreBlockEntity sourceCore)) return;
        Frame frame = findFrame(source, corePos, kind);
        if (frame == null) { notify(actor, "Frame incomplete: check the 4x5 or 5x5 ring and empty doorway."); return; }
        if (sourceCore.active()) {
            // Never silently relink an existing gate: that would orphan its other end.
            notify(actor, "This door is already linked."); return;
        }
        ServerPlayer builder = actor instanceof ServerPlayer sp ? sp : null;
        BlockPos targetPos = findSafeSite(target, corePos, frame.axis(), kind, builder);
        if (targetPos == null) {
            notify(actor, CLAIM_BLOCKED.get() ? "The far side is protected land. No door can open there."
                    : "No safe unoccupied location found for the return door.");
            return;
        }
        Frame other = new Frame(targetPos, frame.axis(), kind, true);
        // Final check before modifying either world. A site is only ever cleared of natural ground cover; player blocks,
        // fluids and anything with contents always make a site unusable.
        boolean platform = PLATFORM_SITE.get();
        if (!(platform ? openAir(target, other) : safeSite(target, other)) || !permitted(target, other, builder)) {
            notify(actor, "The destination is no longer clear."); return;
        }
        if (platform) buildPlatform(target, other);
        buildFrame(target, other);
        if (!(target.getBlockEntity(targetPos) instanceof PortalCoreBlockEntity targetCore)) {
            notify(actor, "Failed to create the destination door."); return;
        }
        sourceCore.linkTo(targetCore, target.dimension().location());
        targetCore.linkTo(sourceCore, source.dimension().location());
        fillInterior(source, frame);
        fillInterior(target, other);
        notify(actor, "Linked door activated. The far end is ready for return travel.");
        if (builder != null) com.dorohedoro.hole.progress.HoleAdvancements.award(builder, kind == PortalKind.ARTIFICIAL
                ? com.dorohedoro.hole.progress.HoleAdvancements.ARTIFICIAL_DOOR_OPENED : com.dorohedoro.hole.progress.HoleAdvancements.DEVIL_GATE_OPENED);
    }

    /** Strict frame validation, no guesswork about diagonal or mirrored portal geometry. */
    public static Frame findFrame(ServerLevel world, BlockPos core, PortalKind kind) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            for (boolean generated : new boolean[]{false, true}) {
            Frame frame = new Frame(core, axis, kind, generated);
            boolean valid = true;
            for (int x = 0; x < kind.width && valid; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    BlockPos p = frame.at(x, y);
                    BlockState state = world.getBlockState(p);
                    if (x == kind.coreColumn && y == 0) {
                        if (!state.is(generated ? kind.generatedCore() : kind.core())) { valid = false; break; }
                    } else if (y == 0 || y == HEIGHT - 1 || x == 0 || x == kind.width - 1) {
                        if (!state.is(generated ? kind.generatedFrame() : kind.frame())) { valid = false; break; }
                    } else if (!state.isAir() && !state.is(ModBlocks.PORTAL_SURFACE.get())) {
                        valid = false; break;
                    }
                }
            }
            if (valid) return frame;
            }
        }
        return null;
    }

    /** Set when the last site search turned down a clear site only because the builder may not build there. */
    private static final ThreadLocal<Boolean> CLAIM_BLOCKED = ThreadLocal.withInitial(() -> false);

    /** Set when the last search fell back to a platform in open air. */
    private static final ThreadLocal<Boolean> PLATFORM_SITE = ThreadLocal.withInitial(() -> false);

    /**
     * Finds where the far end of a door stands: on the ground as close as possible to the same coordinates, searching in
     * square rings outward (nearest first) to {@link #SEARCH_RADIUS} blocks. Ground under natural leaves, grass, flowers
     * and thin snow counts. If no ground site exists there (open water, a dense forest on a slope, a ravine), the far end
     * gets a small platform in open air above the highest block, so a door always has somewhere to land. Either way the
     * site must be somewhere the builder may build. The search is finite, so at most a few chunks are generated.
     */
    private static BlockPos findSafeSite(ServerLevel level, BlockPos near, Direction.Axis axis, PortalKind kind, ServerPlayer builder) {
        CLAIM_BLOCKED.set(false);
        PLATFORM_SITE.set(false);
        for (int r = 0; r <= SEARCH_RADIUS; r += SEARCH_STEP) {
            for (int dx = -r; dx <= r; dx += SEARCH_STEP) for (int dz = -r; dz <= r; dz += SEARCH_STEP) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                int x = near.getX() + dx, z = near.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y < level.getMinBuildHeight() + 4 || y > level.getMaxBuildHeight() - HEIGHT - 3) continue;
                Frame test = new Frame(new BlockPos(x, y, z), axis, kind, true);
                if (!safeSite(level, test)) continue;
                if (permitted(level, test, builder)) return test.core();
                CLAIM_BLOCKED.set(true);
            }
        }
        // No ground site: stand on a platform just above everything at the matching spot.
        int top = level.getMinBuildHeight();
        Frame probe = new Frame(near, axis, kind, true);
        for (int x = -1; x <= kind.width; x++) for (int side = -1; side <= 1; side++) {
            BlockPos column = probe.at(x, 0).offset(probe.stand(side).subtract(near));
            top = Math.max(top, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
        }
        int y = Math.max(top + 1, level.getSeaLevel() + 1);
        if (y > level.getMaxBuildHeight() - HEIGHT - 3) return null;
        Frame raised = new Frame(new BlockPos(near.getX(), y, near.getZ()), axis, kind, true);
        if (!openAir(level, raised)) return null;
        if (!permitted(level, raised, builder)) { CLAIM_BLOCKED.set(true); return null; }
        PLATFORM_SITE.set(true);
        return raised.core();
    }

    /**
     * Natural ground cover the far end may clear: air, plants, grass, single snow layers and leaves that grew naturally.
     * Never fluids, never anything with a block entity, never player-placed leaves.
     */
    private static boolean clearable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        if (!level.getFluidState(pos).isEmpty() || state.hasBlockEntity()) return false;
        if (state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
            return !state.getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT);
        }
        return state.canBeReplaced();
    }

    /** The frame, its opening and both landing cells (with the cell above each) are empty air. */
    private static boolean openAir(ServerLevel level, Frame frame) {
        for (int x = 0; x < frame.kind.width; x++) for (int y = 0; y < HEIGHT; y++) {
            if (!level.getBlockState(frame.at(x, y)).isAir()) return false;
        }
        for (int side : new int[]{-1, 1}) {
            BlockPos feet = frame.stand(side);
            if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) return false;
        }
        return true;
    }

    /** A floor under the frame and one row on each side, placed only into air. Plain stone, recovered like any stone. */
    private static void buildPlatform(ServerLevel level, Frame frame) {
        BlockState floor = (frame.kind == PortalKind.ARTIFICIAL ? Blocks.STONE_BRICKS : ModBlocks.HELL_ROCK.get()).defaultBlockState();
        for (int x = -1; x <= frame.kind.width; x++) for (int side = -1; side <= 1; side++) {
            BlockPos below = frame.at(x, 0).offset(frame.stand(side).subtract(frame.core())).below();
            if (level.getBlockState(below).isAir()) level.setBlock(below, floor, Block.UPDATE_ALL);
        }
    }

    /**
     * The far end of a door is built for the player who activates it, so it must be somewhere they could build by hand.
     * This is what keeps a door from opening inside someone else's FTB Chunks claim or spawn protection.
     */
    private static boolean permitted(ServerLevel level, Frame frame, ServerPlayer builder) {
        if (builder == null) return true;
        for (int x = 0; x < frame.kind.width; x++) for (int y = 0; y < HEIGHT; y++) {
            if (!com.dorohedoro.hole.world.BlockProtection.mayPlace(builder, level, frame.at(x, y))) return false;
        }
        for (int side : new int[]{-1, 1}) {
            if (!com.dorohedoro.hole.world.BlockProtection.mayPlace(builder, level, frame.stand(side))) return false;
        }
        return true;
    }

    private static boolean safeSite(ServerLevel level, Frame frame) {
        for (int x = 0; x < frame.kind.width; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (!clearable(level, frame.at(x, y))) return false;
            }
            BlockPos base = frame.at(x, 0).below();
            if (!level.getBlockState(base).isFaceSturdy(level, base, Direction.UP)) return false;
        }
        // The player must arrive outside the opening, in a collision-free and solid-footed cell.
        return validLanding(level, frame.stand(1)) || validLanding(level, frame.stand(-1));
    }

    /** Feet and head cells the player can stand in (grass and flowers are fine), dry, on solid ground. */
    private static boolean validLanding(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return open(level, feet) && open(level, feet.above())
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean open(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).isEmpty() && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static void buildFrame(ServerLevel level, Frame frame) {
        for (int x = 0; x < frame.kind.width; x++) for (int y = 0; y < HEIGHT; y++) {
            BlockPos at = frame.at(x, y);
            // Natural cover inside the opening (leaves, tall grass) goes; the surface fills it afterwards.
            if (!level.getBlockState(at).isAir() && clearable(level, at)) level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            if (y == 0 || y == HEIGHT - 1 || x == 0 || x == frame.kind.width - 1) {
                Block b = (x == frame.kind.coreColumn && y == 0)
                        ? (frame.generated ? frame.kind.generatedCore() : frame.kind.core())
                        : (frame.generated ? frame.kind.generatedFrame() : frame.kind.frame());
                level.setBlock(frame.at(x, y), b.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void fillInterior(ServerLevel level, Frame frame) {
        for (int x = 1; x < frame.kind.width - 1; x++) for (int y = 1; y < HEIGHT - 1; y++) {
            BlockState smoke = ModBlocks.PORTAL_SURFACE.get().defaultBlockState().setValue(PortalSurfaceBlock.AXIS, frame.axis);
            level.setBlock(frame.at(x, y), smoke, Block.UPDATE_ALL);
        }
    }

    /** Called by the door surface. Never trusts the client for destination or ownership data. */
    public static void enter(ServerLevel source, BlockPos surface, Direction.Axis axis, ServerPlayer player) {
        long tick = source.getServer().getTickCount();
        if (TRAVEL_COOLDOWNS.getOrDefault(player.getUUID(), 0L) > tick) return;
        // Only bottom-row cores of an intact frame can authorize a teleport.
        for (int height = 1; height < HEIGHT - 1; height++) for (int offset = -3; offset <= 3; offset++) {
            BlockPos candidate = axis == Direction.Axis.X ? surface.offset(offset, -height, 0) : surface.offset(0, -height, offset);
            BlockEntity be = source.getBlockEntity(candidate);
            if (!(be instanceof PortalCoreBlockEntity core) || !core.active()) continue;
            PortalKind kind = source.getBlockState(candidate).is(ModBlocks.ARTIFICIAL_DOOR_CORE.get()) ? PortalKind.ARTIFICIAL
                    : source.getBlockState(candidate).is(ModBlocks.GENERATED_ARTIFICIAL_CORE.get()) ? PortalKind.ARTIFICIAL
                    : source.getBlockState(candidate).is(ModBlocks.INFERNAL_DOOR_CORE.get()) ? PortalKind.DEVIL
                    : source.getBlockState(candidate).is(ModBlocks.GENERATED_INFERNAL_CORE.get()) ? PortalKind.DEVIL : null;
            if (kind == null) continue;
            Frame frame = findFrame(source, candidate, kind);
            if (frame == null || frame.axis != axis) continue;
            if (core.destination() == null || core.destinationCore() == null) continue;
            ResourceKey<Level> destKey = ResourceKey.create(Registries.DIMENSION, core.destination());
            if (!destKey.equals(kind.target(source))) continue;
            ServerLevel destLevel = source.getServer().getLevel(destKey);
            if (destLevel == null) continue;
            // Force destination chunk availability before any entity crosses dimensions.
            destLevel.getChunkAt(core.destinationCore());
            BlockEntity otherBE = destLevel.getBlockEntity(core.destinationCore());
            if (!(otherBE instanceof PortalCoreBlockEntity other) || !other.active() || !other.id().equals(core.linkedId())
                    || !core.id().equals(other.linkedId()) || !source.dimension().location().equals(other.destination())
                    || !candidate.equals(other.destinationCore())) continue;
            Frame returnFrame = findFrame(destLevel, core.destinationCore(), kind);
            if (returnFrame == null || returnFrame.axis != frame.axis) continue;
            BlockPos landing = validLanding(destLevel, returnFrame.stand(1)) ? returnFrame.stand(1)
                    : validLanding(destLevel, returnFrame.stand(-1)) ? returnFrame.stand(-1) : null;
            if (landing == null) { notify(player, "Return doorway obstructed; travel prevented."); return; }
            TRAVEL_COOLDOWNS.put(player.getUUID(), tick + COOLDOWN_TICKS);
            Vec3 spawn = new Vec3(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
            boolean leavingHell = kind == PortalKind.DEVIL && !source.dimension().equals(Level.OVERWORLD);
            player.changeDimension(new DimensionTransition(destLevel, spawn, Vec3.ZERO, player.getYRot(), player.getXRot(),
                    DimensionTransition.PLAY_PORTAL_SOUND));
            com.dorohedoro.hole.progress.HoleAdvancements.award(player, com.dorohedoro.hole.progress.HoleAdvancements.TRAVELED_BY_DOOR);
            if (leavingHell && player.isAlive()) com.dorohedoro.hole.progress.HoleAdvancements.award(player,
                    com.dorohedoro.hole.progress.HoleAdvancements.RETURNED_FROM_HELL);
            return;
        }
    }

    /** When a core is broken, retire the paired door and remove its active smoke panels. */
    public static void closeLinkedDoors(ServerLevel source, BlockPos pos, PortalCoreBlockEntity core) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            clearSurface(source, pos, axis);
        }
        if (core.active() && core.destination() != null && core.destinationCore() != null) {
            ServerLevel target = source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, core.destination()));
            if (target != null) {
                target.getChunkAt(core.destinationCore());
                if (target.getBlockEntity(core.destinationCore()) instanceof PortalCoreBlockEntity other
                        && core.id().equals(other.linkedId()) && other.id().equals(core.linkedId())) {
                    other.deactivate();
                    for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                        clearSurface(target, core.destinationCore(), axis);
                    }
                    if (target.getBlockState(core.destinationCore()).is(ModBlocks.GENERATED_ARTIFICIAL_CORE.get())
                            || target.getBlockState(core.destinationCore()).is(ModBlocks.GENERATED_INFERNAL_CORE.get())) {
                        clearGeneratedReturnFrame(target, core.destinationCore());
                    }
                }
            }
        }
        core.deactivate();
    }

    private static void clearGeneratedReturnFrame(ServerLevel world, BlockPos core) {
        for (PortalKind kind : PortalKind.values()) for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            Frame frame = new Frame(core, axis, kind, true);
            // Source-side frame is never removed and generated frames drop no salvageable materials.
            for (int x = 0; x < kind.width; x++) for (int y = 0; y < HEIGHT; y++) {
                if (y != 0 && y != HEIGHT - 1 && x != 0 && x != kind.width - 1) continue;
                BlockPos at = frame.at(x, y);
                BlockState b = world.getBlockState(at);
                if (b.is(kind.generatedCore()) || b.is(kind.generatedFrame())) {
                    world.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void clearSurface(ServerLevel world, BlockPos core, Direction.Axis axis) {
        // We only remove generated surface blocks, never the actual frame or ordinary player blocks.
        for (int offset = -3; offset <= 3; offset++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                BlockPos p = axis == Direction.Axis.X ? core.offset(offset, y, 0) : core.offset(0, y, offset);
                if (world.getBlockState(p).is(ModBlocks.PORTAL_SURFACE.get())) {
                    world.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void notify(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
}
