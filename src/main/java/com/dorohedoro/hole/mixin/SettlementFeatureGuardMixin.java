package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.worldgen.sorcerer.SorcererTownStructure;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.BlockBlobFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps vanilla lakes (including surface lava lakes) and boulders out of Sorcerers' World settlements. A neighbouring
 * chunk can be decorated after a settlement chunk, and these two features overwrite whatever they land on, so without
 * this a lake can carve through a house or a boulder can land on a street depending on generation order. The check
 * reads only the origin chunk's own structure references: every settlement chunk, and a 16-block margin around the
 * settlement, carries one.
 */
@Mixin({LakeFeature.class, BlockBlobFeature.class})
public abstract class SettlementFeatureGuardMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$notInSettlements(FeaturePlaceContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        ChunkAccess chunk = context.level().getChunk(context.origin());
        for (Structure structure : chunk.getAllReferences().keySet()) {
            if (structure instanceof SorcererTownStructure) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
