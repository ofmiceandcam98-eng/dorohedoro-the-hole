package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.worldgen.StructurePrecheck72;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Skips structure candidates that {@link StructurePrecheck72} proves vanilla would reject anyway. */
@Mixin(Structure.class)
public abstract class StructureMixin {
    @Inject(method = "findValidGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$skipProvablyAbsent(Structure.GenerationContext context,
                                               CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (StructurePrecheck72.provablyAbsent((Structure) (Object) this, context)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
