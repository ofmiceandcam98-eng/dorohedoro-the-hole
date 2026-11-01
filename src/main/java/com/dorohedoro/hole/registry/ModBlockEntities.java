package com.dorohedoro.hole.registry;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.portal.PortalCoreBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Dorohedoro.MOD_ID);
    public static final Supplier<BlockEntityType<PortalCoreBlockEntity>> PORTAL_CORE = TYPES.register("portal_core", () ->
            BlockEntityType.Builder.of(PortalCoreBlockEntity::new,
                    ModBlocks.ARTIFICIAL_DOOR_CORE.get(), ModBlocks.INFERNAL_DOOR_CORE.get(),
                    ModBlocks.GENERATED_ARTIFICIAL_CORE.get(), ModBlocks.GENERATED_INFERNAL_CORE.get()).build(null));
    private ModBlockEntities() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
