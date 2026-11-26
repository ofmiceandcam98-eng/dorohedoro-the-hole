package com.dorohedoro.hole.portal;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerLevel;

/** Gameplay-adapted door categories; both share the same persistence and teleport backend. */
public enum PortalKind {
    ARTIFICIAL(4, 1, ResourceLocation.fromNamespaceAndPath("dorohedoro", "sorcerers_world")),
    DEVIL(5, 2, ResourceLocation.fromNamespaceAndPath("dorohedoro", "hell"));

    public final int width;
    public final int coreColumn;
    public final ResourceLocation realm;

    PortalKind(int width, int coreColumn, ResourceLocation realm) {
        this.width = width;
        this.coreColumn = coreColumn;
        this.realm = realm;
    }

    public Block frame() { return this == ARTIFICIAL ? ModBlocks.REINFORCED_DOOR_FRAME.get() : ModBlocks.ETCHED_GATE_STONE.get(); }
    public Block core() { return this == ARTIFICIAL ? ModBlocks.ARTIFICIAL_DOOR_CORE.get() : ModBlocks.INFERNAL_DOOR_CORE.get(); }
    public Block generatedCore() { return this == ARTIFICIAL ? ModBlocks.GENERATED_ARTIFICIAL_CORE.get() : ModBlocks.GENERATED_INFERNAL_CORE.get(); }
    public Block generatedFrame() { return this == ARTIFICIAL ? ModBlocks.GENERATED_DOOR_FRAME.get() : ModBlocks.GENERATED_GATE_STONE.get(); }

    /** No general teleportation from the other dimensions: a gate only joins its realm with the Hole. */
    public ResourceKey<Level> target(ServerLevel source) {
        if (source.dimension().equals(Level.OVERWORLD)) return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, realm);
        if (source.dimension().location().equals(realm)) return Level.OVERWORLD;
        return null;
    }
}
