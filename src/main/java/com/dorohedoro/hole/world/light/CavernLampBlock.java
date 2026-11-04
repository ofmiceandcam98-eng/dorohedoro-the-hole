package com.dorohedoro.hole.world.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Compact industrial lamp. Old hanging/floor states are preserved; a new wall
 * placement mode uses four facing directions and an actual mounting bracket.
 * No inventory or recipe IDs change. Model geometry matches the small shape.
 */
public final class CavernLampBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty HANGING = LanternBlock.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape FLOOR = box(5, 0, 5, 11, 11, 11);
    private static final VoxelShape CEILING = box(5, 5, 5, 11, 16, 11);
    private static final VoxelShape NORTH = box(5, 4, 0, 11, 13, 11);
    private static final VoxelShape SOUTH = box(5, 4, 5, 11, 13, 16);
    private static final VoxelShape EAST = box(5, 4, 5, 16, 13, 11);
    private static final VoxelShape WEST = box(0, 4, 5, 11, 13, 11);

    public CavernLampBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WALL, false).setValue(HANGING, false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        // Prefer the face actually clicked, then find another supported mounting
        // surface. Direction points away from the support block.
        Direction[] dirs = context.getNearestLookingDirections();
        Direction clicked = context.getClickedFace();
        BlockState first = mounted(clicked, water);
        if (first.canSurvive(context.getLevel(), context.getClickedPos())) return first;
        for (Direction dir : dirs) {
            BlockState candidate = mounted(dir, water);
            if (candidate.canSurvive(context.getLevel(), context.getClickedPos())) return candidate;
        }
        return null;
    }

    private BlockState mounted(Direction face, boolean water) {
        return defaultBlockState().setValue(WATERLOGGED, water)
                .setValue(WALL, face.getAxis().isHorizontal())
                .setValue(HANGING, face == Direction.DOWN)
                .setValue(FACING, face.getAxis().isHorizontal() ? face : Direction.NORTH);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Direction supportDirection = state.getValue(WALL) ? state.getValue(FACING).getOpposite()
                : state.getValue(HANGING) ? Direction.UP : Direction.DOWN;
        return Block.canSupportCenter(level, pos.relative(supportDirection), supportDirection.getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        Direction support = state.getValue(WALL) ? state.getValue(FACING).getOpposite()
                : state.getValue(HANGING) ? Direction.UP : Direction.DOWN;
        return direction == support && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (!state.getValue(WALL)) return state.getValue(HANGING) ? CEILING : FLOOR;
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> FLOOR;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WALL, HANGING, FACING, WATERLOGGED);
    }
}
