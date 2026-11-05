package com.dorohedoro.hole.client;

import com.dorohedoro.hole.worldgen.HoleBiomes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

/**
 * Client-only sky treatment for the Hole. A layer of polluted smoke hides celestial objects,
 * rather than generating a literal roof that would break rain, daylight checks and construction.
 *
 * IMPORTANT: The Hole reuses minecraft:overworld, so its dimension-effects registration also
 * services vanilla Overworld worlds. Every effect is conditional on the camera's Hole biome.
 * Other world presets and dimensions keep the vanilla appearance.
 */
public final class HoleAtmosphere {
    private HoleAtmosphere() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(HoleAtmosphere::registerDimensionEffects);
        NeoForge.EVENT_BUS.addListener(HoleAtmosphere::fogColor);
        NeoForge.EVENT_BUS.addListener(HoleAtmosphere::fogDistance);
    }

    private static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        // The vanilla dimension-type key is used because the Hole is the actual Overworld.
        event.register(ResourceLocation.withDefaultNamespace("overworld"), new SmokeCoveredOverworld());
    }

    private static boolean isHole(ClientLevel level, net.minecraft.core.BlockPos at) {
        if (level == null || !level.dimension().equals(Level.OVERWORLD)) return false;
        var biome = level.getBiome(at);
        return biome.is(HoleBiomes.RESIDENTIAL)
                || biome.is(HoleBiomes.MARKET)
                || biome.is(HoleBiomes.INDUSTRIAL)
                || biome.is(HoleBiomes.WATERFRONT)
                || biome.is(HoleBiomes.DERELICT)
                || biome.is(HoleBiomes.GRAVEYARD)
                || biome.is(HoleBiomes.OUTSKIRTS)
                || biome.is(HoleBiomes.UNDERCITY);
    }

    /** Keep the horizon a uniform grimy grey-green, without a visible clear blue sky. */
    private static void fogColor(ViewportEvent.ComputeFogColor event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null || !isHole(minecraft.level, event.getCamera().getBlockPosition())) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return; // Don't override underwater/lava fog.
        event.setRed(0.245F);
        event.setGreen(0.272F);
        event.setBlue(0.255F);
    }

    /** Pollution should obscure the distance, not make the player's next five blocks invisible. */
    private static void fogDistance(ViewportEvent.RenderFog event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null || !isHole(minecraft.level, event.getCamera().getBlockPosition())) return;
        if (event.getType() != FogType.NONE) return;
        float far = Math.min(120.0F, event.getFarPlaneDistance());
        event.setNearPlaneDistance(Math.min(25.0F, far * 0.35F));
        event.setFarPlaneDistance(far);
        event.setCanceled(true);
    }

    /**
     * Keep a NORMAL fallback for other world presets. Only the Hole short-circuits vanilla
     * sky rendering, so the sun, moon, stars and clouds are not visibly drawn through smog.
     */
    private static final class SmokeCoveredOverworld extends DimensionSpecialEffects {
        private SmokeCoveredOverworld() {
            super(192.0F, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return fogColor.multiply(brightness * 0.94F + 0.06F,
                    brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick,
                Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix,
                boolean isFoggy, Runnable setupFog) {
            // The renderer's normal fog-colored clear background becomes a smoke canopy.
            // Do not draw sun/moon/stars; don't modify sky brightness or weather simulation.
            return isHole(level, camera.getBlockPosition());
        }

        @Override
        public boolean renderClouds(ClientLevel level, int ticks, float partialTick,
                PoseStack poseStack, double camX, double camY, double camZ,
                Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
            // Vanilla clouds look like suspended white geometric shapes; the Hole has smog.
            return isHole(level, net.minecraft.core.BlockPos.containing(camX, camY, camZ));
        }
    }
}
