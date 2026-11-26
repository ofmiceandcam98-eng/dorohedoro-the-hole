package com.dorohedoro.hole.portal;

import com.dorohedoro.hole.config.SmokeWorldConfig;
import com.dorohedoro.hole.magic.SmokeEngine;
import com.dorohedoro.hole.magic.SmokeState;
import com.dorohedoro.hole.progress.HoleAdvancements;
import com.dorohedoro.hole.registry.ModBlocks;
import com.dorohedoro.hole.world.BlockProtection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Smoke Doors: a sorcerer breathes out a doorway of condensed smoke on the ground in front of them, and a matching exit
 * opens in the other world (the Hole and the Sorcerers' World only). Both ends dissolve after a while.
 *
 * <ul>
 *   <li>Needs a sorcerer of at least the configured rank, and the configured smoke, which is spent on success only.</li>
 *   <li>The door stands on solid ground with a clear 2x3 opening, framed in condensed smoke. Nothing but natural ground
 *       cover is ever replaced, and both ends must be on land the sorcerer may build on (claims, spawn protection).</li>
 *   <li>Anyone may walk through while it is open. Breaking any of its smoke, or the time running out, closes both ends.</li>
 *   <li>Open doors are saved with the world, so a restart cannot leave a half-door behind.</li>
 * </ul>
 * Built on the same surface block and travel cooldown as the crafted doors; it looks like smoke, not a Nether portal.
 */
public final class SmokeDoors {
    /** One end: the opening's lower-left cell, and the axis the doorway runs along. */
    record End(ResourceKey<Level> dimension, BlockPos base, Direction.Axis axis) {
        /** Column -1..2 (frame posts at -1 and 2, opening at 0 and 1), row 0..3 (lintel at 3). */
        BlockPos at(int column, int row) {
            return axis == Direction.Axis.X ? base.offset(column, row, 0) : base.offset(0, row, column);
        }
        BlockPos front(int sign) {
            return axis == Direction.Axis.X ? base.offset(0, 0, sign) : base.offset(sign, 0, 0);
        }
        boolean opening(BlockPos pos) {
            for (int c = 0; c <= 1; c++) for (int r = 0; r <= 2; r++) if (at(c, r).equals(pos)) return true;
            return false;
        }
    }

    record Door(UUID id, UUID owner, long expires, End a, End b) {}

    private static final int REACH = 6;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private SmokeDoors() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SmokeDoors::tick);
    }

    /** The sorcerer's request to open a door where they are looking. All checks happen here, on the server. */
    public static void open(ServerPlayer player) {
        if (SmokeEngine.innate(player) == null) {
            message(player, "only_sorcerers");
            return;
        }
        SmokeState state = SmokeEngine.state(player);
        if (state.rank() < SmokeWorldConfig.SMOKE_DOOR_MIN_RANK.get()) {
            message(player, "too_weak", SmokeWorldConfig.SMOKE_DOOR_MIN_RANK.get());
            return;
        }
        int cost = SmokeWorldConfig.SMOKE_DOOR_COST.get();
        if (state.smoke() < cost) {
            message(player, "not_enough_smoke", cost);
            return;
        }
        long now = player.server.getTickCount();
        long ready = COOLDOWNS.getOrDefault(player.getUUID(), 0L);
        if (ready > now) {
            message(player, "cooldown", (ready - now + 19) / 20);
            return;
        }
        ServerLevel here = player.serverLevel();
        ResourceKey<Level> otherKey = PortalKind.ARTIFICIAL.target(here);
        ServerLevel there = otherKey == null ? null : here.getServer().getLevel(otherKey);
        if (there == null) {
            message(player, "wrong_world");
            return;
        }
        End near = aim(player, here);
        if (near == null) {
            message(player, "no_room_here");
            return;
        }
        End far = findFar(there, near, player);
        if (far == null) {
            message(player, "no_room_there");
            return;
        }
        build(here, near);
        build(there, far);
        int seconds = SmokeWorldConfig.SMOKE_DOOR_SECONDS.get();
        Data data = Data.get(player.server);
        data.doors.add(new Door(UUID.randomUUID(), player.getUUID(), here.getGameTime() + seconds * 20L, near, far));
        data.setDirty();
        SmokeEngine.spend(player, cost);
        COOLDOWNS.put(player.getUUID(), now + SmokeWorldConfig.SMOKE_DOOR_COOLDOWN.get() * 20L);
        here.playSound(null, near.base(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.6F);
        here.sendParticles(ParticleTypes.LARGE_SMOKE, near.base().getX() + 0.5, near.base().getY() + 1.5, near.base().getZ() + 0.5,
                40, 1.0, 1.2, 1.0, 0.01);
        message(player, "opened", seconds);
        HoleAdvancements.award(player, HoleAdvancements.SMOKE_DOOR_OPENED);
    }

    /** The doorway's place: on top of the block the sorcerer looks at, across their line of sight. */
    @Nullable
    private static End aim(ServerPlayer player, ServerLevel level) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(REACH)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getDirection() != Direction.UP) return null;
        Direction facing = player.getDirection();
        Direction.Axis axis = facing.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
        BlockPos base = hit.getBlockPos().above();
        End end = new End(level.dimension(), base, axis);
        return fits(level, end, player) ? end : null;
    }

    /** Clear cells (natural cover allowed), a solid floor under the posts and the opening, buildable, with a landing. */
    private static boolean fits(ServerLevel level, End end, ServerPlayer player) {
        for (int c = -1; c <= 2; c++) {
            BlockPos floor = end.at(c, 0).below();
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return false;
            for (int r = 0; r <= 3; r++) {
                BlockPos p = end.at(c, r);
                if (!level.isInWorldBounds(p) || !PortalService.clearable(level, p)) return false;
                if (!BlockProtection.mayPlace(player, level, p)) return false;
            }
        }
        return landing(level, end) != null;
    }

    @Nullable
    private static BlockPos landing(ServerLevel level, End end) {
        for (int sign : new int[]{1, -1}) {
            BlockPos feet = end.front(sign);
            BlockPos below = feet.below();
            if (PortalService.open(level, feet) && PortalService.open(level, feet.above())
                    && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return feet;
        }
        return null;
    }

    /** The exit: the nearest ground site to the same coordinates in the other world, within 24 blocks. */
    @Nullable
    private static End findFar(ServerLevel level, End near, ServerPlayer player) {
        for (int r = 0; r <= 24; r += 4) {
            for (int dx = -r; dx <= r; dx += 4) for (int dz = -r; dz <= r; dz += 4) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                int x = near.base().getX() + dx, z = near.base().getZ() + dz;
                int y = PortalService.surfaceY(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                End end = new End(level.dimension(), new BlockPos(x, y, z), near.axis());
                if (fits(level, end, player)) return end;
            }
        }
        return null;
    }

    private static void build(ServerLevel level, End end) {
        var smoke = ModBlocks.PORTAL_SURFACE.get().defaultBlockState().setValue(PortalSurfaceBlock.AXIS, end.axis());
        var frame = ModBlocks.CONDENSED_SMOKE_BARRIER.get().defaultBlockState();
        for (int c = -1; c <= 2; c++) for (int r = 0; r <= 3; r++) {
            boolean post = c == -1 || c == 2 || r == 3;
            level.setBlock(end.at(c, r), post ? frame : smoke, Block.UPDATE_ALL);
        }
    }

    /** Removes only this door's own blocks, so nothing a player placed afterwards is touched. */
    private static void dissolve(ServerLevel level, End end) {
        for (int c = -1; c <= 2; c++) for (int r = 0; r <= 3; r++) {
            BlockPos p = end.at(c, r);
            var s = level.getBlockState(p);
            if (s.is(ModBlocks.PORTAL_SURFACE.get()) || s.is(ModBlocks.CONDENSED_SMOKE_BARRIER.get())) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, end.base().getX() + 0.5, end.base().getY() + 1.5,
                end.base().getZ() + 0.5, 12, 0.8, 1.0, 0.8, 0.01);
    }

    private static boolean intact(ServerLevel level, End end) {
        for (int c = -1; c <= 2; c++) for (int r = 0; r <= 3; r++) {
            boolean post = c == -1 || c == 2 || r == 3;
            if (!level.getBlockState(end.at(c, r)).is(post ? ModBlocks.CONDENSED_SMOKE_BARRIER.get() : ModBlocks.PORTAL_SURFACE.get())) return false;
        }
        return true;
    }

    /** Called from the door surface. Returns true if the surface belongs to a Smoke Door (travel handled or not). */
    static boolean enter(ServerLevel source, BlockPos surface, ServerPlayer player) {
        Data data = Data.get(source.getServer());
        for (Door door : data.doors) {
            End from = door.a().dimension().equals(source.dimension()) && door.a().opening(surface) ? door.a()
                    : door.b().dimension().equals(source.dimension()) && door.b().opening(surface) ? door.b() : null;
            if (from == null) continue;
            End to = from == door.a() ? door.b() : door.a();
            ServerLevel destination = source.getServer().getLevel(to.dimension());
            if (destination == null) return true;
            destination.getChunkAt(to.base());
            BlockPos landing = landing(destination, to);
            if (landing == null || !intact(destination, to)) return true;
            PortalService.TRAVEL_COOLDOWNS.put(player.getUUID(), (long) source.getServer().getTickCount() + PortalService.COOLDOWN_TICKS);
            player.changeDimension(new DimensionTransition(destination, new Vec3(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5),
                    Vec3.ZERO, PortalService.facingAway(to.base(), landing), 0.0F, DimensionTransition.PLAY_PORTAL_SOUND));
            HoleAdvancements.award(player, HoleAdvancements.TRAVELED_BY_DOOR);
            return true;
        }
        return false;
    }

    /** Closes doors whose time is up or whose smoke was broken. Ends in unloaded chunks close when next loaded. */
    private static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 3) return;
        Data data = Data.get(server);
        if (data.doors.isEmpty()) return;
        long now = server.overworld().getGameTime();
        Iterator<Door> it = data.doors.iterator();
        while (it.hasNext()) {
            Door door = it.next();
            ServerLevel a = server.getLevel(door.a().dimension()), b = server.getLevel(door.b().dimension());
            boolean aLoaded = a != null && a.hasChunkAt(door.a().base()), bLoaded = b != null && b.hasChunkAt(door.b().base());
            boolean broken = (aLoaded && !intact(a, door.a())) || (bLoaded && !intact(b, door.b()));
            if (now < door.expires() && !broken) continue;
            if (aLoaded) dissolve(a, door.a());
            if (bLoaded) dissolve(b, door.b());
            if ((aLoaded || a == null) && (bLoaded || b == null)) {
                it.remove();
            } else {
                // Close the loaded end now; the other closes the moment its chunk is loaded again.
                int i = data.doors.indexOf(door);
                data.doors.set(i, new Door(door.id(), door.owner(), Math.min(door.expires(), now), door.a(), door.b()));
            }
            data.setDirty();
        }
    }

    private static void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.dorohedoro.smoke_door." + key, args), true);
    }

    /** Open Smoke Doors, saved with the Overworld's data. */
    static final class Data extends SavedData {
        private static final String NAME = "dorohedoro_smoke_doors";
        final List<Door> doors = new ArrayList<>();

        static Data get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(Data::new, Data::load, null), NAME);
        }

        static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            for (Tag t : tag.getList("Doors", Tag.TAG_COMPOUND)) {
                CompoundTag d = (CompoundTag) t;
                data.doors.add(new Door(d.getUUID("Id"), d.getUUID("Owner"), d.getLong("Expires"), end(d.getCompound("A")), end(d.getCompound("B"))));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Door door : doors) {
                CompoundTag d = new CompoundTag();
                d.putUUID("Id", door.id());
                d.putUUID("Owner", door.owner());
                d.putLong("Expires", door.expires());
                d.put("A", end(door.a()));
                d.put("B", end(door.b()));
                list.add(d);
            }
            tag.put("Doors", list);
            return tag;
        }

        private static CompoundTag end(End end) {
            CompoundTag t = new CompoundTag();
            t.putString("Dimension", end.dimension().location().toString());
            t.putLong("Base", end.base().asLong());
            t.putString("Axis", end.axis().getSerializedName());
            return t;
        }

        private static End end(CompoundTag t) {
            return new End(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(t.getString("Dimension"))),
                    BlockPos.of(t.getLong("Base")), Direction.Axis.byName(t.getString("Axis")));
        }
    }
}
