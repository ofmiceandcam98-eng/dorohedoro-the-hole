from pathlib import Path
import json,re
r=Path('/mnt/data/doro_pass36_work')
p=r/'src/main/java/com/dorohedoro/hole/registry/ModBlocks.java'
s=p.read_text()
s=s.replace('import com.dorohedoro.hole.world.light.GlowMothLightBlock;', 'import com.dorohedoro.hole.world.light.GlowMothLightBlock;\nimport com.dorohedoro.hole.world.light.CavernLampBlock;')
old='''public static final DeferredBlock<Block> CAVERN_LAMP = register("cavern_lamp", () -> new net.minecraft.world.level.block.LanternBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.6F, 5.0F)
                    .lightLevel(state -> 13).sound(SoundType.GLASS).requiresCorrectToolForDrops()));'''
new='''public static final DeferredBlock<Block> CAVERN_LAMP = register("cavern_lamp", () -> new CavernLampBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.6F, 5.0F)
                    .lightLevel(state -> 12).sound(SoundType.GLASS).requiresCorrectToolForDrops()));'''
assert old in s
p.write_text(s.replace(old,new))

p=r/'src/main/java/com/dorohedoro/hole/world/light/CavernLampBlock.java'
p.write_text('''package com.dorohedoro.hole.world.light;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
'''.replace('import net.minecraft.world.level.block.state.properties.BlockStateProperties;\nimport net.minecraft.world.phys.shapes', 'import net.minecraft.world.phys.shapes'))

# Reauthor small lamp geometry to match Minecraft 16-unit grid and preserve textures.
base=r/'src/main/resources/assets/dorohedoro/models/block'
def elem(p1,p2,tex):
    return {'from':p1,'to':p2,'faces':{k:{'texture':'#'+tex} for k in ['north','south','east','west','up','down']}}
tex={'particle':'dorohedoro:block/cavern_lamp_frame','frame':'dorohedoro:block/cavern_lamp_frame','glow':'dorohedoro:block/cavern_lamp_glow','chain':'minecraft:block/chain'}
frame=[elem([5,1,5],[11,2,11],'frame'),elem([5,9,5],[11,10,11],'frame'),elem([6,2,6],[10,9,10],'glow')]
for x,z in [(5,5),(10,5),(5,10),(10,10)]: frame.append(elem([x,2,z],[x+1,9,z+1],'frame'))
stand={'textures':tex,'elements':frame+[elem([7,10,7],[9,11,9],'frame')]}
hang={'textures':tex,'elements':[dict(e,**{'from':[e['from'][0],e['from'][1]+5,e['from'][2]],'to':[e['to'][0],e['to'][1]+5,e['to'][2]]}) for e in frame]+[elem([7,15,7],[9,16,9],'chain')]}
# Wall: north-facing bracket from wall plane z=0 into lantern body at z~7;
# y offset +3 to make wall lamp sit at window-head height.
wall_e=[]
for e in frame:
    wall_e.append(dict(e,**{'from':[e['from'][0],e['from'][1]+3,e['from'][2]],'to':[e['to'][0],e['to'][1]+3,e['to'][2]]}))
wall={'textures':tex,'elements':wall_e+[elem([7,11,0],[9,13,8],'frame'),elem([6,10,0],[10,11,3],'frame')]}
for name,model in [('standing',stand),('hanging',hang),('wall',wall)]:
    (base/f'cavern_lamp_{name}.json').write_text(json.dumps(model,indent=2)+'\n')
state={'variants':{}}
for w in ['false','true']:
 for h in ['false','true']:
  for face,rot in [('north',0),('east',90),('south',180),('west',270)]:
   for water in ['false','true']:
    k=f'wall={w},hanging={h},facing={face},waterlogged={water}'
    value={'model':'dorohedoro:block/cavern_lamp_'+('wall' if w=='true' else 'hanging' if h=='true' else 'standing')}
    if w=='true': value['y']=rot
    state['variants'][k]=value
(r/'src/main/resources/assets/dorohedoro/blockstates/cavern_lamp.json').write_text(json.dumps(state,indent=2)+'\n')
print('patched lamp states',len(state['variants']))
