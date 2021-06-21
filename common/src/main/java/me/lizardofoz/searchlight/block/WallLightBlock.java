package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;

public class WallLightBlock extends WallMountedBlock
{
    protected static final VoxelShape CEILING_X_SHAPE = Block.createCuboidShape(6, 14, 5, 10, 16, 11);
    protected static final VoxelShape CEILING_Z_SHAPE = Block.createCuboidShape(5, 14, 6, 11, 16, 10);
    protected static final VoxelShape FLOOR_X_SHAPE = Block.createCuboidShape(6, 0, 5, 10, 2, 11);
    protected static final VoxelShape FLOOR_Z_SHAPE = Block.createCuboidShape(5, 0, 6, 11, 2, 10);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5, 8, 14, 11, 12, 16);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5, 8, 0, 11, 12, 2);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14, 8, 5, 16, 12, 11);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 8, 5, 2, 12, 11);

    public WallLightBlock(@NotNull Settings settings)
    {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(FACE, WallMountLocation.WALL));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, FACE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        Direction direction = SearchlightUtil.getDirection(state);
        if (direction == Direction.UP)
            return state.get(FACING).getAxis() == Direction.Axis.X ? FLOOR_X_SHAPE : FLOOR_Z_SHAPE;
        else if (direction == Direction.DOWN)
            return state.get(FACING).getAxis() == Direction.Axis.X ? CEILING_X_SHAPE : CEILING_Z_SHAPE;
        else if (direction == Direction.EAST)
            return EAST_SHAPE;
        else if (direction == Direction.WEST)
            return WEST_SHAPE;
        else if (direction == Direction.SOUTH)
            return SOUTH_SHAPE;
        return NORTH_SHAPE;
    }
}
