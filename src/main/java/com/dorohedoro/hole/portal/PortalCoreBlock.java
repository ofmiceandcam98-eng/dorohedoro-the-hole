package com.dorohedoro.hole.portal;

import com.dorohedoro.hole.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/** A physical door-core block; activation requires the appropriate tool and an intact frame. */
public final class PortalCoreBlock extends BaseEntityBlock {
    public static final MapCodec<PortalCoreBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(PortalKind::valueOf, PortalKind::name).fieldOf("kind").forGetter(block -> block.kind),
            propertiesCodec()
    ).apply(instance, PortalCoreBlock::new));

    private final PortalKind kind;

    public PortalCoreBlock(PortalKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalCoreBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock()) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof PortalCoreBlockEntity core) {
            PortalService.closeLinkedDoors(server, pos, core);
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.DOOR_LINKING_KEY.get())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof PortalCoreBlockEntity core) {
                player.displayClientMessage(Component.literal(core.active()
                        ? "Linked to " + core.destination() + " at " + core.destinationCore().toShortString()
                        : "This door has not been linked."), true);
            }
            return ItemInteractionResult.SUCCESS;
        }
        boolean correctTool = stack.is(kind == PortalKind.ARTIFICIAL ? ModItems.DOOR_ACTIVATION_TOOL.get() : ModItems.RITUAL_TOOL.get());
        if (!correctTool) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (state.is(kind.generatedCore())) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("This return endpoint is automatically linked; activate the original frame."), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            PortalService.activate(serverLevel, pos, kind, player);
        }
        return ItemInteractionResult.SUCCESS;
    }
}
