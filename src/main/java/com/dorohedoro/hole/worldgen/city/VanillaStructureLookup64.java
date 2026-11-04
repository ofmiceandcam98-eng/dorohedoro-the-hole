package com.dorohedoro.hole.worldgen.city;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Binds real vanilla structure-reference data at SURFACE generation time.
 * STRUCTURE_STARTS and STRUCTURE_REFERENCES precede SURFACE in vanilla chunk status.
 * Never scans or generates neighboring chunks and never predicts a structure by seed.
 */
public final class VanillaStructureLookup64 {
    private VanillaStructureLookup64() {}

    public static List<VanillaStructureBuffer64.Box> forChunk(StructureManager manager, ChunkPos chunk) {
        if (!manager.shouldGenerateStructures()) return List.of();
        var registry = manager.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<VanillaStructureBuffer64.Box> result = new ArrayList<>();
        for (StructureStart start : manager.startsForStructure(chunk, (Structure structure) -> {
            ResourceLocation id = registry.getKey(structure);
            return id != null && "minecraft".equals(id.getNamespace()) && switch (id.getPath()) {
                case "stronghold", "mineshaft", "mineshaft_mesa", "trial_chambers", "ancient_city" -> true;
                default -> false;
            };
        })) {
            if (!start.isValid()) continue;
            BoundingBox bounds = start.getBoundingBox();
            VanillaStructureBuffer64.Box box = new VanillaStructureBuffer64.Box(
                    bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ());
            if (box.nearChunk(chunk.getMinBlockX(), chunk.getMinBlockZ())) result.add(box);
        }
        return List.copyOf(result);
    }
}
