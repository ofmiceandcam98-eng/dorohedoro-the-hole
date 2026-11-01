package com.dorohedoro.hole.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Pass 20.1: PHYSICAL mushroom magic. No temporary BlockDisplay substitutes.
 *
 * Small casts plant actual red/brown mushrooms (which, in vanilla, have NO
 * collision). Fungal Bloom / Spore Dominion grow genuine, solid, harvestable
 * mushroom stems and caps. All placements are air-only and require support;
 * giant sites check the entire 3x3x4 footprint and living entities before
 * scheduling. Permanent blocks remain vanilla; only immature growth expires.
 *
 * Mastery is the current skill-specific mastery (0..1000). Small mushrooms:
 *   below 150 = 30 sec, 150..299 = 2 min, >= 300 = permanent.
 * Giant mushrooms: below 300 = 45 sec, 300..599 = 3 min, >= 600 = permanent.
 *
 * World rule mobGriefing must be enabled for magic to create blocks. This
 * provides a vanilla operator control, not a full land-claim integration.
 */
public final class MushroomSpellVisuals {
    private MushroomSpellVisuals() {}
    private static final int MAX_BATCHES = 32;
    private static final int MAX_GIANT_PER_CAST = 2;
    private static final ArrayDeque<Batch> BATCHES = new ArrayDeque<>();

