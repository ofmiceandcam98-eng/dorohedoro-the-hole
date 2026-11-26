package com.dorohedoro.hole.portal;

import com.dorohedoro.hole.registry.ModBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Both ends persist their own UUID and opposite dimension/position in chunk NBT. */
public final class PortalCoreBlockEntity extends BlockEntity {
    private UUID id = UUID.randomUUID();
    private UUID linkedId;
    private ResourceLocation destination;
    private BlockPos destinationCore;
    private boolean active;

    public PortalCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PORTAL_CORE.get(), pos, state);
    }

    public UUID id() { return id; }
    public UUID linkedId() { return linkedId; }
    public ResourceLocation destination() { return destination; }
    public BlockPos destinationCore() { return destinationCore; }
    public boolean active() { return active; }

    public void linkTo(PortalCoreBlockEntity other, ResourceLocation dimension) {
        this.linkedId = other.id;
        this.destination = dimension;
        this.destinationCore = other.getBlockPos().immutable();
        this.active = true;
        setChanged();
    }

    public void deactivate() {
        active = false;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("PortalId")) id = tag.getUUID("PortalId");
        linkedId = tag.hasUUID("LinkedId") ? tag.getUUID("LinkedId") : null;
        destination = tag.contains("DestinationDimension") ? ResourceLocation.tryParse(tag.getString("DestinationDimension")) : null;
        destinationCore = tag.contains("DestinationCore") ? BlockPos.of(tag.getLong("DestinationCore")) : null;
        active = tag.getBoolean("Active") && linkedId != null && destination != null && destinationCore != null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("PortalId", id);
        if (linkedId != null) tag.putUUID("LinkedId", linkedId);
        if (destination != null) tag.putString("DestinationDimension", destination.toString());
        if (destinationCore != null) tag.putLong("DestinationCore", destinationCore.asLong());
        tag.putBoolean("Active", active);
    }
}
