package com.dorohedoro.hole.mixin;

import com.dorohedoro.hole.worldgen.RealmBiomes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * No ordinary rain or thunder in the Sorcerers' World. Vanilla weather is shared by every dimension, and the realm now
 * uses vanilla biomes that would otherwise rain whenever the Hole does. Rain level drives rendering, sounds, snow
 * build-up, lightning and fire, so zeroing it here covers all of them on both client and server.
 */
@Mixin(Level.class)
public abstract class SorcererWeatherMixin {
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$noSorcererRain(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (RealmBiomes.SORCERERS_WORLD.equals(((Level) (Object) this).dimension())) cir.setReturnValue(0.0F);
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void dorohedoro$noSorcererThunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (RealmBiomes.SORCERERS_WORLD.equals(((Level) (Object) this).dimension())) cir.setReturnValue(0.0F);
    }
}