    private record Placed(BlockPos pos, BlockState state, int delay) {}
    private record Batch(ServerLevel world, UUID casterId, List<Placed> pending,
                         long start, long expires, boolean permanent) {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MushroomSpellVisuals::serverTick);
        NeoForge.EVENT_BUS.addListener(MushroomSpellVisuals::blockBroken);
    }

    /** Called exactly once, after the caster's smoke reaches its destination. */
    public static void animate(ServerLevel world, UUID casterId, SmokeSkill skill, Vec3 impact,
                               int phase, int index, boolean focused, long castTick, int mastery) {
        if (phase != 2 || !world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        ServerPlayer caster = world.getServer().getPlayerList().getPlayer(casterId);
        if (caster == null || caster.level() != world) return;
        // Prevent invisible block placement on unobserved dimensions and extreme spam.
        if (BATCHES.size() >= MAX_BATCHES) return;
        BlockPos foot = BlockPos.containing(impact.add(0, -0.96, 0));
        List<Placed> small = smallCluster(world, foot, focused, index, castTick);
        boolean permanentSmall = mastery >= 300;
        long smallExpiry = world.getGameTime() + (mastery >= 150 ? 2400 : 600);
        schedule(world, casterId, small, smallExpiry, permanentSmall);

        if (skill != SmokeSkill.FUNGAL_BLOOM && skill != SmokeSkill.SPORE_DOMINION) return;
        long giantExpiry = world.getGameTime() + (mastery >= 300 ? 3600 : 900);
        boolean permanentGiant = mastery >= 600;
        // Fungal Bloom grows one structure. Focused Spore Dominion grows two.
        int count = skill == SmokeSkill.SPORE_DOMINION && focused ? MAX_GIANT_PER_CAST : 1;
        for (int n = 0; n < count; n++) {
            BlockPos target = foot.offset((castTick % 2 == 0 ? 3 : -3) + n * -5, 0,
                                          (castTick % 3 == 0 ? 3 : -3) + n * 4);
            int radius = mastery >= 300 ? 2 : 1;
            int stemHeight = mastery >= 300 ? 5 : 3;
            BlockPos site = findGiantSite(world, target, radius, stemHeight);
            // Dense city streets can still produce a smaller growth if the
            // taller, wider form lacks clearance; never push through walls.
            if (site == null && radius == 2) {
                radius = 1;
                stemHeight = 3;
                site = findGiantSite(world, target, radius, stemHeight);
            }
            if (site != null) {
                List<Placed> plan = giantPlan(site, (index + n) % 2 == 0, radius, stemHeight);
                schedule(world, casterId, plan, giantExpiry, permanentGiant);
            }
        }
    }

    private static void schedule(ServerLevel world, UUID casterId, List<Placed> blocks,
                                 long expires, boolean permanent) {
        if (blocks.isEmpty() || BATCHES.size() >= MAX_BATCHES) return;
        if (!permanent && !MushroomGrowthSavedData.get(world).canTrack()) return;
        BATCHES.addLast(new Batch(world, casterId, blocks, world.getGameTime(), expires, permanent));
    }

    private static List<Placed> smallCluster(ServerLevel world, BlockPos foot, boolean focused,
                                             int index, long castTick) {
        List<Placed> placements = new ArrayList<>();
        int count = focused ? 5 : 3;
        for (int i = 0; i < count; i++) {
            double theta = Math.PI * 2 * i / count + (castTick % 9) * 0.13;
            BlockPos pos = foot.offset((int)Math.round(Math.cos(theta) * 1.8), 0,
                                      (int)Math.round(Math.sin(theta) * 1.8));
            if (!world.isInWorldBounds(pos) || !world.hasChunkAt(pos) || !world.isEmptyBlock(pos)) continue;
            BlockState mushroom = ((i + index) % 2 == 0 ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM).defaultBlockState();
            // Vanilla support/light rules; mushrooms naturally break if support
            // is subsequently lost, and can be harvested and bone-mealed.
            if (mushroom.canSurvive(world, pos)) placements.add(new Placed(pos, mushroom, i));
        }
        return placements;
    }

    private static BlockPos findGiantSite(ServerLevel world, BlockPos preferred,
                                          int capRadius, int stemHeight) {
        int[][] offsets = {{0,0}, {3,0}, {-3,0}, {0,3}, {0,-3}, {4,4}, {-4,-4}};
        for (int[] offset : offsets) {
            BlockPos site = preferred.offset(offset[0], 0, offset[1]);
            // Avoid filling a neighborhood with permanent giant caps.
            int neighboringCaps = 0;
            for (BlockPos nearby : BlockPos.betweenClosed(site.offset(-5, 0, -5), site.offset(5, 4, 5))) {
                if (!world.hasChunkAt(nearby)) continue;
                BlockState nearbyState = world.getBlockState(nearby);
                if (nearbyState.is(Blocks.BROWN_MUSHROOM_BLOCK) || nearbyState.is(Blocks.RED_MUSHROOM_BLOCK)) {
                    neighboringCaps++;
                    if (neighboringCaps > 26) break;
                }
            }
            if (neighboringCaps > 26) continue;
            if (!world.isInWorldBounds(site) || !world.hasChunkAt(site)) continue;
            BlockPos below = site.below();
            if (!world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) continue;
            boolean empty = true;
            for (int y = 0; y <= stemHeight && empty; y++) {
                int r = y == stemHeight ? capRadius : 0;
                for (int x = -r; x <= r && empty; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos p = site.offset(x, y, z);
                        if (!world.isInWorldBounds(p) || !world.hasChunkAt(p) || !world.isEmptyBlock(p)) {
                            empty = false;
                            break;
                        }
                    }
                }
            }
            if (!empty) continue;
            // No living entity may be trapped inside the future solid stem.
            AABB trunk = new AABB(site).expandTowards(0, stemHeight, 0);
            if (!world.getEntitiesOfClass(LivingEntity.class, trunk,
                    LivingEntity::isAlive).isEmpty()) continue;
            return site;
        }
        return null;
    }

    private static List<Placed> giantPlan(BlockPos root, boolean brown,
                                          int capRadius, int stemHeight) {
        List<Placed> result = new ArrayList<>();
        BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
        for (int y = 0; y < stemHeight; y++)
            result.add(new Placed(root.above(y), stem, y * 3));
        BlockState cap = (brown ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK).defaultBlockState();
        for (int x = -capRadius; x <= capRadius; x++) for (int z = -capRadius; z <= capRadius; z++) {
            BlockState oriented = cap.setValue(HugeMushroomBlock.NORTH, z == -capRadius)
                                     .setValue(HugeMushroomBlock.SOUTH, z == capRadius)
                                     .setValue(HugeMushroomBlock.WEST, x == -capRadius)
                                     .setValue(HugeMushroomBlock.EAST, x == capRadius)
                                     .setValue(HugeMushroomBlock.UP, true)
                                     .setValue(HugeMushroomBlock.DOWN, false);
            result.add(new Placed(root.offset(x, stemHeight, z), oriented,
                                  stemHeight * 3 + Math.abs(x) + Math.abs(z)));
        }
        return result;
    }

    private static void serverTick(ServerTickEvent.Post event) {
        // Time-sliced real block placement; nearby players receive vanilla
        // block updates and normal collision/harvesting, not cosmetic displays.
        Iterator<Batch> it = BATCHES.iterator();
        while (it.hasNext()) {
            Batch batch = it.next();
            if (batch.world().getServer() != event.getServer()) { it.remove(); continue; }
            Iterator<Placed> steps = batch.pending().iterator();
            while (steps.hasNext()) {
                Placed step = steps.next();
                if (batch.world().getGameTime() - batch.start() < step.delay()) continue;
                steps.remove();
                BlockPos pos = step.pos();
                if (!batch.world().isInWorldBounds(pos) || !batch.world().hasChunkAt(pos) ||
                    !batch.world().isEmptyBlock(pos) ||
                    !batch.world().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) continue;
                ServerPlayer owner = batch.world().getServer().getPlayerList().getPlayer(batch.casterId());
                if (owner == null || !owner.mayUseItemAt(pos, Direction.UP,
                        new ItemStack(step.state().getBlock()))) continue;
                // Avoid encasing a moving player or mob during staged growth.
                if (!(step.state().is(Blocks.BROWN_MUSHROOM) || step.state().is(Blocks.RED_MUSHROOM))
                    && !batch.world().getEntitiesOfClass(LivingEntity.class, new AABB(pos),
                        LivingEntity::isAlive).isEmpty()) continue;
                // Normal vanilla small mushrooms must still be supportable.
                if ((step.state().is(Blocks.RED_MUSHROOM) || step.state().is(Blocks.BROWN_MUSHROOM))
                    && !step.state().canSurvive(batch.world(), pos)) continue;
                MushroomGrowthSavedData data = null;
                if (!batch.permanent()) {
                    data = MushroomGrowthSavedData.get(batch.world());
                    if (!data.canTrack()) continue;
                }
                if (batch.world().setBlock(pos, step.state(), 3) && data != null) {
                    data.track(pos, step.state(), batch.expires());
                }
            }
            if (batch.pending().isEmpty()) it.remove();
        }
        // Check all loaded dimensions, not just where a particular caster is
        // online. Saves survive restarts; unloaded chunks are never force-loaded.
        if (event.getServer().getTickCount() % 20 == 0) {
            for (ServerLevel world : event.getServer().getAllLevels()) {
                MushroomGrowthSavedData.get(world).expire(world);
            }
        }
    }

    private static void blockBroken(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level)
            MushroomGrowthSavedData.get(level).forget(event.getPos());
    }
}
