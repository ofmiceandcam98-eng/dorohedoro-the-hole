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
    /** v54: pyramids and jungle temples measure the city's ground, not the flat noise under it. */
    @Inject(method = "getLowestY(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;II)I",
            at = @At("HEAD"), cancellable = true)
    private static void dorohedoro$cityGround(Structure.GenerationContext context, int width, int depth,
                                             CallbackInfoReturnable<Integer> cir) {
        Integer y = com.dorohedoro.hole.worldgen.SurfaceStructures73.cityLowestY(context, width, depth);
        if (y != null) cir.setReturnValue(y);
    }

    @Inject(method = "findValidGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$skipProvablyAbsent(Structure.GenerationContext context,
                                               CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (StructurePrecheck72.provablyAbsent((Structure) (Object) this, context)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
