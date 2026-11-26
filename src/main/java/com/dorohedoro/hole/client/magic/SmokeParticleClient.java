package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.registry.ModSmokeParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Client-only, original sprite animations. The server only sends particle events. */
public final class SmokeParticleClient {
    private SmokeParticleClient() {}

    public static void register(IEventBus bus) {
        bus.addListener(SmokeParticleClient::registerProviders);
    }

    private static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModSmokeParticles.SOOT_PLUME.get(),
                sprites -> new Provider(sprites, 0));
        event.registerSpriteSet(ModSmokeParticles.SMOKE_FILAMENT.get(),
                sprites -> new Provider(sprites, 1));
        event.registerSpriteSet(ModSmokeParticles.HEALING_FIBER.get(),
                sprites -> new Provider(sprites, 2));
        event.registerSpriteSet(ModSmokeParticles.FUNGAL_SPORE.get(),
                sprites -> new Provider(sprites, 3));
    }

    private static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int variant;
        private Provider(SpriteSet sprites, int variant) {
            this.sprites = sprites;
            this.variant = variant;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new AnimatedSmokeParticle(world, x, y, z, vx, vy, vz, sprites, variant);
        }
    }

    private static final class AnimatedSmokeParticle extends TextureSheetParticle {
        private final SpriteSet sprites;
        private final float startSize;
        private final int variant;
        private final float turn;

        private AnimatedSmokeParticle(ClientLevel level, double x, double y, double z,
                                      double vx, double vy, double vz,
                                      SpriteSet sprites, int variant) {
            super(level, x, y, z);
            this.sprites = sprites;
            this.variant = variant;
            this.hasPhysics = false;
            this.friction = variant == 1 ? .88f : .93f;
            this.xd = vx * .20 + (this.random.nextDouble() - .5) * .018;
            this.yd = vy * .20 + (variant < 2 ? .008 : .003);
            this.zd = vz * .20 + (this.random.nextDouble() - .5) * .018;
            this.lifetime = (variant == 0 ? 22 : variant == 1 ? 17 : 14) + this.random.nextInt(7);
            this.startSize = (variant == 0 ? .39f : variant == 1 ? .19f : .22f)
                    * (.8f + this.random.nextFloat() * .45f);
            this.quadSize = this.startSize;
            this.alpha = 0f;
            this.turn = (this.random.nextFloat() - .5f) * .045f;
            this.roll = this.random.nextFloat() * 6.283185f;
            setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) return;
            float t = Math.min(1f, this.age / (float) Math.max(1, this.lifetime));
            // No abrupt sprite pops: quick fade-in, slow diffusion, dissipating tail.
            float fadeIn = Math.min(1f, t / .17f);
            float fadeOut = Math.min(1f, (1f - t) / .42f);
            this.alpha = Math.max(0f, Math.min(fadeIn, fadeOut))
                    * (variant == 0 ? .79f : variant == 1 ? .74f : .90f);
            this.quadSize = this.startSize * (variant < 2 ? .74f + .85f*t : .82f + .22f*t);
            this.oRoll = this.roll;
            this.roll += this.turn;
            setSpriteFromAge(this.sprites);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }
}
