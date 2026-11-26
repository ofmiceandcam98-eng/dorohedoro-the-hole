package com.dorohedoro.hole.worldgen.sorcerer;

import com.dorohedoro.hole.Dorohedoro;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Structure templates converted from permission-cleared community builds
 * ({@code data/dorohedoro/structure/sorcerer_city/<id>.nbt}, made by {@code tools/community/convert-builds.mjs}).
 * Templates are authored in the mod's building frame: front at z = 0 facing north, ground (outside feet level) at y = 0.
 * They are read once from the server's resources and cached; painting goes through {@link Painter#template}, so they
 * rotate and clip to the chunk exactly like the hand-written buildings.
 */
public final class TemplateLibrary {
    public record Block(int x, int y, int z, BlockState state) {}

    public record Template(int sizeX, int sizeY, int sizeZ, List<Block> blocks) {}

    private static final Map<String, Template> CACHE = new ConcurrentHashMap<>();

    private TemplateLibrary() {}

    public static Template get(String id) {
        return CACHE.computeIfAbsent(id, TemplateLibrary::load);
    }

    private static Template load(String id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) throw new IllegalStateException("Sorcerers' World templates need a running server");
        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "structure/sorcerer_city/" + id + ".nbt");
        try (InputStream in = server.getResourceManager().getResourceOrThrow(path).open()) {
            CompoundTag root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            ListTag size = root.getList("size", Tag.TAG_INT);
            ListTag paletteTag = root.getList("palette", Tag.TAG_COMPOUND);
            List<BlockState> palette = new ArrayList<>(paletteTag.size());
            for (int i = 0; i < paletteTag.size(); i++) {
                palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), paletteTag.getCompound(i)));
            }
            ListTag blockTags = root.getList("blocks", Tag.TAG_COMPOUND);
            List<Block> blocks = new ArrayList<>(blockTags.size());
            for (int i = 0; i < blockTags.size(); i++) {
                CompoundTag b = blockTags.getCompound(i);
                ListTag pos = b.getList("pos", Tag.TAG_INT);
                blocks.add(new Block(pos.getInt(0), pos.getInt(1), pos.getInt(2), palette.get(b.getInt("state"))));
            }
            return new Template(size.getInt(0), size.getInt(1), size.getInt(2), List.copyOf(blocks));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read Sorcerers' World template " + path, e);
        }
    }
}
