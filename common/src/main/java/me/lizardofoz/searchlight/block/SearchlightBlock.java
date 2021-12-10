package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SearchlightBlock extends WallMountedBlock implements BlockEntityProvider
{
    public static final BooleanProperty POWERED = Properties.POWERED;

    protected static final VoxelShape CEILING_SHAPE = Block.createCuboidShape(3, 3, 3, 13, 16, 13);
    protected static final VoxelShape FLOOR_SHAPE = Block.createCuboidShape(3, 0, 3, 13, 13, 13);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(3, 3, 3, 13, 13, 16);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(3, 3, 0, 13, 13, 13);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(3, 3, 3, 16, 13, 13);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 3, 3, 13, 13, 13);

    //==============================
    //Block definition
    //==============================
    public SearchlightBlock(@NotNull Settings settings)
    {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(FACE, WallMountLocation.WALL)
                .with(POWERED, false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState blockState)
    {
        return new SearchlightBlockEntity(pos, blockState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, FACE, POWERED);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        //I know the switch operator exists, but it makes a mess in bytecode for some reason
        Direction direction = SearchlightUtil.getDirection(state);
        if (direction == Direction.UP)
            return FLOOR_SHAPE;
        else if (direction == Direction.DOWN)
            return CEILING_SHAPE;
        else if (direction == Direction.EAST)
            return EAST_SHAPE;
        else if (direction == Direction.WEST)
            return WEST_SHAPE;
        else if (direction == Direction.SOUTH)
            return SOUTH_SHAPE;
        return NORTH_SHAPE;
    }

    //==============================
    //Block overrides and functionality
    //==============================

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        boolean isPoweredNow = world.isReceivingRedstonePower(pos);
        boolean wasPoweredBefore = state.get(POWERED);
        if (!wasPoweredBefore && isPoweredNow)
            SearchlightUtil.castBlockEntity(world.getBlockEntity(pos), pos, SearchlightBlockEntity::turnOffLightSource);
        else if (wasPoweredBefore && !isPoweredNow)
            SearchlightUtil.castBlockEntity(world.getBlockEntity(pos), pos, SearchlightBlockEntity::turnOnLightSource);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world != null && !world.isClient)
            if (!updateSearchLight(world, pos, state, placer))
                updateSearchLight(world, pos, state, null);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
    {
        if (player == null || world == null)
            return ActionResult.FAIL;

        if (!world.isClient && updateSearchLight(world, pos, state, player))
            world.playSound(null, pos, SoundEvents.BLOCK_NETHERITE_BLOCK_PLACE, SoundCategory.BLOCKS, 1, 0.4f);
        return ActionResult.SUCCESS;
    }

    //Here we delete a Light Source when a Searchlight gets removed (can't be pushed by pistons)
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!world.isClient)
            SearchlightUtil.castBlockEntity(world.getBlockEntity(pos), pos, SearchlightBlockEntity::deleteLightSource);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    /**
     * @return TRUE if a new Light Source has been successfully placed
     */
    protected boolean updateSearchLight(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer)
    {
        boolean[] result = new boolean[1];

        SearchlightUtil.castBlockEntity(world.getBlockEntity(pos), pos, (SearchlightBlockEntity blockEntity) ->
                result[0] = blockEntity.raycastAndPlaceLightSource(placer != null
                        ? placer.getRotationVector().multiply(-1)
                        : SearchlightUtil.directionToBeamVector(SearchlightUtil.getDirection(state))));
        return result[0];
    }
}