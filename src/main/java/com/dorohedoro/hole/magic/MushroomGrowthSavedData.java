package com.dorohedoro.hole.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tracks ONLY temporary mushroom blocks created by smoke magic. Real/permanent
 * mushrooms are ordinary vanilla blocks and are deliberately never recorded.
 *
 * 1.21.1 SavedData persists expiry even across server restarts. No unloaded
 * chunks are force-loaded during cleanup. Expired growth is removed only if
 * the exact state still matches, avoiding destruction of unrelated buildings.
 */
public final class MushroomGrowthSavedData extends SavedData {
    private static final String DATA_NAME = "dorohedoro_mushroom_growth";
    private static final int MAX_PENDING = 2048;
    private record Growth(String blockId, long expires) {}
    private final Map<Long, Growth> temporary = new HashMap<>();

    public static MushroomGrowthSavedData get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(MushroomGrowthSavedData::new, MushroomGrowthSavedData::load), DATA_NAME);
    }

    private static MushroomGrowthSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MushroomGrowthSavedData result = new MushroomGrowthSavedData();
        ListTag list = tag.getList("growth", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < MAX_PENDING; i++) {
            CompoundTag entry = list.getCompound(i);
            String id = entry.getString("block");
            if (ResourceLocation.tryParse(id) != null) {
                result.temporary.put(entry.getLong("pos"), new Growth(id, entry.getLong("expires")));
            }
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag out, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        temporary.forEach((pos, growth) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pos);
            entry.putString("block", growth.blockId());
            entry.putLong("expires", growth.expires());
            list.add(entry);
        });
        out.put("growth", list);
        return out;
    }

    public boolean canTrack() { return temporary.size() < MAX_PENDING; }

    public void track(BlockPos pos, BlockState placed, long expires) {
        if (!canTrack()) return;
        String id = BuiltInRegistries.BLOCK.getKey(placed.getBlock()).toString();
        temporary.put(pos.asLong(), new Growth(id, expires));
        setDirty();
    }

    /** Called when a user breaks a spawned block; the harvest/drop is vanilla. */
    public void forget(BlockPos pos) {
        if (temporary.remove(pos.asLong()) != null) setDirty();
    }

    /**
     * Works with vanilla mushrooms/stems/caps. A mushroom that has already
     * been harvested, transformed or replaced is NEVER removed here.
     */
    public void expire(ServerLevel world) {
        if (temporary.isEmpty()) return;
        long now = world.getGameTime();
        boolean modified = false;
        Iterator<Map.Entry<Long, Growth>> iterator = temporary.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Growth> entry = iterator.next();
            BlockPos pos = BlockPos.of(entry.getKey());
            if (!world.hasChunkAt(pos)) continue;
            Growth growth = entry.getValue();
            BlockState present = world.getBlockState(pos);
            ResourceLocation expectedId = ResourceLocation.tryParse(growth.blockId());
            Block expected = expectedId == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(expectedId);
            // The exact expected block type is sufficient for vanilla placement;
            // a break event unregisters the position before the user can replace it.
            if (present.getBlock() != expected) {
                iterator.remove();
                modified = true;
            } else if (now >= growth.expires()) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                iterator.remove();
                modified = true;
            }
        }
        if (modified) setDirty();
    }
}
