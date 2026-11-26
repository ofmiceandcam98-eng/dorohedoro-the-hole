package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.worldgen.SorcererVanillaBiomeSource;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Sorcerers' World uses vanilla terrain and biomes, but its towns are its own. Structures tagged
 * {@code #dorohedoro:not_in_sorcerers_world} (vanilla villages and pillager outposts) are refused there, so a medieval
 * village never sits beside a sorcerers' town. Every other vanilla structure (temples, mineshafts, strongholds, ancient
 * cities) still generates, and nothing changes in any other dimension.
 */
@Mixin(Structure.class)
public abstract class SorcererStructureFilterMixin {
    private static final TagKey<Structure> NOT_IN_SORCERERS_WORLD = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "not_in_sorcerers_world"));

    @Inject(method = "findValidGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$sorcerersOwnTowns(Structure.GenerationContext context,
                                             CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (!(context.biomeSource() instanceof SorcererVanillaBiomeSource)) return;
        boolean refused = context.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .wrapAsHolder((Structure) (Object) this).is(NOT_IN_SORCERERS_WORLD);
        if (refused) cir.setReturnValue(Optional.empty());
    }
}
