package com.dorohedoro.hole.worldgen.sorcerer;

import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

/** Painter output into the chunk being generated. Writes outside the chunk's box are dropped. */
final class WorldSink implements Painter.Sink {
    private final WorldGenLevel level;
    private final BoundingBox clip;
    private final RandomSource random;

    WorldSink(WorldGenLevel level, BoundingBox clip, RandomSource random) {
        this.level = level;
        this.clip = clip;
        this.random = random;
    }

    @Override
    public boolean inside(BlockPos pos) {
        return clip.isInside(pos);
    }

    @Override
    public void set(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
        // Fences, walls, panes and stairs settle their connections when the chunk finishes.
        if (state.getBlock() instanceof CrossCollisionBlock || state.getBlock() instanceof WallBlock
                || state.getBlock() instanceof StairBlock) {
            level.getChunk(pos).markPosForPostprocessing(pos);
        }
    }

    @Override
    public BlockState get(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public void loot(BlockPos pos, ResourceKey<LootTable> table) {
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, table);
    }

    @Override
    public void sign(BlockPos pos, String[] lines) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SignBlockEntity)) return;
        CompoundTag front = new CompoundTag();
        ListTag messages = new ListTag();
        for (int i = 0; i < 4; i++) {
            String line = i < lines.length ? lines[i] : "";
            messages.add(StringTag.valueOf(new JsonPrimitive(line).toString()));
        }
        front.put("messages", messages);
        front.putString("color", "black");
        front.putBoolean("has_glowing_text", false);
        CompoundTag tag = new CompoundTag();
        tag.put("front_text", front);
        tag.putBoolean("is_waxed", true);
        // Loading the tag sets the text without touching the (not yet existing) level.
        be.loadWithComponents(tag, level.registryAccess());
        be.setChanged();
    }

    @Override
    public void villager(BlockPos pos, VillagerProfession profession) {
        Villager villager = EntityType.VILLAGER.create(level.getLevel());
        if (villager == null) return;
        villager.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360F, 0F);
        villager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        if (profession != VillagerProfession.NONE) {
            villager.setVillagerData(villager.getVillagerData().setProfession(profession));
        }
        villager.setPersistenceRequired();
        level.addFreshEntityWithPassengers(villager);
    }
}
