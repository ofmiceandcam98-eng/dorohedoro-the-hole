package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.worldgen.StructurePrecheck72;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code /locate} and explorer maps ask {@link StructureCheck} about each candidate chunk. For a chunk not in memory,
 * vanilla first reads it from disk through the chunk IO thread and waits for the answer, which costs about a
 * millisecond for every one of roughly 40,000 candidates. If {@link StructurePrecheck72} proves the structure cannot
 * start in that chunk, no saved chunk can hold that start either, so the read is skipped. Chunks already in memory
 * keep vanilla's answer: the injection sits after that lookup. World generation does not use this class.
 */
@Mixin(StructureCheck.class)
public abstract class StructureCheckMixin {
    @Shadow @Final private RegistryAccess registryAccess;
    @Shadow @Final private StructureTemplateManager structureTemplateManager;
    @Shadow @Final private ChunkGenerator chunkGenerator;
    @Shadow @Final private RandomState randomState;
    @Shadow @Final private LevelHeightAccessor heightAccessor;
    @Shadow @Final private BiomeSource biomeSource;
    @Shadow @Final private long seed;

    @Inject(method = "checkStart",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/StructureCheck;tryLoadFromStorage(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;ZJ)Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;"),
            cancellable = true)
    private void dorohedoro$skipDiskForProvablyAbsent(ChunkPos chunkPos, Structure structure, StructurePlacement placement,
                                                     boolean skipKnownStructures, CallbackInfoReturnable<StructureCheckResult> cir) {
        // Same context vanilla's canCreateStructure builds for this chunk.
        Structure.GenerationContext context = new Structure.GenerationContext(registryAccess, chunkGenerator, biomeSource,
                randomState, structureTemplateManager, seed, chunkPos, heightAccessor, structure.biomes()::contains);
        if (StructurePrecheck72.provablyAbsent(structure, context)) {
            cir.setReturnValue(StructureCheckResult.START_NOT_PRESENT);
        }
    }
}
